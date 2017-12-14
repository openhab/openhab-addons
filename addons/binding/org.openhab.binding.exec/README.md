# Exec Binding

This binding integrates the possibility to execute arbitrary shell commands.

## Supported Things

Currently, the binding supports a single type of Thing, being the ```command``` Thing.

## Binding Configuration

The binding does not require any specific configuration.

Note that the commands are executed in the context and with the privileges of the process running the java virtual machine.
It is not advised to run the virtual machine as superuser/root.

## Thing Configuration

The `command` Thing requires the command to execute on the shell.
Optionally one can specify:

-   a transformation to apply on the execution result,
-   an interval, in seconds, the command will be repeatedly executed,
-   a time-out, in seconds, the execution of the command will time out, and lastly,
-   a boolean parameter to make the command execute immediately every time the state of the input channel has changed.

For each command a separate Thing has to be defined.

```
Thing exec:command:apc [command="/usr/local/bin/apcaccess  status", interval=15, timeout=5, autorun=false]
```

`command` itself can be enhanced using the well known syntax of the **java.util.Formatter** class.
The following parameters are automatically added:

-   the current date (as java.util.Date, example: `%1$tY-%1$tm-%1$td`)
-   the current State of the input channel (see below, example: `%2$s`)

## Channels

All Things support the following channels:

| Channel Type ID | Item Type | Description                                                                             |
|-----------------|-----------|-----------------------------------------------------------------------------------------|
| input           | String    | Input parameter to provide to the command                                               |
| output          | String    | Output of the last execution of the command                                             |
| exit            | Number    | The exit value of the last execution of the command                                     |
| run             | Switch    | Send ON to execute the command and the current state tells whether it is running or not |
| lastexecution   | DateTime  | Time/Date the command was last executed, in yyyy-MM-dd'T'HH:mm:ss.SSSZ format           |


**Example**

**demo.things**

```
Thing exec:command:apc [command="/usr/local/bin/apcaccess  status", interval=15, timeout=5]
Thing exec:command:myscript [command="php ./configurations/scripts/script.php %2$s", transform="REGEX((.*?))"]
```

**demo.items**

```
String APCRaw "[%s]" (All) {channel="exec:command:apc:output"}
Switch APCRunning { channel="exec:command:apc:run"}
Number APCExitValue {channel="exec:command:apc:exit"}
DateTime APCLastExecution {channel="exec:command:apc:lastexecution"}
```
