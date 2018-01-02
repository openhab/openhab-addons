
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
| ipAddress           | for `TUNNEL` | Network address of the KNX/IP gateway                                                                        | Required if type is ROUTER                           |
| portNumber          | for `TUNNEL` | Port number of the KNX/IP gateway                                                                            | Not required if type is ROUTER; 3671                 |
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

*basic* Things are wrappers around an arbitrary group addresses on the KNX bus. They have no specific function in the KNX binding, except that if the *address* is defined the binding will actively poll the Individual Address on the KNX bus to detect that the KNX actor is reachable. Under normal real world circumstances, either all devices on a bus are reachable, or the entire bus is down. When *fetch* is set to true, the binding will read-out the memory of the KNX actor in order to detect configuration data and so forth. This is however an experimental feature very prone to the actual on the KNX bus. 

| Name         | Required | Description                                                                                                              | Default value                                                               |
|--------------|----------|--------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| address      | N        | The individual device address (in 0.0.0 notation)                                                                        | -  |
| fetch        | N        | Read out the device parameters and address/communication object tables (requires the address)                            | false                                                                       |
| pingInterval | N        | Interval (in seconds) to contact the device and set the thing status based on the result (requires the address)          | 600                                                                         |
| readInterval | N        | Interval (in seconds) to actively request reading of values from the bus (0 if they should only be read once at startup) | 0                                                                           |

Different kinds of channels are defined and can be used to group together Group Addresses. All channel types share two configuration parameters: *read*, an optional parameter to indicate if the 'readable' group addresses of that Channel should be read at startup (default: false), and *interval*, an optional parameter that defines an interval between attempts to read the status group address on the bus, in seconds. When defined and set to 0, the interval is ignored (default: 0)

#### Standard Channels

Standard channels are used most of the time. They are used in the common case where the physical state is owned by a decive within the KNX bus, e.g. by a switch actor who "knows" whether the light is turned on or of or by a temperature sensor which reports the room temperature regularly.

##### Switch

| Parameter | Description | Default DPT |
|----|----|-----|
| switch | Group address | 1.001 | 


##### Dimmer

| Parameter | Description | Default DPT |
|----|----|-----|
| switch | Group address | 1.001 | 
| position | Group address | 5.001 | 
| increaseDecrease | Group address | 3.007 | 

##### Rollershutter

| Parameter | Description | Default DPT |
|----|----|-----|
| upDown | Group address | 1.008 | 
| stopMove | Group address | 1.010 | 
| position | Group address | 5.001 | 

##### Contact

| Parameter | Description | Default DPT |
|----|----|-----|
| ga | Group address | 1.009 | 

##### Number

| Parameter | Description | Default DPT |
|----|----|-----|
| ga | Group address | 9.001 | 

##### String

| Parameter | Description | Default DPT |
|----|----|-----|
| ga | Group address | 16.001 | 

##### Datetime

| Parameter | Description | Default DPT |
|----|----|-----|
| ga | Group address | 19.001 | 


#### Control Channels

In contrast to the standard channels above, the control channel types are used for cases where the KNX bus does not own the physical state of a device. This could be the case if e.g. a lamp from another binding should be controlled by a KNX wall switch.

TODO

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
    tape="TUNNEL", 
    readingPause=50, 
    responseTimeout=10, 
    readRetriesLimit=3, 
    autoReconnectPeriod=1,
    localSourceAddr="0.0.0"
] {
    Thing device magicActor [
        address="1.2.3",
        fetch=true,
        pingInterval=300,
        readInterval=3600
    ] {
        Type number : demoTemperature1            "Demo Temperature 1"       [ ga="9.001:<5/0/0" ]
        Type number : demoTemperature2            "Demo Temperature 2"       [ ga="<5/0/0" ]
        
        Type rollershutter : demoRollershutter    "Demo Rollershutter"       [ upDown="4/3/50+<4/3/51",  stopMove="4/3/52+<4/3/53",  position="4/3/54+<4/3/55" ]
        
        /* TODO: complete */
    }
}
```

knx.items:

```xtend
        /* TODO: complete */
```

knx.sitemap:

```xtend
        /* TODO: complete */
```

### Control Example


control.things:

```xtend
Bridge knx:ip:bridge [ 
    ipAddress="192.168.0.10", 
    portNumber=3671, 
    localIp="192.168.0.11", 
    tape="TUNNEL", 
    readingPause=50, 
    responseTimeout=10, 
    readRetriesLimit=3, 
    autoReconnectPeriod=1,
    localSourceAddr="0.0.0"
] {
    Thing device generic {
        Type switch-control : demoSwitch          "Control Switch"              [ switch="3/3/10+<3/3/11" ]
        Type dimmer-control : controlSwitch       "Control Dimmer"              [ switch="3/3/50+3/3/48", increaseDecrease="3/3/49" ]

        /* TODO: complete */
    }
}
```

control.items:

```xtend
        /* TODO: complete */
```

control.sitemap:

```xtend
        /* TODO: complete */
```
