package servlets;

import com.google.gson.Gson;
import model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@WebServlet("/test")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        User user = new User(username, password);
        System.out.println(user.getEmail() + user.getPassword());

        //Turn object into JSON
        String json = new Gson().toJson(user);
        System.out.println(json);

        //Make the POST HTTP request
        URL serverUrl = new URL("http://localhost:8080/user/login");
        HttpURLConnection urlConnection = (HttpURLConnection)serverUrl.openConnection();

        // Indicate that we want to write to the HTTP request body
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");

        // Writing the post data to the HTTP request body
        BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
        httpRequestBodyWriter.write(json);
        httpRequestBodyWriter.close();

        // Reading from the HTTP response body
        Scanner httpResponseScanner = new Scanner(urlConnection.getInputStream());
        while(httpResponseScanner.hasNextLine()) {
            System.out.println(httpResponseScanner.nextLine());
        }
        httpResponseScanner.close();

    }
}