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
import java.util.Optional;

@WebServlet("/move")
public class MoveController extends HttpServlet
{
    public DocumentDao documentDao;
    public DirectoryDao directoryDao;

    public MoveController()
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {

        try
        {
            User user = (User) req.getSession().getAttribute("user");
            int directoryId = Integer.parseInt(req.getParameter("dir"));
            int documentId = Integer.parseInt(req.getParameter("doc"));

            Optional<Directory> directory = directoryDao.findDirectoryById(directoryId, user.getId());

            if(!directory.isPresent())
            {
                resp.sendError(404, "Directory not found!");
                return;
            }

            Optional<Document> document = documentDao.findDocumentById(documentId, user.getId());

            if(!document.isPresent())
            {
                resp.sendError(404, "Document not found!");
                return;
            }

            if(document.get().getParentId() == directory.get().getId() || directory.get().getId() == 0)
            {
                resp.sendError(409, "Cannot move the document in that directory!");
                return;
            }

            if(documentDao.exists(document.get().getName(), directoryId, user.getId()))
            {
                resp.sendError(409, "Document with name "+document.get().getName()+" already exists in this directory!");
                return;
            }

            if(!documentDao.moveDocument(documentId, directoryId, user.getId()))
            {
                resp.sendError(500, "Error while moving document!");
                return;
            }

            resp.setStatus(200);

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
