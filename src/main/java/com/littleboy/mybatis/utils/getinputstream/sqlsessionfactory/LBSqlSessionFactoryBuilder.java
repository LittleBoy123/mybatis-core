package com.littleboy.mybatis.utils.getinputstream.sqlsessionfactory;

import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LBSqlSessionFactoryBuilder {
    public LBSqlSessionFactoryBuilder(){ }
    public LBSqlSessionFactory build(InputStream inputStream){
       return  this.build((InputStream) inputStream, (String) null,(Properties) null);
    }

    public LBSqlSessionFactory build(InputStream inputStream,String enviorment, Properties props){
        LBSqlSessionFactory sqlSessionFactory;

        try{
            LittleBoyXMLConfigBuilder configBuilder = new LittleBoyXMLConfigBuilder(inputStream,enviorment,props);
            sqlSessionFactory = this.build(configBuilder.parse());
        }catch (Exception e){
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        }finally {
            ErrorContext.instance().reset();

            try {
                inputStream.close();
            } catch (IOException var13) {
                ;
            }
        }
        return sqlSessionFactory;
    }

    public LBSqlSessionFactory build(Configuration configuration){
        return new LBDefaultSqlSessionFactory(configuration);
    }
}
