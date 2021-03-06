package com.meraki.controller;

import com.meraki.Application;
import com.meraki.service.Processor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Handler for the route used to close DB connection and
 * stop the server instance
 * @author Siddhanth Venkateshwaran
 */
public class TerminationHandler implements HttpHandler {
    private final static Logger logger = LoggerFactory.getLogger(TerminationHandler.class);

    @Override
    public void handle(HttpExchange he) throws IOException {
        OutputStream os = he.getResponseBody();
        String response = "<h1>Stream processor stopped!</h1>";
        he.sendResponseHeaders(200, response.length());
        os.write(response.getBytes());

        os.close();
        Application.stop();
    }
}
