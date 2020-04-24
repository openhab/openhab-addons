# WlanThermo Binding

This binding add support for the WlanThermo BBQ Thermometer.
![WlanThermo Nano V1+](doc/nano.jpg)

## Supported Things

This binding supports all version of WlanThermo, namely:
 - Mini V1
 - Mini V2
 - Nano V1
 - Nano V1+

For all devices, the binding provides general data about the connected probes of each channel:
- name (r/w)
- type (ro)
- current temperature (ro)
- minimum temperature threshold (r/w)
- maximum temperature threshold (r/w)
- buzzer alarm (r/w)
- push alarm (r/w)
- special alarm channel for openhab (trigger/switch) (ro)
- color (r/w)

Additionally, the binding provides status information about the device itself: 
- battery level (ro)
- charging state (ro)
- signal stregth (ro)

## Discovery

There is no auto-discovery of WlanThermo Things. 
Things must be created manually, e.g. via PaperUI.

## Thing Configuration

WlanThermo things require you to specify the IP-address of your WlanThermo device (the one you enter into your browser to access the WebUI)
The configuration of username/password is optional. If ommitted, the binding data will be read-only for all channels.

## Channels

As described above, the following channels apply for all 8 probes:

| channel  | type   | description                  |
|----------|--------|------------------------------|
| name | String | The name of this probe |
| type | String | The type of this probe |
| temp | Number:Temperature | The current temperature |
| min |  Number:Temperature | The minimum temperature threshold for this probe to trigger an alarm |
| max |  Number:Temperature | The maximum temperature threshold for this probe to trigger an alarm |
| alarm_device | Switch | Turn on/off the buzzer alarm on the device for this probe |
| alarm_push | Switch | Turn on/off the push alarm for this probe |
| alarm_openhab_low | Switch | Will turn on if current temp is below minimum temperature threshold |
| alarm_openhab_high | Switch | Will turn on if current temp is above maximum temperature threshold |
| color | Color | The color of this probe



The device itself provides the following channels:

| channel  | type   | description                  |
|----------|--------|------------------------------|
| soc | Number | Battery Level in % |
| charging | Switch | On, if device is charging, off otherwise | 
| rssi | Number | Signal Strength in range [0 ... 4] |

## Triggers

The following trigger apply for all 8 channels:

| trigger | values | description |
|--|--|--|
| alarm_openhab | MIN | Triggers repeatedly if current temp is below minimum temperature threshold |
| alarm_openhab | MAX | Triggers repeatedly if current temp is above maximum temperature threshold |


## Full Example

### Items

