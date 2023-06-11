# Gembird energenie Binding

This binding integrates the Gembird energenie range of power extenders by using the Energenie Data Exchange Protocol and power reading devices through HTTP interface.

## Supported Things

The Binding supports PM2-LAN, PMS-LAN, PMS2-LAN or PMS-WLAN power extenders as well as PWM-LAN power measurement devices.

## Discovery

Gembird energenie devices don't support autodiscovery, all Things need to be created manually.

## Binding Configuration

The Binding does not need any specific configuration

## Thing Configuration

The device requires the IP-address and a password as a configuration value in order for the binding to know where to access it and to login to the device.

| Parameter | Description                                          |
|-----------|------------------------------------------------------|
| host      | IP-Address of energenie device                       |
| password  | Password to access energenie device, defaults to "1" |

## Channels

The following channels are supported by PM2-LAN, PMS-LAN, PMS2-LAN or PMS-WLAN devices

| channel  | type   | description                                        |
|----------|--------|----------------------------------------------------|
| socket1  | Switch | This is the control channel for the first socket   |
| socket2  | Switch | This is the control channel for the second socket  |
| socket3  | Switch | This is the control channel for the third socket   |
| socket4  | Switch | This is the control channel for the fourth socket  |

PWM-LAN devices support the following channels

| channel  | type                     | description                              |
|----------|--------------------------|------------------------------------------|
| voltage  | Number:ElectricPotential | Channel for output voltage measurement   |
| current  | Number:ElectricCurrent   | Channel for output current measurement   |
| power    | Number:Power             | Channel for output power measurement     |
| energy   | Number:Energy            | channel for output energy measurement    |

## Full Example

Things

```java
Thing energenie:pm2lan:pm2lan [ host="xxx.xxx.xxx.xxx", password="your password" ]
Thing energenie:pmslan:pmslan [ host="xxx.xxx.xxx.xxx", password="your password" ]
Thing energenie:pms2lan:pms2lan [ host="xxx.xxx.xxx.xxx", password="your password" ]
Thing energenie:pmswlan:pmswlan [ host="xxx.xxx.xxx.xxx", password="your password" ]
Thing energenie:pwmlan:pwmlan [ host="xxx.xxx.xxx.xxx", password="your password" ]
```

Items

```java
//Power extenders
Switch Socket1  { channel="energenie:pm2lan:pm2lan:socket1" }
Switch Socket2  { channel="energenie:pm2lan:pm2lan:socket2" }
Switch Socket3  { channel="energenie:pm2lan:pm2lan:socket3" }
Switch Socket4  { channel="energenie:pm2lan:pm2lan:socket4" }

//Power measurement
Number Voltage { channel="energenie:pwmlan:pwmlan:voltage" }
Number Current { channel="energenie:pwmlan:pwmlan:current" }
Number Power { channel="energenie:pwmlan:pwmlan:power" }
Number Energy { channel="energenie:pwmlan:pwmlan:energy" }
```

Sitemap

```perl
sitemap energenie label="Energenie Devices"
{
    Frame {
       // Power extenders
       Switch item=Socket1
       Switch item=Socket2
       Switch item=Socket3
       Switch item=Socket4
       
       // Power measurement
       Number item=Voltage
       Number item=Current
       Number item=Power
       Number item=Energy
       }
}
```
