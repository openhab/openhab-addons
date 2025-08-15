# Roborock Binding

This binding is used to control Roborock robot vacuum cleaner products implementing the Roborock protocol.

## Supported Things

The following things types are available:

| ThingType        | Description                                                                                                              |
|------------------|--------------------------------------------------------------------------------------------------------------------------|
| roborock:vacuum  | For RoboRock Robot Vacuum products                                                                                       |

## Discovery

After (manually) adding a Roborock Account bridge, registered vehicles will be auto discovered.

## `account` Bridge Configuration

Account configuration is necessary. 
The easiest way to do this is from the UI. 
Just add a new thing, select the Roborock binding, then Roborock Account Binding Thing, and enter the email and password for your Roborock account.

| Thing Parameter | Default Value | Required | Advanced | Description                                                                          |
|-----------------|---------------|----------|----------|--------------------------------------------------------------------------------------|
| email           | N/A           | Yes      | No       | Email address for your Roborock account                                              |
| password        | N/A           | Yes      | No       | Password for your Roborock account                                                   |

## `vacuum` Thing Configuration

These should be created via discovery as the thingID is set to the ID discovered by the API.
| Thing Parameter | Default Value | Required | Advanced | Description                                                                          |
|-----------------|---------------|----------|----------|--------------------------------------------------------------------------------------|
| refresh         | 5             | No       | Yes      | The frequency with which to refresh information from Roborock specified in minutes   |

## Channels

| Type    | Channel                           | Description                |
|---------|-----------------------------------|----------------------------|
| String  | network#ssid                      | Network SSID               |
| String  | network#bssid                     | Network BSSID              |
| Number  | network#rssi                      | Network RSSI               |
| String  | actions#command                   | Send command via cloud     |
| String  | actions#rpc                       | Send command via cloud     |
| Number  | status#segment_status             | Segment Status             |
| Number  | status#map_status                 | Map Box Status             |
| Number  | status#led_status                 | Led Box Status             |
| String  | info#carpet_mode                  | Carpet Mode details        |
| String  | info#fw_features                  | Firmware Features          |
| String  | info#room_mapping                 | Room Mapping details       |
| String  | info#multi_maps_list              | Maps Listing details       |

Additionally depending on the capabilities of your robot vacuum other channels may be enabled at runtime

| Type    | Channel                           | Description                |
|---------|-----------------------------------|----------------------------|
| Switch  | status#water_box_status           | Water Box Status           |
| Switch  | status#lock_status                | Lock Status                |
| Number  | status#water_box_mode             | Water Box Mode             |
| Number  | status#mop_mode                   | Mop Mode                   |
| Switch  | status#water_box_carriage_status  | Water Box Carriage Status  |
| Switch  | status#mop_forbidden_enable       | Mop Forbidden              |
| Switch  | status#is_locating                | Robot is locating          |
| Number  | actions#segment                   | Room Clean  (enter room #) |
| Switch  | actions#collect_dust              | Start collecting dust      |
| Switch  | actions#clean_mop_start           | Start mop wash             |
| Switch  | actions#clean_mop_stop            | Stop mop wash              |
| Number  | status#mop_drying_time            | Mop drying Time            |
| Switch  | status#is_mop_drying              | Mop cleaning active        |
| Number  | status#dock_state_id              | Dock status id             |
| String  | status#dock_state                 | Dock status message        |

There are several advanced channels, which may be useful in rules (e.g. for individual room cleaning etc)
In case your vacuum does not support one of these commands, it will show "unsupported_method" for string channels or no value for numeric channels.

## Full Example

### `demo.things` Example

```java
Bridge roborock:account:account [ email="xxxx", password="xxxx" ] {
    roborock:vacuum:QrevoS [ refresh=5 ]
}
```

### `example.items` Example

