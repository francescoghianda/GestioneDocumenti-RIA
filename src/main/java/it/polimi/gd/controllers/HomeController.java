package it.polimi.gd.controllers;

import it.polimi.gd.Application;
import it.polimi.gd.beans.User;
import org.thymeleaf.context.WebContext;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("")
public class HomeController extends HttpServlet
{
    private int maxDocumentNameLength;
    private int maxDocumentSummaryLength;
    private int maxDirectoryNameLength;

    public HomeController()
    {
        super();
    }

    @Override
    public void init()
    {
        maxDocumentNameLength = Integer.parseInt(getServletContext().getInitParameter("maxDocumentNameLength"));
        maxDocumentSummaryLength = Integer.parseInt(getServletContext().getInitParameter("maxDocumentSummaryLength"));
        maxDirectoryNameLength = Integer.parseInt(getServletContext().getInitParameter("maxDirectoryNameLength"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        WebContext webContext = new WebContext(req, resp, getServletContext(), req.getLocale());

        User user = (User) req.getSession().getAttribute("user");

        webContext.setVariable("version", Application.getVersion());
        webContext.setVariable("user", user);
        webContext.setVariable("maxDocumentNameLen", maxDocumentNameLength);
        webContext.setVariable("maxDocumentSummaryLen", maxDocumentSummaryLength);
        webContext.setVariable("maxDirectoryNameLen", maxDirectoryNameLength);

        Application.getTemplateEngine().process("home", webContext, resp.getWriter());
    }
}
