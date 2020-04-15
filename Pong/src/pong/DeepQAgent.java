package pong;

import java.util.ArrayList;
import javafx.scene.input.KeyCode;
import static pong.Pong.*;
import pong.NNLib.*;

public class DeepQAgent {

    final String PADDLE;
    final KeyCode UP;
    final KeyCode DOWN;
    NN nn;
    NN tn;
    final double LR = Math.pow(10, -6);
    final Initializer INITIALIZER = Initializer.HE;
    final ActivationFunction HIDDENACTIVATION = ActivationFunction.LEAKYRELU;
    final ActivationFunction OUTPUTACTIVATION = ActivationFunction.LINEAR;
    final LossFunction LOSSFUNCTION = LossFunction.QUADRATIC;
    final Optimizer OPTIMIZER = Optimizer.AMSGRAD;
    final int[] ARCHITECTURE = {14, 50, 50, 2};
    ArrayList<Experience> replay = new ArrayList<>();
    private int trainCount = 0;

    private final int TRAINSBEFORETNRESET = 20;
    private final int BATCH = 500;
    private final int REPLAYSIZE = 20000;
    private boolean start = true;
    private float[][] s;
    private int a;
    private float[][] s_;
    private final float DISCOUNT = .9f;

    private final boolean TRAINING = true;

    DeepQAgent(String paddle) {
        if (paddle.equals(PADDLELEFT)) {
            PADDLE = paddle;
            UP = P1UP;
            DOWN = P1DOWN;
            nn = new NNLib().new NN("left", 7, LR, INITIALIZER, HIDDENACTIVATION, OUTPUTACTIVATION, LOSSFUNCTION, OPTIMIZER, ARCHITECTURE);
            tn = nn.clone();
        } else if (paddle.equals(PADDLERIGHT)) {
            PADDLE = paddle;
            UP = P2UP;
            DOWN = P2DOWN;
            nn = new NNLib().new NN("right", 214, LR, INITIALIZER, HIDDENACTIVATION, OUTPUTACTIVATION, LOSSFUNCTION, OPTIMIZER, ARCHITECTURE);
            tn = nn.clone();
//            NNLib.graphJFX(false, nn);
        } else {
            throw new IllegalArgumentException();
        }
        nn.load();
//        NNLib.graphJFX(false, nn);
    }

    public void update() {
        if (frames % FRAMESKIP == 0) {
            if (!start) {
                s_ = getState();
                addExperience(s, a, getReward(), s_, false);
                s = getState();
                if (nn.getRandom().nextDouble() < exploration) {
                    if (nn.getRandom().nextDouble() < .5) {
                        a = 0;
                    } else {
                        a = 1;
                    }
                } else {
                    float[][] Q_sa = nn.feedforward(s);
                    a = nn.argmax(Q_sa);
                }

                if (a == 0) {
                    KEYS.put(DOWN, false);
                    KEYS.put(UP, true);
                } else {
                    KEYS.put(UP, false);
                    KEYS.put(DOWN, true);
                }
            } else {//initial observation
                s = getState();
                if (nn.getRandom().nextDouble() < exploration) {
                    if (nn.getRandom().nextDouble() < .5) {
                        a = 0;
                    } else {
                        a = 1;
                    }
                } else {
                    float[][] Q_sa = nn.feedforward(s);
                    a = nn.argmax(Q_sa);
                }
                if (a == 0) {
                    KEYS.put(DOWN, false);
                    KEYS.put(UP, true);
                } else {
                    KEYS.put(UP, false);
                    KEYS.put(DOWN, true);
                }
                start = false;
            }
        }
    }

    public void reset() {
        s_ = getState();
        addExperience(s, a, getReward(), s_, true);//Add terminal state
        if (TRAINING) {
            train();
        }
        start = true;
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

    public void addExperience(float[][] s, int a, float r, float[][] s_, boolean t) {
        replay.add(new Experience(s, a, r, s_, t));
        if (replay.size() > REPLAYSIZE) {
            replay.remove(0);
        }
    }

    class Experience {

        float[][] s;
        int a;
        float r;
        float[][] s_;
        boolean t;

        Experience(float[][] state, int action, float reward, float[][] statePrime, boolean terminal) {
            s = state;
            a = action;
            r = reward;
            s_ = statePrime;
            t = terminal;
        }
    }

    void train() {
        for (int i = 0; i < BATCH; i++) {
            try {
                int index = nn.getRandom().nextInt(replay.size());
                Experience e = replay.get(index);
                float[][] Q_sa = nn.feedforward(e.s);
                float[][] Q_sa_ = tn.feedforward(e.s_);
                if (!e.t) {
                    Q_sa[0][e.a] = e.r + DISCOUNT * Q_sa_[0][tn.argmax(Q_sa_)];
                } else {
                    Q_sa[0][e.a] = e.r;
                }

//                nn.print(Q_sa, "before");
                nn.backpropagation(e.s, Q_sa);
//                nn.print(nn.feedforward(e.s), "after");
            } catch (Exception e) {

            }
        }
        trainCount++;
        if (trainCount % TRAINSBEFORETNRESET == 0) {
            tn = nn.clone();
        }
        nn.save();
    }
}
