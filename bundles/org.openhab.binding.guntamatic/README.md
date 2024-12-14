# Guntamatic Binding

The Guntamatic Binding can be used to monitor and control [Guntamatic Heating Systems](https://www.guntamatic.com/en/).

## Supported Things

The Guntamatic Binding was developed and tested using Guntamatic Biostar 15kW Pellets Heating System, running Firmware 3.3d.
It should work for all other Guntamatic Heating Systems as well, that support the same web interface (Pellets, WoodChips, EnergyGrain as well as Log Heating Systems).

## Things

Guntamatic Heating Systems supported as Thing Types:

| Name      | Thing Type ID | Heating System Type  | Binding Development Status                       |
| --------- | ------------- | -------------------- | ------------------------------------------------ |
| Biostar   | `biostar`     | Pellets              | tested via 15kW, firmware 3.3d, German & English |
| Biosmart  | `biosmart`    | Logs                 | tested via 22kW, firmware 3.2f, German           |
| Powerchip | `powerchip`   | WoodChips            | tested via 100kW, firmware 3.2d, French          |
| Powercorn | `powercorn`   | EnergyGrain          | untested (no user feedback)                      |
| Biocom    | `biocom`      | Pellets              | untested (no user feedback)                      |
| Pro       | `pro`         | Pellets or WoodChips | untested (no user feedback)                      |
| Therm     | `therm`       | Pellets              | untested (no user feedback)                      |
| Generic   | `generic`     | -                    | use, if none from above                          |

### Thing Configuration

| Parameter         | Description                                                                                                                                                                                                     | Default        |
| ----------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------- |
| `hostname`        | Hostname or IP address of the Guntamatic Heating System                                                                                                                                                         |                |
| `key`             | Optional, but required to read protected parameters and to control the Guntamatic Heating System.<br/>The key needs to be requested from Guntamatic support, e.g. via <https://www.guntamatic.com/en/contact/>. |                |
| `refreshInterval` | Interval the Guntamatic Heating System is polled in seconds                                                                                                                                                     | `60`           |
| `encoding`        | Code page used by the Guntamatic Heating System                                                                                                                                                                 | `windows-1252` |

### Properties

| Property          | Description                                         | Supported                                         |
| ----------------- | --------------------------------------------------- | ------------------------------------------------- |
| `extraWwHeat`     | Parameter used by `controlExtraWwHeat` channels     | all                                               |
| `boilerApproval`  | Parameter used by `controlBoilerApproval` channel   | Biostar, Powerchip, Powercorn, Biocom, Pro, Therm |
| `heatCircProgram` | Parameter used by `controlHeatCircProgram` channels | all                                               |
| `program`         | Parameter used by `controlProgram` channel          | all                                               |
| `wwHeat`          | Parameter used by `controlWwHeat` channels          | all                                               |

## Channels

### Control Channels

The Guntamatic Heating System can be controlled using the following channels:

| Channel                   | Description                                                                     | Type     | Unit | Security Access Level | ReadOnly | Advanced |
| ------------------------- | ------------------------------------------------------------------------------- | -------- | :--: | :-------------------: | :------: | :------: |
| `controlBoilerApproval`   | Set Boiler Approval (`AUTO`, `OFF`, `ON`)                                       | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlProgram`          | Set Program (`OFF`, `NORMAL`, `WARMWATER`, `MANUAL`<sup id="a1">[1](#f1)</sup>) | `String` |      |        🔐 W1         |   R/W    |  false   |
| `controlHeatCircProgram0` | Set Heat Circle 0 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)                    | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlHeatCircProgram1` | Set Heat Circle 1 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)                    | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlHeatCircProgram2` | Set Heat Circle 2 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)                    | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlHeatCircProgram3` | Set Heat Circle 3 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)                    | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlHeatCircProgram4` | Set Heat Circle 4 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)                    | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlHeatCircProgram5` | Set Heat Circle 5 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)                    | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlHeatCircProgram6` | Set Heat Circle 6 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)                    | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlHeatCircProgram7` | Set Heat Circle 7 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)                    | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlHeatCircProgram8` | Set Heat Circle 8 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)                    | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlWwHeat0`          | Trigger Warm Water Circle 0 (`RECHARGE`)                                        | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlWwHeat1`          | Trigger Warm Water Circle 1 (`RECHARGE`)                                        | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlWwHeat2`          | Trigger Warm Water Circle 2 (`RECHARGE`)                                        | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlExtraWwHeat0`     | Trigger Extra Warm Water Circle 0 (`RECHARGE`)                                  | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlExtraWwHeat1`     | Trigger Extra Warm Water Circle 1 (`RECHARGE`)                                  | `String` |      |        🔐 W1         |   R/W    |   true   |
| `controlExtraWwHeat2`     | Trigger Extra Warm Water Circle 2 (`RECHARGE`)                                  | `String` |      |        🔐 W1         |   R/W    |   true   |

