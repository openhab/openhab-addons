# SolarMax Binding

This binding supports SolarMax PV inverters.

## Supported Things

This binding only has a single `inverter` Thing that can be added manually.
The SolarMax MT Series is supported (tested with 8MT2 devices).

## Discovery

Auto-discovery is currently not available.

## Thing Configuration

Each inverter requires the following configuration parameters:

| parameter       | required | default | description                                                          |
| --------------- | -------- | ------- | -------------------------------------------------------------------- |
| host            | yes      |         | hostname or IP address of the inverter                               |
| portNumber      | no       | 12345   | Port number to connect to. This should be `12345` for most inverters |
| deviceAddress   | no       | 1       | Device address for devices connected serially.                       |
| refreshInterval | no       | 15      | Interval (in seconds) to refresh the channel values.                 |

## Properties

| property        | description                                            |
| --------------- | ------------------------------------------------------ |
| softwareVersion | Software Version installed on the SolarMax device      |
| buildNumber     | Firmware Build Number installed on the SolarMax device |

## Channels

| channel                  | type                     | description                                  |
| ------------------------ | ------------------------ | -------------------------------------------- |
| lastUpdated              | DateTime                 | Time when data was last read from the device |
| startups                 | Number                   | Number of times the device has started       |
| acPhase1Current          | Number:ElectricCurrent   | AC Phase 1 current in A                      |
| acPhase2Current          | Number:ElectricCurrent   | AC Phase 2 current in A                      |
| acPhase3Current          | Number:ElectricCurrent   | AC Phase 3 current in A                      |
| energyGeneratedToday     | Number:Energy            | Energy Generated Today in Wh                 |
| energyGeneratedTotal     | Number:Energy            | Energy Generated since recording began in Wh |
| operatingHours           | Number                   | Operating Hours since recording began in h   |
| energyGeneratedYesterday | Number:Energy            | Energy Generated Yesterday in Wh             |
| energyGeneratedLastMonth | Number:Energy            | Energy Generated Last Month in Wh            |
| energyGeneratedLastYear  | Number:Energy            | Energy Generated Last Year in Wh             |
| energyGeneratedThisMonth | Number:Energy            | Energy Generated This Month in Wh            |
| energyGeneratedThisYear  | Number:Energy            | Energy Generated This Year in Wh             |
| currentPowerGenerated    | Number:Power             | Power currently being generated in W         |
| acFrequency              | Number:Frequency         | AC frequency in Hz                           |
| acPhase1Voltage          | Number:ElectricPotential | AC Phase 1 voltage in V                      |
| acPhase2Voltage          | Number:ElectricPotential | AC Phase 2 voltage in V                      |
| acPhase3Voltage          | Number:ElectricPotential | AC Phase 3 voltage in V                      |
| heatSinkTemperature      | Number:Temperature       | Heat sink temperature in degrees Celsius     |
| operatingState           | Number                   | Operating state code (see below)             |
| alarmCode                | Number                   | Alarm code, a bit mask (see below)           |
| dcInput1Voltage          | Number:ElectricPotential | DC voltage of input (MPP tracker) 1 in V     |
| dcInput2Voltage          | Number:ElectricPotential | DC voltage of input (MPP tracker) 2 in V     |
| dcInput1Current          | Number:ElectricCurrent   | DC current of input (MPP tracker) 1 in A     |
| dcInput2Current          | Number:ElectricCurrent   | DC current of input (MPP tracker) 2 in A     |
| dcInput1Power            | Number:Power             | DC power of input (MPP tracker) 1 in W       |
| dcInput2Power            | Number:Power             | DC power of input (MPP tracker) 2 in W       |
| dcPower                  | Number:Power             | Total DC power of all inputs in W            |

Channels whose values are not supported by a device model are not updated.

### Operating State and Alarm Codes

SolarMax does not publish the meaning of the operating state (`SYS`) and alarm (`SAL`) codes in its MaxComm protocol description.
The labels of the `operatingState` channel are therefore taken from open source projects which read SolarMax inverters, and from texts of the SolarMax MaxTalk software.
Codes without a label are shown as plain numbers.
Typical values during normal operation are `20002` (irradiance too low), `20003` (startup) and `20008` (mains operation).
Codes from `20100` upwards indicate an error, e.g. `20115` (no mains), `20116`/`20117` (mains frequency too high/low) or `20122`/`20123` (mains voltage too high/low).

The `alarmCode` channel is a bit mask, `0` means no alarm.
If several bits are set, several alarms are active at the same time:

| bit value | alarm                            |
| --------- | -------------------------------- |
| 1         | External fault 1                 |
| 2         | Insulation fault DC side         |
| 4         | Earth fault current too high     |
| 8         | Fuse failure midpoint earth      |
| 16        | External alarm 2                 |
| 32        | Long-term temperature limitation |
| 64        | Error AC supply                  |
| 128       | External alarm 4                 |
| 256       | Fan failure                      |
| 512       | Fuse failure                     |
| 1024      | Failure temperature sensor       |

