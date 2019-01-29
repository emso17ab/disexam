package com.cbsexam;

import controllers.UIController;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class HomeEndpoint {

    @GET
    @Path("/")
    public Response getLoginPage() {

        return Response.status(200).type(MediaType.TEXT_HTML_TYPE).entity(UIController.getPage("/LoginPage.html")).build();
    }

}
