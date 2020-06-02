package it.polimi.utils.file;

import it.polimi.gd.exceptions.ApplicationInitializationException;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FileManager
{
    private static FileManager instance;

    private final File rootDirectory;
    private final String rootDirectoryPath;

    private FileManager(ServletContext context)
    {
        rootDirectoryPath = context.getRealPath("/")+File.separator+"files";
        rootDirectory = new File(rootDirectoryPath);

        if(!rootDirectory.exists() && !rootDirectory.mkdir())
            throw new ApplicationInitializationException(ApplicationInitializationException.Cause.ROOT_DIR_CREATION_ERROR);
        if(!rootDirectory.isDirectory())
            throw new ApplicationInitializationException(ApplicationInitializationException.Cause.ROOT_DIR_IS_NOT_A_DIRECTORY);
    }

    public static FileManager getInstance(ServletContext context)
    {
        if(instance == null) instance = new FileManager(context);
        return instance;
    }

    public FileInputStream getFileInputStream(int documentId) throws FileNotFoundException
    {
        File documentFile = new File(rootDirectoryPath+File.separator+documentId);
        return new FileInputStream(documentFile);
    }
}
