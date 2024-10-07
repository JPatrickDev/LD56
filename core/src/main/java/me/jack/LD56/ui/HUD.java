package me.jack.LD56.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import me.jack.LD56.screen.GameScreen;

import java.util.ArrayList;
import java.util.List;

public class HUD {

    private int x,y,w,h;

    private List<UIElement> elementList = new ArrayList<>();
    public HUD(int x, int y, int w, int h, GameScreen parent){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;

     //   elementList.add(new BarDisplay("Hunger",x,y + 100,w,16,Color.YELLOW,() -> 100, () -> (int) parent.playerEntity.hunger));
    //    elementList.add(new BarDisplay("Experience",x,y + 50,w,16,Color.YELLOW,() -> (int) parent.playerEntity.expForNextLevel(), () -> (int) parent.playerEntity.exp));

    }

    public void render(SpriteBatch batch, ShapeRenderer renderer){
      //  renderer.setColor(Color.GRAY);
     //   renderer.set(ShapeRenderer.ShapeType.Filled);
     //   renderer.rect(x,y,w,h);
      //  renderer.set(ShapeRenderer.ShapeType.Line);

        for(UIElement e :  elementList){
            e.render(batch,renderer);
        }
    }
}
