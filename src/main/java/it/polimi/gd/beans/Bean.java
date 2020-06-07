package it.polimi.gd.beans;

import javax.json.JsonObjectBuilder;

public interface Bean
{
    JsonObjectBuilder toJson();
}
