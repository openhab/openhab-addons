# Sunsa Binding

Binding for Controlling Sunsa devices like the Sunsa wand.
See [Sunsa Website](https://www.sunsahomes.com/) for more details about the devices.
The cloud API is documented in [SwaggerHub](https://app.swaggerhub.com/apis/Sunsa/Sunsa).

## Supported Things

- `cloudBridge`: **Required** to communicate with cloud services.
- `device`: Sunsa device (e.g. Sunsa wand).

## Discovery

Discovery is supported when a cloud bridge is available and configured with the appropriate credentials.

## Thing Configuration

All devices require a bridge in order to work.
Currently a cloud based bridge is available and needs to be configured before adding other things.

### `cloudBridge` Thing Configuration

These required configuration can be retrieved from the Sunsa mobile apps.

1. Go to `Settings`
1. Go to `API Settings`
1. Ensure that Api Settings is toggled to `Active` in the mobile app.
1. Click `Generate New API Key`.
1. Copy over the User ID and the Api Key to the configuration.

| Name            | Type    | Description                                     | Default | Required | Advanced |
|-----------------|---------|-------------------------------------------------|---------|----------|----------|
| userId          | text    | Unique User ID assigned to the account          | N/A     | yes      | no       |
| apiKey          | text    | Required to authenticate with the cloud service | N/A     | yes      | no       |

### `device` Thing Configuration

| Name                       | Type    | Description                               | Default | Required | Advanced |
|----------------------------|---------|-------------------------------------------|---------|----------|----------|
| id                         | text    | Unique id of the device                   | N/A     | yes      | no       |
| configurablePositionClosed | integer | Used for the confiurable position channel | N/A     | no       | no       |
| configurablePositionOpen   | integer | Used for the confiurable position channel | N/A     | no       | no       |

## Channels

| Channel              | Type          | Read/Write | Description                 |
|----------------------|---------------|------------|-----------------------------|
| rawPosition          | Number        | RW         | Provides the raw position of the device `[-100, 100]`. `-100` is closed up, `0` is open, and `100` is closed down. This is the value used as the position in the cloud service. |
| configurablePosition | RollerShutter | RW         | RollerShutter requires a position value in the range `[0, 100]` so the raw position is not suitable to be used. This provides a configurable position range of the device and maps it to `[0, 100]`. Configuration is done as part of the `device` thing configuration. Rather than using the _raw_ position, this allows for greater flexibility of configuring the `Open` and `Closed` positions. For example, setting the `Closed` position to `-100` and the `Open` position to `0` will restrict the position to that range. Defaults to close in the up position (i.e. `-100`).
| batteryLevel         | Battery       | R          | Provides the battery level percentage of the device. |

## Full Example

sunsa.things:

```
Bridge sunsa:cloudBridge:mybridge [ userId="1234", apiKey="abcdef-12345-ghijkl" ] {
    Thing device mydevice1 [ id="0001", configurablePositionClosed=-100, configurablePositionOpen=0 ]
    Thing device mydevice2 [ id="0002" ]
}
```

sunsa.items:

```
Rollershutter Configurable_Position_Blinds "Configurable Position Blinds" <blinds> {channel="sunsa:device:mybridge:mydevice1:configurablePosition"}
Number Configurable_Position_Blinds_Battery "Configurable Position Blinds Battery" <battery> {channel="sunsa:device:mybridge:mydevice1:batteryLevel"}
Number Raw_Position_Blinds "Raw Position Blinds" <blinds> {channel="sunsa:device:mybridge:mydevice2:rawPosition"}
```

sunsa.sitemap:

```
sitemap sunsa label="Sunsa Example" {
    // Can use different item types including Slider, Rollershutter, Switch, etc.
    Slider icon="blinds" item=Configurable_Position_Blinds step=10 maxValue=100
    Default item=Configurable_Position_Blinds

    // This widget exposes different mappings for values of the raw position
    Switch icon="blinds" item=Raw_Position_Blinds mappings=[-100="Closed up",0="Open",100="Closed down"]
}
```
