# Exec Binding

This binding integrates the possibility to execute arbitrary shell commands.

## Supported Things

Currently, the binding supports a single type of Thing, being the `command` Thing.

## Binding Configuration

The binding does not require any specific configuration.


**Linux:**
Note that the commands are executed in the context and with the privileges of the process running the Java Virtual Machine.
On a Linux system the system user `openhab` needs to have the privileges needed to execute your intended command.
It is advised to test the correct operation of the command in the scope of the `openhab` user on the command line first:

```shell
sudo -u openhab <YOUR COMMAND>
```
It is not advised to run the virtual machine as superuser/root.

## Thing Configuration
.
The "command" Thing requires the command to execute on the shell.
Optionally one can specify:


- `transform` - A [transformation](https://www.openhab.org/docs/configuration/transformations.html) to apply on the execution result,
- `interval` - An interval, in seconds, the command will be repeatedly executed,
- `timeout` - A time-out, in seconds, the execution of the command will time out, and lastly,
- `autorun` - A boolean parameter to make the command execute immediately every time the state of the input channel has changed.

For each command a separate Thing has to be defined.

```java
Thing exec:command:uniquename [command="/command/to/execute here", interval=15, timeout=5, autorun=false]
```


The `command` itself can be enhanced using the well known syntax of the [Java formatter class syntax](http://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html#syntax).
The following parameters are automatically added:

-   the current date (as java.util.Date, example: `%1$tY-%1$tm-%1$td`)
-   the current State of the input channel (see below, example: `%2$s`)


## Channels

All Things support the following channels:

| Channel Type ID | Item Type | Description                                                                          |
|-----------------|-----------|--------------------------------------------------------------------------------------|
| input           | String    | Input parameter to provide to the command                                            |
| output          | String    | Output of the last execution of the command                                          |
| exit            | Number    | The exit value of the last execution of the command                                  |
| run             | Switch    | Send ON to execute the command, the current state tells whether it is running or not |
| lastexecution   | DateTime  | Time/Date the command was last executed, in yyyy-MM-dd'T'HH:mm:ss.SSSZ format        |


## Minimal Example

**demo.things**

```java
Thing exec:command:apc [command="/usr/local/bin/apcaccess status", interval=15, timeout=5]
Thing exec:command:myscript [command="php ./configurations/scripts/script.php %2$s", transform="REGEX((.*?))"]
```

**demo.items**

```java
String APCRaw "[%s]" (All) {channel="exec:command:apc:output"}
String APCRunning {channel="exec:command:apc:run"}
String APCExitValue {channel="exec:command:apc:exit"}
String APCLastExecution {channel="exec:command:apc:lastexecution"}
```

## Full Example

Following is an example how to set up an exec command thing, debug it with a rule and set the returned string to an Number Item. 

**For this to work also the openHAB RegEx Transformation has to be installed**

**demo.things**

```java
// "%2$s" will be replace by the input channel, this makes it possible to use one command line with different arguments.
// e.g: "ls" as <YOUR COMMAND> and "-a" or "-l" as additional argument set to the input channel in the rule.
Thing exec:command:yourcommand [ command="<YOUR COMMAND> %2$s", interval=0, autorun=false ]
```

**demo.items**

```java
Switch YourTrigger
Number YourNumber "Your Number [%.1f °C]"

// state of the execution, is running or finished
Switch yourcommand {channel="exec:command:yourcommand:run"}
// Arguments to be placed for '%2$s' in command line
String yourcommand_Args {channel="exec:command:yourcommand:input"}
// Output of command line execution 
String yourcommand_out {channel="exec:command:yourcommand:output"}
```

**demo.sitemap**

```java
// Name of file and name of sitemap has to be the same
sitemap demo label="Your Value"
{
        Frame {
            Switch item=YourTrigger
            Text item=YourNumber
        }
}
```

**demo.rules**

```java
rule "Your Execution"
  when
     Item YourTrigger changed
  then
        if(YourTrigger.state == ON){
                yourcommand_Args.sendCommand("Additional Argument to command line for ON")
        }else{
                yourcommand_Args.sendCommand("Additional Argument to command line for OFF")
        }

      // wait for the command to complete
      // State will be NULL if not used before or ON while command is executed
      while(yourcommand.state != OFF){
         Thread::sleep(500)
      }
      
      // Trigger execution
      yourcommand.sendCommand(ON)
      
      // Logging of command line result
      logInfo("Your command exec", "Result:" + yourcommand_out.state )
      
      // If the returned string is just a number it can be parsed
      // If not a regex or another transformation can be used
      YourNumber.postUpdate(
            (Integer::parseInt(yourcommand_out.state.toString) as Number )
      )
end
```

## Source

[OpenHAB community thread with a detailed example.](https://community.openhab.org/t/1-openhab-433mhz-radio-transmitter-tutorial/34977)
