package com.littleboy.mybatis.utils.getinputstream.sqlsessionfactory;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;

import java.sql.Connection;

public interface LBSqlSessionFactory {
    SqlSession openSession();

    SqlSession openSessopn(boolean var1);

    SqlSession openSession(Connection var1);

    SqlSession openSession(TransactionIsolationLevel var1);

    SqlSession openSession(ExecutorType var1);

    SqlSession openSession(ExecutorType var1, boolean var2);

    SqlSession openSession(ExecutorType var1, Connection var2);

    SqlSession openSession(ExecutorType var1, TransactionIsolationLevel var2);

    Configuration getConfiguration();

}
