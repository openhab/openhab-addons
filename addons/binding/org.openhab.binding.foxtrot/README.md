# Foxtrot Binding

The Foxtrot binding allows interaction with [Tecomat Foxtrot](https://www.tecomat.com/products/cat/cz/plc-tecomat-foxtrot-3/) system from Czech company [Teco](https://www.tecomat.com).
Tecomat Foxtrot is a compact modular control and regulation system with powerful processor, mature communications, original two-wires and wireless connection with intelligent electroinstallation elements and peripherals. 

**Important features**
* High performance
* Modularity
* Installation design („circuit breakers“)
* Up to 270 inputs and outputs / 320 modules on CIB bus
* Built-in ethernet, web server, web pages
* On-line programming using Mosaic software
* Possibility of simple parameterization using FoxTool software
* Application software back-up in the internal memory
* Native Reliance 4 SCADA/HMI driver, Tecomat OPC server available
* SD/SDHC/MMC card slot as a high capacity storage mass up to 32 GB
* Support of Modbus RTU/TCP, Profi bus DP, CAN, BACnet,
* HTTP and other standard protocols

_If possible, provide some resources like pictures, a YouTube video, etc. to give an impression of what can be done with this binding. You can place such resources into a `doc` folder next to this README.md._

The central element of Foxtrot system is the Foxtrot basic module - PLC (Programmable Logic Controller), i.e. CP-1000 and other variants. `todo nieco o senzoroch, actoroch, programoch a premennych` Foxtrot PLC provides standard ethernet conectivity, but implements own specific EPSNET over standard TCP/IP protocol to communicate with basic module.     

To simplify communication between external systems and Foxtrot PLC company provides communication server software called [PLCComS](https://www.tecomat.com/download/software-and-firmware/plccoms/). PLCComS provide TCP/IP connection with client device/software and a PLC. Communication of server with client is created by simple text oriented protocol - question/answer. Server communicates with PLC optimalized by EPSNET protocol. PLCComS can runs on PC with Linux (32bit/64bit) or Windows operating system or on ARM based devices like Raspberry-Pi with Linux (eabi, eabihf).

This binding uses PLCComS communication server to gets and sets published PLC's program public variables.

`obrazok`

## Supported Things

_Please describe the different supported things / devices within this section._
_Which different types are supported, which models were tested etc.?_
_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

`zakladne variable string, number a bool a dalsie logicke struktury reprezentujuce ... poskladane z premennych`

## Discovery

_Describe the available auto-discovery features here. Mention for what it works and what needs to be kept in mind when using it._

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

_If your binding does not offer any generic configurations, you can remove this section completely._

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
