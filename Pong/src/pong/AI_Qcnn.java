package pong;

import java.util.ArrayList;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import static pong.Pong.*;
import pong.NNlib.*;
import static pong.NNlib.*;

public class AI_Qcnn implements AI {

    final String PADDLE;
    final KeyCode UP;
    final KeyCode DOWN;
    NN nn;
    NN tn;
    ArrayList<Experience> replay = new ArrayList<>();
    private int trainCount = 0;
    private final int TRAINSBEFORETNRESET = 20;
    private final int REPLAYSIZE = 100;
    private final int BATCH = 10;
    private boolean start = true;
    private float[][][] s;
    private int a;
    private float[][][] s_;
    private final float DISCOUNT = .9f;
    private final int FRAMESKIP = 1;

    private final boolean TRAINING = true;

    AI_Qcnn(String paddle) {
        String name = "";
        if (paddle.equals(PADDLELEFT)) {
            PADDLE = paddle;
            UP = P1UP;
            DOWN = P1DOWN;
            name = "leftQcnn";
        } else if (paddle.equals(PADDLERIGHT)) {
            PADDLE = paddle;
            UP = P2UP;
            DOWN = P2DOWN;
            name = "rightQcnn";
        } else {
            throw new IllegalArgumentException();
        }
        nn = new NN(name, 123456789, .001f, LossFunction.QUADRATIC(.5), Optimizer.ADAM,
                new Layer.Conv(1, 1, 16, 16, 16, 0, 0, Activation.TANH),//1-800-1200 conv(s=16) 7-1-16-16 = 7-50-75
                new Layer.Flatten(1, 50, 75),//7-10-15 = 1050
                new Layer.Dense(3750, 2, Activation.SIGMOID, Initializer.XAVIER)
        );
        nn.loadInsideJar();
        tn = nn.clone();
        tn.setLabel("Target Network");
        NNlib.setInfoUpdateRate(500);
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
                float[][] Q_sa = (float[][]) nn.feedforward(s);
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

    public float[][][] getState() {
        WritableImage image = ROOT.snapshot(new SnapshotParameters(), null);
        PixelReader pr = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        float[][] pixelarray = new float[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelarray[y][x] = pr.getArgb(x, y);
            }
        }
        return new float[][][]{pixelarray};
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

    public void addExperience(float[][][] s, int a, float r, float[][][] s_, boolean t) {
        replay.add(new Experience(s, a, r, s_, t));
        if (replay.size() > REPLAYSIZE) {
            replay.remove(0);
        }
    }

    class Experience {

        float[][][] s;
        int a;
        float r;
        float[][][] s_;
        boolean t;

        Experience(float[][][] state, int action, float reward, float[][][] statePrime, boolean terminal) {
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
            float[][] Q_sa = (float[][]) nn.feedforward(e.s);
            if (!e.t) {
                float[][] Q_sa_ = (float[][]) tn.feedforward(e.s_);
                Q_sa[0][e.a] = e.r + DISCOUNT * Q_sa_[0][argmax(Q_sa_)];
            } else {
                Q_sa[0][e.a] = e.r;
            }
            nn.backpropagation(e.s, Q_sa);
        }
        trainCount++;
        if (trainCount % TRAINSBEFORETNRESET == 0) {
            tn.copyParameters(nn);
            nn.saveInsideJar();
        }
    }
}
