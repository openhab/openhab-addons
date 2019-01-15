# Paradox Alarm System binding

This binding is intended to provide basic support for Paradox Alarm system.<br>
With the power of OpenHab this binding can be used for complex decision rules combining motion/magnetic sensor or whole partitions states with different scenarios. <br>
Examples: All partitions are armed, therefore there is no one at home. Window is opened for more than 10 minutes and temperature outside is bellow XXX degrees, send mail/any other supported notification to particular people.

## Supported Paradox panels/systems
Currently binding supports the following panels: EVO192, EVO48(not tested), EVO96(not tested)

## Supported things and bridges

The binding supports the following things:

#### Bridge
**ip150** - the bridge is used to communicate with IP150 ethernet module attached to Paradox security system.
Supported commands: LOGOUT, LOGIN, RESET

#### Things
**panel** - this is representation of Paradox panel. Has the general information about the main panel module, i.e. serial number, firmware/hardware/software versions, panel type, etc...<br>
**partition** - representation of Paradox partition - currently provides "state"(armed, disarmed, in alarm), partition label and in channel additional states are aggregated additional states which are booleans (ready to arm, trouble, force instant arm ready, etc...)<br>
**zone** - Paradox zone. Can be anything - magnetic, motion or any other opened/closed sensor. State channel is contact, low battery and is tampered channels are boolean, label is String<br>

### IP150 bridge configuration

#### Parameters:

**refresh** - value is in seconds. Defines the refresh interval when the binding polls from paradox system.<br>
**ip150Password** - pretty obvious. The password to your IP150 (not your panel PIN).<br>
**pcPassword** - The code 3012 setting. Default value is 0000 for Paradox<br>
**ipAddress** - pretty obvious. IP address of your IP150<br>
**port** - the port used for data communication. Default is 10000 for Paradox<br>
**panelType** - not mandatory. Will be used if discovery does not identify the panel.

#### Channels:

**communicationCommand** - Possible values [LOGOUT, LOGIN, RESET]<br>
&nbsp;&nbsp;&nbsp;&nbsp;LOGOUT: logs out and disconnects from Paradox alarm system.<br>
&nbsp;&nbsp;&nbsp;&nbsp;LOGIN: creates socket if necessary, connects to paradox system and uses the logon data from the thing parameters to connect.<br>
&nbsp;&nbsp;&nbsp;&nbsp;RESET: does logout and then login with recreation of communicator objects inside the code.<br>

### Entities configuration parameters:

**id** - the numeric ID of the zone/partition<br>


### Example things configuration

```java
   Bridge paradoxalarm:ip150:ip150 [refresh=5, panelType=“EVO192”, ip150Password=“asdfasdf”, pcPassword=“1234”, ipAddress=“192.168.100.100”, port=10000 ] {

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

### Example items configuration

```java
//Groups
    Group Paradox “Paradox security group”
    Group Partitions “Paradox partitions” (Paradox)
    Group Floor1MUC “Magnetic sensors - Floor 1” (Paradox)
    Group PIRSensors “Motion sensors” (Paradox)

//COMMUNICATOR BRIDGE
    String paradoxSendCommand “Send command to IP150” {channel=“paradoxalarm:ip150:ip150:communicationCommand”}

//PANEL
    String panelState “Paradox panel state: [%s]” (Paradox) { channel = “paradoxalarm:panel:ip150:panel:state” }
    String panelType “Paradox panel type: [%s]” (Paradox) { channel = “paradoxalarm:panel:ip150:panel:panelType” }
    String serialNumber “Paradox Serial number: [%s]” (Paradox) { channel = “paradoxalarm:panel:ip150:panel:serialNumber” }
    String hardwareVersion “Paradox HW version: [%s]” (Paradox) { channel = “paradoxalarm:panel:ip150:panel:hardwareVersion” }
    String applicationVersion “Paradox Application version: [%s]” (Paradox) { channel = “paradoxalarm:panel:ip150:panel:applicationVersion” }
    String bootloaderVersion “Paradox Bootloader version: [%s]” (Paradox) { channel = “paradoxalarm:panel:ip150:panel:bootloaderVersion” }

//PARTITIONS
    String partition1State “Magnetic sensors - Floor 1: [%s]” (Partitions) { channel = “paradoxalarm:partition:ip150:partition1:state” }
    String partition1AdditionalStates “Floor1 MUC additional states: [%s]” (Partitions) { channel = “paradoxalarm:partition:ip150:partition1:additionalStates” }

//ZONES
    Contact CorridorFl1_PIR_state “Corridor Fl1 motion: [%s]” (PIRSensors) { channel = “paradoxalarm:zone:ip150:MotionSensor1:isOpened” }
    Contact CorridorFl1_MUC_state “Corridor Fl1 window: [%s]” (Floor1MUC) { channel = “paradoxalarm:zone:ip150:MagneticSensorWindow1:isOpened” }
```

### Example sitemap configuration

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
