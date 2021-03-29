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

| channel                  | type   | description                                 |
| ------------------------ | ------ | ------------------------------------------- |
| LastUpdated              | Point  | When was the data last read from the device |
| SoftwareVersion    | Point  | Software Version installed on the SolarMax device |
| BuildNumber   | Point  | Firmware Build Number installed on the SolarMax device |
| Startups                 | Point  | Number of times the device has started      |
| AcPhase1Current          | Point  | Ac Phase 1 Current in Amps                  |
| AcPhase2Current          | Point  | Ac Phase 2 Current in Amps                  |
| AcPhase3Current          | Point  | Ac Phase 3 Current in Amps                  |
| EnergyGeneratedToday     | Point  | Energy Generated Today in wH                |
| EnergyGeneratedTotal    | Point  | Energy Generated since recording began in wH |
| OperatingHours           | Point  | Operating Hours since recording began in H  |
| EnergyGeneratedYesterday | Point  | Energy Generated Yesterday in wH            |
| EnergyGeneratedLastMonth | Point  | Energy Generated Last Month in wH           |
| EnergyGeneratedLastYear  | Point  | Energy Generated Last Year in wH            |
| EnergyGeneratedThisMonth | Point  | Energy Generated This Month in wH           |
| EnergyGeneratedThisYear  | Point  | Energy Generated This Year in wH            |
| Current Power Generated  | Point  | Power currently being generated in w        |
| AcFrequency              | Point  | AcFrequency in Hz                           |
| AcPhase1Voltage          | Point  | Ac Phase1 Voltage in V                      |
| AcPhase2Voltage          | Point  | Ac Phase2 Voltage in V                      |
| AcPhase3Voltage          | Point  | Ac Phase3 Voltage in V                      |
| HeatSinkTemperature      | Point  | Heat Sink Temperature in degrees celcius    |

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
