# MiFlora Binding

The miflora binding ...

_Give some details about what this binding is meant for - a protocol, system, specific device._

_If possible, provide some resources like pictures, a YouTube video, etc. to give an impression of what can be done with this binding. You can place such resources into a `doc` folder next to this README.md._

## Supported Things

_Please describe the different supported things / devices within this section._
_Which different types are supported, which models were tested etc.?_
_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

This binding will use the Bluetooth Low Energy support in Eclipse Smarthome.

## Discovery

This binding provides no discovery. The desired flora sensors must be configured manually or via a things file.

_Describe the available auto-discovery features here. Mention for what it works and what needs to be kept in mind when using it._

## Binding Configuration

The binding has no configuration options itself, all configuration is done at 'Things' level.

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

## Channels

In the table is shown more detailed information about each Channel type.
The binding introduces the following channels:

| Channel ID                                      | Channel Description                                          | Supported item type | Advanced |
|-------------------------------------------------|--------------------------------------------------------------|---------------------|----------|

## Full Example

miflora.things:

```
Thing miflora:flora:avocado "Avocado" @ "Living Room" [ ]
```

miflora.items:

```
Number   PlantAvocadoMoisture     "Avocado Moisture [%d %%]"
<humidity>
{
	channel="miflora:flora:avocado:measurements#moisture",
	mqtt="<[mosquitto:plants/avocado/moist:state:default]"
}

Switch   PlantAvocadoMoistWarn    "Avocado Moisture Level Warning"
```

miflora.sitemap:
TODO - enhance sitemap

```
sitemap miflora label="Miflora Menu"
{
	Group item=Plants
}
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_


## MQTT topic tree from checkplant script

+ python3 /home/pirate/miflora/checkplant.py C4:7C:8D:61:90:56
+ LOGROW='2017-01-03 12:45:09 C: 23.40 M: 27 L: 1873 CD: 277 B: 94 time:1483447509963'

+ mosquitto_pub -h 1.2.3.4 -m 23.40 -t plants/avocado/temp -q 1
+ mosquitto_pub -h 1.2.3.4 -m 27 -t plants/avocado/moist -q 1
+ mosquitto_pub -h 1.2.3.4 -m 1873 -t plants/avocado/light -q 1
+ mosquitto_pub -h 1.2.3.4 -m 277 -t plants/avocado/cond -q 1
+ mosquitto_pub -h 1.2.3.4 -m 94 -t plants/avocado/battery -q 1
+ mosquitto_pub -h 1.2.3.4 -m 20 -t plants/avocado/moistlow -q 1
+ mosquitto_pub -h 1.2.3.4 -m 60 -t plants/avocado/moisthigh -q 1
+ mosquitto_pub -h 1.2.3.4 -m 500 -t plants/avocado/condlow -q 1
+ mosquitto_pub -h 1.2.3.4 -m 2000 -t plants/avocado/condhigh -q 1

+ echo 'Published avocado: TEMP=23.40 MOIST=27 LIGHT=1873 COND=277 BATTERY=94'
Published avocado: TEMP=23.40 MOIST=27 LIGHT=1873 COND=277 BATTERY=94
+ echo 'Published COND_LOW=500 COND_HIGH=2000'
Published COND_LOW=500 COND_HIGH=2000

## MQTT topic tree from ThomDietrich's script

* https://github.com/ThomDietrich/miflora-mqtt-daemon

Adding device from config to Mi Flora device list ...
Name:         "Avocado"
Device name:  "Flower mate"
MAC address:  C4:7C:8D:61:B5:C5
Firmware:     2.6.2

Connecting to MQTT broker ...
Connected.

[2017-07-25 12:26:16] Attempting to publishing to MQTT topic "miflora/Avocado" ...
Data: {"temperature": 24.0, "conductivity": 859, "battery": 100, "light": 148, "moisture": 35}
Data successfully published!
