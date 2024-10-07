package me.jack.LD56.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import me.jack.LD56.ai.MoveToMateAI;
import me.jack.LD56.entity.Entity;
import me.jack.LD56.entity.EntityBacteria;

import java.util.List;

public class LD56AIOverlayRenderer {

    private ShapeRenderer shapeRenderer;
    private World world;
    private OrthographicCamera camera;
    private Array<Body> bodies;  // Array to store bodies

    public LD56AIOverlayRenderer(World world, OrthographicCamera camera) {
        this.world = world;
        this.camera = camera;
        this.bodies = new Array<>(); // Initialize the Array<Body>
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
    }
    public void drawCross( float x, float y, float size) {
        // Draw the two diagonal lines that form the X
        shapeRenderer.line(x - size / 2, y - size / 2, x + size / 2, y + size / 2);  // First diagonal
        shapeRenderer.line(x - size / 2, y + size / 2, x + size / 2, y - size / 2);  // Second diagonal
    }

    public void render(float delta, List<Entity> toDraw) {
        // Set the projection matrix from the camera
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Clear the array and populate it with the bodies from the world
        bodies.clear();
        world.getBodies(bodies);
        shapeRenderer.begin();
        for(Entity e : toDraw){
            if(e instanceof EntityBacteria){
                if(((EntityBacteria) e).getTarget() != null){
                    drawCross(((EntityBacteria) e).getTarget().x,((EntityBacteria) e).getTarget().y,2);
                    if(((EntityBacteria) e).currentBehaviour instanceof MoveToMateAI){
                        shapeRenderer.setColor(Color.PINK);
                    }else{
                        shapeRenderer.setColor(Color.WHITE);
                    }
                    shapeRenderer.line(e.getBody().getPosition(),((EntityBacteria) e).getTarget());
                    shapeRenderer.circle(((EntityBacteria) e).getTarget().x,((EntityBacteria) e).getTarget().y, ((EntityBacteria) e).targetProximityThreshold);
                    shapeRenderer.circle(((EntityBacteria) e).getBody().getPosition().x,((EntityBacteria) e).getBody().getPosition().y, ((EntityBacteria) e).eyeSize * 60);

                }
            }

        }
        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
