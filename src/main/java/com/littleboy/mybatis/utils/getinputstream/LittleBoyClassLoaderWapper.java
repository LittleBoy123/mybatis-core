package com.littleboy.mybatis.utils.getinputstream;

import java.io.InputStream;

public class LittleBoyClassLoaderWapper {
    private ClassLoader defaultClassLoader;
    private ClassLoader systemClassLoader;

    LittleBoyClassLoaderWapper(){
        systemClassLoader = ClassLoader.getSystemClassLoader();
        System.out.println("SystemClassLoader:"+systemClassLoader);
    }
    //获取classLoader数组，后面几个参数的具体意义暂时不清楚，以后再做具体研究
    ClassLoader[] getClassLoaders(ClassLoader classLoader){
        System.out.println("classLoader: "+classLoader+"\n  this.defaultClassLoader: "+this.defaultClassLoader+
                "\n Thread.currentThread().getContextClassLoader(): "+Thread.currentThread().getContextClassLoader()+
                "\n this.getClass().getClassLoader(): "+this.getClass().getClassLoader()+
                "\n this.systemClassLoader: "+this.systemClassLoader);
        return new ClassLoader[] {classLoader,this.defaultClassLoader,Thread.currentThread().getContextClassLoader(),
                                this.getClass().getClassLoader(),this.systemClassLoader};
    }
    public InputStream getClassLoaderAsInputStream(String resource,ClassLoader[] classLoaders){
        ClassLoader[] classLoaders1 = classLoaders;
        int length = classLoaders1.length;
        for(int i = 0;i<length;i++){
            ClassLoader classLoader = classLoaders1[i];
            if(null != classLoader){
                InputStream inputStream = classLoader.getResourceAsStream(resource);
                System.out.println("inputStream: "+ inputStream.toString());
                if(null == inputStream){
                    inputStream = classLoader.getResourceAsStream("/"+resource);
                    System.out.println("inputStream: "+ inputStream.toString());
                }
                if(null != inputStream){
                    return inputStream;
                }

            }
        }
        return null;
    }

    InputStream getResourceAsStream(String resource,ClassLoader classLoader){
        return this.getClassLoaderAsInputStream(resource,this.getClassLoaders(classLoader));
    }

}
