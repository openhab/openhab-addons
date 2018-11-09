# Dresden Elektronik deCONZ Binding

The Zigbee binding currently does not support the Dresden Elektronik Raspbee and Conbee Zigbee dongles.
The manufacturer provides a companion app called deCONZ together with the mentioned hardware. deCONZ
offers a documented real-time channel that this binding makes use of to bring support for all
paired Zigbee sensors and switches.

deCONZ also acts as a HUE bridge. This binding is meant to be used together with the HUE binding
which makes the lights and plugs available.

## Supported Things

There is one bridge (`deconz`) that manages the connection to the deCONZ software instance.
These things are supported:

| Think name            | Description                     | Channels                    |
| :-------------------- |:--------------------------------|:---------------------------:|
| `presencesensor`    | Any presence sensor             | `presence`                 |
| `powersensor`       | Any power sensor                | `power`                    |
| `switch`             | Any switch                      | `buttonevent`             |
| `daylightsensor`    | The deCONZ artificial daylight sensor | `value`,`light`      |

## Discovery

deCONZ software instances are discovered automatically in the same subnet.
Sensors, switches are discovered as soon as a `deconz` bridge Thing comes online.

## Thing Configuration

Important: You need to authorize 3rd party applications in deCONZ for the bridge Thing to successfully connect.
The Online/Offline status of the bridge Thing will tell you, when to enable the authorization process.

These configuration values need to be provided:


| Config                | Description                                           | Type  | Default |
| :-------------------- |:------------------------------------------------------|:-----:|:-------:|
| ip                    | Host address (hostname/ip:port) of deCONZ interface   |string | n/a     |
| apikey                | Authorization API key (optional, can be filled automatically) |string | n/a     |

## Channels

Overview of provided channels for `presencesensor`:

| Channel ID                |  Description                       | Read/Write | Values               |
| :------------------------- | :-----------------------------------|:-:|:----------------------:|
| presence                     | Contact type                     |r| OPEN/CLOSE for presence/no-presence              |

Overview of provided channels for `powersensor`:

| Channel ID                |  Description                       | Read/Write | Values               |
| :------------------------- | :-----------------------------------|:-:|:----------------------:|
| power                     | Current power usage in Watts    |r| Number value             |

Overview of provided channels for `daylightsensor`:

| Channel ID                |  Description                       | Read/Write | Values               |
| :------------------------- | :-----------------------------------|:-:|:----------------------:|
| value                     |A number that represents the sun position: Dawn is around 130, sunrise at 140, sunset at 190, and dusk at 210   |r| Number value             |
| light                     |A light level   |r| Daylight,Sunset,Dark           |

## Full Example

### Things file ###

```
Bridge deconz:deconz:homeserver [ ip="192.168.1.3" ] {
    presencesensor livingroom  [ ]
}
```

### Items file ###

```
Contact presence                   "Current state: [%s]"   {channel="deconz:deconz:presencesensor:presence"}
```

