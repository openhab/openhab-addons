# Ruuvi Tag / Ruuvi Air

This extension adds support for [Ruuvi Tag](https://ruuvi.com/) and [Ruuvi Air](https://ruuvi.com/) sensor beacons.

## Supported Things

This binding supports the following thing type:

| Thing Type ID    | Description                           |
| ---------------- | ------------------------------------- |
| ruuvitag_beacon  | Ruuvi Beacon (RuuviTag or Ruuvi Air)  |

The `ruuvitag_beacon` thing type supports both RuuviTag and Ruuvi Air devices:
- **RuuviTag**: Environmental sensors (temperature, humidity, pressure, acceleration)
- **Ruuvi Air**: Air quality sensors (environmental + PM, CO2, VOC, NOx, luminosity)

Under normal conditions sensors should submit data every 10 seconds.
However, if no data has been retrieved after 1 minute, the thing is set to OFFLINE and all channel states are set to UNDEF.
When new data is received, the thing is set back to ONLINE.

## Discovery

As any other Bluetooth device, sensor beacons are discovered automatically by the corresponding bridge.

## Thing Configuration

There is only a single configuration parameter `address`, which corresponds to the Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX").

## Channels

The following channels are available on these sensors:

| Channel ID                | Item Type                | RuuviTag | Ruuvi Air | Data Format | Description                           |
| ------------------------- | ------------------------ | -------- | --------- | ----------- | ------------------------------------- |
| rssi                      | Number:Power             | ✓        | ✓         | All         | Received signal strength indicator    |
| temperature               | Number:Temperature       | ✓        | ✓         | All         | Measured temperature                  |
| humidity                  | Number:Dimensionless     | ✓        | ✓         | All         | Measured humidity                     |
| pressure                  | Number:Pressure          | ✓        | ✓         | All         | Measured air pressure                 |
| dataFormat                | Number                   | ✓        | ✓         | All         | Data format version                   |
| measurementSequenceNumber | Number:Dimensionless     | ✓        | ✓         | All         | Measurement sequence number           |
| **RuuviTag Channels**     | | | | | |
| batteryVoltage            | Number:ElectricPotential | ✓        |           | 3, 5        | Measured battery voltage              |
| accelerationx             | Number:Acceleration      | ✓        |           | 3, 5        | Acceleration on X axis                |
| accelerationy             | Number:Acceleration      | ✓        |           | 3, 5        | Acceleration on Y axis                |
| accelerationz             | Number:Acceleration      | ✓        |           | 3, 5        | Acceleration on Z axis                |
| txPower                   | Number:Power             | ✓        |           | 3, 5        | TX power                              |
| movementCounter           | Number:Dimensionless     | ✓        |           | 3, 5        | Movement counter                      |
| **Ruuvi Air Channels**    | | | | | |
| pm1                       | Number:Density           |          | ✓         | E1          | PM1.0 particulate matter (≤1.0 μm)    |
| pm25                      | Number:Density           |          | ✓         | 6, E1       | PM2.5 particulate matter (≤2.5 μm)    |
| pm4                       | Number:Density           |          | ✓         | E1          | PM4.0 particulate matter (≤4.0 μm)    |
| pm10                      | Number:Density           |          | ✓         | E1          | PM10 particulate matter (≤10.0 μm)    |
| co2                       | Number:Dimensionless     |          | ✓         | 6, E1       | CO2 concentration (ppm)               |
| vocIndex                  | Number:Dimensionless     |          | ✓         | 6, E1       | VOC (Volatile Organic Compounds) index (0-500) |
| noxIndex                  | Number:Dimensionless     |          | ✓         | 6, E1       | NOX (Nitrogen Oxides) index (0-500) |
| luminosity                | Number:Illuminance       |          | ✓         | 6, E1       | Light intensity (lux)                 |
| calibrationCompleted      | Switch                   |          | ✓         | 6, E1       | Sensor calibration status             |
| airQualityIndex           | Number:Dimensionless     |          | ✓         | 6, E1       | Air quality index (0-100%, higher = better) |

Note: not all channels are available on all data formats. Availability depends on the [Ruuvi Data Format](https://docs.ruuvi.com/communication/bluetooth-advertisements) used by the device.

### Air Quality Index Interpretation (Ruuvi Air only)

- **VOC Index**: Measures Volatile Organic Compounds on a scale of 0-500. The average value is 100. Values below 100 indicate improving air quality, while values above 100 indicate degrading air quality.
- **NOX Index**: Measures Nitrogen Oxides on a scale of 0-500. The base value is 1, representing typical outdoor conditions. Higher values indicate increased nitrogen oxide concentration.

## Example

demo.things:

```java
// RuuviTag sensor
bluetooth:ruuvitag_beacon:hci0:tag1   "RuuviTag Sensor" (bluetooth:bluez:hci0)   [ address="12:34:56:78:9A:BC" ]

// Ruuvi Air sensor
bluetooth:ruuvitag_beacon:hci0:air1   "Ruuvi Air Sensor" (bluetooth:bluez:hci0)  [ address="12:34:56:78:9A:BD" ]
```

demo.items:

```java
// Common channels (available on both RuuviTag and Ruuvi Air)
Number:Temperature      temperature "Room Temperature [%.1f %unit%]"  { channel="bluetooth:ruuvitag_beacon:hci0:tag1:temperature" }
Number:Dimensionless    humidity    "Humidity [%.0f %unit%]"           { channel="bluetooth:ruuvitag_beacon:hci0:tag1:humidity" }
Number:Pressure         pressure    "Air Pressure [%.0f %unit%]"       { channel="bluetooth:ruuvitag_beacon:hci0:tag1:pressure" }

// RuuviTag-specific channels
Number:Acceleration     accelX      "Acceleration X [%.3f %unit%]"    { channel="bluetooth:ruuvitag_beacon:hci0:tag1:accelerationx" }
Number:ElectricPotential battery    "Battery Voltage [%.2f %unit%]"   { channel="bluetooth:ruuvitag_beacon:hci0:tag1:batteryVoltage" }

// Ruuvi Air channels (Format 6 or E1)
Number:Density          pm25        "PM2.5 [%.1f %unit%]"             { channel="bluetooth:ruuvitag_beacon:hci0:air1:pm25" }
Number:Density          pm1         "PM1.0 [%.1f %unit%]"             { channel="bluetooth:ruuvitag_beacon:hci0:air1:pm1" }
Number:Density          pm10        "PM10.0 [%.1f %unit%]"            { channel="bluetooth:ruuvitag_beacon:hci0:air1:pm10" }
Number:Dimensionless    co2         "CO2 [%.0f ppm]"                  { channel="bluetooth:ruuvitag_beacon:hci0:air1:co2" }
Number:Dimensionless    vocIndex    "VOC Index [%.0f]"                { channel="bluetooth:ruuvitag_beacon:hci0:air1:vocIndex" }
Number:Illuminance      luminosity  "Light Intensity [%.0f %unit%]"   { channel="bluetooth:ruuvitag_beacon:hci0:air1:luminosity" }
Number:Dimensionless    aqi         "Air Quality Index [%.0f %unit%]" { channel="bluetooth:ruuvitag_beacon:hci0:air1:airQualityIndex" }
```

## Data Format Support

The binding supports the following Ruuvi data formats:

| Format | Description | RuuviTag | Ruuvi Air | BLE Version |
| ------ | ----------- | -------- | --------- | ----------- |
| 3 (RAWv2) | Environmental + acceleration | ✓ | | BT 4.x+ |
| 5 (RAWv2) | Environmental + acceleration (extended range) | ✓ | | BT 4.x+ |
| 6 | Environmental + air quality | | ✓ | BT 4.x+ |
| E1 | Environmental + air quality (extended) | | ✓ | BT 5.0+ |

**Format E1 Support:**
The binding automatically detects Data Format E1 (Extended v1) from Bluetooth 5.0+ capable Ruuvi Air devices.
Per the Ruuvi specification, once Format E1 is detected from a device, subsequent Format 6 packets are automatically discarded
to ensure the device preferentially uses the newer, more comprehensive E1 format.

**PM Measurements:**
- **Format 6** provides: PM2.5
- **Format E1** provides: PM1.0, PM2.5, PM4.0, PM10.0 (all four particulate matter sizes)

For more information about Ruuvi data formats, see the [official Ruuvi documentation](https://docs.ruuvi.com/communication/bluetooth-advertisements).
