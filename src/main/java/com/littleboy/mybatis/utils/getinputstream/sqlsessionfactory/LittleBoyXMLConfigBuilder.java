package com.littleboy.mybatis.utils.getinputstream.sqlsessionfactory;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.loader.ProxyFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

public class LittleBoyXMLConfigBuilder extends BaseBuilder {

    private boolean parsed;
    private final LBXMLParse xmlParse;
    private String enviroment;

    //默认的实现类是DefaultReflectorFactory
    private final ReflectorFactory reflectorFactory;

    public LittleBoyXMLConfigBuilder(InputStream inputStream, String enviroment, Properties props) {
        this(new LBXMLParse(inputStream, true, props, new XMLMapperEntityResolver()), enviroment, props);
    }

    private LittleBoyXMLConfigBuilder(LBXMLParse xmlParse, String enviroment, Properties properties) {
        super(new Configuration());
        //由这个接口实现向上转型
        this.reflectorFactory = new DefaultReflectorFactory();
        this.xmlParse = xmlParse;
        //enviroment == ull;
        this.enviroment = enviroment;
        ErrorContext.instance().resource("SQL MAPPER Configuration");
        this.configuration.setVariables(properties);
        this.parsed = false;
    }

    public Configuration parse() throws Exception {
        if (this.parsed) {
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        } else {
            this.parsed = true;
            this.parseConfiguration(this.xmlParse.evalNode("/configuration"));
            return this.configuration;
        }

    }

