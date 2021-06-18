package com.meraki.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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
            logger.info(String.format("Received update request - %s%n", query));

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
}
