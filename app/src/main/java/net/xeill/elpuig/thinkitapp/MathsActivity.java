package net.xeill.elpuig.thinkitapp;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

public class MathsActivity extends AppCompatActivity {
    MediaPlayer musicPlayer;
    int correctAnswers=0;
    VideoView bgVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_maths);

        //TODO: Añadir créditos bensound.com en help/about
        musicPlayer=MediaPlayer.create(this,R.raw.bensound_jazzyfrenchy);
        musicPlayer.setLooping(true);
        musicPlayer.start();

        bgVideo = findViewById(R.id.bg_video);
        bgVideo.setVideoURI(Uri.parse("android.resource://net.xeill.elpuig.thinkitapp/" + R.raw.bg_maths));
        bgVideo.start();

        bgVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        loadOperation();
    }

    private void loadOperation() {
        TextView correctText = findViewById(R.id.correctAnswers);
        correctText.setText(correctAnswers+"");

        //Probando números fáciles
        int range = (100 - 1) + 1;
        Operation op1 = new Operation((int)(Math.random() * range) + 1,(int)(Math.random() * range) + 1);

        op1.setOpType(Operation.OpType.values()[(int) (Math.random() * 4)]);

        op1.calculate();

        List<Button> answerButtons = new ArrayList<>();
        answerButtons.add((Button) findViewById(R.id.answer1));
        answerButtons.add((Button) findViewById(R.id.answer2));
        answerButtons.add((Button) findViewById(R.id.answer3));
        answerButtons.add((Button) findViewById(R.id.answer4));
        answerButtons.add((Button) findViewById(R.id.answer5));
        answerButtons.add((Button) findViewById(R.id.answer6));
        answerButtons.add((Button) findViewById(R.id.answer7));
        answerButtons.add((Button) findViewById(R.id.answer8));

        //Clear buttons
        for (Button b : answerButtons) {
            b.setText("");
        }

        int correctButtonIndex = (int) (Math.random() * 8);
        answerButtons.get(correctButtonIndex).setText(op1.getRes() + "");

        //Margen +-10
        for (int i = 0; i < answerButtons.size(); i++) {
            if (answerButtons.get(i).getText()=="") {
                int answerRange = ((op1.getRes()+10) - (op1.getRes()-10)) + 1;
                answerButtons.get(i).setText(((int)(Math.random() * answerRange) + op1.getRes()-10) + "");
                //Pa que no se repita
                //TODO: Se repiten a veces con las divisiones
                while (answerButtons.get(i).getText().equals(op1.getRes())) {
                    answerButtons.get(i).setText(((int)(Math.random() * answerRange) + op1.getRes()-10) + "");
                }
            }
        }

        TextView oper1Op1 = findViewById(R.id.oper1_op1);
        TextView oper1OpType = findViewById(R.id.oper1_opType);
        TextView oper1Op2 = findViewById(R.id.oper1_op2);
        TextView oper1Res = findViewById(R.id.oper1_res);

        oper1Op1.setText(op1.getOp1()+"");
        oper1OpType.setText(op1.getOpTypeStr());
        oper1Op2.setText(op1.getOp2()+"");

        //TODO: En multiplicaciones y divisiones, el segundo operando debe ser más pequeño

        answerButtons.get(correctButtonIndex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                correctAnswers++;
                //TODO: El listener se queda incluso en otras iteraciones. La primera vez va bien,
                // luego se van acumulando listeners y al final todas son correctas.
                loadOperation();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(musicPlayer!=null && musicPlayer.isPlaying()){
            musicPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(musicPlayer!=null && !musicPlayer.isPlaying()){
            musicPlayer.start();
        }
    }
}
