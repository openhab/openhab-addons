# PLCLogo Binding

This binding provides native support of Siemens LOGO! PLC devices. Communication with Logo is done via Moka7 library.
Currently only two devices are supported: `0BA7` (LOGO! 7) and `0BA8` (LOGO! 8). Additionally multiple devices are supported.
Different families of LOGO! devices should work also, but was not tested now due to lack of hardware.
Binding works nicely at least 100ms polling rate, if network connection is stable.

## Pitfalls
- Changing of block parameter while running the binding may kill your LOGO!, so that program flashing via LOGO! SoftComort will be required. Furthermore programs within LOGO! SoftComfort and LOGO! itself will differ, so that online simulation will not work anymore without program synchronisation.
- Flashing the LOGO! while running the binding may crash the network interface of your LOGO!. Before flashing the LOGO! with LOGO! SoftComfort stop openHAB service. If network interface is crashed, no reader could be created for this device. See troubleshooting section below how to recover.

## Discovery

Siemens LOGO! devices can be manually discovered by sending a request to every IP on the network.
This functionality should be used with caution, because it produces heavy load to the operating hardware.
For this reason, the binding does not do an automatic background discovery, but discovery can be triggered manually.

## Bridge configuration

Every Siemens LOGO! PLC is configured as bridge:

```
Bridge plclogo:device:<DeviceId> [ address="<ip>", family="<0BA7/0BA8>", localTSAP="0x<number>", remoteTSAP="0x<number>", refresh=<number> ]
```

| Parameter  | Type    | Required   | Default   | Description                                                      |
| ---------- | :-----: | :--------: | :-------: | ---------------------------------------------------------------- |
| address    | String  | Yes        |           | IP address of the LOGO! PLC.                                     |
| family     | String  | Yes        |           | LOGO! family to communicate with. Can be `0BA7` or `0BA8` now.   |
| localTSAP  | String  | Yes        |           | TSAP (as hex) is used by the local instance. Check configuration |
|            |         |            |           | in LOGO!Soft Comfort. Common used value is `0x0300`.             |
| remoteTSAP | String  | Yes        |           | TSAP (as hex) of the remote LOGO! PLC, as configured by          |
|            |         |            |           | LOGO!Soft Comfort. Common used value is `0x0200`.                |
| refresh    | Integer | No         | 100ms     | Polling interval, in milliseconds. Is used for query the LOGO!.  |

Be sure not to use the same values for localTSAP and remoteTSAP, if configure more than one LOGO!

## Thing configuration

Binding supports two types of things: digital and analog.

### Digital Things
The configuration pattern for digital things is:

```
Thing digital <ThingId> "Label" @ "Location" [ block="<name>", force=<true/false> ]
```

| Parameter | Type    | Required   | Default   | Description                                                  |
| --------- | :-----: | :--------: | :-------: | ------------------------------------------------------------ |
| block     | String  | Yes        |           | Block name                                                   |
| force     | Boolean | No         | false     | Send current value to openHAB, independent if changed or not |

Follow block names are allowed for digital things:

| Type           | `0BA7`              | `0BA8`            | 
| -------------- | :-----------------: | :---------------: |
| Input          | `I[1-24]`           | `I[1-24]`         |
| Output         | `Q[1-16]`           | `Q[1-20]`         |
| Marker         | `M[1-27]`           | `M[1-64]`         |
| Network input  |                     | `NI[1-64]`        |
| Network output |                     | `NQ[1-64]`        |
| Memory         | `VB[0-850].[0-7]`   | `VB[0-850].[0-7]` |

Please, consider `openHAB` and/or `Eclipse SmartHome` documentation for details.

### Analog Things
The configuration pattern for analog things is as follow

```
Thing analog <ThingId>  "Label" @ "Location" [ block="<name>", threshold=<number>, force=<true/false>, type="<number/date/time>" ]
```

