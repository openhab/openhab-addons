# Paradox Alarm System binding

This binding is intended to provide basic support for Paradox Alarm system.

With the power of openHAB this binding can be used for complex decision rules combining motion/magnetic sensor or whole partitions states with different scenarios.

Examples:

- All partitions are armed, therefore there is no one at home.
- Window is opened for more than 10 minutes and temperature outside is bellow XXX degrees, send mail/any other supported notification to particular people.

## Supported Paradox panels/systems

Currently binding supports the following panels: EVO192, EVO48(not tested), EVO96(not tested)

## Supported things

| Thing      | Thing Type | Description                                                    |
|------------|------------|----------------------------------------------------------------|
| ip150      | Bridge     | The bridge is used to communicate with IP150 ethernet module attached to Paradox security system.|
| panel      | Thing      | This is representation of Paradox panel. Has the general information about the main panel module, i.e. serial number, firmware/hardware/software versions, panel type, etc...|
| partition  | Thing      | The partition is grouped aggregation of multiple zones. It's also referred in Paradox Babyware as "Area". |
| zone       | Thing      | Paradox zone. Can be anything - magnetic, motion or any other opened/closed sensor. State channel is contact, "low battery" and "is tampered" channels are switch, label is String |

## Things configuration

### IP150 bridge parameters

| Parameter         | Description                            |
|-------------------|----------------------------------------|
| refresh           | Value is in seconds. Defines the refresh interval when the binding polls from paradox system. Optional parameter. Default 5 seconds.|
| ip150Password     | The password to your IP150 (not your panel PIN). Mandatory parameter.  |
| pcPassword        | The panel programming code 3012 setting. Optional parameter. Default value is 0000.|
| ipAddress         | IP address or hostname of your IP150. If hostname is used must be resolvable by OpenHAB. Mandatory parameter.  |
| port              | The port used for data communication. Optional parameter. Default value is 10000.|
| panelType         | If parameter is passed, auto-discovery of panel type will be skipped. Provide string - EVO48, EVO96, EVO192, etc... Optional parameter. |
| reconnectWaitTime | Value is in seconds. The time to wait before a reconnect occurs after socket timeout. Optional parameter. Default value is 30 seconds.|
| maxPartitions     | Sets maximum partitions to use during refresh. If not set, maximum allowed amount from panelType will be used. Optional parameter. |
| maxZones          | Sets maximum zones to use during refresh. If not set, maximum allowed amount from panelType will be used. Optional parameter.|
| encrypt           | Sets if encryption has to be used. Optional parameter. Default value is false |

### IP150 bridge channels

| Channel             | Description                                    |
|---------------------|------------------------------------------------|
|communicationCommand | Possible values [LOGOUT, LOGIN, RESET]         |
|communicationState   | Shows the communication status to Paradox. Different from Bridge status. Bridge may be online and able to receive commands but communication may be offline due to various reasons. Possible values [Offline, Online] |

#### Communication command channel allowed values

| Value  | Description                                                                        |
|--------|------------------------------------------------------------------------------------|
| LOGOUT | Logs out and disconnects from Paradox alarm system                                 |
| LOGIN  | Creates socket if necessary, connects to paradox system and uses the logon data from the thing parameters to connect.|
| RESET  | Does logout and then login with recreation of communicator objects inside the code.|

### Entities (zones, partitions) configuration parameters:

| Value             | Description                                                                        |
|-------------------|------------------------------------------------------------------------------------|
| id                | The numeric ID of the zone/partition                                               |
| disarmEnabled     | Optional boolean flag. Valid for partitions. When set to true the command DISARM will be allowed for the partition where the flag is enabled. CAUTION: Enabling DISARM command can be dangerous. If attacker can gain access to your openHAB (via API or UI), this command can be used to disarm your armed partition (area) |

### Panel channels:

| Channel                  | Type                       | Description                                                                               |
|--------------------------|----------------------------|-------------------------------------------------------------------------------------------|
| state                    | String                     | Overall panel state                                                                       |
| inputVoltage             | Number:ElectricPotential   | Supply Voltage                                                                            |
| boardVoltage             | Number:ElectricPotential   | Board DC Voltage                                                                          |
| batteryVoltage           | Number:ElectricPotential   | Battery Voltage                                                                           |
| panelTime                | DateTime                   | Panel internal time (Timezone is set to default zone of the Java virtual machine)         |

### Partition channels:

| Channel                  | Type    | Description                                                                                   |
|--------------------------|---------|-----------------------------------------------------------------------------------------------|
| partitionLabel           | String  | Label of partition inside Paradox configuration                                               |
| state                    | String  |State of partition (armed, disarmed, in alarm)                                                 |
| additionalState          | String  | This used to be a channel where all different states were consolidated as semi-colon separated string. With implementation of each state as channel additional states should be no longer used. (deprecated channel)                             |
| readyToArm               | Switch  | Partition is Ready to arm                                                                     |
| inExitDelay              | Switch  | Partition is in Exit delay                                                                    |
| inEntryDelay             | Switch  | Partition in Entry Delay                                                                      |
| inTrouble                | Switch  | Partition has trouble                                                                         |
| alarmInMemory            | Switch  | Partition has alarm in memory                                                                 |
| zoneBypass               | Switch  | Partition is in Zone Bypass                                                                   |
| zoneInTamperTrouble      | Switch  | Partition is in Tamper Trouble                                                                |
| zoneInLowBatteryTrouble  | Switch  | Partition has zone in Low Battery Trouble                                                     |
| zoneInFireLoopTrouble    | Switch  | Partition has zone in Fire Loop Trouble                                                       |
| zoneInSupervisionTrouble | Switch  | Partition has zone in Supervision Trouble                                                     |
| stayInstantReady         | Switch  | Partition is in state Stay Instant Ready                                                      |
| forceReady               | Switch  | Partition is in state Force Ready                                                             |
| bypassReady              | Switch  | Partition is in state Bypass Ready                                                            |
| inhibitReady             | Switch  | Partition is in state Inhibit Ready                                                           |
| allZonesClosed           | Contact | All zones in partition are currently closed                                                   |
| command                  | String  | Command to be send to partition. Can be (ARM, DISARM, FORCE_ARM, INSTANT_ARM, STAY_ARM, BEEP) |

