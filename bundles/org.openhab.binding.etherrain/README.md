# Etherrain Binding

The Etherrain binding is used to control a sprinkler controller from Quicksmart:

<http://www.quicksmart.com/qs_etherrain.html>

The API exposed by the controller is fairly robust, but it is specific.
The binding attempts to map this API to an openHAB thing while perserving the functionality.

## Overview

An Etherrain controller can control 7-8 zones (depending on model) and can also report on the status of a rain sensor.
The controller itself accepts a single command that contains an initial delay, and an on-time for each of the zone.
Once this execute command is sent, the controller will, first, wait the initial delay time, then cycle through each zone and turn it on the amount of time specified.
The binding exposes the rain sensor as a contact as well as the operating status of of the controller.

## Supported Things

The etherrain thing represents a physical Etherrain controller and contains all channels need to control it.

## Discovery

The binding will automatically discover Etherrain controllers when a thing is added.

## Binding Configuration

There are two main categories of configuration.
The first is the configuration of the communication settings (IP address, timeout, etc.).
The second is the initial delay and on-time for each zone when an execute command is issued.

## Thing Configuration

This is optional, it is recommended to let the binding discover and add Etherrain controllers.

To manually configure an Etherrain controller you may specify its host name or ip ("host").
You can also optionally specify the unit's password ("pw"), port it is communicating on ("port") or refresh rate ("refresh")

```java
Bridge etherrain:etherrain:BackyardSprinkler [ host="192.168.1.100"]
```

## Channels

The etherrain controller exposes the rain sensor as well as several status messages.
Finally, there are commands to execute and clear the commands:

items:

```java
String SprinklerCommandStatus       "Command Status [%s]"  (gMain) { channel="etherrain:etherrain:sprinkler0:commandstatus" }
String SprinklerOperatingStatus     "Operating Status [%s]"  (gMain) { channel="etherrain:etherrain:sprinkler0:operatingstatus" }
String SprinklerOperatingResult     "Operating Result [%s]"  (gMain) { channel="etherrain:etherrain:sprinkler0:operatingresult" }
            
String SprinklerActiveZone         "Active Zone [%s]"  (gMain) { channel="etherrain:etherrain:sprinkler0:relayindex" }                  
Switch SprinklerRainSensor        (gMain) { channel="etherrain:etherrain:sprinkler0:rainsensor" }
            
Switch SprinklerExecute            (gMain) { channel="etherrain:etherrain:sprinkler0:execute" }
Switch SprinklerClear              (gMain) { channel="etherrain:etherrain:sprinkler0:clear" }
```
