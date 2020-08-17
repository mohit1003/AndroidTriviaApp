package com.bawp.trivia;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bawp.trivia.data.AnswerListAsyncResponse;
import com.bawp.trivia.data.QuestionBank;
import com.bawp.trivia.model.Question;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView questionTextview;
    private TextView questionCounterTextview;
    private Button trueButton;
    private Button falseButton;
    private ImageButton nextButton;
    private ImageButton prevButton;
    private TextView points;
    private int currentQuestionIndex = 0;
    private List<Question> questionList;
    SoundPool soundpool;
    public static int MAX_STREAMS = 4;
    public static int SOUND_PRIORITY = 1;
    public static int SOUND_QUALITY = 100;
    public int pointScored = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        points = findViewById(R.id.points);
        nextButton = findViewById(R.id.next_button);
        prevButton = findViewById(R.id.prev_button);
        trueButton = findViewById(R.id.true_button);
        falseButton = findViewById(R.id.false_button);
        questionCounterTextview = findViewById(R.id.counter_text);
        questionTextview = findViewById(R.id.question_textview);

        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        trueButton.setOnClickListener(this);
        falseButton.setOnClickListener(this);

        SharedPreferences saveQuestion = getSharedPreferences("saveQuestion", MODE_PRIVATE);
        currentQuestionIndex = saveQuestion.getInt("saveQuestion", 0);

        SharedPreferences sharedPreferences = getSharedPreferences("score", MODE_PRIVATE);
        pointScored = sharedPreferences.getInt("score", 0);
        points.setText(Integer.toString(pointScored));




        questionList = new QuestionBank().getQuestions(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Question> questionArrayList) {


                questionTextview.setText(questionArrayList.get(currentQuestionIndex).getAnswer());
                questionCounterTextview.setText(currentQuestionIndex + " / " + questionArrayList.size()); // 0 / 234
//                Log.d("Inside", "processFinished: " + questionArrayList);

            }
        });

        // Log.d("Main", "onCreate: " + questionList);


    }

    @Override
    protected void onPause() {
        super.onPause();
        saveQuestion();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prev_button:
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex = (currentQuestionIndex - 1) % questionList.size();
                    updateQuestion();
                }
                break;
            case R.id.next_button:
                currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
                updateQuestion();
                break;
            case R.id.true_button:
                checkAnswer(true);
                currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
                updateQuestion();
                break;
            case R.id.false_button:
                checkAnswer(false);
                currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
                updateQuestion();
                break;
        }

    }

    public void saveResults(){
        SharedPreferences sharedPreferences = getSharedPreferences("score", MODE_PRIVATE);
        SharedPreferences.Editor editScore = sharedPreferences.edit();
        editScore.putInt("score", pointScored);
        editScore.apply();

    }

    private void checkAnswer(boolean userChooseCorrect) {
        boolean answerIsTrue = questionList.get(currentQuestionIndex).isAnswerTrue();
        int toastMessageId = 0;
        if (userChooseCorrect == answerIsTrue) {
            
            fadeView();
            toastMessageId = R.string.correct_answer;
            pointScored += 10;
        } else {
            shakeAnimation();
            toastMessageId = R.string.wrong_answer;
            if(pointScored > 0)
                pointScored -= 10;
        }
        Toast.makeText(MainActivity.this, toastMessageId,
                Toast.LENGTH_SHORT)
                .show();

    }

    private void saveQuestion(){
        SharedPreferences saveQuestion = getSharedPreferences("saveQuestion", MODE_PRIVATE);
        SharedPreferences.Editor questionEditor = saveQuestion.edit();
        questionEditor.putInt("saveQuestion", currentQuestionIndex);
        questionEditor.apply();
    }

    private void updateQuestion() {
        String question = questionList.get(currentQuestionIndex).getAnswer();
        questionTextview.setText(question);
        questionCounterTextview.setText(currentQuestionIndex + " / " + questionList.size()); // 0 / 234

        points.setText(Integer.toString(pointScored));
        saveResults();

    }

    private void fadeView() {
        final CardView cardView = findViewById(R.id.cardView);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

        alphaAnimation.setDuration(350);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        cardView.setAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void shakeAnimation() {
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this,
                R.anim.shake_animation);
        final CardView cardView = findViewById(R.id.cardView);
        cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.RED);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }
}
