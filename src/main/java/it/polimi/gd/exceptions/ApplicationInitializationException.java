package it.polimi.gd.exceptions;

public class ApplicationInitializationException extends RuntimeException
{
    public enum Cause
    {
        ROOT_DIR_CREATION_ERROR("Error during creation of files root folder!"),
        ROOT_DIR_IS_NOT_A_DIRECTORY("File root folder is not a directory!"),
        JDBC_NOT_FOUND("SQL driver not found!");

        Cause(String message)
        {
            this.message = message;
        }

        private final String message;
    }

    public ApplicationInitializationException(Cause cause)
    {
        super(cause.message);
    }
}
