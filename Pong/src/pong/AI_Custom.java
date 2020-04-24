package pong;

import java.util.ArrayList;
import javafx.scene.input.KeyCode;
import static pong.Pong.*;
import pong.NNLib.*;
import static pong.NNLib.*;

public class AI_Custom implements AI {

    final String PADDLE;
    final KeyCode UP;
    final KeyCode DOWN;
    NN nn;
    final double LR = .001;
    final BiFunction<float[][], float[][], Object[]> LF = LossFunction.HUBER(.5f);
    final TriFunction<Float, float[][], float[][][], float[][][][]> OPT = Optimizer.AMSGRAD;
    Layer[] LAYERS = {new LayerDense(10, 50, ActivationFunction.LEAKYRELU, Initializer.HE),
        new LayerDense(50, 50, ActivationFunction.LEAKYRELU, Initializer.HE),
        new LayerDense(50, 50, ActivationFunction.LEAKYRELU, Initializer.HE),
        new LayerDense(50, 2, ActivationFunction.SOFTMAX, Initializer.XAVIER)};
    ArrayList<Experience> replay = new ArrayList<>();
    double replayFraction = .2;

    private final boolean TRAINING = true;

    AI_Custom(String paddle) {
        if (paddle.equals(PADDLELEFT)) {
            PADDLE = paddle;
            UP = P1UP;
            DOWN = P1DOWN;
            nn = new NNLib().new NN("leftA", 7, LR, LF, OPT, LAYERS);
        } else if (paddle.equals(PADDLERIGHT)) {
            PADDLE = paddle;
            UP = P2UP;
            DOWN = P2DOWN;
            nn = new NNLib().new NN("rightA", 214, LR, LF, OPT, LAYERS);
        } else {
            throw new IllegalArgumentException();
        }
        nn.load();
        NNLib.graphJFX(false, nn);
    }

    @Override
    public void update() {
        if (frames % FRAMESKIP == 0) {
            float[][] s = getState();
            float[][] out = nn.feedforward(s);
            int a;
            if (nn.getRandom().nextDouble() < exploration) {
                a = (int) (Math.random() * 2);
            } else {
                a = argmax(out);
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

    @Override
    public void reset() {
        if (TRAINING) {
            train(getReward());
        }
    }

    public float[][] getState() {
        return scale(.01f, new float[][]{{
            (float) ball.getBoundsInParent().getMinX(),
            (float) ball.getBoundsInParent().getMaxX(),
            (float) ball.getBoundsInParent().getMinY(),
            (float) ball.getBoundsInParent().getMaxY(),
            (float) ball.v.x,
            (float) ball.v.y,
            (float) p1.getBoundsInParent().getMinY(),
            (float) p1.getBoundsInParent().getMaxY(),
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
        for (int i = 0; i < size * replayFraction; i++) {
            int index = nn.getRandom().nextInt(replay.size());
            Experience e = replay.get(index);
            float[][] targets;
            if (target == 1) {
                targets = new float[][]{{0, 0}};
            } else {
                targets = new float[][]{{1, 1}};
            }
            targets[0][e.a] = target;
//            print(e.s, "state");
//            print(nn.feedforward(e.s), "prob1");
//            print(targets, "targets");
            nn.backpropagation(e.s, targets);
//            print(nn.feedforward(e.s), "prob2");
        }
        replay.clear();
        nn.save();
    }
}