- <b id="f1">1)</b> ... `MANUAL` is supported by Biostar, Powerchip, Powercorn, Biocom, Pro as well as Therm only [↩](#a1)

#### Response of Control Channels

- `{"ack":"confirmation message"}` ... in case of success
- `{"err":"error message"}`        ... in case of error

The reaction of the Guntamatic Heating System can be monitored via the corresponding data channel. E.g. `programHc1` if you triggered `controlHeatCircProgram1`. The data channel gets updated with the next cyclic update (according to the `refreshInterval` configuration).

### Monitoring Channels

The Binding dynamically generates Channels, derived from the data provided from the actual Guntamatic Heating System.

Example list of Channels using a Guntamatic Biostar 15kW Pellets Heating System running firmware 3.2d and Guntamatic System Language configured to English:

| Channel                  | Description            | Type                   | Unit | Security Access Level | ReadOnly | Advanced |
| ------------------------ | ---------------------- | ---------------------- | :--: | :-------------------: | :------: | -------: |
| `000_running`            | Running                | `String`               |      | 🔓 W0                 | R/O      | false    |
| `001_outsideTemp`        | Outside Temp.          | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `002_blrTargetTemp`      | Blr.Target Temp        | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `003_boilerTemperature`  | Boiler Temperature     | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `004_flueGasUtilisation` | Flue gas utilisation   | `Number:Dimensionless` | `%`  | 🔐 W1                 | R/O      | false    |
| `005_output`             | Output                 | `Number:Dimensionless` | `%`  | 🔓 W0                 | R/O      | false    |
| `006_returnTemp`         | Return temp            | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `007_co2Target`          | CO2 Target             | `Number:Dimensionless` | `%`  | 🔐 W1                 | R/O      | false    |
| `008_co2Content`         | CO2 Content            | `Number:Dimensionless` | `%`  | 🔓 W0                 | R/O      | false    |
| `009_returnTempTarget`   | Return temp target     | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `010_statusCode`         | Status code            | `Number`               |      | 🔐 W1                 | R/O      | false    |
| `011_efficiency`         | Efficiency             | `Number:Dimensionless` | `%`  | 🔐 W1                 | R/O      | false    |
| `012_output`             | Output                 | `Number:Dimensionless` | `%`  | 🔐 W1                 | R/O      | false    |
| `013_extractorSystem`    | Extractor System       | `Number:Dimensionless` | `%`  | 🔓 W0                 | R/O      | false    |
| `014_feedTurbine`        | Feed Turbine           | `String`               |      | 🔐 W1                 | R/O      | false    |
| `015_dischargeMotor`     | Discharge motor        | `Number:Dimensionless` | `%`  | 🔓 W0                 | R/O      | false    |
| `016_g1Target`           | G1 Target              | `Number:Dimensionless` | `%`  | 🔓 W0                 | R/O      | false    |
| `017_bufferTop`          | Buffer Top             | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `018_bufferMid`          | Buffer Mid             | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `019_bufferBtm`          | Buffer Btm             | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `020_pumpHp0`            | Pump HP0               | `Switch`               |      | 🔓 W0                 | R/O      | false    |
| `021_dhw0`               | DHW 0                  | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `022_bDhw0`              | B DHW 0                | `Switch`               |      | 🔓 W0                 | R/O      | false    |
| `023_dhw1`               | DHW 1                  | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `024_bDhw1`              | B DHW 1                | `Switch`               |      | 🔓 W0                 | R/O      | false    |
| `025_dhw2`               | DHW 2                  | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `026_bDhw2`              | B DHW 2                | `Switch`               |      | 🔓 W0                 | R/O      | false    |
| `027_roomTempHc0`        | Room Temp:HC 0         | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `028_heatCirc0`          | Heat Circ. 0           | `Switch`               |      | 🔓 W0                 | R/O      | false    |
| `029_roomTempHc1`        | Room Temp:HC 1         | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `030_flowTarget1`        | Flow Target 1          | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `031_flowIs1`            | Flow is 1              | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `032_mixer1`             | Mixer 1                | `String`               |      | 🔐 W1                 | R/O      | false    |
| `033_heatCirc1`          | Heat Circ. 1           | `Switch`               |      | 🔐 W1                 | R/O      | false    |
| `034_roomTempHc2`        | Room Temp:HC 2         | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `035_flowTarget2`        | Flow Target 2          | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `036_flowIs2`            | Flow is 2              | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `037_mixer2`             | Mixer 2                | `String`               |      | 🔐 W1                 | R/O      | false    |
| `038_heatCirc2`          | Heat Circ. 2           | `Switch`               |      | 🔓 W0                 | R/O      | false    |
| `039_roomTempHc3`        | Room Temp:HC 3         | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `040_heatCirc3`          | Heat Circ. 3           | `Switch`               |      | 🔓 W0                 | R/O      | false    |
| `041_roomTempHc4`        | Room Temp:HC 4         | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `042_flowTarget4`        | Flow Target 4          | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `043_flowIs4`            | Flow is 4              | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `044_mixer4`             | Mixer 4                | `String`               |      | 🔐 W1                 | R/O      | false    |
| `045_heatCirc4`          | Heat Circ. 4           | `Switch`               |      | 🔓 W0                 | R/O      | false    |
| `046_roomTempHc5`        | Room Temp:HC 5         | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `047_flowTarget5`        | Flow Target 5          | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `048_flowIs5`            | Flow is 5              | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `049_mixer5`             | Mixer 5                | `String`               |      | 🔐 W1                 | R/O      | false    |
| `050_heatCirc5`          | Heat Circ. 5           | `Switch`               |      | 🔓 W0                 | R/O      | false    |
| `051_roomTempHc6`        | Room Temp:HC 6         | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `052_heatCirc6`          | Heat Circ. 6           | `Switch`               |      | 🔓 W0                 | R/O      | false    |
| `053_roomTempHc7`        | Room Temp:HC 7         | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `054_flowTarget7`        | Flow Target 7          | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `055_flowIs7`            | Flow is 7              | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `056_mixer7`             | Mixer 7                | `String`               |      | 🔐 W1                 | R/O      | false    |
| `057_heatCirc7`          | Heat Circ. 7           | `Switch`               |      | 🔓 W0                 | R/O      | false    |
| `058_roomTempHc8`        | Room Temp:HC 8         | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `059_flowTarget8`        | Flow Target 8          | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `060_flowIs8`            | Flow is 8              | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `061_mixer8`             | Mixer 8                | `String`               |      | 🔐 W1                 | R/O      | false    |
| `062_heatCirc8`          | Heat Circ. 8           | `Switch`               |      | 🔓 W0                 | R/O      | false    |
| `065_fuelLevel`          | Fuel Level             | `String`               |      | 🔐 W1                 | R/O      | false    |
| `066_stb`                | STB                    | `String`               |      | 🔐 W1                 | R/O      | false    |
| `067_tks`                | TKS                    | `String`               |      | 🔐 W1                 | R/O      | false    |
| `068_boilerApproval`     | Boiler approval        | `Switch`               |      | 🔐 W1                 | R/O      | false    |
| `069_programme`          | Programme              | `String`               |      | 🔓 W0                 | R/O      | false    |
| `070_programHc0`         | Program HC0            | `String`               |      | 🔓 W0                 | R/O      | false    |
| `071_programHc1`         | Program HC1            | `String`               |      | 🔓 W0                 | R/O      | false    |
| `072_programHc2`         | Program HC2            | `String`               |      | 🔓 W0                 | R/O      | false    |
| `073_programHc3`         | Program HC3            | `String`               |      | 🔓 W0                 | R/O      | false    |
| `074_programHc4`         | Program HC4            | `String`               |      | 🔓 W0                 | R/O      | false    |
| `075_programHc5`         | Program HC5            | `String`               |      | 🔓 W0                 | R/O      | false    |
| `076_programHc6`         | Program HC6            | `String`               |      | 🔓 W0                 | R/O      | false    |
| `077_programHc7`         | Program HC7            | `String`               |      | 🔓 W0                 | R/O      | false    |
| `078_programHc8`         | Program HC8            | `String`               |      | 🔓 W0                 | R/O      | false    |
| `079_interuption0`       | Interuption 0          | `String`               |      | 🔓 W0                 | R/O      | false    |
| `080_interuption1`       | Interuption 1          | `String`               |      | 🔓 W0                 | R/O      | false    |
| `081_serial`             | Serial                 | `Number`               |      | 🔓 W0                 | R/O      | false    |
| `082_version`            | Version                | `String`               |      | 🔓 W0                 | R/O      | false    |
| `083_runningTime`        | Running Time           | `Number:Time`          | `h`  | 🔓 W0                 | R/O      | false    |
| `084_serviceHrs`         | Service Hrs            | `Number:Time`          | `d`  | 🔓 W0                 | R/O      | false    |
| `085_emptyAshIn`         | Empty ash in           | `Number:Time`          | `h`  | 🔓 W0                 | R/O      | false    |
| `086_flowIs0`            | Flow is 0              | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `087_flowIs3`            | Flow is 3              | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `088_flowIs6`            | Flow is 6              | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `089_fuelCounter`        | Fuel counter           | `Number:Volume`        | `m³` | 🔐 W1                 | R/O      | false    |
| `090_bufferLoad`         | Buffer load.           | `Number:Dimensionless` | `%`  | 🔓 W0                 | R/O      | false    |
| `091_bufferTop0`         | Buffer Top 0           | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `092_bufferBtm0`         | Buffer Btm 0           | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `093_bufferTop1`         | Buffer Top 1           | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `094_bufferBtm1`         | Buffer Btm 1           | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `095_bufferTop2`         | Buffer Top 2           | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `096_bufferBtm2`         | Buffer Btm 2           | `Number:Temperature`   | `°C` | 🔐 W1                 | R/O      | false    |
| `097_bExtraWw0`          | B extra-WW. 0          | `Switch`               |      | 🔐 W1                 | R/O      | false    |
| `098_bExtraWw1`          | B extra-WW. 1          | `Switch`               |      | 🔐 W1                 | R/O      | false    |
| `099_bExtraWw2`          | B extra-WW. 2          | `Switch`               |      | 🔐 W1                 | R/O      | false    |
| `100_auxiliaryPump0`     | Auxiliary pump 0       | `Switch`               |      | 🔐 W1                 | R/O      | false    |
| `101_auxiliaryPump1`     | Auxiliary pump 1       | `Switch`               |      | 🔐 W1                 | R/O      | false    |
| `102_auxiliaryPump2`     | Auxiliary pump 2       | `Switch`               |      | 🔐 W1                 | R/O      | false    |
| `104_boilersConditionNo` | Boiler´s condition no. | `String`               |      | 🔐 W1                 | R/O      | false    |
| `108_bufferT5`           | Buffer T5              | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `109_bufferT6`           | Buffer T6              | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `110_bufferT7`           | Buffer T7              | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `111_extraWw0`           | Extra-WW. 0            | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `112_extraWw1`           | Extra-WW. 1            | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `113_extraWw2`           | Extra-WW. 2            | `Number:Temperature`   | `°C` | 🔓 W0                 | R/O      | false    |
| `114_grate`              | Grate                  | `Number:Dimensionless` | `%`  | 🔓 W0                 | R/O      | false    |

#### Security Access Levels

- 🔓 W0 ... Open
- 🔐 W1 ... End Customer Key
- 🔒 W2 ... Service Partner

## Full Example

### Thing File

```java
Thing   guntamatic:biostar:mybiostar   "Guntamatic Biostar"    [ hostname="192.168.1.100", key="0123456789ABCDEF0123456789ABCDEF0123", refreshInterval=60, encoding="windows-1252" ]
```

### Item File

```java
String               Biostar_Running                 "Running"                            { channel="guntamatic:biostar:mybiostar:000_running" }
Number:Temperature   Biostar_OutsideTemp             "Outside Temp."                      { channel="guntamatic:biostar:mybiostar:001_outsideTemp" }
Number:Temperature   Biostar_BlrTargetTemp           "Blr.Target Temp"                    { channel="guntamatic:biostar:mybiostar:002_blrTargetTemp" }
Number:Temperature   Biostar_BoilerTemperature       "Boiler Temperature"                 { channel="guntamatic:biostar:mybiostar:003_boilerTemperature" }
Number:Dimensionless Biostar_FlueGasUtilisation      "Flue gas utilisation"               { channel="guntamatic:biostar:mybiostar:004_flueGasUtilisation" }
Number:Dimensionless Biostar_Output                  "Output"                             { channel="guntamatic:biostar:mybiostar:005_output" }
Number:Temperature   Biostar_ReturnTemp              "Return temp"                        { channel="guntamatic:biostar:mybiostar:006_returnTemp" }
Number:Dimensionless Biostar_Co2Target               "CO2 Target"                         { channel="guntamatic:biostar:mybiostar:007_co2Target" }
Number:Dimensionless Biostar_Co2Content              "CO2 Content"                        { channel="guntamatic:biostar:mybiostar:008_co2Content" }
Number:Temperature   Biostar_ReturnTempTarget        "Return temp target"                 { channel="guntamatic:biostar:mybiostar:009_returnTempTarget" }
Number               Biostar_StatusCode              "Status code"                        { channel="guntamatic:biostar:mybiostar:010_statusCode" }
Number:Dimensionless Biostar_Efficiency              "Efficiency"                         { channel="guntamatic:biostar:mybiostar:011_efficiency" }
Number:Dimensionless Biostar_Output2                 "Output"                             { channel="guntamatic:biostar:mybiostar:012_output" }
Number:Dimensionless Biostar_ExtractorSystem         "Extractor System"                   { channel="guntamatic:biostar:mybiostar:013_extractorSystem" }
String               Biostar_FeedTurbine             "Feed Turbine"                       { channel="guntamatic:biostar:mybiostar:014_feedTurbine" }
Number:Dimensionless Biostar_DischargeMotor          "Discharge motor"                    { channel="guntamatic:biostar:mybiostar:015_dischargeMotor" }
Number:Dimensionless Biostar_G1Target                "G1 Target"                          { channel="guntamatic:biostar:mybiostar:016_g1Target" }
Number:Temperature   Biostar_BufferTop               "Buffer Top"                         { channel="guntamatic:biostar:mybiostar:017_bufferTop" }
Number:Temperature   Biostar_BufferMid               "Buffer Mid"                         { channel="guntamatic:biostar:mybiostar:018_bufferMid" }
Number:Temperature   Biostar_BufferBtm               "Buffer Btm"                         { channel="guntamatic:biostar:mybiostar:019_bufferBtm" }
Switch               Biostar_PumpHp0                 "Pump HP0"                           { channel="guntamatic:biostar:mybiostar:020_pumpHp0" }
Number:Temperature   Biostar_Dhw0                    "DHW 0"                              { channel="guntamatic:biostar:mybiostar:021_dhw0" }
Switch               Biostar_BDhw0                   "B DHW 0"                            { channel="guntamatic:biostar:mybiostar:022_bDhw0" }
Number:Temperature   Biostar_Dhw1                    "DHW 1"                              { channel="guntamatic:biostar:mybiostar:023_dhw1" }
Switch               Biostar_BDhw1                   "B DHW 1"                            { channel="guntamatic:biostar:mybiostar:024_bDhw1" }
Number:Temperature   Biostar_Dhw2                    "DHW 2"                              { channel="guntamatic:biostar:mybiostar:025_dhw2" }
Switch               Biostar_BDhw2                   "B DHW 2"                            { channel="guntamatic:biostar:mybiostar:026_bDhw2" }
Number:Temperature   Biostar_RoomTempHc0             "Room Temp:HC 0"                     { channel="guntamatic:biostar:mybiostar:027_roomTempHc0" }
Switch               Biostar_HeatCirc0               "Heat Circ. 0"                       { channel="guntamatic:biostar:mybiostar:028_heatCirc0" }
Number:Temperature   Biostar_RoomTempHc1             "Room Temp:HC 1"                     { channel="guntamatic:biostar:mybiostar:029_roomTempHc1" }
Number:Temperature   Biostar_FlowTarget1             "Flow Target 1"                      { channel="guntamatic:biostar:mybiostar:030_flowTarget1" }
Number:Temperature   Biostar_FlowIs1                 "Flow is 1"                          { channel="guntamatic:biostar:mybiostar:031_flowIs1" }
String               Biostar_Mixer1                  "Mixer 1"                            { channel="guntamatic:biostar:mybiostar:032_mixer1" }
Switch               Biostar_HeatCirc1               "Heat Circ. 1"                       { channel="guntamatic:biostar:mybiostar:033_heatCirc1" }
Number:Temperature   Biostar_RoomTempHc2             "Room Temp:HC 2"                     { channel="guntamatic:biostar:mybiostar:034_roomTempHc2" }
Number:Temperature   Biostar_FlowTarget2             "Flow Target 2"                      { channel="guntamatic:biostar:mybiostar:035_flowTarget2" }
Number:Temperature   Biostar_FlowIs2                 "Flow is 2"                          { channel="guntamatic:biostar:mybiostar:036_flowIs2" }
String               Biostar_Mixer2                  "Mixer 2"                            { channel="guntamatic:biostar:mybiostar:037_mixer2" }
Switch               Biostar_HeatCirc2               "Heat Circ. 2"                       { channel="guntamatic:biostar:mybiostar:038_heatCirc2" }
Number:Temperature   Biostar_RoomTempHc3             "Room Temp:HC 3"                     { channel="guntamatic:biostar:mybiostar:039_roomTempHc3" }
Switch               Biostar_HeatCirc3               "Heat Circ. 3"                       { channel="guntamatic:biostar:mybiostar:040_heatCirc3" }
Number:Temperature   Biostar_RoomTempHc4             "Room Temp:HC 4"                     { channel="guntamatic:biostar:mybiostar:041_roomTempHc4" }
Number:Temperature   Biostar_FlowTarget4             "Flow Target 4"                      { channel="guntamatic:biostar:mybiostar:042_flowTarget4" }
Number:Temperature   Biostar_FlowIs4                 "Flow is 4"                          { channel="guntamatic:biostar:mybiostar:043_flowIs4" }
String               Biostar_Mixer4                  "Mixer 4"                            { channel="guntamatic:biostar:mybiostar:044_mixer4" }
Switch               Biostar_HeatCirc4               "Heat Circ. 4"                       { channel="guntamatic:biostar:mybiostar:045_heatCirc4" }
Number:Temperature   Biostar_RoomTempHc5             "Room Temp:HC 5"                     { channel="guntamatic:biostar:mybiostar:046_roomTempHc5" }
Number:Temperature   Biostar_FlowTarget5             "Flow Target 5"                      { channel="guntamatic:biostar:mybiostar:047_flowTarget5" }
Number:Temperature   Biostar_FlowIs5                 "Flow is 5"                          { channel="guntamatic:biostar:mybiostar:048_flowIs5" }
String               Biostar_Mixer5                  "Mixer 5"                            { channel="guntamatic:biostar:mybiostar:049_mixer5" }
Switch               Biostar_HeatCirc5               "Heat Circ. 5"                       { channel="guntamatic:biostar:mybiostar:050_heatCirc5" }
Number:Temperature   Biostar_RoomTempHc6             "Room Temp:HC 6"                     { channel="guntamatic:biostar:mybiostar:051_roomTempHc6" }
Switch               Biostar_HeatCirc6               "Heat Circ. 6"                       { channel="guntamatic:biostar:mybiostar:052_heatCirc6" }
Number:Temperature   Biostar_RoomTempHc7             "Room Temp:HC 7"                     { channel="guntamatic:biostar:mybiostar:053_roomTempHc7" }
Number:Temperature   Biostar_FlowTarget7             "Flow Target 7"                      { channel="guntamatic:biostar:mybiostar:054_flowTarget7" }
Number:Temperature   Biostar_FlowIs7                 "Flow is 7"                          { channel="guntamatic:biostar:mybiostar:055_flowIs7" }
String               Biostar_Mixer7                  "Mixer 7"                            { channel="guntamatic:biostar:mybiostar:056_mixer7" }
Switch               Biostar_HeatCirc7               "Heat Circ. 7"                       { channel="guntamatic:biostar:mybiostar:057_heatCirc7" }
Number:Temperature   Biostar_RoomTempHc8             "Room Temp:HC 8"                     { channel="guntamatic:biostar:mybiostar:058_roomTempHc8" }
Number:Temperature   Biostar_FlowTarget8             "Flow Target 8"                      { channel="guntamatic:biostar:mybiostar:059_flowTarget8" }
Number:Temperature   Biostar_FlowIs8                 "Flow is 8"                          { channel="guntamatic:biostar:mybiostar:060_flowIs8" }
String               Biostar_Mixer8                  "Mixer 8"                            { channel="guntamatic:biostar:mybiostar:061_mixer8" }
Switch               Biostar_HeatCirc8               "Heat Circ. 8"                       { channel="guntamatic:biostar:mybiostar:062_heatCirc8" }
String               Biostar_FuelLevel               "Fuel Level"                         { channel="guntamatic:biostar:mybiostar:065_fuelLevel" }
String               Biostar_Stb                     "STB"                                { channel="guntamatic:biostar:mybiostar:066_stb" }
String               Biostar_Tks                     "TKS"                                { channel="guntamatic:biostar:mybiostar:067_tks" }
Switch               Biostar_BoilerApproval          "Boiler approval"                    { channel="guntamatic:biostar:mybiostar:068_boilerApproval" }
String               Biostar_Programme               "Programme"                          { channel="guntamatic:biostar:mybiostar:069_programme" }
String               Biostar_ProgramHc0              "Program HC0"                        { channel="guntamatic:biostar:mybiostar:070_programHc0" }
String               Biostar_ProgramHc1              "Program HC1"                        { channel="guntamatic:biostar:mybiostar:071_programHc1" }
String               Biostar_ProgramHc2              "Program HC2"                        { channel="guntamatic:biostar:mybiostar:072_programHc2" }
String               Biostar_ProgramHc3              "Program HC3"                        { channel="guntamatic:biostar:mybiostar:073_programHc3" }
String               Biostar_ProgramHc4              "Program HC4"                        { channel="guntamatic:biostar:mybiostar:074_programHc4" }
String               Biostar_ProgramHc5              "Program HC5"                        { channel="guntamatic:biostar:mybiostar:075_programHc5" }
String               Biostar_ProgramHc6              "Program HC6"                        { channel="guntamatic:biostar:mybiostar:076_programHc6" }
String               Biostar_ProgramHc7              "Program HC7"                        { channel="guntamatic:biostar:mybiostar:077_programHc7" }
String               Biostar_ProgramHc8              "Program HC8"                        { channel="guntamatic:biostar:mybiostar:078_programHc8" }
String               Biostar_Interuption0            "Interuption 0"                      { channel="guntamatic:biostar:mybiostar:079_interuption0" }
String               Biostar_Interuption1            "Interuption 1"                      { channel="guntamatic:biostar:mybiostar:080_interuption1" }
Number               Biostar_Serial                  "Serial"                             { channel="guntamatic:biostar:mybiostar:081_serial" }
String               Biostar_Version                 "Version"                            { channel="guntamatic:biostar:mybiostar:082_version" }
Number:Time          Biostar_RunningTime             "Running Time"                       { channel="guntamatic:biostar:mybiostar:083_runningTime" }
Number:Time          Biostar_ServiceHrs              "Service Hrs"                        { channel="guntamatic:biostar:mybiostar:084_serviceHrs" }
Number:Time          Biostar_EmptyAshIn              "Empty ash in"                       { channel="guntamatic:biostar:mybiostar:085_emptyAshIn" }
Number:Temperature   Biostar_FlowIs0                 "Flow is 0"                          { channel="guntamatic:biostar:mybiostar:086_flowIs0" }
Number:Temperature   Biostar_FlowIs3                 "Flow is 3"                          { channel="guntamatic:biostar:mybiostar:087_flowIs3" }
Number:Temperature   Biostar_FlowIs6                 "Flow is 6"                          { channel="guntamatic:biostar:mybiostar:088_flowIs6" }
Number:Volume        Biostar_FuelCounter             "Fuel counter"                       { channel="guntamatic:biostar:mybiostar:089_fuelCounter" }
Number:Dimensionless Biostar_BufferLoad              "Buffer load."                       { channel="guntamatic:biostar:mybiostar:090_bufferLoad" }
Number:Temperature   Biostar_BufferTop0              "Buffer Top 0"                       { channel="guntamatic:biostar:mybiostar:091_bufferTop0" }
Number:Temperature   Biostar_BufferBtm0              "Buffer Btm 0"                       { channel="guntamatic:biostar:mybiostar:092_bufferBtm0" }
Number:Temperature   Biostar_BufferTop1              "Buffer Top 1"                       { channel="guntamatic:biostar:mybiostar:093_bufferTop1" }
Number:Temperature   Biostar_BufferBtm1              "Buffer Btm 1"                       { channel="guntamatic:biostar:mybiostar:094_bufferBtm1" }
Number:Temperature   Biostar_BufferTop2              "Buffer Top 2"                       { channel="guntamatic:biostar:mybiostar:095_bufferTop2" }
Number:Temperature   Biostar_BufferBtm2              "Buffer Btm 2"                       { channel="guntamatic:biostar:mybiostar:096_bufferBtm2" }
Switch               Biostar_BExtraWw0               "B extra-WW. 0"                      { channel="guntamatic:biostar:mybiostar:097_bExtraWw0" }
Switch               Biostar_BExtraWw1               "B extra-WW. 1"                      { channel="guntamatic:biostar:mybiostar:098_bExtraWw1" }
Switch               Biostar_BExtraWw2               "B extra-WW. 2"                      { channel="guntamatic:biostar:mybiostar:099_bExtraWw2" }
Switch               Biostar_AuxiliaryPump0          "Auxiliary pump 0"                   { channel="guntamatic:biostar:mybiostar:100_auxiliaryPump0" }
Switch               Biostar_AuxiliaryPump1          "Auxiliary pump 1"                   { channel="guntamatic:biostar:mybiostar:101_auxiliaryPump1" }
Switch               Biostar_AuxiliaryPump2          "Auxiliary pump 2"                   { channel="guntamatic:biostar:mybiostar:102_auxiliaryPump2" }
String               Biostar_BoilersConditionNo      "Boiler´s condition no."             { channel="guntamatic:biostar:mybiostar:104_boilersConditionNo" }
Number:Temperature   Biostar_BufferT5                "Buffer T5"                          { channel="guntamatic:biostar:mybiostar:108_bufferT5" }
Number:Temperature   Biostar_BufferT6                "Buffer T6"                          { channel="guntamatic:biostar:mybiostar:109_bufferT6" }
Number:Temperature   Biostar_BufferT7                "Buffer T7"                          { channel="guntamatic:biostar:mybiostar:110_bufferT7" }
Number:Temperature   Biostar_ExtraWw0                "Extra-WW. 0"                        { channel="guntamatic:biostar:mybiostar:111_extraWw0" }
Number:Temperature   Biostar_ExtraWw1                "Extra-WW. 1"                        { channel="guntamatic:biostar:mybiostar:112_extraWw1" }
Number:Temperature   Biostar_ExtraWw2                "Extra-WW. 2"                        { channel="guntamatic:biostar:mybiostar:113_extraWw2" }
Number:Dimensionless Biostar_Grate                   "Grate"                              { channel="guntamatic:biostar:mybiostar:114_grate" }
```

### Rule

```java
rule "Example Guntamatic Rule"
when
    Item Season changed
then
    if ( (Season.state != NULL) && (Season.state != UNDEF) )
    {
        if ( Season.state.toString == "WINTER" )
        {
            Biostar_ControlProgram.sendCommand("NORMAL")
        }
        else
        {
            Biostar_ControlProgram.sendCommand("OFF")
        }
    }
end
```

## Your feedback is required

The Guntamatic Binding was developed and tested using Guntamatic Biostar 15kW Pellets Heating System, running Firmware 3.3d.
Please provide feedback (👍 as well as 👎) when using the Binding for other Guntamatic Heating Systems.

Forum topic for feedback:

- [openHAB community #128451](https://community.openhab.org/t/guntamatic-new-binding-for-guntamatic-heating-systems-biostar-powerchip-powercorn-biocom-pro-therm/128451 "openHAB community #128451")
