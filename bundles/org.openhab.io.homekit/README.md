# HomeKit Add-on

This is an add-on that exposes your openHAB system as a bridge over the HomeKit protocol.

Using this add-on, you will be able to control your openHAB system using Apple's Siri, or any of a number of HomeKit enabled iOS apps.
In order to do so, you will need to make some configuration changes.
HomeKit organizes your home into "accessories" that are made up of a number of "characteristics".
Some accessory types require a specific set of characteristics.

HomeKit integration supports following accessory types:

- Switchable
- Outlet
- Lighting (simple, dimmable, color)
- Fan
- Thermostat
- Heater / Cooler
- Lock
- Security System
- Garage Door Opener
- Motorized Door
- Motorized Window
- Window Covering/Blinds
- Slat  
- Valve
- Faucet / Shower  
- Speaker
- SmartSpeaker  
- Microphone  
- Air Quality Sensor
- Contact Sensor
- Leak Sensor
- Motion Sensor
- Occupancy Sensor
- Smoke Sensor
- Temperature Sensor
- Humidity Sensor
- Light Sensor
- Carbon Dioxide Sensor
- Carbon Monoxide Sensor
- Battery
- Filter Maintenance

## Quick start

- install homekit addon via UI
  
- add metadata to an existing item (see [UI based configuration](#UI-based-Configuration))
  
- scan QR code from UI->Settings->HomeKit Integration
  
  ![settings_qrcode.png](doc/settings_qrcode.png)
  
- open Home app on your iPhone or iPad
- create new home
  
  ![ios_add_new_home.png](doc/ios_add_new_home.png)
  
- add accessory
  
  ![ios_add_accessory.png](doc/ios_add_accessory.png)
  
- scan QR code from UI->Setting-HomeKit Integration
  
  ![ios_scan_qrcode.png](doc/ios_scan_qrcode.png)  

- click "Add Anyway"
  
  ![ios_add_anyway.png](doc/ios_add_anyway.png)
  
- follow the instruction of the Home app wizard
  
  ![ios_add_accessory_wizard.png](doc/ios_add_accessory_wizard.png)
  
Add metadata to more items or fine-tune your configuration using further settings


## Global Configuration

You can define HomeKit settings either via mainUI or via `$OPENHAB_CONF/services/homekit.cfg`.
HomeKit works with default settings, but we recommend changing the pin for the bridge.
This will be used in iOS when pairing without QR Code. The pin code is in the form "###-##-###".
Requirements beyond this are not clear, and Apple enforces limitations on eligible pins within iOS.
At the very least, you cannot use repeating (111-11-111) or sequential (123-45-678) pin codes.

Other settings, such as using Fahrenheit temperatures, customizing the thermostat heat/cool/auto modes, and specifying the interface to advertise the HomeKit bridge are also illustrated in the following sample:

```
org.openhab.homekit:port=9123
org.openhab.homekit:pin=031-45-154
org.openhab.homekit:useFahrenheitTemperature=true
org.openhab.homekit:thermostatTargetModeCool=CoolOn
org.openhab.homekit:thermostatTargetModeHeat=HeatOn
org.openhab.homekit:thermostatTargetModeAuto=Auto
org.openhab.homekit:thermostatTargetModeOff=Off
org.openhab.homekit:networkInterface=192.168.0.6
org.openhab.homekit:useOHmDNS=false
org.openhab.homekit:blockUserDeletion=false
org.openhab.homekit:name=openHAB
org.openhab.homekit:instances=1
org.openhab.homekit:useDummyAccessories=false
```

Some settings are only visible in UI if the checkbox "Show advanced" is activated.

### Overview of all settings

| Setting                  | Description                                                                                                                                                                                                                                                                                                                                                                          | Default value        |
|:-------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------|
| networkInterface         | IP address or domain name under which the HomeKit bridge can be reached. If no value is configured, the add-on uses the primary IP address configured for openHAB. If unsure, keep it empty                                                                                                                                                                                          | (none)               |
| port                     | Port under which the HomeKit bridge can be reached.                                                                                                                                                                                                                                                                                                                                  | 9123                 |
| useOHmDNS                | mDNS service is used to advertise openHAB as HomeKit bridge in the network so that HomeKit clients can find it. openHAB has already mDNS service running. This option defines whether the mDNS service of openHAB or a separate service should be used.                                                                                                                              | false                |
| blockUserDeletion        | Blocks HomeKit user deletion in openHAB and as result unpairing of devices. If you experience an issue with accessories becoming non-responsive after some time, try to enable this setting. You can also enable this setting if your HomeKit setup is done and you will not re-pair ios devices.                                                                                    | false                |
| pin                      | Pin code used for pairing with iOS devices. Apparently, pin codes are provided by Apple and represent specific device types, so they cannot be chosen freely. The pin code 031-45-154 is used in sample applications and known to work.                                                                                                                                              | 031-45-154           |
| useFahrenheitTemperature | Set to true to use Fahrenheit degrees, or false to use Celsius degrees. Note if an item has a QuantityType as its state, this configuration is ignored and it's always converted properly.                                                                                                                                                                                           | false                |
| thermostatTargetModeCool | Word used for activating the cooling mode of the device (if applicable). It can be overwritten at item level.                                                                                                                                                                                                                                                                        | CoolOn               |
| thermostatTargetModeHeat | Word used for activating the heating mode of the device (if applicable). It can be overwritten at item level.                                                                                                                                                                                                                                                                        | HeatOn               |
| thermostatTargetModeAuto | Word used for activating the automatic mode of the device (if applicable). It can be overwritten at item level.                                                                                                                                                                                                                                                                      | Auto                 |
| thermostatTargetModeOff  | Word used to set the thermostat mode of the device to off (if applicable).  It can be overwritten at item level.                                                                                                                                                                                                                                                                     | Off                  |
| name                     | Name under which this HomeKit bridge is announced on the network. This is also the name displayed on the iOS device when searching for available bridges.                                                                                                                                                                                                                            | openHAB              |
| instances                | Defines how many bridges to expose. Necessary if you have more than 149 accessories. Accessories must be assigned to additional instances via metadata. Additional bridges will use incrementing port numbers.                                                                                                                                                                       | 1                    |
| useDummyAccessories      | When an accessory is missing, substitute a dummy in its place instead of removing it. See [Dummy Accessories](#dummy-accessories).                                                                                                                                                                                                                                                   | false                |

## Item Configuration

After setting the global configuration, you will need to tag your [openHAB items](https://www.openhab.org/docs/configuration/items.html) for HomeKit with accessory type.
For our purposes, you may consider HomeKit accessories to be of two types: simple and complex.

A simple accessory will be mapped to a single openHAB item, e.g. HomeKit lighting can represent an openHAB Switch, Dimmer, or Color item.
A complex accessory will be made up of multiple openHAB items, e.g. HomeKit Thermostat can be composed of mode, and current & target temperature.
Complex accessories require a tag on a Group Item indicating the accessory type, as well as tags on the items it composes.

A HomeKit accessory has mandatory and optional characteristics (listed below in the table).
The mapping between openHAB items and HomeKit accessory and characteristics is done by means of [metadata](https://www.openhab.org/docs/concepts/items.html#item-metadata)

If the first word of the item name match the room name in Home app, Home app will hide it.
E.g. item with the name "Kitchen Light" will be shown in "Kitchen" room as "Light". This is recommended naming convention for HomeKit items and rooms.

### UI based Configuration

In order to add metadata to an item:

- select desired item in mainUI
- click on "Add Metadata"
  
  ![item_add_metadata_button.png](doc/item_add_metadata_button.png)
  
- select "Apple HomeKit" namespace
  
  ![select_homekit_namespace.png](doc/select_homekit_namespace.png)
  
- click on "HomeKit Accessory/Characteristic"
  
  ![add_homekit_tag.png](doc/add_homekit_tag.png)

- select required HomeKit accessory type or characteristic
  
  ![select_homekit_accessory_type.png](doc/select_homekit_accessory_type.png)
  
- click on "Save"


### Textual configuration

```xtend
Switch leaksensor_metadata  "Leak Sensor"           {homekit="LeakSensor"}
```

The tag can be:

- full qualified: i.e. with accessory type and characteristic, e.g. "LeakSensor.LeakDetectedState"
- shorthand version: with only either accessory type or characteristic, e.g. "LeakSensor", "LeakDetectedState".

if shorthand version has only accessory type, then HomeKit will automatically link *all* mandatory characteristics of this accessory type to the openHAB item.
e.g. HomeKit window covering has 3 mandatory characteristics: CurrentPosition, TargetPosition, PositionState.
Following are equal configuration:

```xtend
Rollershutter    window_covering    "Window Rollershutter"     {homekit="WindowCovering"}
Rollershutter    window_covering    "Window Rollershutter"     {homekit="WindowCovering, WindowCovering.CurrentPosition, WindowCovering.TargetPosition, WindowCovering.PositionState"}
```

If the shorthand version has only a characteristic then it must be a part of a group which has a HomeKit accessory type.
You can use openHAB group to define complex accessories. The group item must indicate the HomeKit accessory type,
e.g. LeakSensor definition

```xtend
Group  gLeakSensor                      "Leak Sensor Group"                                              {homekit="LeakSensor"}
Switch leaksensor                       "Leak Sensor"                           (gLeakSensor)            {homekit="LeakSensor.LeakDetectedState"}
Switch leaksensor_battery               "Leak Sensor Battery"                   (gLeakSensor)            {homekit="LeakSensor.BatteryLowStatus"}
```

### Complex Multiple Service Accessories

Alternatively, you may want to have a choice of controlling the items individually, OR as a group, from HomeKit.
The following examples defines a single HomeKit accessory _with multiple services_ that the Home app will allow you to control together, or drill down and control individually.
Note that `AccessoryGroup` doesn't expose any services itself, but allows you to group other services together underneath it.
Also note that when nesting accessories, you cannot use the shorthand of naming only a characteristic, and not its accessory type, since it would be ambiguous if that item belongs to a secondary service, or to the primary service it's nested under.

```java
Group:Switch:OR(ON,OFF) gLight "Light Group" {homekit="AccessoryGroup"}
Switch light1 "Light 1" (gLight) {homekit="Lighting"}
Switch light2 "Light 2" (gLight) {homekit="Lighting"}
```

![Group of Lights](doc/group_of_lights.png)

You can also group additional accessories directly under another accessory.
In this example, HomeKit will show three separate light controls.
As this is somewhat confusing that Home will allow controlling all members as a group, and you also have the group as a distinct switch inside the HomeKit accessory, this is not a recommended configuration.

```xtend
Group:Switch:OR(ON,OFF) gLight "Light Group" {homekit="Lighting"}
Switch light1 "Light 1" (gLight) {homekit="Lighting"}
Switch light2 "Light 2" (gLight) {homekit="Lighting"}
```

![Light Group With Additional Lights](doc/group_of_lights_group_plus_lights.png)

You can also mix and match accessories:

```java
Group gFan {homekit="Fan"}
Switch fan1 "Fan" (gFan) {homekit="Fan.Active"}
Switch fan1_light "Fan Light" (gFan) {homekit="Lighting"}
```

![Fan With Light](doc/fan_with_light.png)

Another way to build complex accessories is to associate multiple accessory types with the root group, and then define all of the individual characteristics on group members.
When using this style, you cannot have multiple instance of the same accessory type.

```java
Group           FanWithLight        "Fan with Light"                           {homekit = "Fan,Lighting"}
Switch          FanActiveStatus     "Fan Active Status"     (FanWithLight)     {homekit = "Fan.ActiveStatus"}
Number          FanRotationSpeed    "Fan Rotation Speed"    (FanWithLight)     {homekit = "Fan.RotationSpeed"}
Switch          Light               "Light"                 (FanWithLight)     {homekit = "Lighting.OnState"}
```

or in MainUI:

![ui_fan_with_light_group_view.png](doc/ui_fan_with_light_group_view.png)
![ui_fan_with_light_group_code.png](doc/ui_fan_with_light_group_code.png)
![ui_fan_with_light_group_config.png](doc/ui_fan_with_light_group_config.png)

Finally, you can link one openHAB item to one or more HomeKit accessories, as well:

```java
Switch occupancy_and_motion_sensor       "Occupancy and Motion Sensor Tag"  {homekit="OccupancySensor,MotionSensor"}
```

You can even form complex sensors this way.
Just be sure that you fully specify additional characteristics, so that the addon knows which root service to add it to.

```java
Group eBunkAirthings "Bunk Room Airthings Wave Plus" { homekit="AirQualitySensor,TemperatureSensor,HumiditySensor" }

String Bunk_AirQuality "Bunk Room Air Quality" (eBunkAirthings) { homekit="AirQualitySensor.AirQuality" }
Number:Dimensionless Bunk_Humidity "Bunk Room Relative Humidity [%d %%]" (eBunkAirthings) { homekit="HumiditySensor.RelativeHumidity" }
Number:Temperature Bunk_AmbTemp "Bunk Room Temperature [%.1f °F]" (eBunkAirthings) { homekit="TemperatureSensor.CurrentTemperature" }
Number:Dimensionless Bunk_tVOC "Bunk Room tVOC [%d ppb]" (eBunkAirthings)  { homekit="AirQualitySensor.VOCDensity" [ maxValue=10000 ] }
```

A sensor with a battery configured in MainUI:

![ui_sensor_with_battery.png](doc/ui_sensor_with_battery.png)

The Home app uses the first accessory in a group as the icon for the group as a whole.
E.g. an accessory defined as `homekit="Fan,Light"` will be shown as a fan and an accessory defined as `homekit="Light,Fan"` will be shown as a light in the Home app.
You can also override the primary service by using adding `primary=<type>` to the HomeKit metadata configuration:

```java
Group           FanWithLight        "Fan with Light"                           {homekit = "Light,Fan" [primary = "Fan"]}
```

on in MainUI:

![ui_fan_with_light_primary.png](doc/ui_fan_with_light_primary.png)

Unusual combinations are also possible, e.g. you can combine temperature sensor with blinds and light.

It will be represented by the Home app as follows:

![ios_complex_accessory_detail_screen.png](doc/ios_complex_accessory_detail_screen.png)

Note that for sensors that aren't interactive, the Home app will show the constituent pieces in the room and home summaries, and you'll only be able to see the combined accessory when viewing the accessories associated with a particular bridge in the home settings:

![Triple Air Sensor](doc/triple_air_sensor.png)
![Triple Air Sensor Broken Out](doc/triple_air_sensor_broken_out.png)

## Dummy Accessories

OpenHAB is a highly dynamic system, and prone to occasional misconfigurations where items can't be loaded for various reasons, especially if you're using something besides the UI to manage your items.
This is a problem for Homekit because if the bridge makes a connection, but accessories are missing, then the Homekit database will simply remove that accessory.
When the accessory does come back (i.e. because you corrected a syntax error in an .items file, or OpenHAB completes booting), all customization of that accessory will be lost - the room assignment, customized  name, custom icon, status/home screen/favorite preferences, etc.
In order to work around this, the Homekit addon can create dummy accessories for any accessory it has previously published to Homekit.
To enable this behavior, turn on the `useDummyAccessories` setting.
OpenHAB will then simply present a non-interactive accessory for any that are missing.
The OpenHAB log will also contain information whenever a dummy accessory is created.
If the item backing the accessory is later re-created, everything will sync back up and nothing will be lost.
You can also run the console command `openhab:homekit listDummyAccessories` to see which items are missing.
Apple devices may or may not show "Not Responding" for some or all accessories when there are dummy accessories, since they will no longer be backed by actual items with state.
It's recommended that you resolve this state as soon as possible, since Homekit may decide your entire bridge is being uncooperative, and remove everything itself.
If you actually meant to remove an item, you will need to purge the dummy items from the database so that they'll disappear from the Home app altogether.
In order to do so, run the console command `openhab:homekit pruneDummyAccessories`.
Alternatively, disabling, saving, and then re-enabling `useDummyAccessories` in the addon settings will have the same effect.

## Accessory Configuration Details

This section provides examples widely used accessory types. 
For complete list of supported accessory types and characteristics please see section [Supported accessory type](#Supported accessory type)

### Dimmers

The way HomeKit handles dimmer devices can be different to the actual dimmers' way of working.
HomeKit home app sends following commands/update:

- On brightness change home app sends "ON" event along with target brightness, e.g. "Brightness = 50%" + "State = ON".
- On "ON" event home app sends "ON" along with brightness 100%, i.e. "Brightness = 100%" + "State = ON"
- On "OFF" event home app sends "OFF" without brightness information.

However, some dimmer devices for example do not expect brightness on "ON" event, some others do not expect "ON" upon brightness change.
In order to support different devices HomeKit integration can filter some events. Which events should be filtered is defined via dimmerMode configuration.

```xtend
Dimmer dimmer_light   "Dimmer Light"     {homekit="Lighting, Lighting.Brightness" [dimmerMode="<mode>"]}
```

Following modes are supported:

- "normal" - no filtering. The commands will be sent to device as received from HomeKit. This is default mode.
- "filterOn" - ON events are filtered out. only OFF events and brightness information are sent
- "filterBrightness100" - only Brightness=100% is filtered out. everything else sent unchanged. This allows custom logic for soft launch in devices.
- "filterOnExceptBrightness100"  - ON events are filtered out in all cases except of brightness = 100%.

Examples:

 ```xtend
 Dimmer dimmer_light_1   "Dimmer Light 1"     {homekit="Lighting, Lighting.Brightness" [dimmerMode="filterOn"]}
 Dimmer dimmer_light_2   "Dimmer Light 2"     {homekit="Lighting, Lighting.Brightness" [dimmerMode="filterBrightness100"]}
 Dimmer dimmer_light_3   "Dimmer Light 3"     {homekit="Lighting, Lighting.Brightness" [dimmerMode="filterOnExceptBrightness100"]}
 ```

### Color Temperature

Color temperature can be represented various ways in OpenHAB. Given the base bulb configured like this:

```xtend
Group gLight "CCT Light" { homekit="Lighting" }
Switch light_switch (gLight) { homekit="Lighting.OnState" }
```

The color temperature might be configured in any of these ways:

```xtend
// Number item presumed in mireds
Number light_temp (gLight) { homekit="Lighting.ColorTemperature" }

// Number item explicitly in mireds
Number:Temperature light_temp "Temp [%.0f mired]" { homekit="Lighting.ColorTemperature" }

// Number item explicitly in Kelvin
Number:Temperature light_temp "Temp [%.0f K]" { homekit="Lighting.ColorTemperature" }

// Dimmer item, with allowed range given in mireds
Dimmer light_temp { homekit="Lighting.ColorTemperature"[ minValue=50, maxValue=400 ]}

// Dimmer item, with allowed range given in Kelvin
Dimmer light_temp { homekit="Lighting.ColorTemperature"[ minValue="2700 K", maxValue="5000 K" ]}

// Dimmer item, where 0% represents "warm" instead of "cool" (i.e. if it's backed by a channel
// that's ultimately interpreting the value in Kelvin instead of mireds)
Dimmer light_temp { homekit="Lighting.ColorTemperature"[ minValue="2700 K", maxValue="5000 K", inverted=true ]}
```

### Windows Covering (Blinds) / Window / Door

HomeKit Windows Covering, Window and Door accessory types have following mandatory characteristics:

- CurrentPosition (0-100% of current window covering position)
- TargetPosition (0-100% of target position)
- PositionState (DECREASING,INCREASING or STOPPED as state). If no state provided, HomeKit will send STOPPED

These characteristics can be mapped to a single openHAB rollershutter item. In such case currentPosition will always equal target position, means if you request to close a blind/window/door, HomeKit will immediately report that the blind/window/door is closed.
As discussed above, one can use full or shorthand definition. Following two definitions are equal:

```xtend
Rollershutter    window                "Window"                    {homekit = "Window"}
Rollershutter    door                  "Door"                      {homekit = "Door"}
Rollershutter   window_covering        "Window Rollershutter"      {homekit = "WindowCovering"}
Rollershutter    window_covering_long  "Window Rollershutter long" {homekit = "WindowCovering, WindowCovering.CurrentPosition, WindowCovering.TargetPosition, WindowCovering.PositionState"}
 ```

openHAB Rollershutter is defined by default as:

- OPEN if position is 0%,
- CLOSED if position is 100%.

In contrast, HomeKit window covering/door/window have inverted mapping

- OPEN if position 100%
- CLOSED if position is 0%

Therefore, HomeKit integration inverts by default the values between openHAB and HomeKit, e.g. if openHAB current position is 30% then it will send 70% to HomeKit app.
In case you need to disable this logic you can do it with configuration parameter inverted=false, e.g.

```xtend
Rollershutter window_covering "Window Rollershutter" {homekit = "WindowCovering"  [inverted=false]}
Rollershutter window          "Window"               {homekit = "Window" [inverted=false]}
Rollershutter door            "Door"                 {homekit = "Door" [inverted=false]}
 ```

HomeKit home app never sends "STOP" but only the target position. 
If you add configuration parameter "stop=true", openHAB will emulate stop and send "STOP" command to rollershutter item if you click on the blind icon in the iOS home app while the blind is moving.

```xtend
Rollershutter window_covering "Window Rollershutter" {homekit = "WindowCovering"  [stop=true]}
 ```

Some blinds devices do support "STOP" command but would stop if they receive UP/DOWN while moving om the same direction. In order to support such devices add "stopSameDirection" parameter.

```xtend
Rollershutter window_covering "Window Rollershutter" {homekit = "WindowCovering"  [stop=true, stopSameDirection=true]}
 ```

Window covering can have a number of optional characteristics like horizontal & vertical tilt, obstruction status and hold position trigger.
If your blind supports tilt, and you want to control tilt via HomeKit you need to define blind as a group.
e.g.

```xtend
Group           gBlind                  "Blind with tilt"                               {homekit = "WindowCovering"}
Rollershutter   window_covering         "Blind"                         (gBlind)        {homekit = "WindowCovering"}
Dimmer          window_covering_htilt   "Blind horizontal tilt"         (gBlind)        {homekit = "WindowCovering.CurrentHorizontalTiltAngle, WindowCovering.TargetHorizontalTiltAngle"}
Dimmer          window_covering_vtilt   "Blind vertical tilt"           (gBlind)        {homekit = "WindowCovering.CurrentVerticalTiltAngle, WindowCovering.TargetVerticalTiltAngle"}
 ```

Current and Target Position characteristics can be linked to Rollershutter but also to Number or Dimmer item types.
e.g.

```xtend
Group           gBlind   "Blinds"                        {homekit = "WindowCovering"}
Dimmer          blind_current_position    (gBlind)       {homekit = "CurrentPosition"}
Number          blind_target_position     (gBlind)       {homekit = "TargetPosition"}
String          blind_position            (gBlind)       {homekit = "PositionState"}
```

### Thermostat

A HomeKit thermostat has following mandatory characteristics:

- CurrentTemperature
- TargetTemperature
- CurrentHeatingCoolingMode
- TargetHeatingCoolingMode

In order to define a thermostat you need to create a group with at least these 4 items.
Example:

```xtend
Group           gThermostat                "Thermostat"                                             {homekit = "Thermostat"}
Number          thermostat_current_temp    "Thermostat Current Temp [%.1f °C]"  (gThermostat)       {homekit = "CurrentTemperature"}
Number          thermostat_target_temp     "Thermostat Target Temp [%.1f °C]"   (gThermostat)       {homekit = "TargetTemperature"}  
String          thermostat_current_mode    "Thermostat Current Mode"            (gThermostat)       {homekit = "CurrentHeatingCoolingMode"}          
String          thermostat_target_mode     "Thermostat Target Mode"             (gThermostat)       {homekit = "TargetHeatingCoolingMode"}           
```

In addition, thermostat can have thresholds for cooling and heating modes.
Example with thresholds:

```xtend
Group           gThermostat                "Thermostat"                                             {homekit = "Thermostat"}
Number          thermostat_current_temp    "Thermostat Current Temp [%.1f °C]"        (gThermostat) {homekit = "CurrentTemperature"}
Number          thermostat_target_temp     "Thermostat Target Temp[%.1f °C]"          (gThermostat) {homekit = "TargetTemperature"}  
String          thermostat_current_mode    "Thermostat Current Mode"                  (gThermostat) {homekit = "CurrentHeatingCoolingMode"}          
String          thermostat_target_mode     "Thermostat Target Mode"                   (gThermostat) {homekit = "TargetHeatingCoolingMode"}           
Number          thermostat_cool_thrs       "Thermostat Cool Threshold Temp [%.1f °C]" (gThermostat) {homekit = "CoolingThresholdTemperature"}
Number          thermostat_heat_thrs       "Thermostat Heat Threshold Temp [%.1f °C]" (gThermostat) {homekit = "HeatingThresholdTemperature"}
```

#### Min / max temperatures

Current  and target temperatures have default min and max values. Any values below or above max limits will be replaced with min or max limits.
Default limits are:

- current temperature: min value = 0 °C, max value = 100 °C
- target temperature: min value = 10 °C, max value = 38 °C

You can overwrite default values using minValue and maxValue configuration at item level, e.g.

```xtend
Number          thermostat_current_temp    "Thermostat Current Temp [%.1f °C]"     (gThermostat)       {homekit = "CurrentTemperature" [minValue=5, maxValue=30]}
Number          thermostat_target_temp     "Thermostat Target Temp[%.1f °C]"       (gThermostat)       {homekit = "TargetTemperature" [minValue=10.5, maxValue=27]} 
```

If "useFahrenheitTemperature" is set to true, the min and max temperature must be provided in Fahrenheit.

#### Thermostat modes

HomeKit thermostat supports following modes

- CurrentHeatingCoolingMode: OFF, HEAT, COOL
- TargetHeatingCoolingMode: OFF, HEAT, COOL, AUTO

These modes are mapped to string values of openHAB items using either global configuration (see [Global Configuration](#Global Configuration)) or configuration at item level.
e.g. if your current mode item can have following values: "OFF", "HEATING", "COOLING" then you need following mapping at item level

```xtend
String          thermostat_current_mode     "Thermostat Current Mode" (gThermostat) {homekit = "CurrentHeatingCoolingMode" [OFF="OFF", HEAT="HEATING", COOL="COOLING"]} 
```

You can provide mapping for target mode in similar way.

The custom mapping at item level can be also used to reduce number of modes shown in home app. The modes can be only reduced, but not added, i.e. it is not possible to add new custom mode to HomeKit thermostat.

Example: if your thermostat does not support cooling, then you need to limit mapping  to OFF and HEAT values only:

```xtend
String          thermostat_current_mode    "Thermostat Current Mode"            (gThermostat) {homekit = "CurrentHeatingCoolingMode" [HEAT="HEATING", OFF="OFF"]}          
String          thermostat_target_mode     "Thermostat Target Mode"             (gThermostat) {homekit = "TargetHeatingCoolingMode" [HEAT="HEATING", OFF="OFF"]}
```

The mapping using main UI looks like following:

![mode_mapping.png](doc/mode_mapping.png)

### Valve

The HomeKit valve accessory supports following 2 optional characteristics:

- duration: this describes how long the valve should set "InUse" once it is activated. The duration changes will apply to the next operation. If valve is already active then duration changes have no effect.

- remaining duration: this describes the remaining duration on the valve. Notifications on this characteristic must only be used if the remaining duration increases/decreases from the accessoryʼs usual countdown of remaining duration.

Upon valve activation in home app, home app starts to count down from the "duration" to "0" without contacting the server. Home app also does not trigger any action if it remaining duration get 0.
It is up to valve to have an own timer and stop valve once the timer is over.
Some valves have such timer, e.g. pretty common for sprinklers.
In case the valve has no timer capability, openHAB can take care on this -  start an internal timer and send "Off" command to the valve once the timer is over.

configuration for these two cases looks as follow:

- valve with timer:

```xtend
Group           gValve                   "Valve Group"                             {homekit="Valve"  [homekitValveType="Irrigation"]}
Switch          valve_active             "Valve active"             (gValve)       {homekit = "Valve.ActiveStatus, Valve.InUseStatus"}
Number          valve_duration           "Valve duration"           (gValve)       {homekit = "Valve.Duration"}
Number          valve_remaining_duration "Valve remaining duration" (gValve)       {homekit = "Valve.RemainingDuration"}
```

- valve without timer (no item for remaining duration required)

```xtend
Group           gValve             "Valve Group"                             {homekit="Valve"  [homekitValveType="Irrigation", homekitTimer="true"]}
Switch          valve_active       "Valve active"               (gValve)     {homekit = "Valve.ActiveStatus, Valve.InUseStatus"}
Number          valve_duration     "Valve duration"             (gValve)     {homekit = "Valve.Duration" [homekitDefaultDuration = 1800]}
```

### Sensors

Sensors have typically one mandatory characteristic, e.g. temperature or lead trigger, and several optional characteristics which are typically used for battery powered sensors and/or wireless sensors.
Following table summarizes the optional characteristics supported by sensors.

|  Characteristics             | Supported openHAB items  | Description                                                                                                                                                                                                              |
|:-----------------------------|:-------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Name                         | String                   | Name of the sensor. This characteristic is interesting only for very specific cases in which the name of accessory is dynamic. if you not sure then you don't need it.                                                   |
| ActiveStatus                 | Switch, Contact          | Accessory current working status. "ON"/"OPEN" indicates that the accessory is active and is functioning without any errors.                                                                                              |
| FaultStatus                  | Switch, Contact          | Accessory fault status. "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.      |
| TamperedStatus               | Switch, Contact          | Accessory tampered status. "ON"/"OPEN" indicates that the accessory has been tampered. Value should return to "OFF"/"CLOSED" when the accessory has been reset to a non-tampered state.                                  |
| BatteryLowStatus             | Switch, Contact, Number  | Accessory battery status. "ON"/"OPEN" indicates that the battery level of the accessory is low. Value should return to "OFF"/"CLOSED" when the battery charges to a level that's above the low threshold. Alternatively, you can give a Number item that's the battery level, and if it's lower than the lowThreshold configuration, it will report low. |

Switch and Contact items support inversion of the state mapping, e.g. by default the openHAB switch state "ON" is mapped to HomeKit contact sensor state "Open", and "OFF" to "Closed". 
The configuration "inverted=true" inverts this mapping, so that "ON" will be mapped to "Closed" and "OFF" to "Open".  

Examples of sensor definitions.
Sensors without optional characteristics:

```xtend
Switch  leaksensor_single    "Leak Sensor"                   {homekit="LeakSensor"}
Number  light_sensor         "Light Sensor"                  {homekit="LightSensor"}
Number  temperature_sensor   "Temperature Sensor [%.1f °C]"  {homekit="TemperatureSensor"}
Contact contact_sensor       "Contact Sensor"                {homekit="ContactSensor"}
Contact contact_sensor       "Contact Sensor"                {homekit="ContactSensor" [inverted=true]}

Switch  occupancy_sensor     "Occupancy Sensor"              {homekit="OccupancyDetectedState"}
Switch  motion_sensor        "Motion Sensor"                 {homekit="MotionSensor"}
Number  humidity_sensor      "Humidity Sensor"               {homekit="HumiditySensor"}
```

Sensors with optional characteristics:

```xtend
Group           gLeakSensor                "Leak Sensor"                                             {homekit="LeakSensor"}
Switch          leaksensor                 "Leak Sensor State"                  (gLeakSensor)        {homekit="LeakDetectedState"}
Switch          leaksensor_bat             "Leak Sensor Battery"                (gLeakSensor)        {homekit="BatteryLowStatus" }
Switch          leaksensor_active          "Leak Sensor Active"                 (gLeakSensor)        {homekit="ActiveStatus" [inverted=true]}
Switch          leaksensor_fault           "Leak Sensor Fault"                  (gLeakSensor)        {homekit="FaultStatus"}
Switch          leaksensor_tampered        "Leak Sensor Tampered"               (gLeakSensor)        {homekit="TamperedStatus"}

Group           gMotionSensor              "Motion Sensor"                                           {homekit="MotionSensor"}
Switch          motionsensor               "Motion Sensor State"                (gMotionSensor)      {homekit="MotionDetectedState"}
Switch          motionsensor_bat           "Motion Sensor Battery"              (gMotionSensor)      {homekit="BatteryLowStatus" [inverted=true]}
Switch          motionsensor_active        "Motion Sensor Active"               (gMotionSensor)      {homekit="ActiveStatus"}
Switch          motionsensor_fault         "Motion Sensor Fault"                (gMotionSensor)      {homekit="FaultStatus"}
Switch          motionsensor_tampered      "Motion Sensor Tampered"             (gMotionSensor)      {homekit="TamperedStatus"}
```

or using UI

![sensor_ui_config.png](doc/sensor_ui_config.png)
 
## Supported accessory type

| Accessory Tag        | Mandatory Characteristics   | Optional     Characteristics | Supported OH items            | Description                                                                                                                                                                                                                                                                                                                                         |
|:---------------------|:----------------------------|:-----------------------------|:------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AirQualitySensor     |                             |                              |                               | Air Quality Sensor which can measure different parameters                                                                                                                                                                                                                                                                                           |
|                      | AirQuality                  |                              | String                        | Air quality state, possible values (UNKNOWN,EXCELLENT,GOOD,FAIR,INFERIOR,POOR). Custom mapping can be defined at item level, e.g. [EXCELLENT="BEST", POOR="BAD"]                                                                                                                                                                                    |
|                      |                             | OzoneDensity                 | Number                        | Ozone density in micrograms/m3, max 1000                                                                                                                                                                                                                                                                                                            |
|                      |                             | NitrogenDioxideDensity       | Number                        | NO2 density in micrograms/m3, max 1000                                                                                                                                                                                                                                                                                                              |
|                      |                             | SulphurDioxideDensity        | Number                        | SO2 density in micrograms/m3, max 1000                                                                                                                                                                                                                                                                                                              |
|                      |                             | PM25Density                  | Number                        | PM2.5 micrometer particulate density in micrograms/m3, max 1000                                                                                                                                                                                                                                                                                     |
|                      |                             | PM10Density                  | Number                        | PM10 micrometer particulate density in micrograms/m3, max 1000                                                                                                                                                                                                                                                                                      |
|                      |                             | VOCDensity                   | Number                        | VOC Density in micrograms/m3, default max 1000, supported configuration: minValue, maxValue, step.                                                                                                                                                                                                                                                  |
|                      |                             | Name                         | String                        | Name of the sensor                                                                                                                                                                                                                                                                                                                                  |
|                      |                             | ActiveStatus                 | Switch, Contact               | Working status                                                                                                                                                                                                                                                                                                                                      |
|                      |                             | FaultStatus                  | Switch, Contact               | Fault status                                                                                                                                                                                                                                                                                                                                        |
|                      |                             | TamperedStatus               | Switch, Contact               | Tampered status                                                                                                                                                                                                                                                                                                                                     |
|                      |                             | BatteryLowStatus             | Switch, Contact, Number       | Battery status                                                                                                                                                                                                                                                                                                                                      |
| LeakSensor           |                             |                              |                               | Leak Sensor                                                                                                                                                                                                                                                                                                                                         |
|                      | LeakDetectedState           |                              | Switch, Contact, Dimmer       | Leak sensor state (ON=Leak Detected, OFF=no leak)                                                                                                                                                                                                                                                                                                   |
|                      |                             | Name                         | String                        | Name of the sensor                                                                                                                                                                                                                                                                                                                                  |
|                      |                             | ActiveStatus                 | Switch, Contact               | Working status                                                                                                                                                                                                                                                                                                                                      |
|                      |                             | FaultStatus                  | Switch, Contact               | Fault status                                                                                                                                                                                                                                                                                                                                        |
|                      |                             | TamperedStatus               | Switch, Contact               | Tampered status                                                                                                                                                                                                                                                                                                                                     |
|                      |                             | BatteryLowStatus             | Switch, Contact, Number       | Battery status                                                                                                                                                                                                                                                                                                                                      |
| MotionSensor         |                             |                              |                               | Motion Sensor                                                                                                                                                                                                                                                                                                                                       |
|                      | MotionDetectedState         |                              | Switch, Contact, Dimmer       | Motion sensor state (ON=motion detected, OFF=no motion)                                                                                                                                                                                                                                                                                             |
|                      |                             | Name                         | String                        | Name of the sensor                                                                                                                                                                                                                                                                                                                                  |
|                      |                             | ActiveStatus                 | Switch, Contact               | Working status                                                                                                                                                                                                                                                                                                                                      |
|                      |                             | FaultStatus                  | Switch, Contact               | Fault status                                                                                                                                                                                                                                                                                                                                        |
|                      |                             | TamperedStatus               | Switch, Contact               | Tampered status                                                                                                                                                                                                                                                                                                                                     |
|                      |                             | BatteryLowStatus             | Switch, Contact, Number       | Battery status                                                                                                                                                                                                                                                                                                                                      |
| OccupancySensor      |                             |                              |                               | Occupancy Sensor                                                                                                                                                                                                                                                                                                                                    |
|                      | OccupancyDetectedState      |                              | Switch, Contact, Dimmer       | Occupancy sensor state (ON=occupied, OFF=not occupied)                                                                                                                                                                                                                                                                                              |
|                      |                             | Name                         | String                        | Name of the sensor                                                                                                                                                                                                                                                                                                                                  |
|                      |                             | ActiveStatus                 | Switch, Contact               | Working status                                                                                                                                                                                                                                                                                                                                      |
|                      |                             | FaultStatus                  | Switch, Contact               | Fault status                                                                                                                                                                                                                                                                                                                                        |
|                      |                             | TamperedStatus               | Switch, Contact               | Tampered status                                                                                                                                                                                                                                                                                                                                     |
|                      |                             | BatteryLowStatus             | Switch, Contact, Number       | Battery status                                                                                                                                                                                                                                                                                                                                      |
| ContactSensor        |                             |                              |                               | Contact Sensor, An accessory with on/off state that can be viewed in HomeKit but not changed such as a contact sensor for a door or window                                                                                                                                                                                                          |
|                      | ContactSensorState          |                              | Switch, Contact, Dimmer       | Contact sensor state (ON=open, OFF=closed)                                                                                                                                                                                                                                                                                                          |
|                      |                             | Name                         | String                        | Name of the sensor                                                                                                                                                                                                                                                                                                                                  |
|                      |                             | ActiveStatus                 | Switch, Contact               | Working status                                                                                                                                                                                                                                                                                                                                      |
|                      |                             | FaultStatus                  | Switch, Contact               | Fault status                                                                                                                                                                                                                                                                                                                                        |
|                      |                             | TamperedStatus               | Switch, Contact               | Tampered status                                                                                                                                                                                                                                                                                                                                     |
|                      |                             | BatteryLowStatus             | Switch, Contact, Number       | Battery status                                                                                                                                                                                                                                                                                                                                      |
| SmokeSensor          |                             |                              |                               | Smoke Sensor                                                                                                                                                                                                                                                                                                                                        |
|                      | SmokeDetectedState          |                              | Switch, Contact, Dimmer       | Smoke sensor state (ON=smoke detected, OFF=no smoke)                                                                                                                                                                                                                                                                                                |
|                      |                             | Name                         | String                        | Name of the sensor                                                                                                                                                                                                                                                                                                                                  |
|                      |                             | ActiveStatus                 | Switch, Contact               | Working status                                                                                                                                                                                                                                                                                                                                      |
|                      |                             | FaultStatus                  | Switch, Contact               | Fault status                                                                                                                                                                                                                                                                                                                                        |
|                      |                             | TamperedStatus               | Switch, Contact               | Tampered status                                                                                                                                                                                                                                                                                                                                     |
|                      |                             | BatteryLowStatus             | Switch, Contact, Number       | Battery status                                                                                                                                                                                                                                                                                                                                      |
| LightSensor          |                             |                              |                               | Light sensor                                                                                                                                                                                                                                                                                                                                        |
|                      | LightLevel                  |                              | Number                        | Light level in lux. supported configuration: minValue, maxValue.                                                                                                                                                                                                                                                                                    |
|                      |                             | Name                         | String                        | Name of the sensor                                                                                                                                                                                                                                                                                                                                  |
|                      |                             | ActiveStatus                 | Switch, Contact               | Working status                                                                                                                                                                                                                                                                                                                                      |
|                      |                             | FaultStatus                  | Switch, Contact               | Fault status                                                                                                                                                                                                                                                                                                                                        |
|                      |                             | TamperedStatus               | Switch, Contact               | Tampered status                                                                                                                                                                                                                                                                                                                                     |
|                      |                             | BatteryLowStatus             | Switch, Contact, Number       | Battery status                                                                                                                                                                                                                                                                                                                                      |
| HumiditySensor       |                             |                              |                               | Relative Humidity Sensor providing read-only values                                                                                                                                                                                                                                                                                                 |
|                      | RelativeHumidity            |                              | Number                        | Relative humidity in % between 0 and 100. additional configuration homekitMultiplicator = <number to multiply result with>.                                                                                                                                                                                                                         |
|                      |                             | Name                         | String                        | Name of the sensor                                                                                                                                                                                                                                                                                                                                  |
|                      |                             | ActiveStatus                 | Switch, Contact               | Working status                                                                                                                                                                                                                                                                                                                                      |
|                      |                             | FaultStatus                  | Switch, Contact               | Fault status                                                                                                                                                                                                                                                                                                                                        |
|                      |                             | TamperedStatus               | Switch, Contact               | Tampered status                                                                                                                                                                                                                                                                                                                                     |
|                      |                             | BatteryLowStatus             | Switch, Contact, Number       | Battery status                                                                                                                                                                                                                                                                                                                                      |
| TemperatureSensor    |                             |                              |                               | Temperature sensor                                                                                                                                                                                                                                                                                                                                  |
|                      | CurrentTemperature          |                              | Number                        | current temperature. supported configuration: minValue, maxValue, step.                                                                                                                                                                                                                                                                             |
|                      |                             | Name                         | String                        | Name of the sensor                                                                                                                                                                                                                                                                                                                                  |
|                      |                             | ActiveStatus                 | Switch, Contact               | Working status                                                                                                                                                                                                                                                                                                                                      |
|                      |                             | FaultStatus                  | Switch, Contact               | Fault status                                                                                                                                                                                                                                                                                                                                        |
|                      |                             | TamperedStatus               | Switch, Contact               | Tampered status                                                                                                                                                                                                                                                                                                                                     |
|                      |                             | BatteryLowStatus             | Switch, Contact, Number       | Battery status                                                                                                                                                                                                                                                                                                                                      |
| CarbonDioxideSensor  |                             |                              |                               | Carbon Dioxide Sensor                                                                                                                                                                                                                                                                                                                               |
|                      | CarbonDioxideDetectedState  |                              | Switch, Contact, Dimmer       | carbon dioxide sensor state (ON- abnormal level of carbon dioxide detected, OFF - level is normal)                                                                                                                                                                                                                                                  |
|                      |                             | CarbonDioxideLevel           | Number                        | Carbon dioxide level in ppm, max 100000                                                                                                                                                                                                                                                                                                             |
|                      |                             | CarbonDioxidePeakLevel       | Number                        | highest detected level (ppm) of carbon dioxide detected by a sensor, max 100000                                                                                                                                                                                                                                                                     |
|                      |                             | Name                         | String                        | Name of the sensor                                                                                                                                                                                                                                                                                                                                  |
|                      |                             | ActiveStatus                 | Switch, Contact               | Working status                                                                                                                                                                                                                                                                                                                                      |
|                      |                             | FaultStatus                  | Switch, Contact               | Fault status                                                                                                                                                                                                                                                                                                                                        |
|                      |                             | TamperedStatus               | Switch, Contact               | Tampered status                                                                                                                                                                                                                                                                                                                                     |
|                      |                             | BatteryLowStatus             | Switch, Contact, Number       | Battery status                                                                                                                                                                                                                                                                                                                                      |
| CarbonMonoxideSensor |                             |                              |                               | Carbon monoxide Sensor                                                                                                                                                                                                                                                                                                                              |
|                      | CarbonMonoxideDetectedState |                              | Switch, Contact, Dimmer       | Carbon monoxide sensor state (ON- abnormal level of carbon monoxide detected, OFF - level is normal)                                                                                                                                                                                                                                                |
|                      |                             | CarbonMonoxideLevel          | Number                        | Carbon monoxide level in ppm, max 100                                                                                                                                                                                                                                                                                                               |
|                      |                             | CarbonMonoxidePeakLevel      | Number                        | highest detected level (ppm) of carbon monoxide detected by a sensor, max 100                                                                                                                                                                                                                                                                       |
|                      |                             | Name                         | String                        | Name of the sensor                                                                                                                                                                                                                                                                                                                                  |
|                      |                             | ActiveStatus                 | Switch, Contact               | Working status                                                                                                                                                                                                                                                                                                                                      |
|                      |                             | FaultStatus                  | Switch, Contact               | Fault status                                                                                                                                                                                                                                                                                                                                        |
|                      |                             | TamperedStatus               | Switch, Contact               | Tampered status                                                                                                                                                                                                                                                                                                                                     |
|                      |                             | BatteryLowStatus             | Switch, Contact, Number       | Battery status                                                                                                                                                                                                                                                                                                                                      |
| Door                 |                             |                              |                               | Motorized door. One Rollershutter item covers all mandatory characteristics. see examples below.                                                                                                                                                                                                                                                    |
|                      | CurrentPosition             |                              | Rollershutter, Dimmer, Number | Current position of motorized door                                                                                                                                                                                                                                                                                                                  |
|                      | TargetPosition              |                              | Rollershutter, Dimmer, Number | Target position of motorized door                                                                                                                                                                                                                                                                                                                   |
|                      | PositionState               |                              | Rollershutter, String         | Position state. Supported states: DECREASING, INCREASING, STOPPED. Mapping can be redefined at item level, e.g. [DECREASING="Down", INCREASING="Up"]. If no state provided, "STOPPED" is used.                                                                                                                                                      |
|                      |                             | Name                         | String                        | Name of the motorized door                                                                                                                                                                                                                                                                                                                          |
|                      |                             | HoldPosition                 | Switch, Rollershutter         | Motorized door should stop at its current position. ON is sent to Switch items. STOP is sent to Rollershutter items. Only supported by 3rd party Home apps (such as Elgato Eve)                                                                                                                                                                     |
|                      |                             | ObstructionStatus            | Switch, Contact, Dimmer       | Current status of obstruction sensor. ON-obstruction detected, OFF - no obstruction                                                                                                                                                                                                                                                                 |
| Window               |                             |                              |                               | Motorized window. One Rollershutter item covers all mandatory characteristics. see examples below.                                                                                                                                                                                                                                                  |
|                      | CurrentPosition             |                              | Rollershutter, Dimmer, Number | Current position of motorized window                                                                                                                                                                                                                                                                                                                |
|                      | TargetPosition              |                              | Rollershutter, Dimmer, Number | Target position of motorized window                                                                                                                                                                                                                                                                                                                 |
|                      | PositionState               |                              | Rollershutter, String         | Position state. Supported states: DECREASING, INCREASING, STOPPED. Mapping can be redefined at item level, e.g. [DECREASING="Down", INCREASING="Up"]. If no state provided, "STOPPED" is used.                                                                                                                                                      |
|                      |                             | Name                         | String                        | Name of the motorized window                                                                                                                                                                                                                                                                                                                        |
|                      |                             | HoldPosition                 | Switch, Rollershutter         | Motorized door should stop at its current position. ON is sent to Switch items. STOP is sent to Rollershutter items. Only supported by 3rd party Home apps (such as Elgato Eve)                                                                                                                                                                     |
|                      |                             | ObstructionStatus            | Switch, Contact, Dimmer       | Current status of obstruction sensor. ON-obstruction detected, OFF - no obstruction                                                                                                                                                                                                                                                                 |
| WindowCovering       |                             |                              |                               | Window covering / blinds. One Rollershutter item covers all mandatory characteristics. see examples below.                                                                                                                                                                                                                                          |
|                      | CurrentPosition             |                              | Rollershutter, Dimmer, Number | Current position of window covering                                                                                                                                                                                                                                                                                                                 |
|                      | TargetPosition              |                              | Rollershutter, Dimmer, Number | Target position of window covering                                                                                                                                                                                                                                                                                                                  |
|                      | PositionState               |                              | Rollershutter, String         | Currently only "STOPPED" is supported.                                                                                                                                                                                                                                                                                                              |
|                      |                             | Name                         | String                        | Name of the windows covering                                                                                                                                                                                                                                                                                                                        |
|                      |                             | HoldPosition                 | Switch, Rollershutter         | Motorized door should stop at its current position. ON is sent to Switch items. STOP is sent to Rollershutter items. Only supported by 3rd party Home apps (such as Elgato Eve)                                                                                                                                                                     |
|                      |                             | ObstructionStatus            | Switch, Contact, Dimmer       | Current status of obstruction sensor. ON-obstruction detected, OFF - no obstruction                                                                                                                                                                                                                                                                 |
|                      |                             | CurrentHorizontalTiltAngle   | Number, Dimmer                | Number Item = current angle of horizontal slats. values -90 to 90. A value of 0 indicates that the slats are rotated to a fully open position. A value of -90 indicates that the slats are rotated all the way in a direction where the user-facing edge is higher than the window-facing edge. Dimmer Item =  the percentage of openness (0%-100%) |
|                      |                             | TargetHorizontalTiltAngle    | Number, Dimmer                | Number Item = target angle of horizontal slats (-90 to +90). Dimmer Item =  the percentage of openness (0%-100%)                                                                                                                                                                                                                                    |
|                      |                             | CurrentVerticalTiltAngle     | Number, Dimmer                | Number Item = current angle of vertical slats (-90 to +90) . Dimmer Item =  the percentage of openness (0%-100%)                                                                                                                                                                                                                                    |
|                      |                             | TargetVerticalTiltAngle      | Number, Dimmer                | Number Item = target angle of vertical slats. Dimmer Item =  the percentage of openness (0%-100%)                                                                                                                                                                                                                                                   |
| Slat                 |                             |                              |                               | Slat which tilts on a vertical or a horizontal axis. Configuration "type:horizontal" or "type:vertical"                                                                                                                                                                                                                                             |
|                      | CurrentSlatState            |                              | String                        | Current slat state. possible values (FIXED,SWINGING,JAMMED). Custom mapping can be defined at item level, e.g. [JAMMED="JAM", FIXED="FIX"]                                                                                                                                                                                                           |
|                      |                             | Name                         | String                        | Name of the slat                                                                                                                                                                                                                                                                                                                                    |
|                      |                             | SwingMode                    | Number, Switch                | Swing mode.  values: 0/OFF=SWING DISABLED, 1/ON=SWING ENABLED                                                                                                                                                                                                                                                                                       |
|                      |                             | CurrentTiltAngle             | Number, Dimmer                | Number Item = current angle of slats. values -90 to 90. A value of 0 indicates that the slats are rotated to a fully open position. Dimmer Item =  the percentage of openness (0%-100%)                                                                                                                                                             |
|                      |                             | TargetTiltAngle              | Number, Dimmer                | Number Item = target angle of slats (-90 to +90). Dimmer Item =  the percentage of openness (0%-100%)                                                                                                                                                                                                                                               |
| Switchable           |                             |                              |                               | An accessory that can be turned off and on. While similar to a lightbulb, this will be presented differently in the Siri grammar and iOS apps                                                                                                                                                                                                       |
|                      | OnState                     |                              | Switch, Dimmer                | State of the switch - ON/OFF                                                                                                                                                                                                                                                                                                                        |
|                      |                             | Name                         | String                        | Name of the switch                                                                                                                                                                                                                                                                                                                                  |
| Outlet               |                             |                              |                               | An accessory that can be turned off and on. While similar to a lightbulb, this will be presented differently in the Siri grammar and iOS apps                                                                                                                                                                                                       |
|                      | OnState                     |                              | Switch, Dimmer                | State of the outlet - ON/OFF                                                                                                                                                                                                                                                                                                                        |
|                      | InUseStatus                 |                              | Switch, Contact, Dimmer       | Indicates whether the outlet has an appliance e.g., a floor lamp, physically plugged in. This characteristic is set to True even if the plugged-in appliance is off.                                                                                                                                                                                |
|                      |                             | Name                         | String                        | Name of the switch                                                                                                                                                                                                                                                                                                                                  |
| Lighting             |                             |                              |                               | A lightbulb, can have further optional parameters for brightness, hue, etc                                                                                                                                                                                                                                                                          |
|                      | OnState                     |                              | Switch                        | State of the light - ON/OFF                                                                                                                                                                                                                                                                                                                         |
|                      |                             | Name                         | String                        | Name of the light                                                                                                                                                                                                                                                                                                                                   |
|                      |                             | Hue                          | Dimmer, Color                 | Hue                                                                                                                                                                                                                                                                                                                                                 |
|                      |                             | Saturation                   | Dimmer, Color                 | Saturation in % (1-100)                                                                                                                                                                                                                                                                                                                             |
|                      |                             | Brightness                   | Dimmer, Color                 | Brightness in % (1-100). See "Usage of dimmer modes" for configuration details.                                                                                                                                                                                                                                                                     |
|                      |                             | ColorTemperature             | Number, Dimmer                | Color temperature. If the item is a Number with no units, it is represented in mireds. The default value range is from 50 to 400 (2500 K to 20,000 K). If the item is a Dimmer, it will be transformed linearly to mireds. Color temperature should not be used in combination with hue, saturation and brightness. It supports following configuration parameters: minValue, maxValue, inverted |
| Fan                  |                             |                              |                               | Fan                                                                                                                                                                                                                                                                                                                                                 |
|                      | ActiveStatus                |                              | Switch, Dimmer                | Accessory current working status. A value of "ON"/"OPEN" indicates that the accessory is active and is functioning without any errors.                                                                                                                                                                                                              |
|                      |                             | CurrentFanState              | Number                        | Current fan state.  values: 0=INACTIVE, 1=IDLE, 2=BLOWING AIR                                                                                                                                                                                                                                                                                       |
|                      |                             | TargetFanState               | Number, Switch                | Target fan state.  values: 0/OFF=MANUAL, 1/ON=AUTO. Flag [inverted=true] swaps the default mapping                                                                                                                                                                                                                                                  |
|                      |                             | RotationDirection            | Number, Switch                | Rotation direction.  values: 0/OFF=CLOCKWISE, 1/ON=COUNTER CLOCKWISE. Flag [inverted=true] swaps the default mapping                                                                                                                                                                                                                                |
|                      |                             | RotationSpeed                | Number, Dimmer                | Fan rotation speed in % (1-100)                                                                                                                                                                                                                                                                                                                     |
|                      |                             | SwingMode                    | Number, Switch                | Swing mode.  values: 0/OFF=SWING DISABLED, 1/ON=SWING ENABLED. Flag [inverted=true] swaps the default mapping                                                                                                                                                                                                                                       |
|                      |                             | LockControl                  | Number, Switch                | Status of physical control lock.  values: 0/OFF=CONTROL LOCK DISABLED, 1/ON=CONTROL LOCK ENABLED.Flag [inverted=true] swaps the default mapping                                                                                                                                                                                                     |
| BasicFan             |                             |                              |                               | Fan. A BasicFan is a subset of Fan, but the Home app allows you to customize the icon of the accessory to show a ceiling fan.                                                                                                                                                                                                                       |
|                      | OnState                     |                              | Switch, Dimmer                | Accessory current working status. A value of "ON"/"OPEN" indicates that the accessory is active and is functioning without any errors.                                                                                                                                                                                                              |
|                      |                             | RotationDirection            | Number, Switch                | Rotation direction.  values: 0/OFF=CLOCKWISE, 1/ON=COUNTER CLOCKWISE. Flag [inverted=true] swaps the default mapping                                                                                                                                                                                                                                |
|                      |                             | RotationSpeed                | Number, Dimmer                | Fan rotation speed in % (1-100)                                                                                                                                                                                                                                                                                                                     |
| Thermostat           |                             |                              |                               | A thermostat requires all mandatory characteristics defined below                                                                                                                                                                                                                                                                                   |
|                      | CurrentTemperature          |                              | Number                        | Current temperature. supported configuration: minValue, maxValue, step                                                                                                                                                                                                                                                                              |
|                      | TargetTemperature           |                              | Number                        | Target temperature. supported configuration: minValue, maxValue, step                                                                                                                                                                                                                                                                               |
|                      | CurrentHeatingCoolingMode   |                              | String                        | Current heating cooling mode (OFF, AUTO, HEAT, COOL). for mapping see homekit settings above.                                                                                                                                                                                                                                                       |
|                      | TargetHeatingCoolingMode    |                              | String                        | Target heating cooling mode (OFF, AUTO, HEAT, COOL). for mapping see homekit settings above.                                                                                                                                                                                                                                                        |
|                      |                             | Name                         | String                        | Name of the thermostat                                                                                                                                                                                                                                                                                                                              |
|                      |                             | CoolingThresholdTemperature  | Number                        | Maximum temperature that must be reached before cooling is turned on. min/max/step can configured at item level, e.g. minValue=10.5, maxValue=50, step=2]                                                                                                                                                                                           |
|                      |                             | HeatingThresholdTemperature  | Number                        | Minimum temperature that must be reached before heating is turned on. min/max/step can configured at item level, e.g. minValue=10.5, maxValue=50, step=2]                                                                                                                                                                                           |
|                      |                             | RelativeHumidity             | Number                        | Relative humidity in % between 0 and 100.                                                                                                                                                                                                                                                                                                           |
| HeaterCooler         |                             |                              |                               | Heater or/and cooler device                                                                                                                                                                                                                                                                                                                         |
|                      | ActiveStatus                |                              | Switch, Dimmer                | Accessory current working status. A value of "ON"/"OPEN" indicates that the accessory is active and is functioning without any errors.                                                                                                                                                                                                              |
|                      | CurrentTemperature          |                              | Number                        | Current temperature. supported configuration: minValue, maxValue, step                                                                                                                                                                                                                                                                              |
|                      | CurrentHeaterCoolerState    |                              | String                        | Current heater/cooler mode (INACTIVE, IDLE, HEATING, COOLING). Mapping can be redefined at item level, e.g. [HEATING="HEAT", COOLING="COOL"]                                                                                                                                                                                                        |
|                      | TargetHeaterCoolerState     |                              | String                        | Target heater/cooler mode (AUTO, HEAT, COOL). Mapping can be redefined at item level, e.g. [AUTO="AUTOMATIC"]                                                                                                                                                                                                                                       |
|                      |                             | Name                         | String                        | Name of the heater/cooler                                                                                                                                                                                                                                                                                                                           |
|                      |                             | RotationSpeed                | Number                        | Fan rotation speed in % (1-100)                                                                                                                                                                                                                                                                                                                     |
|                      |                             | SwingMode                    | Number, Switch                | Swing mode.  values: 0/OFF=SWING DISABLED, 1/ON=SWING ENABLED                                                                                                                                                                                                                                                                                       |
|                      |                             | LockControl                  | Number, Switch                | Status of physical control lock.  values: 0/OFF=CONTROL LOCK DISABLED, 1/ON=CONTROL LOCK ENABLED                                                                                                                                                                                                                                                    |
|                      |                             | CoolingThresholdTemperature  | Number                        | Maximum temperature that must be reached before cooling is turned on. min/max/step can configured at item level, e.g. minValue=10.5, maxValue=50, step=2]                                                                                                                                                                                           |
|                      |                             | HeatingThresholdTemperature  | Number                        | Minimum temperature that must be reached before heating is turned on. min/max/step can configured at item level, e.g. minValue=10.5, maxValue=50, step=2]                                                                                                                                                                                           |
| Lock                 |                             |                              |                               | A Lock Mechanism. with flag [inverted=true] the default mapping to switch ON/OFF can be inverted.                                                                                                                                                                                                                                                   |
|                      | LockCurrentState            |                              | Switch, Number                | Current state of lock mechanism (1/ON=SECURED, 0/OFF=UNSECURED, 2=JAMMED, 3=UNKNOWN)                                                                                                                                                                                                                                                                |
|                      | LockTargetState             |                              | Switch                        | Target state of lock mechanism (ON=SECURED, OFF=UNSECURED)                                                                                                                                                                                                                                                                                          |
|                      |                             | Name                         | String                        | Name of the lock                                                                                                                                                                                                                                                                                                                                    |
| Valve                |                             |                              |                               | Valve. additional configuration: homekitValveType = ["Generic", "Irrigation", "Shower", "Faucet"]                                                                                                                                                                                                                                                   |
|                      | ActiveStatus                |                              | Switch, Dimmer                | Accessory current working status. A value of "ON"/"OPEN" indicates that the accessory is active and is functioning without any errors.                                                                                                                                                                                                              |
|                      | InUseStatus                 |                              | Switch, Dimmer                | Indicates whether fluid flowing through the valve. A value of "ON"/"OPEN" indicates that fluid is flowing.                                                                                                                                                                                                                                          |
|                      |                             | Duration                     | Number                        | Defines how long a valve should be set to ʼIn Useʼ in second. You can define the default duration via configuration homekitDefaultDuration = <default duration in seconds>                                                                                                                                                                          |
|                      |                             | RemainingDuration            | Number                        | Describes the remaining duration on the accessory. the remaining duration increases/decreases from the accessoryʼs usual countdown. i.e. changes from 90 to 80 in a second.                                                                                                                                                                         |
|                      |                             | Name                         | String                        | Name of the valve                                                                                                                                                                                                                                                                                                                                   |
|                      |                             | FaultStatus                  | Switch, Contact               | Accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                                                                                                                                |
| Faucet               |                             |                              |                               | Faucet or shower. It should be used in combination with Valve or/and HeaterCooler.                                                                                                                                                                                                                                                                  |
|                      | Active                      |                              | Switch, Contact, Dimmer       | Accessory current working status. A value of "ON"/"OPEN" indicates faucet/shower is open.                                                                                                                                                                                                                                                           |
|                      |                             | Name                         | String                        | Name of the Faucet                                                                                                                                                                                                                                                                                                                                  |
|                      |                             | FaultStatus                  | Switch, Contact               | Accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                                                                                                                                |
| SecuritySystem       |                             |                              |                               | Security system.                                                                                                                                                                                                                                                                                                                                    |
|                      | CurrentSecuritySystemState  |                              | String                        | Current state of the security system. STAY_ARM / AWAY_ARM / NIGHT_ARM / DISARMED / TRIGGERED. Mapping can be redefined at item level, e.g. [AWAY_ARM="AWAY", NIGHT_ARM="NIGHT" ]                                                                                                                                                                    |
|                      | TargetSecuritySystemState   |                              | String                        | Requested state of the security system. STAY_ARM / AWAY_ARM / NIGHT_ARM / DISARM. While the requested state is not DISARM, and the current state is DISARMED, HomeKit will display "Arming...", for example during an exit delay. Mapping can be redefined at item level, e.g. [AWAY_ARM="AWAY", NIGHT_ARM="NIGHT" ]                                |
|                      |                             | Name                         | String                        | Name of the security system                                                                                                                                                                                                                                                                                                                         |
|                      |                             | FaultStatus                  | Switch, Contact               | accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                                                                                                                                |
|                      |                             | TamperedStatus               | Switch, Contact               | accessory tampered status. A status of "ON"/"OPEN" indicates that the accessory has been tampered with. Value should return to "OFF"/"CLOSED" when the accessory has been reset to a non-tampered state.                                                                                                                                            |
| GarageDoorOpener     |                             |                              |                               | A garage door opener.                                                                                                                                                                                                                                                                                                                               |
|                      | ObstructionStatus           |                              | Switch                        | Current status of obstruction sensor. ON-obstruction detected, OFF - no obstruction                                                                                                                                                                                                                                                                 |
|                      | CurrentDoorState            |                              | String                        | Current door state. Possible values: OPEN, OPENING, CLOSED, CLOSING, STOPPED                                                                                                                                                                                                                                                                        |
|                      | TargetDoorState             |                              | Switch, String                | Target door state. ON/"OPEN" = open door, OFF/"CLOSED" = closed door                                                                                                                                                                                                                                                                                |
|                      |                             | Name                         | String                        | Name of the garage door                                                                                                                                                                                                                                                                                                                             |
|                      |                             | LockCurrentState             | Switch                        | current states of lock mechanism (OFF=SECURED, ON=UNSECURED)                                                                                                                                                                                                                                                                                        |
|                      |                             | LockTargetState              | Switch                        | target states of lock mechanism (OFF=SECURED, ON=UNSECURED)                                                                                                                                                                                                                                                                                         |
| Battery              |                             |                              |                               | Accessory with battery. Battery can be chargeable (configuration chargeable:true) and non-chargeable  (configuration chargeable:false)                                                                                                                                                                                                              |
|                      | BatteryLevel                |                              | Number                        | Battery level 0% to 100%                                                                                                                                                                                                                                                                                                                            |
|                      | BatteryLowStatus            |                              | Switch, Contact, Number       | Battery low indicator. ON/OPEN = battery level is low; for number if the value is below the lowThreshold, then it is low. Default is 20.                                                                                                                                                                                                                                                                                              |
|                      | BatteryChargingState        |                              | Switch, Contact, Dimmer       | Mandatory only for chargeable battery. ON/OPEN =  battery is charging                                                                                                                                                                                                                                                                               |
|                      |                             | Name                         | String                        | Name of the battery accessory                                                                                                                                                                                                                                                                                                                       |
| Filter               |                             |                              |                               | Accessory with filter maintenance indicator                                                                                                                                                                                                                                                                                                         |
|                      | FilterChangeIndication      |                              | Switch, Contact, Dimmer       | Filter change indicator. ON/OPEN = filter change is required.                                                                                                                                                                                                                                                                                       |
|                      |                             |  FilterLifeLevel             | Number                        | Current filter life level. 0% to 100%                                                                                                                                                                                                                                                                                                               |
|                      |                             |  FilterResetIndication       | Switch                        | Send "filter reset" action triggered by user in iOS home app to openHAB ("ON" = reset requested by user).                                                                                                                                                                                                                                           |
|                      |                             | Name                         | String                        | Name of the filter accessory                                                                                                                                                                                                                                                                                                                        |
| Microphone           |                             |                              |                               | Microphone accessory                                                                                                                                                                                                                                                                                                                                |
|                      | Mute                        |                              | Switch, Contact, Dimmer       | Mute indication. ON/OPEN = microphone is muted                                                                                                                                                                                                                                                                                                      |
|                      |                             | Name                         | String                        | Name of the microphone accessory                                                                                                                                                                                                                                                                                                                    |
|                      |                             | Volume                       | Number                        | Microphone volume from 0% to 100%                                                                                                                                                                                                                                                                                                                   |
| Speaker              |                             |                              |                               | Speaker accessory                                                                                                                                                                                                                                                                                                                                   |
|                      | Mute                        |                              | Switch, Contact, Dimmer       | Mute indication. ON/OPEN = speaker is muted                                                                                                                                                                                                                                                                                                         |
|                      |                             | Name                         | String                        | Name of the speaker accessory                                                                                                                                                                                                                                                                                                                       |
|                      |                             | Volume                       | Number                        | Speaker volume from 0% to 100%                                                                                                                                                                                                                                                                                                                      |
|                      |                             | Active                       | Switch, Contact               | Working status                                                                                                                                                                                                                                                                                                                                      |
| SmartSpeaker         |                             |                              |                               | Smart speaker accessory with Play/Stop/Pause control                                                                                                                                                                                                                                                                                                |
|                      | CurrentMediaState           |                              | String                        | Current smart speaker state. possible values (STOP,PLAY,PAUSE,UNKNOWN). Custom mapping can be defined at item level, e.g. [STOP="STOPPED", PLAY="PLAYING"]                                                                                                                                                                                          |
|                      | TargetMediaState            |                              | String                        | Target smart speaker state. possible values (STOP,PLAY,PAUSE). Custom mapping can be defined at item level, e.g. [STOP="STOPPED", PLAY="PLAYING"]                                                                                                                                                                                                   |
|                      |                             | Mute                         | Switch, Contact               | Mute indication. ON/OPEN = speaker is muted                                                                                                                                                                                                                                                                                                         |
|                      |                             | Name                         | String                        | Name of the speaker accessory                                                                                                                                                                                                                                                                                                                       |
|                      |                             | ConfiguredName               | String                        | Name of the speaker accessory configured in iOS home app. User can rename speaker in iOS home app and this characteristic can be used to reflect change in openHAB and sync name changes from openHAB to home app.                                                                                                                                  |
|                      |                             | Volume                       | Number                        | Speaker volume from 0% to 100%                                                                                                                                                                                                                                                                                                                      |

### Examples

See the sample below for example items:

```xtend
Color           color_light_single         "Color Light Single"                                        {homekit="Lighting"}
Color           color_light_dimmable       "Legacy Color Light Dimmable"                               {homekit="Lighting, Lighting.Brightness"}
Color           color_light_hue            "Legacy Color Light Hue"                                    {homekit="Lighting, Lighting.Hue, Lighting.Brightness, Lighting.Saturation"}

Rollershutter   window_covering            "Window Rollershutter"                                      {homekit="WindowCovering"}
Rollershutter   window_covering_long       "Window Rollershutter long"                                 {homekit="WindowCovering, WindowCovering.CurrentPosition, WindowCovering.TargetPosition, WindowCovering.PositionState"}

Switch          leaksensor_single          "Leak Sensor single"                                        {homekit="LeakSensor"}
Switch          lock                       "Lock single"                                               {homekit="Lock"}
Switch          valve_single               "Valve single"                                              {homekit="Valve" [homekitValveType="Shower"]}

Number          temperature_sensor         "Temperature Sensor [%.1f °C]"                              {homekit="TemperatureSensor" [minValue=10.5, maxValue=27] }
Number          light_sensor               "Light Sensor"                                              {homekit="LightSensor"}

Group           gValve                     "Valve Group"                                               {homekit="Valve"  [homekitValveType="Irrigation"]}
Switch          valve_active               "Valve active"                         (gValve)             {homekit="Valve.ActiveStatus, Valve.InUseStatus"}
Number          valve_duration             "Valve duration"                       (gValve)             {homekit="Valve.Duration"}
Number          valve_remaining_duration   "Valve remaining duration"             (gValve)             {homekit="Valve.RemainingDuration"}

Group           gThermostat                "Thermostat"                                                {homekit="Thermostat"}
Number          thermostat_current_temp    "Thermostat Current Temp [%.1f °C]"    (gThermostat)        {homekit="Thermostat.CurrentTemperature" [minValue=0, maxValue=40]}
Number          thermostat_target_temp     "Thermostat Target Temp[%.1f °C]"      (gThermostat)        {homekit="Thermostat.TargetTemperature"  [minValue=10.5, maxValue=27]}
String          thermostat_current_mode    "Thermostat Current Mode"              (gThermostat)        {homekit="Thermostat.CurrentHeatingCoolingMode"}
String          thermostat_target_mode     "Thermostat Target Mode"               (gThermostat)        {homekit="Thermostat.TargetHeatingCoolingMode"}

Group           gLeakSensor                "Leak Sensor Group"                                         {homekit="LeakSensor"}
Switch          leaksensor                 "Leak Sensor"                          (gLeakSensor)        {homekit="LeakDetectedState"}
String          leaksensor_name            "Leak Sensor Name"                     (gLeakSensor)        {homekit="Name"}
Switch          leaksensor_bat             "Leak Sensor Battery"                  (gLeakSensor)        {homekit="BatteryLowStatus"}
Switch          leaksensor_active          "Leak Sensor Active"                   (gLeakSensor)        {homekit="ActiveStatus"}
Switch          leaksensor_fault           "Leak Sensor Fault"                    (gLeakSensor)        {homekit="FaultStatus"}
Switch          leaksensor_tampered        "Leak Sensor Tampered"                 (gLeakSensor)        {homekit="TamperedStatus"}

Group           gMotionSensor              "Motion Sensor Group"                                       {homekit="MotionSensor"}
Switch          motionsensor               "Motion Sensor"                        (gMotionSensor)      {homekit="MotionSensor.MotionDetectedState"}
Switch          motionsensor_bat           "Motion Sensor Battery"                (gMotionSensor)      {homekit="MotionSensor.BatteryLowStatus"}
Switch          motionsensor_active        "Motion Sensor Active"                 (gMotionSensor)      {homekit="MotionSensor.ActiveStatus"}
Switch          motionsensor_fault         "Motion Sensor Fault"                  (gMotionSensor)      {homekit="MotionSensor.FaultStatus"}
Switch          motionsensor_tampered      "Motion Sensor Tampered"               (gMotionSensor)      {homekit="MotionSensor.TamperedStatus"}

Group           gOccupancySensor           "Occupancy Sensor Group"                                    {homekit="OccupancySensor"}
Switch          occupancysensor            "Occupancy Sensor"                     (gOccupancySensor)   {homekit="OccupancyDetectedState"}
Switch          occupancysensor_bat        "Occupancy Sensor Battery"             (gOccupancySensor)   {homekit="BatteryLowStatus"}
Switch          occupancysensor_active     "Occupancy Sensor Active"              (gOccupancySensor)   {homekit="OccupancySensor.ActiveStatus"}
Switch          occupancysensor_fault      "Occupancy Sensor Fault"               (gOccupancySensor)   {homekit="OccupancySensor.FaultStatus"}
Switch          occupancysensor_tampered   "Occupancy Sensor Tampered"            (gOccupancySensor)   {homekit="OccupancySensor.TamperedStatus"}

Group           gContactSensor             "Contact Sensor Group"                                      {homekit="ContactSensor"}
Contact         contactsensor              "Contact Sensor"                       (gContactSensor)     {homekit="ContactSensor.ContactSensorState"}
Switch          contactsensor_bat          "Contact Sensor Battery"               (gContactSensor)     {homekit="ContactSensor.BatteryLowStatus"}
Switch          contactsensor_active       "Contact Sensor Active"                (gContactSensor)     {homekit="ContactSensor.ActiveStatus"}
Switch          contactsensor_fault        "Contact Sensor Fault"                 (gContactSensor)     {homekit="ContactSensor.FaultStatus"}
Switch          contactsensor_tampered     "Contact Sensor Tampered"              (gContactSensor)     {homekit="ContactSensor.TamperedStatus"}

Group           gAirQualitySensor           "Air Quality Sensor"                                       {homekit="AirQualitySensor"}
String          airquality                  "Air Quality"                         (gAirQualitySensor)  {homekit="AirQuality"}
Number          ozone                       "Ozone Density"                       (gAirQualitySensor)  {homekit="OzoneDensity"}
Number          voc                         "VOC Density"                         (gAirQualitySensor)  {homekit="VOCDensity"}
Number          nitrogen                    "Nitrogen Density"                    (gAirQualitySensor)  {homekit="NitrogenDioxideDensity"}
Number          sulphur                     "Sulphur Density"                     (gAirQualitySensor)  {homekit="SulphurDioxideDensity"}
Number          pm25                        "PM25 Density"                        (gAirQualitySensor)  {homekit="PM25Density"}
Number          pm10                        "PM10 Density"                        (gAirQualitySensor)  {homekit="PM10Density"}

Group           gSecuritySystem            "Security System Group"                                     {homekit="SecuritySystem"}
String          security_current_state     "Security Current State"               (gSecuritySystem)    {homekit="SecuritySystem.CurrentSecuritySystemState"}
String          security_target_state      "Security Target State"                (gSecuritySystem)    {homekit="SecuritySystem.TargetSecuritySystemState"}

Group           gCooler                    "Cooler Group"                                              {homekit="HeaterCooler"}
Switch          cooler_active              "Cooler Active"                        (gCooler)            {homekit="ActiveStatus"}
Number          cooler_current_temp        "Cooler Current Temp [%.1f °C]"        (gCooler)            {homekit="CurrentTemperature"}
String          cooler_current_mode        "Cooler Current Mode"                  (gCooler)            {homekit="CurrentHeaterCoolerState" [HEATING="HEAT", COOLING="COOL"]}
String          cooler_target_mode         "Cooler Target Mode"                   (gCooler)            {homekit="TargetHeaterCoolerState"}
Number          cooler_cool_thrs           "Cooler Cool Threshold Temp [%.1f °C]" (gCooler)            {homekit="CoolingThresholdTemperature" [minValue=10.5, maxValue=50]}
Number          cooler_heat_thrs           "Cooler Heat Threshold Temp [%.1f °C]" (gCooler)            {homekit="HeatingThresholdTemperature" [minValue=0.5, maxValue=20]}
```

## Multiple Instances

Homekit has a limitation of 150 accessories per bridge.
The bridge itself counts as an accessory, so in practice it's 149.
In order to overcome this limitation, you can instruct OpenHAB to expose multiple bridges to Homekit, and then manually assign specific accessories to different instances.
You will need to manually add each additional bridge in the Home app, since the QR Code in settings will only be for the primary bridge; however the same PIN is still used.
In order to assign a particular accessory to a different bridge, set the `instance` metadata parameter:

```
Switch kitchen_light {homekit="Lighting" [instance=2]}
```

Note that instances are numbered starting at 1.
If you reference an instance that doesn't exist, then that accessory won't be exposed on _any_ bridge.

For complex items, only the root group needs to be tagged for the specific instance:

```
Group           gSecuritySystem            "Security System Group"                                     {homekit="SecuritySystem" [instance=2]}
String          security_current_state     "Security Current State"               (gSecuritySystem)    {homekit="SecuritySystem.CurrentSecuritySystemState"}
String          security_target_state      "Security Target State"                (gSecuritySystem)    {homekit="SecuritySystem.TargetSecuritySystemState"}
```

## Additional Notes

HomeKit allows only a single pairing to be established with the bridge.
This pairing is normally shared across devices via iCloud.
If you need to establish a new pairing, you will need to clear the existing pairings.
To do this, you can issue the command `openhab:homekit clearPairings` from the [OSGi console](https://www.openhab.org/docs/administration/console.html).
After doing this, you may need to remove the file `$OPENHAB_USERDATA/jsondb/homekit.json` and restart openHAB.

HomeKit requires a unique identifier for each accessory advertised by the bridge.
This unique identifier is hashed from the Item's name.
For that reason, it is important that the name of your Items exposed to HomeKit remain consistent.

HomeKit listens by default on port 9124.
Java prefers the IPv6 network stack by default.
If you have connection or detection problems, you can configure Java to prefer the IPv4 network stack instead.
To prefer the IPv4 network stack, adapt the Java command line arguments to include: `-Djava.net.preferIPv4Stack=true`
Depending on the openHAB installation method, you should modify `start.sh`, `start_debug.sh`, `start.bat`, or `start_debug.bat` (standalone/manual installation) or `EXTRA_JAVA_OPTS` in `/etc/default/openhab2` (Debian installation).

If you encounter any issues with the add-on and need support, it may be important to get detailed logs of your device's communication with openHAB.
In order to get logs from the underlying library used to implement the HomeKit protocol, enable trace logging using the following commands at [the console](https://www.openhab.org/docs/administration/console.html):

```
openhab> log:set TRACE io.github.hapjava
openhab> log:tail io.github.hapjava
```

In order to enable detailed logs of openHAB HomeKit binding

```
openhab> log:set TRACE org.openhab.io.homekit.internal
openhab> log:tail org.openhab.io.homekit.internal
```

## Console commands

`openhab:homekit list` - list all HomeKit accessories currently advertised to the HomeKit clients.

`openhab:homekit show <accessory_id | name>` - print additional details of the accessories which partially match provided ID or name.

## Troubleshooting 

### openHAB is not listed in home app

if you don't see openHAB in the home app, probably multicast DNS (mDNS) traffic is not routed correctly from openHAB to home app device or openHAB is already in paired state. 
You can verify this with [Discovery DNS iOS app](https://apps.apple.com/us/app/discovery-dns-sd-browser/id305441017) as follow: 

- install discovery dns app from app store 
- start discovery app
- find `_hap._tcp`  in the list of service types
- if you don't find _hap._tcp on the list, probably the traffic is blocked. 
  - to confirm this, check whether you can find _openhab-server._tcp. if you don't see it as well, traffic is blocked. check your network router/firewall settings.
- if you found _hap._tcp, open it. you should see the name of your openHAB HomeKit bridge (default name is openHAB)

![discovery_hap_list.png](doc/discovery_hap_list.png)  

- if you don't see openHAB bridge name, the traffic is blocked
- if you see openHAB HomeKit bridge, open it

![discovery_openhab_details.png](doc/discovery_openhab_details.png)

- verify the IP address. it must be the IP address of your openHAB server, if not, set the correct IP address using `networkInterface` settings
- verify the flag "sf". 
  - if sf is equal 1, openHAB is accepting pairing from new iOS device. 
  - if sf is equal 0 (as on screenshot), openHAB is already paired and does not accept any new pairing request. you can reset pairing using `openhab:homekit clearPairings` command in karaf console.
- if you see openHAB bridge and sf is equal 1 but you dont see openHAB in home app, probably you home app still think it is already paired with openHAB. remove your home from home app and restart iOS device.
