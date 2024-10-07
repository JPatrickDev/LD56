package me.jack.LD56.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import me.jack.LD56.Box2DHelper;
import me.jack.LD56.ColorUtils;
import me.jack.LD56.GameContactListener;
import me.jack.LD56.entity.*;
import me.jack.LD56.render.LD56AIOverlayRenderer;
import me.jack.LD56.render.LD56GameWorldRenderer;
import me.jack.LD56.ui.HUD;
import me.jack.LD56.ui.SliderUI;

import java.util.*;

public class GameScreen extends ScreenAdapter {
    private final float MIN_ZOOM =1f;       // Minimum zoom level (zoomed in)
    private final float MAX_ZOOM = 170f;       // Maximum zoom level (zoomed out)

    private static World world;
    private Box2DDebugRenderer debugRenderer;
    private LD56GameWorldRenderer gameWorldRenderer;

    private LD56AIOverlayRenderer aiOverlayRenderer;
    public static OrthographicCamera camera;
    private static List<Entity> entities;
    private SpriteBatch batch;
    private ShapeRenderer renderer;
    private OrthographicCamera hudCamera;
    private final float TIME_STEP = 1 / 60f;
    private final int VELOCITY_ITERATIONS = 6;
    private final int POSITION_ITERATIONS = 2;

    private HUD hud;

    private static List<Entity> toSpawn = new ArrayList<>();
    public static void spawn(Entity b) {
        toSpawn.add(b);
    }

    public static float dishRadius =1000f;


    static boolean shouldReset = false;
    public static void requestReset() {
        shouldReset = true;
    }

    private void createHollowCircularBorder(Vector2 center, float radius, int segments) {
        // Step 1: Define a static body for the boundary
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(center); // Set the center of the circular boundary

        // Step 2: Create the static body in the world
        Body boundaryBody = world.createBody(bodyDef);
        boundaryBody.setUserData(new EntityWorldBorder());
        // Step 3: Define the ChainShape to approximate the circle
        ChainShape chainShape = new ChainShape();

        // Step 4: Calculate the vertices for the circle approximation
        Vector2[] vertices = new Vector2[segments];
        float angleStep = (float)(2 * Math.PI / segments);
        for (int i = 0; i < segments; i++) {
            float angle = i * angleStep;
            float x = center.x + radius * (float) Math.cos(angle);
            float y = center.y + radius * (float) Math.sin(angle);
            vertices[i] = new Vector2(x - center.x, y - center.y);  // Relative to the body's position
        }

        // Step 5: Set the vertices as a loop in the ChainShape
        chainShape.createLoop(vertices);

        // Step 6: Define a fixture for the chain shape
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = chainShape;
        fixtureDef.density = 0.0f;       // Static bodies don't need density
        fixtureDef.restitution = 1.0f;   // Set restitution for bounciness
        fixtureDef.friction = 0.0f;      // Friction for sliding control

        // Step 7: Attach the fixture to the body
        boundaryBody.createFixture(fixtureDef);

        // Step 8: Dispose of the shape after it is used
        chainShape.dispose();
    }
    private void createCircularBorder(Vector2 center, float radius) {
       createHollowCircularBorder(center,radius,50);
    }
    // Variables for panning
    private float lastX, lastY;
    private boolean isPanning = false;


    public static SliderUI ui;

