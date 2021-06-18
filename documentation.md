# Real time data stream processor

- This project utilizes **Java 11** and **PostGreSQL 12** to implement a service which processes a stream of data and stores the results in a relational database. The data represents statistical data coming from remote devices at different time points, and which maybe active throughout the year.

## Working of main components
- The main components of the project are as follows:

    - *Web Server*: This is a a http server instance based on the simple implementation of [com.sun.net.httpserver](https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html), which exposes different API endpoints to query / update data pertaining to the requesting device. The server is configured to start in localhost at port 8000. The port number can be changed in the `src/main/resources/application.conf` file. The following main API endpoints currently exist to process the stream:

        1. `/devices`:  This endpoint is used when a remote device needs to update the backend with its data. The device data can have either of `application/json` and `application/x-www-form-urlencoded` content types.
        2. `/deviceStats`: This endpoint is used when a client needs to query the backend to get the most recently updated device stats for a given timestamp. This is more useful in scenarios when the service needs to be tested for consistency in storing and retrieving correct device records from the database. The data returned in response consists of the minimum, maximum and average values computed for the specified device during the specified timestamp.
        3. `/termination`: This endpoint is used when the service may need to be remotely stopped and the database connection may need to be closed to save resources. (in addition to this the service and the database connection can be closed programmatically if required)

    - *DBMS*: A relational database management system consisting of 1 table (explained below) to persist device data corresponding to different timestamps, query them and create new / update existing device records whenever required. All database operations are exposed to the service using a **data access object** to reduce dependency of the rest of the codebase on the underlying database management system. A postgresql based datasource instance is used to create a single connection using valid user credentials and expose the connection object to the DAO. Each record in the device table has the following attributes:

        1. *deviceId*: ID of the device as sent by the device
        2. *timestamp*: start_time of the 1-minute interval at which the data were processed and updated
        3. *minimum*: smallest value received in the 1-minute interval for the given device
        4. *maximum*: largest value received in the 1-minute interval for the given device
        5. *total*: This is the sum of all values received in the 1-minute interval for the given interval. The purpose of storing this attribute is to quickly compute the average value for the given device in this 1-minute interval.
        6. *deviceCount*: This is the number of times an update request was received by the device in the 1-minute interval. In conjunction with value of *total*, the value of this attribute is used to quickly compute the average value.
        7. *average*: This is the average of all values received in the 1-minute interval for the given device.
           Both the *deviceId* and *timestamp* values are used as primary keys to uniquely identify each datapoint and quickly access a record in the database whenever the service receives an update / query request for a specified device in a specified timestamp.

    - *Service*: The service accepts inputs having the following fields - (deviceId, value, timestamp), and computes the valid *start_timestamp* based on 1-minute increments from the first timestamp which was ever received by the service. The service used both the *deviceId* and *start_timestamp* values to initially check if a record already exists for the given device at the given timestamp. If so, then the service simply computes the new minimum, maximum and average values and updates the existing record with the new values. If a record doesn't exist, then a new record is created having the above mentioned attributes.
      This results in 2 database queries / update request by 1 device, which is discussed in more depth below.

## JSON (De) Serialization
- As mentioned above, both the `/devices`  and `/deviceStats` endpoints allow both `application/json` and `application/x-www-form-urlencoded` content types in the request body.

- A valid request to the `/devices` endpoint should have all of the following properties in the request body having non-null values:
    1. did/deviceId
    2. value
    3. ts/timestamp

- A valid request to the `/deviceStats` endpoint should have all of the following properties having non-null values:
    1. did/deviceId
    2. ts/timestamp

- The urlencoded content type is supported in order to make it easier to test the endpoints with simple curl requests having relatively simple structures for the data in the request body.
- The json content type is also supported to allow more complex structures when the device data may possibly have more properties to update the service with, in the future.

- For supporting the json types, the service uses a custom deserializer to ensure that requests having malformed data or missing properties are not processed and responded with a 422 http error, to represent unprocessable entities.

- The [jackson](https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-databind/2.12.1/index.html) library is used for serializing and deserializing requests to/from JSON.

## Database Schema
- The initial design of the database consisted of 2 separate tables, one named deviceTimestamps and the other named deviceData. The first table (deviceTimestamps) consisted of the following attributes:
    1. deviceId: ID of device
    2. timestamp: start timestamp of 1-minute interval
    3. dataId: foreign key referencing the corresponding attribute in the deviceData table

  The second table (deviceData) consisted of all the following (similar to the above mentioned 7 attributes):

    1. dataId: auto-incrementing primary key to uniquely identify data points and leverage the auto creation of an index by postgres on this key to speed up the database access.
    2. minimum: smallest value in 1-minute interval
    3. maximum: largest value in 1-minute interval
    4. total: sum of all values in 1-minute interval
    5. deviceCount: number of update requests made in 1-minute interval
    6. average: average of all values in 1-minute interval

