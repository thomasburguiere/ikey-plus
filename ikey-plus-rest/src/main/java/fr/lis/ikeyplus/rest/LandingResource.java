package fr.lis.ikeyplus.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class Landing {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public IkeyInfo status(){
        return new IkeyInfo("2.0-SNAPSHOT");
    }
}
