# Hunter Douglas (Luxaflex) PowerView Binding

This is an openHAB binding for [Hunter Douglas PowerView](https://www.hunterdouglas.com/operating-systems/motorized/powerview-motorization/overview) motorized shades via their PowerView hub.
In some countries the PowerView system is sold under the brand name [Luxaflex](https://www.luxaflex.com/)

![PowerView](doc/hdpowerview.png)

PowerView shades have motorization control for their vertical position, as well as vane controls on the shade's slats.

This binding also supports scenes that are defined in the PowerView app.
This helps to work around a limitation of the hub; commands are executed serially with a several second delay between executions.
By using a scene to control multiple shades at once, the shades will all begin moving at the same time.

## Supported Things

| Thing           | Thing Type | Description        |
|-----------------|------------|--------------------|
| PowerView Hub   | Bridge     | The PowerView hub provides the interface between your network and the shade's radio network. It also contains channels used to interact with scenes. |
| PowerView Shade | Thing      | A motorized shade. |

## Discovery

Make sure your shades are visible in the PowerView app before attempting discovery.

The binding can automatically discover the PowerView hub.
The discovery process can be started by pressing the refresh button in the PaperUI Inbox.
However you can also manually create a (bridge) thing for the hub, and enter the required Configuration Parameters (see Thing Configuration below).
If the Configuration Parameters are all valid, the binding will then automatically attempt to connect to the hub.
If the connection succeeds, the hub will indicate its status as Online, otherwise it will show an error status. 

Once the hub thing has been created and successfully connected, the binding will automatically interrogate the hub to discover all shades and scenes that are in it.

- For each shade discovered: the binding will create a new dedicated thing with its own channels.
- For each scene discovered: the binding will create a new channel dynamically within the hub thing.

If in the future, you add additional shades or scenes to your system, the binding will discover them too.

## Thing Configuration

### Thing Configuration for PowerView Hub

| Configuration Parameter | Description   |
|-------------------------|---------------|
| host                    | The host name or IP address of the hub on your network. |
| refresh                 | The number of milli-seconds between fetches of the PowerView hub's shade state. Default value: 60,000 (one minute). |

### Thing Configuration for PowerView Shades

PowerView shades should preferably be configured via the automatic discovery process.
It is quite difficult to configure manually as the `id` of the shade is not exposed in the PowerView app.
However, the configuration parameters are described below:

| Configuration Parameter | Description                    |
|-------------------------|--------------------------------|
| id                      | The ID of the PowerView shade. |

## Channels

### Channels for PowerView Hub

The hub always has one fixed channel as below:

| Channel  | Item Type | Description |
|----------|-----------| ------------|
| refresh  | Switch    | See "Refreshing the PowerView Hub Cache" below. If you switch on this channel, it will make the hub scan all shades and update its cache state. Include `{autoupdate="false"}` in your item configuration to avoid needing to toggle off and on. |

In addition, scene channels will be added dynamically to the binding as they are discovered in the hub.
Each scene channel will have an entry in the hub as shown below, whereby different scenes have different `id` values:

| Channel  | Item Type | Description |
|----------|-----------| ------------|
| id | Switch | Turning this to ON will activate the scene. Scenes are stateless in the PowerView hub; they have no on/off state. Include `{autoupdate="false"}` in your item configuration to avoid needing to toggle off and on. |

### Channels for PowerView Shade

A shade always implements a roller shutter channel `position` which controls the main vertical position of the shade.
If the shade has rotatable vanes it will also have a dimmer channel `vane` which controls the vane angle.
If the shade is a combined top-down / bottom-up shade then it will also have a roller shutter channel `secondary` which controls the position of the secondary rail.
All of these channels are implemented in the binding, but if it has no physical implementation on the shade, then the respective channels have no purpose.

| Channel    | Item Type     | Description                                                                                                                                                 |
|------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| position   | Rollershutter | The vertical position of the shade. Up/Down commands will move the shade to its completely up or completely down position. Move/Stop commands are ignored.  |
| secondary  | Rollershutter | The secondary vertical position of the shade. This channel only applies in the case of shades with combined top-down and bottom-up motors. The function is identical to the (primary) position channel above. |
| vane       | Dimmer        | The amount the slats on the shade are open. Setting this value will completely close the shade first, as the slats can only be controlled in that position. |
| batteryLow | Switch        | Indicates ON when the battery level of the shade is low, as determined by the hub's internal rules. |

### Refreshing the PowerView Hub Cache

The hub maintains a cache of the last known state of its shades, and this binding delivers those values.
Usually the shades will moved by this binding, so since the hub is always involved in the process, it can update its cache accordingly.

However  shades can also be moved manually without the hub’s knowledge.
A person can manually move a shade by pressing a button on the side of the shade or via a remote control.
In neither case will the hub be aware of the shade’s new position.

So the hub implements the `refresh` Switch type channel (see above) in order to overcome this issue.

Note: You can also force the hub to refresh itself by sending an item `Refresh` command in a rule, as follows:

```
import org.eclipse.smarthome.core.types.RefreshType

sendCommand(ITEM_NAME, RefreshType.REFRESH)
```

or

```
sendCommand(ITEM_NAME, "REFRESH")
```

## Full Example

### `demo.things` File

```
Bridge hdpowerview:hub:g24 "Luxaflex Hub" @ "Living Room" [host="192.168.1.123"] {
    Thing shade s50150 "Living Room Shade" @ "Living Room" [id="50150"]
}
```

### `demo.items` File

Shade items:

```
Rollershutter Living_Room_Shade_Position "Living Room Shade Position [%.0f %%]" {channel="hdpowerview:shade:g24:s50150:position"}

Rollershutter Living_Room_Shade_Secondary "Living Room Shade Secondary Position [%.0f %%]" {channel="hdpowerview:shade:g24:s50150:secondary"}

Dimmer Living_Room_Shade_Vane "Living Room Shade Vane [%.0f %%]" {channel="hdpowerview:shade:g24:s50150:vane"}

Switch Living_Room_Shade_Battery_Low_Alarm "Living Room Shade Battery Low Alarm [%s]" {channel="hdpowerview:shade:g24:s50150:lowBattery"}
```

Scene items:

```
Switch Living_Room_Shades_Scene_Heart "Living Room Shades Scene Heart" <blinds> (g_Shades_Scene_Trigger) {channel="hdpowerview:hub:g24:22663", autoupdate="false"}
```

### `demo.sitemap` File

```
Frame label="Living Room Shades" {
  Switch item=Living_Room_Shades_Scene_Open
  Slider item=Living_Room_Shade_1_Position 
}
```
