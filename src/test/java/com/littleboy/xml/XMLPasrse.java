package com.littleboy.xml;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;

/**
*       xml 文档解析和mybatis同样利用xpath原生来进行解析  文档位置存放在E:/study/xml
 * time:2018/5/23
 * author:littleboy
*
* */
public class XMLPasrse {
    public static  void main(String[] args) throws Exception {
        String xmlPath = "E:\\study\\xml\\book.xml";
        String expression = "book/authors/author";

        //文件使用FileInputStream  如果是当前目录下  使用classLoader.getResourceAsStream
        InputStream inputStream = new FileInputStream(xmlPath);
        //InputStream inputStream = XMLPasrse.class.getClassLoader().getResourceAsStream(xmlPath);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //防止命名空间引发的错误
        dbf.setNamespaceAware(false);
        DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource(inputStream));

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        NodeList nodeList = (NodeList) xPath.evaluate(expression, document, XPathConstants.NODESET);
        //Node nodes = (Node) xPath.evaluate(expression, document, XPathConstants.NODE);

        System.out.println(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength() ; i++) {
            /**
             *xpath将每个key-value键值对当做是一个node 我们当前解析到author 可以看到  有4个author 对象，所以我们用list来接收
             * ，也可以使用单个Node来接收，r然后利用getChildNodes返回node集合，实际返回的是NodeList
             * ,mybatis是转为node之后再利用list集合来进行接收的，最后迭代操作，xml中，节点的属性值也是key-value形式的，所以也是
             * 一个node  当前节点如果直接获取值是null必须要获取属性map ,利用map获得相应的属性Node 在利用属性Node.getNodeValue来
             * 获取属性的值，getTextContext是获取当前节点的文本值
             */
            Node child = nodeList.item(i);

            NamedNodeMap attributes = child.getAttributes();
            Node nodeName = attributes.getNamedItem("name");
            Node nodeAge = attributes.getNamedItem("age");
            Node nodeSex = attributes.getNamedItem("sex");

            if(i == 3){
                //获取文本属性的值
                System.out.println(child.getTextContent());
            }

            System.out.println("name = "+nodeName.getNodeValue()+
                    "     "+"age = "+nodeAge.getNodeValue()+
                    "     "+"sex = "+nodeSex.getNodeValue());

        }


    }
}
