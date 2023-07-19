# Tesla T-Smart Binding

This binding is designed to connect to the Tesla T-Smart Smart Immersion Heater / Thermostat device https://www.tsmart.co.uk/.

The device allows monitoring and control over the temperature and heating of water in a water cylinder.

## Supported Things

This supports a single thing type, the Tesla T-Smart Device.

## Discovery

The binding will discover Tesla T-Smart devices on your local network using a UDP Broadcast packet.

## Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device  | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in sec. | 60      | no       | yes      |

## Channels

| Channel         | Type               | Read/Write | Description                                |
|-----------------|--------------------|------------|--------------------------------------------|
| `power`           | Switch             | RW         | Power for the thermostat                   |
| `mode`            | String             | RW         | Operating mode of the thermostat           |
| `setpoint`        | Number:Temperature | RW         | Setpoint of the hot water temperature      |
| `temperature`     | Number:Temperature | R          | Measured temperature of the water cylinder |
| `temperatureHigh` | Number:Temperature | R          | Temperature of the high thermostat         |
| `temperatureLow`  | Number:Temperature | R          | Temperature of the low thermostat          |
| `relay`           | Switch             | R          | Relay state of the immersion heater        |
| `smartState`      | String             | R          | State of the thermostat's Smart Mode       |

## Full Example

### .things file

```java
Thing tsmart:tsmart:heater  "Immersion Heater" [hostname="192.168.0.100"]
```

### .items file

```java
Switch             TSmart_Power       "Immersion Heater Power"       {channel="tsmart:tsmart:heater:power"}
String             TSmart_Mode        "Immersion Heater Mode"        {channel="tsmart:tsmart:heater:mode"}
Number:Temperature TSmart_Setpoint    "Immersion Heater Setpoint"    {channel="tsmart:tsmart:heater:setpoint"}
Number:Temperature TSmart_Temperature "Hot Water Temperature"        {channel="tsmart:tsmart:heater:temperature"}
Switch             TSmart_Relay       "Immersion Heater Relay State" {channel="tsmart:tsmart:heater:power"}
```
