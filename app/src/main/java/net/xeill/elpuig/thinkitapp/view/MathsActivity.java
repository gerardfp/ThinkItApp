package net.xeill.elpuig.thinkitapp.view;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import net.xeill.elpuig.thinkitapp.model.Operation;
import net.xeill.elpuig.thinkitapp.R;

import java.util.ArrayList;
import java.util.List;

public class MathsActivity extends AppCompatActivity implements View.OnClickListener {
    MediaPlayer musicPlayer;
    int correctAnswers=0;
    List<AppCompatButton> answerButtons;
    int correctButtonIndex;
    boolean firstTime = true;
    VideoView bgVideo;
    ColorStateList defColor = ColorStateList.valueOf(Color.GRAY);
    Drawable defTimerColor;
    float defOp1Size = 40;
    float defOp2Size = 24;
    Operation op1=null;
    Operation op2=null;
    int mLives=3;
    TextView mTimer;
    CountDownTimer mCountdownTimer;
    MediaPlayer mCountdownPlayer;
    boolean mCountdownPlayed = false;
    int mScore=0;
    TextView mScoreText;
    TextView mAddedScoreText;
    TextView mAddedTimeText;
    TextView mLevelText;
    long mBonusTime=0;
    boolean mHasBonus=true;
    long mInitialMillis=0;
    long mMillisLeft =0;
    boolean mAnswerWasCorrect=false;
    boolean isFirstAnswer=true;
    ImageButton mLifeline5050;
    ImageButton mLifelinePassover;
    //TODO: FOR DEBUG ONLY
    //int level=5;
    int level = 1;
    int scalableLevelsStartValueOp1 = 50;
    int scalableLevelsStartValueOp2 = 50;
    int scalableLevelsStartValueOp1Bis = 40; //Bis is for multiplications and divisions
    int scalableLevelsStartValueOp2Bis = 20;

