# Chamberlain MyQ Binding

[Chamberlain MyQ](http://www.chamberlain.com/smartphone-control-products/myq-smartphone-control) system allows you to connect your garage door to the internet to be controlled from anywhere using your smartphone. Using this API, The Chamberlain MyQ Binding can get the status of your garage door opener and send commands to open or close it.

## Supported Things

```
MyQ Gateway Thing Bridge - Gateway to MyQ Online API, Must be configured to use binding.
MyQ Garage Door Thing - Standard Chamberlain Garage Door.
MyQ Light Switch Thing - Chamberlain MyQ Controlled Light Switch or Plugin Module.
```

## Discovery

Gararge Doors and Light Modules Things will be auto Discovery after a MYQ Gateway Thing is added manually.


## Thing Configuration

The Chamberlain MyQ Gateway Thing only requires your Chamberlain MyQ Username and Password.

|   Property   | Default | Required | Description |
|--------------|---------|:--------:|-------------|
| username     |         |   Yes    | Chamberlain MyQ Username |
| password     |         |   Yes    | Chamberlain MyQ Password |
| refresh      | 60      |   No     | Data refresh interval in seconds |
| quickrefresh | 2       |   No     | Data refresh interval after Event is trigger in seconds |
| timeout      | 25      |   No     | Timeout for HTTP requests in seconds |

## Channels
```
Door State - Garage Door Open/Close State.
Roller State - Garage Door Roller Shutter State.
Door Status - Garage Door Status as a string.
Door Open - Garage Door Is Open Contact.
Door Closed - Garage Door Is Closed Contact.
Light State - Light On/Off State.
Serial Number - MyQ Device Serial Number.
Description - MyQ Device Description, Should Match Device User specified Name in the MyQ app.
```

## Examples

### Items

```
Switch LampModule               "Lamp Module On"                {channel="chamberlainmyq:MyQLight:344bf0bc:108573427:lightstate"}
String LampModuleDesc           "Lamp Module Desc [%s]"         {channel="chamberlainmyq:MyQLight:344bf0bc:108573427:description"}
String LampModuleSerialNumber   "Lamp Module SerialNumber [%s]" {channel="chamberlainmyq:MyQLight:344bf0bc:108573427:serialnumber"}

Switch LightSwitch               "Light Switch On"                {channel="chamberlainmyq:MyQLight:344bf0bc:20384288:lightstate"}
String LightSwitchDesc           "Light Switch Desc [%s]"         {channel="chamberlainmyq:MyQLight:344bf0bc:20384288:description"}
String LightSwitchSerialNumber   "Light Switch SerialNumber [%s]" {channel="chamberlainmyq:MyQLight:344bf0bc:20384288:serialnumber"}

Switch GarageDoorSwitch         "Garage Door Open"              {channel="chamberlainmyq:MyQDoorOpener:344bf0bc:1810905:doorstate"}
String GarageDoorString         "Garage Door [%s]"              {channel="chamberlainmyq:MyQDoorOpener:344bf0bc:1810905:doorstatus"}
Rollershutter GarageDoorShutter "Garage Door Open"              {channel="chamberlainmyq:MyQDoorOpener:344bf0bc:1810905:rollerstate"}
Contact GarageDoorOpenContact   "Garage Door Open [%s]"         {channel="chamberlainmyq:MyQDoorOpener:344bf0bc:1810905:dooropen"}
Contact GarageDoorClosedContact "Garage Door Closed [%s]"       {channel="chamberlainmyq:MyQDoorOpener:344bf0bc:1810905:doorclosed"}
String GarageDoorDesc           "Garage Door Desc [%s]"         {channel="chamberlainmyq:MyQDoorOpener:344bf0bc:1810905:description"}
String GarageDoorSerialNumber   "Garage Door SerialNumber [%s]" {channel="chamberlainmyq:MyQDoorOpener:344bf0bc:1810905:serialnumber"}
```

### Sitemap

```
Switch item=LampModule
Text item=LampModuleDesc
Text item=LampModuleSerialNumber

Switch item=LightSwitch
Text item=LightSwitchDesc
Text item=LightSwitchSerialNumber

Switch item=GarageDoorSwitch
Text item=GarageDoorString
Switch item=GarageDoorShutter
Text item=GarageDoorOpenContact
Text item=GarageDoorClosedContact
Text item=GarageDoorDesc
Text item=GarageDoorSerialNumber
```

## Known Working Hardware

| Model     | Name |
|-----------|------|
| HD950WF   | Chamberlain 1.25 hps Wi-Fi Garage Door Opener |
| 825LM     | Liftmaster 825LM Remote Light Module |
| 823LM     | Liftmaster 823LM Remote Light Switch |