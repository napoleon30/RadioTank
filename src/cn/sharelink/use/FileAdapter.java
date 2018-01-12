package cn.sharelink.use;

import java.io.File;
import java.util.ArrayList;

import com.xinlian.radio_tank_t_72.R;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends BaseAdapter {

	ArrayList<File> al;
	LayoutInflater lf;
	boolean type;
	
	public FileAdapter(Context context, ArrayList<File> al, boolean type) {
		// TODO Auto-generated constructor stub
		this.al = al;
		this.lf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.type = type;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return al.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return al.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ViewHolder holder;
		
		if(convertView == null) {
			convertView = lf.inflate(R.layout.item_file, null);
			
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.file_ico);
			holder.tv_name = (TextView) convertView.findViewById(R.id.file_name);
			holder.tv_size = (TextView) convertView.findViewById(R.id.file_size);
			holder.tv_createTime = (TextView) convertView.findViewById(R.id.file_createTime);
			convertView.setTag(holder); // 保存控件的信息
		} else {
			holder = (ViewHolder) convertView.getTag(); // 获取控件的信息
		}
		
		File file = al.get(position);
		holder.icon.setImageResource(type ? R.drawable.image_ico : R.drawable.video_ico);
		
		holder.tv_name.setText(file.getName());
		// 开始计算文件的大小
		long size = AppUtil.getFileSizes(file) / 1024; // 单位KB
		String text_size = null;
		if(size < 1024) {
			text_size = size + "KB";
		} else if(size / 1024 < 1024){
			float size_m = (float) (size / 1024.0);
			text_size = String.format("%.2f", size_m) + "MB";
		} else {
			float size_g = (float) (size / 1024.0 / 1024.0);
			text_size = String.format("%.2f", size_g) + "GB";
		}
		// 显示文件的大小
		holder.tv_size.setText(text_size); 
		// 显示文件的创建时间
		holder.tv_createTime.setText(AppUtil.getFileCreateDate(file)); 
		
		return convertView;
	}

	class ViewHolder {
		ImageView icon;
		TextView tv_name;
		TextView tv_size;
		TextView tv_createTime;
	}
}