package it.polimi.gd;
import it.polimi.gd.exceptions.ApplicationInitializationException;
import it.polimi.gd.log.Log;
import it.polimi.utils.file.FileManager;
import it.polimi.utils.sql.ConnectionPool;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;

public class Application
{
    private static FileManager fileManager;

    private static String templatesPath = "/WEB-INF/templates/";
    private static TemplateEngine templateEngine;
    private static ServletContextTemplateResolver templateResolver;

    private Application() {}

    public static void init(ServletContext context)
    {
        Log.info("Init application...");

        fileManager = FileManager.getInstance(context);

        templateResolver = new ServletContextTemplateResolver(context);
        templateResolver.setCacheable(false);
        templateResolver.setPrefix(templatesPath);
        templateResolver.setSuffix(".html");
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        try
        {
            ConnectionPool.init(context);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            throw new ApplicationInitializationException(ApplicationInitializationException.Cause.JDBC_NOT_FOUND);
        }

        Log.info("Application initialized.");
    }

    public static void close()
    {
        ConnectionPool.getInstance().close();
    }

    public static FileManager getFileManager()
    {
        return fileManager;
    }

    public static TemplateEngine getTemplateEngine()
    {
        return templateEngine;
    }

}
