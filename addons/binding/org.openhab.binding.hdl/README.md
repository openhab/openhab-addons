# HDL Binding

This is the binding for HDL.
This binding allows you to integrate, view and control the HDL  items in the openHAB environment

## Supported Things

This binding support for now 9 different HDL items.
More will be added as the binding are expanded.
Thing names are using the article number that HDL are using.

| Thing         | Type      | Description                                                   |
|---------------|-----------|---------------------------------------------------------------|
| bridge        | Bridge    | This is the HDL LAN gateway (MBUS01IP)                    |
| MDT0601_233   | Thing     | HDL Dimmer 6x1A - Universal                                   |
| ML01          | Thing     | HDL logic module                                              |
| MPL8_48_FH    | Thing     | HDL Button Panel (DLP) with AC, Music, Clock, Floor Heating   |
| MPT04_48      | Thing     | Digital touch switch 4 buttons                                |
| MR1216_233    | Thing     | HDL Relay 12x16A                                              |
| MRDA06        | Thing     | HDL Ballast controller, 6 channels, 0-10V                     |
| MS08Mn_2C     | Thing     | HDL Sensor with 8 functions                                   |
| MS12_2C       | Thing     | HDL Sensor with 12 functions                                  |
| MS24          | Thing     | HDL with 24 dry contacts                                      |
| MW02          | Thing     | HDL Curtain controller for controlling off 3. parts curtains  |

## Discovery

Discovery has not been added yet, so all Things and Items need to be defined in things and items files.

## Binding Configuration

No binding wide settings.

## Thing Configuration

All things are identified by their Subnet and Device ID number, hence this is mandatory. 
The LAN (`bridge` thing) also requires the IP address and Port Number (6000) to be defined. 


## Channels

Depending on the thing it supports different Channels

DryContact(1-24)Status  means that that it can be 24 Dry Contact channels. What is available on that thing is shown under "Available on thing" for instead (1-2) means that channel DryContact1 and DryContact2 is available on that thing. If nothing is set all channels is available on that thing.

| Channel Type ID       | Item Type | Description                                               | Available on thing                |
|-----------------------|-----------|-----------------------------------------------------------|-----------------------------------|
| DimChannel(1-6)       | Dimmer    | This channel indicates the value of the dimmer.           |MDT0601_233,MRDA06                 |
| DryContact(1-24)Status| Contact   | This channel indicates the status of the dry contact.     |MS24,MS08Mn_2C(1-2),MS12_2C(1-2)   |
| RelayCh(1-12)         | Switch    | This channel indicates the value of the relay.            |MR1216233,MS12_2C(1-2)             |
| UVSwitch(1-240)       | Switch    | This channel indicates the value of the UV Switch.        |ML01(200-240)                      |
| Brightness            | Number    | This channel indicates the measured lumen.                |MS08Mn_2C,MS12_2C                  |
| MotionSensor          | Motion    | This channel indicates if there is any movement.          |MS08Mn_2C,MS12_2C                  |
| Sonic                 | Motion    | This channel indicates if there is any movement.          |MS12_2C                            |
| temperature           | Number    | This channel indicates the measured temperature (in °C).  |MPL8_48_FH,MS08Mn_2C,MS12_2C       |

## Full Example

Since auto discovery has not been added yet Things need to be defined manually. You need a `hdl:bridge` definition incl the right IP address of the HDL network item and its port number that should be 6000. 

hdl.things:

