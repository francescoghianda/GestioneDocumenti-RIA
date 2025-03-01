package it.polimi.gd.dao;

import it.polimi.gd.Application;
import it.polimi.gd.beans.Document;
import it.polimi.utils.file.FileManager;
import it.polimi.utils.sql.ConnectionPool;
import it.polimi.utils.sql.PooledConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class DocumentDao
{
    private final ConnectionPool connectionPool;

    public DocumentDao()
    {
        connectionPool = ConnectionPool.getInstance();
    }

    private Document metadataFromResultSet(ResultSet resultSet) throws SQLException
    {
        return new Document(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getTimestamp("creation_date"),
                resultSet.getString("summary"),
                resultSet.getString("type"),
                resultSet.getInt("parent"),
                resultSet.getInt("owner"));
    }

    public Optional<Document> findDocumentById(int documentId, int owner) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM document WHERE id = ? AND owner = ?"))
        {
            statement.setInt(1, documentId);
            statement.setInt(2, owner);

            try(ResultSet resultSet = statement.executeQuery())
            {
                if(!resultSet.next())return Optional.empty();
                return Optional.of(metadataFromResultSet(resultSet));
            }
        }
    }

    public List<Document> findDocumentsByParentId(int parentId, int owner) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM document doc WHERE doc.parent = ? AND doc.owner = ?"))
        {
            statement.setInt(1, parentId);
            statement.setInt(2, owner);

            try(ResultSet resultSet = statement.executeQuery())
            {
                List<Document> metadataList = new ArrayList<>();
                while (resultSet.next())
                    metadataList.add(metadataFromResultSet(resultSet));
                return metadataList;
            }
        }
    }

    public boolean moveDocument(int documentId, int destinationDirectoryId, int owner) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "UPDATE document SET parent = ? WHERE id = ? AND owner = ?"))
        {
            statement.setInt(1, destinationDirectoryId);
            statement.setInt(2, documentId);
            statement.setInt(3, owner);

            return statement.executeUpdate() == 1;
        }
    }

    public boolean exists(String documentName, int parentId, int owner) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT id FROM document doc WHERE doc.name = ? AND doc.parent = ? AND doc.owner = ?"))
        {
            statement.setString(1, documentName);
            statement.setInt(2, parentId);
            statement.setInt(3, owner);

            try(ResultSet resultSet = statement.executeQuery())
            {
                return resultSet.next();
            }
        }
    }

    public Optional<Document> createDocument(String documentName, String summary, String documentType, int parentId, int owner) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "INSERT INTO document (name, creation_date, summary, type, parent, owner) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS))
        {

            Timestamp now = new Timestamp(new Date().getTime());

            statement.setString(1, documentName);
            statement.setTimestamp(2, now);
            statement.setString(3, summary);
            statement.setString(4, documentType);
            statement.setInt(5, parentId);
            statement.setInt(6, owner);

            connection.getConnection().setAutoCommit(false);

            if(statement.executeUpdate() == 0)return Optional.empty();

            try(ResultSet resultSet = statement.getGeneratedKeys())
            {
                if(!resultSet.next())
                {
                    connection.getConnection().rollback();
                    connection.getConnection().setAutoCommit(true);
                    return Optional.empty();
                }
                connection.getConnection().commit();
                connection.getConnection().setAutoCommit(true);
                int id = resultSet.getInt(1);
                return Optional.of(new Document(id, documentName, now, summary, documentType, parentId, owner));
            }
        }

    }

    public boolean deleteDocument(int documentId, int owner) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "DELETE FROM document doc WHERE doc.id = ? AND doc.owner = ?"))
        {
            statement.setInt(1, documentId);
            statement.setInt(2, owner);
            return statement.executeUpdate() == 1 && FileManager.getInstance(Application.getServletContext()).deleteFile(documentId, owner);
        }
    }

}
