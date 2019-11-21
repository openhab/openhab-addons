# openHAB SEMP Service

This service implements the SEMP protocol for openHAB

## Features:

*   UPNP automatic discovery
*   Full SEMP Support
*   Support ON/OFF and Decimal item types

## Installation:

After the activation of this plugin and the configuration of the consumer(s) the upnp service will start immediately. After some minutes you can start a device search in the SMA Sunny Portal. It will detect the device(s) and you will be able to add it to your device list.    

## Configuration:

An UUID (Universal Unique Identifier) is a 128-bit number used to uniquely identify some object or entity on the Internet. Usually it's a mac and time based V1 UUID but it is possible to use a random generated UUID, too. There are some generators in the internet available. 
Important: if you change it then you have to recreate the consumers in SMA Sunny Portal.

```
org.openhab.semp:discoveryUUID=8e75f1cc-848e-40ca-b7ec-87369019bd10
```

(Optional) For systems with multiple IP addresses the IP to use for UPNP may be specified, otherwise the first non loopback address will be used.

```
org.openhab.semp:discoveryIp=192.168.1.100
```

(Optional) Some SEMP applications require a different port (80) then what openHAB runs on by default (8080).  This option will only advertise a different port then what we are listening on.  Useful if you have an iptables rule redirect traffic from this port to the openHAB port. </description>


```
org.openhab.semp:discoveryHttpPort=8080
```

## Persistence (Optional)

Usually this plugin will send the devices status every minute. But there is the possibility to send values up to the last 10 minutes, too. If you want to use this feature then you have to configure persistence for this item. If your persistence is supporting a step smaller then one minute then this plugin will determine additional values like min, max or average from the last 60 seconds.  


## Device Tagging

The SEMP service supports multiple consumers. Every SEMP consumer needs up to four items mapped inside one group. These items are a switch item (on/off) for device controlling, a number item (power), an optional contact item for the “Connected” state and an optional contact item for the “Listening” state.
The identification of a SEMP consumer is a group-tag [“Consumer”]. The identification for the consumers items is a tag with the name of the consumers group. These “contact” items has to be tagged for identification, too. Use the [“Connected”] tag for the indication whether the consumer is connected (like a car on an EVCharger) and the [“Listening”] Tag for the indication whether the consumer is listening to the SEMP commands. (e.g. you should set it to open if you are charging a car manually without SEMP).

## Consumer Configuration

The consumer need some configuration parameters. This parameters are tags for the group item, too.

All tags are formatted like this:

```
semp:<tagtype>:<value>
```

For example:

```
semp:max_power:1400
```

If you've defined your Items in _.items_ files, tags can be added using:

```
[ "mytag" ]
```

