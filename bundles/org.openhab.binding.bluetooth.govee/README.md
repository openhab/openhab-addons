# Govee

This extension adds support for [Govee](https://www.govee.com/) Bluetooth Devices.

## Supported Things

Only two thing types are supported by this extension at the moment.

| Thing Type ID          | Description                               | Supported Models                                            |
|------------------------|-------------------------------------------|-------------------------------------------------------------|
| goveeHygrometer        | Govee Thermo-Hygrometer                   | H5051,H5071                                                 |
| goveeHygrometerMonitor | Govee Thermo-Hygrometer w/ Warning Alarms | H5052,H5072,H5074,H5075,H5101,H5102,H5177,H5179,B5175,B5178 |

## Discovery

As any other Bluetooth device, Govee devices are discovered automatically by the corresponding bridge.

## Thing Configuration

Govee things have the following configuration parameters:

| Thing                       | Parameter               | Required | Default | Description                                                                       |
|-----------------------------|-------------------------|----------|---------|-----------------------------------------------------------------------------------|
| all                         | address                 | yes      |         | The Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX")               |
| all                         | refreshInterval         |          | 300     | How often, in seconds, the sensor data of the device should be refreshed          |
| goveeHygrometer<sup>1</sup> | temperatureCalibration  | no       |         | Offset to apply to temperature<sup>2</sup> sensor readings                        |
| goveeHygrometer<sup>1</sup> | humidityCalibration     | no       |         | Offset to apply to humidity sensor readings                                       |
| goveeHygrometerMonitor      | temperatureWarningAlarm |          | false   | Enables warning alarms to be broadcast when temperature is out of specified range |
| goveeHygrometerMonitor      | temperatureWarningMin   |          | 0       | The lower safe temperature<sup>2</sup> threshold <sup>3</sup>                     |
| goveeHygrometerMonitor      | temperatureWarningMax   |          | 0       | The upper safe temperature<sup>2</sup> threshold <sup>3</sup>                     |
| goveeHygrometerMonitor      | humidityWarningAlarm    |          | false   | Enables warning alarms to be broadcast when humidity is out of specified range    |
| goveeHygrometerMonitor      | humidityWarningMin      |          | 0       | The lower safe humidity threshold <sup>3</sup>                                    |
| goveeHygrometerMonitor      | humidityWarningMax      |          | 0       | The upper safe humidity threshold <sup>3</sup>                                    |

1. Available to both `goveeHygrometer` and `goveeHygrometerMonitor` thing types.
1. In °C
1. Only applies if alarm feature is enabled

## Channels

Govee things have the following channels in addition to the default bluetooth channels:

| Thing                       | Channel ID       | Item Type              | Description                                                    |
|-----------------------------|------------------|------------------------|----------------------------------------------------------------|
| goveeHygrometer<sup>1</sup> | temperature      | Number:Temperature     | The measured temperature                                       |
| goveeHygrometer<sup>1</sup> | humidity         | Number:Dimensionless   | The measured relative humidity                                 |
| goveeHygrometer<sup>1</sup> | battery          | Number:Dimensionless   | The measured battery percentage                                |
| goveeHygrometerMonitor      | temperatureAlarm | Switch                 | Indicates if current temperature is out of range. <sup>2</sup> |
| goveeHygrometerMonitor      | humidityAlarm    | Switch                 | Indicates if current humidity is out of range. <sup>2</sup>    |

1. Available to both `goveeHygrometer` and `goveeHygrometerMonitor` thing types.
1. Only applies if warning alarms are enabled in the configuration.

## Example

demo.things:

```java
bluetooth:goveeHygrometer:hci0:beacon  "Govee Temperature Humidity Monitor" (bluetooth:bluez:hci0) [ address="12:34:56:78:9A:BC" ]
```

demo.items:

```java
Number:Temperature      temperature "Room Temperature [%.1f %unit%]" { channel="bluetooth:goveeHygrometer:hci0:beacon:temperature" }
Number:Dimensionless    humidity    "Humidity [%.0f %unit%]"         { channel="bluetooth:goveeHygrometer:hci0:beacon:humidity" }
Number:Dimensionless    battery    "Battery [%.0f %unit%]"         { channel="bluetooth:goveeHygrometer:hci0:beacon:battery" }
```
