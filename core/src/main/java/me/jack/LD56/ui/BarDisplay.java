package me.jack.LD56.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.function.IntSupplier;

public class BarDisplay extends UIElement{


    String label;
    Color baseColour,darkerColour;
    IntSupplier currentValue,max;
    private BitmapFont font;
    public BarDisplay(String label, int x, int y, int w, int h, Color baseColour, IntSupplier max, IntSupplier currentValue) {
        super(x, y, w, h);
        font = new BitmapFont();
        this.label = label;
        this.baseColour = baseColour;
        this.darkerColour = new Color(baseColour);
        darkerColour.add(Color.BLACK);
        darkerColour.add(Color.BLACK);
        darkerColour.add(Color.BLACK);
        this.max = max;
        this.currentValue= currentValue;
    }

    @Override
    public void render(SpriteBatch images, ShapeRenderer renderer) {
        renderer.set(ShapeRenderer.ShapeType.Line);
        font.setColor(Color.RED);
        font.draw(images,label,getX(),getY());
        renderer.set(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(darkerColour);
        renderer.rect(getX() + 50,getY()-16,(getW()-50),getH());
        renderer.setColor(Color.RED);
        renderer.rect(getX() + 50,getY()-16,(getW()-50) * ((float) currentValue.getAsInt() /max.getAsInt()),getH());
        renderer.set(ShapeRenderer.ShapeType.Line);
    }
}
