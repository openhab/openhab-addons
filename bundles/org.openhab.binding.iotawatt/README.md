# IoTaWatt Binding

This binding integrates [IoTaWatt™ Open WiFi Electric Power Monitor](https://iotawatt.com/) into openHAB.

Limitations of this version:

- No authentication support
- Support of IoTaWatt's `Input` channels with the values below.

Supported values on the `Input` channels:

- Power consumption
- Voltage
- AC Frequency

## Supported Things

IoTaWatt binding supports one Thing called `iotawatt`.

## Discovery

The binding does not auto-discover the IoTaWatt device.
It detects configured inputs and creates channels for them.

## Thing Configuration

### IoTaWatt Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device  | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in sec. | 10      | no       | no       |

## Channels

| Channel           | Type                     | Read/Write | Description                   |
|-------------------|--------------------------|------------|-------------------------------|
| Power Consumption | Number:Power             | RO         | The current power consumption |
| Voltage           | Number:ElectricPotential | RO         | The current voltage           |
| Frequency         | Number:Frequency         | RO         | The current AC frequency      |

## Example configuration

### Thing with channels

```java
Thing iotawatt:iotawatt:iotawatt1 "IoTaWatt 1" [ hostname="192.168.1.10" ] {
 Channels:
  Type watts : input_01#watts "Power Consumption"
  Type voltage  : input_00#voltage  "Voltage"
  Type frequency    : input_00#frequency    "AC Frequency"
}
```

### Items

```java
Number Watts "Current Watts"        { channel="iotawatt:iotawatt:iotawatt1:input_01#watts" }
Number voltage  "Current Voltage"      { channel="iotawatt:iotawatt:iotawatt1:input_00#voltage"  }
Number frequency    "Current AC Frequency" { channel="iotawatt:iotawatt:iotawatt1:input_00#frequency"    }
```
