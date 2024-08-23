# Systeminfo Binding

The system information binding provides operating system and hardware information including:

- Operating system name, version and manufacturer;
- CPU average load for last 1, 5, 15 minutes, name, description, number of physical and logical cores, running threads number, system uptime, max frequency and frequency by logical core;
- Free, total and available memory;
- Free, total and available swap memory;
- Hard drive name, model and serial number;
- Free, total, available storage space and storage type (NTSFS, FAT32 ..);
- Battery information - estimated remaining time, capacity, name;
- Sensors information - CPU voltage and temperature, fan speeds;
- Display information;
- Network IP, name and adapter name, mac, data sent and received, packets sent and received;
- Process information - size of RAM memory used, CPU load, process name, path, number of threads.

The binding uses the [OSHI](https://github.com/oshi/oshi) library to access this information regardless of the underlying OS and hardware.

## Supported Things

The binding supports only one thing type - **computer**. This thing represents a system with one storage volume, one display device and one network adapter.

The thing has the following properties:

- `cpu_logicalCores` - Number of CPU logical cores
- `cpu_physicalCores` - Number of CPU physical cores
- `os_manufacturer` - The manufacturer of the operating system
- `os_version` - The version of the operating system
- `os_family` - The family of the operating system

If multiple storage or display devices support is needed, a new thing type has to be defined.

## Discovery

The discovery service implementation tries to resolve the computer name.
If the resolving process fails, the computer name is set to "Unknown".
In both cases it creates a Discovery Result with thing type  **computer**.

## Thing configuration

The configuration of the Thing gives the user the possibility to update channels at different intervals.

The thing has two configuration parameters:

- **interval_high** - refresh interval in seconds for channels with 'High' priority configuration. Default value is 1 s.
- **interval_medium** - refresh interval in seconds for channels with 'Medium' priority configuration. Default value is 60s.

That means that by default configuration:

- channels with priority set to 'High' are updated every second
- channels with priority set to 'Medium' are updated every minute
- channels with priority set to 'Low' are updated only at initialization or if the `REFRESH` command is sent to the channel.

Channels, not linked to an item, do not get updates, and do not periodically consume resources.

For more info see [channel configuration](#channel-configuration)

## Channels

The binding support several channel group.
Each channel group, contains one or more channels.
In the list below, you can find, how are channel group and channels id`s related.

**thing** `computer`

- **group** `memory`
  - **channel** `available, total, used, availablePercent, usedPercent, usedHeapPercent, availableHeap`
- **group** `swap`
  - **channel** `available, total, used, availablePercent, usedPercent`
- **group** `storage` (deviceIndex)
  - **channel** `available, total, used, availablePercent, usedPercent, name, description, type`
- **group** `drive` (deviceIndex)
  - **channel** `name, model, serial`
- **group** `display` (deviceIndex)
  - **channel** `information`
- **group** `battery` (deviceIndex)
  - **channel** `name, remainingCapacity, remainingTime`
- **group** `cpu`
  - **channel** `name, description, maxfreq, freq `(deviceIndex)`, load, load1, load5, load15, uptime, threads`
- **group** `sensors`
  - **channel** `cpuTemp, cpuVoltage, fanSpeed `(deviceIndex)
- **group** `network` (deviceIndex)
  - **channel** `ip, mac, networkDisplayName, networkName, packetsSent, packetsReceived, dataSent, dataReceived`
- **group** `currentProcess`
  - **channel** `load, used, name, threads, path`
- **group** `process` (pid)
  - **channel** `load, used, name, threads, path`

The groups marked with "(deviceIndex)" may have device index attached to the Channel Group.

- channel ::= channel_group & (deviceIndex) & # channel_id
- deviceIndex ::= number >= 0
- (e.g. _storage1#available_)

The channels marked with "(deviceIndex)" may have a device index attached to the Channel.

- channel ::= channel_group & # channel_id & (deviceIndex)
- deviceIndex ::= number >= 0

Channels or channel groups without a trailing index will show the data for the first device (index 0) if multiple exist.
If only one device for a group exists, no channels or channel groups with indexes will be created.

The group `process` is using a configuration parameter "pid" instead of "deviceIndex".
This makes it possible to change the tracked process at runtime.

The group `currentProcess` has the same channels as the `process` group without the "pid" configuration parameter.
The PID is dynamically set to the PID of the process running openHAB.

The binding uses this index to get information about a specific device from a list of devices (e.g on a single computer several local disks could be installed with names C:\, D:\, E:\ - the first will have deviceIndex=0, the second deviceIndex=1 etc).
If device with this index is not existing, the binding will display an error message on the console.

The table shows more detailed information about each Channel type.
The binding introduces the following channels:

| Channel ID         | Channel Description                                              | Supported item type | Default priority | Advanced |
|--------------------|------------------------------------------------------------------|---------------------|------------------|----------|
| load               | CPU Load (total or by process) in %                              | Number:Dimensionless| High             | False    |
| load1              | Load for the last 1 minute                                       | Number              | Medium           | True     |
| load5              | Load for the last 5 minutes                                      | Number              | Medium           | True     |
| load15             | Load for the last 15 minutes                                     | Number              | Medium           | True     |
| threads            | Number of threads currently running or for the process           | Number              | Medium           | True     |
| maxfreq            | CPU maximum frequency                                            | Number:Frequency    | Low              | True     |
| freq               | Logical processor frequency                                      | Number:Frequency    | High             | True     |
| path               | The full path of the process                                     | String              | Low              | False    |
| uptime             | System uptime (time after start) in minutes                      | Number:Time         | Medium           | True     |
| name               | Name of the device or process                                    | String              | Low              | False    |
| available          | Available size                                                   | Number:DataAmount   | High             | False    |
| used               | Used size                                                        | Number:DataAmount   | High             | False    |
| total              | Total size                                                       | Number:DataAmount   | Low              | False    |
| availablePercent   | Available size in %                                              | Number:Dimensionless| High             | False    |
| usedPercent        | Used size in %                                                   | Number:Dimensionless| High             | False    |
| model              | The model of the device                                          | String              | Low              | True     |
| serial             | The serial number of the device                                  | String              | Low              | True     |
| description        | Description of the device                                        | String              | Low              | True     |
| type               | Storage type                                                     | String              | Low              | True     |
| cpuTemp            | CPU Temperature in degrees Celsius                               | Number:Temperature  | High             | True     |
| cpuVoltage         | CPU Voltage                                                      | Number:ElectricPotential| Medium       | True     |
| fanSpeed           | Fan speed in rpm                                                 | Number              | Medium           | True     |
| remainingTime      | Remaining time in minutes                                        | Number:Time         | Medium           | False    |
| remainingCapacity  | Remaining capacity in %                                          | Number:Dimensionless| Medium           | False    |
| information        | Product, manufacturer, SN, width and height of the display in cm | String              | Low              | True     |
| ip                 | Host IP address of the network                                   | String              | Low              | False    |
| mac                | MAC address                                                      | String              | Low              | True     |
| networkName        | The name of the network                                          | String              | Low              | False    |
| networkDisplayName | The display name of the network                                  | String              | Low              | False    |
| packetsSent        | Number of packets sent                                           | Number              | Medium           | True     |
| packetsReceived    | Number of packets received                                       | Number              | Medium           | True     |
| dataSent           | Volume of data sent                                              | Number:DataAmount   | Medium           | True     |
| dataReceived       | Volume of data received                                          | Number:DataAmount   | Medium           | True     |
| availableHeap      | How much space is available in the currently committed heap      | Number:DataAmount   | Medium           | True     |
| usedHeapPercent    | How much of the MAX heap size is actually used in %              | Number:Dimensionless| Medium           | False    |

## Channel configuration

All channels can change its configuration parameters at runtime.
The binding will trigger the necessary changes (reduce or increase the refresh time, change channel priority or the process that is being tracked).

Each of the channels has a default configuration parameter - priority.
It has the following options:

- **High**
- **Medium**
- **Low**

The ''load'' channel will update total or by process CPU load at the frequency defined by the priority update interval, by default high priority, every second.
The value corresponds to the average CPU load over the interval.

Channels from group ''process'' have additional configuration parameter - PID (Process identifier).
This parameter is used as 'deviceIndex' and defines which process is tracked from the channel.
This makes the channels from this groups very flexible - they can change its PID dynamically.

Parameter PID has a default value 0 - this is the PID of the System Idle process in Windows OS.

## Known issues and workarounds

- Temperature readings are not well supported on standard Windows systems, run [OpenHardwareMonitor.exe](https://openhardwaremonitor.org) for the binding to get more reliable readings.
- CPU frequency readings are not available on some OS versions.

## Reporting issues

As already mentioned this binding depends heavily on the [OSHI](https://github.com/oshi/oshi) API to provide the operating system and hardware information.

Take a look at the console for an ERROR log message.

If you find an issue with support for a specific hardware or software architecture please take a look at the [OSHI issues](https://github.com/oshi/oshi/issues).
Your problem might have be already reported and solved!
Feel free to open a new issue there with the log message and the information about your software or hardware configuration.

For a general problem with the binding report the issue directly to openHAB.

## Example

Things:

```java
Thing systeminfo:computer:work [interval_high=3, interval_medium=60]
```

Items:

```java
/* Network information*/
String Network_AdapterName         "Adapter name"        <network>       { channel="systeminfo:computer:work:network#networkDisplayName" }
String Network_Name                "Name"                <network>       { channel="systeminfo:computer:work:network#networkName" }
String Network_IP                  "IP address"          <network>       { channel="systeminfo:computer:work:network#ip" }
String Network_Mac                 "Mac address"         <network>       { channel="systeminfo:computer:work:network#mac" }
Number:DataAmount Network_DataSent "Data sent"           <flowpipe>      { channel="systeminfo:computer:work:network#dataSent" }
Number:DataAmount Network_DataReceived "Data received"   <returnpipe>    { channel="systeminfo:computer:work:network#dataReceived" }
Number Network_PacketsSent         "Packets sent"        <flowpipe>      { channel="systeminfo:computer:work:network#packetsSent" }
Number Network_PacketsReceived     "Packets received"    <returnpipe>    { channel="systeminfo:computer:work:network#packetsReceived" }

/* CPU information*/
String CPU_Name                    "Name"                <none>          { channel="systeminfo:computer:work:cpu#name" }
String CPU_Description             "Description"         <none>          { channel="systeminfo:computer:work:cpu#description" }
Number:Frequency CPU_MaxFreq       "CPU Max Frequency"   <none>          { channel="systeminfo:computer:work:cpu#maxfreq" }
Number:Frequency CPU_Freq          "CPU Frequency"       <none>          { channel="systeminfo:computer:work:cpu#freq" }
Number:Dimensionless CPU_Load      "CPU Load"            <none>          { channel="systeminfo:computer:work:cpu#load" }
Number CPU_Load1                   "Load (1 min)"        <none>          { channel="systeminfo:computer:work:cpu#load1" }
Number CPU_Load5                   "Load (5 min)"        <none>          { channel="systeminfo:computer:work:cpu#load5" }
Number CPU_Load15                  "Load (15 min)"       <none>          { channel="systeminfo:computer:work:cpu#load15" }
Number CPU_Threads                 "Threads"             <none>          { channel="systeminfo:computer:work:cpu#threads" }
Number:Time CPU_Uptime             "Uptime"              <time>          { channel="systeminfo:computer:work:cpu#uptime" }

/* Drive information*/
String Drive_Name                  "Name"                <none>          { channel="systeminfo:computer:work:drive#name" }
String Drive_Model                 "Model"               <none>          { channel="systeminfo:computer:work:drive#model" }
String Drive_Serial                "Serial"              <none>          { channel="systeminfo:computer:work:drive#serial" }

/* Storage information*/
String Storage_Name                "Name"                <none>          { channel="systeminfo:computer:work:storage#name" }
String Storage_Type                "Type"                <none>          { channel="systeminfo:computer:work:storage#type" }
String Storage_Description         "Description"         <none>          { channel="systeminfo:computer:work:storage#description" }
Number:DataAmount Storage_Available "Available"          <none>          { channel="systeminfo:computer:work:storage#available" }
Number:DataAmount Storage_Used     "Used"                <none>          { channel="systeminfo:computer:work:storage#used" }
Number:DataAmount Storage_Total    "Total"               <none>          { channel="systeminfo:computer:work:storage#total" }
Number:Dimensionless Storage_Available_Percent "Available (%)" <none>    { channel="systeminfo:computer:work:storage#availablePercent" }
Number:Dimensionless Storage_Used_Percent "Used (%)"     <none>          { channel="systeminfo:computer:work:storage#usedPercent" }

/* Memory information*/
Number:DataAmount Memory_Available "Available"           <none>          { channel="systeminfo:computer:work:memory#available" }
Number:DataAmount Memory_Used      "Used"                <none>          { channel="systeminfo:computer:work:memory#used" }
Number:DataAmount Memory_Total     "Total"               <none>          { channel="systeminfo:computer:work:memory#total" }
Number:Dimensionless Memory_Available_Percent "Available (%)" <none>     { channel="systeminfo:computer:work:memory#availablePercent" }
Number:Dimensionless Memory_Used_Percent "Used (%)"      <none>          { channel="systeminfo:computer:work:memory#usedPercent" }

/* Swap memory information*/
Number:DataAmount Swap_Available   "Available"           <none>          { channel="systeminfo:computer:work:swap#available" }
Number:DataAmount Swap_Used        "Used"                <none>          { channel="systeminfo:computer:work:swap#used" }
Number:DataAmount Swap_Total       "Total"               <none>          { channel="systeminfo:computer:work:swap#total" }
Number:Dimensionless Swap_Available_Percent "Available (%)" <none>       { channel="systeminfo:computer:work:swap#availablePercent" }
Number:Dimensionless Swap_Used_Percent "Used (%)"        <none>          { channel="systeminfo:computer:work:swap#usedPercent" }

/* Battery information*/
String Battery_Name                "Name"                <batterylevel>  { channel="systeminfo:computer:work:battery#name" }
Number:Dimensionless Battery_RemainingCapacity "Remaining Capacity" <batterylevel> { channel="systeminfo:computer:work:battery#remainingCapacity" }
Number:Time Battery_RemainingTime  "Remaining Time"      <batterylevel>  { channel="systeminfo:computer:work:battery#remainingTime" }

/* Display information*/
String Display_Description         "Display description" <screen>        { channel="systeminfo:computer:work:display#information" }

/* Sensors information*/
Number:Temperature Sensor_CPUTemp  "CPU Temperature"     <temperature>   { channel="systeminfo:computer:work:sensors#cpuTemp" }
Number:ElectricPotential Sensor_CPUVoltage "CPU Voltage" <energy>        { channel="systeminfo:computer:work:sensors#cpuVoltage" }
Number Sensor_FanSpeed             "Fan speed"           <fan>           { channel="systeminfo:computer:work:sensors#fanSpeed" }

/* Current process information*/
Number:Dimensionless Current_process_load "Load"         <none>          { channel="systeminfo:computer:work:currentProcess#load" }
Number:DataAmount Current_process_used "Used"            <none>          { channel="systeminfo:computer:work:currentProcess#used" }
String Current_process_name        "Name"                <none>          { channel="systeminfo:computer:work:currentProcess#name" }
Number Current_process_threads     "Threads"             <none>          { channel="systeminfo:computer:work:currentProcess#threads" }
String Current_process_path        "Path"                <none>          { channel="systeminfo:computer:work:currentProcess#path" }

/* Process information*/
Number:Dimensionless Process_load  "Load"                <none>          { channel="systeminfo:computer:work:process#load" }
Number:DataAmount Process_used     "Used"                <none>          { channel="systeminfo:computer:work:process#used" }
String Process_name                "Name"                <none>          { channel="systeminfo:computer:work:process#name" }
Number Process_threads             "Threads"             <none>          { channel="systeminfo:computer:work:process#threads" }
String Process_path                "Path"                <none>          { channel="systeminfo:computer:work:process#path" }
```

Sitemap:

```perl
sitemap systeminfo label="Systeminfo" {
    Frame label="Network Information" {
        Default item=Network_AdapterName
        Default item=Network_Name
        Default item=Network_IP
        Default item=Network_Mac
        Default item=Network_DataSent
        Default item=Network_DataReceived
        Default item=Network_PacketsSent
        Default item=Network_PacketsReceived
    }
    Frame label="CPU Information" {
        Default item=CPU_Name
        Default item=CPU_Description
        Default item=CPU_MaxFreq
        Default item=CPU_Freq
        Default item=CPU_Load1
        Default item=CPU_Load5
        Default item=CPU_Load15
        Default item=CPU_Threads
        Default item=CPU_Uptime
    }
    Frame label="Drive Information" {
        Default item=Drive_Name
        Default item=Drive_Model
        Default item=Drive_Serial
    }
    Frame label="Storage Information" {
        Default item=Storage_Name
        Default item=Storage_Type
        Default item=Storage_Description
        Default item=Storage_Available
        Default item=Storage_Used
        Default item=Storage_Total
        Default item=Storage_Available_Percent
        Default item=Storage_Used_Percent
    }
    Frame label="Memory Information" {
        Default item=Memory_Available
        Default item=Memory_Used
        Default item=Memory_Total
        Default item=Memory_Available_Percent
        Default item=Memory_Used_Percent
    }
    Frame label="Swap Memory Information" {
        Default item=Swap_Available
        Default item=Swap_Used
        Default item=Swap_Total
        Default item=Swap_Available_Percent
        Default item=Swap_Used_Percent
    }
    Frame label="Battery, Display and Sensor Information" {
        Default item=Battery_Name
        Default item=Battery_RemainingCapacity
        Default item=Battery_RemainingTime
        Default item=Display_Description
        Default item=Sensor_CPUTemp
        Default item=Sensor_CPUVoltage
        Default item=Sensor_FanSpeed
    }
    Frame label="Current Process Information" {
        Default item=Current_process_load
        Default item=Current_process_used
        Default item=Current_process_name
        Default item=Current_process_threads
        Default item=Current_process_path
    }
    Frame label="Process Information" {
        Default item=Process_load
        Default item=Process_used
        Default item=Process_name
        Default item=Process_threads
        Default item=Process_path
    }
}
```
