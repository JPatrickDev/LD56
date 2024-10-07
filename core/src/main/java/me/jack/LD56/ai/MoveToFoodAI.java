package me.jack.LD56.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import me.jack.LD56.Box2DHelper;
import me.jack.LD56.entity.Entity;
import me.jack.LD56.entity.EntityBacteria;
import me.jack.LD56.entity.EntityFloatingFood;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MoveToFoodAI extends AbstractAI{

    private Entity targetEntity;


    public static boolean isWithin10Percent(float numA, float numB) {
        // Calculate 10% of numB
        float tolerance = 0.25f * numB;

        // Check if numA is within the range [numB - tolerance, numB + tolerance]
        return numA >= (numB - tolerance) && numA <= (numB + tolerance);
    }

    @Override
    public Vector2 runAI(EntityBacteria parent, World world) {
        if(targetEntity != null){
            if(targetEntity.dead){
                targetEntity = null;
                return null;
            }
            if(targetEntity.getBody().getPosition().dst(parent.getBody().getPosition()) >= (parent.eyeSize*60)){
                targetEntity = null;
                return null;
            }
            return targetEntity.getBody().getPosition();
        }
        Array<Body> foundBodies = new Array<>();
        Box2DHelper.findBodiesInCircle(world, parent.getBody().getPosition(), parent.eyeSize * 60, foundBodies);
        List<Entity> validEntities = new ArrayList<Entity>();
        for (Body body : foundBodies) {
            if(body.getUserData() instanceof EntityFloatingFood) {
                if (isWithin10Percent(parent.entityArea, ((EntityFloatingFood) body.getUserData()).entityArea) || ((EntityFloatingFood) body.getUserData()).entityArea <= parent.entityArea) {

                    validEntities.add((Entity) body.getUserData());
                }
            }
        }
        EntityFloatingFood closestEntity = null;
        float closestDistance = Float.MAX_VALUE;  // Initialize to a very large value

        for (Entity entity : validEntities) {
            // Calculate the distance between the current entity and the valid target entity
            float distance = Box2DHelper.calculateDistanceBetweenBodies(parent.getBody(), entity.getBody());

            // Update if this entity is the closest one
            if (distance < closestDistance) {
                closestEntity = (EntityFloatingFood) entity;
                closestDistance = distance;
            }
        }
        if(new Random().nextInt(3) == 0 && !validEntities.isEmpty()){
            closestEntity = (EntityFloatingFood) validEntities.get(new Random().nextInt(validEntities.size()));
        }
        if(closestEntity != null){
            targetEntity = closestEntity;
            parent.getBody().setLinearVelocity(0, 0);

            return closestEntity.getBody().getPosition();
        }
        return null;
    }
}
