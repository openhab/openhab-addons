# Exec Binding

This binding integrates the possibility to execute arbitrary shell commands.

## Supported Things

Currently, the binding supports a single Thing, being the 'command' thing.

## Binding Configuration

The binding does not require any specific configuration

## Thing Configuration

The command Thing requires the command to execute on the shell, and optionally one can specify a transformation to apply on the execution result, an interval, in seconds, the command will be repeatedly executed and lastly a time-out, in seconds, the execution of the command will time out

```
Thing exec:command:apc [ command="/usr/local/bin/apcaccess  status", interval=15, timeout=5]
```

## Channels

All devices support the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|----------------- |------------- |
| output | String       | Output of the last execution of the command |
| exit | Number       | The exit value of the last execution of the command |
| run | Switch       | Send ON to execute the command and the current state tells whether it is running or not |
| lastexecution | DateTime       | Time/Date the command was last executed, in yyyy-MM-dd'T'HH:mm:ss.SSSZ format |

## Full Example

demo.Things:

```
Thing exec:command:apc [ command="/usr/local/bin/apcaccess  status", interval=15, timeout=5]
```

demo.items:

```
String APCRaw "[%s]" (All) {channel="exec:command:apc:output"} 
Switch APCRunning { channel="exec:command:apc:running"}
Number APCExitValue {channel="exec:command:apc:exit"}
Switch APCExecute {channel="exec:command:apc:execute"}
DateTime APCLastExecution {channel="exec:command:apc:lastexecution"}
```