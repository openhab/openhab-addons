# Fronius Binding

This binding uses the [Fronius Solar API V1](http://www.fronius.com/cps/rde/xchg/SID-50921547-DF4684B5/fronius_international/hs.xsl/83_28911_DEU_HTML.htm) to obtain data from a Fronius devices


## Supported Things

There is exactly one supported thing type, which represents a fronius device. 
You can add multiple Things with different "DeviceIds". ( Default 1 ) 

## Discovery

There is no discovery implemented. You have to create your things manually and specify the IP of the Datalogger and the DeviceId.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                              |
|-----------|------------------------------------------------------------------------- |
| Ip        | the ip-address of your Fronius Datalogger |
| DeviceId  | The identifier of your device ( Default: 1) |
| refresh   | Refresh interval in seconds |

## Channels

| Channel ID | Item Type    | Description              |
|------------|--------------|------------------------- |
| day_energy | Number | Energy generated on current day |
| pac | Number | AC powery |
| total_energy | Number | Energy generated overall |
| year_energy | Number | Energy generated in current year |
| fac | Number | AC frequency |
| iac | Number | AC current |
| idc | Number | DC current |
| uac | Number | AC voltage |
| udc | Number | DC voltage |
| pGrid | Number | Power + from grid, - to grid |
| pLoad | Number | Power + generator, - consumer |
| pAkku | Number | Power + charge, - discharge |

## Full Example

demo.things:

```
Thing fronius:powerinverter:mydevice [ ip="192.168.66.148",refresh=120,deviceId=1 ]
```

demo.items:

```
Number AC_Powery { channel="fronius:powerinverter:mydevice:inverterdatachannelpac" }
Number Day_Energy { channel="fronius:powerinverter:mydevice:inverterdatachanneldayenergy" }
Number Total_Energy { channel="fronius:powerinverter:mydevice:inverterdatachanneltotal" }
Number Year_Energy { channel="fronius:powerinverter:mydevice:inverterdatachannelyear" }
Number FAC { channel="fronius:powerinverter:mydevice:inverterdatachannelfac" }
Number IAC { channel="fronius:powerinverter:mydevice:inverterdatachanneliac" }
Number IDC { channel="fronius:powerinverter:mydevice:inverterdatachannelidc" }
Number UAC { channel="fronius:powerinverter:mydevice:inverterdatachanneluac" }
Number UDC { channel="fronius:powerinverter:mydevice:inverterdatachanneludc" }
Number Grid_Power { channel="fronius:powerinverter:mydevice:powerflowchannelpgrid" }
Number Load_Power { channel="fronius:powerinverter:mydevice:powerflowchannelpload" }
Number Load_Power { channel="fronius:powerinverter:mydevice:powerflowchannelpload" }
Number Load_Power { channel="fronius:powerinverter:mydevice:powerflowchannelpload" }
Number Battery_Power { channel="fronius:powerinverter:mydevice:powerflowchannelpakku" }

```

## Any custom content here!

Tested with a Fronius Symo 8.2-3-M
