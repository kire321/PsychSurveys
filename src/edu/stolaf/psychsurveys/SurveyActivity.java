package edu.stolaf.psychsurveys;

import org.json.JSONArray;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;

import android.widget.LinearLayout.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class SurveyActivity extends Activity {
	
	public static Context context;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);            
            Log.i("PsychSurveys", "Survey started");
            context = this;
            LinearLayout linLayout = new LinearLayout(this);
            linLayout.setOrientation(LinearLayout.VERTICAL);
            
            SharedPreferences prefs = getSharedPreferences(RepeatingTask.tag, 0);
            String reply = prefs.getString(Globals.question, "");
            Editor editor = prefs.edit();
            editor.remove(Globals.question);
            
            
            LinearLayout.LayoutParams marginParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            marginParams.leftMargin = 16;
            marginParams.topMargin = 16;
            
            JSONObject question = new JSONObject(reply);
            TextView tv = new TextView(this);
            tv.setText(question.getString("text"));
            tv.setLayoutParams(marginParams);
            linLayout.addView(tv);
            editor.putInt("idNum", question.getInt("idNum"));
            editor.commit();
            
			JSONArray answers = question.getJSONArray("answers");
			for (int i=0; i < answers.length(); i++) {
				Button btn = new Button(this);
		        btn.setText(answers.getString(i));
		        btn.setLayoutParams(marginParams);
		        btn.setId(i+1);		       
		        btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						String idNum = Integer.toString(getSharedPreferences(RepeatingTask.tag, 0).getInt("idNum", 0));
						(new AsyncHttpClient()).get(Globals.cgi + "?question=" + idNum + "&answer=" + Integer.toString(view.getId() - 1), new ExceptionHandlingResponseHandler(Measurer.wakeLock) {
							public void handle(String response) throws Exception {																	
								Editor editor = SurveyActivity.context.getSharedPreferences(Measurer.tag, 0).edit();
								editor.putString(Globals.question, response);
								editor.commit();
								
								Intent intent = new Intent(SurveyActivity.context, SurveyActivity.class);								
								SurveyActivity.context.startActivity(intent);
							}
						});
					}
				});
		        linLayout.addView(btn);
			}
			
			
            
            
            
            
            // set LinearLayout as a root element of the screen 
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT); 
            setContentView(linLayout, layoutParams);
		} catch (Exception e) {
			RepeatingTask.dragnet(e, this);
		}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
    }

}