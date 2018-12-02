# Paradox Alarm System binding

## Supported things and items
The binding supports the following things: ip150 bridge, panel thing, partition thing, zone thing. Examples can be seen bellow.

### IP150 bridge configuration

#### Arguments:

**refresh** - value is in seconds. Defines the refresh interval when the binding polls from paradox system.<br>
**panelType** - Identifier of panel type.<br>
&nbsp;&nbsp;&nbsp;&nbsp;Supported: EVO48, EVO192, EVOHD.<br>
&nbsp;&nbsp;&nbsp;&nbsp;Future use: SP5500, SP6000, SP7000, MG5000, MG5050, SP4000, SP65.<br>
**ip150Password** - pretty obvious. The password to your IP150 (not your panel PIN).<br>
**pcPassword** - The code 3012 setting. Default value is 0000 for Paradox<br>
**ipAddress** - pretty obvious. IP address of your IP150<br>
**port** - the port used for data communication. Default is 10000 for Paradox<br>
#### Channels:

**communicationCommand** - Possible values [LOGOUT, LOGIN, RESET]<br>
&nbsp;&nbsp;&nbsp;&nbsp;LOGOUT: logs out and disconnects from Paradox alarm system.<br>
&nbsp;&nbsp;&nbsp;&nbsp;LOGIN: creates socket if necessary, connects to paradox system and uses the logon data from the thing parameters to connect.<br>
&nbsp;&nbsp;&nbsp;&nbsp;RESET: does logout and then login with recreation of communicator objects inside the code.<br>

### Entities configuration arguments:

**id** - the numeric ID of the zone/partition<br>
**refresh** - Interval on which the things poll from the cache (Probably would be wise to be implemented with something like a callback in the future)<br>

### Example things configuration

```java
    paradoxalarm:paradoxCommunication:panel [refresh=5, panelType="EVO192", ip150Password="<YOUR IP150 PASSWORD>", pcPassword="0000", ipAddress="10.10.10.10", port=10000 ] {
        Thing panel panel [refresh=10]
        Thing partition partition1 [id=1, refresh=10]
        Thing paradoxalarm:partition:partition2 [id=2, refresh=10]
        Thing paradoxalarm:partition:partition3 [id=3, refresh=10]
        Thing paradoxalarm:partition:partition4 [id=4, refresh=10]
        Thing paradoxalarm:zone:bedroomBathMUC [id=20, refresh=10]
    }
```

### Example items configuration

```java
    String paradoxSendCommand "Send command to IP150" {channel="paradoxalarm:ip150:communicator:command"}

    String panelType "Paradox panel type: [%s]" <lock> (Security) { channel = "paradoxalarm:panel:ip150:panel:panelType" }
    String serialNumber "Paradox Serial number: [%s]" <lock> (Security) { channel = "paradoxalarm:panel:ip150:panel:serialNumber" }
    String hardwareVersion "Paradox HW version: [%s]" <lock> (Security) { channel = "paradoxalarm:panel:ip150:panel:hardwareVersion" }
    String applicationVersion "Paradox Application version: [%s]" <lock> (Security) { channel = "paradoxalarm:panel:ip150:panel:applicationVersion" }
    String bootloaderVersion "Paradox Bootloader version: [%s]" <lock> (Security) { channel = "paradoxalarm:panel:ip150:panel:bootloaderVersion" }

    String partition1Label "Partition1 label: [%s]" <lock> (Security) { channel = "paradoxalarm:partition:ip150:partition3:label" }
    String partition1State "Partition1 state: [%s]" <lock> (Security) { channel = "paradoxalarm:partition:ip150:partition3:state" }
    String partition1AdditionalStates "Partition1 additional states: [%s]" <lock> (Security) { channel = "paradoxalarm:partition:ip150:partition3:addidionalStates" }

    String partition2Label "Partition2 label: [%s]" <lock> (Security) { channel = "paradoxalarm:partition:ip150:partition2:label" }
    String partition2State "Partition2 state: [%s]" <lock> (Security) { channel = "paradoxalarm:partition:ip150:partition2:state" }
    String partition2AdditionalStates "Partition2 additional state: [%s]" <lock> (Security) { channel = "paradoxalarm:partition:ip150:partition2:addidionalStates" }

    String zoneBedRoomLabel "Zone label: [%s]" <lock> (Security) { channel = "paradoxalarm:zone:ip150:bedroomBathMUC:label" }
    Contact zoneBedRoomIsOpened "Zone opened: [%s]" <lock> (Security) { channel = "paradoxalarm:zone:ip150:bedroomBathMUC:isOpened" }
    Contact zoneBedRoomIsTampered "Zone tampered: [%s]" <lock> (Security) { channel = "paradoxalarm:zone:ip150:bedroomBathMUC:isTampered" }
    Contact zoneBedRoomHasLowBattery "Zone low battery: [%s]" <lock> (Security) { channel = "paradoxalarm:zone:ip150:bedroomBathMUC:hasLowBattery" }
```

### Example sitemap configuration

```java
    Frame {
        Switch item=paradoxSendCommand mappings=["LOGOUT"="Logout", "LOGIN"="Login", "RESET"="Reset"]
    }
    Frame {
        Text item=panelType
        Text item=serialNumber
        Text item=hardwareVersion
        Text item=applicationVersion
        Text item=bootloaderVersion
    }
    Frame {
        Text item=partition1State valuecolor=[partition1State=="Disarmed"="green", partition1State=="Armed"="red"]
        Text item=partition1AdditionalStates valuecolor=[partition1AdditionalStates=="Disarmed"="green", partition1AdditionalStates=="Armed"="red"]
        Text item=partition2State valuecolor=[partition2State=="Disarmed"="green", partition2State=="Armed"="red"]
        Text item=partition2AdditionalStates valuecolor=[partition2AdditionalStates=="Disarmed"="green", partition2AdditionalStates=="Armed"="red"]
        Text item=partition3State valuecolor=[partition3State=="Disarmed"="green", partition3State=="Armed"="red"]
        Text item=partition3AdditionalStates valuecolor=[partition3AdditionalStates=="Disarmed"="green", partition3AdditionalStates=="Armed"="red"]
        Text item=partition4State valuecolor=[partition4State=="Disarmed"="green", partition4State=="Armed"="red"]
        Text item=partition4AdditionalStates valuecolor=[partition4AdditionalStates=="Disarmed"="green", partition4AdditionalStates=="Armed"="red"]
    }
    Frame {
        Text item=PIR1 valuecolor=[PIR1=="CLOSED"="green", PIR1=="OPEN"="red"]
        Text item=MUC1 valuecolor=[MUC1=="CLOSED"="green", MUC1=="OPEN"="red"]
    }
```
