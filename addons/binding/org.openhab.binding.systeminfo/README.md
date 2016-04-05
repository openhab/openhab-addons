## Supported Things

* **thing** `computer`
    * **group** `hardware`
        * **group** `display` 
            * **channel** `edid`
        * **group** `filestore` 
            * **channel** `description,name,totalSpace,usableSpace,`
        * **group** `memory`
            * **channel** `available, total`
        * **group** `battery`
            * **channel** `name, remainingCapacity,remainingTime`
        * **group** `cpu`
            * **channel** `family, model, name , identifier, vendor, is64bit, logProcCount,physProcCount,load,avg1min,avg5min,avg15min`
        * **group** `sensors`
            * **channel** `cpuTemp, cpuVoltage, fanSpeed`
    * **group** `os`
        * **channel** `family, manufacturer, version ` (String)
    * **group** `network`
        * **channel** `IP, adapter name, adapter display name` (String)

    - formating 
    - REFRESH Tasks
    - JSON

processes ?
swap ?
Disc read/writes ?


------------HardwareAbstractionLayer-------  
DISPLAY - *
FILE STORES - *
MEMORY - 1
POWER SPURCE/battery/ - 1
CPU - * /1
CPU temp - *
-----------OperatingSystem-----------      
Family, manufacturer, version - 1


-------------JDK-----------
IP - 1
display name - 1 
name - 1

thing computer
channel group hardware
    channel