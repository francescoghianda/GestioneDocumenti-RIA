package it.polimi.gd.listeners;

import it.polimi.gd.Application;
import it.polimi.gd.exceptions.ApplicationInitializationException;
import it.polimi.gd.log.Log;
import it.polimi.utils.hash.PasswordHashGenerator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

@WebListener
public class ContextListener implements ServletContextListener
{

    @Override
    public void contextInitialized(ServletContextEvent e)
    {
        try
        {
            Application.init(e.getServletContext());
        }
        catch (ApplicationInitializationException exception)
        {
            Log.error(exception.getMessage());
            throw exception;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event)
    {
        Application.close();
    }
}
