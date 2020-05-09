package pong;

import java.util.ArrayList;
import java.util.Arrays;
import javafx.scene.input.KeyCode;
import static pong.Pong.*;
import pong.NNLib.*;
import static pong.NNLib.*;

public class AI_Genetic implements AI {

    final String PADDLE;
    final KeyCode UP;
    final KeyCode DOWN;
    Layer[] LAYERS = {new Layer.Dense(10, 50, ActivationFunction.LEAKYRELU, Initializer.HE),
//        new Layer.Dense(50, 50, ActivationFunction.LEAKYRELU, Initializer.HE),
        new Layer.Dense(50, 2, ActivationFunction.SIGMOID, Initializer.XAVIER)};
    final int POPULATION = 10000;
    int index = 0;
    int frames = 0;
    NN base;
    NN current;
    NN[] population = new NN[POPULATION];
    int[] framesAlive = new int[POPULATION];
    boolean[] beatOpponent = new boolean[POPULATION];
    final float MUTATIONRATE = .1f;

    AI_Genetic(String paddle) {
        String name;
        if (paddle.equals(PADDLELEFT)) {
            PADDLE = paddle;
            UP = P1UP;
            DOWN = P1DOWN;
            name = "leftG";
        } else if (paddle.equals(PADDLERIGHT)) {
            PADDLE = paddle;
            UP = P2UP;
            DOWN = P2DOWN;
            name = "rightG";
        } else {
            throw new IllegalArgumentException();
        }
        base = new NNLib().new NN(name, 1, 0, null, null, LAYERS);
        NNLib.showInfo(infoLayers, base);
        base.load();
        start();
    }

    @Override
    public void update() {
//        if (Pong.frames % FRAMESKIP == 0) {
            frames++;
            float[][] out = current.feedforward(getState());
            int a;
//            if (current.getRandom().nextDouble() < out[0][0]) {
//                a = 0;
//            } else {
//                a = 1;
//            }
            if (out[0][0] > out[0][1]) {
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
//        }
    }

    public void start() {
        if (index == 0) {
            populate(base);
        } else if (index == POPULATION - 1) {
            index = 0;
            endPopulation();
            populate(base);
        }
        current = population[index];
    }

    public void populate(NN base) {
        population[0] = base;
        for (int i = 1; i < POPULATION; i++) {
            population[i] = base.clone();
            population[i].mutate(2, MUTATIONRATE);
        }
    }

    @Override
    public void reset() {
        framesAlive[index] = frames;
        frames = 0;
        beatOpponent[index] = hasWon();
        index++;
        start();
    }

    public void endPopulation() {
//        //Who beat the opponent?
//        ArrayList<Integer> winnersIndices = new ArrayList<>();
//        for (int i = 0; i < POPULATION; i++) {
//            if (beatOpponent[i] == true) {
//                winnersIndices.add(i);
//            }
//        }
//        //Pick out the best performing agent for the next generation
//        if (!winnersIndices.isEmpty()) {//Pick out the one that won the fastest
//            int numberOfWinners = winnersIndices.size();
//            int[] winnersFrames = new int[numberOfWinners];
//            for (int i = 0; i < numberOfWinners; i++) {
//                winnersFrames[i] = framesAlive[winnersIndices.get(i)];
//            }
//            base.copyParameters(population[winnersIndices.get(argmin(winnersFrames))]);
//        } else {//If there are no winners, pick out the longest lived
//            base.copyParameters(population[argmax(framesAlive)]);
//        }

        System.out.println(Arrays.toString(framesAlive));
        base.copyParameters(population[argmax(framesAlive)]);
        base.save();
    }

    public static int argmax(int[] arr) {
        int size = arr.length;
        int max = arr[0];
        int index = 0;
        for (int i = 1; i < size; i++) {
            int val = arr[i];
            if (val > max) {
                max = val;
                index = i;
            }
        }
        return index;
    }

    public static int argmin(int[] arr) {
        int size = arr.length;
        int min = arr[0];
        int index = 0;
        for (int i = 1; i < size; i++) {
            int val = arr[i];
            if (val < min) {
                min = val;
                index = i;
            }
        }
        return index;
    }

    public float[][] getState() {
        return new float[][]{{
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
        }};
    }

    public boolean hasWon() {
        if (update.equals(PADDLELEFT)) {
            if (PADDLE.equals(PADDLELEFT)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (PADDLE.equals(PADDLELEFT)) {
                return true;
            } else {
                return false;
            }
        }
    }
}
