package it.polimi.utils.sql;

import java.sql.Connection;
import java.sql.SQLException;

public class PooledConnection implements AutoCloseable
{
    private final Connection connection;
    private long time;

    protected PooledConnection(Connection connection)
    {
        this.connection = connection;
        this.time = System.currentTimeMillis();
    }

    protected void resetTime()
    {
        time = System.currentTimeMillis();
    }

    protected int getIdleTimeMinutes()
    {
        return (int)(((System.currentTimeMillis() - time)/1000)/60);
    }

    public Connection getConnection()
    {
        return this.connection;
    }

    @Override
    public void close() throws SQLException
    {
        ConnectionPool.getInstance().freeConnection(this);
    }
}
