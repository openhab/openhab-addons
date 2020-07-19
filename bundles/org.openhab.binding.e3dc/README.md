# E3DC Binding

<img style="float: right;" src="doc/E3DC_logo.png">
Integrates the Home Power Plants from E3/DC GmbH into openhab. The Power Plant handles all your Electrical Energy Resources like Photovoltaik Producers, Battery Storage, Wallbox Power Supply, Household consumption and even more.
The binding operates via Modbus to read and write values towards the E3DC device. Please refer to the official Modbus documentation for more details.
The binding is designed the following way
1) Create Bridge "E3DC Home Power Plant" and provide IP-Address and Port Number for the general Device Conncetion
2) Add your wanted Blocks 
* if you have a Wallbox connected - add Wallbox Control Block 
* if you want Details of your attched Strings - add String Details Block
With this design it's possible for you to install only the parts you are interested in.


## Supported Things

First you need a Bridge which establishes the basic connection towards your E3DC device

| Name               | Bridge Type ID | Description                                                                                            |
|--------------------|----------------|--------------------------------------------------------------------------------------------------------|
| E3DC Home Power Plant | e3dc-device    | Establishes Modbus Connection to your Device. Add your desired Blocks to this Bridge afterwards.     |

After establishing the Bridge add certain Blocks to gather Informations and Settings

| Name               | Thing Type ID | Description                                                                                            |
|--------------------|----------------|--------------------------------------------------------------------------------------------------------|
| E3DC Information Block | e3dc-info    | Basic Information of your E3DC Device like Model Name, Serial Number and Software Versions             |
| E3DC Power Block | e3dc-power    | Provides values of your attached eletrical Producers (Photovoltaik, Battery, ... and Consumers (Household, Wallbox, ...) |
| E3DC Wallbox Control Block | e3dc-wallbox    | Provides your Wallbox Settings. Switches like "Sunmode" or "3Phase Charging" can be changed! |
| E3DC String Details Block | e3dc-strings    | Provides detailed values of your attached Photovoltaik Strings. Evaluate how much Power each String provides |
| E3DC EMS Block | e3dc-emergency    | Provides values of Emergency Power Status (EMS) and regulations like Battery loading / unloading restrictions |


## Discovery

There's no discovery. Modbus registers are available for all devices. Just install the blocks you are interested in.

## Binding Configuration

_If your binding requires or supports general configuration settings, please create a folder ```cfg``` and place the configuration file ```<bindingId>.cfg``` inside it. In this section, you should link to this file and provide some information about the options. The file could e.g. look like:_

```
# Configuration for the Philips Hue Binding
#
# Default secret key for the pairing of the Philips Hue Bridge.
# It has to be between 10-40 (alphanumeric) characters
# This may be changed by the user for security reasons.
secret=openHABSecret
```

_Note that it is planned to generate some part of this based on the information that is available within ```src/main/resources/ESH-INF/binding``` of your binding._

_If your binding does not offer any generic configurations, you can remove this section completely._

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/ESH-INF/thing``` of your binding._

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/ESH-INF/thing``` of your binding._

| channel  | type   | description                  |
|----------|--------|------------------------------|
| control  | Switch | This is the control channel  |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
