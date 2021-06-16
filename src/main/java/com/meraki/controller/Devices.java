package com.meraki.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Devices implements HttpHandler {

    /**
     * Represents a container for JSON properties contained in a device request
     * @author Siddhanth Venkateshwaran
     */
    static class Request {
        private long did, timestamp;
        private int value;

        @JsonCreator
        public Request(@JsonProperty("did") long did,
                       @JsonProperty("value") int value,
                       @JsonProperty("ts") long ts) {
            this.did = did;
            this.value = value;
            this.timestamp = ts;
        }

        public long getDid() {
            return did;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public int getValue() {
            return value;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Devices.class);

    @Override
    public void handle(HttpExchange he) {
        try {
            InputStreamReader isr = new InputStreamReader(he.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine(), response;
            OutputStream os = he.getResponseBody();

            logger.info(String.format("Received query - %s%n", query));

            Map<String, Object> parameters = parse(query);

            if (!(parameters.containsKey("did") && parameters.containsKey("value") && parameters.containsKey("ts")))
                response = "Please send valid request!";
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
            }
            he.sendResponseHeaders(200, response.length());
            os.write(response.getBytes());
            os.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public Map<String, Object> parse(String query)
            throws UnsupportedEncodingException, JsonProcessingException {
        if (query.contains("&") && query.contains("="))
            return parseURL(query);
        return parseJsonRequest(query);
    }

    public Map<String, Object> parseURL(String query) throws UnsupportedEncodingException {
        Map<String, Object> parameters = new HashMap<>();
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
                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    List<String> values = new ArrayList<>();
                    values.add((String)obj);
                    values.add(value);
                    parameters.put(key, values);
                }
                else parameters.put(key, value);
            }
        }
        return parameters;
    }

    public Map<String, Object> parseJsonRequest(String query) throws JsonProcessingException {
        Map<String, Object> parameters = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        Request request = mapper.readValue(query, Request.class);
        parameters.put("did", request.getDid());
        parameters.put("value", request.getValue());
        parameters.put("ts", request.getTimestamp());
        return parameters;
    }
}