### Full Example

Below you can find an example textual configuration for a SolarMax with some basic functionality.
This can be extended/adjusted according to your needs and depending on the required channels (see list above).

_inverter.things:_

```java
Thing solarmax:inverter:solarmax "SolarMax Inverter" [
    host="192.168.1.151",
    portNumber="12345",
    deviceAddress="1",
    refreshInterval="15"
]
```

_inverter.items:_

```java
Group    gInverter   "SolarMax Inverter"

DateTime lastUpdated "Last Updated" <clock> (gInverter) {channel="solarmax:inverter:solarmax:lastUpdated"}

Number startups "Startups" (gInverter) { channel="solarmax:inverter:solarmax:startups" }

Number:ElectricCurrent acPhase1Current "AC Phase 1 current in A" <energy> (gInverter) { channel="solarmax:inverter:solarmax:acPhase1Current" }
Number:ElectricCurrent acPhase2Current "AC Phase 2 current in A" <energy> (gInverter) { channel="solarmax:inverter:solarmax:acPhase2Current" }
Number:ElectricCurrent acPhase3Current "AC Phase 3 current in A" <energy> (gInverter) { channel="solarmax:inverter:solarmax:acPhase3Current" }

Number:Energy energyGeneratedToday "Energy Generated Today in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:energyGeneratedToday" }
Number:Energy energyGeneratedTotal "Energy Generated since recording began in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:energyGeneratedTotal" }

Number operatingHours "Operating Hours since recording began in h" <time> (gInverter) { channel="solarmax:inverter:solarmax:operatingHours" }

Number:Energy energyGeneratedYesterday "Energy generated yesterday in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:energyGeneratedYesterday" }
Number:Energy energyGeneratedLastMonth "Energy Generated Last Month in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:energyGeneratedLastMonth" }
Number:Energy energyGeneratedLastYear "Energy Generated Last Year in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:energyGeneratedLastYear" }
Number:Energy energyGeneratedThisMonth "Energy Generated This Month in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:energyGeneratedThisMonth" }
Number:Energy energyGeneratedThisYear "Energy Generated This Year in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:energyGeneratedThisYear" }

Number:Power currentPowerGenerated "Power currently being generated in W" (gInverter) { channel="solarmax:inverter:solarmax:currentPowerGenerated" }
Number:Frequency acFrequency "AC frequency in Hz" (gInverter) { channel="solarmax:inverter:solarmax:acFrequency" }

Number:ElectricPotential acPhase1Voltage "AC Phase 1 voltage in V" <energy> (gInverter) { channel="solarmax:inverter:solarmax:acPhase1Voltage" }
Number:ElectricPotential acPhase2Voltage "AC Phase 2 voltage in V" <energy> (gInverter) { channel="solarmax:inverter:solarmax:acPhase2Voltage" }
Number:ElectricPotential acPhase3Voltage "AC Phase 3 voltage in V" <energy> (gInverter) { channel="solarmax:inverter:solarmax:acPhase3Voltage" }

Number:Temperature heatSinkTemperature "Heat sink temperature in degrees Celsius" <temperature> (gInverter) { channel="solarmax:inverter:solarmax:heatSinkTemperature" }

```

### SolarMax Commands

During the implementation the SolarMax device was sent all possible 3 character commands and a number of 4 character commands, to see what it responded to.
The most interesting, identifiable and useful commands were implemented as channels above.

Here is a list of other commands, which are known to return some kind of value: ADR (Device Address), AMM, CID, CPG, CPL, CP1, CP2, CP3, CP4, CP5, CYC, DIN, DMO, ETH, FH2, FQR, FWV, IAA, IED, IEE, IEM, ILM, IP4, ISL, ITS, KFS, KHS, KTS, LAN (Language), MAC (MAC Address), PAE, PAM, PDA, PFA, PIN (Power Installed), PLR, PPC, PRL (AC Power Percent, PSF, PSR, PSS, QAC, QMO, QUC, RA1, RA2, RB1, RB2, REL, RH1, RH2, RPR, RSD, SAC, SAM, SCH, SNM (IP Broadcast Address??), SPS, SRD, SRS, TCP (probably port number - 12345), TI1, TL1, TL3, TND, TNH, TNL, TP1, TP2, TP3, TV0, TV1, TYP (Type?), UA2, UB2, UGD, UI1, UI2, UI3, ULH, ULL, UMX, UM1, UM2, UM3, UPD, UZK, VCM

Valid commands which returned a null/empty value during testing: FFK, FRT, GCP, ITN, PLD, PLE, PLF, PLS, PPO, TV2, VLE, VLI, VLO
