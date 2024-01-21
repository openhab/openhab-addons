# ABB/Busch-free@home Smart Home binding

openHAB ABB/Busch-free@home binding based on the offical free@home local API.

# Description

This binding allows you to connect your free@home Smart Home system from ABB / Busch-Jaeger to openHAB and to control and observe most of the components.
It requires a System Access Point with version 2.6.1 or higher.

# Supported Devices

**Network Gateway / System Access Point**

 - ABB / Busch-Jaeger System Access Point 2.0

**Sensors and Actuators**

 - Switch Actuator Sensor with single and multiple channels (wired and wireless)
 - Dimming Actuator Sensor with single and multiple channels (wired, wireless and flex)
 - Motion detection with and without actuator (wired, wireless and flex)
 - Switch Actuator 4-channel
 - Dimming Actuator 4- and 6-channel
 - Door opener actuator
 - Door ring sensor
 - Hue devices (untested)

**Blinds and Windows**

 - Shutter Actuator with single and multiple channels (wired and wireless)
 - Blind Actuator  with single and multiple channels (wired and wireless)
 - Attic window actuator
 - Awning actuator

**Room Temperature Control**

 - Room temperature controller master without fan
 - Room temperature controller master with fan
 - Room temperature controller slave

**Other devices** (e.g. movement detector, ring sensor and door opener)

 - IP-touch panel (function: door opener, door ring sensor)
 - Virtual devices (e.g. virtual switch, RTC and detectors)

**Information about virtual devices**
Virtual device in the free@home smart-home system needs continuous keep-alive signal otherwise the free@home device is marked as unresponsive.
This keep-alive signal must be provided by a user script or set the TTL value of the virtual device to "-1" during the creation of the virtual device.

# Tested SysAP Versions

| Version | Supported |
|---------|-----------|
| 2.6.1   | yes       |
| 2.6.3   | yes       |
| 3.1.1   | yes       |

# Setup / Installation

## Prerequisites

To make use of this Binding first the local free@home API has to be activated.
The API is disabled by default.

1. Open the free@home next app
1. Browse to "Settings ⇨ free@home settings ⇨ local API and activate the checkbox

## Setup and Discovery

The free@home bridge shall be added manually.
Once it is added as a Thing with correct credentials, the scan of free@home devices will be possible.

## free@home components as openHAB Things

The ABB/Busch free@home system is calling its smart home components as free@home devices.
The free@home system devices can have one or multiple channels depending the device's features.
During the scanning process the openHAB binding will detect only the devices IDs, the device features will be detected at the point in time, when a openHAB Thing is created.
At the of the creation the free@home binding will automatically create the relevant channels without any further configuration.
If a free@home system device has multiple smart-home channels (e.g. 4x DIN/rail Actuator), the newly created Thing will get all relevant channels to operate all actuators existing inside the free@home device.

## Sensors and Actuators of free@home Devices as Things in openHAB

The free@home system is supporting sensors and actuators.
The connection of sensors and actuators are done on the free@home system dashboard.
If a Thing channel is a free@home device sensor channel, this channel is read only.

## Bridge Configuration

There are several settings for a bridge:

| Parameter                | Description                             |
|--------------------------|-----------------------------------------|
| **ipAddress** (required) | Network address of the free@home SysAP  |
| **username** (required)  | Valid user name for the free@home SysAP |
| **password** (required)  | Password of the user                    |

## Examples for .things

Things are all discovered automatically and visible on the openHAB dashboard after pushing the scan button

In order to manually configure a Thing:

```java
Bridge freeathomesystem:bridge:mysysap [ ipAddress="...", username="...", password="..." ]
{
    Thing free-at-home-device    ABB700997F0A
}
```

The only parameter needed to create a Thing is the free@home device ID, which you can find as sticker on the device.
The creation of the openHAB channels to operate the free@home device is happening automatically based on the device features detected online.

## Examples for .items

Sample for the free@home thermostat device

```java
Number Livingroom_MeasuredTemperature                     "Measured Temperature"            <temperature>  (Livingroom)  ["Measurement"]  { channel="freeathomesystem:thermostat:0836e63805:ABB700CF0FB0_0:thermostatMeasuredTemperature" }
Number LivingRoom_Thermostat_SetpointTemperature          "Setpoint Temperature"            <temperature>  (Livingroom)  ["Setpoint"]     { channel="freeathomesystem:thermostat:0836e63805:ABB700CF0FB0_0:thermostatSetpointTemperature" }
Switch LivingRoom_Thermostat_ThermostatActivationOnOff    "Thermostat Activation"           <switch>       (Livingroom)  ["Setpoint"]     { channel="freeathomesystem:thermostat:0836e63805:ABB700CF0FB0_0:thermostatOnoffSwitch" }
Switch LivingRoom_Thermostat_MainEcoOnOff                 "Thermostat Eco Activation"       <switch>       (Livingroom)  ["Setpoint"]     { channel="freeathomesystem:thermostat:0836e63805:ABB700CF0FB0_0:thermostatEcoSwitch" }
Number LivingRoom_ThermostatHeatingActive                 "Thermostat Heating Active"       <temperature>  (Livingroom)  ["Status"]       { channel="freeathomesystem:thermostat:0836e63805:ABB700CF0FB0_0:thermostatHeatingActive" }
Number LivingRoom_ThermostatHeatingDemand                 "Thermostat Heating Demand"       <temperature>  (Livingroom)  ["Status"]       { channel="freeathomesystem:thermostat:0836e63805:ABB700CF0FB0_0:thermostatHeatingDemand" }
```

Sample for the free@home sensor/actuator 1/1

```java
Switch SwitchMoodlight_Livingroom                         "Moodlight Livingroom"     <switch>  (Livingroom)  ["Light"]   { channel="freeathomesystem:actuator:0836e63805:ABB700D85E0A_0:switchChannel }
```

Sample for the free@home sensor/actuator 2/2

```java
Switch Switch_Livingroom_Double_1                         "Moodlight Livingroom"     <switch>  (Livingroom)  ["Light"]   { channel="freeathomesystem:actuator:0836e63805:ABB700887F0A_0:switchChannel }
Switch Switch_Livingroom_Double_2                         "Moodlight Livingroom"     <switch>  (Livingroom)  ["Light"]   { channel="freeathomesystem:actuator:0836e63805:ABB700887F0A_1:switchChannel }
```

# Communities

[openhAB communitiy of this binding](https://community.openhab.org/t/abb-busch-jager-free-home-official-rest-api/141698/71)

[Busch-Jaeger Community](https://community.busch-jaeger.de/)

[free@home user group Facebook DE](https://www.facebook.com/groups/738242583015188)

[free@home user group Facebook EN](https://www.facebook.com/groups/452502972031360)
