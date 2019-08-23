# OpenGarage Binding

The OpenGarage binding allows you to control an OpenGarage controller (https://opensprinkler.com/product/opengarage/) using OpenHAB


## Supported Things

Opengarage controllers from https://opensprinkler.com/product/opengarage/ are supported.

## Discovery

Auto-discover is not currently supported. 
You need to manually add a new item using its IP address.

## Thing Configuration

As a minimum, the IP address is needed:
* host - The hostname of the OpenGarage controller. Typically you'd use an IP address such as `192.168.0.5` for this field.
* port - the port the OpenGarage is listening on. Defaults to port 80
* refresh - The frequency with which to refresh information from the OpenGarage controller specified in seconds. Defaults to 10 seconds.
* password - The password to send commands to the OpenGarage. Defaults to "opendoor"


## Channels

| channel  | type   | description                                            |
|----------|--------|--------------------------------------------------------|
| distance | Number:Length | Distance reading from the OpenGarage controller (default in cm)       |
| status   | Switch | Door status OFF = Closed, ON = Open                    |
| vehicle  | String | Report vehicle presence from the OpenGarage controller |

## Full Example

opengarage.things:

```
opengarage:opengarage:garage [ host="192.168.0.5" ]
```

opengarage.items:

```
Switch OpenGarage_Status { channel="opengarage:opengarage:garage:status" }
Number:Distance OpenGarage_Distance { channel="opengarage:opengarage:garage:setpoint" }
String OpenGarage_Vehicle { channel="opengarage:opengarage:garage:vehicle" }
```

opengarage.sitemap:

```
Switch item=OpenGarage_Status icon="garagedoorclosed" mappings=[ON=Open]  visibility=[OpenGarage_Status == CLOSED]
Switch item=OpenGarage_Status icon="garagedooropen"   mappings=[OFF=Close] visibility=[OpenGarage_Status == OPEN]
Switch item=OpenGarage_Status icon="garage" 
Text item=OpenGarage_Distance label="OG distance"
String item=OpenGarage_Vehicle label=Vehicle Presence"
```


