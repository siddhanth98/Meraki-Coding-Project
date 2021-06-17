package com.meraki;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meraki.controller.DeviceRequest;
import com.meraki.controller.DeviceStats;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpResponse;

import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Implements tests for stream processing service
 * @author Siddhanth Venkateshwaran
 */
public class DeviceAPITest {
    private final static Config config = ConfigFactory.parseFile(new File("src/main/resources/application.conf"));
    private final static Logger logger = LoggerFactory.getLogger(DeviceAPITest.class);

    /**
     * Reads device data from configuration file for testing
     * @param property Required configuration object path
     */
    public List<DeviceRequest> getDeviceStream(String property) {
        List<? extends ConfigObject> requestsList = config.getObjectList(property);
        List<DeviceRequest> deviceStream = new ArrayList<>();
        requestsList.stream().map(ConfigObject::toConfig).forEach(o -> {
            DeviceRequest deviceRequest = new DeviceRequest();
            try {
                if (o.hasPath("did")) {
                    deviceRequest.setDeviceId(Long.parseLong(o.getString("did")));
                }
                if (o.hasPath("ts")) {
                    deviceRequest.setTimestamp(Long.parseLong(o.getString("ts")));
                }
                if (o.hasPath("value")) {
                    deviceRequest.setValue(Integer.parseInt(o.getString("value")));
                }
            }
            catch(NumberFormatException ex) {
                logger.warn("One or more of the request fields is malformed!");
            }
            deviceStream.add(deviceRequest);
        });
        return deviceStream;
    }

    @BeforeClass
    public static void initiate() {
        Application.start();
    }

    @AfterClass
    public static void stop() {
        Application.stop();
    }

    /**
     * Tests whether service responds correctly during startup
     */
    @Test
    public void serviceShouldStart() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:8000/");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        assertEquals(200, httpResponse.getCode());
    }

    /**
     * Tests whether all device update requests sent are processed
     * without any errors by the service
     */
    @Test
    public void serviceShouldObtainAllDeviceUpdates() throws IOException {
        List<DeviceRequest> deviceStream = getDeviceStream("conf.TEST.VALID_REQUESTS1");
        Set<Integer> responseCodes = requestDeviceUpdates(deviceStream);
        responseCodes.add(200);

        logger.info(String.format("Response codes across all device update requests => %s", responseCodes.toString()));
        assertEquals(1, responseCodes.size());
    }

    /**
     * Tests whether service sends the proper response for
     * an invalid device update request (malformed request parameters/missing values)
     */
    @Test
    public void serviceShouldNotProcessInvalidRequests() throws IOException {
        List<DeviceRequest> deviceStream = getDeviceStream("conf.TEST.INVALID_REQUESTS");
        Set<Integer> responseCodes = requestDeviceUpdates(deviceStream);
        responseCodes.add(200);

        logger.info(String.format("Response codes across all device update requests => %s", responseCodes.toString()));
        assertNotEquals(1, responseCodes.size());
    }

    /**
     * Tests whether service returns the most updated stats for
     * a given device at the desired timestamp
     */
    @Test
    public void serviceShouldReturnUpdatedDeviceStats() throws IOException, Exception {
        List<DeviceRequest> deviceStream = getDeviceStream("conf.TEST.VALID_REQUESTS2");
        requestDeviceUpdates(deviceStream);
        Map<String, Object> stats = requestDeviceStats();
        assertTrue((int)stats.get("min") == 1 && (int)stats.get("max") == 2 && (float)stats.get("avg") == 1.5F);
    }

    public Map<String, Object> requestDeviceStats() throws IOException, Exception {
        Map<String, Object> deviceStatsMap;
        ObjectMapper mapper = new ObjectMapper();
        List<DeviceRequest> deviceStats = getDeviceStream("conf.TEST.DEVICE_STATS_REQUEST");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("http://localhost:8000/deviceStats");
            httpPost.setHeader("content-type", "application/json");

            httpPost.setEntity(new StringEntity(mapper.writeValueAsString(deviceStats.get(0))));
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
//                logger.info(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
                deviceStatsMap = deserializeJsonStatsResponse(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            }
        }
        return deviceStatsMap;
    }

    public Set<Integer> requestDeviceUpdates(List<DeviceRequest> deviceStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Set<Integer> responseCodes = new HashSet<>();

        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("http://localhost:8000/devices");
            httpPost.setHeader("content-type", "application/json");

            for (DeviceRequest req : deviceStream) {
                httpPost.setEntity(new StringEntity(mapper.writeValueAsString(req)));
                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    responseCodes.add(response.getCode());
                }
            }
        }
        return responseCodes;
    }

    public Map<String, Object> deserializeJsonStatsResponse(String response) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        DeviceStats stats = mapper.readValue(response, DeviceStats.class);
        return Map.of("min", stats.getMin(), "max", stats.getMax(), "avg", stats.getAvg());
    }
}
