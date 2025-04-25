# VentaAir Binding

This binding is for air humidifiers from Venta Air.
Thankfully the vendor allows for communicating within the local network without needing any internet access or accounts.
This is even stated in the official manual.
Hence this binding communicates locally with the humidifier and is able to read out the current measurements and settings, as well as changing the settings.

## Supported Things

It currently supports the LW60-T device (`ThingType`: "lw60t") as well as a `ThingType` ("generic") for other models.
For now the generic `ThingType` only adds the "boost" channel, but in the status reply from the device there is more which could be added in the future by someone who owns a different device.

## Discovery

This binding supports an automatic discovery for humidifiers that are connected to the local network and which are in the same broadcast domain.
To do so, the binding listens to UDP port 48000 for data and creates `DiscoveryResult`s based on the received data from the device.
This comes in handy for getting the MAC address for the device for example.
Once the `DiscoveryResult` is added as a `Thing`, a connection to the device will be created and it will beep, showing a confirmation screen that the device "openHAB" would like to get access.
After confirming this request, the user can link its items to receive data or control the device.

## Thing Configuration

There are three mandatory configuration parameters for a thing: `ipAddress`, `macAddress` and `deviceType`.

| parameter  | required | description |
|----------|------------|-------------------------------------------|
| ipAddress | Y | The IP Address or hostname of the device. |
| macAddress | Y | The MAC address of the device. |
| deviceType | Y | Defines the type of device. It is an integer value and its best to use the automatic discovery to obtain it from the device. |
| pollingTime | N | The time interval in seconds in which the data should be polled from the device, default is 10 seconds. |
| hash | N | It is a negative integer value and it is used by the device to identify a connection to a client, like the App from the vendor for example. (*) |

(*) I do not know whether there are devices which are restricted to only one client, so I added this parameter to allow the user to set the same value as his App on the phone (can be obtained via sniffing the network).
However, the LW60-T allows for multiple connections to different clients, identified by different `hash` values at the same time without issues.
By default the binding uses "-42", so a new ID that is not known to the device and hence it asks for confirmation, see the Discovery section.

Example Thing configuration:

```java
Thing ventaair:lw60t:humidifier [ ipAddress="192.168.42.69", macAddress="f8:f0:05:a6:4e:03", deviceType=4, pollingTime=10, hash=-42]
```

## Channels

These are the channels that are currently supported:

| channel  | type (RO=read-only)   | description                  |
|----------|--------|------------------------------|
| power          | Switch | This is the power on/off channel  |
| fanSpeed       | Number | This is the channel to control the steps (in range 0-5 where 0 means "off") for the speed of the fan |
| targetHumidity | Number | This channel sets the target humidity (in percent) that should be tried to reach by the device (allowed values: 30-70)  |
| timer          | Number | This channel sets the power off timer to the set value in hours, i.e. 3 = turn off in 3 hours from now (allowed values: 0-9 where 0 means "off") |
| sleepMode      | Switch | This channel controls the sleep mode of the device (dims the display and slows down the fan) |
| childLock      | Switch | This is the control channel for the child lock |
| automatic      | Switch | This is the control channel to start the automatic operation mode of the device |
| cleanMode      | Switch (RO) | This is the channel that indicates if the device is in the cleaning mode |
| temperature    | Number:Temperature (RO) | This channel provides the current measured temperature in Celsius or Fahrenheit as configured on the device |
| humidity       | Number:Dimensionless (RO) | This channel provides the humidity measured by the device in percent |
| waterLevel     | Number (RO) | This channel indicates the water level of the tank where 1 is equal to the yellow "refill tank" warning on the device/App |
| fanRPM         | Number (RO) | This channel provides the speed of the ventilation fan |
| timerTimePassed  | Number:Time (RO) | If a timer has been set, this channel provides the minutes since when the timer was started  |
| operationTime  | Number:Time (RO) | This channel provides the operation time of the device in hours |
| discReplaceTime | Number:Time (RO) | This channel provides the time in how many hours the cleaning disc should be replaced  |
| cleaningTime   | Number:Time (RO) | This channel provides the time in how many hours the device should be cleaned  |
| boost          | Switch | This is the control channel for the boost mode (on some devices that supports it) |

