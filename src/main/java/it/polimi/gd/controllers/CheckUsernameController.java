package it.polimi.gd.controllers;

import it.polimi.gd.dao.UserDao;

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
            PrintWriter pw = resp.getWriter();
            resp.setStatus(200);

            if(username.trim().isEmpty() || username.length() > maxUsernameLength)
            {
                pw.print("invalid");
                return;
            }

            if(userDao.usernameExists(username)) pw.print("unavailable");
            else pw.print("ok");
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getLocalizedMessage());
        }
    }
}
