package com.arjinmc.okhttpfiledownload;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Random;

/**
 * download service
 * Created by Eminem Lu on 25/3/16.
 * Email arjinmc@hotmail.com
 */
public class DownloadService extends Service {

    private NotificationManager notificationManager;
    private Notification notification;
    private RemoteViews rootView;

    private String path;
    private String url;

    private final int MSG_START = 1;
    private final int MSG_FINISH = 2;
    private final int MSG_ERROR = 3;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_START:
                    downaload();
                    break;
                case MSG_FINISH:
                    PendingIntent contentIntent = PendingIntent.getActivity(
                            DownloadService.this,0,install(path), PendingIntent.FLAG_UPDATE_CURRENT);
                    notification.contentIntent = contentIntent;
                    notification.flags = Notification.FLAG_AUTO_CANCEL;
                    viewFinish();
                    break;
            }

        }
    };

    private int notificationId = new Random().nextInt(Integer.MAX_VALUE);


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        url = intent.getStringExtra("url");
        path = intent.getStringExtra("path");

        notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notification=new Notification();
        notification.icon=android.R.drawable.stat_sys_download;
        notification.tickerText=getString(R.string.app_name)+" is dowloaded to update";
        notification.when= System.currentTimeMillis();
        notification.defaults = Notification.DEFAULT_LIGHTS;
        notification.flags =  Notification.FLAG_NO_CLEAR;

        rootView=new RemoteViews(getPackageName(),R.layout.widget_download_notification);
        notification.contentView=rootView;
        notification.contentView.setProgressBar(R.id.pb_progress,100,0,false);
        notification.contentView.setTextViewText(R.id.tv_title,"downloading...");

        notificationManager.notify(notificationId,notification);

        handler.sendEmptyMessage(MSG_START);

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * download
     */
    private void downaload(){
        FileDownloader.getInstence().downLoadFile(url, path, new FileDownloader.DownloadProgressListener() {
            @Override
            public void onProgressUpdate(int progress, long bytesRead, long contentLength) {
                Log.d("downloadProgress",progress+"");
                updateView(progress);

            }

            @Override
            public void onFinish() {
                handler.sendEmptyMessage(MSG_FINISH);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(Exception e) {
//                handler.sendEmptyMessage(MSG_START);
//                Log.e("error",error);

            }
        });

    }

    /**
     * download ing
     * @param progress
     */
    private void updateView(int progress){
        notification.contentView.setProgressBar(R.id.pb_progress,100,progress,false);
        notificationManager.notify(notificationId,notification);
    }

    /**
     * download complete to show view
     */
    private void viewFinish(){
        notification.contentView.setProgressBar(R.id.pb_progress,100,100,false);
        notification.contentView.setTextViewText(R.id.tv_title,"click to update");
        notificationManager.notify(notificationId,notification);
        onDestroy();
    }

    /***
     * install app
     * @param path
     * @return
     */
    public static Intent install(String path){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + path),"application/vnd.android.package-archive");
        return intent;
    }
}
