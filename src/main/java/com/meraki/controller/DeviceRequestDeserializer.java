package com.meraki.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class DeviceRequestDeserializer extends StdDeserializer<DeviceRequest> {

    public DeviceRequestDeserializer() {
        this(null);
    }

    public DeviceRequestDeserializer(Class<?> c) {
        super(c);
    }

    @Override
    public DeviceRequest deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        JsonNode targetNode;
        Long deviceId = null;
        Long timestamp = null;
        Integer value = null;

        targetNode = getValid(node, "did", "deviceId");
        if (targetNode != null)
            deviceId = targetNode.longValue();

        targetNode = getValid(node, "ts", "timestamp");
        if (targetNode != null)
            timestamp = targetNode.longValue();

        targetNode = getValid(node, "val", "value");
        if (targetNode != null)
            value = targetNode.intValue();

        System.out.printf("did=%d, value=%d, ts=%d%n", deviceId, value, timestamp);
        return new DeviceRequest(deviceId, timestamp, value);
    }

    public JsonNode getValid(JsonNode node, String props1, String props2) {
        JsonNode result = null;
        if (node.has(props1) && !node.get(props1).isNull())
            result = node.get(props1);
        else if (node.has(props2) && !node.get(props2).isNull())
            result = node.get(props2);
        return result;
    }
}
