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

@WebServlet("/check-email")
public class CheckEmailController extends HttpServlet
{
    private UserDao userDao;

    private int maxEmailLength;

    public CheckEmailController()
    {
        super();
    }

    @Override
    public void init()
    {
        userDao = new UserDao();
        maxEmailLength = Integer.parseInt(getServletContext().getInitParameter("maxEmailLength"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            String email = Objects.toString(req.getParameter("email"), "");
            PrintWriter pw = resp.getWriter();
            resp.setStatus(200);

            if(email.trim().isEmpty() || email.length() > maxEmailLength || !RegisterController.emailPattern.matcher(email).matches())
            {
                pw.print("invalid");
                return;
            }

            if(userDao.emailExists(email))
            {
                pw.print("unavailable");
                return;
            }

            pw.print("ok");
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getLocalizedMessage());
        }
    }
}
