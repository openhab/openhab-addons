# KNX Binding

The openHAB KNX binding allows to connect to [KNX Home Automation](https://www.knx.org/) installations.
Switching lights on and off, activating your roller shutters, or changing room temperatures are only some examples.

To access your KNX bus, you either need a gateway device which is connected to the KNX bus and allows computers to access the bus communication.
This can be either an Ethernet (as a Router or a Tunnel type) or a serial gateway.
The KNX binding then can communicate directly with this gateway.
Alternatively, a PC running [KNXD](https://github.com/knxd/knxd) (free open source component software) can be put in between which then acts as a broker allowing multiple clients to connect to the same gateway.
Since the protocol is identical, the KNX binding can also communicate with it transparently.

_**Attention:**_ With the introduction of Unit of Measurement (UoM) support, some data types have changed (see `number` channel below):

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

### Discovery

KNX IP bridges, i.e., IP interfaces, routers, and knxd instances, are discovered through multicast communication in the local network.
As a KNX setup is typically static, this in only done during startup of the binding.
Corresponding bridges are added to the inbox.
Additional configuration might be necessary after adding a bridge.

Note that several items per device might be created, as routers typically support routing and tunneling.
Make sure you import only one item per device.

Discovery is not available for serial bridges and device Things described below.
Discovery of IP bridges will not work without further measures if openHAB and the interface run on different network segments,
as multicast traffic is typically not forwarded.

### IP Gateway

The IP Gateway is the most commonly used way to connect to the KNX bus.
At its base, the _ip_ bridge accepts the following configuration parameters:

| Name                | Required     | Description                                                                                                                      | Default value                                             |
|---------------------|--------------|----------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| type                | Yes          | The IP connection type for connecting to the KNX bus (`TUNNEL`, `ROUTER`, `SECURETUNNEL` or `SECUREROUTER`)                      | -                                                         |
| ipAddress           | for `TUNNEL` | Network address of the KNX/IP gateway. If type `ROUTER` is set, the IPv4 Multicast Address can be set.                           | for `TUNNEL`: \<nothing\>, for `ROUTER`: 224.0.23.12      |
| portNumber          | for `TUNNEL` | Port number of the KNX/IP gateway                                                                                                | 3671                                                      |
| localIp             | No           | Network address of the local host to be used to set up the connection to the KNX/IP gateway                                      | the system-wide configured primary interface address      |
| localSourceAddr     | No           | The (virtual) individual address for identification of this openHAB Thing within the KNX bus <br/><br/>Note: Use a free address, not the one of the interface. Or leave it at `0.0.0` and let openHAB decide which address to use.<br/>When using knxd, make sure _not to use_ one of the addresses reserved for tunneling clients. | 0.0.0                                                     |
| useNAT              | No           | Whether there is network address translation between the server and the gateway                                                  | false                                                     |
| readingPause        | No           | Time in milliseconds of how long should be paused between two read requests to the bus during initialization                     | 50                                                        |
| responseTimeout     | No           | Timeout in seconds to wait for a response from the KNX bus                                                                       | 10                                                        |
| readRetriesLimit    | No           | Limits the read retries while initialization from the KNX bus                                                                    | 3                                                         |
| autoReconnectPeriod | No           | Seconds between connect retries when KNX link has been lost (0 means never).                                                     | 0                                                         |
| monitorBufferSize   | No           | Number of telegrams kept for the console command `openhab:knx monitor` (0 keeps none, a negative value counts as 0, at most 100000).                          | 2000                                                      |
| groupMonitorSend    | No           | Lets an administrator read or write group addresses from the Group Monitor of the add-on's page and with `POST /rest/knx/bridges/{b}/group-telegram`. | false                                                     |
| routerBackboneKey   | No           | KNX secure: Backbone key for secure router mode                                                                                  | -                                                         |
| tunnelUserId        | No           | KNX secure: Tunnel user id for secure tunnel mode (if specified, it must be a number >0)                                         | -                                                         |
| tunnelUserPassword  | No           | KNX secure: Tunnel user key for secure tunnel mode                                                                               | -                                                         |
| tunnelDeviceAuthentication  | No   | KNX secure: Tunnel device authentication for secure tunnel mode                                                                  | -                                                         |
| keyringFile         | No           | KNX secure: Keyring file exported from ETS and placed in openHAB config/misc folder. Mandatory to decode secure group addresses. | -                                                         |
| keyringPassword     | No           | KNX secure: Keyring file password (set during export from ETS)                                                                   | -                                                         |
| tunnelSourceAddress | No           | KNX secure: Physical KNX address of tunnel in secure mode to identify tunnel. If given, openHAB will read tunnelUserId, tunnelUserPassword, tunnelDeviceAuthentication from keyring.  | -    |

### Serial Gateway

The _serial_ bridge accepts the following configuration parameters:

| Name                | Required | Description                                                                                                                      | Default value |
|---------------------|----------|----------------------------------------------------------------------------------------------------------------------------------|---------------|
| serialPort          | Y        | The serial port to use for connecting to the KNX bus                                                                             | -             |
| readingPause        | N        | Time in milliseconds of how long should be paused between two read requests to the bus during initialization                     | 50            |
| responseTimeout     | N        | Timeout in seconds to wait for a response from the KNX bus                                                                       | 10            |
| readRetriesLimit    | N        | Limits the read retries while initialization from the KNX bus                                                                    | 3             |
| autoReconnectPeriod | N        | Seconds between connect retries when KNX link has been lost, 0 means never retry                                                 | 0             |
| monitorBufferSize   | N        | Number of telegrams kept for the console command `openhab:knx monitor` (0 keeps none, a negative value counts as 0, at most 100000)                           | 2000          |
| groupMonitorSend    | N        | Lets an administrator read or write group addresses from the Group Monitor of the add-on's page and with `POST /rest/knx/bridges/{b}/group-telegram`.          | false         |
| useCemi             | N        | Use newer CEMI message format, useful for newer devices like KNX RF sticks, kBerry, etc.                                         | false         |
| keyringFile         | N        | KNX secure: Keyring file exported from ETS and placed in openHAB config/misc folder. Mandatory to decode secure group addresses. | -             |
| keyringPassword     | N        | KNX secure: Keyring file password (set during export from ETS)                                                                   | -             |

## Things

### _device_ Things

_basic_ Things are wrappers around arbitrary group addresses on the KNX bus.
They have no specific function in the KNX binding, except that if the _address_ is defined, the binding will actively poll the Individual Address on the KNX bus to detect that the KNX actuator is reachable.
Under normal real-world circumstances, either all devices on a bus are reachable, or the entire bus is down.
If line couplers are installed, physical device addressing might be filtered; in this case, please do not specify the addresses for devices on this line.
When _fetch_ is set to true, the binding will read out the memory of the KNX actuator in order to detect configuration data and so forth.
This is just for information and has no effect on the functionality of the binding.
It can safely be turned off to save bandwidth on the bus or avoid problems with older devices.

| Name           | Required   | Description                                                                                                                                      | Default value   |
|----------------|------------|--------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| address        | N          | The individual device address (in 0.0.0 notation)                                                                                                | -               |
| fetch          | N          | Read out the device parameters and address/communication object tables (requires the address)                                                    | false           |
| pingInterval   | N          | Interval (in seconds) to contact the device and set the Thing status based on the result (requires the address, 0 disables polling)              | 600             |
| readInterval   | N          | Interval (in seconds) to actively request reading of values from the bus (0 if they should only be read once at startup)                         | 0               |

Different kinds of channels are defined and can be used to group together Group Addresses.
All channels of a device share one configuration parameter defined at the device level: _readInterval_, an optional parameter that indicates if the 'readable' group addresses of that Channel should be read periodically at the given interval, in seconds.
'Readable' group addresses are marked with an `<` in the group address definition of a Channel, see below.
All readable group addresses are queried by openHAB during startup.
If readInterval is not specified or set to 0, no further periodic reading will be triggered (default: 0).

#### Channel Types

Standard channels are used most of the time.
They are used in the common case where the physical state is owned by a device within the KNX bus, e.g., by a switch actuator that "knows" whether the light is turned on or off, or by a temperature sensor that reports the room temperature regularly.

Control channel types (suffix `-control`) are used for cases where the KNX bus does not own the physical state of a device.
This could be the case if, for example, a lamp from another binding should be controlled by a KNX wall switch.
When a `GroupValueRead` telegram is sent from the KNX bus to a *-control Channel, the bridge responds with a `GroupValueResponse` telegram to the KNX bus.

##### Channel Type `color`, `color-control`

| Parameter        | Description                            | Default DPT |
|------------------|----------------------------------------|-------------|
| hsb              | Group address for the color            | 232.600     |
| switch           | Group address for the binary switch    | 1.001       |
| position         | Group address brightness               | 5.001       |
| increaseDecrease | Group address for relative brightness  | 3.007       |

The `hsb` address supports DPT 232.600 (RGB), 242.600 (xyY), and 251.600 (RGBW).

Some RGB/RGBW products (e.g. MDT) use HSB values for DPT 232.600 instead of RGB.
This is supported as "vendor-specific DPT" with a value of 232.60000.

RGBW (DPT 251.600) can either be converted to HSBType, or represented by two items: a HSBType for RGB and an additional PercentType for the W channel.
Default handling for RGBW is to use separate items.
Note that this also requires two frames to be sent out separately when these elements are sent to the bus, as the binary representation uses a partially populated KNX frame.
Alternatively, a single HSB item can be used.
Conversion to a single HSBType will lose the exact setting for W, and will reconstruct it when a conversion to RGBW is required.
This option can be selected using the special DPT 251.60600.

##### Channel Type `contact`, `contact-control`

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 1.009       |

_Attention:_ Due to a bug in the original implementation, the states for DPT 1.009 are inverted (i.e., `1` is mapped to `OPEN` instead of `CLOSED`).
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
If the channel is linked to the correct item-type (`Number:Temperature` in this case), the display unit can be controlled by item metadata (e.g., `%.1f °F` for 1 digit of precision in Fahrenheit).
The unit is stripped if the channel is linked to a plain number item (type `Number`).

Outgoing values with unit are first converted to the unit associated with the DPT (e.g., a value of `10 °F` is converted to `-8.33 °C` if the channel has DPT 9.001).
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
This could, for example, be the case if a lamp from another binding should be controlled by a KNX wall switch.
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

_Attention:_ Due to a bug in the original implementation, the states for DPT 1.009 are inverted (i.e., `1` is mapped to `OPEN` instead of `CLOSED`).
A change would break all existing installations and is therefore not implemented.

##### Channel Type "number-control"

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 9.001       |

For UoM support, see the explanations of the `number` channel.

##### Channel Type "string-control"

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 16.001      |

##### Channel Type "datetime-control"

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | 19.001      |

#### Trigger Channel Type

##### Channel Type "telegram-trigger"

A trigger channel fires an event for every `GroupValueWrite` telegram that arrives on one of its group addresses, so a rule can react to chosen telegrams without an item.
The event is the value as text, e.g. `21.5 °C` or `ON`, decoded with the data type given in the configuration; without one, or if the value does not fit the data type, the event is the raw bytes in hex, e.g. `0C33`.
It does not fire for `GroupValueResponse` or `GroupValueRead` telegrams or for what openHAB sends itself.
The channel never reads and never sends: a `<` in front of an address is ignored, and a command sent to it does nothing.
There can be several addresses, separated by `+`; each of them fires the channel.

| Parameter | Description   | Default DPT |
|-----------|---------------|-------------|
| ga        | Group address | none (raw)  |

Every event is a line in `events.log` (`ChannelTriggeredEvent`), so use this channel for chosen addresses only, not for a busy line.

```java
rule "Window sensor telegram"
when
    Channel "knx:device:bridge:generic:telegrams" triggered
then
    logInfo("knx", "Telegram value: " + receivedEvent)
end
```

#### Group Address Notation

```text
<config>="[<dpt>:][<|>]<mainGA>[[+[<]<listeningGA>][+[<]<listeningGA>..]]"
```

where parts in brackets `[]` denote optional information.

**Each configuration parameter has a `mainGA` where commands are written to and optionally several `listeningGA`s.**

`mainGA` also listens to incoming packets, unless prefixed with a `>` character.
This is recommended if you have a dedicated status group address which is added as `listeningGA`.

The optional `<` sign tells whether the group address of the datapoint accepts read requests on the KNX bus (it does, if the sign is there).
The group addresses marked with `<` are read by openHAB during startup.
Future versions might support reading from one GA only.
With `*-control` channels, the state is not owned by any device on the KNX bus, therefore no read requests will be sent by the binding, i.e. `<` signs will be ignored for them.

The element `dpt` is  highly recommended and may change to a mandatory element in future versions.
If omitted, the corresponding default value will be used (see the channel descriptions above).

## Mapping DPTs to openHAB Types

Datapoint Types (DPTs) define how the content of a KNX telegram is interpreted.

The following table is a complete list of DPTs currently supported by openHAB.
OpenHAB supports all DPTs supported by the corresponding release of the Calimero library, plus a few [specific additions](#special-dpts).

The default mapping is given, however DPTs can be overwritten.
KNX frames do not contain information about the encoding or DPT, so any DPT with a compatible byte size and a useful encoding can be used.
A good example for this are bitfields represented as String which are mapped to a DPT of DecimalType for handling in rules.
For more details, see the KNX specification (section 3.7.2, System Specifications, Interworking, Datapoint Types).

In case a missing DPT or subtype is needed, there is a good chance that a DPT of matching size and DecimalType is found.
Further DPTs and subtypes may be added later once implemented and released in the [Calimero library](https://github.com/calimero-project/calimero-core).

| DPT             | Primary openHAB type (things) (items with UOM)     | Remark                            |
|-----------------|----------------------------------------------------|-----------------------------------|
| 1.001-1.007     | OnOffType (switch), OpenClosedType (contact)       |                                   |
| 1.008           | UpDownType (e.g. for dimmer, rollershutter)        |                                   |
| 1.009           | OnOffType (switch), OpenClosedType (contact)       | Not according to spec, see above  |
| 1.010           | StopMoveType (e.g. for rollershutter)              |                                   |
| 1.011-1.016     | OnOffType (switch), OpenClosedType (contact)       |                                   |
| 1.017           | OnOffType (switch), OpenClosedType (contact)       | Trigger, use states OFF or CLOSED |
| 1.018-1.019     | OnOffType (switch), OpenClosedType (contact)       |                                   |
| 1.021           | OnOffType (switch), OpenClosedType (contact)       |                                   |
| 1.022           | DecimalType (number)                               | Counting from 0, use DPT if you need a DecimalType |
| 1.023-1.024     | OnOffType (switch), OpenClosedType (contact)       |                                   |
| 1.100           | OnOffType (switch), OpenClosedType (contact)       |                                   |
| 1.1200-1.1201   | OnOffType (switch), OpenClosedType (contact)       |                                   |
|                 |                                                     |                                   |
| 2.001-2.012     | DecimalType (number)                               |                                   |
|                 |                                                     |                                   |
| 3.007           | IncreaseDecreaseType (e.g. dimmer)                 |                                   |
| 3.008           | UpDownType (e.g. rollershutter)                    |                                   |
|                 |                                                     |                                   |
| 5.001           | QuantityType\<> (number) (Number:Percent)          | Alternatively: PercentType        |
| 5.003           | QuantityType\<> (number) (Number:Angle)            |                                   |
| 5.004           | QuantityType\<> (number) (Number:Percent)          | 0-255%, no mapping to PercentType |
| 5.005           | DecimalType (number)                               |                                   |
| 5.006           | DecimalType (number)                               | 255 is reserved                   |
| 5.010           | DecimalType (number)                               |                                   |
|                 |                                                     |                                   |
| 6.001           | QuantityType\<> (number) (Number:Percent)          | -128..127%, no mapping to PercentType |
| 6.010           | DecimalType (number)                               | -128..127                         |
| 6.020           | StringType (string)                                | Override with DPT5.010 if you need DecimalType |
|                 |                                                     |                                   |
| 7.001           | DecimalType (number)                               |                                   |
| 7.002-7.007     | QuantityType\<> (number) (Number:Time)             |                                   |
| 7.010           | DecimalType (number)                               |                                   |
| 7.011           | QuantityType\<> (number) (Number:Length)           |                                   |
| 7.012           | QuantityType\<> (number) (Number:ElectricCurrent)  |                                   |
| 7.013           | QuantityType\<> (number) (Number:Length)           |                                   |
| 7.600           | QuantityType\<> (number) (Number:Temperature)      |                                   |
|                 |                                                     |                                   |
| 8.001           | DecimalType (number)                               |                                   |
| 8.002-7.007     | QuantityType\<> (number) (Number:Time)             |                                   |
| 8.010           | QuantityType\<> (number) (Number:Percent)          |                                   |
| 8.011           | QuantityType\<> (number) (Number:Angle)            |                                   |
| 8.012           | QuantityType\<> (number) (Number:Length)           |                                   |
|                 |                                                     |                                   |
| 9.xxx           | QuantityType\<> (number) (Number:...)              | See [documentation on UOM](https://www.openhab.org/docs/concepts/units-of-measurement.html) |
| 9.001           | QuantityType\<> (number) (Number:...)              | Lower values than absolute zero will be set to -273 °C |
| 9.002-9.003     | QuantityType\<> (number) (Number:...)              |                                   |
| 9.004-9.008     | QuantityType\<> (number) (Number:...)              | No negative values allowed        |
| 9.009-9.011     | QuantityType\<> (number) (Number:...)              |                                   |
| 9.020-9.027     | QuantityType\<> (number) (Number:...)              |                                   |
| 9.027-9.030     | QuantityType\<> (number) (Number:...)              | No negative values allowed        |
|                 |                                                     |                                   |
| 10.001          | DateTimeType (datetime)                            | Time. Date is set to 1/Jan/1970 + ofs if weekday is given. KNX can represent year 1990..2089. |
|                 |                                                     |                                   |
| 11.001          | DateTimeType (datetime)                            | Date only.                        |
|                 |                                                     |                                   |
| 12.001          | DecimalType (number)                               |                                   |
| 12.100-12.102   | QuantityType\<> (number) (Number:Time)             |                                   |
| 12.1200-12.1201 | QuantityType\<> (number) (Number:Volume)           |                                   |
|                 |                                                     |                                   |
| 13.xxx          | QuantityType\<> (number) (Number:...)              | See [documentation on UOM](https://www.openhab.org/docs/concepts/units-of-measurement.html) |
| 13.001          | DecimalType (number)                               |                                   |
| 13.002          | QuantityType\<> (number) (Number:VolumetricFlowRate) |                                 |
| 13.010-13.016   | QuantityType\<> (number) (Number:...)              |                                   |
| 13.100          | QuantityType\<> (number) (Number:...)              |                                   |
| 13.1200-13.1201 | QuantityType\<> (number) (Number:Time)             |                                   |
|                 |                                                     |                                   |
| 14.xxx          | QuantityType\<> (number) (Number:...)              | See [documentation on UOM](https://www.openhab.org/docs/concepts/units-of-measurement.html) |
| 14.000-14.080   | QuantityType\<> (number) (Number:...)              |                                   |
| 14.1200-14.1201 | QuantityType\<> (number) (Number:...)              |                                   |
|                 |                                                     |                                   |
| 16.000          | StringType (string)                                | ASCII                             |
| 16.001          | StringType (string)                                | ISO 8859-1                        |
|                 |                                                     |                                   |
| 17.001          | DecimalType (number)                               | Scene 0..63                       |
|                 |                                                     |                                   |
| 18.001          | DecimalType (number)                               | Scene 0..63, add offset 0x80 (128) for storing scenes |
|                 |                                                     |                                   |
| 19.001          | DateTimeType (datetime) (DateTime)                 | Date and Time, year can be 1900..2155 |
|                 |                                                     |                                   |
| 20.xxx          |                                                    | Override with DPT5.010 if you need enum as DecimalType |
| 20.001-20.009   | StringType (string)                                |                                   |
| 20.011-20.014   | StringType (string)                                |                                   |
| 20.017          | StringType (string)                                |                                   |
| 20.020-20.022   | StringType (string)                                |                                   |
| 20.100-20.115   | StringType (string)                                |                                   |
| 20.120-20.122   | StringType (string)                                |                                   |
| 20.600-20.613   | StringType (string)                                |                                   |
| 20.801-20.804   | StringType (string)                                |                                   |
| 20.1000-20.1005 | StringType (string)                                |                                   |
| 20.1200         | StringType (string)                                |                                   |
| 20.1202-20.1209 | StringType (string)                                |                                   |
|                 |                                                     |                                   |
| 21.xxx          |                                                    | Override with DPT5.010 if you need bitset as DecimalType |
| 21.001-20.002   | StringType (string)                                |                                   |
| 21.100-20.106   | StringType (string)                                |                                   |
| 21.601          | StringType (string)                                |                                   |
| 21.1000-21.1002 | StringType (string)                                |                                   |
| 21.1010         | StringType (string)                                |                                   |
| 21.1200-21.1201 | StringType (string)                                |                                   |
|                 |                                                     |                                   |
| 22.xxx          |                                                    | Override with DPT7.010 if you need bitset as DecimalType |
| 22.100-22.101   | StringType (string)                                |                                   |
| 22.1000         | StringType (string)                                |                                   |
| 22.1010         | StringType (string)                                |                                   |
|                 |                                                     |                                   |
| 28.001          | StringType (string)                                | KNX representation is Null-terminated, do not include null characters |
|                 |                                                     |                                   |
| 29.010-29.012   | QuantityType\<> (number) (Number:...)              |                                   |
|                 |                                                     |                                   |
| 229.001         | DecimalType (number)                               | Scaling coded in KNX frame is regarded; for sending always encoded with flag "dimensionless" |
|                 |                                                     |                                   |
| 232.600         | HSBType (color)                                    | RGB                                  |
| 232.60000       | HSBType (color)                                    | Non-Standard, DPT 232.600 with HSB instead of RGB data |
|                 |                                                     |                                   |
| 235.001         | QuantityType\<> (number) (Number:ActiveEnergy)     | Composed DPT 235.001, first element ActiveEnergy (Wh), read only  |
| 235.61001       | DecimalType (number)                               | Non-Standard, composed DPT 235.001, second element Tariff (plain number), read only |
|                 |                                                     |                                   |
| 242.600         | HSBType (color)                                    | xyY                               |
|                 |                                                     |                                   |
| 243.600         | StringType (string)                                |                                   |
|                 |                                                     |                                   |
| 249.600         | StringType (string)                                |                                   |
|                 |                                                     |                                   |
| 250.600         | StringType (string)                                |                                   |
|                 |                                                     |                                   |
| 251.600         | HSBType (color)                                    | RGBW, RGB part as HSBType         |
| 251.600         | PercentType                                        | RGBW, W part separately for Dimmer |
| 251.60600       | HSBType (color)                                    | Non-Standard, lossy conversion from HSBType to RGBW |
|                 |                                                     |                                   |
| 252.600         | StringType (string)                                |                                   |
|                 |                                                     |                                   |
| 253.600         | StringType (string)                                |                                   |
|                 |                                                     |                                   |
| 254.600         | StringType (string)                                |                                   |
|                 |                                                     |                                   |

## Special DPTs

OpenHAB supports all DPTs supported by the corresponding release of the Calimero library.

Additional DPTs have been introduced to add functionality:

| DPT           | Description                                                 | Remark     |
|---------------|-------------------------------------------------------------|------------|
| DPT 0.000     | Raw frame data as Hex String (for unsupported frame formats)| read only  |
| DPT 232.60000 | DPT 232.600 with HSB instead of RGB data (see below)        | read/write |
| DPT 235.001   | Composed DPT 235.001, first element ActiveEnergy (Wh)       | read only  |
| DPT 235.61001 | Composed DPT 235.001, second element Tariff (plain number)  | read only  |

## KNX Secure

> NOTE: Support for KNX Secure is partially implemented.
KNX IP Secure connections using a secure tunnel work stable.
All other features should be considered experimental.

### KNX IP Secure

KNX IP Secure protects the traffic between openHAB and your KNX installation.
It **requires a KNX Secure Router or a Secure IP Interface** and a KNX installation **with security features enabled in the ETS tool**.

For _Secure routing_ mode, the so-called `backbone key` needs to be configured in openHAB.
It is created by the ETS tool and cannot be changed via the ETS user interface.
There are two possible ways to provide the key to openHAB:

- The backbone key can be extracted from the Security report (ETS, Reports, Security, look for a 32-digit key) and specified in parameter `routerBackboneKey`.
- The backbone key is included in ETS keyring export (ETS, project settings, export keyring). The keyring file is configured using `keyringFile` (put it in `config\misc` folder of the openHAB installation) and also requires `keyringPassword`.

For _Secure tunneling_ with a Secure IP Interface (or a router in tunneling mode), more parameters are required.
A unique device authentication key, and a specific tunnel identifier and password need to be available.
It can be provided to openHAB in two different ways:

- All information can be looked up in ETS and provided separately: `tunnelDeviceAuthentication`, `tunnelUserPassword`.
 `tunnelUserId` is a number that is not directly visible in ETS, but can be looked up in the keyring export or deduced (typically 2 for the first tunnel of a device, 3 for the second one, etc.).
 `tunnelUserPassword` is set in ETS in the properties of the tunnel (below the IP interface, you will see the different tunnels listed) and denoted as "Password".
 `tunnelDeviceAuthentication` is set in the properties of the IP interface itself; check for the tab "IP" and the description "Authentication Code".
- All necessary information is included in ETS keyring export (ETS, project settings, export keyring).
 The keyring file is configured using `keyringFile` (put it in `config\misc` folder of the openHAB installation) and `keyringPassword`.
 In addition, `tunnelSourceAddress` needs to be set to uniquely identify the tunnel in use.
 Once the keyring file is saved on the bridge, MainUI suggests the tunnels of the keyring for `tunnelSourceAddress` (and the other interfaces for `localSourceAddr`), each with the number of secure group addresses ETS linked to it. Any address can still be typed.

### KNX Data Secure

KNX Data Secure protects the content of messages on the KNX bus.
In a KNX installation, both classic and secure group addresses can coexist.
Data Secure does _not_ necessarily require a KNX Secure Router or a Secure IP Interface, but a KNX installation with newer KNX devices that support Data Secure and with **security features enabled in the ETS tool**.

**openHAB ignores messages with secure group addresses, unless data secure is configured.**

All necessary information to decode secure group addresses is included in ETS keyring export (ETS, project settings, export keyring).
The keyring file is configured using `keyringFile` (put it in `config\misc` folder of the openHAB installation) and also requires `keyringPassword`.

openHAB reads and writes secure group addresses and answers read requests on them.
KNX devices only accept secure telegrams from senders they know.
Before exporting the keyring, link every secure group address openHAB uses to the tunnel openHAB connects through in ETS, or in router mode to a dummy device with the address set in `localSourceAddr`.
Outgoing secure telegrams are numbered from the system clock, so the openHAB host needs the correct time.

If ETS has not linked the address openHAB sends from to a secure group address, devices ignore the telegram although it goes out.
openHAB then keeps the bridge ONLINE and shows a warning in its status text, once when the connection is made if the address is not linked to any secure group address, and once for every secure group address openHAB sends to that it is not linked to.
The warning is gone after the bridge is restarted with a keyring that no longer has the problem.

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

## Console Commands

The KNX binding provides additional functionality which can be triggered from the openHAB console.
Type `openhab:knx` on the openHAB console for further information.

`openhab:knx keyring [<bridge>]` checks the ETS keyring of a bridge, or of every bridge that has one.
It shows the keyring file, whether its signature matches the configured `keyringPassword`, whether it holds a backbone key, the number of secure group addresses, and one line per interface with its address, type, host, user id and the number of secure group addresses ETS linked to it.
The interface openHAB sends from is marked.
Keys, passwords and hashes are never shown.
This is the quickest way to find out why a secured device ignores openHAB: its interface shows 0 links, or the signature is not valid.

`openhab:knx monitor [<bridge>] [filters]` shows the last telegrams of a bridge, or of every bridge: time, direction, read / write / response, source, group address, whether the telegram was secured, data type and value, and for telegrams openHAB sent whether they went out.
The value is decoded with the data type of the channel that uses the group address; without one the raw bytes are shown.
Filters are `src=`, `dst=`, `type=` (`write`, `read`, `response`), `dir=` (`in`, `out`), `dpt=` (`9` or `9.001`), `from=` and `to=` (`-10m`, `14:05:30`, `2026-11-14T14:05`), `context=<ms>` (adds the telegrams around each match) and `limit=<n>` (default 50, the newest are kept); several values inside one filter are separated by commas.
A plain telegram to a group address that has a group key in the keyring is marked `plain on secured address` and counted in the header; openHAB still takes its value, as before.
A secured telegram that could not be decrypted is shown as `not decrypted` with the reason, without a value, and counted per reason in the header: `NO_KEY` (no group key for the address), `SENDER_NOT_LISTED` (the secure layer has a sender list for the address and the sender is not on it), `INVALID_CONTROL_FIELD`, `SEQUENCE_NUMBER` (the sequence number is not above the last one accepted from that sender), `AUTHENTICATION` (the check value does not match) and `ACCESS` (tool access on a group address).
The counters start again at every connect; the shown telegrams do not.
For an address a channel uses, the log gets one warning per address and reason and connection, as the usual cause is a device that ETS did not update.
The bridge shows the address it sends from as property `ownAddress`.
`openhab:knx monitor [<bridge>] clear` forgets the kept telegrams.
How many telegrams a bridge keeps is set by its parameter `monitorBufferSize`; they are kept in memory only.
A write that openHAB sent is shown when the library call has ended, so a status telegram that a device sends while the write waits for the tunnel's confirmation can show above the write that caused it.

### The ETS Project in openHAB

An ETS project file (`.knxproj`) can be imported once per bridge.
openHAB keeps the names and data types the project gives, and uses them in the commands below and in the group monitor.
Nothing is created from it: no Things, no channels, no Items.
The commands only show what the project says; you copy what you want into a channel yourself.

`openhab:knx project import <bridge> <file>` reads the file once.
`<file>` is an absolute path, or the name of a file in `conf/misc` (a name without a folder part).
Nothing else is read: a relative path with a folder in it, or the name `..`, is refused.
An absolute path is read wherever it points, so the user of the console decides what it reads.
The file itself is not kept, so it can be deleted afterwards, and a new import replaces the earlier project of that bridge.
The names of objects are taken in the language of openHAB's regional setting at the time of the import; to change the language, import again.

```text
openhab:knx project import knx:ip:home house.knxproj
Imported "house.knxproj" for KNX bridge "Home" (knx:ip:home): 350 group addresses, 39 devices, 709 group objects.
The file was read once and is not kept.
```

If the project is protected with a password (ETS 6), the console asks for it on the next line and hides what you type.
The password is never given in the command, so it does not end up in the command history; it is not shown, logged or stored, and the array the binding copies it into is overwritten as soon as the import is over.
There is one try; a wrong password imports nothing.
A console that cannot ask for a hidden line (a script, for example) says so and imports nothing.
A project without a password never asks, so it can be imported from a script.
Other kinds of protection are refused with a message.

What is kept is written to `userdata/knx/project-knx_ip_home.json` (the UID of the bridge, with `_` in place of `:`): the project name, the group addresses with their names and data types, the devices, the group objects of the devices and what links them, the building structure, and the products.
Never kept are the project file, its password, any key or device password in it, and the values of device parameters.
There is one project per bridge.
A stored project stays when its bridge is removed; `openhab:knx project info` lists it as the project of a bridge that is not running, which is also what it says for a disabled bridge, and `project remove` takes it away.
A stored file that is damaged, or was written by a newer version of the binding, is not used; the log says so once, and the commands tell you to import the project again.

`openhab:knx project info [<bridge>]` shows the project of a bridge, or of every bridge: its name, when and from which file it was imported, the language of the texts, the number of group addresses (and how many are secured), devices, group objects, spaces and functions, and the tool that wrote it.

```text
openhab:knx project info home
KNX bridge "Home" (knx:ip:home): project "Made-up house", imported 2026-11-14 12:34 from "house.knxproj", texts: de-DE
  350 group addresses (12 secured), 39 devices, 709 group objects, 41 spaces, 18 functions
  written by ETS6 6.4.7777.0, last change 2026-03-04T05:06:07.000Z
```

`openhab:knx project addresses <bridge> [<filter>] [limit=<n>]` lists the group addresses of the project, and `openhab:knx project devices <bridge> [<filter>] [limit=<n>]` its devices.
A filter that starts with a digit is the start of an address (`31/7` or `7.7`), anything else is searched in the name, ignoring case.
`limit` takes 1 to 10000, 50 is the default; when rows are left out, the last line says how many.
The first line shows how many rows match out of how many there are.

```text
openhab:knx project addresses home 31/7/ limit=2
KNX bridge "Home" (knx:ip:home): 6 of 350 group addresses
31/7/1  1.001  secure  "Made-up light"
31/7/2  9.001  -  "Made-up temperature"
4 more. Narrow the filter or use limit=<n>.
```

`openhab:knx project remove <bridge>` deletes the stored project of a bridge.
For a project whose bridge no longer exists, give its UID.

`openhab:knx channels [<bridge>] <device>` prints, for a device of the project such as `7.7.1`, one line per linked group object in the form you can copy into a channel of a device Thing: the channel type, the channel configuration (`ga="1.001:31/7/1"` or the key that fits the type), and the name of the group object.
A note below a line says what the project could not tell, for example that the objects linked to an address disagree on the type.
It creates nothing.

```text
openhab:knx channels home 7.7.1
7.7.1 "Made-up switch actuator": 3 channel lines
  switch  ga="1.001:31/7/1+31/7/4"  — "1 – Switch – Made-up output"
  number  ga="9.001:<31/7/2"  — "2 – Temperature – Made-up sensor"
  rollershutter  upDown="1.008:31/7/5"  — "3 – Blind 7 – Made-up blind"
Nothing was created. Copy the lines you want into a channel of a device Thing.
```

`openhab:knx check-project [<bridge>]` compares the group addresses of the channels of the bridge's device Things with the project and lists what differs: an address that is not in the project (`ADDRESS_NOT_IN_PROJECT`), one that no device object uses (`ADDRESS_WITHOUT_OBJECT`), a different data type (`DATAPOINT_TYPE_DIFFERS`), and a channel that reads an address on which no object answers a read (`READ_WITHOUT_READ_FLAG`).
Where only the sub number of the data type differs (9.001 against 9.002), the line starts with `note:` and is counted as a note, not as a difference.
Device Things that are not initialised cannot be checked; their number is shown.

```text
openhab:knx check-project home
KNX bridge "Home" (knx:ip:home): 5 configured addresses checked, 1 differences, 1 notes
  DATAPOINT_TYPE_DIFFERS  31/7/2  Made-up temperature channel  configured 5.001, project 9.001
  note: DATAPOINT_TYPE_DIFFERS  31/7/1  Made-up light channel  configured 1.002, project 1.001
```

`openhab:knx list-unknown-ga` shows the name the project gives to an address after the count, in quotes.
With a project imported, `openhab:knx monitor` shows the names of group addresses and devices, and decodes the value of an address no channel uses with the data type of the project.
The bridge argument of `channels` and `check-project` can be left out when only one bridge has a project.
A project text that is long or holds control characters is shortened and cleaned to one line of at most 60 characters.

## REST

Everything the console shows about monitors, keyrings and projects is also available as JSON under `/rest/knx`, for pages and scripts.
Every call needs the token of an administrator in a header, `Authorization: Bearer <token>` or `X-OPENHAB-TOKEN: <token>`; there is nothing for other users and nothing without login.
`{b}` is the UID of a bridge (`knx:ip:home`), `{device}` an individual address (`7.7.1`) or the device's project id.
Nothing here creates or changes a Thing, a channel or an Item.
Only `POST …/group-telegram` sends to the bus, and only when the bridge's setting `groupMonitorSend` is on.

| Method and path | What |
|---|---|
| `GET /bridges` | the bridges, then stored projects whose bridge is gone |
| `GET /bridges/{b}` | one bridge |
| `GET /bridges/{b}/monitor` | the monitor's counters, its `lastNumber` and `monitorId` |
| `GET /bridges/{b}/telegrams` | rows of the monitor; `src dst type dir dpt from to context` as on the console, `limit` (default 200, at most 5000), `after` (a row number) and `monitor` (a `monitorId`) to read on |
| `DELETE /bridges/{b}/telegrams` | forgets the rows, as `monitor <bridge> clear` |
| `GET /bridges/{b}/last-values` | the last telegram per group address |
| `POST /bridges/{b}/group-telegram` | sends one group telegram: `{"address": "1/2/3", "action": "read"}` or `{"address": "1/2/3", "action": "write", "dpt": "1.001", "value": "ON"}`, the value written as a channel takes a command. Only with the bridge setting `groupMonitorSend` (off by default, else `403 send-not-allowed`); at most 5 telegrams in 10 seconds per bridge (`429 too-many-telegrams`) |
| `GET /bridges/{b}/keyring` | what the keyring says, as `keyring <bridge>` |
| `PUT /bridges/{b}/keyring` | uploads a keyring |
| `GET /bridges/{b}/project` | the state of the stored project |
| `PUT /bridges/{b}/project` | uploads a project; `language` (`de-DE`) and `name` (the name to keep as its source) |
| `DELETE /bridges/{b}/project` | removes the stored project, also of a bridge that is gone |
| `GET /bridges/{b}/project/group-addresses` | the group addresses; `offset`, `limit` (default 1000, at most 10000) |
| `GET /bridges/{b}/project/group-address?address=1/2/3` | one group address; `usedBy` lists the channels of the bridge's Things that hold it (Thing, channel, type, role `main`, `status` or `listening`) and the Items linked to each |
| `GET /bridges/{b}/project/devices` | the devices; `offset`, `limit` |
| `GET /bridges/{b}/project/devices/{device}` | a device with its group objects |
| `GET /bridges/{b}/project/devices/{device}/channels` | the channel lines of `channels` |
| `POST /bridges/{b}/project/channels` | the channel lines for `{"groupAddresses": ["31/7/1"]}`, at most 2000; changes nothing |
| `GET /bridges/{b}/project/topology` | areas, lines and devices |
| `GET /bridges/{b}/project/locations` | the building structure |
| `GET /bridges/{b}/project/check` | the differences of `check-project` |
| `GET /bridges/{b}/telegrams/stream` | live telegrams as server-sent events; `src dst type dir dpt after monitor` |

Every parameter is read as text by the binding, so a value it cannot read is answered with its own error body; a query parameter given twice counts with its first value.
Every failure is answered with `{"code": "...", "text": "..."}` and a fixed English text, such as `400 bad-input`, `404 unknown-bridge`, `404 no-project`, `409 busy` or `413 too-large`.
One exception: a body of `POST …/project/channels` that is not JSON at all is refused by openHAB before the binding sees it, with openHAB's own answer.
Texts are not made safe for HTML.

Uploads send the file itself as the body (`Content-Type: application/octet-stream`).
A password goes in the header `X-KNX-Password` as Base64 of its UTF-8 bytes, never in the address; without TLS it can be read on the network, as the token can.
It is not logged or stored, and the binding overwrites its copy when the upload is over.
One upload runs at a time; a second one gets `409 busy`.
A project may have 200 MB, a keyring 1 MB; a larger body is refused, also when it was announced smaller.
The project file lies in its own folder under `userdata/knx/upload` only while it is read and is deleted afterwards.
An uploaded keyring is stored unchanged as `userdata/knx/keyring/knx_ip_home.knxkeys` (the bridge UID, with `_` in place of `:`); the answer's `fileName` is that name.
The bridge does not switch to it by itself: set its parameter `keyringFile` to that name and save the bridge.
A bridge looks for `keyringFile` in `conf/misc` first, as before, and in `userdata/knx/keyring` only when `conf/misc` has no file of that name; the answer says `shadowedByConfigFolder` when such a file is in the way.
With the password header the answer says whether the keyring's signature matches it; the keyring password itself goes into the bridge parameter `keyringPassword` as before.

The stream always answers with status 200.
Its first event is `open` with `lastNumber` and `monitorId` (and `reset: true` when the numbers given belong to another monitor or lie ahead, so the rows start again from the beginning of the buffer).
Then come `telegram` (one row), `outcome` (how an own send ended whose row came as `PENDING`), `lost` (how many rows left the buffer before they could be sent), and `alive` every 10 seconds with the number to read on after.
The last event is `end` with a code, such as `unknown-bridge`, `monitor-off`, `bad-input`, `too-many-streams` (8 streams in all), `slow-client` (a reader that took nothing for about 10 to 20 seconds while 256 events waited for it), `bridge-gone` or `shutdown`.
To read on without a hole, open the stream again with `after=<last number>&monitor=<monitorId>`; a row may come twice.

## Troubleshooting

### Extended Logging

Logging can be configured via the UI.
This affects only the log of the binding and does not include Calimero library used for KNX communication.
In special cases, it might be useful to set both KNX logging and Calimero logging.
This can be done in the openHAB console:

```shell
log:set TRACE org.openhab.binding.knx
log:set TRACE calimero
```

Note that this will lead to a few lines in the log for each KNX frame received or sent.

Logging can be set back to normal, similarly using:

```shell
log:set DEFAULT org.openhab.binding.knx
log:set DEFAULT calimero
```

### Serial Devices

In case you experience communication errors like incomplete or dropped frames, BCU reset errors in the log, or similar problems:

- Make sure that no other service accesses the serial port (e.g., `knxd`, `hciuart`, etc.).
Use `ps aux` and make sure none of those are running in parallel.
- Increase the log level also for Calimero library, see above.
- If the problem relates only to KNX data secure, please note that very old KNX devices do not support long frames, which are required for KNX data secure. Check your interface and the couplers (technical reference manual, ETS, or openHAB device info when fetch is enabled).

### kBerry Module for Raspberry Pi

#### Configuration of the kBerry Module

Uploading an application to the module is not necessary for use with openHAB.

#### Configuration of the Serial Port

Configuration of the serial port is necessary.
This is done by editing text files in the folder `/boot/firmware` on the Pi (for older distributions, it is `/boot`).
This can be done manually or using the tool `raspi-config`.

If something goes wrong, you can also recover using a Windows machine, as this partition is also accessible from Windows when using a micro SD card reader.

##### config.txt

The serial port (UART) needs to be activated on GPIO pins 14 and 15.
The configuration options have changed over time, depending on the Linux distribution and the hardware revision of the Raspberry Pi.

The following settings might work for you:

For Pi 5, add `dtoverlay=disable-bt` and `dtoverlay=uart0`.

For Pi 4, add `dtoverlay=disable-bt`.

For Pi 3, add `dtoverlay=pi3-miniuart-bt`.

##### cmdline.txt

Remove the part that refers to the serial port, e.g. `console=serial0,115200`.

Finally, reboot the Pi.

#### openHAB Configuration

Add a KNX Thing manually, KNX FT1.2 Interface, select `/dev/ttyAMA0` as port.
If you use file-based configuration, see the example above.
