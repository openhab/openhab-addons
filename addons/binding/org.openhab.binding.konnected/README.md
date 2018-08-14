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
The binding will attempt to discover the ip address of your OpenHAB server.  However, if it is unable to determine the ip address you can manually define the ip address and port in the thing configuration.

## Channels

The auto discovered thing adds two default channels.
One channel for Zone 6 which is a sensor type channel, and one channel for the out pin that is an actuator type channel.
These channels represent the two pins on the Konnected module whoose type cannot be changed.
For zones 1-5, you will need to add channels for the remaining zones that you have connected.
The only requirement is that the last charector of the typeId is the corresponding zone number in integer format.  
For example, a sensor hooked up to zone 1.  A new channel of type sensor with a typeID of any of the following names would work:
Zone1
Zone_1
Sensor1
Pin1
Pin_1
1

Then you need to link the corresponding item to the channel.
For sensor type channels the channel type is the contact item.
For the actuator type channel, the channel type is switch item.

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._


*.items

Contact Front_Door_Sensor " Front Door" {channel="konnected:module:16631321:Zone_2"}
Contact Back_Door_Sensor "Back Door"   {channel="konnected:module:16631321:Zone_1"}
Contact Konnected_Zone_4 "Back M"    {channel="konnected:module:16631321:Zone_4"}
Contact Konnected_Zone_5 "Front M"   {channel="konnected:module:16631321:Zone_5"}
Contact Konnected_Zone_6 "Kitchen M" {channel="konnected:module:16631321:Zone_6"}

*.sitemap

Switch item=Front_Door_Sensor label="Front Door" icon="door" mappings=["1"="Open", "0"="Closed"]
            Switch item=Back_Door_Sensor label="Back Door" icon="door" mappings=["1"="Open", "0"="Closed"]
            Switch item=Konnected_Zone_4 icon="door" mappings=["1"="Open", "0"="Closed"]
            Switch item=Konnected_Zone_5 icon="door" mappings=["1"="Open", "0"="Closed"]
            Switch item=Konnected_Zone_6 icon="door" mappings=["1"="Open", "0"="Closed"]
            