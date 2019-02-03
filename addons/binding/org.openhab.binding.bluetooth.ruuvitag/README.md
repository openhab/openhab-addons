# Ruuvi Tag

This extension adds support for [Ruuvi Tag](http://www.ruuvitag.com/) Sensor Beacons. 

## Supported Things

Only a single thing type is added by this extension:

| Thing Type ID   | Description               |
| --------------- | ------------------------- |
| ruuvitag_beacon | A Ruuvi Tag Sensor Beacon |

## Discovery

As any other Bluetooth device, Ruuvi Tag Beacons are discovered automatically by the corresponding bridge. 

## Thing Configuration

There is only a single configuration parameter `address`, which corresponds to the Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX").

## Channels

A Ruuvi Tag Smart Beacon has the following channels:

| Channel ID                  | Item Type                | Description                    |
| --------------------------- | ------------------------ | ------------------------------ |
| temperature                 | Number:Temperature       | The measured temperature       |
| humidity                    | Number:Dimensionless     | The measured humidity          |
| pressure                    | Number:Pressure          | The measured air pressure      |
| battery_voltage             | Number:ElectricPotential | The measured battery voltage   |
| accelerationx               | Number:Acceleration      | The measured acceleration of X |
| accelerationy               | Number:Acceleration      | The measured acceleration of Y |
| accelerationz               | Number:Acceleration      | The measured acceleration of Z |
| tx_power                    | Number:Power             | TX power                       |
| data_format                 | Number                   | Data format version            |
| measurement_sequence_number | Number:Dimensionless     | Measurement sequence number    |
| movement_counter            | Number:Dimensionless     | Movement counter               |

Note: not all channels are always available. Available fields depends on [Ruuvi Data Format](https://github.com/ruuvi/ruuvi-sensor-protocols).

## Example

demo.things:

```
bluetooth:ruuvitag:hci0:beacon  "RuuviTag Sensor Beacon" (bluetooth:bluez:hci0) [ address="12:34:56:78:9A:BC" ]
```

demo.items:

```
Number:Temperature      temperature "Room Temperature [%.1f %unit%]" { channel="bluetooth:ruuvitag:hci0:beacon:temperature" }
Number:Dimensionless    humidity    "Humidity [%.0f %unit%]"         { channel="bluetooth:ruuvitag:hci0:beacon:humidity" }
Number:Pressure         pressure    "Air Pressure [%.0f %unit%]"     { channel="bluetooth:ruuvitag:hci0:beacon:pressure" }
```
