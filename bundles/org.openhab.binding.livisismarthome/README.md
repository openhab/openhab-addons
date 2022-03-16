# LIVISI SmartHome Binding

The binding integrates the [LIVISI (RWE/innogy) SmartHome](https://www.livisi.de) system into openHAB.
The binding is the successor of the innogy SmartHome openHAB binding, which is communicating with the LIVISI cloud-servers over the internet.

This binding uses a DIRECT communication with LIVISI SmartHome Controllers (SHC). It does NOT need to communicate with the LIVISI cloud-services and does NOT require an internet connection.

On your SHC you need a minimum Software Version of 1.2.XX.XXX with activated "Local SmartHome". 


## Supported things

### Bridge

The LIVISI SmartHome Controller (SHC) is the bridge, that provides the central communication with the devices.
Without the SHC, you cannot communicate with the devices.

### Devices

The following table shows all supported and tested devices and their channels.
The channels are described in detail in the next chapter.

| Device | Description                                                              | Supported channels                                                                                                        |
|--------|--------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| SHC    | SmartHome Controller (Bridge)                                            | status, cpu, disk, memory                                                                                                 |
| BRC8   | Basic Remote Controller                                                  | button1 ... button8, button1_count ... button8_count, battery_low                                                         |
| ISC2   | In Wall Smart Controller                                                 | button1, button2, button1_count, button2_count, battery_low                                                               |
| ISD2   | In Wall Smart Dimmer                                                     | button1, button2, button1_count, button2_count, dimmer                                                                    |
| ISR2   | In Wall Smart Rollershutter                                              | button1, button2, button1_count, button2_count, rollershutter                                                             |
| ISS2   | In Wall Smart Switch                                                     | button1, button2, button1_count, button2_count, switch                                                                    |
| PSD    | Pluggable Smart Dimmer                                                   | dimmer                                                                                                                    |
| PSS    | Pluggable Smart Switch, indoor                                           | switch                                                                                                                    |
| PSSO   | Pluggable Smart Switch, outdoor                                          | switch                                                                                                                    |
| BT-PSS | Bluetooth Pluggable Smart Switch, indoor                                 | switch                                                                                                                    |
| RST    | Radiator Mounted Smart Thermostat                                        | set_temperature, temperature, frost_warning, humidity, mold_warning, operation_mode, window_reduction_active, battery_low |
| RST2   | Radiator Mounted Smart Thermostat (newer two battery version since 2018) | set_temperature, temperature, frost_warning, humidity, mold_warning, operation_mode, window_reduction_active, battery_low |
|        | VariableActuator                                                         | switch                                                                                                                    |
| WDS    | Window Door Sensor                                                       | contact, battery_low                                                                                                      |
| WMD    | Wall Mounted Motion Detector, indoor                                     | motion_count, luminance, battery_low                                                                                      |
| WMDO   | Wall Mounted Motion Detector, outdoor                                    | motion_count, luminance, battery_low                                                                                      |
| WRT    | Wall Mounted Room Thermostat                                             | set_temperature, temperature, frost_warning, humidity, mold_warning, operation_mode, window_reduction_active, battery_low |
| WSC2   | Wall Mounted Smart Controller                                            | button1, button2, button1_count, button2_count, battery_low                                                               |
| WSD    | Wall Mounted Smoke Detector, old version                                 | smoke, alarm, battery_low                                                                                                 |
| WSD2   | Wall Mounted Smoke Detector, new version                                 | smoke, alarm, battery_low                                                                                                 |

Powermeter devices

| Device          | Description                                                    | Supported channels                                                                                                                                                                                         |
|-----------------|----------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AnalogMeter     | The Analog Meter from the LIVISI EnergyControl product         | energy_consumption_month_kwh, absolute_energy_consumption, energy_consumption_month_euro, energy_consumption_day_euro, energy_consumption_day_kwh                                                          |
| GenerationMeter | The Generation Meter from the LIVISI PowerControlSolar product | energy_generation_month_kwh, total_energy_generation, energy_generation_month_euro, energy_generation_day_euro, energy_generation_day_kwh, power_generation_watt                                           |
| SmartMeter      | The Smart Meter from the LIVISI PowerControl product.          | energy_consumption_month_kwh, absolute_energy_consumption, energy_consumption_month_euro, energy_consumption_day_euro, energy_consumption_day_kwh, power_consumption_watt                                  |
| Two-Way-Meter   | The Two-Way-Meter from the LIVISI PowerControlSolar product    | energy_month_kwh, total_energy, energy_month_euro, energy_day_euro, energy_day_kwh, energy_feed_month_kwh, total_energy_fed, energy_feed_month_euro, energy_feed_day_euro, energy_feed_day_kwh, power_watt |

## Discovery

The bridge (SHC) can not be discovered automatically. It must be added manually (see below under "Configuration").

After the bridge is added, devices are discovered automatically.
As there is no background discovery implemented at the moment, you have to start the discovery manually.
However, only devices will appear that are added in the LIVISI SmartHome app before, because the LIVISI Binding does not support the coupling of devices to the bridge.

## Channels

| Channel Type ID         | Item Type     | Description                                                               | Available on thing                                          |
|-------------------------|---------------|---------------------------------------------------------------------------|-------------------------------------------------------------|
| alarm                   | Switch        | Switches the alarm (ON/OFF)                                               | WSD, WSD2                                                   |
| battery_low             | Switch        | Indicates, if the battery is low (ON/OFF)                                 | BRC8, ISC2, RST, RST2, WDS, WMD, WMD0, WRT, WSC2, WSD, WSD2 |
| contact                 | Contact       | Indicates the contact state (OPEN/CLOSED)                                 | WDS                                                         |
| cpu                     | Number        | CPU-Usage of the SHC in percent                                           | SHC (bridge)                                                |
| dimmer                  | Dimmer        | Allows to dimm a light device                                             | ISD2, PSD                                                   |
| disk                    | Number        | Disk-Usage of the SHC in percent                                          | SHC (bridge)                                                |
| frost_warning           | Switch        | active, if the measured temperature is too low (ON/OFF)                   | RST, RST2, WRT                                              |
| humidity                | Number        | Relative humidity in percent                                              | RST, RST2, WRT                                              |
| button1                 | -             | Trigger channel for rules, fires with each push                           | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2                          |
| button2                 | -             | Trigger channel for rules, fires with each push                           | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2                          |
| button3                 | -             | Trigger channel for rules, fires with each push                           | BRC8                                                        |
| button4                 | -             | Trigger channel for rules, fires with each push                           | BRC8                                                        |
| button5                 | -             | Trigger channel for rules, fires with each push                           | BRC8                                                        |
| button6                 | -             | Trigger channel for rules, fires with each push                           | BRC8                                                        |
| button7                 | -             | Trigger channel for rules, fires with each push                           | BRC8                                                        |
| button8                 | -             | Trigger channel for rules, fires with each push                           | BRC8                                                        |
| button1_count           | Number        | Number of button pushes for button 1, increased with each push            | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2                          |
| button2_count           | Number        | Number of button pushes for button 2, increased with each push            | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2                          |
| button3_count           | Number        | Number of button pushes for button 3, increased with each push            | BRC8                                                        |
| button4_count           | Number        | Number of button pushes for button 4, increased with each push            | BRC8                                                        |
| button5_count           | Number        | Number of button pushes for button 5, increased with each push            | BRC8                                                        |
| button6_count           | Number        | Number of button pushes for button 6, increased with each push            | BRC8                                                        |
| button7_count           | Number        | Number of button pushes for button 7, increased with each push            | BRC8                                                        |
| button8_count           | Number        | Number of button pushes for button 8, increased with each push            | BRC8                                                        |
| luminance               | Number        | Indicates the measured luminance in percent                               | WMD, WMD0                                                   |
| memory                  | Number        | Memory-Usage of the SHC in percent                                        | SHC (bridge)                                                |
| mold_warning            | Switch        | Active, if the measured humidity is too low (ON/OFF)                      | RST, RST2, WRT                                              |
| motion_count            | Number        | Number of detected motions, increases with each detected motion           | WMD, WMDO                                                   |
| operation_mode          | String        | The mode of a thermostat (auto/manual)                                    | RST, RST2, WRT                                              |
| rollershutter           | Rollershutter | Controls a roller shutter                                                 | ISR2                                                        |
| set_temperature         | Number        | Sets the target temperature in °C (min 6 °C, max 30 °C)                   | RST, RST2, WRT                                              |
| smoke                   | Switch        | Indicates, if smoke was detected (ON/OFF)                                 | WSD, WSD2                                                   |
| status                  | String        | Status of the SHC (ACTIVE/NORMAL, INITIALIZING/REBOOTING or SHUTTINGDOWN) | SHC (bridge)                                                |
| switch                  | Switch        | A switch to turn the device or variable on/off (ON/OFF)                   | ISS2, PSS, PSSO, VariableActuator                           |
| temperature             | Number        | Holds the actual temperature in °C                                        | RST, RST2, WRT                                              |
| window_reduction_active | Switch        | Indicates if a linked window is open and temperature reduced (ON/OFF)     | RST, RST2, WRT                                              |

The `rollershutter` channel has a `boolean` parameter `invert`.
It is `false` by default.
This means `100` on LIVISI is `UP` and `0` is `DOWN`.
When `invert` is `true` than `0` on LIVISI is `UP` and `100` is `DOWN`.


## Triggers

| Trigger Type  | Description                                                             | Available on thing                  |
|---------------|-------------------------------------------------------------------------|-------------------------------------|
| SHORT_PRESSED | Fired when you press a button short (not supported by SHC 1 / classic)  | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2  |
| LONG_PRESSED  | Fired when you press a button longer (not supported by SHC 1 / classic) | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2  |
| PRESSED       | Fired when you press a button (short or long)                           | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2  |


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

```
Bridge livisismarthome:bridge:<bridge-id> "Livisi: SmartHome Controller (SHC)" [ host="192.168.0.99", password="SomethingSecret", websocketidletimeout=900]
```

** *Security warning!**
The communication between the binding and the SHC is not encrypted and can be traced.
So be careful and secure your local network from unauthorized access.

All other LIVISI devices can be added using the following syntax:

```
Thing WDS <thing-id> "<thing-name>" @ "<room-name>" [ id="<the-device-id>" ]
```

The device ID (e.g. e9a74941a3807b57332214f346fb1129) can be found in the UI inbox, as you find it below all things there in the form `livisismarthome:<device-type>:<bridge-id>:<the-device-id>` (example: `livisismarthome:WSC2:SMARTHOME01:e9a74941a3807b57332214f346fb1129`).

However, a full example .things configuration look like this:

```
Bridge livisismarthome:bridge:mybride "LIVISI SmartHome Controller" {
    Thing ISD2 myDimmer "Dimmer Kitchen" @ "Kitchen" [ id="<device-id>" ]
    Thing ISS2 myLightSwitch "Light Livingroom" @ "Livingroom" [ id="<device-id>" ]
    Thing PSS myTVSwitch "TV" @ "Livingroom" [ id="<device-id>" ]
    Thing RST myHeating "Thermostat Livingroom" @ "Livingroom" [ id="<device-id>" ]
    Thing ISR2 myRollerShutter1 "RollerShutter" @ "Livingroom" [ id="<device-id>" ]
    Thing ISR2 myRollerShutter2 "RollerShutter (inverted)" @ "Livingroom" [ id="<device-id>" ] {Type RollerShutterActuator : rollershutter  [invert=true]}
    Thing VariableActuator myLivisiVariable "My Variable" [ id="<device-id>" ]
    Thing WDS myWindowContact "Window Kitchen" @ "Kitchen" [ id="<device-id>" ]
    Thing WMD myMotionSensor "Motion entry" @ "Entry" [ id="<device-id>" ]
    Thing WSC2 myPushButton "Pushbutton" @ "Living" [ id="<device-id>" ]
    Thing WSD mySmokeDetector "Smoke detector Livingroom" @ "Living" [ id="<device-id>" ]
}
```

## Items configuration

You can then configure your items in your *.items config files as usual, for example:

```
Contact myWindowContact        "Kitchen"                <window>      {channel="livisismarthome:WDS:mybridge:myWindowContact:contact"}
Switch myWindowContactBattery  "Battery low"            <battery>     {channel="livisismarthome:WDS:mybridge:myWindowContact:battery_low"}
Number myHeatingTemp           "Bath [%.1f °C]"         <temperature> {channel="livisismarthome:RST:mybridge:myHeating:temperature"}
Number myHeatingModeTempTarget "Settemp bath [%.1f °C]" <temperature> {channel="livisismarthome:RST:mybridge:myHeating:set_temperature"}
String myHeatingMode           "Mode bath [%s]"         <temperature> {channel="livisismarthome:RST:mybridge:myHeating:operation_mode"}
Number myHeatingHumidity       "Bath [%.1f %%]"         <humidity>    {channel="livisismarthome:RST:mybridge:myHeating:humidity"}

```

## Sitemap configuration

The site configuration works a usual. One special example

```
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

```
rule "Button triggered rule"
when
	Channel 'livisismarthome:WSC2:mybridge:myPushButton:button1' triggered PRESSED
then
    // do something...
	logInfo("testlogger", "Button 1 pressed")
end
```
