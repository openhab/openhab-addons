# Exec Binding

This binding integrates the possibility to execute arbitrary shell commands.

## Supported Things

Currently, the binding supports a single type of Thing, being the ```command``` Thing.

## Binding Configuration

The binding does not require any specific configuration.


Note that the commands are executed in the context and with the privileges of the process running the java virtual machine. It is not advised to run the virtual machine as superuser/root. Linux Os needs the user openhab/openhabian to be able to execute dedicated command, it is advised to always test in the command line if this is possible.

```
sudo -u openhab <YOUR COMMAND>
```

## Thing Configuration


The ```command``` Thing requires the command to execute on the shell. Optionally one can specify:


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


## Minimal Example

**demo.things**

```
Thing exec:command:apc [command="/usr/local/bin/apcaccess  status", interval=15, timeout=5]
Thing exec:command:myscript [command="php ./configurations/scripts/script.php %2$s", transform="REGEX((.*?))"]
```

**demo.items**

```

String APCRaw "[%s]" (All) {channel="exec:command:apc:output"} 
String APCRunning { channel="exec:command:apc:run"}
String APCExitValue {channel="exec:command:apc:exit"}
String APCLastExecution {channel="exec:command:apc:lastexecution"}
```

## Full Example

Following is an example how to set up an exec Thing, debug it with a rule and set the returned string to an Number Item. 

**For this to work also the openhab-transformation-regex has to be installed**

**demo.things**
```
Thing exec:command:yourcommand [ command="<YOUR COMMAND> %2$s"         
                                 interval=0,
                                 autorun=true ]
```

**demo.items**
```
Switch YourTrigger
Number YourNumber "Your Number [%.1f °C]"

// state of the execution, is runnung or finished
Switch yourcommand { channel="exec:command:yourcommand:run" }
// Arguments to be placed for '%2$s' in command line
String yourcommand_Args { channel="exec:command:yourcommand:input"}
// Output of command line execution 
String yourcommand_out { channel="exec:command:yourcommand:output" }
```

**demo.sitemap**
```
// Name of file and name of sitemap has to be the same
sitemap your label="Your Value"
{
        Frame {
            Switch item=YourTrigger
            Text item=YourNumber
        }
}
```

**demo.rules**
```
rule "Your Execution"
  when
     Item YourTrigger received update
  then
        if(YourTrigger == ON){
                yourcommand_Args.sendCommand("Additional Arguments")
        }else{
                yourcommand_Args.sendCommand("Other Additional Arguments")
        }

      // wait for the command to complete
      // State will be NULL if not used before or ON while command is executed
      while(yourcommand.state != OFF){
         Thread::sleep(500)
      }
      logInfo("Your command exec", "Resut:" + yourcommand_out.state )
      
      // If the returned string is just a number it can be parsed
      // If not a regex or another transformation can be used
      YourNumber.postUpdate( 
            (Integer::parseInt(yourcommand_out.state.toString) as Number ) 
      ) 
end

```
The logging messages can be viewed in the Karaf console have a closer look [in the manual](http://docs.openhab.org/administration/console.html) for more information

## Source

[OpenHAB Community Thread with an detailed example.](https://community.openhab.org/t/1-openhab-433mhz-radio-transmitter-tutorial/34977)

