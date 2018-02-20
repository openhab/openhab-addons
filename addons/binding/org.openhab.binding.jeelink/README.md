# Jeelink Binding

This binding integrates JeeLink USB RF receivers.

## Introduction

Binding should be compatible with JeeLink USB receivers and connected LaCrosse temperature sensors as well as EC3000 sensors.

## Supported Things

This binding supports:

*   JeeLink USB RF receivers (as bridge)
*   JeeLink USB RF receivers connected over TCP (as bridge)
*   LaCrosse Temperature Sensors connected to the bridge
*   EC3000 Power Monitors connected to the bridge

## Binding configuration

Configuration of the binding is not needed.

## Thing discovery

Only sensor discovery is supported, the thing for the USB receiver has to be created manually. Pay attention to use the correct serial port, as otherwise the binding may interfere with other bindings accessing serial ports.

Afterwards, discovery reads from the USB receiver to find out which sensors are currently connected.
It then creates a thing for every sensor for which currently no other thing with the same sensor ID is registered with the bridge.

## Thing configuration

#### JeeLink USB receivers

| Parameter     | Item Type | Description                                                                                                                |
|---------------|-----------|----------------------------------------------------------------------------------------------------------------------------|
| Serial Port   | String    | The serial port name for the USB receiver. Valid values are e.g. COM1 for Windows and /dev/ttyS0 or /dev/ttyUSB0 for Linux |
| Baud Rate     | Number    | The baud rate of the USB Receiver. Valid values are 9600, 19200, 38400, 57600 (default), and 115200                        |
| Init Commands | String    | A semicolon separated list of init commands that will be send to the Jeelink, e.g. "0a v" for disabling the LED            |


#### JeeLink receivers connected over TCP

| Parameter     | Item Type | Description                                                                                                     |
|---------------|-----------|-----------------------------------------------------------------------------------------------------------------|
| IP Address    | String    | The IP address of the Server to which the USB Receiver is connected                                             |
| TCP Port      | Number    | The TCP port over which the serial port is made available                                                       |
| Init Commands | String    | A semicolon separated list of init commands that will be send to the Jeelink, e.g. "0a v" for disabling the LED |


#### LaCrosse temperature sensors

| Parameter                  | Item Type | Description                                                                                                                                          |
|----------------------------|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| Sensor ID                  | Number    | The ID of the connected sensor                                                                                                                       |
| Sensor Timeout             | Number    | The amount of time in seconds that should result in OFFLINE status when no readings have been received from the sensor                               |
| Update Interval            | Number    | The update interval in seconds how often value updates are propagated. A value of 0 leads to propagation of every value                              |
| Buffer Size                | Number    | The number of readings used for computing the rolling average                                                                                        |
| Lower Temperature Limit    | Decimal   | The lowest allowed valid temperature. Lower temperature readings will be ignored                                                                     |
| Upper Temperature Limit    | Decimal   | The highest allowed valid temperature. Higher temperature readings will be ignored                                                                   |
| Maximum allowed difference | Decimal   | The maximum allowed difference from a value to the previous value (0 disables this check). If the difference is higher, the reading will be ignored. |


#### EC3000 power monitors

| Parameter       | Item Type | Description                                                                                                             |
|-----------------|-----------|-------------------------------------------------------------------------------------------------------------------------|
| Sensor ID       | Number    | The ID of the connected sensor                                                                                          |
| Sensor Timeout  | Number    | The amount of time in seconds that should result in OFFLINE status when no readings have been received from the sensor  |
| Update Interval | Number    | The update interval in seconds how often value updates are propagated. A value of 0 leads to propagation of every value |
| Buffer Size     | Number    | The number of readings used for computing the rolling average                                                           |

## Channels

#### LaCrosse temperature sensors

| Channel Type ID | Item Type | Description                                       |
|-----------------|-----------|---------------------------------------------------|
| temperature     | Number    | Temperature reading                               |
| humidity        | Number    | Humidity reading                                  |
| batteryNew      | Contact   | Whether the battery is new (CLOSED) or not (OPEN) |
| batteryLow      | Contact   | Whether the battery is low (CLOSED) or not (OPEN) |

#### EC3000 power monitors

| Channel Type ID  | Item Type | Description                                        |
|------------------|-----------|----------------------------------------------------|
| currentWatt      | Number    | Instantaneous power in Watt                        |
| maxWatt          | Number    | Maximum load power in Watt                         |
| consumptionTotal | Number    | Total energy  consumption                          |
| applianceTime    | Number    | Total electrical appliance operating time in hours |
| sensorTime       | Number    | Total turn on time of power monitor in hours       |
| resets           | Number    | Number of resets                                   |

## Commands

The binding does not handle commands.

## Full Example

A typical Thing configuration for ec3k looks like this:

```
Thing jeelink:jeelinkUsb:ec3k "Jeelink ec3k" @ "home" [ serialPort="COM4", sketchName="ec3kSerial" ]
Thing jeelink:ec3k:0E3D "ec3k 1" (jeelink:jeelinkUsb:ec3k)  @ "home" [ sensorId="0E3D"]
Thing jeelink:ec3k:14E7 "ec3k 2" (jeelink:jeelinkUsb:ec3k)  @ "home" [ sensorId="14E7"]
```

A typical Thing configuration for lacrosse looks like this:

```
Thing jeelink:jeelinkUsb:lacrosse "Jeelink lacrosse" @ "home" [ serialPort="COM6", sketchName="LaCrosseITPlusReader" ]
Thing jeelink:lacrosse:sensor1 "Jeelink lacrosse 1" (jeelink:jeelinkUsb:lacrosse)  @ "home" [ sensorId="16", minTemp=10, maxTemp=32]
Thing jeelink:lacrosse:sensor2 "Jeelink lacrosse 2" (jeelink:jeelinkUsb:lacrosse)  @ "home" [ sensorId="18", minTemp=10, maxTemp=32]
```

A typical item configuration for a LaCrosse temperature sensor looks like this:

```
Number Humidty_LR "Living Room" <humidity> {channel="jeelink:lacrosse:42:humidity"}
Number Temperature_LR "Living Room" <temperature> {channel="jeelink:lacrosse:42:temperature"}
Contact Battery_Low_LR "Battery Low Living Room" {channel="jeelink:lacrosse:42:batteryLow"}
Contact Battery_New_LR "Battery New Living Room" {channel="jeelink:lacrosse:42:batteryLow"}
```