```
Group					gWlanThermo		"WlanThermo"
Number:Dimensionless 	soc  			"State of Charge"		(gWlanThermo)	{channel="wlanthermo:thermometer:f21c26bb:system#soc"}
Number 					rssi 			"Signal Strength"		(gWlanThermo)	{channel="wlanthermo:thermometer:f21c26bb:system#rssi"}
Switch 					charging 		"Charging"				(gWlanThermo)	{channel="wlanthermo:thermometer:f21c26bb:system#charge"}

Group					gProbe1			"Probe 1"				(gWlanThermo)
String 					name_1 			"Name"					(gProbe1)		{channel="wlanthermo:thermometer:f21c26bb:channel1#name"}
String 					typ_1 			"Type"					(gProbe1)		{channel="wlanthermo:thermometer:f21c26bb:channel1#typ"}
Number:Temperature 		temp_1 			"Temperature"			(gProbe1)		{channel="wlanthermo:thermometer:f21c26bb:channel1#temp"}
Number:Temperature 		min_1 			"Min Temperature"		(gProbe1)		{channel="wlanthermo:thermometer:f21c26bb:channel1#min"}
Number:Temperature 		max_1 			"Max Temperature"		(gProbe1)		{channel="wlanthermo:thermometer:f21c26bb:channel1#max"}
Switch 					alarm_device_1 	"Enable Buzzer Alarm"	(gProbe1)		{channel="wlanthermo:thermometer:f21c26bb:channel1#alarm_device"}
Switch 					alarm_push_1 	"Enable Push Alarm"		(gProbe1)		{channel="wlanthermo:thermometer:f21c26bb:channel1#alarm_push"}
Switch 					alarm_low_1 	"Low Temp. Alarm"		(gProbe1)		{channel="wlanthermo:thermometer:f21c26bb:channel1#alarm_openhab_low"}
Switch 					alarm_high_1 	"High Temp. Alarm"		(gProbe1)		{channel="wlanthermo:thermometer:f21c26bb:channel1#alarm_openhab_high"}
Color 					color_1 		"Color"					(gProbe1)		{channel="wlanthermo:thermometer:f21c26bb:channel1#color"}

Group					gProbe2			"Probe 2"				(gWlanThermo)
String 					name_2 			"Name"					(gProbe2)		{channel="wlanthermo:thermometer:f21c26bb:channel2#name"}
String 					typ_2 			"Type"					(gProbe2)		{channel="wlanthermo:thermometer:f21c26bb:channel2#typ"}
Number:Temperature 		temp_2 			"Temperature"			(gProbe2)		{channel="wlanthermo:thermometer:f21c26bb:channel2#temp"}
Number:Temperature 		min_2 			"Min Temperature"		(gProbe2)		{channel="wlanthermo:thermometer:f21c26bb:channel2#min"}
Number:Temperature 		max_2 			"Max Temperature"		(gProbe2)		{channel="wlanthermo:thermometer:f21c26bb:channel2#max"}
Switch 					alarm_device_2 	"Enable Buzzer Alarm"	(gProbe2)		{channel="wlanthermo:thermometer:f21c26bb:channel2#alarm_device"}
Switch 					alarm_push_2 	"Enable Push Alarm"		(gProbe2)		{channel="wlanthermo:thermometer:f21c26bb:channel2#alarm_push"}
Switch 					alarm_low_2 	"Low Temp. Alarm"		(gProbe2)		{channel="wlanthermo:thermometer:f21c26bb:channel2#alarm_openhab_low"}
Switch 					alarm_high_2 	"High Temp. Alarm"		(gProbe2)		{channel="wlanthermo:thermometer:f21c26bb:channel2#alarm_openhab_high"}
Color 					color_2 		"Color"					(gProbe2)		{channel="wlanthermo:thermometer:f21c26bb:channel2#color"}

Group					gProbe3			"Probe 3"				(gWlanThermo)
String 					name_3 			"Name"					(gProbe3)		{channel="wlanthermo:thermometer:f21c26bb:channel3#name"}
String 					typ_3 			"Type"					(gProbe3)		{channel="wlanthermo:thermometer:f21c26bb:channel3#typ"}
Number:Temperature 		temp_3 			"Temperature"			(gProbe3)		{channel="wlanthermo:thermometer:f21c26bb:channel3#temp"}
Number:Temperature 		min_3 			"Min Temperature"		(gProbe3)		{channel="wlanthermo:thermometer:f21c26bb:channel3#min"}
Number:Temperature 		max_3 			"Max Temperature"		(gProbe3)		{channel="wlanthermo:thermometer:f21c26bb:channel3#max"}
Switch 					alarm_device_3 	"Enable Buzzer Alarm"	(gProbe3)		{channel="wlanthermo:thermometer:f21c26bb:channel3#alarm_device"}
Switch 					alarm_push_3 	"Enable Push Alarm"		(gProbe3)		{channel="wlanthermo:thermometer:f21c26bb:channel3#alarm_push"}
Switch 					alarm_low_3 	"Low Temp. Alarm"		(gProbe3)		{channel="wlanthermo:thermometer:f21c26bb:channel3#alarm_openhab_low"}
Switch 					alarm_high_3 	"High Temp. Alarm"		(gProbe3)		{channel="wlanthermo:thermometer:f21c26bb:channel3#alarm_openhab_high"}
Color 					color_3 		"Color"					(gProbe3)		{channel="wlanthermo:thermometer:f21c26bb:channel3#color"}

Group					gProbe4			"Probe 4"				(gWlanThermo)
String 					name_4 			"Name"					(gProbe4)		{channel="wlanthermo:thermometer:f21c26bb:channel4#name"}
String 					typ_4 			"Type"					(gProbe4)		{channel="wlanthermo:thermometer:f21c26bb:channel4#typ"}
Number:Temperature 		temp_4 			"Temperature"			(gProbe4)		{channel="wlanthermo:thermometer:f21c26bb:channel4#temp"}
Number:Temperature 		min_4 			"Min Temperature"		(gProbe4)		{channel="wlanthermo:thermometer:f21c26bb:channel4#min"}
Number:Temperature 		max_4 			"Max Temperature"		(gProbe4)		{channel="wlanthermo:thermometer:f21c26bb:channel4#max"}
Switch 					alarm_device_4 	"Enable Buzzer Alarm"	(gProbe4)		{channel="wlanthermo:thermometer:f21c26bb:channel4#alarm_device"}
Switch 					alarm_push_4 	"Enable Push Alarm"		(gProbe4)		{channel="wlanthermo:thermometer:f21c26bb:channel4#alarm_push"}
Switch 					alarm_low_4 	"Low Temp. Alarm"		(gProbe4)		{channel="wlanthermo:thermometer:f21c26bb:channel4#alarm_openhab_low"}
Switch 					alarm_high_4 	"High Temp. Alarm"		(gProbe4)		{channel="wlanthermo:thermometer:f21c26bb:channel4#alarm_openhab_high"}
Color 					color_4 		"Color"					(gProbe4)		{channel="wlanthermo:thermometer:f21c26bb:channel4#color"}

Group					gProbe5			"Probe 5"				(gWlanThermo)
String 					name_5 			"Name"					(gProbe5)		{channel="wlanthermo:thermometer:f21c26bb:channel5#name"}
String 					typ_5 			"Type"					(gProbe5)		{channel="wlanthermo:thermometer:f21c26bb:channel5#typ"}
Number:Temperature 		temp_5 			"Temperature"			(gProbe5)		{channel="wlanthermo:thermometer:f21c26bb:channel5#temp"}
Number:Temperature 		min_5 			"Min Temperature"		(gProbe5)		{channel="wlanthermo:thermometer:f21c26bb:channel5#min"}
Number:Temperature 		max_5 			"Max Temperature"		(gProbe5)		{channel="wlanthermo:thermometer:f21c26bb:channel5#max"}
Switch 					alarm_device_5 	"Enable Buzzer Alarm"	(gProbe5)		{channel="wlanthermo:thermometer:f21c26bb:channel5#alarm_device"}
Switch 					alarm_push_5 	"Enable Push Alarm"		(gProbe5)		{channel="wlanthermo:thermometer:f21c26bb:channel5#alarm_push"}
Switch 					alarm_low_5 	"Low Temp. Alarm"		(gProbe5)		{channel="wlanthermo:thermometer:f21c26bb:channel5#alarm_openhab_low"}
Switch 					alarm_high_5 	"High Temp. Alarm"		(gProbe5)		{channel="wlanthermo:thermometer:f21c26bb:channel5#alarm_openhab_high"}
Color 					color_5 		"Color"					(gProbe5)		{channel="wlanthermo:thermometer:f21c26bb:channel5#color"}

Group					gProbe6			"Probe 6"				(gWlanThermo)
String 					name_6 			"Name"					(gProbe6)		{channel="wlanthermo:thermometer:f21c26bb:channel6#name"}
String 					typ_6 			"Type"					(gProbe6)		{channel="wlanthermo:thermometer:f21c26bb:channel6#typ"}
Number:Temperature 		temp_6 			"Temperature"			(gProbe6)		{channel="wlanthermo:thermometer:f21c26bb:channel6#temp"}
Number:Temperature 		min_6 			"Min Temperature"		(gProbe6)		{channel="wlanthermo:thermometer:f21c26bb:channel6#min"}
Number:Temperature 		max_6 			"Max Temperature"		(gProbe6)		{channel="wlanthermo:thermometer:f21c26bb:channel6#max"}
Switch 					alarm_device_6 	"Enable Buzzer Alarm"	(gProbe6)		{channel="wlanthermo:thermometer:f21c26bb:channel6#alarm_device"}
Switch 					alarm_push_6 	"Enable Push Alarm"		(gProbe6)		{channel="wlanthermo:thermometer:f21c26bb:channel6#alarm_push"}
Switch 					alarm_low_6 	"Low Temp. Alarm"		(gProbe6)		{channel="wlanthermo:thermometer:f21c26bb:channel6#alarm_openhab_low"}
Switch 					alarm_high_6 	"High Temp. Alarm"		(gProbe6)		{channel="wlanthermo:thermometer:f21c26bb:channel6#alarm_openhab_high"}
Color 					color_6 		"Color"					(gProbe6)		{channel="wlanthermo:thermometer:f21c26bb:channel6#color"}

Group					gProbe7			"Probe 7"				(gWlanThermo)
String 					name_7 			"Name"					(gProbe7)		{channel="wlanthermo:thermometer:f21c26bb:channel7#name"}
String 					typ_7 			"Type"					(gProbe7)		{channel="wlanthermo:thermometer:f21c26bb:channel7#typ"}
Number:Temperature 		temp_7 			"Temperature"			(gProbe7)		{channel="wlanthermo:thermometer:f21c26bb:channel7#temp"}
Number:Temperature 		min_7 			"Min Temperature"		(gProbe7)		{channel="wlanthermo:thermometer:f21c26bb:channel7#min"}
Number:Temperature 		max_7 			"Max Temperature"		(gProbe7)		{channel="wlanthermo:thermometer:f21c26bb:channel7#max"}
Switch 					alarm_device_7 	"Enable Buzzer Alarm"	(gProbe7)		{channel="wlanthermo:thermometer:f21c26bb:channel7#alarm_device"}
Switch 					alarm_push_7 	"Enable Push Alarm"		(gProbe7)		{channel="wlanthermo:thermometer:f21c26bb:channel7#alarm_push"}
Switch 					alarm_low_7 	"Low Temp. Alarm"		(gProbe7)		{channel="wlanthermo:thermometer:f21c26bb:channel7#alarm_openhab_low"}
Switch 					alarm_high_7 	"High Temp. Alarm"		(gProbe7)		{channel="wlanthermo:thermometer:f21c26bb:channel7#alarm_openhab_high"}
Color 					color_7 		"Color"					(gProbe7)		{channel="wlanthermo:thermometer:f21c26bb:channel7#color"}

Group					gProbe8			"Probe 8"				(gWlanThermo)
String 					name_8 			"Name"					(gProbe8)		{channel="wlanthermo:thermometer:f21c26bb:channel8#name"}
String 					typ_8 			"Type"					(gProbe8)		{channel="wlanthermo:thermometer:f21c26bb:channel8#typ"}
Number:Temperature 		temp_8 			"Temperature"			(gProbe8)		{channel="wlanthermo:thermometer:f21c26bb:channel8#temp"}
Number:Temperature 		min_8 			"Min Temperature"		(gProbe8)		{channel="wlanthermo:thermometer:f21c26bb:channel8#min"}
Number:Temperature 		max_8 			"Max Temperature"		(gProbe8)		{channel="wlanthermo:thermometer:f21c26bb:channel8#max"}
Switch 					alarm_device_8 	"Enable Buzzer Alarm"	(gProbe8)		{channel="wlanthermo:thermometer:f21c26bb:channel8#alarm_device"}
Switch 					alarm_push_8 	"Enable Push Alarm"		(gProbe8)		{channel="wlanthermo:thermometer:f21c26bb:channel8#alarm_push"}
Switch 					alarm_low_8 	"Low Temp. Alarm"		(gProbe8)		{channel="wlanthermo:thermometer:f21c26bb:channel8#alarm_openhab_low"}
Switch 					alarm_high_8 	"High Temp. Alarm"		(gProbe8)		{channel="wlanthermo:thermometer:f21c26bb:channel8#alarm_openhab_high"}
Color 					color_8 		"Color"					(gProbe8)		{channel="wlanthermo:thermometer:f21c26bb:channel8#color"}
```