    private void parseConfiguration(LBXNode node) {

        try {
            this.propertiesElement(node);
            Properties settings = this.settingsAsProperties(node.evalNode("setting"));
            this.loadCustomVfs(settings);
            this.typeAliasesElement(node.evalNode("typeAliases"));
            this.pluginElement(node.evalNode("plugins"));
            this.objectFactoryElement(node.evalNode("objectFactory"));
            this.objectWrapperFactoryElement(node.evalNode("objectWrapperFactory"));
            this.reflectorFactoryElement(node.evalNode("reflectorFactory"));
            this.settingsElement(settings);
            this.environmentsElement(node.evalNode("environments"));
            this.databaseIdProviderElement(node.evalNode("databaseIdProvider"));
            this.typeHandlerElement(node.evalNode("typeHandlers"));
            this.mapperElement(node.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }

    }

    private void mapperElement(LBXNode context) throws Exception {
        if (context != null) {
            Iterator var2 = context.getChildren().iterator();

            while (true) {
                while (var2.hasNext()) {
                    //有异常抛出
                    LBXNode child = (LBXNode) var2.next();
                    String resource;
                    if ("package".equals(child.getName())) {
                        resource = child.getStringAttribute("name");
                        this.configuration.addMappers(resource);
                    } else {
                        resource = child.getStringAttribute("resource");
                        String url = child.getStringAttribute("url");
                        String mapperClass = child.getStringAttribute("class");
                        XMLMapperBuilder mapperParser;
                        InputStream inputStream;
                        if (resource != null && url == null && mapperClass == null) {
                            ErrorContext.instance().resource(resource);
                            inputStream = Resources.getResourceAsStream(resource);
                            mapperParser = new XMLMapperBuilder(inputStream, this.configuration, resource, this.configuration.getSqlFragments());
                            mapperParser.parse();
                        } else if (resource == null && url != null && mapperClass == null) {
                            ErrorContext.instance().resource(url);
                            inputStream = Resources.getUrlAsStream(url);
                            mapperParser = new XMLMapperBuilder(inputStream, this.configuration, url, this.configuration.getSqlFragments());
                            mapperParser.parse();
                        } else {
                            if (resource != null || url != null || mapperClass == null) {
                                throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                            }

                            Class<?> mapperInterface = Resources.classForName(mapperClass);
                            this.configuration.addMapper(mapperInterface);
                        }
                    }
                }

                return;
            }
        }
    }

    private void typeHandlerElement(LBXNode parent) {
        if (null != parent) {
            Iterator iterator = parent.getChildren().iterator();

            if (iterator.hasNext()) {
                LBXNode child = (LBXNode) iterator.next();
                String typeHandlerPackage;
                if ("package".equals(child.getName())) {
                    typeHandlerPackage = child.getStringAttribute("name");
                    this.typeHandlerRegistry.register(typeHandlerPackage);
                } else {
                    typeHandlerPackage = child.getStringAttribute("javaType");
                    String jdbcTypeName = child.getStringAttribute("jdbcType");
                    String handlerTypeName = child.getStringAttribute("handler");
                    Class<?> javaTypeClass = this.resolveClass(typeHandlerPackage);
                    JdbcType jdbcType = this.resolveJdbcType(jdbcTypeName);
                    Class<?> typeHandlerClass = this.resolveClass(handlerTypeName);

                    if (null != javaTypeClass) {
                        if (jdbcType == null) {
                            this.typeHandlerRegistry.register(javaTypeClass, typeHandlerClass);
                        } else {
                            this.typeHandlerRegistry.register(javaTypeClass, jdbcType, typeHandlerClass);
                        }

                    } else {
                        this.typeHandlerRegistry.register(typeHandlerClass);
                    }
                }
            }
        }
    }

    private void databaseIdProviderElement(LBXNode context) throws Exception {
        DatabaseIdProvider idProvider = null;
        if (null != context) {
            String type = context.getStringAttribute("type");
            if ("VENDOR".equals(type)) {
                type = "DB_VENDOR";
            }

            Properties properties = context.getChildrenAsProperties();
            idProvider = (DatabaseIdProvider) this.resolveClass(type).newInstance();
            idProvider.setProperties(properties);
        }

        Environment environment = this.configuration.getEnvironment();
        if (null != environment && null != idProvider) {
            String dataBaseId = idProvider.getDatabaseId(environment.getDataSource());
            this.configuration.setDatabaseId(dataBaseId);
        }
    }

    private void environmentsElement(LBXNode context) throws Exception {


        if (null != context) {
            if (null == this.enviroment) {
                //System.out.println(context.getStringAttribute("default"));
                this.enviroment = context.getStringAttribute("default");
            }
            Iterator iterator = context.getChildren().iterator();



            while (iterator.hasNext()) {
                LBXNode node = (LBXNode) iterator.next();
                String id = node.getStringAttribute("id");
                if (this.isSpecifiedEnvironment(id)) {
                    TransactionFactory transactionFactory = this.transactionManagerElement(node.evalNode("transactionManager"));
                    DataSourceFactory dataFactory = this.dataSourceElement(node.evalNode("dataSource"));
                    DataSource dataSource = dataFactory.getDataSource();
                    Environment.Builder environmentBuilder = (new Environment.Builder(id)).transactionFactory(transactionFactory).dataSource(dataSource);
                    this.configuration.setEnvironment(environmentBuilder.build());
                }
            }
        }
    }

    private DataSourceFactory dataSourceElement(LBXNode context) throws Exception {
        if (null != context) {
            String type = context.getStringAttribute("type");
            Properties properties = context.getChildrenAsProperties();
            DataSourceFactory dataFactory = (DataSourceFactory) this.resolveClass(type).newInstance();
            dataFactory.setProperties(properties);
            return dataFactory;
        } else {
            throw new BuilderException("Environment declaration requires a DataSourceFactory.");
        }
    }

    private TransactionFactory transactionManagerElement(LBXNode context) throws Exception {
        if (null != context) {
            String type = context.getStringAttribute("type");
            Properties properties = context.getChildrenAsProperties();
            TransactionFactory factory = (TransactionFactory) this.resolveClass(type).newInstance();
            factory.setProperties(properties);
            return factory;
        } else {
            throw new BuilderException("Environment declaration requires a TransactionFactory.");
        }
    }

    private boolean isSpecifiedEnvironment(String id) {
        if (null == this.enviroment) {
            throw new BuilderException("No environment specified.");
        } else if (null == id) {
            throw new BuilderException("Environment requires an id attribute.");
        } else {
            return this.enviroment.equals(id);
        }
    }

    private void settingsElement(Properties props) {
        this.configuration.setAutoMappingBehavior(AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior", "PARTIAL")));
        this.configuration.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.valueOf(props.getProperty("autoMappingUnknownColumnBehavior", "NONE")));
        this.configuration.setCacheEnabled(this.booleanValueOf(props.getProperty("cacheEnabled"), true));
        this.configuration.setProxyFactory((ProxyFactory) this.createInstance(props.getProperty("proxyFactory")));
        this.configuration.setLazyLoadingEnabled(this.booleanValueOf(props.getProperty("lazyLoadingEnabled"), false));
        this.configuration.setAggressiveLazyLoading(this.booleanValueOf(props.getProperty("aggressiveLazyLoading"), false));
        this.configuration.setMultipleResultSetsEnabled(this.booleanValueOf(props.getProperty("multipleResultSetsEnabled"), true));
        this.configuration.setUseColumnLabel(this.booleanValueOf(props.getProperty("useColumnLabel"), true));
        this.configuration.setUseGeneratedKeys(this.booleanValueOf(props.getProperty("useGeneratedKeys"), false));
        this.configuration.setDefaultExecutorType(ExecutorType.valueOf(props.getProperty("defaultExecutorType", "SIMPLE")));
        this.configuration.setDefaultStatementTimeout(this.integerValueOf(props.getProperty("defaultStatementTimeout"), (Integer) null));
        this.configuration.setDefaultFetchSize(this.integerValueOf(props.getProperty("defaultFetchSize"), (Integer) null));
        this.configuration.setMapUnderscoreToCamelCase(this.booleanValueOf(props.getProperty("mapUnderscoreToCamelCase"), false));
        this.configuration.setSafeRowBoundsEnabled(this.booleanValueOf(props.getProperty("safeRowBoundsEnabled"), false));
        this.configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope", "SESSION")));
        this.configuration.setJdbcTypeForNull(JdbcType.valueOf(props.getProperty("jdbcTypeForNull", "OTHER")));
        this.configuration.setLazyLoadTriggerMethods(this.stringSetValueOf(props.getProperty("lazyLoadTriggerMethods"), "equals,clone,hashCode,toString"));
        this.configuration.setSafeResultHandlerEnabled(this.booleanValueOf(props.getProperty("safeResultHandlerEnabled"), true));
        this.configuration.setDefaultScriptingLanguage(this.resolveClass(props.getProperty("defaultScriptingLanguage")));
        Class<? extends TypeHandler> typeHandler = (Class<? extends TypeHandler>) this.resolveClass(props.getProperty("defaultEnumTypeHandler"));
        this.configuration.setDefaultEnumTypeHandler(typeHandler);
        this.configuration.setCallSettersOnNulls(this.booleanValueOf(props.getProperty("callSettersOnNulls"), false));
        this.configuration.setUseActualParamName(this.booleanValueOf(props.getProperty("useActualParamName"), true));
        this.configuration.setReturnInstanceForEmptyRow(this.booleanValueOf(props.getProperty("returnInstanceForEmptyRow"), false));
        this.configuration.setLogPrefix(props.getProperty("logPrefix"));
        Class<? extends Log> logImpl = (Class<? extends Log>) this.resolveClass(props.getProperty("logImpl"));
        this.configuration.setLogImpl(logImpl);
        this.configuration.setConfigurationFactory(this.resolveClass(props.getProperty("configurationFactory")));
    }

    private void reflectorFactoryElement(LBXNode context) throws Exception {
        if (null != context) {
            String type = context.getStringAttribute("type");
            ReflectorFactory reflectorFactory = (ReflectorFactory) this.resolveClass(type).newInstance();
            this.configuration.setReflectorFactory(reflectorFactory);
        }
    }

    private void objectWrapperFactoryElement(LBXNode context) throws Exception {
        if (null != context) {
            String type = context.getStringAttribute("type");
            ObjectWrapperFactory wrapperFactory = (ObjectWrapperFactory) this.resolveClass(type).newInstance();
            this.configuration.setObjectWrapperFactory(wrapperFactory);
        }
    }

    private void objectFactoryElement(LBXNode context) throws Exception {
        if (null != context) {
            String type = context.getStringAttribute("type");
            Properties properties = context.getChildrenAsProperties();
            ObjectFactory factory = (ObjectFactory) this.resolveClass(type).newInstance();
            factory.setProperties(properties);
            this.configuration.setObjectFactory(factory);
        }
    }

    private void pluginElement(LBXNode parent) throws Exception {
        if (null != parent) {
            Iterator iterator = parent.getChildren().iterator();
            while (iterator.hasNext()) {
                LBXNode child = (LBXNode) iterator.next();
                String intercept = child.getStringAttribute("interceptor");
                Properties properties = child.getChildrenAsProperties();
                Interceptor interceptorInstance = (Interceptor) this.resolveClass(intercept).newInstance();
                interceptorInstance.setProperties(properties);
                this.configuration.addInterceptor(interceptorInstance);
            }
        }
    }

    private void typeAliasesElement(LBXNode parent) {
        if (null != parent) {
            Iterator iterator = parent.getChildren().iterator();
            while (iterator.hasNext()) {
                LBXNode child = (LBXNode) iterator.next();
                String alias;
                if ("package".equals(child.getName())) {
                    alias = child.getStringAttribute("name");
                    this.configuration.getTypeAliasRegistry().registerAliases(alias);
                } else {
                    alias = child.getStringAttribute("alias");
                    String type = child.getStringAttribute("type");

                    try {
                        Class<?> clazz = Resources.classForName(type);
                        if (null == alias) {
                            this.typeAliasRegistry.registerAlias(clazz);
                        } else {
                            this.typeAliasRegistry.registerAlias(alias, clazz);
                        }

                    } catch (ClassNotFoundException c) {
                        throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + c, c);
                    }
                }

            }
        }
    }

