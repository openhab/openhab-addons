# SolarMax Binding

This binding supports SolarMax PV inverters.

## Supported Things

This binding only has a single `inverter` thing that can be added manually.
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
| acPhase1Current          | Number:ElectricCurrent   | Ac Phase 1 Current in Amps                   |
| acPhase2Current          | Number:ElectricCurrent   | Ac Phase 2 Current in Amps                   |
| acPhase3Current          | Number:ElectricCurrent   | Ac Phase 3 Current in Amps                   |
| energyGeneratedToday     | Number:Energy            | Energy Generated Today in Wh                 |
| energyGeneratedTotal     | Number:Energy            | Energy Generated since recording began in Wh |
| operatingHours           | Number                   | Operating Hours since recording began in h   |
| energyGeneratedYesterday | Number:Energy            | Energy Generated Yesterday in Wh             |
| energyGeneratedLastMonth | Number:Energy            | Energy Generated Last Month in Wh            |
| energyGeneratedLastYear  | Number:Energy            | Energy Generated Last Year in Wh             |
| energyGeneratedThisMonth | Number:Energy            | Energy Generated This Month in Wh            |
| energyGeneratedThisYear  | Number:Energy            | Energy Generated This Year in Wh             |
| currentPowerGenerated    | Number:Power             | Power currently being generated in W         |
| acFrequency              | Number:Frequency         | AcFrequency in Hz                            |
| acPhase1Voltage          | Number:ElectricPotential | Ac Phase1 Voltage in V                       |
| acPhase2Voltage          | Number:ElectricPotential | Ac Phase2 Voltage in V                       |
| acPhase3Voltage          | Number:ElectricPotential | Ac Phase3 Voltage in V                       |
| heatSinkTemperature      | Number:Temperature       | Heat Sink Temperature in degrees celcius     |

### Full Example

Below you can find some example textual configuration for a solarmax with some basic functionallity. This can be extended/adjusted according to your needs and depending on the required channels (see list above).

_inverter.things:_

```java
Thing solarmax:inverter:solarmax "SolarMax Inverter" [
    host="192.168.1.151",
    portNumber="12345",
    deviceAddress="1",
    refresh="15"
]
```

_inverter.items:_

```java
Group    gInverter   "SolarMax Inverter"

DateTime lastUpdated "Last Updated" <clock> (gInverter) {channel="solarmax:inverter:solarmax:lastUpdated"}

Number startups "Startups" (gInverter) { channel="solarmax:inverter:solarmax:startups" }

Number:ElectricCurrent acPhase1Current "Ac Phase 1 Current in Amps" <energy> (gInverter) { channel="solarmax:inverter:solarmax:acPhase1Current" }
Number:ElectricCurrent acPhase2Current "Ac Phase 2 Current in Amps" <energy> (gInverter) { channel="solarmax:inverter:solarmax:acPhase2Current" }
Number:ElectricCurrent acPhase3Current "Ac Phase 3 Current in Amps" <energy> (gInverter) { channel="solarmax:inverter:solarmax:acPhase3Current" }

Number:Energy energyGeneratedToday "Energy Generated Today in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:energyGeneratedToday" }
Number:Energy energyGeneratedTotal "Energy Generated since recording began in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:energyGeneratedTotal" }

Number operatingHours "Operating Hours since recording began in h" <time> (gInverter) { channel="solarmax:inverter:solarmax:operatingHours" }

Number:Energy energyGeneratedYesterday "Energy Generated Yesterday in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:operatingHours" }
Number:Energy energyGeneratedLastMonth "Energy Generated Last Month in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:energyGeneratedLastMonth" }
Number:Energy energyGeneratedLastYear "Energy Generated Last Year in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:energyGeneratedLastYear" }
Number:Energy energyGeneratedThisMonth "Energy Generated This Month in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:energyGeneratedThisMonth" }
Number:Energy energyGeneratedThisYear "Energy Generated This Year in Wh" <energy> (gInverter) { channel="solarmax:inverter:solarmax:energyGeneratedThisYear" }

Number:Power currentPowerGenerated "Power currently being generated in W" (gInverter) { channel="solarmax:inverter:solarmax:currentPowerGenerated" }
Number:Frequency acFrequency "AcFrequency in Hz" (gInverter) { channel="solarmax:inverter:solarmax:acFrequency" }

Number:ElectricPotential acPhase1Voltage "Ac Phase1 Voltage in V" <energy> (gInverter) { channel="solarmax:inverter:solarmax:acPhase1Voltage" }
Number:ElectricPotential acPhase2Voltage "Ac Phase2 Voltage in V" <energy> (gInverter) { channel="solarmax:inverter:solarmax:acPhase2Voltage" }
Number:ElectricPotential acPhase3Voltage "Ac Phase3 Voltage in V" <energy> (gInverter) { channel="solarmax:inverter:solarmax:acPhase3Voltage" }

Number:Temperature heatSinkTemperature "Heat Sink Temperature in degrees celcius" <temperature> (gInverter) { channel="solarmax:inverter:solarmax:heatSinkTemperature" }

```

### SolarMax Commands

During the implementation the SolarMax device was sent all possible 3 character commands and a number of 4 character commands, to see what it responded to.
The most interesting, identifiable and useful commands were implemented as channels above.

Here is a list of other commands, which are known to return some kind of value: ADR (Device Address), AMM, CID, CPG, CPL, CP1, CP2, CP3, CP4, CP5, CYC, DIN, DMO, ETH, FH2, FQR, FWV, IAA, IED, IEE, IEM, ILM, IP4, ISL, ITS, KFS, KHS, KTS, LAN (Language), MAC (MAC Address), PAE, PAM, PDA, PDC, PFA, PIN (Power Installed), PLR, PPC, PRL (AC Power Percent, PSF, PSR, PSS, QAC, QMO, QUC, RA1, RA2, RB1, RB2, REL, RH1, RH2, RPR, RSD, SAC, SAL, SAM, SCH, SNM (IP Broadcast Address??), SPS, SRD, SRS, SYS (Operating State), TCP (probably port number - 12345), TI1, TL1, TL3, TND, TNH, TNL, TP1, TP2, TP3, TV0, TV1, TYP (Type?), UA2, UB2, UGD, UI1, UI2, UI3, ULH, ULL, UMX, UM1, UM2, UM3, UPD, UZK, VCM

Valid commands which returned a null/empty value during testing: FFK, FRT, GCP, ITN, PLD, PLE, PLF, PLS, PPO, TV2, VLE, VLI, VLO
