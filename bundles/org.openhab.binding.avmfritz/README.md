# AVM FRITZ! Binding

The binding integrates the AHA ( [AVM Home Automation](https://avm.de/ratgeber/smart-home/) ) system.  

## Supported Things

### FRITZ!Box

The well known FRITZ!Boxes are supported as bridge for accessing other AHA devices.
It is planned to support some channels directly at the bridge like call monitoring and others - but these things are under current development by now.
The Box has to run at least on firmware FRITZ!OS 6.00 and has to support the "Smart Home" service.

### FRITZ!DECT 200 / FRITZ!DECT 210

This switchable outlets [FRITZ!DECT 210](https://avm.de/produkte/fritzdect/fritzdect-210/) and [FRITZ!DECT 200](https://avm.de/produkte/fritzdect/fritzdect-200/) have to be connected to a FRITZ!Box by DECT protocol.
They support switching the outlet and reading the current power, current voltage, accumulated energy consumption and temperature.
**NOTE:** The `voltage` channel will be added to the thing during runtime - if the interface supports it (FRITZ!OS 7 or higher).

### FRITZ!DECT Repeater 100

This [DECT repeater](https://avm.de/produkte/fritzdect/fritzdect-repeater-100/) has to be connected to a FRITZ!Box by DECT protocol.
It only supports temperature readings.

### FRITZ!Powerline 546E

This [powerline adapter](https://avm.de/produkte/fritzpowerline/fritzpowerline-546e/) can be used via the bridge or in stand-alone mode.
It supports switching the outlet and reading the current power, current voltage and accumulated energy consumption.
This device does not contain a temperature sensor.
**NOTE:** The `voltage` channel will be added to the thing during runtime - if the interface supports it (FRITZ!OS 7 or higher).

### FRITZ!DECT 301 / FRITZ!DECT 300 / Comet DECT

These devices [FRITZ!DECT 301](https://avm.de/produkte/fritzdect/fritzdect-301/), FRITZ!DECT 300 and [Comet DECT](https://www.eurotronic.org/produkte/comet-dect.html) ( [EUROtronic Technology GmbH](https://www.eurotronic.org) ) are used to regulate radiators via DECT protocol.
The FRITZ!Box can handle up to twelve heating thermostats.
The binding provides channels for reading and setting the temperature.
Additionally you can check the eco temperature, the comfort temperature and the battery level of the device.
The FRITZ!Box has to run at least on firmware FRITZ!OS 6.35.
**NOTE:** The `battery_level` channel will be added to the thing during runtime - if the interface supports it (FRITZ!OS 7 or higher).

### DECT-ULE / HAN-FUN devices

The following sensors have been successfully tested using FRITZ!OS 7 for FRITZ!Box 7490 / 7590:

- [SmartHome Tür-/Fensterkontakt (optisch)](https://www.smarthome.de/geraete/eurotronic-smarthome-tuer-fensterkontakt-optisch) - an optical door/window contact
- SmartHome Tür-/Fensterkontakt (magnetisch) - a magnetic door/window contact
- [SmartHome Bewegungsmelder](https://www.smarthome.de/geraete/telekom-smarthome-bewegungsmelder-innen) - a motion sensor
- [SmartHome Rauchmelder](https://www.smarthome.de/geraete/smarthome-rauchmelder-weiss) - a smoke detector
- [SmartHome Wandtaster](https://www.smarthome.de/geraete/telekom-smarthome-wandtaster) - a switch with two buttons

The use of other Sensors should be possible, if these are compatible with DECT-ULE / HAN-FUN standards.

The FRITZ!Box has to run at least on firmware FRITZ!OS 7.

### FRITZ! groups 

The FRITZ!OS supports two different types of groups.
On the one hand there are groups for heating thermostats on the other hand there are groups for switchable outlets and power meters.
The first one provides the same channels like the [FRITZ!DECT 301 / FRITZ!DECT 300 / Comet DECT](https://www.openhab.org/addons/bindings/avmfritz/#fritz-dect-301-fritz-dect-300-comet-dect) devices.
The later one provides the same channels like the [FRITZ!DECT 200 / FRITZ!DECT 210](https://www.openhab.org/addons/bindings/avmfritz/#fritz-dect-200-fritz-dect-210) / [FRITZ!Powerline 546E](https://www.openhab.org/addons/bindings/avmfritz/#fritz-powerline-546e) devices.
The FRITZ!Box has to run at least on firmware FRITZ!OS 6.69.

## Discovery

The FRITZ!Box and the powerline adapter are discovered through UPnP in the local network.
When added as things, a username/password has eventually to be set depending on your Box/Powerline security configuration.
The credentials given in the settings must have HomeAuto permissions.
This implies to enable "login to the home network with user name and password" setting in the FRITZ!Box.
To do so

- Click "System" in the FRITZ!Box user interface.
- Click "FRITZ!Box Users" in the "System" menu.
- Click on the "Login to the Home Network" tab.
- Enable the option "Login with FRITZ!Box user name and password".
- Click "Apply" to save the settings.

Note: Now you can only log in to the FRITZ!Box with a user account, i.e. after entering a user name and password.

Auto-discovery is enabled by default.
To disable it, you can add the following line to `<openHAB-conf>/services/runtime.cfg`:

```
discovery.avmfritz:background=false
```

If correct credentials are set in the bridge configuration, connected AHA devices are discovered automatically (may last up to 3 minutes).

## Thing Configuration

### FRITZ!Box

- `ipAddress` (mandatory), default "fritz.box"
- `protocol` (optional, "http" or "https"), default "http"
- `port` (optional, 1 to 65535), no default (derived from protocol: 80 or 443)
- `password` (optional), no default (depends on FRITZ!Box security configuration)
- `user` (optional), no default (depends on FRITZ!Box security configuration)
- `pollingInterval` (optional, 5 to 60), default 15 (in seconds)
- `asyncTimeout` (optional, 1000 to 60000), default 10000 (in milliseconds)
- `syncTimeout` (optional, 500 to 15000), default 2000 (in milliseconds)

### FRITZ!Powerline 546E

- `ain` (optional, advanced), no default (AIN number of the device)
- `ipAddress` (mandatory), default "fritz.powerline"
- `protocol` (optional, "http" or "https"), default "http"
- `port` (optional, 1 to 65535), no default (derived from protocol: 80 or 443)
- `password` (optional), no default (depends on FRITZ!Powerline security configuration)
- `pollingInterval` (optional, 5 to 60), default 15 (in seconds)
- `asyncTimeout` (optional, 1000 to 60000), default 10000 (in milliseconds)
- `syncTimeout` (optional, 500 to 15000), default 2000 (in milliseconds)

If the FRITZ!Powerline 546E is added via auto-discovery it determines its own `ain`, otherwise you have to configure it manually.

### Things Connected To FRITZ!Box Or FRITZ!Powerline 546E

- `ain` (mandatory), no default (AIN number of the device)

### Finding The AIN ###

The AIN (actor identification number) can be found in the FRITZ!Box interface -> Home Network -> SmartHome. When opening the details view for a device with the edit button, the AIN is shown. Use the AIN without the blank.

## Supported Channels

| Channel Type ID | Item Type                | Description                                                                                                                                         | Available on thing                                                                                  |
|-----------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| mode            | String                   | States the mode of the device (MANUAL/AUTOMATIC/VACATION)                                                                                          | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!Powerline 546E, FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT    |
| locked          | Contact                  | Device is locked for switching over external sources (OPEN/CLOSE)                                                                                  | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!Powerline 546E, FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT    |
| device_locked   | Contact                  | Device is locked for switching manually (OPEN/CLOSE) - FRITZ!OS 6.90                                                                               | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!Powerline 546E, FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT    |
| apply_template  | String                   | Apply template for device(s) (channel's state options contains available templates, for an alternative way see the description below) - FRITZ!OS 7 | FRITZ!Box, FRITZ!Powerline 546E                                                                     |
| temperature     | Number:Temperature       | Current measured temperature                                                                                                                       | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!DECT Repeater 100, FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT |
| energy          | Number:Energy            | Accumulated energy consumption                                                                                                                     | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!Powerline 546E                                                |
| power           | Number:Power             | Current power consumption                                                                                                                          | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!Powerline 546E                                                |
| voltage         | Number:ElectricPotential | Current voltage - FRITZ!OS 7                                                                                                                       | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!Powerline 546E                                                |
| outlet          | Switch                   | Switchable outlet (ON/OFF)                                                                                                                         | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!Powerline 546E                                                |
| actual_temp     | Number:Temperature       | Current temperature of heating thermostat                                                                                                          | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| set_temp        | Number:Temperature       | Set Temperature of heating thermostat                                                                                                              | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| eco_temp        | Number:Temperature       | Eco Temperature of heating thermostat                                                                                                              | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| comfort_temp    | Number:Temperature       | Comfort Temperature of heating thermostat                                                                                                          | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| radiator_mode   | String                   | Mode of heating thermostat (ON/OFF/COMFORT/ECO/BOOST/WINDOW_OPEN)                                                                                  | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| next_change     | DateTime                 | Next change of the Set Temperature if scheduler is activated in the FRITZ!Box settings - FRITZ!OS 6.80                                             | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| next_temp       | Number:Temperature       | Next Set Temperature if scheduler is activated in the FRITZ!Box settings - FRITZ!OS 6.80                                                           | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| battery_level   | Number                   | Battery level (in %) - FRITZ!OS 7                                                                                                                  | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| battery_low     | Switch                   | Battery level low (ON/OFF) - FRITZ!OS 6.80                                                                                                         | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| contact_state   | Contact                  | Contact state information (OPEN/CLOSED).                                                                                                           | HAN-FUN contact (e.g. SmartHome Tür-/Fensterkontakt or SmartHome Bewegungsmelder)- FRITZ!OS 7       |
| last_change     | DateTime                 | States the last time the button was pressed.                                                                                                       | HAN-FUN switch (e.g. SmartHome Wandtaster) - FRITZ!OS 7                                             |

### Triggers

| Channel Type ID | Item Type | Description                                            | Available on thing                                      |
|-----------------|-----------|--------------------------------------------------------|---------------------------------------------------------|
| press           | Trigger   | Dispatches a `PRESSED` event when a button is pressed. | HAN-FUN switch (e.g. SmartHome Wandtaster) - FRITZ!OS 7 |

The trigger channel `press` is of type `system.rawbutton` to allow the usage of the `rawbutton-toggle-switch` profile.

### FRITZ! Smart Home Templates

With the new [templates feature](https://en.avm.de/guide/smart-home/meet-the-smart-home-templates-from-fritz/) in FRITZ!OS 7, you can now save the settings of your Smart Home devices and groups as a template for certain occasions e.g. holidays or vacation.
Unfortunately it is not that simple to find out the unique identifier (AIN) for a template needed for sending it as command to the `apply_template` channel.
Here is a work-around:
To retrieve the list of AINs assigned by FRITZ! for your templates, go to the FRITZ!Box' Support page at http://fritz.box/html/support.html within your local network and login.
Then in the section "Support Data" ("Support-Daten") press the button "Create Support Data" ("Support-Daten erstellen") and save the generated text file.
Open the file in a text editor and search for the term "avm_home_device_type_template".
You will find entries like the attached one.
The `identifyer 'tmpFC0F2C-3960B7EE6'` contains the templates AINs you need for using them in rules.

```
Name 'Demo Template', identifyer 'tmpFC0F2C-3960B7EE6', firmware version '0.1' 
    [aktive] ID 60013, emc 0x0, model 0x0, grouphash=0x0, devicetype 'avm_home_device_type_template', functionbitmask 0x4000, sortid 0, batt perc 255 low 255, pollinterval 0, polltimeout 0, validchangetime: 0
    --------------------
```

templates.rules

```java
rule "Apply template"
when
    ...
then
    ApplyTemplate.sendCommand("tmpFC0F2C-3960B7EE6")
end
```

## Full Example

demo.things:

```java
Bridge avmfritz:fritzbox:1 "FRITZ!Box" [ ipAddress="192.168.x.x", password="xxx", user="xxx" ] {
    Thing FRITZ_DECT_200 xxxxxxxxxxxx "FRITZ!DECT 200 #1" [ ain="xxxxxxxxxxxx" ]
    Thing FRITZ_Powerline_546E yy_yy_yy_yy_yy_yy "FRITZ!Powerline 546E #2" [ ain="yy:yy:yy:yy:yy:yy" ]
    Thing Comet_DECT aaaaaabbbbbb "Comet DECT #3" [ ain="aaaaaabbbbbb" ]
    Thing HAN_FUN_CONTACT zzzzzzzzzzzz_1 "HAN-FUN Contact #4" [ ain="zzzzzzzzzzzz-1" ]
    Thing HAN_FUN_SWITCH zzzzzzzzzzzz_2 "HAN-FUN Switch #5" [ ain=zzzzzzzzzzzz-2" ]
    Thing FRITZ_DECT_Repeater_100 rrrrrrrrrrrr "DECT Repeater 100 #6" [ ain="rrrrrrrrrrrr" ]
    Thing FRITZ_GROUP_HEATING AA_AA_AA_900 "Heating group" [ ain="AA:AA:AA-900" ]
    Thing FRITZ_GROUP_SWITCH BB_BB_BB_900 "Switch group" [ ain="BB:BB:BB-900" ]
}
```

demo.items:

```java
String ApplyTemplate "Apply template" { channel="avmfritz:fritzbox:1:apply_template" }

Switch Outlet1 "Switchable outlet" { channel="avmfritz:FRITZ_DECT_200:1:xxxxxxxxxxxx:outlet" }
Number:Temperature Temperature1 "Current measured temperature [%.1f %unit%]" { channel="avmfritz:FRITZ_DECT_200:1:xxxxxxxxxxxx:temperature" }
Number:Energy Energy1 "Accumulated energy consumption [%.3f kWh]" { channel="avmfritz:FRITZ_DECT_200:1:xxxxxxxxxxxx:energy" }
Number:Power Power1 "Current power consumption [%.2f %unit%]" { channel="avmfritz:FRITZ_DECT_200:1:xxxxxxxxxxxx:power" }
Number:ElectricPotential Voltage1 "Current voltage [%.1f %unit%]" { channel="avmfritz:FRITZ_DECT_200:1:xxxxxxxxxxxx:voltage" }

Switch Outlet2 "Switchable outlet" { channel="avmfritz:FRITZ_Powerline_546E:1:yy_yy_yy_yy_yy_yy:outlet" }

Number:Temperature COMETDECTTemperature "Current measured temperature [%.1f %unit%]" { channel="avmfritz:Comet_DECT:1:aaaaaabbbbbb:actual_temp" }
Number:Temperature COMETDECTSetTemperature "Thermostat temperature set point [%.1f %unit%]" { channel="avmfritz:Comet_DECT:1:aaaaaabbbbbb:set_temp" }
String COMETDECTRadiatorMode "Radiator mode [%s]" { channel="avmfritz:Comet_DECT:1:aaaaaabbbbbb:radiator_mode" }
Number COMETDECTBattery "Battery level" { channel="avmfritz:Comet_DECT:1:aaaaaabbbbbb:battery_level" }
Switch COMETDECTBatteryLow "Battery low" { channel="avmfritz:Comet_DECT:1:aaaaaabbbbbb:battery_low" }

Contact HANFUNContactState "Status [%s]" { channel="avmfritz:HAN_FUN_CONTACT:1:zzzzzzzzzzzz_1:contact_state" }

DateTime HANFUNSwitchLastChanged "Last change" { channel="avmfritz:HAN_FUN_SWITCH:1:zzzzzzzzzzzz_2:last_change" }

Number:Temperature Temperature1 "Current measured temperature [%.1f %unit%]" { channel="avmfritz:FRITZ_DECT_Repeater_100:1:rrrrrrrrrrrr:temperature" }

Number:Temperature FRITZ_GROUP_HEATINGSetTemperature "Group temperature set point [%.1f %unit%]" { channel="avmfritz:FRITZ_GROUP_HEATING:1:AA_AA_AA_900:set_temp" }

Switch Outlet3 "Group switch" { channel="avmfritz:FRITZ_GROUP_SWITCH:1:BB_BB_BB_900:outlet" }
```

demo.sitemap:

```java
sitemap demo label="Main Menu" {

    Frame label="FRITZ!Box" {
        Selection item=ApplyTemplate
    }

    Frame label="FRITZ!DECT 200 switchable outlet" {
        Switch item=Outlet1 icon="poweroutlet"
        Text item=Temperature1 icon="temperature"
        Text item=Energy1 icon="energy"
        Text item=Power1 icon="energy"
        Text item=Voltage1 icon="energy"
    }

    Frame label="FRITZ!Powerline 546E switchable outlet" {
        Switch item=Outlet2 icon="poweroutlet"
    }

    Frame label="Comet DECT heating thermostat" {
        Text item=COMETDECTTemperature icon="temperature"
        Setpoint item=COMETDECTSetTemperature minValue=8.0 maxValue=28.0 step=0.5 icon="temperature"
        Selection item=COMETDECTRadiatorMode mappings=["ON"="ON", "OFF"="OFF", "COMFORT"="COMFORT", "ECO"="ECO", "BOOST"="BOOST"] icon="heating"
        Text item=COMETDECTBattery icon="battery"
        Switch item=COMETDECTBatteryLow icon="lowbattery"
    }
 
    Frame label="HAN-FUN Contact" {
        Text item=HANFUNContactState
    }

    Frame label="HAN-FUN Switch" {
        Text item=HANFUNSwitchLastChanged
    }
}
```

demo.rules:

```java
rule "HAN-FUN Button pressed"
when
    Channel "avmfritz:HAN_FUN_SWITCH:1:zzzzzzzzzzzz_2:press" triggered
then
    logInfo("demo", "Button pressed")
end
```
