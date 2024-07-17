# Jeelink Binding

This binding integrates JeeLink USB RF receivers and LaCrosseGateways.

## Introduction

Binding should be compatible with JeeLink USB receivers and LaCrosseGateways. It supports connected LaCrosse temperature sensors, EC3000 sensors, PCA301 power monitoring wireless sockets and TX22 temperature and humidity sensors (including connected TX23 wind and TX26 rain sensors).

## Supported Things

This binding supports:

- JeeLink (connected to USB)
- JeeLink (connected over TCP)
- LaCrosseGateway (connected to USB)
- LaCrosseGateway (connected over TCP)
- LaCrosse temperature sensors
- EC3000 power monitors
- Revolt power monitors
- PCA301 power monitoring wireless sockets
- TX22 temperature & humidity Sensors (including connected TX23 wind and TX26 rain sensors)

## Binding configuration

Configuration of the binding is not needed.

## Thing discovery

Only sensor discovery is supported, the thing for the USB receiver / LaCrosseGateway has to be created manually. Pay attention to use the correct serial port, as otherwise the binding may interfere with other bindings accessing serial ports.

Afterwards, sensor discovery can be triggered using the `Scan` button in `Things` &rarr; `+` &rarr; `JeeLink Binding`.
Discovery only detects sensors that actually send a value during discovery.
These will show up in your inbox.

LaCrosse temperature sensors send values every few seconds, so that they are normally caught by the discovery. In rare cases, a second discovery run is needed.

PCA301 sockets are polled every 120 seconds by default. This results in sockets not being found by the discovery. In order to make sure the socket is discovered, press the button on the socket during discovery (and make sure you have paired the socket to the USB stick / LaCrosseGateway before by pressing the button for 3 seconds while the receiver is powered).

## Thing configuration

### JeeLink / LaCrosseGateway (connected to USB)

| Parameter     | Item Type | Description                                                                                                                                  |
|---------------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------|
| Serial Port   | String    | The serial port name for the USB receiver / LaCrosseGateway. Valid values are e.g. COM1 for Windows and /dev/ttyS0 or /dev/ttyUSB0 for Linux |
| Baud Rate     | Number    | The baud rate of the USB Receiver. Valid values are 9600, 19200, 38400, 57600 (default), and 115200                                          |
| Init Commands | String    | A semicolon separated list of init commands that will be send to the Jeelink / LaCrosseGateway, e.g. "0a" for disabling the LED              |

The available init commands depend on the sketch that is running on the USB stick / LaCrosseGateway.

### JeeLink / LaCrosseGateway (connected over TCP)

| Parameter     | Item Type | Description                                                                                                                       |
|---------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------|
| IP Address    | String    | The IP address of the Server to which the USB Receiver is connected, or the IP address of the LaCrosseGateway                     |
| TCP Port      | Number    | The TCP port over which the serial port is made available, or the LaCrosseGateway port (which usually is 81)                      |
| Init Commands | String    | A semicolon separated list of init commands that will be send to the Jeelink / LaCrosseGateway, e.g. "0a" for disabling the LED   |

### LaCrosse temperature sensors

| Parameter                  | Item Type | Description                                                                                                                                          |
|----------------------------|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| Sensor ID                  | Number    | The ID of the connected sensor                                                                                                                       |
| Sensor Timeout             | Number    | The amount of time in seconds that should result in OFFLINE status when no readings have been received from the sensor                               |
| Update Interval            | Number    | The update interval in seconds how often value updates are propagated. A value of 0 leads to propagation of every value                              |
| Buffer Size                | Number    | The number of readings used for computing the rolling average                                                                                        |
| Lower Temperature Limit    | Decimal   | The lowest allowed valid temperature. Lower temperature readings will be ignored                                                                     |
| Upper Temperature Limit    | Decimal   | The highest allowed valid temperature. Higher temperature readings will be ignored                                                                   |
| Maximum allowed difference | Decimal   | The maximum allowed difference from a value to the previous value (0 disables this check). If the difference is higher, the reading will be ignored. |

### EC3000 power monitors

| Parameter       | Item Type | Description                                                                                                             |
|-----------------|-----------|-------------------------------------------------------------------------------------------------------------------------|
| Sensor ID       | Number    | The ID of the connected sensor                                                                                          |
| Sensor Timeout  | Number    | The amount of time in seconds that should result in OFFLINE status when no readings have been received from the sensor  |
| Update Interval | Number    | The update interval in seconds how often value updates are propagated. A value of 0 leads to propagation of every value |
| Buffer Size     | Number    | The number of readings used for computing the rolling average                                                           |

