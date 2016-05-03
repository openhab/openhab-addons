# Systeminfo Binding

System information Binding provides operating system and hardware information including:

 - Operating system name, version and manufacturer
 - CPU average load, name, description, number of physical and logical cores
 - Free, total and available memory
 - Free, total and available swap memory
 - Hard drive name, model and serial number
 - Free, total, available storage space and storage type (NTSFS, FAT32 ..)
 - Battery information - estimated remaining time, capacity, name
 - Sensors information - CPU voltage and temperature, fan speeds 
 - Display information
 - Network IP,name and adapter name
 
 The binding uses [oshi](https://github.com/dblock/oshi) API to access this information regardless of the underlying platform and does not need any native parts.
 
## Supported Things

The binding supports only one thing type - **computer**. This thing represents a system with one storage volume, one display device and one network adapter.

If multiple storage or display devices support is needed, new thing type has to be defined. This is workaround until [this issue] (https://github.com/eclipse/smarthome/issues/588) is resolved and it is possible to add dynamically channels to DSL defined thing.

## Discovery

The discovery service implementation tries to resolve the computer name. If the resolving process fails, the computer name is set to "Unknown". In both cases it creates a Discovery Result with thing type  **computer**.

## Binding configuration

No binding configuration required.

## Thing configuration
The configuration of the Thing gives the user the possibility to update channels at different intervals.

The thing has two configuration parameters:
   * **interval_high** - refresh interval in seconds for channels with 'High' priority configuration. Default value is 1 s.
   * **interval_medium** - refresh interval in seconds for channels with 'Medium' priority configuration. Default value is 60s.

That means that by default configuration, channels with priority set to 'High' are updated every second, channels with priority set to 'Medium' - every minute, channels with priority set to 'Low' only at initializing or at Refresh command.


## Channels

The binding support several channel group. Each channel group, contains one or more channels. In the list below, you can find, how are channel group and channels related.

**thing** `computer`
    * **group** `os`
        * **channel** `family, manufacturer, version `
    * **group** `memory`
        * **channel** `available, total, used, available_percent`
    * **group** `swap`
        * **channel** `available, total, used, available_percent`
    * **group** `storage`
        * **channel** `available, total, used, available_percent, name, description, type`
    * **group** `drive` 
        * **channel** `name, model, serial`
    * **group** `display` 
        * **channel** `information`
    * **group** `battery`
        * **channel** `name, remainingCapacity,remainingTime`
    * **group** `cpu`
        * **channel** `name, description, load, logicalCores, physicalCores`
    * **group** `sensors`
        * **channel** `cpuTemp, cpuVoltage, fanSpeed`
    * **group** `network`
        * **channel** `ip, networkDisplayName, networkName` (String)

In the table is shown more detailed information about each Channel type.
The binding introduces the following channel types:

| Chnanel Type | Channel Description | Supported item type | Default priority | 
| ------------- | ------------- |------------|----------|
| manufacturer  | The manufacturer of the operating system  | String | Low |
| version  | The version of the operating system  | String | Low | 
| family  | The family of the operating system | String | Low |
| load  | Load in percents  | Number | High | 
| name | Name of the device  | String | Low | 
| logicalCores  | Number of CPU logical cores  | Number | Low |
| phisycalCores  | Number of CPU physical cores<  | Number | Low |
| available  | Available size in MB  | Number | High |
| used  | Used size in MB  | Number | High |
| total  | Total size in MB  | Number | Low |
| availablePercent  | Available size in %  | Number | High |
| model  | The model of the device  | String | Low |
| serial  | The serial number of the device  | String | Low |
| description  | Description of the device  | String | Low |
| type  | Storage type  | String | Low |
| cpuTemp  | CPU Temperature in Celsius degrees  | Number | High |
| cpuVoltage  | CPU Voltage in V  | Number | Medium |
| fanSpeed  | Fan speed in rpm  | String | Low |
| remainingTime  | Remaining time in minutes | Number | Medium |
| remainingCapacity  | Remaining capacity in percents  | Number | Medium |
| information  | Product, manufacturer, SN, width and height of the display in cm  | String | Low |
| ip  | Host IP address of the network  | String | Low |
| networkName  | The name of the network.  | String | Low |
| networkDisplayName  | The display name of the network  | String | Low |

Some of the channels may have device index attached to the Channel Type.
 - channel_ID  ::= Chnanel_Type & (deviceIndex) 
 - deviceIndex ::= number
 - (e.g. *storage_available1*)
 
 The binding uses this index to get information about a specific device from a list of devices.
 (e.g on a single computer could be installed several local disks with names C:\, D:\, E:\ - the first will have deviceIndex=0, the second deviceIndex=1 ant etc).

##Channel configuration

Each of the channels has a default configuration parameter - priority. It has the following options:
 - **High**
 - **Medium**
 - **Low**
 
## Full example

Things:
```
systeminfo:computer:work [interval_high=3, interval_medium=60] 
```
Items:
```
/* Operating system */
String OS_Family                    { channel="systeminfo:computer:work:os#family" }
String OS_Manufacturer              { channel="systeminfo:computer:work:os#manufacturer" }
String OS_Version                   { channel="systeminfo:computer:work:os#version" }

/* Network information*/
String Network_AdapterName          { channel="systeminfo:computer:work:network#networkDisplayName" }
String Network_Name                 { channel="systeminfo:computer:work:network#networkName" }
String Network_IP                   { channel="systeminfo:computer:work:network#ip" }

/* CPU information*/
String CPU_Name                     { channel="systeminfo:computer:work:cpu#name" }
String CPU_Description              { channel="systeminfo:computer:work:cpu#description" }
Number CPU_Load                     { channel="systeminfo:computer:work:cpu#load"} 
Number CPU_LogicalProcCount         { channel="systeminfo:computer:work:cpu#logicalCores" }
Number CPU_PhysicalProcCount        { channel="systeminfo:computer:work:cpu#phisycalCores" }

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

/* Memory information*/
Number Memory_Available              { channel="systeminfo:computer:work:memory#available" }
Number Memory_Used                   { channel="systeminfo:computer:work:memory#used" }
Number Memory_Total                  { channel="systeminfo:computer:work:memory#total" }
Number Memory_Available_Percent      { channel="systeminfo:computer:work:memory#availablePercent" }

/* Swap memory information*/
Number Swap_Available                { channel="systeminfo:computer:work:swap#available" }
Number Swap_Used                     { channel="systeminfo:computer:work:swap#used" }
Number Swap_Total                    { channel="systeminfo:computer:work:swap#total" }
Number Swap_Available_Percent        { channel="systeminfo:computer:work:swap#availablePercent" }

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
```
