package me.jack.LD56.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import me.jack.LD56.screen.GameScreen;

import java.util.HashMap;

public class SliderUI {

    private Stage stage;
    private Skin skin;
    private Table table;
    private HashMap<String, Slider> sliders;
    private HashMap<String, Label> sliderValues; // To store and update the value labels

    private Camera camera = null;
    private TextButton actionButton;
    private ShapeRenderer shapeRenderer;

    public SliderUI() {
        camera = new OrthographicCamera();
        stage = new Stage(new ScreenViewport());

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        sliders = new HashMap<>();
        sliderValues = new HashMap<>();

        // Initialize a table to hold sliders and labels
        table = new Table();
        table.setFillParent(true);
        table.top().left(); // Align to top-left
        stage.addActor(table);

        // Add sliders
        addSlider("Mutation Rate");
        addSlider("Metabolism");
        addSlider("Food Nutrition");
        addSlider("Creature Density");
        addSlider("Food Spawn Rate");

        // Add a row for padding between the last slider and the button
        table.row().padTop(20); // Adjust padding as needed

        // Add the button at the bottom
        addButton("Recreate World");
        shapeRenderer = new ShapeRenderer();
    }

    // Method to add a button at the bottom of the table
    private void addButton(String buttonText) {
        actionButton = new TextButton(buttonText, skin);

        // Add a click listener to the button (replace with desired action)
        actionButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameScreen.requestReset();
            }
        });

        // Add the button to the table in a separate row
        table.row();
        table.add(actionButton).colspan(3).center().padTop(10).padBottom(10);
    }

    public boolean isMouseInsideUI() {
        Vector2 mousePosition = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        return mousePosition.x >= 0 && mousePosition.x <= 400 &&
            mousePosition.y >= 0 && mousePosition.y <= 200;
    }

    public void addSlider(String key) {
        Label label = new Label(key, skin);

        Slider slider = new Slider(1, 100, 1, false, skin);
        slider.setValue(50);

        final Label valueLabel = new Label(String.valueOf((int) slider.getValue()), skin);
        valueLabel.setAlignment(Align.center);

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                valueLabel.setText(String.valueOf((int) slider.getValue()));
            }
        });

        sliders.put(key, slider);
        sliderValues.put(key, valueLabel);

        // Add the label, slider, and value display to the table
        table.add(label).padRight(10);
        table.add(slider).width(200);
        table.add(valueLabel).width(40).padLeft(10);
        table.row().padTop(10); // Add some space between rows
    }

    public int getCurrentValue(String key) {
        Slider slider = sliders.get(key);
        if (slider != null) {
            if("Mutation Rate".equals(key)){
                return (int) (100 - (slider.getValue()));
            }
            return (int) slider.getValue();
        } else {
            throw new IllegalArgumentException("Slider with key '" + key + "' not found.");
        }
    }

    // Method to render the stage (call this in your render loop)
    public void render() {

        // Draw the stage on top of the red background
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    // Dispose resources (call this in your dispose method)
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    // Method to add input processing for stage
    public Stage getStage() {
        return stage;
    }
}
