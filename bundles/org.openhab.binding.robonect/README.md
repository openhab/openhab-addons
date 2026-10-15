# Robonect Binding

Robonect is a piece of hardware which has to be put into your Husqvarna, Gardena and other branded automower and makes it accessible in your internal network.
More details about the Robonect module can be found at [robonect.de](https://forum.robonect.de/)

This binding integrates mowers having the robonect module installed as a Thing into the home automation solution, allowing to control the mower and react on mower status changes in rules.

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

The following configuration settings are supported for the `mower` Thing.

| parameter name | mandatory | description                                                                                       |
|----------------|-----------|---------------------------------------------------------------------------------------------------|
| host           | yes       | the hostname or ip address of the mower.                                                          |
| pollInterval   | no        | the interval for the binding to poll for mower status information.                                |
| offlineTimeout | no        | the maximum time, the mower can be offline before the binding triggers the offlineTrigger channel |
| user           | no        | the username if authentication is enabled in the firmware.                                        |
| password       | no        | the password if authentication is enabled in the firmware.                                        |
| timezone       | no        | the timezone as configured in Robonect on the robot (default: Europe/Berlin)                      |
| ajaxUiScraping | no        | read job timing and end-of-day mode from the localized Robonect web interface (default: false).   |

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
| `job`                     | Switch               | Starts a job. The channels can be configured with the parameters `remoteStart`, `afterMode`, `duration`, `start` and `end`. `remoteStart` supports `STANDARD` (0), `DOCKING_STATION` (1), and `REMOTE_1` through `REMOTE_5` (2 through 6). `afterMode` is the mode the mower will be set after the job is done. Allowed values are `AUTO`, `HOME` or `EOD`. `duration` is the job duration in minutes. Please note, if the mower is charging it will wait to start the job until it is fully charged, but the jobs duration is already started. |
| `job-start`               | Number:Time          | Start time of the active job in minutes after midnight; requires `ajaxUiScraping`.                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `job-end`                 | Number:Time          | End time of the active job in minutes after midnight; requires `ajaxUiScraping`.                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
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

This channel is triggered if the mower is offline longer than the configured `offlineTriggerTimeout`.
This may indicate that the mower is stuck somewhere in an error state but does not have a signal.

### Additional Robonect Information

Robonect firmware 1.x exposes optional telemetry and configuration channels for battery and motor diagnostics,
statistics, remote-start points, timers, boundary-wire sensors, weather, WLAN, portal, GPIO extensions, door control,
and service operations. Unsupported endpoints are ignored, so these channels can remain unset on older firmware.

The binding creates `battery-<slot>-*` and `wire-sensor-<slot>-*` channels from the returned lists, with slots numbered
from 1. Battery groups expose ID, charge, voltage, current, temperature, full capacity, and remaining capacity; wire
groups expose label, quality, and the `a`, `n`, `f`, `g1`, and `g2` diagnostic values. The existing unnumbered `battery-*`
and remote-start name, distance, and proportion channels are writable where supported by the Robonect API. Remote-start
names are limited to 16 characters; distances are limited to 0-300 metres and proportions to 0-100 percent. The binding
truncates longer names and logs a warning. The visible, path, and corridor-cut channels can be written by enabling the
`useUnofficialApi` Thing configuration. This uses the undocumented web-interface endpoint and may stop working after a
firmware update. Its save request includes the current remote-start settings to preserve values not being changed.
Remaining battery and remote-start telemetry channels are read-only.

Enabling `useUnofficialApi` also creates the writable `push-mode` Number channel with options `0` (Off), `1` (GET),
and `2` (POST). Disabling it removes the channel. The binding reads `/push` and extracts the selected mode radio
input from the returned HTML. Commands use `/push&mode=<value>` and apply the mode selected in the returned HTML
without an additional read. Missing or invalid selections produce `UNDEF`. Mode polling uses
`detailedInfoInterval` even when `detailedInfo` is disabled.

Each push trigger in the response creates `push-trigger-<id>-<name>-enter` and `push-trigger-<id>-<name>-leave` channels.
The name is converted to lowercase and spaces are replaced with underscores, for example `Way home` becomes
`way_home`. For trigger ID 2, the channels are `push-trigger-2-way_home-enter` and `push-trigger-2-way_home-leave`.
Existing Item links must be updated. No separate name channel is created. Triggers with missing or invalid names
are ignored. Duplicate normalized names remain distinct through their numeric IDs. Valid normalized names
contain only lowercase ASCII letters, digits, underscores, and hyphens. Renaming a trigger changes its channel IDs.
The enter and leave Switch channels are writable; commands extract the numeric ID from the channel ID and read the
current settings to preserve the other setting. The `prevent` Number
channel reports the raw mowing prevention code, and the `home` Switch reports whether the mower is at home.

Successful configuration commands (except `push-mode`) and door commands apply the returned channel values
immediately and request a follow-up read after five seconds to fill in data omitted from the response.
A newer command to the same endpoint replaces
its pending follow-up read. Mode changes (including HOME), job commands, and start/stop commands request mower
status immediately after the write returns instead of waiting five seconds.

Timer and job start/end times use the binding's configured `timezone` (default: `Europe/Berlin`). Timer DateTime
commands are converted back to the mower's local time in the same zone.

The `statistics-seek` and `statistics-mowing` String channels contain comma-separated recent durations in minutes.
Each also has `-min`, `-max`, and `-average` `Number:Time` channels with a minute unit hint.

The optional `ajaxUiScraping` configuration reads the localized, undocumented web-interface endpoint. It enables job
start and end times and recognizes end-of-day mode where the JSON API reports only `HOME`. Because this interface can
change with a Robonect firmware update and varies by language, it is disabled by default; failures fall back to normal
JSON polling.

The weather, timer, push, and service channels can change mower configuration. The write-only `push-password` String
channel accepts a new push-service password as a command. Its state remains `UNDEF`; the password cannot be read back.
Automatic state prediction is disabled for this channel; also set `autoupdate="false"` on its Item. The binding applies
the other push settings returned by the password update and reads them again after five seconds. Password request
URLs and responses are excluded from binding logs, but openHAB Item commands can appear in event logs. The example
sitemap includes a plain text password input for testing; it does not mask the password.

To receive a push notification, configure the Robonect push URL as
`http://<openhab-host>:8080/robonect/robonect:mower:<thing-id>`. A received notification triggers an immediate status
refresh; it does not expose the notification payload to openHAB.

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
