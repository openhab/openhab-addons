# Nest Binding

The Nest binding integrates devices by [Nest](https://nest.com) using the [Nest API](https://developers.nest.com/documentation/cloud/get-started) (REST).

Because the Nest API runs on Nest's servers a connection with the Internet is required for sending and receiving information.
The binding uses HTTPS to connect to the Nest API using ports 443 and 9553. Make sure outbound connections to these ports are not blocked by a firewall.

## Supported Things

The table below lists the Nest binding thing types:

| Things                                  | Description                                                                                                                                    | Thing Type     |
|-----------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|----------------|
| Nest Account                            | An account for using the Nest REST API                                                                                                         | account        |
| Nest Cam (Indoor, IQ, Outdoor), Dropcam | A Nest Cam registered with your account                                                                                                        | camera         |
| Nest Protect                            | The smoke detector/Nest Protect for the account                                                                                                | smoke_detector |
| Structure                               | The Nest structure defines the house the account has setup on Nest. You will only have more than one structure if you have more than one house | structure      |
| Nest Thermostat (E)                     | A Thermostat to control the various aspects of the house's HVAC system                                                                         | thermostat     |

## Authorization

The Nest API uses OAuth for authorization.
Therefor the binding needs some authorization parameters before it can access your Nest account via the Nest API.

To get these authorization parameters you first need to sign up as a [Nest Developer](https://developer.nest.com) and [register a new Product](https://developer.nest.com/products/new) (free and instant).

While registering a new Product (on the Product Details page) make sure to:

*   Leave both "OAuth Redirect URI" fields empty to enable PIN-based authorization.
*   Grant all the permissions you intend to use. When in doubt, enable the permission because the binding needs to be reauthorized when permissions change at a later time.

After creating the Product, your browser shows the Product Overview page.
This page contains the **Product ID** and **Product Secret** authorization parameters that are used by the binding.
Take note of both parameters or keep this page open in a browser tab.
Now copy and paste the "Authorization URL" in a new browser tab.
Accept the permissions and you will be presented the **Pincode** authorization parameter that is also used by the binding.

You can return to the Product Overview page at a later time by opening the [Products](https://console.developers.nest.com/products) page and selecting your Product.

## Discovery

The binding will discover all Nest Things from your account when you add and configure a "Nest Account" Thing.
See the Authorization paragraph above for details on how to obtain the Product ID, Product Secret and Pincode configuration parameters.

Once the binding has successfully authorized with the Nest API, it obtains an Access Token using the Pincode.
The configured Pincode is cleared because it can only be used once.
The obtained Access Token is saved as an advanced configuration parameter of the "Nest Account".

You can reuse an Access Token for authorization but not the Pincode.
A new Pincode can again be generated via the "Authorization URL" (see Authorization paragraph).

## Channels

### Account Channels

The account Thing Type does not have any channels.

### Camera Channels

**Camera group channels**

Information about the camera.

| Channel Type ID       | Item Type | Description                                       | Read Write |
|-----------------------|-----------|---------------------------------------------------|:----------:|
| app_url               | String    | The app URL to see the camera                     |      R     |
| audio_input_enabled   | Switch    | If the audio input is currently enabled           |      R     |
| last_online_change    | DateTime  | Timestamp of the last online status change        |      R     |
| public_share_enabled  | Switch    | If public sharing is currently enabled            |      R     |
| public_share_url      | String    | The URL to see the public share of the camera     |      R     |
| snapshot_url          | String    | The URL to use for a snapshot of the video stream |      R     |
| streaming             | Switch    | If the camera is currently streaming              |     R/W    |
| video_history_enabled | Switch    | If the video history is currently enabled         |      R     |
| web_url               | String    | The web URL to see the camera                     |      R     |

**Last event group channels**

Information about the last camera event (requires Nest Aware subscription).

| Channel Type ID    | Item Type | Description                                                                        | Read Write |
|--------------------|-----------|------------------------------------------------------------------------------------|:----------:|
| activity_zones     | String    | Identifiers for activity zones that detected the event (comma separated)           |      R     |
| animated_image_url | String    | The URL showing an animated image for the camera event                             |      R     |
| app_url            | String    | The app URL for the camera event, allows you to see the camera event in an app     |      R     |
| end_time           | DateTime  | Timestamp when the camera event ended                                              |      R     |
| has_motion         | Switch    | If motion was detected in the camera event                                         |      R     |
| has_person         | Switch    | If a person was detected in the camera event                                       |      R     |
| has_sound          | Switch    | If sound was detected in the camera event                                          |      R     |
| image_url          | String    | The URL showing an image for the camera event                                      |      R     |
| start_time         | DateTime  | Timestamp when the camera event started                                            |      R     |
| urls_expire_time   | DateTime  | Timestamp when the camera event URLs expire                                        |      R     |
| web_url            | String    | The web URL for the camera event, allows you to see the camera event in a web page |      R     |

### Smoke Detector Channels

| Channel Type ID       | Item Type | Description                                                                       | Read Write |
|-----------------------|-----------|-----------------------------------------------------------------------------------|:----------:|
| co_alarm_state        | String    | The carbon monoxide alarm state of the Nest Protect (OK, EMERGENCY, WARNING)      |      R     |
| last_connection       | DateTime  | Timestamp of the last successful interaction with Nest                            |      R     |
| last_manual_test_time | DateTime  | Timestamp of the last successful manual test                                      |      R     |
| low_battery           | Switch    | Reports whether the battery of the Nest protect is low (if it is battery powered) |      R     |
| manual_test_active    | Switch    | Manual test active at the moment                                                  |      R     |
| smoke_alarm_state     | String    | The smoke alarm state of the Nest Protect (OK, EMERGENCY, WARNING)                |      R     |
| ui_color_state        | String    | The current color of the ring on the smoke detector (GRAY, GREEN, YELLOW, RED)    |      R     |

### Structure Channels

| Channel Type ID              | Item Type | Description                                                                                            | Read Write |
|------------------------------|-----------|--------------------------------------------------------------------------------------------------------|:----------:|
| away                         | String    | Away state of the structure (HOME, AWAY)                                                               |     R/W    |
| country_code                 | String    | Country code of the structure ([ISO 3166-1 alpha-2](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2)) |      R     |
| co_alarm_state               | String    | Carbon Monoxide alarm state (OK, EMERGENCY, WARNING)                                                   |      R     |
| eta_begin                    | DateTime  | Estimated time of arrival at home, will setup the heat to turn on and be warm                          |      R     |
| peak_period_end_time         | DateTime  | Peak period end for the Rush Hour Rewards program                                                      |      R     |
| peak_period_start_time       | DateTime  | Peak period start for the Rush Hour Rewards program                                                    |      R     |
| postal_code                  | String    | Postal code of the structure                                                                           |      R     |
| rush_hour_rewards_enrollment | Switch    | If rush hour rewards system is enabled or not                                                          |      R     |
| security_state               | String    | Security state of the structure (OK, DETER)                                                            |      R     |
| smoke_alarm_state            | String    | Smoke alarm state (OK, EMERGENCY, WARNING)                                                             |      R     |
| time_zone                    | String    | The time zone for the structure ([IANA time zone format](http://www.iana.org/time-zones))              |      R     |

### Thermostat Channels

| Channel Type ID             | Item Type            | Description                                                                            | Read Write |
|-----------------------------|----------------------|----------------------------------------------------------------------------------------|:----------:|
| can_cool                    | Switch               | If the thermostat can actually turn on cooling                                         |      R     |
| can_heat                    | Switch               | If the thermostat can actually turn on heating                                         |      R     |
| eco_max_set_point           | Number:Temperature   | The eco range max set point temperature                                                |      R     |
| eco_min_set_point           | Number:Temperature   | The eco range min set point temperature                                                |      R     |
| fan_timer_active            | Switch               | If the fan timer is engaged                                                            |     R/W    |
| fan_timer_duration          | Number:Time          | Length of time that the fan is set to run (15, 30, 45, 60, 120, 240, 480, 960 minutes) |     R/W    |
| fan_timer_timeout           | DateTime             | Timestamp when the fan stops running                                                   |      R     |
| has_fan                     | Switch               | If the thermostat can control the fan                                                  |      R     |
| has_leaf                    | Switch               | If the thermostat is currently in a leaf mode                                          |      R     |
| humidity                    | Number:Dimensionless | Indicates the current relative humidity                                                |      R     |
| last_connection             | DateTime             | Timestamp of the last successful interaction with Nest                                 |      R     |
| locked                      | Switch               | If the thermostat has the temperature locked to only be within a set range             |      R     |
| locked_max_set_point        | Number:Temperature   | The locked range max set point temperature                                             |      R     |
| locked_min_set_point        | Number:Temperature   | The locked range min set point temperature                                             |      R     |
| max_set_point               | Number:Temperature   | The max set point temperature                                                          |     R/W    |
| min_set_point               | Number:Temperature   | The min set point temperature                                                          |     R/W    |
| mode                        | String               | Current mode of the Nest thermostat (HEAT, COOL, HEAT_COOL, ECO, OFF)                  |     R/W    |
| previous_mode               | String               | The previous mode of the Nest thermostat (HEAT, COOL, HEAT_COOL, ECO, OFF)             |      R     |
| state                       | String               | The active state of the Nest thermostat (HEATING, COOLING, OFF)                        |      R     |
| temperature                 | Number:Temperature   | Current temperature                                                                    |      R     |
| time_to_target              | Number:Time          | Time left to the target temperature approximately                                      |      R     |
| set_point                   | Number:Temperature   | The set point temperature                                                              |     R/W    |
| sunlight_correction_active  | Switch               | If sunlight correction is active                                                       |      R     |
| sunlight_correction_enabled | Switch               | If sunlight correction is enabled                                                      |      R     |
| using_emergency_heat        | Switch               | If the system is currently using emergency heat                                        |      R     |

Note that the Nest API rounds Thermostat values so they will differ from what shows up in the Nest App.
The Nest API applies the following rounding:

*   degrees Celsius to 0.5 degrees
*   degrees Fahrenheit to whole degrees
*   humidity to 5%

## Example

You can use the discovery functionality of the binding to obtain the deviceId and structureId values for defining Nest things in files.

Another way to get the deviceId and structureId values is by querying the Nest API yourself. First [obtain an Access Token](https://developers.nest.com/documentation/cloud/sample-code-auth) (or use the Access Token obtained by the binding).
Then use it with one of the [API Read Examples](https://developers.nest.com/documentation/cloud/how-to-read-data).

### demo.things:

```
Bridge nest:account:demo_account [ productId="8fdf9885-ca07-4252-1aa3-f3d5ca9589e0", productSecret="QITLR3iyUlWaj9dbvCxsCKp4f", accessToken="c.6rse1xtRk2UANErcY0XazaqPHgbvSSB6owOrbZrZ6IXrmqhsr9QTmcfaiLX1l0ULvlI5xLp01xmKeiojHqozLQbNM8yfITj1LSdK28zsUft1aKKH2mDlOeoqZKBdVIsxyZk4orH0AvKEZ5aY" ] {
    camera         fish_cam           [ deviceId="qw0NNE8ruxA9AGJkTaFH3KeUiJaONWKiH9Gh3RwwhHClonIexTtufQ" ]
    smoke_detector hallway_smoke      [ deviceId="Tzvibaa3lLKnHpvpi9OQeCI_z5rfkBAV" ]
    structure      home               [ structureId="20wKjydArmMV3kOluTA7JRcZg8HKBzTR-G_2nRXuIN1Bd6laGLOJQw" ]
    thermostat     living_thermostat  [ deviceId="ZqAKzSv6TO6PjBnOCXf9LSI_z5rfkBAV" ]
}
```

### demo.items:


```
/* Camera */
String   Cam_App_URL               "App URL [%s]"                                                      { channel="nest:camera:demo_account:fish_cam:camera#app_url" }
Switch   Cam_Audio_Input_Enabled   "Audio Input Enabled"                                               { channel="nest:camera:demo_account:fish_cam:camera#audio_input_enabled" }
DateTime Cam_Last_Online_Change    "Last Online Change [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"          { channel="nest:camera:demo_account:fish_cam:camera#last_online_change" }
String   Cam_Snapshot_URL          "Snapshot URL [%s]"                                                 { channel="nest:camera:demo_account:fish_cam:camera#snapshot_url" }
Switch   Cam_Streaming             "Streaming"                                                         { channel="nest:camera:demo_account:fish_cam:camera#streaming" }
Switch   Cam_Public_Share_Enabled  "Public Share Enabled"                                              { channel="nest:camera:demo_account:fish_cam:camera#public_share_enabled" }
String   Cam_Public_Share_URL      "Public Share URL [%s]"                                             { channel="nest:camera:demo_account:fish_cam:camera#public_share_url" }
Switch   Cam_Video_History_Enabled "Video History Enabled"                                             { channel="nest:camera:demo_account:fish_cam:camera#video_history_enabled" }
String   Cam_Web_URL               "Web URL [%s]"                                                      { channel="nest:camera:demo_account:fish_cam:camera#web_url" }
String   Cam_LE_Activity_Zones     "Last Event Activity Zones [%s]"                                    { channel="nest:camera:demo_account:fish_cam:last_event#activity_zones" }
String   Cam_LE_Animated_Image_URL "Last Event Animated Image URL [%s]"                                { channel="nest:camera:demo_account:fish_cam:last_event#animated_image_url" }
String   Cam_LE_App_URL            "Last Event App URL [%s]"                                           { channel="nest:camera:demo_account:fish_cam:last_event#app_url" }
DateTime Cam_LE_End_Time           "Last Event End Time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"         { channel="nest:camera:demo_account:fish_cam:last_event#end_time" }
Switch   Cam_LE_Has_Motion         "Last Event Has Motion"                                             { channel="nest:camera:demo_account:fish_cam:last_event#has_motion" }
Switch   Cam_LE_Has_Person         "Last Event Has Person"                                             { channel="nest:camera:demo_account:fish_cam:last_event#has_person" }
Switch   Cam_LE_Has_Sound          "Last Event Has Sound"                                              { channel="nest:camera:demo_account:fish_cam:last_event#has_sound" }
String   Cam_LE_Image_URL          "Last Event Image URL [%s]"                                         { channel="nest:camera:demo_account:fish_cam:last_event#image_url" }
DateTime Cam_LE_Start_Time         "Last Event Start Time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"       { channel="nest:camera:demo_account:fish_cam:last_event#start_time" }
DateTime Cam_LE_URLs_Expire_Time   "Last Event URLs Expire Time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" { channel="nest:camera:demo_account:fish_cam:last_event#urls_expire_time" }
String   Cam_LE_Web_URL            "Last Event Web URL [%s]"                                           { channel="nest:camera:demo_account:fish_cam:last_event#web_url" }

/* Smoke Detector */
String   Smoke_CO_Alarm            "CO Alarm [%s]"                                            { channel="nest:smoke_detector:demo_account:hallway_smoke:co_alarm_state" }
Switch   Smoke_Battery_Low         "Battery Low"                                              { channel="nest:smoke_detector:demo_account:hallway_smoke:low_battery" }
Switch   Smoke_Manual_Test         "Manual Test"                                              { channel="nest:smoke_detector:demo_account:hallway_smoke:manual_test_active" }
DateTime Smoke_Last_Connection     "Last Connection [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"    { channel="nest:smoke_detector:demo_account:hallway_smoke:last_connection" }
DateTime Smoke_Last_Manual_Test    "Last Manual Test [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"   { channel="nest:smoke_detector:demo_account:hallway_smoke:last_manual_test_time" }
String   Smoke_Smoke_Alarm         "Smoke Alarm [%s]"                                         { channel="nest:smoke_detector:demo_account:hallway_smoke:smoke_alarm_state" }
String   Smoke_UI_Color            "UI Color [%s]"                                            { channel="nest:smoke_detector:demo_account:hallway_smoke:ui_color_state" }

/* Thermostat */
Switch   Thermostat_Can_Cool       "Can Cool"                                                 { channel="nest:thermostat:demo_account:living_thermostat:can_cool" }
Switch   Thermostat_Can_Heat       "Can Heat"                                                 { channel="nest:thermostat:demo_account:living_thermostat:can_heat" }
Number:Temperature Therm_EMaxSP    "Eco Max Set Point [%.1f %unit%]"                          { channel="nest:thermostat:demo_account:living_thermostat:eco_max_set_point" }
Number:Temperature Therm_EMinSP    "Eco Min Set Point [%.1f %unit%]"                          { channel="nest:thermostat:demo_account:living_thermostat:eco_min_set_point" }
Switch   Thermostat_FT_Active      "Fan Timer Active"                                         { channel="nest:thermostat:demo_account:living_thermostat:fan_timer_active" }
Number:Time Thermostat_FT_Duration "Fan Timer Duration [%d %unit%]"                           { channel="nest:thermostat:demo_account:living_thermostat:fan_timer_duration" }
DateTime Thermostat_FT_Timeout     "Fan Timer Timeout [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"  { channel="nest:thermostat:demo_account:living_thermostat:fan_timer_timeout" }
Switch   Thermostat_Has_Fan        "Has Fan"                                                  { channel="nest:thermostat:demo_account:living_thermostat:has_fan" }
Switch   Thermostat_Has_Leaf       "Has Leaf"                                                 { channel="nest:thermostat:demo_account:living_thermostat:has_leaf" }
Number:Dimensionless Therm_Hum     "Humidity [%.1f %unit%]"                                   { channel="nest:thermostat:demo_account:living_thermostat:humidity" }
DateTime Thermostat_Last_Conn      "Last Connection [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"    { channel="nest:thermostat:demo_account:living_thermostat:last_connection" }
Switch   Thermostat_Locked         "Locked"                                                   { channel="nest:thermostat:demo_account:living_thermostat:locked" }
Number:Temperature Therm_LMaxSP    "Locked Max Set Point [%.1f %unit%]"                       { channel="nest:thermostat:demo_account:living_thermostat:locked_max_set_point" }
Number:Temperature Therm_LMinSP    "Locked Min Set Point [%.1f %unit%]"                       { channel="nest:thermostat:demo_account:living_thermostat:locked_min_set_point" }
Number:Temperature Therm_Max_SP    "Max Set Point [%.1f %unit%]"                              { channel="nest:thermostat:demo_account:living_thermostat:max_set_point" }
Number:Temperature Therm_Min_SP    "Min Set Point [%.1f %unit%]"                              { channel="nest:thermostat:demo_account:living_thermostat:min_set_point" }
String   Thermostat_Mode           "Mode [%s]"                                                { channel="nest:thermostat:demo_account:living_thermostat:mode" }
String   Thermostat_Previous_Mode  "Previous Mode [%s]"                                       { channel="nest:thermostat:demo_account:living_thermostat:previous_mode" }
String   Thermostat_State          "State [%s]"                                               { channel="nest:thermostat:demo_account:living_thermostat:state" }
Number:Temperature Thermostat_SP   "Set Point [%.1f %unit%]"                                  { channel="nest:thermostat:demo_account:living_thermostat:set_point" }
Switch   Thermostat_Sunlight_CA    "Sunlight Correction Active"                               { channel="nest:thermostat:demo_account:living_thermostat:sunlight_correction_active" }
Switch   Thermostat_Sunlight_CE    "Sunlight Correction Enabled"                              { channel="nest:thermostat:demo_account:living_thermostat:sunlight_correction_enabled" }
Number:Temperature Therm_Temp      "Temperature [%.1f %unit%]"                                { channel="nest:thermostat:demo_account:living_thermostat:temperature" }
Number:Time Therm_Time_To_Target   "Time To Target [%d %unit%]"                               { channel="nest:thermostat:demo_account:living_thermostat:time_to_target" }
Switch   Thermostat_Using_Em_Heat  "Using Emergency Heat"                                     { channel="nest:thermostat:demo_account:living_thermostat:using_emergency_heat" }

/* Structure */
String   Home_Away                 "Away [%s]"                                                { channel="nest:structure:demo_account:home:away" }
String   Home_Country_Code         "Country Code [%s]"                                        { channel="nest:structure:demo_account:home:country_code" }
String   Home_CO_Alarm_State       "CO Alarm State [%s]"                                      { channel="nest:structure:demo_account:home:co_alarm_state" }
DateTime Home_ETA                  "ETA [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"                { channel="nest:structure:demo_account:home:eta_begin" }
DateTime Home_PP_End_Time          "PP End Time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"        { channel="nest:structure:demo_account:home:peak_period_end_time" }
DateTime Home_PP_Start_Time        "PP Start Time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"      { channel="nest:structure:demo_account:home:peak_period_start_time" }
String   Home_Postal_Code          "Postal Code [%s]"                                         { channel="nest:structure:demo_account:home:postal_code" }
Switch   Home_Rush_Hour_Rewards    "Rush Hour Rewards"                                        { channel="nest:structure:demo_account:home:rush_hour_rewards_enrollment" }
String   Home_Security_State       "Security State [%s]"                                      { channel="nest:structure:demo_account:home:security_state" }
String   Home_Smoke_Alarm_State    "Smoke Alarm State [%s]"                                   { channel="nest:structure:demo_account:home:smoke_alarm_state" }
String   Home_Time_Zone            "Time Zone [%s]"                                           { channel="nest:structure:demo_account:home:time_zone" }
```

## Attribution

This documentation contains parts written by John Cocula which were copied from the 1.0 Nest binding.
