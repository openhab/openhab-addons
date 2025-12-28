# Powermax Binding

Visonic produces the Powermax alarm panel series (PowerMax, PowerMax+, PowerMaxExpress, PowerMaxPro and PowerMaxComplete) and the Powermaster alarm series (PowerMaster 10 and PowerMaster 30).
This binding lets you control the alarm panel (arm/disarm) and use Visonic sensors (motion, door contact, etc.) within openHAB.

The PowerMax provides support for a serial interface.
This serial interface is not installed by default but can be ordered from any PowerMax vendor (called the Visonic RS-232 Adapter Kit).

If your alarm panel is connected directly to a serial port of your openHAB server (or to a USB port through a serial-to-USB converter), you must set up a serial connection Thing type in openHAB.

You don't even need to have your alarm panel connected directly to your openHAB server.
For example, you can connect it to a Raspberry Pi and use the [ser2net](https://sourceforge.net/projects/ser2net/) Linux tool to make the serial connection available on the LAN (serial over IP).
In this case, you must set up an IP connection Thing type in openHAB.

Here is an example ser2net.conf you can use to share your serial port /dev/ttyUSB0 on IP port 4444:

```text
4444:raw:0:/dev/ttyUSB0:9600 8DATABITS NONE 1STOPBIT
```

Visonic does not provide a specification of the RS-232 protocol; thus, the binding uses the available protocol specification given at the [domoticaforum](https://www.domoticaforum.eu/viewtopic.php?f=68&t=6581).

The binding implementation of this protocol is largely inspired by the [Vera plugin](http://code.mios.com/trac/mios_visonic-powermax).

## Supported Things

This binding supports the following Thing types:

| Thing  | Thing Type | Description                                                                      |
|--------|------------|----------------------------------------------------------------------------------|
| ip     | Bridge     | The IP connection to the alarm system.                                           |
| serial | Bridge     | The serial connection to the alarm system.                                       |
| x10    | Thing      | An X10 device.                                                                   |
| zone   | Thing      | A zone representing a physical device such as a door, window or a motion sensor. |

## Discovery

The alarm system is not discovered automatically.
First, manually create a bridge Thing—either of type IP or serial—depending on how your openHAB server is connected to the alarm system.
Then the binding will automatically discover all zones and X10 devices that are set up in your alarm system.

## Binding configuration

There are no global binding configuration settings that need to be set.
All settings are provided through Thing configuration parameters.

## Thing Configuration

### IP connection

The IP bridge Thing requires the following configuration parameters:

| Parameter Label     | Parameter ID      | Description                                                                                                               | Required | Default     |
|---------------------|-------------------|---------------------------------------------------------------------------------------------------------------------------|----------|-------------|
| IP address          | ip                | The IP address to use for connecting to the Ethernet interface of the alarm system                                        | true     |             |
| TCP port            | tcpPort           | The TCP port to use for connecting to the Ethernet interface of the alarm system.                                         | true     |             |
| Motion reset delay  | motionOffDelay    | The delay, in minutes, to reset a motion detection                                                                        | false    | 3           |
| Allow arming        | allowArming       | Enable or disable arming the alarm system from openHAB.                                                                   | false    | false       |
| Allow disarming     | allowDisarming    | Enable or disable disarming the alarm system from openHAB.                                                                | false    | false       |
| PIN code            | pinCode           | The PIN code to use for arming/disarming the alarm system from openHAB. Not required unless Powerlink mode cannot be used | false    |             |
| Force standard mode | forceStandardMode | Force standard mode rather than trying to use Powerlink mode                                                              | false    | false       |
| Panel type          | panelType         | The panel type (only required when forcing standard mode)                                                                 | false    | PowerMaxPro |
| Sync time           | autoSyncTime      | Automatically sync time at openHAB startup                                                                                | false    | true        |

### Serial connection

The serial bridge Thing requires the following configuration parameters:

| Parameter Label     | Parameter ID      | Description                                                                                                                     | Required | Default     |
|---------------------|-------------------|---------------------------------------------------------------------------------------------------------------------------------|----------|-------------|
| Serial port         | serialPort        | The serial port used to connect to the alarm system (e.g., COM1 for Windows; /dev/ttyS0 or /dev/ttyUSB0 for Linux)              | true     |             |
| Motion reset delay  | motionOffDelay    | The delay in minutes to reset a motion detection.                                                                               | false    | 3           |
| Allow arming        | allowArming       | Enable or disable arming the alarm system from openHAB.                                                                         | false    | false       |
| Allow disarming     | allowDisarming    | Enable or disable disarming the alarm system from openHAB.                                                                      | false    | false       |
| PIN code            | pinCode           | The PIN code to use for arming/disarming the alarm system from openHAB. Not required except when Powerlink mode cannot be used. | false    |             |
| Force standard mode | forceStandardMode | Force the standard mode rather than trying using the Powerlink mode.                                                            | false    | false       |
| Panel type          | panelType         | Define the panel type. Only required when forcing the standard mode.                                                            | false    | PowerMaxPro |
| Sync time           | autoSyncTime      | Automatic sync time at openHAB startup.                                                                                         | false    | true        |

Notes:

- On Linux, you may get an error stating the serial port cannot be opened when the Powermax binding tries to load.
  You can resolve this by adding the `openhab` user to the `dialout` group, for example: `usermod -a -G dialout openhab`.
- Also on Linux you may have issues with the USB if using two serial USB devices e.g. Powermax and RFXcom.
  See the [general documentation about serial port configuration](/docs/administration/serial.html) for more on symlinking the USB ports.

### X10 device

The X10 Thing requires the following configuration parameters:

| Parameter Label | Parameter ID | Description        | Required |
|-----------------|--------------|--------------------|----------|
| Device number   | deviceNumber | The device number. | true     |

### Zone

The zone Thing requires the following configuration parameters:

| Parameter Label | Parameter ID | Description      | Required |
|-----------------|--------------|------------------|----------|
| Zone number     | zoneNumber   | The zone number. | true     |

## Channels

The following channels are available:

| Thing Types | Channel Type ID              | Item Type | Access Mode | Description                                                                                              |
|-------------|------------------------------|-----------|-------------|----------------------------------------------------------------------------------------------------------|
| ip / serial | system_status                | String    | R           | A short status summary of the system                                                                     |
| ip / serial | system_armed                 | Switch    | RW          | Whether or not the system is armed                                                                       |
| ip / serial | arm_mode                     | String    | RW          | System arm mode (accepted values: Disarmed, Stay, Armed, StayInstant, ArmedInstant, Night, NightInstant) |
| ip / serial | alarm_active                 | Switch    | R           | Whether or not an alarm is active                                                                        |
| ip / serial | ready                        | Switch    | R           | Whether or not the system is ready for arming                                                            |
| ip / serial | with_zones_bypassed          | Switch    | R           | Whether or not at least one zone is bypassed                                                             |
| ip / serial | trouble                      | Switch    | R           | Whether or not a trouble is detected                                                                     |
| ip / serial | alert_in_memory              | Switch    | R           | Whether or not an alert is saved in system memory                                                        |
| ip / serial | pgm_status                   | Switch    | RW          | PGM status                                                                                               |
| ip / serial | mode                         | String    | R           | System current mode (Standard, Powerlink or Download)                                                    |
| ip / serial | event_log_1 ... event_log_10 | String    | R           | Event log entry (1 is the most recent)                                                                   |
| ip / serial | update_event_logs            | Switch    | W           | Switch command to update the event logs                                                                  |
| ip / serial | download_setup               | Switch    | W           | Switch command to download the setup                                                                     |
| x10         | x10_status                   | String    | RW          | X10 device status (accepted values: OFF, ON, DIM and BRIGHT)                                             |
| zone        | tripped                      | Contact   | R           | Whether or not the zone is tripped                                                                       |
| zone        | armed                        | Switch    | R           | Whether or not the zone is armed                                                                         |
| zone        | last_trip                    | DateTime  | R           | Timestamp when the zone was last tripped                                                                 |
| zone        | low_battery                  | Switch    | R           | Whether or not the sensor battery is low                                                                 |
| zone        | bypassed                     | Switch    | RW          | Whether or not the zone is bypassed                                                                      |

## Console Commands

The binding provides few specific commands you can use in the console.
Enter the command `openhab:powermax` to get the usage of each available command.

```shell
Usage: openhab:powermax <bridgeUID> info_setup - information on setup
Usage: openhab:powermax <bridgeUID> download_setup - download setup
```

The command `info_setup` displays information about your current panel setup.
The command `download_setup` will trigger a new download of the panel setup.

Example command: `openhab:powermax powermax:serial:home info_setup`.

## Notes and limitations

- For Powerlink mode to work, the enrollment procedure must be followed.
  If you don't enroll Powerlink on the PowerMax, the binding will operate in Standard mode; if enrolled, it will operate in Powerlink mode.
  On newer software versions of the PowerMax, Powerlink enrollment is automatic, and the binding should operate only in Powerlink mode (if enrollment is successful).
- In Powerlink mode, the binding downloads the panel setup at startup.
  When openHAB starts, this download may fail on a Raspberry Pi for unclear reasons (possibly because many services start simultaneously).
  A retry mechanism will attempt up to three times with a delay of one minute between attempts.
  In most cases the download eventually succeeds.
  If it still fails, you can trigger the download later by using the `download_setup` channel or the console command.
- Visonic does not provide a specification of the RS-232 protocol; use this binding at your own risk.
- The binding is not able to arm/disarm a particular partition.
- The compatibility of the binding with the Powermaster alarm panel series is probably only partial.
- In order to be able to bypass zones, you must first enable this feature by updating your panel configuration. Look at your installer's manual.

## Full Example

demo.things:

```java
Bridge powermax:serial:home "Alarm Home" [ serialPort="/dev/ttyUSB0", allowArming=true, panelType="PowerMaxProPart", autoSyncTime=true ] {
    Thing zone kitchen "Window kitchen" [ zoneNumber=9 ]
    Thing x10 lamp1 "Lamp 1" [ deviceNumber=1 ]
}
```

demo.items:

```java
Switch SystemArmed "System armed" {channel="powermax:serial:home:system_armed", autoupdate="false"}
String SystemArmMode "System arm mode" {channel="powermax:serial:home:arm_mode", autoupdate="false"}
Switch PGM "PGM" {channel="powermax:serial:home:pgm_status", autoupdate="false"}
String SystemStatus "System status" {channel="powermax:serial:home:system_status"}
Switch SystemReady "System ready" {channel="powermax:serial:home:ready"}
Switch WithZonesBypassed "With zones bypassed" {channel="powermax:serial:home:with_zones_bypassed"}
Switch AlarmActive "Alarm active" {channel="powermax:serial:home:alarm_active"}
Switch Trouble "Trouble detected" {channel="powermax:serial:home:trouble"}
Switch AlertInMem "Alert in memory" {channel="powermax:serial:home:alert_in_memory"}
String SystemMode "System mode" {channel="powermax:serial:home:mode"}
Switch UpdateEventLogs "Update Event logs" {channel="powermax:serial:home:update_event_logs", autoupdate="false"}
String EventLog1 "Event log 1" {channel="powermax:serial:home:event_log_1"}
String EventLog2 "Event log 2" {channel="powermax:serial:home:event_log_2"}
String EventLog3 "Event log 3" {channel="powermax:serial:home:event_log_3"}
String EventLog4 "Event log 4" {channel="powermax:serial:home:event_log_4"}
String EventLog5 "Event log 5" {channel="powermax:serial:home:event_log_5"}
String EventLog6 "Event log 6" {channel="powermax:serial:home:event_log_6"}
String EventLog7 "Event log 7" {channel="powermax:serial:home:event_log_7"}
String EventLog8 "Event log 8" {channel="powermax:serial:home:event_log_8"}
String EventLog9 "Event log 9" {channel="powermax:serial:home:event_log_9"}
String EventLog10 "Event log 10" {channel="powermax:serial:home:event_log_10"}
Switch DownloadSetup "Download setup" {channel="powermax:serial:home:download_setup", autoupdate="false"}

Contact WindowKitchenTripped "Tripped [%s]" <contact> {channel="powermax:zone:home:kitchen:tripped"}
Switch WindowKitchenArmed "Armed" {channel="powermax:zone:home:kitchen:armed"}
DateTime WindowKitchenLastTrip "Last trip" <clock> {channel="powermax:zone:home:kitchen:last_trip"}
Switch WindowKitchenLowBattery "Low battery" {channel="powermax:zone:home:kitchen:low_battery"}
Switch WindowKitchenBypassed "Bypassed" {channel="powermax:zone:home:kitchen:bypassed", autoupdate="false"}

String Lamp1 "Lamp 1" {channel="powermax:x10:home:lamp1:x10_status", autoupdate="false"}
```
