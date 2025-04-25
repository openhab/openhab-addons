# PLCLogo Binding

This binding provides native support of Siemens LOGO! PLC devices.
Communication with LOGO! is done via Moka7 library.
Currently only two devices are supported: `0BA7` (LOGO! 7) and `0BA8` (LOGO! 8).
Additionally multiple devices are supported.
Different families of LOGO! devices should work also, but was not tested now due to lack of hardware.
Binding works nicely at least 100ms polling rate, if network connection is stable.

## Pitfalls

- Changing of block parameter while running the binding may kill your LOGO!, so that program flashing via LOGO! SoftComort
  will be required. Furthermore programs within LOGO! SoftComfort and LOGO! itself will differ, so that online simulation
  will not work anymore without program synchronisation.

- Flashing the LOGO! while running the binding may crash the network interface of your LOGO!. Before flashing the LOGO!
  with LOGO! SoftComfort stop openHAB service. If network interface is crashed, no reader could be created for this
  device. See troubleshooting section below how to recover.

## Discovery

Siemens LOGO! devices can be manually discovered by sending a request to every IP on the network.
This functionality should be used with caution, because it produces heavy load to the operating hardware.
For this reason, the binding does not do an automatic background discovery, but discovery can be triggered manually.

## Bridge configuration

Every Siemens LOGO! PLC is configured as bridge:

```java
Bridge plclogo:device:<DeviceId> [ address="<ip>", family="<0BA7/0BA8>", localTSAP="0x<number>", remoteTSAP="0x<number>", refresh=<number> ]
```

| Parameter  | Type    | Required   | Default   | Description                                                      |
| ---------- | :-----: | :--------: | :-------: | ---------------------------------------------------------------- |
| address    | String  | Yes        |           | IP address of the LOGO! PLC.                                     |
| family     | String  | Yes        |           | LOGO! family to communicate with. Can be `0BA7` or `0BA8` now.   |
| localTSAP  | String  | Yes        |           | TSAP (as hex) is used by the local instance. Check configuration |
|            |         |            |           | in LOGO!Soft Comfort. Common used value is `0x3000`.             |
| remoteTSAP | String  | Yes        |           | TSAP (as hex) of the remote LOGO! PLC, as configured by          |
|            |         |            |           | LOGO!Soft Comfort. Common used value is `0x2000`.                |
| refresh    | Integer | No         | 100ms     | Polling interval, in milliseconds. Is used for query the LOGO!.  |

Be sure not to use the same values for localTSAP and remoteTSAP, if configure more than one LOGO!

## Thing configuration

Binding supports four types of things: digital, analog, memory and datetime.

### Digital Things

The configuration pattern for digital things is:

```java
Thing digital <ThingId> "Label" @ "Location" [ kind="<kind>", force=<true/false> ]
```

| Parameter | Type    | Required   | Default   | Description                                                  |
| --------- | :-----: | :--------: | :-------: | ------------------------------------------------------------ |
| kind      | String  | Yes        |           | Blocks kind                                                  |
| force     | Boolean | No         | false     | Send current value to openHAB, independent if changed or not |

Follow block kinds are allowed for digital things:

| Type           | `0BA7` | `0BA8` |
| -------------- | :----: | ------ |
| Input          | `I`    | `I`    |
| Output         | `Q`    | `Q`    |
| Marker         | `M`    | `M`    |
| Network input  |        | `NI`   |
| Network output |        | `NQ`   |

### Analog Things

The configuration pattern for analog things is:

```java
Thing analog <ThingId>  "Label" @ "Location" [ kind="<kind>", threshold=<number>, force=<true/false> ]
```

| Parameter | Type    | Required   | Default   | Description                                                   |
| --------- | :-----: | :--------: | :-------: | ------------------------------------------------------------- |
| kind      | String  | Yes        |           | Blocks kind                                                   |
| threshold | Integer | No         | 0         | Send current value to openHAB, if changed more than threshold |
| force     | Boolean | No         | false     | Send current value to openHAB, independent if changed or not  |

Follow block kinds are allowed for analog things:

| Type           | `0BA7` | `0BA8` |
| -------------- | :----: | ------ |
| Input          | `AI`   | `AI`   |
| Output         | `AQ`   | `AQ`   |
| Marker         | `AM`   | `AM`   |
| Network input  |        | `NAI`  |
| Network output |        | `NAQ`  |

