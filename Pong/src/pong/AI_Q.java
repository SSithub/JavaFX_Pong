package pong;

import java.util.ArrayList;
import javafx.scene.input.KeyCode;
import static pong.Pong.*;
import pong.NNLib.*;
import static pong.NNLib.*;

public class AI_Q implements AI {

    final String PADDLE;
    final KeyCode UP;
    final KeyCode DOWN;
    NN nn;
    NN tn;
    final double LR = .0000001;
    final BiFunction<float[][], float[][], Object[]> LF = LossFunction.HUBER(1);
    final QuadFunction<Integer, Float, float[][], float[][][], float[][][][]> OPT = Optimizer.ADAM;
    Layer[] LAYERS = {new Layer.Dense(10, 50, ActivationFunction.LEAKYRELU, Initializer.HE),
        new Layer.Dense(50, 2, ActivationFunction.LINEAR, Initializer.VANILLA)};
    ArrayList<Experience> replay = new ArrayList<>();
    private int trainCount = 0;

    private final int TRAINSBEFORETNRESET = 20;
    private final int REPLAYSIZE = 10000;
    private final int BATCH = 1000;
    private boolean start = true;
    private float[][] s;
    private int a;
    private float[][] s_;
    private final float DISCOUNT = .9f;

    private final boolean TRAINING = true;

    AI_Q(String paddle) {
        String name = "";
        if (paddle.equals(PADDLELEFT)) {
            PADDLE = paddle;
            UP = P1UP;
            DOWN = P1DOWN;
            name = "leftQ";
        } else if (paddle.equals(PADDLERIGHT)) {
            PADDLE = paddle;
            UP = P2UP;
            DOWN = P2DOWN;
            name = "rightQ";
        } else {
            throw new IllegalArgumentException();
        }
        nn = new NNLib().new NN(name, 7777, LR, LF, OPT, LAYERS);
        nn.load();
        tn = nn.clone();
        tn.setLabel("Target Network");
        NNLib.setInfoUpdateRate(500);
        NNLib.showInfo(infoLayers, nn);
//        NNLib.showInfo(infoLayers, tn);
    }

    @Override
    public void update() {
        if (frames % FRAMESKIP == 0) {
            if (start) {//Initial observation
                s = getState();
                start = false;
            } else {
                s_ = getState();
                addExperience(s, a, 0, s_, false);
                s = getState();
            }
            //Decide on an action to take
            if (nn.getRandom().nextDouble() < exploration) {
                if (nn.getRandom().nextDouble() < .5) {
                    a = 0;
                } else {
                    a = 1;
                }
            } else {
                float[][] Q_sa = nn.feedforward(s);
//                print(s);
//                print(Q_sa);
                a = argmax(Q_sa);
            }
            //Input the action into the environment
            if (a == 0) {
                KEYS.put(DOWN, false);
                KEYS.put(UP, true);
            } else {
                KEYS.put(UP, false);
                KEYS.put(DOWN, true);
            }
        }
    }

    @Override
    public void reset() {
        s_ = getState();
        addExperience(s, a, getReward(), s_, true);//Add terminal state
        if (TRAINING) {
            train();
        }
        start = true;
    }

    public float[][] getState() {
        return normalizeZScore(new float[][]{{
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
        }});//ROOT.snapshot
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
            int index = nn.getRandom().nextInt(replay.size());
            Experience e = replay.get(index);
            float[][] Q_sa = nn.feedforward(e.s);
            if (!e.t) {
                float[][] Q_sa_ = tn.feedforward(e.s_);
                Q_sa[0][e.a] = e.r + DISCOUNT * Q_sa_[0][argmax(Q_sa_)];
            } else {
                Q_sa[0][e.a] = e.r;
            }
            nn.backpropagation(e.s, Q_sa);
        }
        trainCount++;
        if (trainCount % TRAINSBEFORETNRESET == 0) {
            tn.copyParameters(nn);
            nn.save();
        }
    }
}