### PCA301 power monitoring wireless sockets

| Parameter         | Item Type    | Description                                                                                                            |
|-------------------|--------------|------------------------------------------------------------------------------------------------------------------------|
| Sensor ID         | Number       | The ID of the connected sensor                                                                                         |
| Sensor Timeout    | Number       | The amount of time in seconds that should result in OFFLINE status when no readings have been received from the sensor |
| Retry Count       | Number       | The number of times a switch command will be resent to the socket until giving up                                      |

### Revolt power monitors

| Parameter         | Item Type    | Description                                                                                                            |
|-------------------|--------------|------------------------------------------------------------------------------------------------------------------------|
| Sensor ID         | Number       | The ID of the connected sensor                                                                                         |
| Sensor Timeout    | Number       | The amount of time in seconds that should result in OFFLINE status when no readings have been received from the sensor |

### EMT7110 energy meter

| Parameter         | Item Type    | Description                                                                                                            |
|-------------------|--------------|------------------------------------------------------------------------------------------------------------------------|
| Sensor ID         | Number       | The ID of the connected sensor                                                                                         |
| Sensor Timeout    | Number       | The amount of time in seconds that should result in OFFLINE status when no readings have been received from the sensor |


## Channels

### LaCrosse temperature sensors

| Channel Type ID | Item Type             | Description                                       |
|-----------------|-----------------------|---------------------------------------------------|
| temperature     | Number:Temperature    | Temperature reading                               |
| humidity        | Number:Dimensionless  | Humidity reading                                  |
| batteryNew      | Contact               | Whether the battery is new (CLOSED) or not (OPEN) |
| batteryLow      | Contact               | Whether the battery is low (CLOSED) or not (OPEN) |

### TX22 temperature and humidity sensors

| Channel Type ID | Item Type             | Description                |
|-----------------|-----------------------|----------------------------|
| temperature     | Number:Temperature    | Temperature reading        |
| humidity        | Number:Dimensionless  | Humidity reading           |
| pressure        | Number:Pressure       | Current pressure reading   |
| rain            | Number:Length         | Rainfall today             |
| windStrength    | Number:Speed          | Current wind speed         |
| windAngle       | Number:Angle          | Current wind direction     |
| gustStrength    | Number:Speed          | Gust speed                 |

### EC3000 power monitors

| Channel Type ID  | Item Type     | Description                               |
|------------------|---------------|-------------------------------------------|
| currentPower     | Number:Power  | Current power draw                        |
| maxPower         | Number:Power  | Maximum power draw                        |
| consumptionTotal | Number:Energy | Total energy consumption                  |
| applianceTime    | Number:Time   | Total electrical appliance operating time |
| sensorTime       | Number:Time   | Total turn on time of power monitor       |
| resets           | Number        | Number of resets                          |

### PCA301 power monitoring wireless sockets

| Channel Type ID         | Item Type     | Description                                          |
|-------------------------|---------------|------------------------------------------------------|
| switchingState          | Switch        | Whether the sockets are currently switched on or off |
| currentPower            | Number:Power  | Current power draw                                   |
| consumptionTotal        | Number:Energy | Total energy consumption                             |

### Revolt power monitors

| Channel Type ID   | Item Type                | Description                               |
|-------------------|--------------------------|-------------------------------------------|
| currentPower      | Number:Power             | Current power draw                        |
| consumptionTotal  | Number:Energy            | Total energy consumption                  |
| powerFactor       | Number                   | Ratio of real power to apparent power     |
| electricCurrent   | Number:ElectricCurrent   | The measured Electric Current             |
| electricPotential | Number:ElectricPotential | The measured Electric Potential           |
| powerFrequency    | Number:Frequency         | The measured AC power frequency           |

### EMT7110 energy meter

| Channel Type ID   | Item Type                | Description                           |
|-------------------|--------------------------|---------------------------------------|
| currentPower      | Number:Power             | Current power draw                    |
| consumptionTotal  | Number:Energy            | Total energy consumption in kWh       |
| electricCurrent   | Number:ElectricCurrent   | The measured Electric Current         |
| electricPotential | Number:ElectricPotential | The measured Electric Potential in mA |


## Commands

### PCA301 power monitoring wireless sockets

| Channel Type ID         | Item Type    | Description                                       |
|-------------------------|--------------|---------------------------------------------------|
| switchingState          | Switch       | Supports ON and OFF commands to switch the socket |

## Full Example

A typical thing configuration for PCA301 looks like this:

