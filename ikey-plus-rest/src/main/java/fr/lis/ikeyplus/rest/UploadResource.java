package fr.lis.ikeyplus.rest;

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.cxf.message.Attachment;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/upload")
public class UploadResource {


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(TEXT_PLAIN)
    public String upload(MultipartBody body) {
        return "works";
    }
}
