# Exec Binding

This binding integrates the possibility to execute arbitrary shell commands.

## Supported Things

Currently, the binding supports a single type of Thing, being the ```command``` Thing.

## Binding Configuration

The binding does not require any specific configuration.

Note that the commands are executed in the context and with the privileges of the process running the java virtual machine. It is not advised to run the virtual machine as superuser/root.

## Thing Configuration

The `command` Thing requires the command to execute on the shell. Optionally one can specify:

- [transform] a transformation to apply on the execution result, 
- [interval] an interval, in seconds, the command will be repeatedly executed, 
- [timeput] a time-out, in seconds, the execution of the command will time out, and lastly, 
- [runOnInput] a boolean parameter to make the command execute immediately every time the state of an input channel is set by a Command 
- [repeatEnabled] a boolean parameter to allow the command execute repeatedly, e.g. when the state of an input channel is set repeatedly with the same state. This parameter only makes sense when used in combination with [runOnInput] set to true.

For each command a separate Thing has to be defined.

```
Thing exec:command:apc [command="/usr/local/bin/apcaccess  status", interval=15, timeout=5, runOnInput=true, repeatEnabled=true]
```

```command``` itself can be enhanced using the well known syntax of the **java.util.Formatter** class. 
The following parameters are automatically added:

- the current date (as java.util.Date, example: `%1$tY-%1$tm-%1$td`)
- the current State of an input channel (see below, example: `%2$s`)

A different input and output channel is defined for each Item Type. States of an input channel are translated into their string equivalent and passed on as a parameter to the command using the `%2$s` **java.util.Formatter** class syntax. If the output of a command execution matches the string equivalent of an Item Type, for example "ON" or "OFF" for a Switch, then the relevant output channel is updated with State equivalent of that output string, i.e. ON or OFF

## Channels

All Things support the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|----------------- |------------- |
| contactInput | Contact       | Input parameter to provide to the command. Contact compatible Item types are first translated to their string equivalent |
| dimmerInput | Dimmer       | Input parameter to provide to the command. Dommer compatible Item types are first translated to their string equivalent |
| rollershutterInput | Rollershutter       | Input parameter to provide to the command. Rollershutter compatible Item types are first translated to their string equivalent |
| stringInput | String       | Input parameter to provide to the command |
| switchInput | Switch       | Input parameter to provide to the command. Switch compatible Item types, e.g ON/OFF, are first translated to their string equivalent, e.g "ON"/"OFF" |
| contactOutput | Contact       | Output of the last execution of the command |
| dimmerOutput | Dimmer       | Output of the last execution of the command |
| rollershutterOutput | Rollershutter       | Output of the last execution of the command |
| stringOutput | String       | Output of the last execution of the command |
| switchOutput | Switch       | Output of the last execution of the command |
| exit | Number       | The exit value of the last execution of the command |
| run | Switch       | Send ON to execute the command and the current state tells whether it is running or not |
| lastexecution | DateTime       | Time/Date the command was last executed, in yyyy-MM-dd'T'HH:mm:ss.SSSZ format |

## Full Example

**demo.things**

```
Thing exec:command:apc [command="/usr/local/bin/apcaccess  status", interval=15, timeout=5]
Thing exec:command:myscript [command="php ./configurations/scripts/script.php %2$s", transform="REGEX((.*?))"]
Thing exec:command:switch_control [command="switch_control.sh %2$s", runOnInput=true, repeatEnabled=true]
Thing exec:command:switch_monitor [command="switch_control.sh check", interval=1]
```

**demo.items**

```
String APCRaw "[%s]" (All) {channel="exec:command:apc:output"} 
Switch APCRunning { channel="exec:command:apc:run"}
Number APCExitValue {channel="exec:command:apc:exit"}
DateTime APCLastExecution {channel="exec:command:apc:lastexecution"}
Switch LampSwitch {channel="exec:command:switch_control:switchInput", channel="exec:command:switch_monitor:switchOutput" }
```