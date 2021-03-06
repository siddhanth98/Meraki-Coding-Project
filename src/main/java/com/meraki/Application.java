package com.meraki;

import com.meraki.controller.DeviceStatsHandler;
import com.meraki.controller.DeviceUpdateHandler;
import com.meraki.controller.RootHandler;
import com.meraki.controller.TerminationHandler;
import com.meraki.service.Processor;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

import static com.meraki.service.Processor.connectDatabase;

/**
 * Main class which starts and stops the server instance and specifies the route handlers
 * @author Siddhanth Venkateshwaran
 */
public class Application {
    private final static Logger logger = LoggerFactory.getLogger(Application.class);
    private static HttpServer server;

    public static void main(String[] args) {
        start();
    }

    public static void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(Constants.PORT), 0);
            logger.info(String.format("Server started at port %d%n", server.getAddress().getPort()));
            connectDatabase();

            server.createContext("/", new RootHandler());
            server.createContext("/devices", new DeviceUpdateHandler());
            server.createContext("/deviceStats", new DeviceStatsHandler());
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

    public static void stop() {
        Processor.closeDatabaseConnection();
        logger.info(String.format("Stopping server running at port %d!", server.getAddress().getPort()));
        server.stop(0);
    }
}
