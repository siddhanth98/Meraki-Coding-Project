## Running the project
The documentation for the project can be found [here](documentation.md)

- The project utilizes Java 11, PostGreSQL 12.2 and the SBT build tool to manage the project's dependencies, in a Windows 10 platform.

- So a version (>= 12.2) of the PostGreSQL server and other utilities, along with the SBT toolkit need to be installed in the target system.

- PostGreSQL server and tools can be downloaded from [here](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads) and SBT can be downloaded from [here](https://www.scala-sbt.org/download.html)

- After installation, configure a database by using a command line tool like psql, using the following command: `CREATE DATABASE databaseName;`

- Edit the following in the `src/main/resources/application.conf` file:

    - `conf.DB.USER`: Set this to a custom user name if required. Default user is "postgres".
    - `conf.DB.PWD`: Set this to the configured password of the current user (This property will be removed in the future to make the service more secure)
    - `conf.DB.HOST`: Set this to the name of the host in which the database server will run. Default name is localhost.
    - `conf.DB.NAME`: Set this to the name of the  database which was configured earlier.
    - `conf.DB.TABLE.NAME`: Set this to the desired name of the device data table.

- Clone the repository

- From the root of the project repository, run command `sbt` to pull all dependencies specified in the `build.sbt` file.

- Run the project using the following steps:

    - Ensure the database server is running and the configuration variables are properly set up as specified above.
    - From the root directory of the project, run the main application program using `sbt run` or `sbt "runMain com.meraki.Application"`. This will start the server and automatically (drop if it exists) create the required table with the specified columns in the database.
    - The service can now be manually tested using a simple curl request as follows:

        - `curl http://localhost:8000`
        - `curl -d "did=1&value=100&ts=1234556" http://localhost:8000/devices` Send a device update request using URL encoded content type in the request body.
        - `curl -H "Content-Type:application/json" -d "{\"did\"=1,\"value\"=100,\"ts\"=1234556}" http://localhost:8000/devices` Send a device update request using JSON encoded content in the request body.
        - `curl -d "did=1&ts=1234657" http://localhost:8000/deviceStats` - This will obtain the device stats for the requested device ID 1.
        - `curl -H "Content-Type:application/json" -d "{\"did\"=1,\"ts\"=1234556}" http://localhost:8000/deviceStats` - This is the same as the previous request except for the change in the content-type from `application/x-www-form-urlencoded` to `application/json`.

    - Appropriate responses will be received and the corresponding database table can be checked to ensure records are being stored.


### Running the tests
- There are 4 tests that test the core functionality of the service.
    - **Test1**: Ensures service is active
    - **Test2**: Ensures service gets all device updates in sequence
    - **Test3**: Ensures invalid requests are handled properly.
    - **Test4**: Ensures a subsequent query for device stats returns the most updated and most recent device values for the specified timestamp.

- Run the tests by running either of the following commands from the root directory of the project:

    - `sbt test`
    - `sbt "testOnly com.meraki.DeviceAPITest`