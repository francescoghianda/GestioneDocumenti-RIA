package it.polimi.gd.dao;

import com.mysql.cj.xdevapi.ClientImpl;
import it.polimi.gd.Application;
import it.polimi.gd.beans.Directory;
import it.polimi.gd.beans.Document;
import it.polimi.utils.file.FileManager;
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
                resultSet.getInt("parent"),
                resultSet.getInt("owner"));
    }

    public List<Directory> findAll(int owner) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM directory WHERE owner = ?");
            )
        {
            statement.setInt(1, owner);
            try(ResultSet resultSet = statement.executeQuery())
            {
                List<Directory> directories = new ArrayList<>();
                while (resultSet.next())directories.add(metadataFromResultSet(resultSet));
                return directories;
            }

        }
    }

    public List<Directory> findRootDirectories(int owner) throws SQLException
    {
        return findDirectoriesByParentId(0, owner);
    }

    public List<Directory> findDirectoriesByParentId(int parentId, int owner) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM directory dir WHERE dir.parent = ? AND dir.owner = ?"))
        {
            statement.setInt(1, parentId);
            statement.setInt(2, owner);

            try(ResultSet resultSet = statement.executeQuery())
            {
                List<Directory> metadataList = new ArrayList<>();
                while (resultSet.next())
                    metadataList.add(metadataFromResultSet(resultSet));
                return metadataList;
            }
        }
    }

    public Optional<Directory> findDirectoryById(int id, int owner) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM directory dir WHERE dir.id = ? AND dir.owner = ?"))
        {
            statement.setInt(1, id);
            statement.setInt(2, owner);
            try(ResultSet resultSet = statement.executeQuery())
            {
                if(!resultSet.next()) return Optional.empty();
                return Optional.of(metadataFromResultSet(resultSet));
            }
        }
    }

    public Optional<Directory> findDirectory(String directoryName, int parentId, int owner) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM directory dir WHERE dir.name = ? AND dir.parent = ? AND dir.owner = ?"))
        {
            statement.setString(1, directoryName);
            statement.setInt(2, parentId);
            statement.setInt(3, owner);

            try(ResultSet resultSet = statement.executeQuery())
            {
                if(!resultSet.next())return Optional.empty();
                return Optional.of(metadataFromResultSet(resultSet));
            }
        }
    }

    public int createDirectory(String directoryName, int parentId, int owner) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "INSERT INTO directory (name, creation_date, parent, owner) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, directoryName);
            statement.setString(2, dateFormat.format(new Date()));
            statement.setInt(3, parentId);
            statement.setInt(4, owner);

            if(statement.executeUpdate() != 1) return -1;

            try(ResultSet resultSet = statement.getGeneratedKeys())
            {
                if(!resultSet.next()) return -1;
                return resultSet.getInt(1);
            }
        }
    }

    public boolean deleteDirectory(int id, int owner) throws SQLException
    {
        List<Directory> subDirectories = findDirectoriesByParentId(id, owner);

        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "DELETE FROM directory dir WHERE (dir.id = ? AND dir.owner = ?) OR (dir.parent = ? AND dir.owner = ?)"))
        {
            if(!deleteDocumentsInFolder(connection.getConnection(), id, owner))return false;
            for(Directory directory : subDirectories)
                if(!deleteDocumentsInFolder(connection.getConnection(), directory.getId(), owner)) return false;

            statement.setInt(1, id);
            statement.setInt(2, owner);
            statement.setInt(3, id);
            statement.setInt(4, owner);

            return statement.executeUpdate() > 0;
        }
    }

    private boolean deleteDocumentsInFolder(Connection connection, int folderId, int owner) throws SQLException
    {
        DocumentDao documentDao = new DocumentDao();
        List<Document> documents = documentDao.findDocumentsByParentId(folderId, owner);
        if(documents.isEmpty())return true;

        PreparedStatement statement = connection.prepareStatement("DELETE FROM document WHERE parent = ? and owner = ?");
        statement.setInt(1, folderId);
        statement.setInt(2, owner);
        Optional<Boolean> documentsDeleted = documents.stream().map(document -> FileManager.getInstance(Application.getServletContext()).deleteFile(document.getId(), owner)).reduce((b1, b2) -> b1 && b2);
        return statement.executeUpdate() >= 0 && documentsDeleted.orElse(false);
    }

}