- The reason for this vertically fragmented schema was to avoid making frequent queries to a single large table to check if a device record exists or not and to possibly reduce duplication of deviceIds and timestamps in the large table.

- The service then used to query the first table to check if a device record exists or not, and if it did, then use the foreign key to perform a JOIN operation and access the corresponding record in the second table to get old min, max, total, count and avg values.

- However this design posed the following apparent problems:
    1. Whenever a data point is created for the first time in the deviceData table, a corresponding record has to be created in the referencing table deviceTimestamps to maintain consistency and allow the JOIN operations to be performed. To do this, either of the following options seems plausible:

        - Create a trigger which inserts the corresponding record in the referencing table. This results in 1 select and 2 insert queries to be executed for every device update request in the worst case.

        - Make the service itself insert the corresponding record in the referencing table. This requires the service to obtain the *dataId* key value created for the new record as part of the auto-incrementing nature of postgres, and then use this key to insert the record in the referencing table, which consequently requires an additional select query to be executed. This results in 2 select and 2 insert queries to be executed for every device update request in the worst case.

- Due to this inability to obtain an apparent performance benefit by vertically fragmenting the tables, the schema was changed to comprise only 1 table named deviceData, which requires 1 select and 1 insert query to be executed when a data point is first created.

## Potential Improvements and Changes
This section discusses improvements in the design and implementation of the service which will be made with more time.

### Database size
- The current design consists of a single web server instance connected to a single database server instance to process the stream of device data.

- One apparent drawback of this approach is that the deviceData table can grow to a very large size if the rate of update requests made is around 100,000 reqs/second.
  This sums up to (100,000 x 86,400) = 8.64 billion requests made in 1 day.
  Assuming a new database record gets created every 1 minute, this leads to ~144 million database records getting created in 1 day.

- One solution to prevent the database table from growing further in size which seems apparent is to store all records created in the whole day in another long-term storage device using a background archival process, which can start executing on a fixed time every day / night. All archived records can then be removed from the database, so that the table can start filling up afresh in the next day.

- However if a request to obtain device stats from a previous day is received, then this will require that record to be fetched from the archive storage, which can be slower. This problem can possibly be reduced using an **LRU** caching scheme, where only the most recently queried devices have a higher chance of getting queried again and these can stored in a high-speed cache maintained locally. Whenever the request timestamp is from the previous day and the record for that device is not found in the cache, then the correct record can be fetched from archive and replaced for the one which belongs to a device which has not been queried for the longest time. This will possibly make the queries more complex but the caching benefit compensates for the longer querying times of more complex queries.

### Database replication
- The single database instance can become a single source of failure with no backups. This can be resolved by periodically replicating the most recently created database records in a standby database server, which can take control when the master server fails. Having several standby servers increases the fault-tolerance further.

### Cluster of web server instances
- Similar to the previous point, the single web server instance can quickly become a bottleneck if rate of requests is very high, with no fault-tolerance. Assuming the web server handles device update requests from devices in a particular zone/region, a cluster of web server instances can be maintained in that region such that all instances manage connections with a common set of Database servers to ensure consistency of device stats retrieved upon request. In addition, having a load balancer manage allocation of device update requests to web servers in the cluster can fairly well distribute the load and thus increase resiliency.

### Implementation of Data Access Object
- The current implementation consists of a data access object belonging to a single class which executes SQL queries in the PostGreSQL database.

- If the DBMS has to be changed to MySQL, then this requires a new DAO implementation for executing MySQL queries and this will introduce changes in the rest of the codebase which still use the methods of the PostGreSQL DAO.

- This change can be avoided by having an interface expose common methods to the rest of the codebase to manage database operations. Any change in the DBMS then only requires implementing a new DAO and doesn't require changes in the rest of the codebase.

### Communication protocol
- The web server currently uses HTTP to communicate with clients. Device data coming from clients are insecure while in transit using this protocol. This protocol will be changed to HTTPS using a 3rd party certificate authority to issue a TLS certificate for the web server.

### Management of Database credentials
- The current implementation imports database user credentials from a configuration file, which is not secure. This will be changed to possibly import the sensitive credentials from an environment variable programmatically to avoid having to store passwords in a configuration file in an unencrypted manner.