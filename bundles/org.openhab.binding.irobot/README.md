# iRobot Binding

This binding provides integration of products by iRobot company (<https://www.irobot.com/>). It is currently developed
to support Roomba vacuum cleaner/mopping robots with built-in Wi-Fi module. The binding interfaces to the robot directly
without any need for a dedicated MQTT server.

## Supported Things

- iRobot Roomba robotic vacuum cleaner (<https://www.irobot.com/roomba>).
- iRobot Braava has also been reported to (partially) work.
- In general, the channel list is far from complete. There is a lot to do now.

## Discovery

Roombas on the same network will be discovered automatically, however in order to connect to them a password is needed. The
password is a machine-generated string, which is unfortunately not exposed by the original iRobot smartphone application,
but it can be downloaded from the robot itself. If no password is configured, the Thing enters "CONFIGURATION PENDING" state.
Now you need to perform authorization by pressing and holding the HOME button on your robot until it plays series of tones
(approximately 2 seconds). The Wi-Fi indicator on the robot will flash for 30 seconds, the binding should automatically
receive the password and go ONLINE.

After you've done this procedure you can write the password somewhere in case if you need to reconfigure your binding. It's
not known, however, whether the password is eternal or can change during factory reset.
If you have issues getting the password make sure there are no other devices like your smartphone communicating with the robot.
You can also try using [these python scripts](https://github.com/NickWaterton/Roomba980-Python) to get the password.

## Thing Configuration

| Parameter | Type    | Required  | Default  | Description       |
| --------- | :-----: | :-------: | :------: | ----------------- |
| ipaddress | String  | Yes       |          | Robot IP address  |
| blid      | String  | No        |          | Robot ID          |
| password  | String  | No        |          | Robot Password    |

All parameters will be autodiscovered. If using textual configuration, then `ipaddress` shall be specified.

## Channels

| channel       | type   | description                                                               | Read-only |
|---------------|--------|---------------------------------------------------------------------------|-----------|
| command       | String | Command to execute: clean, spot, dock, pause, stop                        | N |
| cycle         | String | Current mission: none, clean, spot                                        | Y |
| phase         | String | Current phase of the mission; see below.                                  | Y |
| battery       | Number | Battery charge in percents                                                | Y |
| bin           | String | Bin status: ok, removed, full                                             | Y |
| error         | String | Error code; see below                                                     | Y |
| rssi          | Number | Wi-Fi Received Signal Strength indicator in db                            | Y |
| snr           | Number | Wi-Fi Signal to noise ratio                                               | Y |
| sched_mon     | Switch | Scheduled clean enabled for Monday                                        | N |
| sched_tue     | Switch | Scheduled clean enabled for Tuesday                                       | N |
| sched_wed     | Switch | Scheduled clean enabled for Wednesday                                     | N |
| sched_thu     | Switch | Scheduled clean enabled for Thursday                                      | N |
| sched_fri     | Switch | Scheduled clean enabled for Friday                                        | N |
| sched_sat     | Switch | Scheduled clean enabled for Saturday                                      | N |
| sched_sun     | Switch | Scheduled clean enabled for Sunday                                        | N |
| schedule      | Number | Schedule bitmask for use in scripts. 7 bits, bit #0 corresponds to Sunday | N |
| edge_clean    | Switch | Seek out and clean along walls and furniture legs                         | N |
| always_finish | Switch | Whether to keep cleaning if the bin becomes full                          | N |
| power_boost   | String | Power boost mode: "auto", "performance", "eco"                            | N |
| clean_passes  | String | Number of cleaning passes: "auto", "1", "2"                               | N |
| map_upload    | Switch | Enable or disable uploading Clean Map(tm) to cloud for notifications      | N |
| last_command  | String | Json string containing the parameters of the last executed command        | N |

Known phase strings and their meanings:

| phase     | Meaning                           |
|-----------|-----------------------------------|
| charge    | Charging                          |
| new       | New Mission (*)                   |
| run       | Running                           |
| resume    | Resumed (*)                       |
| hmMidMsn  | Going for recharge during mission |
| recharge  | Recharging                        |
| stuck     | Stuck                             |
| mUsrDock  | Going home (on user command)      |
| dock      | Docking (*)                       |
| dockend   | Docking - End Mission (*)         |
| cancelled | Cancelled (*)                     |
| stop      | Stopped                           |
| pause     | Paused (*)                        |
| hmPostMsn | Going home after mission          |
| "" (empty string) | None (*)                  |

Phases, marked with asterisk (*), have not been seen being reported by Roomba 930. All the definitions
are taken from Roomba980-Python.

Error codes. Data type is string in order to be able to utilize mapping to human-readable strings.

| Code | Meaning                    |
|------|----------------------------|
| 0    | None                       |
| 1    | Left wheel off floor       |
| 2    | Main Brushes stuck         |
| 3    | Right wheel off floor      |
| 4    | Left wheel stuck           |
| 5    | Right wheel stuck          |
| 6    | Stuck near a cliff         |
| 7    | Left wheel error           |
| 8    | Bin error                  |
| 9    | Bumper stuck               |
| 10    | Right wheel error         |
| 11    | Bin error                 |
| 12    | Cliff sensor issue        |
| 13    | Both wheels off floor     |
| 14    | Bin missing               |
| 15    | Reboot required           |
| 16    | Bumped unexpectedly       |
| 17    | Path blocked              |
| 18    | Docking issue             |
| 19    | Undocking issue           |
| 20    | Docking issue             |
| 21    | Navigation problem        |
| 22    | Navigation problem        |
| 23    | Battery issue             |
| 24    | Navigation problem        |
| 25    | Reboot required           |
| 26    | Vacuum problem            |
| 27    | Vacuum problem            |
| 29    | Software update needed    |
| 30    | Vacuum problem            |
| 31    | Reboot required           |
| 32    | Smart map problem         |
| 33    | Path blocked              |
| 34    | Reboot required           |
| 35    | Unrecognized cleaning pad |
| 36    | Bin full                  |
| 37    | Tank needed refilling     |
| 38    | Vacuum problem            |
| 39    | Reboot required           |
| 40    | Navigation problem        |
| 41    | Timed out                 |
| 42    | Localization problem      |
| 43    | Navigation problem        |
| 44    | Pump issue                |
| 45    | Lid open                  |
| 46    | Low battery               |
| 47    | Reboot required           |
| 48    | Path blocked              |
| 52    | Pad required attention    |
| 65    | Hardware problem detected |
| 66    | Low memory                |
| 68    | Hardware problem detected |
| 73    | Pad type changed          |
| 74    | Max area reached          |
| 75    | Navigation problem        |
| 76    | Hardware problem detected |

## Cleaning specific regions

You can clean one or many specific regions of a given map by sending the following String to the command channel:

```text
cleanRegions:<pmapId>;[r=]<region_id1>,[r=]<region_id2>,z=<zone_id1>,...;[<user_pmapv_id>]
```

Some devices support cleaning rooms (aka regions). Additionally, support for cleaning rectangle areas previously defined in the iRobot-App (aka zones) may be available.
If the type string such as `r=` (region) or `z=` (zone) is omnitted, the type defaults to region.

The easiest way to determine the pmapId, region_ids/zoneids and userPmapvId is to monitor the last_command channel while starting a new mission for the specific region or zone with the iRobot-App.

## Known Problems / Caveats

1. Sending "pause" command during missions other than "clean" is equivalent to sending "stop"
1. Switching to "spot" mission is possible only in "stop" state. Attempt to do it otherwise causes error: the command is rejected and error tones are played.
1. Roomba's built-in MQTT server, used for communication, supports only a single local connection at a time. Bear this in mind when you want to do something that requires local connection from your phone, like reconfiguring the network. Disable openHAB Thing before doing this.
1. Sometimes during intensive testing Roomba just stopped communicating over the local connection. If this happens, try rebooting it. On my robot it's done by holding "Clean" button for about 10 seconds until all the LEDs come on. Release the button and the reboot tone will be played. It looks like there are some bugs in the firmware.

## Example

irobot.things:

```java
Thing irobot:roomba:my_roomba [ ipaddress="192.168.0.5", password="xxxxxxxx" ]
```

irobot.items:

```java
String Roomba_Command { channel="irobot:roomba:my_roomba:command" }
String Roomba_Cycle { channel="irobot:roomba:my_roomba:cycle" }
String Roomba_Phase { channel="irobot:roomba:my_roomba:phase" }
Number Roomba_Battery { channel="irobot:roomba:my_roomba:battery" }
String Roomba_Bin { channel="irobot:roomba:my_roomba:bin" }
String Roomba_Error { channel="irobot:roomba:my_roomba:error" }
```

irobot.sitemap:

```perl
Selection item=Roomba_Command mappings=["clean"="Clean", "spot"="Spot", dock="Dock", pause="Pause", stop="Stop"]
Text item=Roomba_Cycle label="Current cycle"
Text item=Roomba_Phase label="Current phase"
Text item=Roomba_Battery label="Battery charge [%d %%]"
Text item=Roomba_Bin label="Bin status"
Text item=Roomba_Error label="Error"
```

## Credits

This code is a result of development of an abandoned draft by hkunh42 (<https://github.com/hkuhn42/openhab2.roomba>)
and heavily uses the following projects as a reference:

- Roomba980-Python by Nick Waterton (<https://github.com/NickWaterton/Roomba980-Python>)
- Dorita980 by Facu ZAK (<https://github.com/koalazak/dorita980>)
