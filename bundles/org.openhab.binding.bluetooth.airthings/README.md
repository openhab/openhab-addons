# Airthings

This extension adds support for [Airthings](https://www.airthings.com) indoor air quality monitor sensors. 

## Supported Things

Following thing types are supported by this extension:

| Thing Type ID       | Description               |
| ------------------- | ------------------------- |
| airthings_wave_plus | Airthings Wave+           |


## Discovery

As any other Bluetooth device, Airthings devices are discovered automatically by the corresponding bridge. 

## Thing Configuration

Supported configuration parameters for `Airthings Wave+` thing:

| Property                        | Type    | Default | Required | Description                                                     |
|---------------------------------|---------|---------|----------|-----------------------------------------------------------------|
| address                         | String  |         | Yes      | Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX") |
| refreshInterval                 | Integer | 300     | No       | How often a refresh shall occur in seconds                      |

## Channels

Following channels are supported for `Airthings Wave+` thing:

| Channel ID         | Item Type                | Description                                 |
| ------------------ | ------------------------ | ------------------------------------------- |
| temperature        | Number:Temperature       | The measured temperature                    |
| humidity           | Number:Dimensionless     | The measured humidity                       |
| pressure           | Number:Pressure          | The measured air pressure                   |
| co2                | Number:Dimensionless     | The measured CO2 level                      |
| tvoc               | Number:Dimensionless     | The measured TVOC level                     |
| radon_st_avg       | Number:Density           | The measured radon short term average level |
| radon_lt_avg       | Number:Density           | The measured radon long term average level  |

## Example

airthings.things (assuming you have a Bluetooth bridge with the ID `bluetooth:bluegiga:adapter1`:

```
bluetooth:airthings_wave_plus:adapter1:sensor1  "Airthings Wave Plus Sensor 1" (bluetooth:bluegiga:adapter1) [ address="12:34:56:78:9A:BC", refreshInterval=300 ]
```

airthings.items:

```
Number:Temperature      temperature     "Temperature [%.1f %unit%]"                   { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:temperature" }
Number:Dimensionless    humidity        "Humidity [%d %unit%]"                        { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:humidity" }
Number:Pressure         pressure        "Air Pressure [%d %unit%]"                    { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:pressure" }
Number:Dimensionless    co2             "CO2 level [%d %unit%]"                       { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:co2" }
Number:Dimensionless    tvoc            "TVOC level [%d %unit%]"                      { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:tvoc" }
Number:Density          radon_st_avg    "Radon short term average level [%d %unit%]"  { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:radon_st_avg" }
Number:Density          radon_lt_avg    "Radon long term average level [%d %unit%]"   { channel="bluetooth:airthings_wave_plus:adapter1:sensor1:radon_lt_avg" }
```
