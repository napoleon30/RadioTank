package cn.sharelink.use;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.AES.AESCrypto;

/**
 * @author ChenJun 处理HTTP通信的线程类
 * 
 */
public class HttpThread extends Thread {

	private static final String TAG = "HTTP_Thread";

	public static final int HTTP_START = 0;
	public static final int HTTP_SET_TIME = 1;
	public static final int HTTP_CHECK_STROAGE = 2;
	public static final int HTTP_BRIDGE = 3;
	public static final int HTTP_TAKEPHOTO = 4;
	public static final int HTTP_START_RECORD = 5;
	public static final int HTTP_STOP_RECORD = 6;
	public static final int HTTP_GET_PRIVILEGE = 7;
	public static final int HTTP_RELEASE_PRIVILEGE = 8;
	public static final int HTTP_FLIP_MIRROR_IMAGE = 9;

	private static final String http_url = "http://192.168.100.1:80";
	private static final String authcode_str = "&authcode=";
	private static final String getPrivilege_cmd = "/server.command?command=get_privilege";
	private static final String releasePrivilege_cmd = "/server.command?command=release_privilege"
			+ authcode_str;
	private static final String setDate_cmd = "/server.command?command=set_date&tz=GMT-8:00&date=";
	private static final String checkStroage_cmd = "/server.command?command=check_storage";
	private static final String snapshot_cmd = "/server.command?command=snapshot&pipe=0"
			+ authcode_str;
	private static final int record_pipe = 0;
	private static final String isRecord_cmd = "/server.command?command=is_pipe_record&type=h264&pipe="
			+ record_pipe;
	private static final String startRecord_cmd = "/server.command?command=start_record_pipe&type=h264&pipe="
			+ record_pipe + authcode_str;
	private static final String stopRecord_cmd = "/server.command?command=stop_record&type=h264&pipe="
			+ record_pipe + authcode_str;
	private static final String flipOrMirror_cmd = "/server.command?command=set_flip_mirror&value=";
	private static final String bridge1_cmd = "/server.command?command=bridge&type=0&value=";
	private static final String bridge2_cmd = authcode_str;
	public boolean isRun=false;
	public int cmd_index;

	private Handler handler;
	private int value=1;

	public HttpThread(int cmd_index, Handler handler) {
		// TODO Auto-generated constructor stub
		this.cmd_index = cmd_index;
		this.handler = handler;
	}

