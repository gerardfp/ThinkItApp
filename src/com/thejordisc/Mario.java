package com.thejordisc;

import javafx.scene.canvas.Canvas;

public class Mario extends Sprite{
    private boolean reset=false;

    public Mario() {
        positionY=300;
        setVelocityX(2);
        this.setImage("/com/thejordisc/Sprites/mario.png");
    }

    @Override
    public void move(Canvas canvas, double time) {
        if (positionX > canvas.getWidth()+ getWidth()) {
            reset=true;
        }

        if (positionX < 0) {
            reset=false;
        }

        if (reset) {
            positionX=0-getWidth();
            reset=false;
        }else{
            positionX+= getVelocityX() *1;
        }
    }
}