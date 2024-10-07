package me.jack.LD56.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import me.jack.LD56.entity.EntityBacteria;

public abstract class AbstractAI {

    public abstract Vector2 runAI(EntityBacteria parent, World world);
}