### Zone channels:

| Channel         | Type    | Description                                                                    |
|-----------------|---------|--------------------------------------------------------------------------------|
| zoneLabel       | String  | Label of zone inside Paradox configuration                                     |
| openedState     | Contact | Zone opened / closed                                                           |
| tamperedState   | Switch  | Zone is tampered / not tampered                                                |

## Example things configuration

```java
   Bridge paradoxalarm:ip150:ip150 [refresh=5, panelType="EVO192", ip150Password="********", pcPassword="0000", ipAddress=XXX.XXX.XXX.XXX", port=10000, reconnectWaitTime=10, maxPartitions=4, maxZones=50, encrypt=true ] {

        Thing panel panel

        Thing partition partition1 [id=1]
        Thing partition partition2 [id=2]
        Thing partition partition3 [id=3]
        Thing partition partition4 [id=4]

        Thing zone MotionSensor1 [id=1]
        Thing zone MagneticSensorWindow1 [id=2]
        Thing zone MotionSensor2 [id=3]
        Thing zone MagneticSensorWindow2 [id=4]
}
```

## Example items configuration

```java
//Groups
    Group Paradox "Paradox security group"
    Group Partitions "Paradox partitions" (Paradox)
    Group Floor1MUC "Magnetic sensors - Floor 1" (Paradox)
    Group Floor2MUC "Magnetic sensors - Floor 2" (Paradox)
    Group Floor3MUC "Magnetic sensors - Floor 3" (Paradox)
    Group PIRSensors "Motion sensors" (Paradox)

//COMMUNICATOR BRIDGE
    String paradoxSendCommand "Send command to IP150" {channel="paradoxalarm:ip150:ip150:communicationCommand"}

    String panelState "Paradox panel state: [%s]"<network> (Paradox) { channel = "paradoxalarm:ip150:ip150:communicationState" }
    Number paradoxAcVoltage “Input Voltage: [%.1f V]” (Paradox) { channel = “paradoxalarm:panel:ip150:panel:inputVoltage” }
    Number paradoxDcVoltage “Board DC Voltage: [%.1f V]” (Paradox) { channel = “paradoxalarm:panel:ip150:panel:boardVoltage” }
    Number paradoxBatteryVoltage “Battery Voltage: [%.1f V]” (Paradox) { channel = “paradoxalarm:panel:ip150:panel:batteryVoltage” }
    DateTime paradoxTime "Paradox Time: [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1tS]" <lock> (Paradox) { channel = "paradoxalarm:panel:ip150:panel:panelTime" }

//PARTITIONS
    String partition1State "Magnetic sensors - Floor 1: [%s]" (Partitions) { channel = "paradoxalarm:partition:ip150:partition1:state" }
    String  partition1Command "Command for MUCFL1: [%s]" <lock> (Partitions) { channel = "paradoxalarm:partition:ip150:partition1:command" }

//ZONES
    Contact CorridorFl1_PIR_state "Corridor Fl1 motion: [%s]" (PIRSensors) { channel = "paradoxalarm:zone:ip150:MotionSensor1:opened" }
    Contact CorridorFl1_MUC_state "Corridor Fl1 window: [%s]" (Floor1MUC) { channel = "paradoxalarm:zone:ip150:MagneticSensorWindow1:opened" }
```

## Example sitemap configuration

```java
    Text label="Security" icon="lock" {
        Frame label="IP150 communication" {
            Text item=panelState valuecolor=[panelState=="Online"="green", panelState=="Offline"="red"]
            Selection item=paradoxSendCommand mappings=["LOGOUT"="Logout", "LOGIN"="Login", "RESET"="Reset"]
        }
        Frame label="Panel" {
            Text item=paradoxTime
            Text item=paradoxAcVoltage
            Text item=paradoxDcVoltage
            Text item=paradoxBatteryVoltage
        }
        Frame label="Partitions" {
            Text item=partition1State valuecolor=[partition1State=="Disarmed"="green", partition1State=="Armed"="red"]
            Selection item=partition1Command mappings=["ARM"="Arm", "FORCE_ARM"="Force Arm", "STAY_ARM"="Stay Arm", "INSTANT_ARM"="Instant Arm", "BEEP"="Keyboard Beep"]
        }
        Frame label="Zones" {
            Group item=Floor1MUC
            Group item=Floor2MUC
            Group item=Floor3MUC
            Group item=PIRSensors
        }
    }
```

## Acknowledgements

This binding would not be possible without the reverse engineering of the byte level protocol and the development by other authors in python, C# and other languages. Many thanks to the following authors and their respective GitHub repositories for their development that helped in creating this binding:

João Paulo Barraca - <https://github.com/ParadoxAlarmInterface/pai>

Jean Henning - repository not available

Tertuish - <https://github.com/Tertiush/ParadoxIP150v2> / <https://github.com/Tertiush/ParadoxIP150>
