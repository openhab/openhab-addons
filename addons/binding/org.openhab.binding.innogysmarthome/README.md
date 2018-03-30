# innogy SmartHome Binding

The binding integrates the [innogy SmartHome](http://innogy.com/smarthome) system into openHAB.
It uses the official API as provided by innogy as cloud service.
As all status updates and commands have to go through the API, a permanent internet connection is required.
Currently there is no API for a direct communication with the innogy SmartHome Controller (SHC).

## Supported things

### Bridge

The innogy SmartHome Controller (SHC) is the bridge, that provides the central communication with the devices.
Without the SHC, you cannot communicate with the devices.

### Devices

The following table shows all supported and tested devices and their channels.
The channels are described in detail in the next chapter.

| Device | Description | Supported channels |
| ------ | ----------- | ------------------ |
| BRC8 | Basic Remote Controller | button1 ... button8, button1_count ... button8_count, battery_low |
| ISC2 | In Wall Smart Controller | button1, button2, button1_count, button2_count |
| ISD2 | In Wall Smart Dimmer | button1, button2, button1_count, button2_count, dimmer |
| ISR2 | In Wall Smart Rollershutter | button1, button2, button1_count, button2_count, rollershutter |
| ISS2 | In Wall Smart Switch | button1, button2, button1_count, button2_count, switch |
| PSD | Pluggable Smart Dimmer | dimmer |
| PSS | Pluggable Smart Switch, indoor | switch |
| PSSO | Pluggable Smart Switch, outdoor | switch |
| RST | Radiator Mounted Smart Thermostat | set_temperature, temperature, frost_warning, humidity, mold_warning, operation_mode, window_reduction_active, battery_low |
| RST2 | Radiator Mounted Smart Thermostat (newer two battery version since 2018) | set_temperature, temperature, frost_warning, humidity, mold_warning, operation_mode, window_reduction_active, battery_low |
| | VariableActuator | switch |
| WDS | Window Door Sensor | contact, battery_low |
| WMD| Wall Mounted Motion Detector, indoor | motion_count, luminance, battery_low |
| WMDO | Wall Mounted Motion Detector, outdoor | motion_count, luminance, battery_low |
| WRT | Wall Mounted Room Thermostat | set_temperature, temperature, humidity, battery_low |
| WSC2 | Wall Mounted Smart Controller | button1, button2, button1_count, button2_count, battery_low |
| WSD | Wall Mounted Smoke Detector, old version | smoke, alarm, battery_low |
| WSD2 | Wall Mounted Smoke Detector, new version | smoke, alarm, battery_low |

Powermeter devices

| Device | Description | Supported channels |
| ------ | ----------- | ------------------ |
| AnalogMeter | The Analog Meter from the innogy EnergyControl product | energy_consumption_month_kwh, absolute_energy_consumption, energy_consumption_month_euro, energy_consumption_day_euro, energy_consumption_day_kwh |
| GenerationMeter | The Generation Meter from the innogy PowerControlSolar product | energy_generation_month_kwh, total_energy_generation, energy_generation_month_euro, energy_generation_day_euro, energy_generation_day_kwh, power_generation_watt |
| SmartMeter | The Smart Meter from the innogy PowerControl product. | energy_consumption_month_kwh, absolute_energy_consumption, energy_consumption_month_euro, energy_consumption_day_euro, energy_consumption_day_kwh, power_consumption_watt |
| Two-Way-Meter | The Two-Way-Meter from the innogy PowerControlSolar product | energy_month_kwh, total_energy, energy_month_euro, energy_day_euro, energy_day_kwh, energy_feed_month_kwh, total_energy_fed, energy_feed_month_euro, energy_feed_day_euro, energy_feed_day_kwh, power_watt |
  

## Discovery

If the bridge (SHC) is located in the same LAN as the openHAB server, the bridge should be discovered automatically by mDNS.
However, this can sometimes take a couple of minutes.
If the bridge is not found, it can be added manually (see below under "Configuration").

After the bridge is added, devices are discovered automatically.
As there is no background discovery implemented at the moment, you have to start the discovery manually.
However, only devices will appear that are added in the innogy SmartHome app before, as the innogy Binding does not support the coupling of devices to the bridge.

## Channels

| Channel Type ID | Item Type    | Description  | Available on thing |
| --------------- | ------------ | ------------ | ------------------ |
| alarm | Switch | Switches the alarm (ON/OFF) | WSD, WSD2 | 
| battery_low | Switch | Indicates, if the battery is low (ON/OFF) | BRC8, RST, WDS, WMD, WMD0, WRT, WSC2, WSD, WSD2 |
| contact | Contact | Indicates the contact state (OPEN/CLOSED) | WDS |
| dimmer | Dimmer | Allows to dimm a light device | ISD2, PSD |
| frost_warning | Switch | active, if the measured temperature is too low (ON/OFF) | RST |
| humidity | Number | Relative humidity in percent | RST, WRT |
| button1 | - | trigger channel for rules, fires with each push | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2 |
| button2 | - | trigger channel for rules, fires with each push | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2 |
| button3 | - | trigger channel for rules, fires with each push | BRC8 |
| button4 | - | trigger channel for rules, fires with each push | BRC8 |
| button5 | - | trigger channel for rules, fires with each push | BRC8 |
| button6 | - | trigger channel for rules, fires with each push | BRC8 |
| button7 | - | trigger channel for rules, fires with each push | BRC8 |
| button8 | - | trigger channel for rules, fires with each push | BRC8 |
| button1_count | Number | number of button pushes for button 1, increased with each push | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2 |
| button2_count | Number | number of button pushes for button 2, increased with each push | BRC8, ISC2, ISD2, ISR2, ISS2, WSC2 |
| button3_count | Number | number of button pushes for button 3, increased with each push | BRC8 |
| button4_count | Number | number of button pushes for button 4, increased with each push | BRC8 |
| button5_count | Number | number of button pushes for button 5, increased with each push | BRC8 |
| button6_count | Number | number of button pushes for button 6, increased with each push | BRC8 |
| button7_count | Number | number of button pushes for button 7, increased with each push | BRC8 |
| button8_count | Number | number of button pushes for button 8, increased with each push | BRC8 |
| luminance | Number | Indicates the measured luminance in percent | WMD, WMD0 |
| mold_warning | Switch | active, if the measured humidity is too low (ON/OFF) | RST | 
| motion_count | Number | Number of detected motions, increases with each detected motion | WMD, WMDO |
| operation_mode | String | the mode of a thermostat (auto/manual) | RST | 
| rollershutter | Rollershutter | Controls a roller shutter | ISR2 |
| set_temperature | Number | Sets the target temperature in °C | RST, WRT |
| smoke | Switch | Indicates, if smoke was detected (ON/OFF) | WSD, WSD2 |
| switch | Switch | A switch to turn the device or variable on/off (ON/OFF) | ISS2, PSS, PSSO, VariableActuator |
| temperature | Number | Holds the actual temperature in °C | RST, WRT |
| window_reduction_active | Switch | indicates if a linked window is open and temperature reduced (ON/OFF)  | RST |

## Thing configuration

### Configuring the SmartHome Controller (SHC)

The SmartHome Controller (SHC) can be configured in the Paper UI as follows:

After the "innogy SmartHome Controller" is added via the Inbox, edit the controller and add the "Authorization code" by following the hints in the description. 
Save your changes. 
The SHC should now login and go online. 
Be sure it is connected to the Internet.

### Obtaining the authorization code and tokens

Authorization is done as oauth2 workflow with the innogy API.

To receive the auth-code, go to one of the following URLs depending on your brand and login with your credentials (you can find this link also in the SHC thing in Paper UI, if you edit it):

* [innogy SmartHome authorization page](https://api.services-smarthome.de/AUTH/authorize?response_type=code&client_id=24635748&redirect_uri=https%3A%2F%2Fwww.openhab.org%2Foauth%2Finnogy%2Finnogy-smarthome.html&scope&lang=de-DE)
* [SmartHome Austria authorization page](https://api.services-smarthome.de/AUTH/authorize?response_type=code&client_id=24635749&redirect_uri=https%3A%2F%2Fwww.openhab.org%2Foauth%2Finnogy%2Fsmarthome-austria.html&scope&lang=de-DE)
* [Start SmartHome authorization page](https://api.services-smarthome.de/AUTH/authorize?response_type=code&client_id=24635750&redirect_uri=https%3A%2F%2Fwww.openhab.org%2Foauth%2Finnogy%2Fstart-smarthome.html&scope&lang=de-DE)

You will be redirected to openhab.org and the auth-code will be displayed.
Copy and paste it into your SHC configuration and you are done.

The binding then requests the access and refresh tokens and saves them in the SHC configuration.
The auth-code can only be used once and therefore is dropped.
The access token is then used to login at the innogy API, but is valid only for a couple of hours.
The binding automatically requests a new access token as needed by using the refresh token.
So the refresh token is the relevant credential.
**Never give it to anybody!**

### Discovering devices

All devices bound to the bridge are found by the discovery service once the SHC is online.
As device discovery is not implemented as a background service, you should start it manually in the Inbox to find all devices.
Now you can add all devices from your Inbox as things.

### Manual configuration

As an alternative to the automatic discovery process and graphical configuration using Paper UI, innogy things can be configured manually.
The innogy SmartHome Controller (SHC) can be configured using the following syntax:

```
Bridge innogysmarthome:bridge:<bridge-id> [ refreshtoken="<refresh-token>" ]
```

The easiest way is to retrieve the refresh-token using the Paper UI as described above.
But you can do it manually by:

1. Changing the log level to TRACE, as the refresh-token is not written into the logs in lower log levels*
2. Retrieving the auth-code (see description above)
3. Saving it once in the Bridge configuration like shown below
4. Fishing the refresh-code from the openhab.log file.

```
Bridge innogysmarthome:bridge:<bridge-id> [ authcode="<authcode>" ]
```

** *Security warning!**
As the refresh-token is THE one and only credential one needs to access the innogy webservice with all device data, you have to make sure it is never given to another person.
Thus it is recommended to remove the line from the openhab.log and/or make sure, the logfile is definitely never accessible by others!

All other innogy devices can be added using the following syntax:

```
Thing WDS <thing-id> "<thing-name>" @ "<room-name>" [ id="<the-device-id>" ]
```

The device ID (e.g. e9a74941a3807b57332214f346fb1129) can be found in the Paper UI inbox, as you find it below all things there in the form `innogysmarthome:<device-type>:<bridge-id>:<the-device-id>` (example: `innogysmarthome:WSC2:SMARTHOME01:e9a74941a3807b57332214f346fb1129`).

However, a full example .things configuration look like this:

```
Bridge innogysmarthome:bridge:mybride "innogy SmartHome Controller" [ refreshtoken="<insert-your-refresh-token-here>" ] {
    Thing ISD2 myDimmer "Dimmer Kitchen" @ "Kitchen" [ id="<device-id>" ]
    Thing ISS2 myLightSwitch "Light Livingroom" @ "Livingroom" [ id="<device-id>" ]
    Thing PSS myTVSwitch "TV" @ "Livingroom" [ id="<device-id>" ]
    Thing RST myHeating "Thermostat Livingroom" @ "Livingroom" [ id="<device-id>" ]
    Thing VariableActuator myInnogyVariable "My Variable" [ id="<device-id>" ]
    Thing WDS myWindowContact "Window Kitchen" @ "Kitchen" [ id="<device-id>" ]
    Thing WMD myMotionSensor "Motion entry" @ "Entry" [ id="<device-id>" ]
    Thing WSC2 myPushButton "Pushbutton" @ "Living" [ id="<device-id>" ]
    Thing WSD mySmokeDetector "Smoke detector Livingroom" @ "Living" [ id="<device-id>" ]
}
```

You can then configure your items in your *.items config files as usual, for example:

```
Contact myWindowContact       "Kitchen"     <window>  {channel="innogysmarthome:WDS:mybridge:myWindowContact:contact"}
Switch myWindowContactBattery "Battery low" <battery> {channel="innogysmarthome:WDS:mybridge:myWindowContact:battery_low"}
```




