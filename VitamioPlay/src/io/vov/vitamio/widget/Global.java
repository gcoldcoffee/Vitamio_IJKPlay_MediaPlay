package io.vov.vitamio.widget;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;



import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

//import com.kascend.video.autoupgrade.UpdateManager;

public class Global {
	
	public static final String APP_DATA_PATH;
    public static final String CATEGORY_ICON_PATH;
    public static final String DOWNLOAD_PATH;
    public static final Object DownloadThreadLock = new Object();
    public static final String INBOX_THUMB_PATH;
    private static boolean ISARMV7 = false;
    private static boolean IsHD = false;
    private static boolean IsSupportNeon = false;
    public static final String MYVIDEO_THUMBNAIL_FILE_PATH;
    public static final Object MyVideoCursorLock = new Object();
    public static final String ONLINE_THUMB_ROOTPATH;
    public static final String PLAYER_INI_PATH;
    public static final String PLAY_LIST_TEMP_PATH;
    public static final String PRESEARCH_FILE;
    public static final String SCAN_SUFFIX_FILE_FILTER[];
    public static final String SEARCH_PATH;
    public static final String TEMP_PATH;
    private static HashMap mCurrentMap = new HashMap();
//    public static LoginManager mLoginManager = null;
    private static String serverPath = null;
    private static String strVersion = null;
//    private static UpdateManager updateManager = null;
    static 
    {
        String as[] = new String[21];
        as[0] = ".mp4";
        as[1] = ".3gp";
        as[2] = ".3gpp";
        as[3] = ".3g2";
        as[4] = ".3gpp2";
        as[5] = ".avi";
        as[6] = ".mkv";
        as[7] = ".flv";
        as[8] = ".divx";
        as[9] = ".f4v";
        as[10] = ".rm";
        as[11] = ".rmvb";
        as[12] = ".rv";
        as[13] = ".wmv";
        as[14] = ".asf";
        as[15] = ".mov";
        as[16] = ".m2ts";
        as[17] = ".ts";
        as[18] = ".vob";
        as[19] = ".tp";
        as[20] = ".mpg";
        SCAN_SUFFIX_FILE_FILTER = as;
        String s = String.valueOf(Environment.getExternalStorageDirectory().toString());
        APP_DATA_PATH = (new StringBuilder(s)).append("/kascend/videoshow/").toString();
        String s1 = String.valueOf(Environment.getExternalStorageDirectory().toString());
        DOWNLOAD_PATH = (new StringBuilder(s1)).append("/kascend/videoshow/download").toString();
        String s2 = String.valueOf(Environment.getExternalStorageDirectory().toString());
        SEARCH_PATH = (new StringBuilder(s2)).append("/kascend/videoshow/search").toString();
        String s3 = String.valueOf(Environment.getExternalStorageDirectory().toString());
        TEMP_PATH = (new StringBuilder(s3)).append("/kascend/videoshow/temp/").toString();
        String s4 = String.valueOf(APP_DATA_PATH);
        MYVIDEO_THUMBNAIL_FILE_PATH = (new StringBuilder(s4)).append("myvideo_thumbnail/").toString();
        String s5 = String.valueOf(Environment.getDataDirectory().toString());
        ONLINE_THUMB_ROOTPATH = (new StringBuilder(s5)).append("/data/com.kanke.video/onlinethumbnail/").toString();
        String s6 = String.valueOf(Environment.getDataDirectory().toString());
        INBOX_THUMB_PATH = (new StringBuilder(s6)).append("/data/com.kanke.video/inboxthumbnail/").toString();
        String s7 = String.valueOf(Environment.getDataDirectory().toString());
        PLAYER_INI_PATH = (new StringBuilder(s7)).append("/data/com.kanke.video/ini/").toString();
        String s8 = String.valueOf(Environment.getDataDirectory().toString());
        PLAY_LIST_TEMP_PATH = (new StringBuilder(s8)).append("/data/com.kanke.video/temp.xml").toString();
        String s9 = String.valueOf(Environment.getDataDirectory().toString());
        CATEGORY_ICON_PATH = (new StringBuilder(s9)).append("/data/com.kanke.video/categoryicon/").toString();
        String s10 = String.valueOf(APP_DATA_PATH);
        PRESEARCH_FILE = (new StringBuilder(s10)).append("searchresponse.xml").toString();
    }
    
