# Modbus Binding

This binding supports generic Modbus TCP and serial slave devices.
And it has specialist extensions for the following manufacturers products:

<!--list-subs-->

RTU, ASCII and BIN variants of Serial Modbus are supported.
Modbus TCP slaves are usually also called as Modbus TCP servers.

The binding can act as

- Modbus TCP Client (that is, as modbus master), querying data from Modbus TCP servers (that is, modbus slaves)
- Modbus serial master, querying data from modbus serial slaves

The Modbus binding polls the slave data with a configurable poll period.
openHAB commands are translated to write requests.

The rest of this page contains details for configuring this binding:

[[toc]]

## Main Features

The binding polls (or _reads_) Modbus data using function codes (FC) FC01 (Read coils), FC02 (Read discrete inputs), FC03 (Read multiple holding registers) or FC04 (Read input registers).
This polled data is converted to data suitable for use in openHAB.
Functionality exists to interpret typical number formats (e.g. single precision float).

The binding can also _write_ data to Modbus slaves using FC05 (Write single coil), FC06 (Write single holding register), FC15 (Write multiple coils) or FC16 (Write multiple holding registers).

## Caveats And Limitations

Please note the following caveats or limitations

- The binding does _not_ act as Modbus slave (e.g. as Modbus TCP server).
- The binding _does_ support Modbus RTU over Modbus TCP, (also known as "Modbus over TCP/IP" or "Modbus over TCP" or "Modbus RTU/IP"), as well as normal "Modbus TCP".

## Background Material

Reader of the documentation should understand the basics of Modbus protocol.
Good sources for further information:

