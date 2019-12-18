# Wiz Lighting Binding

This binding integrates the Wiz Color Lighting devices .

## Supported Things

- WiZ Connected Lights Color A E26 Light Kit 


## Discovery

The devices are discovered only once when the app is installed for the first time. Once the app is configured it allows creating additional invite codes that allow installation of the app on other ios or android devices. In order to start auto discovery, create a file named wizlighting.token in Userdata folder with the 7 digit invite token and initiate discovery. If this invite code is provided, we mimic the flow of an ios device to obtain list of devices from the wiz world api end point. 

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

The Binding support the following channel:

| Channel Type ID | Item Type | Description                                          | Access |
|-----------------|-----------|------------------------------------------------------|--------|
| switch          | Switch    | Power state of the Bulb (ON/OFF)                     | R/W    |
| color           | Color     | Color of the RGB LEDs                                | R/W    |
| white           | Dimmer    | Brightness of the first (warm) white LEDs (min=0, max=100) | R/W    |
| white2          | Dimmer    | Brightness of the second (warm) white LEDs (min=0, max=100) | R/W    |
| scene           | String    | Program to run by the controller (i.e. color cross fade, strobe, etc.) | R/W |
| speed           | Dimmer    | Speed of the program                                 | R/W    |

