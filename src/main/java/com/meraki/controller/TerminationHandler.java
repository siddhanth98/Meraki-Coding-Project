package com.meraki.controller;

import com.meraki.service.Processor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class TerminationHandler implements HttpHandler {
    private final static Logger logger = LoggerFactory.getLogger(TerminationHandler.class);

    @Override
    public void handle(HttpExchange he) throws IOException {
        OutputStream os = he.getResponseBody();
        String response = "<h1>Stream processor stopped!</h1>";
        he.sendResponseHeaders(200, response.length());
        os.write(response.getBytes());

        os.close();
        Processor.closeDatabaseConnection();
        he.getHttpContext().getServer().stop(0);
        logger.info("Stream processor stopped!");
    }
}
