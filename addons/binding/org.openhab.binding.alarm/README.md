# Alarm Binding

This is the binding for an alarm controller inspired by [Aritech](https://aritech-security.de) and [Abus](https://www.abus.com)  

The binding is kept very simple and allows you to create alarm controllers with a configurable amount of alarm zones. Each alarm zone has a type like ACTIVE, SABOTAGE, EXIT_ENTRY, ... and you can bind a (Window/Motiondetector/...) Contact to each alarm zone. You can also send some commands to the controller for arming and disarming.

## Supported Bridges

None, this binding does not need a bridge

## Supported Things

This binding supports only one Thing: An alarm controller

## Discovery

On discovery, the binding creates a default alarm controller

## Thing Configuration

One alarm controller is created automatically, you can handle it in PaperUI.  

You can also configure one or multiple controllers (things) manually:

```java
alarm:controller:home [alarmZones=10, entryTime=30, exitTime=30, passthroughTime=30, alarmDelay=30] {
    Channels:
        Type alarmZone : alarmZone_1 "My alarm zone"    [ type = "ACTIVE" ]
        Type alarmZone : alarmZone_2 "My sabotage zone" [ type = "SABOTAGE" ]
}
```

| Thing config | Description |
|-|-|
| alarmZones      | Required, the number of alarm zones the controller should handle, max 1000     |
| entryTime       | The time in seconds until alarm at entry                                       |
| exitTime        | The time in seconds until arming at exit                                       |
| passthroughTime | The time in seconds to passthrough a exit/entry alarm zone on internally armed |
| alarmDelay      | The time in seconds the alarm is delayed                                       |
| | |

All alarm zone channels have a type. With the alarm zone types you can define the behaviour of the individual alarm zone:

| Alarm zone types  | Description |
|-|-|
| ```DISABLED``` | Ignored by the controller |
| ```ACTIVE```   | Default type, active on external arming |
| ```INTERN_ACTIVE``` | Alarm zone is active on internal **AND** external arming |
| ```EXIT_ENTRY``` | Set this type to an alarm zone, where you enter and leave the secured area, e.g door(s) |
| ```IMMEDIATELY``` | Activates the alarm if armed, ignoring the configured delays |
| ```SABOTAGE``` | Tags the alarm zone as sabotage zone, triggers a ```SABOTAGE_ALARM```, even when the controller is ```DISARMED``` |
| ```ALWAYS``` | Always triggers an alarm, even when the controller is ```DISARMED``` |
| ```ALWAYS_IMMEDIATELY``` | Same as ```ALWAYS``` but ignores the configured delays |
| | |


You can send these commands to the controller:
| Commands  | Description |
|-|-|
| ```ARM_INTERNALLY``` | Activates the internal armed mode |
| ```ARM_EXTERNALLY``` | Activates the external armed mode |
| ```PASSTHROUGH``` | Activates the passthrough mode. If the controller is internal armed and you activate pasthrough, you can open alarm zones of type ```EXIT_ENTRY``` without triggering an alarm |
| ```FORCE_ALARM``` | Immediately triggers an alarm |
| ```DISARM``` | Disarmes the controller |
| | |

Available status:
| Status  | Description |
|-|-|
| ```DISARMED``` | Disarmed, watching only alarm zones with type ```SABOTAGE```, ```ALWAYS``` and ```ALWAYS_IMMEDIATELY``` |
| ```INTERNALLY_ARMED``` | Watches also all ```INTERN_ACTIVE``` alarm zones |
| ```EXTERNALLY_ARMED``` | Watches all alarm zones |
| ```ENTRY``` | Someone opens a ```ENTRY_EXIT``` alarm zone in external armed mode and a entry time has been configured |
| ```EXIT``` | After activating external armed mode and a exit time has been configured |
| ```PASSTHROUGH``` | After activating passthrough and a passthrough time has been configured |
| ```PREALARM``` | Before an alarm when a alarm delay has been configured |
| ```ALARM``` | An alarm has been triggered |
| ```SABOTAGE_ALARM``` | A sabotage alarm has been triggered |
| | |


## Item examples
```java
String  Alarm_Status     "Status"           { channel = "alarm:controller:home:status" }
String  Alarm_Command    "Command"          { channel = "alarm:controller:home:command" }
Number  Alarm_Countdown  "Countdown [%d]"   { channel = "alarm:controller:home:countdown" }
Switch  Can_Arm_Internal "Can Arm Internal" { channel = "alarm:controller:home:internalArmingPossible" }
Switch  Can_Arm_External "Can Arm External" { channel = "alarm:controller:home:externalArmingPossible" }
Switch  Can_Passthrough  "Can Passthrough"  { channel = "alarm:controller:home:passthroughPossible" }

Contact Alarmzone_1      "Alarmzone_1"      { channel = "alarm:controller:home:alarmZone_1" }
Contact Alarmzone_2      "Alarmzone_2"      { channel = "alarm:controller:home:alarmZone_2" }
```
If you bind the alarm zones this way, you have to feed them manually in rules. You can also bind them directly to real contacts.  
Let's say you have a Homematic window contact and you like to map this contact directly to alarm zone one:

```java
Contact Kitchen_Window "Kitchen" { channel="homematic:HMW-Sen-SC-12-FM:ccu:KEQ00*****:1#SENSOR, alarm:controller:home:alarmZone_1" }
```

## Commands

You can send commands to the alarm controller
```java
// arming
sendCommand(Alarm_Command, "ARM_INTERNALLY")
// or
sendCommand(Alarm_Command, "ARM_EXTERNALLY")

// disarming
sendCommand(Alarm_Command, "DISARM")
```
