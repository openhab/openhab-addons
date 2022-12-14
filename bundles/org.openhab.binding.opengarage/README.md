# OpenGarage Binding

The OpenGarage binding allows you to control an OpenGarage controller (<https://opensprinkler.com/product/opengarage/>) using openHAB

## Supported Things

Opengarage controllers from <https://opensprinkler.com/product/opengarage/> are supported.

## Discovery

Auto-discover is not currently supported.
You need to manually add a new item using its IP address.

## Thing Configuration

As a minimum, the IP address is needed:

- `hostname` - The hostname of the OpenGarage controller. Typically you'd use an IP address such as `192.168.0.5` for this field.
- `port` - the port the OpenGarage is listening on. Defaults to port 80
- `refresh` - The frequency with which to refresh information from the OpenGarage controller specified in seconds. Defaults to 10 seconds.
- `password` - The password to send commands to the OpenGarage. Defaults to "opendoor"

## Channels

| channel              | type          | description                                                                           |
|----------------------|---------------|---------------------------------------------------------------------------------------|
| distance             | Number:Length | Distance reading from the OpenGarage controller (default in cm)                       |
| status-switch        | Switch        | Door status (OFF = Closed, ON = Open), set "invert=true" on channel to invert switch  |
| status-contact       | Contact       | Door status (Open or Closed)                                                          |
| status-rollershutter | Rollershutter | Door status (DOWN = Closed, UP = Open)                                                |
| vehicle-status       | Number        | Report vehicle presence (0=Not Detected, 1=Detected, 2=Unknown)                       |

## Full Example

opengarage.things:

```java
opengarage:opengarage:OpenGarage [ hostname="192.168.0.5" ]
```

opengarage.items:

```java
Switch OpenGarage_Status { channel="opengarage:opengarage:OpenGarage:status" }
Contact OpenGarage_Status_Contact { channel="opengarage:opengarage:OpenGarage:status-contact" }
Rollershutter OpenGarage_Status_Rollershutter { channel="opengarage:opengarage:OpenGarage:status-rollershutter" }
Number:Length OpenGarage_Distance { channel="opengarage:opengarage:OpenGarage:setpoint" }
String OpenGarage_Vehicle { channel="opengarage:opengarage:OpenGarage:vehicle" }
```

opengarage.sitemap:

```perl
Switch item=OpenGarage_Status icon="garagedoorclosed" mappings=[ON=Open]  visibility=[OpenGarage_Status == OFF]
Switch item=OpenGarage_Status icon="garagedooropen"   mappings=[OFF=Close] visibility=[OpenGarage_Status == ON]
Switch item=OpenGarage_Status icon="garage" 
Contact item=OpenGarage_Status_Contact icon="garage" 
Rollershutter item=OpenGarage_Status_Rollershutter icon="garage" 
Text item=OpenGarage_Distance label="OG distance"
Text item=OpenGarage_Vehicle label="Vehicle Presence"
```
