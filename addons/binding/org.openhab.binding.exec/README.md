# Exec Binding

This binding integrates the possibility to execute arbitrary shell commands.

## Supported Things

Currently, the binding supports a single type of Thing, being the `command` Thing.

## Binding Configuration

The binding does not require any specific configuration.

Note that the commands are executed in the context and with the privileges of the process running the java virtual machine.
It is not advised to run the virtual machine as superuser/root.
Have a closer look at the example for how to test if the command you want to execute can run as user you like.

## Thing Configuration

The `command` Thing requires the command to execute on the shell.
Optionally one can specify:

- `transform` - A transformation to apply on the execution result
- `interval` - An interval, in seconds, the command will be repeatedly executed
- `timeout` - A time-out, in seconds, after which the execution of the command will be terminated
- `autorun` - A boolean parameter to make the command execute immediately every time the state of the input channel has changed

For each command a separate Thing has to be defined.

```java
Thing exec:command:uniquename [command="/command/to/execute here", interval=15, timeout=5, autorun=false]
```

The `command` itself can be enhanced using the well known syntax of the [java.util.Formatter](https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html) class.
The following parameters are automatically added:

- the current date (as java.util.Date, example: `%1$tY-%1$tm-%1$td`)
- the current State of the input channel (see below, example: `%2$s`)

## Channels

All Things support the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|
| input | String       | Input parameter to provide to the command |
| output | String       | Output of the last execution of the command |
| exit | Number       | The exit value of the last execution of the command |
| run | Switch       | Send ON to execute the command and the current state tells whether it is running or not |
| lastexecution | DateTime       | Time/Date the command was last executed, in yyyy-MM-dd'T'HH:mm:ss.SSSZ format |

## Short Example

**demo.things**

```java
Thing exec:command:apc [command="/usr/local/bin/apcaccess status", interval=15, timeout=5]
Thing exec:command:myscript [command="php ./configurations/scripts/script.php %2$s", transform="REGEX((.*?))"]
```

**demo.items**

```java
String APCRaw "[%s]" (All) {channel="exec:command:apc:output"}
Switch APCRunning {channel="exec:command:apc:run"}
Number APCExitValue {channel="exec:command:apc:exit"}
DateTime APCLastExecution {channel="exec:command:apc:lastexecution"}
```

## Full Example

Following is a example shown how to read out the temperature of the RPI with all files needed to set up and debug or log the executed command.
The folder structure is for an installation with apt-get, for a different installation setup look [here](http://docs.openhab.org/installation/linux.html#file-locations) or search for them in your drive.

First we need to check if the user openhab is able to execute the command we want to execute.
The first command is executet as the user we are logged in the second as user openhab.
If you get an error or an massages which indicates the command does not execute properly as user openhab then try to google the needed permissions and set them for the user openhab.
More details [here](https://community.openhab.org/t/2-openhab2-rpi-system-temperature-chart-with-persistence/35182) and [here](https://community.openhab.org/t/openhab-sudo-exec-binding/34988).

```shell
cat /sys/class/thermal/thermal_zone0/temp
48312

sudo -u openhab cat /sys/class/thermal/thermal_zone0/temp
47774
```

As the command got executed as user `openhab` we can proceed to set up our OpenHAB.

First we need a **Thing** file which configures the command line call to execute.

```shell
sudo nano /etc/openhab2/things/exec.things
```

```java
Thing exec:command:cpuTemp [
        command="cat /sys/class/thermal/thermal_zone0/temp",
        interval=10,
        autorun=false]
```

Then we need an **Items** file to store hte string we get back from the execution and to stored the transformed and divided temperature as number.

```shell
sudo nano /etc/openhab2/items/SysTemp.items
```

```java
Number System_Temperature_CPU "Temperature CPU [%.1f °C]"

// Output of command line execution
String cpuTemp_out { channel="exec:command:cpuTemp:output" }
```

Then we need a **Sitemap** file to configure the site which will be displayed.
**It has to have the same name as the sitemap inside the file.**

```shell
sudo nano /etc/openhab2/sitemaps/SysTemp.sitemap
```

```java
// Name of file and name of sitemap has to be the same
sitemap SysTemp label="System Temperature RPI"
{
        Frame {
            Text item=System_Temperature_CPU
        }
}
```

Now we need a **Rule** file which is triggered when the returned string of our execution changes and then transforms string to a number, divide it and also log the output of the execution.

```shell
sudo nano /etc/openhab2/rules/SysTemp.rules
```

```java
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

The logging massages can be viewed in the Karaf console have a closer look [here](http://docs.openhab.org/administration/console.html) for more information.

If you like to chart the values follow the [Tutorial](https://community.openhab.org/t/2-openhab2-rpi-system-temperature-chart-with-persistence/35182)
link and it works go ahead to the more advanced [InfluxDB+Grafana](https://community.openhab.org/t/influxdb-grafana-persistence-and-graphing/13761) Tutorial.

## Sources

- [OpenHAB 1 Addons wiki](https://github.com/openhab/openhab1-addons/wiki/Raspberry-Pi-System-Temperature)
- [OpenHAB Community Thread](https://community.openhab.org/t/reading-raspberry-pi-cpu-temp-with-exec-binding/4964)
