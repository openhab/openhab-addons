# Listener Binding

Bluetooth extension that supports receiving broadcasted data. 
These are manufacturer and service data broadcasted from bluetooth device.
It is therefore possible to receive data from bluetooth device without connecting to them or from non-connectable device.

## Supported Things

| Thing Type ID   | Description                |
| --------------- | -------------------------- |
| listener        | Bluetooth broadcast device |

## Thing Configuration

| Parameter           | Required | Default | Description                                                                    |
|---------------------|----------|---------|--------------------------------------------------------------------------------|
| address             | yes      |         | The address of the bluetooth device (in format "XX:XX:XX:XX:XX:XX")            |
| changeByteOrder     | no       | false   | For number channels with length 2, 4 or 8 bytes byte order could be changed. Default (false) order is little endian. |
| autoChannelCreation | no       | false   | Enable automatic channnel creation from received service and manufacturer data |
| dataTimeout         | no       | 1       | Maximum time in minutes before a communication error is raised                 |

## Channels

All channels are read-only. 

| Channel             | Type   | Read/Write | Description                 |
|---------------------|--------|------------|-----------------------------|
| rssi                | Number | R          | Signal strength             |
| advertise-interval  | Number | R          | Advertising interval        |
| manufacturer-number | Number | R          | Manufacturer numeric data   |
| manufacturer-raw    | String | R          | Manufacturer raw data       |
| service-number      | Number | R          | Service numeric data        |
| service-raw         | String | R          | Service raw data            |