| Parameter | Type    | Required   | Default   | Description                                                   |
| --------- | :-----: | :--------: | :-------: | ------------------------------------------------------------- |
| block     | String  | Yes        |           | Block name                                                    |
| threshold | Integer | No         | 0         | Send current value to openHAB, if changed more than threshold |
| force     | Boolean | No         | false     | Send current value to openHAB, independent if changed or not  |
| type      | String  | No         | "number"  | Configure how to interpret data fetched from LOGO! device     |

If parameter `type` is `"number"`, incomig data will be interpret as numeric value. In this case, the appropriate
channel must be linked to an `Number` item. Is `type` set to `"date"`, then the binding will try to interpret
incomig data as calendar date. If `type` is set to `"time"`, then incoming data will be tried to interpret as
time of day. For both `"date"` and `"time"` types, the appropriate channel must be linked to an `DateTime` item.

Follow block names are allowed for analog things:

| Type           | `0BA7`        | `0BA8`      | 
| -------------- | :-----------: | :---------: |
| Input          | `AI[1-8]`     | `AI[1-8]`   |
| Output         | `AQ[1-2]`     | `AQ[1-8]`   |
| Marker         | `AM[1-16]`    | `AM[1-64]`  |
| Network input  |               | `NAI[1-32]` |
| Network output |               | `NAQ[1-16]` |
| Memory (DWORD) | `VD[0-847]`   | `VD[0-847]` |
| Memory (WORD)  | `VW[0-849]`   | `VW[0-849]` |

Please, consider `openHAB` and/or `Eclipse SmartHome` documentation for details.

## Channels
### Bridge
Each device have currently one channel `rtc`:

```
channel="plclogo:device:<DeviceId>:rtc"
```

This channel supports `DateTime` items only. Since Siemens `0BA7` (LOGO! 7) devices will not transfer
any useful data for this channel, local time of openHAB host will be used. Rather for Siemens `0BA8`
(LOGO! 8) devices, the data will be read from PLC. Since the smallest resolution provided by LOGO! is
one second, `rtc` channel will be tried to update with the same rate.

### Digital
Each digital thing have currently one channel `state`:

```
channel="plclogo:digital:<DeviceId>:<ThingId>:state"
```

Dependend on configured block type, channel supports one of two different item types: `Contact`
for inputs and `Switch` for outputs. Means, that for `I` and `NI` blocks `Contact` items must
be used. For other blocks simply use `Switch`, since they are bidirectional.

### Analog
Each analog thing have currently one channel `value`:

```
channel="plclogo:digital:<DeviceId>:<ThingId>:value"
```

This channel supports `Number` or `DateTime` items dependend on thing configuration.


## Examples
Configuration of one Siemens LOGO!

logo.things:

```
Bridge plclogo:device:Logo [ address="192.168.0.1", family="0BA8", localTSAP="0x3000", remoteTSAP="0x2000", refresh=100 ]
{
  Thing digital VB0_0 [ block="VB0.0" ]
  Thing digital VB0_1 [ block="VB0.1" ]
  Thing digital NI1   [ block="NI1" ]
  Thing digital NI2   [ block="NI2" ]
  Thing digital Q1    [ block="Q1" ]
  Thing digital Q2    [ block="Q2" ]
  Thing analog  VW100 [ block="VW100", threshold=1, force=true ]
  Thing analog  VW102 [ block="VW102", type="time" ]
  Thing analog  VW104 [ block="VW104", type="time" ]
}
```

logo.items:

```
// NI1 is mapped to VB0.0 address in LOGO!Soft Comfort 
// NI2 is mapped to VB0.1 address in LOGO!Soft Comfort 
Switch   LogoUp                             {channel="plclogo:digital:Logo:VB0_0:state"}
Switch   LogoDown                           {channel="plclogo:digital:Logo:VB0_1:state"}
Contact  LogoIsUp                           {channel="plclogo:digital:Logo:NI1:state"}
Contact  LogoIsDown                         {channel="plclogo:digital:Logo:NI2:state"}
Switch   Output1                            {channel="plclogo:digital:Logo:Q1:state"}
Switch   Output2                            {channel="plclogo:digital:Logo:Q2:state"}
Number   Position                           {channel="plclogo:analog:Logo:VW100:value"}
DateTime Sunrise    "Sunrise [%1$tH:%1$tM]" {channel="plclogo:analog:Logo:VW102:value"}
DateTime Sunset     "Sunset [%1$tH:%1$tM]"  {channel="plclogo:analog:Logo:VW104:value"}
DateTime RTC                                {channel="plclogo:device:Logo:rtc}
```

