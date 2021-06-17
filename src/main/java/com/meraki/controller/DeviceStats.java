package com.meraki.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceStats {
    private final int min, max;
    private final float avg;

    @JsonCreator
    public DeviceStats(@JsonProperty("min") int min,
                       @JsonProperty("max") int max,
                       @JsonProperty("avg") float avg) {
        this.min = min;
        this.max = max;
        this.avg = avg;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public float getAvg() {
        return avg;
    }
}
