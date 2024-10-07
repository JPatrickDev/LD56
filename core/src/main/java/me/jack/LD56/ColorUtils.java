package me.jack.LD56;

import com.badlogic.gdx.graphics.Color;
import java.util.Random;

public class ColorUtils {

    private static final Color[] PRESET_COLORS_OLD = {
        Color.RED, Color.BLUE, Color.YELLOW
    };

    private static final Color[] PRESET_COLORS = {
        Color.MAGENTA, Color.YELLOW, Color.GOLD,
        Color.ORANGE, Color.PURPLE, Color.CYAN, Color.PINK,
    };

    private static final Random random = new Random();

    public static Color getRandomColor() {
        int randomIndex = random.nextInt(PRESET_COLORS.length);
        return PRESET_COLORS[randomIndex];
    }
}
