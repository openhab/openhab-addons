# IoTaWatt Binding

This binding integrates [IoTaWatt™ Open WiFi Electric Power Monitor](https://iotawatt.com/) into openHAB.

Limitations of this version:

- No authentication support

## Supported Things

The IoTaWatt binding supports one Thing called `iotawatt`.

## Discovery

The binding does not auto-discover the IoTaWatt device.  

## Thing Configuration

### IoTaWatt Thing Configuration

| Name            | Type    | Description                                    | Default | Required | Advanced |
|-----------------|---------|------------------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device           | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in sec.          | 10      | no       | no       |
| requestTimeout  | long    | The request timeout to call the device in sec. | 10      | no       | no       |

## Channels

The binding detects configured inputs and outputs and creates channels for them.

| Channel             | Type                     | ID                  | Read/Write | Description                     |
|---------------------|--------------------------|---------------------|------------|---------------------------------|
| Amps                | Number:Power             | amps                | RO         | The current amps                |
| Frequency           | Number:Frequency         | frequency           | RO         | The current AC frequency        |
| Power Factor        | Number:Dimensionless     | power-factor        | RO         | The current power factor        |
| Apparent Power      | Number:Power             | apparent-power      | RO         | The current apparent power      |
| Reactive Power      | Number:Power             | reactive-power      | RO         | The current reactive power      |
| Reactive Power hour | Number:Power             | reactive-power-hour | RO         | The current reactive power hour |
| Voltage             | Number:ElectricPotential | voltage             | RO         | The current voltage             |
| Power Consumption   | Number:Power             | watts               | RO         | The current power consumption   |
| Phase               | Number:Dimensionless     | phase               | RO         | The current phase               |

## Example Configuration

### Thing with Channels

```java
Thing iotawatt:iotawatt:iotawatt1 "IoTaWatt 1" [ hostname="192.168.1.10" ] {
 Channels:
  Type voltage      : input_00#voltage "Voltage"
  Type frequency    : input_00#frequency "AC Frequency"
  Type phase        : input_00#phase "Phase"
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
Number:ElectricPotential input_voltage "Voltage"           { channel="iotawatt:iotawatt:iotawatt1:input_00#voltage"  }
Number:Frequency input_frequency "AC Frequency"    { channel="iotawatt:iotawatt:iotawatt1:input_00#frequency"  }
Number:Dimensionless input_phase0 "Phase"               { channel="iotawatt:iotawatt:iotawatt1:input_00#phase" }
Number:Power input_watts "Watts"               { channel="iotawatt:iotawatt:iotawatt1:input_01#watts" }
Number:Dimensionless input_power_factor "Power Factor" { channel="iotawatt:iotawatt:iotawatt1:input_01#power-factor" }
Number:Dimensionless input_phase1 "Phase"               { channel="iotawatt:iotawatt:iotawatt1:input_01#phase" }

Number:ElectricCurrent output_amps "Amps"                               { channel="iotawatt:iotawatt:iotawatt1:output_00#Input_1_amps" }
Number:Frequency output_frequency "AC Frequency"                     { channel="iotawatt:iotawatt:iotawatt1:output_01#Input_1_hz" }
Number:Dimensionless output_power_factor "Power Factor"               { channel="iotawatt:iotawatt:iotawatt1:output_02#Input_1_pf" }
Number:Power output_apparent_power "Apparent Power"           { channel="iotawatt:iotawatt:iotawatt1:output_03#Input_1_va" }
Number:Power output_reactive_power "Reactive Power"           { channel="iotawatt:iotawatt:iotawatt1:output_04#Input_1_var" }
Number:Energy output_reactive_power_hour "Reactive Power Hour" { channel="iotawatt:iotawatt:iotawatt1:output_05#Input_1_varh" }
Number:ElectricPotential output_voltage "Voltage"                         { channel="iotawatt:iotawatt:iotawatt1:output_06#Input_1_volts" }
Number:Power output_watts "Watts"                             { channel="iotawatt:iotawatt:iotawatt1:output_07#Input_1_watts" }
```
