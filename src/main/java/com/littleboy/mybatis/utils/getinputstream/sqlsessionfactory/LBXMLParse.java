package com.littleboy.mybatis.utils.getinputstream.sqlsessionfactory;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.parsing.XNode;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.Properties;

public class LBXMLParse {
    private final Document document;
    private boolean validation;
    private EntityResolver entityResolver;
    private Properties variables;
    private XPath xPath;


    public LBXMLParse(InputStream inputStream, boolean validation, Properties props, EntityResolver entityResolver) {
        this.commonConstructor(validation, props, entityResolver);
        document = this.createDocument(new InputSource(inputStream));
    }

    private void commonConstructor(boolean validation, Properties props, EntityResolver entityResolver) {
        this.validation = validation;
        this.entityResolver = entityResolver;
        this.variables = variables;
        this.xPath = XPathFactory.newInstance().newXPath();
    }

    private Document createDocument(InputSource inputSource) {
        try {
            //documentBuilderDactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validation);
            factory.setNamespaceAware(false);
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(false);
            factory.setCoalescing(false);
            factory.setExpandEntityReferences(true);

            //builder
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(entityResolver);
            builder.setErrorHandler(new ErrorHandler() {
                public void warning(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                }
            });
            return builder.parse(inputSource);
        } catch (Exception e) {

        }

        return null;
    }

    public LBXNode evalNode(String expression) {
        return this.evalNode(this.document, expression);
    }

    public LBXNode evalNode(Object root, String expression) {
        Node node = (Node) this.evaluate(expression, root, XPathConstants.NODE);
        if (expression.equals("environments")) {
            NamedNodeMap attributes = node.getAttributes();
            Node aDefault = attributes.getNamedItem("default");
        }

        return node == null ? null : new LBXNode(this, node, this.variables);
    }

    private Object evaluate(String expression, Object root, QName returnType) {
        try {


            return this.xPath.evaluate(expression, root, returnType);

        } catch (Exception var5) {
            throw new BuilderException("Error evaluating XPath.  Cause: " + var5, var5);
        }
    }

    public void setVariables(Properties variables) {
        this.variables = variables;
    }
}
