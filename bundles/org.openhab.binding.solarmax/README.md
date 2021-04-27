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
| port            | no       | 12345   | Port number to connect to. This should be `12345` for most inverters |
| refreshInterval | no       | 15      | Interval (in seconds) to refresh the channel values.                 |


## Channels

| channel                  | type     | description                                            |
| ------------------------ | -------- | ------------------------------------------------------ |
| lastUpdated              | DateTime | Time when data was last read from the device           |
| softwareVersion          | Point    | Software Version installed on the SolarMax device      |
| buildNumber              | Point    | Firmware Build Number installed on the SolarMax device |
| startups                 | Point    | Number of times the device has started                 |
| acPhase1Current          | Point    | Ac Phase 1 Current in Amps                             |
| acPhase2Current          | Point    | Ac Phase 2 Current in Amps                             |
| acPhase3Current          | Point    | Ac Phase 3 Current in Amps                             |
| energyGeneratedToday     | Point    | Energy Generated Today in wH                           |
| energyGeneratedTotal     | Point    | Energy Generated since recording began in wH           |
| operatingHours           | Point    | Operating Hours since recording began in H             |
| energyGeneratedYesterday | Point    | Energy Generated Yesterday in wH                       |
| energyGeneratedLastMonth | Point    | Energy Generated Last Month in wH                      |
| energyGeneratedLastYear  | Point    | Energy Generated Last Year in wH                       |
| energyGeneratedThisMonth | Point    | Energy Generated This Month in wH                      |
| energyGeneratedThisYear  | Point    | Energy Generated This Year in wH                       |
| currentPowerGenerated    | Point    | Power currently being generated in w                   |
| acFrequency              | Point    | AcFrequency in Hz                                      |
| acPhase1Voltage          | Point    | Ac Phase1 Voltage in V                                 |
| acPhase2Voltage          | Point    | Ac Phase2 Voltage in V                                 |
| acPhase3Voltage          | Point    | Ac Phase3 Voltage in V                                 |
| heatSinkTemperature      | Point    | Heat Sink Temperature in degrees celcius               |

### SolarMax Commands

During the implementation the SolarMax device was sent all possible 3 character commands and a number of 4 character commands, to see what it responded to.
The most interesting, identifiable and useful commands were implemented as channels above.

Here is a list of other commands, which are know to return some kind of value: ADR (DeviceAddress / Device Number - only used if the devices are linked serially), AMM, CID, CPG, CPL, CP1, CP2, CP3, CP4, CP5, CYC, DIN, DMO, ETH, FH2, FQR, FWV, IAA, IED, IEE, IEM, ILM, IP4, ISL, ITS, KFS, KHS, KTS, LAN (Language), MAC (MAC Address), PAE, PAM, PDA, PDC, PFA, PIN (Power Installed), PLR, PPC, PRL (AC Power Percent, PSF, PSR, PSS, QAC, QMO, QUC, RA1, RA2, RB1, RB2, REL, RH1, RH2, RPR, RSD, SAC, SAL, SAM, SCH, SNM (IP Broadcast Address??), SPS, SRD, SRS, SYS (Operating State), TCP (probably port number - 12345), TI1, TL1, TL3, TND, TNH, TNL, TP1, TP2, TP3, TV0, TV1, TYP (Type?), UA2, UB2, UGD, UI1, UI2, UI3, ULH, ULL, UMX, UM1, UM2, UM3, UPD, UZK, VCM

Valid commands which returned a null/empty value during testing: FFK, FRT, GCP, ITN, PLD, PLE, PLF, PLS, PPO, TV2, VLE, VLI, VLO
