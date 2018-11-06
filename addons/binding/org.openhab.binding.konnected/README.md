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
However, the ip address of the openHAB server can be configured via the OSGI NetworkAddressService property.  
If this is configured the binding will use this as the local ip address of the openHAB server.
If it is not configured the the binding will utilize the NetworkAddressService to attempt to obtain the ipAddress of the openHAB server.

## Thing Configuration

The binding will attempt to discover Konnected modules via the upnp service.
The auto-discovery service of the binding will detect the ipAddress and port of the Konnected module.  
But once it is added you will need to provide an Authority Token to secure communication between the module and openHAB.  
As discussed above the binding will attempt to discover the ip address of your openHAB server.  However, if it is unable to determine the ip address you can manually define the ip address and port in the thing configuration.
In addition you can also turn off discovery which when this setting is synced to the module will cause the device to no longer respond to upnp requests as documented. https://help.konnected.io/support/solutions/articles/32000023968-disabling-device-discovery
Please use this setting with caution and do not enable until a static ip address has been provided for your Konnected module via DHCP, router or otherwise.
The blink setting will disable the transmission LED on the Konnected module.


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
A momentary switch actuates a switch for a specified time (in milliseconds) and then reverts it back to the off state. This is commonly used with a relay module to actuate a garage door opener, or with a door bell to send a momentary trigger to sound the door bell.
A beep/blink switch is like a momentary switch that repeats either a specified number of times or indefinitely. This is commonly used with a a piezo buzzer to make a "beep beep" sound when a door is opened, or to make a repeating beep pattern for an alarm or audible warning. It can also be used to blink lights.

DSB1820 temperature probes.
These are one wire devices which can all be Konnected to the same "Zone" on the Konnected module.
As part of its transmission  the module will include an unique "address" property of each sensor probe that will be logged to the debug log when received. 
This needs to be added to the channel if there are multiple probes connected. 
The default behavior in absence of this configuration will be to overwrite the channel on every transmission. 
A channel should be added for each probe, as indicated above just make sure that the typeID of all of the DSB1820 sensors on the same zone end with the same number corresponding to the zone in which they are connected.  
For example:
Temp1Zone1
Temp2Zone1
Temp3Zone1
Then separately configure each channel to have the unique address received from the module.

## Full Example

*.items

Contact Front_Door_Sensor " Front Door" {channel="konnected:module:16631321:Zone_2"}
Switch Siren "Siren"   {channel="konnected:module:16631321:Zone_1"}


*.sitemap

Switch item=Front_Door_Sensor label="Front Door" icon="door" mappings=[OPEN="Open", CLOSED="Closed"]
Switch item=Siren label="Alarm Siren" icon="Siren" mappings=[ON="Open", OFF="Closed"]
            
*.things

Thing konnected:module:1586517 "Konnected Module" [ipAddress="http://192.168.30.153:9586", macAddress="1586517"]