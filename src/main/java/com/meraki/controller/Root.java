package com.meraki.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;

/**
 * Root route handler for the web server
 * @author Siddhanth Venkateshwaran
 */
public class Root implements HttpHandler {

    @Override
    public void handle(HttpExchange he) {
        try {
            String response = "<h1>Stream processor started!</h1>";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
