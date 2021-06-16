package com.meraki.service;

/**
 * This class is responsible computes and updates the minimum,
 * maximum and average values of the given device using the data obtained
 * from the database
 * @author Siddhanth Venkateshwaran
 */
public class Processor {

    /**
     * This function computes the valid timestamp-start for the given
     * device timestamp and either creates a new record or updates an
     * existing record for the device at timestamp-start
     * @param deviceId ID of device sending the data
     * @param value Device data
     * @param timestamp Time at which device sent it's data
     */
    public void process(long deviceId, int value, long timestamp) {

    }

    /**
     * This function gets the start time point of the current minute interval
     * to which the given timestamp belongs
     * @param timestamp Input timestamp sent by a device
     * @param start Start timestamp of the first device which contacted the web server
     * @return Start time point of input timestamp based on 1-minute increments
     */
    public long getTimestampStart(long timestamp, long start) {
        long timestampStart = 0;
        for (long i = timestamp; i >= timestamp-60; i--) {
            if ((i-start) % 60 == 0) {
                timestampStart = i;
                break;
            }
        }
        return timestampStart;
    }
}