### Sitemap

```
sitemap wlanthermo label="WlanThermo" {
	Frame label="WlanThermo" {
		Default item=soc icon="batterylevel"
		Default item=rssi icon="qualityofservice"
		Default item=charging icon="energy"
		Text icon=""
		
		Text item=name_1 icon="fire" {
			Frame {
				Default item=typ_1 icon="settings"
				Default item=color_1
				
				Default item=temp_1 icon="temperature"
				Text icon=""
				Setpoint item=min_1 icon="temperature_cold"
				Setpoint item=max_1 icon="temperature_hot"
				
				Default item=alarm_device_1 icon="switch"
				Default item=alarm_push_1 icon="switch"
				Default item=alarm_low_1 icon="siren"
				Default item=alarm_high_1 icon="siren"
			}
		}
		
		Text item=name_2 icon="fire" {
			Frame {
				Default item=typ_2 icon="settings"
				Default item=color_2
				
				Default item=temp_2 icon="temperature"
				Text icon=""
				Setpoint item=min_2 icon="temperature_cold"
				Setpoint item=max_2 icon="temperature_hot"
				
				Default item=alarm_device_2 icon="switch"
				Default item=alarm_push_2 icon="switch"
				Default item=alarm_low_2 icon="siren"
				Default item=alarm_high_2 icon="siren"
			}
		}
		
		Text item=name_3 icon="fire" {
			Frame {
				Default item=typ_3 icon="settings"
				Default item=color_3
				
				Default item=temp_3 icon="temperature"
				Text icon=""
				Setpoint item=min_3 icon="temperature_cold"
				Setpoint item=max_3 icon="temperature_hot"
				
				Default item=alarm_device_3 icon="switch"
				Default item=alarm_push_3 icon="switch"
				Default item=alarm_low_3 icon="siren"
				Default item=alarm_high_3 icon="siren"
			}
		}
		
		Text item=name_4 icon="fire" {
			Frame {
				Default item=typ_4 icon="settings"
				Default item=color_4
				
				Default item=temp_4 icon="temperature"
				Text icon=""
				Setpoint item=min_4 icon="temperature_cold"
				Setpoint item=max_4 icon="temperature_hot"
				
				Default item=alarm_device_4 icon="switch"
				Default item=alarm_push_4 icon="switch"
				Default item=alarm_low_4 icon="siren"
				Default item=alarm_high_4 icon="siren"
			}
		}
		
		Text item=name_5 icon="fire" {
			Frame {
				Default item=typ_5 icon="settings"
				Default item=color_5
				
				Default item=temp_5 icon="temperature"
				Text icon=""
				Setpoint item=min_5 icon="temperature_cold"
				Setpoint item=max_5 icon="temperature_hot"
				
				Default item=alarm_device_5 icon="switch"
				Default item=alarm_push_5 icon="switch"
				Default item=alarm_low_5 icon="siren"
				Default item=alarm_high_5 icon="siren"
			}
		}
		
		Text item=name_6 icon="fire" {
			Frame {
				Default item=typ_6 icon="settings"
				Default item=color_6
				
				Default item=temp_6 icon="temperature"
				Text icon=""
				Setpoint item=min_6 icon="temperature_cold"
				Setpoint item=max_6 icon="temperature_hot"
				
				Default item=alarm_device_6 icon="switch"
				Default item=alarm_push_6 icon="switch"
				Default item=alarm_low_6 icon="siren"
				Default item=alarm_high_6 icon="siren"
			}
		}
		
		Text item=name_7 icon="fire" {
			Frame {
				Default item=typ_7 icon="settings"
				Default item=color_7
				
				Default item=temp_7 icon="temperature"
				Text icon=""
				Setpoint item=min_7 icon="temperature_cold"
				Setpoint item=max_7 icon="temperature_hot"
				
				Default item=alarm_device_7 icon="switch"
				Default item=alarm_push_7 icon="switch"
				Default item=alarm_low_7 icon="siren"
				Default item=alarm_high_7 icon="siren"
			}
		}
		
		Text item=name_8 icon="fire" {
			Frame {
				Default item=typ_8 icon="settings"
				Default item=color_8
				
				Default item=temp_8 icon="temperature"
				Text icon=""
				Setpoint item=min_8 icon="temperature_cold"
				Setpoint item=max_8 icon="temperature_hot"
				
				Default item=alarm_device_8 icon="switch"
				Default item=alarm_push_8 icon="switch"
				Default item=alarm_low_8 icon="siren"
				Default item=alarm_high_8 icon="siren"
			}
		}
	}
}

```

