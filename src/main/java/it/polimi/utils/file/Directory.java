package it.polimi.utils.file;

import it.polimi.gd.beans.DirectoryMetadata;

import java.io.File;

public class Directory
{
    private final File directoryFile;
    private final int level;
    private DirectoryMetadata metadata;

    protected Directory(File directory, int level)
    {
        if(!directory.exists()) throw new IllegalArgumentException(directory.getName()+" not exists!");
        if(!directory.isDirectory()) throw new IllegalArgumentException(directory.getName()+" is not a directory!");
        this.directoryFile = directory;
        this.level = level;
    }

    /*public List<Directory> getSubdirectories()
    {
        File[] directories = directoryFile.listFiles(File::isDirectory);
        return directories == null ? new ArrayList<>() : Arrays.stream(directories).map(file -> new Directory(file, level+1)).collect(Collectors.toList());
    }

    public List<Document> getDocuments()
    {
        File[] documents = directoryFile.listFiles(file -> !file.isDirectory());
        return documents == null ? new ArrayList<>() : Arrays.stream(documents).map(Document::new).collect(Collectors.toList());
    }*/

    public void setMetadata(DirectoryMetadata metadata)
    {
        this.metadata = metadata;
    }

    public DirectoryMetadata getMetadata()
    {
        return metadata;
    }
}
