# D-Link Smart Home Binding

A binding for D-Link Smart Home devices.

## Supported Things

### DCH-S150 (WiFi motion sensor)

The binding has been tested with hardware revisions A1 and A2 running firmware version 1.22. 
The mydlink Home service is now end of life and the device requires a daily reboot (performed by the binding) to keep it responsive.

## Discovery

The binding can automatically discover devices that have already been added to the Wifi network.

## Binding Configuration

The binding does not require any special configuration.

## Thing Configuration

It is recommended to let the binding discover and add devices.
Once added the configuration must be updated to specify the PIN code located on the back of the device.

### DCH-S150

- **ipAddress** - Hostname or IP of the device
- **pin** - PIN code from the back of the device
- **rebootHour** - Hour (24h) of the day that the device will be rebooted to ensure that it remains responsive (default is 3).

To manually configure a DCH-S150 Thing you must specify its IP address and PIN code.

In the Thing file, this looks like e.g.

```java
  Thing dlinksmarthome:DCH-S150:mysensor [ ipAddress="192.168.2.132" pin="1234" ]
```

## Channels

### DCH-S150

- **motion** - Triggered when the sensor detects motion.

## Example usage

### DCH-S150

```perl
  rule "Landing motion"
  when
      Channel "dlinksmarthome:DCH-S150:90-8D-78-XX-XX-XX:motion" triggered
  then
      println("Motion has been detected")
  end
```
