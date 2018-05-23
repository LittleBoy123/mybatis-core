package com.littleboy.mybatis.test;

import com.littleboy.mybatis.User;
import com.littleboy.mybatis.utils.getinputstream.sqlsessionfactory.LBSqlSessionFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

public class MybatisTest {
    public static void main(String[] arg) throws SQLException {
        //会话工厂
        SqlSessionFactory sqlSessionFactory;
        InputStream inputStream = null;

        // 配置文件   将配置文件转换为流的形式
        String resource = "SqlMapConfig.xml";
        try {
            inputStream = Resources.getResourceAsStream(resource);
            //inputStream = MybatisTest.class.getClassLoader().getResourceAsStream(resource);
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            dbf.setNamespaceAware(false);
//
//            //documentBuilder
//            DocumentBuilder builder = dbf.newDocumentBuilder();
//            Document document = (Document) builder.parse(new InputSource(inputStream));
//
//            XPathFactory xPathFactory = XPathFactory.newInstance();
//            XPath xPath = xPathFactory.newXPath();
//
//            String expression = "/configuration";
//            XPathExpression compile = xPath.compile(expression);
//            //NodeList nodes = (NodeList) compile.evaluate(document, XPathConstants.NODESET);
//
//            Node node = (Node) xPath.evaluate(expression,document,XPathConstants.NODE);
//
//            Node node1 = (Node) xPath.evaluate("environments",node,XPathConstants.NODE);
//
//            NamedNodeMap attributes = node1.getAttributes();
//            Node aDefault = attributes.getNamedItem("default");
//            System.out.println(aDefault.getNodeValue());




        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("发生异常");
        }



        //String resource = "SqlMapConfig.xml";
//        try {
//            InputStream inputStream1 = LittleBoyClassLoader.getInputStream(resource);
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }

        //首先获取配置文件，将配置文件转换为流信息



         //使用SqlSessionFactoryBuilder从xml配置文件中创建SqlSessionFactory  注意到sqlSessionFactory并不是单列的
       //sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

       sqlSessionFactory = new LBSqlSessionFactory().build(inputStream);
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
