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
        Long deviceId, timestamp;
        Integer value;

        if (node.get("deviceId").isNull())
            deviceId = null;
        else
            deviceId = node.get("deviceId").longValue();

        if (node.get("value").isNull())
            value = null;
        else
            value = (node.get("value")).intValue();

        if (node.get("timestamp").isNull())
            timestamp = null;
        else
            timestamp = (node.get("timestamp")).longValue();
        return new DeviceRequest(deviceId, timestamp, value);
    }
}
