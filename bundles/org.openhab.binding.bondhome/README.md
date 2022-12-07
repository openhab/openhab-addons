# Bond Home Binding

This binding connects the [Bond Home](https://bondhome.io/) Bridge to openHAB using the [BOND V2 Local HTTP API](http://docs-local.appbond.com).
You'll need to acquire your [Local Token](http://docs-local.appbond.com/#section/Getting-Started/Getting-the-Bond-Token).
The easiest way is to open the Bond Home app on your mobile device, tap on your bridge device, open the Advanced Settings, and copy it from the Local Token entry.

## Supported Things

| Thing Type       | Description                                                       |
|------------------|-------------------------------------------------------------------|
| bondBridge       | The RF/IR/WiFi Bridge                                             |
| bondFan          | An RF or IR remote controlled ceiling fan with or without a light |
| bondFireplace    | An RF or IR remote controlled fireplace with or without a fan     |
| bondGenericThing | A generic RF or IR remote controlled device                       |
| bondShades       | An RF or IR remote controlled motorized shade                     |

## Discovery

Once the bridge has been added, individual devices will be auto-discovered and added to the inbox.

## Thing Configuration

### bondBridge

| Parameter          | Description                                                           | Required |
|--------------------|-----------------------------------------------------------------------|----------|
| bondId             | The Bond ID of the bridge from the Bond Home app.                     | Yes      |
| localToken         | The authentication token for the local API.                           | Yes      |
| bondIpAddress      | The exact IP address to connect to the Bond Hub on the local network  | No       |

## Channels

Not all channels will be available for every device.
They are dependent on how the device is configured in the Bond Home app.

### `common` Group

| Channel    | Type     | Description                                                     |
|------------|----------|-----------------------------------------------------------------|
| power      | Switch   | Device Power                                                    |
| command    | String   | Send a command to the device                                    |

Available commands:
| Command                   | Description                                       |
|---------------------------|---------------------------------------------------|
| STOP                      | Stop any in-progress dimming operation            |
| PRESET                    | Move a shade to a preset                          |
| DIM_START_STOP            | Dim the fan light (cyclically)                    |
| DIM_INCREASE              | Start increasing the brightness of the fan light  |
| DIM_DECREASE              | Start decreasing the brightness of the fan light  |
| UP_LIGHT_DIM_START_STOP   | Dim the fan light (cyclically)                    |
| UP_LIGHT_DIM_INCREASE     | Start increasing the brightness of the up light   |
| UP_LIGHT_DIM_DECREASE     | Start decreasing the brightness of the up light   |
| DOWN_LIGHT_DIM_START_STOP | Dim the fan light (cyclically)                    |
| DOWN_LIGHT_DIM_INCREASE   | Start increasing the brightness of the down light |
| DOWN_LIGHT_DIM_DECREASE   | Start decreasing the brightness of the down light |

### `fan` Group

| Channel           | Type     | Description                                       |
|-------------------|----------|---------------------------------------------------|
| power             | Switch   | Fan power (only applicable to fireplace fans)     |
| speed             | Dimmer   | Sets the fan speed. The 0-100% value will be scaled to however many speeds the fan actually has. Note that you cannot set the fan to speed 0 - you must turn `OFF` the power channel instead. |
| breezeState       | Switch   | Enables or disables breeze mode                   |
| breezeMean        | Dimmer   | Sets the average speed in breeze mode             |
| breezeVariability | Dimmer   | Sets the variability of the speed in breeze mode. |
| direction         | String   | Sets the fan direction - "Summer" or "Winter"     |
| timer             | Number   | Sets an automatic off timer for s seconds (turning on the fan if necessary) |

### `light`, `upLight`, `downLight` Groups

| Channel         | Type   | Description                                            |
|-----------------|--------|--------------------------------------------------------|
| power           | Switch | Turns the light on or off                              |
| brightness      | Dimmer | Adjusts the brightness of the light                    |

### `fireplace` Group

| Channel  | Type   | Description                            |
|----------|--------|----------------------------------------|
| flame    | Dimmer | Adjust the flame level                 |

### `shade` Group

| Channel       | Type          | Description                                      |
|---------------|---------------|--------------------------------------------------|
| rollershutter | Rollershutter | Only UP, DOWN, STOP, 0%, and 100% are supported. |

## Full Example

### `bond.things` File

```java
bondhome:bondBridge:BD123456 "Bond Bridge" [ ipAddress="192.168.0.10", localToken="abc123", serialNumber="BD123456" ]
bondhome:bondFan:BD123456:0d11f00 "Living Room Fan" (bondhome:bondBridge:BD123456) [ deviceId="0d11f00" ]
```

### `bond.items` File

```java
Switch GreatFan_Switch "Great Room Fan" { channel="bondhome:bondFan:BD123456:0d11f00:common#power" }
Dimmer GreatFan_Dimmer "Great Room Fan" { channel="bondhome:bondFan:BD123456:0d11f00:fan#speed" }
String GreatFan_Rotation "Great Room Fan Rotation" { channel="bondhome:bondFan:BD123456:0d11f00:fan#direction" }
Switch GreatFanLight_Switch "Great Room Fan Light" { channel="bondhome:bondFan:BD123456:0d11f00:light#power" }
```
