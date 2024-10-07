package me.jack.LD56.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

public class SmoothedPolygonRenderer {

    private CatmullRomSpline<Vector2> spline;
    private Vector2[] controlPoints;
    private Vector2[] smoothedPoints;

    public SmoothedPolygonRenderer(Polygon polygon) {
        float[] vertices = polygon.getVertices();
        controlPoints = new Vector2[vertices.length / 2 + 1];

        // Convert vertices to control points (for Catmull-Rom spline)
        for (int i = 0; i < vertices.length / 2; i++) {
            controlPoints[i] = new Vector2(vertices[i * 2], vertices[i * 2 + 1]);
        }

        // Add the first point at the end to close the loop (wrap around)
        controlPoints[controlPoints.length - 1] = controlPoints[0];

        spline = new CatmullRomSpline<>(controlPoints, true);

        // Generate smoothed points
        int numSmoothedPoints = 100;  // More points for smoother shape
        smoothedPoints = new Vector2[numSmoothedPoints];
        for (int i = 0; i < numSmoothedPoints; i++) {
            smoothedPoints[i] = new Vector2();
            spline.valueAt(smoothedPoints[i], (float) i / (numSmoothedPoints - 1));  // Interpolate along the spline
        }
    }

    // Render method now takes x and y position to offset the polygon
    public void render(ShapeRenderer shapeRenderer, float x, float y) {

        for (int i = 0; i < smoothedPoints.length - 1; i++) {
            // Offset each point by the given x and y position
            shapeRenderer.line(smoothedPoints[i].x + x, smoothedPoints[i].y + y,
                smoothedPoints[i + 1].x + x, smoothedPoints[i + 1].y + y);
        }

        // Close the loop by connecting the last point to the first, with the offset
        shapeRenderer.line(smoothedPoints[smoothedPoints.length - 1].x + x,
            smoothedPoints[smoothedPoints.length - 1].y + y,
            smoothedPoints[0].x + x, smoothedPoints[0].y + y);

    }
}
