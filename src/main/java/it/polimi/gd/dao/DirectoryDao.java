package it.polimi.gd.dao;

import it.polimi.gd.beans.Directory;
import it.polimi.utils.sql.ConnectionPool;
import it.polimi.utils.sql.PooledConnection;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class DirectoryDao
{
    private final ConnectionPool connectionPool;
    private final SimpleDateFormat dateFormat;

    public DirectoryDao()
    {
        connectionPool = ConnectionPool.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    private Directory metadataFromResultSet(ResultSet resultSet) throws SQLException
    {
        return new Directory(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getDate("creation_date"),
                resultSet.getInt("parent"));
    }

    public List<Directory> findAll() throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM directory");
            ResultSet resultSet = statement.executeQuery())
        {
            List<Directory> directories = new ArrayList<>();
            while (resultSet.next())directories.add(metadataFromResultSet(resultSet));
            return directories;
        }
    }

    public List<Directory> findRootDirectories() throws SQLException
    {
        return findDirectoriesByParentId(0);
    }

    public List<Directory> findDirectoriesByParentId(int parentId) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM directory dir WHERE dir.parent = ?"))
        {
            statement.setInt(1, parentId);

            try(ResultSet resultSet = statement.executeQuery())
            {
                List<Directory> metadataList = new ArrayList<>();
                while (resultSet.next())
                    metadataList.add(metadataFromResultSet(resultSet));
                return metadataList;
            }
        }
    }

    public Optional<Directory> findDirectoryById(int id) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM directory dir WHERE dir.id = ?"))
        {
            statement.setInt(1, id);
            try(ResultSet resultSet = statement.executeQuery())
            {
                if(!resultSet.next()) return Optional.empty();
                return Optional.of(metadataFromResultSet(resultSet));
            }
        }
    }

    public Optional<Directory> findDirectory(String directoryName, int parentId) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM directory dir WHERE dir.name = ? AND dir.parent = ?"))
        {
            statement.setString(1, directoryName);
            statement.setInt(2, parentId);

            try(ResultSet resultSet = statement.executeQuery())
            {
                if(!resultSet.next())return Optional.empty();
                return Optional.of(metadataFromResultSet(resultSet));
            }
        }
    }

    public boolean createDirectory(String directoryName, int parentId) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "INSERT INTO directory (name, creation_date, parent) VALUES (?, ?, ?)"))
        {
            statement.setString(1, directoryName);
            statement.setString(2, dateFormat.format(new Date()));
            statement.setInt(3, parentId);

            System.out.println(statement.toString());

            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteDirectory(String directoryName, int parentId) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "DELETE FROM directory dir WHERE dir.name = ? AND dir.parent = ?"))
        {
            statement.setString(1, directoryName);
            statement.setInt(2, parentId);
            return statement.executeUpdate() == 1;
        }
    }

}
