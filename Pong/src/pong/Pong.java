package pong;

import java.util.HashMap;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
    static final KeyCode P1UP = KeyCode.W;
    static final KeyCode P1DOWN = KeyCode.S;
    static final KeyCode P2UP = KeyCode.UP;
    static final KeyCode P2DOWN = KeyCode.DOWN;
    static final String PADDLELEFT = "pleft";
    static final String PADDLERIGHT = "pright";
    static Paddle p1;
    static Paddle p2;
    static Ball ball;
    static final HashMap<KeyCode, Boolean> KEYS = new HashMap<>();
    static final Text P1SCORE = new Text("0");
    static final Text P2SCORE = new Text("0");
    final Color P1COLOR = Color.valueOf("0xD7263D");
    final Color P2COLOR = Color.valueOf("0x177E89");
//    final String STYLE = "-fx-font: 100 '" + Font.getFamilies().get((int) (Math.random() * Font.getFamilies().size())) + "';";
    final String STYLE = "-fx-font: 100 'MS PGothic';";
    static String update = "";
    
    static final int PADDLEHEIGHT = 200;
    static final int PADDLEWIDTH = 20;
    static final int BALLRADIUS = 5;
    static final int MAXVELOCITY = 50;
    static final int PADDLESPEED = 10;
    static final int BALLSTARTSPEED = 5;
    
    DeepLearningAgent agent = new DeepLearningAgent(PADDLERIGHT);
    
    Timeline loop = new Timeline(new KeyFrame(Duration.millis(16), handler -> {
        agent.update();
        if (update.equals(PADDLELEFT)) {
            reset(true);
        } else if (update.equals(PADDLERIGHT)) {
            reset(false);
        }
        if (KEYS.getOrDefault(P1UP, false)) {
            p1.moveUp();
        }
        if (KEYS.getOrDefault(P1DOWN, false)) {
            p1.moveDown();
        }
        if (KEYS.getOrDefault(P2UP, false)) {
            p2.moveUp();
        }
        if (KEYS.getOrDefault(P2DOWN, false)) {
            p2.moveDown();
        }
        update = ball.update();
    }));
    
    public static void incrementText(Text t) {
        t.setText(Integer.toString(Integer.parseInt(t.getText()) + 1));
    }
    
    public void reset(boolean p1win) {
        if (update.equals(PADDLELEFT)) {
            incrementText(P1SCORE);
        } else {
            incrementText(P2SCORE);
        }
        update = "";
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
        agent.reset();
    }
    
    public void setup() {
        P1SCORE.setStyle(STYLE);
        P1SCORE.setFill(P1COLOR);
        P2SCORE.setStyle(STYLE);
        P2SCORE.setFill(P2COLOR);
        TextFlow tf = new TextFlow(P1SCORE, new Text("                         "), P2SCORE);
        tf.setTextAlignment(TextAlignment.CENTER);
        VBox box = new VBox(tf);
        box.setAlignment(Pos.BOTTOM_CENTER);
        box.setPrefSize(SCREENWIDTH, SCREENHEIGHT);
        p1 = new Paddle(PADDLEWIDTH, PADDLEHEIGHT, SCREENWIDTH / 12 - PADDLEWIDTH / 2, SCREENHEIGHT / 2 - PADDLEHEIGHT / 2, P1COLOR);
        p2 = new Paddle(PADDLEWIDTH, PADDLEHEIGHT, 11 * SCREENWIDTH / 12 - PADDLEWIDTH / 2, SCREENHEIGHT / 2 - PADDLEHEIGHT / 2, P2COLOR);
        ball = new Ball(BALLRADIUS, SCREENWIDTH / 2 - BALLRADIUS, SCREENHEIGHT / 2 - BALLRADIUS);
        root.getChildren().addAll(back, box, p1, p2, ball);
        loop.setCycleCount(Animation.INDEFINITE);
        loop.play();
        
        Slider slider = new Slider();
        slider.setPrefWidth(200);
        slider.setMax(2000);
        slider.setMin(1);
        slider.setValue(1);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            loop.setRate(newValue.doubleValue());
        });
        slider.setTranslateX(20);
        slider.setTranslateY(20);
        root.getChildren().addAll(slider);
    }
    
    @Override
    public void start(Stage stage) {
        setup();
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(event -> {
            KEYS.put(event.getCode(), true);
        });
        scene.setOnKeyReleased(event -> {
            KEYS.put(event.getCode(), false);
        });
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.show();
        System.out.println(P1SCORE.getFont().getName());
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
