package pong;

import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import static pong.Pong.SCREENHEIGHT;
import static pong.Pong.SCREENWIDTH;
import static pong.Pong.p1;
import static pong.Pong.p2;
import static pong.Pong.MAXVELOCITY;

public class Ball extends Circle {

    private class Vector {

        double x;
        double y;

        Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    Vector v = new Vector(-Pong.BALLSTARTSPEED, 0);

    Ball(int r, int x, int y) {
        setTranslateX(x);
        setTranslateY(y);
        setRadius(r);
        setFill(Color.WHEAT);
    }

    void update() {
        for (int i = 0; i < Math.abs(v.x); i++) {
            collisions();
            setTranslateX(getTranslateX() + (v.x > 0 ? 1 : -1));
        }
        for (int i = 0; i < Math.abs(v.y); i++) {
            collisions();
            setTranslateY(getTranslateY() + (v.y > 0 ? 1 : -1));
        }
    }

    void collisions() {
        Bounds b1 = p1.getBoundsInParent();
        Bounds b2 = p2.getBoundsInParent();
        if (getBoundsInParent().intersects(b1.getMinX(), b1.getMinY(), b1.getWidth(), 0)//Tops of paddles
                || getBoundsInParent().intersects(b2.getMinX(), b2.getMinY(), b2.getWidth(), 0)) {
            v.y = -(Math.abs(v.y) + 1);
        } else if (getBoundsInParent().intersects(b1.getMinX(), b1.getMaxY(), b1.getWidth(), 0)//Bottoms of paddles
                || getBoundsInParent().intersects(b2.getMinX(), b2.getMaxY(), b2.getWidth(), 0)) {
            v.y = Math.abs(v.y) + 1;
        } else if (getBoundsInParent().intersects(b1.getMaxX(), b1.getMinY(), 0, b1.getHeight())) {//Paddle 1 front
            double distanceFromCenter = (getTranslateY() + getRadius()) - (b1.getMaxY() - b1.getHeight() / 2);
            v.x = Math.abs(v.x) + 1;
            v.y = distanceFromCenter / 10;
        } else if (getBoundsInParent().intersects(b2.getMinX(), b2.getMinY(), 0, b2.getHeight())) {//Paddle 2 front
            double distanceFromCenter = (getTranslateY() + getRadius()) - (b2.getMaxY() - b2.getHeight() / 2);
            v.x = -(Math.abs(v.x) + 1);
            v.y = distanceFromCenter / 10;
        } else if (getBoundsInParent().intersects(b1.getMinX(), b1.getMinY(), 0, b1.getHeight())) {//Paddle 1 back
            v.x *= -1;
        } else if (getBoundsInParent().intersects(b2.getMaxX(), b2.getMinY(), 0, b2.getHeight())) {//Paddle 2 back
            v.x *= -1;
        }
        if (getBoundsInParent().intersects(0, 0, SCREENWIDTH, 0)) {//Top of the screen
            v.y = Math.abs(v.y);
        } else if (getBoundsInParent().intersects(0, SCREENHEIGHT, SCREENWIDTH, 0)) {//Bottom of the screen
            v.y = -Math.abs(v.y);
        }
        if (getBoundsInParent().intersects(0, 0, 0, SCREENHEIGHT)) {//Left of the screen
//            v.x = Math.abs(v.x);
            Pong.reset(false);
            Pong.incrementText(Pong.p2score);
        } else if (getBoundsInParent().intersects(SCREENWIDTH, 0, 0, SCREENHEIGHT)) {//Right of the screen
//            v.x = -Math.abs(v.x);
            Pong.reset(true);
            Pong.incrementText(Pong.p1score);
        }
        if (Math.abs(v.x) > MAXVELOCITY) {
            v.x = v.x / Math.abs(v.x) * MAXVELOCITY;
        }
        if (Math.abs(v.y) > MAXVELOCITY) {
            v.y = v.y / Math.abs(v.y) * MAXVELOCITY;
        }
    }

    public void setVector(int a, int b) {
        v.x = a;
        v.y = b;
    }
}
