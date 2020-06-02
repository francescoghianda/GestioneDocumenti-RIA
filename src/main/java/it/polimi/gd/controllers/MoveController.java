package it.polimi.gd.controllers;

import it.polimi.gd.beans.DirectoryMetadata;
import it.polimi.gd.beans.DocumentMetadata;
import it.polimi.gd.dao.DirectoryDao;
import it.polimi.gd.dao.DocumentDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
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
            int directoryId = Integer.parseInt(req.getParameter("dir"));
            int documentId = Integer.parseInt(req.getParameter("doc"));

            Optional<DirectoryMetadata> directory = directoryDao.findDirectoryById(directoryId);

            if(!directory.isPresent())
            {
                resp.sendError(404, "Directory not found!");
                return;
            }

            Optional<DocumentMetadata> document = documentDao.findDocumentById(documentId);

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

            if(!documentDao.moveDocument(documentId, directoryId))
            {
                resp.sendError(500, "Error while moving document!");
                return;
            }

            resp.sendRedirect("/documents?dir="+directoryId);

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
