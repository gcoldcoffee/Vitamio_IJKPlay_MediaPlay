package vitamio.vitamiolibrary.videos.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import vitamio.vitamiolibrary.videos.mediaimpl.VPImpl;

/**
 * Created by aoe on 2015/12/22.
 */
public class ScreenShot {


    /**
     * 加载本地图片
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**截屏保存至本地**/
    public static void shotScrean(Context context,Bitmap bitmap,VPImpl.VPShotScreen vpShotScreen){
        boolean flag=false;
        String image_name=null;
        String filepath=null;
        if (bitmap!=null) {
            try {
                image_name = BoTools.getDataStr()+ ".jpg";
                File localFile1 = new File(FileUtil.getFilePath(context));
                if(!localFile1.exists()) {
                    localFile1.mkdirs();
                }
                filepath = localFile1.getAbsolutePath()+ "/" + image_name;
                FileOutputStream b = new FileOutputStream(filepath);
//                Matrix matrix = new Matrix();
//                matrix.postScale((float)(1*0.8), (float)(1*0.8));
//                Canvas canvas = new Canvas(bitmap);
//                Paint paint = new Paint();
//                paint.setFlags(Paint.FILTER_BITMAP_FLAG);
//                canvas.drawBitmap(bitmap, matrix, paint);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, b);
                b.flush();
                b.close();
                flag=true;
                //通知系统相册更新
                if(!BoTools.isEmpty(filepath)){
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(filepath)));
                }
            } catch (IOException e) {
                e.printStackTrace();
                flag=false;
            }
        }
        if(vpShotScreen!=null){
            vpShotScreen.shotScreen(flag,image_name,filepath);
        }
    }
}
