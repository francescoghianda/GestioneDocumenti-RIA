package it.polimi.gd.beans;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class User implements Bean
{
    private int id;
    private String username;

    public User() {}

    public User(int id, String username)
    {
        this.id = id;
        this.username = username;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    @Override
    public JsonObjectBuilder toJson()
    {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        objectBuilder.add("id", id);
        objectBuilder.add("username", username);
        return objectBuilder;
    }
}
