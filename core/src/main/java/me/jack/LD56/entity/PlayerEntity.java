package me.jack.LD56.entity;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import me.jack.LD56.render.RenderableObject;
import me.jack.LD56.screen.GameScreen;

public class PlayerEntity extends InputAdapter implements RenderableObject {

    private Body body;
    private World world;
    private OrthographicCamera camera;
    private float width = 1f; // Width of the rounded rectangle
    private float height = 0.5f; // Height of the rounded rectangle
    private float speed = 2f; // Speed of the player when moving towards the target

    public float hunger = 100f;
    public float exp = 0;
    public int level;

    public float bodyArea = -1;

    public float x, y;

    public PlayerEntity(World world, OrthographicCamera camera, float x, float y) {
        this.world = world;
        this.camera = camera;
        // createBody(x, y);
        this.x = x;
        this.y = y;
        // Register this class to handle input events
        Gdx.input.setInputProcessor(this);
    }




    // Handle mouse click input
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 worldClickPosition = getMouseWorldPosition(screenX, screenY);
        propelTowards(worldClickPosition);
        return true;
    }

    // Convert the screen coordinates to world coordinates using the camera
    private Vector2 getMouseWorldPosition(int screenX, int screenY) {
        Vector3 screenPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(screenPosition); // Convert screen coordinates to world coordinates
        return new Vector2(screenPosition.x, screenPosition.y);
    }

    // Propels the player towards the target position using an impulse
    private void propelTowards(Vector2 targetPosition) {
        Vector2 direction = targetPosition.sub(new Vector2(x,y)).nor();
        x += direction.x;
        y += direction.y;
      //  body.applyLinearImpulse(direction.scl(speed), currentPosition, true);
    }

    public void update(float deltaTime) {
        hunger -= hungerRate;
        speed = 2 * GameScreen.camera.zoom;
    }

    public float hungerRate = 0.01f;

    public void render(SpriteBatch batch) {
        // Render entity (you can add sprites or shapes here)
        // For simplicity, debug rendering with Box2DDebugRenderer handles rendering
    }

    public void dispose() {
        body.getWorld().destroyBody(body);
    }



    public float expForNextLevel() {
        return level * 5;
    }

    public Body getBody() {
        return body;
    }

    @Override
    public Color getEdgeColor() {
        return Color.WHITE;
    }

    @Override
    public Color getFillColor() {
        return Color.BLACK;
    }

    @Override
    public int getEdgeThickness() {
        return 2;
    }
}
