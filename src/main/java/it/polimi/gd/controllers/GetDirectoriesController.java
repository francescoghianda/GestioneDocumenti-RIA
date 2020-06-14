package it.polimi.gd.controllers;

import it.polimi.gd.beans.Directory;
import it.polimi.gd.beans.User;
import it.polimi.gd.dao.DirectoryDao;
import it.polimi.utils.file.DirectoriesTree;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/get-directories")
public class GetDirectoriesController extends HttpServlet
{
    private DirectoryDao directoryDao;

    public GetDirectoriesController()
    {
        super();
    }

    @Override
    public void init()
    {
        directoryDao = new DirectoryDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try
        {
            User user = (User) req.getSession().getAttribute("user");
            List<Directory> directories = directoryDao.findAll(user.getId());

            DirectoriesTree directoriesTree = DirectoriesTree.build(directories);
            try(JsonGenerator generator = Json.createGenerator(resp.getWriter()))
            {
                generator.write(directoriesTree.toJson().build());
            }
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getLocalizedMessage());
        }
    }
}
