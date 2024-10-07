package me.jack.LD56.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import me.jack.LD56.entity.Entity;
import com.badlogic.gdx.Gdx; // Import to use OpenGL commands

public class LD56GameWorldRenderer {

    private ShapeRenderer shapeRenderer;
    private World world;
    private OrthographicCamera camera;
    private Array<Body> bodies;  // Array to store bodies

    public LD56GameWorldRenderer(World world, OrthographicCamera camera) {
        this.world = world;
        this.camera = camera;
        this.bodies = new Array<>(); // Initialize the Array<Body>
        shapeRenderer = new ShapeRenderer();
    }

    public void render(float delta) {
        // Set the projection matrix from the camera
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Clear the array and populate it with the bodies from the world
        bodies.clear();
        world.getBodies(bodies);

        // Iterate through all the bodies in the array
        for (Body body : bodies) {
            for (Fixture fixture : body.getFixtureList()) {
                renderFixture(fixture, body.getPosition(), body.getAngle(), body);
            }
        }
    }

    private void renderFixture(Fixture fixture, Vector2 bodyPosition, float bodyAngle, Body parentBody) {
        Shape shape = fixture.getShape();

        switch (shape.getType()) {
            case Circle:
                renderCircle((CircleShape) shape, bodyPosition, bodyAngle, parentBody);
                break;
            case Polygon:
                renderPolygon((PolygonShape) shape, bodyPosition, bodyAngle, parentBody);
                break;
            case Edge:
                renderEdge((EdgeShape) shape, bodyPosition, bodyAngle, parentBody);
                break;
            default:
                // Other shapes can be added here if necessary
                break;
        }
    }

    private void renderCircle(CircleShape circleShape, Vector2 bodyPosition, float bodyAngle, Body parent) {
        float radius = circleShape.getRadius();
        Vector2 localCenter = circleShape.getPosition();  // Local position of the circle (relative to the body)

        // Create a new vector for the world position
        Vector2 worldCenter = new Vector2(localCenter).rotateRad(bodyAngle).add(bodyPosition);

        RenderableObject parentEntity = (RenderableObject) parent.getUserData();

        // Check if the fill color is provided
        Color fillColor = parentEntity.getFillColor();
        if (fillColor != null) {
            // Draw filled circle
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(fillColor);
            shapeRenderer.circle(worldCenter.x, worldCenter.y, radius, 30);
            shapeRenderer.end();
        }

        // Draw outline circle with thickness
        float edgeThickness = parentEntity.getEdgeThickness();
        Gdx.gl.glLineWidth(edgeThickness);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(parentEntity.getEdgeColor());
        shapeRenderer.circle(worldCenter.x, worldCenter.y, radius, 30);

        // Optional: draw a line to show the body's angle
        Vector2 angleLineEnd = new Vector2(radius, 0).rotateRad(bodyAngle).add(worldCenter);
        shapeRenderer.line(worldCenter, angleLineEnd);
        shapeRenderer.end();

        // Reset the line width to default
        Gdx.gl.glLineWidth(1f);
    }

    private void renderPolygon(PolygonShape polygonShape, Vector2 bodyPosition, float bodyAngle, Body parent) {
        int vertexCount = polygonShape.getVertexCount();
        Vector2[] vertices = new Vector2[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            vertices[i] = new Vector2();
            polygonShape.getVertex(i, vertices[i]);
            vertices[i].rotateRad(bodyAngle).add(bodyPosition); // Apply body angle and position
        }

        RenderableObject parentEntity = (RenderableObject) parent.getUserData();

        // Check if the fill color is provided
        Color fillColor = parentEntity.getFillColor();
        if (fillColor != null) {
            // Draw filled polygon
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(fillColor);
            for (int i = 1; i < vertexCount - 1; i++) {
                shapeRenderer.triangle(vertices[0].x, vertices[0].y, vertices[i].x, vertices[i].y, vertices[i + 1].x, vertices[i + 1].y);
            }
            shapeRenderer.end();
        }

        // Draw outline polygon with thickness
        float edgeThickness = parentEntity.getEdgeThickness();
        Gdx.gl.glLineWidth(edgeThickness);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(parentEntity.getEdgeColor());
        for (int i = 0; i < vertexCount; i++) {
            Vector2 v1 = vertices[i];
            Vector2 v2 = vertices[(i + 1) % vertexCount];
            shapeRenderer.line(v1.x, v1.y, v2.x, v2.y);
        }
        shapeRenderer.end();

        // Reset the line width to default
        Gdx.gl.glLineWidth(1f);
    }

    private void renderEdge(EdgeShape edgeShape, Vector2 bodyPosition, float bodyAngle, Body parent) {
        Vector2 v1 = new Vector2();
        Vector2 v2 = new Vector2();
        edgeShape.getVertex1(v1);
        edgeShape.getVertex2(v2);

        v1.rotateRad(bodyAngle).add(bodyPosition);
        v2.rotateRad(bodyAngle).add(bodyPosition);

        RenderableObject parentEntity = (RenderableObject) parent.getUserData();
        shapeRenderer.setColor(parentEntity.getEdgeColor());

        // Set the edge thickness
        float edgeThickness = parentEntity.getEdgeThickness();
        Gdx.gl.glLineWidth(edgeThickness);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.line(v1.x, v1.y, v2.x, v2.y);
        shapeRenderer.end();

        // Reset the line width to default
        Gdx.gl.glLineWidth(1f);
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
