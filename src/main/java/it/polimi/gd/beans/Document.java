package it.polimi.gd.beans;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class Document implements Bean
{
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int id;
    private String name;
    private Date creationDate;
    private String summary;
    private String type;
    private int parentId;
    private int owner;

    public Document() {}

    public Document(int id, String name, Date creationDate, String summary, String type, int parentId, int owner)
    {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.summary = summary;
        this.type = type;
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

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
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
        objectBuilder.add("summary", summary == null ? "" : summary);
        objectBuilder.add("type", type);
        objectBuilder.add("parentId", parentId);
        objectBuilder.add("owner", owner);
        return objectBuilder;
    }
}