    private Properties settingsAsProperties(LBXNode context) {
        if (null == context) {
            return new Properties();
        } else {
            Properties properties = context.getChildrenAsProperties();
            MetaClass metaConfig = MetaClass.forClass(Configuration.class, this.reflectorFactory);

            Iterator iterator = properties.keySet().iterator();
            Object key;
            do {
                if (!iterator.hasNext()) {
                    return properties;
                }
                key = iterator.next();
            } while (metaConfig.hasSetter(String.valueOf(key)));

            throw new BuilderException("The setting \" + key + \" is not known.  Make sure you spelled it correctly (case sensitive).");
        }
    }

    private void loadCustomVfs(Properties props) throws Exception {
        String value = props.getProperty("vfsImpl");
        if (null != value) {
            String[] clazzea = value.split(",");
            String[] var4 = clazzea;
            int var5 = clazzea.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String clazz = var4[var6];
                if (!(clazz.length() == 0)) {
                    Class<? extends VFS> vfsImpl = (Class<? extends VFS>) Resources.classForName(clazz);
                    this.configuration.setVfsImpl(vfsImpl);
                }
            }
        }
    }

    private void propertiesElement(LBXNode context) throws Exception {
        if (null != context) {
            Properties defaults = context.getChildrenAsProperties();
            String resource = context.getStringAttribute("resource");
            String url = context.getStringAttribute("url'");
            if (null != resource && null != resource) {
                throw new BuilderException("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
            }
            if (null != resource) {
                defaults.putAll(Resources.getResourceAsProperties(resource));
            } else if (null != url) {
                defaults.putAll(Resources.getUrlAsProperties(url));
            }

            Properties vars = this.configuration.getVariables();
            if (null != vars) {
                defaults.putAll(vars);
            }
            this.xmlParse.setVariables(defaults);
            this.configuration.setVariables(defaults);
        }
    }

}