```java
Bridge jeelink:jeelinkUsb:pca301 "Jeelink pca301" @ "home" [ serialPort="/dev/ttyUSB0" ]
Thing jeelink:pca301:1-160-236 "ec3k 1" (jeelink:jeelinkUsb:pca301)  @ "home" [ sensorId="1-160-236"]
```

A typical thing configuration for EC3000 looks like this:

```java
Bridge jeelink:jeelinkUsb:ec3k "Jeelink ec3k" @ "home" [ serialPort="COM4" ]
Thing jeelink:ec3k:0E3D "ec3k 1" (jeelink:jeelinkUsb:ec3k)  @ "home" [ sensorId="0E3D"]
Thing jeelink:ec3k:14E7 "ec3k 2" (jeelink:jeelinkUsb:ec3k)  @ "home" [ sensorId="14E7"]
```

A typical Thing configuration for lacrosse looks like this:

```java
Bridge jeelink:jeelinkUsb:lacrosse "Jeelink lacrosse" @ "home" [ serialPort="COM6" ]
Thing jeelink:lacrosse:sensor1 "Jeelink lacrosse 1" (jeelink:jeelinkUsb:lacrosse)  @ "home" [ sensorId="16", minTemp=10, maxTemp=32]
Thing jeelink:lacrosse:sensor2 "Jeelink lacrosse 2" (jeelink:jeelinkUsb:lacrosse)  @ "home" [ sensorId="18", minTemp=10, maxTemp=32]
```

A typical thing configuration for Revolt looks like this:

```java
Bridge jeelink:jeelinkUsb:revolt "Jeelink revolt" @ "home" [ serialPort="COM4" ]
Thing jeelink:revolt:4F1B "revolt 1" (jeelink:jeelinkUsb:revolt)  @ "home" [ sensorId="4F1B"]
```

A typical item configuration for a LaCrosse temperature sensor looks like this:

```java
Number:Dimensionless Humidty_LR "Living Room [%.1f %unit%]" <humidity> {channel="jeelink:lacrosse:42:humidity"}
Number:Temperature Temperature_LR "Living Room [%.1f %unit%]" <temperature> {channel="jeelink:lacrosse:42:temperature"}
Contact Battery_Low_LR "Battery Low Living Room" {channel="jeelink:lacrosse:42:batteryLow"}
Contact Battery_New_LR "Battery New Living Room" {channel="jeelink:lacrosse:42:batteryNew"}
```

A typical item configuration for a PCA301 power monitoring wireless sockets looks like this:

```java
Switch SocketSwitch {channel="jeelink:pca301:1-160-236:switchingState"}
Number:Power SocketWattage {channel="jeelink:pca301:1-160-236:currentPower"}
Number:Energy SocketConsumption {channel="jeelink:pca301:1-160-236:consumptionTotal"}
```

A typical item configuration for a TX22 temperature and humidity sensor looks like this:

```java
Number:Dimensionless Humidity "Outside [%.1f %unit%]" <humidity> {channel="jeelink:tx22:42:humidity"}
Number:Temperature Temperature "Outside [%.1f %unit%]" <temperature> {channel="jeelink:tx22:42:temperature"}
Contact Battery_Low_LR "Battery Low Outside" {channel="jeelink:tx22:42:batteryLow"}
Contact Battery_New_LR "Battery New Outside" {channel="jeelink:tx22:42:batteryNew"}
Number:Length Rain "Outside [%.1f %unit%]" {channel="jeelink:tx22:42:rain"}
Number:Speed WindStrength "Wind [%.1f %unit%]" {channel="jeelink:tx22:42:windStrength"}
Number:Angle WindDir "Wind dir [%.1f %unit%]" {channel="jeelink:tx22:42:windAngle"}
Number:Speed GustStrength "Gust [%.1f %unit%]" {channel="jeelink:tx22:42:gustStrength"}
```

A typical item configuration for a Revolt power monitor looks like this:

```java
Number:Power SocketWattage {channel="jeelink:revolt:4F1B:currentPower"}
Number:Energy SocketConsumption {channel="jeelink:revolt:4F1B:consumptionTotal"}
Number:Dimensionless POwerFactor {channel="jeelink:revolt:4F1B:powerFactor"}
Number:ElectricCurrent Current {channel="jeelink:revolt:4F1B:electricCurrent"}
Number:ElectricPotential Voltage {channel="jeelink:revolt:4F1B:electricPotential"}
Number:Frequency PowerFrequency {channel="jeelink:revolt:4F1B:powerFrequency"}
```
