# DBQuery Binding

This binding allows creating items from the result of native database queries.
It currently supports InfluxDB 2.X and several relational databases databases through JDBC.

You can use the addon in any situation where you want to create an item from a native query.
The source of the query can be any supported database, and doesn't need to be the one you use as the persistence service in openHAB.
Some use cases can be:

- Integrate a device that stores its data in a database
- Query derived data from you openHAB persistence, for example with Influx2 tasks you can process your data to create a new one   
- Bypass limitations of current openHAB persistence queries

## Supported Things

There are three types of supported things: `influxdb2`, `jdbc` and `query`.
For each different database you want to connect to, you must define a `Bridge` thing for that database.
Then each `Bridge` can define as many `Query` things that you want to execute.

## Thing Configuration

### Bridges

#### influxdb2

Defines a connection to an Influx2 database and allows creating queries on it.

| Parameter    | Required | Description                               |
|--------------|----------|-----------------------------------------  |
| url          | Yes      | database url                              |
| user         | Yes      | name of the database user                 |
| token        | Yes      | token to authenticate to the database  ([Intructions about how to create one](https://v2.docs.influxdata.com/v2.0/security/tokens/create-token/)) |
| organization | Yes      | database organization name                |
| bucket       | Yes      | database bucket name                      |

#### jdbc

Defines a connection to a relational database through JDBC connection and allows creating queries on it.

| Parameter    | Required | Description                               |
|--------------|----------|-----------------------------------------  |
| url          | Yes      | database url                              |
| user         | Yes      | name of the database user                 |
| password     | Yes      | password of the database user             |
| maxPoolSize  | No       | maximum number of connections             |
| minimumIdle  | No       | minimum number of connections that the pool will maintain idle to serve queries|

Currently, supported[^1] databases are:

- *PostgreSQL*  (with driver version 42.2.18)
- *Derby* (with driver version 10.12.1.1)
- *H2* (with driver version 1.4.191)
- *HSQLDB* (with driver version 2.3.3)
- *MariaDB* (with driver version 1.3.5)
- *MySQL* (with driver version 8.0.26)
- *Sqlite* (with driver version 3.16.1)

JDBC driver must be installed in your OpenHab server. For convenience, if you only need one driver the addon provides several features with JDBC driver included for each of the previous databases.
If you need more than one driver you must install them manually from OpenHab console. 
For example to install PostgreSQL driver you can use: `bundle:install https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.18/postgresql-42.2.18.jar`

[^1]: Newer versions or other JDBC database drivers can work correctly but are not tested.

### query

The `Query` thing defines a native query that provides several channels that you can bind to items. 

#### Query parameters

The query items support the following parameters:

| Parameter    | Required | Default  | Description                                                           |
|--------------|----------|----------|-----------------------------------------------------------------------|
| query        | true     |          | Query string in native syntax                                         |
| interval     | false    | 0        | Interval in seconds in which the query is automatically executed      |
| hasParameters| false    | false    | True if the query has parameters, false otherwise                     | 
| timeout      | false    | 0        | Query execution timeout in seconds                                    |
| scalarResult | false    | true     | If query always returns a single value or not                         |
| scalarColumn | false    |          | In case of multiple columns, it indicates which to use for scalarResult|

These are described further in the following subsections.

##### query  

The query the items represents in the native language of your database:

 - Flux for `influxdb2`
 
#### hasParameters

If `hasParameters=true` you can use parameters in the query string that can be dynamically set with the `setQueryParameters` action.
 
For InfluxDB use the `${paramName}` syntax for each parameter, and keep in mind that the values from that parameters must be from a trusted source as current
parameter substitution is subject to query injection attacks.

For JDBC use `:parameterName` syntax.

#### timeout

A time-out in seconds to wait for the query result, if it's exceeded, the result will be discarded and the addon will do its best to cancel the query.
Currently it's ignored and it will be implemented in a future version.

#### scalarResult 

If `true` the query is expected to return a single scalar value that will be available to `result` channels as string, number, boolean,...
If the query can return several rows and/or several columns per row then it needs to be set to `false` and the result can be retrieved in `resultString`
channel as JSON or using the `getLastQueryResult` action.   

#### scalarColumn

In case `scalarResult` is `true` and the select returns multiple columns you can use that parameter to choose which column to use to extract the result.

## Channels

Query items offer the following channels to be able to query / bind them to items:

| Channel Type ID | Item Type | Description                                                                                                                        |
|-----------------|-----------|------------------------------------------------------------------------------------------------------------------------------------|
| execute         | Switch    | Send `ON` to execute the query manually. It also indicates if query is currently running (`ON`) or not running (`OFF`)          |
| resultString    | String    | Result of last executed query as a String |
| resultNumber    | Number    | Result of last executed query as a Number, query must have `scalarResult=true` |
| resultDateTime  | DateTime  | Result of last executed query as a DateTime, query must have `scalarResult=true` |
| resultContact   | Contact   | Result of last executed query as Contact, query must have `scalarResult=true` |
| resultSwitch    | Switch    | Result of last executed query as Switch, query must have `scalarResult=true` |
| parameters      | String    | Contains parameters of last executed query as JSON|
| correct         | Switch    | `ON` if the last executed query completed successfully, `OFF` if the query failed.|

All the channels, except `execute`, are updated when the query execution finishes, and while there is a query in execution they have the values from
last previous executed query.

The `resultString` channel is the only valid one if `scalarResult=false`, and in that case it contains the query result serialized to JSON in that format:

    {
        correct : true,
        data : [
            { 
                column1 : value,
                column2 : value
            },
            { ... }, //row2        
            { ... }  //row3
        ]
    }
    
### Channel Triggers

#### calculateParameters

Triggers when there's a need to calculate parameters before query execution.
When a query has `hasParameters=true` it fires the `calculateParameters` channel trigger and pauses the execution until `setQueryParameters` action is call in
 that query.
 
In the case a query has parameters, it's expected that there is a rule that catches the `calculateParameters` trigger, calculate the parameters with the corresponding logic and then calls the `setQueryParameters` action, after that the query will be executed.
 
## Actions

### For DatabaseBridge

#### executeQuery 

It allows executing a query synchronously from a script/rule without defining it in a Thing.

To execute the action you need to pass the following parameters:

- String query: The query to execute
- Map<String,Object>: Query parameters (empty map if not needed)
- int timeout: Query timeout in seconds

And it returns an `ActionQueryResult` that has the following properties:

- correct (boolean) : True if the query was executed correctly, false otherwise
- data (List<Map<String,Object>>): A list where each element is a row that is stored in a map with (columnName,value) entries  
- isScalarResult: It returns if the result is scalar one (only one row with one column)
- resultAsScalar: It returns the result as a scalar if possible, if not returns null


Example (using Jython script):

     from core.log import logging, LOG_PREFIX 
     log = logging.getLogger("{}.action_example".format(LOG_PREFIX))
     map = {"time" : "-2h"}
     influxdb = actions.get("dbquery","dbquery:influxdb2:sampleQuery") //Get bridge thing
     result = influxdb.executeQuery("from(bucket: \"default\") |> range(start:-2h)  |> filter(fn: (r) => r[\"_measurement\"] == \"go_memstats_frees_total\")  |> filter(fn: (r) => r[\"_field\"] == \"counter\")  |> mean()",{},5)
     log.info("execute query result is "+str(result.data))
    

Use this action with care, because as the query is executed synchronously, it is not good to execute long-running queries that can block script execution.

### For Queries

#### setQueryParameters

It's used for queries with parameters to set them.
To execute the action you need to pass the parameters as a Map.

Example (using Jython script):

    params = {"time" : "-2h"}
    dbquery = actions.get("dbquery","dbquery:query:queryWithParams")  //Get query thing
    dbquery.setQueryParameters(params)

#### getLastQueryResult

It can be used in scripts to get the last query result.
It doesn't have any parameters and returns an `ActionQueryResult` as defined in `executeQuery` action.

Example (using Jython script):

    dbquery = actions.get("dbquery","dbquery:query:queryWithParams")  //Get query thing
    result = dbquery.getLastQueryResult()
    

## Examples

### The Simplest case InfluxDB2

Define a InfluxDB2 or JDBC database thing and a query with an interval execution.
That executes the query every 15 seconds and puts the result in `myItem`.

#### InfluxDB2

    # Bridge Thing definition
    Bridge dbquery:influxdb2:mydatabase "InfluxDB2 Bridge" [ bucket="default", user="admin", url="http://localhost:8086", organization="openhab", token="*******" ]
    
    # Query Thing definition
    Thing dbquery:query:myquery "My Query" [ interval=15, hasParameters=false, scalarResult=true, timeout=0, query="from(bucket: \"default\") |> range(start:-1h) |> filter(fn: (r) => r[\"_measurement\"] == \"go_memstats_frees_total\")  |> filter(fn: (r) => r[\"_field\"] == \"counter\")  |> mean()", scalarColumn="_value" ]
    
    # Item definition
    Number myItem "QueryResult" {channel="dbquery:query:myquery:resultNumber"}

#### JDBC

    # Bridge Thing definition
    Bridge dbquery:jdbc:mydatabase2 "JDBC Bridge" [ url="jdbc:postgresql://localhost:5432/openhab", user="openhab", password="openhab" ]
    
    # Query Thing definition
    Thing dbquery:query:myquery2 "My Query2" [ interval=15, hasParameters=false, scalarResult=true, timeout=0, query="select avg(value) from temperatures where time >= current_date - 1"]
    
    # Item definition
    Number myItem2 "QueryResult" {channel="dbquery:query:myquery2:resultNumber"}

### A query with parameters

#### InfluxDB2

Parameters are set using `${parameterName}`.
Using the previous example (at simplest case) you change the `range(start:-1h)` for `range(start:${time})`. 

Create a rule that is fired 

 - **When** `calculateParameters` is triggered in `myquery`
 - **Then** executes the following script action (in that example Jython):
      
            map = {"time" : "-2h"}   
            dbquery = actions.get("dbquery","dbquery:query:myquery")   
            dbquery.setQueryParameters(map)

#### JDBC

Parameters are set using `:parameterName`.
Using the previous example (at simplest case) you change the `time >= current_date - 1` for `time >= current_date - :days`

Create a rule that is fired

- **When** `calculateParameters` is triggered in `myquery2`
- **Then** executes the following script action (in that example Jython):

           map = {"days" : 2}   
           dbquery = actions.get("dbquery","dbquery:query:myquery2")   
           dbquery.setQueryParameters(map)
