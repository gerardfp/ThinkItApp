package com.thejordisc;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;


public class Game extends Application {

    //TODO:separar texto, fix clear, listener para scene background

    Mario mario;
    int score=0;
    int level=1;
    List<Goomba> goombas = new ArrayList<>();
    Canvas canvas;
    long lastNanoTime;
    int totalGoomba;
    Label labelScore = new Label(""+score);
    Label labelLevel = new Label("LEVEL "+level);
    Label labelTotalScore = new Label("LEVEL "+level);
    GraphicsContext gc;
    double elapsedTime;
    Scene theScene;
    Image image;
    ImagePattern pattern;
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage theStage) {

        theStage.setTitle( "My Game" );

        Group root = new Group();
        theScene = new Scene( root );
        theStage.setScene( theScene );

        int screenWidth = 800;
        theStage.setWidth(screenWidth);

        int screenHeight = 600;
        theStage.setHeight(screenHeight);

        canvas = new Canvas(theStage.getWidth(),theStage.getHeight());
        canvas.setOnMouseClicked(this::mouseCliked);
        root.getChildren().add(canvas);
        root.getChildren().add(labelScore);
        root.getChildren().add(labelLevel);
        root.getChildren().add(labelTotalScore);

        image = new Image("/com/thejordisc/Sprites/bg.png");
        pattern = new ImagePattern(image);
        theScene.setFill(pattern);

        gc = canvas.getGraphicsContext2D();
        labelLevel.autosize();
        mario= new Mario();
        mario.setWidth(100);
        totalGoomba=(int)(Math.random() * 12) + 6;
        for (int i=0;i<totalGoomba;i++){
            createGoomba();
        }

        lastNanoTime = System.nanoTime();

        new AnimationTimer()
        {
            public void handle(long currentNanoTime)
            {
                elapsedTime = (currentNanoTime - lastNanoTime) / 1000000000.0;
                lastNanoTime=currentNanoTime;

                updateWindowSize();

                clearSprites();
                renderSprites();
                moveSprites();
                checkSpriteCollision();

                updateLevel();
                labelLevel.setText("LEVEL "+level);
                labelScore.setText("ROUND SCORE "+score);
                labelTotalScore.setText("TOTAL SCORE "+score*level);

            }
        }.start();
        
        theStage.show();
    }


    private void updateLevel() {
        if (score==totalGoomba){
            level++;
            score=0;
        }
    }

    private void updateWindowSize() {
        canvas.setWidth(theScene.getWidth());
        canvas.setHeight(theScene.getHeight());

        theScene.setFill(pattern);
    }

    private void moveSprites() {
        mario.move(canvas,elapsedTime);
        for (Goomba g :
                goombas) {
            g.move(canvas,elapsedTime);
        }
    }

    private void renderSprites() {
        mario.render(gc);
        for (Goomba g :
                goombas) {
            g.render(gc);
        }
    }

    private void clearSprites() {
        for (Goomba g :
                goombas) {
            g.clear(gc);
        }
        mario.clear(gc);
    }

    private void createGoomba() {
        for (int i = 0; i < 1; i++) {
            Goomba goomba = new Goomba();

            int rangePos = (int) ((canvas.getWidth()-goomba.getWidth() - 0) + 1);
            int randomPos=(int)(Math.random() * rangePos) + 0;
            goomba.setPosition(randomPos,0-goomba.getHeight());

            int rangeVel = ((50 - 20) + 1);
            int randomVel = (int)(Math.random() * rangeVel) + 20;
            goomba.setVelocityY(randomVel*level);
            goombas.add(goomba);
        }
    }

    private void mouseCliked(MouseEvent e) {
        //TODO: implements iterator
        for (Goomba g : new ArrayList<>(goombas)) {
            // Do something
            if (g.isClicked(new Point2D(e.getSceneX(),e.getSceneY()))){
                goombas.remove(g);
                score++;
                createGoomba();
            }
        }
    }

    private void checkSpriteCollision() {
        for (Goomba g :
                goombas) {
            if(mario.intersects(g)){
                resetGame();
            }
        }
    }

    private void resetGame() {
        score=0;
        level=1;
        mario.positionX=0;
        mario.setVelocityX(2);
        labelScore.setText(""+score);
    }
}