# Hunter Douglas (Luxaflex) PowerView Binding

This is an openHAB binding for [Hunter Douglas PowerView](https://www.hunterdouglas.com/operating-systems/motorized/powerview-motorization/overview) motorized shades via their PowerView hub.
In some countries the PowerView system is sold under the brand name [Luxaflex](https://www.luxaflex.com/)

![PowerView](doc/hdpowerview.png)

PowerView shades have motorization control for their vertical position, and some also have vane controls to change the angle of their slats.

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
The discovery process can be started by pressing the refresh button in the Main Configuration UI Inbox.
However you can also manually create a (bridge) thing for the hub, and enter the required configuration parameters (see Thing Configuration below).
If the configuration parameters are all valid, the binding will then automatically attempt to connect to the hub.
If the connection succeeds, the hub will indicate its status as Online, otherwise it will show an error status. 

Once the hub thing has been created and successfully connected, the binding will automatically discover all shades and scenes that are in it.

- For each shade discovered: the binding will create a new dedicated thing with its own channels.
- For each scene discovered: the binding will create a new channel dynamically within the hub thing.

If in the future, you add additional shades or scenes to your system, the binding will discover them too.

## Thing Configuration

### Thing Configuration for PowerView Hub

| Configuration Parameter | Description   |
|-------------------------|---------------|
| host                    | The host name or IP address of the hub on your network. |
| refresh                 | The number of milli-seconds between fetches of the PowerView hub's shade state (default 60'000 one minute). |
| hardRefresh             | The number of minutes between hard refreshes of the PowerView hub's shade state (default 180 three hours). See [Refreshing the PowerView Hub Cache](#Refreshing-the-PowerView-Hub-Cache). |
| hardRefreshBatteryLevel | The number of hours between hard refreshes of battery levels from the PowerView Hub (or 0 to disable, defaulting to weekly). See [Refreshing the PowerView Hub Cache](#Refreshing-the-PowerView-Hub-Cache). |

### Thing Configuration for PowerView Shades

PowerView shades should preferably be configured via the automatic discovery process.
It is quite difficult to configure manually as the `id` of the shade is not exposed in the PowerView app.
However, the configuration parameters are described below:

| Configuration Parameter | Description                                                   |
|-------------------------|---------------------------------------------------------------|
| id                      | The ID of the PowerView shade in the app. Must be an integer. |

## Channels

### Channels for PowerView Hub

Scene, scene group and automation channels will be added dynamically to the binding as they are discovered in the hub.
Each will have an entry in the hub as shown below, whereby different scenes, scene groups and automations
have different `id` values:

| Channel Group | Channel | Item Type | Description |
|---------------|---------|-----------|-------------|
| scenes        | id      | Switch    | Setting this to ON will activate the scene. Scenes are stateless in the PowerView hub; they have no on/off state. Note: include `{autoupdate="false"}` in the item configuration to avoid having to reset it to off after use. |
| sceneGroups   | id      | Switch    | Setting this to ON will activate the scene group. Scene groups are stateless in the PowerView hub; they have no on/off state. Note: include `{autoupdate="false"}` in the item configuration to avoid having to reset it to off after use. |
| automations   | id      | Switch    | Setting this to ON will enable the automation, while OFF will disable it. |

### Channels for PowerView Shade

A shade always implements a roller shutter channel `position` which controls the vertical position of the shade's (primary) rail.
If the shade has slats or rotatable vanes, there is also a dimmer channel `vane` which controls the slat / vane position.
If it is a dual action (top-down plus bottom-up) shade, there is also a roller shutter channel `secondary` which controls the vertical position of the secondary rail.
All of these channels appear in the binding, but only those which have a physical implementation in the shade, will have any physical effect.

| Channel        | Item Type                | Description |
|----------------|--------------------------|-------------|
| position       | Rollershutter            | The vertical position of the shade's rail -- see [next chapter](#Roller-Shutter-Up/Down-Position-vs.-Open/Close-State). Up/Down commands will move the rail completely up or completely down. Percentage commands will move the rail to an intermediate position. Stop commands will halt any current movement of the rail. |
| secondary      | Rollershutter            | The vertical position of the secondary rail (if any). Its function is basically identical to the `position` channel above -- but see [next chapter](#Roller-Shutter-Up/Down-Position-vs.-Open/Close-State). |
| vane           | Dimmer                   | The degree of opening of the slats or vanes. Setting this to a non-zero value will first move the shade `position` fully down, since the slats or vanes can only have a defined state if the shade is in its down position -- see [Interdependency between Channel positions](#Interdependency-between-Channel-positions). |
| lowBattery     | Switch                   | Indicates ON when the battery level of the shade is low, as determined by the hub's internal rules. |
| batteryLevel   | Number                   | Battery level (10% = low, 50% = medium, 100% = high)
| batteryVoltage | Number:ElectricPotential | Battery voltage reported by the shade. |
| signalStrength | Number                   | Signal strength (0 for no or unknown signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |

### Roller Shutter Up/Down Position vs. Open/Close State

The `position` and `secondary` channels are Rollershutter types.
For vertical shades, the binding maps the vertical position of the "rail" to the Rollershutter ▲ / ▼ commands, and its respective percent value.
And for horizontal shades, it maps the horizontal position of the "truck" to the Rollershutter ▲ / ▼ commands, and its respective percent value.

Depending on whether the shade is a top-down, bottom-up, left-right, right-left, or dual action shade, the `OPEN` and `CLOSED` position of the shades may differ from the ▲ / ▼ commands follows..

| Type of Shade            | Channel           | Rollershutter Command | Motion direction | Shade State    | Percent |
|--------------------------|-------------------|-----------------------|------------------|----------------|---------|
| Single action bottom-up  | `position`        | ▲                     | Up               | `OPEN`         | 0%      |
|                          |                   | ▼                     | Down             | `CLOSED`       | 100%    |
| Single action top-down   | `position`        | ▲                     | Up               | ***`CLOSED`*** | 0%      |
|                          |                   | ▼                     | Down             | ***`OPEN`***   | 100%    |
| Single action right-left | `position`        | ▲                     | ***Left***       | `OPEN`         | 0%      |
|                          |                   | ▼                     | ***Right***      | `CLOSED`       | 100%    |
| Single action left-right | `position`        | ▲                     | ***Right***      | `OPEN`         | 0%      |
|                          |                   | ▼                     | ***Left***       | `CLOSED`       | 100%    |
| Dual action (lower rail) | `position`        | ▲                     | Up               | `OPEN`         | 0%      |
|                          |                   | ▼                     | Down             | `CLOSED`       | 100%    |
| Dual action (upper rail) | ***`secondary`*** | ▲                     | ***Down***       | `OPEN`         | 0%      |
|                          |                   | ▼                     | ***Up***         | `CLOSED`       | 100%    |

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

On dual action shades, the top rail cannot move below the position of the bottom rail.
So the value of `secondary` may be constrained by the value of `position`.

## Refreshing the PowerView Hub Cache

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

```
rule "Hub Refresh (every 20 minutes)"
when
    Time cron "0 1/20 0 ? * * *"
then
    sendCommand(HUB_ITEM_NAME, "REFRESH") // refresh all shades in HUB

    sendCommand(SHADE_ITEM_NAME, "REFRESH") // refresh single shade that ITEM is bound to
end
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
Switch Living_Room_Shades_Scene_Heart "Living Room Shades Scene Heart" <blinds> (g_Shades_Scene_Trigger) {channel="hdpowerview:hub:g24:scenes#22663", autoupdate="false"}
```

### `demo.sitemap` File

```
Frame label="Living Room Shades" {
  Switch item=Living_Room_Shades_Scene_Open
  Slider item=Living_Room_Shade_1_Position 
}
```
