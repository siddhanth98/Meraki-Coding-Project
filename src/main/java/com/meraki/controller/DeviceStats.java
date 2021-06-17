package com.meraki.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a container for JSON properties contained in a device request
 * @author Siddhanth Venkateshwaran
 */
public class DeviceStats {
    private long deviceId, timestamp;
    private int value;

    @JsonCreator
    public DeviceStats(@JsonProperty("did") long deviceId,
                       @JsonProperty("ts") long timestamp,
                       @JsonProperty("value") int value) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getValue() {
        return value;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