    boolean mPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_maths);

        //TODO: Añadir créditos bensound.com en help/about
        musicPlayer=MediaPlayer.create(this,R.raw.bensound_jazzyfrenchy);
        musicPlayer.setLooping(true);
        musicPlayer.setVolume(0.7f,0.7f);
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

        final FloatingActionButton homeFAB = findViewById(R.id.fab_home);

        homeFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MathsActivity.this)
                        .setMessage(R.string.home_sure)
                        .setPositiveButton(R.string.exit_menu, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent homeIntent = new Intent(MathsActivity.this,MainActivity.class);
                                startActivity(homeIntent);
                                MathsActivity.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.home_resume, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // NOTHING
                            }
                        })
                        .create().show();
            }
        });

        answerButtons = new ArrayList<>();
        answerButtons.add((AppCompatButton) findViewById(R.id.answer1));
        answerButtons.add((AppCompatButton) findViewById(R.id.answer2));
        answerButtons.add((AppCompatButton) findViewById(R.id.answer3));
        answerButtons.add((AppCompatButton) findViewById(R.id.answer4));
        answerButtons.add((AppCompatButton) findViewById(R.id.answer5));
        answerButtons.add((AppCompatButton) findViewById(R.id.answer6));
        answerButtons.add((AppCompatButton) findViewById(R.id.answer7));
        answerButtons.add((AppCompatButton) findViewById(R.id.answer8));

        for (AppCompatButton b : answerButtons) {
            b.setVisibility(View.GONE);
        }

        mScoreText = findViewById(R.id.score);
        mScoreText.setText(mScore+"");
        mAddedScoreText = findViewById(R.id.added_score);
        mAddedTimeText = findViewById(R.id.added_time);
        mTimer=findViewById(R.id.timer);

        mLevelText = findViewById(R.id.level);
        mLevelText.setText(getString(R.string.level) + " " + level);

        loadOperation();

        //COMODINES
        mLifelinePassover=findViewById(R.id.lifeline_passover);
        mLifeline5050=findViewById(R.id.lifeline_50_50);
        mLifelinePassover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                ViewCompat.setBackgroundTintList(view,ColorStateList.valueOf(Color.GRAY));

                mCountdownTimer.cancel();

                //TODO: STOP COUNTDOWN ONPAUSE ONSTOP
                if (mCountdownPlayer != null) {
                    mCountdownPlayer.stop();
                    mCountdownPlayer.release();
                    mCountdownPlayer=null;
                }

                for (AppCompatButton b : answerButtons) {
                    b.setEnabled(false);
                }

                correctAnswer();
            }
        });

        mLifeline5050.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                ViewCompat.setBackgroundTintList(view,ColorStateList.valueOf(Color.GRAY));
                MediaPlayer lifelinePlayer = MediaPlayer.create(MathsActivity.this,R.raw.lifeline);
                lifelinePlayer.start();

                List<Integer> toHide = new ArrayList<>();
                for (int i = 0; i < answerButtons.size()/2; i++) {
                    int hiddenButtonIndex = ((int)(Math.random() * (((answerButtons.size()-1) - 0) + 1)) + 0);
                    boolean alreadyHidden;

                    do {
                        alreadyHidden = false;
                        for (Integer j : toHide) {
                            if (j.equals(hiddenButtonIndex)) {
                                alreadyHidden=true;
                                break;
                            }
                        }

                        hiddenButtonIndex = ((int)(Math.random() * (((answerButtons.size()-1) - 0) + 1)) + 0);
                    } while (hiddenButtonIndex == correctButtonIndex || alreadyHidden);


                    toHide.add(hiddenButtonIndex);
                }

                for (int i : toHide) {
                    answerButtons.get(i).setText("");
                    answerButtons.get(i).setEnabled(false);
                }
            }
        });
    }

    private void loadOperation() {
        mCountdownPlayed=false;
        TextView op1Op1TV = findViewById(R.id.oper1_op1);
        TextView op1Op2TV = findViewById(R.id.oper1_op2);
        TextView op1ResTV = findViewById(R.id.oper1_res);

        TextView op2Op1TV = findViewById(R.id.oper2_op1);
        TextView op2Op2TV = findViewById(R.id.oper2_op2);
        TextView op2ResTV = findViewById(R.id.oper2_res);

        if (firstTime) {
            defTimerColor=mTimer.getBackground();
            defColor = op1Op1TV.getTextColors();
            defOp1Size = op1Op1TV.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
            defOp2Size = op2Op1TV.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
            //getTextSize returns px -> This converts to sp
            op1 = calculateOperation();
            op2 = calculateOperation();
        } else {
            op1 = op2;
            op2 = calculateOperation();
        }

        if (correctAnswers%10==0 && !isFirstAnswer) {
            level++;
            mLevelText.setText(getString(R.string.level) + " " + level);
        }

        mTimer.setBackground(defTimerColor);

        //Poner símbolo de operación
        TextView op1OpType = findViewById(R.id.oper1_opType);
        op1OpType.setText(op1.getOpTypeStr());

        TextView op2OpType = findViewById(R.id.oper2_opType);
        op2OpType.setText(op2.getOpTypeStr());

        //Poner campos según toque
        switch (op1.getHiddenField()) {
            case 0:
                op1Op1TV.setText("?");
                op1Op1TV.setTextSize(60);
                op1Op1TV.setTextColor(Color.RED);

                op1Op2TV.setText(op1.getOp2()+"");
                op1Op2TV.setTextColor(defColor);
                op1Op2TV.setTextSize(defOp1Size);

                op1ResTV.setText(op1.getRes()+"");
                op1ResTV.setTextColor(defColor);
                op1ResTV.setTextSize(defOp1Size);
                break;
            case 1:
                op1Op1TV.setText(op1.getOp1()+"");
                op1Op1TV.setTextColor(defColor);
                op1Op1TV.setTextSize(defOp1Size);


                op1Op2TV.setText("?");
                op1Op2TV.setTextSize(60);
                op1Op2TV.setTextColor(Color.RED);

                op1ResTV.setText(op1.getRes()+"");
                op1ResTV.setTextColor(defColor);
                op1ResTV.setTextSize(defOp1Size);

                break;
            case 2:
                op1Op1TV.setText(op1.getOp1()+"");
                op1Op1TV.setTextColor(defColor);
                op1Op1TV.setTextSize(defOp1Size);


                op1Op2TV.setText(op1.getOp2()+"");
                op1Op2TV.setTextColor(defColor);
                op1Op2TV.setTextSize(defOp1Size);


                op1ResTV.setText("?");
                op1ResTV.setTextSize(60);
                op1ResTV.setTextColor(Color.RED);
                break;
        }

        switch (op2.getHiddenField()) {
            case 0:
                op2Op1TV.setText("?");
                op2Op1TV.setTextSize(40);
                op2Op1TV.setTextColor(Color.RED);

                op2Op2TV.setText(op2.getOp2()+"");
                op2Op2TV.setTextColor(defColor);
                op2Op2TV.setTextSize(defOp2Size);

                op2ResTV.setText(op2.getRes()+"");
                op2ResTV.setTextColor(defColor);
                op2ResTV.setTextSize(defOp2Size);
                break;
            case 1:
                op2Op1TV.setText(op2.getOp1()+"");
                op2Op1TV.setTextColor(defColor);
                op2Op1TV.setTextSize(defOp2Size);

                op2Op2TV.setText("?");
                op2Op2TV.setTextSize(40);
                op2Op2TV.setTextColor(Color.RED);

                op2ResTV.setText(op2.getRes()+"");
                op2ResTV.setTextColor(defColor);
                op2ResTV.setTextSize(defOp2Size);

                break;
            case 2:
                op2Op1TV.setText(op2.getOp1()+"");
                op2Op1TV.setTextColor(defColor);
                op2Op1TV.setTextSize(defOp2Size);


                op2Op2TV.setText(op2.getOp2()+"");
                op2Op2TV.setTextColor(defColor);
                op2Op2TV.setTextSize(defOp2Size);


                op2ResTV.setText("?");
                op2ResTV.setTextSize(40);
                op2ResTV.setTextColor(Color.RED);
                break;
        }

        //Rellenar teclado

        //Clear buttons
        for (AppCompatButton b : answerButtons) {
            b.setText("");
        }

        int maxButtons;

        switch (level) {
            case 1:
                maxButtons=4;
                answerButtons.get(0).setVisibility(View.VISIBLE);
                answerButtons.get(1).setVisibility(View.VISIBLE);
                answerButtons.get(2).setVisibility(View.VISIBLE);
                answerButtons.get(3).setVisibility(View.VISIBLE);
                break;
            case 2:
                answerButtons.get(4).setVisibility(View.VISIBLE);
                answerButtons.get(5).setVisibility(View.VISIBLE);
                maxButtons=6;
                break;
            case 3 :
                answerButtons.get(6).setVisibility(View.VISIBLE);
                answerButtons.get(7).setVisibility(View.VISIBLE);
                maxButtons=8;
                break;
            default:
                maxButtons=8;
        }

        correctButtonIndex = (int) (Math.random() * maxButtons);

        switch (op1.getHiddenField()) {
            case 0:
                answerButtons.get(correctButtonIndex).setText(op1.getOp1() + "");
                break;
            case 1:
                answerButtons.get(correctButtonIndex).setText(op1.getOp2() + "");
                break;
            case 2:
                answerButtons.get(correctButtonIndex).setText(op1.getRes() + "");
                break;
        }

        //TODO: REMOVE ON RELEASE. FOR DEBUG ONLY
        //ViewCompat.setBackgroundTintList(answerButtons.get(correctButtonIndex),ColorStateList.valueOf(Color.GREEN));

        //Margen +-10
        for (int i = 0; i < answerButtons.size(); i++) {
            if (answerButtons.get(i).getText()=="") {
                int answerRange=0;

                //TODO: Siguen saliendo repetidos
                switch (op1.getHiddenField()) {
                    case 0:
                        answerRange = ((op1.getOp1()+10) - (op1.getOp1()-10)) + 1;
                        answerButtons.get(i).setText(((int)(Math.random() * answerRange) + op1.getOp1()-10) + "");
                        while (answerButtons.get(i).getText().equals(answerButtons.get(correctButtonIndex).getText())) {
                            answerButtons.get(i).setText(((int)(Math.random() * answerRange) + op1.getOp1()-10) + "");
                        }
                        break;
                    case 1:
                        answerRange = ((op1.getOp2()+10) - (op1.getOp2()-10)) + 1;
                        answerButtons.get(i).setText(((int)(Math.random() * answerRange) + op1.getOp2()-10) + "");
                        while (answerButtons.get(i).getText().equals(answerButtons.get(correctButtonIndex).getText())) {
                            answerButtons.get(i).setText(((int)(Math.random() * answerRange) + op1.getOp2()-10) + "");
                        }
                        break;
                    case 2:
                        answerRange = ((op1.getRes()+10) - (op1.getRes()-10)) + 1;
                        answerButtons.get(i).setText(((int)(Math.random() * answerRange) + op1.getRes()-10) + "");
                        while (answerButtons.get(i).getText().equals(answerButtons.get(correctButtonIndex).getText())) {
                            answerButtons.get(i).setText(((int)(Math.random() * answerRange) + op1.getRes()-10) + "");
                        }
                        break;
                }
                //TODO: Se repiten a veces con las divisiones
            }
        }

        for (AppCompatButton b : answerButtons) {
            b.setOnClickListener(this);
        }



        if (mMillisLeft>10000) {
            if (mBonusTime + mMillisLeft > 59000) {
                mInitialMillis = 59000;
            } else {
                mInitialMillis = mBonusTime + mMillisLeft;
            }
        } else {
            if (mAnswerWasCorrect) {
                mInitialMillis=10000+mMillisLeft;
            } else {
                mInitialMillis=10000;
            }
        }

        mHasBonus=true;

        mCountdownTimer = new CountDownTimer(mInitialMillis, 500) {

            public void onTick(long millisUntilFinished) {
                mMillisLeft = millisUntilFinished;
                mTimer.setText("00:" + String.format("%02d",(millisUntilFinished/1000)+1));

                if (millisUntilFinished <= mInitialMillis-10000) {
                    mHasBonus=false;
                }

                if (millisUntilFinished / 1000 == 4 && mTimer.getCurrentTextColor() != Color.RED) {
                    mTimer.setBackgroundColor(Color.RED);

                    //Sonido countdown
                    if (!mCountdownPlayed) {
                        mCountdownPlayer = MediaPlayer.create(MathsActivity.this,R.raw.countdown);
                        mCountdownPlayer.start();
                        mCountdownPlayed = true;
                    }
                }
            }

            public void onFinish() {
                firstTime=false;
                mTimer.setText("00:00");
                if (mScore-50>0) {
                    mScore-=50;
                    mAddedScoreText.setText("-50");
                    mAddedScoreText.setVisibility(View.VISIBLE);
                } else {
                    mAddedScoreText.setText("-" + mScore);
                    mAddedScoreText.setVisibility(View.VISIBLE);
                    mScore=0;
                }
                mScoreText.setText(mScore+"");
                Toast.makeText(MathsActivity.this, "TIME UP!", Toast.LENGTH_SHORT).show();
                for (AppCompatButton b : answerButtons) {
                    b.setEnabled(false);
                }
                mLives--;
                if (mLives>0) {
                    //TODO: Modularizar?
                    //Quitar vida
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            switch (mLives) {
                                case 2:
                                    findViewById(R.id.life3).setVisibility(View.GONE);
                                    break;
                                case 1:
                                    findViewById(R.id.life2).setVisibility(View.GONE);
                                    break;
                                //case 0:
                                    //findViewById(R.id.life1).setVisibility(View.GONE);
                                    //findViewById(R.id.no_lives).setVisibility(View.VISIBLE);
                            }
                        }
                    }, 500L);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAddedScoreText.setVisibility(View.GONE);
                            for (AppCompatButton b : answerButtons) {
                                b.setEnabled(true);
                            }
                            loadOperation();
                        }
                    }, 1500L);
                } else {
                    gameOver();
                }
            }
        }.start();
    }

    public Operation calculateOperation() {
        Operation op1 = new Operation();;
        int op1Range=1, op2Range=1, newOp2=1;

        do {
            //Seleccionar operación
            op1.setOpType(Operation.OpType.values()[(int) (Math.random() * 4)]);

            //Calcular operandos
            switch (level) {
                case 1:
                    if (op1.getOpTypeStr().equals("÷")) {
                        op1Range = (10 - 1) + 1;
                    } else {
                        op1Range = (5 - 1) + 1;
                    }


                    if (op1.getOpTypeStr().equals("÷")) {
                        op2Range = (10 - 1) + 1;
                    } else {
                        op2Range = (5 - 1) + 1;
                    }

                    break;
                case 2:
                    op1Range = (10 - 1) + 1;

                    op2Range = (10 - 1) + 1;

                    break;
                case 3:
                    op1Range = (20 - 1) + 1;

                    if (op1.getOpTypeStr().equals("x") || op1.getOpTypeStr().equals("÷")) {
                        op2Range = (10 - 1) + 1;
                    } else {
                        op2Range = (20 - 1) + 1;
                    }

                    break;
                case 4:
                    if (op1.getOpTypeStr().equals("x") || op1.getOpTypeStr().equals("÷")) {
                        op1Range = (30 - 1) + 1;
                    } else {
                        op1Range = (40 - 1) + 1;
                    }


                    if (op1.getOpTypeStr().equals("x") || op1.getOpTypeStr().equals("÷")) {
                        op2Range = (10 - 1) + 1;
                    } else {
                        op2Range = (40 - 1) + 1;
                    }

                    break;
                case 5:
                    if (op1.getOpTypeStr().equals("x") || op1.getOpTypeStr().equals("÷")) {
                        op1Range = (40 - 1) + 1;
                    } else {
                        op1Range = (50 - 1) + 1;
                    }


                    if (op1.getOpTypeStr().equals("x") || op1.getOpTypeStr().equals("÷")) {
                        op2Range = (20 - 1) + 1;
                    } else {
                        op2Range = (50 - 1) + 1;
                    }

                    break;
                default:
                    if (op1.getOpTypeStr().equals("x") || op1.getOpTypeStr().equals("÷")) {
                        op1Range = (scalableLevelsStartValueOp1Bis - 1) + 1;
                        scalableLevelsStartValueOp1Bis+=10;
                    } else {
                        op1Range = (scalableLevelsStartValueOp1 - 1) + 1;
                        scalableLevelsStartValueOp1+=10;
                    }


                    if (op1.getOpTypeStr().equals("x") || op1.getOpTypeStr().equals("÷")) {
                        op2Range = (scalableLevelsStartValueOp2Bis - 1) + 1;
                        scalableLevelsStartValueOp2Bis+=5;
                    } else {
                        op2Range = (scalableLevelsStartValueOp2+10 - 1) + 1;
                        scalableLevelsStartValueOp2+=10;
                    }

                    break;
            }

            op1.setOp1((int)(Math.random() * op1Range) + 1);

            newOp2 = (int)(Math.random() * op2Range) + 1;
            while (op1.getOpTypeStr().equals("÷") && op1.getOp1()%newOp2 !=0) {
                newOp2 = (int)(Math.random() * op2Range) + 1;
            }
            op1.setOp2(newOp2);

        } while ((op1.getOpTypeStr().equals("÷") && (op1.getOp1()/op1.getOp2()) <= 0) || (op1.getOpTypeStr().equals("-") && op1.getOp1()-op1.getOp2() < 0));

        //Guardar resultado
        op1.calculate();

        //Calcular campo escondido
        op1.setHiddenField((int)(Math.random() * ((2 - 0) + 1)) + 0);

        return op1;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(musicPlayer!=null && musicPlayer.isPlaying()){
            musicPlayer.pause();
        }

        mCountdownTimer.cancel();
        mPaused=true;

        if (mCountdownPlayer != null && mCountdownPlayer.isPlaying()) {
            mCountdownPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(musicPlayer!=null && !musicPlayer.isPlaying()){
            musicPlayer.start();
        }

        if(mCountdownPlayer!=null && !mCountdownPlayer.isPlaying()){
            mCountdownPlayer.start();
        }

        if (mPaused) {
            //TODO: Recuperar estado de countdown, inciar como tal (como?)
            mCountdownTimer.start();
            mPaused=false;
        }
    }

    @Override
    public void onClick(final View view) {
        mCountdownTimer.cancel();

        //TODO: STOP COUNTDOWN ONPAUSE ONSTOP
        if (mCountdownPlayer != null) {
            mCountdownPlayer.stop();
            mCountdownPlayer.release();
            mCountdownPlayer=null;
        }

        for (AppCompatButton b : answerButtons) {
            b.setEnabled(false);
        }

        //ACIERTA
        if (answerButtons.indexOf(view) == correctButtonIndex) {
            correctAnswer();
        } else { //FALLA
            mAnswerWasCorrect=false;
            view.setOnClickListener(null);
            firstTime=false;
            mHasBonus=false;
            mBonusTime=0;

            if (mScore-50>0) {
                mScore-=50;
                mAddedScoreText.setText("-50");
                mAddedScoreText.setVisibility(View.VISIBLE);
            } else {
                mAddedScoreText.setText("-" + mScore);
                mAddedScoreText.setVisibility(View.VISIBLE);
                mScore=0;
            }

            mScoreText.setText(mScore+"");

            ViewCompat.setBackgroundTintList(answerButtons.get(correctButtonIndex),ColorStateList.valueOf(Color.GREEN));

            final ColorStateList defButtonColor = ViewCompat.getBackgroundTintList(view);
            ViewCompat.setBackgroundTintList(view,ColorStateList.valueOf(Color.RED));

            MediaPlayer.create(MathsActivity.this,R.raw.incorrect).start();

            mLives--;
            if (mLives>0) {
                //Quitar vida
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switch (mLives) {
                            case 2:
                                findViewById(R.id.life3).setVisibility(View.GONE);
                                break;
                            case 1:
                                findViewById(R.id.life2).setVisibility(View.GONE);
                                break;

                            //This was for "life 0" state
//                            case 0:
//                                findViewById(R.id.life1).setVisibility(View.GONE);
//                                findViewById(R.id.no_lives).setVisibility(View.VISIBLE);

                        }
                    }
                }, 500L);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAddedScoreText.setVisibility(View.GONE);
                        for (AppCompatButton b : answerButtons) {
                            b.setEnabled(true);
                        }
                        ViewCompat.setBackgroundTintList(view,defButtonColor);
                        ViewCompat.setBackgroundTintList(answerButtons.get(correctButtonIndex),defButtonColor);
                        loadOperation();
                    }
                }, 1500L);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gameOver();
                    }
                }, 1500L);

            }
        }
    }

    private void correctAnswer() {
        isFirstAnswer=false;
        correctAnswers++;
        mAnswerWasCorrect=true;

        mScore+=100;
        mScore+= (mMillisLeft/1000)*10+10;
        mScoreText.setText(mScore+"");

        mAddedScoreText.setText("+100" + " +" + ((mMillisLeft/1000)*10+10));
        mAddedScoreText.setVisibility(View.VISIBLE);

        if (mHasBonus) {
            mBonusTime=10000-(mInitialMillis-mMillisLeft);
            mAddedTimeText.setText("+00:" + String.format("%02d",((int) mBonusTime/1000 +1)));
            mAddedTimeText.setVisibility(View.VISIBLE);
        } else {
            mBonusTime=0;
        }

        answerButtons.get(correctButtonIndex).setOnClickListener(null);
        firstTime=false;

        //Triquiñuelas para API <21
        final ColorStateList defButtonColor = ViewCompat.getBackgroundTintList(answerButtons.get(correctButtonIndex));
        ViewCompat.setBackgroundTintList(answerButtons.get(correctButtonIndex),ColorStateList.valueOf(Color.GREEN));

        MediaPlayer.create(MathsActivity.this,R.raw.correct).start();

        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(MathsActivity.this,R.animator.op2_movement);
        LinearLayout op2Layout = findViewById(R.id.op2_layout);
        set.setTarget(op2Layout);
        set.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAddedScoreText.setVisibility(View.GONE);
                mAddedTimeText.setVisibility(View.GONE);
                for (AppCompatButton b : answerButtons) {
                    b.setEnabled(true);
                }
                //TODO: CHANGE ON RELEASE. FOR DEBUG ONLY
                ViewCompat.setBackgroundTintList(answerButtons.get(correctButtonIndex),defButtonColor);
                loadOperation();
            }
        }, 1500L);
    }

    private void gameOver() {
        findViewById(R.id.life1).setVisibility(View.GONE);
        mAddedTimeText.setVisibility(View.GONE);
        mAddedScoreText.setVisibility(View.GONE);
        mLifeline5050.setEnabled(false);
        ViewCompat.setBackgroundTintList(mLifeline5050,ColorStateList.valueOf(Color.GRAY));
        mLifelinePassover.setEnabled(false);
        ViewCompat.setBackgroundTintList(mLifelinePassover,ColorStateList.valueOf(Color.GRAY));

        Toast.makeText(MathsActivity.this, "GAME OVER", Toast.LENGTH_SHORT).show();

        Intent resultIntent = new Intent(MathsActivity.this, ResultActivity.class);
        resultIntent.putExtra("score",mScore);
        startActivity(resultIntent);
        MathsActivity.this.finish();
    }
}