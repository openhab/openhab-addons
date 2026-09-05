# AutoBlind Binding

This binding integrates Norman ShadeAuto motorized shades (manufactured by Nien Made) with openHAB.
It communicates with the ShadeAuto hub over the local network using the hub's HTTP API on port 10123.
No cloud connection is required.

## Supported Things

| Thing | Thing Type | Description                                          |
| ----- | ---------- | ---------------------------------------------------- |
| hub   | Bridge     | The ShadeAuto hub that connects to shades via RF.    |
| shade | Thing      | An individual motorized shade controlled by the hub. |

## Discovery

After adding the hub bridge manually, the binding will automatically discover all shades registered on the hub.
Discovery queries the hub's room and group hierarchy and creates an inbox entry for each shade, labeled with its name and location.

## Thing Configuration

### Hub Bridge Configuration

| Name            | Type    | Description                                 | Default | Required |
| --------------- | ------- | ------------------------------------------- | ------- | -------- |
| host            | text    | IP address or hostname of the ShadeAuto hub | N/A     | yes      |
| pollingInterval | integer | Interval in seconds between status polls    | 1800    | no       |

### Shade Thing Configuration

| Name           | Type    | Description                                                     | Default | Required |
| -------------- | ------- | --------------------------------------------------------------- | ------- | -------- |
| peripheralUid  | integer | Unique identifier of the shade on the hub                       | N/A     | yes      |
| invertPosition | boolean | Invert reported/commanded position (see Position Mapping below) | true    | no       |

Shade configuration is normally set automatically via discovery.

## Channels

### Shade Channels

| Channel ID    | Item Type            | Description                                            |
| ------------- | -------------------- | ------------------------------------------------------ |
| position      | Dimmer               | Shade position (0% = fully open, 100% = fully closed). |
| battery-level | Number:Dimensionless | Battery level of the shade as a percentage.            |
| low-battery   | Switch               | Indicates ON when the battery level is low.            |

### Position Mapping

The Norman ShadeAuto hub API uses 0 = closed and 100 = open.
By default (`invertPosition = true`) the binding converts this so that openHAB convention is
followed: 0% = open and 100% = closed. If your hub already reports position using the same
convention as openHAB, set `invertPosition = false` on the shade to disable the conversion.

## Thing Actions

The `hub` bridge exposes the following action:

| Action         | Description                                                             |
| -------------- | ----------------------------------------------------------------------- |
| forceRefresh() | Clear command suppression and poll the hub for current shade positions. |

Example, calling the action from a rule:

```java
val hubActions = getActions("autoblind", "autoblind:hub:myhub")
hubActions.forceRefresh()
```

## Full Example

### autoblind.things

```java
Bridge autoblind:hub:myhub "AutoBlind Hub" [ host="192.168.1.100", pollingInterval=1800 ] {
    Thing shade bedroom "Bedroom Shade" [ peripheralUid=1 ]
    Thing shade livingroom "Living Room Shade" [ peripheralUid=2 ]
}
```

### autoblind.items

```java
Dimmer               BedroomShade_Position     "Bedroom Shade [%d %%]"       { channel="autoblind:shade:myhub:bedroom:position" }
Number:Dimensionless BedroomShade_Battery      "Bedroom Battery [%d %%]"     { channel="autoblind:shade:myhub:bedroom:battery-level" }
Switch               BedroomShade_LowBattery   "Bedroom Low Battery"         { channel="autoblind:shade:myhub:bedroom:low-battery" }
```

### autoblind.sitemap

```perl
sitemap autoblind label="AutoBlind" {
    Frame label="Bedroom" {
        Slider item=BedroomShade_Position
        Text item=BedroomShade_Battery
        Text item=BedroomShade_LowBattery
    }
}
```

## Acknowledgments

API protocol knowledge informed by Kevin Lester's [ShadeAuto-HomeAssistant](https://github.com/kevinlester/ShadeAuto-HomeAssistant) integration.
