# Universal Powerline Bus Binding

The UPB binding is used to enable communication with UPB devices. This binding requires the use of a UPB PIM or power-line modem. This binding has only been tested against simply automated devices.

## Supported Things

Switches (either ON/OFF or dimmers) are supported as are UPB links. Receptacles should also work.

## Discovery

No discovery is supported at this time. Devices must be added manually.

## Binding Configuration

This binding does not require any special configuration

## Thing Configuration

The bridge requires a serial port and network identifier to work.

```
Thing upb:bridge:upbmodem [ serialPort="/dev/ttyUSB0", network=33 ]
```

Each device or link requires the UPB identifier used in the UPB network. Links support a duplicateTimeout parameter which causes the binding to not send status updates for identical messages for a given time.

```
Thing upb:switch:bathroom (upb:bridge:upbmodem) [ id=2 ]
Thing upb:dimmer:livingroom (upb:bridge:upbmodem) [ id=1 ]
Thing upb:link:lamps (upb:bridge:upbmodem) [ id=22, duplicateTimeout=1500 ]
```

## Channels

The following channels are supported:

| Thing Type      | Channel Type ID   | Item Type    | Description                                  |
|-----------------|-------------------|--------------|--------------------------------------------- |
| dimmer          | brightness        | Dimmer       | Increase / decrease the brightness           |
| switch          | switch            | Switch       | ON / OFF status of the switch                |
| link            | brightness        | Dimmer       | Increase / decrease the brightness           |
