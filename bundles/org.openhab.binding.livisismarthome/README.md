# LIVISI SmartHome Binding

The binding integrates the [LIVISI (RWE/innogy) SmartHome](https://www.livisi.de) system into openHAB.
The binding is the successor of the innogy SmartHome openHAB binding, which was communicating with the LIVISI cloud servers over the Internet.

This binding communicates directly with LIVISI SmartHome Controllers (SHC) and not through the LIVISI cloud services.

On your SHC you need a minimum software version of 1.2.XX.XXX (SHC 2) or 1.914-3.1.XXXX.XX (SHC 1 / classic) with activated "Local SmartHome".

## Supported things

### Bridge

The LIVISI SmartHome Controller (SHC) is the bridge, that provides the central communication with the devices.
Without the SHC, you cannot communicate with the devices.

### Devices

The following table shows all supported and tested devices and their channels.
The channels are described in detail in the next chapter.

| Device | Description                                                              | Supported channels                                                                                                           |
|--------|--------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| SHC    | SmartHome Controller (Bridge)                                            | status, cpu, disk, memory (updated by events; SHC classic: Updated every minute)                                             |
| BRC8   | Basic Remote Controller                                                  | button1 ... button8, button1Count ... button8Count, batteryLow                                                               |
| ISC2   | In Wall Smart Controller                                                 | button1, button2, button1Count, button2Count, batteryLow                                                                     |
| ISD2   | In Wall Smart Dimmer                                                     | button1, button2, button1Count, button2Count, dimmer                                                                         |
| ISR2   | In Wall Smart Rollershutter                                              | button1, button2, button1Count, button2Count, rollershutter                                                                  |
| ISS2   | In Wall Smart Switch                                                     | button1, button2, button1Count, button2Count, switch                                                                         |
| PSD    | Pluggable Smart Dimmer                                                   | dimmer                                                                                                                       |
| PSS    | Pluggable Smart Switch, indoor                                           | switch                                                                                                                       |
| PSSO   | Pluggable Smart Switch, outdoor                                          | switch                                                                                                                       |
| BT-PSS | Bluetooth Pluggable Smart Switch, indoor                                 | switch                                                                                                                       |
| RST    | Radiator Mounted Smart Thermostat                                        | targetTemperature, currentTemperature, frostWarning, humidity, moldWarning, operationMode, windowReductionActive, batteryLow |
| RST2   | Radiator Mounted Smart Thermostat (newer two battery version since 2018) | targetTemperature, currentTemperature, frostWarning, humidity, moldWarning, operationMode, windowReductionActive, batteryLow |
|        | VariableActuator                                                         | switch                                                                                                                       |
| WDS    | Window Door Sensor                                                       | contact, batteryLow                                                                                                          |
| WMD    | Wall Mounted Motion Detector, indoor                                     | motionCount, luminance, batteryLow                                                                                           |
| WMDO   | Wall Mounted Motion Detector, outdoor                                    | motionCount, luminance, batteryLow                                                                                           |
| WRT    | Wall Mounted Room Thermostat                                             | targetTemperature, currentTemperature, frostWarning, humidity, moldWarning, operationMode, windowReductionActive, batteryLow |
| WSC2   | Wall Mounted Smart Controller                                            | button1, button2, button1Count, button2Count, batteryLow                                                                     |
| WSD    | Wall Mounted Smoke Detector, old version                                 | smoke, alarm, batteryLow                                                                                                     |
| WSD2   | Wall Mounted Smoke Detector, new version                                 | smoke, alarm, batteryLow                                                                                                     |

Powermeter devices

| Device          | Description                                                    | Supported channels                                                                                                                                                                 |
|-----------------|----------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AnalogMeter     | The Analog Meter from the LIVISI EnergyControl product         | energyConsumptionMonthKwh, absoluteEnergyConsumption, energyConsumptionMonthEuro, energyConsumptionDayEuro, energyConsumptionDayKwh                                                |
| GenerationMeter | The Generation Meter from the LIVISI PowerControlSolar product | energyGenerationMonthKwh, totalEnergyGeneration, energyGenerationMonthEuro, energyGenerationDayEuro, energyGenerationDayKwh, powerGenerationWatt                                   |
| SmartMeter      | The Smart Meter from the LIVISI PowerControl product.          | energyConsumptionMonthKwh, absoluteEnergyConsumption, energyConsumptionMonthEuro, energyConsumptionDayEuro, energyConsumptionDayKwh, powerConsumptionWatt                          |
| Two-Way-Meter   | The Two-Way-Meter from the LIVISI PowerControlSolar product    | energyMonthKwh, totalEnergy, energyMonthEuro, energyDayEuro, energyDayKwh, energyFeedMonthKwh, totalEnergyFed, energyFeedMonthEuro, energyFeedDayEuro, energyFeedDayKwh, powerWatt |

## Discovery

The bridge (SHC) can not be discovered automatically. It must be added manually (see below under "Configuration").

After the bridge is added, devices are discovered automatically.
As there is no background discovery implemented at the moment, you have to start the discovery manually.
However, only devices will appear that are added in the LIVISI SmartHome app before, because the LIVISI Binding does not support the coupling of devices to the bridge.

## Channels

| Channel Type ID       | Item Type     | Description                                                               | Available on thing                                          |
|-----------------------|---------------|---------------------------------------------------------------------------|-------------------------------------------------------------|
| alarm                 | Switch        | Switches the alarm (ON/OFF)                                               | WSD, WSD2                                                   |
| batteryLow            | Switch        | Indicates, if the battery is low (ON/OFF)                                 | BRC8, ISC2, RST, RST2, WDS, WMD, WMD0, WRT, WSC2, WSD, WSD2 |
| contact               | Contact       | Indicates the contact state (OPEN/CLOSED)                                 | WDS                                                         |
| cpu                   | Number        | CPU-Usage of the SHC in percent                                           | SHC (bridge)                                                |
| dimmer                | Dimmer        | Allows to dimm a light device                                             | ISD2, PSD                                                   |
| disk                  | Number        | Disk-Usage of the SHC in percent                                          | SHC (bridge)                                                |
| frostWarning          | Switch        | active, if the measured temperature is too low (ON/OFF)                   | RST, RST2, WRT                                              |
| humidity              | Number        | Relative humidity in percent                                              | RST, RST2, WRT                                              |
| button1               | -             | Trigger channel for rules, fires with each push                           | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2                          |
| button2               | -             | Trigger channel for rules, fires with each push                           | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2                          |
| button3               | -             | Trigger channel for rules, fires with each push                           | BRC8                                                        |
| button4               | -             | Trigger channel for rules, fires with each push                           | BRC8                                                        |
| button5               | -             | Trigger channel for rules, fires with each push                           | BRC8                                                        |
| button6               | -             | Trigger channel for rules, fires with each push                           | BRC8                                                        |
| button7               | -             | Trigger channel for rules, fires with each push                           | BRC8                                                        |
| button8               | -             | Trigger channel for rules, fires with each push                           | BRC8                                                        |
| button1Count          | Number        | Number of button pushes for button 1, increased with each push            | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2                          |
| button2Count          | Number        | Number of button pushes for button 2, increased with each push            | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2                          |
| button3Count          | Number        | Number of button pushes for button 3, increased with each push            | BRC8                                                        |
| button4Count          | Number        | Number of button pushes for button 4, increased with each push            | BRC8                                                        |
| button5Count          | Number        | Number of button pushes for button 5, increased with each push            | BRC8                                                        |
| button6Count          | Number        | Number of button pushes for button 6, increased with each push            | BRC8                                                        |
| button7Count          | Number        | Number of button pushes for button 7, increased with each push            | BRC8                                                        |
| button8Count          | Number        | Number of button pushes for button 8, increased with each push            | BRC8                                                        |
| luminance             | Number        | Indicates the measured luminance in percent                               | WMD, WMD0                                                   |
| memory                | Number        | Memory-Usage of the SHC in percent                                        | SHC (bridge)                                                |
| moldWarning           | Switch        | Active, if the measured humidity is too low (ON/OFF)                      | RST, RST2, WRT                                              |
| motionCount           | Number        | Number of detected motions, increases with each detected motion           | WMD, WMDO                                                   |
| operationMode         | String        | The mode of a thermostat (auto/manual)                                    | RST, RST2, WRT                                              |
| rollershutter         | Rollershutter | Controls a roller shutter                                                 | ISR2                                                        |
| targetTemperature     | Number        | Sets the target temperature in °C (min 6 °C, max 30 °C)                   | RST, RST2, WRT                                              |
| smoke                 | Switch        | Indicates, if smoke was detected (ON/OFF)                                 | WSD, WSD2                                                   |
| status                | String        | Status of the SHC (ACTIVE/NORMAL, INITIALIZING/REBOOTING or SHUTTINGDOWN) | SHC (bridge)                                                |
| switch                | Switch        | A switch to turn the device or variable on/off (ON/OFF)                   | ISS2, PSS, PSSO, VariableActuator                           |
| currentTemperature    | Number        | Holds the actual temperature in °C                                        | RST, RST2, WRT                                              |
| windowReductionActive | Switch        | Indicates if a linked window is open and temperature reduced (ON/OFF)     | RST, RST2, WRT                                              |

The `rollershutter` channel has a `boolean` parameter `invert`.
It is `false` by default.
This means `100` on LIVISI is `UP` and `0` is `DOWN`.
When `invert` is `true` than `0` on LIVISI is `UP` and `100` is `DOWN`.

## Triggers

| Trigger Type  | Description                                   | Available on thing                  |
|---------------|-----------------------------------------------|-------------------------------------|
| SHORT_PRESSED | Fired when you press a button short           | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2  |
| LONG_PRESSED  | Fired when you press a button longer          | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2  |
| PRESSED       | Fired when you press a button (short or long) | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2  |

## Thing configuration

### Configuring the SmartHome Controller (SHC)

The SmartHome Controller (SHC) can be configured in the UI as follows:

When adding the "LIVISI SmartHome Controller" via the Inbox, you have to define the hostname or local IP address and the password for the local user.
Save your changes. The SHC should now login and go online.

### Discovering devices

All devices bound to the bridge are found by the discovery service once the SHC is online.
As device discovery is not implemented as a background service, you should start it manually in the Inbox to find all devices.
Now you can add all devices from your Inbox as things.

### File based configuration

As an alternative to the automatic discovery process and graphical configuration using the UI, LIVISI things can be configured manually.
The LIVISI SmartHome Controller (SHC) can be configured using the following syntax:

```java
Bridge livisismarthome:bridge:<bridge-id> "Livisi: SmartHome Controller (SHC)" [ host="192.168.0.99", password="SomethingSecret", webSocketIdleTimeout=900]
```

** _Security warning!_*
The communication between the binding and the SHC is not encrypted and can be traced.
So be careful and secure your local network from unauthorized access.

All other LIVISI devices can be added using the following syntax:

```java
Thing WDS <thing-id> "<thing-name>" @ "<room-name>" [ id="<the-device-id>" ]
```

The device ID (e.g. e9a74941a3807b57332214f346fb1129) can be found in the UI inbox, as you find it below all things there in the form `livisismarthome:<device-type>:<bridge-id>:<the-device-id>` (example: `livisismarthome:WSC2:SMARTHOME01:e9a74941a3807b57332214f346fb1129`).

However, a full example .things configuration look like this:

```java
Bridge livisismarthome:bridge:mybride "LIVISI SmartHome Controller" {
    Thing ISD2 myDimmer "Dimmer Kitchen" @ "Kitchen" [ id="<device-id>" ]
    Thing ISS2 myLightSwitch "Light Livingroom" @ "Livingroom" [ id="<device-id>" ]
    Thing PSS myTVSwitch "TV" @ "Livingroom" [ id="<device-id>" ]
    Thing RST myHeating "Thermostat Livingroom" @ "Livingroom" [ id="<device-id>" ]
    Thing ISR2 myRollerShutter1 "RollerShutter" @ "Livingroom" [ id="<device-id>" ]
    Thing ISR2 myRollerShutter2 "RollerShutter (inverted)" @ "Livingroom" [ id="<device-id>" ] {Type rollershutterActuator : rollershutter  [invert=true]}
    Thing VariableActuator myLivisiVariable "My Variable" [ id="<device-id>" ]
    Thing WDS myWindowContact "Window Kitchen" @ "Kitchen" [ id="<device-id>" ]
    Thing WMD myMotionSensor "Motion entry" @ "Entry" [ id="<device-id>" ]
    Thing WSC2 myPushButton "Pushbutton" @ "Living" [ id="<device-id>" ]
    Thing WSD mySmokeDetector "Smoke detector Livingroom" @ "Living" [ id="<device-id>" ]
}
```

## Items configuration

You can then configure your items in your *.items config files as usual, for example:

```java
Contact myWindowContact        "Kitchen"                <window>      {channel="livisismarthome:WDS:mybridge:myWindowContact:contact"}
Switch myWindowContactBattery  "Battery low"            <battery>     {channel="livisismarthome:WDS:mybridge:myWindowContact:batteryLow"}
Number myHeatingTemp           "Bath [%.1f °C]"         <temperature> {channel="livisismarthome:RST:mybridge:myHeating:currentTemperature"}
Number myHeatingModeTempTarget "Settemp bath [%.1f °C]" <temperature> {channel="livisismarthome:RST:mybridge:myHeating:targetTemperature"}
String myHeatingMode           "Mode bath [%s]"         <temperature> {channel="livisismarthome:RST:mybridge:myHeating:operationMode"}
Number myHeatingHumidity       "Bath [%.1f %%]"         <humidity>    {channel="livisismarthome:RST:mybridge:myHeating:humidity"}

```

## Sitemap configuration

Example:

```perl
sitemap default label="Home" {
    Frame {
        Text item=myHeatingTemp label="Temperature"
        Text item=myHeatingHumidity label="Humidity"
        Switch item=myHeatingMode label="Mode" mappings=[Manu="Manual", Auto="Auto"]
        Setpoint item=myHeatingModeTempTarget label="Target temperature" minValue=16 maxValue=25 step=1
    }
}
```

## Rules example for push-buttons

Push-buttons provide trigger channels, that can only be used in rules.
Here is an example rule:

```java
rule "Button triggered rule"
when
    Channel 'livisismarthome:WSC2:mybridge:myPushButton:button1' triggered PRESSED
then
    // do something...
    logInfo("testlogger", "Button 1 pressed")
end
```
