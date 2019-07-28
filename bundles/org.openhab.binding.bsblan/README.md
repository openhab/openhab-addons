# BSB-LAN Binding

This binding uses the REST API of [BSB-LPB-PPS-LAN](https://github.com/fredlcore/bsb_lan) to obtain data from the device.

## Supported Things

Currently only retrieving parameters of a connected BSB-LAN device is supported.

## Discovery

There is no discovery implemented. You have to create your things manually and specify the hostname/IP of the BSB-LAN device.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

### Bridge Thing Configuration

| Property         | Default | Required | Type    | Description                                                                                |
|------------------|---------|----------|---------|--------------------------------------------------------------------------------------------|
| hostname         | -       | Yes      | String  | The hostname or IP address of the BSB-LAN device.                                          |
| passkey          | -       | No       | String  | The passkey required to access the BSB-LAN device.                                         |
| username         | -       | No       | String  | The username required to access the BSB-LAN device (when using HTTP Basic Authentication). |
| password         | -       | No       | String  | The password required to access the BSB-LAN device (when using HTTP Basic Authentication). |
| refreshInterval  | 60      | No       | Integer | Specifies the refresh (poll) interval in seconds. Minimum value: 5s                        |

### Parameter Thing Configuration

| Property  | Default | Required | Type    | Description                                                                              |
|-----------|---------|----------|---------|------------------------------------------------------------------------------------------|
| id        | -       | Yes      | Integer | Specific parameter identifier (numeric value)                                            |
| setId     | -       | No       | Integer | Parameter identifier used for set requests (numeric value).<br />If not specified it falls back to the value of the `id` property. |
| setType   | 0       | No       | Integer | Message type used for set requests. Possible values are:<br />`0` for `SET` requests<br />`1` for `INF` requests |

## Channels

### Parameter Thing Channels

| Channel ID   | Item Type | Description                                                                        |
|--------------|-----------|------------------------------------------------------------------------------------|
| name         | String    | Name of the parameter as provided by the BSB-LAN device.                           |
| number-value | Number    | Value of the parameter converted to a numerical value (if possible).               |
| string-value | String    | Value of the parameter as provided by the BSB-LAN device.                          |
| switch-value | Switch    | Value of the parameter.<br />`0` is interpreted as `OFF`, everything else as `ON`. |
| unit         | String    | Unit as provided by the BSB-LAN device (HTML unescaping applied).                  |
| description  | String    | Description as provided by the BSB-LAN device.                                     |
| datatype     | Number    | Datatype as provided by the BSB-LAN device. Possible values are currently<br />`0` for `DT_VALS`: plain value<br />`1` for `DT_ENUM`: value (8/16 Bit) followed by space followed by text<br />`2` for `DT_BITS`: bit value followed by bitmask followed by text<br />`3` for `DT_WDAY`: weekday<br />`4` for `DT_HHMM`: hour:minute<br />`5` for `DT_DTTM`: date and time<br />`6` for `DT_DDMM`: day and month<br />`7` for `DT_STRN`: String<br />`8` for `DT_DWHM`: PPS time (day of week, hour:minute) |

## Full Example

bsblan.things:

```
Bridge bsblan:bridge:heating [hostname="192.168.1.100", refreshInterval=30] {
    Thing parameter p700  [id=700]
    Thing parameter p710  [id=710]
    Thing parameter p8730 [id=8730]
}
```

bsblan.items:

```
Number BsbParameter700NumberValue  { channel="bsblan:parameter:heating:p700:number-value" }
Number BsbParameter710NumberValue  { channel="bsblan:parameter:heating:p710:number-value" }
String BsbParameter8730Description { channel="bsblan:parameter:heating:p8730:description" }
```

bsblan.sitemap:

```
sitemap bsblan label="BSB-LAN" {
    Selection item=BsbParameter700NumberValue label="Operating Mode" mappings=[0="Protection", 1="Automatic", 2="Reduced", 3="Comfort"]
    Text item=BsbParameter710NumberValue label="Room Temperature Comfort Setpoint [%.1f °C]" icon="temperature"
    Text item=BsbParameter8730Description label="Heating Circuit Pump [%s]"
}
```
