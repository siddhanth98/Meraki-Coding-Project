package com.meraki.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handler for obtaining stream of device data
 * @author Siddhanth Venkateshwaran
 */
public class Devices implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(Devices.class);

    @Override
    public void handle(HttpExchange he) {

    }

    public void parse(String query, Map<String, Object> params) throws UnsupportedEncodingException {
        if (query != null) {
            String[] pairs = query.split("[&]");
            for (String pair : pairs) {
                String[] p = pair.split("[=]");
                String key = null, value = null;
                if (p.length > 0) {
                    key = URLDecoder.decode(p[0], System.getProperty("file.encoding"));
                }
                if (p.length > 1) {
                    value = URLDecoder.decode(p[1], System.getProperty("file.encoding"));
                }
                if (params.containsKey(key)) {
                    Object obj = params.get(key);
                    List<String> values = new ArrayList<>();
                    values.add((String)obj);
                    values.add(value);
                    params.put(key, values);
                }
                else params.put(key, value);
            }
        }
    }
}
