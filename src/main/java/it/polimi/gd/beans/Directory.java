package it.polimi.gd.beans;

import java.sql.Date;

public class Directory
{
    private int id;
    private String name;
    private Date creationDate;
    private int parentId;

    public Directory() {}

    public Directory(int id, String name, Date creationDate, int parentId)
    {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.parentId = parentId;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    public int getParentId()
    {
        return parentId;
    }

    public void setParentId(int parentId)
    {
        this.parentId = parentId;
    }
}
