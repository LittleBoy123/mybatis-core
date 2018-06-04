package com.littleboy.mybatis.utils.getinputstream.resulthandler;

public interface LBResultContext<T> {
    T getResultObject();

    int getResultCount();
    boolean isStopped();
    void stop();
}
