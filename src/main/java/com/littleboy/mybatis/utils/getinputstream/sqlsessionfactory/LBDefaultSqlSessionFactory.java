package com.littleboy.mybatis.utils.getinputstream.sqlsessionfactory;

import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class LBDefaultSqlSessionFactory implements LBSqlSessionFactory {
    private final Configuration configuration;

    public LBDefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    public SqlSession openSession() {
        return this.openSessionFromDataSource(this.configuration.getDefaultExecutorType(), (TransactionIsolationLevel) null, false);
    }

    public SqlSession openSessopn(boolean autoCommit) {
        return this.openSessionFromDataSource(this.configuration.getDefaultExecutorType(), (TransactionIsolationLevel) null, autoCommit);
    }


    public SqlSession openSession(TransactionIsolationLevel level) {
        return this.openSessionFromDataSource(this.configuration.getDefaultExecutorType(), level, false);
    }

    public SqlSession openSession(ExecutorType executorType) {
        return this.openSessionFromDataSource(executorType, (TransactionIsolationLevel) null, false);
    }

    public SqlSession openSession(ExecutorType executorType, boolean autoCommit) {
        return this.openSessionFromDataSource(executorType, (TransactionIsolationLevel) null, autoCommit);
    }

    public SqlSession openSession(ExecutorType executorType, TransactionIsolationLevel level) {
        return this.openSessionFromDataSource(executorType, level, false);
    }


    public SqlSession openSession(Connection connection) {
        return this.openSessionFromConnection(this.configuration.getDefaultExecutorType(), connection);
    }

    public SqlSession openSession(ExecutorType executorType, Connection connection) {
        return this.openSessionFromConnection(executorType, connection);
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    private SqlSession openSessionFromDataSource(ExecutorType executorType, TransactionIsolationLevel level, boolean autoCommit) {
        Transaction tx = null;
        DefaultSqlSession sqlSession;

        try {
            Environment environment = this.configuration.getEnvironment();
            TransactionFactory transactionFactory = this.getTransactionFactoryFromEnvironment(environment);
            tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
            Executor executor = this.configuration.newExecutor(tx, executorType);
            sqlSession = new DefaultSqlSession(this.configuration, executor, autoCommit);
        } catch (Exception e) {
            this.closeTransaction(tx);
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }


        return sqlSession;
    }

    private SqlSession openSessionFromConnection(ExecutorType executorType, Connection connection) {
        DefaultSqlSession sqlSession;

        try{
            boolean autoCommit;
            try{
                autoCommit = connection.getAutoCommit();
            }catch (SQLException e2){
                autoCommit = true;
            }
            Environment environment = this.configuration.getEnvironment();
            TransactionFactory transactionFactory = this.getTransactionFactoryFromEnvironment(environment);
            Transaction transaction = transactionFactory.newTransaction(connection);
            Executor executor = this.configuration.newExecutor(transaction,executorType);
            sqlSession = new DefaultSqlSession(this.configuration,executor,autoCommit);

        }catch (Exception e1){
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e1, e1);
        }finally {
            ErrorContext.instance().reset();
        }
        return sqlSession;
    }

    private TransactionFactory getTransactionFactoryFromEnvironment(Environment environment) {
        return (TransactionFactory) (environment != null && environment.getTransactionFactory() != null ? environment.getTransactionFactory():new ManagedTransactionFactory());
    }

    private void closeTransaction(Transaction tx) {
        if (tx != null) {
            try {
                tx.close();
            } catch (SQLException e) {
                ;
            }
        }
    }
}
