# Hunter Douglas (Luxaflex) PowerView Binding

This is an openHAB binding for [Hunter Douglas PowerView](https://www.hunterdouglas.com/operating-systems/motorized/powerview-motorization/overview) motorized shades via their PowerView hub.
In some countries the PowerView system is sold under the brand name [Luxaflex](https://www.luxaflex.com/).

This binding supports hubs/gateways of all generations.
Hubs of Generation 1 or 2 are handled commonly and their generation specific features are identified with the *'Generation 1/2 only'* annotation and/or via the <sup>[1/2]</sup> mark.
Gateways of Generation 3 have generation specific features which are identified with the *'Generation 3 only'* annotation and/or via the <sup>[3]</sup> mark.
Features that are common to all generations are not annotated or marked.


![PowerView](doc/hdpowerview.png)

PowerView shades have motorization control for their vertical position, and some also have vane controls to change the angle of their slats.

This binding also supports scenes that are defined in the PowerView app.
This helps to work around a limitation of the hub; commands are executed serially with a several second delay between executions.
By using a scene to control multiple shades at once, the shades will all begin moving at the same time.

## Supported Things

| Thing    | Generation | Thing Type | Description                                                                                                                   |
|----------|------------|------------|-------------------------------------------------------------------------------------------------------------------------------|
| hub      | 1, 2       | Bridge     | The PowerView hub interfaces between the LAN and the shade's radio network. Contains channels to interact with scenes.        |
| shade    | 1, 2       | Thing      | A motorized shade.                                                                                                            |
| repeater | 1, 2       | Thing      | A PowerView signal repeater.                                                                                                  |
| gateway  | 3          | Bridge     | Generation 3 gateway interfaces between the LAN and the shade's Bluetooth network. Contains channels to interact with scenes. |
| shade3   | 3          | Thing      | A Powerview Generation 3 motorized shade.                                                                                     |

## Discovery

Make sure your shades are visible in the PowerView app before attempting discovery.

The binding can automatically discover PowerView hubs and gateways.
The discovery process can be started by pressing the refresh button in the Main Configuration UI Inbox.
However you can also manually create a (bridge) thing for the hub, and enter the required configuration parameters (see Thing Configuration below).
If the configuration parameters are all valid, the binding will then automatically attempt to connect to the hub/gateway.
If the connection succeeds, the hub will indicate its status as Online, otherwise it will show an error status.

Once the hub thing has been created and successfully connected, the binding will automatically discover all shades and scenes that are in it.

- For each shade discovered: the binding will create a new dedicated thing with its own channels.
- For each scene discovered: the binding will create a new channel dynamically within the hub/gateway thing.
- <sup>[1/2]</sup> For each repeater discovered: the binding will create a new dedicated thing with its own channels.
- <sup>[1/2]</sup> For each scene group discovered: the binding will create a new channel dynamically within the hub thing.
- <sup>[1/2]</sup> For each automation discovered: the binding will create a new channel dynamically within the hub thing.

<sup>[1/2]</sup> Generation 1/2 hubs only.

If in the future, you add additional shades, scenes, repeaters, scene groups or automations to your system, the binding will discover them too.

## Thing Configuration

### Thing Configuration for PowerView Hub / Gateway (Thing type `hub`<sup>[1/2]</sup> or `gateway`<sup>[3]</sup>)

| Configuration Parameter                 | Description                                                                                                                      |
|-----------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| host                                    | The host name or IP address of the hub on your network.                                                                          |
| refresh<sup>[1/2]</sup>                 | The number of milli-seconds between fetches of the PowerView hub's shade state (default 60'000 one minute).                      |
| hardRefresh                             | The number of minutes between hard refreshes of the PowerView hub's shade state (default 180 three hours). See [Hard Refreshes](#hard-refreshes).                                                                                                                                                           |
| hardRefreshBatteryLevel<sup>[1/2]</sup> | The number of hours between hard refreshes of battery levels from the PowerView Hub (or 0 to disable, defaulting to weekly). See [Hard Refreshes](#hard-refreshes).                                                                                                                                           |

<sup>[1/2]</sup> Generation 1/2 hubs only.

### Thing Configuration for PowerView Shades and Accessories<sup>[3]</sup>

PowerView shades and repeaters<sup>[3]</sup> should preferably be configured via the automatic discovery process.
However, for manual configuration of shades and repeaters, the console command `openhab:hdpowerview showIds` can be used to identify the IDs of all connected equipment.
This can be used for the `id` parameters described below.

#### Thing Configuration for PowerView Shades

| Configuration Parameter | Description                                                   |
|-------------------------|---------------------------------------------------------------|
| id                      | The ID of the PowerView shade in the app. Must be an integer. |

#### Thing Configuration for PowerView Repeaters<sup>[1/2]</sup>

| Configuration Parameter | Description                                                      |
|-------------------------|------------------------------------------------------------------|
| id                      | The ID of the PowerView repeater in the app. Must be an integer. |

## Channels

### Channels for Hub<sup>[1/2]</sup> (Thing type `hub`) or Gateway<sup>[3]</sup> (Thing type `gateway`)

Scene, scene group and automation channels will be added dynamically to the binding as they are discovered in the hub.
Each will have an entry in the hub as shown below, whereby different scenes, scene groups and automations have different `id` values:

| Channel Group               | Channel | Item Type | Description                                                                                                            |
|-----------------------------|---------|-----------|------------------------------------------------------------------------------------------------------------------------|
| scenes                      | id      | Switch    | Setting this to ON will activate the scene. Scenes are stateless in the PowerView hub; they have no on/off state.      |
| sceneGroups<sup>[1/2]</sup> | id      | Switch    | Setting this to ON will activate the scene group. Scene groups are stateless in the PowerView hub; they have no on/off state.                                                                                                                                                                       |
| automations<sup>[1/2]</sup> | id      | Switch    | Setting this to ON will enable the automation, while OFF will disable it.                                              |

<sup>[1/2]</sup> Generation 1/2 hubs only.

### Channels for Shades (Thing type `shade`<sup>[1/2]</sup> or `shade3`<sup>[3]</sup>)

A shade always implements a roller shutter channel `position` which controls the vertical position of the shade's (primary) rail.
If the shade has slats or rotatable vanes, there is also a dimmer channel `vane` which controls the slat / vane position.
If it is a dual action (top-down plus bottom-up) shade, there is also a roller shutter channel `secondary` which controls the vertical position of the secondary rail.
All of these channels appear in the binding, but only those which have a physical implementation in the shade, will have any physical effect.

| Channel                        | Item Type                | Description                                                                                                    |
|--------------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------|
| position                       | Rollershutter            | The vertical position of the shade's rail (if any). -- See [next chapter](#roller-shutter-updown-position-vs-openclose-state). Up/Down commands will move the rail completely up or completely down. Percentage commands will move the rail to an intermediate position. Stop commands will halt any current movement of the rail.                                                                                             |
| secondary                      | Rollershutter            | The vertical position of the secondary rail (if any). Its function is similar to the `position` channel above. -- But see [next chapter](#roller-shutter-updown-position-vs-openclose-state).                                                                                                  |
| vane                           | Dimmer                   | The degree of opening of the slats or vanes (if any). On some shade types, setting this to a non-zero value might first move the shade `position` fully down, since the slats or vanes can only have a defined state if the shade is in its down position. See [Interdependency between Channel positions](#interdependency-between-channel-positions).                                                                                                                                |
| command                        | String                   | Advanced: Send a command to the shade.                                                                         |
| lowBattery                     | Switch                   | Indicates ON when the battery level of the shade is low, as determined by the hub's internal rules.            |
| batteryLevel                   | Number                   | Battery level (10% = low, 50% = medium, 100% = high)                                                           |
| batteryVoltage<sup>[1/2]</sup> | Number:ElectricPotential | Battery (resp. mains power supply) voltage reported by the shade.                                              |
| signalStrength                 | Number / Number:Power    | Shade radio signal strength.                                                                                   |
| hubRssi<sup>[1/2]</sup>        | Number:Power             | Received Signal Strength Indicator for Hub.                                                                    |
| repeaterRssi<sup>[1/2]</sup>   | Number:Power             | Received Signal Strength Indicator for Repeater.                                                               |

Notes:

- The channels `position`, `secondary` and `vane` exist if the shade physically supports such channels.
- The shade's Power Option is set via the PowerView app with possible values 'Battery Wand', 'Rechargeable Battery Wand' or 'Hardwired Power Supply'.
The channels `lowBattery` and `batteryLevel` exist if you have _not_ selected 'Hardwired Power Supply' in the app.
- On Generation 1/2 hubs, the valid values for the `command` channel are: `CALIBRATE`, `IDENTIFY`.
On Generation 3 gateways, the only valid value is: `IDENTIFY`.
- On Generation 1/2 hubs the signal strength is 0 for no or unknown signal, 1 for weak, 2 for average, 3 for good, or 4 for excellent.
On Generation 3 gateways the signal strength is displayed in dBm (deciBel-milliWatt).
- <sup>[1/2]</sup> The RSSI values will only be updated upon manual request by a `REFRESH` command (e.g. in a rule).
- <sup>[1/2]</sup> Repeaters are supported on Generation 1/2 hubs only.

### Channels for Repeaters (Thing type `repeater`)<sup>[1/2]</sup>

| Channel         | Item Type | Description                                                                                 |
|-----------------|-----------|---------------------------------------------------------------------------------------------|
| color           | Color     | Controls the color of the LED ring. A switch item can be linked: ON = white, OFF = turn off |
| brightness      | Dimmer    | Controls the brightness of the LED ring.                                                    |
| identify        | String    | Flash repeater to identify. Valid values are: `IDENTIFY`                                    |
| blinkingEnabled | Switch    | Blink during commands.                                                                      |

### Roller Shutter Up/Down Position vs. Open/Close State

The `position` and `secondary` channels are Rollershutter types.
For vertical shades, the binding maps the vertical position of the "rail" to the Rollershutter ▲ / ▼ commands, and its respective percent value.
And for horizontal shades, it maps the horizontal position of the "truck" to the Rollershutter ▲ / ▼ commands, and its respective percent value.

Depending on whether the shade is a top-down, bottom-up, left-right, right-left, dual action shade, or, a shade with a secondary blackout panel, the `OPEN` and `CLOSED` position of the shades may differ from the ▲ / ▼ commands follows..

| Type of Shade               | Channel           | Rollershutter Command | Motion direction | Shade State    | Percent           | Pebble Remote Button                       |
|-----------------------------|-------------------|-----------------------|------------------|----------------|-------------------|--------------------------------------------|
| Single action<br>bottom-up  | `position`        | ▲                     | Up               | `OPEN`         | 0%                | ▲                                          |
|                             |                   | ▼                     | Down             | `CLOSED`       | 100%              | ▼                                          |
| Single action<br>top-down   | `position`        | ▲                     | Up               | **`CLOSED`**   | 0%                | ▲                                          |
|                             |                   | ▼                     | Down             | **`OPEN`**     | 100%              | ▼                                          |
| Single action<br>right-left | `position`        | ▲                     | _**Left**_       | `OPEN`         | 0%                | ▲                                          |
|                             |                   | ▼                     | _**Right**_      | `CLOSED`       | 100%              | ▼                                          |
| Single action<br>left-right | `position`        | ▲                     | _**Right**_      | `OPEN`         | 0%                | ▲                                          |
|                             |                   | ▼                     | _**Left**_       | `CLOSED`       | 100%              | ▼                                          |
| Dual action<br>(lower rail) | `position`        | ▲                     | Up               | `OPEN`         | 0%                | ▲                                          |
|                             |                   | ▼                     | Down             | `CLOSED`       | 100%              | ▼                                          |
| Dual action<br>(upper rail) | _**`secondary`**_ | ▲                     | Up               | **`CLOSED`** | 0%<sup>1)</sup>     | ![dual_action arrow_right](doc/right.png)  |
|                             |                   | ▼                     | Down             | **`OPEN`**   | 100%<sup>1)</sup>   | ![dual_action arrow_left](doc/left.png)    |
| Blackout panel ('DuoLite')  | _**`secondary`**_ | ▲                     | Up               | `OPEN`         | 0%                | ▲                                          |
|                             |                   | ▼                     | Down             | `CLOSED`       | 100%              | ▼                                          |

_**<sup>1)</sup> BUG NOTE**_: In openHAB versions v3.1.x and earlier, there was a bug in the handling of the position percent value of the `secondary` shade.
Although the RollerShutter Up/Down commands functioned properly as described in the table above, the percent state values (e.g. displayed on a slider control), did not.
After moving the shade, the percent value would initially display the correct value, but on the next refresh it would 'flip' to the **inverse** of the correct value.
The details are shown in the following table.
This bug has been fixed from openHAB v3.2.x (or later) —
_so if you have rules that depend on the percent value, and you update from an earlier openHAB version to v3.2.x (or later), you will need to modify them!_

| Channel     | UI Control Element | UI Control Command  | Immediate Action<br>on Shade State | Dimmer Percent Display<br>(Initial => Final) |
|-------------|--------------------|---------------------|------------------------------------|----------------------------------------------|
| `secondary` | RollerShutter      | Press `UP` button   | Rail moves Up (`CLOSED`)           | 0% (initial) => 100% (final)                 |
|             |                    | Press `DOWN` button | Rail moves Down (`OPEN`)           | 100% (initial) => 0% (final)                 |
|             | Dimmer             | Move slider to 0%   | Rail moves Up (`CLOSED`)           | 0% (initial) => 100% (final)                 |
|             |                    | Move slider to 100% | Rail moves Down (`OPEN`)           | 100% (initial) => 0% (final)                 |

### Interdependency between Channel positions

On some types of shades with movable vanes, the vanes cannot be moved unless the shade is down.
So there is an interdependency between the value of `vane` and the value of `position` as follows..

| Case                       | State of `position` | State of `vane` |
|----------------------------|---------------------|-----------------|
| Shade up                   | 0% = `UP`           | `UNDEFINED`     |
| Shade 50% down             | 50%                 | `UNDEFINED`     |
| Shade 100% down, Vane 0%   | 100% = `DOWN`       | 0%              |
| Shade 100% down, Vane 50%  | 100% = `DOWN`       | 50%             |
| Shade 100% down, Vane 100% | 100% = `DOWN`       | 100%            |

On dual action shades, the top rail cannot move below the bottom rail, nor can the bottom rail move above the top.
So the value of `secondary` is constrained by the prior value of `position`.
And the value of `position` is constrained by the prior value of `secondary`.

On shades with a secondary blackout panel 'DuoLite', the secondary blackout panel cannot be moved unless the main shade panel is already down.
In this case, the position of the secondary blackout panel is reported as 0%.

## Hard Refreshes

### Hard Refresh on Generation 3 Gateways<sup>[3]</sup>

In Generation 3 systems, whenever the state of a shade or scene changes, it immediately notifies the gateway, and the gateway immediately notifies the openHAB binding.
However, in case that any notifications have been lost, (e.g. due to a network error), the binding occasionally requests a full update (i.e. a 'hard refresh') of all shade and scene states from the gateway.
The time interval between hard refreshes is set in the `hardRefresh` configuration parameter.

### Hard Refresh on Generation 1/2 Hubs<sup>[1/2]</sup>

The hub maintains a cache of the last known state of its shades, and this binding delivers those values.
Usually the shades will be moved by this binding, so since the hub is always involved in the moving process, it updates this cache accordingly.

However shades can also be moved manually without the hub’s knowledge.
A person can manually move a shade by pressing a button on the side of the shade or via a remote control.
In neither case will the hub be aware of the shade’s new position.

The hub periodically does a _**"hard refresh"**_ in order to overcome this issue.
The time interval between hard refreshes is set in the `hardRefresh` configuration parameter.
To disable periodic hard refreshes, set `hardRefresh` to zero.

Similarly, the battery level is transient and is only updated automatically by the hub once a week.
To change this interval, set `hardRefreshBatteryLevel` to number of hours between refreshes.
To use default hub behavior (weekly updates), set `hardRefreshBatteryLevel` to zero.

Note: You can also force the hub to refresh itself by sending a `REFRESH` command in a rule to an item that is connected to a channel in the hub as follows:

```java
rule "Hub Refresh (every 20 minutes)"
when
    Time cron "0 1/20 0 ? * * *"
then
    sendCommand(HUB_ITEM_NAME, "REFRESH") // refresh all shades in HUB

    sendCommand(SHADE_ITEM_NAME, "REFRESH") // refresh single shade that ITEM is bound to
end
```

For single shades the refresh takes the item's channel into consideration:

| Channel        | Hard refresh kind |
|----------------|-------------------|
| position       | Position          |
| secondary      | Position          |
| vane           | Position          |
| lowBattery     | Battery           |
| batteryLevel   | Battery           |
| batteryVoltage | Battery           |
| signalStrength | Survey            |
| hubRssi        | Survey            |
| repeaterRssi   | Survey            |

## Full Example

### `demo.things` File

```java
// generation 1/2
Bridge hdpowerview:hub:home "Luxaflex Hub" @ "Living Room" [host="192.168.1.123"] {
    Thing shade s50150 "Living Room Shade" @ "Living Room" [id="50150"]
    Thing repeater r16384 "Bedroom Repeater" @ "Bedroom" [id="16384"]
}

// generation 3
Bridge hdpowerview:gateway:home "Luxaflex Gateway" @ "Living Room" [host="192.168.1.123"] {
    Thing shade3 s50150 "Living Room Shade" @ "Living Room" [id="50150"]
}
```

### `demo.items` File

Shade items:

```java
// generation 1/2
Rollershutter Living_Room_Shade_Position "Living Room Shade Position [%.0f %%]" {channel="hdpowerview:shade:home:s50150:position"}
Rollershutter Living_Room_Shade_Secondary "Living Room Shade Secondary Position [%.0f %%]" {channel="hdpowerview:shade:home:s50150:secondary"}
Dimmer Living_Room_Shade_Vane "Living Room Shade Vane [%.0f %%]" {channel="hdpowerview:shade:home:s50150:vane"}
Switch Living_Room_Shade_Battery_Low_Alarm "Living Room Shade Battery Low Alarm [%s]" {channel="hdpowerview:shade:home:s50150:lowBattery"}
Number Living_Room_Shade_Battery_Level "Battery Level" {channel="hdpowerview:shade:home:s50150:batteryLevel"}
Number:ElectricPotential Living_Room_Shade_Battery_Voltage "Battery Voltage" {channel="hdpowerview:shade:home:s50150:batteryVoltage"}
String Living_Room_Shade_Command "Living Room Shade Command" {channel="hdpowerview:shade:home:s50150:command"}
Number Living_Room_Shade_SignalStrength "Living Room Shade Signal Strength" {channel="hdpowerview:shade:home:s50150:signalStrength"}

// generation 3
Rollershutter Living_Room_Shade_Position "Living Room Shade Position [%.0f %%]" {channel="hdpowerview:shade3:home:s50150:position"}
Rollershutter Living_Room_Shade_Secondary "Living Room Shade Secondary Position [%.0f %%]" {channel="hdpowerview:shade3:home:s50150:secondary"}
Dimmer Living_Room_Shade_Vane "Living Room Shade Vane [%.0f %%]" {channel="hdpowerview:shade3:home:s50150:vane"}
Switch Living_Room_Shade_Battery_Low_Alarm "Living Room Shade Battery Low Alarm [%s]" {channel="hdpowerview:shade3:home:s50150:lowBattery"}
Number Living_Room_Shade_Battery_Level "Battery Level" {channel="hdpowerview:shade3:home:s50150:batteryLevel"}
String Living_Room_Shade_Command "Living Room Shade Command" {channel="hdpowerview:shade3:home:s50150:command"}
```

Repeater items<sup>[1/2]</sup>:

```java
Color Bedroom_Repeater_Color "Bedroom Repeater Color" {channel="hdpowerview:repeater:home:r16384:color"}
Dimmer Bedroom_Repeater_Brightness "Bedroom Repeater Brightness" {channel="hdpowerview:repeater:home:r16384:brightness"}
String Bedroom_Repeater_Identify "Bedroom Repeater Identify" {channel="hdpowerview:repeater:home:r16384:identify"}
Switch Bedroom_Repeater_BlinkingEnabled "Bedroom Repeater Blinking Enabled [%s]" {channel="hdpowerview:repeater:home:r16384:blinkingEnabled"}
```

Scene items:

```java
// generation 1/2
Switch Living_Room_Shades_Scene_Heart "Living Room Shades Scene Heart" <blinds> (g_Shades_Scene_Trigger) {channel="hdpowerview:hub:home:scenes#22663"}

// generation 3
Switch Living_Room_Shades_Scene_Heart "Living Room Shades Scene Heart" <blinds> (g_Shades_Scene_Trigger) {channel="hdpowerview:gateway:home:scenes#22663"}
```

Scene Group items<sup>[1/2]</sup>:

```java
Switch Children_Rooms_Shades_Up "Good Morning Children" {channel="hdpowerview:hub:home:sceneGroups#27119"}
```

Automation items<sup>[1/2]</sup>:

```java
Switch Automation_Children_Up_Sun "Children Up At Sunrise" {channel="hdpowerview:hub:home:automations#1262"}
Switch Automation_Children_Up_Time "Children Up At 6:30" {channel="hdpowerview:hub:home:automations#49023"}
```

### `demo.sitemap` File

```php
Frame label="Living Room" {
    Switch item=Living_Room_Shades_Scene_Open
    Slider item=Living_Room_Shade_Position
    Switch item=Living_Room_Shade_Command mappings=[CALIBRATE="Calibrate"]
    Text item=Living_Room_Shade_SignalStrength
}

// generation 1/2 only
Frame label="Bedroom" {
    Colorpicker item=PowerViewRepeater_Color
    Switch item=PowerViewRepeater_Color
    Slider item=PowerViewRepeater_Brightness
    Switch item=Bedroom_Repeater_Identify mappings=[IDENTIFY="Identify"]
    Switch item=Bedroom_Repeater_BlinkingEnabled
}
```
