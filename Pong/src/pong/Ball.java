package pong;

import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import static pong.Pong.*;

public class Ball extends Circle {

    class Vector {

        double x;
        double y;

        Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    Vector v = new Vector(-BALLSTARTSPEED, 0);

    Ball(int r, int x, int y) {
        setTranslateX(x);
        setTranslateY(y);
        setRadius(r);
        setFill(Color.WHEAT);
    }

    String update() {
        String update = "";
        for (int i = 0; i < Math.abs(v.x); i++) {
            update = collisions();
            if (!update.equals("")) {
                return update;
            }
            setTranslateX(getTranslateX() + (v.x > 0 ? 1 : -1));
        }
        for (int i = 0; i < Math.abs(v.y); i++) {
            update = collisions();
            if (!update.equals("")) {
                return update;
            }
            setTranslateY(getTranslateY() + (v.y > 0 ? 1 : -1));
        }
        return update;
    }

    String collisions() {
        Bounds b1 = p1.getBoundsInParent();
        Bounds b2 = p2.getBoundsInParent();
        if (getBoundsInParent().intersects(b1.getMinX(), b1.getMinY(), b1.getWidth(), 0)//Tops of paddles
                || getBoundsInParent().intersects(b2.getMinX(), b2.getMinY(), b2.getWidth(), 0)) {
            v.y = -(Math.abs(v.y) + 1);
        } else if (getBoundsInParent().intersects(b1.getMinX(), b1.getMaxY(), b1.getWidth(), 0)//Bottoms of paddles
                || getBoundsInParent().intersects(b2.getMinX(), b2.getMaxY(), b2.getWidth(), 0)) {
            v.y = Math.abs(v.y) + 1;
        } else if (getBoundsInParent().intersects(b1.getMaxX(), b1.getMinY(), 0, b1.getHeight())) {//Paddle 1 front
            v.x = Math.abs(v.x) + 1;
            v.y = formulaY(b1);
        } else if (getBoundsInParent().intersects(b2.getMinX(), b2.getMinY(), 0, b2.getHeight())) {//Paddle 2 front
            v.x = -(Math.abs(v.x) + 1);
            v.y = formulaY(b2);
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
            return PADDLERIGHT;
        } else if (getBoundsInParent().intersects(SCREENWIDTH, 0, 0, SCREENHEIGHT)) {//Right of the screen
//            v.x = -Math.abs(v.x);
            return PADDLELEFT;
        }
        if (Math.abs(v.x) > MAXVELOCITY) {
            v.x = v.x / Math.abs(v.x) * MAXVELOCITY;
        }
        if (Math.abs(v.y) > MAXVELOCITY) {
            v.y = v.y / Math.abs(v.y) * MAXVELOCITY;
        }
        return "";
    }

    private double formulaY(Bounds b) {
        double distanceFromCenter = (getTranslateY() + getRadius()) - (b.getMaxY() - b.getHeight() / 2);
        return (distanceFromCenter / 10) * (Math.sqrt(Math.abs(v.x)) / 2);
    }

    public void setVector(int a, int b) {
        v.x = a;
        v.y = b;
    }
}
