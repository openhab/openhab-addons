# Xiaomi Robot Vacuum Binding

This Binding is used to control a Xiaomi Robot Vacuum.

## Supported Things

This Binding supports Xiaomi Robot Vacuum devices.

## Discovery

The binding needs a token from the Xiaomi Robot Vacuum in order to be able to control it.

Below firmware version firmware version 3.3.9_003073:
In order to fetch the token, reset the robot vacuum, connect to its the network its announcing (rockrobo-XXXX) and run the discovery again. After the token is retrieved you can connect the vacuum to your phone again.
Once connected to your phone & the regular wifi network, run discovery once more to retrieve the new ipaddress.

Firmware version 3.3.9_003073 & higher
Newer firmware change the token upon inclusion in the mihome app. The token needs to be retrieved from the application database (find the mio2db file with and read it sqlite). The easiest way to do is by using [MiToolkit](https://github.com/ultrara1n/MiToolkit/releases)


## Binding Configuration
No binding configuration is required.

## Thing Configuration

The binding needs ip address and token to be able to communicate. See discovery for details.
Optional configuration is the refresh interval and the deviceID. Note that the deviceID is automatically retrieved when it is left blank.

## Channels

note: the  `actions#commands` channel can be used to send commands that are not automated via the binding.
e.g. `smarthome:send actionCommand  "upd_timer['1498595904821', 'on']"` would enable a pre-configured timer. See https://github.com/marcelrv/XiaomiRobotVacuumProtocol for all known available commands.


## Example example item file

```
Group  gVac     "Xiaomi Robot Vacuum"      <fan>
Group  gVacStat "Status Details"           <status> (gVac)
Group  gVacCons "Consumables Usage"        <line-increase> (gVac)
Group  gVacDND  "Do Not Disturb Settings"  <moon> (gVac)
Group  gVacHist "Cleaning History"         <calendar> (gVac)

Switch actionVacuum    "Start Vacuum"    (gVac) {channel="xiaomivacuum:vacuum:034F0E45:actions#vacuum" }
Switch actionSpot    "Spot Clean"    (gVac) {channel="xiaomivacuum:vacuum:034F0E45:actions#spot_clean"}
Switch actionDock    "Return Dock"    (gVac) {channel="xiaomivacuum:vacuum:034F0E45:actions#return" }
Switch actionPause    "Pause Vacuum"    (gVac) {channel="xiaomivacuum:vacuum:034F0E45:actions#pause" }
String actionCommand  "Vacuum Command"          {channel="xiaomivacuum:vacuum:034F0E45:actions#commands" }

Number statusBat    "Battery Level [%1.0f%%]" <battery>   (gVac,gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#battery" }
Number statusArea    "Cleaned Area [%1.0f m2]" <zoom>   (gVac,gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#clean_area" }
Number statusTime    "Cleaning Time [%1.0f min]" <clock>   (gVac,gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#clean_time" }
String  statusError    "Error [%s]"  <error>  (gVac,gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#error_code" }
Number statusFanPow    "Fan Power [%1.0f %%]"  <signal>   (gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#fan_power" } 
Number statusClean    "In Cleaning Status [%1.0f]"   <switch>  (gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#in_cleaning" }
Switch statusDND    "DND Activated"    (gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#dnd_enabled" }
Number statusStatus    "Status [%1.0f]"  <status>  (gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#state"} 

Number consumableMain    "Main Brush [%1.0f]"    (gVacCons) {channel="xiaomivacuum:vacuum:034F0E45:consumables#main_brush_time"}
Number consumableSide    "Side Brush [%1.0f]"    (gVacCons) {channel="xiaomivacuum:vacuum:034F0E45:consumables#side_brush_time"}
Number consumableFilter    "Filter Time[%1.0f]"    (gVacCons) {channel="xiaomivacuum:vacuum:034F0E45:consumables#filter_time" }
Number consumableSensor    "Sensor [%1.0f]"    (gVacCons) {channel="xiaomivacuum:vacuum:034F0E45:consumables#sensor_dirt_time"}

Switch dndFunction   "DND Function" <moon>   (gVacDND) {channel="xiaomivacuum:vacuum:034F0E45:dnd#dnd_function"}
String dndStart   "DND Start Time [%s]" <clock>   (gVacDND) {channel="xiaomivacuum:vacuum:034F0E45:dnd#dnd_start"}
String dndEnd   "DND End Time [%s]"   <clock-on>  (gVacDND) {channel="xiaomivacuum:vacuum:034F0E45:dnd#dnd_end"}

Number historyArea    "Total Cleaned Area [%1.0f m2]" <zoom>    (gVacHist) {channel="xiaomivacuum:vacuum:034F0E45:history#total_clean_area"}
String historyTime    "Total Clean Time [%s] min"   <clock>     (gVacHist) {channel="xiaomivacuum:vacuum:034F0E45:history#total_clean_time"}
Number historyCount    "Total # Cleanings [%1.0f]"  <office>  (gVacHist) {channel="xiaomivacuum:vacuum:034F0E45:history#total_clean_count"}
```
