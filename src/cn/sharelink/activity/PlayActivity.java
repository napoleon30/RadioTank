package cn.sharelink.activity;

import java.util.List;

import com.xinlian.radio_tank_t_72.R;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import cn.sharelink.use.AppUtil;
import cn.sharelink.use.ControlMsg;
import cn.sharelink.use.DensityUtils;
import cn.sharelink.use.HttpThread;
import cn.sharelink.use.PlayVoice;
import cn.sharelink.view.MenuButton;
import cn.sharelink.view.MenuButton.MenuButtonOnClickListener;
import cn.sharelink.view.MyToast;
import cn.sharelink.view.RockerView;

import cn.sharelink.view.RtspVideoView;

/**
 * @author ChenJun video显示、飞行控制的Activity
 * 
 */
public class PlayActivity extends BaseActivity implements OnLongClickListener,
		OnClickListener {

	private final static String TAG = "DEBUG/VideoPlayerActivity";

	// HTTP通信的控制参数
	private static final int HTTP_START = HttpThread.HTTP_START;
	private static final int HTTP_SET_TIME = HttpThread.HTTP_SET_TIME;
	private static final int HTTP_CHECK_STROAGE = HttpThread.HTTP_CHECK_STROAGE;
	private static final int HTTP_BRIDGE = HttpThread.HTTP_BRIDGE;
	private static final int HTTP_TAKEPHOTO = HttpThread.HTTP_TAKEPHOTO;
	private static final int HTTP_START_RECORD = HttpThread.HTTP_START_RECORD;
	private static final int HTTP_STOP_RECORD = HttpThread.HTTP_STOP_RECORD;
	private static final int HTTP_GET_PRIVILEGE = HttpThread.HTTP_GET_PRIVILEGE;
	private static final int HTTP_RELEASE_PRIVILEGE = HttpThread.HTTP_RELEASE_PRIVILEGE;
	// 各个窗口的layout定义
	private RelativeLayout mLayoutView_menu; // 菜单栏的layout
	private RelativeLayout mLayoutView_rocker; // 摇杆的layout
	private RelativeLayout mLayoutView_trim; // 微调的layout
	private RelativeLayout mLayoutView_screen; // 屏幕, 用于监听触摸屏幕
	private RelativeLayout relativeLayout1;

	// mLayoutView_videoView中的控件4
	private RtspVideoView mVideoView;

	// mLayoutView_menu中的控件
	private MenuButton mBtn_exit; // 退出按钮
	private MenuButton mBtn_snapShot; // 截图按钮
	private MenuButton mBtn_record; // 录像按钮
	private MenuButton mBtn_playback; // 查看本地文件按钮
	// private MenuButton mBtn_SDRecord; // 远程SD卡录像按钮
	private MenuButton mBtn_lock; // 锁定按钮
	private MenuButton mBtn_setting; // 设置按钮

	// mLayoutView_rocker中的控件
	private RockerView mRocker_left; // 左侧摇杆
	private RockerView mRocker_right; // 右侧摇杆

	private boolean isSDRecording = false; // 正在录像
	private boolean isRecording = false; // 正在本地录像
	private boolean isStartRecord = false; // 已启动本地录像
	private long mSDRecord_startTime = 0; // 录像开始时间
	private long mRecord_startTime = 0; // 本地录像开始时间

	private MyToast mToast; // 定义Toast

	public ControlMsg mCtlMsg; // 飞控控制数据

	private boolean haveSDcard = false; // 摄像头模块有无SDcard

	private ListenRecordThread listenRecordThread = new ListenRecordThread(); // 监听录像线程
	private HttpThread bridgeThread = null; // HTTP桥接线程

	private String mAuthcode; // 控制权限的数据

	private boolean isHideAllView = false;
	private boolean isCountDown_HideAllView = true; // 可以倒计时
	private static final int s_TotalTime_HideAllView = 60;
	private int mTime_HideAllView = s_TotalTime_HideAllView;
	private int mLanguage;
	private boolean isOpenControl = true;
	private int mControlMode;
	private int mFlipImage = 1;
	private int mStroageLocaltion;

	private PlayVoice mPlayVoice;

	private Button btn_gun;
	private Button btn_cannon;
	private Button btn_power;

	private MyMenuOnClickListener myMenuOnClickListener;
	private MyButtonOnClickListener myButtonOnClickListener;
	private MyRockerChangeListener myRockerChangeListener;

	private PopupWindow mPopu;
	int setWidth;
	int setHeight;

	boolean isConnected = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_IMMERSIVE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 设置全屏 , 屏幕长亮

		setContentView(R.layout.activity_play);

		initView(); // 初始化activity组件

		Log.e(TAG, "onCreate");
		mToast = new MyToast(this);
		mPlayVoice = new PlayVoice(this);
		mVideoView = (RtspVideoView) findViewById(R.id.rtsp_videoView);

		Log.e("test", "==========" + isWifiConnected());
		if (isWifiConnected()) {
			// 开启已影像 参数RTSP_URL_nHD为640*368，若获取720p影像设参数RTSP_URL_720P
			mVideoView.setVideo(AppUtil.RTSP_URL_nHD, videoEventHandler);

			// 创建并开启bridgeThread线程
			bridgeThread = new HttpThread(HTTP_START, HTTP_handler);
			bridgeThread.start(); // 获取控制权限-->设置时间-->检测SD卡-->发送控制数据

			listenRecordThread.start(); // 开启录像监听线程
			isConnected = true;

		} else {
			isConnected = false;
			mToast.showToast("Connection failed");
		}

		mCtlMsg = ControlMsg.getInstance();

		mLanguage = AppUtil.s_Language;

		// control_lockAndHighLimit(0); // 开启定高
		changeTextLanguage(mLanguage); // 修改语言

	}

	/** Called when the activity is first created. */
	public void initView() {

		/********* 自定义按钮出发事件 ***********/

		myMenuOnClickListener = new MyMenuOnClickListener();
		myButtonOnClickListener = new MyButtonOnClickListener();
		myRockerChangeListener = new MyRockerChangeListener();

		/********* layout_menu 中的控件 ***********/
		mLayoutView_menu = (RelativeLayout) findViewById(R.id.layoutView_menu);
		mBtn_snapShot = (MenuButton) findViewById(R.id.btn_snapshot);
		mBtn_record = (MenuButton) findViewById(R.id.btn_record);
		mBtn_playback = (MenuButton) findViewById(R.id.btn_playback);
		mBtn_setting = (MenuButton) findViewById(R.id.btn_setting);

		mBtn_snapShot.setMenuOnClickListener(myMenuOnClickListener);
		mBtn_record.setMenuOnClickListener(myMenuOnClickListener);
		mBtn_playback.setMenuOnClickListener(myMenuOnClickListener);
		mBtn_setting.setMenuOnClickListener(myMenuOnClickListener);

		/************************** 底部button功能按钮 ****************************************/
		btn_gun = (Button) findViewById(R.id.ib_machinegun);
		btn_cannon = (Button) findViewById(R.id.ib_cannon_en);
		btn_power = (Button) findViewById(R.id.ib_power_en);
		relativeLayout1 = (RelativeLayout) findViewById(R.id.relativeLayout1);

		btn_gun.setOnLongClickListener(this);
		btn_gun.setOnClickListener(myButtonOnClickListener);
		btn_cannon.setOnClickListener(myButtonOnClickListener);
		btn_power.setOnClickListener(myButtonOnClickListener);

		/********* layout_rocker 中的控件 ***********/
		mLayoutView_rocker = (RelativeLayout) findViewById(R.id.layoutView_rocker);
		mRocker_left = (RockerView) findViewById(R.id.rocker_left);
		mRocker_right = (RockerView) findViewById(R.id.rocker_right);

		mRocker_left.setRockerChangeListener(myRockerChangeListener);
		mRocker_right.setRockerChangeListener(myRockerChangeListener);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.iv_en:
			mPopu.dismiss();
			displaypop = false;
			btn_cannon.setBackgroundResource(R.drawable.btn_connon00);
			btn_gun.setBackgroundResource(R.drawable.btn_gun00);
			btn_power.setBackgroundResource(R.drawable.btn_power00);
			AppUtil.s_Language = 0;
			break;
		case R.id.iv_russia:
			mPopu.dismiss();
			displaypop = false;
			btn_cannon.setBackgroundResource(R.drawable.btn_connon01);
			btn_gun.setBackgroundResource(R.drawable.btn_gun01);
			btn_power.setBackgroundResource(R.drawable.btn_power01);
			AppUtil.s_Language = 1;
			break;
		case R.id.iv_b:
			mPopu.dismiss();
			displaypop = false;
			btn_cannon.setBackgroundResource(R.drawable.btn_connon02);
			btn_gun.setBackgroundResource(R.drawable.btn_gun02);
			btn_power.setBackgroundResource(R.drawable.btn_power02);
			AppUtil.s_Language = 2;
			break;
		}

	}

	private void popup() {
		View contentView = LayoutInflater.from(PlayActivity.this).inflate(
				R.layout.language_set_pop, null);
		mPopu = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mPopu.setContentView(contentView);
		mPopu.setBackgroundDrawable(new BitmapDrawable());
		mPopu.setFocusable(false);
		mPopu.setOutsideTouchable(true);
		// 设置监听
		mPopu.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE
						&& !mPopu.isFocusable()) {
					// 如果焦点不在popupWindow上，且点击了外面，不再往下dispatch事件：
					// 不做任何响应,不 dismiss popupWindow
					return true;
				}
				// 否则default，往下dispatch事件:关掉popupWindow，
				return false;
			}
		});

		ImageView flagEn = (ImageView) contentView.findViewById(R.id.iv_en);
		ImageView flagB = (ImageView) contentView.findViewById(R.id.iv_b);
		ImageView flagRussia = (ImageView) contentView
				.findViewById(R.id.iv_russia);

		flagEn.setOnClickListener(this);
		flagB.setOnClickListener(this);
		flagRussia.setOnClickListener(this);
		displaypop = true;
		
		 contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		 int xOffset = mBtn_setting.getWidth() / 2 - contentView.getMeasuredWidth() / 2;
