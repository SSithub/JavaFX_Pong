package pong;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import static pong.Pong.PADDLESPEED;
import static pong.Pong.SCREENHEIGHT;
import static pong.Pong.ball;

public class Paddle extends Rectangle {

    Paddle(int width, int height, int x, int y) {
        setTranslateX(x);
        setTranslateY(y);
        setWidth(width);
        setHeight(height);
        setFill(Color.PALEVIOLETRED);
    }

    void moveUp() {
        for (int i = 0; i < PADDLESPEED; i++) {
            if (getTranslateY() - 1 != 0 && !getBoundsInParent().intersects(ball.getBoundsInParent())) {//Should never be less than 0 because of incrementing by 1
                setTranslateY(getTranslateY() - 1);
            }
        }
    }

    void moveDown() {
        for (int i = 0; i < PADDLESPEED; i++) {
            if (getTranslateY() + getHeight() + 1 != SCREENHEIGHT && !getBoundsInParent().intersects(ball.getBoundsInParent())) {//Should never be greater than the screen resolution
                setTranslateY(getTranslateY() + 1);
            }
        }
    }
}