## Full Example

Things:

```java
Thing ventaair:lw60t:humidifier [ ipAddress="192.168.42.69", macAddress="f8:f0:05:a6:4e:03", deviceType=4, pollingTime=10, hash=-42]
```

Items:

```java
Group gHumidifier "Air Humidifier" <humidity>

Switch Humidifier_Power "Power: [%s]" (gHumidifier) { channel="ventaair:lw60t:humidifier:power" }
Number Humidifier_FanSpeed "FanSpeed: [%s]" (gHumidifier) { channel="ventaair:lw60t:humidifier:fanSpeed" }
Number Humidifier_TargetHum "Target Humidity: [%s]" (gHumidifier) { channel="ventaair:lw60t:humidifier:targetHumidity" }
Number Humidifier_Timer "Timer: [%s]" (gHumidifier) { channel="ventaair:lw60t:humidifier:timer" }

Switch Humidifier_SleepMode "SleepMode:" (gHumidifier) { channel="ventaair:lw60t:humidifier:sleepMode" }
Switch Humidifier_ChildLock "ChildLock:" (gHumidifier) { channel="ventaair:lw60t:humidifier:childLock" }
Switch Humidifier_Automatic "Automatic:" (gHumidifier) { channel="ventaair:lw60t:humidifier:automatic" }

Switch Humidifier_CleaningMode "Cleaning mode:" (gHumidifier) { channel="ventaair:lw60t:humidifier:cleanMode" }

Number:Temperature Humidifier_Temperature "Temp: [%.1f %unit%]" (gHumidifier) { channel="ventaair:lw60t:humidifier:temperature" }
Number:Temperature Humidifier_temperatureF "Temp: [%.1f °F]" (gHumidifier) { channel="ventaair:lw60t:humidifier:temperature" }
Number Humidifier_Humidity "Humidity: [%.1f %%]" (gHumidifier) { channel="ventaair:lw60t:humidifier:humidity" }

Number Humidifier_WaterLevel "WaterLevel: [%d]" (gHumidifier) { channel="ventaair:lw60t:humidifier:waterLevel" }
Number Humidifier_FanRPM "Fan RPM: [%d]" (gHumidifier) { channel="ventaair:lw60t:humidifier:fanRPM" }

Number Humidifier_TimerTime "Timer time: [%d]" (gHumidifier) { channel="ventaair:lw60t:humidifier:timerTimePassed" }
Number Humidifier_OpTime "Operation Time: [%d]" (gHumidifier) { channel="ventaair:lw60t:humidifier:operationTime" }
Number Humidifier_ReplaceTime "Disc replace in (h): [%d]" (gHumidifier) { channel="ventaair:lw60t:humidifier:discReplaceTime" }
Number Humidifier_CleaningTime "Cleaning in (h): [%d]" (gHumidifier) { channel="ventaair:lw60t:humidifier:cleaningTime" }

//for generic devices:
Switch boost "Boost:" { channel="ventaair:generic:humidifier:boost" }
```

Sitemap:

```perl
Text item=Humidifier_Humidity
Text item=Humidifier_Temperature
Switch item=Humidifier_Power
Switch item=Humidifier_SleepMode
Switch item=Humidifier_FanSpeed icon="fan" mappings=[0="0", 1="1", 2="2", 3="3", 4="4", 5="5"]
Switch item=Humidifier_TargetHum mappings=[30="30", 35="35", 40="40", 45="45", 50="50", 55="55", 60="60", 65="65", 70="70"]
Switch item=Humidifier_Timer mappings=[0="0", 1="1", 3="3", 5="5", 7="7", 9="9"]
Text item=Humidifier_WaterLevel
Text item=Humidifier_FanRPM
Text item=Humidifier_OpTime
Text item=Humidifier_ReplaceTime
Text item=Humidifier_CleaningTime
Text item=Humidifier_TimerTime
Switch item=Humidifier_CleaningModeActive
Switch item=Humidifier_ChildLock
Switch item=Humidifier_Automatic
```
