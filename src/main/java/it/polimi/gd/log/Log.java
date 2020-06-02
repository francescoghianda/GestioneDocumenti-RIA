package it.polimi.gd.log;

public class Log
{
    private static LogLevel level = LogLevel.INFO;

    public static void setLevel(LogLevel level)
    {
        Log.level = level;
    }

    private static void log(String message, LogLevel level)
    {
        System.out.print("["+level.toString()+"] ");
        System.out.println(message);
    }

    public static void info(String message)
    {
        if(level.ordinal() <= LogLevel.INFO.ordinal()) log(message, LogLevel.INFO);
    }

    public static void warning(String message)
    {
        if(level.ordinal() <= LogLevel.WARNING.ordinal()) log(message, LogLevel.WARNING);
    }

    public static void error(String message)
    {
        if(level.ordinal() <= LogLevel.ERROR.ordinal()) log(message, LogLevel.ERROR);
    }
}
