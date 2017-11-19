
# KNX Binding

The openHAB KNX binding allows to connect to [KNX Home Automation](http://www.knx.org/) installations. Switching lights on and off, activating your roller shutters or changing room temperatures are only some examples.

To access your KNX bus you either need an KNX IP gateway (like e.g. the [Gira KNX IP Router](http://www.gira.com/en/gebaeudetechnik/systeme/knx-eib_system/knx-produkte/systemgeraete/knx-ip-router.html)) or a PC running [KNXD](https://github.com/knxd/knxd) (free open source component that enables communication with the KNX bus), which is the successor of the no longer maintained [EIBD](http://www.auto.tuwien.ac.at/~mkoegler/index.php/eibd).

## Supported Things

The KNX binding supports two kinds of Bridges, and two kinds of Things to access actors on the KNX bus. There is an *ip* bridge to connect to KNX IP Gateways, and a *serial* bridge for connection over a serial port to a host-attached gateway. The configuration of the two kinds of bridges is very similar (see below).

With respect to accessing actors on the KNX bus, one has different options:

1. Let the KNX binding automatically parse ETS5 generated knxproj files, and define Things automatically based on what is stored in the knxproj file. This will generate a complete set of KNX Thing and Channels 100% in line with what is defined through ETS5. 

2. use a *generic* Thing to represent a physical actor on the bus. In this case, a number of Channels like *switch*, *dimmer*,... can be used to group together one or more Group Addresses that make up the functionality of the given Channel

The KNX binding also sports an autodiscovery feature. The discovery feature will scan the KNX bus for Individual Addresses it sees flying by, and will create a *generic* Thing for each actor it sees. However, because of the dynamic nature of channels (see below), no channels will be added to this Thing. There is experimental code in the binding to read-out the configuration tables of the actor over the bus. Beware, this is not the same as parsing the knxproj file mentioned here above.

## Binding Configuration

The binding itself does not require any special configuration. 

## Bridge Configuration

### IP Gateway

The IP Gateway is the most commonly used way to connect to the KNX bus. At its base, the *ip* bridge accepts the following configuration parameters:

|Name|Required|Description|Default value|
|----|--------|-----------|-------------|
|ipConnectionType|Y|The ip connection type for connecting to the KNX bus. Could be either TUNNEL or ROUTER||
|ipAddress|N|Network address of the KNX/IP gateway|Not required if ipConnectionType is ROUTER|
|portNumber|N|Port number of the KNX/IP gateway|Not required if ipConnectionType is ROUTER; 3671|
|localIp|N|Network address of the local host to be used to set up the connection to the KNX/IP gateway||
|localSourceAddr|N|The group address for identification of this KNX/IP gateway within the KNX bus|0.0.0|
|readingPause|N|Time in milliseconds of how long should be paused between two read requests to the bus during initialization|50|
|responseTimeout|N|Timeout in seconds to wait for a response from the KNX bus|10|
|readRetriesLimit|N|Limits the read retries while initialization from the KNX bus|3|
|autoReconnectPeriod|N|Seconds between connect retries when KNX link has been lost, 0 means never retry|0|
|enableDiscovery|N|Enable or disable automatic Individual Address discovery|true|
|knxProj|N|KNX Project File (knxproj) to parse and add to the Things managed by this Bridge||
|useNAT|Y|Whether there is network address translation between the server and the gateway|false|

### Serial Gateway

The *serial* bridge accepts the following configuration parameters:

|Name|Required|Description|Default value|
|----|--------|-----------|-------------|
|serialPort|Y|The serial port to use for connecting to the KNX bus||
|readingPause|N|Time in milliseconds of how long should be paused between two read requests to the bus during initialization|50|
|responseTimeout|N|Timeout in seconds to wait for a response from the KNX bus|10|
|readRetriesLimit|N|Limits the read retries while initialization from the KNX bus|3|
|autoReconnectPeriod|N|Seconds between connect retries when KNX link has been lost, 0 means never retry|0|
|enableDiscovery|N|Enable or disable automatic Individual Address discovery|true|
|knxProj|N|KNX Project File (knxproj) to parse and add to the Things managed by this Bridge||

## Thing Configuration

### *generic* Things

*generic* Things are placeholders to identify Individual Addresses / actors on the KNX bus. They have no specific function in the KNX binding, except that if the *address* is defined the binding will actively poll the Individual Address on the KNX bus to detect that the KNX actor is reachable. Under normal real world circumstances, either all devices on a bus are reachable, or either the whole bus is down. When *fetch* is set to true, the binding will read-out the memory of the KNX actor in order to detect configuration data and so forth. This is however an experimental feature very prone to the actual on the KNX bus. 

|Name|Required|Description|Default value|
|----|--------|-----------|-------------|
|address|N|The individual address in x.y.z notation|Required if *fetch* is set to true|
|fetch|N|Read out the device parameters and address/communication object tables|false|

### Things provided to knxproj parsing

If a *knxProj* file is defined in the configuration of the Bridge, then the KNX binding will parse that ETS5 configuration file. For each Individual Address defined in the ETS5 file, a *generic* Thing will be created and provided to the runtime.

In order to enable automatic parsing of the knxproj files provided to the runtime, the entry

```
org.openhab.binding.knx.folder:knx=knxproj
```

has to be added to *services.cfg* in the etc folder. In the *conf* folder a subdirectory *knx* should be created, and the knxproj files, as exported by the ETS5 application, can be directly placed in that folder

## Channels

The Bridges support the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|--------------|--------------|
| errorsall|Number|The number of errors that occurred on the KNX bus since the start of the binding|
| errors5min|Number|The number of errors that occurred on the KNX bus during the last 5 minutes|

### *generic* Thing Channels of Things defined by the end-user

Different kinds of Channels are defined and can be used to group together Group Addresses. All Channel types share two configuration parameters: *read*, an optional parameter to indicate if the 'readable' group addresses of that Channel should be read at startup (default: false), and *interval*, an optional parameter that defines an interval between attempts to read the status group address on the bus, in seconds. When defined and set to 0, the interval is ignored (default: 0)

For specific configurations reference is made to the *generic.xml* ESH-INF file of the source distribution. For example, the *statusswitch* Channel type defines two configuration parameters: *switchGA*, the group address in x/y/z notation to toggle the switch, and *statusGA*, the group address in x/y/z notation to read the switch status. Since it is about a switch, the DPT does not have to be defined, e.g. it is supposed to be 1.001. For example, the *number* Channel type is used to define Channels to read/write ordinary Numbers. 

```
Thing generic someactor {
    Type statusswitch :  someswitch [ switchGA="2/4/99", statusGA="2/4/100", read=true, interval=3600]
    Type number : ga_2_4_101 [ groupaddress="2/4/101", dpt="9.001", read=true, interval=3600]
}
```

### *generic* Thing Channels of Things derived from the knxproj file

The ETS5 parser contained in the Binding will automatically create a *generic* Channel for each Group Address that is used, either for reading or writing, for every *generic* Thing that it discovered in the knxproj file. The Type of the Channel will automatically be set to an Item Type that is compatible with the Datapoint type (DPT) that is defined in the knxproj file. In addition, the *read* parameter will be set to true, and the *interval* parameter will be set to 3600 seconds. The *generic* Channel type is specifically used by the parser, and can not be manually defined through the .things DSL files. 

The *generic* Thing will be named x_y_z, and the Channels will be equally named x_y_z based on the Individual Address and Group Address respectively 

As a result, entries similar to the ones here below will be found in the openHAB logs:

```
Found KNX master data with version '459'
Found KNX hardware description for 'PKC-GROUP Oyj'
Found KNX product 'Gateway between Ventilation unit and EIB' by 'PKC-GROUP Oyj'
Found KNX hardware description for 'Elsner Elektronik GmbH'
...
Found KNX product 'KNX TH-UP basic' by 'Elsner Elektronik GmbH'
Found KNX product 'KNX TH-UP basic' by 'Elsner Elektronik GmbH'
Found KNX product 'JAL-0810D.01 Shutter Actuator 8-fold, 8TE,24VDC,8A' by 'MDT technologies'
Found KNX product 'JAL-0410.01 Shutter Actuator 4-fold, 4TE, 230VAC, 10A' by 'MDT technologies'
Found KNX product 'JAL-0410D.01 Shutter Actuator 4-fold,4TE,24VDC,8A' by 'MDT technologies'
...
Added a Thing knx:generic:ip1:1_1_67 for an actor of type Presence detector - brightness sensor UP 258/E11 made by Siemens
Added the Channel knx:generic:ip1:1_1_67:7_1_23 (Type Switch, DPT 1.001, true/true, 'TechnicalFacilities-Server-Motion Stop')
Added the Channel knx:generic:ip1:1_1_67:7_1_22 (Type Switch, DPT 1.001, true/true, 'TechnicalFacilities-Server-Motion Start')
Added the Channel knx:generic:ip1:1_1_67:7_1_21 (Type Switch, DPT 1.001, true/true, 'TechnicalFacilities-Server-Motion Status')
Added the Channel knx:generic:ip1:1_1_67:7_1_27 (Type Number, DPT 9.004, true/true, 'TechnicalFacilities-Server-Brightness')
Added the Channel knx:generic:ip1:1_1_67:7_1_26 (Type Switch, DPT 1.001, true/true, 'TechnicalFacilities-Server-Presence Stop')
Added the Channel knx:generic:ip1:1_1_67:7_1_25 (Type Switch, DPT 1.001, true/true, 'TechnicalFacilities-Server-Presence Start')
Added the Channel knx:generic:ip1:1_1_67:7_1_24 (Type Switch, DPT 1.001, true/true, 'TechnicalFacilities-Server-Presence Status')
```

## Full Example

demo.Things:

```
Bridge knx:ip:ip1 [ ipAddress="192.168.0.10", portNumber=3671, localIp="192.168.0.166", ipConnectionType="TUNNEL", readingPause=50, responseTimeout=10, readRetriesLimit=3, autoReconnectPeriod=1,localSourceAddr="0.0.0", knxProj="Export 20161222.knxproj"] {
    Thing generic someactor {
        Type statusswitch :  someswitch [ switchGA="2/4/99", statusGA="2/4/100", read=true, interval=3600]
        Type number : ga_2_4_101 [ groupaddress="2/4/101", dpt="9.001", read=true, interval=3600]
    }
}
```

demo.items:

[Manually defined]
```
Switch someSwitchItem "Manually Defined Switch" <switch> { channel="knx:generic:ip1:someactor:someswitch" }
```

[knxproj parsing]
```
Number WellnessWifiCurrent "Wellness - Wifi - Current [%.0f mA]" <energy> (gWellness,gOutlets,gCurrent,gWellness) { channel="knx:generic:ip1:1_4_103:2_6_28" }
Number WellnessWifiEnergy "Wellness - Wifi - Energy [%.0f Wh]" <energy> (gWellness,gOutlets,gEnergy,gWellness) { channel="knx:generic:ip1:1_4_103:2_6_29" }
Number WellnessWifiOperatingHours "Wellness - Wifi - Operating Hours [%.1f h]" <clock> (gWellness,gOutlets,gOperatingHours,gWellness) { channel="knx:generic:ip1:1_4_103:2_6_27" }
Switch WellnessWifiSwitch "Wellness - Wifi - Switch" <switch> (gWellness,gOutlets,gSwitch,gWellness) { channel="knx:generic:ip1:1_4_103:2_6_25", channel="knx:generic:ip1:1_4_103:2_6_26" }
```

## Supported Datapoint Types

The KNX binding supports a limited set of Datapoint types (DPTs). If your thing configuration contains a DPT that is not supported by the KNX binding, openHAB 1.4.0 and later will throw an exception during startup ("DPT n.nnn is not supported by the KNX binding").

To get an overview of the supported DPTs, it's best to look into the source code of the KNX binding and the library it depends on. The DPTs for the binding are defined in [KNXCoreTypeMapper](https://github.com/openhab/openhab/blob/master/bundles/binding/org.openhab.binding.knx/src/main/java/org/openhab/binding/knx/internal/dpt/KNXCoreTypeMapper.java). The constants (and their mapping to DPTs) are defined in the library [calimero](https://github.com/calimero-project/calimero/tree/master/src/tuwien/auto/calimero/dptxlator).

## KNX Logging

Since version 1.5.0 of this binding, it is possible to capture log events from calimero. These log events contain detailed information from the KNX bus (what is written to the bus, what gets read from the bus, ...)

To enable this logging, the following line has to be added to `logback.xml`:

    <logger name="tuwien.auto.calimero" level="DEBUG" />
