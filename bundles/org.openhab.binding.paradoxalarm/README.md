# Paradox Alarm System binding

This binding is intended to provide basic support for Paradox Alarm system.
Currently the binding does not support active communication, i.e. you cannot change states (arming, disarming). The intention is to use it only for monitoring of your security system.
With the power of openHAB this binding can be used for complex decision rules combining motion/magnetic sensor or whole partitions states with different scenarios.

Examples: 

* All partitions are armed, therefore there is no one at home. 
* Window is opened for more than 10 minutes and temperature outside is bellow XXX degrees, send mail/any other supported notification to particular people.

## Supported Paradox panels/systems

Currently binding supports the following panels: EVO192, EVO48(not tested), EVO96(not tested)

## Supported things

| Thing      | Thing Type | Description                                                    |
|------------|------------|----------------------------------------------------------------|
| ip150      | Bridge     | The bridge is used to communicate with IP150 ethernet module attached to Paradox security system.|
| panel      | Thing      | this is representation of Paradox panel. Has the general information about the main panel module, i.e. serial number, firmware/hardware/software versions, panel type, etc...|
| partition  | Thing      | provides "state"(armed, disarmed, in alarm), "partition label" and "additional states" are aggregated additional states which are booleans (ready to arm, trouble, force instant arm ready, etc...)|
| zone       | Thing      | Paradox zone. Can be anything - magnetic, motion or any other opened/closed sensor. State channel is contact, low battery and is tampered channels are switch, label is String |

## Things configuration

### IP150 bridge parameters

| Parameter         | Description                            |
|-------------------|----------------------------------------|
| refresh           | Value is in seconds. Defines the refresh interval when the binding polls from paradox system.|
| ip150Password     | The password to your IP150 (not your panel PIN).|
| pcPassword        | The code 3012 setting. Default value is 0000.|
| ipAddress         | IP address of your IP150.|
| port              | The port used for data communication. Default value is 10000.|
| panelType         | Optional parameter. Will be used if discovery does not identify the panel. Otherwise provide EVO48, EVO96, EVO192, etc...|
| reconnectWaitTime | Value is in seconds. The time to wait before a reconnect occurs after socket timeout.|
| maxPartitions     | Optional parameter which sets maximum partitions to use during refresh. If not set, maximum allowed amount from panelType will be used.|
| maxZones          | Optional parameter which sets maximum zones to use during refresh. If not set, maximum allowed amount from panelType will be used.|

### IP150 bridge channels

| Channel             | Description                                    |
|---------------------|------------------------------------------------|
|communicationCommand | Possible values [LOGOUT, LOGIN, RESET]         |

| Value  | Description                                                                        |
|--------|------------------------------------------------------------------------------------|
| LOGOUT | Logs out and disconnects from Paradox alarm system                                 |
| LOGIN  | Creates socket if necessary, connects to paradox system and uses the logon data from the thing parameters to connect.|
| RESET  | Does logout and then login with recreation of communicator objects inside the code.| 

### Entities (zones, partitions) configuration parameters:

| Value  | Description                                                                        |
|--------|------------------------------------------------------------------------------------|
| id     | The numeric ID of the zone/partition                                               |

### Partition channels:

| Channel                  | Type    | Description                                                                                 |
|--------------------------|---------|---------------------------------------------------------------------------------------------|
| partitionLabel           | String  | Label of partition inside Paradox configuration                                             |
| state                    | String  |State of partition (armed, disarmed, in alarm)                                               |
| additionalState          | String  | This used to be a channel where all different states were consolidated as semi-colon separated string. With implementation of each state as channel additional states should be no longer used. (deprecated channel) |
| readyToArm               | Switch  | Partition is Ready to arm                                                                   |
| inExitDelay              | Switch  | Partition is in Exit delay                                                                  |
| inEntryDelay             | Switch  | Partition in Entry Delay                                                                    |
| inTrouble                | Switch  | Partition has trouble                                                                       |
| alarmInMemory            | Switch  | Partition has alarm in memory                                                               |
| zoneBypass               | Switch  | Partition is in Zone Bypass                                                                 |
| zoneInTamperTrouble      | Switch  | Partition is in Tamper Trouble                                                              |
| zoneInLowBatteryTrouble  | Switch  | Partition has zone in Low Battery Trouble                                                   |
| zoneInFireLoopTrouble    | Switch  | Partition has zone in Fire Loop Trouble                                                     |
| zoneInSupervisionTrouble | Switch  | Partition has zone in Supervision Trouble                                                   |
| stayInstantReady         | Switch  | Partition is in state Stay Instant Ready                                                    |
| forceReady               | Switch  | Partition is in state Force Ready                                                           |
| bypassReady              | Switch  | Partition is in state Bypass Ready                                                          |
| inhibitReady             | Switch  | Partition is in state Inhibit Ready                                                         |
| allZonesClosed           | Contact | All zones in partition are currently closed                                                 |

