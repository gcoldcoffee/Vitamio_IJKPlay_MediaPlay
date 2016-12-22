package vitamio.vitamiolibrary.utils;

import android.os.Build;

/**
 * Created by guomengqi on 16/11/29.
 */

public class SystemApi {

    /**
     * 判断android SDK 版本是否大于等于5.0
     * @return
     */
    public static boolean isAndroid5() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

}
