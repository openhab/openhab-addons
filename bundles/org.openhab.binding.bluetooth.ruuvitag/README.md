# Ruuvi Tag / Ruuvi Air

This extension adds support for [Ruuvi Tag](https://ruuvi.com/) and [Ruuvi Air](https://ruuvi.com/) sensor beacons.

## Supported Things

This binding supports the following thing types:

| Thing Type ID    | Description                           |
| ---------------- | ------------------------------------- |
| ruuvitag_beacon  | RuuviTag sensor (all variants)        |
| ruuviair_beacon  | Ruuvi Air air quality sensor          |

The `ruuvitag_beacon` thing type supports RuuviTag devices, including RuuviTag Pro 2in1, 3in1, and 4in1 variants.

Under normal conditions sensors should submit data every 10 seconds.
However, if no data has been retrieved after 1 minute, the thing is set to OFFLINE and all channel states are set to UNDEF.
When new data is received, the thing is set back to ONLINE.

## Discovery

As any other Bluetooth device, sensor beacons are discovered automatically by the corresponding bridge.

## Thing Configuration

There is only a single configuration parameter `address`, which corresponds to the Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX").

## Channels

The following channels are available on these sensors:

| Channel ID                | Item Type                | RuuviTag | Ruuvi Air | Description                           |
| ------------------------- | ------------------------ | -------- | --------- | ------------------------------------- |
| rssi                      | Number:Power             | ✓        | ✓         | Received signal strength indicator    |
| temperature               | Number:Temperature       | ✓        | ✓         | Measured temperature                  |
| humidity                  | Number:Dimensionless     | ✓        | ✓         | Measured humidity                     |
| pressure                  | Number:Pressure          | ✓        | ✓         | Measured air pressure                 |
| batteryVoltage            | Number:ElectricPotential | ✓        |           | Measured battery voltage              |
| accelerationx             | Number:Acceleration      | ✓        |           | Acceleration on X axis                |
| accelerationy             | Number:Acceleration      | ✓        |           | Acceleration on Y axis                |
| accelerationz             | Number:Acceleration      | ✓        |           | Acceleration on Z axis                |
| txPower                   | Number:Power             | ✓        |           | TX power                              |
| dataFormat                | Number                   | ✓        | ✓         | Data format version                   |
| measurementSequenceNumber | Number:Dimensionless     | ✓        | ✓         | Measurement sequence number           |
| movementCounter           | Number:Dimensionless     | ✓        |           | Movement counter                      |
| pm25                      | Number:Density           |          | ✓         | PM2.5 particulate matter (≤2.5 μm)    |
| co2                       | Number:Dimensionless     |          | ✓         | CO2 concentration (ppm)               |
| vocIndex                  | Number:Dimensionless     |          | ✓         | VOC (Volatile Organic Compounds) index (0-500, avg=100) |
| noxIndex                  | Number:Dimensionless     |          | ✓         | NOX (Nitrogen Oxides) index (0-500, base=1) |
| luminosity                | Number:Illuminance       |          | ✓         | Light intensity (lux)                 |
| calibrationCompleted      | Switch                   |          | ✓         | Sensor calibration status             |

Note: not all channels are available on all data formats. Availability depends on the [Ruuvi Data Format](https://github.com/ruuvi/ruuvi-sensor-protocols) used by the device.

### Air Quality Index Interpretation (Ruuvi Air only)

- **VOC Index**: Measures Volatile Organic Compounds on a scale of 0-500. The average value is 100. Values below 100 indicate improving air quality, while values above 100 indicate degrading air quality.
- **NOX Index**: Measures Nitrogen Oxides on a scale of 0-500. The base value is 1, representing typical outdoor conditions. Higher values indicate increased nitrogen oxide concentration.

## Example

demo.things:

```java
bluetooth:ruuvitag_beacon:hci0:tag1   "RuuviTag Sensor" (bluetooth:bluez:hci0)   [ address="12:34:56:78:9A:BC" ]
bluetooth:ruuviair_beacon:hci0:air1   "Ruuvi Air Sensor" (bluetooth:bluez:hci0)  [ address="12:34:56:78:9A:BD" ]
```

demo.items:

```java
Number:Temperature      temperature "Room Temperature [%.1f %unit%]"  { channel="bluetooth:ruuvitag_beacon:hci0:tag1:temperature" }
Number:Dimensionless    humidity    "Humidity [%.0f %unit%]"           { channel="bluetooth:ruuvitag_beacon:hci0:tag1:humidity" }
Number:Pressure         pressure    "Air Pressure [%.0f %unit%]"       { channel="bluetooth:ruuvitag_beacon:hci0:tag1:pressure" }

// Air quality measurements from Ruuvi Air
Number:Density          pm25        "PM2.5 [%.1f %unit%]"             { channel="bluetooth:ruuviair_beacon:hci0:air1:pm25" }
Number:Dimensionless    co2         "CO2 [%.0f ppm]"                  { channel="bluetooth:ruuviair_beacon:hci0:air1:co2" }
```