    private void handleInput(float delta) {
        if (ui.isMouseInsideUI())
            return;

        // Panning with left mouse button
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!isPanning) {
                isPanning = true;
                lastX = Gdx.input.getX();
                lastY = Gdx.input.getY();
            } else {
                float deltaX = lastX - Gdx.input.getX();
                float deltaY = lastY - Gdx.input.getY();

                // Set target camera position based on panning
                targetCameraPosition.add((deltaX * camera.zoom) / 4f, (-deltaY * camera.zoom) / 4f,0);

                lastX = Gdx.input.getX();
                lastY = Gdx.input.getY();
            }
        } else {
            // Stop panning when mouse button is released
            isPanning = false;
        }

        // Smoothly move the camera towards the target position
        camera.position.lerp(targetCameraPosition, panSpeed * delta);

        // Ensure the camera properties (position, zoom) are applied
        camera.update();
    }
    private Vector3 targetCameraPosition = new Vector3();
    private float panSpeed = 5f; // Adjust for smoother panning



    public static Vector2 getRandomPointInsideArea(){

        Random random = new Random();

        // Generate a random angle between 0 and 2π
        double angle = random.nextDouble() * 2 * Math.PI;

        // Generate a random radius that is uniformly distributed within the circle
        double randomRadius = (dishRadius-150) * Math.sqrt(random.nextDouble());

        // Convert polar coordinates to Cartesian coordinates
        double x = randomRadius * Math.cos(angle);
        double y = randomRadius * Math.sin(angle);

        // Return the (x, y) coordinates as an array
        return new Vector2((float) x, (float) y);
    } private boolean isZooming = false;

    @Override
    public void show() {

        ui = new SliderUI();

        InputMultiplexer inputMultiplexer = new InputMultiplexer();

        // Add the Stage's input processor from the slider UI first
        inputMultiplexer.addProcessor(ui.getStage());

        // Add your custom input processor
        inputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                isZooming = true;

                float zoomAmount = 5f; // Adjust this value to control zoom sensitivity
                float targetZoom = camera.zoom;

                // Calculate the target zoom based on scroll input
                if (amountY > 0) {
                    targetZoom += zoomAmount; // Zoom out
                } else if (amountY < 0) {
                    targetZoom -= zoomAmount; // Zoom in
                }

                // Clamp the target zoom level
                targetZoom = MathUtils.clamp(targetZoom, MIN_ZOOM, MAX_ZOOM);

                // Get the mouse position in screen coordinates
                Vector3 mouseScreenPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

                // Get the mouse world coordinates before zooming
                Vector3 mouseWorldBeforeZoom = camera.unproject(mouseScreenPos.cpy());

                // Apply the new zoom level
                camera.zoom = targetZoom;
                camera.update();

                // Get the mouse world coordinates after zooming
                Vector3 mouseWorldAfterZoom = camera.unproject(mouseScreenPos);

                // Calculate the difference and adjust the camera position
                Vector3 diff = mouseWorldBeforeZoom.sub(mouseWorldAfterZoom);
                camera.position.add(diff);

                // Update target position to match current position
                targetCameraPosition.set(camera.position);

                isZooming = false;
                return true;
            }
        });

        // Set the InputMultiplexer as the current input processor
        Gdx.input.setInputProcessor(inputMultiplexer);

        hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        hudCamera.update();
        world = new World(new Vector2(0, 0), true); // No gravity, top-down game
        debugRenderer = new Box2DDebugRenderer();

        camera = new OrthographicCamera(20, 20 * (Gdx.graphics.getHeight() / (float) Gdx.graphics.getWidth()));

        batch = new SpriteBatch();
        batch.enableBlending();
        entities = new ArrayList<>();
        renderer = new ShapeRenderer();
        renderer.setAutoShapeType(true);






        world.setContactListener(new GameContactListener());

        gameWorldRenderer= new LD56GameWorldRenderer(world,camera);
        aiOverlayRenderer = new LD56AIOverlayRenderer(world,camera);
        hud = new HUD(0,0,Gdx.graphics.getWidth(),100,this);

        createCircularBorder(new Vector2(0,0),dishRadius+30f);

        int velocityIterations = 6;  // Standard velocity iterations
        int positionIterations = 2;  // Standard position iterations

        world.step(100f, velocityIterations, positionIterations);
        camera.zoom = 120;


        resetGameWorld();
    }


    public static void resetGameWorld(){
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        for (Body body : bodies) {
            if(body.getUserData() instanceof Entity) {
                world.destroyBody(body);
                entities.remove(body.getUserData());
            }
        }
        entities.clear();
        // Create some entities
        for (int i = 0; i <1000; i++) {
            Entity entity = new EntityFloatingFood( getRandomPointInsideArea(), new Random().nextFloat()+0.1f);
            entity.createBody(world);
            entities.add(entity);
        }

        for (int i = 0; i <6; i++) {
            Color edge = ColorUtils.getRandomColor();
            Color fill = ColorUtils.getRandomColor();
            int Jmax = new Random().nextInt(15)  + 5;
            Vector2 startPoint = getRandomPointInsideArea();
            float r = (float) (new Random().nextInt(5) + 0.25);
            boolean hasTail = new Random().nextInt(15) == 0;

            float eyeSize = new Random().nextFloat(3) + 1;

            for(int j = 0; j != Jmax;j++) {
                EntityBacteria entity = new EntityBacteria(Box2DHelper.getRandomPointInCircle(startPoint.x,startPoint.y,50f),r + new Random().nextFloat(), new Random().nextInt(5)+1);
                entity.edgeColor = edge;
                entity.fillColor = fill;
                entity.hasTail = hasTail;
                entity.eyeSize = eyeSize;
                entity.createBody(world);

                entities.add(entity);
            }
        }



        // Create some entities
        for (int i = 0; i <200; i++) {
            Entity entity = new EntityFloatingFood( getRandomPointInsideArea(),new Random().nextInt(5) + 1);
            entity.createBody(world);
            entities.add(entity);
        }
    }


    public static float[] getRandomPointInCircles(float centerX, float centerY, float innerRadius, float outerRadius) {
        Random random = new Random();

        // Step 1: Generate a random radius between innerRadius and outerRadius
        // We use sqrt to ensure uniform distribution across the area
        float randomRadius = (float) Math.sqrt(random.nextFloat() * (outerRadius * outerRadius - innerRadius * innerRadius) + innerRadius * innerRadius);

        // Step 2: Generate a random angle (0 to 2*PI radians)
        float randomAngle = (float) (random.nextFloat() * 2 * Math.PI);

        // Step 3: Convert polar coordinates (randomRadius, randomAngle) to Cartesian coordinates
        float randomX = centerX + randomRadius * (float) Math.cos(randomAngle);
        float randomY = centerY + randomRadius * (float) Math.sin(randomAngle);

        // Return the result as an array [x, y]
        return new float[]{randomX, randomY};
    }

    @Override
    public void render(float delta) {
        // Step the Box2D world
       // world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
// Normal and fast speeds
        float normalSpeed = 1/60f;  // 60 FPS
        float fastSpeed = normalSpeed * 50;  // 10x speed (or use any factor)

// This will be updated dynamically based on input
        float timeStep = normalSpeed;

// Check for button press to toggle speed
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {  // You can use any input condition
            timeStep = fastSpeed;  // Run at 10x speed
        }else if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {  // You can use any input condition
            timeStep = fastSpeed/2;  // Run at 10x speed
        } else {
            timeStep = normalSpeed;  // Run at normal speed
        }

// Simulate the world using the selected time step
        int velocityIterations = 6;  // Standard velocity iterations
        int positionIterations = 2;  // Standard position iterations

        world.step(timeStep, velocityIterations, positionIterations);


        // Handle zoom input
        handleInput(delta);

        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update the camera position to follow the player
      //  Vector2 playerPosition = playerEntity.getBody().getPosition();
        boundCamera();
        camera.update(); // Ensure the camera properties (position, zoom) are applied

        // Render the Box2D world using Box2DDebugRenderer
        debugRenderer.render(world, camera.combined);
        gameWorldRenderer.render(delta);
        if(Gdx.input.isKeyPressed(Input.Keys.F1))
        aiOverlayRenderer.render(delta,entities);
        // Update the projection matrix for SpriteBatch and ShapeRenderer
        batch.setProjectionMatrix(camera.combined);
        renderer.setProjectionMatrix(camera.combined);

        // Render entities
        batch.begin();
        renderer.begin();
        List<Entity> toRemove = new ArrayList<>();
        for (Entity entity : entities) {
            entity.update(delta,world);
            entity.render(batch, renderer);
            if (entity.dead) {
                toRemove.add(entity);
            }
        }
        for (Entity e : toRemove) {
            world.destroyBody(e.getBody());
            Array<Body> found = new Array<Body>();
            world.getBodies(found);
            for(Body b : found){
                if(b.getUserData() == e)
                    world.destroyBody(b);
            }
            entities.remove(e);

            if(!(e instanceof EntityFloatingFood))
                spawnedCount--;
        }

      //  drawHUD();
        batch.end();
        renderer.end();


        ui.render();

        System.out.println(GameScreen.ui.getCurrentValue("Food Spawn Rate"));
        if(new Random().nextFloat() <= (GameScreen.ui.getCurrentValue("Food Spawn Rate") /100.0f) * 0.25) {
            int tries = (int) (delta);
            tries+=2;
            for (int tI = 0; tI != tries; tI++) {
                Entity entity = new EntityFloatingFood(getRandomPointInsideArea(), (float) (new Random().nextInt(15) + 0.1));
                spawn(entity);
            }
            if(new Random().nextFloat() <= 0.25) {
                Entity entity = new EntityFloatingFood(getRandomPointInsideArea(), (float) (new Random().nextInt(150) + 20));
                spawn(entity);
            }
            //
        }
        Iterator<Entity> spawnIt = toSpawn.iterator();
        while(spawnIt.hasNext()){
            Entity e = spawnIt.next();
            float r = 10f;
            String info = "";
            if(e instanceof EntityFloatingFood){
                r= (((EntityFloatingFood) e).radius) * 5f;
              //  r = 100;
                info = " : " + ((EntityFloatingFood) e).radius;
            }
          //  if(Box2DHelper.isPositionFree(world,e.x,e.y,r)) {
           //     System.out.println("Spawning " + e.getClass().getCanonicalName() + info);
                e.createBody(world);
                entities.add(e);
                spawnIt.remove();
                spawnedCount++;
           // }else{
           //     if(e instanceof EntityFloatingFood){

             //   }else{
              //  }

          //  }

        }

        if(shouldReset){
            shouldReset = false;
            resetGameWorld();
        }

    }

    private void boundCamera() {
        System.out.println("Camera" + camera.zoom);
        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        // Calculate the boundaries
        float minX = effectiveViewportWidth / 2f - dishRadius*2;
        float maxX = dishRadius*2 - effectiveViewportWidth / 2f;
        float minY = effectiveViewportHeight / 2f - dishRadius*2;
        float maxY =  dishRadius*2 - effectiveViewportHeight / 2f;

        // Clamp the camera position
        camera.position.x = MathUtils.clamp(camera.position.x, minX, maxX);
        camera.position.y = MathUtils.clamp(camera.position.y, minY, maxY);
    }

    public static int countForType(Color edge,Color fill){
        int c = 0;
        Iterator<Entity> e = entities.iterator();
        while(e.hasNext()){
            Entity n = e.next();
            if(n instanceof EntityBacteria){
                if(((EntityBacteria) n).edgeColor == edge && ((EntityBacteria) n).fillColor == fill){
                    c++;
                }
            }
        }
        return c;
    }

    public static int getCurrentTypeCount(){
        Set<String> vals = new HashSet<>();
        Iterator<Entity> e = entities.iterator();
        while(e.hasNext()){
            Entity n = e.next();
            if(n instanceof EntityBacteria){
               vals.add(n.edgeColor + ":" + n.fillColor);
            }
        }
        return vals.size();
    }
    public int spawnedCount = 0;
    private void drawHUD() {
        // Use the HUD camera for drawing the HUD elements
        batch.setProjectionMatrix(hudCamera.combined);
        renderer.setProjectionMatrix(hudCamera.combined);
        hud.render(batch,renderer);
    }

    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
        batch.dispose();
        renderer.dispose();
        for (Entity entity : entities) {
            // entity.dispose();
        }
    }
}
