package me.jack.LD56.entity;

import com.badlogic.gdx.graphics.Color;
import me.jack.LD56.render.RenderableObject;

public class EntityWorldBorder implements RenderableObject {
    @Override
    public Color getEdgeColor() {
        return Color.WHITE;
    }

    @Override
    public Color getFillColor() {
        return Color.BLACK;
    }

    @Override
    public int getEdgeThickness() {
        return 5;
    }
}
