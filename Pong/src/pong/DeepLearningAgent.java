package pong;

import java.util.ArrayList;
import javafx.scene.input.KeyCode;
import static pong.Pong.*;

public class DeepLearningAgent {

    final String PADDLE;
    final KeyCode UP;
    final KeyCode DOWN;
    NNest.NN dqn;
    final int[] ARCHITECTURE = {14, 7, 4, 2};
    ArrayList<Experience> replay = new ArrayList<>();
    double exploration = .1;
    boolean start = true;
//    int counter = 0;
    float[][] s;
    int a;
    final int BATCHSIZE = 1000;
    final float DISCOUNT = .1f;
    int timeAlive = 0;

    DeepLearningAgent(String paddle) {
        if (paddle.equals(PADDLELEFT)) {
            PADDLE = paddle;
            UP = P1UP;
            DOWN = P1DOWN;
            dqn = new NNest().new NN("left ", .0001, 0, "relu", "linear", "quadratic", "adam", ARCHITECTURE);
        } else if (paddle.equals(PADDLERIGHT)) {
            PADDLE = paddle;
            UP = P2UP;
            DOWN = P2DOWN;
            dqn = new NNest().new NN("right ", .0001, 0, "relu", "linear", "quadratic", "adam", ARCHITECTURE);
        } else {
            throw new IllegalArgumentException("Invalid Paddle");
        }
        dqn.load();
    }

    public void update() {
        if (!start) {//Observe the new state and reward
            replay.add(new Experience(s, a, getState(), getReward(), !update.equals("")));
        }
        s = getState();
        //Choose action
        if (Math.random() < exploration) {
            a = (int) (Math.random() * 2);
        } else {
            a = dqn.argmax(dqn.feedforward(s));
        }
        //Input action
        if (a == 0) {
            KEYS.put(DOWN, false);
            KEYS.put(UP, true);
        } else {
            KEYS.put(UP, false);
            KEYS.put(DOWN, true);
        }
        start = false;
//        counter++;
//        if (counter % 1000 == 0) {
//            train();
//        }
        timeAlive++;
    }

    public void reset() {
        train();
        start = true;
        timeAlive = 0;
    }

    public float[][] getState() {
        return dqn.normalizeTanhEstimator(new float[][]{{
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
        if (update.equals(PADDLE)) {
            return 1;
        } else if (update.equals("")) {
            return 0;
        } else {
            return -1;
        }
//        return timeAlive;
    }

    class Experience {

        float[][] s;
        int a;
        float[][] s_;
        float r;
        boolean terminal;

        Experience(float[][] state, int action, float[][] nextState, float reward, boolean terminalState) {
            s = state;
            a = action;
            s_ = nextState;
            r = reward;
            terminal = terminalState;
        }
    }

    void train() {
        for (int i = 0; i < BATCHSIZE; i++) {
            Experience e = replay.get((int) (Math.random() * replay.size()));
            float target;
            if (e.terminal) {
                target = e.r;
            } else {
                float[][] predictions = dqn.feedforward(e.s_);
                target = e.r + DISCOUNT * predictions[0][dqn.argmax(predictions)];
            }
//            System.out.println(target);
            float[][] targets = dqn.feedforward(s);
            targets[0][e.a] = target;
            dqn.backpropagation(e.s, targets);
        }
        dqn.save();
        replay = new ArrayList<>();
    }
}
