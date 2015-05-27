#MAX! Binding

This is the binding for the [eQ-3 MAX! Home Solution](http://www.eq-3.de/).
This binding allows you to integrate, view and control the MAX! Thermostats in the Openhab environment

## Supported Things

This binding support 6 different things types

| Thing | Type    | Description  |
|----------------|---------|-----------------------------------|
| bridge | Bridge | This is the MAX! Cube LAN gateway  |
| thermostat | Thing | This is for the MAX! Heating Thermostat. This is also used for the powerplug switch "Zwischenstecker-Schaltaktor".  |
| thermostatplus | Thing | This is for the MAX! Heating Thermostat+. This is the type that can hold the program by itself |
| wallthermostat | Thing | MAX! Wall Thermostat. |
| ecoswitch  | Thing | MAX! Ecoswitch. |
| shuttercontact  | Thing  | MAX! Shuttercontact / Window Contact. |

Generally one does not have to worry about the thing types as they are automatically defined.
If for any reason you need to manually define the Things and you are not exactly sure what type of thermostat you have, you can choose `thermostat` for both the thermostat and thermostat+ of them, this will not affect its working.

## Discovery

The discovery process for the MAX! binding works in 2 steps.
When the binding is started or when manually triggered, the network is queried for the existence of a MAX! Cube lan gateway. When the Cube is found, it will be available in the discovery inbox. Periodically the network is queried again for a Cube.

One the Cube is available in Openhab, all the devices connected to it are discovered and added to the discovery inbox. No scan is needed to trigger this. 

## Binding Configuration

In the Openhab2 version of this binding there are no binding wide settings. 
The configuration settings are now by MAX! Cube, hence in case you have multiple Cubes, they can run with alternative settings.
Generally there is no need to manually_If your binding requires or supports general configuration settings, please create a folder 

## Thing Configuration

All the things are identified by their Serial number, hence this is mandatory. 
The Cube (`bridge` thing) also requires the IP address to be defined. 
All other configuration is optional. 

Note that several configuration options are automatically populated. Later versions of the binding may allow you to update this information (where possible).

## Channels

Depending on the thing it supports different Channels


| Channel Type ID | Item Type    | Description  |
|------------------|------------------------|--------------|----------------- |------------- |
| mode | String       | This channel indicates the mode of a thermostat |
| battery_low | Switch | This channel indicates if the device battery is low |
| set_temp | Number | This channel indicates the sets temperature of a thermostat. |
| actual_temp | Number | This channel indicates the measured temperature of a thermostat.  see below for more details|
| valve | Number | This channel indicates the valve opening in %. Note this is an advaned setting, normally not visible |
| contact_state | Contact | This channel indicates the contact state for a shutterswitch |
| free_mem | Number |This channel indicates the free available memory on the cube to hold send commands. Note this is an advaned setting, normally not visible |
| duty_cycle | Number |  This channel indicates the duty cycle (due to regelatory reasons the cube is allowed only to send for a limited time. Duty cycle indicates how much of the available time is consumed) Note this is an advaned setting, normally not visible. |


## Full Example

In most cases no Things need to be defined manually. In case your Cube can't be discovered you need a `max:bridge` definition incl the right IP address of the Cube. Only in exceptional cases you would need to define the termostats etc.

max.things:

```
Bridge max:bridge:KEQ0565026 [ ipAddress="192.168.3.9", serialNumber="KEQ0565026" ]
max:thermostat:KEQ0565026 [ serialNumber="KEQ0565123" ]
```

max.items:
```
Group gMAX 			"MAX Heating" 	<temperature>	[ "home-group" ]

Switch maxBattery "Battery Low" (gMAX) {channel="max:thermostat:KEQ0565026:KEQ0648949:battery_low"}
String maxMode    "Thermostat Mode Setting" (gMAX) {channel="max:thermostat:KEQ0565026:KEQ0648949:mode"}
Number maxActual  "Actual measured room temperature  [%.1f °C]" (gMAX) {channel="max:thermostat:KEQ0565026:KEQ0648949:actual_temp"}
Number maxSetTemp "Thermostat temperature setpoint [%.1f °C]"  (gMAX) {channel="max:thermostat:KEQ0565026:KEQ0648949:set_temp"}


```

demo.sitemap:
```
sitemap demo label="Main Menu"
{
	Frame label="MAX Heating System" {
			Switch  item=maxMode  icon="climate" mappings=[AUTOMATIC=AUTOMATIC, MANUAL=MANUAL, BOOST=BOOST]
			Setpoint item=maxSetTemp minValue=4.5 maxValue=32 step=0.5 icon="temperature"
			Text item=maxActual  icon="temperature"
			Switch  item=maxBattery
		}
				
}
```

## Actual Temperature Update

Please be aware that the actual temperature measure for thermostats is only updated after the valve moved position or the thermostats mode has changed. Hence the temperature you see may be hours old. In that case you can update the temperature by changing the mode, wait approx 2 minutes and change the mode back. (note: Future versions of the binding may automate this.)

