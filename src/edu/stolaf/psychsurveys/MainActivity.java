package edu.stolaf.psychsurveys;

import java.util.ArrayList;

import android.widget.LinearLayout.LayoutParams;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);            
                Log.i("PsychSurveys", "Activity started");
                (new Globals(getApplicationContext())).schedulePsychSurveysComponents();
                // creating LinearLayout
                LinearLayout linLayout = new LinearLayout(this);
                // specifying vertical orientation
                linLayout.setOrientation(LinearLayout.VERTICAL);
                // creating LayoutParams                  
                LinearLayout.LayoutParams marginParams = new LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                marginParams.leftMargin = 16;
                marginParams.topMargin = 16;
          
                SharedPreferences prefs = getSharedPreferences(RepeatingTask.tag, 0);
                String reply = prefs.getString(Globals.question, "");
                Editor editor = prefs.edit();
                editor.remove(Globals.question);
                editor.commit();
                
                //TODO: split into main/survey activities
                
                String question = "Thanks for manually starting Psych Surveys. We're now reporting back to the researchers.";
                ArrayList<String> answers = new ArrayList<String>();
                
                if ( ! reply.equals("")) {
                	//parse json
                	question = reply;
                }
                
                TextView tv = new TextView(this);
                tv.setText(question);
                tv.setLayoutParams(marginParams);
                linLayout.addView(tv);
                
                // set LinearLayout as a root element of the screen 
                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT); 
                setContentView(linLayout, layoutParams);                                
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                // Inflate the menu; this adds items to the action bar if it is present.
                getMenuInflater().inflate(R.menu.main, menu);
                return true;
        }

}