syntax (after the _(Groups)_ and before the _{channel}_).
If you created your items another way, e.g. using the Paper UI, [HABmin](https://github.com/openhab/org.openhab.ui.habmin) allows you to modify the tags.

### Tag: _device___id_

The device ID is represented by a 92 bit integer which consists of the following components: 
Device ID (92 bits):

```
Vendor ID type (4 bits)  + Vendor ID (32 bits)  + Serial-number (48 bits) + Sub-device ID (8 bits) 
```

_Required_: no<br>
_Default_: auto-generated from consumers name<br>
Example: 

```
semp:device_id:F-11223344-112233445566-00
```

### Tag: _device___vendor_

Name of the device’s vendor. 

_Required_: no<br>
_Default_: Unknown<br>
Example: 

```
semp:device_vendor:Unknown
```

### Tag: _device___type_

Type of device. One of the values predefined in the SEMP XSD should be used:

```
AirConditioning
Charger
DishWasher
Dryer
ElectricVehicle
EVCharger
Fridge
Heater
HeatPump
Motor
Pump
WashingMachine
Other
```

_Required_: no<br>
_Default_: Other<br>
Example: 

```
semp:device_type:WashingMachine
```

### Tag: _device___serial_

A serial number that is known by the user so that he is able 
to identify the device. If available use the vendor specific serial number which is printed on 
the device. 

_Required_: yes<br>

Example: 

```
semp:device_serial:abc123456789
```

### Tag: _max___power_

 Maximum power consumption of the device (in W). 

_Required_: yes<br>

Example: 

```
semp:max_power:1400
```

### Tag: _min____off____time_

When switched off (or paused), the device has to remain off for at least the given amount 
of seconds.  

_Required_: no<br>
_Default_: 0<br>
Example: 

```
semp:min_off_time:15
```

### Tag: _min____on____time_

When switched on (or un-paused), the device has to remain on for at least the given 
amount of seconds.   

_Required_: no<br>
_Default_: 0<br>
Example: 

```
semp:min_on_time:15
```

### Tag: _inter___alowed_

Specifies whether the device can be interrupted (paused) during runtime. This 
allows a more flexible energy management for the device. For instance the EM 
can interrupt a device in case of unpredictable bad weather conditions or when 
the user switches on a device with conflicting energy needs and restart the inter-
rupted device afterwards. 
 
Should be set to “true”. Set to “false” only if the device operation cannot be 
paused, e.g. for some program based devices.    

_Required_: no<br>
_Default_: true<br>
Example: 

```
semp:inter_alowed:true
```

### Tag: _earliest___start_

Specifies the earliest start time [in minutes from midnight] of the device.    

_Required_: yes<br>

Example: 

```
semp:earliest_start:600
600->every day at 10am
```

If you want multiple time frames then you need to separate them with":"
 
```
semp:earliest_start:600:1020
```


### Tag: _latest___end_

Specifies the end of the planning range. This is the latest possible time the device opera-
tion has to be finished. [in minutes from midnight]    

_Required_: yes<br>

Example: 

```
semp:latest_end:660
660->every day at 11am
```

If you want multiple time frames then you need to separate them with":"
 
```
semp:latest_end:660:1080
```

### Tag: _min____running____time_

The minimum amount of time the device needs to run in the time range specified by Earli-
estStart and LatestEnd. It will be assigned to device by the EM even if there is no PV-
production so that grid-energy has to be used to power the device. Note: If set to “0” 
all of the energy is optional and should only be allocated to the device if certain 
conditions are fulfilled (e.g. cheap PV-energy available). [in minutes]

_Required_: yes<br>

Example: 

```
semp:min_running_time:30
```

If you want multiple time frames then you need to separate them with":"
 
```
semp:min_running_time:30:20
```

### Tag: _max____running____time_

The maximum amount of time the device needs to run in the time range specified by Earli-
estStart and LatestEnd. The difference between MaxRunningTime and MinRunningTime is 
the optional runtime that can be assigned to the device by the EM if cheap energy (e.g. 
excess PV-energy) is available.  [in minutes]

_Required_: yes<br>

Example: 

```
semp:max_running_time:50
```

If you want multiple time frames then you need to separate them with":"
 
```
semp:max_running_time:50:40
```

### Tag: days___of___week

Some consumers didn’t have to run every day. Meybe only on the weekdays or on the weekend. With this parameter it’s possible to set the relevant days of the week. These days have to be set in short format, Mon, Tue, Wed, Thu, Fri, Sat, Sun.

_Required_: no

Example:

```
semp:days_of_week:Mon
```

If you want multiple days then you need to separate them with":"

```
semp:days_of_week:Mon,Wed,Fri
```


## Example

demo.items:

```
Group PwrSwitchMedia    "PowerSwitchMedia"    ["Consumer","semp:device_serial:abc123456789","semp:max_power:1400","semp:earliest_start:600","semp:latest_end:1000","semp:min_running_time:0","semp:max_running_time:300"] 

Switch Media_Switch   "Media center switch" (PwrSwitchMedia) { knx="<3/1/4" }
Number Media_Power    "Media center power" (PwrSwitchMedia)  { channel="edimax:sp2101w:29a0126d:power"} 

Contact isListening "Consumer is Listening" (PwrSwitchMedia)  ["Listening"] 
```


