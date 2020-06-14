package it.polimi.gd.beans;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Directory implements Bean
{
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int id;
    private String name;
    private Date creationDate;
    private int parentId;
    private int owner;

    public Directory() {}

    public Directory(int id, String name, Timestamp creationDate, int parentId, int owner)
    {
        this.id = id;
        this.name = name;
        this.creationDate = new Date(creationDate.getTime());
        this.parentId = parentId;
        this.owner = owner;
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

    public int getOwner()
    {
        return owner;
    }

    public void setOwner(int owner)
    {
        this.owner = owner;
    }

    @Override
    public JsonObjectBuilder toJson()
    {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        objectBuilder.add("id", id);
        objectBuilder.add("name", name);
        objectBuilder.add("creationDate", dateFormat.format(creationDate));
        objectBuilder.add("parentId", parentId);
        objectBuilder.add("owner", owner);
        return objectBuilder;
    }
}
