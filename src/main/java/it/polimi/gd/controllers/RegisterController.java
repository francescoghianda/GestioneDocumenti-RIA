package it.polimi.gd.controllers;

import it.polimi.gd.Application;
import it.polimi.gd.dao.UserDao;
import org.thymeleaf.context.WebContext;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

@WebServlet("/register")
public class RegisterController extends HttpServlet
{
    private UserDao userDao;
    private int maxUsernameLength;
    private int minPasswordLength;

    public RegisterController()
    {
        super();
    }

    @Override
    public void init()
    {
        userDao = new UserDao();
        maxUsernameLength = Integer.parseInt(getServletContext().getInitParameter("maxUsernameLength"));
        minPasswordLength = Integer.parseInt(getServletContext().getInitParameter("minPasswordLength"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        WebContext webContext = new WebContext(req, resp, getServletContext(), resp.getLocale());
        webContext.setVariable("maxUsernameLen", maxUsernameLength);
        webContext.setVariable("minPasswordLen", minPasswordLength);
        Application.getTemplateEngine().process("register", webContext, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            String username = Objects.toString(req.getParameter("username"), "");
            String password = Objects.toString(req.getParameter("password"), "");
            String passwordConfirm = Objects.toString(req.getParameter("password-confirm"), "");

            if(username.trim().isEmpty() || username.length() > maxUsernameLength)
            {
                resp.sendError(400, "Invalid username!");
                return;
            }

            if(password.length() < minPasswordLength || !password.equals(passwordConfirm))
            {
                resp.sendError(400, "Invalid password!");
                return;
            }

            if(!userDao.createUser(username, password))
            {
                resp.sendError(500, "Error! Username not created!");
                return;
            }

            resp.sendRedirect("/");
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getLocalizedMessage());
        }
    }
}
