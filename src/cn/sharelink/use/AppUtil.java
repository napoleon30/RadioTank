package cn.sharelink.use;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.util.Log;

/**
 * APP工具类
 */
public class AppUtil {

	public static final String RTSP_URL_nHD = "rtsp://192.168.100.1/cam1/h264-1";  //640*368
	public static final String RTSP_URL_720P = "rtsp://192.168.100.1/cam1/h264";   //1280*720
	public static int code;

	public static final String APP_PATH = getSDPath()
			+ "/Android/data/cn.sharelink.tank";

	public static final String IMG_TYPE = ".jpg";
	public static final String VID_TYPE = ".mp4";

	public static String getCurrentTime() {
		// return new SimpleDateFormat("yyyyMMdd_hhmmss").format(new
		// Date(System.currentTimeMillis())); // 12小时制
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System
				.currentTimeMillis())); // 24小时制
	}

	public static String getFilePath() {
		File appDir = new File(getSDPath() + "/TankApp");
		if (!appDir.exists()) {
			appDir.mkdirs();
		}

		return getSDPath() + "/TankApp";
	}

	public static String getImagePath() {
		//Log.e(">>>>>>", getFilePath());
		File appDir = new File(getFilePath() + "/snapshot");
		if (!appDir.exists()) {
			appDir.mkdirs();
		}
		return getFilePath() + "/snapshot";
	}

	public static String getVideoPath() {
		File appDir = new File(getFilePath() + "/video");
		if (!appDir.exists()) {
			appDir.mkdirs();
		}
		return getFilePath() + "/video";
	}

	public static String getImageName() {
		return AppUtil.getImagePath() + "/IMG_" + AppUtil.getCurrentTime()
				+ ".jpg";
	}

	public static String getVideoName() {
		return AppUtil.getVideoPath() + "/VID_" + AppUtil.getCurrentTime()
				+ ".mp4";
	}

	private static final String[][] Menu_TEXT = { { "Open", "открыто","Otwórz" },
			{ "Share", "Поделиться","Udział" }, { "Rename", "переименовывать","Musieli przemianować" },
			{ "Delete", "Удалить","Skreślić," }, { "Share file", "Общий доступ к файлам","Udział w aktach" } };
	private static final String[][] text_fileTittle = {
			{ "Pictures", "картинки","Zdjęcia" }, { "Videos", "Видео","Wideo" } };

	public static final String[] text_dialog_delfile_tittle = { "Delete",
			"Удалить","Skreślić," };
	public static final String[] text_dialog_delfile_content = { "Are you sure delete this file?",
			"Точно удалить этот файл?" ,"Jesteś pewien, że to usunąć plik?"};
	public static final String[] text_dialog_delfile_cancel = { "Cancel",
			"Отменить" ,"Odwołaj"};
	public static final String[] text_dialog_delfile_confirm = { "OK",
//			"Подтвердить","OK" };
	"OK","OK" };
	public static final String[] text_dialog_delfile_failed = {
			"Delete  failed", "Не удалось удалить","Skreślić, nie" };

	public static final String[] text_dialog_renamefile_tittle = { "Rename",
			"переименовывать","Musieli przemianować" };
	public static final String[] text_dialog_renamefile_cancel = { "Cancel",
			"Отмена","Odwołaj" };
	public static final String[] text_dialog_renamefile_confirm = { "OK", "ОК","OK" };
	public static final String[] text_dialog_renamefile_failed = {
			"Rename  failed", "переименование не удалось","Nazwana niepowodzeniem" };

	public static final String[] text_dialog = { "Connecting, please wait...",
			"подключения, пожалуйста, подождите ...","łączę, proszę czekać...." };

	public static String getProgressDialogText() {
		return text_dialog[s_Language];
	}

	public static String getSDPath() {
		boolean hasSDCard = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (hasSDCard) {
			return Environment.getExternalStorageDirectory().toString();
		} else
			return Environment.getDownloadCacheDirectory().toString();
	}

	public static long getFileSizes(File f) {
		long size = 0;
		if (f.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
				size = fis.available();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return size;
	}

	public static String getFileCreateDate(File f) {
		String time_str = new SimpleDateFormat("yyyy/MM/dd").format(new Date(f
				.lastModified()));
		return time_str;
	}

	// /**
	// * 根据指定的图像路径和大小来获取缩略图
	// * 此方法有两点好处：
	// * 1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
	// * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
	// * 2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
	// * 用这个工具生成的图像不会被拉伸。
	// * @param imagePath 图像的路径
	// * @param width 指定输出图像的宽度
	// * @param height 指定输出图像的高度
	// * @return 生成的缩略图
	// */
	// public static Bitmap getImageThumbnail(String imagePath, int width, int
	// height) {
	// Bitmap bitmap = null;
	// BitmapFactory.Options options = new BitmapFactory.Options();
	// options.inJustDecodeBounds = true;
	// // 获取这个图片的宽和高，注意此处的bitmap为null
	// bitmap = BitmapFactory.decodeFile(imagePath, options);
	// options.inJustDecodeBounds = false; // 设为 false
	// // 计算缩放比
	// int h = options.outHeight;
	// int w = options.outWidth;
	// int beWidth = w / width;
	// int beHeight = h / height;
	// int be = 1;
	// if (beWidth < beHeight) {
	// be = beWidth;
	// } else {
	// be = beHeight;
	// }
	// if (be <= 0) {
	// be = 1;
	// }
	// options.inSampleSize = be;
	// // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
	// bitmap = BitmapFactory.decodeFile(imagePath, options);
	// // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
	// bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
	// ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
	// return bitmap;
	// }
	// /**
	// * 获取视频的缩略图
	// * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	// * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	// * @param videoPath 视频的路径
	// * @param width 指定输出视频缩略图的宽度
	// * @param height 指定输出视频缩略图的高度度
	// * @param kind 参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	// * 其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	// * @return 指定大小的视频缩略图
	// */
	// public static Bitmap getVideoThumbnail(String videoPath, int width, int
	// height,
	// int kind) {
	// Bitmap bitmap = null;
	// // 获取视频的缩略图
	// bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
	// System.out.println("w"+bitmap.getWidth());
	// System.out.println("h"+bitmap.getHeight());
	// bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
	// ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
	// return bitmap;
	// }

	public static int s_SpeedChoose;
	public static int s_NoHead;
	public static int s_Language;
	public static int s_ControlMode;
	public static int s_StroageLocation;
	public static int s_FlipImage;

	public static int trim1;
	public static int trim2;
	public static int trim3;

	public static void readDataFile() throws NumberFormatException, IOException {

		File appDir = new File(APP_PATH);
		if (!appDir.exists()) {
			appDir.mkdirs();
		}

		File dataFile = new File(AppUtil.APP_PATH + "/data");

		if (dataFile.exists()) {
			FileInputStream fis = new FileInputStream(dataFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis));
			String line;

			while ((line = reader.readLine()) != null) {
				String dat = line.substring(line.indexOf(":") + 1);
				Log.e("AppUtil打印", dat);
				if (line.startsWith("speed")) {

					if (dat.equals("30%")) {
						s_SpeedChoose = 0;
					} else if (dat.equals("60%")) {
						s_SpeedChoose = 1;
					} else if (dat.equals("100%")) {
						s_SpeedChoose = 2;
					} else {
						s_SpeedChoose = 0;
					}
				}

				if (line.startsWith("nohead")) {

					if (dat.equals("CLOSE")) {
						s_NoHead = 0;
					} else if (dat.equals("OPEN")) {
						s_NoHead = 1;
					} else {
						s_NoHead = 0;
					}
				}

				if (line.startsWith("language")) {
					Log.e("AppUtil打印", "language_line："+line);
					Log.e("AppUtil打印", "language_line："+dat);
					if (dat.equals("EN")) {
						s_Language = 0;
					} else if (dat.equals("EN")) {
						s_Language = 1;
					} else {
						s_Language = 0;
					}
				}

				if (line.startsWith("hand")) {
					if (dat.equals("LEFT")) {
						s_ControlMode = 0;
					} else if (dat.equals("RIGHT")) {
						s_ControlMode = 1;
					} else {
						s_ControlMode = 0;
					}
				}

				if (line.startsWith("stroage")) {
					if (dat.equals("SDcard")) {
						s_StroageLocation = 0;
					} else if (dat.equals("Phone")) {
						s_StroageLocation = 1;
					} else {
						s_StroageLocation = 0;
					}
				}

				if (line.startsWith("rotate")) {
					if (dat.equals("0")) {
						s_FlipImage = 0;
					} else if (dat.equals("180")) {
						s_FlipImage = 1;
					} else {
						s_FlipImage = 0;
					}
				}

				if (line.startsWith("trim1")) {
					trim1 = Integer.parseInt(dat);
				}
				if (line.startsWith("trim2")) {
					trim2 = Integer.parseInt(dat);
				}
				if (line.startsWith("trim3")) {
					trim3 = Integer.parseInt(dat);
				}
			}
			reader.close();
			fis.close();
		} else {
			s_SpeedChoose = 0;
			s_Language = 0;
			s_ControlMode = 0;
			s_NoHead = 0;
			s_StroageLocation = 0;
			s_FlipImage = 0;
			trim1 = 32;
			trim2 = 32;
			trim3 = 32;

			writeSetupParameterToFile();
		}
	}

	public static void writeSetupParameterToFile() {

		try {
			File appDir = new File(APP_PATH);
			if (!appDir.exists()) {
				appDir.mkdirs();
			}

			File dataFile = new File(AppUtil.APP_PATH + "/data");
			FileOutputStream fos = new FileOutputStream(dataFile, false); // 如果采用追加方式用true
			StringBuffer sb = new StringBuffer();

			if (s_SpeedChoose == 0) {
				sb.append("speed:30%\n");
			} else if (s_SpeedChoose == 1) {
				sb.append("speed:60%\n");
			} else if (s_SpeedChoose == 2) {
				sb.append("speed:100%\n");
			}

			if (s_NoHead == 0) {
				sb.append("nohead:CLOSE\n");
			} else {
				sb.append("nohead:OPEN\n");
			}

			if (s_Language == 0) {
				sb.append("language:CH\n");
			} else {
				sb.append("language:EN\n");
			} 
			Log.e("AppUtil打印", "语言"+sb);

			if (s_ControlMode == 0) {
				sb.append("hand:LEFT\n");
			} else {
				sb.append("hand:RIGHT\n");
			}

			if (s_StroageLocation == 0) {
				sb.append("stroage:SDcard\n");
			} else {
				sb.append("stroage:Phone\n");
			}

			if (s_FlipImage == 0) {
				sb.append("rotate:0\n");
			} else {
				sb.append("rotate:180\n");
			}

			sb.append("trim1:" + trim1 + "\n").append("trim2:" + trim2 + "\n")
					.append("trim3:" + trim3 + "\n");

			fos.write(sb.toString().getBytes("UTF8"));
			fos.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void setSetupParameter(int speedChoose, int headDirection,
			int language, int controlMode, int stroageLocation, int flipImage,
			int trim1, int trim2, int trim3) {

		AppUtil.s_SpeedChoose = speedChoose;
		AppUtil.s_NoHead = headDirection;
		AppUtil.s_Language = language;
		AppUtil.s_ControlMode = controlMode;
		AppUtil.s_StroageLocation = stroageLocation;
		AppUtil.s_FlipImage = flipImage;
		AppUtil.trim1 = trim1;
		AppUtil.trim2 = trim2;
		AppUtil.trim3 = trim3;

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				writeSetupParameterToFile();
			}
		}).start();

	}
}
