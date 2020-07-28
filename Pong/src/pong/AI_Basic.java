package pong;

import javafx.scene.input.KeyCode;
import static pong.Pong.*;

public class AI_Basic implements AI {

    final KeyCode UP;
    final KeyCode DOWN;
    final Paddle p;
    final double randomness = .2;
    final int FRAMESKIP = 4;

    AI_Basic(String paddle) {
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

    public void update() {
        if (frames % FRAMESKIP == 0) {
//            if (Math.random() < randomness) {
//                if (Math.random() < .5) {
//                    up();
//                } else {
//                    down();
//                }
//            } else {
                if (ball.getBoundsInParent().getMaxY() - ball.getRadius() > p.getBoundsInParent().getMaxY() - p.getBoundsInParent().getHeight() / 2) {
                    down();
                } else {
                    up();
                }
//            }
        }
    }

    public void up() {
        KEYS.put(DOWN, false);
        KEYS.put(UP, true);
    }

    public void down() {
        KEYS.put(UP, false);
        KEYS.put(DOWN, true);
    }

    @Override
    public void reset() {
    }
}
