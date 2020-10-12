# DBQuery Binding

This binding allow creating items from the result of native database queries.
It currently supports InfluxDB 2.0 and will support major relational databases through JDBC.

## Supported Things

There are two types of supported things: `Bridge` and a `Query`.
For each different database you want to connect you must define a `Bridge` thing for that database.
Then each `Bridge` can define as much as wanted `Query` things you want to execute.

Current supported `Bridge` are:
- `Influx2Bridge`

## Thing Configuration

### Bridges

#### Influx2Bridge

| Parameter    | Required | Description                               |
|--------------|----------|-----------------------------------------  |
| url          | Yes      | database url                              |
| user         | Yes      | name of the database user                 |
| token        | Yes      | token to authenticate to the database  ([Intructions about how to create one](https://v2.docs.influxdata.com/v2.0/security/tokens/create-token/))   |
| organization | Yes      | database organitzation name               |
| bucket       | Yes      | database bucket name                      |

### Query

The `Query` Thing requieres the native query to run in the `query` parameter. The syntax of that query must
 be in the native syntax that your database supports:
 - Flux for `Influx2Bridge`
 - SQL for `JDBCBridge`
 
 You can use parameters using `***toBedefined***` in the query. If the query has some parameters they will need to be defined in
 the `parameters channel. 

Optionally you can specify the following parameters:
- `timeout` - A time-out in seconds to wait for the query result, if it's exceeded result will be discarded and the 
addon will do it's best effort to cancel the query.
- `scalarResult` - If true (the default value) the query is expected to return a single scalar value that will be available to `result` channels as an
scalar value.
If the query can return several rows and/or several columns per row then it needs to be set to false and the result will be saved in 
`result` channel as a JSON String in `nonScalarResultFormat`.   


## Channels

Item channels:

| Channel Type ID | Item Type | Description                                                                                                                        |
|-----------------|-----------|------------------------------------------------------------------------------------------------------------------------------------|
| execute         | Switch    | Send `ON` to execute the query, the current state tells if the query is running                                                    |
| result          | String    | Result data of last executed query. A string with the result if `scalarResult` is true and a list of maps for each register a JSON with result data if set to false |
| parameters      | String    | If query has parameters it exposes current parameters values as JSON object                                                        |
| correct         | Switch    | True if the result executed correctly, otherwise false                                                                             |

Trigger channels:

- `calculateParameters` : Triggers when there's a need to calculate parameters before query execution

### Non scalar results
When `scalarResult` is set to false that `onScalarResultFormat` is used for the result:

    {
        correct : true,
        data : [
            {
                column1 : value,
                column2 : value
            },
            { ... }        
        ]
    }

### Setting parameters
If your query has parameters they must be set in a Rule using the `setQueryParameters` action. 

## Actions
The addon also provides actions to run a query from a rule or script if you need it.

Actions that must be executed on a Bridge Thing:
 
    String result = executeQuery(String query, Map<String,Object> parameters)
    QueryResult result  = executeQueryNonScalar(String query, Map<String,Object> parameters) //TODO: check if possible to return custom type

You don't need to define any Query Item to use that actions.


Actions that must be executed on a Query Thing:

    setQueryParameters(Map<String,Object> parameters)


## Full Example

**demo.things**

    Bridge dbquery:JDBCBridge:mysql "MySQL" [ 
            url="jdbc:mysql://localhost:3306/openhab",
            user="openhab",
            password="mypassword"            
    ] {
        Thing query query1 "My first query" [query="select value from tablex where state=:state limit 1", interval=60]
    }
      
**demo.rules**

    rule "Set up your parameters"
    when
       Channel "querydb:query:query1:calculateParameters" triggered START
    then
          val setParamsAction = getActions("querydb","querydb:query:query1")
          val Map<String,String> params = newHashMap
          params.put("state","state1")
          setParamsAction.setQueryParameters(params)          
    end
    
**demo.items**    

    Number result { channel="querydb:query:query1:result"}
    
## Action Example
**demo.things**

    Bridge dbquery:JDBCBridge:mysql "MySQL" [ 
            url="jdbc:mysql://localhost:3306/openhab",
            user="openhab",
            password="mypassword"            
    ] 
 
**demo.rules**
    
    rule "Your rule"    
    when
      ...
    then 
    val 
        val Map<String,String> params = newHashMap
        params.put("state","state1")    
        val result = executeQuery("select value from tablex where state=:state limit 1",params)
        logInfo("Your query result is " + result)        
    end
