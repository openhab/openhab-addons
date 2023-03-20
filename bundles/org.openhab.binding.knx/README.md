# KNX Binding

The openHAB KNX binding allows to connect to [KNX Home Automation](https://www.knx.org/) installations.
Switching lights on and off, activating your roller shutters, or changing room temperatures are only some examples.

To access your KNX bus, you either need a gateway device which is connected to the KNX bus and allows computers to access the bus communication.
This can be either an Ethernet (as a Router or a Tunnel type) or a serial gateway.
The KNX binding then can communicate directly with this gateway.
Alternatively, a PC running [KNXD](https://github.com/knxd/knxd) (free open source component software) can be put in between which then acts as a broker allowing multiple client to connect to the same gateway.
Since the protocol is identical, the KNX binding can also communicate with it transparently.

***Attention:*** With the introduction of Unit of Measurement (UoM) support, some data types have changed (see `number` channel below):

- Data type for DPT 5.001 (Percent 8bit, 0 -> 100%) has changed from `PercentType` to `QuantityType`for `number` channels (`dimmer`, `color`, `rollershutter` channels stay with `PercentType`).
- Data type for DPT 5.004 (Percent 8bit, 0 -> 255%) has changed from `PercentType` to `QuantityType`.
- Data type for DPT 6.001 (Percent 8bit -128 -> 127%) has changed from `PercentType` to `QuantityType`.
- Data type for DPT 9.007 (Humidity) has changed from `PercentType` to `QuantityType`.

Rules that check for or compare states and transformations that expect a raw value might need adjustments.
If you run into trouble with that and need some time, you can disable UoM support on binding level via the `disableUoM` parameter.
UoM are enabled by default and need to be disabled manually.
A new setting is activated immediately without restart.

## Supported Things

The KNX binding supports two types of bridges, and one type of things to access the KNX bus.
There is an _ip_ bridge to connect to KNX IP Gateways, and a _serial_ bridge for connection over a serial port to a host-attached gateway.

## Bridges

The following two bridge types are supported.
Bridges don't have channels on their own.

### IP Gateway

The IP Gateway is the most commonly used way to connect to the KNX bus. At its base, the _ip_ bridge accepts the following configuration parameters:

| Name                | Required     | Description                                                                                                  | Default value                                        |
|---------------------|--------------|--------------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| type                | Yes          | The IP connection type for connecting to the KNX bus (`TUNNEL`, `ROUTER`, `SECURETUNNEL` or `SECUREROUTER`)  | -                                                    |
| ipAddress           | for `TUNNEL` | Network address of the KNX/IP gateway. If type `ROUTER` is set, the IPv4 Multicast Address can be set.       | for `TUNNEL`: \<nothing\>, for `ROUTER`: 224.0.23.12 |
| portNumber          | for `TUNNEL` | Port number of the KNX/IP gateway                                                                            | 3671                                                 |
| localIp             | No           | Network address of the local host to be used to set up the connection to the KNX/IP gateway                  | the system-wide configured primary interface address |
| localSourceAddr     | No           | The (virtual) individual address for identification of this openHAB Thing within the KNX bus <br/><br/>Note: Use a free address, not the one of the interface. Or leave it at `0.0.0` and let openHAB decide which address to use. When using knxd, make sure _not to use_ one of the addresses reserved for tunneling clients.  | 0.0.0                                                |
| useNAT              | No           | Whether there is network address translation between the server and the gateway                              | false                                                |
| readingPause        | No           | Time in milliseconds of how long should be paused between two read requests to the bus during initialization | 50                                                   |
| responseTimeout     | No           | Timeout in seconds to wait for a response from the KNX bus                                                   | 10                                                   |
| readRetriesLimit    | No           | Limits the read retries while initialization from the KNX bus                                                | 3                                                    |
| autoReconnectPeriod | No           | Seconds between connect retries when KNX link has been lost (0 means never).                                 | 0                                                    |
| routerBackboneKey   | No           | KNX secure: Backbone key for secure router mode                                                              | -                                                    |
| tunnelUserId        | No           | KNX secure: Tunnel user id for secure tunnel mode (if specified, it must be a number >0)                     | -                                                    |
| tunnelUserPassword  | No           | KNX secure: Tunnel user key for secure tunnel mode                                                           | -                                                    |
| tunnelDeviceAuthentication  | No   | KNX secure: Tunnel device authentication for secure tunnel mode                                              | -                                                    |

### Serial Gateway

The _serial_ bridge accepts the following configuration parameters:

| Name                | Required | Description                                                                                                  | Default value |
|---------------------|----------|--------------------------------------------------------------------------------------------------------------|---------------|
| serialPort          | Y        | The serial port to use for connecting to the KNX bus                                                         | -             |
| readingPause        | N        | Time in milliseconds of how long should be paused between two read requests to the bus during initialization | 50            |
| responseTimeout     | N        | Timeout in seconds to wait for a response from the KNX bus                                                   | 10            |
| readRetriesLimit    | N        | Limits the read retries while initialization from the KNX bus                                                | 3             |
| autoReconnectPeriod | N        | Seconds between connect retries when KNX link has been lost, 0 means never retry                             | 0             |
| useCemi             | N        | Use newer CEMI message format, useful for newer devices like KNX RF sticks, kBerry, etc.                     | false         |

## Things

### _device_ Things

_basic_ Things are wrappers around arbitrary group addresses on the KNX bus.
They have no specific function in the KNX binding, except that if the _address_ is defined, the binding will actively poll the Individual Address on the KNX bus to detect that the KNX actuator is reachable.
Under normal real-world circumstances, either all devices on a bus are reachable, or the entire bus is down.
If line couplers are installed, physical device addressing might be filtered; in this case please do not specify the addresses for devices on this line.
When _fetch_ is set to true, the binding will read-out the memory of the KNX actuator in order to detect configuration data and so forth.
This is however an experimental feature, very prone to the actual on the KNX bus.

| Name         | Required | Description                                                                                                              | Default value                                                               |
|--------------|----------|--------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| address      | N        | The individual device address (in 0.0.0 notation)                                                                        | -  |
| fetch        | N        | Read out the device parameters and address/communication object tables (requires the address)                            | false                                                                       |
| pingInterval | N        | Interval (in seconds) to contact the device and set the thing status based on the result (requires the address)          | 600                                                                         |
| readInterval | N        | Interval (in seconds) to actively request reading of values from the bus (0 if they should only be read once at startup) | 0                                                                           |

Different kinds of channels are defined and can be used to group together Group Addresses.
All channels of a device share one configuration parameter defined on device level: _readInterval_, an optional parameter which indicates if 'readable' group addresses of that Channel should be read periodically at the given interval, in seconds.
'Readable' group addresses are marked with an `<` in the group address definition of a Channel, see below.
All readable group addresses are queried by openHAB during startup.
If readInterval is not specified or set to 0, no further periodic reading will be triggered (default: 0).

#### Channel Types

Standard channels are used most of the time.
They are used in the common case where the physical state is owned by a device within the KNX bus, e.g. by a switch actuator who "knows" whether the light is turned on or off, or by a temperature sensor which reports the room temperature regularly.

Control channel types (suffix `-control`) are used for cases where the KNX bus does not own the physical state of a device.
This could be the case if e.g. a lamp from another binding should be controlled by a KNX wall switch.
When a `GroupValueRead` telegram is sent from the KNX bus to a *-control Channel, the bridge responds with a `GroupValueResponse` telegram to the KNX bus.

##### Channel Type `color`, `color-control`

| Parameter        | Description                            | Default DPT |
|------------------|----------------------------------------|-------------|
| hsb              | Group address for the color            | 232.600     |
| switch           | Group address for the binary switch    | 1.001       |
| position         | Group address brightness               | 5.001       |
| increaseDecrease | Group address for relative brightness  | 3.007       |

The `hsb` address supports DPT 242.600 and 251.600.

Some RGB/RGBW products (e.g. MDT) support HSB values for DPT 232.600 instead of RGB.
This is supported as "vendor-specific DPT" with a value of 232.60000.

##### Channel Type `contact`, `contact-control`

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 1.009       |

*Attention:* Due to a bug in the original implementation, the states for DPT 1.009 are inverted (i.e. `1` is mapped to `OPEN` instead of `CLOSE`).
A change would break all existing installations and is therefore not implemented.

##### Channel Type `datetime`, `datetime-control`

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 19.001      |

##### Channel Type `dimmer`, `dimmer-control`

| Parameter        | Description                            | Default DPT |
|------------------|----------------------------------------|-------------|
| switch           | Group address for the binary switch    | 1.001       |
| position         | Group address of the absolute position | 5.001       |
| increaseDecrease | Group address for relative movement    | 3.007       |

##### Channel Type `number`, `number-control` 

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 9.001       |

Note: The `number` channel has full support for Units Of Measurement (UoM).

Using the UoM feature of openHAB (QuantityType) requires that the DPT value is set correctly.
Automatic type conversion will be applied if required.

Incoming values from the KNX bus are converted to values with units (e.g. `23 °C`).
If the channel is linked to the correct item-type (`Number:Temperature` in this case) the display unit can be controlled by item metadata (e.g. `%.1f °F` for 1 digit of precision in Fahrenheit).
The unit is stripped if the channel is linked to a plain number item (type `Number`). 

Outgoing values with unit are first converted to the unit associated with the DPT (e.g. a value of `10 °F` is converted to `-8.33 °C` if the channel has DPT 9.001).
Values from plain number channels are sent as-is (without any conversion).

##### Channel Type `rollershutter`, `rollershutter-control`

| Parameter | Description                             | Default DPT |
|-----------|-----------------------------------------|-------------|
| upDown    | Group address for relative movement     | 1.008       |
| stopMove  | Group address for stopping              | 1.010       |
| position  | Group address for the absolute position | 5.001       |

##### Channel Type `string`, `string-control`

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 16.001      |

##### Channel Type `switch`, `switch-control`

| Parameter | Description                         | Default DPT |
|-----------|-------------------------------------|-------------|
| ga        | Group address for the binary switch | 1.001       |

#### Control Channel Types

In contrast to the standard channels above, the control channel types are used for cases where the KNX bus does not own the physical state of a device.
This could for example be the case if a lamp from another binding should be controlled by a KNX wall switch.
When a `GroupValueRead` telegram is sent from the KNX bus to a *-control Channel, the bridge responds with a `GroupValueResponse` telegram to the KNX bus.

##### Channel Type "switch-control"

| Parameter | Description                         | Default DPT |
|-----------|-------------------------------------|-------------|
| ga        | Group address for the binary switch | 1.001       |

##### Channel Type "dimmer-control"

| Parameter        | Description                                                                                                                                   | Default DPT |
|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|-------------|
| switch           | Group address for the binary switch                                                                                                           | 1.001       |
| position         | Group address of the absolute position                                                                                                        | 5.001       |
| increaseDecrease | Group address for relative movement                                                                                                           | 3.007       |
| frequency        | Increase/Decrease frequency in milliseconds in case the binding should handle that (0 if the KNX device sends the commands repeatedly itself) | 0           |


##### Channel Type "rollershutter-control"

| Parameter | Description                             | Default DPT |
|-----------|-----------------------------------------|-------------|
| upDown    | Group address for relative movement     | 1.008       |
| stopMove  | Group address for stopping              | 1.010       |
| position  | Group address for the absolute position | 5.001       |

##### Channel Type "contact-control"

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 1.009       |

*Attention:* Due to a bug in the original implementation, the states for DPT 1.009 are inverted (i.e. `1` is mapped to `OPEN` instead of `CLOSE`).
A change would break all existing installations and is therefore not implemented.

##### Channel Type "number-control"

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 9.001       |

For UoM support see the explanations of the `number` channel.

##### Channel Type "string-control"

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 16.001      |

##### Channel Type "datetime-control"

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 19.001      |

#### Group Address Notation

```text
<config>="[<dpt>:][<]<mainGA>[[+[<]<listeningGA>][+[<]<listeningGA>..]]"
```

where parts in brackets `[]` denote optional information.

The optional `<` sign tells whether the group address of the datapoint accepts read requests on the KNX bus (it does, if the sign is there).
All group addresses marked with `<` are read by openHAB during startup.
With `*-control` channels, the state is not owned by any device on the KNX bus, therefore no read requests will be sent by the binding, i.e. `<` signs will be ignored for them.

Each configuration parameter has a `mainGA` where commands are written to and optionally several `listeningGA`s.

The `dpt` element is optional. If omitted, the corresponding default value will be used (see the channel descriptions above).

## KNX Secure

> NOTE: Support for KNX Secure is partly implemented for openHAB and should be considered as experimental.

### KNX IP Secure

KNX IP Secure protects the traffic between openHAB and your KNX installation.
It **requires a KNX Secure Router or a Secure IP Interface** and a KNX installation **with security features enabled in ETS tool**.

For _Secure routing_ mode, the so called `backbone key` needs to be configured in openHAB.
It is created by the ETS tool and cannot be changed via the ETS user interface.

- The backbone key can be extracted from Security report (ETS, Reports, Security, look for a 32-digit key) and specified in parameter `routerBackboneKey`.

For _Secure tunneling_ with a Secure IP Interface (or a router in tunneling mode), more parameters are required.
A unique device authentication key, and a specific tunnel identifier and password need to be available.

- All information can be looked up in ETS and provided separately: `tunnelDeviceAuthentication`, `tunnelUserPassword`. `tunnelUserId` is a number which is not directly visible in ETS, but can be looked up in keyring export or deduced (typically 2 for the first tunnel of a device, 3 for the second one, ...). `tunnelUserPasswort` is set in ETS in the properties of the tunnel (below the IP interface you will see the different tunnels listed) denoted as "Password". `tunnelDeviceAuthentication` is set in the properties of the IP interface itself, check for a tab "IP" and a description "Authentication Code".

### KNX Data Secure

KNX Data Secure protects the content of messages on the KNX bus. In a KNX installation, both classic and secure group addresses can coexist.
Data Secure does _not_ necessarily require a KNX Secure Router or a Secure IP Interface, but a KNX installation with newer KNX devices which support Data Secure and with **security features enabled in ETS tool**.

> NOTE: **openHAB currently ignores messages with secure group addresses.**

## Examples

The following two templates are sufficient for almost all purposes.
Only add parameters to the Bridge and Thing configuration if you know exactly what functionality it is needed for.

### Type ROUTER mode configuration Template

knx.things:

```java
Bridge knx:ip:bridge [
    type="ROUTER",
    autoReconnectPeriod=60 //optional, do not set <30 sec.
] {
    Thing device knx_device "knx_device_name" @ "knx_device_group" [
        //readInterval=3600 //optional, only used if reading values are present
    ] {
        //Items configurations
    }
}
```

### Type TUNNEL mode configuration Template

knx.things:

```java
Bridge knx:ip:bridge [
    type="TUNNEL",
    ipAddress="192.168.0.111",
    autoReconnectPeriod=60 //optional, do not set <30 sec.
] {
    Thing device knx_device "knx_device_name" @ "knx_device_group" [
        //readInterval=3600 //optional, only used if reading values are present
    ] {
        //Items configurations
    }
}
```

### Full Example

```java
//TUNNEL
Bridge knx:ip:bridge [
    type="TUNNEL",
    ipAddress="192.168.0.10",
    portNumber=3671,
    localIp="192.168.0.11",
    readingPause=50,
    responseTimeout=10,
    readRetriesLimit=3,
    autoReconnectPeriod=60,
    localSourceAddr="0.0.0"
] {
    Thing device generic [
        address="1.2.3",
        fetch=true,
        pingInterval=300,
        readInterval=3600
    ] {
        Type switch        : demoSwitch        "Light"       [ ga="3/0/4+<3/0/5" ]
        Type color         : demoColorLight    "Color"       [ hsb="6/0/10+<6/0/11", switch="6/0/12+<6/0/13", position="6/0/14+<6/0/15", increaseDecrease="6/0/16+<6/0/17" ]
        Type rollershutter : demoRollershutter "Shade"       [ upDown="4/3/50+4/3/51", stopMove="4/3/52+4/3/53", position="4/3/54+<4/3/55" ]
        Type contact       : demoContact       "Door"        [ ga="1.019:<5/1/2" ]
        Type number        : demoTemperature   "Temperature" [ ga="9.001:<5/0/0" ]
        Type dimmer        : demoDimmer        "Dimmer"      [ switch="5/0/0", position="5/0/2+<5/0/3", increaseDecrease="5/0/4" ]
        Type string        : demoString        "Message"     [ ga="5/3/1" ]
        Type datetime      : demoDatetime      "Alarm"       [ ga="5/5/42" ]
    }
}

//ROUTER
Bridge knx:ip:bridge [
    type="ROUTER",
    ipAddress="224.0.23.12",
    portNumber=3671,
    localIp="192.168.0.11",
    readingPause=50,
    responseTimeout=10,
    readRetriesLimit=3,
    autoReconnectPeriod=60,
    localSourceAddr="0.0.0"
] {}
```

knx.items:

```java
Switch              demoSwitch         "Light [%s]"               <light>          { channel="knx:device:bridge:generic:demoSwitch" }
Color               demoColorLight     "Color [%s]"               <light>          { channel="knx:device:bridge:generic:demoColorLight" }
Dimmer              demoDimmer         "Dimmer [%d %%]"           <light>          { channel="knx:device:bridge:generic:demoDimmer" }
Rollershutter       demoRollershutter  "Shade [%d %%]"            <rollershutter>  { channel="knx:device:bridge:generic:demoRollershutter" }
Contact             demoContact        "Front Door [%s]"          <frontdoor>      { channel="knx:device:bridge:generic:demoContact" }
Number:Temperature  demoTemperature    "Temperature [%.1f °C]"    <temperature>    { channel="knx:device:bridge:generic:demoTemperature" }
String              demoString         "Message of the day [%s]"                   { channel="knx:device:bridge:generic:demoString" }
DateTime            demoDatetime       "Alarm [%1$tH:%1$tM]"                       { channel="knx:device:bridge:generic:demoDatetime" }
```

knx.sitemap:

```perl
sitemap knx label="KNX Demo Sitemap" {
  Frame label="Demo Elements" {
    Switch      item=demoSwitch
    Slider      item=demoDimmer
    Colorpicker item=demoColorLight
    Default     item=demoRollershutter
    Text        item=demoContact
    Text        item=demoTemperature
    Text        item=demoString
    Text        item=demoDatetime
  }
}

```

### Control Example

control.things:

```java
Bridge knx:serial:bridge [
    serialPort="/dev/ttyAMA0",
    readingPause=50,
    responseTimeout=10,
    readRetriesLimit=3,
    autoReconnectPeriod=60
] {
    Thing device generic {
        Type switch-control        : controlSwitch        "Control Switch"        [ ga="3/3/10+<3/3/11" ]   // '<'  signs are allowed but will be ignored for control Channels
        Type dimmer-control        : controlDimmer        "Control Dimmer"        [ switch="3/3/50+3/3/48", position="3/3/46", increaseDecrease="3/3/49", frequency=300 ]
        Type color                 : controlColorLight    "Color"                 [ hsb="6/0/10", switch="6/0/12", position="6/0/14", 
        Type rollershutter-control : controlRollershutter "Control Rollershutter" [ upDown="3/4/1+3/4/2", stopMove="3/4/3", position="3/4/4" ]
        Type number-control        : controlNumber        "Control Number"        [ ga="1/2/2" ]
        Type string-control        : controlString        "Control String"        [ ga="1/4/2" ]
        Type datetime-control      : controlDatetime      "Control Datetime"      [ ga="5/1/30" ]
    }
}

Bridge hue:bridge:bridge "Philips Hue Bridge" [
    ipAddress="...",
    userName="..."
] {
    Thing 0210 1 "Color Lamp" [ lightId="1" ]
}
```

knx.items:

```java
Switch        demoSwitch         "Light [%s]"               <light>          { channel="hue:0210:bridge:1:color", channel="knx:device:bridge:generic:controlSwitch" }
Dimmer        demoDimmer         "Dimmer [%d %%]"           <light>          { channel="hue:0210:bridge:1:color", channel="knx:device:bridge:generic:controlDimmer" }
```
