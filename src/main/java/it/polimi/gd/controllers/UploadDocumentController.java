package it.polimi.gd.controllers;

import it.polimi.gd.beans.Directory;
import it.polimi.gd.beans.User;
import it.polimi.gd.dao.DirectoryDao;
import it.polimi.gd.dao.DocumentDao;
import it.polimi.utils.file.FileManager;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@WebServlet("/upload")
@MultipartConfig
public class UploadDocumentController extends HttpServlet
{
    private DirectoryDao directoryDao;
    private DocumentDao documentDao;
    private FileManager fileManager;
    private int maxDocumentNameLength;
    private int maxDocumentTypeLength;
    private int maxDocumentSummaryLength;

    public UploadDocumentController()
    {
        super();
    }

    @Override
    public void init()
    {
        documentDao = new DocumentDao();
        directoryDao = new DirectoryDao();
        fileManager = FileManager.getInstance(getServletContext());
        maxDocumentNameLength = Integer.parseInt(getServletContext().getInitParameter("maxDocumentNameLength"));
        maxDocumentTypeLength = Integer.parseInt(getServletContext().getInitParameter("maxDocumentTypeLength"));
        maxDocumentSummaryLength = Integer.parseInt(getServletContext().getInitParameter("maxDocumentSummaryLength"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try
        {
            User user = (User) req.getSession().getAttribute("user");
            int parentId = Integer.parseInt(req.getParameter("parent"));
            String name = Objects.toString(req.getParameter("name"), "");
            String summary = req.getParameter("summary");

            if(parentId == 0)
            {
                resp.sendError(409, "Cannot create document in this directory!");
                return;
            }

            Optional<Directory> directory = directoryDao.findDirectoryById(parentId, user.getId());

            if(!directory.isPresent())
            {
                resp.sendError(404, "Directory not found!");
            }

            if(name.trim().isEmpty() || name.length() > maxDocumentNameLength)
            {
                resp.sendError(400, "Invalid document name!");
                return;
            }

            if(summary.length() > maxDocumentSummaryLength)
            {
                resp.sendError(400, "Summary too long! (max "+maxDocumentSummaryLength+" characters)");
                return;
            }

            Part documentPart = req.getPart("document");

            String[] fileName = documentPart.getSubmittedFileName().split("\\.");
            String documentType = fileName.length >= 2 ? fileName[fileName.length-1].toUpperCase() : "UNKNOWN";

            if(documentType.length() > maxDocumentTypeLength)
            {
                resp.sendError(400, "Document format too long! (max "+maxDocumentTypeLength+" characters)");
                return;
            }

            int documentId;

            if((documentId = documentDao.createDocument(name, summary.isEmpty() ? null : summary, documentType, parentId, user.getId())) <= 0)
            {
                resp.sendError(409, "Document already exists!");
                return;
            }

            FileOutputStream outputStream = fileManager.getFileOutputStream(documentId, user.getId());

            if(outputStream == null)
            {
                resp.sendError(500, "Error uploading file!");
                documentDao.deleteDocument(documentId, user.getId());
                return;
            }

            InputStream inputStream = documentPart.getInputStream();
            int length;
            byte[] buffer = new byte[4096];

            while((length = inputStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, length);

            outputStream.flush();
            outputStream.close();

            try(JsonGenerator generator = Json.createGenerator(resp.getWriter()))
            {
                JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
                objectBuilder.add("id", documentId);
                objectBuilder.add("parentId", parentId);
                objectBuilder.add("name", name);
                generator.write(objectBuilder.build());
            }

        }
        catch (NumberFormatException e)
        {
            resp.sendError(400, e.getLocalizedMessage());
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getLocalizedMessage());
        }
    }
}
