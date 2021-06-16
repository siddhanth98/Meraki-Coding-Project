package com.meraki.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for obtaining stream of device data
 * @author Siddhanth Venkateshwaran
 */
public class Devices implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(Devices.class);

    @Override
    public void handle(HttpExchange exchange) {

    }
}
