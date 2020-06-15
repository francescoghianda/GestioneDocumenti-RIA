package it.polimi.gd.dao;

import it.polimi.gd.beans.User;
import it.polimi.utils.hash.PasswordHashGenerator;
import it.polimi.utils.sql.ConnectionPool;
import it.polimi.utils.sql.PooledConnection;

import javax.servlet.http.HttpSession;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao
{
    private ConnectionPool connectionPool;

    public UserDao()
    {
        connectionPool = ConnectionPool.getInstance();
    }

    public boolean usernameExists(String username) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT TRUE FROM user WHERE username = ?"))
        {
            statement.setString(1, username);

            try(ResultSet resultSet = statement.executeQuery())
            {
                return resultSet.next();
            }
        }
    }

    public boolean emailExists(String email) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT TRUE FROM user WHERE email = ?"))
        {
            statement.setString(1, email);

            try(ResultSet resultSet = statement.executeQuery())
            {
                return resultSet.next();
            }
        }
    }

    public boolean createUser(String username, String email, String password) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "INSERT INTO user (username, email, password) VALUES (?, ?, ?)"))
        {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, PasswordHashGenerator.digest(password));

            return statement.executeUpdate() == 1;
        }
    }

    public boolean login(HttpSession session, String username, String password) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM user WHERE username = ?"))
        {
            statement.setString(1, username);
            try(ResultSet resultSet = statement.executeQuery())
            {
                if(!resultSet.next())return false;
                String passwordDigest = resultSet.getString("password");
                if(!PasswordHashGenerator.check(passwordDigest, password))return false;
                session.setAttribute("user", new User(resultSet.getInt("id"), username, resultSet.getString("email")));
                return true;
            }
        }
    }
}
