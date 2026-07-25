# MELCloud Binding

This is an openHAB binding for Mitsubishi [MELCloud](https://www.melcloud.com/) and [MELCloud Home](https://melcloudhome.com/).
Installing this binding, you can control your Mitsubishi devices from openHAB without accessing the MELCloud App and benefiting from all openHAB automations.

The binding supports two independent Thing families, backed by two different MELCloud cloud APIs, each with its own bridge:

- **MELCloud Things** (`melcloudaccount`/`acdevice`/`heatpumpdevice`): the original, official MELCloud cloud API.
- **MELCloud Home Things** (`home-account`/`ata-unit`/`atw-unit`): the newer cloud API used by the MELCloud Home app.

Both families can be configured and used at the same time.

## Supported Things

| Thing ID | Family | Bridge? | Description |
|----------|--------|---------|-------------|
| `melcloudaccount` | MELCloud | Yes | MELCloud account. |
| `acdevice` | MELCloud | No | Air-to-Air (ATA) heat pump, marketed as an A.C. device. |
| `heatpumpdevice` | MELCloud | No | Air-to-Water (ATW) heat pump. |
| `home-account` | MELCloud Home | Yes | MELCloud Home account. |
| `ata-unit` | MELCloud Home | No | Air-to-Air (ATA) heat pump, marketed as an A.C. unit. |
| `atw-unit` | MELCloud Home | No | Air-to-Water (ATW) heat pump. |

A bridge is required to connect to your MELCloud account: `melcloudaccount` for MELCloud Things, `home-account` for MELCloud Home Things.

Both Thing families cover the same two Mitsubishi Electric heat pump types.
ATA (Air-to-Air) units exchange heat with indoor air and are commonly sold and known as air conditioners.
ATW (Air-to-Water) units heat water, used for space heating and/or a hot water tank.

## Discovery

Discovery works identically for both Thing families, regardless of which bridge type is used.

1. Add the binding
1. Add a new bridge Thing and configure it with your login credentials: `melcloudaccount` for MELCloud, `home-account` for MELCloud Home
1. Go to Inbox and start discovery devices using MELCloud Binding
1. Supported devices/units should appear in your inbox: A.C. Device / Heatpump Device for MELCloud, labelled `MELCloud Home ATA - <unit name>` / `MELCloud Home ATW - <unit name>` for MELCloud Home

Background discovery runs once automatically when the bridge goes online, so devices/units already present in your account at that time are found without a manual scan.
It does not run periodically afterwards.
If you add a new device or unit to your account later, you need to trigger a manual scan from the Inbox to discover it.

Both families also support manual Thing configuration by Thing files, as an alternative to discovery.

## Thing Configuration

### MELCloud Things

In order to manually create a Thing file and not use the discovery routine you will need to know device MELCloud device ID.
This is a bit difficult to get. The easiest way of getting this is enable debug level logging of the binding or discovery devices by the binding (discovered device can be removed afterwards).

MELCloud account configuration:

| Config   | Mandatory | Description                             |
|----------|-----------|-----------------------------------------|
| username | x         | Email address tied to MELCloud account. |
| password | x         | Password to MELCloud account.           |
| language |           | Language ID, see table below.           |

| LanguageId  | Language          |
|-------------|-------------------|
| 0           | English (default) |
| 1           | Bulgarian         |
| 2           | Czech             |
| 3           | Danish            |
| 4           | German            |
| 5           | Estonian          |
| 6           | Spanish           |
| 7           | French            |
| 8           | Armenian          |
| 9           | Latvian           |
| 10          | Lithuanian        |
| 11          | Hungarian         |
| 12          | Dutch             |
| 13          | Norwegian         |
| 14          | Polish            |
| 15          | Portuguese        |
| 16          | Russian           |
| 17          | Finnish           |
| 18          | Swedish           |
| 19          | Italian           |
| 20          | Ukrainian         |
| 21          | Turkish           |
| 22          | Greek             |
| 23          | Croatian          |
| 24          | Romanian          |
| 25          | Slovenian         |

A.C. device and Heatpump device configuration:

| Config          | Mandatory | Description                                                                                                                                                                                                                                       |
|-----------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| deviceID        | x         | MELCloud device ID.                                                                                                                                                                                                                               |
| buildingID      |           | MELCloud building ID. If not defined, binding tries to find matching id by device ID.                                                                                                                                                             |
| pollingInterval |           | Refresh time interval in seconds for updates from MELCloud. Minimum is 180, defaults to 360 seconds. Mitsubishi Electric introduced limits on their API so changing default value may cause excessive traffic and lock you out for several hours. |

### MELCloud Home Things

As with MELCloud Things, manually configuring an `ata-unit`/`atw-unit` Thing file requires the unit's `unitId`.
The easiest way to get it is to run discovery once and read the value off the discovered Thing (it can be removed afterwards if you prefer a Thing file).

MELCloud Home account configuration:

| Config   | Mandatory | Description                                             |
|----------|-----------|----------------------------------------------------------|
| username | x         | Email address used to sign in to the MELCloud Home app. |
| password | x         | Password used to sign in to the MELCloud Home app.      |

ATA unit and ATW unit configuration:

| Config | Mandatory | Description                                                        |
|--------|-----------|----------------------------------------------------------------------|
| unitId | x         | The unit's identifier, as returned by the MELCloud Home API.        |

Unlike MELCloud Things, MELCloud Home Things have no `pollingInterval` config parameter.
The `home-account` bridge centrally polls every registered unit's state every 60 seconds; each unit additionally polls its own energy/outdoor-temperature telemetry (`energy-consumed`/`energy-produced`/`outdoor-temperature` channels) every 30 minutes, since that data changes slowly.

## Channels

### MELCloud Things

#### A.C. device

| Channel             | Type               | Description                                                                              | Read Only |
|---------------------|--------------------|------------------------------------------------------------------------------------------|-----------|
| power               | Switch             | Power Status of Device.                                                                  | False     |
| operationMode       | String             | Operation mode: "1" = Heat, "2" = Dry, "3" = Cool, "7" = Fan, "8" = Auto.                | False     |
| setTemperature      | Number:Temperature | Set Temperature: Min = 10, Max = 40.                                                     | False     |
| fanSpeed            | String             | Fan speed: "0" = Auto, "1" = 1, "2" = 2, "3" = 3, "4" = 4, "5" = 5.                      | False     |
| vaneHorizontal      | String             | Vane Horizontal: "0" = Auto, "1" = 1, "2" = 2, "3" = 3, "4" = 4, "5" = 5, "12" = Swing.  | False     |
| vaneVertical        | String             | Vane Vertical: "0" = Auto, "1" = 1, "2" = 2, "3" = 3, "4" = 4, "5" = 5, "7" = Swing.     | False     |
| roomTemperature     | Number:Temperature | Room temperature.                                                                        | True      |
| lastCommunication   | DateTime           | Last Communication time when MELCloud communicated to the device.                        | True      |
| nextCommunication   | DateTime           | Next communication time when MELCloud will communicate to the device.                    | True      |
| offline             | Switch             | Is device in offline state.                                                              | True      |
| hasPendingCommand   | Switch             | Device has a pending command(s).                                                         | True      |

#### Heatpump device

| Channel                    | Type               | Description                                                                                                                              | Read Only |
|----------------------------|--------------------|------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| power                      | Switch             | Power Status of Device.                                                                                                                  | False     |
| forcedHotWaterMode         | Switch             | If water mode is Heat Now (true) or Auto (false)                                                                                         | False     |
| setTemperatureZone1        | Number:Temperature | Set Temperature Zone 1: Min = 10, Max = 30.                                                                                              | False     |
| roomTemperatureZone1       | Number:Temperature | Room temperature Zone 1.                                                                                                                 | True      |
| setTemperatureZone2        | Number:Temperature | Set Temperature Zone 2: Min = 10, Max = 30.                                                                                              | False     |
| roomTemperatureZone2       | Number:Temperature | Room temperature Zone 2.                                                                                                                 | True      |
| tankWaterTemperature       | Number:Temperature | Tank water temperature.                                                                                                                  | True      |
| tankTargetWaterTemperature | Number:Temperature | Tank water target temperature for heating.                                                                                               | False     |
| lastCommunication          | DateTime           | Last Communication time when MELCloud communicated to the device.                                                                        | True      |
| nextCommunication          | DateTime           | Next communication time when MELCloud will communicate to the device.                                                                    | True      |
| offline                    | Switch             | Is device in offline state.                                                                                                              | True      |
| hasPendingCommand          | Switch             | Device has a pending command(s).                                                                                                         | True      |
| heatFlowTemperatureZone1   | Number:Temperature | Heat flow temperature Zone 1.                                                                                                            | False     |
| heatFlowTemperatureZone2   | Number:Temperature | Heat flow temperature Zone 2.                                                                                                            | False     |
| heatTemperatureModeZone1   | Number             | Temperature control mode for Zone 1 (0 = "Heat thermostat", 1 = "Heat flow", 2 = "Heat curve", 3 = "Cool thermostat", 4 = "Cool flow"    | False     |
| heatTemperatureModeZone2   | Number             | Temperature control mode for Zone 2 (0 = "Heat thermostat", 1 = "Heat flow", 2 = "Heat curve", 3 = "Cool thermostat", 4 = "Cool flow"    | False     |
| operationMode              | String             | Operation mode: "0" = Idle, "1" = Heat water, "2" = Heat zones, "3" = Cooling, "4" = Defrost, "5" = Stand-by, "6" = Legionella           | False     |

### MELCloud Home Things

#### Air-to-Air (ATA) heat pump

| Channel             | Type                | Description                                                                          | Read Only |
|----------------------|--------------------|---------------------------------------------------------------------------------------|-----------|
| power                | Switch              | Power status of the unit.                                                            | False     |
| operation-mode       | Number              | Operation mode: "1" = Heat, "2" = Dry, "3" = Cool, "7" = Fan, "8" = Automatic.       | False     |
| set-temperature      | Number:Temperature  | Set temperature: Min = 10, Max = 40.                                                 | False     |
| room-temperature     | Number:Temperature  | Room temperature.                                                                    | True      |
| fan-speed            | Number              | Fan speed: "0" = Auto, "1" = One, "2" = Two, "3" = Three, "4" = Four, "5" = Five.    | False     |
| vane-horizontal      | Number              | Vane Horizontal: "0" = Auto, "1" = Left, "2" = Left Centre, "3" = Centre, "4" = Right Centre, "5" = Right, "12" = Swing. | False |
| vane-vertical        | Number              | Vane Vertical: "0" = Auto, "1" = One, "2" = Two, "3" = Three, "4" = Four, "5" = Five, "7" = Swing. | False |
| outdoor-temperature  | Number:Temperature  | Outdoor temperature reported by the unit. Refreshed every 30 minutes.               | True      |
| energy-consumed      | Number:Energy       | Cumulative energy consumed, in Wh. Refreshed every 30 minutes.                       | True      |
| rssi                 | Number              | Wi-Fi signal quality (openHAB's built-in `system.signal-strength` channel, 0 = no signal to 4 = excellent). | True |
| is-in-error          | Switch              | Whether the unit is currently reporting an error.                                    | True      |
| error-code           | String              | The unit's current error code, if any.                                              | True      |
| in-standby-mode      | Switch              | Whether the unit is currently in standby mode.                                      | True      |

#### Air-to-Water (ATW)

| Channel                        | Type                | Description                                                                          | Read Only |
|---------------------------------|--------------------|---------------------------------------------------------------------------------------|-----------|
| power                           | Switch              | Power status of the unit.                                                           | False     |
| operation-status                | String              | What the unit is doing right now (e.g. Stop, HotWater, a zone mode).                | True      |
| zone1-operation-mode            | String              | Configured heating/cooling strategy for zone 1: `HeatRoomTemperature`, `HeatFlowTemperature`, `HeatCurve`, `CoolRoomTemperature`, `CoolFlowTemperature`. | False |
| set-temperature-zone1           | Number:Temperature  | Set temperature Zone 1: Min = 10, Max = 30.                                         | False     |
| room-temperature-zone1          | Number:Temperature  | Room temperature Zone 1.                                                             | True      |
| zone2-operation-mode            | String              | Same options as `zone1-operation-mode`, for zone 2. Only present if the unit reports a second zone. | False |
| set-temperature-zone2           | Number:Temperature  | Set temperature Zone 2: Min = 10, Max = 30. Only present if the unit reports a second zone. | False |
| room-temperature-zone2          | Number:Temperature  | Room temperature Zone 2. Only present if the unit reports a second zone.            | True      |
| tank-water-temperature          | Number:Temperature  | Tank water temperature.                                                             | True      |
| tank-target-water-temperature   | Number:Temperature  | Tank water target temperature: Min = 20, Max = 65.                                  | False     |
| forced-hot-water-mode           | Switch              | If water mode is Heat Now (true) or Auto (false).                                   | False     |
| outdoor-temperature             | Number:Temperature  | Outdoor temperature reported by the unit. Refreshed every 30 minutes.               | True      |
| energy-consumed                 | Number:Energy       | Cumulative energy consumed, in Wh. Refreshed every 30 minutes.                       | True      |
| energy-produced                 | Number:Energy       | Cumulative energy produced, in Wh. Refreshed every 30 minutes.                       | True      |
| cop                             | Number              | Coefficient of performance: ratio of energy produced to energy consumed.            | True      |
| rssi                            | Number              | Wi-Fi signal quality (openHAB's built-in `system.signal-strength` channel, 0 = no signal to 4 = excellent). | True |
| is-in-error                     | Switch              | Whether the unit is currently reporting an error.                                   | True      |
| error-code                      | String              | The unit's current error code, if any.                                             | True      |
| in-standby-mode                 | Switch              | Whether the unit is currently in standby mode.                                     | True      |
| holiday-mode                    | Switch              | Whether holiday mode is currently enabled.                                          | True      |
| frost-protection                | Switch              | Whether frost protection is currently enabled.                                     | True      |

## Full Example for items configuration

### MELCloud Things

#### melcloud.things

```plaintext
Bridge melcloud:melcloudaccount:myaccount "My MELCloud account" [ username="user.name@example.com", password="xxxxxx", language="0" ] {
 Thing acdevice livingroom "Livingroom A.C. device" [ deviceID=123456, pollingInterval=360 ]
 Thing heatpumpdevice attic "Attic Heatpump device" [ deviceID=789012, pollingInterval=360 ]
}
```

#### melcloud.items

```plaintext
Switch      power               { channel="melcloud:acdevice:myaccount:livingroom:power" }
String      operationMode       { channel="melcloud:acdevice:myaccount:livingroom:operationMode" }
Number      setTemperature      { channel="melcloud:acdevice:myaccount:livingroom:setTemperature" }
String      fanSpeed            { channel="melcloud:acdevice:myaccount:livingroom:fanSpeed" }
String      vaneHorizontal      { channel="melcloud:acdevice:myaccount:livingroom:vaneHorizontal" }
String      vaneVertical        { channel="melcloud:acdevice:myaccount:livingroom:vaneVertical" }
Number      roomTemperature     { channel="melcloud:acdevice:myaccount:livingroom:roomTemperature" }
DateTime    lastCommunication   { channel="melcloud:acdevice:myaccount:livingroom:lastCommunication" }
DateTime    nextCommunication   { channel="melcloud:acdevice:myaccount:livingroom:nextCommunication" }
Switch      offline             { channel="melcloud:acdevice:myaccount:livingroom:offline" }
Switch      hasPendingCommand   { channel="melcloud:acdevice:myaccount:livingroom:hasPendingCommand" }

Switch      heatpumpPower                       { channel="melcloud:heatpumpdevice:myaccount:attic:power" }
Switch      heatpumpForcedHotWaterMode          { channel="melcloud:heatpumpdevice:myaccount:attic:forcedHotWaterMode" }
Number      heatpumpSetTemperatureZone1         { channel="melcloud:heatpumpdevice:myaccount:attic:setTemperatureZone1" }
Number      heatpumpRoomTemperatureZone1        { channel="melcloud:heatpumpdevice:myaccount:attic:roomTemperatureZone1" }
Number      heatpumpSetTemperatureZone2         { channel="melcloud:heatpumpdevice:myaccount:attic:setTemperatureZone2" }
Number      heatpumpRoomTemperatureZone2        { channel="melcloud:heatpumpdevice:myaccount:attic:roomTemperatureZone2" }
Number      heatpumpTankWaterTemperature        { channel="melcloud:heatpumpdevice:myaccount:attic:tankWaterTemperature" }
Number      heatpumpTankTargetWaterTemperature  { channel="melcloud:heatpumpdevice:myaccount:attic:tankTargetWaterTemperature" }
DateTime    heatpumpLastCommunication           { channel="melcloud:heatpumpdevice:myaccount:attic:lastCommunication" }
DateTime    heatpumpNextCommunication           { channel="melcloud:heatpumpdevice:myaccount:attic:nextCommunication" }
Switch      heatpumpOffline                     { channel="melcloud:heatpumpdevice:myaccount:attic:offline" }
Switch      heatpumpHasPendingCommand           { channel="melcloud:heatpumpdevice:myaccount:attic:hasPendingCommand" }
Number      heatpumpFlowTemperatureZone1        { channel="melcloud:heatpumpdevice:myaccount:attic:heatFlowTemperatureZone1" }
Number      heatpumpFlowTemperatureZone2        { channel="melcloud:heatpumpdevice:myaccount:attic:heatFlowTemperatureZone2" }
String      heatpumpHeatTemperatureModeZone1    { channel="melcloud:heatpumpdevice:myaccount:attic:heatTemperatureModeZone1" }
String      heatpumpHeatTemperatureModeZone2    { channel="melcloud:heatpumpdevice:myaccount:attic:heatTemperatureModeZone2" }
String      heatpumpHeatPumpOperationMode       { channel="melcloud:heatpumpdevice:myaccount:attic:operationMode" }
```

### MELCloud Home Things

#### melcloud-home.things

```plaintext
Bridge melcloud:home-account:myhomeaccount "My MELCloud Home account" [ username="user.name@example.com", password="xxxxxx" ] {
 Thing ata-unit livingroom "Livingroom A.C. unit" [ unitId="<unit-id-from-discovery>" ]
 Thing atw-unit attic "Attic Heatpump unit" [ unitId="<unit-id-from-discovery>" ]
}
```

#### melcloud-home.items

```plaintext
Switch      homePower                    { channel="melcloud:ata-unit:myhomeaccount:livingroom:power" }
Number      homeOperationMode            { channel="melcloud:ata-unit:myhomeaccount:livingroom:operation-mode" }
Number      homeSetTemperature           { channel="melcloud:ata-unit:myhomeaccount:livingroom:set-temperature" }
Number      homeRoomTemperature          { channel="melcloud:ata-unit:myhomeaccount:livingroom:room-temperature" }
Number      homeFanSpeed                 { channel="melcloud:ata-unit:myhomeaccount:livingroom:fan-speed" }
Number      homeVaneHorizontal           { channel="melcloud:ata-unit:myhomeaccount:livingroom:vane-horizontal" }
Number      homeVaneVertical             { channel="melcloud:ata-unit:myhomeaccount:livingroom:vane-vertical" }
Number      homeOutdoorTemperature       { channel="melcloud:ata-unit:myhomeaccount:livingroom:outdoor-temperature" }
Number      homeEnergyConsumed           { channel="melcloud:ata-unit:myhomeaccount:livingroom:energy-consumed" }
Number      homeSignalStrength           { channel="melcloud:ata-unit:myhomeaccount:livingroom:rssi" }
Switch      homeIsInError                { channel="melcloud:ata-unit:myhomeaccount:livingroom:is-in-error" }
String      homeErrorCode                { channel="melcloud:ata-unit:myhomeaccount:livingroom:error-code" }
Switch      homeInStandbyMode            { channel="melcloud:ata-unit:myhomeaccount:livingroom:in-standby-mode" }

Switch      homeHeatpumpPower                      { channel="melcloud:atw-unit:myhomeaccount:attic:power" }
String      homeHeatpumpOperationStatus            { channel="melcloud:atw-unit:myhomeaccount:attic:operation-status" }
String      homeHeatpumpZone1OperationMode         { channel="melcloud:atw-unit:myhomeaccount:attic:zone1-operation-mode" }
Number      homeHeatpumpSetTemperatureZone1        { channel="melcloud:atw-unit:myhomeaccount:attic:set-temperature-zone1" }
Number      homeHeatpumpRoomTemperatureZone1       { channel="melcloud:atw-unit:myhomeaccount:attic:room-temperature-zone1" }
Number      homeHeatpumpTankWaterTemperature       { channel="melcloud:atw-unit:myhomeaccount:attic:tank-water-temperature" }
Number      homeHeatpumpTankTargetWaterTemperature { channel="melcloud:atw-unit:myhomeaccount:attic:tank-target-water-temperature" }
Switch      homeHeatpumpForcedHotWaterMode         { channel="melcloud:atw-unit:myhomeaccount:attic:forced-hot-water-mode" }
Number      homeHeatpumpOutdoorTemperature         { channel="melcloud:atw-unit:myhomeaccount:attic:outdoor-temperature" }
Number      homeHeatpumpEnergyConsumed             { channel="melcloud:atw-unit:myhomeaccount:attic:energy-consumed" }
Number      homeHeatpumpEnergyProduced             { channel="melcloud:atw-unit:myhomeaccount:attic:energy-produced" }
Number      homeHeatpumpCop                        { channel="melcloud:atw-unit:myhomeaccount:attic:cop" }
Number      homeHeatpumpSignalStrength             { channel="melcloud:atw-unit:myhomeaccount:attic:rssi" }
Switch      homeHeatpumpIsInError                  { channel="melcloud:atw-unit:myhomeaccount:attic:is-in-error" }
String      homeHeatpumpErrorCode                  { channel="melcloud:atw-unit:myhomeaccount:attic:error-code" }
Switch      homeHeatpumpInStandbyMode              { channel="melcloud:atw-unit:myhomeaccount:attic:in-standby-mode" }
Switch      homeHeatpumpHolidayMode                { channel="melcloud:atw-unit:myhomeaccount:attic:holiday-mode" }
Switch      homeHeatpumpFrostProtection            { channel="melcloud:atw-unit:myhomeaccount:attic:frost-protection" }
```
