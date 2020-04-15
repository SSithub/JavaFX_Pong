package pong;

import java.util.ArrayList;
import javafx.scene.input.KeyCode;
import static pong.Pong.*;
import pong.NNLib.*;

public class PolicyGradientAgent {

    final String PADDLE;
    final KeyCode UP;
    final KeyCode DOWN;
    NN nn;
    final double LR = Math.pow(10, -5);
    final Initializer INITIALIZER = Initializer.HE;
    final ActivationFunction HIDDENACTIVATION = ActivationFunction.LEAKYRELU;
    final ActivationFunction OUTPUTACTIVATION = ActivationFunction.SOFTMAX;
    final LossFunction LOSSFUNCTION = LossFunction.CROSS_ENTROPY;
    final Optimizer OPTIMIZER = Optimizer.AMSGRAD;
    final int[] ARCHITECTURE = {14, 50, 50, 2};
    ArrayList<Experience> replay = new ArrayList<>();

    private final boolean TRAINING = true;

    PolicyGradientAgent(String paddle) {
        if (paddle.equals(PADDLELEFT)) {
            PADDLE = paddle;
            UP = P1UP;
            DOWN = P1DOWN;
            nn = new NNLib().new NN("left", 7, LR, INITIALIZER, HIDDENACTIVATION, OUTPUTACTIVATION, LOSSFUNCTION, OPTIMIZER, ARCHITECTURE);
        } else if (paddle.equals(PADDLERIGHT)) {
            PADDLE = paddle;
            UP = P2UP;
            DOWN = P2DOWN;
            nn = new NNLib().new NN("right", 214, LR, INITIALIZER, HIDDENACTIVATION, OUTPUTACTIVATION, LOSSFUNCTION, OPTIMIZER, ARCHITECTURE);
//            NNLib.graphJFX(false, nn);
        } else {
            throw new IllegalArgumentException();
        }
        nn.load();
//        NNLib.graphJFX(false, nn);
    }

    public void update() {
        if (frames % FRAMESKIP == 0) {
            float[][] s = getState();
            float[][] prob = nn.feedforward(s);
            int a;
            if (nn.getRandom().nextDouble() < prob[0][0]) {
                a = 0;
            } else {
                a = 1;
            }

            if (a == 0) {
                KEYS.put(DOWN, false);
                KEYS.put(UP, true);
            } else {
                KEYS.put(UP, false);
                KEYS.put(DOWN, true);
            }

            addExperience(s, a);
        }
    }

    public void reset() {
        if (TRAINING) {
            train(getReward());
        }
    }

    public float[][] getState() {
        return nn.scale(.001f, new float[][]{{
            (float) ball.getBoundsInParent().getMinX(),
            (float) ball.getBoundsInParent().getMaxX(),
            (float) ball.getBoundsInParent().getMinY(),
            (float) ball.getBoundsInParent().getMaxY(),
            (float) ball.v.x,
            (float) ball.v.y,
            (float) p1.getBoundsInParent().getMinX(),
            (float) p1.getBoundsInParent().getMaxX(),
            (float) p1.getBoundsInParent().getMinY(),
            (float) p1.getBoundsInParent().getMaxY(),
            (float) p2.getBoundsInParent().getMinX(),
            (float) p2.getBoundsInParent().getMaxX(),
            (float) p2.getBoundsInParent().getMinY(),
            (float) p2.getBoundsInParent().getMaxY()
        }});
    }

    public float getReward() {
        if (update.equals(PADDLELEFT)) {
            if (PADDLE.equals(PADDLELEFT)) {
                return 1;
            } else {
                return 0;
            }
        } else {
            if (PADDLE.equals(PADDLELEFT)) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    public void addExperience(float[][] s, int a) {
        replay.add(new Experience(s, a));
    }

    class Experience {

        float[][] s;
        int a;

        Experience(float[][] state, int action) {
            s = state;
            a = action;
        }
    }

    void train(float target) {
        int size = replay.size();
        for (int i = 0; i < size; i++) {
            try {
                int index = nn.getRandom().nextInt(replay.size());
                Experience e = replay.get(index);
                replay.remove(index);
                float[][] targets = new float[][]{{0,0}};
                targets[0][e.a] = target;
                nn.backpropagation(e.s,targets);
            } catch (Exception e) {

            }
        }
        nn.save();
    }
}
