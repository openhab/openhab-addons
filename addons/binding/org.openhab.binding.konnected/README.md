# Konnected Binding

This binding is for interacting with the Konnected Module. A module which intefaces with existing home security sensors. 

https://konnected.io/_


## Supported Things

This binding supports one type of thing:  a konnected module.

## Discovery

The binding will auto disocver konnected modules which are attached to the same network as the server running openhab via UPnP.  The binding will then create things for each module discovered which can be added.

## Binding Configuration

There is no configuration required for the binding.

## Thing Configuration

The autodiscovery service of the binding will detect the ipAddress and port of the konnected module.  But once it is added you will need to provide an Authority Token to secure communication between the module and OpenHAB.  

For channels 1-5 the default setting is that these channels are connected to sensors.  If you have them connected to actuators you will need to change the appropriate setting in the thing via the paper ui.

## Channels

There are seven channels.  Zones 1-6 and the out channel which represent the respective pins on the konnected module.  You should only link the channels to items that you actually have sensors/actuators connected to.

Zones 1-6 will only accept a string item type with values of 0 or 1. While the out Channel  (Zone 7) will only accept a switch type item.

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
