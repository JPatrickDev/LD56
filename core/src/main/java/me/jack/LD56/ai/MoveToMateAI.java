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

public class MoveToMateAI extends AbstractAI{

    private EntityBacteria currentTarget;
    @Override
    public Vector2 runAI(EntityBacteria parent, World world) {
        if(currentTarget != null){
            if(currentTarget.dead){
                currentTarget = null;
                return null;
            }
            return currentTarget.getBody().getPosition();
        }
        Array<Body> foundBodies = new Array<>();
        Box2DHelper.findBodiesInCircle(world, parent.getBody().getPosition(), 50f, foundBodies);
        List<Entity> validEntities = new ArrayList<Entity>();
        for (Body body : foundBodies) {
            if(body.getUserData() == parent)
                continue;
            if(body.getUserData() instanceof EntityBacteria) {
                if(((EntityBacteria) body.getUserData()).getEdgeColor() == parent.getEdgeColor() && ((EntityBacteria) body.getUserData()).getFillColor() == parent.getFillColor()){
                    currentTarget = (EntityBacteria) body.getUserData();
                 //   Box2DHelper.attract(currentTarget.getBody(),parent.getBody());
                }
            }
        }
        if(currentTarget == null)
            return null;


        return currentTarget.getBody().getPosition();
    }
}
