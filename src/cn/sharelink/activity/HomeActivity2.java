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
import cn.sharelink.use.AppUtil;
import cn.sharelink.view.RtspVideoView;


public class HomeActivity2 extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_home2);
	
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
					Intent intent = new Intent(HomeActivity2.this,
						PlayActivity.class);
				startActivity(intent);
				
				
			}
		};

		new Timer().schedule(task, 1000);//1s后跳转到视频显示界面
	}
	
	
}
