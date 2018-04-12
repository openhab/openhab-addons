# Fronius Binding

This binding uses the [Fronius Solar API V1](http://www.fronius.com/en/photovoltaics/products/all-products/system-monitoring/open-interfaces/fronius-solar-api-json-) to obtain data from a Fronius devices.


## Supported Things

Support Fronius Galvo, Fronius Symo inverters and other Fronius inverters in combination with the Fronius Datamanager 1.0 / 2.0 or Fronius Datalogger. 
You can add multiple inverters that depend on the same datalogger with different device ids. ( Default 1 ) 

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
Bridge fronius:bridge:mybridge [hostname="192.168.66.148",refreshInterval=5] {
    Thing powerinverter myinverter [ deviceId=1 ]
}
```

demo.items:

```
Number AC_Powery { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachannelpac" }
Number Day_Energy { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachanneldayenergy" }
Number Total_Energy { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachanneltotal" }
Number Year_Energy { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachannelyear" }
Number FAC { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachannelfac" }
Number IAC { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachanneliac" }
Number IDC { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachannelidc" }
Number UAC { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachanneluac" }
Number UDC { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachanneludc" }
Number Grid_Power { channel="fronius:powerinverter:mybridge:myinverter:powerflowchannelpgrid" }
Number Load_Power { channel="fronius:powerinverter:mybridge:myinverter:powerflowchannelpload" }
Number Load_Power { channel="fronius:powerinverter:mybridge:myinverter:powerflowchannelpload" }
Number Load_Power { channel="fronius:powerinverter:mybridge:myinverter:powerflowchannelpload" }
Number Battery_Power { channel="fronius:powerinverter:mybridge:myinverter:powerflowchannelpakku" }
```

Tested with a Fronius Symo 8.2-3-M
