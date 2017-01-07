package com.vocabbuilder.brandjamie.vocabbuilder;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


public class MainActivity extends AppCompatActivity  {
    ArrayList vocabListEng = new ArrayList <VocabItem> ();
    ArrayList vocabListArr = new ArrayList <VocabItem> ();
    List weeks;
    VocabItem currItem;
    Boolean currItemAnswered;
    Boolean translit;
    TextView questionTextView;
    CardView questionCardView;
    TextView[] answerTextViews = new TextView[4];
    CardView[] answerCardViews = new CardView[4];
    TextView scoreTextView;
    TextView hiscoreTextView;
    Context context = this;
    SharedPreferences sharedPref;
    SharedPreferences sharedPrefd;
    SharedPreferences.Editor editor;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    //= this.getPreferences(Context.MODE_PRIVATE);
    //SharedPreferences sharedPref = context.getSharedPreferences(
    //        getString(R.string.preference_file_key), Context.MODE_PRIVATE);



    List <ObjectAnimator> animators;
    int score;
    int hiscore;
    class VocabItem {
        String lang;
        int week;
        int day;
        String question;
        String answer;
        String trans;

        VocabItem(String lang, int week, int day, String question, String answer, String trans) {
            this.lang = lang;
            this.week = week;
            this.day = day;
            this.question = question;
            this.answer = answer;
            this.trans = trans;
        }
        String getLang () {
            return(lang);
        }
        int getWeek () {
            return(week);
        }
        int getDay () { return (day); }
        String getQuestion () {
            return(question);
        }
        String getAnswer () {
            return(answer);
        }
        String getTrans () {
            return(trans);
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        InputStream is = null;
        animators = new ArrayList<ObjectAnimator>();
        context = this;
        // read saved data
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        sharedPrefd = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();
        editor.clear();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

                        reload();

                    }
                };
        sharedPrefd.registerOnSharedPreferenceChangeListener(listener);

        scoreTextView = (TextView) findViewById(R.id.Score);
        hiscoreTextView = (TextView) findViewById(R.id.HiScore);
        Boolean daily = false;
        Boolean accumulative = false;

        score = sharedPref.getInt(getString(R.string.saved_score), 0);

        hiscore = sharedPref.getInt(getString(R.string.saved_high_score), 0);
        //String vocabpref = sharedPref.getString("vocabChoice", "10");

        int vocabpref = Integer.parseInt(sharedPrefd.getString("vocabChoice", "1"));
        translit = sharedPrefd.getBoolean("transliteration",false);






        if (vocabpref ==0 ) {
            daily = true;
            accumulative = false;
        } else if (vocabpref ==1) {
            daily = false;
            accumulative = false;
        } else if (vocabpref == 2) {
            daily = true;
            accumulative = true;

        } else  {
            daily = false;
            accumulative = true;
        }
        setScoreViews();
        setHiScore();
        Date c = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyMMdd");
        int todaysdate = Integer.parseInt(dateFormatter.format(c));




        // default items to allow ///////////////////////////////

        int num_of_weeks = 5;
        int currentWeek = 0;
        int currentDay = 0;
        boolean[] weekIncluded = new boolean[num_of_weeks+1];
        // could do this better using calender
        int[] weekstarts = new int[5];
        weekstarts[0] = 161014;
        weekstarts[1] = 161021;
        weekstarts[2] = 161028;
        weekstarts[3] = 161104;
        weekstarts[4] = 161111;

        for (int i = 1; i< num_of_weeks; i++) {
            if (todaysdate > weekstarts[i]) {

                currentWeek = i;
                // should move to next weeks vocab on Friday but only onto second day of vocab on Monday
                currentDay = todaysdate - (weekstarts[i]+2);
                if (currentDay < 0) {
                    currentDay = 0;
                }
            }

        }

        //currentWeek = 3;
        //currentDay = 2;


        // only get vocab on the first 4 days.
        int num_of_days = 4;
        currentDay = Math.min(currentDay,num_of_days-1);

        //daily = false;
        boolean[][] weeks = new boolean[num_of_weeks][];
        for (int i = 0; i < num_of_weeks; i++) {
            boolean[] thisweek = new boolean[4];

            if (currentWeek < i) {
                weekIncluded[i] = false;
            } else if (currentWeek > i && accumulative == false) {
                weekIncluded[i] = false;
            } else {
                weekIncluded[i] = true;
            }



            for (int j = 0; j < num_of_days; j++) {

                if (accumulative == true && currentWeek > i) {
                    thisweek[j] = true;
                } else if (currentWeek == i && currentDay > j && accumulative == true) {
                    thisweek[j] = true;
                } else if (currentWeek == i && currentDay == j) {
                    thisweek[j] = true;
                }


            }
            weeks[i] = thisweek;
        }




// add questions
        questionTextView = (TextView)findViewById(R.id.question);
        answerTextViews[0] = (TextView)findViewById(R.id.answerA);
        answerTextViews[1] = (TextView)findViewById(R.id.answerB);
        answerTextViews[2] = (TextView)findViewById(R.id.answerC);
        answerTextViews[3] = (TextView)findViewById(R.id.answerD);
        questionCardView = (CardView) findViewById(R.id.qcard);
        answerCardViews[0] = (CardView)findViewById(R.id.carda);
        answerCardViews[1] = (CardView)findViewById(R.id.cardb);
        answerCardViews[2] = (CardView)findViewById(R.id.cardc);
        answerCardViews[3] = (CardView)findViewById(R.id.cardd);

