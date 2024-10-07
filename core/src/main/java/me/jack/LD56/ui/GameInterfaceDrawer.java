package me.jack.LD56.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import java.util.function.Supplier;

public class GameInterfaceDrawer {

    private final SpriteBatch batch;
    private final BitmapFont font;

    // Bar display properties
    private static class BarDisplay {
        String label;
        Rectangle fullBarBounds;
        Rectangle filledBarBounds;
        Supplier<Float> valueSupplier; // Lambda to provide current value

        BarDisplay(String label, Rectangle fullBarBounds, Supplier<Float> valueSupplier) {
            this.label = label;
            this.fullBarBounds = fullBarBounds;
            this.valueSupplier = valueSupplier;
            this.filledBarBounds = new Rectangle(fullBarBounds.x, fullBarBounds.y, fullBarBounds.width, fullBarBounds.height);
        }
    }

    private final BarDisplay[] bars;

    public GameInterfaceDrawer(SpriteBatch batch, BitmapFont font) {
        this.batch = batch;
        this.font = font;

        bars = new BarDisplay[3];
        bars[0] = new BarDisplay("Health", new Rectangle(50, 400, 300, 30), () -> 0.75f);
        bars[1] = new BarDisplay("Stamina", new Rectangle(50, 350, 300, 30), () -> 0.50f);
        bars[2] = new BarDisplay("Mana", new Rectangle(50, 300, 300, 30), () -> 0.25f);
    }

    public void draw() {
        batch.begin();
        for (BarDisplay bar : bars) {
            // Draw label
            font.setColor(Color.WHITE);
            font.draw(batch, bar.label, bar.fullBarBounds.x, bar.fullBarBounds.y + bar.fullBarBounds.height + 20);

            // Draw full bar in dark color
            batch.setColor(Color.DARK_GRAY);
            batch.draw(TextureSingleton.getInstance().getPixel(), bar.fullBarBounds.x, bar.fullBarBounds.y, bar.fullBarBounds.width, bar.fullBarBounds.height);

            // Draw filled portion in light color based on value
            float value = bar.valueSupplier.get();
            bar.filledBarBounds.width = bar.fullBarBounds.width * value;
            batch.setColor(Color.GREEN);
            batch.draw(TextureSingleton.getInstance().getPixel(), bar.filledBarBounds.x, bar.filledBarBounds.y, bar.filledBarBounds.width, bar.filledBarBounds.height);
        }
        batch.end();
    }

    // Helper class for getting a 1x1 white pixel texture
    private static class TextureSingleton {
        private static TextureSingleton instance;
        private final com.badlogic.gdx.graphics.Texture pixel;

        private TextureSingleton() {
            pixel = new com.badlogic.gdx.graphics.Texture("1x1.png");
        }

        public static TextureSingleton getInstance() {
            if (instance == null) {
                instance = new TextureSingleton();
            }
            return instance;
        }

        public com.badlogic.gdx.graphics.Texture getPixel() {
            return pixel;
        }
    }
}
