package pong;

import javafx.scene.input.KeyCode;
import static pong.Pong.*;

public class BasicAI {

    final KeyCode UP;
    final KeyCode DOWN;
    final Paddle p;

    BasicAI(String paddle) {
        if (paddle.equals(PADDLELEFT)) {
            UP = P1UP;
            DOWN = P1DOWN;
            p = p1;
        } else if (paddle.equals(PADDLERIGHT)) {
            UP = P2UP;
            DOWN = P2DOWN;
            p = p2;
        } else {
            throw new IllegalArgumentException();
        }
    }

    void update() {
        if (frames % FRAMESKIP == 0) {
            if (ball.getBoundsInParent().getMaxY() - ball.getRadius() > p.getBoundsInParent().getMaxY() - p.getBoundsInParent().getHeight() / 2) {
                KEYS.put(UP, false);
                KEYS.put(DOWN, true);
            } else {
                KEYS.put(DOWN, false);
                KEYS.put(UP, true);
            }
        }
    }
}
