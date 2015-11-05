package fr.lis.ikeyplus.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class Landing {

    @GET
    public String helloWorld(){
        return "it works";
    }
}
