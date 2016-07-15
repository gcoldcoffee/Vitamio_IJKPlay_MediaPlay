package vitamio.vitamiolibrary.videos.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

public class FileUtil {

	/** SD卡本地文件目录 */
	public static final String KANKETV_DIR = "BoMiVideo";


	/** 内部文件目录 */

	public static final String KANKETV_INTERNAL_FILE_DIR = "BoMiVideoFile";
	public static final String KANKETV_INTERNAL_CACHE_DIR = "BoMiVideoCache";

	/**
	 * 获取文件目录，首先检查SD卡是否存在，存在则获取应用默认的文件目录，或者获取内部存储自定义目录
	 *
	 * @param context
	 * @return eg:/storage/emulated/0/Android/data/项目包名/files
	 */
	public static String getFilePath(Context context) {
		String path = "";
		if (checkSDCard()) {
			File file = context.getExternalFilesDir(null);
//			if(!file.exists()) {
//				file.mkdirs();
//			}
			// File file = Environment.getExternalStorageDirectory();
			// 如果外部文件目录获取不到，则使用内部存储控件
			if (null != file) {
				if(!file.exists()) {
					file.mkdirs();
				}
				path = file.getAbsolutePath();// + File.separator + KANKETV_DIR;
			} else {
				path = context.getDir(KANKETV_INTERNAL_FILE_DIR, Context.MODE_PRIVATE).getAbsolutePath();
			}
		} else {
			path = context.getDir(KANKETV_INTERNAL_FILE_DIR, Context.MODE_PRIVATE).getAbsolutePath();
		}
		return path;
	}

	/**
	 * 获取文件目录，首先检查SD卡是否存在，存在则获取应用默认的缓存目录，或者获取内部存储自定义目录
	 *
	 * @param context
	 * @return eg:/storage/emulated/0/Android/data/项目包名/caches
	 */
	public static String getCachePath(Context context) {
		String path = "";
		if (checkSDCard()) {
			File file = context.getExternalCacheDir();
			// File file = Environment.getExternalStorageDirectory();
			if (null != file) {// 如果外部文件目录获取不到，则使用内部存储控件
				path = file.getAbsolutePath();// + File.separator
				// +KANKETV_CACHE_DIR;
			} else {
				path = context.getDir(KANKETV_INTERNAL_CACHE_DIR, Context.MODE_PRIVATE).getAbsolutePath();
			}
		} else {
			path = context.getDir(KANKETV_INTERNAL_CACHE_DIR, Context.MODE_PRIVATE).getAbsolutePath();
		}
		return path;
	}

	/**
	 * 检测sd卡是否可用
	 *
	 * @return
	 */
	public static boolean checkSDCard() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取sd卡路径
	 *
	 * @return
	 */
	public static String getRootPath() {
		if (checkSDCard()) {
			return Environment.getExternalStorageDirectory().getPath();
		}
		return "";
	}


	/**
	 * 文件是否存在
	 *
	 * @param path
	 *            文件目录
	 * @return
	 */
	public static boolean isFileExists(String path) {
		if (TextUtils.isEmpty(path)) {
			return false;
		}
		return new File(path).exists();
	}
}
