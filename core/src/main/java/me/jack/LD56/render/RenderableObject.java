package me.jack.LD56.render;

import com.badlogic.gdx.graphics.Color;

public interface RenderableObject {

    Color getEdgeColor();

    Color getFillColor();

    int getEdgeThickness(); //Could represent armour/defence (cell wall kinda thing)
}
