package it.polimi.gd.controllers;

import it.polimi.gd.Application;
import it.polimi.gd.beans.Directory;
import it.polimi.gd.dao.DirectoryDao;
import org.thymeleaf.context.WebContext;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@WebServlet("/new-directory")
public class NewDirectoryController extends HttpServlet
{
    private DirectoryDao directoryDao;
    private int maxDirectoryNameLength;

    public NewDirectoryController()
    {
        super();
    }

    @Override
    public void init()
    {
        directoryDao = new DirectoryDao();
        maxDirectoryNameLength = Integer.parseInt(getServletContext().getInitParameter("maxDirectoryNameLength"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            int parentId = Integer.parseInt(req.getParameter("parent"));
            WebContext webContext = new WebContext(req, resp, getServletContext(), req.getLocale());
            webContext.setVariable("parentId", parentId);
            webContext.setVariable("maxNameLen", maxDirectoryNameLength);
            Application.getTemplateEngine().process("new-directory", webContext, resp.getWriter());
        }
        catch (NumberFormatException e)
        {
            resp.sendError(400, e.getLocalizedMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            int parentId = Integer.parseInt(req.getParameter("parent"));
            String name = Objects.toString(req.getParameter("name"), "");

            if(name.trim().isEmpty() || name.length() > maxDirectoryNameLength)
            {
                resp.sendError(400, "Invalid directory name!");
                return;
            }

            if(parentId != 0)
            {
                Optional<Directory> parentDir = directoryDao.findDirectoryById(parentId);

                if(!parentDir.isPresent())
                {
                    resp.sendError(404, "Parent directory not found!");
                    return;
                }

                if(parentDir.get().getParentId() != 0)
                {
                    resp.sendError(409, "Cannot create a subdirectory in this directory!");
                    return;
                }
            }

            if(!directoryDao.createDirectory(name, parentId))
            {
                resp.sendError(409, "The directory named "+name+" already exists!");
                return;
            }

            resp.sendRedirect("/");
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