### Zone channels:

| Channel         | Type    | Description                                                                    |
|-----------------|---------|--------------------------------------------------------------------------------|
| zoneLabel       | String  | Label of zone inside Paradox configuration                                     |
| openedState     | Contact | Zone opened / closed                                                           |
| tamperedState   | Switch  | Zone is tampered / not tampered                                                |
## Example things configuration

```java
   Bridge paradoxalarm:ip150:ip150 [refresh=5, panelType="EVO192", ip150Password="asdfasdf", pcPassword="1234", ipAddress=XXX.XXX.XXX.XXX", port=10000 ] {

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
    Group PIRSensors "Motion sensors" (Paradox)

//COMMUNICATOR BRIDGE
    String paradoxSendCommand "Send command to IP150" {channel="paradoxalarm:ip150:ip150:communicationCommand"}

//PANEL
    String panelState "Paradox panel state: [%s]" (Paradox) { channel = "paradoxalarm:panel:ip150:panel:state" }
    String panelType "Paradox panel type: [%s]" (Paradox) { channel = "paradoxalarm:panel:ip150:panel:panelType" }
    String serialNumber "Paradox Serial number: [%s]" (Paradox) { channel = "paradoxalarm:panel:ip150:panel:serialNumber" }
    String hardwareVersion "Paradox HW version: [%s]" (Paradox) { channel = "paradoxalarm:panel:ip150:panel:hardwareVersion" }
    String applicationVersion "Paradox Application version: [%s]" (Paradox) { channel = "paradoxalarm:panel:ip150:panel:applicationVersion" }
    String bootloaderVersion "Paradox Bootloader version: [%s]" (Paradox) { channel = "paradoxalarm:panel:ip150:panel:bootloaderVersion" }

//PARTITIONS
    String partition1State "Magnetic sensors - Floor 1: [%s]" (Partitions) { channel = "paradoxalarm:partition:ip150:partition1:state" }
    String partition1AdditionalStates "Floor1 MUC additional states: [%s]" (Partitions) { channel = "paradoxalarm:partition:ip150:partition1:additionalStates" }

//ZONES
    Contact CorridorFl1_PIR_state "Corridor Fl1 motion: [%s]" (PIRSensors) { channel = "paradoxalarm:zone:ip150:MotionSensor1:opened" }
    Contact CorridorFl1_MUC_state "Corridor Fl1 window: [%s]" (Floor1MUC) { channel = "paradoxalarm:zone:ip150:MagneticSensorWindow1:opened" }
```

## Example sitemap configuration

```java
   Text label="Security" icon="lock"{
        Frame label="Panel"{
            Text item=panelState valuecolor=[panelState=="Online"="green", panelState=="Offline"="red"]
            Text item=panelType
            Text item=serialNumber
            Text item=hardwareVersion
            Text item=applicationVersion
            Text item=bootloaderVersion
        }
        Frame label="IP150 communication" {
            Switch item=paradoxSendCommand mappings=["LOGOUT"="Logout", "LOGIN"="Login", "RESET"="Reset"]
        }
        Frame label="Partitions" {
            Text item=partition1State valuecolor=[partition1State=="Disarmed"="green", partition1State=="Armed"="red"]
            Text item=partition1AdditionalStates
            Text item=partition2State valuecolor=[partition2State=="Disarmed"="green", partition2State=="Armed"="red"]
            Text item=partition2AdditionalStates
            Text item=partition3State valuecolor=[partition3State=="Disarmed"="green", partition3State=="Armed"="red"]
            Text item=partition3AdditionalStates
            Text item=partition4State valuecolor=[partition4State=="Disarmed"="green", partition4State=="Armed"="red"]
            Text item=partition4AdditionalStates
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
This binding would not be possible without the reverse engineering of the byte level protocol and the development by other authors in python, C# and other languages. Many thanks to the following authors and their respective github repositories for their development that helped in creating this binding:

João Paulo Barraca - https://github.com/ParadoxAlarmInterface/pai

Jean Henning - repository not available

Tertuish - https://github.com/Tertiush/ParadoxIP150v2 / https://github.com/Tertiush/ParadoxIP150
