package me.jack.LD56;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import me.jack.LD56.entity.*;
import me.jack.LD56.screen.GameScreen;

import java.util.Random;

import static me.jack.LD56.screen.GameScreen.getCurrentTypeCount;
import static me.jack.LD56.screen.GameScreen.getRandomPointInCircles;

public class GameContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        // Get the fixtures involved in the collision
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        // Get the bodies involved in the collision
        Body bodyA = fixtureA.getBody();
        Body bodyB = fixtureB.getBody();

        // Get the user data from the bodies (assuming it's your entity class)
        Object userDataA = bodyA.getUserData();
        Object userDataB = bodyB.getUserData();


        if (userDataA == userDataB)
            return;
        if (userDataA instanceof EntityBacteria || userDataB instanceof EntityBacteria) {
            if (userDataA instanceof EntityBacteria) {
                if (userDataB instanceof EntityFloatingFood) {
                    ((EntityBacteria) userDataA).onCollision((EntityFloatingFood) userDataB);
                } else if (userDataB instanceof EntityBacteria) {
                    if (((EntityBacteria) userDataA).getFillColor() == ((EntityBacteria) userDataB).getFillColor() &&
                        ((EntityBacteria) userDataA).getEdgeColor() == ((EntityBacteria) userDataB).getEdgeColor()) {
                        if(((EntityBacteria) userDataA).onMateCooldown || ((EntityBacteria) userDataB).onMateCooldown){
                            return;
                        }
                        if(GameScreen.countForType(((EntityBacteria) userDataA).edgeColor,((EntityBacteria) userDataA).fillColor) > 20)
                            return;
                        int j =  new Random().nextInt(4) + 1;
                        Vector2 startPoint = ((EntityBacteria) userDataA).getBody().getPosition();
                        for(int i = 0; i !=j;i++){
                            //  if (new Random().nextInt(500) == 0) {
                           // ((EntityBacteria) userDataA).hunger -= ((EntityBacteria) userDataA).maxhunger / 10.0f;
                           // ((EntityBacteria) userDataB).hunger -= ((EntityBacteria) userDataB).maxhunger/10.0f;
                            boolean newType = false;
                            Color newFill = ColorUtils.getRandomColor(),newEdge = ColorUtils.getRandomColor();

                            boolean hasTail = ((EntityBacteria) userDataB).hasTail;;
                            if (new Random().nextInt((int) (50 * (GameScreen.ui.getCurrentValue("Mutation Rate") /100.0f)) + 1) == 0) {
                                hasTail = !hasTail;
                            }
                            int tailSegments = ((EntityBacteria) userDataB).NUM_TAIL_SEGMENTS;;
                            if (new Random().nextInt((int) (50 * (GameScreen.ui.getCurrentValue("Mutation Rate") /100.0f)) + 1) == 0) {
                                tailSegments += new Random().nextInt(5) - 2;
                            }

                            boolean createsPoison = ((EntityBacteria) userDataB).createsPoison;
                            boolean hasResistance = ((EntityBacteria) userDataB).hasResistance;
                            if (new Random().nextInt((int) (30 * (GameScreen.ui.getCurrentValue("Mutation Rate") /100.0f)) + 1) == 0) {
                                createsPoison = !createsPoison;
                            }
                            if (new Random().nextInt((int) (50 * (GameScreen.ui.getCurrentValue("Mutation Rate") /100.0f)) + 1) == 0) {
                                hasResistance = !hasResistance;
                            }

                            boolean hasEyes = ((EntityBacteria) userDataB).hasEyes;
                            float eyeSize = ((EntityBacteria) userDataB).eyeSize;
                            if (new Random().nextInt((int) (70 * (GameScreen.ui.getCurrentValue("Mutation Rate") /100.0f)) + 1) == 0) {
                                hasEyes = !hasEyes;
                                eyeSize+= new Random().nextInt(6) - 1;
                                if(eyeSize < 2f)
                                    eyeSize = 2f;
                            }

                            if(new Random().nextInt((int) (500 * (GameScreen.ui.getCurrentValue("Mutation Rate") /100.0f)) + 1) == 0
                                && (hasTail != ((EntityBacteria) userDataB).hasTail
                                || hasResistance != ((EntityBacteria) userDataB).hasResistance
                                || createsPoison != ((EntityBacteria) userDataB).createsPoison ||
                                hasEyes != ((EntityBacteria) userDataB).hasEyes) && getCurrentTypeCount() <= 8){
                                newType= true;
                                System.out.println("New type being created");
                            }
                            int count = 1;
                            if(newType){
                                count = 3 + new Random().nextInt(3);
                            }
                            for(int k = 0; k != count; k++) {
                                Vector2 pos = Box2DHelper.getRandomPointInCircle(startPoint.x, startPoint.y, 5f);
                                EntityBacteria entity = new EntityBacteria((int) pos.x, (int) pos.y, Entity.combineTraits(((EntityBacteria) userDataA).getScale(), ((EntityBacteria) userDataB).getScale()), tailSegments);
                                entity.edgeColor = ((EntityBacteria) userDataA).getEdgeColor();
                                entity.fillColor = ((EntityBacteria) userDataA).getFillColor();
                                entity.hasTail = hasTail;
                                entity.createsPoison = createsPoison;
                                entity.hasResistance = hasResistance;
                                entity.hasEyes = hasEyes;
                                entity.eyeSize = eyeSize;
                                if(new Random().nextInt((int) (5 * (GameScreen.ui.getCurrentValue("Mutation Rate") /100.0f)) + 1) == 0) {
                                    entity.poisonRate = (int) Entity.fudge(((EntityBacteria) userDataB).poisonRate);
                                    entity.poisonDamage = (int) Entity.fudge(((EntityBacteria) userDataB).poisonDamage);
                                    entity.poisonLife = (int) Entity.fudge(((EntityBacteria) userDataB).poisonLife);
                                }
                                if(newType){
                                    entity.edgeColor = newEdge;
                                    entity.fillColor = newFill;
                                }
                                GameScreen.spawn(entity);
                            }
                            //   }
                        }
                        ((EntityBacteria) userDataB).onMateCooldown = true;
                        ((EntityBacteria) userDataA).onMateCooldown = true;

                        ((EntityBacteria) userDataB).lastMate = 0;
                        ((EntityBacteria) userDataA).lastMate = 0;
                    }
                }
            } else {
                if (userDataA instanceof EntityFloatingFood) {
                    ((EntityBacteria) userDataB).onCollision((EntityFloatingFood) userDataA);
                }
            }
        }


        if (userDataA instanceof EntityFloatingFood && userDataB instanceof EntityFloatingFood && ((EntityFloatingFood) userDataB).radius > 5 && ((EntityFloatingFood) userDataB).splitCount < 5) {
            if (new Random().nextInt(50) == 0) {
               ((EntityFloatingFood) userDataB).dead = true;
                int j = new Random().nextInt(3) + 2;
                for (int i = 0; i != j; i++) {
                 //   float[] x = getRandomPointInCircles(((EntityFloatingFood) userDataA).getBody().getPosition().x, ((EntityFloatingFood) userDataA).getBody().getPosition().y, 0, 100);

                    EntityFloatingFood entity = new EntityFloatingFood(Box2DHelper.getRandomPointInCircle(((EntityFloatingFood) userDataB).getBody().getPosition().x,((EntityFloatingFood) userDataB).getBody().getPosition().y,5f), ((EntityFloatingFood) userDataB).radius / j);
                    entity.splitCount = ((EntityFloatingFood) userDataB).splitCount+1;
                    GameScreen.spawn(entity);
                }
            }
        }

        if(userDataA instanceof EntityPoison){
            if(userDataB instanceof EntityBacteria){
                ((EntityPoison) userDataA).onCollideWith((EntityBacteria) userDataB);
            }
        }else if(userDataB instanceof EntityPoison){
            if(userDataA instanceof EntityBacteria){
                ((EntityPoison) userDataB).onCollideWith((EntityBacteria) userDataA);
            }
        }

    }

    public static Vector2 findEmptyAreaForSpawn(World world, Body existingBody, float spawnRadius, float newEntitySize) {
        Vector2 existingPosition = existingBody.getPosition();
        Array<Body> nearbyBodies = new Array<>();

        // Define an AABB area to search for nearby bodies around the existing body
        float aabbLowerX = existingPosition.x - spawnRadius;
        float aabbLowerY = existingPosition.y - spawnRadius;
        float aabbUpperX = existingPosition.x + spawnRadius;
        float aabbUpperY = existingPosition.y + spawnRadius;

        // Create a query callback to find nearby bodies
        QueryCallback callback = new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                nearbyBodies.add(fixture.getBody());
                return true;  // Continue querying
            }
        };

        // Query the world to find nearby bodies
        world.QueryAABB(callback, aabbLowerX, aabbLowerY, aabbUpperX, aabbUpperY);

        // Iterate through potential spawn positions (e.g., sample in a grid around the entity)
        for (float angle = 0; angle < 360; angle += 15) {
            // Calculate a spawn candidate position around the existing body at different angles
            Vector2 candidatePosition = new Vector2(existingPosition).add(
                new Vector2(spawnRadius, 0).rotateDeg(angle)
            );

            // Check if the candidate position is not overlapping any of the nearby bodies
            boolean isPositionValid = true;
            for (Body body : nearbyBodies) {
                if (body == existingBody) continue;  // Skip checking the existing body itself

                // Check distance from candidate position to other bodies
                if (body.getPosition().dst2(candidatePosition) <= (newEntitySize + 200) * (newEntitySize + 200)) {
                    isPositionValid = false;
                    break;
                }
            }

            // If we found a valid position, return it
            if (isPositionValid) {
                return candidatePosition;
            }
        }

        // If no valid position was found, return null
        return null;
    }

    @Override
    public void endContact(Contact contact) {
        // Handle end of collision if needed
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Handle before collision response
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // Handle after collision response
    }
}
