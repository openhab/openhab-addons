# Ruuvi Gateway MQTT Binding

This binding allows integration of Ruuvi sensors via MQTT data, as collected by [Ruuvi Gateway](https://ruuvi.com/gateway/).
Ruuvi Gateway listens for Bluetooth advertisements and publishes that data over MQTT.
Ruuvi Cloud Subscription is not required as the integration is local.

Compared to the Ruuvi Tag Bluetooth binding, this binding benefits from the strong and reliable antenna of Ruuvi Gateway, as opposed to the typically weaker antenna integrated onto computer motherboards.
The primary requirement is having a Ruuvi Gateway device.

This binding supports RuuviTag sensors (all variants including Pro 2in1, 3in1, and 4in1) and Ruuvi Air quality sensors.

## Set up the Gateway

Before using this binding, the Ruuvi Gateway needs to be configured to publish the sensor data via MQTT.

For further instructions, refer to the relevant section in the [Ruuvi Gateway documentation](https://ruuvi.com/gateway-config/).
For the most convenient usage of this binding, ensure that the "Use 'ruuvi' as the prefix" MQTT setting is enabled on the Ruuvi Gateway.

## Discovery

First install the MQTT binding and set up a `broker` Thing, and make sure it is ONLINE, as this binding uses the MQTT binding to talk to your broker.

This binding discovers the Ruuvi Tags via the MQTT bridge; the discovered Things should appear in your Thing Inbox.

## Thing Configuration

This binding supports the following thing type:

| Thing Type ID    | Description                          |
| ---------------- | ------------------------------------ |
| ruuvitag_beacon  | Ruuvi Beacon (RuuviTag or Ruuvi Air) |

No manual configuration is needed as discovery can be used instead.

For users who prefer manual configuration, here are the configurable parameters:

| Parameter | Description                               | Required | Default |
|-----------|-------------------------------------------|----------|---------|
| `topic`   | MQTT topic containing the gateway payload | Yes      | (N/A)   |

## Channels

The following channels are available on these sensors:

| Channel ID                | Item Type                | RuuviTag | Ruuvi Air | Description                                                            |
|---------------------------|--------------------------|----------|-----------|------------------------------------------------------------------------|
| rssi                      | Number:Power             | ✓        | ✓         | Received signal strength indicator (between gateway and sensor)         |
| temperature               | Number:Temperature       | ✓        | ✓         | Measured temperature                                                    |
| humidity                  | Number:Dimensionless     | ✓        | ✓         | Measured humidity                                                       |
| pressure                  | Number:Pressure          | ✓        | ✓         | Measured air pressure                                                   |
| dataFormat                | Number                   | ✓        | ✓         | Data format version                                                     |
| measurementSequenceNumber | Number:Dimensionless     | ✓        | ✓         | Measurement sequence number                                             |
| **RuuviTag Channels**     | | | | |
| batteryVoltage            | Number:ElectricPotential | ✓        |           | Measured battery voltage                                                |
| accelerationx             | Number:Acceleration      | ✓        |           | Acceleration on X axis                                                  |
| accelerationy             | Number:Acceleration      | ✓        |           | Acceleration on Y axis                                                  |
| accelerationz             | Number:Acceleration      | ✓        |           | Acceleration on Z axis                                                  |
| txPower                   | Number:Power             | ✓        |           | TX power                                                                |
| movementCounter           | Number:Dimensionless     | ✓        |           | Movement counter                                                        |
| **Ruuvi Air Channels**    | | | | |
| pm1                       | Number:Density           |          | ✓         | PM1.0 particulate matter (≤1.0 μm concentration, E1 only)              |
| pm25                      | Number:Density           |          | ✓         | PM2.5 particulate matter (≤2.5 μm concentration)                        |
| pm4                       | Number:Density           |          | ✓         | PM4.0 particulate matter (≤4.0 μm concentration, E1 only)              |
| pm10                      | Number:Density           |          | ✓         | PM10 particulate matter (≤10.0 μm concentration, E1 only)              |
| co2                       | Number:Dimensionless     |          | ✓         | CO2 concentration (ppm)                                                 |
| vocIndex                  | Number                   |          | ✓         | VOC (Volatile Organic Compounds) index (0-500)                          |
| noxIndex                  | Number                   |          | ✓         | NOX (Nitrogen Oxides) index (0-500)                                     |
| luminosity                | Number:Illuminance       |          | ✓         | Light intensity (lux)                                                   |
| calibrationCompleted      | Switch                   |          | ✓         | Sensor calibration status                                               |
| airQualityIndex           | Number:Dimensionless     |          | ✓         | Air quality index (0-100%, higher = better)                             |
| **Gateway Channels**      | | | | |
| ts                        | DateTime                 | ✓        | ✓         | Timestamp when the message from sensor was received by gateway          |
| gwts                      | DateTime                 | ✓        | ✓         | Timestamp when the message from sensor was relayed by gateway           |
| gwmac                     | String                   | ✓        | ✓         | MAC address of Ruuvi Gateway                                            |

Note: Not all channels are available on all data formats. Availability depends on the [Ruuvi Data Format](https://docs.ruuvi.com/communication/bluetooth-advertisements) used by the device.

Some measurements may not be meaningful for all device types. For example, RuuviTag Pro 2in1 does not have humidity measurement capability.

### Air Quality Index Interpretation (Ruuvi Air only)

- **VOC Index**: Measures Volatile Organic Compounds on a scale of 0-500. The average value is 100. Values below 100 indicate improving air quality, while values above 100 indicate degrading air quality.
- **NOX Index**: Measures Nitrogen Oxides on a scale of 0-500. The base value is 1, representing typical outdoor conditions. Higher values indicate increased nitrogen oxide concentration.

## Example

Please note that Thing and Item configuration can be done fully in the UI.
For those who prefer textual configuration, we share this example here.

To use these examples for textual configuration, you must already have configured an MQTT `broker` Thing and know its unique ID.
This UID will be used in the things file and will replace the text `myBroker`.
The first line in the things file will create a `broker` Thing and this can be removed if you have already setup a broker in another file or via the UI already.

### *.things

```java
Bridge mqtt:broker:myBroker [ host="localhost", secure=false, password="*******", qos=1, username="user"]
mqtt:ruuvitag_beacon:myTag1  "RuuviTag Sensor" (mqtt:broker:myBroker) [ topic="ruuvi/mygw/DE:AD:BE:EF:AA:01" ]
mqtt:ruuvitag_beacon:myAir1  "Ruuvi Air Sensor" (mqtt:broker:myBroker) [ topic="ruuvi/mygw/DE:AD:BE:EF:AA:02" ]
```

### *.items

```java
// Common channels (available on both RuuviTag and Ruuvi Air)
Number:Temperature      temperature "Room Temperature [%.1f %unit%]"  { channel="mqtt:ruuvitag_beacon:myTag1:temperature" }
Number:Dimensionless    humidity    "Humidity [%.0f %unit%]"           { channel="mqtt:ruuvitag_beacon:myTag1:humidity" }
Number:Pressure         pressure    "Air Pressure [%.0f %unit%]"       { channel="mqtt:ruuvitag_beacon:myTag1:pressure" }

// RuuviTag-specific channels
Number:Acceleration     accelX      "Acceleration X [%.3f %unit%]"    { channel="mqtt:ruuvitag_beacon:myTag1:accelerationx" }
Number:ElectricPotential battery    "Battery Voltage [%.2f %unit%]"   { channel="mqtt:ruuvitag_beacon:myTag1:batteryVoltage" }

// Ruuvi Air sensor channels
Number:Density          pm1         "PM1.0 [%.1f %unit%]"             { channel="mqtt:ruuvitag_beacon:myAir1:pm1" }
Number:Density          pm25        "PM2.5 [%.1f %unit%]"             { channel="mqtt:ruuvitag_beacon:myAir1:pm25" }
Number:Density          pm4         "PM4.0 [%.1f %unit%]"             { channel="mqtt:ruuvitag_beacon:myAir1:pm4" }
Number:Density          pm10        "PM10 [%.1f %unit%]"              { channel="mqtt:ruuvitag_beacon:myAir1:pm10" }
Number:Dimensionless    co2         "CO2 [%.0f ppm]"                  { channel="mqtt:ruuvitag_beacon:myAir1:co2" }
Number                  vocIndex    "VOC Index [%.0f]"                { channel="mqtt:ruuvitag_beacon:myAir1:vocIndex" }
Number                  noxIndex    "NOx Index [%.0f]"                { channel="mqtt:ruuvitag_beacon:myAir1:noxIndex" }
Number:Illuminance      luminosity  "Light Intensity [%.0f %unit%]"   { channel="mqtt:ruuvitag_beacon:myAir1:luminosity" }
Number:Dimensionless    aqi         "Air Quality Index [%.0f %unit%]" { channel="mqtt:ruuvitag_beacon:myAir1:airQualityIndex" }
Switch                  cal_status  "Calibration Completed"           { channel="mqtt:ruuvitag_beacon:myAir1:calibrationCompleted" }
```
