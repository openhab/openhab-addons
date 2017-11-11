# JEnOcean Binding

This binding enables support for receiving messages from EnOcean rocker switches.


## Supported Things

Supports only EnOcean rocker wall switches with EEP F6-02-02.
Support for other devices is planned but not supported as of yet. The underlying library used for communication does support both sensors and actors though.

## Discovery

This binding does not support device discovery.

## Binding Configuration

_If your binding requires or supports general configuration settings, please create a folder ```cfg``` and place the configuration file ```<bindingId>.cfg``` inside it. In this section, you should link to this file and provide some information about the options. The file could e.g. look like:_

```
# Configuration for the Philips Hue Binding
#
# Default secret key for the pairing of the Philips Hue Bridge.
# It has to be between 10-40 (alphanumeric) characters 
# This may be changed by the user for security reasons.
secret=EclipseSmartHome
```

_Note that it is planned to generate some part of this based on the information that is available within ```ESH-INF/binding``` of your binding._

Config file: jenocean.cfg
Only parameter:
serialPort
Enter the port of the USB300 here. Like this for example:
serialPort=/dev/ttyUSB0

The binding can also be configured via the PaperUI:

Go to "Configuration" -> Bindings -> click pen symbol next to "JEnOcean Binding" -> enter serial port address -> click "save"

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

A rocker switch needs to be manually added through the PaperUI or via a thing file.

### Via PaperUI

Go to "Inbox" -> press green + -> select JEnOcean Binding -> Select Rocker Switch -> enter EnOcean address of device -> confirm with green checkmark.

The RockerSwitch Thing requires the configuration of its unique EnOcean address.
The parameter is "enoceanAddress".
The address may contain a dash (-) between each symbol pair or no delimiter at all.
For example:
enoceanAddress=AA-BB-CC-DD


## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

There are currently two channels for a rocker switch:

### On-Off-Channel

This channel allows you to bind any Item that supports ON and OFF commands to the rocker switch.
The ON state will be set whenever the up-button is pressed on the rocker switch.
The OFF state will be set whenever the down-button is pressed on the rocker switch.

### Rocker Switch Trigger Channel

This channel provides trigger events to OpenHAB. It cannot be directly linked to by items without using a special profile that converts these trigger events to commands which OpenHAB items can understand. 
Custom profiles are not yet supported in OpenHAB and so you cannot directly link this channel to an item.
You can however react to these events in rules.
Example:

```
rule "example EnOcean trigger rule"
when
    Channel 'jenocean:RockerSwitch:8b644edd:channel_a_rocker' triggered UP_PRESSED 
then
    sendCommand(MY_OTHER_ITEM, ON)
end
```

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

