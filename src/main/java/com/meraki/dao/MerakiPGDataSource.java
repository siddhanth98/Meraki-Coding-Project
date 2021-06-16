package com.meraki.dao;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

import static com.meraki.Constants.*;

/**
 * Data source for getting the database connection object
 * @author Siddhanth Venkateshwaran
 */
public class MerakiPGDataSource {
    private final PGSimpleDataSource dataSource;

    public MerakiPGDataSource() {
        dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{dbHost});
        dataSource.setDatabaseName(dbName);
        dataSource.setUser(dbUser);
        dataSource.setPassword(dbPassword);
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