### Memory Things

The configuration pattern for analog things is:

```java
Thing memory <ThingId>  "Label" @ "Location" [ block="<name>", threshold=<number>, force=<true/false> ]
```

Follow block names are allowed for memory things:

| Type  | `0BA7`            | `0BA8`            |
| ----- | :---------------: | ----------------- |
| Bit   | `VB[0-850].[0-7]` | `VB[0-850].[0-7]` |
| Byte  | `VB[0-850]`       | `VB[0-850]`       |
| Word  | `VW[0-849]`       | `VW[0-849]`       |
| DWord | `VD[0-847]`       | `VD[0-847]`       |

Parameter `threshold` will be taken into account for Byte, Word and DWord, i.e Number items, only.

### DateTime Things

The configuration pattern for datetime things is:

```java
Thing datetime <ThingId>  "Label" @ "Location" [ block="<name>", type=<type>, force=<true/false> ]
```

Follow block names are allowed for datetime things:

| Type  | `0BA7`      | `0BA8`      |
| ----- | :---------: | ----------- |
| Word  | `VW[0-849]` | `VW[0-849]` |

If parameter `type` is `"date"`, then the binding will try to interpret incoming data as calendar date.
The time this case will be taken from openHAB host.
If `type` is set to `"time"`, then incoming data will be tried to interpret as time of day.
The date this case will be taken from openHAB host.

### Pulse Things

The configuration pattern for pulse things is:

```java
Thing pulse <ThingId>  "Label" @ "Location" [ block="<name>", observe="<name>", pulse=<number> ]
```

Follow block names are allowed for pulse things:

| Type  | `0BA7`            | `0BA8`            |
| ----- | :---------------: | ----------------- |
| Bit   | `VB[0-850].[0-7]` | `VB[0-850].[0-7]` |

Follow observed block names are allowed for pulse things:

| Type  | `0BA7`            | `0BA8`            |
| ----- | :---------------: | ----------------- |
| Bit   | `VB[0-850].[0-7]` | `VB[0-850].[0-7]` |
| Bit   | `I[1-24]`         | `I[1-24]`         |
| Bit   | `Q[1-16]`         | `Q[1-20]`         |
| Bit   | `M[1-27]`         | `M[1-64]`         |
| Bit   |                   | `NI[1-64]`        |
| Bit   |                   | `NQ[1-64]`        |

If `observe` is not set or set equal `block`, simply pulse with length `pulse` milliseconds is send to `block`.
If `observe` is set and differ from `block`, binding will wait for value change on `observe` and send then a pulse with length `pulse` milliseconds to block.
Please note, update rate for change detection depends on bridge refresh value.
For both use cases: if `block` was `0` then `1` is send and vice versa.

## Channels

### Bridge

Each device have currently three channels `diagnostic`, `rtc` and `weekday`:

```java
channel="plclogo:device:<DeviceId>:diagnostic"
channel="plclogo:device:<DeviceId>:rtc"
channel="plclogo:device:<DeviceId>:weekday"
```

Channels `diagnostic` and `weekday` supports `String` items. Channel `diagnostic` contains the last diagnostic message reported by LOGO!.
Channel `weekday` contains current day of the week.
The value is provided by LOGO!.
Channel `rtc` supports `DateTime` items only. Since Siemens `0BA7` (LOGO! 7) devices will not transfer any useful data for this channel, local time of openHAB host will be used.
Rather for Siemens `0BA8` (LOGO! 8) devices, the data will be read from PLC.
Since the smallest resolution provided by LOGO! is one second, `rtc` channel will be tried to update with the same rate.

### Digital

Format pattern for digital channels is

```java
channel="plclogo:digital:<DeviceId>:<ThingId>:<Channel>"
```

Dependent on configured LOGO! PLC and thing kind, follow channels are available:

| Kind | `0BA7`    | `0BA8`     | Item      |
| ---- | :-------: | :--------: | --------- |
| `I`  | `I[1-24]` | `I[1-24]`  | `Contact` |
| `Q`  | `Q[1-16]` | `Q[1-20]`  | `Switch`  |
| `M`  | `M[1-27]` | `M[1-64]`  | `Switch`  |
| `NI` |           | `NI[1-64]` | `Contact` |
| `NQ` |           | `NQ[1-64]` | `Switch`  |

