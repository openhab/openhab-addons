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

Discovery is not necessary.

## Binding configuration

No binding configuration required.

## Thing configuration
The configuration of the Thing gives the user the possibility to update channels at different intervals.

The thing has two configuration parameters:
   * **interval_high** - refresh interval in seconds for channels with 'High' priority configuration. Default value is 1 s.
   * **interval_medium** - refresh interval in seconds for channels with 'Medium' priority configuration. Default value is 60s.

That means that by default configuration, channels with priority set to 'High' are updated every second, channels with priority set to 'Medium' - every minute, channels with priority set to 'Low' only at initializing or at Refresh command.


## Channels
The binding introduces the following channel types:

| Chnanel Type | Channel Description | Supported item type | Default priority | Device index |
| ------------- | ------------- |------------|----------| -------- |
| os_manufacturer  | The manufacturer of the operating system  | String | Low | No |
| os_version  | The version of the operating system  | String | Low | No |
| os_family  | The family of the operating system | String | Low | No |
| cpu_load  | CPU load in percents  | Number | High | No |
| cpu_name  | CPU name  | String | Low | No |
| cpu_description  | Processor model, family, SN, identifier, vendor, architecture  | String | Low | No |
| cpu_logical_cores  | Number of CPU logical cores  | Number | Low | No |
| cpu_phisycal_cores  | Number of CPU physical cores<  | Number | Low | No |
| memory_available  | Available memory size in MB  | Number | High | No |
| memory_used  | Used memory size in MB  | Number | High | No |
| memory_total  | Total memory size in MB  | Number | Low | No |
| memory_available_percent  | Available memory size in %  | Number | High | No |
| swap_available  | Available swap memory size in MB  | Number | High | No |
| swap_used  | Used swap memory size in MB  | Number | High | No |
| swap_total  | Total swap memory size in MB  | Number | Low | No |
| swap_available_percent  | Available swap memory size in %  | Number | High | No |
| drive_name  | Drive name  | String | Low | Yes |
| drive_model  | Drive description  | String | Low | Yes |
| drive_serial  | Drive serial number  | String | Low | Yes |
| storage_name  | Storage name  | String | Low | Yes |
| storage_description  | Storage description  | String | Low | Yes |
| storage_type  | Storage type  | String | Low | Yes |
| storage_used  | Used storage size in MB  | Number | Medium | Yes |
| storage_available  | Available storage size in MB  | Number | Medium | Yes |
| storage_available_percent  | Available storage size in percents  | Number | Medium | Yes |
| storage_total  | Total storage size in MB  | Number | Low | Yes |
| cpu_temperature  | Temperature of the CPU in Celsius degrees  | Number | High | No |
| cpu_voltage  | Voltage of the CPU in V  | Number | Medium | No |
| fan_speed  | Speed of the CPU fan in rpm  | String | Low | Yes |
| battery_name  | Battery name  | String | Low | Yes |
| battery_time  | Remaining time of the battery in minutes | Number | Medium | Yes |
| battery_capacity  | Percentage of capacity left  | Number | Medium | Yes |
| display_information  | Product, manufacturer, SN, width and height of the display  | String | Low | Yes |
| network_ip  | Host IP address of the network  | String | Low | Yes |
| network_name  | Network name  | String | Low | Yes |
| network_adapter_name  | Network adapter name  | String | Low | Yes |

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
String OS_Family                    { channel="systeminfo:computer:work:os_family" }
String OS_Manufacturer              { channel="systeminfo:computer:work:os_manufacturer" }
String OS_Version                   { channel="systeminfo:computer:work:os_version" }

/* Network information*/
String Network_AdapterName          { channel="systeminfo:computer:work:network_adapter_name" }
String Network_Name                 { channel="systeminfo:computer:work:network_name" }
String Network_IP                   { channel="systeminfo:computer:work:network_ip" }

/* CPU information*/
String CPU_Name                     { channel="systeminfo:computer:work:cpu_name" }
String CPU_Description              { channel="systeminfo:computer:work:cpu_description" }
Number CPU_Load                     { channel="systeminfo:computer:work:cpu_load"} 
Number CPU_LogicalProcCount         { channel="systeminfo:computer:work:cpu_logical_cores" }
Number CPU_PhysicalProcCount        { channel="systeminfo:computer:work:cpu_phisycal_cores" }

/* Drive information*/
String Drive_Name                    { channel="systeminfo:computer:work:drive_name" }
String Drive_Model                   { channel="systeminfo:computer:work:drive_model" }
String Drive_Serial                  { channel="systeminfo:computer:work:drive_serial" }

/* Storage information*/
String Storage_Name                  { channel="systeminfo:computer:work:storage_name" }
String Storage_Type                  { channel="systeminfo:computer:work:storage_type" }
String Storage_Description           { channel="systeminfo:computer:work:storage_description" }
Number Storage_Available             { channel="systeminfo:computer:work:storage_available" }
Number Storage_Used                  { channel="systeminfo:computer:work:storage_used" }
Number Storage_Total                 { channel="systeminfo:computer:work:storage_total" }
Number Storage_Available_Percent     { channel="systeminfo:computer:work:storage_available_percent" }

/* Memory information*/
Number Memory_Available              { channel="systeminfo:computer:work:memory_available" }
Number Memory_Used                   { channel="systeminfo:computer:work:memory_used" }
Number Memory_Total                  { channel="systeminfo:computer:work:memory_total" }
Number Memory_Available_Percent      { channel="systeminfo:computer:work:memory_available_percent" }

/* Swap memory information*/
Number Swap_Available                { channel="systeminfo:computer:work:swap_available" }
Number Swap_Used                     { channel="systeminfo:computer:work:swap_used" }
Number Swap_Total                    { channel="systeminfo:computer:work:swap_total" }
Number Swap_Available_Percent        { channel="systeminfo:computer:work:swap_available_percent" }

/* Battery information*/
String Battery_Name                  { channel="systeminfo:computer:work:battery_name" }
Number Battery_RemainingCapacity     { channel="systeminfo:computer:work:battery_remaining_capacity" }
Number Battery_RemainingTime         { channel="systeminfo:computer:work:battery_remaining_time" }

/* Display information*/
String Display_Description           { channel="systeminfo:computer:work:display_information" }

/* Sensors information*/
Number Sensor_CPUTemp                { channel="systeminfo:computer:work:sensors_cpu_temperature" }
Number Sensor_CPUVoltage             { channel="systeminfo:computer:work:sensors_cpu_voltage" }
Number Sensor_FanSpeed               { channel="systeminfo:computer:work:sensors_fan_speed" }
```
