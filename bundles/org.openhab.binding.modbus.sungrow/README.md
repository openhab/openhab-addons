# Sungrow

This extension adds support for Sungrow hybrid inverters. These hybrid inverters allow direct connection of a storage battery.

## Supported Things

This extension has been tested with an SH10RT inverter but should at least other inverters of SH*RT series.

### Auto Discovery

This extension fully supports modbus auto discovery.

Auto discovery is turned off by default in the modbus binding so you have to enable it manually.

You can set the `enableDiscovery=true` parameter in your bridge.

A typical bridge configuration would look like this:

```
Bridge modbus:tcp:bridge [ host="10.0.0.2", port=502, id=1, enableDiscovery=true ]
```

## Thing Configuration

You need first to set up a TCP Modbus bridge according to the Modbus documentation.
Things in this extension will use the selected bridge to connect to the device.

The thing supports the following parameters:

| Parameter | Type    | Required | Default if omitted  | Description                             |
|-----------|---------|----------|---------------------|-----------------------------------------|
| refresh   | integer | no       | 5                   | Poll interval in seconds. Increase this if you encounter connection errors |
