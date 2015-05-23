package com.mtxc.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * 下载图片工具类
 */
public class DownLoadUtil{

    private OnDownLoadListener onDownLoadListener = null;

    /**
     * 下载路径
     */
    public final static String DOWNLOAD_DIR = "/TreasureCity/imgDownLoad";

    /**
     * 图片缓存路径
     */
    public final static String CACHE_DIR = "/TreasureCity/imgCache";

    /**
     * 开始下载任务
     * @param url  图片的url
     * @param path   图片保存的路径
     */
    public void downLoad(final String url, final String path){
        new Thread(){
            @Override
            public void run() {
                try {
                    //开始下载
                    if(onDownLoadListener != null)
                        onDownLoadListener.onDownLoadStart();
                    URL mUrl = new URL(url);
                    Drawable drawable = Drawable.createFromStream(mUrl.openStream(), "");
                    Bitmap bitmap = drawableToBitmap(drawable);
                    saveMyBitmap(bitmap, path);
                    //下载完成
                    if(onDownLoadListener != null)
                        onDownLoadListener.onDownLoadComplete();
                } catch (Exception e) {
                    //下载出错
                    if(onDownLoadListener != null)
                        onDownLoadListener.onDownLoadError();
                }
            }
        }.start();
    }

    /**
     * 设置TextView的html显示
     */
    public static void setHtmlText(final TextView textView, final String html, final int imgWidth, final int imgHeight){
        /**
         * 创建下载路径
         */
        File f = new File(Environment.getExternalStorageDirectory().getPath() + DOWNLOAD_DIR);
        if(!f.exists()) {
            f.mkdirs();
        }

        final DownLoadUtil downLoadUtil = new DownLoadUtil();

        /**
         * 设置获取图片的回调
         */
        final Html.ImageGetter imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String s) {
                Drawable drawable = null;
                File filePath = new File(Environment.getExternalStorageDirectory().getPath() + DOWNLOAD_DIR + "/" + String.valueOf(s.hashCode())+".jpg");
                if(filePath.exists()){
                    //文件存在
                    drawable = Drawable.createFromPath(filePath.getPath());
                    drawable.setBounds(0, 0, imgWidth, imgHeight);
                }else{
                    //文件不存在，下载图片
                    downLoadUtil.downLoad(s, filePath.getPath());
                }
                return drawable;
            }
        };

        /**
         * 设置下载监听器
         */
        downLoadUtil.setOnDownLoadListener(new DownLoadUtil.OnDownLoadListener() {
            @Override
            public void onDownLoadStart() {

            }

            @Override
            public void onDownLoadComplete() {
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(Html.fromHtml(html, imageGetter, null));
                    }
                });
            }

            @Override
            public void onDownLoadError() {

            }
        });

        textView.setText(Html.fromHtml(html, imageGetter, null));

    }

    /**
     * 从缓存中设置imageView的图片
     */
    public static void setImageWithCache(final ImageView imageView, String url){
        /**
         * 创建下载路径
         */
        File f = new File(Environment.getExternalStorageDirectory().getPath() + CACHE_DIR);
        if(!f.exists()) {
            f.mkdirs();
        }

        final File filePath = new File(Environment.getExternalStorageDirectory().getPath() + CACHE_DIR + "/" + String.valueOf(url.hashCode())+".jpg");
        if(filePath.exists()){
            //文件存在
            Uri uri = Uri.fromFile(filePath);
            imageView.setImageURI(uri);
        }else{
            //文件不存在，下载图片
            DownLoadUtil downLoadUtil = new DownLoadUtil();
            downLoadUtil.setOnDownLoadListener(new OnDownLoadListener() {
                @Override
                public void onDownLoadStart() {
                    //下载开始
                }

                @Override
                public void onDownLoadComplete() {
                    //下载完成
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageURI(Uri.fromFile(filePath));
                        }
                    });
                }

                @Override
                public void onDownLoadError() {
                    //下载出错
                }
            });
            downLoadUtil.downLoad(url, filePath.getPath());
        }
    }

    /**
     * 将drawable转换成bitmap
     */
    public Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 将bitmap保存到file
     */
    public void saveMyBitmap(Bitmap bm,String path) throws IOException {
        File f = new File(path);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bm.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置下载监听器
     */
    public void setOnDownLoadListener(OnDownLoadListener onDownLoadListener) {
        this.onDownLoadListener = onDownLoadListener;
    }

    /**
     * 下载监听器
     */
    public interface OnDownLoadListener{
        void onDownLoadStart();
        void onDownLoadComplete();
        void onDownLoadError();
    }
}
