package com.meraki;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

/**
 * This class provides the configuration values for the project
 * @author Siddhanth Venkateshwaran
 */
public class Constants {
    static Config config = ConfigFactory.parseFile(new File("src/main/resources/application.conf"));
    public static int PORT = config.getInt("conf.PORT");

    public static String dbHost = config.getString("conf.DB.HOST");
    public static String dbUser = config.getString("conf.DB.USER");
    public static String dbPassword = config.getString("conf.DB.PASSWORD");
    public static String dbName = config.getString("conf.DB.NAME");

}
