# Somfy myLink Binding

This binding supports the [Somfy myLink](https://www.somfysystems.com/en-us/products/1811403/mylink-) device for control of Somfy RTS blinds/shades.

## Supported Things

Currently these things are supported:

- bridge (Somfy MyLink bridge, which can discover roller shades and scenes)
- roller shades (UP, DOWN, STOP control of a roller shade).

This binding has been tested with myLink firmware 5.23.

## Discovery

To use auto discovery first setup a Somfy MyLink bridge, then supply its IP address and system id.
The system id can be found in the integration settings -> Control4 section of your My Link app.
Once the Somfy MyLink bridge is ONLINE you can start a scan that will detect and add to your Inbox any discovered roller shades.

## Thing Configuration

### mylink

| Parameter      | Parameter ID | Required/Optional | Description                                   |
| -------------- | ------------ | ----------------- | --------------------------------------------- |
| IP or Hostname | ipAddress    | Required          | Hostname or IP Address of the myLink device   |
| System Id      | systemId     | Required          | The system id configured on the myLink device |

### shade

| Parameter | Parameter ID | Required/Optional | Description                          |
| --------- | ------------ | ----------------- | ------------------------------------ |
| Target ID | targetId     | Required          | Address of shade in the Somfy system |

### scene

| Parameter | Parameter ID | Required/Optional | Description                          |
| --------- | ------------ | ----------------- | ------------------------------------ |
| Scene ID  | sceneId      | Required          | Address of scene in the Somfy system |

## Channels

The following channels are supported by the binding. Note that specific weather station models may support only some or all of these channels.

| Channel ID   | Item Type     | Description                                              |
| ------------ | ------------- | -------------------------------------------------------- |
| shadeControl | Rollershutter | Device control (UP, DOWN, STOP)                          |
| scenelist    | String        | Comma-separated list of scenes of form sceneId=sceneName |
| button       | Switch        | Button to trigger a scene or rule                        |

## Example

### Things

```java
Bridge somfymylink:mylink:mylink1 "myLink Bridge" @ "Office" [ ipAddress="192.168.1.1", systemId="mysystemidhere" ] {
    Thing shade shade1 "Living Room" [ targetId="CC114A21.1" ]
}
```
