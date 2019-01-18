package controllers;

import com.cbsexam.HomeEndpoint;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UIController {

    public UIController() {
    }

    public static String getPage(String htmlFilePath) {
        // Read File and store input
        InputStream input = HomeEndpoint.class.getResourceAsStream(htmlFilePath);
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
        return stringBuffer.toString();
    }

}
