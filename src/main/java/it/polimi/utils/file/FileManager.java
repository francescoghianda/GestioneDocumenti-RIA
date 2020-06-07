package it.polimi.utils.file;

import it.polimi.gd.exceptions.ApplicationInitializationException;

import javax.servlet.ServletContext;
import java.io.*;
import java.nio.file.Files;

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

    public FileInputStream getFileInputStream(int documentId, int owner) throws FileNotFoundException
    {
        File documentFile = new File(rootDirectoryPath+File.separator+getUserFolderName(owner)+File.separator+documentId);
        return new FileInputStream(documentFile);
    }

    public FileOutputStream getFileOutputStream(int documentId, int owner) throws IOException
    {
        File documentFile = new File(rootDirectoryPath+File.separator+getUserFolderName(owner)+File.separator+documentId);
        if(!documentFile.getParentFile().exists() && !documentFile.getParentFile().mkdir())return null;
        if(!documentFile.createNewFile())return null;
        return new FileOutputStream(documentFile);
    }

    public boolean deleteFile(int documentId, int owner)
    {
        File documentFile = new File(rootDirectoryPath+File.separator+getUserFolderName(owner)+File.separator+documentId);
        return documentFile.exists() && documentFile.delete();
    }

    public boolean fileExists(int documentId, int owner)
    {
        File documentFile = new File(rootDirectoryPath+File.separator+getUserFolderName(owner)+File.separator+documentId);
        return documentFile.exists() && !documentFile.isDirectory();
    }

    private String getUserFolderName(int userId)
    {
        return "user-"+userId;
    }
}