//		mPopu.showAsDropDown(mBtn_setting);
		 mPopu.showAsDropDown(mBtn_setting, xOffset, 0);  

	}

	@Override
	public boolean onLongClick(View v) {

		if (v.getId() == R.id.ib_machinegun) {
			// 机关枪按鍵 长按事件

			mCtlMsg.setGunbtn(1);
			// mHandler.sendEmptyMessage(5);
			Log.e("==>>>>>>>", "changan");
		}

		return false;
	}

	/*
	 * Activity得到或者失去焦点的时候 就会调用该方法实现里面的方法
	 * 
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		// changeControlMode();
	}

	private boolean isWifiConnected() {

		boolean flag = false;
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String ssid = wifiInfo.getSSID().trim();
		// 部分安卓4.1手机只支持ssid.startsWith("Skycam")调用
		if (ssid.startsWith("Skycam", 1) || ssid.startsWith("Skycam")
				|| ssid.startsWith("TANK") || ssid.startsWith("TANK", 1)) {
			flag = true;
		} else {
			flag = false;
		}

		return flag;
	}

	/**
	 * 　录像监听线程
	 */
	class ListenRecordThread extends Thread {
		/*
		 * 开启一个单独的工作线程来监听录像 循环监听 设置isRun为true
		 */
		public boolean isRun;

		@Override
		public void run() {
			isRun = true;
			while (isRun) {

				if (mVideoView.videoIsRecording()) {
					mHandler.sendEmptyMessage(0);// 如果已经启动录制，则设置msg.what=0
				} else {
					mHandler.sendEmptyMessage(1);// 如果没有启动录制，则设置msg.what=1
					mRecord_startTime = System.currentTimeMillis(); // 记录本地录像的启动时间
				}
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 　用于处理录像信息的Handler 和 清除mCtlMsg byte[5]
	 */
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: // 正在本地录像
				isRecording = true;
				setRecordTime(mBtn_record, mRecord_startTime);
				break;

			case 1: // 停止本地录像
				isRecording = false;
				mBtn_record.setText("");
				break;
			case 2:
				mCtlMsg.setConnonBtn(0);
				Log.i("mHandler", "0.1后清除");
				break;
			case 3:
				mCtlMsg.setGunbtn(0);
				Log.i("mHandler", "0.1后清除");
				break;
			case 4:
				mCtlMsg.setPowerBtn(0);
				Log.i("mHandler", "0.1后清除");
				break;
			case 5:
				mCtlMsg.setGunbtn(0);
				Log.i("mHandler", "长按松开清除");
				break;

			default:
				break;
			}

			if (isSDRecording) { // 正在录像
				// setRecordTime(mBtn_SDRecord, mSDRecord_startTime); //
				// 修改mTv_record
			} else {
				mSDRecord_startTime = 0;
			}

		}

		/**
		 * 设置录像时间TextView
		 * 
		 * @param tv
		 *            需要设置的TextView
		 * 
		 * @param startTime
		 *            录制开始时间
		 * 
		 */
		private void setRecordTime(MenuButton btn, long startTime) {
			int time = (int) ((System.currentTimeMillis() - startTime) / 1000); // 总时间，单位s
			String sTime = String.format("%02d", time / 60) + ":"
					+ String.format("%02d", time % 60);
			btn.setText(1, sTime);

		}
	};

	/**
	 * RtspVideoView通过该Handler反馈View的状态信息
	 */
	Handler videoEventHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			RtspVideoView.HandlerMsg handlerMsg = (RtspVideoView.HandlerMsg) msg.obj;
			boolean isSuccess = handlerMsg.isSuccess;
			switch (msg.what) {
			case RtspVideoView.START_RTSP:
				if (!isSuccess) {
					mToast.showToast("START_RTSP failed");
					setContentView(R.layout.activity_play);
				}
				break;
			case RtspVideoView.SNAPSHOT:
				if (!isSuccess) {
					// mToast.showToast(AppUtil.text_toast2[mLanguage]);
				} else {
					mPlayVoice.play(PlayVoice.VOICE_SNAPSHOT);

					// mToast.showToast( handlerMsg.msg + "");
				}
				break;

			case RtspVideoView.RECORD:
				if (!isSuccess) {
					// mToast.showToast(AppUtil.text_toast8[mLanguage]);
				} else {
					mPlayVoice.play(PlayVoice.VOICE_RECORD1);
					/*
					 * mToast.showToast(AppUtil.text_toast1[mLanguage] + "\"" +
					 * handlerMsg.msg + "\"");
					 */
				}
				break;
			case RtspVideoView.STOP_RTSP:
				if (!isSuccess) {
					Log.d(TAG, "stop_stsp success");
				}
			case RtspVideoView.WIFI_CON:
				if (!isSuccess) {
					// mToast.showToast("Connection failed");
				}
			default:
				break;
			}
		}
	};

	/**
	 * 监听摇杆动作的类
	 */
	int tmp1;
	int tmp2;
	int tmp3;
	int tmp4;

	class MyRockerChangeListener implements RockerView.RockerChangeListener {

		@Override
		public void report(View v, float x, float y) {
			// TODO Auto-generated method stub
			if (v.getId() == R.id.rocker_left) {

				if (y < -0.1f) {

					// tmp1 = mCtlMsg.setConnonFourLow(y);
					mCtlMsg.setConnonUpDown(0); // 炮台下移
				} else if (y > 0.1f) {

					// tmp1 = mCtlMsg.setConnonFourLow(y);
					mCtlMsg.setConnonUpDown(1); // 炮台上移
				} else {
					y = 0;
					mCtlMsg.setConnonUpDown(1);
				}
				tmp1 = mCtlMsg.setConnonFourLow(y);

				if (x < -0.1f) {

					// tmp2 = mCtlMsg.setConnonFourLow(x);
					mCtlMsg.setConnonLeftRight(0); // 炮台左移
				} else if (x > 0.1f) {

					// tmp2 = mCtlMsg.setConnonFourLow(x);
					mCtlMsg.setConnonLeftRight(1); // 炮台右移

				} else {
					x = 0;
					mCtlMsg.setConnonLeftRight(1);
				}
				tmp2 = mCtlMsg.setConnonFourLow_x(x);

				mCtlMsg.setConnonFour(tmp2, tmp1); // 高四位和低四位
				// mCtlMsg.setConnonLeftRight(1);

			} else if (v.getId() == R.id.rocker_right) {
				// mCtlMsg.setLeftRight(1);
				// mCtlMsg.setFourLow(x);

				if (y > 0.1f) {

					mCtlMsg.setFrontBack(1); // 小车前进
					// tmp3 = mCtlMsg.setFourLow(y);

				} else if (y < -0.1f) {

					mCtlMsg.setFrontBack(0); // 小车后退
					// tmp3 = mCtlMsg.setFourLow(y);

				} else {
					y = 0;
					mCtlMsg.setFrontBack(1);
				}
				tmp3 = mCtlMsg.setFourLow(y);
				if (x > 0.1f) {

					mCtlMsg.setLeftRight(0); // 小车右转
					// tmp4 = mCtlMsg.setFourLow(x);
				} else if (x < -0.1f) {

					mCtlMsg.setLeftRight(1); // 小车左转
					// tmp4 = mCtlMsg.setFourLow(x);
				} else {
					x = 0;
					mCtlMsg.setLeftRight(0);
				}
				tmp4 = mCtlMsg.setFourLow_x(x);

				mCtlMsg.setFour(tmp4, tmp3); // //高四位和低四位

			}
			mTime_HideAllView = s_TotalTime_HideAllView;
			// isHideAllView = false;
		}
	}

	/**
	 * 
	 * @author 底部功能按钮点击事件
	 * 
	 * 
	 */

	class MyButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.ib_cannon_en:
				// 打炮按鍵

				mCtlMsg.setConnonBtn(1);
				mHandler.sendEmptyMessageDelayed(2, 100); // 1s后自动清零

				break;
			case R.id.ib_machinegun:
				// 机关枪按键
				mCtlMsg.setGunbtn(1);
				mHandler.sendEmptyMessageDelayed(3, 100); // 1s后自动清零
				break;

			case R.id.ib_power_en:
				// 电源开关按鍵

				mCtlMsg.setPowerBtn(1);
				mHandler.sendEmptyMessageDelayed(4, 100); // 1s后自动清零

				break;
			}

		}

	}

	/**
	 * true表示popupwindow已展示 false表示popupwindow未展示
	 */
	boolean displaypop = false;

	/*
	 * 为菜单键设置监听，根据获得的控件ID来执行相应的操作
	 */
	class MyMenuOnClickListener implements MenuButtonOnClickListener,
			OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			switch (v.getId()) {
			// 设置
			case R.id.btn_setting:
				ViewTreeObserver viewTreeObserver = mBtn_setting
						.getViewTreeObserver();
				viewTreeObserver
						.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
							@Override
							public boolean onPreDraw() {
								setHeight = mBtn_setting.getHeight();
								setWidth = mBtn_setting.getWidth();
								Log.e("获取mBtn_setting宽高", "宽度:" + setWidth
										+ ",高度:" + setHeight);
								return true;
							}
						});
				if (displaypop == true) {
					mPopu.dismiss();
					displaypop = false;
				} else if (displaypop == false) {
					popup();
				}
				break;
			// 截图
			case R.id.btn_snapshot:
				mBtn_snapShot.setEnabled(false);// 将截屏键设置为不可按
				mVideoView.takeSnapShot(AppUtil.getImageName());// 执行截图操作
				// Log.e("=====", "截图成功");
				mBtn_snapShot.setEnabled(true);// 将截屏键设置为可按
				break;
			// 录像
			case R.id.btn_record:
				if (isConnected == false) {
					Log.e(TAG, "影像未连接");
				} else {
					try {
						if (mVideoView.isReady()) // 括号中的值返回为false
						{
							if (mVideoView.videoIsRecording()) {
								isStartRecord = false;
								mVideoView.videoRecordStop(); // 停止录像
								Log.e("Baidu", "停止录像"); // 调用rtspClient的JniStopRtsp(String
								// rtspUrl)方法去停止录像
							} else {
								mVideoView.videoRecordStart(AppUtil
										.getVideoName()); // 启动录像
								mPlayVoice.play(PlayVoice.VOICE_RECORD0); // 播放声音
								Log.e("Baidu", "启动录像录像");
							}
						} else {
							if (mBtn_record.isChecked()) {
								mBtn_record.setChecked(false);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;

			// 查看截图录像文件
			case R.id.btn_playback:
				if (!isSDRecording && !isRecording) {
					Intent intent = new Intent(PlayActivity.this,
							FileActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				} else {
					// mToast.showToast(AppUtil.text_toast3[mLanguage]);
				}
				break;
			// 设置按键
			// case R.id.btn_setting:
			// isCountDown_HideAllView = false;
			// if (mBtn_setting.isChecked()) {
			// mBtn_setting.findViewById(R.id.btn_menu1)
			// .setBackgroundResource(R.drawable.language_rus);
			// btn_cannon.setBackgroundResource(R.drawable.btn_connon01);
			// btn_gun.setBackgroundResource(R.drawable.btn_gun01);
			// btn_power.setBackgroundResource(R.drawable.btn_power01);
			// AppUtil.s_Language = 1;
			//
			// } else {
			// mBtn_setting.findViewById(R.id.btn_menu1)
			// .setBackgroundResource(R.drawable.language_en);
			// btn_cannon.setBackgroundResource(R.drawable.btn_connon00);
			// btn_gun.setBackgroundResource(R.drawable.btn_gun00);
			// btn_power.setBackgroundResource(R.drawable.btn_power00);
			// AppUtil.s_Language = 0;
			// }
			//
			// break;
			default:
				break;
			}
		}

	}

	/**
	 * 切换语言
	 * 
	 * @param language
	 */
	private void changeTextLanguage(int language) {
		int i = language;

		// mBtn_exit.setText(AppUtil.text_menu[0][i]);
		// mBtn_snapShot.setText(AppUtil.text_menu[1][i]);
		// mBtn_record.setText(AppUtil.text_menu[2][i]);
		// mBtn_SDRecord.setText(AppUtil.text_menu[3][i]);
		// mBtn_playback.setText(AppUtil.text_menu[4][i]);
		// mBtn_setting.setText(AppUtil.text_menu[10][i]);

	}

	private void setViewVisibility(View view, int visibility) {
		if (view.getVisibility() != visibility) {
			view.setVisibility(visibility);
		}
	}

	/**
	 * @author Administrator 处理HTTP反馈信息Handler
	 */
	public Handler HTTP_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int value = (Integer) msg.obj;

			if (msg.what < 0) {
				return;
			}

			switch (msg.what) {

			case HTTP_GET_PRIVILEGE: // 获取权限返回的消息 authcode值
				if (value == 0) {
					Bundle bundle = msg.getData();
					mAuthcode = bundle.getString("authcode");
					Log.w(TAG, "mAuthcode" + mAuthcode);
				} else {
					mVideoView.videoPause(); // 暂停影像，只允许一个用户接入影像
					mToast.showToast("Access failed");
				}
				break;
			case HTTP_RELEASE_PRIVILEGE:// 释放权限返回的消息
				if (value != 0) {
				}
				break;

			case HTTP_SET_TIME: // 设置时间返回的消息
				if (value != 0) {
				}
				break;

			case HTTP_CHECK_STROAGE: // 检测SDcard返回的消息
				if (value == 0) {
					haveSDcard = false;
				} else if (value == 1) {
					haveSDcard = true;
				}
				break;

			case HTTP_BRIDGE: // 发送控制数据返回的消息
				Bundle bundle = msg.getData();
				String send = bundle.getString("send");
				break;
			case HTTP_TAKEPHOTO: // 截图返回的消息
				if (value == 0) {
					// mToast.showToast("拍照成功");
				} else {
					// mToast.showToast("拍照失败");
				}
				// mBtn_takePhoto.setEnabled(true);
				break;

			case HTTP_START_RECORD: // 启动录像返回的消息
				if (value == 0) {
					mPlayVoice.play(PlayVoice.VOICE_RECORD0); // 暂停影像
					mSDRecord_startTime = System.currentTimeMillis();
					// mToast.showToast(AppUtil.text_toast7[mLanguage]);
					isSDRecording = true;
				} else {
					// mToast.showToast(AppUtil.text_toast8[mLanguage]);
					// mBtn_SDRecord.setChecked(false);
					// mBtn_SDRecord.setText(AppUtil.text_menu[3][mLanguage]);
					isSDRecording = false;
				}
				// mBtn_SDRecord.setEnabled(true);
				break;

			case HTTP_STOP_RECORD: // 停止录像返回的消息
				if (value == 0) {
					mPlayVoice.play(PlayVoice.VOICE_RECORD1);
					// mToast.showToast(AppUtil.text_toast9[mLanguage]);
				} else {
					// mToast.showToast(AppUtil.text_toast8[mLanguage]);
				}
				isSDRecording = false;
				// mBtn_SDRecord.setText(AppUtil.text_menu[3][mLanguage]);
				// mBtn_SDRecord.setEnabled(true);
				break;
			}

		}
	};

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mVideoView.videoResume();
		isCountDown_HideAllView = true; // 重新启动隐藏所用View倒计时
		mTime_HideAllView = s_TotalTime_HideAllView;
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mVideoView.videoPause(); // 暂停影像
		isCountDown_HideAllView = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mVideoView.destory(); // 销毁控件
		mRocker_left.destory();
		mRocker_right.destory();
		listenRecordThread.isRun = false; // 关闭录像监听线程
		if (bridgeThread != null) {
			bridgeThread.isRun = false; // 关闭桥接控制线程
		}
		new HttpThread(HTTP_RELEASE_PRIVILEGE, HTTP_handler).start(); // 释放控制权限
		super.onDestroy();
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		BaseActivity.finishAll(); // finish所有的activity
		return super.onKeyDown(keyCode, event);
	}

}
