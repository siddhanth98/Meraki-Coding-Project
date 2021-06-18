package com.meraki.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to implement parse URLs / deserialize JSON data from a request
 * @author Siddhanth Venkateshwaran
 */
public class QueryParser {

    private final static Logger logger = LoggerFactory.getLogger(QueryParser.class);

    /**
     * Parses the given request query and returns a map of properties
     * either from a JSON content or query parameters
     * @param query Request query string
     */
    public static Map<String, Object> parse(String query)
            throws UnsupportedEncodingException, JsonProcessingException {
        if (query != null) {
            if (query.contains("&") && query.contains("="))
                return parseURL(query);
            return parseJsonRequest(query);
        }
        return new HashMap<>();
    }

    /**
     * Parses the query and extracts the values from the query parameters
     * @param query Request query
     * @return Map of properties of query parameters
     */
    public static Map<String, Object> parseURL(String query) throws UnsupportedEncodingException {
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
    public static Map<String, Object> parseJsonRequest(String query) throws JsonProcessingException {
        Map<String, Object> parameters = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        DeviceRequest request = mapper.readValue(query, DeviceRequest.class);

        if (request.hasDeviceId()) {
            parameters.put("did", request.getDeviceId());
        }
        if (request.hasValue()) {
            parameters.put("value", request.getValue());
        }
        if (request.hasTimestamp()) {
            parameters.put("ts", request.getTimestamp());
        }
        logger.info(String.format("Deserialized query - %s", parameters.toString()));
        return parameters;
    }
}
