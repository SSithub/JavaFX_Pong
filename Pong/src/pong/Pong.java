package pong;

import java.util.HashMap;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Pong extends Application {

    static final int SCREENWIDTH = (int) Screen.getPrimary().getBounds().getWidth();
    static final int SCREENHEIGHT = (int) Screen.getPrimary().getBounds().getHeight();
    Group root = new Group();
    Rectangle back = new Rectangle(SCREENWIDTH, SCREENHEIGHT, Color.valueOf("0x363636"));
    final KeyCode PLAYER1UP = KeyCode.W;
    final KeyCode PLAYER1DOWN = KeyCode.S;
    final KeyCode PLAYER2UP = KeyCode.UP;
    final KeyCode PLAYER2DOWN = KeyCode.DOWN;
    static Paddle p1;
    static Paddle p2;
    static Ball ball;
    HashMap<KeyCode, Boolean> keys = new HashMap<>();
    static Text p1score = new Text("0");
    static Text p2score = new Text("0");
    Color p1color = Color.valueOf("0xD7263D");
    Color p2color = Color.valueOf("0x177E89");
//    final String STYLE = "-fx-font: 100 '" + Font.getFamilies().get((int) (Math.random() * Font.getFamilies().size())) + "';";
    final String STYLE = "-fx-font: 100 'MS PGothic';";
    static final int PADDLEHEIGHT = 200;
    static final int PADDLEWIDTH = 20;
    static final int BALLRADIUS = 5;
    static final int MAXVELOCITY = 50;
    static final int PADDLESPEED = 10;
    static final int BALLSTARTSPEED = 5;

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

    public static void incrementText(Text t) {
        t.setText(Integer.toString(Integer.parseInt(t.getText()) + 1));
    }

    public static void reset(boolean p1win) {
        p1.setTranslateX(SCREENWIDTH / 12 - PADDLEWIDTH / 2);
        p1.setTranslateY(SCREENHEIGHT / 2 - PADDLEHEIGHT / 2);
        p2.setTranslateX(11 * SCREENWIDTH / 12 - PADDLEWIDTH / 2);
        p2.setTranslateY(SCREENHEIGHT / 2 - PADDLEHEIGHT / 2);
        ball.setTranslateX(SCREENWIDTH / 2 - BALLRADIUS);
        ball.setTranslateY(SCREENHEIGHT / 2 - BALLRADIUS);
        if (p1win) {
            ball.setVector(BALLSTARTSPEED, 0);
        } else {
            ball.setVector(-BALLSTARTSPEED, 0);
        }
    }

    public void setup() {
        p1score.setStyle(STYLE);
        p1score.setFill(p1color);
        p2score.setStyle(STYLE);
        p2score.setFill(p2color);
        TextFlow tf = new TextFlow(p1score, new Text("                         "), p2score);
        tf.setTextAlignment(TextAlignment.CENTER);
        VBox box = new VBox(tf);
        box.setAlignment(Pos.BOTTOM_CENTER);
        box.setPrefSize(SCREENWIDTH, SCREENHEIGHT);
        p1 = new Paddle(PADDLEWIDTH, PADDLEHEIGHT, SCREENWIDTH / 12 - PADDLEWIDTH / 2, SCREENHEIGHT / 2 - PADDLEHEIGHT / 2, p1color);
        p2 = new Paddle(PADDLEWIDTH, PADDLEHEIGHT, 11 * SCREENWIDTH / 12 - PADDLEWIDTH / 2, SCREENHEIGHT / 2 - PADDLEHEIGHT / 2, p2color);
        ball = new Ball(BALLRADIUS, SCREENWIDTH / 2 - BALLRADIUS, SCREENHEIGHT / 2 - BALLRADIUS);
        root.getChildren().addAll(back, box, p1, p2, ball);
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
        System.out.println(p1score.getFont().getName());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
