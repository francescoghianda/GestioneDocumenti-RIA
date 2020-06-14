package it.polimi.gd.controllers;

import it.polimi.gd.beans.Directory;
import it.polimi.gd.beans.User;
import it.polimi.gd.dao.DirectoryDao;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@WebServlet("/new-directory")
@MultipartConfig
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            User user = (User) req.getSession().getAttribute("user");
            int parentId = Integer.parseInt(req.getParameter("parent"));
            String name = Objects.toString(req.getParameter("name"), "");

            if(name.trim().isEmpty() || name.length() > maxDirectoryNameLength)
            {
                resp.sendError(400, "Invalid directory name!");
                return;
            }

            if(parentId != 0)
            {
                Optional<Directory> parentDir = directoryDao.findDirectoryById(parentId, user.getId());

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

            Optional<Directory> directory = directoryDao.findDirectory(name, parentId, user.getId());

            if(directory.isPresent())
            {
                resp.sendError(409, "The directory named "+name+" already exists!");
                return;
            }

            Optional<Directory> dir = directoryDao.createDirectory(name, parentId, user.getId());

            if(!dir.isPresent())
            {
                resp.sendError(500, "Error creating new folder!");
                return;
            }

            try(JsonGenerator generator = Json.createGenerator(resp.getWriter()))
            {
                generator.write(dir.get().toJson().build());
            }


        }
        catch (NumberFormatException e)
        {
            resp.sendError(400, "Id must be a number!");
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getLocalizedMessage());
        }
    }
}
