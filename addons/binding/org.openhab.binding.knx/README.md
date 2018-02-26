
# KNX Binding

The openHAB KNX binding allows to connect to [KNX Home Automation](http://www.knx.org/) installations. Switching lights on and off, activating your roller shutters or changing room temperatures are only some examples.

To access your KNX bus you either need a gateway device which is connected to the KNX bus and allows computers to access the bus communication. This can be either an Ethernet (as a Router or a Tunne type) or a serial gateway. The KNX binding then can communicate directly with this gateway. Alternatively a PC running [KNXD](https://github.com/knxd/knxd) (free open source component sofware) can be put in between which then acts as a broker allowing multiple client to connect to the same gateway. Since the protocol is identical, the KNX binding can also communicate with it transparently.

## Supported Things

The KNX binding supports two types of bridges, and one type of things to access the KNX bus. There is an *ip* bridge to connect to KNX IP Gateways, and a *serial* bridge for connection over a serial port to a host-attached gateway.

## Binding Configuration

The binding itself does not require any special configuration. 

## Bridges

The following two bridge types are supported. Bridges don't have channels on their own.

### IP Gateway

The IP Gateway is the most commonly used way to connect to the KNX bus. At its base, the *ip* bridge accepts the following configuration parameters:

| Name                | Required     | Description                                                                                                  | Default value                                        |
|---------------------|--------------|--------------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| type                | Yes          | The IP connection type for connecting to the KNX bus (`TUNNEL` or `ROUTER`)                                  | -                                                    |
| ipAddress           | for `TUNNEL` | Network address of the KNX/IP gateway                                                                        | -                                                    |
| portNumber          | for `TUNNEL` | Port number of the KNX/IP gateway                                                                            | 3671                                                 |
| localIp             | No           | Network address of the local host to be used to set up the connection to the KNX/IP gateway                  | the system-wide configured primary interface address |
| localSourceAddr     | No           | The group address for identification of this KNX/IP gateway within the KNX bus                               | 0.0.0                                                |
| useNAT              | No           | Whether there is network address translation between the server and the gateway                              | false                                                |
| readingPause        | No           | Time in milliseconds of how long should be paused between two read requests to the bus during initialization | 50                                                   |
| responseTimeout     | No           | Timeout in seconds to wait for a response from the KNX bus                                                   | 10                                                   |
| readRetriesLimit    | No           | Limits the read retries while initialization from the KNX bus                                                | 3                                                    |
| autoReconnectPeriod | No           | Seconds between connect retries when KNX link has been lost (0 means never).                                 | 0                                                    |


### Serial Gateway

The *serial* bridge accepts the following configuration parameters:

| Name                | Required | Description                                                                                                  | Default value |
|---------------------|----------|--------------------------------------------------------------------------------------------------------------|---------------|
| serialPort          | Y        | The serial port to use for connecting to the KNX bus                                                         | -             |
| readingPause        | N        | Time in milliseconds of how long should be paused between two read requests to the bus during initialization | 50            |
| responseTimeout     | N        | Timeout in seconds to wait for a response from the KNX bus                                                   | 10            |
| readRetriesLimit    | N        | Limits the read retries while initialization from the KNX bus                                                | 3             |
| autoReconnectPeriod | N        | Seconds between connect retries when KNX link has been lost, 0 means never retry                             | 0             |

## Things

### *device* Things

*basic* Things are wrappers around an arbitrary group addresses on the KNX bus. They have no specific function in the KNX binding, except that if the *address* is defined the binding will actively poll the Individual Address on the KNX bus to detect that the KNX actuator is reachable. Under normal real world circumstances, either all devices on a bus are reachable, or the entire bus is down. When *fetch* is set to true, the binding will read-out the memory of the KNX actuator in order to detect configuration data and so forth. This is however an experimental feature very prone to the actual on the KNX bus. 

| Name         | Required | Description                                                                                                              | Default value                                                               |
|--------------|----------|--------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| address      | N        | The individual device address (in 0.0.0 notation)                                                                        | -  |
| fetch        | N        | Read out the device parameters and address/communication object tables (requires the address)                            | false                                                                       |
| pingInterval | N        | Interval (in seconds) to contact the device and set the thing status based on the result (requires the address)          | 600                                                                         |
| readInterval | N        | Interval (in seconds) to actively request reading of values from the bus (0 if they should only be read once at startup) | 0                                                                           |

Different kinds of channels are defined and can be used to group together Group Addresses. All channel types share two configuration parameters: *read*, an optional parameter to indicate if the 'readable' group addresses of that Channel should be read at startup (default: false), and *interval*, an optional parameter that defines an interval between attempts to read the status group address on the bus, in seconds. When defined and set to 0, the interval is ignored (default: 0)

#### Standard Channels

Standard channels are used most of the time. They are used in the common case where the physical state is owned by a decive within the KNX bus, e.g. by a switch actuator who "knows" whether the light is turned on or of or by a temperature sensor which reports the room temperature regularly.

##### Switch

| Parameter | Description                         | Default DPT |
|-----------|-------------------------------------|-------------|
| switch    | Group address for the binary switch | 1.001       |


##### Dimmer

| Parameter        | Description                            | Default DPT |
|------------------|----------------------------------------|-------------|
| switch           | Group address for the binary switch    | 1.001       |
| position         | Group address of the absolute position | 5.001       |
| increaseDecrease | Group address for relative movement    | 3.007       |

##### Rollershutter

| Parameter | Description                             | Default DPT |
|-----------|-----------------------------------------|-------------|
| upDown    | Group address for relative movement     | 1.008       |
| stopMove  | Group address for stopping              | 1.010       |
| position  | Group address for the relative position | 5.001       |

##### Contact

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 1.009       |

##### Number

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 9.001       |

##### String

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 16.001      |

##### Datetime

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 19.001      |


#### Control Channels

In contrast to the standard channels above, the control channel types are used for cases where the KNX bus does not own the physical state of a device. This could be the case if e.g. a lamp from another binding should be controlled by a KNX wall switch.

##### Switch-Control

| Parameter | Description                         | Default DPT |
|-----------|-------------------------------------|-------------|
| switch    | Group address for the binary switch | 1.001       |


##### Dimmer-Control

| Parameter        | Description                                                                                                                                   | Default DPT |
|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|-------------|
| switch           | Group address for the binary switch                                                                                                           | 1.001       |
| position         | Group address of the absolute position                                                                                                        | 5.001       |
| increaseDecrease | Group address for relative movement                                                                                                           | 3.007       |
| frequency        | Increase/Decrease frequency in milliseconds in case the binding should handle that (0 if the KNX device sends the commands repeatedly itself) | 0           |

##### Rollershutter-Control

| Parameter | Description                             | Default DPT |
|-----------|-----------------------------------------|-------------|
| upDown    | Group address for relative movement     | 1.008       |
| stopMove  | Group address for stopping              | 1.010       |
| position  | Group address for the relative position | 5.001       |

##### Contact-Control

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 1.009       |

##### Number-Control

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 9.001       |

##### String-Control

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 16.001      |

##### Datetime-Control

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 19.001      |

#### Group Address Notation

```
<config>="[<][<dpt>:]<mainGA>[[+[<]<listeningGA>]+[<]<listeningGA>..]]"
```

where parts in brackets `[]` denote optional information.

The optional `<` sign tells whether the group address of the datapoint accepts read requests on the KNX bus (it does, if the sign is there).

Each configuration parameter has a `mainGA` where commands are written to and optionally several `listeningGA`s.

The `dpt` element is optional. If ommitted, the corresponding default value will be used (see the channel descriptions above).

## Examples

### Full Example

knx.things:

```xtend
Bridge knx:ip:bridge [ 
    ipAddress="192.168.0.10", 
    portNumber=3671, 
    localIp="192.168.0.11", 
    type="TUNNEL", 
    readingPause=50, 
    responseTimeout=10, 
    readRetriesLimit=3, 
    autoReconnectPeriod=1,
    localSourceAddr="0.0.0"
] {
    Thing device generic [
        address="1.2.3",
        fetch=true,
        pingInterval=300,
        readInterval=3600
    ] {
		Type switch        : demoSwitch        "Light"       [ switch="3/0/4+<3/0/5" ]
        Type rollershutter : demoRollershutter "Shade"       [ upDown="4/3/50+4/3/51", stopMove="4/3/52+4/3/53", position="4/3/54+<4/3/55" ]
		Type contact       : demoContact       "Door"        [ ga="1.019:<5/1/2" ]
        Type number        : demoTemperature   "Temperature" [ ga="9.001:<5/0/0" ]
        Type dimmer        : demoDimmer        "Dimmer"      [ switch="5/0/0+<5/0/1", position="5/0/2+<5/0/3", increaseDecrease="5/0/4" ]
        Type string        : demoString        "Message"     [ ga="5/3/1" ]
        Type datetime      : demoDatetime      "Alarm"       [ ga="5/5/42" ]
    }
}
```

knx.items:

```xtend
Switch        demoSwitch         "Light [%s]"               <light>          { channel="knx:device:bridge:generic:demoSwitch" }
Dimmer        demoDimmer         "Dimmer [%d %%]"           <light>          { channel="knx:device:bridge:generic:demoDimmer" }
Rollershutter demoRollershutter  "Shade [%d %%]"            <rollershutter>  { channel="knx:device:bridge:generic:demoRollershutter" }
Contact       demoContact        "Front Door [%s]"          <frontdoor>      { channel="knx:device:bridge:generic:demoContact" }
Number        demoTemperature    "Temperature [%.1f °C]"    <temperature>    { channel="knx:device:bridge:generic:demoTemperature" }
String        demoString         "Message of the day [%s]"                   { channel="knx:device:bridge:generic:demoString" }
DateTime      demoDatetime       "Alarm [%1$tH:%1$tM]"                       { channel="knx:device:bridge:generic:demoDatetime" }
```

knx.sitemap:

```xtend
sitemap knx label="KNX Demo Sitemap" {
  Frame label="Demo Elements" {
    Switch item=demoSwitch
    Switch item=demoRollershutter
    Text   item=demoContact
    Text   item=demoTemperature
    Slider item=demoDimmer
    Text   item=demoString
    Text   item=demoDatetime
  }                
}

```

### Control Example


control.things:

```xtend
Bridge knx:serial:bridge [ 
    serialPort="/dev/ttyAMA0", 
    readingPause=50, 
    responseTimeout=10, 
    readRetriesLimit=3, 
    autoReconnectPeriod=1
] {
    Thing device generic {
        Type switch-control        : demoSwitch           "Control Switch"        [ switch="3/3/10+<3/3/11" ]
        Type dimmer-control        : controlDimmer        "Control Dimmer"        [ switch="3/3/50+3/3/48", position="3/3/46", increaseDecrease="3/3/49", frequency=300 ]
        Type rollershutter-control : controlRollershutter "Control Rollershutter" [ upDown="3/4/1+3/4/2", stopMove="3/4/3", position="3/4/4" ]
        Type number-control        : controlNumber        "Control Number"        [ ga="1/2/2" ]
        Type string-control        : controlString        "Control String"        [ ga="1/4/2" ]
        Type datetime-control      : controlDatetime      "Control Datetime"      [ ga="5/1/30" ]
    }
}
```