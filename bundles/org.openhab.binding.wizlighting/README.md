# Wiz Lighting Binding

This binding integrates the Wiz Color Lighting devices .

## Supported Things

- WiZ Connected Lights Color A E26 Light Kit


## Discovery

Auto-discovery is currently not supported.  All bulbs/plugs/switches must be added manually.

## Binding Configuration

The binding does not require any special configuration. The device should be connected to the same network.

## Thing Configuration

To configure a device manually we need its ip address, mac address and homeId. These can be found in the ios or android app.

Wifi Socket thing parameters:

| Parameter ID | Parameter Type | Mandatory | Description | Default |
|--------------|----------------|------|------------------|-----|
| macAddress | text | true | The bulb MAC address |  |
| ipAddress | text | true | The bulb Ip address |  |
| homeId | text | true | The homeId the bulb belongs to |  |
| updateInterval | integer | false | Update time interval in seconds to request the status of the bulb. | 60 |


E.g.

```
Thing wizlighting:wizBulb:lamp [ macAddress="accf23343c50", ipAddress="192.168.0.183", homeId=18529 ]
```

## Channels

The Binding supports the following channel:

| Channel Type ID | Item Type | Description                                          | Access |
|-----------------|-----------|------------------------------------------------------|--------|
| color           | Color     | State, intensity, and color of the LEDs              | R/W    |
| scene           | String    | Preset light mode name to run on the bulb            | R/W    |
| speed           | Dimmer    | Speed of the color changes in dynamic light modes    | R/W    |
