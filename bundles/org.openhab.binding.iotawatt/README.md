# IoTaWatt Binding

This binding integrates [IoTaWatt™ Open WiFi Electric Power Monitor](https://iotawatt.com/) into openHAB.

Limitations of this version:

- No authentication support
- Support of IoTaWatt's `Input` and `Output` channels.

## Supported Things

IoTaWatt binding supports one Thing called `iotawatt`.

## Discovery

The binding does not auto-discover the IoTaWatt device.  
It detects configured inputs and outputs and creates channels for them.

## Thing Configuration

### IoTaWatt Thing Configuration

| Name            | Type    | Description                          | Default | Required | Advanced |
|-----------------|---------|--------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in sec. | 10      | no       | no       |

## Channels

| Channel             | Type                     | Read/Write | Description                     |
|---------------------|--------------------------|------------|---------------------------------|
| Amps                | Number:Power             | RO         | The current amps                |
| Frequency           | Number:Frequency         | RO         | The current AC frequency        |
| Power Factor        | Number:Dimensionless     | RO         | The current power factor        |
| Apparent Power      | Number:Power             | RO         | The current apparent power      |
| Reactive Power      | Number:Power             | RO         | The current reactive power      |
| Reactive Power hour | Number:Power             | RO         | The current reactive power hour |
| Voltage             | Number:ElectricPotential | RO         | The current voltage             |
| Power Consumption   | Number:Power             | RO         | The current power consumption   |
| Phase               | Number:Dimensionless     | RO         | The current phase               |

## Example Configuration

### Thing with Channels

```java
Thing iotawatt:iotawatt:iotawatt1 "IoTaWatt 1" [ hostname="192.168.1.10" ] {
 Channels:
  Type voltage      : input_00#voltage "Voltage"
  Type frequency    : input_00#frequency "AC Frequency"
  Type watts        : input_01#watts "Power Consumption"
  Type power-factor : input_01#power-factor "Power Factor"
  Type phase        : input_01#phase "Phase"

  Type amps : output_00#Input_1_amps "Amps"
  Type frequency : output_01#Input_1_hz "Frequency"
  Type power-factor : output_02#Input_1_pf "Power Factor"
  Type apparent-power : output_03#Input_1_va "Apparent Power"
  Type reactive-power : output_04#Input_1_var "Reactive Power"
  Type reactive-power-hour : output_05#Input_1_varh "Reactive Power Hour"
  Type voltage : output_06#Input_1_volts "Voltage"
  Type watts : output_07#Input_1_watts "Watts"
}
```

### Items

```java
Number input_watts "Watts"               { channel="iotawatt:iotawatt:iotawatt1:input_01#watts" }
Number input_voltage "Voltage"           { channel="iotawatt:iotawatt:iotawatt1:input_00#voltage"  }
Number input_frequency "AC Frequency"    { channel="iotawatt:iotawatt:iotawatt1:input_00#frequency"  }
Number input_power_factor "Power Factor" { channel="iotawatt:iotawatt:iotawatt1:input_01#power-factor" }
Number input_phase "Phase"               { channel="iotawatt:iotawatt:iotawatt1:input_01#phase" }

Number output_amps "Amps"                               { channel="iotawatt:iotawatt:iotawatt1:output_00#Input_1_amps" }
Number output_frequency "Frequency"                     { channel="iotawatt:iotawatt:iotawatt1:output_01#Input_1_hz" }
Number output_power_factor "Power Factor"               { channel="iotawatt:iotawatt:iotawatt1:output_02#Input_1_pf" }
Number output_apparent_power "Apparent Power"           { channel="iotawatt:iotawatt:iotawatt1:output_03#Input_1_va" }
Number output_reactive_power "Reactive Power"           { channel="iotawatt:iotawatt:iotawatt1:output_04#Input_1_var" }
Number output_reactive_power_hour "Reactive Power Hour" { channel="iotawatt:iotawatt:iotawatt1:output_05#Input_1_varh" }
Number output_voltage "Voltage"                         { channel="iotawatt:iotawatt:iotawatt1:output_06#Input_1_volts" }
Number output_watts "Watts"                             { channel="iotawatt:iotawatt:iotawatt1:output_07#Input_1_watts" }
```
