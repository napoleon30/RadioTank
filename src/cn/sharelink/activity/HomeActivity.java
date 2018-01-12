package cn.sharelink.activity;


import java.util.Timer;
import java.util.TimerTask;

import com.xinlian.radio_tank_t_72.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import cn.sharelink.use.AppUtil;
import cn.sharelink.view.RtspVideoView;

/* 
 * 第一个logo界面
 */
public class HomeActivity extends BaseActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_home);
		
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				Intent intent = new Intent(HomeActivity.this,
						HomeActivity2.class);
				startActivity(intent);

			}
		};

		new Timer().schedule(task, 2000);  //2s后跳转到第二个logo界面
	}
	
	
}
