package it.polimi.gd.controllers;

import it.polimi.gd.beans.Document;
import it.polimi.gd.beans.User;
import it.polimi.gd.dao.DocumentDao;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/document")
public class DocumentDetailsController extends HttpServlet
{
    private DocumentDao documentDao;

    public DocumentDetailsController()
    {
        super();
    }

    @Override
    public void init()
    {
        documentDao = new DocumentDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            User user = (User) req.getSession().getAttribute("user");
            int documentId = Integer.parseInt(req.getParameter("doc"));
            Optional<Document> document = documentDao.findDocumentById(documentId, user.getId());

            if(!document.isPresent())
            {
                resp.sendError(404, "Document not found!");
                return;
            }

            try (JsonGenerator generator = Json.createGenerator(resp.getWriter()))
            {
                generator.write(document.get().toJson().build());
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
