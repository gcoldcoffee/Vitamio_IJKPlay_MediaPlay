package vitamio.vitamiolibrary.videos.utils;

import android.util.Log;

public class Alog {

	public static boolean mbLog = true;

	public static void d(String paramString1, String paramString2) {
		if (!mbLog)
			return;
		int i = Log.d(paramString1, paramString2);
	}

	public static void d(String paramString2) {
		if (!mbLog)
			return;
		int i = Log.d("BoPlayer", paramString2);
	}

	public static void e(String paramString1, String paramString2) {
		if (!mbLog)
			return;
		int i = Log.e(paramString1, paramString2);
	}

	public static void i(String paramString1, String paramString2) {
		if (!mbLog)
			return;
		int i = Log.i(paramString1, paramString2);
	}

	public static void i(String paramString1) {
		if (!mbLog)
			return;
		int i = Log.i("", paramString1);
	}

	public static String registerMod(String paramString) {
		return paramString;
	}

	public static void v(String paramString1, String paramString2) {
		if (!mbLog)
			return;
		int i = Log.v(paramString1, paramString2);
	}

	public static void w(String paramString1, String paramString2) {
		if (!mbLog)
			return;
		int i = Log.w(paramString1, paramString2);
	}
}
