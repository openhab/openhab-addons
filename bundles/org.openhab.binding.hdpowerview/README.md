# Hunter Douglas PowerView Binding

This is an openHAB binding for the [Hunter Douglas PowerView Motorized Shades](https://www.hunterdouglas.com/operating-systems/motorized/powerview-motorization/overview) via the PowerView Hub.

PowerView shades have motorization control for their vertical position, as well as vane controls on the shade's slats.
Make sure your Shades are visible in the dedicated PowerView app before attempting discovery.
This binding also supports Scenes that are defined via the PowerView app.
This helps to work around a limitation of the Hub - commands are executed serially with a several second delay between executions.
By using a Scene to control multiple shades at once, the shades will all begin moving at the same time.

## Supported Things

| Thing           | Thing Type | Description                                                                                                                                          |
|-----------------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| PowerView Hub   | Bridge     | The PowerView Hub provides the interface between your network and the shade's radio network. It also contains channels used to interact with scenes. |
| PowerView Shade | Thing      | A single motorized shade                                                                                                                             |

## Discovery

The PowerView Hub is discovered via a NetBIOS query.
This is the same method used by the dedicated PowerView app.
After the Hub is added, Shades and Scenes will be discovered by querying the Hub.

## Thing Configuration

PowerView things should be configured via discovery - it would be difficult to configure manually as the IDs of the shades and scenes are not exposed via the dedicated app.
However, the configuration parameters are described below:

<table>
 <tr>
  <td><b>Thing</b></td>
  <td><b>Configuration Parameters</b></td>
 </tr>
 <tr>
  <td>PowerView Hub</td>
  <td>
   <table>
    <tr><td><b>host</b> - the hostname or IP address of the Hub on your network.</td></tr>
    <tr><td><b>refresh</b> - the number of milliseconds between fetches of the PowerView Hub's shade state. Defaults to 60,000 (one minute).</td></tr>
   </table>
  </td>
 </tr>
 <tr>
  <td>PowerView Shade</td>
  <td>
   <table>
    <tr><td><b>id</b> - the ID of the PowerView Shade on the Hub.</td></tr>
   </table>
  </td>
 </tr>
</table>

## Channels

### PowerView Shade

| Channel    | Item Type     | Description                                                                                                                                                 |
|------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| position   | Rollershutter | The vertical position of the shade. Up/Down commands will move the shade to its completely up or completely down position. Move/Stop commands are ignored.  |
| secondary  | Rollershutter | The secondary vertical position of the shade. This channel only applies in the case of shades with combined top-down and bottom-up motors. The function is the sames as the (proimary) position channel above. |
| vane       | Dimmer        | The amount the slats on the shade are open. Setting this value will completely close the shade first, as the slats can only be controlled in that position. |
| batteryLow | Switch        | Indicates ON when the battery level of the shade is low, as determined by the Hub's internal rules                                                          |

### PowerView Scene

Scenes channels are added to the Hub as they are discovered.

| Channel  | Item Type | Description                                                                                                                                                                                                         |
|----------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| activate | Switch    | Turning this to ON will activate the scene. Scenes are stateless in the PowerView hub - they have no on/off state. Include { autoupdate="false" } on your item configuration to avoid needing to toggle off and on. |

## Transient Hub State and Manual Operation

The Hub maintains transient shade state such as position and battery level, so a call by this binding  that returns the shade position and/or the battery level is delivering the last Hub saved value of these attributes.
In general, shades are moved via binding calls to Shades and Scenes. and since  the Hub is always involved in these motion events, it tracks the final shade position and saves it.
However, shades can be moved without the Hub’s knowledge.
An individual can manually move a shade simply by pressing the motion button on the side of the shade.
In addition, shades can be moved using a PowerView Motorization handheld remote control device.
In both of these cases, the Hub is not told of the shade’s new position.

If you want to force the Hub to refresh its state after using such manual control commands, then you can send an Item `Refresh` command in a rule, as follows:

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
    Thing shade s50150 "Living Room Shade 1" @ "Living Room" [id="50150"]
}
```

### `demo.items` File
```
Rollershutter Living_Room_Shade_Position "Living Room Shade Position [%.0f %%]" <blinds> (g_Living_Room_Shades_Position) {channel="hdpowerview:shade:g24:s50150:position"}

Rollershutter Living_Room_Shade_Secondary "Living Room Shade Secondary Position [%.0f %%]" <blinds> (g_Living_Room_Shades_Position) {channel="hdpowerview:shade:g24:s50150:secondary"}

Dimmer Living_Room_Shade_Vane "Living Room Shade Vane [%.0f %%]" <flow> {channel="hdpowerview:shade:g24:s50150:vane"}

Switch Living_Room_Shade_Battery_Low_Alarm "Living Room Shade Battery Low Alarm [MAP(24g-battery.map):%s]" <battery> (g_Battery_Low_Alarm) {channel="hdpowerview:shade:g24:s50150:lowBattery"}

Switch Living_Room_Shades_Scene_Heart "Living Room Shades Scene Heart" <blinds> (g_Shades_Scene_Trigger) {channel="hdpowerview:hub:g24:22663", autoupdate="false"}
```
### `demo.sitemap` File
```
Frame label="Living Room Shades" {
  Switch item=Living_Room_Shades_Scene_Open
  Slider item=Living_Room_Shade_1_Position 
}
```