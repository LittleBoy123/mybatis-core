package com.littleboy.mybatis.utils.getinputstream.resulthandler;

public interface LBResultHandler<T> {
    void handlerResult(LBResultContext<? extends T> var1);

}
