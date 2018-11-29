package com.cbsexam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Path("/")
public class HomeEndpoint {

    @GET
    @Path("/")
    public Response getLoginPage() {

        // Read File and store input
        InputStream input = HomeEndpoint.class.getResourceAsStream("/LoginPage.html");
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        // Go through the lines one by one
        StringBuffer stringBuffer = new StringBuffer();
        String str;

        // Read file one line at a time
        try {
            while ((str = reader.readLine()) != null) {
                stringBuffer.append(str);
            }
        }catch (Exception e){
            System.out.println(e);
        }

        String htmlString = stringBuffer.toString();


        return Response.status(200).type(MediaType.TEXT_HTML_TYPE).entity(htmlString).build();
    }

}
