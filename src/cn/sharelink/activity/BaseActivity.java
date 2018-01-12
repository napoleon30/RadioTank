package cn.sharelink.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity{
	
	static List<Activity> activities = new ArrayList<Activity>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		activities.add(this);
	}

	public static void finishAll(){
		for(Activity act : activities){
			act.finish();
		}
	}
}
