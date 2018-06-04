package com.littleboy.mybatis.utils.getinputstream.cache;

import com.littleboy.mybatis.utils.getinputstream.resulthandler.LBResultHandler;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BaseExecutor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class LBSimpleExecutor extends LBBaseExecutor {
    protected LBSimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    protected int doUpdate(MappedStatement mappedStatement, Object o) throws SQLException {
        return 0;
    }

    protected List<BatchResult> doFlushStatements(boolean b) throws SQLException {
        return null;
    }

    protected <E> List<E> doQuery(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Statement stmt = null;

        List var9;
        try{
            Configuration configuration = mappedStatement.getConfiguration();
            StatementHandler handler = configuration.newStatementHandler(this.wrapper,mappedStatement,parameter,rowBounds,resultHandler,boundSql);
            stmt = this.prepareStatement(handler,mappedStatement.getStatementLog());
            var9 = handler.query(stmt,resultHandler);
        }finally {
            this.closeStatement(stmt);
        }

        return var9;
    }



    public Statement prepareStatement(StatementHandler resultHandler,Log statementLog) throws SQLException {
        Connection connection = this.getConnection(statementLog);
        Statement stmt = resultHandler.prepare(connection,this.transaction.getTimeout());
        resultHandler.parameterize(stmt);
        return stmt;
    }




}
