package net.xeill.elpuig.thinkitapp.view;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.xeill.elpuig.thinkitapp.R;
import net.xeill.elpuig.thinkitapp.model.Score;
import net.xeill.elpuig.thinkitapp.viewmodel.ScoreViewModel;

public class ResultActivity extends AppCompatActivity {
    SharedPreferences settings;
    int mScore;
    TextView mScoreTextView,countTextView;
    EditText name_edit;

    ScoreViewModel scoreViewModel;

    MediaPlayer mResultPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        mResultPlayer = MediaPlayer.create(ResultActivity.this,R.raw.bensound_jazzcomedy);
        mResultPlayer.setLooping(true);

        settings=getSharedPreferences("prefs", 0);
        if (settings.getBoolean("mute",true)) {
            mResultPlayer.setVolume(0f,0f);
        } else {
            mResultPlayer.setVolume(1f,1f);
        }

        mResultPlayer.start();

        scoreViewModel = ViewModelProviders.of(this).get(ScoreViewModel.class);

        mScore = getIntent().getIntExtra("score", 0);




        mScoreTextView = findViewById(R.id.score);

        mScoreTextView.setText(mScore + "");

        name_edit = findViewById(R.id.name_edit);
        countTextView = findViewById(R.id.counter);
        countTextView.setText("20/20 caracteres restantes" );
        final FloatingActionButton homeFAB = findViewById(R.id.fab_stop);

        homeFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent homeIntent = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(homeIntent);
                ResultActivity.this.finish();
            }
        });

        final FloatingActionButton saveScore = findViewById(R.id.save_score);

        saveScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name_edit.getText().length() == 0) {
                    name_edit.setError("Se requiere un nombre para guardar la puntuación");
                } else{
                    Score score = new Score();
                    score.setUser("" + name_edit.getText());
                    score.setScore(mScore);

                    scoreViewModel.insertScore(score).observe(ResultActivity.this, new Observer<Long>() {
                        @Override
                        public void onChanged(@Nullable Long aLong) {
                            //scoreRecyclerAdapter.notifyDataSetChanged();

                            Intent scoreIntent = new Intent(ResultActivity.this,ScoreActivity.class);
                            scoreIntent.putExtra("scoreId",aLong);
                            startActivity(scoreIntent);

                        }
                    });
                }
            }
        });

        final FloatingActionButton restartButton = findViewById(R.id.restart);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent playIntent = new Intent(ResultActivity.this, MathsActivity.class);
                startActivity(playIntent);
                finish();
            }
        });

        name_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft){

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                // this will show characters remaining
                countTextView.setText((20 - s.toString().length()) + "/20 caracteres restantes");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mResultPlayer !=null && mResultPlayer.isPlaying()){
            mResultPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mResultPlayer !=null && !mResultPlayer.isPlaying()){
            mResultPlayer.start();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(ResultActivity.this)
                //.setMessage(R.string.tutorial_msg)
                .setMessage(R.string.dialog_result_exit_withoutsave)
                .setPositiveButton(R.string.tutorial_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent playIntent = new Intent(ResultActivity.this,MainActivity.class);
                        startActivity(playIntent);
                    }
                })
                .setNegativeButton(R.string.tutorial_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).create().show();
    }
}
