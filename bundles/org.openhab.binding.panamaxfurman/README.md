
# PanamaxFurman Binding

Binding for Panamax/Furman power conditioners which have the BlueBOLT-CV1 or BlueBOLT-CV2 interface card installed.

## Supported Things

- `powerconditioner`: Accesses the Panamax/Furman power conditioner.

## Discovery

Discovery is not supported by the Panamax/Furman power conditioner devices.

## Binding Configuration

Configuration is done at the Thing level.

## Thing Configuration

### `powerconditioner` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| address         | text    | Hostname or IP address of the device  | N/A     | yes      | no       |
| telnetPort      | integer | The telnet port of the device         | 23      | no       | yes      |


## Channels

| Channel            | Type   | Read/Write | Description                           |
|--------------------|--------|------------|---------------------------------------|
| outlet-1#power        | Switch | RW         | Turn the power on/off for each outlet |

*note: outlet number can be [1-8].


## Full Example

_powerconditioner.things:_

```java
Thing panamaxfurman:powerconditioner:avpower "Power Conditioner" @ "Living Room" [ address="192.168.1.100"]
```

_powerconditioner.items:_

```java
Switch Plug_PowerConditioner_Outlet_1 "AV DAC Power" {channel="panamaxfurman:powerconditioner:avpower:outlet-1#power"}
Switch Plug_PowerConditioner_Outlet_2 "AV SOtM Power" {channel="panamaxfurman:powerconditioner:avpower:outlet-2#power"}
// and so on for the other outlets
```

_example.sitemap_

```perl
Switch item=Plug_PowerConditioner_Outlet_1
Switch item=Plug_PowerConditioner_Outlet_2
```

## Notes

The Panamax/Furamn Power Conditioner devices actually support 3 different protocols:
| Interface | Supported by Binding? | Interface Card               |
|-----------|-----------------------|------------------------------|
| telnet    | Y                     | BlueBOLT-CV1 or BlueBOLT-CV2 |
| HTTP      | N                     | BlueBOLT-CV1 or BlueBOLT-CV2 |
| RS-232    | N *                   | Bluebolt RS 232              |

*While the RS-232 interface is not currently supported, it should be trivial to implement given the similarities to the Telnet inteface

The Telnet interface was chosen for implementation due to the following:

* Telnet interface supports "push" notifications from the device which provide instant and more reliable status information as opposed to repetitive polling of state as required by the HTTP interface
* The telnet API is very similar to the RS-232 API so by implementing the telnet interface, most of the work is already done for RS-232
