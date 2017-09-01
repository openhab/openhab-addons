# Robonect Binding

Robonect is a piece of hardware which has to be put into your Husqvarna, Gardena and other branded automower and makes 
it accessable in your internal network. More details about the Robonect module can be found at [robonect.de](http://www.robonect.de)

This binding integrates mowers having the robonect module installed as a thing into the home automation solution, allowing to
control the mower and react on mower status changes in rules. 

## Supported Things

The binding exposes just one Thing type which is the `mower`.

Tested mowers

| Mower                   | Robonect module  | Robonect firmware version |
|-------------------------|------------------|---------------------------|
| Husqvarna Automower 105 | Robonect Hx      | 0.9c, 0.9e                |
| Husqvarna Automower 315 | Robonect Hx      | 0.9e, 1.0 preview         |

## Discovery

Robonect does not support automatic discovery. So the thing has to be added manually either via Paper UI or things configuration.

## Thing Configuration

following configuration settings are supported for the `mower` thing.

| property name | mandatory | description                                                        |
|---------------|-----------|--------------------------------------------------------------------|
| host          | yes       | the hostname or ip address of the mower.                           |
| pollInterval  | no        | the interval for the binding to poll for mower status information. |
| offlineTimeout| no        | the maximum time, the mower can be offline before the binding triggers the offlineTrigger channel  |
| user          | no        | the username if authentication is enabled in the firmware.         |
| password      | no        | the password if authenticaiton is enabled in the firmware.         |


An example things configuration might look like
```
Thing robonect:mower:automower "Mower" @ "Garden" [ host="192.168.2.1", pollInterval="5", user="gardener", password = "cutter"]
```

## Channels

| Channel ID               | Item Type                 | Description                                                                        |                                                                     
|--------------------------|---------------------------|------------------------------------------------------------------------------------|
| `mowerInfo#name`         | String                    | Retrieves or sets the name of the mower                                            |
| `mowerStatus#battery`    | Number                    | Retrieves the current battery status in percent                                    |
| `mowerStatus#duration`   | Number                    | Retrieves the duration of the current status (see `mowerStatus#status`) of the mower |
| `mowerStatus#hours`      | Number                    | Retrieves the number of hours of mowing operation                                  |
| `mowerStatus#mode`       | String                    | Retrieves or  sets the mode of the mower. Possible values retrieval values are <ul><li>HOME</li><li>AUTO</li><li>MAN</li></ul> In addition he channel allows to set following values for triggering special actions <ul><li>EOD : triggers the "end of day" mode. The mower will switch in to the HOME mode and stay int this mode for the rest of the day</li><li>JOB : The mower will start a job accoriding to the job parameters defined with the channels `job#remoteStart`, `job#afterMoe`, `job#start` and `job#end`</li>  </ul> |
| `mowerStatus#status`     | Number                    | Retrieves the current mower status which can be <ul><li>0 : DETECTING_STATUS</li><li>1 : PARKING</li><li>2 : MOWING</li><li>3 : SEARCH_CHARGING_STATION</li><li>4 : CHARGING</li><li>5 : SEARCHING</li><li>6 : UNKNOWN_6</li><li>7 : ERROR_STATUS</li><li>16 : OFF</li><li>17 : SLEEPING</li><li>98 : OFFLINE (Binding cannot connect to mower)</li><li>99 : UNKNOWN</li></ul> |
| `mowerStatus#started`    | Switch                    | Switches the mower ON (analog to start button on mower) or OFF (analog to stop button on mower). |
| `mowerStatus#offlineTrigger`    | Trigger            | Is triggered if the mower is longer than the configured offlineTriggerTimeout offline. This might help if the mower is somewhere in error state but does not have a signal |
| `job#remoteStart`        | String                    | Sets the remote start type for a JOB. Possible values are <ul><li>STANDARD : use the remote start option defined in the mower settings</li><li>REMOTE_1 : start from remote 1</li><li>REMOTE_2 : start from remote 2</li></ul>
| `job#afterMode`          | String                    | Sets the mode the mower should be put in after a mowing Job. (See JOB on channel `owerStatus#mode`)
| `job#start`              | String                    | Sets the start time for the next job in the form hh:mm  |
| `job#end`                | String                    | Sets the end time for the next job in the form hh:mm |
| `timer#status`           | String                    | Retrieves the status of the timer which can be <ul><li>INACTIVE : no timer set</li><li>ACTIVE - timer set and currently running</li><li>STANDBY - timer set but not triggered/running yet</li></ul> |
| `timer#nextTimer`        | DateTime                  | Retrieves the Date and Time of the next timer set. This is just valid if there is an ACTIVE timer status (see `timer#status`). |
| `wlan#signal`            | Number                    | Retrieves the current WLAN Signal strength in dB |
| `error#code`             | Number                    | The mower manufacturer code in case the mower is in status 7 (error). The binding resets this to UNDEF, once the mower is not in error status anymore. |
| `error#message`          | String                    | The error message in case the mower is in status 7 (error). The binding resets this to UNDEF, once the mower is not in error status anymore. |
| `error#date   `          | DateTime                  | The date and time the error happened. The binding resets this to UNDEF, once the mower is not in error status anymore. |
| `lastError#code`         | Number                    | The mower manufacturer code of the last error happened |
| `lastError#message`      | String                    | The error message of the last error happened |
| `lastError#date   `      | DateTime                  | The date and time of the last error happened |
| `health#temperature`     | Number                    | The temperature of the mower (just available for robonect firmware >= 1.0) |
| `health#humidity   `     | Number                    | The humidity of the mower (just available for robonect firmware >= 1.0) |
| `version#serial`         | String                    | Retrieves the serial number of the Robonect Module |
| `version#version`        | String                    | Retrieves the firmware version of the Robonect Module |
| `version#compiled`       | String                    | Retrieves the compile date and time of the Robonect firmware version |
| `version#comment`        | String                    | Retrieves the firmware version comment |
    

## Full Example

Things file `.things`
```
Thing robonect:mower:automower "Mower" @ "Garden" [ host="192.168.2.1", pollInterval="5", user="gardener", password = "cutter"]
```

Items file `.items`
```
String mowerName "Mower name" {robonect:mower:automower:mowerInfo#name"}
Number mowerBattery "Mower battery [%d %%]" <energy> {channel="robonect:mower:automower:mowerStatus#battery"}
Number mowerHours "Mower operation hours [%d h]" <clock> {channel="robonect:mower:automower:mowerStatus#hours"}
Number mowerDuration "Duration of current mode" {channel="robonect:mower:automower:mowerStatus#duration"}
String mowerMode "Mower mode" {channel="robonect:mower:automower:mowerStatus#mode"}
Number mowerStatus "Mower Status [MAP(robonect_status.map):%s]" {channel="robonect:mower:automower:mowerStatus#status"}
Switch mowerStarted "Mower started" {channel="robonect:mower:automower:mowerStatus#started"}
String mowerTimerStatus "Mower timer status" {channel="robonect:mower:automower:timer#status"}
DateTime mowerNextTimer "Next timer [%1$td/%1$tm %1$tH:%1$tM]" <clock> {channel="robonect:mower:automower:timer#nextTimer"}
Number mowerWlanSignal "WLAN signal [%d dB ]" {channel="robonect:mower:automower:wlan#signal"}
String mowerJobAfterMode "Mode after job execution" {channel="robonect:mower:automower:job#afterMode"}
String mowerJobRemoteStart "Remote start on job execution" {channel="robonect:mower:automower:job#remoteStart"}
String mowerJobStart "Job start time" {channel="robonect:mower:automower:job#start"}
String mowerJobEnd "Job end time" {channel="robonect:mower:automower:job#end"}
Number mowerErrorCode "Error code" {channel="robonect:mower:automower:error#code"}
String mowerErrorMessage "Error message" {channel="robonect:mower:automower:error#message"}
DateTie mowerErrorDate "Error date [%1$td/%1$tm %1$tH:%1$tM]" {channel="robonect:mower:automower:error#date"}
String mowerRobonectSerial "Robonect serialnumber" {channel="robonect:mower:automower:version#serial"}
String mowerRobonectVersion "Robonect version" {channel="robonect:mower:automower:version#version"}
String mowerRobonectVersionComment "Robonect Version comment" {channel="robonect:mower:automower:version#comment"}
```

Map transformation for mower status (`robonect_status.map`)
```
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