        try {
            is = assetManager.open("fall(a)vocab.csv");
        } catch (IOException e) {
            System.err.println("error opening file");
            //e.printStackTrace();
        }

        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));


        String line = "";
        StringTokenizer st = null;
        try {

            while ((line = reader.readLine()) != null) {
                st = new StringTokenizer(line, ",");
                String lang = "English";
                int week = Integer.parseInt(st.nextToken());
                int day = Integer.parseInt(st.nextToken());
                String question = st.nextToken();
                String answer = st.nextToken();
                String trans = st.nextToken();
                boolean includeItem = false;
                if (daily == true && weeks[week-1][day-1] == true) {
                    includeItem = true;
                } else if (daily != true && weekIncluded[week-1]==true){
                    includeItem = true;
                }
                if (includeItem) {



                    VocabItem vocabItem = new VocabItem(lang, week, day, question, answer, trans);

                    vocabListEng.add(vocabItem);
                    lang = "Arabic";
                    vocabItem = new VocabItem(lang, week, day, answer, question, trans);
                    vocabListArr.add(vocabItem);
                }
            }
        } catch (IOException e) {
            System.err.println("error tokenising");
           // e.printStackTrace();
        }
        if (vocabListEng.size() > 4) {
            setQuestion();
        } else {
            setNoQuestion();

        }
        // create buttons
        View qcard = (View) findViewById(R.id.qcard);

        qcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextQuestion();
            }
        });
        View carda = (View) findViewById(R.id.carda);

        carda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currItemAnswered) {
                    giveAnswer(v, 0);
                } else {
                    nextQuestion();
                }
            }
        });

        View cardb = (View) findViewById(R.id.cardb);

        cardb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currItemAnswered) {
                    giveAnswer(v, 1);
                } else {
                    nextQuestion();
                }
            }
        });
        View cardc = (View) findViewById(R.id.cardc);

        cardc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currItemAnswered) {
                    giveAnswer(v, 2);
                } else {
                    nextQuestion();
                }
            }
        });
        View cardd = (View) findViewById(R.id.cardd);

        cardd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currItemAnswered) {
                    giveAnswer(v,3);
                } else {
                    nextQuestion();
                }
            }
        });





    }
    // restart the activity after preferences changed.
    void reload () {
        this.recreate();

    }

    //@Override
    //protected void onResume() {
      //  super.onResume();
        //sharedPrefd.registerOnSharedPreferenceChangeListener(listener);
   // }

    //@Override
    //protected void onPause() {
    //    super.onPause();
    //    sharedPrefd.unregisterOnSharedPreferenceChangeListener(listener);
    //}
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, PreferencesActivity.class);
                startActivity(i);

                return true;
            default:

                return super.onOptionsItemSelected(item);
        }
    }


    List<Integer> pickRandomItems (int n) {
        List<Integer> itemNums = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            itemNums.add(i);
        }

        Collections.shuffle(itemNums);
        return itemNums.subList(0,4);
    }

    void setQuestion () {
        List<VocabItem> thisItemList;
        boolean arabic = false;
        if (Math.random() > 0.5) {
            thisItemList = vocabListEng;
        } else {
            thisItemList = vocabListArr;
            arabic = true;
        }
        currItemAnswered = false;
        List<Integer> answerNums = pickRandomItems(thisItemList.size());
        List<String> answers = new ArrayList<String>();
        String correctAnswer;
        currItem = (VocabItem) thisItemList.get(answerNums.get(0));
        correctAnswer = currItem.getAnswer();
        if (translit == true && !arabic) {
            correctAnswer = correctAnswer + "\n"+currItem.getTrans();
        }
        answers.add(correctAnswer);
        VocabItem thisA = (VocabItem) thisItemList.get(answerNums.get(1));
        String thisanswer = thisA.getAnswer();
        if (translit == true && !arabic) {
            thisanswer = thisanswer + "\n"+thisA.getTrans();
        }
        answers.add(thisanswer);
        thisA = (VocabItem) thisItemList.get(answerNums.get(2));
        thisanswer = thisA.getAnswer();
        if (translit == true && !arabic) {
            thisanswer = thisanswer + "\n"+thisA.getTrans();
        }
        answers.add(thisanswer);
        thisA = (VocabItem) thisItemList.get(answerNums.get(3));
        thisanswer = thisA.getAnswer();
        if (translit == true && !arabic) {
            thisanswer = thisanswer + "\n"+thisA.getTrans();
        }
        answers.add(thisanswer);


        Collections.shuffle(answers);

        for (int i = 0; i < 4; i++) {

            answerTextViews[i].setText(answers.get(i));
        }

        String questiontext = currItem.getQuestion();
        if (translit == true && arabic == true) {
            questiontext = questiontext + "\n"+currItem.getTrans();
        }
        questionTextView.setText(questiontext);

    }
    void setScoreViews () {
        scoreTextView.setText(Integer.toString(score));
        if (score > hiscore) {
            setHiScore ();
        }
        editor.putInt(getString(R.string.saved_score), score);
        editor.putInt(getString(R.string.saved_high_score), hiscore);

        editor.commit();
        editor.clear();

    }
    void setHiScore () {




        if (score > hiscore) {
           hiscore = score;
            editor.putInt(getString(R.string.saved_high_score), hiscore);
            editor.commit();
            editor.clear();

        }
        String hi = "Hi Score:\n" + Integer.toString(hiscore);
        hiscoreTextView.setText(hi);
        //editor.putInt(getString(R.string.saved_high_score), hiscore);
        //editor.commit();
       // editor.clear();
    }

    void setNoQuestion () {

        questionTextView.setText("No vocab items available");
    }
    void clearColors() {
        ObjectAnimator animator = ObjectAnimator.ofInt(questionCardView, "backgroundColor", ContextCompat.getColor(this, R.color.cardview_dark_background), ContextCompat.getColor(this, R.color.cardview_light_background)).setDuration(300);
        animator.setEvaluator(new ArgbEvaluator());
        animator.start();
        animators.add(animator);
        for (int i = 0; i < 4; i++ ) {
            animator = ObjectAnimator.ofInt(answerCardViews[i], "backgroundColor", ContextCompat.getColor(this, R.color.cardview_dark_background), ContextCompat.getColor(this, R.color.cardview_light_background)).setDuration(300);
            animator.setEvaluator(new ArgbEvaluator());
            animator.start();

        }
    }

    void clearAnimations() {
        for (int i = 0; i < animators.size(); i++) {
            animators.get(i).cancel();
        }

    }
    void nextQuestion() {
        clearAnimations();
        clearColors();
        setQuestion();
    }
    void giveAnswer(View v, int ans_id) {
        boolean result;
        currItemAnswered = true;
        int corrAnswer = 0;
        //CharSequence answer = answerTextViews[ans_id].getText();
        CharSequence answer = currItem.getAnswer();
        if (currItem.getLang() == "English" && translit == true) {
            answer = answer + "\n" + currItem.getTrans();
        }
        if (((String) answerTextViews[ans_id].getText()).equals((String) answer)) {
           result = true;
        } else {
            result = false;
            for (int i = 0; i<4; i++) {
                if (((String) answerTextViews[i].getText()).equals((String) answer)) {
                    corrAnswer = i;
                }
            }
        }

        if (result) {
            ObjectAnimator animator = ObjectAnimator.ofInt(questionCardView, "backgroundColor", ContextCompat.getColor(this, R.color.cardview_light_background), ContextCompat.getColor(this, R.color.colorCorrectGreen)).setDuration(1500);
            animator.setEvaluator(new ArgbEvaluator());
            animator.start();
            animators.add(animator);
            animator = ObjectAnimator.ofInt(v, "backgroundColor", ContextCompat.getColor(this, R.color.cardview_light_background), ContextCompat.getColor(this, R.color.colorCorrectGreen)).setDuration(1500);
            animator.setEvaluator(new ArgbEvaluator());
            animator.start();
            animators.add(animator);
            score += 1;
            setScoreViews();
        } else {
            ObjectAnimator animator = ObjectAnimator.ofInt(questionCardView, "backgroundColor", ContextCompat.getColor(this, R.color.cardview_light_background), ContextCompat.getColor(this, R.color.colorIncorrectRed)).setDuration(1500);
            animator.setEvaluator(new ArgbEvaluator());
            animator.start();
            animators.add(animator);
            animator = ObjectAnimator.ofInt(v, "backgroundColor", ContextCompat.getColor(this, R.color.cardview_light_background), ContextCompat.getColor(this, R.color.colorIncorrectRed)).setDuration(1500);
            animator.setEvaluator(new ArgbEvaluator());
            animator.start();
            animators.add(animator);
            animator = ObjectAnimator.ofInt(answerCardViews[corrAnswer], "backgroundColor", ContextCompat.getColor(this, R.color.cardview_light_background), ContextCompat.getColor(this, R.color.colorCorrectGreen)).setDuration(1500);
            animator.setEvaluator(new ArgbEvaluator());
            animator.start();
            animators.add(animator);
            score = 0;
            setScoreViews();
        }

    }

}
