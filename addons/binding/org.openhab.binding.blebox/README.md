# Blebox Binding

This binding integrates the [Blebox] devices (http://blebox.eu)

## Supported Things

Supported wireless devices:

|  Device  | Description                                  | Thing Type |
|----------|----------------------------------------------|------------|
|switchBox | On/Off 230v switch                      | switchBox |
|switchBoxD| Double On/Off 230v switch              | switchBoxD |
|wLightBox | LED/LED RGBW lighting controller     | wLightBox |
|wLightBoxS| Single color LED dimmer                | wLightBoxS |
|dimmerBox | On/Off, dim 230v lights                | dimmerBox |
|gateBox   | Open/Close gates, doors                | gateBox |
|shutterBox | Roller shutters controller | Not supported |


## Discovery

Devices are discovered automatically in the local network. Binding searches our local IP range, and puts discovered devices in the Inbox.

## Binding Configuration
 
The binding has no configuration options, all configuration is done at Thing level.
 
## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                              |
|-----------|------------------------------------------------------------------------- |
| serialNumber  | Unique ID of the device. |
| ip  | IP of the device in local network. |
| pollingInterval   | Refresh interval in seconds. Default value is 10 seconds.  |



## Channels

All devices support some of the following channels:


| Channel ID | Item Type    | Description              |
|------------|--------------|------------------------- |
| color | Color | Allows to control the color of a light. It is also possible to dim values and switch the light on and off |
| brightness | Dimmer | Allows to control the brightness of a light. It is also possible to switch the light on and off |
| switch0 | Switch | Allows to switch the light on and off (Relay 0) |
| switch1 | Switch | Allows to switch the light on and off (Relay 1) |
| position | Rollershutter | Allows to control position of electric roller shutters|




## Full Example

blebox.things:

```
blebox:dimmerBox:kitchen "Kitchen light" @ "" [ ip="192.168.1.214", serialNumber="A021323A", pollingInterval=10 ]
```

blebox.items:

```
Group Blebox <flow>

Dimmer   KitchenLight              "Kitchen light" <line> (Blebox) { channel="blebox:dimmerBox:kitchen:brightness" }
```