	 public static final class Errno
	    {

	        public Errno()
	        {
	        }
	    }

	    public static final class FriendStatus
	    {

	        public FriendStatus()
	        {
	        }
	    }

	    public static final class InBoxItemLocaltionType
	    {

	        public InBoxItemLocaltionType()
	        {
	        }
	    }

	    public static final class InBoxMenuIdofGroup
	    {

	        public InBoxMenuIdofGroup()
	        {
	        }
	    }

	    public static final class InboxAppFilter
	    {

	        public InboxAppFilter()
	        {
	        }
	    }

	    public static final class InboxFilter
	    {

	        public InboxFilter()
	        {
	        }
	    }

	    public static final class MenuIdofGroup
	    {

	        public MenuIdofGroup()
	        {
	        }
	    }

	    public static final class MenuIdofWBList
	    {

	        public MenuIdofWBList()
	        {
	        }
	    }

	    public static final class MyVideoMenuIdofGroup
	    {

	        public MyVideoMenuIdofGroup()
	        {
	        }
	    }

	    public static final class OnlineMenuIdofGroup
	    {

	        public OnlineMenuIdofGroup()
	        {
	        }
	    }

	    public static final class OptionMenuFriend
	    {

	        public OptionMenuFriend()
	        {
	        }
	    }

	    public static final class OutBoxMenuIdofGroup
	    {

	        public OutBoxMenuIdofGroup()
	        {
	        }
	    }

	    public static final class RequestCode
	    {

	        public RequestCode()
	        {
	        }
	    }

	    public static final class StatusWB
	    {

	        public StatusWB()
	        {
	        }
	    }

	    public static final class VideoPageNumber
	    {

	        public VideoPageNumber()
	        {
	        }
	    }
	    
	    public static String getServerPath()
	    {
	        return serverPath;
	    }
	    
	    public static enum VideoPlayerError{
	    	AUDIOCODEC_UNSUPPORT_ERROR,
	    	EXCEPTION_ERROR,
	    	GETVIDEOSOURCE_ERROR,
	    	NETWORK_ERROR,
	    	STORAGE_UNENOUGH_ERROR,
	    	STREAMING_SERVER_ERROR,
	    	STREAMING_TIMEOUT_ERROR,
	    	UNSUPPORT_FILE_ERROR,
	    	UNSUPPORT_ONLINE_FILE_ERROR,
	    	VIDEOCODEC_UNSUPPORT_ERROR;
	    	
	    }
	    
	    public static boolean getIsArmv7Flag()
	    {
	        return ISARMV7;
	    }
	    
	    public static void setIsArmv7Flag(boolean flag)
	    {
	        ISARMV7 = flag;
	    }
	    
	    public static String getapkversion(Application paramApplication)
	    {
	      PackageManager localPackageManager = paramApplication.getPackageManager();
	      try
	      {
	        PackageInfo localPackageInfo = localPackageManager.getPackageInfo(paramApplication.getPackageName(), 0);
	        if (localPackageInfo != null)
	        {
	          String str = localPackageInfo.versionName;
	          return str;
	        }
	      }
	      catch (PackageManager.NameNotFoundException e)
	      {
	    	  e.printStackTrace();
	    	  return null;
	      }
	      
	      return null;
	    }
	    
	    
	    
	    public static String generateUUID()
	    {
	        Random random = new Random((new Date()).getTime());
	        String s = "";
	        int i = 0;
	        do
	        {
	            if(i >= 32)
	                return s;
	            char c = (char)(random.nextInt() % 16);
	            StringBuilder stringbuilder = new StringBuilder(String.valueOf(s));
	            int j;
	            if(c < '\n')
	                j = c + 48;
	            else
	                j = 97 + (c - 10);
	            s = stringbuilder.append(j).toString();
	            if(i == 7 || i == 11 || i == 15 || i == 19)
	                s = (new StringBuilder(String.valueOf(s))).append("-").toString();
	            i++;
	        } while(true);
	    }


}
