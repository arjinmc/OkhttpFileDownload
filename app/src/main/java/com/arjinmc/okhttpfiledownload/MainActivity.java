package com.arjinmc.okhttpfiledownload;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    TextView tvProgress;
    ProgressBar pbProgress;
    Button btnDownload;
    Button btnDownloadService;
    Button btnCancel;

    private String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"light.apk";
//    private String url = "http://shouji.360tpcdn.com/140527/b1e11ecdace2f81bee42bc5fe54741cd/com.hicsg.light_1.apk";
    private String url = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk";

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            long read = msg.getData().getLong("read");
            long content = msg.getData().getLong("content");
            showView(msg.what,read,content);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        //if file is exists just detele it
        File file = new File(path);
        if(file.exists()){
            file.delete();
            Log.e("file","has been deleted");
        }

    }

    private void init() {

        tvProgress = (TextView) findViewById(R.id.tv_progress);
        pbProgress = (ProgressBar) findViewById(R.id.pb_progress);

        btnDownload = (Button) findViewById(R.id.btn_download);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileDownloader.getInstence().downLoadFile(url, path, new FileDownloader.DownloadProgressListener() {

                    @Override
                    public void onProgressUpdate(int progress, long bytesRead, long contentLength) {
                        Message msg = new Message();
                        msg.what = progress;
                        Bundle data = new Bundle();
                        data.putLong("read",bytesRead);
                        data.putLong("content",contentLength);
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onFinish() {
                        Log.e("downloading","finish");
                        //download success then start to install
                        startInstall();

                    }

                    @Override
                    public void onCancel() {
                        //when it be cancel
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });


        btnDownloadService = (Button) findViewById(R.id.btn_download_service);
        btnDownloadService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(MainActivity.this,DownloadService.class);
                serviceIntent.putExtra("url",url);
                serviceIntent.putExtra("path",path);
                startService(serviceIntent);
            }
        });

        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileDownloader.getInstence().cancel();
            }
        });
    }

    private void startInstall(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + path),"application/vnd.android.package-archive");
        startActivity(intent);
    }

    private void showView(int progress,long bytesRead,long contentLength){
        tvProgress.setText("download bytes:"+bytesRead+"\tfile tototal:"+contentLength);
        pbProgress.setProgress(progress);
    }
}
