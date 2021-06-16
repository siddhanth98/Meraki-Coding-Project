package com.meraki.service;

import static com.meraki.Constants.*;
import com.meraki.dao.DeviceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible computes and updates the minimum,
 * maximum and average values of the given device using the data obtained
 * from the database
 * @author Siddhanth Venkateshwaran
 */
public class Processor {
    private static long initialTimestamp = -1;
    private static DeviceData db;
    private static final Logger logger = LoggerFactory.getLogger(Processor.class);

    public static void connectDatabase() throws Exception {
        db = new DeviceData();
        db.createDeviceDataTable(deviceTable);
    }

    /**
     * This function computes the valid timestamp-start for the given
     * device timestamp and either creates a new record or updates an
     * existing record for the device at timestamp-start
     * @param deviceId ID of device sending the data
     * @param value Device data
     * @param timestamp Time at which device sent it's data
     */
    public static Map<String, Object> process(long deviceId, int value, long timestamp) {
        Map<String, Object> record;
        Map<String, Object> deviceRecord = new HashMap<>();

        try {
            long tsStart = getTimestampStart(timestamp, initialTimestamp == -1 ? timestamp : initialTimestamp);
            logger.info(String.format("Processing device-id %d, timestamp = %d%n", deviceId, tsStart));
            record = db.getDeviceRecord(deviceId, tsStart);
            if (record.isEmpty())
                db.insertNewDeviceRecord(deviceId, tsStart, value, value, value, 1, value);
            else {
                int min = Integer.min(value, (int)record.get("minimum"));
                int max = Integer.max(value, (int)record.get("maximum"));
                long total = (long)record.get("total")+value;
                int count = (int)record.get("count")+1;
                float average = ((float)total / (float)count);
                db.updateDeviceRecord(deviceId, tsStart, min, max, total, count, average);
                deviceRecord = Map.of("did", deviceId, "min", min, "max", max, "avg", average);
            }
            if (initialTimestamp == -1)
                initialTimestamp = tsStart;
        }
        catch(SQLException ex) {
            logger.error(String.format("Database error - %d", ex.getErrorCode()));
            ex.printStackTrace();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return deviceRecord;
    }

    /**
     * This function gets the start time point of the current minute interval
     * to which the given timestamp belongs
     * @param timestamp Input timestamp sent by a device
     * @param start Start timestamp of the first device which contacted the web server
     * @return Start time point of input timestamp based on 1-minute increments
     */
    public static long getTimestampStart(long timestamp, long start) {
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
