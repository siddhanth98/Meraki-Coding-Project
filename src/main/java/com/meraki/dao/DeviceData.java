package com.meraki.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class communicates with the postgresql database
 * to query and update device data.
 * @author Siddhanth Venkateshwaran
 */
public class DeviceData {
    private Connection conn;
    private PreparedStatement select;
    private PreparedStatement update;
    private PreparedStatement insert;

    private static final Logger logger = LoggerFactory.getLogger(DeviceData.class);

    public DeviceData(String url, String user, String pwd) throws Exception {
        this.conn = initializeConnection(url, user, pwd);
    }

    /**
     * Connects to the database and returns a connection object
     * @param url Database server URL
     * @param user Current user of the database
     * @param pwd Password of current user
     */
    private Connection initializeConnection(String url, String user, String pwd) throws Exception {
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pwd);
        return DriverManager.getConnection(url, props);
    }

    /**
     * Checks whether or not a record already exists for the given device
     * @param deviceId ID of current device
     * @param timestamp Start timestamp of device
     * @return True if record already exists and false otherwise
     */
    public boolean hasDeviceRecord(long deviceId, long timestamp) throws Exception {
        select.setLong(1, deviceId);
        select.setLong(2, timestamp);
        ResultSet resultSet = select.executeQuery();
        return resultSet.next();
    }

    /**
     * Accesses and obtains the record for the given device if it exists
     * @param deviceId ID of given device
     * @param timestamp Start timestamp of device
     * @return Empty map if record doesn't exist or else map having
     * min, max and average values for given device
     */
    public Map<String, Object> getDeviceRecord(long deviceId, long timestamp) throws Exception {
        Map<String, Object> record = new HashMap<>();

        select.setLong(1, deviceId);
        select.setLong(2, timestamp);

        ResultSet resultSet = select.executeQuery();
        if (resultSet.next())
            record = Map.of("minimum", Integer.parseInt(resultSet.getString("minimum")),
                    "maximum", Integer.parseInt(resultSet.getString("maximum")),
                    "average", Float.parseFloat(resultSet.getString("average")),
                    "total", Long.parseLong(resultSet.getString("total")),
                    "count", Integer.parseInt(resultSet.getString("deviceCount")));
        return record;
    }

    /**
     * Creates new database with given name
     * @param databaseName Name of new database
     */
    public void createDatabase(String databaseName) throws Exception {
        String createStatement = String.format("CREATE DATABASE %s", databaseName);
        Statement statement = conn.createStatement();
        statement.executeUpdate(createStatement);
        logger.info(String.format("Created database %s%n", databaseName));
        statement.close();
    }

    /**
     * Creates a new device data table and initializes prepared statements
     * for select, insert and update operations on a device
     * @param tableName Name of device data table
     */
    public void createDeviceDataTable(String tableName) throws Exception {
        String createStatement =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                        "did BIGINT, " +
                        "ts BIGINT, " +
                        "minimum INT, " +
                        "maximum INT, " +
                        "total BIGINT, " +
                        "deviceCount INT, " +
                        "average FLOAT, " +
                        "PRIMARY KEY (did, ts));", tableName);
        Statement st = conn.createStatement();
        st.executeUpdate(createStatement);
        logger.info(String.format("Created table %s%n", tableName));
        st.close();

        setUpdateStatement(tableName);
        setSelectStatement(tableName);
        setInsertStatement(tableName);
    }

    /**
     * Inserts a record for a new device with given parameter values
     * @param deviceId ID of new device
     * @param timestamp Start timestamp of new device
     * @param min Minimum value for the device computed by the processor
     * @param max Maximum value for the device computed by the processor
     * @param total Sum of all values seen so far in the current minute for the device
     * @param count Number of times data received by the given device in the current minute
     * @param average Average of values for device in the current minute
     */
    public void insertNewDeviceRecord(long deviceId, long timestamp, int min, int max, long total, int count, float average) throws Exception {
        insert.setLong(1, deviceId);
        insert.setLong(2, timestamp);
        insert.setInt(3, min);
        insert.setInt(4, max);
        insert.setLong(5, total);
        insert.setInt(6, count);
        insert.setFloat(7, average);
        insert.executeUpdate();
    }

    /**
     * Updates existing record for given device with new values
     * @param deviceId ID of device
     * @param timestamp Start timestamp of current minute
     * @param min New minimum value computed for given device in current minute
     * @param max New maximum value computed for given device in current minute
     * @param total Updated sum of values for given device in current minute
     * @param count New count of updated for given device in current minute
     * @param average New average for given device in current minute
     */
    public void updateDeviceRecord(long deviceId, long timestamp, int min, int max, long total, int count, float average) throws Exception {
        update.setInt(1, min);
        update.setInt(2, max);
        update.setLong(3, total);
        update.setInt(4, count);
        update.setFloat(5, average);
        update.setLong(6, deviceId);
        update.setLong(7, timestamp);
        update.executeUpdate();
    }

    /**
     * Initializes prepared statements for update operations
     * @param tableName Name of device data table
     */
    public void setUpdateStatement(String tableName) throws Exception {
        this.update = conn.prepareStatement(getUpdateStatement(tableName));
    }

    /**
     * Initializes prepared statements for select operations
     * @param tableName Name of device data table
     */
    public void setSelectStatement(String tableName) throws Exception {
        this.select = conn.prepareStatement(getSelectStatement(tableName));
    }

    /**
     * Initializes prepared statements for insert operations
     * @param tableName - Name of device data table
     */
    public void setInsertStatement(String tableName) throws Exception {
        this.insert = conn.prepareStatement(getInsertStatement(tableName));
    }

    /**
     * Creates and returns a statement which would be
     * pre-compiled and used for inserting new device records
     * @param tableName Name of the device data table
     * @return Insert SQL statement for device data
     */
    public String getInsertStatement(String tableName) {
        return String.format("INSERT INTO %s(did, ts, minimum, maximum, total, deviceCount, average) " +
                "VALUES(?,?,?,?,?,?,?);", tableName);
    }

    /**
     * Creates and returns a select SQL statement for
     * device data
     * @param tableName Name of device data table
     * @return Select SQL statement for device data
     */
    public String getSelectStatement(String tableName) {
        return String.format("SELECT * FROM %s WHERE did=? AND ts=?", tableName);
    }

    /**
     * Creates and returns a update SQL statement for
     * device data
     * @param tableName Name of device data table
     * @return Update SQL statement for device data
     */
    public String getUpdateStatement(String tableName) {
        return String.format("UPDATE %s SET minimum=?, maximum=?, total=?, deviceCount=?, " +
                "average=? WHERE did=? AND ts=?", tableName);
    }
}
