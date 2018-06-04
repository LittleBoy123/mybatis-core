package com.littleboy.mybatis.utils.getinputstream.cache;

import org.apache.ibatis.cache.CacheException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

public class LBPerpetualCache implements  LBCache {
    private final String id;
    private Map<Object,Object> cache = new HashMap<Object,Object>();

    public LBPerpetualCache(String id){
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public int getSize() {
        return this.cache.size();
    }

    public void putObject(Object key, Object value) {
        this.cache.put(key,value);
    }

    public Object getObject(Object key) {
        return this.cache.get(key);
    }

    public Object removeObject(Object key) {
        return this.cache.remove(key);
    }

    public void clear() {
        this.cache.clear();
    }

    public ReadWriteLock getReadWritLock() {
        return null;
    }


    public boolean equals(Object obj) {
        if(this.getId() == null){
            throw new CacheException("Cache instances require an ID.");
        }else if(this == obj){
            return true;
        }else if(!(obj instanceof  LBCache)){
            return false;
        }else{
            LBCache otherCache = (LBCache) obj;
            return this.getId().equals(otherCache.getId());
        }

    }

    public int hashCode(){
        if(this.getId() == null){
            throw new CacheException("Cache instances require an ID.");
        }else{
            return this.getId().hashCode();
        }
    }
}
