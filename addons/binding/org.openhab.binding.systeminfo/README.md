# Systeminfo Binding

System information Binding provides operating system and hardware information including:

-   Operating system name, version and manufacturer;
-   CPU average recent load and load for last 1, 5, 15 minutes, name, description, number of physical and logical cores, running threads number, system uptime;
-   Free, total and available memory;
-   Free, total and available swap memory;
-   Hard drive name, model and serial number;
-   Free, total, available storage space and storage type (NTSFS, FAT32 ..);
-   Battery information - estimated remaining time, capacity, name;
-   Sensors information - CPU voltage and temperature, fan speeds;
-   Display information;
-   Network IP,name and adapter name, mac, data sent and received, packets sent and received;
-   Process information - size of RAM memory used, CPU load, process name, path, number of threads.

The binding uses [OSHI](https://github.com/oshi/oshi) API to access this information regardless of the underlying platform and does not need any native parts.

## Supported Things

The binding supports only one thing type - **computer**. This thing represents a system with one storage volume, one display device and one network adapter.

The thing has the following properties:

-   `cpu_logicalCores` - Number of CPU logical cores
-   `cpu_physicalCores` - Number of CPU physical cores
-   `os_manufacturer` - The manufacturer of the operating system
-   `os_version` - The version of the operating system
-   `os_family` - The family of the operating system

If multiple storage or display devices support is needed, new thing type has to be defined.
This is workaround until [this issue] (https://github.com/eclipse/smarthome/issues/588) is resolved and it is possible to add dynamically channels to DSL defined thing.

## Discovery

The discovery service implementation tries to resolve the computer name.
If the resolving process fails, the computer name is set to "Unknown".
In both cases it creates a Discovery Result with thing type  **computer**.

When [this issue] (https://github.com/eclipse/smarthome/issues/1118) is resolved it will be possible to implement creation of dynamic channels (e.g. the binding will scan how much storage devices are present and create channel groups for them).
At the moment this is not supported.

## Binding configuration

No binding configuration required.

## Thing configuration

The configuration of the Thing gives the user the possibility to update channels at different intervals.

The thing has two configuration parameters:

*   **interval_high** - refresh interval in seconds for channels with 'High' priority configuration. Default value is 1 s.
*   **interval_medium** - refresh interval in seconds for channels with 'Medium' priority configuration. Default value is 60s.

That means that by default configuration:

*   channels with priority set to 'High' are updated every second
*   channels with priority set to 'Medium' - every minute
*   channels with priority set to 'Low' only at initializing or at Refresh command.

For more info see [channel configuration](#channel-configuration)

## Channels

The binding support several channel group. Each channel group, contains one or more channels.
In the list below, you can find, how are channel group and channels id`s related.

**thing** `computer`
*   **group** `memory`
     **channel** `available, total, used, availablePercent, usedPercent`
*   **group** `swap`
     **channel** `available, total, used, availablePercent, usedPercent`
*   **group** `storage` (deviceIndex)
     **channel** `available, total, used, availablePercent, usedPercent, name, description, type`
*   **group** `drive` (deviceIndex)
     **channel** `name, model, serial`
*   **group** `display` (deviceIndex)
     **channel** `information`
*   **group** `battery` (deviceIndex)
     **channel** `name, remainingCapacity, remainingTime`
*   **group** `cpu`
     **channel** `name, description, load, load1, load5, load15, uptime`
*   **group** `sensors`
     **channel** `cpuTemp, cpuVoltage, fanSpeed`
*   **group** `network` (deviceIndex)
     **channel** `ip, mac, networkDisplayName, networkName, packetsSent, packetsReceived, dataSent, dataReceived`
*   **group** `process` (pid)
     **channel** `load, used, name, threads, path`

The groups marked with "deviceIndex" may have device index attached to the Channel Group.

-   channel ::= chnanel_group & (deviceIndex) & # channel_id
-   deviceIndex ::= number > 0
-   (e.g. *storage1#available*)

The group `process` is using a configuration parameter "pid" instead of "deviceIndex".
This makes possible to changed the tracked process at runtime.

The binding uses this index to get information about a specific device from a list of devices (e.g on a single computer could be installed several local disks with names C:\, D:\, E:\ - the first will have deviceIndex=0, the second deviceIndex=1 ant etc).
If device with this index is not existing, the binding will display an error message on the console.

Unfortunately this feature can't be used at the moment without manually adding these new channel groups to the thing description (located in ESH-INF/thing/computer.xml).

The table shows more detailed information about each Channel type.
The binding introduces the following channels:

| Channel ID         | Channel Description                                              | Supported item type | Default priority | Advanced |
|--------------------|------------------------------------------------------------------|---------------------|------------------|----------|
| load               | Recent load in percents                                          | Number              | High             | False    |
| load1              | Load for the last 1 minute                                       | Number              | Medium           | True     |
| load5              | Load for the last 5 minutes                                      | Number              | Medium           | True     |
| load15             | Load for the last 15 minutes                                     | Number              | Medium           | True     |
| threads            | Number of threads currently running                              | Number              | Medium           | True     |
| uptime             | System uptime (time after start) in minutes                      | Number              | Medium           | True     |
| name               | Name of the device                                               | String              | Low              | False    |
| available          | Available size in MB                                             | Number              | High             | False    |
| used               | Used size in MB                                                  | Number              | High             | False    |
| total              | Total size in MB                                                 | Number              | Low              | False    |
| availablePercent   | Available size in %                                              | Number              | High             | False    |
| usedPercent        | Used size in %                                                   | Number              | High             | False    |
| model              | The model of the device                                          | String              | Low              | True     |
| serial             | The serial number of the device                                  | String              | Low              | True     |
| description        | Description of the device                                        | String              | Low              | True     |
| type               | Storage type                                                     | String              | Low              | True     |
| cpuTemp            | CPU Temperature in Celsius degrees                               | Number              | High             | True     |
| cpuVoltage         | CPU Voltage in V                                                 | Number              | Medium           | True     |
| fanSpeed           | Fan speed in rpm                                                 | Number              | Medium           | True     |
| remainingTime      | Remaining time in minutes                                        | Number              | Medium           | False    |
| remainingCapacity  | Remaining capacity in percents                                   | Number              | Medium           | False    |
| information        | Product, manufacturer, SN, width and height of the display in cm | String              | Low              | True     |
| ip                 | Host IP address of the network                                   | String              | Low              | False    |
| mac                | MAC address                                                      | String              | Low              | True     |
| networkName        | The name of the network                                          | String              | Low              | False    |
| networkDisplayName | The display name of the network                                  | String              | Low              | False    |
| packetsSent        | Number of packets sent                                           | Number              | Medium           | True     |
| packetsReceived    | Number of packets received                                       | Number              | Medium           | True     |
| dataSent           | Data sent in MB                                                  | Number              | Medium           | True     |
| dataReceived       | Data received in MB                                              | Number              | Medium           | True     |


## Channel configuration

All channels can change its configuration parameters at runtime.
The binding will trigger the necessary changes (reduce or increase the refresh time, change channel priority or the process that is being tracked).

Each of the channels has a default configuration parameter - priority.
It has the following options:

-   **High**
-   **Medium**
-   **Low**

Channels from group ''process'' have additional configuration parameter - PID (Process identifier).
This parameter is used as 'deviceIndex' and defines which process is tracked from the channel.
This makes the channels from this groups very flexible - they can change its PID dynamically.

Parameter PID has a default value 0 - this is the PID of the System Idle process in Windows OS.

## Reporting issues

As already mentioned this binding depends heavily on the [OSHI](https://github.com/oshi/oshi) API to provide the operating system and hardware information.

Take a look at the console for an ERROR log message.

If you find an issue with a support for a specific hardware or software architecture please take a look at the [OSHI issues](https://github.com/oshi/oshi/issues).
Your problem might have be already reported and solved!
Feel free to open a new issue there with the log message and the and information about your software or hardware configuration.

After the issue is resolved the binding has to be [updated](#updating-this-binding).

For a general problem with the binding report the issue directly to openHAB.

## Updating this binding

OSHI project has a good support and regularly updates the library with fixes to issues and new features.

In order to update the version used in the binding, follow these easy steps:

-   Go to the [OSHI github repo](https://github.com/oshi/oshi) and download the newest version available of the module oshi-core or download the jar from the [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Coshi-). Check if the versions of the OSHI dependencies as well (jna and jna-platform) are changed;
-   Replace the jars in lib folder;
-   Modify the .classpath file with the new versions of the jars;
-   Modify the header Bundle-ClassPath in the META-INF/MANIFEST.MF.

## Example

Things:

```
systeminfo:computer:work [interval_high=3, interval_medium=60]
```

Items:

```
/* Network information*/
String Network_AdapterName          { channel="systeminfo:computer:work:network#networkDisplayName" }
String Network_Name                 { channel="systeminfo:computer:work:network#networkName" }
String Network_IP                   { channel="systeminfo:computer:work:network#ip" }
String Network_Mac                  { channel="systeminfo:computer:work:network#mac" }
Number Network_DataSent             { channel="systeminfo:computer:work:network#dataSent" }
Number Network_DataRecevied         { channel="systeminfo:computer:work:network#dataReceived" }
Number Network_PacketsSent         { channel="systeminfo:computer:work:network#packetsSent" }
Number Network_PacketsRecevied     { channel="systeminfo:computer:work:network#packetsReceived" }

/* CPU information*/
String CPU_Name                     { channel="systeminfo:computer:work:cpu#name" }
String CPU_Description              { channel="systeminfo:computer:work:cpu#description" }
Number CPU_Load                     { channel="systeminfo:computer:work:cpu#load"}
Number CPU_Load1                    { channel="systeminfo:computer:work:cpu#load1" }
Number CPU_Load5                    { channel="systeminfo:computer:work:cpu#load5" }
Number CPU_Load15                   { channel="systeminfo:computer:work:cpu#load15" }
Number CPU_Threads                  { channel="systeminfo:computer:work:cpu#threads" }
Number CPU_Uptime                   { channel="systeminfo:computer:work:cpu#uptime" }

/* Drive information*/
String Drive_Name                    { channel="systeminfo:computer:work:drive#name" }
String Drive_Model                   { channel="systeminfo:computer:work:drive#model" }
String Drive_Serial                  { channel="systeminfo:computer:work:drive#serial" }

/* Storage information*/
String Storage_Name                  { channel="systeminfo:computer:work:storage#name" }
String Storage_Type                  { channel="systeminfo:computer:work:storage#type" }
String Storage_Description           { channel="systeminfo:computer:work:storage#description" }
Number Storage_Available             { channel="systeminfo:computer:work:storage#available" }
Number Storage_Used                  { channel="systeminfo:computer:work:storage#used" }
Number Storage_Total                 { channel="systeminfo:computer:work:storage#total" }
Number Storage_Available_Percent     { channel="systeminfo:computer:work:storage#availablePercent" }
Number Storage_Used_Percent          { channel="systeminfo:computer:work:storage#usedPercent" }

/* Memory information*/
Number Memory_Available              { channel="systeminfo:computer:work:memory#available" }
Number Memory_Used                   { channel="systeminfo:computer:work:memory#used" }
Number Memory_Total                  { channel="systeminfo:computer:work:memory#total" }
Number Memory_Available_Percent      { channel="systeminfo:computer:work:memory#availablePercent" }
Number Memory_Used_Percent           { channel="systeminfo:computer:work:memory#usedPercent" }

/* Swap memory information*/
Number Swap_Available                { channel="systeminfo:computer:work:swap#available" }
Number Swap_Used                     { channel="systeminfo:computer:work:swap#used" }
Number Swap_Total                    { channel="systeminfo:computer:work:swap#total" }
Number Swap_Available_Percent        { channel="systeminfo:computer:work:swap#availablePercent" }
Number Swap_Used_Percent             { channel="systeminfo:computer:work:swap#usedPercent" }

/* Battery information*/
String Battery_Name                  { channel="systeminfo:computer:work:battery#name" }
Number Battery_RemainingCapacity     { channel="systeminfo:computer:work:battery#remainingCapacity" }
Number Battery_RemainingTime         { channel="systeminfo:computer:work:battery#remainingTime" }

/* Display information*/
String Display_Description           { channel="systeminfo:computer:work:display#information" }

/* Sensors information*/
Number Sensor_CPUTemp                { channel="systeminfo:computer:work:sensors#cpuTemp" }
Number Sensor_CPUVoltage             { channel="systeminfo:computer:work:sensors#cpuVoltage" }
Number Sensor_FanSpeed               { channel="systeminfo:computer:work:sensors#fanSpeed" }

/* Process information*/
Number Process_load                  { channel="systeminfo:computer:work:process#load" }
Number Process_used                  { channel="systeminfo:computer:work:process#used" }
String Process_name                  { channel="systeminfo:computer:work:process#name" }
Number Process_threads               { channel="systeminfo:computer:work:process#threads" }
String Process_path                  { channel="systeminfo:computer:work:process#path" }
```
