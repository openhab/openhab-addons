# Apache Camel service

From Wikipedia
```
Apache Camel is a rule-based routing and mediation engine that provides a Java object-based implementation of the Enterprise Integration Patterns using an API (or declarative Java Domain Specific Language) to configure routing and mediation rules. The domain-specific language means that Apache Camel can support type-safe smart completion of routing rules in an integrated development environment using regular Java code without large amounts of XML configuration files, though XML configuration inside Spring is also supported.
```
See more from [Apache Camel](http://camel.apache.org) web pages.

## Supported Camel features

See available Camel [components](http://camel.apache.org/components.html),  [data formats](http://camel.apache.org/data-format.html) and [enterprise integration patterns](http://camel.apache.org/enterprise-integration-patterns.html) from Camel web page.

OpenHAB Camel binding also implement openHAB Camel component, which can be used to send openHAB item events (updates and commands) to Camel routes and vice versa. OpenHAB component act also as persistence service, which send item store events to Camel routes. Also automation rules can utilize Camel component by sending messages to Camel routes.

## Global Configuration
Camel binding works fine without configuration parameters. By default Camel routes are loaded from "camel" folder in openHAB configuration folder and default corePoolSize is 3 and maxPoolSize is 5. If you want to override those parameters, you can add camel.cfg file in conf/services folder or change parameters e.g. via PaperUI. 

Sample camel.cfg content:
```
org.openhab.camel:folderName=camel
org.openhab.camel:corePoolSize=3
org.openhab.camel:maxPoolSize=5
```

## Route Configuration

Camel routes are configured by xml files (one or several). Binding loads xml files from configurable folder and monitor file changes and automatically reload changes.

URI format
```
openhab://service[?options]
```
Where **service** defines used OpenHAB feature. Available services are following:

 - status
 - command
 - persistence
 - action

#### Status  service
Status service can be used to send item state updates from openHAB to Camel routes or vice versa, to send item state updates from Camel routes to openHAB item states.
```
openhab://status[?itemName=<itemName>]
```
Where itemName option can be used to filter wanted item. If you want to send all openHAB items states to same Camel route, you can use * as a itemName option.

#### Command service
Command service can be used to send item commands from Camel routes to openHAB items.
```
openhab://command[?itemName=<itemName>]
```
Where itemName can be used to select correct item. 

####Persistence service
Persistence service can be used to store item states to Camel routes.
```
openhab://persistence[?itemName=<itemName>]
```
Where itemName option can be used to filter wanted item. If you want to send all openHAB items to same Camel route, you can use * as a itemName option. You need to define also camel.persist file in conf/persistence folder.

####Action service
Action service can be used to send Camel messages from openHAB automation rules.
```
openhab://action[?actionId=<actionId>]
```
Where actionId can be used to to select correct Camel route. 


## Automation rules
```
sendCamelAction(<actionId>, <headers>, <message>)
```

where 
**actionId** is used to select correct Camel route
**headers** to be included in Camel message, can be null
**message** to set to Camel message body

Examples:
```
sendCamelAction("demoAction", null, "test message")
```

```
val myHeaders = newLinkedHashMap('a' -> 1, 'b' -> 2) 
sendCamelAction("demoAction", myHeaders, "test message")
```


## Example route configuration
Examples below gives some ideas what could be done by the Camel routes.
```
<routes xmlns="http://camel.apache.org/schema/spring">

    <!--
    Store all item updates to file /tmp/updates.log. 
    Route also enrich data by Camel message translator (add time stamp and item name)
    E.g. 2016-05-18 20:09:54 Wifi_Level 8

    Routes also log message body and headers to openHAB log.
    -->
    <route id="All-items"> 
        <from uri="openhab:status?itemName=*"/>
        <log message="body=${in.body}, headers=${in.headers}"/>
        <transform>
            <simple>${date:now:yyyy-MM-dd HH:mm:ss} ${in.headers.itemName} ${in.body}\n</simple>
        </transform>
        <inOnly uri="file:/tmp?fileName=updates.log&amp;fileExist=append"/> 
    </route>

    <!--
    Routes direct all sendCamelAction method calls from 
    automation rules to /tmp/action.log file, where actionId is demoAction.

    sendCamelAction("demoAction", null, "test message")
    -->
    <route id="Action"> 
        <from uri="openhab:action?actionId=demoAction"/>
        <to uri="file:/tmp?fileName=action.log&amp;fileExist=append"/> 
    </route>

    <!--
    Route update openHAB item motionSensor state to ON state every 30 seconds
    -->
    <route id="Motion-In"> 
        <from uri="timer://foo?fixedRate=true&amp;period=30000"/>
        <setBody>
            <simple>ON</simple>
        </setBody>
        <log message="body=${in.body}, headers=${in.headers}"/>
        <to uri="openhab:command?itemName=motionSensor"/> 
    </route>

</routes>
```

