package com.meraki.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meraki.service.Processor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for the route where device stats is queried by a client
 * @author Siddhanth Venkateshwaran
 */
public class DeviceStatsHandler implements HttpHandler {

    private final static Logger logger = LoggerFactory.getLogger(DeviceStatsHandler.class);

    @Override
    public void handle(HttpExchange he) {
        try {
            InputStreamReader isr = new InputStreamReader(he.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine(), response ;
            int responseCode = 200;

            OutputStream os = he.getResponseBody();
            Map<String, Object> params = QueryParser.parse(query);
            Map<String, Object> stats = new HashMap<>();
            if (params.containsKey("did") && params.containsKey("ts"))
                stats = Processor.getDeviceStats((long)params.get("did"), (long)params.get("ts"));

            if (stats.isEmpty()) {
                responseCode = 404;
                response = "Stats not found!";
            }
            else
                response = getJsonResponse(stats);
            he.sendResponseHeaders(responseCode, response.length());
            os.write(response.getBytes());
            os.close();
        }
        catch(JsonProcessingException ex) {
            logger.error(String.format("ERROR while serializing device stats - %s%n", ex.getMessage()));
            ex.printStackTrace();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getJsonResponse(Map<String, Object> record) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(
                new DeviceStats(
                        (int)record.get("minimum"),
                        (int)record.get("maximum"),
                        (float)record.get("average")
                )
        );
    }
}
