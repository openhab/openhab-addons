# Robonect Binding

Robonect is a piece of hardware which has to be put into your Husqvarna, Gardena and other branded automower and makes it accessible in your internal network.
More details about the Robonect module can be found at [robonect.de](https://forum.robonect.de/)

This binding integrates mowers having the robonect module installed as a thing into the home automation solution, allowing to control the mower and react on mower status changes in rules.

## Supported Things

The binding supports one Thing type which is the `mower`.

Tested mowers

| Mower                   | Robonect module  | Robonect firmware version |
|-------------------------|------------------|---------------------------|
| Husqvarna Automower 105 | Robonect Hx      | 0.9c, 0.9e                |
| Husqvarna Automower 315 | Robonect Hx      | 0.9e, 1.0 preview         |
| Husqvarna Automower 320 | Robonect Hx      | 1.0 Beta7a                |
| Husqvarna Automower 420 | Robonect Hx      | 0.9e, 1.0 Beta2           |
| Gardena SILENO city 250 | Robonect Hx      | 1.2                       |

## Discovery

Automatic discovery is not supported.

## Thing Configuration

The following configuration settings are supported for the `mower` thing.

| parameter name | mandatory | description                                                                                       |
|----------------|-----------|---------------------------------------------------------------------------------------------------|
| host           | yes       | the hostname or ip address of the mower.                                                          |
| pollInterval   | no        | the interval for the binding to poll for mower status information.                                |
| offlineTimeout | no        | the maximum time, the mower can be offline before the binding triggers the offlineTrigger channel |
| user           | no        | the username if authentication is enabled in the firmware.                                        |
| password       | no        | the password if authenticaiton is enabled in the firmware.                                        |
| timezone       | no        | the timezone as configured in Robonect on the robot (default: Europe/Berlin)                      |

An example things configuration might look like:

```java
Thing robonect:mower:automower "Mower" @ "Garden" [ host="192.168.2.1", pollInterval="5", user="gardener", password = "cutter"]
```

## Channels

| Channel ID                | Item Type            | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|---------------------------|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `name`                    | String               | Retrieves or sets the name of the mower                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `battery`                 | Number               | Retrieves the current battery status in percent                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `status-duration`         | Number               | Retrieves the duration of the current status (see `status`) of the mower                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| `status-distance`         | Number               | Retrieves the distance of the mower from the charging station when searching for the remote starting point                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `mowing-hours`            | Number               | Retrieves the number of hours of mowing operation                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `mode`                    | String               | Retrieves or  sets the mode of the mower. Possible values retrieval values are <ul><li>HOME</li><li>AUTO</li><li>MANUAL</li><li>EOD : triggers the "end of day" mode. The mower will switch in to the HOME mode and stay int this mode for the rest of the day. After midnight it will switch back to the mode which was set previously.</li></ul>                                                                                                                                                                                       |
| `status`                  | Number               | Retrieves the current mower status which can be <ul><li>0 : DETECTING_STATUS</li><li>1 : PARKING</li><li>2 : MOWING</li><li>3 : SEARCH_CHARGING_STATION</li><li>4 : CHARGING</li><li>5 : SEARCHING</li><li>6 : UNKNOWN_6</li><li>7 : ERROR_STATUS</li><li>16 : OFF</li><li>17 : SLEEPING</li><li>98 : OFFLINE (Binding cannot connect to mower)</li><li>99 : UNKNOWN</li></ul>                                                                                                                                                           |
| `start`                   | Switch               | Starts the mower. ON is started (analog to pressing the start button on mower) or OFF (analog to the stop button on mower).                                                                                                                                                                                                                                                                                                                                                                                                              |
| `job`                     | Switch               | Starts a job. The channels can be configured with the three parameters `remoteStart`, `afterMode` and `duration`. `remoteStart` defines the mowing start point with the corresponding options `REMOTE_1`, `REMOTE_2` and `DEFAULT`. `afterMode` is the mode the mower will be set after the job is done. Allowed values are `AUTO`, `HOME` or `EOD`. `duration` is the job duration in minutes. Please note, if the mower is charging it will wait to start the job until it is fully charged, but the jobs duration is already started. |
| `timer-status`            | String               | Retrieves the status of the timer which can be <ul><li>INACTIVE : no timer set</li><li>ACTIVE - timer set and currently running</li><li>STANDBY - timer set but not triggered/running yet</li></ul>                                                                                                                                                                                                                                                                                                                                      |
| `timer-next`              | DateTime             | Retrieves the Date and Time of the next timer set. This is just valid if there is an ACTIVE timer status (see `timer-status`).                                                                                                                                                                                                                                                                                                                                                                                                           |
| `wlan-signal`             | Number               | Retrieves the current WLAN Signal strength in dB                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `error-code`              | Number               | The mower manufacturer code in case the mower is in status 7 (error). The binding resets this to UNDEF, once the mower is not in error status anymore.                                                                                                                                                                                                                                                                                                                                                                                   |
| `error-message`           | String               | The error message in case the mower is in status 7 (error). The binding resets this to UNDEF, once the mower is not in error status anymore.                                                                                                                                                                                                                                                                                                                                                                                             |
| `error-date`              | DateTime             | The date and time the error happened. The binding resets this to UNDEF, once the mower is not in error status anymore.                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `last-error-code`         | Number               | The mower manufacturer code of the last error happened                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `last-error-message`      | String               | The error message of the last error happened                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `last-error-date`         | DateTime             | The date and time of the last error happened                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `health-temperature`      | Number               | The temperature of the mower (just available for robonect firmware >= 1.0)                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `health-humidity`         | Number               | The humidity of the mower (just available for robonect firmware >= 1.0)                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `blades-quality`          | Number:Dimensionless | The quality of the blades                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `blades-replacement-days` | Number:Time          | The number of days since the blades have been changed                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `blades-usage-hours`      | Number:Time          | The usage time in hours of the blades                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |

### Offline Trigger Channel

This channel s triggered if the mower is longer than the configured `offlineTriggerTimeout` offline.
This may indicate that the mower may stuck somewhere in error state but does not have a signal.

## Full Example

Things file `.things`

```java
Thing robonect:mower:automower "Mower" @ "Garden" [ host="192.168.2.1", pollInterval=5, user="gardener", password = "cutter"]
```

Items file `.items`

```java
String                  mowerName            "Mower name"                                    {channel="robonect:mower:automower:name"}
Number                  mowerBattery         "Mower battery [%d %%]" <energy>                {channel="robonect:mower:automower:battery"}
Number                  mowerHours           "Mower operation hours [%d h]" <clock>          {channel="robonect:mower:automower:mowing-hours"}
Number                  mowerDuration        "Duration of current mode"                      {channel="robonect:mower:automower:status-duration"}
String                  mowerMode            "Mower mode"                                    {channel="robonect:mower:automower:mode"}
Number                  mowerStatus          "Mower Status [MAP(robonect_status.map):%s]"    {channel="robonect:mower:automower:status"}
Switch                  mowerStarted         "Mower started"                                 {channel="robonect:mower:automower:started"}
String                  mowerTimerStatus     "Mower timer status"                            {channel="robonect:mower:automower:timer-status"}
DateTime                mowerNextTimer       "Next timer [%1$td/%1$tm %1$tH:%1$tM]" <clock>  {channel="robonect:mower:automower:timer-next"}
Number                  mowerWlanSignal      "WLAN signal [%d dB ]"                          {channel="robonect:mower:automower:wlan-signal"}
Switch                  mowerOneHourJob      "Start mowing for one hour from now"            {channel="robonect:mower:automower:job",remoteStart=REMOTE_1,afterMode=AUTO,duration=60}
Number                  mowerErrorCode       "Error code"                                    {channel="robonect:mower:automower:error-code"}
String                  mowerErrorMessage    "Error message"                                 {channel="robonect:mower:automower:error-message"}
DateTime                mowerErrorDate       "Error date [%1$td/%1$tm %1$tH:%1$tM]"          {channel="robonect:mower:automower:error-date"}
Number:Dimensionless    mowerBladesQuality   "Blades quality [%d %%]"                        {channel="robonect:mower:automower:blades-quality"}
Number:Time             mowerBladesDays      "Days since last blade change [%d d]"                      {channel="robonect:mower:automower:blades-replacement-days"}
Number:Time             mowerBladesHours     "Blades usage in hours [%d h]"                      {channel="robonect:mower:automower:blades-usage-hours"}
```

Map transformation for mower status (`robonect_status.map`)

```text
0=DETECTING_STATUS
1=PARKING
2=MOWING
3=SEARCH_CHARGING_STATION
4=CHARGING
5=SEARCHING
7=ERROR_STATUS
8=LOST_SIGNAL
16=OFF
17=SLEEPING
99=UNKNOWN
```
