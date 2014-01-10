package edu.stolaf.psychsurveys;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpResponseHandler;

import android.widget.LinearLayout.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

class QuestionHandler extends AsyncHttpResponseHandler {	
	
	public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) {
		try {			
			Globals.json = new JSONObject(new String(responseBody));																
			SurveyActivity.start();
		} catch (Exception e) {
			RepeatingTask.dragnet(e);			
		}
	}
	
	public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable throwable) {		
		RepeatingTask.error("Status code " + Integer.toString(statusCode), throwable);
	}
}

class AnswerListener implements OnClickListener {
	
	int answer, question;
	
	public AnswerListener(int ans, int que) {
		answer = ans;
		question = que;
	}
	
	@Override
	public void onClick(View view) {
		try {			
			if (question == Globals.json.getInt("idNum")) {
				String idNum = Integer.toString(Globals.json.getInt("idNum"));
				String url = Globals.cgi + "?question=" + idNum + "&answer=" + Integer.toString(answer);			
				Globals.client.get(url, new QuestionHandler());
			} else {
				SurveyActivity.start();
			}
			
		} catch (Exception e) {
			RepeatingTask.dragnet(e, SurveyActivity.context);
		}
	}
}

public class SurveyActivity extends Activity {
	
	public static Context context;
	
	public static void start() {
		Intent intent = new Intent(SurveyActivity.context, SurveyActivity.class);								
		SurveyActivity.context.startActivity(intent);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);            
            Log.i("PsychSurveys", "Survey started");
            context = this;
            LinearLayout linLayout = new LinearLayout(this);
            linLayout.setOrientation(LinearLayout.VERTICAL);                                                          
            
            LinearLayout.LayoutParams marginParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            marginParams.leftMargin = 16;
            marginParams.topMargin = 16;
                        
            TextView tv = new TextView(this);
            tv.setText(Globals.json.getString("text"));
            tv.setLayoutParams(marginParams);
            linLayout.addView(tv);            
            
			JSONArray answers = Globals.json.getJSONArray("answers");
			for (int i=0; i < answers.length(); i++) {
				Button btn = new Button(this);
		        btn.setText(answers.getString(i));
		        //btn.setLayoutParams(marginParams);		        		       
		        btn.setOnClickListener(new AnswerListener(i, Globals.json.getInt("idNum")));
		        linLayout.addView(btn);
			}
			
            // set LinearLayout as a root element of the screen 
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            ScrollView scrollView = new ScrollView(this);
            scrollView.addView(linLayout);
            setContentView(scrollView, layoutParams);
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