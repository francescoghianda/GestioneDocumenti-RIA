package it.polimi.gd.controllers;

import it.polimi.gd.beans.Directory;
import it.polimi.gd.beans.Document;
import it.polimi.gd.beans.User;
import it.polimi.gd.dao.DirectoryDao;
import it.polimi.gd.dao.DocumentDao;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@WebServlet("/documents")
public class DocumentController extends HttpServlet
{

    private DocumentDao documentDao;
    private DirectoryDao directoryDao;

    public DocumentController()
    {
        super();
    }

    @Override
    public void init()
    {
        documentDao = new DocumentDao();
        directoryDao = new DirectoryDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try
        {
            User user = (User) req.getSession().getAttribute("user");
            int directoryId = Integer.parseInt(req.getParameter("dir"));

            Optional<Directory> directory = directoryDao.findDirectoryById(directoryId, user.getId());

            if(!directory.isPresent())
            {
                resp.sendError(404, "Directory not found.");
                return;
            }

            List<Document> documents = documentDao.findDocumentsByParentId(directoryId, user.getId());

            try (JsonGenerator generator = Json.createGenerator(resp.getWriter()))
            {
                JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                documents.forEach(document -> arrayBuilder.add(document.toJson()));
                generator.write(arrayBuilder.build());
            }
        }
        catch (NumberFormatException e)
        {
            resp.sendError(400, e.getLocalizedMessage());
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getSQLState());
        }
    }
}