	public HttpThread(int cmd_index, int value, Handler handler) {
		// TODO Auto-generated constructor stub
		this.cmd_index = cmd_index;
		this.handler = handler;
		this.value = value;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			switch (cmd_index) {
			case HTTP_START:
				do_setDate(); // 设置时间
				do_getPrivilege(); // 获取权限
				do_flipOrMirror(0); //翻转影像 ：0正常  3翻转
				do_checkStorage(); // 检测SD卡是否存在
				do_bridge(); // 不断的发送控制数据
				
				break;

			case HTTP_TAKEPHOTO:
				do_takePhoto(); // 发送拍照
				break;

			case HTTP_START_RECORD:
				do_startRecord(); // 发送开始录像
				break;

			case HTTP_STOP_RECORD: // 停止录像
				do_stopRecord();
				break;

			case HTTP_RELEASE_PRIVILEGE: // 释放控制权限
				do_releasePrivilege();
				break;

			case HTTP_FLIP_MIRROR_IMAGE:
				do_flipOrMirror(value);
				break;

			default:
				break;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static String sAuthcode = null;

	/**
	 * 获取控制权限
	 */
	void do_getPrivilege() throws Exception {
		String strResult;
		int ret = -1;
		HttpGet getMethod = new HttpGet(http_url + getPrivilege_cmd);
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			HttpEntity httpEntity = httpResponse.getEntity();
			InputStream inputStream = httpEntity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line = reader.readLine();
			JSONObject jsonObj = new JSONObject(line);
			ret = jsonObj.getInt("value");
			if (ret == 0) {
				line = reader.readLine();
				jsonObj = new JSONObject(line);
				String nonce = jsonObj.getString("nonce");
				sAuthcode = AESCrypto.getAuthcode(nonce);
				Log.i("do_getPrivilege", "nonce:" + nonce);
				// Log.i("do_getPrivilege", "encrypt:" +
				// AESCrypto.encrypt(nonce));
				Log.i("do_getPrivilege", "Authcode:" + sAuthcode);
				AppUtil.code=200;
				
			}
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}
	/*	getMethod = new HttpGet("/server.command?command=set_flip_mirror&value=0&authcode="+sAuthcode);
		HttpClient httpClient1 = new DefaultHttpClient();

		//发送请求并等待响应 
		HttpResponse httpResponse1 = httpClient.execute(getMethod);
		Log.e("=============", getMethod+"");*/
		
		Message msg = handler.obtainMessage(HTTP_GET_PRIVILEGE, ret);
		Bundle bundle = new Bundle();
		bundle.putString("authcode", sAuthcode);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	void do_releasePrivilege() throws ClientProtocolException, IOException,
			JSONException {
		String strResult;
		if (sAuthcode == null) {
			return;
		}
		int ret = -1;
		String url = http_url + releasePrivilege_cmd + sAuthcode;
		Log.i("do_releasePrivilege", url);
		HttpGet getMethod = new HttpGet(url);
		HttpClient httpClient = new DefaultHttpClient();

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			ret = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Log.i("do_releasePrivilege", strResult);
		handler.obtainMessage(HTTP_RELEASE_PRIVILEGE, ret).sendToTarget();
	}

	void do_setDate() throws ClientProtocolException, IOException,
			JSONException {
		String strResult;
		int ret = -1;
		SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
		String time = df.format(new Date());
		HttpGet getMethod = new HttpGet(http_url + setDate_cmd + time);
		HttpClient httpClient = new DefaultHttpClient();

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			ret = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Log.i("do_setDate", strResult);
		handler.obtainMessage(HTTP_SET_TIME, ret).sendToTarget();
	}

	void do_checkStorage() throws ClientProtocolException, IOException,
			JSONException {
		String strResult;
		int ret = -1;
		HttpGet getMethod = new HttpGet(http_url + checkStroage_cmd);
		HttpClient httpClient = new DefaultHttpClient();

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			ret = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Log.i("do_checkStorage", strResult);
		handler.obtainMessage(HTTP_CHECK_STROAGE, ret).sendToTarget();

	}

	void do_bridge() throws ClientProtocolException, IOException,
			JSONException, InterruptedException {
		if (sAuthcode == null) {
			return;
		}
		String strResult;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getMethod;
		HttpResponse httpResponse;
		// RequestConfig requestConfig =
		// RequestConfig.custom().setSocketTimeout(200).setConnectTimeout(200).build();//设置请求和传输超时时间
		isRun = true;
		ControlMsg ctlMsg = ControlMsg.getInstance();
		while (isRun) {
			int ret = -1;
			String sendData = ctlMsg.getDataHexString("_"); //拼接成16进制的字符串

		Log.e("httpthread-sendData", "===========" + sendData);

			getMethod = new HttpGet(http_url + bridge1_cmd + sendData
					+ bridge2_cmd + sAuthcode);
			/* 发送请求并等待响应 */
			httpResponse = httpClient.execute(getMethod);

			/* 若状态码为200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* 读返回数据 */
				strResult = EntityUtils.toString(httpResponse.getEntity());
				ret = JSON_getValue(strResult);
			} else {
				getMethod = new HttpGet(http_url + bridge1_cmd + "66_00_00_00_0c_99"
						+ bridge2_cmd + sAuthcode);
				httpClient.execute(getMethod);
			}

			// Message msg = handler.obtainMessage(HTTP_BRIDGE, ret);
			// Bundle bundle = new Bundle();
			// bundle.putString("send", sendData.replace('_', ' '));
			// msg.setData(bundle);
			// handler.sendMessage(msg);
			handler.obtainMessage(HTTP_BRIDGE, ret).sendToTarget();
			//Thread.sleep(50);
		}
	}

	void do_takePhoto() throws ClientProtocolException, IOException,
			JSONException {
		if (sAuthcode == null) {
			return;
		}
		String strResult;
		int ret = -1;
		String url = http_url + snapshot_cmd + sAuthcode;
		Log.i("do_takePhoto", url);
		HttpGet getMethod = new HttpGet(url);
		HttpClient httpClient = new DefaultHttpClient();

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			ret = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Log.i("do_takePhoto", strResult);
		handler.obtainMessage(HTTP_TAKEPHOTO, ret).sendToTarget();
	}

	void do_startRecord() throws ClientProtocolException, IOException,
			JSONException {
		if (sAuthcode == null) {
			return;
		}

		String strResult;
		int ret = -1;

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getMethod = new HttpGet(http_url + isRecord_cmd);
		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			ret = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}
		Log.i("do_startRecord1", strResult);

		if (ret == 1) { // 表示正在录制
			handler.obtainMessage(HTTP_START_RECORD, ret).sendToTarget();
			return;
		}

		getMethod = new HttpGet(http_url + startRecord_cmd + sAuthcode);
		httpResponse = httpClient.execute(getMethod);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			ret = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		handler.obtainMessage(HTTP_START_RECORD, ret).sendToTarget();

		Log.i("do_startRecord2", strResult);
	}

	void do_stopRecord() throws ClientProtocolException, IOException,
			JSONException {
		if (sAuthcode == null) {
			return;
		}

		String strResult;
		int ret = -1;

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getMethod = new HttpGet(http_url + isRecord_cmd);
		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			ret = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}
		Log.i("do_stopRecord1", strResult);

		if (ret == 0) { // 表示没有录制
			handler.obtainMessage(HTTP_STOP_RECORD, ret).sendToTarget();
			return;
		}

		getMethod = new HttpGet(http_url + stopRecord_cmd + sAuthcode);
		httpResponse = httpClient.execute(getMethod);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			ret = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		handler.obtainMessage(HTTP_STOP_RECORD, ret).sendToTarget();

		Log.i("do_stopRecord2", strResult);
	}

	void do_flipOrMirror(int value) throws ClientProtocolException,
			IOException, JSONException {
		if (sAuthcode == null) {
			return;
		}

		String strResult;
		int ret = -1;

		StringBuffer sb = new StringBuffer();
		sb.append(http_url).append(flipOrMirror_cmd).append(value)
				.append(authcode_str).append(sAuthcode);
		
		Log.e("=============", sb+"");

		HttpGet getMethod = new HttpGet(sb.toString());
		HttpClient httpClient = new DefaultHttpClient();

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			ret = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		handler.obtainMessage(HTTP_FLIP_MIRROR_IMAGE, ret).sendToTarget();

		Log.i("do_flipOrMirror", strResult);

	}

	int JSON_getValue(String strResult) throws JSONException {
		JSONObject jsonObj = new JSONObject(strResult);
		int value = jsonObj.getInt("value");
		return value;
	}
}