package it.polimi.utils.file;

import it.polimi.gd.beans.DocumentMetadata;

import java.io.File;

public class Document
{
    private final File document;
    private DocumentMetadata metadata;

    protected Document(File document)
    {
        if(!document.exists()) throw new IllegalArgumentException(document.getName()+" not exists!");
        if(document.isDirectory()) throw new IllegalArgumentException(document.getName()+" is not a document!");
        this.document = document;
    }

    public void addMetadata(DocumentMetadata metadata)
    {
        this.metadata = metadata;
    }

    public DocumentMetadata getMetadata()
    {
        return metadata;
    }

}
