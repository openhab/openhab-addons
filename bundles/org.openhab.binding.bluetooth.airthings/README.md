# Airthings Wave Plus

This extension adds support for [Airthings Wave Plus](https://www.airthings.com) indoor air quality monitor with radon detection. 

## Supported Things

Only a single thing type is added by this extension:

| Thing Type ID       | Description               |
| ------------------- | ------------------------- |
| airthings_wave_plus | Airthings Wave Plus       |


## Discovery

As any other Bluetooth device, Airthings Wave Plus devices are discovered automatically by the corresponding bridge. 

## Thing Configuration

Supported configuration parameters for `Airthings Wave Plus`:

| Property                        | Type    | Default | Required | Description                                                     |
|---------------------------------|---------|---------|----------|-----------------------------------------------------------------|
| address                         | String  |         | Yes      | Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX") |
| refreshInterval                 | Integer | 300     | No       | How often a refresh shall occur in seconds                      |

## Channels

Following channels are supported for `Airthings Wave Plus`:

| Channel ID         | Item Type                | Description                                |
| ------------------ | ------------------------ | ------------------------------------------ |
| temperature        | Number:Temperature       | The measured temperature                   |
| humidity           | Number:Dimensionless     | The measured humidity                      |
| pressure           | Number:Pressure          | The measured air pressure                  |
| co2                | Number:Dimensionless     | The measured CO2 level                     |
| voc                | Number                   | The measured VOC level                     |
| radon_st_avg       | Number                   | The measured Radon shor term average level |
| radon_lt_avg       | Number                   | The measured Radon long term average level |

## Example

demo.things:

```
```

demo.items:

```
```