Configuration of two Siemens LOGO!

logo.things:

```
Bridge plclogo:device:Logo1 [ address="192.168.0.1", family="0BA8", localTSAP="0x3000", remoteTSAP="0x2000", refresh=100 ]
{
  Thing digital VB0_0 [ block="VB0.0" ]
  Thing digital VB0_1 [ block="VB0.1" ]
  Thing digital NI1   [ block="NI1" ]
  Thing digital NI2   [ block="NI2" ]
  Thing digital Q1    [ block="Q1" ]
  Thing digital Q2    [ block="Q2" ]
  Thing analog  VW100 [ block="VW100", threshold=1 ]
}
Bridge plclogo:device:Logo2 [ address="192.168.0.2", family="0BA8", localTSAP="0x3100", remoteTSAP="0x2000", refresh=100 ]
{
  Thing digital VB0_0 [ block="VB0.0" ]
  Thing digital VB0_1 [ block="VB0.1" ]
  Thing digital NI1   [ block="NI1" ]
  Thing digital NI2   [ block="NI2" ]
  Thing digital Q1    [ block="Q1" ]
  Thing digital Q2    [ block="Q2" ]
  Thing analog  VW100 [ block="VW100", threshold=1 ]
}
```

logo.items:

```
Switch   Logo1_Up       {channel="plclogo:digital:Logo1:VB0_0:state"}
Switch   Logo1_Down     {channel="plclogo:digital:Logo1:VB0_1:state"}
Contact  Logo1_IsUp     {channel="plclogo:digital:Logo1:NI1:state"}
Contact  Logo1_IsDown   {channel="plclogo:digital:Logo1:NI2:state"}
Switch   Logo1_Output1  {channel="plclogo:digital:Logo1:Q1:state"}
Switch   Logo1_Output2  {channel="plclogo:digital:Logo1:Q2:state"}
Number   Logo1_Position {channel="plclogo:analog:Logo1:VW100:value"}
DateTime Logo1_RTC      {channel="plclogo:device:Logo1:rtc}

Switch   Logo2_Up       {channel="plclogo:digital:Logo2:VB0_0:state"}
Switch   Logo2_Down     {channel="plclogo:digital:Logo2:VB0_1:state"}
Contact  Logo2_IsUp     {channel="plclogo:digital:Logo2:NI1:state"}
Contact  Logo2_IsDown   {channel="plclogo:digital:Logo2:NI2:state"}
Switch   Logo2_Output1  {channel="plclogo:digital:Logo2:Q1:state"}
Switch   Logo2_Output2  {channel="plclogo:digital:Logo2:Q2:state"}
Number   Logo2_Position {channel="plclogo:analog:Logo2:VW100:value"}
DateTime Logo2_RTC      {channel="plclogo:device:Logo2:rtc}
```

## Troubleshooting

**LOGO! bridge will not go online**

Be sure to have only one bridge for each LOGO! device.

**Log shows reader was created but no communication with LOGO! possible**

Check TSAP values: localTSAP and remoteTSAP should not be the same. You have to choose different addresses.

**openHAB is starting without errors but no reader was created for the LOGO!**

If all configuration parameters were checked and fine, it maybe possible that the network interface of the LOGO! is crashed.
To recover stop openHAB, cold boot your LOGO! (power off/on) and reflash the program with LOGO! SoftComfort. Then restart
openHAB and check logging for a created reader.

**RTC value differs from the value shown in LOGO! (0BA7)**

This is no bug! Since there is no way to read the RTC from a 0BA7, the binding simply returns the local time of openHAB host.