```java
Group  gVac     "Roborock Robot Vacuum"      <fan>
Group  gVacStat "Status Details"           <status> (gVac)
Group  gVacCons "Consumables Usage"        <line-increase> (gVac)
Group  gVacDND  "Do Not Disturb Settings"  <moon> (gVac)
Group  gVacHist "Cleaning History"         <calendar> (gVac)
Group  gVacLast "Last Cleaning Details"       <calendar> (gVac)

String actionControl  "Vacuum Control"          {channel="roborock:vacuum:034F0E45:actions#control" }
String actionCommand  "Vacuum Command"          {channel="roborock:vacuum:034F0E45:actions#commands" }

Number statusBat    "Battery Level [%1.0f%%]" <battery>   (gVac,gVacStat) {channel="roborock:vacuum:034F0E45:status#battery" }
Number statusArea    "Cleaned Area [%1.0fm²]" <zoom>   (gVac,gVacStat) {channel="roborock:vacuum:034F0E45:status#clean_area" }
Number statusTime    "Cleaning Time [%1.0f']" <clock>   (gVac,gVacStat) {channel="roborock:vacuum:034F0E45:status#clean_time" }
String  statusError    "Error [%s]"  <error>  (gVac,gVacStat) {channel="roborock:vacuum:034F0E45:status#error_code" }
Number statusFanPow    "Fan Power [%1.0f%%]"  <signal>   (gVacStat) {channel="roborock:vacuum:034F0E45:status#fan_power" }
Number statusClean    "In Cleaning Status [%1.0f]"   <switch>  (gVacStat) {channel="roborock:vacuum:034F0E45:status#in_cleaning" }
Switch statusDND    "DND Activated"    (gVacStat) {channel="roborock:vacuum:034F0E45:status#dnd_enabled" }
Number statusStatus    "Status [%1.0f]"  <status>  (gVacStat) {channel="roborock:vacuum:034F0E45:status#state"}
Switch isLocating    "Locating"    (gVacStat) {channel="roborock:vacuum:034F0E45:status#is_locating" }

Number consumableMain    "Main Brush [%1.0f]"    (gVacCons) {channel="roborock:vacuum:034F0E45:consumables#main_brush_time"}
Number consumableSide    "Side Brush [%1.0f]"    (gVacCons) {channel="roborock:vacuum:034F0E45:consumables#side_brush_time"}
Number consumableFilter    "Filter Time[%1.0f]"    (gVacCons) {channel="roborock:vacuum:034F0E45:consumables#filter_time" }
Number consumableSensor    "Sensor [%1.0f]"    (gVacCons) {channel="roborock:vacuum:034F0E45:consumables#sensor_dirt_time"}

Switch dndFunction   "DND Function" <moon>   (gVacDND) {channel="roborock:vacuum:034F0E45:dnd#dnd_function"}
String dndStart   "DND Start Time [%s]" <clock>   (gVacDND) {channel="roborock:vacuum:034F0E45:dnd#dnd_start"}
String dndEnd   "DND End Time [%s]"   <clock-on>  (gVacDND) {channel="roborock:vacuum:034F0E45:dnd#dnd_end"}

Number historyArea    "Total Cleaned Area [%1.0fm²]" <zoom>    (gVacHist) {channel="roborock:vacuum:034F0E45:history#total_clean_area"}
String historyTime    "Total Clean Time [%s]"   <clock>     (gVacHist) {channel="roborock:vacuum:034F0E45:history#total_clean_time"}
Number historyCount    "Total # Cleanings [%1.0f]"  <office>  (gVacHist) {channel="roborock:vacuum:034F0E45:history#total_clean_count"}

String lastStart   "Last Cleaning Start time [%s]" <clock> (gVacLast) {channel="roborock:vacuum:034F0E45:cleaning#last_clean_start_time"}
String lastEnd     "Last Cleaning End time [%s]" <clock> (gVacLast) {channel="roborock:vacuum:034F0E45:cleaning#last_clean_end_time"}
Number lastArea    "Last Cleaned Area [%1.0fm²]" <zoom>    (gVacLast) {channel="roborock:vacuum:034F0E45:cleaning#last_clean_area"}
Number lastTime    "Last Clean Time [%1.0f']"   <clock>     (gVacLast) {channel="roborock:vacuum:034F0E45:cleaning#last_clean_duration"}
Number lastError    "Error [%s]"  <error>  (gVacLast) {channel="roborock:vacuum:034F0E45:cleaning#last_clean_error" }
Switch lastCompleted  "Last Cleaning Completed"    (gVacLast) {channel="roborock:vacuum:034F0E45:cleaning#last_clean_finish" }
```
