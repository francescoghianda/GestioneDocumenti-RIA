package it.polimi.gd.dao;

import it.polimi.gd.beans.DocumentMetadata;
import it.polimi.utils.file.Document;
import it.polimi.utils.sql.ConnectionPool;
import it.polimi.utils.sql.PooledConnection;

import java.net.CookieHandler;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

public class DocumentDao
{
    private final ConnectionPool connectionPool;

    public DocumentDao()
    {
        connectionPool = ConnectionPool.getInstance();
    }

    private DocumentMetadata metadataFromResultSet(ResultSet resultSet) throws SQLException
    {
        return new DocumentMetadata(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getDate("creation_date"),
                resultSet.getString("summary"),
                resultSet.getString("type"),
                resultSet.getInt("parent"));
    }

    public Optional<DocumentMetadata> findDocumentById(int documentId) throws SQLException
    {
        try(PooledConnection connection = ConnectionPool.getInstance().getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM document WHERE id = ?"))
        {
            statement.setInt(1, documentId);

            try(ResultSet resultSet = statement.executeQuery())
            {
                if(!resultSet.next())return Optional.empty();
                return Optional.of(metadataFromResultSet(resultSet));
            }
        }
    }

    public List<DocumentMetadata> findDocumentsByParentId(int parentId) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM document doc WHERE doc.parent = ?"))
        {
            statement.setInt(1, parentId);

            try(ResultSet resultSet = statement.executeQuery())
            {
                List<DocumentMetadata> metadataList = new ArrayList<>();
                while (resultSet.next())
                    metadataList.add(metadataFromResultSet(resultSet));
                return metadataList;
            }
        }
    }

    public boolean moveDocument(int documentId, int destinationDirectoryId) throws SQLException
    {
        try(PooledConnection connection = ConnectionPool.getInstance().getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "UPDATE document SET parent = ? WHERE id = ?"))
        {
            statement.setInt(1, destinationDirectoryId);
            statement.setInt(2, documentId);

            return statement.executeUpdate() == 1;
        }
    }

    public Optional<DocumentMetadata> getDocumentMetadata(String documentName, int parentId) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "SELECT * FROM document doc WHERE doc.name = ? AND doc.parent = ?"))
        {
            statement.setString(1, documentName);
            statement.setInt(2, parentId);

            try(ResultSet resultSet = statement.executeQuery())
            {
                if(!resultSet.next())return Optional.empty();
                return Optional.of(metadataFromResultSet(resultSet));
            }
        }
    }

    public boolean createDocument(String documentName, String summary, String documentType, int parentId) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "INSERT INTO document (name, creation_date, summary, type, parent) VALUES (?, ?, ?, ?, ?)"))
        {
            statement.setString(1, documentName);
            statement.setDate(2, new Date(Calendar.getInstance().getTimeInMillis()));
            statement.setString(3, summary);
            statement.setString(4, documentType);
            statement.setInt(5, parentId);

            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteDirectory(String documentName, int parentId) throws SQLException
    {
        try(PooledConnection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.getConnection().prepareStatement(
                    "DELETE FROM document doc WHERE doc.name = ? AND doc.parent = ?"))
        {
            statement.setString(1, documentName);
            statement.setInt(2, parentId);
            return statement.executeUpdate() == 1;
        }
    }

}
