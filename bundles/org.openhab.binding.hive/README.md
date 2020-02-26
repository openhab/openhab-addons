# Hive Binding

This binding integrates the [Hive smart home system](https://www.hivehome.com) using the Hive REST API.

_N.B. As this binding uses the Hive REST API it can only integrate Hive branded devices connected to a Hive Hub.
Setups that do not include a Hive Hub or connect to a different brand of Zigbee hub/bridge will not work with this binding._

## Supported Things

| Thing                         | Supported | Tested |
|-------------------------------|-----------|--------|
| Hive Thermostat Gen 1 (white) | ✓         | ✗      |
| Hive Thermostat Gen 2 (black) | ✓         | ✓      |
| Hive Radiator Valve           | ✓         | ✓      |
| Hive Boiler Module            | ✓         | ✓      |

_N.B. At the moment I have not kept track of the firmware / hardware revision of devices.  Hopefully the Hive API abstracts away from this so it should not be a problem._

## Discovery

After manually adding a "Hive Account" thing all the things associated with that account will be auto-discovered
and appear in the UI Inbox.


## Thing Configuration

### Hive Account

| Parameter       | Required | Description                                                                             |
|-----------------|----------|-----------------------------------------------------------------------------------------|
| username        | YES      | Hive account username (same as in mobile app)                                           |
| password        | YES      | Hive account password (same as in mobile app)                                           |
| pollingInterval | YES      | The time in seconds to wait between each poll of the Hive API for updates (default: 10) |

### Other Things

It is recommended that you use auto-discovery and the UI to add things other than `Hive Account`s.
You should not have to modify these parameters if you use auto-discovery.

`bridge` - The `Hive Account` to use to communicate with this thing.

| Parameter | Required | Description                                                 |
|-----------|----------|-------------------------------------------------------------|
| nodeId    | YES      | The Hive API `nodeId` that identifies the thing.            |

_N.B. You can only get the nodeId of things by querying the Hive API.  This is why use of auto-discovery is recommended._

## Channels

### Hive Account

#### Advanced channels

| Channel                    | Type                     | Read/Write   | Description                  |
|----------------------------|--------------------------|--------------|------------------------------|
| last_poll_timestamp        | DateTime                 | Read Only    | The last time this Hive Account thing polled the Hive API. (Mainly for debugging) |
| dump_nodes                 | Switch                   | Read/Write   | Turning this on triggers dumping the nodes reported by the Hive API for this account to a file in the "userdata" directory. (For debugging) |


### Hive Boiler Module

#### Advanced Channels

| Channel                  | Type               | Read/Write   | Description                                                                    |
|--------------------------|--------------------|--------------|--------------------------------------------------------------------------------|
| radio-lqi-average        | Number             | Read Only    | The average zigbee radio **L**ink **Q**uality **I**ndicator                    |
| radio-lqi-last_known     | Number             | Read Only    | The last known zigbee radio **L**ink **Q**uality **I**ndicator                 |
| radio-rssi-average       | Number             | Read Only    | The average zigbee radio **R**eceived **S**ignal **S**trength **I**ndicator    |
| radio-rssi-last_known    | Number             | Read Only    | The last known zigbee radio **R**eceived **S**ignal **S**trength **I**ndicator |


### Hive Heating Zone

#### Basic channels

| Channel                       | Type               | Read/Write   | Description                  |
|-------------------------------|--------------------|--------------|------------------------------|
| temperature-current           | Number:Temperature | Read Only    | The current temperature of the zone.  Only in Celsius for now. |
| temperature-target            | Number:Temperature | Read/Write   | The temperature you want the zone to be heated to.  Only in Celsius for now. |
| easy-mode-operating           | String             | Read/Write   | The operating mode of heating in this zone as in app (Manual / Schedule/ Off). |
| easy-mode-boost               | Switch             | Read/Write   | Is the transient override (boost) active for this zone. (Handles API trickiness for you). |
| easy-state-is_on              | Switch             | Read Only    | Is the heating currently active in this zone? |
| temperature-target-boost      | Number:Temperature | Read/Write   | The temperature you want the zone to be heated to when the transient override (boost) is active.  Only in Celsius for now. |
| transient-duration            | Number             | Read/Write   | How long in minutes the transient override (boost) should be active for once started. |
| transient-remaining           | Number             | Read Only    | If `transient-end_time` is in the future and the transient override (boost) is active: the time between now and `transient-end_time` in minutes, otherwise 0. |
| auto_boost-temperature-target | Number:Temperature | Read/Write   | The max temperature you want the zone to be heated to when auto boost (heating-on-demand) is active.  Only in Celsius for now. |

#### Advanced Channels

| Channel                  | Type               | Read/Write   | Description                  |
|--------------------------|--------------------|--------------|------------------------------|
| mode-operating           | String             | Read/Write   | The operating mode of heating in the zone (Schedule vs. Manual). |
| mode-on_off              | Switch             | Read/Write   | Is heating for this zone turned on or off. |
| state-operating          | String             | Read Only    | Is heating for this zone active? (OFF/HEAT). |
| mode-operating-override  | Switch             | Read/Write   | Is the transient override (boost) active for this zone. |
| transient-enabled        | Switch             | Read/Write   | Is the transient override (boost) enabled for this zone. |
| transient-start_time     | DateTime           | Read Only    | The last time the transient override (boost) was started. |
| transient-end_time       | DateTime           | Read Only    | The last time the transient override (boost) was scheduled to end (even if it was cancelled). |
| auto_boost-duration      | Number             | Read/Write   | How long in minutes the auto boost (heating-on-demand boost) should be active for once started. |

### Hive Hot Water

#### Basic Channels

| Channel                  | Type               | Read/Write   | Description                  |
|--------------------------|--------------------|--------------|------------------------------|
| easy-mode-operating      | String             | Read/Write   | The operating mode of hot water as in app (On / Schedule/ Off). |
| easy-mode-boost          | Switch             | Read/Write   | Is the transient override (boost) active for this zone. (Handles API trickiness for you). |
| easy-state-is_on         | Switch             | Read Only    | Is the hot water currently being heated. |
| transient-duration       | Number             | Read/Write   | How long in minutes the transient override (boost) should be active for once started. |
| transient-remaining      | Number             | Read Only    | If `transient-end_time` is in the future and the transient override (boost) is active: the time between now and `transient-end_time` in minutes, otherwise 0. |


#### Advanced Channels

| Channel                  | Type               | Read/Write   | Description                  |
|--------------------------|--------------------|--------------|------------------------------|
| mode-operating           | String             | Read/Write   | The operating mode of the hot water (Schedule vs. On). |
| mode-on_off              | Switch             | Read/Write   | Is the hot water turned on or off. |
| transient-enabled        | Switch             | Read/Write   | Is the transient override (boost) enabled for this zone. |
| transient-start_time     | DateTime           | Read Only    | The last time the transient override (boost) was started. |
| transient-end_time       | DateTime           | Read Only    | The last time the transient override (boost) was scheduled to end (even if it was cancelled). |


### Hive Hub

No channels exposed in current version of binding.

### Hive Thermostat

#### Basic channels

| Channel                    | Type                     | Read/Write   | Description                  |
|----------------------------|--------------------------|--------------|------------------------------|
| battery-level              | Number                   | Read Only    | The percentage of charge the device's battery has left. _N.B. This seems to be a very coarse measurement so don't expect to see it change very often._ |
| battery-low                | Switch                   | Read Only    | Turns "On" when the battery is low. |

#### Advanced channels

| Channel                    | Type                     | Read/Write   | Description                  |
|----------------------------|--------------------------|--------------|------------------------------|
| battery-state              | String                   | Read Only    | The battery state (FULL/NORMAL/LOW) |
| battery-voltage            | Number:ElectricPotential | Read Only    | The battery voltage. |
| battery-notification_state | String                   | Read Only    | Indicates whether a "low battery" notification has been sent to the Hive app. |
| radio-lqi-average          | Number                   | Read Only    | The average zigbee radio **L**ink **Q**uality **I**ndicator                    |
| radio-lqi-last_known       | Number                   | Read Only    | The last known zigbee radio **L**ink **Q**uality **I**ndicator                 |
| radio-rssi-average         | Number                   | Read Only    | The average zigbee radio **R**eceived **S**ignal **S**trength **I**ndicator    |
| radio-rssi-last_known      | Number                   | Read Only    | The last known zigbee radio **R**eceived **S**ignal **S**trength **I**ndicator |

### Hive Radiator Valve

#### Basic channels

| Channel                    | Type                     | Read/Write   | Description                  |
|----------------------------|--------------------------|--------------|------------------------------|
| temperature-current        | Number:Temperature       | Read Only    | The current temperature of the zone.  Only in Celsius for now. |
| battery-level              | Number                   | Read Only    | The percentage of charge the device's battery has left. _N.B. This seems to be a very coarse measurement so don't expect to see it change very often._ |
| battery-low                | Switch                   | Read Only    | Turns "On" when the battery is low. |

#### Advanced channels

| Channel                    | Type                     | Read/Write   | Description                  |
|----------------------------|--------------------------|--------------|------------------------------|
| battery-state              | String                   | Read Only    | The battery state (FULL/NORMAL/LOW) |
| battery-voltage            | Number:ElectricPotential | Read Only    | The battery voltage. |
| battery-notification_state | String                   | Read Only    | Indicates whether a "low battery" notification has been sent to the Hive app. |
| radio-lqi-average          | Number                   | Read Only    | The average zigbee radio **L**ink **Q**uality **I**ndicator                    |
| radio-lqi-last_known       | Number                   | Read Only    | The last known zigbee radio **L**ink **Q**uality **I**ndicator                 |
| radio-rssi-average         | Number                   | Read Only    | The average zigbee radio **R**eceived **S**ignal **S**trength **I**ndicator    |
| radio-rssi-last_known      | Number                   | Read Only    | The last known zigbee radio **R**eceived **S**ignal **S**trength **I**ndicator |


### Hive Radiator Valve Heating Zone

#### Basic channels

| Channel                  | Type               | Read/Write   | Description                  |
|--------------------------|--------------------|--------------|------------------------------|
| temperature-current      | Number:Temperature | Read Only    | The current temperature of the zone.  Only in Celsius for now. |
| temperature-target       | Number:Temperature | Read/Write   | The temperature you want the zone to be heated to.  Only in Celsius for now. |
| easy-mode-operating      | String             | Read/Write   | The operating mode of heating in this zone as in app (Manual / Schedule/ Off). |
| easy-mode-boost          | Switch             | Read/Write   | Is the transient override (boost) active for this zone. (Handles API trickiness for you). |
| easy-state-is_on         | Switch             | Read Only    | Is the heating currently active in this zone? |
| temperature-target-boost | Number:Temperature | Read/Write   | The temperature you want the zone to be heated to when the transient override (boost) is active.  Only in Celsius for now. |
| transient-duration       | Number             | Read/Write   | How long in minutes the transient override (boost) should be active for once started. |
| transient-remaining      | Number             | Read Only    | If `transient-end_time` is in the future and the transient override (boost) is active: the time between now and `transient-end_time` in minutes, otherwise 0. |

#### Advanced Channels

| Channel                  | Type               | Read/Write   | Description                  |
|--------------------------|--------------------|--------------|------------------------------|
| mode-operating           | String             | Read/Write   | The operating mode of heating in the zone (Schedule vs. Manual). |
| mode-on_off              | Switch             | Read/Write   | Is heating for this zone turned on or off. |
| state-operating          | String             | Read Only    | Is heating for this zone active? (OFF/HEAT). |
| mode-operating-override  | Switch             | Read/Write   | Is the transient override (boost) active for this zone. |
| transient-enabled        | Switch             | Read/Write   | Is the transient override (boost) enabled for this zone. |
| transient-start_time     | DateTime           | Read Only    | The last time the transient override (boost) was started. |
| transient-end_time       | DateTime           | Read Only    | The last time the transient override (boost) was scheduled to end (even if it was cancelled). |


## Full Example

### Things

Add using the UI and auto-discovery as explained above.

### example.items

```
// Thermostat / TRV Heating Zone
Number:Temperature LivingRoom_HeatingZone_Temperature            "Current Temperature"          <temperature> {channel="hive:heating:my-bridge:deadbeef-dead-beef-dead-beefdeadbeef:temperature-current"}
Number:Temperature LivingRoom_HeatingZone_TargetTemperature      "Target Temperature"           <heating>     {channel="hive:heating:my-bridge:deadbeef-dead-beef-dead-beefdeadbeef:temperature-target"}
Switch             LivingRoom_HeatingZone_IsActive               "Heating On"                   <heating>     {channel="hive:heating:my-bridge:deadbeef-dead-beef-dead-beefdeadbeef:easy-state-is_on"}
String             LivingRoom_HeatingZone_Mode                   "Heating Mode"                 <heating>     {channel="hive:heating:my-bridge:deadbeef-dead-beef-dead-beefdeadbeef:easy-mode-operating"}
Switch             LivingRoom_HeatingZone_BoostActive            "Boost Active"                 <heating>     {channel="hive:heating:my-bridge:deadbeef-dead-beef-dead-beefdeadbeef:easy-mode-boost"}
Number             LivingRoom_HeatingZone_BoostDuration          "Boost Duration [%d minutes]"  <heating>     {channel="hive:heating:my-bridge:deadbeef-dead-beef-dead-beefdeadbeef:transient-duration"}
Number             LivingRoom_HeatingZone_BoostRemaining         "Boost Remaining [%d minutes]" <heating>     {channel="hive:heating:my-bridge:deadbeef-dead-beef-dead-beefdeadbeef:transient-remaining"}
Number:Temperature LivingRoom_HeatingZone_BoostTargetTemperature "Boost Target Temperature"     <heating>     {channel="hive:heating:my-bridge:deadbeef-dead-beef-dead-beefdeadbeef:temperature-target-boost"}

// Hot Water
Switch            HotWater_IsActive                              "Hot Water On"                 <water>       {channel="hive:hot_water:my-bridge:deadbeef-dead-beef-dead-beefdeadbeef:easy-state-is_on"}
String            HotWater_Mode                                  "Hot Water Mode"               <water>       {channel="hive:hot_water:my-bridge:deadbeef-dead-beef-dead-beefdeadbeef:easy-mode-operating"}
Switch            HotWater_BoostActive                           "Boost Active"                 <water>       {channel="hive:hot_water:my-bridge:deadbeef-dead-beef-dead-beefdeadbeef:easy-mode-boost"}
Number            HotWater_BoostDuration                         "Boost Duration [%d minutes]"  <water>       {channel="hive:hot_water:my-bridge:deadbeef-dead-beef-dead-beefdeadbeef:transient-duration"}
Number            HotWater_BoostRemaining                        "Boost Remaining [%d minutes]" <water>       {channel="hive:hot_water:my-bridge:deadbeef-dead-beef-dead-beefdeadbeef:transient-remaining"}
```

### example.sitemap

```
sitemap home label="Home" {
    Frame label="Living Room" icon="sofa" {
        Default  item=LivingRoom_HeatingZone_Temperature
        Setpoint item=LivingRoom_HeatingZone_TargetTemperature minValue=7 maxValue=25
        Text     item=LivingRoom_HeatingZone_IsActive label="Heating is currently [%s]"
        Default  item=LivingRoom_HeatingZone_Mode
        Default  item=LivingRoom_HeatingZone_BoostActive
        Setpoint item=LivingRoom_HeatingZone_BoostDuration minValue=10 maxValue=60 step=5
        Default  item=LivingRoom_HeatingZone_BoostRemaining
        Setpoint item=LivingRoom_HeatingZone_BoostTargetTemperature minValue=7 maxValue=25 step=0.5
    }

    Frame label="Hot Water" icon="water" {
        Text     item=HotWater_IsActive label="Hot Water is currently [%s]"
        Default  item=HotWater_Mode
        Default  item=HotWater_BoostActive
        Setpoint item=HotWater_BoostDuration minValue=10 maxValue=60 step=5
        Default  item=LivingRoom_HeatingZone_BoostRemaining
    }
}
```
