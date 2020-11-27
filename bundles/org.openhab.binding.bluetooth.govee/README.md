# Govee

This extension adds support for [Govee](https://www.govee.com/) Bluetooth Devices. 

## Supported Things

Only two thing types are supported by this extension at the moment. More may be added later.

| Thing Type ID | Description                                     |
|---------------|-------------------------------------------------|
| govee_h5051   | Govee Wi-Fi Temperature Humidity Monitor        |
| govee_h5052   | Govee Temperature Humidity Monitor              |
| govee_h5071   | Govee Temperature Humidity Monitor              |
| govee_h5072   | Govee Temperature Humidity Monitor              |
| govee_h5074   | Govee Mini Temperature Humidity Monitor         |
| govee_h5075   | Govee Temperature Humidity Monitor              |

## Discovery

As any other Bluetooth device, Govee devices are discovered automatically by the corresponding bridge. 

## Thing Configuration

There is only a single configuration parameter `address`, which corresponds to the Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX").

## Channels

A Govee Bluetooth device has the following channels:

| Channel ID    | Item Type              | Description                        |
|---------------|------------------------|------------------------------------|
| temperature   | Number:Temperature     | The measured temperature           |
| humidity      | Number:Dimensionless   | The measured humidity              |
| battery       | Number:Dimensionless   | The measured battery               |

## Example

demo.things:

```
bluetooth:govee_h5074:hci0:beacon  "Govee Temperature Humidity Monitor" (bluetooth:bluez:hci0) [ address="12:34:56:78:9A:BC" ]
```

demo.items:

```
Number:Temperature      temperature "Room Temperature [%.1f %unit%]" { channel="bluetooth:govee_h5074:hci0:beacon:temperature" }
Number:Dimensionless    humidity    "Humidity [%.0f %unit%]"         { channel="bluetooth:govee_h5074:hci0:beacon:humidity" }
Number:Dimensionless    battery    "Battery [%.0f %unit%]"         { channel="bluetooth:govee_h5074:hci0:beacon:battery" }
```
