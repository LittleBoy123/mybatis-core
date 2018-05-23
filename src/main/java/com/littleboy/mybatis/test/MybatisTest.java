package com.littleboy.mybatis.test;

import com.littleboy.mybatis.User;
import com.littleboy.mybatis.utils.getinputstream.LittleBoyClassLoader;
import com.littleboy.mybatis.utils.getinputstream.sqlsessionfactory.LBSqlSessionFactory;
import com.littleboy.mybatis.utils.getinputstream.sqlsessionfactory.LBSqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;

import java.io.InputStream;
import java.util.List;

public class MybatisTest {
    public static void main(String[] arg) throws Exception {
        //会话工厂
        LBSqlSessionFactory sqlSessionFactory;
        InputStream inputStream = null;

        // 配置文件   将配置文件转换为流的形式
        String resource = "SqlMapConfig.xml";
        inputStream = LittleBoyClassLoader.getInputStream(resource);
        //inputStream = Resources.getResourceAsStream(resource);

         //使用SqlSessionFactoryBuilder从xml配置文件中创建SqlSessionFactory  注意到sqlSessionFactory并不是单列的
       //sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        sqlSessionFactory = new LBSqlSessionFactoryBuilder().build(inputStream);
        DataSource dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();


        // 根据 id查询用户信息

        // 数据库会话实例
        SqlSession sqlSession = null;
        try {
            // 创建数据库会话实例sqlSession   传入configuration  executor autocommit  sqlSessionFactory没有实际意义
            sqlSession = sqlSessionFactory.openSession();

            // 查询单个记录，根据用户id查询用户信息
            long l = System.currentTimeMillis();
            //insert   update delete 这些数据库操作之后都要进行提交  才能将数据插入数据库   我们在创建的时候提供了一种自动提交的方法
            //sqlSessionFactory.openSession(true)   这时候就可以自动提交了，默认为false需要我们手动去提交
            List<User> users = sqlSession.selectList("test.testquerytime");
            long l2 = System.currentTimeMillis();
            System.out.println(l2 - l);
            List<User> user2 = sqlSession.selectList("test.testquerytime");
            long l3 = System.currentTimeMillis();
            System.out.println(l3 - l2);

            // 输出用户信息

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("发生异常");
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }


    }


}
