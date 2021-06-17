package com.meraki.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.meraki.service.Processor.*;

/**
 * Handler for obtaining stream of device data
 * @author Siddhanth Venkateshwaran
 */
public class DeviceUpdateHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeviceUpdateHandler.class);

    /**
     * Handler for each device request
     * @param he HTTP flow object having the request and response structures
     */
    @Override
    public void handle(HttpExchange he) {
        try {
            InputStreamReader isr = new InputStreamReader(he.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine(), response;
            int responseCode;
            OutputStream os = he.getResponseBody();

            logger.info(String.format("Received query - %s%n", query));

            Map<String, Object> parameters = QueryParser.parse(query);

            if (!(parameters.containsKey("did") && parameters.containsKey("value") && parameters.containsKey("ts"))) {
                response = "Invalid request to process!";
                responseCode = 422;
            }
            else {
                long did = Long.parseLong(String.valueOf(parameters.get("did"))),
                        ts = Long.parseLong(String.valueOf(parameters.get("ts")));
                int value = Integer.parseInt(String.valueOf(parameters.get("value")));
                Map<String, Object> record = process(did, value, ts);

                if (record.isEmpty())
                    response = String.format("Processed device %d, Updated result => (did=%d, min=%d, max=%d, avg=%f)",
                            did, did, value, value, (float) value);
                else
                    response = String.format("Processed device %d, Updated result => (did=%d, min=%d, max=%d, avg=%f)",
                            did, did, (int) record.get("min"), (int) record.get("max"), (float) record.get("avg"));
                responseCode = 200;
            }
            he.sendResponseHeaders(responseCode, response.length());
            os.write(response.getBytes());
            os.close();
        }
        catch(UnsupportedEncodingException ex) {
            logger.error(String.format("Error while parsing URL parameters - %s%n", ex.getMessage()));
            ex.printStackTrace();
        }
        catch(JsonProcessingException ex) {
            logger.error(String.format("Error while processing JSON request body - %s%n", ex.getMessage()));
            ex.printStackTrace();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Parse the given request query and return a map of properties
     * either from a JSON content or query parameters
     */
    public Map<String, Object> parse(String query)
            throws UnsupportedEncodingException, JsonProcessingException {
        if (query != null) {
            if (query.contains("&") && query.contains("="))
                return QueryParser.parseURL(query);
            return QueryParser.parseJsonRequest(query);
        }
        return new HashMap<>();
    }

    /**
     * Parses the query and extracts the values from the query parameters
     * @param query Request query
     * @return Map of properties of query parameters
     */
    public Map<String, Object> parseURL(String query) throws UnsupportedEncodingException {
        Map<String, Object> parameters = new HashMap<>();
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
            if (parameters.containsKey(key)) {
                Object obj = parameters.get(key);
                List<String> values = new ArrayList<>();
                values.add((String)obj);
                values.add(value);
                parameters.put(key, values);
            }
            else parameters.put(key, value);
        }
        return parameters;
    }

    /**
     * De-serializes a JSON string query and returns a map of those JSON properties
     * @param query Request query
     * @return Map of properties from JSON request
     */
    public Map<String, Object> parseJsonRequest(String query) throws JsonProcessingException {
        Map<String, Object> parameters = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        DeviceRequest stats = mapper.readValue(query, DeviceRequest.class);
        parameters.put("did", stats.getDeviceId());
        parameters.put("value", stats.getValue());
        parameters.put("ts", stats.getTimestamp());
        return parameters;
    }
}
