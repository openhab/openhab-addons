# Fronius Binding

This binding uses the [Fronius Solar API V1](https://www.fronius.com/en/photovoltaics/products/all-products/system-monitoring/open-interfaces/fronius-solar-api-json-) to obtain data from a Fronius devices.


## Supported Things

Support Fronius Galvo, Fronius Symo inverters and other Fronius inverters in combination with the Fronius Datamanager 1.0 / 2.0 or Fronius Datalogger.
You can add multiple inverters that depend on the same datalogger with different device ids. (Default 1)

## Discovery

There is no discovery implemented. You have to create your things manually and specify the hostname or IP address of the Datalogger and the device id.

## Binding Configuration

The binding has no configuration options, all configuration is done at `bridge` or `powerinverter` level.

## Thing Configuration

### Bridge Thing Configuration

| Parameter       | Description                                           |
|-----------------|------------------------------------------------------ |
| hostname        | The hostname or IP address of your Fronius Datalogger |
| refreshInterval | Refresh interval in seconds                           |

### Powerinverter Thing Configuration

| Parameter       | Description                                           |
|-----------------|------------------------------------------------------ |
| deviceId        | The identifier of your device (Default: 1)            |

## Channels

| Channel ID | Item Type    | Description              |
|------------|--------------|------------------------- |
| inverterdatachanneldayenergy | Number | Energy generated on current day |
| inverterdatachannelpac | Number | AC powery |
| inverterdatachanneltotal | Number | Energy generated overall |
| inverterdatachannelyear | Number | Energy generated in current year |
| inverterdatachannelfac | Number | AC frequency |
| inverterdatachanneliac | Number | AC current |
| inverterdatachannelidc | Number | DC current |
| inverterdatachanneluac | Number | AC voltage |
| inverterdatachanneludc | Number | DC voltage |
| inverterdatadevicestatuserrorcode | Number | Device error code |
| inverterdatadevicestatusstatuscode | Number | Device status code<br />`0` - `6` Startup<br />`7` Running <br />`8` Standby<br />`9` Bootloading<br />`10` Error |
| powerflowchannelpgrid | Number | Power + from grid, - to grid |
| powerflowchannelpload | Number | Power + generator, - consumer |
| powerflowchannelpakku | Number | Power + charge, - discharge |

## Full Example

demo.things:

```
Bridge fronius:bridge:mybridge [hostname="192.168.66.148", refreshInterval=5] {
    Thing powerinverter myinverter [deviceId=1]
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
Number ErrorCode { channel="fronius:powerinverter:mybridge:myinverter:inverterdatadevicestatuserrorcode" }
Number StatusCode { channel="fronius:powerinverter:mybridge:myinverter:inverterdatadevicestatusstatuscode" }
Number Grid_Power { channel="fronius:powerinverter:mybridge:myinverter:powerflowchannelpgrid" }
Number Load_Power { channel="fronius:powerinverter:mybridge:myinverter:powerflowchannelpload" }
Number Battery_Power { channel="fronius:powerinverter:mybridge:myinverter:powerflowchannelpakku" }
```

Tested with a Fronius Symo 8.2-3-M
