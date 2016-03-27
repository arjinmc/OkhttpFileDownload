package com.arjinmc.okhttpfiledownload;

/**
 * Created by Eminem on 2016/3/26.
 */
public interface ProgressListener {
    void update(long bytesRead, long contentLength, boolean done);
}
