package me.jack.LD56.entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import me.jack.LD56.render.RenderableObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public abstract class Entity implements RenderableObject {

    public float x,y;

    public long startTime;


    public Entity(float x,float y){
        this.x = x;
        this.y = y;
        this.startTime = System.currentTimeMillis();
    }
    public abstract void createBody(World world);

    public Color edgeColor = Color.WHITE, fillColor = Color.BLACK;
    public boolean dead = false;
    private Body body;
    public float entityArea = -1f;
    public static float calculatePolygonArea(Vector2[] vertices) {
        int n = vertices.length;
        float area = 0;

        for (int i = 0; i < n; i++) {
            Vector2 current = vertices[i];
            Vector2 next = vertices[(i + 1) % n];


            area += current.x * next.y - current.y * next.x;
        }

        return Math.abs(area) / 2.0f;
    }


    public static float fudge(float input){
        return input + ((new Random().nextFloat() * 2) - 1);
    }

    float lifeTime = 0;
    public void update(float deltaTime,World world) {
        lifeTime += deltaTime;
    }

    public void render(SpriteBatch batch, ShapeRenderer renderer) {

    }

    public void dispose() {
        body.getWorld().destroyBody(body);
    }

    public Body getBody(){
        return this.body;
    }

    public void setBody(Body body){
        body.setUserData(this);
        this.body = body;
    }

    @Override
    public Color getEdgeColor() {
        return this.edgeColor;
    }

    @Override
    public Color getFillColor() {
        return this.fillColor;
    }

    @Override
    public int getEdgeThickness() {
        return 1;
    }

    /**
     * Combines two float values (A and B) into a third value (Z) that simulates evolutionary principles.
     * Z is likely to be close to the average, but there's a chance that:
     * - If both A and B are large, Z can be boosted.
     * - If both A and B are small, Z can become even smaller.
     * - A small random mutation may also occur.
     *
     * @param A The first parent trait.
     * @param B The second parent trait.
     * @return A new trait value Z, a combination of A and B.
     */
    public static float combineTraits(float A, float B) {
        if(true){

            if(new Random().nextInt(2) == 0){
                return Math.max(A,B) * (2+new Random().nextFloat());
            }
            if(new Random().nextInt(3) == 0){
                return Math.min(A,B) * 0.75f;
            }
            if(new Random().nextBoolean())
                return A;
            else
                return B;
        }
        // Calculate the average
        float average = (A + B) / 2f;

        // Add a small random mutation to simulate genetic variation (mutation range [-0.1, 0.1])
        float mutation = MathUtils.random(-0.1f, 0.1f);

        // Bias towards extremes if both values are high or both are low
        float extremeBias = 0f;

        // If both A and B are large, boost Z slightly
        if (A > 0.7f && B > 0.7f) {
            extremeBias = MathUtils.random(0.05f, 0.2f); // Boost for large values
        }

        // If both A and B are small, decrease Z slightly
        if (A < 0.3f && B < 0.3f) {
            extremeBias = MathUtils.random(-0.2f, -0.05f); // Decrease for small values
        }

        // Combine the average, mutation, and extreme bias to get the final trait value Z
        float Z = average + mutation + extremeBias;

        // Ensure Z stays within a reasonable range (optional, adjust range as needed)
        Z = MathUtils.clamp(Z, 0f, 1f);

        return Z;
    }
}