### Analog

Format pattern for analog channels is

```java
channel="plclogo:analog:<DeviceId>:<ThingId>:<Channel>"
```

Dependent on configured LOGO! PLC and thing kind, follow channels are available:

| Kind  | `0BA7`     | `0BA8`      | Item     |
| ----- | :--------: | :---------: | -------- |
| `AI`  | `AI[1-8]`  | `AI[1-8]`   | `Number` |
| `AQ`  | `AQ[1-2]`  | `AQ[1-8]`   | `Number` |
| `AM`  | `AM[1-16]` | `AM[1-64]`  | `Number` |
| `NAI` |            | `NAI[1-32]` | `Number` |
| `NAQ` |            | `NAQ[1-16]` | `Number` |

### Memory

Format pattern for memory channels for bit values is

```java
channel="plclogo:memory:<DeviceId>:<ThingId>:<state/value>"
```

Dependent on configured LOGO! PLC and thing kind, follow channels are available:

| Kind              | `0BA7`  | `0BA8`  | Item     |
| ----------------- | :-----: | :-----: | -------- |
| `VB[0-850].[0-7]` | `state` | `state` | `Switch` |
| `VB[0-850]`       | `value` | `value` | `Number` |
| `VW[0-849]`       | `value` | `value` | `Number` |
| `VD[0-847]`       | `value` | `value` | `Number` |

### DateTime

Format pattern depends for date/time channels is

```java
channel="plclogo:datetime:<DeviceId>:<ThingId>:<date/time>"
```

Dependent on configured LOGO! PLC and thing kind, follow channels are available:

| Kind        | `0BA7`  | `0BA8`  | Item       |
| ----------- | :-----: | :-----: | ---------- |
| `VW[0-849]` | `date`  | `date`  | `DateTime` |
| `VW[0-849]` | `time`  | `time`  | `DateTime` |
| `VW[0-849]` | `value` | `value` | `Number`   |

Channel `date` is available, if thing is configured as `"date"`.
Is thing configured as `"time"`, then channel `time` is provided.
Raw block data is provided via `value` channel, independed from thing configuration:

```java
channel="plclogo:datetime:<DeviceId>:<ThingId>:value"
```

### Pulse

Format pattern depends for pulse channels is

```java
channel="plclogo:pulse:<DeviceId>:<ThingId>:state"
```

Additionally the state of observed block data is provided via `observed` channel

```java
channel="plclogo:pulse:<DeviceId>:<ThingId>:observed"
```

Dependent on configured LOGO! PLC and thing kind, follow channels are available:

| Kind              | `0BA7`     | `0BA8`     | Item      |
| ----------------- | :--------: | :--------: | --------- |
| `VB[0-850].[0-7]` | `state`    | `state`    | `Switch`  |
| `VB[0-850].[0-7]` | `observed` | `observed` | `Switch`  |
| `I[1-24]`         | `observed` | `observed` | `Contact` |
| `Q[1-16/20]`      | `observed` | `observed` | `Switch`  |
| `M[1-27/64]`      | `observed` | `observed` | `Switch`  |
| `NI[1-64]`        |            | `observed` | `Contact` |
| `NQ[1-64]`        |            | `observed` | `Switch`  |

## Examples

Configuration of one Siemens LOGO!

logo.things:

```java
Bridge plclogo:device:Logo [ address="192.168.0.1", family="0BA8", localTSAP="0x3000", remoteTSAP="0x2000", refresh=100 ]
{
  Thing digital  Inputs  [ kind="I" ]
  Thing digital  Outputs [ kind="Q" ]
  Thing memory   VW100 [ block="VW100", threshold=1, force=true ]
  Thing datetime VW102 [ block="VW102", type="time" ]
  Thing datetime VW150 [ block="VW150", type="date" ]
  Thing pulse    VB0_1 [ block="VB0.1", observe="Q1", pulse=500 ]
}
```

logo.items:

