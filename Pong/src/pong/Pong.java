package pong;

import java.util.HashMap;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Pong extends Application {

    static final int SCREENWIDTH = (int) Screen.getPrimary().getBounds().getWidth();
    static final int SCREENHEIGHT = (int) Screen.getPrimary().getBounds().getHeight();
    Group root = new Group();
    Rectangle back = new Rectangle(SCREENWIDTH, SCREENHEIGHT, Color.PURPLE);
    final KeyCode PLAYER1UP = KeyCode.W;
    final KeyCode PLAYER1DOWN = KeyCode.S;
    final KeyCode PLAYER2UP = KeyCode.UP;
    final KeyCode PLAYER2DOWN = KeyCode.DOWN;
    final int PADDLEHEIGHT = 200;
    final int PADDLEWIDTH = 20;
    final int BALLRADIUS = 5;
    static final int MAXVELOCITY = 40;
    static Paddle p1;
    static Paddle p2;
    static Ball ball;
    HashMap<KeyCode, Boolean> keys = new HashMap<>();
    static final int PADDLESPEED = 5;

    Timeline loop = new Timeline(new KeyFrame(Duration.millis(16), handler -> {
        if (keys.getOrDefault(PLAYER1UP, false)) {
            p1.moveUp();
        }
        if (keys.getOrDefault(PLAYER1DOWN, false)) {
            p1.moveDown();
        }
        if (keys.getOrDefault(PLAYER2UP, false)) {
            p2.moveUp();
        }
        if (keys.getOrDefault(PLAYER2DOWN, false)) {
            p2.moveDown();
        }
        ball.update();
    }));

    public void setup() {
        p1 = new Paddle(PADDLEWIDTH, PADDLEHEIGHT, SCREENWIDTH / 12 - PADDLEWIDTH / 2, SCREENHEIGHT / 2 - PADDLEHEIGHT / 2);
        p2 = new Paddle(PADDLEWIDTH, PADDLEHEIGHT, 11 * SCREENWIDTH / 12 - PADDLEWIDTH / 2, SCREENHEIGHT / 2 - PADDLEHEIGHT / 2);
        ball = new Ball(BALLRADIUS, SCREENWIDTH / 2 - BALLRADIUS, SCREENHEIGHT / 2 - BALLRADIUS);
        root.getChildren().addAll(back, p1, p2, ball);
        loop.setCycleCount(Animation.INDEFINITE);
        loop.play();
    }

    @Override
    public void start(Stage stage) {
        setup();
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(event -> {
            keys.put(event.getCode(), true);
        });
        scene.setOnKeyReleased(event -> {
            keys.put(event.getCode(), false);
        });
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
