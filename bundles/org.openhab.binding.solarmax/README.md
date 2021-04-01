# SolarMax Binding

This binding supports SolarMax PV inverters.

## Supported Things

The SolarMax MT Series is support. (tested with 8MT2 devices)

## Discovery

Auto-discovery is currently not available.

## Thing Configuration

The IP address and port number (default 12345) of the device needs to be configured.


```
############################## openHAB SolarMax Binding #############################

# The IP address or hostname of the SolarMax device
#host=192.168.1.151|SolarMax1

# The port number configured on the SolarMax device
# Default is 12345
#portNumber=12345

# The refresh interval (in seconds) 
# Default is 15 
#refreshInterval=15
```

## Channels

| channel                  | type  | description                                            |
| ------------------------ | ----- | ------------------------------------------------------ |
| LastUpdated              | Point | When was the data last read from the device            |
| SoftwareVersion          | Point | Software Version installed on the SolarMax device      |
| BuildNumber              | Point | Firmware Build Number installed on the SolarMax device |
| Startups                 | Point | Number of times the device has started                 |
| AcPhase1Current          | Point | Ac Phase 1 Current in Amps                             |
| AcPhase2Current          | Point | Ac Phase 2 Current in Amps                             |
| AcPhase3Current          | Point | Ac Phase 3 Current in Amps                             |
| EnergyGeneratedToday     | Point | Energy Generated Today in wH                           |
| EnergyGeneratedTotal     | Point | Energy Generated since recording began in wH           |
| OperatingHours           | Point | Operating Hours since recording began in H             |
| EnergyGeneratedYesterday | Point | Energy Generated Yesterday in wH                       |
| EnergyGeneratedLastMonth | Point | Energy Generated Last Month in wH                      |
| EnergyGeneratedLastYear  | Point | Energy Generated Last Year in wH                       |
| EnergyGeneratedThisMonth | Point | Energy Generated This Month in wH                      |
| EnergyGeneratedThisYear  | Point | Energy Generated This Year in wH                       |
| Current Power Generated  | Point | Power currently being generated in w                   |
| AcFrequency              | Point | AcFrequency in Hz                                      |
| AcPhase1Voltage          | Point | Ac Phase1 Voltage in V                                 |
| AcPhase2Voltage          | Point | Ac Phase2 Voltage in V                                 |
| AcPhase3Voltage          | Point | Ac Phase3 Voltage in V                                 |
| HeatSinkTemperature      | Point | Heat Sink Temperature in degrees celcius               |

### SolarMax Commands

During the implementation the SolarMax device was sent all possible 3 character commands and a number of 4 character commands, to see what it responded to. The most interesting, identifiable and useful commands were implemented as channels above.

Here is a list of other commands, which are know to return some kind of value: ADR (DeviceAddress / Device Number - only used if the devices are linked serially), AMM, CID, CPG, CPL, CP1, CP2, CP3, CP4, CP5, CYC, DIN, DMO, ETH, FH2, FQR, FWV, IAA, IED, IEE, IEM, ILM, IP4, ISL, ITS, KFS, KHS, KTS, LAN (Language), MAC (MAC Address), PAE, PAM, PDA, PDC, PFA, PIN (Power Installed), PLR, PPC, PRL (AC Power Percent, PSF, PSR, PSS, QAC, QMO, QUC, RA1, RA2, RB1, RB2, REL, RH1, RH2, RPR, RSD, SAC, SAL, SAM, SCH, SNM (IP Broadcast Address??), SPS, SRD, SRS, SYS (Operating State), TCP (probably port number - 12345), TI1, TL1, TL3, TND, TNH, TNL, TP1, TP2, TP3, TV0, TV1, TYP (Type?), UA2, UB2, UGD, UI1, UI2, UI3, ULH, ULL, UMX, UM1, UM2, UM3, UPD, UZK, VCM

Valid commands which returned a null/empty value during testing: FFK, FRT, GCP, ITN, PLD, PLE, PLF, PLS, PPO, TV2, VLE, VLI, VLO

## Full Example

Example Thing Configuration
```
UID: solarmax:inverter:7a56fa7252
label: SolarMax Power Inverter East
thingTypeUID: solarmax:inverter
configuration:
  host: 192.168.1.151
  refreshInterval: 15
  portNumber: 12345
```
