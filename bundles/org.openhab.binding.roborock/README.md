# Roborock Binding

This binding is used to control Roborock robot vacuum cleaner products implementing the Roborock protocol.

## Supported Things

The following things types are available:

| ThingType | Description                                                                                                              |
|-----------|--------------------------------------------------------------------------------------------------------------------------|
| account   | Account bridge for Roborock Robot Vacuum products                                                                        |
| vacuum    | For Roborock Robot Vacuum products                                                                                       |

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

| Thing Parameter | Default Value | Required | Advanced | Description                                                                          |
|-----------------|---------------|----------|----------|--------------------------------------------------------------------------------------|
| duid            | N/A           | Yes      | No       | duid (Device UID) of the robot from the Roborock api                                 |
| refresh         | 5             | No       | Yes      | The frequency with which to refresh information from Roborock specified in minutes   |

### Channels

| Type    | Channel                           | Description                |
|---------|-----------------------------------|----------------------------|
| String  | network#ssid                      | Network SSID               |
| String  | network#bssid                     | Network BSSID              |
| Number  | network#rssi                      | Network RSSI               |
| String  | actions#command                   | Send command via cloud     |
| String  | actions#rpc                       | Send command via cloud     |
| Number  | status#segment-status             | Segment Status             |
| Number  | status#map-status                 | Map Box Status             |
| Number  | status#led-status                 | Led Box Status             |
| String  | info#carpet-mode                  | Carpet Mode details        |
| String  | info#fw-features                  | Firmware Features          |
| String  | info#room-mapping                 | Room Mapping details       |
| String  | info#multi-maps-list              | Maps Listing details       |

Additionally depending on the capabilities of your robot vacuum other channels may be enabled at runtime

| Type    | Channel                           | Description                |
|---------|-----------------------------------|----------------------------|
| Switch  | status#water-box-status           | Water Box Status           |
| Switch  | status#lock-status                | Lock Status                |
| Number  | status#water-box-mode             | Water Box Mode             |
| Number  | status#mop-mode                   | Mop Mode                   |
| Switch  | status#water-box-carriage-status  | Water Box Carriage Status  |
| Switch  | status#mop-forbidden-enable       | Mop Forbidden              |
| Switch  | status#is-locating                | Robot is locating          |
| Number  | actions#segment                   | Room Clean  (enter room #) |
| Switch  | actions#collect-dust              | Start collecting dust      |
| Switch  | actions#clean-mop-start           | Start mop wash             |
| Switch  | actions#clean-mop-stop            | Stop mop wash              |
| Number  | status#mop-drying-time            | Mop drying Time            |
| Switch  | status#is-mop-drying              | Mop cleaning active        |
| Number  | status#dock-state-id              | Dock status id             |
| String  | status#dock-state                 | Dock status message        |

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
Group                gVac            "Roborock Robot Vacuum"         <fan>
Group                gVacStat        "Status Details"                <status>        (gVac)
Group                gVacCons        "Consumables Usage"             <line-increase> (gVac)
Group                gVacDND         "Do Not Disturb Settings"       <moon>          (gVac)
Group                gVacHist        "Cleaning History"              <calendar>      (gVac)
Group                gVacLast        "Last Cleaning Details"         <calendar>      (gVac)

String               actionControl   "Vacuum Control"                                                {channel="roborock:vacuum:034F0E45:actions#control" }
String               actionCommand   "Vacuum Command"                                                {channel="roborock:vacuum:034F0E45:actions#commands" }

Number:Dimensionless statusBat       "Battery Level [%1.0f%%]"       <battery>       (gVac,gVacStat) {channel="roborock:vacuum:034F0E45:status#battery" }
Number:Area          statusArea      "Cleaned Area [%1.0fm²]"        <zoom>          (gVac,gVacStat) {channel="roborock:vacuum:034F0E45:status#clean-area" }
Number:Time          statusTime      "Cleaning Time [%1.0f']"        <clock>         (gVac,gVacStat) {channel="roborock:vacuum:034F0E45:status#clean-time" }
String               statusError     "Error [%s]"                    <error>         (gVac,gVacStat) {channel="roborock:vacuum:034F0E45:status#error-code" }
Number:Dimensionless statusFanPow    "Fan Power [%1.0f%%]"           <signal>        (gVacStat)      {channel="roborock:vacuum:034F0E45:status#fan-power" }
Number               statusClean     "In Cleaning Status [%1.0f]"    <switch>        (gVacStat)      {channel="roborock:vacuum:034F0E45:status#in-cleaning" }
Switch               statusDND       "DND Activated"                                 (gVacStat)      {channel="roborock:vacuum:034F0E45:status#dnd-enabled" }
Number               statusStatus    "Status [%1.0f]"                <status>        (gVacStat)      {channel="roborock:vacuum:034F0E45:status#state"}
Switch               isLocating      "Locating"                                      (gVacStat)      {channel="roborock:vacuum:034F0E45:status#is-locating" }

Number:Time          consumableMain   "Main Brush [%1.0f]"                           (gVacCons)      {channel="roborock:vacuum:034F0E45:consumables#main-brush-time"}
Number:Time          consumableSide   "Side Brush [%1.0f]"                           (gVacCons)      {channel="roborock:vacuum:034F0E45:consumables#side-brush-time"}
Number:Time          consumableFilter "Filter Time[%1.0f]"                           (gVacCons)      {channel="roborock:vacuum:034F0E45:consumables#filter-time" }
Number:Time          consumableSensor "Sensor [%1.0f]"                               (gVacCons)      {channel="roborock:vacuum:034F0E45:consumables#sensor-dirt-time"}

Switch               dndFunction      "DND Function"                 <moon>          (gVacDND)       {channel="roborock:vacuum:034F0E45:dnd#dnd-function"}
String               dndStart         "DND Start Time [%s]"          <clock>         (gVacDND)       {channel="roborock:vacuum:034F0E45:dnd#dnd-start"}
String               dndEnd           "DND End Time [%s]"            <clock-on>      (gVacDND)       {channel="roborock:vacuum:034F0E45:dnd#dnd-end"}

Number:Area          historyArea      "Total Cleaned Area [%1.0fm²]" <zoom>          (gVacHist)      {channel="roborock:vacuum:034F0E45:history#total-clean-area"}
String               historyTime      "Total Clean Time [%s]"        <clock>         (gVacHist)      {channel="roborock:vacuum:034F0E45:history#total-clean-time"}
Number               historyCount     "Total # Cleanings [%1.0f]"    <office>        (gVacHist)      {channel="roborock:vacuum:034F0E45:history#total-clean-count"}

String               lastStart        "Last Cleaning Start time [%s]" <clock>        (gVacLast)      {channel="roborock:vacuum:034F0E45:cleaning#last-clean-start-time"}
String               lastEnd          "Last Cleaning End time [%s]"   <clock>        (gVacLast)      {channel="roborock:vacuum:034F0E45:cleaning#last-clean-end-time"}
Number:Area          lastArea         "Last Cleaned Area [%1.0fm²]"   <zoom>         (gVacLast)      {channel="roborock:vacuum:034F0E45:cleaning#last-clean-area"}
Number:Time          lastTime         "Last Clean Time [%1.0f']"      <clock>        (gVacLast)      {channel="roborock:vacuum:034F0E45:cleaning#last-clean-duration"}
Number               lastError        "Error [%s]"                    <error>        (gVacLast)      {channel="roborock:vacuum:034F0E45:cleaning#last-clean-error" }
Switch               lastCompleted    "Last Cleaning Completed"                      (gVacLast)      {channel="roborock:vacuum:034F0E45:cleaning#last-clean-finish" }
```
