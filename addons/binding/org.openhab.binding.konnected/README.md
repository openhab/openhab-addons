# Konnected Binding

This binding is for interacting with the Konnected Module. A module which interfaces with existing home security sensors.  

Konnected is an open-source firmware and software that runs on a NodeMCU ESP8266 device. 

The Konnected hardware is specifically designed for an alarm panel installation, but the general purpose firmware/software can be run on any ESP8266 device.

https://Konnected.io/_


## Supported Things

This binding supports one type of thing:  a Konnected module.

## Discovery

The binding will auto discover Konnected modules which are attached to the same network as the server running openHAB via UPnP.  

The binding will then create things for each module discovered which can be added.

## Binding Configuration

There is no configuration required for the binding.

## Thing Configuration

The autodiscovery service of the binding will detect the ipAddress and port of the Konnected module.  

But once it is added you will need to provide an Authority Token to secure communication between the module and openHAB.  

For channels 1-5 the default setting is that these channels are connected to sensors.  

If you have them connected to actuators you will need to change the appropriate setting in the thing.

## Channels

There are seven channels.  

Zones 1-6 and the out channel which represent the respective pins on the Konnected module.  

You should only link the channels to items that you actually have sensors/actuators connected to.

Zones 1-6 will only accept a string item type with values of 0 or 1. While the out Channel  (Zone 7) will only accept a switch type item.

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._


*.items

String Front_Door_Sensor " Front Door" {channel="konnected:module:16631321:Zone_2"}
String Back_Door_Sensor "Back Door"   {channel="konnected:module:16631321:Zone_1"}
String Konnected_Zone_4 "Back M"    {channel="konnected:module:16631321:Zone_4"}
String Konnected_Zone_5 "Front M"   {channel="konnected:module:16631321:Zone_5"}
String Konnected_Zone_6 "Kitchen M" {channel="konnected:module:16631321:Zone_6"}

*.sitemap

Switch item=Front_Door_Sensor label="Front Door" icon="door" mappings=["1"="Open", "0"="Closed"]
            Switch item=Back_Door_Sensor label="Back Door" icon="door" mappings=["1"="Open", "0"="Closed"]
            Switch item=Konnected_Zone_4 icon="door" mappings=["1"="Open", "0"="Closed"]
            Switch item=Konnected_Zone_5 icon="door" mappings=["1"="Open", "0"="Closed"]
            Switch item=Konnected_Zone_6 icon="door" mappings=["1"="Open", "0"="Closed"]





