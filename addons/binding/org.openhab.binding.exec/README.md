# Exec Binding

This binding integrates the possibility to execute arbitrary shell commands.

## Supported Things

Currently, the binding supports a single type of Thing, being the `command` Thing.

## Binding Configuration

The binding does not require any specific configuration.

Note that the commands are executed in the context and with the privileges of the process running the java virtual machine. It is not advised to run the virtual machine as superuser/root. The execution of the commands is triggered by either sending a Command or State Update to the input Channel of the `command` Thing, if configured so, or by sending the ON Command to the run Channel of the `command` Thing (see below)

## Thing Configuration

The `command` Thing requires the following mandatory configuration parameters:

- [command] the command to execute on the shell. 

Optionally one can specify:

- [transform] a transformation to apply on the execution result, 
- [interval] an interval, in seconds, the [command] will be repeatedly executed, 
- [timeout] a time-out, in seconds, after which the execution of the [command] will time out,
- [runOnInput] a boolean parameter to make the [command] execute immediately every time a Command or a State Update is sent to the input channel of the `command` Thing, and lastly, 
- [repeatEnabled] a boolean parameter to allow the [command] to be executed repeatedly, e.g. when the same Command or State Update is sent to input channel repeatedly. This parameter only makes sense when used in combination with [runOnInput] set to true.

For each [command] a separate Thing has to be defined.

```
Thing exec:command:apc [command="/usr/local/bin/apcaccess  status", interval=15, timeout=5, runOnInput=true, repeatEnabled=true]
```

[command] itself can be enhanced using some specific qualifiers that will be substituted at runtime by actual values. The qualifiers have the following syntax:

`${<key>:<transform>:<formatter>}`

whereby 

 - <key> can be
     - the name of any Item
     - `exec-input`, denoting the current State of the input Channel of the `command` Thing
     - `exec-time`, denoting the current date (as java.util.Date)     
- <transform> is any valid Transformation service expression, e.g. REGEX((.*?))
- <formatter> is a formatting string using the well known syntax of the `java.util.Formatter` class

At runtime the binding will grab the value of the <key>, transform it with the <transform> expression, and finally format the transformation result using the <formatter> format. For example,

```
Thing exec:command:lightcontroller [command="/usr/local/bin/light.sh  ${lightSwitch:MAP(en.map):%1$s}"]
```

When the [command] of the lightcontroller item is executed then the value of the lightSwitch Item is retrieved, transformed using the en.map MAP Transform, and passed on as a literal string (i.e. %1$s takes the first argument of the result of the Transform, and formats it as a String) to the ligh.sh script

Nesting of substitution keys is supported, e.g. `${lightSwitch_${lightCounter}}` will resolve the lightCounter Item first, and then subsequently resolve the second substitution key. If the value of lighCounter would happen to be for example 3, then the second substitution resolved to the value of Item `lighSwitch_3`

```
Thing exec:command:lightcontroller [command="/usr/local/bin/light.sh  ${exec-input}"]
```

In the above example, exec-input is substituted with the actual State of the input Channel of the `exec:command:lightcontroller` Thing

The Channels themselves are defined as custom State Channels (https://github.com/eclipse/smarthome/blob/master/docs/documentation/features/dsl.md#defining-channels), so one can freely define the Type of each Channel. The `command` Thing supports both an `input` and `output` Channel to set an input for the [command] and get the output of the command execution.

## Channels

All Things support the following channels:

| Channel Type ID | Item Type    | Description                               |
|-----------------|--------------|-------------------------------------------|
| input           | custom       | Input parameter to provide to the command |
| output          | custom       | Output of the last execution of the command |
| exit            | Number       | The exit value of the last execution of the command |
| run             | Switch       | Send ON to execute the command. the current State of this channel tells whether the command is running or not |
| lastexecution   | DateTime       | Time/Date the command was last executed, in yyyy-MM-dd'T'HH:mm:ss.SSSZ format |

## Full Example

**demo.things**

```
Thing exec:command:apc [command="/usr/local/bin/apcaccess  status", interval=15, timeout=5]
Thing exec:command:myscript [command="php ./configurations/scripts/script.php ${exec-input}", transform="REGEX((.*?))"]
Thing exec:command:switch_monitor [command="switch_control.sh check", interval=1]
Thing exec:command:switch_control [command="switch_control.sh ${exec-input}", runOnInput=true, repeatEnabled=true] {
Channels:
        Switch : input
        String : output
}
```

**demo.items**

```
String APCRaw "[%s]" (All) {channel="exec:command:apc:output"} 
Switch APCRunning { channel="exec:command:apc:run"}
Number APCExitValue {channel="exec:command:apc:exit"}
DateTime APCLastExecution {channel="exec:command:apc:lastexecution"}
Switch LampSwitch {channel="exec:command:switch_control:switchInput", channel="exec:command:switch_monitor:switchOutput" }
```