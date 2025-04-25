# HAI by Leviton OmniLink Binding

This binding integrates the [Omni and Lumina](https://www.leviton.com/en/products/security-automation/automation-av-controllers/omni-security-systems) line of home automation systems.
At its core the Omni is a hardware board that provides security and access features.
It connects to many other devices through serial ports or wired contacts and exposes them through a single TCP based API.

## Supported Things

The OmniPro/Lumina controller acts as a "bridge" for accessing other connected devices.

| Omni type                  | Hardware Type                                    | Things                            |
| :------------------------- | :----------------------------------------------- | :-------------------------------- |
| Controller                 | Omni (Pro II, IIe, LTe), Lumina                  | `controller` (omni, lumina)       |
| Lights                     | Built-in, UPB, HLC                               | `unit`, `dimmable`, `upb`, `room` |
| Thermostats                | Omnistat, Omnistat2                              | `thermostat`                      |
| Temperature Sensors        | 31A00-1/31A00-7                                  | `temp_sensor`                     |
| Humidity Sensors           | 31A00-2                                          | `humidity_sensor`                 |
| Zones                      | Built-in/Hardwire, GE Wireless                   | `zone`                            |
| Audio Zones/Sources        | HAI Hi-Fi, Russound, NuVo, Xantech, Speakercraft | `audio_zone`, `audio_source`      |
| Consoles                   | HAI Omni Console, HAI Lumina Console             | `console`                         |
| Areas                      | Built-in                                         | `area`, `lumina_area`             |
| Buttons                    | Built-in                                         | `button`                          |
| Flags                      | Built-in                                         | `flag`                            |
| Output                     | Built-in/Hardwire                                | `output`                          |
| Access Control Reader Lock | Leviton Access Control Reader                    | `lock`                            |

## Discovery

### Controller

Omni and Lumina controllers must be manually added using the IP and port of the controller as well as the 2 encryption keys required for network access.

### Devices

Once a connection can be established to a controller, all connected devices will be automatically discovered and added to the inbox.

## Thing Configuration

<!-- markdownlint-disable MD038 -->
An Omni or Lumina controller requires the IP address (`ipAddress`), optional port (`port` defaults to 4369), and 2 encryption keys (`key1`, `key2`).
The hexadecimal pairs in the encryption keys are typically delimited using a colon`:`, but dashes `-`, spaces ` ` or no delimiter may be used.
<!-- markdownlint-enable MD038 -->

In the thing file, this looks like:

```java
Bridge omnilink:controller:home [ ipAddress="127.0.0.1", port=4369, key1="XXXXXXXXXXXXXXXX", key2="XXXXXXXXXXXXXXXX" ] {
    // Add your things here
}
```

The devices are identified by the device number that the OmniLink bridge assigns to them, see the [Full Example](#full-example) section below for a manual configuration example.

## Channels

The devices support some of the following channels:

| Channel Type ID             | Item Type            | Description                                                                                  | Thing types supporting this channel                 |
| --------------------------- | -------------------- | -------------------------------------------------------------------------------------------- | --------------------------------------------------- |
| `activate_keypad_emergency` | Number               | Activate a burglary, fire, or auxiliary keypad emergency alarm on Omni based models.         | `area`                                              |
| `alarm_burglary`            | Switch               | Indicates if a burglary alarm is active.                                                     | `area`                                              |
| `alarm_fire`                | Switch               | Indicates if a fire alarm is active.                                                         | `area`                                              |
| `alarm_gas`                 | Switch               | Indicates if a gas alarm is active.                                                          | `area`                                              |
| `alarm_auxiliary`           | Switch               | Indicates if an auxiliary alarm is active.                                                   | `area`                                              |
| `alarm_freeze`              | Switch               | Indicates if a freeze alarm is active.                                                       | `area`                                              |
| `alarm_water`               | Switch               | Indicates if a water alarm is active.                                                        | `area`                                              |
| `alarm_duress`              | Switch               | Indicates if a duress alarm is active.                                                       | `area`                                              |
| `alarm_temperature`         | Switch               | Indicates if a temperature alarm is active.                                                  | `area`                                              |
| `mode`                      | Number               | Represents the area security mode.                                                           | `area`, `lumina_area`                               |
| `disarm`                    | String               | Send a 4 digit user code to disarm the system.                                               | `area`                                              |
| `day`                       | String               | Send a 4 digit user code to arm the system to day.                                           | `area`                                              |
| `night`                     | String               | Send a 4 digit user code to arm the system to night.                                         | `area`                                              |
| `away`                      | String               | Send a 4 digit user code to arm the system to away.                                          | `area`                                              |
| `vacation`                  | String               | Send a 4 digit user code to arm the system to vacation.                                      | `area`                                              |
| `day_instant`               | String               | Send a 4 digit user code to arm the system to day instant.                                   | `area`                                              |
| `night_delayed`             | String               | Send a 4 digit user code to arm the system to night delayed.                                 | `area`                                              |
| `home`                      | String               | Send a 4 digit user code to set the system to home.                                          | `lumina_area`                                       |
| `sleep`                     | String               | Send a 4 digit user code to set the system to sleep.                                         | `lumina_area`                                       |
| `away`                      | String               | Send a 4 digit user code to set the system to away.                                          | `lumina_area`                                       |
| `vacation`                  | String               | Send a 4 digit user code to set the system to vacation.                                      | `lumina_area`                                       |
| `party`                     | String               | Send a 4 digit user code to set the system to party.                                         | `lumina_area`                                       |
| `special`                   | String               | Send a 4 digit user code to set the system to special.                                       | `lumina_area`                                       |
| `source_text_{1,2,3,4,5,6}` | String               | A line of metadata from this audio source.                                                   | `audio_source`                                      |
| `polling`                   | Switch               | Enable or disable polling of this audio source.                                              | `audio_source`                                      |
| `zone_power`                | Switch               | Power status of this audio zone.                                                             | `audio_zone`                                        |
| `zone_mute`                 | Switch               | Mute status of this audio zone.                                                              | `audio_zone`                                        |
| `zone_volume`               | Dimmer               | Volume level of this audio zone.                                                             | `audio_zone`                                        |
| `zone_source`               | Number               | Source for this audio zone.                                                                  | `audio_zone`                                        |
| `zone_control`              | Player               | Control the audio zone, e.g. start/stop/next/previous.                                       | `audio_zone`                                        |
| `system_date`               | DateTime             | Controller date/time. See [Rule Actions](#rule-actions) for how to set controller date/time. | `controller`                                        |
| `last_log`                  | String               | Last log message on the controller, represented in JSON.                                     | `controller`                                        |
| `enable_disable_beeper`     | Switch               | Enable/Disable the beeper for this/all console(s).                                           | `controller`, `console`                             |
| `beep`                      | Switch               | Send a beep command to this/all console(s).                                                  | `controller`, `console`                             |
| `press`                     | Switch               | Sends a button event to the controller.                                                      | `button`                                            |
| `low_setpoint`              | Number               | The current low setpoint for this humidity/temperature sensor.                               | `temp_sensor`, `humidity_sensor`                    |
| `high_setpoint`             | Number               | The current high setpoint for this humidity/temperature sensor.                              | `temp_sensor`, `humidity_sensor`                    |
| `temperature`               | Number:Temperature   | The current temperature at this thermostat/temperature sensor.                               | `thermostat`, `temp_sensor`                         |
| `humidity`                  | Number:Dimensionless | The current relative humidity at this thermostat/humidity sensor.                            | `thermostat`, `humidity_sensor`                     |
| `freeze_alarm`              | Contact              | Closed when freeze alarm is triggered by this thermostat.                                    | `thermostat`                                        |
| `comm_failure`              | Contact              | Closed during a communications failure with this thermostat.                                 | `thermostat`                                        |
| `outdoor_temperature`       | Number:Temperature   | The current outdoor temperature detected by this thermostat.                                 | `thermostat`                                        |
| `heat_setpoint`             | Number:Temperature   | The current low/heating setpoint of this thermostat.                                         | `thermostat`                                        |
| `cool_setpoint`             | Number:Temperature   | The current high/cooling setpoint of this thermostat.                                        | `thermostat`                                        |
| `humidify_setpoint`         | Number:Dimensionless | The current low/humidify setpoint for this thermostat.                                       | `thermostat`                                        |
| `dehumidify_setpoint`       | Number:Dimensionless | The current high/dehumidify setpoint for this thermostat.                                    | `thermostat`                                        |
| `system_mode`               | Number               | The current system mode of this thermostat.                                                  | `thermostat`                                        |
| `fan_mode`                  | Number               | The current fan mode of this thermostat.                                                     | `thermostat`                                        |
| `hold_status`               | Number               | The current hold status of this thermostat.                                                  | `thermostat`                                        |
| `status`                    | Number               | The current numeric status of this thermostat.                                               | `thermostat`                                        |
| `level`                     | Dimmer               | Increase/Decrease the level of this unit/dimmable unit/UPB unit.                             | `unit`, `dimmable`, `upb`                           |
| `switch`                    | Switch               | Turn this unit/dimmable unit/flag/output/room on/off.                                        | `unit`, `dimmable`, `upb`, `flag`, `output`, `room` |
| `on_for_seconds`            | Number               | Turn on this unit for a specified number of seconds.                                         | `unit`, `dimmable`, `upb`, `flag`, `output`         |
| `off_for_seconds`           | Number               | Turn off this unit for a specified number of seconds.                                        | `unit`, `dimmable`, `upb`, `flag`, `output`         |
| `on_for_minutes`            | Number               | Turn on this unit for a specified number of minutes.                                         | `unit`, `dimmable`, `upb`, `flag`, `output`         |
| `off_for_minutes`           | Number               | Turn off this unit for a specified number of minutes.                                        | `unit`, `dimmable`, `upb`, `flag`, `output`         |
| `on_for_hours`              | Number               | Turn on this unit for a specified number of hours.                                           | `unit`, `dimmable`, `upb`, `flag`, `output`         |
| `off_for_hours`             | Number               | Turn off this unit for a specified number of hours.                                          | `unit`, `dimmable`, `upb`, `flag`, `output`         |
| `upb_status`                | String               | Send a UPB status request message for this UPB unit to the controller.                       | `upb`                                               |
| `value`                     | Number               | Numeric value of this flag.                                                                  | `flag`                                              |
| `scene_{a,b,c,d}`           | Switch               | Turn this scene on/off.                                                                      | `room`                                              |
| `state`                     | Number               | The current state of this room.                                                              | `room`                                              |
| `contact`                   | Contact              | Contact state information of this zone.                                                      | `zone`                                              |
| `current_condition`         | Number               | Current condition of this zone.                                                              | `zone`                                              |
| `latched_alarm_status`      | Number               | Latched alarm status of this zone.                                                           | `zone`                                              |
| `arming_status`             | Number               | Arming status of this zone.                                                                  | `zone`                                              |
| `bypass`                    | String               | Send a 4 digit user code to bypass this zone.                                                | `zone`                                              |
| `restore`                   | String               | Send a 4 digit user code to restore this zone.                                               | `zone`                                              |

### Trigger Channels

The devices support some of the following trigger channels:

| Channel Type ID              | Description                                                           | Thing types supporting this channel |
| ---------------------------- | --------------------------------------------------------------------- | ----------------------------------- |
| `all_on_off_event`           | Event sent when an all on/off event occurs.                           | `area`, `lumina_area`               |
| `phone_line_event`           | Event sent when the phone line changes state.                         | `controller`                        |
| `ac_power_event`             | Event sent when AC trouble conditions are detected.                   | `controller`                        |
| `battery_event`              | Event sent when battery trouble conditions are detected.              | `controller`                        |
| `dcm_event`                  | Event sent when digital communicator trouble conditions are detected. | `controller`                        |
| `energy_cost_event`          | Event sent when the cost of energy changes.                           | `controller`                        |
| `camera_trigger_event`       | Event sent when a camera trigger is detected.                         | `controller`                        |
| `upb_link_activated_event`   | Event sent when a UPB link is activated.                              | `controller`                        |
| `upb_link_deactivated_event` | Event sent when a UPB link is deactivated.                            | `controller`                        |
| `activated_event`            | Event sent when a button is activated.                                | `button`                            |
| `switch_press_event`         | Event sent when an ALC, UPB, Radio RA, or Starlite switch is pressed. | `dimmable`, `upb`                   |

## Rule Actions

This binding includes a rule action, which allows synchronizing the controller time to match the openHAB system time with a user specified zone.
There is a separate instance for each contoller, which can be retrieved through:

:::: tabs

::: tab DSL

```java
val omnilinkActions = getActions("omnilink", "omnilink:controller:home")
```

:::

::: tab JavaScript

```javascript
var omnilinkActions = actions.get("omnilink", "omnilink:controller:home");
```

:::

::: tab JRuby

In JRuby, Action methods are available directly on the Thing object.

```ruby
omni_link = things["omnilink:controller:home"]
```

:::

::::

where the first parameter always has to be `omnilink` and the second is the full Thing UID of the controller that should be used.
Once this action instance is retrieved, you can invoke the `synchronizeControllerTime(String zone)` method on it:

:::: tabs

::: tab DSL

```java
omnilinkActions.synchronizeControllerTime("America/Denver")
```

:::

::: tab JavaScript

```javascript
omnilinkActions.synchronizeControllerTime("America/Denver");
```

:::

::: tab JRuby

```ruby
omni_link.synchronize_controller_time("America/Denver")
```

:::

::::

## Full Example

### Example `omnilink.things`

```java
Bridge omnilink:controller:home [ ipAddress="127.0.0.1", port=4369, key1="XXXXXXXXXXXXXXXX", key2="XXXXXXXXXXXXXXXX" ] {
    Thing area         MainArea         "Main Area"              @   "Home"                    [ number=1 ]
    Thing upb          UpKitTable       "Table Lights"           @   "Upstairs Kitchen"        [ number=4 ]
    Thing upb          UpOfcDesk        "Desk Lights"            @   "Upstairs Office"         [ number=10 ]
    Thing thermostat   UpstrsThermo     "Upstairs Temperature"   @   "Upstairs Entry"          [ number=1 ]
    Thing zone         FrontDoor        "Front Door"             @   "Upstairs Entry"          [ number=2 ]
    Thing zone         GarageDoor       "Garage Door"            @   "Laundry Room"            [ number=3 ]
    Thing zone         BackDoor         "Back Door"              @   "Upstairs Kitchen"        [ number=4 ]
    Thing zone         OneCarGarageDo   "One Car Garage"         @   "Garage"                  [ number=5 ]
    Thing zone         TwoCarGarageDo   "Two Car Garage"         @   "Garage"                  [ number=6 ]
    Thing zone         BsmtBackDoor     "Back Door"              @   "Basement Workout Room"   [ number=8 ]
    Thing zone         MBRDeckDoor      "Deck Door"              @   "Master Bedroom"          [ number=9 ]
    Thing zone         MBRMotion        "Motion"                 @   "Master Bedroom"          [ number=10 ]
    Thing zone         PorchDoor        "Porch Door"             @   "Upstairs Office"         [ number=11 ]
    Thing zone         UpOffMotion      "Motion"                 @   "Upstairs Office"         [ number=12 ]
    Thing zone         UpLivMotion      "Motion"                 @   "Upstairs Living Room"    [ number=13 ]
    Thing zone         BsmtWORMotion    "Motion"                 @   "Basement Workout Room"   [ number=14 ]
    Thing zone         GarageMotion     "Motion"                 @   "Garage"                  [ number=15 ]
    Thing console      UpstrsConsole    "Console"                @   "Laundry Room"            [ number=1 ]
    Thing button       MainButton       "Button"                 @   "Home"                    [ number=1 ]
}
```

### Example `omnilink.items`

```java
/*
 * Alarms / Areas
 */
Group:Switch:OR(ON, OFF) Alarms "All Alarms [%s]"
String    AlarmMode          "Alarm [%s]"              <alarm>               {channel="omnilink:area:home:MainArea:mode" [profile="transform:MAP", function="area-modes.map", sourceFormat="%s"]}
Switch    AlarmBurglary      "Burglary Alarm [%s]"               (Alarms)    {channel="omnilink:area:home:MainArea:alarm_burglary"}
Switch    AlarmFire          "Fire Alarm [%s]"                   (Alarms)    {channel="omnilink:area:home:MainArea:alarm_fire"}
Switch    alarm_gas          "Gas Alarm [%s]"                    (Alarms)    {channel="omnilink:area:home:MainArea:alarm_gas"}
Switch    AlarmAuxiliary     "Auxiliary Alarm [%s]"              (Alarms)    {channel="omnilink:area:home:MainArea:alarm_auxiliary"}
Switch    AlarmFreeze        "Freeze Alarm [%s]"                 (Alarms)    {channel="omnilink:area:home:MainArea:alarm_freeze"}
Switch    AlarmWater         "Water Alarm [%s]"                  (Alarms)    {channel="omnilink:area:home:MainArea:alarm_water"}
Switch    AlarmDuress        "Duress Alarm [%s]"                 (Alarms)    {channel="omnilink:area:home:MainArea:alarm_duress"}
Switch    AlarmTemperature   "Temperature Alarm [%s]"            (Alarms)    {channel="omnilink:area:home:MainArea:alarm_temperature"}
Number    AlarmModeDisarm                                                    {channel="omnilink:area:home:MainArea:disarm"}
Number    AlarmModeDay                                                       {channel="omnilink:area:home:MainArea:day"}
Number    AlarmModeNight                                                     {channel="omnilink:area:home:MainArea:night"}
Number    AlarmModeAway                                                      {channel="omnilink:area:home:MainArea:away"}
Number    AlarmModeVacation                                                  {channel="omnilink:area:home:MainArea:vacation"}
Number    AlarmModeDayInstant                                                {channel="omnilink:area:home:MainArea:day_instant"}
Number    AlarmModeNightDelayed                                              {channel="omnilink:area:home:MainArea:night_delayed"}

/*
 * Lights
 */
Switch   UpKitTable     "Table Lights [%s]"   <switch>   {channel="omnilink:upb:home:UpKitTable:level"}
Dimmer   UpOfcDesk      "Desk Lights [%d]"    <slider>   {channel="omnilink:upb:home:UpOfcDesk:level"}

/*
 * Thermostat
 */
Group                UpstrsThermo             "Upstairs Thermostat"
Number:Temperature   UpstrsThermo_Temp        "Temperature [%.1f %unit%]"                  <temperature>        (UpstrsThermo)   {channel="omnilink:thermostat:home:UpstrsThermo:temperature"}
Number               UpstrsThermo_Status      "Status [MAP(therm-status.map):%s]"          <heating>            (UpstrsThermo)   {channel="omnilink:thermostat:home:UpstrsThermo:status"}
Number               UpstrsThermo_System      "System Mode [MAP(therm-tempmode.map):%s]"   <temperature>        (UpstrsThermo)   {channel="omnilink:thermostat:home:UpstrsThermo:system_mode"}
Number               UpstrsThermo_Fan         "Fan Mode [MAP(therm-fanmode.map):%s]"       <fan>                (UpstrsThermo)   {channel="omnilink:thermostat:home:UpstrsThermo:fan_mode"}
Number               UpstrsThermo_Hold        "Hold Mode [MAP(therm-holdmode.map):%s]"     <fan>                (UpstrsThermo)   {channel="omnilink:thermostat:home:UpstrsThermo:hold_mode"}
Number               UpstrsThermo_HeatPoint   "System HeatPoint [%d]"                      <temperature_hot>    (UpstrsThermo)   {channel="omnilink:thermostat:home:UpstrsThermo:heat_setpoint"}
Number               UpstrsThermo_CoolPoint   "System CoolPoint [%d]"                      <temperature_cool>   (UpstrsThermo)   {channel="omnilink:thermostat:home:UpstrsThermo:cool_setpoint"}

/*
 * Motion and Doors
 */
Group:Contact:OR(OPEN, CLOSED)   Doors         "All Doors [%s]"
Contact   FrontDoor        "Front Door"            <door>         (Doors)            {channel="omnilink:zone:home:FrontDoor:contact"}
Contact   GarageDoor       "Garage Door"           <door>         (Doors)            {channel="omnilink:zone:home:GarageDoor:contact"}
Contact   BackDoor         "Back Door"             <door>         (Doors)            {channel="omnilink:zone:home:BackDoor:contact"}
Contact   BsmtBackDoor     "Back Door"             <door>         (Doors)            {channel="omnilink:zone:home:BsmtBackDoor:contact"}
Contact   MBRDeckDoor      "Deck Door"             <door>         (Doors)            {channel="omnilink:zone:home:MBRDeckDoor:contact"}
Contact   PorchDoor        "Porch Door"            <door>         (Doors)            {channel="omnilink:zone:home:PorchDoor:contact"}

Group:Contact:OR(OPEN, CLOSED)   GarageDoors   "All Garage Doors [%s]"
Contact   TwoCarGarageDo   "Two Car Garage Door"   <garagedoor>   (GarageDoors)      {channel="omnilink:zone:home:TwoCarGarageDo:contact"}
Contact   OneCarGarageDo   "One Car Garage Door"   <garagedoor>   (GarageDoors)      {channel="omnilink:zone:home:OneCarGarageDo:contact"}

Group:Contact:OR(OPEN, CLOSED)   Motion        "All Motion Sensors [%s]"
Contact   MBRMotion        "Motion"                <presence>     (Motion)           {homekit="MotionSensor", channel="omnilink:zone:home:MBRMotion:contact"}
Contact   UpOffMotion      "Motion"                <presence>     (Motion)           {homekit="MotionSensor", channel="omnilink:zone:home:UpOffMotion:contact"}
Contact   UpLivMotion      "Motion"                <presence>     (Motion)           {homekit="MotionSensor", channel="omnilink:zone:home:UpLivMotion:contact"}
Contact   BsmtWORMotion    "Motion"                <presence>     (Motion)           {homekit="MotionSensor", channel="omnilink:zone:home:BsmtWORMotion:contact"}
Contact   GarageMotion     "Motion"                <presence>     (Motion)           {homekit="MotionSensor", channel="omnilink:zone:home:GarageMotion:contact"}

/*
 * Console
 */
String     UpstrsConsole_Beeper   "Enable/Disable Beeper [%s]"            {channel="omnilink:console:home:UpstrsConsole:enable_disable_beeper"}
Number     UpstrsConsole_Beep     "Beep Console"                          {channel="omnilink:console:home:UpstrsConsole:beep"}

/*
 * Button
 */
Switch   MainButton   "Toggle button [%s]"   <switch>   {channel="omnilink:button:home:MainButton:press"}

/*
 * Other OmniPro items
 */
DateTime   OmniProTime   "Last Time Update [%1$ta %1$tR]"   <time>   {channel="omnilink:controller:home:system_date"}
```

### Example `therm-status.map`

```text
0=Idle
1=Heating
2=Cooling
```

### Example `therm-tempmode.map`

```text
0=Off
1=Heat
2=Cool
3=Auto
5=Emergency heat
```

### Example `therm-fanmode.map`

```text
0=Auto
1=On
2=Cycle
```

### Example `therm-holdmode.map`

```text
0=Off
1=Hold
2=Vacation hold
```

### Example `area-modes.map`

```text
0=Off
1=Day
2=Night
3=Away
4=Vacation
5=Day instant
6=Night delayed
9=Arming day
10=Arming night
11=Arming away
12=Arming vacation
13=Arming day instant
14=Arming night delay
=Unknown
```
