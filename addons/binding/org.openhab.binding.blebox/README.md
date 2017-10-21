# Blebox Binding

This binding integrates the [Blebox] devices (http://blebox.eu)

## Supported Things

Supported wireless devices:

|  Device   | Description                                  | Thing Type |
|-----------|----------------------------------------------|------------|
|switchBox  | On/Off 230v switch                           | switchBox  |
|switchBoxD | Double On/Off 230v switch                    | switchBoxD |
|wLightBox  | LED/LED RGBW lighting controller             | wLightBox  |
|wLightBoxS | Single color LED dimmer                      | wLightBoxS |
|dimmerBox  | On/Off, dim 230v lights                      | dimmerBox  |
|gateBox    | Open/Close gates, doors                      | gateBox    |


## Discovery

Devices are discovered automatically in the local network. Binding searches our local IP range, and puts discovered devices in the Inbox.

## Binding Configuration
 
The binding has no configuration options, all configuration is done at Thing level.
 
## Thing Configuration

The thing has a few configuration parameters:

|    Parameter     | Description                                                |
|------------------|------------------------------------------------------------|
| ip               | IP of the device in local network.                         |
| pollingInterval  | Refresh interval in seconds. Default value is 10 seconds.  |



## Channels


| Channel ID | Supported by devices              | Item Type     | Description                                                                                               |
|------------|----------------------------------|---------------|---------------------------------------------------------------------------------------------------------- |
| color      | wLightBox                        | Color         | Allows to control the color of a light. It is also possible to dim values and switch the light on and off |
| brightness | wLightBox, wLightBoxS, dimmerBox | Dimmer        | Allows to control the brightness of a light. It is also possible to switch the light on and off           |
| switch     | switchBox, switchBoxD            | Switch        | Allows to switch the light on and off                                                                     |
| position   | gateBox                          | Rollershutter | Allows to control position of electric roller shutters                                                    |




## Full Example

blebox.things:

```
blebox:dimmerBox:kitchen "Kitchen light" @ "" [ ip="192.168.1.214", pollingInterval=10 ]
```

blebox.items:

```
Group Blebox <flow>

Dimmer   KitchenLight              "Kitchen light" <line> (Blebox) { channel="blebox:dimmerBox:kitchen:brightness" }
```
