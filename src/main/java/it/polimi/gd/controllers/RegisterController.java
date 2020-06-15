package it.polimi.gd.controllers;

import it.polimi.gd.Application;
import it.polimi.gd.dao.UserDao;
import org.thymeleaf.context.WebContext;
import sun.jvm.hotspot.jdi.IntegerTypeImpl;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.regex.Pattern;

@WebServlet("/register")
@MultipartConfig
public class RegisterController extends HttpServlet
{
    public static final Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private UserDao userDao;
    private int maxUsernameLength;
    private int minPasswordLength;
    private int maxEmailLength;

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
        maxEmailLength = Integer.parseInt(getServletContext().getInitParameter("maxEmailLength"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        WebContext webContext = new WebContext(req, resp, getServletContext(), resp.getLocale());
        webContext.setVariable("maxUsernameLen", maxUsernameLength);
        webContext.setVariable("maxEmailLen", maxEmailLength);
        webContext.setVariable("minPasswordLen", minPasswordLength);
        webContext.setVariable("version", Application.getVersion());
        Application.getTemplateEngine().process("register", webContext, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            String username = Objects.toString(req.getParameter("username"), "");
            String email = Objects.toString(req.getParameter("email"), "");
            String password = Objects.toString(req.getParameter("password"), "");
            String passwordConfirm = Objects.toString(req.getParameter("password-confirm"), "");

            if(username.trim().isEmpty() || username.length() > maxUsernameLength)
            {
                resp.sendError(400, "Invalid username!");
                return;
            }

            if(email.trim().isEmpty() || email.length() > maxEmailLength || !emailPattern.matcher(email).matches())
            {
                resp.sendError(400, "Invalid email!");
                return;
            }

            if(password.length() < minPasswordLength || !password.equals(passwordConfirm))
            {
                resp.sendError(400, "Invalid password!");
                return;
            }

            if(!userDao.createUser(username, email, password))
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
