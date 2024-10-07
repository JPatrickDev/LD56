package me.jack.LD56.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class EntityFloatingFood extends Entity{


    public float radius;
    public float x,y;

    public int splitCount = 0;
    public EntityFloatingFood(Vector2 pos,float radius){
        this(pos.x,pos.y,radius);
    }
    public EntityFloatingFood(float x, float y, float radius) {
        super(x,y);
        if(radius < 0.2)
            radius = 0.2F;
        this.edgeColor = Color.GREEN;
        this.fillColor = Color.valueOf("3bad4c");
        this.radius = radius;
        this.x = x;
        this.y = y;
    }

    private Vector2[] generateRandomPolygonVertices(int numVertices, float radius) {
        ArrayList<Vector2> points = new ArrayList<>();

        for (int i = 0; i < numVertices; i++) {
            float angle = MathUtils.random(0, MathUtils.PI2);
            float distance = MathUtils.random(0.5f, 1f) * radius;
            float x = MathUtils.cos(angle) * distance;
            float y = MathUtils.sin(angle) * distance;
            points.add(new Vector2(x, y));
        }


        final Vector2 center = new Vector2(0, 0);
        Collections.sort(points, new Comparator<Vector2>() {
            @Override
            public int compare(Vector2 p1, Vector2 p2) {
                float angle1 = (float) Math.atan2(p1.y - center.y, p1.x - center.x);
                float angle2 = (float) Math.atan2(p2.y - center.y, p2.x - center.x);
                return Float.compare(angle1, angle2);
            }
        });


        Vector2[] vertices = new Vector2[numVertices];
        for (int i = 0; i < numVertices; i++) {
            vertices[i] = points.get(i);
        }

        return vertices;
    }

    @Override
    public void createBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);

        Body body = world.createBody(bodyDef);


        Vector2[] vertices = generateRandomPolygonVertices(8, radius);


        PolygonShape shape = new PolygonShape();
        shape.set(vertices);


        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.5f;
        fixtureDef.friction = 1f;
        fixtureDef.restitution = 0.5f;

        body.createFixture(fixtureDef);
        body.setUserData(this);
        shape.dispose();


        applyRandomKick(body,400f * radius);
        entityArea = calculatePolygonArea(vertices);

        this.setBody(body);
    }

    private void applyRandomKick(Body body, float kickStrength) {
        // Step 1: Generate a random angle between 0 and 2 * PI
        float randomAngle = MathUtils.random(0, 2 * MathUtils.PI);

        // Step 2: Calculate the x and y components of the impulse
        float xImpulse = kickStrength * MathUtils.cos(randomAngle);
        float yImpulse = kickStrength * MathUtils.sin(randomAngle);

        // Step 3: Apply the impulse to the body's center of mass
        body.applyLinearImpulse(new Vector2(xImpulse, yImpulse), body.getWorldCenter(), true);
    }
}
