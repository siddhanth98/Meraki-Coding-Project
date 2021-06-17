package com.meraki.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Represents a container for JSON properties contained in a device request
 * @author Siddhanth Venkateshwaran
 */
@JsonDeserialize(using = DeviceRequestDeserializer.class)
public class DeviceRequest {

    private Long deviceId;
    private Long timestamp;
    private Integer value;

    public DeviceRequest() {

    }

    @JsonCreator
    public DeviceRequest(@JsonProperty("did") Long deviceId,
                         @JsonProperty("ts") Long timestamp,
                         @JsonProperty("value") Integer value) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.value = value;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Integer getValue() {
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

    public boolean hasDeviceId() {
        return deviceId != null;
    }

    public boolean hasTimestamp() {
        return timestamp != null;
    }

    public boolean hasValue() {
        return value != null;
    }

    @Override
    public String toString() {
        return String.format("did = %d, value = %d, ts = %d", deviceId, value, timestamp);
    }
}
