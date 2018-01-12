package cn.sharelink.activity;

import com.xinlian.radio_tank_t_72.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class FileActivity extends BaseActivity {

	static final String TAG = "FileActivity";
	private ImageView btn_return;
	private ImageView btn_photos;
	private ImageView btn_videos;
	private ButtonOnClickListener listener;
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_file);

		btn_return = (ImageView) findViewById(R.id.btn_return);
		btn_photos = (ImageView) findViewById(R.id.btn_photos);
		btn_videos = (ImageView) findViewById(R.id.btn_videos);
		listener = new ButtonOnClickListener();

		btn_return.setOnClickListener(listener);
		btn_photos.setOnClickListener(listener);
		btn_videos.setOnClickListener(listener);
		intent = new Intent();

	}

	class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_return:
				FileActivity.this.finish();

				break;
			case R.id.btn_photos:
				intent.setClass(FileActivity.this, PhotosActivity.class);
				startActivity(intent);

				break;
			case R.id.btn_videos:
				intent.setClass(FileActivity.this, VideosActivity.class);
				startActivity(intent);
				break;

			default:
				break;
			}

		}

	}
}
