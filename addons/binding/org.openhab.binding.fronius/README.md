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



## Any custom content here!

Tested with a Fronius Symo 8.2-3-M
