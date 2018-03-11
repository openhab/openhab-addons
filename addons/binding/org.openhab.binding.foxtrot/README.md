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

## Overview

Foxtrot system consists from central bacis module, both wired and wireless extension modules (DIN rail circuit
breakers, wall switches, actors for the installation into electroinstallation boxes, sensors, etc.), see 
[poster1](https://www.tecomat.com/modules/DownloadManager/download.php?alias=poster-foxtrot_1) and 
[poster2](https://www.tecomat.com/modules/DownloadManager/download.php?alias=poster-foxtrot_2).
The central element of Foxtrot system is the Foxtrot basic module - PLC (Programmable Logic Controller), i.e. `CP-1000`
and other variants. Main principle behind PLC is that through digital or analog
inputs and outputs the PLC receives and sends information from/into the unit being controlled. Control algorithms are
saved in the memory of the user program that is executed cyclically. User program uses memory register to store all
program's variables. Some of them can be marked as public and than are accessible by external systems. All public variables
are published into so called PUB text file where are all details needed to accessing them. 
Foxtrot PLC provides standard ethernet conectivity (RJ45 socket on base module). To access Foxtrot PLC internal
memory registers external software/device must use specific EPSNET protocol over standard TCP/IP.

To simplify accessing internal registers by external systems company provides communication server software
called [PLCComS](https://www.tecomat.com/download/software-and-firmware/plccoms/). PLCComS provide TCP/IP connection
with client device/software and a PLC. Communication of server with client is created by simple text oriented
protocol - question/answer. Server communicates with PLC optimalized by EPSNET protocol. PLCComS can runs on PC
with Linux (32bit/64bit) or Windows operating system or on ARM based devices like Raspberry-Pi with
Linux (eabi, eabihf).

The Foxtrot binding uses PLCComS communication server to gets values from and sets values to user program public
variables. The binding represents a "PLCComS" as a bridge thing type and all other things are connected to the bridge.

`obrazok`

## Supported Things

Each thing consist one or more Foxtrot PLC's user program public variable. There are three basic thing represents three
types of variables: string, number (real and integer) and boolean. Other things are logical structure of variables
that represents real-life device (switch, rollershutter, etc.) that is provided and controlled by user program algorithms.

| Things             | Description                                                                       | Thing Type |
|--------------------|-----------------------------------------------------------------------------------|------------|
| PLCComS server     | The bridge represents PLCComS communication server                                | plccoms    |
| String variable    | String variable                                                                   | string     |
| Number variable    | Number variable                                                                   | number     |
| Bool variable      | Boolean variable                                                                  | bool       |
| Switch             | Switch logical structute of variables, see thing configuration                    | switch     |
| Dimmer             | Dimmmer logical structute of variables, see thing configuration                   | dimmer     |
| Blind              | Blind logical structute of variables, see thing configuration                     | blind      |

## Discovery

Currently Auto-discovery is not available. However, it's planned feature in future releases.

## Binding Configuration

The binding requires no special configuration.

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should
be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

### PLCComS Bridge Thing

| parameter               | datatype | required            |
|-------------------------|----------|---------------------|
| hostname                | text     | yes                 |
| port                    | integer  | no (default: 5010)  |
| lowRefreshInterval      | integer  | no (default: 300)   |
| mediumRefreshInterval   | integer  | no (default: 60)    |
| highRefreshInterval     | integer  | no (default: 15)    |
| realtimeRefreshInterval | integer  | no (default: 1)     |

There are four refresh groups: LOW, MEDIUM, HIGH and REALTIME. Each thing connected to bridge which needs to update
state have to decide one of four refresh group. The refresh interval of group is specified in seconds.

A possible entry in your thing file could be:

```
Bridge foxtrot:plccoms:cp1000 [ hostname="192.168.0.20", highRefreshInterval=20 ]
```
 
### String variable Thing

The String thing must be connected through PLCComS bridge and has following parameters:

| parameter               | datatype | required            | description                 |
|-------------------------|----------|---------------------|-----------------------------|
| var                     | text     | yes                 | Variable name from PUB file |
| refreshGroup            | text     | no (default: LOW)   | How often will be refreshed |

A possible entry in your thing file could be:

```
Thing string WindDirection [ var="Meteo.WindDirection", refreshGroup="MEDIUM" ]
```

### Number variable Thing

The Number thing must be connected through PLCComS bridge and has following parameters:

| parameter               | datatype | required            | description                 |
|-------------------------|----------|---------------------|-----------------------------|
| var                     | text     | yes                 | Variable name from PUB file |
| refreshGroup            | text     | no (default: LOW)   | How often will be refreshed |

A possible entry in your thing file could be:

```
Thing number OfficeTemp [ var="OfficeRoom.Temp", refreshGroup="HIGH" ]
```

### Bool variable Thing

The Bool thing must be connected through PLCComS bridge and has following parameters:

| parameter               | datatype | required            | description                 |
|-------------------------|----------|---------------------|-----------------------------|
| var                     | text     | yes                 | Variable name from PUB file |
| refreshGroup            | text     | no (default: LOW)   | How often will be refreshed |

A possible entry in your thing file could be:

```
Thing bool IsNight [ var="IsNight" ]
```

### Switch Thing

The Switch Thing represent specific structure of variables and controll code in PLC user program. Here is snippet
in ST specific language, this is for controlling light:
```
TYPE
  TLightState : struct
    IsOn {PUBLIC}: bool;
    Timeout: time := T#0s;
    IsNightLight: bool := false;
  end_struct;

  TLightCmd : struct
    OnCmd {PUBLIC}: bool;
    OffCmd {PUBLIC}: bool;
  end_struct;
END_TYPE
  
VAR_GLOBAL
  GarageLight: TLightCmd;
END_VAR

VAR_GLOBAL RETAIN
  GarageLightState: TLightState;
END_VAR
``` 

The Switch thing must be connected through PLCComS bridge and has following parameters:

| parameter               | datatype | required            | description                 |
|-------------------------|----------|---------------------|-----------------------------|
| state                   | text     | yes                 | Variable name that holds switch state (0 or 1) |
| on                      | text     | yes                 | Bool variable that trigger switch on action on rising edge (0 -> 1) |
| off                     | text     | yes                 | Bool variable that trigger switch off action on rising edge (0 -> 1) |
| refreshGroup            | text     | no (default: LOW)   | How often will be refreshed |

A possible entry in your thing file could be:

```
Thing switch GarageLight [ state="GarageLightState.IsOn", on="GarageLight.OnCmd", off="GarageLight.OffCmd" ]
```
### Dimmer Thing

`todo`

### Blind Thing

`todo`

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
