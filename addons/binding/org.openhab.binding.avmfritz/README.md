# AVM FRITZ! Binding

The binding integrates the AHA ( [AVM Home Automation](http://avm.de/ratgeber/smart-home/) ) system.  


## Supported Things

### FRITZ!Box

The well known FRITZ!Boxes are supported as bridge for accessing other AHA devices.
It is planned to support some channels directly at the bridge like call monitoring and others - but these things are under current development by now.
The Box has to run at least on firmware FRITZ!OS 6.00 and has to support the "Smart Home" service.

### FRITZ!DECT 200 / FRITZ!DECT 210

This switchable outlets [FRITZ!DECT 210](https://avm.de/produkte/fritzdect/fritzdect-210/) and [FRITZ!DECT 200](https://avm.de/produkte/fritzdect/fritzdect-200/) have to be connected to a FRITZ!Box by DECT protocol.
They support switching the outlet, current power and accumulated energy consumption and temperature readings.

### FRITZ!DECT Repeater 100

This [DECT repeater](https://avm.de/produkte/fritzdect/fritzdect-repeater-100/) has to be connected to a FRITZ!Box by DECT protocol.
It only supports temperature readings.

### FRITZ!Powerline 546E

This [powerline adapter](http://avm.de/produkte/fritzpowerline/fritzpowerline-546e/) can be used via the bridge or in stand-alone mode.
It supports switching the outlet and current power and energy consumption readings.
This device does not contain a temperature sensor.

### FRITZ!DECT 301 / FRITZ!DECT 300 / Comet DECT

These devices [FRITZ!DECT 301](https://avm.de/produkte/fritzdect/fritzdect-301/), FRITZ!DECT 300 and [Comet DECT](https://www.eurotronic.org/produkte/comet-dect.html) ( [EUROtronic Technology GmbH](https://www.eurotronic.org) ) are used to regulate radiators via DECT protocol.
The FRITZ!Box can handle up to twelve heating thermostats.
The binding provides channels for reading and setting the temperature.
Additionally you can check the eco temperature, the comfort temperature and the battery level of the device.
The FRITZ!Box has to run at least on firmware FRITZ!OS 6.35.

## Discovery

The FRITZ!Box and the powerline adapter are discovered through UPNP in the local network.
When added as things, a username/password has eventually to be set depending on your Box/Powerline security configuration.
The credentials given in the settings must have HomeAuto permissions.

If correct credentials are set in the bridge configuration, connected AHA devices are discovered automatically (may last up to 3 minutes).


## Thing Configuration

### FRITZ!Box

*   ipAddress (mandatory), default "fritz.box"
*   protocol (optional, http or https), default "http"
*   port (optional, 0 to 65335), no default (derived from protocol: 80 or 443)
*   password (optional), no default (depends on FRITZ!Box security configuration)
*   user (optional), no default (depends on FRITZ!Box security configuration)
*   pollingInterval (optional, 5 to 60), default 15 (in seconds)
*   asyncTimeout (optional, 1000 to 60000), default 10000 (in millis)
*   syncTimeout (optional, 500 to 15000), default 2000 (in millis)

### FRITZ!Powerline 546E

*   ipAddress (mandatory), default "fritz.powerline"
*   protocol (optional, http or https), default "http"
*   port (optional, 0 to 65335), no default (derived from protocol: 80 or 443)
*   password (optional), no default (depends on FRITZ!Powerline security configuration)
*   pollingInterval (optional, 5 to 60), default 15 (in seconds)
*   asyncTimeout (optional, 1000 to 60000), default 10000 (in millis)
*   syncTimeout (optional, 500 to 15000), default 2000 (in millis)

### AHA things connected to FRITZ!Box bridge

*   AIN (mandatory), no default (AIN number of device)


## Supported Channels

| Channel Type ID | Item Type | Description                                                                                            | Available on thing                                                                                  |
|-----------------|-----------|--------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| mode            | String    | States the mode of the device (MANUAL/AUTOMATIC)                                                       | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!Powerline 546E, FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT    |
| locked          | Contact   | Device is locked for switching over external sources (OPEN/CLOSE)                                      | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!Powerline 546E, FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT    |
| device_locked   | Contact   | Device is locked for switching manually (OPEN/CLOSE) - FRITZ!OS 6.90                                   | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!Powerline 546E, FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT    |
| temperature     | Number    | Actual measured temperature (in °C)                                                                    | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!DECT Repeater 100, FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT |
| energy          | Number    | Accumulated energy consumption (in kWh)                                                                | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!Powerline 546E                                                |
| power           | Number    | Current power consumption (in W)                                                                       | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!Powerline 546E                                                |
| outlet          | Switch    | Switchable outlet (ON/OFF)                                                                             | FRITZ!DECT 210, FRITZ!DECT 200, FRITZ!Powerline 546E                                                |
| actual_temp     | Number    | Actual Temperature of heating thermostat (in °C)                                                       | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| set_temp        | Number    | Set Temperature of heating thermostat (in °C)                                                          | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| eco_temp        | Number    | Eco Temperature of heating thermostat (in °C)                                                          | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| comfort_temp    | Number    | Comfort Temperature of heating thermostat (in °C)                                                      | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| radiator_mode   | String    | Mode of heating thermostat (ON/OFF/COMFORT/ECO/BOOST)                                                  | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| next_change     | DateTime  | Next change of the Set Temperature if scheduler is activated in the FRITZ!Box settings - FRITZ!OS 6.80 | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| next_temp       | Number    | Next Set Temperature if scheduler is activated in the FRITZ!Box settings (in °C) - FRITZ!OS 6.80       | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |
| battery_low     | Switch    | Battery Level Low (ON/OFF) - FRITZ!OS 6.80                                                             | FRITZ!DECT 301, FRITZ!DECT 300, Comet DECT                                                          |


## Full Example

demo.things:

```
Bridge avmfritz:fritzbox:1 @ "Office" [ ipAddress="192.168.x.x", password="xxx", user="xxx" ] {
    Thing FRITZ_DECT_200 xxxxxxxxxxxx "FRITZ!DECT 200 #1" @ "Living Room" [ ain="xxxxxxxxxxxx" ]
    Thing FRITZ_Powerline_546E yy_yy_yy_yy_yy_yy "FRITZ!Powerline 546E #2" @ "Office" [ ain="yy:yy:yy:yy:yy:yy" ]
    Thing Comet_DECT aaaaaabbbbbb "Comet DECT #3" @ "Office" [ ain="aaaaaabbbbbb" ]
}
```

demo.items:

```
Switch Outlet1 "Switchable outlet" { channel="avmfritz:FRITZ_DECT_200:1:xxxxxxxxxxxx:outlet" }
Number Temperature1 "Actual measured temperature [%.1f °C]" { channel="avmfritz:FRITZ_DECT_200:1:xxxxxxxxxxxx:temperature" }
Number Energy1 "Accumulated energy consumption [%.3f Wh]" { channel="avmfritz:FRITZ_DECT_200:1:xxxxxxxxxxxx:energy" }
Number Power1 "Current power consumption [%.2f W]" { channel="avmfritz:FRITZ_DECT_200:1:xxxxxxxxxxxx:power" }

Switch Outlet2 { channel="avmfritz:FRITZ_Powerline_546E:1:yy_yy_yy_yy_yy_yy:outlet" }

Group gCOMETDECT "Comet DECT heating thermostat" <temperature>

Number COMETDECTActualTemp "Actual measured temperature [%.1f °C]" (gCOMETDECT) { channel="avmfritz:Comet_DECT:1:aaaaaabbbbbb:actual_temp" }
Number COMETDECTSetTemp "Thermostat temperature set point [%.1f °C]" (gCOMETDECT) { channel="avmfritz:Comet_DECT:1:aaaaaabbbbbb:set_temp" }
String COMETDECTRadiatorMode "Radiator mode [%s]" (gCOMETDECT) { channel="avmfritz:Comet_DECT:1:aaaaaabbbbbb:radiator_mode" }
Switch COMETDECTBattery "Battery low" (gCOMETDECT) { channel="avmfritz:Comet_DECT:1:aaaaaabbbbbb:battery_low" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
	Frame label="FRITZ!DECT 200 switchable outlet" {
		Switch item=Outlet1 icon="poweroutlet"
		Text item=Temperature1 icon="temperature"
		Text item=Energy1 icon="energy"
		Text item=Power1 icon="energy"
	}
	Frame label="FRITZ!Powerline 546E switchable outlet" {
		Switch item=Outlet2
	}
	Frame "Comet DECT heating thermostat" {
		Text item=COMETDECTActualTemp icon="temperature"
		Setpoint item=COMETDECTSetTemp minValue=8.0 maxValue=28.0 step=0.5 icon="temperature"
		Selection item=COMETDECTRadiatorMode mappings=["ON"="ON", "OFF"="OFF", "COMFORT"="COMFORT", "ECO"="ECO", "BOOST"="BOOST"]
		Switch item=COMETDECTBattery icon="battery"
	}
}
```