- [Wikipedia article](https://en.wikipedia.org/wiki/Modbus): good read on modbus basics and addressing.
- [Simplymodbus.ca](https://www.simplymodbus.ca/): good reference as well as excellent tutorial like explanation of the protocol

Useful tools

- [binaryconvert.com](https://www.binaryconvert.com/): tool to convert numbers between different binary presentations
- [rapidscada.net Modbus parser](https://rapidscada.net/modbus/): tool to parse Modbus requests and responses. Useful for debugging purposes when you want to understand the message sent / received.
- [JSFiddle tool](https://jsfiddle.net/rgypuuxq/) to test JavaScript (JS) transformations interactively

## Supported Things

This binding supports five different Thing types.

| Thing     | Type   | Description                                                                                                                                                                                                                               |
| --------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `tcp`     | Bridge | Modbus TCP server (Modbus TCP slave)                                                                                                                                                                                                      |
| `serial`  | Bridge | Modbus serial slave                                                                                                                                                                                                                       |
| `poller2` | Bridge | Modern poller that owns configured read channels. It is a child of `tcp` or `serial`.                                                                                                                                                     |
| `poller`  | Bridge | Thing taking care of polling the data from modbus slaves. One poller corresponds to single Modbus read request (FC01, FC02, FC03, or FC04). Is child of `tcp` or `serial`.                                                                |
| `data`    | Thing  | Thing for converting polled data to meaningful numbers. Analogously, is responsible of converting openHAB commands to Modbus write requests. Is child of `poller` (read-only or read-write things) or `tcp`/`serial` (write-only things). |

Typically one defines either `tcp` or `serial` bridge, depending on the variant of Modbus slave.

### `poller2` configuration

`poller2` is the modern, channel-owned topology.
Create it below a `tcp` or `serial` bridge; it performs one Modbus read request, and its channels expose values within that request.
Use a separate `poller2` bridge when the Modbus function, address range, or refresh interval differs.

Use Main UI to create the bridge and custom channels, or use the native YAML syntax below.
These are configuration examples, not UI evidence.

```yaml
version: 1
things:
  # FC03: holding registers. The channel can also accept commands.
  modbus:poller2:plant:holding:
    bridge: modbus:tcp:plant
    config:
      start: 100
      length: 4
      type: holding
      refresh: 1000
    channels:
      setpoint:
        itemType: Number
        config:
          address: "100"
          valueType: int16
          writeStart: "100"
          writeValueType: int16
          writeMaxTries: 3

  # FC04: input registers. `itemDimension` makes this a Number:Temperature channel.
  modbus:poller2:plant:inputs:
    bridge: modbus:tcp:plant
    config:
      start: 300
      length: 2
      type: input
      refresh: 5000
    channels:
      supply-temperature:
        itemType: Number
        itemDimension: Temperature
        config:
          address: "300"
          valueType: int16

  # FC01: coils, including an optional write endpoint.
  modbus:poller2:plant:coils:
    bridge: modbus:tcp:plant
    config:
      start: 10
      length: 2
      type: coil
      refresh: 1000
    channels:
      pump-enable:
        itemType: Switch
        config:
          address: "10"
          valueType: bit
          writeStart: "10"

  # FC02: discrete inputs are read-only bits.
  modbus:poller2:plant:discrete:
    bridge: modbus:tcp:plant
    config:
      start: 20
      length: 2
      type: discrete
      refresh: 1000
    channels:
      alarm:
        itemType: Contact
        config:
          address: "20"
          valueType: bit

  # Keep the last known value during a failed FC03 read.
  modbus:poller2:plant:resilient:
    bridge: modbus:tcp:plant
    config:
      start: 400
      length: 1
      type: holding
      refresh: 1000
    channels:
      pressure:
        itemType: Number
        config:
          address: "400"
          valueType: uint16
          errorPolicy: keepLast

  # A raw, two-register value is published as "1234ABCD".
  modbus:poller2:plant:raw:
    bridge: modbus:tcp:plant
    config:
      start: 500
      length: 2
      type: input
      refresh: 1000
    channels:
      device-frame:
        itemType: String
        config:
          address: "500"
          valueType: raw
          length: 2
```

A channel's `itemType` selects the Item type it accepts.
`itemDimension` is optional and turns, for example, `Number` into `Number:Temperature`.
The binding assigns generated ChannelTypes so these channels appear as ordinary typed channel metadata in Main UI.
**Do not add a YAML `type:` field or configure a generated ChannelType identifier.**

For a coil or discrete-input pollers, channels use `address` and `valueType: bit`.
For a raw register channel, use `valueType: raw` with `address` and a positive integer `length`.
A channel can write only when its poller type is `holding` or `coil`: set `writeStart`; holding-register writes also require `writeValueType`, while coil writes use the bit value type.
Optional `readTransform`, `writeTransform`, and `writeMultipleEvenWithSingleRegisterOrCoil` follow the existing transformation and write behavior described later in this document.

### `poller2` coexistence and timing boundaries

Multiple `poller2` bridges can be children of the same `tcp` or `serial` bridge, including when their read ranges overlap.
They use that endpoint bridge's communication interface; a completed read is delivered only to the poller that submitted it.
A read failure changes the failing poller's status and a later successful response recovers that poller without changing a sibling poller's status.

`poller2` can also remain alongside the legacy `poller` + `data` topology while migrating.
Configure the endpoint's timing and connection settings for the combined transaction load: `timeBetweenTransactionsMillis` is the minimum delay between consecutive Modbus transactions, `connectMaxTries` controls connection attempts, and each poller's `maxTries` controls attempts for its own read request.
The endpoint does not promise a fixed ordering or schedule when several pollers are due at the same time.
For serial endpoints, automatic reconnect is disabled; account for the device's serial timing and `receiveTimeoutMillis`.

These compatibility statements cover pollers managed by this binding under one endpoint bridge.
They do not establish coordination with another client or integration that talks to the same device, and they do not establish safe concurrent writes to overlapping device addresses.
Keep write ownership and device-specific write rules explicit.

#### Poller settings

| Setting | Required | Default | Meaning |
| --- | --- | --- | --- |
| `start` | no | `0` | Zero-based first coil, discrete input, or register address. |
| `length` | yes | — | Number of values requested by the Modbus read. |
| `type` | yes | — | `coil` (FC01), `discrete` (FC02), `holding` (FC03), or `input` (FC04). |
| `refresh` | no | `500` ms | Poll interval; use `0` to poll only after an Item sends `REFRESH`. |
| `maxTries` | no | `3` | Read attempts before reporting a failed poll. |
| `cacheMillis` | no | `50` ms | How long a prior response may satisfy `REFRESH`. Set `0` to disable that cache. |

#### Channel settings, addressing, and failures

All addresses are Modbus data-frame addresses: zero-based values sent to the device unchanged.
A device manual's `40001`, `30001`, `00001`, or `10001` notation commonly labels the first address as `0`; confirm the manufacturer's convention before configuring it.
A channel must fit completely within the poller's inclusive `start` through `start + length - 1` window.

| Poller type | Required channel configuration | Notes |
| --- | --- | --- |
| `holding` or `input` | `address` (text) and `valueType` | Use normal Modbus value types such as `int16`, `uint16`, `float32`, or their documented swapped forms. Register sub-values use `X.Y`: bit `Y` is 0–15 and byte `Y` is 0–1. |
| `coil` or `discrete` | `address` (text); `valueType: bit` is optional but recommended | Address is an integer only; `X.Y` is not valid. |
| `holding` or `input` raw data | `address`, `valueType: raw`, and positive integer `length` | Address is an integer and the complete raw range must be in the poll window. |

Set `errorPolicy: keepLast` on numeric or raw register channels to retain their most recently successful state after a failed read.
The default `errorPolicy: undef` publishes `UNDEF`.
Coil and discrete channels publish `UNDEF` on a failed read.

#### Raw hexadecimal data and BIN2JSON

A `raw` register channel publishes contiguous registers as uppercase hexadecimal, four characters per register, in register order.
For example, registers `0x1234` and `0xABCD` produce the `String` state `1234ABCD`.
This is suitable input for the [BIN2JSON transformation](https://www.openhab.org/addons/transformations/bin2json/): link the raw `String` channel to a `String` Item and apply the transformation in the Item link or profile according to the BIN2JSON add-on documentation.
`raw` is read-only; it does not decode a number or write registers itself.

#### Writes and failure behavior

A configured `writeStart` makes a channel commandable only on `holding` or `coil` pollers.
Holding-register writes require `writeValueType`; a holding bit overlay uses `writeStart: "X.Y"` with `writeValueType: bit` (`Y` is 0–15).
The binding needs a successful poll of that same register before it can preserve the other bits, so command it only after the poller has obtained a valid value.
Coil writes use the bit value type and do not accept a bit sub-index.
`input` and `discrete` pollers are read-only.

`writeMaxTries` defaults to `3` and must be at least `1`.
Set `writeMultipleEvenWithSingleRegisterOrCoil: true` only when the device requires FC16 for a single holding register or FC15 for a single coil; the default uses FC06 or FC05.
`readTransform` and `writeTransform` accept the same ordinary transformation syntax used by the binding.
JSON-producing write transforms are legacy `data`-Thing functionality; see [the legacy reference](doc/legacy-poller.md#json-write-transformations-legacy-data-things-only).

#### Thing actions

The `modbus` Thing action namespace provides typed asynchronous writes for a configured commandable channel:

```java
// Rules DSL: retrieve actions for the specific poller2 Thing.
val modbusActions = getActions("modbus", "modbus:poller2:plant:holding")
modbusActions.writeHolding("setpoint", 21)
modbusActions.writeHolding("setpoint", 21, true) // request FC16 for this one write

val coilActions = getActions("modbus", "modbus:poller2:plant:coils")
coilActions.writeCoil("pump-enable", true)
coilActions.writeCoil("pump-enable", true, true) // request FC15 for this one write
```

The action returns whether it was accepted for asynchronous execution; it is not a device-write success confirmation.
Use an existing configured channel ID with the matching type.
A later successful poll reconciles the channel state; failed writes put the Thing offline with a communication error.

#### Migration from legacy `poller` + `data`

New work should use `poller2`.
Existing legacy configurations remain supported and are not removed by migration.
Follow [Legacy Poller Migration](doc/legacy-poller-migration.md) to back up/export first, create a preview, review its YAML and manual-work notices, apply only the reviewed preview identity, verify live device values and commands, then either roll back generated targets or clean up legacy configuration separately.
The [legacy-only reference](doc/legacy-poller.md) preserves `poller`/`data` and JSON-write-transform configuration details.

## Binding Configuration

Other than the things themselves, there is no binding configuration.

## Serial Port Configuration

With serial Modbus slaves, configuration of the serial port in openHAB is important.
Otherwise you might encounter errors preventing all communication.

See [general documentation about serial port configuration](/docs/administration/serial.html) to configure the serial port correctly.

## Thing Configuration

In the tables below the Thing configuration parameters are grouped by Thing type.

Things can be configured using the UI, or using a `.things` file.
The configuration in this documentation explains the `.things` file, although you can find the same parameters in the UI.

Note that parameter type is very critical when writing `.things` file yourself, since it affects how the parameter value is encoded in the text file.

Some examples:

- `parameter="value"` for `text` parameters
- `parameter=4` for `integer`
- `parameter=true` for `boolean`

Note the differences with quoting.

Required parameters _must_ be specified in the `.things` file.
When optional parameters are not specified, they default to the values shown in the table below.

### `tcp` Thing

`tcp` is representing a particular Modbus TCP server (slave).

Basic parameters

| Parameter    | Type    | Required | Default if omitted | Description                                                 |
| ------------ | ------- | -------- | ------------------ | ----------------------------------------------------------- |
| `host`       | text    |          | `"localhost"`      | IP Address or hostname                                      |
| `port`       | integer |          | `502`              | Port number                                                 |
| `id`         | integer |          | `1`                | Slave id. Also known as station address or unit identifier. |
| `rtuEncoded` | boolean |          | `false`            | Use RTU encoding instead of regular TCP encoding.           |

Advanced parameters

| Parameter                       | Required | Type    | Default if omitted | Description                                                                                                                                                                                   |
|---------------------------------|----------|---------|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `timeBetweenTransactionsMillis` |          | integer | `60`               | How long to delay we must have at minimum between two consecutive MODBUS transactions. In milliseconds.                                                                                       |
| `timeBetweenReconnectMillis`    |          | integer | `0`                | How long to wait to before trying to establish a new connection after the previous one has been disconnected. In milliseconds.                                                                |
| `connectMaxTries`               |          | integer | `1`                | How many times we try to establish the connection. Should be at least 1.                                                                                                                      |
| `afterConnectionDelayMillis`    |          | integer | `0`                | Connection warm-up time. Additional time which is spent on preparing connection which should be spent waiting while end device is getting ready to answer first modbus call. In milliseconds. |
| `reconnectAfterMillis`          |          | integer | `0`                | The connection is kept open at least the time specified here. Value of zero means that connection is disconnected after every MODBUS transaction. In milliseconds.                            |
| `connectTimeoutMillis`          |          | integer | `10000`            | The maximum time that is waited when establishing the connection. Value of zero means that system/OS default is respected. In milliseconds.                                                   |
| `enableDiscovery`               |          | boolean | false              | Enable auto-discovery feature. Effective only if a supporting extension has been installed.                                                                                                   |

**Note:** Advanced parameters must be equal for all `tcp` things sharing the same `host` and `port`.

The advanced parameters have conservative defaults, meaning that they should work for most users.
In some cases when extreme performance is required (e.g. poll period below 10 ms), one might want to decrease the delay parameters, especially `timeBetweenTransactionsMillis`.
Similarly, with some slower devices on might need to increase the values.

### `serial` Thing

`serial` is representing a particular Modbus serial slave.

Basic parameters

| Parameter | Type    | Required | Default if omitted | Description                                                                                                                                                                                                |   |
|-----------|---------|----------|--------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---|
| port      | text    | ✓        |                    | Serial port to use, for example `"/dev/ttyS0"` or `"COM1"`                                                                                                                                                 |   |
| id        | integer |          | `1`                | Slave id. Also known as station address or unit identifier. See [Wikipedia](https://en.wikipedia.org/wiki/Modbus) and [simplymodbus](https://www.simplymodbus.ca/index.html) articles for more information |   |
| baud      | integer | ✓        |                    | Baud of the connection. Valid values are: `75`, `110`, `300`, `1200`, `2400`, `4800`, `9600`, `19200`, `38400`, `57600`, `115200`.                                                                         |   |
| stopBits  | text    | ✓        |                    | Stop bits. Valid values are: `"1.0"`, `"1.5"`, `"2.0"`.                                                                                                                                                    |   |
| parity    | text    | ✓        |                    | Parity. Valid values are: `"none"`, `"even"`, `"odd"`.                                                                                                                                                     |   |
| dataBits  | integer | ✓        |                    | Data bits. Valid values are: `5`, `6`, `7` and `8`.                                                                                                                                                        |   |
| encoding  | text    |          | `"rtu"`            | Encoding. Valid values are: `"ascii"`, `"rtu"`, `"bin"`.                                                                                                                                                   |   |
| echo      | boolean |          | `false`            | Flag for setting the RS485 echo mode. This controls whether we should try to read back whatever we send on the line, before reading the response. Valid values are: `true`, `false`.                       |   |

Advanced parameters

| Parameter                       | Required | Type    | Default if omitted | Description                                                                                                                                                                                   |
|---------------------------------|----------|---------|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `receiveTimeoutMillis`          |          | integer | `1500`             | Timeout for read operations. In milliseconds.                                                                                                                                                 |
| `flowControlIn`                 |          | text    | `"none"`           | Type of flow control for receiving. Valid values are: `"none"`, `"xon/xoff in"`, `"rts/cts in"`.                                                                                              |
| `flowControlOut`                |          | text    | `"none"`           | Type of flow control for sending. Valid values are: `"none"`, `"xon/xoff out"`, `"rts/cts out"`.                                                                                              |
| `timeBetweenTransactionsMillis` |          | integer | `35`               | How long to delay we must have at minimum between two consecutive MODBUS transactions. In milliseconds.                                                                                       |
| `connectMaxTries`               |          | integer | `1`                | How many times we try to establish the connection. Should be at least 1.                                                                                                                      |
| `afterConnectionDelayMillis`    |          | integer | `0`                | Connection warm-up time. Additional time which is spent on preparing connection which should be spent waiting while end device is getting ready to answer first modbus call. In milliseconds. |
| `connectTimeoutMillis`          |          | integer | `10000`            | The maximum time that is waited when establishing the connection. Value of zero means thatsystem/OS default is respected. In milliseconds.                                                    |
| `enableDiscovery`               |          | boolean | false              | Enable auto-discovery feature. Effective only if a supporting extension has been installed.                                                                                                   |

With the exception of `id` parameters should be equal for all `serial` things sharing the same `port`.

These parameters have conservative defaults, meaning that they should work for most users.
In some cases when extreme performance is required (e.g. poll period below 10ms), one might want to decrease the delay parameters, especially `timeBetweenTransactionsMillis`.
With some slower devices on might need to increase the values.

With low baud rates and/or long read requests (that is, many items polled), there might be need to increase the read timeout `receiveTimeoutMillis` to e.g. `5000` (=5 seconds).

### Legacy configuration

The original `poller` + `data` configuration, its channels, and its examples are documented only in the [legacy reference](doc/legacy-poller.md).
New configurations should use [`poller2`](#poller2-configuration).

### Discovery

Device specific modbus bindings can take part in the discovery of things, and detect devices automatically. The discovery is initiated by the `tcp` and `serial` bridges when they have `enableDiscovery` setting enabled.

Note that the main binding does not recognize any devices, so it is pointless to turn this on unless you have a suitable add-on binding installed.

## Details

### Comment On Addressing

[Modbus Wikipedia article](https://en.wikipedia.org/wiki/Modbus#Coil.2C_discrete_input.2C_input_register.2C_holding_register_numbers_and_addresses) summarizes this excellently:

> In the traditional standard, [entity] numbers for those entities start with a digit, followed by a number of four digits in range 1–9,999:
>
> - coils numbers start with a zero and then span from 00001 to 09999
> - discrete input numbers start with a one and then span from 10001 to 19999
> - input register numbers start with a three and then span from 30001 to 39999
> - holding register numbers start with a four and then span from 40001 to 49999
>
> This translates into [entity] addresses between 0 and 9,998 in data frames.

Note that entity begins counting at 1, data frame address at 0.

The openHAB modbus binding uses data frame entity addresses when referring to modbus entities.
That is, the entity address configured in modbus binding is passed to modbus protocol frame as-is.
For example, Modbus `poller` Thing with `start=3`, `length=2` and `type=holding` will read modbus entities with the following numbers 40004 and 40005.
The manufacturer of any modbus device may choose to use either notation, you may have to infer which, or use trial and error.

### Value Types On Read And Write

This section explains the detailed descriptions of different value types on read and write.
Note that value types less than 16 bits are not supported for scalar holding-register writes, except the documented `X.Y` bit overlay.

See [Full examples](#full-examples) section for practical examples.

#### `bit`

- a single bit is read from the registers
- address is given as `X.Y`, where `Y` is between 0...15 (inclusive), representing bit of the register `X`
- index `Y=0` refers to the least significant bit
- index `Y=1` refers to the second least significant bit, etc.

#### `int8`

- a byte (8 bits) from the registers is interpreted as signed integer
- address is given as `X.Y`, where `Y` is between 0...1 (inclusive), representing byte of the register `X`
- index `Y=0` refers to low byte
- index `Y=1` refers to high byte
- it is assumed that each high and low byte is encoded in most significant bit first order

#### `uint8`

- same as `int8` except value is interpreted as unsigned integer

#### `int16`

- register with index is interpreted as 16 bit signed integer.
- it is assumed that register is encoded in most significant bit first order

#### `uint16`

- same as `int16` except value is interpreted as unsigned integer

#### `int32`

- registers `index` and `(index + 1)` are interpreted as signed 32bit integer
- it assumed that the first register contains the most significant 16 bits
- it is assumed that each register is encoded in most significant bit first order

#### `uint32`

- same as `int32` except value is interpreted as unsigned integer

#### `float32`

- registers `index` and `(index + 1)` are interpreted as signed 32bit floating point number
- it assumed that the first register contains the most significant 16 bits
- it is assumed that each register is encoded in most significant bit first order

#### `int64`

- registers `index`, `(index + 1)`, `(index + 2)`, `(index + 3)` are interpreted as signed 64bit integer.
- it assumed that the first register contains the most significant 16 bits
- it is assumed that each register is encoded in most significant bit first order

#### `uint64`

- same as `int64` except value is interpreted as unsigned integer

The MODBUS specification defines each 16bit word to be encoded as Big Endian,
but there is no specification on the order of those words within 32bit or larger data types.
The net result is that when you have a master and slave that operate with the same Endian mode things work fine,
but add a device with a different Endian mode and it is very hard to correct.
To resolve this the binding supports a second set of valuetypes that have the words swapped.

If you get strange values using the `int32`, `uint32`, `float32`, `int64`, or `uint64` valuetypes then just try the `int32_swap`, `uint32_swap`, `float32_swap`, `int64_swap`, or `uint64_swap` valuetype, depending upon what your data type is.

#### `int32_swap`

- registers `index` and `(index + 1)` are interpreted as signed 32bit integer
- it assumed that the first register contains the least significant 16 bits
- it is assumed that each register is encoded in most significant bit first order (Big Endian)

#### `uint32_swap`

- same as `int32_swap` except value is interpreted as unsigned integer

#### `float32_swap`

- registers `index` and `(index + 1)` are interpreted as signed 32bit floating point number
- it assumed that the first register contains the least significant 16 bits
- it is assumed that each register is encoded in most significant bit first order (Big Endian)

#### `int64_swap`

- same as `int64` but registers swapped, that is, registers (index + 3), (index + 2), (index + 1), (index + 1) are interpreted as signed 64bit integer

#### `uint64_swap`

- same as `uint64` except value is interpreted as unsigned integer

### Legacy operation and transformations

Legacy `poller`/`data` refresh behavior, data-channel conversion, JSON writes, and the associated examples are retained in the [legacy reference](doc/legacy-poller.md).
For `poller2`, use the channel settings above, send `REFRESH` to a linked Item for an on-demand poll, and use scalar configured writes or [Thing actions](#thing-actions).

## Full Examples

Things can be configured in the UI, or using a `things` file like here.

Legacy `poller` + `data` examples are in the [legacy reference](doc/legacy-poller.md).
Native YAML `poller2` examples are in the [configuration section](#poller2-configuration).

## Troubleshooting

Modbus, while simple at its heart, potentially is a complicated standard to use because there's a lot of freedom (and bugs) when it comes to implementations.
There are many device or vendor specific quirks and wrinkles you might stumble across. Here's some:

- With Modbus TCP devices, there may be multiple network interfaces available, e.g. Wifi and wired Ethernet. However, with some devices the Modbus data is accessible via only one of the interfaces. You need to check the device manufacturer manual, or simply try out which of the IPs are returning valid modbus data.
Attention: a device may have an interface with a port open (502 or other) that it responds to Modbus requests on, but that may have no connection to the real bus hardware, resulting in generic Modbus error responses to _every_ request.
So check ALL interfaces. Usually either the IP on Ethernet will do.

- some devices do not allow to query a range of registers that is too large or spans reserved registers. Do not poll more than 123 registers.
Devices may respond with an error or no error but invalid register data so this error can easily go undedetected.
Turn your poller Thing into multiple things to cover smaller ranges to work around this problem.

- there's potentially many more or less weird inconsistencies with some devices.
  If you fail to read a register or you only ever get invalid values (such as 00 or FF bytes), try with various poller lengths such as the exact length of a register in question or twice the amount.
  In extreme cases you might even need more than a poller for a single register so you have two or more poller with two or more data things and need to combine these into another item using a rule.

## Changes From Modbus 1.x Binding

The 1.x binding configuration model is not directly compatible with Thing-based configuration.
Convert addresses to zero-based data-frame addresses, preserve device byte/word order, and configure the resulting endpoint and `poller2` channels as described above.
Back up the old configuration and validate each live value and command before removing it.

## Troubleshooting Tips

### Thing Status

Check Thing status for errors in configuration or communication.

### Enable Verbose Logging

Enable `DEBUG` or `TRACE` (even more verbose) logging for the loggers named:

- `org.openhab.binding.modbus`
- `org.openhab.core.io.transport.modbus`
- `net.wimpi.modbus`

Consult [openHAB logging documentation](https://www.openhab.org/docs/administration/logging.html#defining-what-to-log) for more information.

## For Developers

This binding can be extended in many ways.
If you have a Modbus enabled device that you want to support in openHAB please read the [developer section](https://github.com/openhab/openhab-addons/blob/main/bundles/org.openhab.binding.modbus/DEVELOPERS.md).
