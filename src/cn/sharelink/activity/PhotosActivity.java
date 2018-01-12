package cn.sharelink.activity;

import java.io.File;
import java.util.ArrayList;

import com.xinlian.radio_tank_t_72.R;

import cn.sharelink.use.AppUtil;
import cn.sharelink.use.FileAdapter;
import cn.sharelink.view.MyToast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class PhotosActivity extends BaseActivity {

	static final String TAG = "PhotosActivity ";
	private ListView mListView_pic;
	private FileAdapter mFileAdapter_pic;
	private ArrayList<File> mArrayList_pic;

	private MyToast mToast;

	private static final String[][] text_fileTittle = {
			{ "Pictures", "картинки" }, { "Videos", "Видео" }, };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_photos);
		listView_init();

		mToast = new MyToast(this);
	}

	void listView_init() {
		mListView_pic = (ListView) findViewById(R.id.lv_pic);

		mArrayList_pic = new ArrayList<File>();
		init_arraylist(0);

		mFileAdapter_pic = new FileAdapter(this, mArrayList_pic, true);

		mListView_pic.setAdapter(mFileAdapter_pic);

		mListView_pic.setOnItemClickListener(new OnItemClickListener_Pic());
		mListView_pic.setOnItemLongClickListener(new OnItemClickListener_Pic());

	}

	private void init_arraylist(int index) {
		if (index == 0) {
			File file_pic = new File(AppUtil.getImagePath());
			if (file_pic.isDirectory()) {
				File[] files = file_pic.listFiles();
				mArrayList_pic.clear();
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if (file.getName().endsWith(AppUtil.IMG_TYPE)) {
						mArrayList_pic.add(0, file);
					}
				}
			}
		}
	}

	/**
	 * 说明：对图片的ListView设置Item监听， 1、短按时打开文件 2、长按时弹出上下文菜单
	 */
	class OnItemClickListener_Pic implements OnItemClickListener,
			OnItemLongClickListener {
		File file ;
		/**
		 * 功能：短按时，打开文件
		 */
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			openFile(mArrayList_pic.get(arg2));
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			 file = mArrayList_pic.get(position);

			AlertDialog.Builder builder = new AlertDialog.Builder(
					PhotosActivity.this);
			 builder.setTitle(AppUtil.text_dialog_delfile_tittle[AppUtil.s_Language]);
			builder.setMessage(AppUtil.text_dialog_delfile_content[AppUtil.s_Language]);
			builder.setPositiveButton(
					AppUtil.text_dialog_delfile_confirm[AppUtil.s_Language],
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							int index = -1;
							String name = file.getName().toLowerCase();
							if (name.endsWith(".jpg")) {
								index = 0;
							} else if (name.endsWith(".mp4")) {
								index = 1;
							}

							if (!file.delete()) {
								mToast.showToast(AppUtil.text_dialog_delfile_failed[AppUtil.s_Language]);
							}
							refresh_listView(index);
						}
					});
			builder.setNegativeButton(
					AppUtil.text_dialog_delfile_cancel[AppUtil.s_Language],
					null);
			builder.create();
			builder.show();
			return true;
		}

		/**
		 * 功能：长按文件时，弹出上下文菜单 有"打开"、"分享"、"重命名"、"删除"四个选项
		 */

	}

	/**
	 * 功能：刷新ListView
	 * 
	 * @param index
	 *            0 ---> 刷新图片文件的ListView 1 ---> 刷新视频文件的ListView
	 */
	private void refresh_listView(int index) {
		init_arraylist(index);
		if (index == 0) {
			mFileAdapter_pic.notifyDataSetChanged();
		}
	}

	/**
	 * 功能：打开文件
	 * 
	 * @param file
	 *            需要打开的文件
	 */
	private void openFile(File file) {
		if (file == null) {
			return;
		}

		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);

		String fileName = file.getName().toLowerCase();
		if (fileName.endsWith(".jpg")) {
			intent.setDataAndType(Uri.fromFile(file), "image/*");
		} else if (fileName.endsWith(".mp4")) {
			intent.setDataAndType(Uri.fromFile(file), "video/mp4");
		}

		startActivity(intent);
	}

	/**
	 * 功能：弹出是否删除文件的对话框
	 * 
	 * @param file
	 *            要删除的文件
	 */
	private void deleteFile(final File file) {

		if (file == null) {
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(AppUtil.text_dialog_delfile_tittle[AppUtil.s_Language]);
		builder.setMessage(AppUtil.text_dialog_delfile_content[AppUtil.s_Language]
				+ file.getName() + "\"?");
		builder.setPositiveButton(
				AppUtil.text_dialog_delfile_confirm[AppUtil.s_Language],
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						int index = -1;
						String name = file.getName().toLowerCase();
						if (name.endsWith(".jpg")) {
							index = 0;
						} else if (name.endsWith(".mp4")) {
							index = 1;
						}

						if (!file.delete()) {
							mToast.showToast(AppUtil.text_dialog_delfile_failed[AppUtil.s_Language]);
						}
						refresh_listView(index);
					}
				});
		builder.setNegativeButton(
				AppUtil.text_dialog_delfile_cancel[AppUtil.s_Language], null);
		builder.create();
		builder.show();
	}

}