```
Bridge hdl:bridge:Setup [Ip="192.168.10.250", Port=6000]{
    Thing MRDA06 1020 [Subnet=1, DeviceID=20]
    Thing MRDA06 1021 [Subnet=1, DeviceID=21]
    Thing MRDA06 1022 [Subnet=1, DeviceID=22]
    Thing MDT0601_233 1023 [Subnet=1, DeviceID=23]
    Thing MDT0601_233 1024 [Subnet=1, DeviceID=24]
    Thing MR1216_233 1030 [Subnet=1, DeviceID=30]
    Thing MR1216_233 1031 [Subnet=1, DeviceID=31]
    Thing MR1216_233 1032 [Subnet=1, DeviceID=32]
    Thing MR1216_233 1033 [Subnet=1, DeviceID=33]
	Thing MW02_231 1038 [Subnet=1, DeviceID=38]
	Thing MS12_2C 1040 [Subnet=1, DeviceID=40, refreshInterval=5]
	Thing MS12_2C 1041 [Subnet=1, DeviceID=41, refreshInterval=5]
	Thing MS08Mn_2C 1050 [Subnet=1, DeviceID=50, refreshInterval=5]
	Thing MS08Mn_2C 1051 [Subnet=1, DeviceID=51, refreshInterval=5]
	Thing MPL8_48_FH 1070 [Subnet=1, DeviceID=70, refreshInterval=120]
	Thing MPL8_48_FH 1071 [Subnet=1, DeviceID=71, refreshInterval=120]
	Thing MPT04_48 1090 [Subnet=1, DeviceID=90]
	Thing MPT04_48 1093 [Subnet=1, DeviceID=93]
	Thing MS24 1100 [Subnet=1, DeviceID=100]
	Thing ML01 1101 [Subnet=1, DeviceID=101]
	Thing MPT04_48 1110 [Subnet=1, DeviceID=110]
}
```

hdl.items:

```
Dimmer  E2R1LD01        "Roof lights [%d %%]"                          			{channel="hdl:MDT0601_233:Setup:1023:DimChannel6"}
Number  E2R1DLP01       "Temperature [%.1f °C]"             <temperature>   	{channel="hdl:MPL8_48_FH:Setup:1082:temperature"}
Switch  E2R1ST01        "Sockets in room"                                       {channel="hdl:MR1216_233:Setup:1032:RelayCh12"}
Dimmer  E2R2LD01        "Roof lights [%d %%]"                                   {channel="hdl:MDT0601_233:Setup:1024:DimChannel1"}
Switch  E2R2ST01        "Sockets in room"                                       {channel="hdl:MR1216_233:Setup:1034:RelayCh5"}
Dimmer  E2R3LD01        "Roof lights [%d %%]"                                   {channel="hdl:MDT0601_233:Setup:1024:DimChannel2"}
Switch  E2R3ST01        "Sockets in room"                                       {channel="hdl:MR1216_233:Setup:1034:RelayCh6"}
Contact	E2R48i101C1		"8in1 DryContact1"										{channel="hdl:MS08Mn_2C:Setup:1050:DryContact1Status"}
Contact	E2R48i101C2		"8in1 DryContact2"										{channel="hdl:MS08Mn_2C:Setup:1050:DryContact2Status"}
Number	E2R48i101Br		"8in1 Brightness"					<sun>				{channel="hdl:MS08Mn_2C:Setup:1050:Brightness"}
Switch	E2R48i101Mo		"8in1 MotionSensor"					<motion>			{channel="hdl:MS08Mn_2C:Setup:1050:MotionSensor"}
Number	E2R512i101		"Temperature [%.1f °C]" 			<temperature>		{channel="hdl:MS12_2C:Setup:1043:temperature"}
Contact	E2R512i101C1	"12in1 DryContact1"										{channel="hdl:MS12_2C:Setup:1043:DryContact1Status"}
Contact	E2R512i101C2	"12in1 DryContact2"										{channel="hdl:MS12_2C:Setup:1043:DryContact2Status"}
Switch	E2R512i101Mo	"12in1 MotionSensor [%s]"			<motion>			{channel="hdl:MS12_2C:Setup:1043:MotionSensor"}
Switch	E2R512i101So	"12in1 Sonic [%s]"					<motion>			{channel="hdl:MS12_2C:Setup:1043:Sonic"}
Number	E2R512i101Br	"12in1 Brightness [%d Lux]"			<sun>				{channel="hdl:MS12_2C:Setup:1043:Brightness"}
Number	E2R5DLP01		"Temperature [%.1f °C]" 			<temperature>		{channel="hdl:MPL8_48_FH:Setup:1081:temperature"}
Number	E2R5DLP01CurTemp"Set Temperatur [%.1f °C]" 			<temperature>		{channel="hdl:MPL8_48_FH:Setup:1081:FHCurrentTempSet"}
String	E2R5DLP01FHM	"Heat Mode: [%s]" 										{channel="hdl:MPL8_48_FH:Setup:1081:FHMode"}
Rollershutter E2R5MW02	"Rollershutter [%s]"									{channel="hdl:MW02_231:Setup:1038:Shutter1Control"}
```

