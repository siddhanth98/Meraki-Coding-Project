package com.meraki;

import com.meraki.controller.DeviceHandler;
import com.meraki.controller.RootHandler;
import com.meraki.controller.TerminationHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

import static com.meraki.service.Processor.connectDatabase;

public class Application {
    private final static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(Constants.PORT), 0);
            logger.info(String.format("Server started at port %d%n", server.getAddress().getPort()));
            connectDatabase();

            server.createContext("/", new RootHandler());
            server.createContext("/devices", new DeviceHandler());
            server.createContext("/termination", new TerminationHandler());

            server.setExecutor(null);
            server.start();
        }
        catch(IOException io) {
            logger.error(String.format("IO Error - %s%n", io.getMessage()));
            io.printStackTrace();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
