package it.polimi.gd.beans;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class User implements Bean
{
    private int id;
    private String username;
    private String email;

    public User() {}

    public User(int id, String username, String email)
    {
        this.id = id;
        this.username = username;
        this.email = email;
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

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getEmail()
    {
        return email;
    }

    @Override
    public JsonObjectBuilder toJson()
    {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        objectBuilder.add("id", id);
        objectBuilder.add("username", username);
        objectBuilder.add("email", email);
        return objectBuilder;
    }
}
