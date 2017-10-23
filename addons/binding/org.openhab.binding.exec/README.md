# Exec Binding

This binding integrates the possibility to execute arbitrary shell commands.

## Supported Things

Currently, the binding supports a single type of Thing, being the ```command``` Thing.

## Binding Configuration

The binding does not require any specific configuration.

Note that the commands are executed in the context and with the privileges of the process running the java virtual machine. It is not advised to run the virtual machine as superuser/root. Have a closer look at the example for how to test if the command you want to execute can run as user you like.

## Thing Configuration

The `command` Thing requires the command to execute on the shell. Optionally one can specify:

- a transformation to apply on the execution result, 
- an interval, in seconds, the command will be repeatedly executed, 
- a time-out, in seconds, the execution of the command will time out, and lastly, 
- a boolean parameter to make the command execute immediately every time the state of the input channel has changed. 

For each command a separate Thing has to be defined.

```
Thing exec:command:apc [command="/usr/local/bin/apcaccess  status", interval=15, timeout=5, autorun=false]
```

```command``` itself can be enhanced using the well known syntax of the **java.util.Formatter** class. 
The following parameters are automatically added:

- the current date (as java.util.Date, example: `%1$tY-%1$tm-%1$td`)
- the current State of the input channel (see below, example: `%2$s`)

## Channels

All Things support the following channels:

| Channel Type ID | Item Type | Description                                                                             |
|-----------------|-----------|-----------------------------------------------------------------------------------------|
| input           | String    | Input parameter to provide to the command                                               |
| output          | String    | Output of the last execution of the command                                             |
| exit            | Number    | The exit value of the last execution of the command                                     |
| run             | Switch    | Send ON to execute the command and the current state tells whether it is running or not |
| lastexecution   | DateTime  | Time/Date the command was last executed, in yyyy-MM-dd'T'HH:mm:ss.SSSZ format           |

## Short Example

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

## Full Example

Following is a example shown how to read out the temperature of the RPI with all files needed to set up and debug or log the executet command.
The folder structure is for an installation with apt-get, for a different installation setup look [in the manual](http://docs.openhab.org/installation/linux.html#file-locations) or search for them in your drive.

First we need to check if the user openhab is able to execute the command we want to execute. The first command is executet as the user we are logged in the second as user openhab. If the execution as openhab displays the same as the execution with your user, then everything is fine. If you get an error or a massage indicating the command does not execute properly you have to search for the needed permission and set it for the user openhab. More details can be found in community threads. [Following thread](https://community.openhab.org/t/2-openhab2-rpi-system-temperature-chart-with-persistence/35182) explains all elements of this setup in detail and maybe you need to get openhab [execute commands with sudo.](https://community.openhab.org/t/openhab-sudo-exec-binding/34988). But mostly you don't.

```
cat /sys/class/thermal/thermal_zone0/temp
48312
sudo -u openhab cat /sys/class/thermal/thermal_zone0/temp
47774
```

As the command got executed as user openhab we can proceed to set up our openHAB.

First we need a **Things** file which configures the command line call to execute.  
```
sudo nano /etc/openhab2/things/exec.things
```
```
Thing exec:command:cpuTemp [
        command="cat /sys/class/thermal/thermal_zone0/temp",
        interval=10,
        autorun=false]
```

Then we need an **Items** file to store the string we get back from the execution and to store the transformed and divided temperature as number.

```
sudo nano /etc/openhab2/items/SysTemp.items
```
```
Number System_Temperature_CPU "Temperature CPU [%.1f °C]"

// Output of command line execution 
String cpuTemp_out { channel="exec:command:cpuTemp:output" }
```

Then we need a **Sitemap** file to configure the site which will be displayed. **It has to have the same name as the sitemap inside the file.**
```
sudo nano /etc/openhab2/sitemaps/SysTemp.sitemap
```
```
// Name of file and name of sitemap has to be the same
sitemap SysTemp label="System Temperature RPI"
{
        Frame {
            Text item=System_Temperature_CPU
        }
}
```

Now we need a **Rules** file which is triggered when the returned string of our execution changes and then transforms string to a number, divide it and also log the output of the execution.
```
sudo nano /etc/openhab2/rules/SysTemp.rules
```
```
rule "System CPU Temperature"
  when
     Item cpuTemp_out received update
  then
  
      System_Temperature_CPU.postUpdate( 
            (Integer::parseInt(cpuTemp_out.state.toString) as Number ) 
            /1000 
      )
      
      logInfo("CPU Temp", cpuTemp_out.state.toString.trim )
end

```

The logging massages can be viewed in the Karaf console have a closer look [in the manual](http://docs.openhab.org/administration/console.html) for more information.

This shows a basic setup, if you are new to openHAB and you try to find out how things work, you probably want to chart this value, so have a look in the [forum](https://community.openhab.org/) and search for [charts and persistence Tutorial](https://community.openhab.org/search?q=charts%20and%20persistence%20Tutorial).

## Sources
[OpenHAB 1 Addons wiki](https://github.com/openhab/openhab1-addons/wiki/Raspberry-Pi-System-Temperature)

[OpenHAB Community Thread](https://community.openhab.org/t/reading-raspberry-pi-cpu-temp-with-exec-binding/4964)
