# Satel Integra Alarm System Binding

The Satel Integra Alarm System allows openHAB to connect to your alarm system via TCP/IP network with ETHM-1/ETHM-1 Plus module installed, or via RS-232 serial port with INT-RS/INT-RS Plus module installed.
For ETHM-1 the binding uses integration protocol, so it must be enabled and properly configured.
Also it is always a good idea to update module/mainboard firmware to the latest version.
For ETHM-1 and INT-RS modules it is a must.
For "Plus" modules however it is not required.

In order to use encryption with ETHM-1/ETHM-1 Plus, Java Runtime Environment must support 192 bit AES keys.
Oracle Java by default supports only 128 bit keys, therefore ["Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files"](https://www.oracle.com/technetwork/java/javase/downloads/index.html) must be installed.
OpenJDK supports unlimited AES keys by default (but OpenJDK is sometimes discouraged for openHAB).

More details and all documentation about Integra system you can find on their site: [satel.pl](https://www.satel.pl/pl/cat/2#cat15)

## Supported Things

This binding supports following thing types:

| Thing type | Description                                                                                                                              |
| ---------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| ethm-1     | Ethernet bridge, supports [ETHM-1](https://www.satel.pl/en/product/115/) and [ETHM-1 Plus](https://www.satel.pl/en/product/698/) modules |
| int-rs     | RS-232 bridge, supports [INT-RS](https://www.satel.pl/en/product/123/) and [INT-RS Plus](https://www.satel.pl/en/product/664/) modules   |
| partition  | Set of zones representing some physical area or logical relation                                                                         |
| zone       | A physical device: reed switch, motion sensor or a virtual zone                                                                          |
| output     | An output defined in the system                                                                                                          |
| shutter    | Two outputs that control a roller shutter, one for "up" direction, another one for "down"                                                |
| system     | A virtual thing describing general status of the alarm system                                                                            |
| event-log  | A virtual thing that allows reading records from the alarm system event log                                                              |
| atd-100    | Wireless temperature detector [ATD-100](https://www.satel.pl/en/produktid/503)                                                           |

## Discovery

The binding discovers all devices (partitions, zones) defined in the system, but bridge things must be configured manually.

## Thing Configuration

### ethm-1 bridge

You can configure the following settings for this bridge:

| Name          | Required | Description                                                                                                                                                                                                                                                                  |
| ------------- | -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| host          | yes      | Host name or IP addres of ETHM-1 module.                                                                                                                                                                                                                                     |
| port          | no       | TCP port for the integration protocol, defaults to 7094.                                                                                                                                                                                                                     |
| timeout       | no       | Timeout value in milliseconds for connect, read and write operations, defaults to 5000 (5secs).                                                                                                                                                                              |
| refresh       | no       | Polling interval in milliseconds, defaults to 5000 (5secs). As of version 2.03 ETHM-1 Plus firmware the module disconnects after 25 seconds of inactivity. Setting this parameter to value greater than 25000 will cause inability to correctly communicate with the module. |
| userCode      | no       | Security code of the user in behalf of all operations will be executed. If empty, only read operations are allowed.                                                                                                                                                          |
| encryptionKey | no       | Encryption key used to encrypt data sent and received. If empty, communication is not encrypted.                                                                                                                                                                             |
| encoding      | no       | Encoding for all the texts received from the module.                                                                                                                                                                                                                         |
| extCommands   | no       | Check this option to enable extended commands, supported by ETHM-1 Plus and newer versions of ETHM-1. Enabled by default, turn off in case of communication timeouts.                                                                                                        |

Example:

```java
Bridge satel:ethm-1:home [ host="192.168.0.2", refresh=1000, userCode="1234", encryptionKey="abcdefgh" ]
```

**NOTE:** There can be only one client connected to ETHM-1 module.
It does not accept new connections if there is already a connection established.
In case you have troubles connecting to the system using this module, please make sure there is no other client (for example installed 1.x version of the binding) already connected to it.

### int-rs bridge

You can configure the following settings for this bridge:

| Name        | Required | Description                                                                                                                                         |
| ----------- | -------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| port        | yes      | Serial port connected to the module.                                                                                                                |
| timeout     | no       | Timeout value in milliseconds for connect, read and write operations, defaults to 5000 (5secs).                                                     |
| refresh     | no       | Polling interval in milliseconds, defaults to 5000 (5secs).                                                                                         |
| userCode    | no       | Security code of the user in behalf of all operations will be executed. If empty, only read operations are allowed.                                 |
| encoding    | no       | Encoding for all the texts received from the module.                                                                                                |
| extCommands | no       | Check this option to enable extended commands, supported by version 2.xx of INT-RS. Enabled by default, turn off in case of communication timeouts. |

Example:

```java
Bridge satel:int-rs:home [ port="/dev/ttyS0", refresh=1000, userCode="1234" ]
```

### partition

You can configure the following settings for a partition:

| Name        | Required | Description                                                      |
| ----------- | -------- | ---------------------------------------------------------------- |
| id          | yes      | Partition number                                                 |
| forceArming | no       | Arms the partition regardless of ongoing troubles and violations |

Example:

```java
Thing partition partition1 [ id=1, forceArming=true ]
```

### zone

You can configure the following settings for a zone:

| Name        | Required | Description                                                              |
| ----------- | -------- | ------------------------------------------------------------------------ |
| id          | yes      | Zone number                                                              |
| invertState | no       | Changes active (ON) state to 0                                           |
| wireless    | no       | This zone is monitored by a wireless detector like APD-100, AFD-100, etc |

Example:

```java
Thing zone zone1 [ id=1 ]
```

### output

You can configure the following settings for an output:

| Name        | Required | Description                                                           |
| ----------- | -------- | --------------------------------------------------------------------- |
| id          | yes      | Output number                                                         |
| invertState | no       | Changes active (ON) state to 0                                        |
| commandOnly | no       | Accepts commands only, does not update state of the thing             |
| wireless    | no       | This output controls a wireless device like ASP-100 R, ASW-100 E, etc |

Example:

```java
Thing output output1 [ id=1, invertState=true, wireless=false ]
```

### shutter

You can configure the following settings for a shutter:

| Name        | Required | Description                                               |
| ----------- | -------- | --------------------------------------------------------- |
| upId        | yes      | Output number for "up" direction                          |
| downId      | yes      | Output number for "down" direction                        |
| commandOnly | no       | Accepts commands only, does not update state of the thing |

Example:

```java
Thing shutter shutter1 [ upId=10, downId=11, commandOnly=true ]
```

### system

This thing type does not have any configuration parameters.

Example:

```java
Thing system System [ ]
```

### event-log

This thing type does not have any configuration parameters.

Example:

```java
Thing event-log EventLog [ ]
```

### atd-100

You can configure the following settings for this thing:

| Name    | Required | Description                                                |
| ------- | -------- | ---------------------------------------------------------- |
| id      | yes      | Zone number in the alarm system monitored by this detector |
| refresh | no       | Polling interval in minutes, defaults to 15                |

Example:

```java
Thing atd-100 KitchenTemp [ id=10, refresh=30 ]
```

## Channels

### partition

| Name               | Type   | Description                       |
| ------------------ | ------ | --------------------------------- |
| armed              | Switch | Armed                             |
| really_armed       | Switch | Really armed                      |
| armed_mode_1       | Switch | Armed in mode 1                   |
| armed_mode_2       | Switch | Armed in mode 2                   |
| armed_mode_3       | Switch | Armed in mode 3                   |
| first_code_entered | Switch | First code entered                |
| entry_time         | Switch | Entry time                        |
| exit_time_gt_10    | Switch | Exit time greater than 10 seconds |
| exit_time_lt_10    | Switch | Exit time less than 10 seconds    |
| temporary_blocked  | Switch | Temporary blocked                 |
| blocked_for_guard  | Switch | Blocked for guard                 |
| alarm              | Switch | Alarm                             |
| alarm_memory       | Switch | Alarm memory                      |
| fire_alarm         | Switch | Fire alarm                        |
| fire_alarm_memory  | Switch | Fire alarm memory                 |
| verified_alarms    | Switch | Verified alarms                   |
| warning_alarms     | Switch | Warning alarms                    |
| violated_zones     | Switch | Violated zones                    |

### zone

| Name                   | Type   | Description                                               |
| ---------------------- | ------ | --------------------------------------------------------- |
| violation              | Switch | Violation                                                 |
| tamper                 | Switch | Tamper                                                    |
| alarm                  | Switch | Alarm                                                     |
| tamper_alarm           | Switch | Tamper alarm                                              |
| alarm_memory           | Switch | Alarm memory                                              |
| tamper_alarm_memory    | Switch | Tamper alarm memory                                       |
| bypass                 | Switch | Bypass                                                    |
| no_violation_trouble   | Switch | No violation trouble                                      |
| long_violation_trouble | Switch | Long violation trouble                                    |
| isolate                | Switch | Isolate                                                   |
| masked                 | Switch | Masked                                                    |
| masked_memory          | Switch | Masked memory                                             |
| device_lobatt          | Switch | Indicates low battery level in the wireless device        |
| device_nocomm          | Switch | Indicates communication troubles with the wireless device |

### output

**NOTE:** You can change state of mono/bistable outputs only.

| Name          | Type   | Description                                               |
| ------------- | ------ | --------------------------------------------------------- |
| state         | Switch | State of the output                                       |
| device_lobatt | Switch | Indicates low battery level in the wireless device        |
| device_nocomm | Switch | Indicates communication troubles with the wireless device |

### shutter

| Name          | Type          | Description          |
| ------------- | ------------- | -------------------- |
| shutter_state | Rollershutter | State of the shutter |

### system

| Name            | Type     | Description                                                                                                                        |
| --------------- | -------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| date_time       | DateTime | Date and time on the alarm system                                                                                                  |
| troubles        | Switch   | Active when the system has troubles (trouble LED is blinking on a panel)                                                           |
| troubles_memory | Switch   | Memorized state of system troubles                                                                                                 |
| service_mode    | Switch   | Active when the system is in service mode                                                                                          |
| acu100_present  | Switch   | Active when there is an ACU-100 module installed in the system                                                                     |
| intrx_present   | Switch   | Active when there is an INT-RX module installed in the system                                                                      |
| grade23_set     | Switch   | Active when Grade2/Grade3 option is set in the system                                                                              |
| user_code       | String   | Accepts string commands that override configured user code. Send empty string to revert user code to the one in the configuration. |

### event-log (deprecated)

These channels and the thing will be removed in the future release of the binding. Please use `readEvent` rule action instead.

| Name        | Type     | Description                                                                            |
| ----------- | -------- | -------------------------------------------------------------------------------------- |
| index       | Number   | Index of the current record in the event log. Send '-1' to get most recent record.     |
| prev_index  | Number   | Index of the previous record in the event log. Use this value to iterate over the log. |
| timestamp   | DateTime | Date and time when the event happened.                                                 |
| description | String   | Textual description of the event.                                                      |
| details     | String   | Details about the event, usually list of objects related to the event.                 |

### atd-100

| Name          | Type               | Description                                               |
| ------------- | ------------------ | --------------------------------------------------------- |
| temperature   | Number:Temperature | Current temperature in the zone                           |
| device_lobatt | Switch             | Indicates low battery level in the wireless device        |
| device_nocomm | Switch             | Indicates communication troubles with the wireless device |

## Rule Actions

### readEvent

This action allows you to read one record from the event log placed at index given by input parameter.
The result of this action is compatible with channels of `event-log` thing and contains following values:

| Name        | Type          | Description                                                                            |
| ----------- | ------------- | -------------------------------------------------------------------------------------- |
| index       | Number        | Index of this record in the event log.                                                 |
| prev_index  | Number        | Index of the previous record in the event log. Use this value to iterate over the log. |
| timestamp   | ZonedDateTime | Date and time when the event happened.                                                 |
| description | String        | Textual description of the event.                                                      |
| details     | String        | Details about the event, usually list of objects related to the event.                 |

Usage:

```java
val actions = getActions("satel", "satel:event-log:home:EventLog")
val eventRec = actions.readEvent(-1)
logInfo("EventLog", eventRec.get("description"))
```

**NOTE:** To have this action available, you must have `event-log` thing configured in openHAB.

## Full Example

### satel.things

```java
Bridge satel:ethm-1:home [ host="192.168.0.2", refresh=1000, userCode="1234", encryptionKey="abcdefgh" ] {
    Thing partition MainPartition [ id=1 ]
    Thing zone LivingRoomPIR [ id=1 ]
    Thing zone BedroomPIR [ id=2 ]
    Thing output KitchenLamp [ id=1 ]
    Thing shutter KitchenWindow [ upId=2, downId=3 ]
    Thing output Siren [ id=17, wireless=true ]
    Thing atd-100 KitchenTemp [ id=10, refresh=30 ]
    Thing system System []
    Thing event-log EventLog []
}

```

### satel.items

```java
Group Satel
Group:Switch:OR(ON,OFF) Alarms "Alarms [(%d)]" <siren>
Switch PARTITION_ARMED "Partition armed" (Satel) { channel="satel:partition:home:MainPartition:armed" }
Switch PARTITION_ALARM "Partition alarm" (Satel,Alarms) { channel="satel:partition:home:MainPartition:alarm" }
Switch LIVING_VIOLATION "Violation in living room" (Satel) { channel="satel:zone:home:LivingRoomPIR:violation" }
Switch LIVING_ALARM "Intruder in living room" (Satel) { channel="satel:zone:home:LivingRoomPIR:alarm" }
Switch BEDROOM_TAMPER "Bedroom PIR tampered" (Satel) { channel="satel:zone:home:BedroomPIR:tamper_alarm" }
Switch BEDROOM_TAMPER_M "Bedroom PIR tamper memory" (Satel) { channel="satel:zone:home:BedroomPIR:tamper_alarm_memory" }
Switch KITCHEN_LAMP "Kitchen lamp" (Satel) { channel="satel:output:home:KitchenLamp:state" }
Rollershutter KITCHEN_BLIND "Kitchen blind" (Satel) { channel="satel:shutter:home:KitchenWindow:shutter_state" }
Switch SYSTEM_TROUBLES "Troubles in the system" (Satel) { channel="satel:system:home:System:troubles" }
String KEYPAD_CHAR ">" <none> (Satel)
String USER_CODE "User code" (Satel) { channel="satel:system:home:System:user_code" }
Switch SIREN_LOBATT "Siren: low battery level" (Satel) { channel="satel:output:home:Siren:device_lobatt" }
Switch SIREN_NOCOMM "Siren: no communication" (Satel) { channel="satel:output:home:Siren:device_nocomm" }
Number:Temperature KITCHEN_TEMP "Kitchen temperature [%.1f °C]" <temperature> (Satel) { channel="satel:atd-100:home:KitchenTemp:temperature" }
Switch KITCHEN_TEMP_LOBATT "Kitchen sensor: low battery" (Satel) { channel="satel:atd-100:home:KitchenTemp:device_lobatt" }
Switch KITCHEN_TEMP_NOCOMM "Kitchen sensor: no communication" (Satel) { channel="satel:atd-100:home:KitchenTemp:device_nocomm" }
```

### satel.sitemap

```perl
Frame label="Alarm system" {
    Switch item=Alarms mappings=[OFF="Clear"]
    Switch item=PARTITION_ARMED mappings=[ON="Arm", OFF="Disarm"]
    Frame label="Status" {
        Switch item=SYSTEM_TROUBLES mappings=[OFF="Clear"]
        Switch item=LIVING_VIOLATION
        Switch item=LIVING_ALARM
        Switch item=BEDROOM_TAMPER
        Switch item=BEDROOM_TAMPER_M
        Switch item=SIREN_LOBATT
        Switch item=SIREN_NOCOMM
    }
    Frame label="Kitchen" {
        Switch item=KITCHEN_LAMP
        Rollershutter item=KITCHEN_BLIND
        Text item=KITCHEN_TEMP
        Switch item=KITCHEN_TEMP_LOBATT
        Switch item=KITCHEN_TEMP_NOCOMM
    }
    Text label="Keypad" icon="settings" {
        Switch item=KEYPAD_CHAR mappings=[ "1"="1", "2"="2", "3"="3" ]
        Switch item=KEYPAD_CHAR mappings=[ "4"="4", "5"="5", "6"="6" ]
        Switch item=KEYPAD_CHAR mappings=[ "7"="7", "8"="8", "9"="9" ]
        Switch item=KEYPAD_CHAR mappings=[ "*"="*", "0"="0", "-"="#" ]
    }
}
```

### satel.rules

```java
var String userCode = ""
var Timer keypadTimer = null
var Timer userCodeTimer = null

rule "Keypad char entered"
when
    Item KEYPAD_CHAR changed
then
    val timeout = now.plusSeconds(20)

    if (KEYPAD_CHAR.state == "-") {
        logInfo("Keypad", "Changing user code")
        USER_CODE.sendCommand(userCode)
        userCode = ""
        if (userCodeTimer != null) {
            userCodeTime.cancel
        }
        userCodeTimer = createTimer(now.plusMinutes(10)) [|
            logInfo("Keypad", "Reverting user code")
            USER_CODE.sendCommand("")
        ]
    } else if (KEYPAD_CHAR.state == "*") {
        logInfo("Keypad", "Reverting user code")
        USER_CODE.sendCommand("")
        userCode = ""
    } else {
        userCode = userCode + KEYPAD_CHAR.state
    }

    if (keypadTimer != null) {
        keypadTimer.cancel
    }
    keypadTimer = createTimer(timeout) [|
        userCode = ""
        KEYPAD_CHAR.postUpdate("")
    ]
end

rule "Send event log"
when
    Item Alarms changed to ON
then
    val actions = getActions("satel", "satel:event-log:home:EventLog")
    if (null === actions) {
        logInfo("EventLog", "Actions not found, check thing ID")
        return
    }
    logInfo("EventLog", "Start")
    var msgBody = ""
    var eventIdx = -1
    (1..10).forEach[
        val eventRec = actions.readEvent(eventIdx)
        val details = eventRec.get("details")
        msgBody += "\n" + String::format("%1$tF %1$tR", eventRec.get("timestamp")) + ": " + eventRec.get("description")
        if (details != NULL && details != "") {
             msgBody += " - " + details
        }
        eventIdx = eventRec.get("prev_index")
    ]
    logInfo("EventLog", "End")
    // sending notifications via mail requires the mail binding
    getActions("mail","mail:smtp:local").sendMail("you@email.net", "Event log", msgBody)
end
```

## Migration from 1.x version of the binding

### binary items

In openHAB all channels have strict types, which means you cannot use other type then designated for a channel.
In Satel binding all binary items are now of 'Switch' type. Using other item types, like 'Contact' is not possible in this version of the binding.
For this reason, when migrating 1.x item files, besides changing binding configuration for each item, you must replace all 'Contact' items to 'Switch' type.

### 'module' channels

In version 2.x of the binding all 'module' channels have been removed.
You can easily replace them with the following configuration:

#### satel.items

```java
Switch MODULE_CONNECTED "Connection status" <network> (Satel)
DateTime MODULE_CONNECTED_SINCE "Connection established at [%1$tF %1$tR]" <time> (Satel)
Number MODULE_CONNECTION_ERRORS "Connection errors [%d]" (Satel)
```

#### satel.rules

```java
rule "Satel bridge changed to ONLINE"
when
    Thing "satel:ethm-1:home" changed to ONLINE
then
    MODULE_CONNECTED.postUpdate(ON)
    MODULE_CONNECTED_SINCE.postUpdate(new DateTimeType())
    MODULE_CONNECTION_ERRORS.postUpdate(0)
end

rule "Satel bridge received OFFLINE"
when
    Thing "satel:ethm-1:home" received update OFFLINE
then
    if (MODULE_CONNECTED.state == ON) {
        MODULE_CONNECTED.postUpdate(OFF)
        MODULE_CONNECTED_SINCE.postUpdate(NULL)
        MODULE_CONNECTION_ERRORS.postUpdate(1)
    } else {
        val connErrors = MODULE_CONNECTION_ERRORS.state as DecimalType
        MODULE_CONNECTION_ERRORS.postUpdate(connErrors.intValue + 1)
    }
end
```

## Security considerations

### User for openHAB integration

To control Integra partitions and outputs, you need to provide security code of a user in behalf of all those operations will be executed.
It is highly recommended to use a separate user for openHAB integration with only required access rights set in Integra configuration, like access to certain partitions, etc.
This allows you to distinguish actions made by openHAB and a user using Integra panel, also it will block unwanted operations in case someone breaks into your local network.

### Disarming and clearing alarms

Although this binding allows you to configure disarming a partition and clearing alarms for a partition, this should be used only in cases when security is not the priority.
Don't forget both these operations can be executed in openHAB without specifying a user code, which is required to disarm or clear alarms using Integra panel.
Consider adding a keypad in your sitemap to temporarily change user code to execute sensitive operations.
You can find such keypad in the [Full Example](#full-example) section.

## Media

- [Arming and clearing troubles](https://www.youtube.com/watch?v=ogdgn0Dk1G8)
