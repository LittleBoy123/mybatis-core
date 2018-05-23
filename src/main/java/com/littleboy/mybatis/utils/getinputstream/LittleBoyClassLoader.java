package com.littleboy.mybatis.utils.getinputstream;

import java.io.IOException;
import java.io.InputStream;

public class LittleBoyClassLoader {
    public static LittleBoyClassLoaderWapper classLoaderWapper = new LittleBoyClassLoaderWapper();
    public static InputStream getInputStream(String resources) throws  IOException{
        return getInputStream(null,resources);
    }
    public static InputStream getInputStream(ClassLoader classLoader,String resources) throws IOException{
        InputStream resourceAsStream = classLoaderWapper.getResourceAsStream(resources, classLoader);
        if(null == resourceAsStream){
            throw new IOException("could not find resource" + resources);
        }else{
            return resourceAsStream;
        }
    }
}
