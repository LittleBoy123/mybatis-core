package com.littleboy.mybatis.utils.getinputstream.cache;

import com.littleboy.mybatis.utils.getinputstream.resulthandler.LBResultHandler;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.*;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import sun.plugin2.main.server.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class LBBaseExecutor<T> implements LBExecutor{
    private static final Log log = LogFactory.getLog(LBBaseExecutor.class);
    protected Transaction transaction;
    protected  LBExecutor wrapper;
    protected ConcurrentLinkedQueue<T> deferredLoads;
    protected LBPerpetualCache localCache;
    protected LBPerpetualCache localOutPutParamterCache;
    protected Configuration configuration;
    protected int queryStack;
    private boolean closed;

    protected LBBaseExecutor(Configuration configuration,Transaction transaction){
        this.transaction = transaction;
        this.deferredLoads = new ConcurrentLinkedQueue<T>();
        this.localCache= new LBPerpetualCache("LocalCache");
        this.localOutPutParamterCache = new LBPerpetualCache("LocalOutPutParamterCache");
        this.closed = false;
        this.configuration = configuration;
        this.wrapper = this;
    }

    public void setWrapper(LBExecutor wrapper){
        this.wrapper = wrapper;
    }

    public <E> List<E> query(MappedStatement ms, Object paramter, RowBounds rowBounds, LBResultHandler resultHandler,CacheKey key,BoundSql boundSql)throws Exception{
        ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
        if(this.closed){
            throw new ExecutorException("Executor was closed.");
        }else{
            if(this.queryStack == 0 && ms.isFlushCacheRequired()){
                this.clearLocalCache();
            }
            List list;
            try{
                ++this.queryStack;
               list =  resultHandler == null ? (List) this.localCache.getObject(key) : null;
               if(list != null){
                   this.handleLocallyCacheOutPutParamter(ms,key,paramter,boundSql);
               }else{
                   list = this.queryFromDatabase(ms,paramter,rowBounds,resultHandler,key,boundSql);
               }
            }finally {
                --this.queryStack;
            }

            if(this.queryStack == 0){
                Iterator iterator = this.deferredLoads.iterator();
                while(iterator.hasNext()){
                    LBBaseExecutor.DeferredLoad deferredLoad = (LBBaseExecutor.DeferredLoad)iterator.next();
                    deferredLoad.load();
                }

                this.deferredLoads.clear();

                if(this.configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT){
                    this.clearLocalCache();
                }
            }
            return list;

        }
    }

    public <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter,RowBounds rowBounds, LBResultHandler resultHandler,CacheKey key,BoundSql boundSql)throws  Exception{
        this.localCache.putObject(key,ExecutionPlaceholder.EXECUTION_PLACEHOLDER);
        List list;
        try{
            list = this.doQuery(ms,parameter,rowBounds,resultHandler,boundSql);
        }finally {
            this.localCache.removeObject(key);
        }
        this.localCache.putObject(key, list);
        if(ms.getStatementType() == StatementType.CALLABLE){
            this.localOutPutParamterCache.putObject(key,parameter);
        }
        return list;
    }


    public void handleLocallyCacheOutPutParamter(MappedStatement ms,CacheKey key,Object parameter,BoundSql boundSql){
        if(ms.getStatementType() == StatementType.CALLABLE){
            Object cacheParameter = this.localOutPutParamterCache.getObject(key);
            if(cacheParameter != null && parameter != null){
                MetaObject metaCachedParamter = this.configuration.newMetaObject(cacheParameter);
                MetaObject metaParamter = this.configuration.newMetaObject(parameter);
                Iterator var8 = boundSql.getParameterMappings().iterator();

                while(var8.hasNext()){
                    ParameterMapping parameterMapping = (ParameterMapping) var8.next();
                    if(parameterMapping.getMode() != ParameterMode.IN){
                        String parameterName = parameterMapping.getProperty();
                        Object cacheValue = metaCachedParamter.getValue(parameterName);
                        metaParamter.setValue(parameterName,cacheValue);
                    }

                }
            }
        }
    }
    public void clearLocalCache(){
        if(!this.closed){
            this.localCache.clear();;
            this.localOutPutParamterCache.clear();
        }
    }

    private static class DeferredLoad{
        private final MetaObject resultObject;
        private final String property;
        private final Class<?> targetType;
        private final CacheKey key;
        private final LBPerpetualCache localCache;
        private final ObjectFactory factory;
        private final ResultExtractor resultExtractor;

        public DeferredLoad(MetaObject resultObject,String property,CacheKey key,LBPerpetualCache localCache,Configuration configuration,Class<?> targetType){
            this.resultObject = resultObject;
            this.key = key;
            this.localCache = localCache;
            this.property = property;
            this.factory = configuration.getObjectFactory();
            this.resultExtractor = new ResultExtractor(configuration,this.factory);
            this.targetType = targetType;
        }

        public boolean canLoad(){
            return this.localCache.getObject(this.key) != null && this.localCache.getObject(this.key) != ExecutionPlaceholder.EXECUTION_PLACEHOLDER;
        }


        public void load(){
            List<Object> list = (List) this.localCache.getObject(this.key);
            Object value = this.resultExtractor.extractObjectFromList(list,this.targetType);
            this.resultObject.setValue(property,value);
        }
    }
    protected Connection getConnection(Log statementLog) throws SQLException {
        Connection connection = this.transaction.getConnection();
        return statementLog.isDebugEnabled() ? ConnectionLogger.newInstance(connection,statementLog,this.queryStack):connection;
    }

    protected  void closeStatement(Statement statement){
        if(statement != null){
            try{
                /**
                 * isClose是JDK6.0新增加的
                 */
                statement.close();
            }catch (SQLException e){

            }
        }
    }

    public int update(MappedStatement ms, Object parameter){
        return 0;
    }
    public List<BatchResult> flushStatements(){
        return null;
    }
    public List<BatchResult> flushStatements(boolean isRollBack){
        return null;
    }
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException{
        return null;
    }
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql){
        return null;
    }
    public boolean isCached(MappedStatement ms, CacheKey key) {
        return this.localCache.getObject(key) != null;
    }
    public void commit(boolean required) throws SQLException{};
    public void rollback(boolean required) throws SQLException{};

    protected abstract int doUpdate(MappedStatement var1, Object var2) throws SQLException;

    protected abstract List<BatchResult> doFlushStatements(boolean var1) throws SQLException;

    protected abstract <E> List<E> doQuery(MappedStatement var1, Object var2, RowBounds var3, LBResultHandler var4, BoundSql var5) throws SQLException;

    protected abstract <E> Cursor<E> doQueryCursor(MappedStatement var1, Object var2, RowBounds var3, BoundSql var4) throws SQLException;
    protected void applyTransactionTimeout(Statement statement) throws SQLException{};
    private void handleLocallyCachedOutputParameters(MappedStatement ms, CacheKey key, Object parameter, BoundSql boundSql){}
    public void setExecutorWrapper(LBExecutor wrapper){
        this.wrapper = wrapper;
    };

}
