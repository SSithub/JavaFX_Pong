package pong;

import java.util.ArrayList;
import javafx.scene.input.KeyCode;
import static pong.Pong.*;
import pong.NNlib.*;
import static pong.NNlib.*;

public class AI_PG implements AI {

    final String PADDLE;
    final KeyCode UP;
    final KeyCode DOWN;
    NN nn;
    ArrayList<float[][]> states = new ArrayList<>();
    ArrayList<Integer> actions = new ArrayList<>();
    private final float DISCOUNT = .99f;
    private int trains = 0;
    private final int FRAMESKIP = 1;

    private final boolean TRAINING = true;

    AI_PG(String paddle) {
        String name = "";
        if (paddle.equals(PADDLELEFT)) {
            PADDLE = paddle;
            UP = P1UP;
            DOWN = P1DOWN;
            name = "leftPG";
        } else if (paddle.equals(PADDLERIGHT)) {
            PADDLE = paddle;
            UP = P2UP;
            DOWN = P2DOWN;
            name = "rightPG";
        } else {
            throw new IllegalArgumentException();
        }
        nn = new NN(name, 123456789, .00001f, LossFunction.CROSSENTROPY(1), Optimizer.RMSPROP,
                new Layer.Dense(8, 64, Activation.TANH, Initializer.XAVIER),
                new Layer.Dense(64, 64, Activation.TANH, Initializer.XAVIER),
                new Layer.Dense(64, 64, Activation.TANH, Initializer.XAVIER),
                new Layer.Dense(64, 2, Activation.SOFTMAX, Initializer.XAVIER)
        );
        nn.loadInsideJar();
        NNlib.setInfoUpdateRate(500);
        NNlib.showInfo(infoLayers, nn);
    }

    @Override
    public void update() {
        if (frames % FRAMESKIP == 0) {
            float[][] s = getState();
            //Decide on an action to take
            int a;
            float[][] probabilities = (float[][]) nn.feedforward(s);
//            print(probabilities);
            if (nn.getRandom().nextFloat() < probabilities[0][0]) {
                a = 0;
            } else {
                a = 1;
            }
            //Input the action into the environment
            if (a == 0) {
                KEYS.put(DOWN, false);
                KEYS.put(UP, true);
            } else {
                KEYS.put(UP, false);
                KEYS.put(DOWN, true);
            }
            states.add(s);
            actions.add(a);
        }
    }

    @Override
    public void reset() {
        if (TRAINING) {
            train();
            states.clear();
            actions.clear();
        }
    }

    public float[][] getState() {
        if (PADDLE.equals(PADDLELEFT)) {
            return normalizeZScore(new float[][]{{
                (float) ball.getBoundsInParent().getMinX(),
                (float) ball.getBoundsInParent().getMaxX(),
                (float) ball.getBoundsInParent().getMinY(),
                (float) ball.getBoundsInParent().getMaxY(),
                (float) ball.v.x,
                (float) ball.v.y,
                (float) p1.getBoundsInParent().getMinY(),
                (float) p1.getBoundsInParent().getMaxY(),}});
        } else if (PADDLE.equals(PADDLERIGHT)) {
            return normalizeZScore(new float[][]{{
                (float) ball.getBoundsInParent().getMinX(),
                (float) ball.getBoundsInParent().getMaxX(),
                (float) ball.getBoundsInParent().getMinY(),
                (float) ball.getBoundsInParent().getMaxY(),
                (float) ball.v.x,
                (float) ball.v.y,
                (float) p2.getBoundsInParent().getMinY(),
                (float) p2.getBoundsInParent().getMaxY(),}});
        }
        throw new IllegalArgumentException("The PADDLE field must be initialized to either PADDLELEFT or PADDLERIGHT");
    }

    public float getReward() {
        if (update.equals("")) {
            return 0;
        } else if (update.equals(PADDLELEFT)) {
            if (PADDLE.equals(PADDLELEFT)) {
                return 1;
            } else {
                return -1;
            }
        } else {
            if (PADDLE.equals(PADDLELEFT)) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    void train() {
        int timesteps = states.size() - 1;
        float reward = getReward();
        for (int t = timesteps; t >= 0; t--) {
            float[][] labels = new float[1][2];
            labels[0][actions.get(t)] = reward;
            nn.backpropagation(states.get(t), labels);
            reward *= DISCOUNT;
        }
        trains++;
        if (trains % 100 == 0) {
            nn.saveInsideJar();
        }
    }
}
