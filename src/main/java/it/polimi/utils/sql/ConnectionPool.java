package it.polimi.utils.sql;

import javax.servlet.ServletContext;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionPool
{
    private static final int MAX_IDLE_CONNECTION_TIME_MINUTES = 10;

    private static ConnectionPool instance;

    private final String databaseUrl;
    private final Properties connectionProperties;

    private final Queue<PooledConnection> freeConnections;
    private final List<PooledConnection> busyConnections;

    private ConnectionPool(String databaseUrl, String user, String password)
    {
        this.databaseUrl = databaseUrl;
        connectionProperties = new Properties();
        connectionProperties.setProperty("user", user);
        connectionProperties.setProperty("password", password);

        freeConnections = new ConcurrentLinkedQueue<>();
        busyConnections = Collections.synchronizedList(new ArrayList<>());
    }

    public static void init(ServletContext context) throws ClassNotFoundException
    {
        String driver = context.getInitParameter("dbDriver");
        String databaseURL = context.getInitParameter("dbUrl");
        String user = context.getInitParameter("dbUser");
        String password = context.getInitParameter("dbPassword");
        Class.forName(driver);

        instance = new ConnectionPool(databaseURL, user, password);
    }

    public static ConnectionPool getInstance()
    {
        return instance;
    }

    private PooledConnection createNewConnection() throws SQLException
    {
        return new PooledConnection(DriverManager.getConnection(databaseUrl, connectionProperties));
    }

    public PooledConnection getConnection() throws SQLException
    {
        PooledConnection connection = freeConnections.poll();

        if(connection == null || connection.getIdleTimeMinutes() > MAX_IDLE_CONNECTION_TIME_MINUTES && !connection.getConnection().isValid(3000))
            connection = createNewConnection();
        else connection.resetTime();

        busyConnections.add(connection);
        return connection;
    }

    void freeConnection(PooledConnection connection) throws SQLException
    {
        busyConnections.remove(connection);
        if(!connection.getConnection().isClosed() && !freeConnections.offer(connection))
            connection.getConnection().close();
    }

    public void close()
    {
        for(PooledConnection connection : freeConnections)
        {
            try {
                connection.getConnection().close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        for(PooledConnection connection : busyConnections)
        {
            try {
                connection.getConnection().close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }
}