```java
Contact LogoI1   { channel="plclogo:digital:Logo:Inputs:I1" }
Contact LogoI2   { channel="plclogo:digital:Logo:Inputs:I2" }
Switch  LogoQ1   { channel="plclogo:digital:Logo:Outputs:Q1" }
Switch  LogoQ2   { channel="plclogo:digital:Logo:Outputs:Q2" }
Number  Position { channel="plclogo:memory:Logo:VW100:value" }

DateTime LogoTime { channel="plclogo:datetime:Logo:VW102:time" }
DateTime LogoDate { channel="plclogo:datetime:Logo:VW150:date" }

Switch  LogoVB1_S { channel="plclogo:pulse:Logo:VB0_1:state"}
Switch  LogoVB1_O { channel="plclogo:pulse:Logo:VB0_1:observed"}

String   Diagnostic { channel="plclogo:device:Logo:diagnostic"}
DateTime RTC        { channel="plclogo:device:Logo:rtc"}
String   DayOfWeek  { channel="plclogo:device:Logo:weekday"}
```

Configuration of two Siemens LOGO!

logo.things:

```java
Bridge plclogo:device:Logo1 [ address="192.168.0.1", family="0BA8", localTSAP="0x3000", remoteTSAP="0x2000", refresh=100 ]
{
  Thing digital Inputs  [ kind="I" ]
  Thing digital Outputs [ kind="Q" ]
  Thing memory  VW100   [ block="VW100", threshold=1 ]
  Thing pulse   VB0_0   [ block="VB0.0", observe="NI1", pulse=250 ]
}
Bridge plclogo:device:Logo2 [ address="192.168.0.2", family="0BA8", localTSAP="0x3100", remoteTSAP="0x2000", refresh=100 ]
{
  Thing digital Inputs  [ kind="I" ]
  Thing digital Outputs [ kind="Q" ]
  Thing memory  VD102   [ block="VD102", threshold=1 ]
  Thing pulse   VB0_1   [ block="VB0.1", observe="VB0.1", pulse=500 ]
}
```

logo.items:

```java
Contact Logo1_I1    { channel="plclogo:digital:Logo1:Inputs:I1" }
Contact Logo1_I2    { channel="plclogo:digital:Logo1:Inputs:I2" }
Switch  Logo1_Q1    { channel="plclogo:digital:Logo1:Outputs:Q1" }
Switch  Logo1_Q2    { channel="plclogo:digital:Logo1:Outputs:Q2" }
Number  Logo1_VW100 { channel="plclogo:memory:Logo1:VW100:value" }
Switch  Logo1_VB0_S { channel="plclogo:pulse:Logo1:VB0_0:state"}
Contact Logo1_VB0_O { channel="plclogo:pulse:Logo1:VB0_0:observed"}
DateTime Logo1_RTC  { channel="plclogo:device:Logo1:rtc"}

Contact Logo2_I1    { channel="plclogo:digital:Logo2:Inputs:I1" }
Contact Logo2_I2    { channel="plclogo:digital:Logo2:Inputs:I2" }
Switch  Logo2_Q1    { channel="plclogo:digital:Logo2:Outputs:Q1" }
Switch  Logo2_Q2    { channel="plclogo:digital:Logo2:Outputs:Q2" }
Number  Logo2_VD102 { channel="plclogo:memory:Logo2:VD102:value" }
Switch  Logo2_VB1_S { channel="plclogo:pulse:Logo2:VB0_1:state"}
Switch  Logo2_VB1_O { channel="plclogo:pulse:Logo2:VB0_1:observed"}
DateTime Logo2_RTC  { channel="plclogo:device:Logo2:rtc"}
```

## Troubleshooting

### LOGO! bridge will not go online

Be sure to have only one bridge for each LOGO! device.

### Log shows reader was created but no communication with LOGO! possible

Check TSAP values: localTSAP and remoteTSAP should not be the same.
You have to choose different addresses.

**openHAB is starting without errors but no reader was created for the LOGO!**

If all configuration parameters were checked and fine, it maybe possible that the network interface of the LOGO! is crashed.
To recover stop openHAB, cold boot your LOGO! (power off/on) and reflash the program with LOGO! SoftComfort.
Then restart openHAB and check logging for a created reader.

### RTC value differs from the value shown in LOGO! (0BA7)

This is no bug! Since there is no way to read the RTC from a 0BA7, the binding simply returns the local time of openHAB host.
