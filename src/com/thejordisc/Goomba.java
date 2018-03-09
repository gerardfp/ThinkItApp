package com.thejordisc;

import javafx.scene.canvas.Canvas;

public class Goomba extends Sprite {

    public Goomba() {
        super();
        this.setImage("/com/thejordisc/Sprites/goomba.png");
    }

    @Override
    public void move(Canvas canvas, double time) {
        this.setPosition(this.getPositionX(),getPositionY()+getVelocityY()*time);
    }
}
