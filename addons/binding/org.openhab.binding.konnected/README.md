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

The auto-discovery service of the binding will detect the ipAddress and port of the Konnected module.  
But once it is added you will need to provide an Authority Token to secure communication between the module and openHAB.  
The binding will attempt to discover the ip address of your openHAB server.  However, if it is unable to determine the ip address you can manually define the ip address and port in the thing configuration.

## Channels

The auto discovered thing adds two default channels.
One channel for Zone 6 which is a sensor type channel, and one channel for the out pin that is an actuator type channel.
These channels represent the two pins on the Konnected module whose type cannot be changed.
For zones 1-5, you will need to add channels for the remaining zones that you have connected.
The only requirement is that the last character of the typeId is the corresponding zone number in integer format.  
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
For the actuator type channels you can also add configuration parameters times, pause and momentary which will be added to the payload that is sent to the Konnected Module.
These parameters will tell the module to pulse the actuator for certain time period.
A momentary switch actuates a switch for a specified time (in milliseconds) and then reverts it back to the off state. This is commonly used with a relay module to actuate a garage door opener, or with a doorbell to send a momentary trigger to sound the doorbell.
A beep/blink switch is like a momentary switch that repeats either a specified number of times or indefinitely. This is commonly used with a a piezo buzzer to make a "beep beep" sound when a door is opened, or to make a repeating beep pattern for an alarm or audible warning. It can also be used to blink lights.

## Full Example

*.items

Contact Front_Door_Sensor " Front Door" {channel="konnected:module:16631321:Zone_2"}
Switch Siren "Siren"   {channel="konnected:module:16631321:Zone_1"}


*.sitemap

Switch item=Front_Door_Sensor label="Front Door" icon="door" mappings=[OPEN="Open", CLOSED="Closed"]
Switch item=Siren label="Alarm Siren" icon="Siren" mappings=[ON="Open", OFF="Closed"]
            