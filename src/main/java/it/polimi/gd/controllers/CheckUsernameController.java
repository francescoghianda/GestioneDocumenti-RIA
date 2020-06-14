package it.polimi.gd.controllers;

import it.polimi.gd.dao.UserDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Objects;

@WebServlet("/check-username")
public class CheckUsernameController extends HttpServlet
{
    private UserDao userDao;
    private int maxUsernameLength;

    public CheckUsernameController()
    {
        super();
    }

    @Override
    public void init()
    {
        userDao = new UserDao();
        maxUsernameLength = Integer.parseInt(getServletContext().getInitParameter("maxUsernameLength"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            String username = Objects.toString(req.getParameter("username"), "");

            if(username.trim().isEmpty() || username.length() > maxUsernameLength)
            {
                resp.sendError(400, "Invalid username!");
                return;
            }

            PrintWriter pw = resp.getWriter();

            if(!userDao.usernameExists(username)) pw.print("valid");
            else pw.print("invalid");

            resp.setStatus(200);
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getLocalizedMessage());
        }
    }
}
