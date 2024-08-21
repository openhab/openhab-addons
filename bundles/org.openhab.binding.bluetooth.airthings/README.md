# Airthings

This extension adds support for [Airthings](https://www.airthings.com) indoor air quality monitor sensors.

## Supported Things

Following thing types are supported by this extension:

| Thing Type ID        | Description                            |
|----------------------|----------------------------------------|
| airthings_wave_plus  | Airthings Wave Plus                    |
| airthings_wave_mini  | Airthings Wave Mini                    |
| airthings_wave_gen1  | Airthings Wave 1st Gen (SN 2900xxxxxx) |
| airthings_wave_radon | Airthings Wave Radon / Wave 2          |

## Discovery

As any other Bluetooth device, Airthings devices are discovered automatically by the corresponding bridge.

## Thing Configuration

Supported configuration parameters for the things:

| Property                        | Type    | Default | Required | Description                                                     |
|---------------------------------|---------|---------|----------|-----------------------------------------------------------------|
| address                         | String  |         | Yes      | Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX") |
| refreshInterval                 | Integer | 300     | No       | How often a refresh shall occur in seconds                      |

## Channels

Following channels are supported for `Airthings Wave Mini` thing:

| Channel ID         | Item Type                | Description                                 |
| ------------------ | ------------------------ | ------------------------------------------- |
| temperature        | Number:Temperature       | The measured temperature                    |
| humidity           | Number:Dimensionless     | The measured humidity                       |
| tvoc               | Number:Dimensionless     | The measured TVOC level                     |

The `Airthings Wave Plus` thing has additionally the following channels:

| Channel ID         | Item Type                        | Description                                 |
| ------------------ | -------------------------------- | ------------------------------------------- |
| pressure           | Number:Pressure                  | The measured air pressure                   |
| co2                | Number:Dimensionless             | The measured CO2 level                      |
| radon_st_avg       | Number:RadiationSpecificActivity | The measured radon short term average level |
| radon_lt_avg       | Number:RadiationSpecificActivity | The measured radon long term average level  |

The `Airthings Wave Gen 1` and `Airthings Wave Radon / Wave 2` thing has the following channels:

| Channel ID         | Item Type                        | Description                                 |
| ------------------ | -------------------------------- | ------------------------------------------- |
| radon_st_avg       | Number:RadiationSpecificActivity | The measured radon short term average level |
| radon_lt_avg       | Number:RadiationSpecificActivity | The measured radon long term average level  |
| temperature        | Number:Temperature               | The measured temperature                    |
| humidity           | Number:Dimensionless             | The measured humidity                       |

Note: For the `Airthings Wave Gen 1`, only one channel can be updated at each refreshInterval, so it will take refreshInterval x 4 cycles to sequentially update all 4 channels  

## Example

airthings.things (assuming you have a Bluetooth bridge with the ID `bluetooth:bluegiga:adapter1`:

```java
bluetooth:airthings_wave_plus:adapter1:sensor1  "Airthings Wave Plus Sensor 1" (bluetooth:bluegiga:adapter1) [ address="12:34:56:78:9A:BC", refreshInterval=300 ]
```

airthings.items:

```java
Number:Temperature                  temperature     "Temperature [%.1f %unit%]"                   { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:temperature" }
Number:Dimensionless                humidity        "Humidity [%d %unit%]"                        { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:humidity" }
Number:Pressure                     pressure        "Air Pressure [%d %unit%]"                    { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:pressure" }
Number:Dimensionless                co2             "CO2 level [%d %unit%]"                       { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:co2" }
Number:Dimensionless                tvoc            "TVOC level [%d %unit%]"                      { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:tvoc" }
Number:RadiationSpecificActivity    radon_st_avg    "Radon short term average level [%d %unit%]"  { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:radon_st_avg" }
Number:RadiationSpecificActivity    radon_lt_avg    "Radon long term average level [%d %unit%]"   { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:radon_lt_avg" }
```
