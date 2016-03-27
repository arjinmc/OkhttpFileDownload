package com.arjinmc.okhttpfiledownload;

import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Okio;

/**
 * FileDownloader
 * Created by Eminem on 2016/3/26.
 */
public class FileDownloader {

    private static FileDownloader fileDownloader;

    public static FileDownloader getInstence(){
        if(fileDownloader==null){
            fileDownloader = new FileDownloader();
        }
        return fileDownloader;
    }


    public DownLoaderAsyncTask downLoaderAsyncTask;
    private DownloadProgressListener downloadProgressListener;

    public void downLoadFile(String url, String storePath, DownloadProgressListener downloadProgressListener) {
        downLoaderAsyncTask = new DownLoaderAsyncTask(url,storePath);
        downLoaderAsyncTask.execute(url);
        this.downloadProgressListener = downloadProgressListener;
    }

    public void cancel(){
        if(downLoaderAsyncTask!=null){
            downLoaderAsyncTask.cancel(true);
        }
    }

    private class DownLoaderAsyncTask extends AsyncTask<String, Integer, String> {

        private String url;
        private String storePath;

        public DownLoaderAsyncTask(String url, String storePath) {
            this.url = url;
            this.storePath = storePath;
        }

        @Override
        protected String doInBackground(String... params) {

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            final ProgressListener progressListener = new ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {

                    downloadProgressListener.onProgressUpdate(
                            (int)((100 * bytesRead) / contentLength),bytesRead,contentLength);
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Response originalResponse = chain.proceed(chain.request());
                            return originalResponse.newBuilder()
                                    .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                                    .build();
                        }
                    })
                    .connectTimeout(30, TimeUnit.MINUTES)
                    .writeTimeout(30, TimeUnit.MINUTES)
                    .readTimeout(30, TimeUnit.MINUTES)
                    .build();


            Response response = null;
            try {
                response = client.newCall(request).execute();
                response.body().source().readAll(Okio.sink(new File(storePath)));

                if (!response.isSuccessful()) {
                    downloadProgressListener.onError(new Exception("Unexpected code " + response));
                }else{
                    downloadProgressListener.onFinish();
                }
            } catch (IOException e) {
                e.printStackTrace();
                downloadProgressListener.onError(e);
            }


            return null;
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
            downloadProgressListener.onCancel();
        }

    }


    public interface DownloadProgressListener {
        public void onProgressUpdate(int progress, long bytesRead, long contentLength);
        public void onFinish();
        public void onCancel();
        public void onError(Exception e);
    }
}
