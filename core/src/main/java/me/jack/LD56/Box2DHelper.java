package me.jack.LD56;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class Box2DHelper {

    public static float calculateDistanceBetweenBodies(Body bodyA, Body bodyB) {
        Vector2 positionA = bodyA.getPosition();
        Vector2 positionB = bodyB.getPosition();

        // Use the dst method of Vector2 to calculate the distance between two points
        return positionA.dst(positionB);
    }

    public static void findBodiesInCircle(World world, final Vector2 center, final float radius, Array<Body> foundBodies) {
        // Clear the array to make sure it's empty before populating it
        foundBodies.clear();

        // Calculate the AABB bounds
        float lowerX = center.x - radius;
        float lowerY = center.y - radius;
        float upperX = center.x + radius;
        float upperY = center.y + radius;

        // Create a QueryCallback that will filter bodies by checking if they are inside the circle
        QueryCallback callback = new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                Body body = fixture.getBody();
                Vector2 bodyPosition = body.getPosition();

                // Check if the body's position is within the circle
                if (bodyPosition.dst2(center) <= radius * radius) {
                    foundBodies.add(body);  // Add to the list of found bodies
                }

                return true;  // Continue querying
            }
        };

        // Query the AABB directly with the lower and upper bounds
        world.QueryAABB(callback, lowerX, lowerY, upperX, upperY);
    }


    public static void attract(Body bodyA, Body bodyB) {
        // Get the positions of both bodies
        Vector2 positionA = bodyA.getPosition();
        Vector2 positionB = bodyB.getPosition();

        // Calculate the direction of the force from bodyA to bodyB
        Vector2 direction = new Vector2(positionB).sub(positionA);

        // Calculate the distance between the two bodies
        float distance = direction.len();

        // Normalize the direction vector (get the unit vector)
        direction.nor();

        // Define a constant for the strength of the attraction (you can adjust this)
        float strength = 1000f;

        // Calculate the force magnitude (inverse square law or any proportional factor)
        // For inverse square law: F = strength / (distance^2)
        float forceMagnitude = strength / (distance * distance);

        // Calculate the force vector by scaling the direction
        Vector2 force = direction.scl(forceMagnitude);

        // Apply the force to bodyA (toward bodyB)
        bodyA.applyForceToCenter(force, true);

        // Apply the opposite force to bodyB (toward bodyA)
        bodyB.applyForceToCenter(force.scl(-1), true);  // Reverse the direction for bodyB
    }
        /**
         * Checks if a given x, y position and radius is free of bodies in the Box2D world.
         *
         * @param world   The Box2D world to query.
         * @param x       The x position to check.
         * @param y       The y position to check.
         * @param radius  The radius around the point to check for bodies.
         * @return True if the position is free of bodies, false otherwise.
         */
        public static boolean isPositionFree(World world, float x, float y, float radius) {
            Vector2 center = new Vector2(x, y);
            Array<Body> nearbyBodies = new Array<>();

            // Define the AABB (axis-aligned bounding box) for the query
            Vector2 lowerBound = new Vector2(x - radius, y - radius);
            Vector2 upperBound = new Vector2(x + radius, y + radius);

            // Create a QueryCallback to gather any bodies in the AABB
            QueryCallback callback = new QueryCallback() {
                @Override
                public boolean reportFixture(Fixture fixture) {
                    Body body = fixture.getBody();
                    // Add body to the list of nearby bodies
                    nearbyBodies.add(body);
                    return true;  // Continue querying
                }
            };

            // Query the Box2D world in the defined AABB
            world.QueryAABB(callback, lowerBound.x, lowerBound.y, upperBound.x, upperBound.y);

            // Check if any of the bodies are within the given radius
            for (Body body : nearbyBodies) {
                Vector2 bodyPosition = body.getPosition();
                if (bodyPosition.dst2(center) <= radius * radius) {
                    // A body is within the radius
                    return false;
                }
            }

            // No bodies were found in the specified radius
            return true;
        }


    public static Vector2 getRandomPointInCircle(double cx, double cy, double radius) {

        // Generate a random angle between 0 and 2π
        double angle = new Random().nextDouble() * 2 * Math.PI;

        // Generate a random radius that is uniformly distributed within the circle
        double randomRadius = radius * Math.sqrt(new Random().nextDouble());

        // Convert polar coordinates to Cartesian coordinates
        double x = cx + randomRadius * Math.cos(angle);
        double y = cy + randomRadius * Math.sin(angle);

        // Return the (x, y) coordinates as an array
        return new Vector2((float) x, (float) y);
    }
    }


