package it.polimi.gd.controllers;

import it.polimi.gd.beans.Directory;
import it.polimi.gd.beans.Document;
import it.polimi.gd.beans.User;
import it.polimi.gd.dao.DirectoryDao;
import it.polimi.gd.dao.DocumentDao;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@WebServlet("/delete")
public class DeleteController extends HttpServlet
{
    private DocumentDao documentDao;
    private DirectoryDao directoryDao;

    public DeleteController()
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            User user = (User)req.getSession().getAttribute("user");
            String type = Objects.toString(req.getParameter("type"), "");
            int id = Integer.parseInt(req.getParameter("id"));

            switch (type)
            {
                case "doc":
                    Optional<Document> document = documentDao.findDocumentById(id, user.getId());
                    if(!document.isPresent())
                    {
                        resp.sendError(404, "Document not found!");
                        return;
                    }
                    if(!documentDao.deleteDocument(id, user.getId()))
                    {
                        resp.sendError(500, "Error deleting document.");
                        return;
                    }
                    resp.setStatus(200);
                    return;
                case "dir":
                    Optional<Directory> directory = directoryDao.findDirectoryById(id, user.getId());
                    if(!directory.isPresent())
                    {
                        resp.sendError(404, "Directory not found!");
                        return;
                    }
                    if(!directoryDao.deleteDirectory(id, user.getId()))
                    {
                        resp.sendError(500, "Error deleting folder.");
                        return;
                    }
                    resp.setStatus(200);
                    return;
                default:
                    resp.sendError(400, "Invalid type. (type must be doc or dir)");
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
