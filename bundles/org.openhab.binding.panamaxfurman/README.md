# PanamaxFurman Binding

Binding for Panamax/Furman power conditioners which have the BlueBOLT-CV1 or BlueBOLT-CV2 interface card installed.

## Supported Things

- `telnet`: Accesses the telnet interface of the BlueBOLT-CV1 or BlueBOLT-CV2 interface card

## Discovery

Discovery is not supported by the Panamax/Furman power conditioner devices.

## Binding Configuration

No configuration required

## Thing Configuration

### `telnet` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| address        | text    | Hostname or IP address of the device  | N/A     | yes      | no       |


## Channels

| Channel | Type   | Read/Write | Description                 |
|---------|--------|------------|-----------------------------|
| Brand | String | R         | Brand of the power conditioner|
| Model | String | R         | Model of the power conditioner|
| Firmware Version | String | R         | Firmware version of the power conditioner|
| Outlet Power [1-8] | Switch | RW         | Turn the power on/off for each outlet |


## Full Example

demo.things:

```
Thing panamaxfurman:telnet:avpowerconditioner "Power Conditioner" @ "Living Room" [ address="192.168.1.100"]
```

demo.items:

```
String AV_PowerConditioner_Brand "AV Power Conditioner Brand" {channel="panamaxfurman:telnet:avpowerconditioner:powerConditionerInfo#brandInfo"}
String AV_PowerConditioner_Model "AV Power Conditioner Model" {channel="panamaxfurman:telnet:avpowerconditioner:powerConditionerInfo#modelInfo"}
String AV_PowerConditioner_Firmware "AV Power Conditioner Firmware" {channel="panamaxfurman:telnet:avpowerconditioner:powerConditionerInfo#firmwareVersionInfo"}
Switch Plug_PowerConditioner_Outlet_1 "AV DAC Power" {channel="panamaxfurman:telnet:avpowerconditioner:outlet1#power"}
Switch Plug_PowerConditioner_Outlet_2 "AV SOtM Power" {channel="panamaxfurman:telnet:avpowerconditioner:outlet2#power"}
// and so on for the other outlets
```

## Notes

The Panamax/Furamn Power Conditioner devices actually support 3 different protocols:
| Interface | Supported?   | Interface Card                  |
|---------|--------|------------|
| telnet | Y |  BlueBOLT-CV1 or BlueBOLT-CV2        | 
| HTTP | N | BlueBOLT-CV1 or BlueBOLT-CV2       | 
| RS-232  | N * |Bluebolt RS 232  | R         | 
 *While the RS-232 interface is not currently supported, it should be trivial to implement given the similarities to the Telnet inteface
 
The Telnet interface was chosen for implementation due to the following:

* Telnet interface supports "push" notifications from the device which provide instant and more reliable status information as opposed to repetitive polling of state as required by the  HTTP interface
* The telnet API is very similar to the RS-232 API so by implmenting the telnet interface, most of the work is already done for RS-232
