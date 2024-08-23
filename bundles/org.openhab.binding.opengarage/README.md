# OpenGarage Binding

The OpenGarage binding allows you to control an OpenGarage controller (<https://opensprinkler.com/product/opengarage/>) using openHAB

## Supported Things

OpenGarage controllers from <https://opensprinkler.com/product/opengarage/> are supported.

## Discovery

Auto-discover is not currently supported.
You need to manually add a new item using its IP address.

## Thing Configuration

As a minimum, the IP address is needed:

- `hostname` - The hostname of the OpenGarage controller. Typically you'd use an IP address such as `192.168.0.5` for this field.
- `port` - the port the OpenGarage is listening on. Defaults to port 80
- `refresh` - The frequency with which to refresh information from the OpenGarage controller specified in seconds. Defaults to 10 seconds.
- `password` - The password to send commands to the OpenGarage. Defaults to "opendoor"
- `doorTransitionTimeSeconds` - Specifies how long it takes the garage door
to fully open / close after triggering it from OpenGarage, including auditory
beeps. Recommend to round up or pad by a second or two.
- `doorOpeningState` - Text state to report when garage is opening. Defaults to "OPENING".
- `doorOpenState` - Text state to report when garage is open (and not in transition). Defaults to "OPEN".
- `doorClosingState` - Text state to report when garage is closing. Defaults to "CLOSING".
- `doorClosedState` - Text state to report when garage is closed (and not in transition). Defaults to "CLOSED".

## Channels

| channel              | type          | description                                                                           |
|----------------------|---------------|---------------------------------------------------------------------------------------|
| distance             | Number:Length | Distance reading from the OpenGarage controller (default in cm)                       |
| status-switch        | Switch        | Door status (OFF = Closed, ON = Open), set "invert=true" on channel to invert switch  |
| status-text          | String        | Text status of the current door state, including transition, using values from configuration: doorOpeningState, doorOpenState, doorClosingState, doorClosedState.                          |
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
String OpenGarage_StatusText { channel="opengarage:opengarage:OpenGarage:status-text" }
```

opengarage.sitemap:

```perl
Text item=OpenGarage_StatusText label="Status"
Switch item=OpenGarage_Status icon="garagedoorclosed" mappings=[ON=Open]  visibility=[OpenGarage_Status == OFF]
Switch item=OpenGarage_Status icon="garagedooropen"   mappings=[OFF=Close] visibility=[OpenGarage_Status == ON]
Switch item=OpenGarage_Status icon="garage" 
Contact item=OpenGarage_Status_Contact icon="garage" 
Rollershutter item=OpenGarage_Status_Rollershutter icon="garage" 
Text item=OpenGarage_Distance label="OG distance"
Text item=OpenGarage_Vehicle label="Vehicle Presence"

```

## Adding to HomeKit

If you have the HomeKit extension installed, you can control your OpenGarage instance via your iPhone.
To wire it up to HomeKit, you might specify the following:

opengarage.items

```
Group  gOpenGarage                      "OpenGarage Door"                                              {homekit="GarageDoorOpener"}
Switch OpenGarage_TargetState "Target state" (gOpenGarage) {homekit="GarageDoorOpener.TargetDoorState", channel="opengarage:opengarage:deadbeef:status-switch"}
String OpenGarage_CurrentState "Current state" (gOpenGarage) {homekit="GarageDoorOpener.CurrentDoorState", channel="opengarage:opengarage:deadbeef:status-text"}
Switch OpenGarage_xxObstruction "Obstruction (do not use)" (gOpenGarage) {homekit="GarageDoorOpener.ObstructionStatus"}
```

The obstruction channel is not bound to any channel.
It's needed because HomeKit requires it, and OpenGarage does not provide it.
HomeKit requires a status for the garage door of `OPEN`, `CLOSED`, `CLOSING`, `OPENING`.
In order to report that, we must provide state transition information.
State transition information is inferred when the garage door state is changed.
For `doorTransitionTimeSeconds` since the last open/close command was issued, the binding reports the state as either "closing" or "opening".
