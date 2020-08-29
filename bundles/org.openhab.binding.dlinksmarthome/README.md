# D-Link Smart Home Binding

A binding for D-Link Smart Home devices.

## Supported Things

### DCH-S150 (WiFi motion sensor)

The binding has been tested with hardware revisions A1 and A2 running firmware version 1.22.

### DSP-W215 (Wifi smart plug)

## Discovery

The binding can automatically discover devices that have already been added to the Wifi network. Please refer to your mydlink Home app for instructions on how to add your device to your Wifi network.

## Binding Configuration

The binding does not require any special configuration.

## Thing Configuration

It is recommended to let the binding discover and add devices.
Once added the configuration must be updated to specify the PIN code located on the back of the device.

### DCH-S150 and DSP-W215

- **ipAddress** - Hostname or IP of the device
- **pin** - PIN code from the back of the device

To manually configure a Thing you must specify its IP address and PIN code.

In the Thing file, this looks like e.g.

```java
  Thing dlinksmarthome:DCH-S150:mysensor [ ipAddress="192.168.2.132" pin="1234" ]
```

## Channels

### DCH-S150

- **motion** - Triggered when the sensor detects motion.

### DSP-W215

- **current_consumption** - the current power consumption.
- **total_consumption** - the total power consumption in the device lifetime.
- **temperature** - the device temperature.
- **state** - the device state (on/off)

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

