package com.littleboy.mybatis.utils.getinputstream.cache;

import java.util.concurrent.locks.ReadWriteLock;

public interface LBCache {
    String getId();

    void putObject(Object var1, Object var2);

    Object getObject(Object var1);

    Object removeObject(Object var1);

    void clear();

    int getSize();

    ReadWriteLock getReadWritLock();
}
