package com.littleboy.mybatis.utils.getinputstream.sqlsessionfactory;

import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.parsing.XNode;
import org.w3c.dom.CharacterData;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.util.*;

public class LBXNode {
    private final Node node;
    private final String name;
    private final String body;
    private final Properties attributes;
    private final Properties variables;
    private final LBXMLParse lbxmlParse;

    public LBXNode(LBXMLParse lbxmlParse,Node node,Properties variables){
        this.lbxmlParse = lbxmlParse;
        this.node = node;
        this.name = node.getNodeName();

        this.variables = variables;
        this.attributes = this.parseAttribute(node);

        this.body = this.parseBody(node);
    }

    public Properties parseAttribute(Node node){
        Properties attibutes = new Properties();
        NamedNodeMap attributesNode = node.getAttributes();

        if(attributesNode != null){
            for (int i = 0; i < attributesNode.getLength() ; i++) {
                Node attribute = attributesNode.item(i);
                String value = PropertyParser.parse(attribute.getNodeValue(),this.variables);
                attibutes.put(attribute.getNodeName(),value);

            }
        }

        return attibutes;
    }

    public String getName() {
        return name;
    }

    public String parseBody(Node node){
        String data = this.getBodyData(node);
        if(null == data){
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength() ; i++) {
                Node child = children.item(i);
                data = this.getBodyData(node);
                if(null != data){
                    break;
                }
            }

        }
        return data;
    }
    /**
     *xml的节点类型
     * 1	ELEMENT_NODE：表示element元素
     *
     * 2	ATTRIBUTE_NODE：表示属性
     *
     * 3	TEXT_NODE：表示元素或者属性中的文本内容
     *
     * 4	CDATA_SECTION_NODE：表示文档中的CDATA字段，文档不会被解析器解析
     *
     * 5	ENTITY_REFERENCE_NODE：
     *
     * 6	ENTITY_NODE：表示实体
     *
     * 7	PROCESSING_INSTRUCTION_NODE：表示处理指令
     *
     * 8	COMMENT_NODE：表示注释
     *
     * 9	DOCUMENT_NODE：表示整个文档  dom树的根节点   文档节点或者跟节点
     *
     * 10	DOCUMENT_TYPE_NODE：为文档定义的实体提供接口
     *
     * 11	DOCUMENT_FRAGMENT_NODE：轻量级的document对象，其中容纳了一部分文档
     *
     * 12	NOTATION_NODE：表示在DTD中声名的符号
     * */
    public String getBodyData(Node child){

        if(child.getNodeType() != 4 && child.getNodeType() != 3){
            return null;
        }else{
            String data = ((CharacterData) child).getData();
            data = PropertyParser.parse(data,this.variables);
            return data;
        }
    }


    public Properties getChildrenAsProperties(){
        Properties properties = new Properties();
        Iterator iterator = this.getChildren().iterator();
        while (iterator.hasNext()){
            LBXNode child = (LBXNode) iterator.next();
            String name = child.getStringAttribute("name");
            String value = child.getStringAttribute("value");
            if (name != null && value != null) {
                properties.setProperty(name, value);
            }
        }
        return properties;
    }
    public List<LBXNode> getChildren(){
        List<LBXNode> children = new ArrayList<LBXNode>();
        NodeList nodeList = this.node.getChildNodes();

        if(null != nodeList){
            int i = 0;
            for(int n = nodeList.getLength();i<n;++i){
                Node node = nodeList.item(i);
                //如果是element元素标签
                if(node.getNodeType() == 1){
                    children.add(new LBXNode(this.lbxmlParse,node,this.variables));
                }
            }

        }
        return children;
    }

    public String getStringAttribute(String name){
       return this.getStringAttribute(name,(String) null);
    }

    public String getStringAttribute(String name,String def){

        String value = this.attributes.getProperty(name);

        return value == null ? def:value;
    }
    public LBXNode evalNode(String expression){
        LBXNode lbxNode = this.lbxmlParse.evalNode(this.node, expression);


        return this.lbxmlParse.evalNode(this.node,expression);
    }

    public Node getNode(){
        return this.node;
    }
}
