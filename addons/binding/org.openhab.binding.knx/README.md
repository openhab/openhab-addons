
# KNX Binding

The openHAB KNX binding allows to connect to [KNX Home Automation](http://www.knx.org/) installations. Switching lights on and off, activating your roller shutters or changing room temperatures are only some examples.

To access your KNX bus you either need an KNX IP gateway (like e.g. the [Gira KNX IP Router](http://www.gira.com/en/gebaeudetechnik/systeme/knx-eib_system/knx-produkte/systemgeraete/knx-ip-router.html)) or a PC running [KNXD](https://github.com/knxd/knxd) (free open source component that enables communication with the KNX bus), which is the successor of the no longer maintained [EIBD](http://www.auto.tuwien.ac.at/~mkoegler/index.php/eibd).

## Supported Things

The KNX binding supports two kinds of Bridges, and two kinds of Things to access actors on the KNX bus. There is an *ip* bridge to connect to KNX IP Gateways, and a *serial* bridge for connection over a serial port to a host-attached gateway. The configuration of the two kinds of bridges is very similar (see below).

## Binding Configuration

The binding itself does not require any special configuration. 

## Bridge Configuration

### IP Gateway

The IP Gateway is the most commonly used way to connect to the KNX bus. At its base, the *ip* bridge accepts the following configuration parameters:

| Name                | Required | Description                                                                                                  | Default value                                    |
|---------------------|----------|--------------------------------------------------------------------------------------------------------------|--------------------------------------------------|
| ipConnectionType    | Y        | The ip connection type for connecting to the KNX bus. Could be either TUNNEL or ROUTER                       |                                                  |
| ipAddress           | N        | Network address of the KNX/IP gateway                                                                        | Not required if ipConnectionType is ROUTER       |
| portNumber          | N        | Port number of the KNX/IP gateway                                                                            | Not required if ipConnectionType is ROUTER; 3671 |
| localIp             | N        | Network address of the local host to be used to set up the connection to the KNX/IP gateway                  |                                                  |
| localSourceAddr     | N        | The group address for identification of this KNX/IP gateway within the KNX bus                               | 0.0.0                                            |
| readingPause        | N        | Time in milliseconds of how long should be paused between two read requests to the bus during initialization | 50                                               |
| responseTimeout     | N        | Timeout in seconds to wait for a response from the KNX bus                                                   | 10                                               |
| readRetriesLimit    | N        | Limits the read retries while initialization from the KNX bus                                                | 3                                                |
| autoReconnectPeriod | N        | Seconds between connect retries when KNX link has been lost, 0 means never retry                             | 0                                                |
| enableDiscovery     | N        | Enable or disable automatic Individual Address discovery                                                     | true                                             |
| useNAT              | Y        | Whether there is network address translation between the server and the gateway                              | false                                            |

### Serial Gateway

The *serial* bridge accepts the following configuration parameters:

| Name                | Required | Description                                                                                                  | Default value |
|---------------------|----------|--------------------------------------------------------------------------------------------------------------|---------------|
| serialPort          | Y        | The serial port to use for connecting to the KNX bus                                                         |               |
| readingPause        | N        | Time in milliseconds of how long should be paused between two read requests to the bus during initialization | 50            |
| responseTimeout     | N        | Timeout in seconds to wait for a response from the KNX bus                                                   | 10            |
| readRetriesLimit    | N        | Limits the read retries while initialization from the KNX bus                                                | 3             |
| autoReconnectPeriod | N        | Seconds between connect retries when KNX link has been lost, 0 means never retry                             | 0             |
| enableDiscovery     | N        | Enable or disable automatic Individual Address discovery                                                     | true          |

## Thing Configuration

### *basic* Things

*basic* Things are wrappers around an arbitrary group addresses on the KNX bus. They have no specific function in the KNX binding, except that if the *address* is defined the binding will actively poll the Individual Address on the KNX bus to detect that the KNX actor is reachable. Under normal real world circumstances, either all devices on a bus are reachable, or the entire bus is down. When *fetch* is set to true, the binding will read-out the memory of the KNX actor in order to detect configuration data and so forth. This is however an experimental feature very prone to the actual on the KNX bus. 

|Name|Required|Description|Default value|
|----|--------|-----------|-------------|
|address|N|The individual address in x.y.z notation|Required if *fetch* is set to true|
|fetch|N|Read out the device parameters and address/communication object tables|false|


## Channels

The Bridges support the following channels:

| Channel Type ID | Item Type | Description                                                                      |
|-----------------|-----------|----------------------------------------------------------------------------------|
| errorsall       | Number    | The number of errors that occurred on the KNX bus since the start of the binding |
| errors5min      | Number    | The number of errors that occurred on the KNX bus during the last 5 minutes      |

### *basic* Thing Channels of Things defined by the end-user

Different kinds of Channels are defined and can be used to group together Group Addresses. All channel types share two configuration parameters: *read*, an optional parameter to indicate if the 'readable' group addresses of that Channel should be read at startup (default: false), and *interval*, an optional parameter that defines an interval between attempts to read the status group address on the bus, in seconds. When defined and set to 0, the interval is ignored (default: 0)

For specific configurations reference is made to the *basic.xml* ESH-INF file of the source distribution. For example, the *statusswitch* Channel type defines two configuration parameters: *switchGA*, the group address in x/y/z notation to toggle the switch, and *statusGA*, the group address in x/y/z notation to read the switch status. Since it is about a switch, the DPT does not have to be defined, e.g. it is supposed to be 1.001. For example, the *number* Channel type is used to define Channels to read/write ordinary Numbers. 

```xtend
Thing basic someactor {
    Type statusswitch :  someswitch [ switchGA="2/4/99", statusGA="2/4/100", read=true, interval=3600]
    Type number : ga_2_4_101 [ groupaddress="2/4/101", dpt="9.001", read=true, interval=3600]
}
```

## Full Example

demo.Things:

```xtend
Bridge knx:ip:ip1 [ ipAddress="192.168.0.10", portNumber=3671, localIp="192.168.0.166", ipConnectionType="TUNNEL", readingPause=50, responseTimeout=10, readRetriesLimit=3, autoReconnectPeriod=1,localSourceAddr="0.0.0"] {
    Thing basic someactor {
        Type statusswitch :  someswitch [ switchGA="2/4/99", statusGA="2/4/100", read=true, interval=3600]
        Type number : ga_2_4_101 [ groupaddress="2/4/101", dpt="9.001", read=true, interval=3600]
    }
}
```

demo.items:

```xtend
Switch someSwitchItem "Manually Defined Switch" <switch> { channel="knx:basic:ip1:someactor:someswitch" }
```


## Supported Datapoint Types

The KNX binding supports a limited set of Datapoint types (DPTs). If your thing configuration contains a DPT that is not supported by the KNX binding, an error will be logged ("DPT n.nnn is not supported by the KNX binding").

To get an overview of the supported DPTs, it's best to look into the source code of the KNX binding and the library it depends on. The DPTs for the binding are defined in [KNXCoreTypeMapper](https://github.com/openhab/openhab/blob/master/bundles/binding/org.openhab.binding.knx/src/main/java/org/openhab/binding/knx/internal/dpt/KNXCoreTypeMapper.java). The constants (and their mapping to DPTs) are defined in the library [calimero](https://github.com/calimero-project/calimero/tree/master/src/tuwien/auto/calimero/dptxlator).

## KNX Logging

Since version 1.5.0 of this binding, it is possible to capture log events from calimero. These log events contain detailed information from the KNX bus (what is written to the bus, what gets read from the bus, ...)

To enable this logging, the following line has to be added to `logback.xml`:

    <logger name="tuwien.auto.calimero" level="DEBUG" />
