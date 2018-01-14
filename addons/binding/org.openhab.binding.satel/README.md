# Satel Integra Alarm System Binding

The Satel Integra Alarm System allows openHAB to connect to your alarm system via TCP/IP network with ETHM-1/ETHM-1 Plus module installed, or via RS-232 serial port with INT-RS/INT-RS Plus module installed. For ETHM-1 the binding uses integration protocol, so it must be enabled and properly configured.  
Also it is always a good idea to update module/mainboard firmware to the latest version. For ETHM-1 and INT-RS modules it is a must. For "Plus" modules however it is not required.

In order to use encryption with ETHM-1/ETHM-1 Plus, Java Runtime Environment must support 192 bit AES keys. Oracle Java by default supports only 128 bit keys, therefore ["Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files"](http://www.oracle.com/technetwork/java/javase/downloads/index.html) must be installed. OpenJDK supports unlimited AES keys by default (but OpenJDK is sometimes discouraged for openHAB).

More details and all documentation about Integra system you can find on their site: [satel.pl](https://www.satel.pl/pl/cat/2#cat15)

## Supported Things

This binding supports following thing types:

| Thing type | Description                                                                                                                              |
|------------|------------------------------------------------------------------------------------------------------------------------------------------|
| ethm-1     | Ethernet bridge, supports [ETHM-1](https://www.satel.pl/pl/product/115/) and [ETHM-1 Plus](https://www.satel.pl/pl/product/698/) modules |
| int-rs     | RS-232 bridge, supports [INT-RS](https://www.satel.pl/pl/product/123/) and [INT-RS Plus](https://www.satel.pl/pl/product/664/) modules   |
| partition  | Set of zones representing some physical area or logical relation                                                                         |
| zone       | A physical device: reed switch, motion sensor or a virtual zone                                                                          |
| output     | An output defined in the system                                                                                                          |
| shutter    | Two outputs that control a roller shutter, one for "up" direction, another one for "down"                                                |
| system     | A logical thing describing general status of the alarm system                                                                            |


## Discovery

The binding discovers all devices (partitions, zones) defined in the system, but bridge things must be configured manually.
  
## Thing Configuration

### ethm-1 bridge

You can configure the following settings for this bridge:

| Name          | Required | Description                                                                                                                                                                                                                                                                  |
|---------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| host          | yes      | Host name or IP addres of ETHM-1 module                                                                                                                                                                                                                                      |
| port          | no       | TCP port for the integration protocol, defaults to 7094                                                                                                                                                                                                                      |
| timeout       | no       | Timeout value in milliseconds for connect, read and write operations, defaults to 5000 (5secs)                                                                                                                                                                               |
| refresh       | no       | Polling interval in milliseconds, defaults to 5000 (5secs). As of version 2.03 ETHM-1 Plus firmware the module disconnects after 25 seconds of inactivity. Setting this parameter to value greater than 25000 will cause inability to correctly communicate with the module. |
| userCode      | no       | Security code of the user in behalf of all operations will be executed. If empty, only read operations are allowed                                                                                                                                                           |
| encryptionKey | no       | Encryption key used to encrypt data sent and received, if empty communication is not encrypted                                                                                                                                                                               |
| encoding      | no       | Encoding for all the texts received from the module                                                                                                                                                                                                                          |

Example:

```
Bridge satel:ethm-1:home [ host="192.168.0.2", refresh=1000, userCode="1234", encryptionKey="abcdefgh" ]
```

### int-rs bridge

You can configure the following settings for this bridge:

| Name     | Required | Description                                                                                                        |
|----------|----------|--------------------------------------------------------------------------------------------------------------------|
| port     | yes      | Serial port connected to the module                                                                                |
| timeout  | no       | Timeout value in milliseconds for connect, read and write operations, defaults to 5000 (5secs)                     |
| refresh  | no       | Polling interval in milliseconds, defaults to 5000 (5secs)                                                         |
| userCode | no       | Security code of the user in behalf of all operations will be executed. If empty, only read operations are allowed |
| encoding | no       | Encoding for all the texts received from the module                                                                |

Example:

```
Bridge satel:int-rs:home [ port="/dev/ttyS0", refresh=1000, userCode="1234" ]
```

### partition

You can configure the following settings for a partition:

| Name        | Required | Description                                                      |
|-------------|----------|------------------------------------------------------------------|
| id          | yes      | Partition number                                                 |
| forceArming | no       | Arms the partition regardless of ongoing troubles and violations |

Example:

```
Thing partition partition1 [ id=1, forceArming=true ]
```

### zone

You can configure the following settings for a zone:

| Name        | Required | Description                    |
|-------------|----------|--------------------------------|
| id          | yes      | Zone number                    |
| invertState | no       | Changes active (ON) state to 0 |

Example:

```
Thing zone zone1 [ id=1 ]
```

### output

You can configure the following settings for an output:

| Name        | Required | Description                                          |
|-------------|----------|------------------------------------------------------|
| id          | yes      | Output number                                        |
| invertState | no       | Changes active (ON) state to 0                       |
| commandOnly | no       | Accepts commands only, does not update thing's state |

Example:

```
Thing output output1 [ id=1, invertState=true ]
```

### shutter

You can configure the following settings for a shutter:

| Name        | Required | Description                                          |
|-------------|----------|------------------------------------------------------|
| upId        | yes      | Output number for "up" direction                     |
| downId      | yes      | Output number for "down" direction                   |
| commandOnly | no       | Accepts commands only, does not update thing's state |

Example:

```
Thing shutter shutter1 [ upId=10, downId=11, commandOnly=true ]
```

### system

This thing type does not have any configuration parameters.

## Channels

### partition

| Name               | Type   | Description                       |
|--------------------|--------|-----------------------------------|
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

| Name                   | Type   | Description            |
|------------------------|--------|------------------------|
| violation              | Switch | Violation              |
| tamper                 | Switch | Tamper                 |
| alarm                  | Switch | Alarm                  |
| tamper_alarm           | Switch | Tamper alarm           |
| alarm_memory           | Switch | Alarm memory           |
| tamper_alarm_memory    | Switch | Tamper alarm memory    |
| bypass                 | Switch | Bypass                 |
| no_violation_trouble   | Switch | No violation trouble   |
| long_violation_trouble | Switch | Long violation trouble |
| isolate                | Switch | Isolate                |
| masked                 | Switch | Masked                 |
| masked_memory          | Switch | Masked memory          |

### output

| Name  | Type   | Description         |
|-------|--------|---------------------|
| state | Switch | State of the output |

### shutter

| Name  | Type          | Description          |
|-------|---------------|----------------------|
| state | Rollershutter | State of the shutter |

### system

| Name            | Type     | Description                                                                                                                        |
|-----------------|----------|------------------------------------------------------------------------------------------------------------------------------------|
| date_time       | DateTime | Date and time on the alarm system                                                                                                  |
| troubles        | Switch   | Active when the system has troubles (trouble led is blinking on a panel)                                                           |
| troubles_memory | Switch   | Memorized state of system troubles                                                                                                 |
| service_mode    | Switch   | Active when the system is in service mode                                                                                          |
| acu100_present  | Switch   | Active when there is an ACU-100 module installed in the system                                                                     |
| intrx_present   | Switch   | Active when there is an INT-RX module installed in the system                                                                      |
| grade23_set     | Switch   | Active when Grade2/Grade3 option is set in the system                                                                              |
| user_code       | String   | Accepts string commands that override configured user code. Send empty string to revert user code to the one in the configuration. |

## Full Example

### satel.things

```
Bridge satel:ethm-1:home [ host="192.168.0.2", refresh=1000, userCode="1234", encryptionKey="abcdefgh" ] {
    Thing partition MainPartition [ id=1 ]
    Thing zone LivingRoomPIR [ id=1 ]
    Thing zone BedroomPIR [ id=2 ]
    Thing output KitchenLamp [ id=1 ]
    Thing shutter KitchenWindow [ upId=2, downId=3 ]
    Thing system System [ ]
}
```

### satel.items

```
Group Satel
Group:Switch:OR(ON,OFF) Alarms "Alarms [(%d)]" <siren>
Switch PARTITION_ARMED "Partition armed" (Satel) { channel="satel:partition:home:MainPartition:armed" }
Switch PARTITION_ALARM "Partition alarm" (Satel,Alarms) { channel="satel:partition:home:MainPartition:alarm" }
Switch LIVING_VIOLATION "Violation in living room" (Satel) { channel="satel:zone:home:LivingRoomPIR:violation" }
Switch LIVING_ALARM "Intruder in living room" (Satel) { channel="satel:zone:home:LivingRoomPIR:alarm" }
Switch BEDROOM_TAMPER "Bedroom PIR tampered" (Satel) { channel="satel:zone:home:BedroomPIR:tamper_alarm" }
Switch BEDROOM_TAMPER_M "Bedroom PIR tamper memory" (Satel) { channel="satel:zone:home:BedroomPIR:tamper_alarm_memory" }
Switch KITCHEN_LAMP "Kitchen lamp" (Satel) { channel="satel:output:home:KitchenLamp:state" }
Rollershutter KITCHEN_BLIND "Kitchen blind" (Satel) { channel="satel:shutter:home:KitchenWindow:state" }
Switch SYSTEM_TROUBLES "Troubles in the system" (Satel) { channel="satel:system:home:System:troubles" }
String KEYPAD_CHAR ">" <none> (Satel)
String USER_CODE "User code" (Satel) { channel="satel:system:home:System:user_code" }
```

### satel.sitemap

```
Frame label="Alarm system" {
    Switch item=Alarms mappings=[OFF="Clear"]
    Switch item=PARTITION_ARMED mappings=[ON="Arm", OFF="Disarm"]
    Frame label="Status" {
        Switch item=SYSTEM_TROUBLES mappings=[OFF="Clear"]
        Switch item=LIVING_VIOLATION
        Switch item=LIVING_ALARM
        Switch item=BEDROOM_TAMPER
        Switch item=BEDROOM_TAMPER_M
    }
    Frame label="Kitchen" {
        Switch item=KITCHEN_LAMP
        Rollershutter item=KITCHEN_BLIND
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

```
var String userCode = ""
var Timer keypadTimer = null
var Timer userCodeTimer = null

rule "Keypad char entered"
when
    Item KEYPAD_CHAR changed
then
    val org.joda.time.DateTime timeout = now.plusSeconds(20)

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
```

## Security considerations

### User for openHAB integration

To control Integra partitions and outputs, you need to provide security code of a user in behalf of all those operations will be executed. It is highly recommended to use a separate user for openHAB integration with only required access rights set in Integra configuration, like access to certain partitions, etc. This allows you to distinguish actions made by openHAB and a user using Integra panel, also it will block unwanted operations in case someone breaks into your local network.

### Disarming and clearing alarms

Although this binding allows you to configure disarming a partition and clearing alarms for a partition, this should be used only in cases when security is not the priority. Don't forget both these operations can be executed in openHAB without specifying a user code, which is required to disarm or clear alarms using Integra panel. Consider adding a keypad in your sitemap to temporarily change user code to execute sensitive operations. You can find such keypad in the [Full Example](#full-example) section.

## Media

* [Arming and clearing troubles](https://www.youtube.com/watch?v=ogdgn0Dk1G8)
