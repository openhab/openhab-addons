# Listener Binding

Bluetooth extension that supports receiving broadcasted data.
These are manufacturer and service data broadcasted from Bluetooth device.
It is therefore possible to receive data from Bluetooth device without connecting to them or from non-connectable device.

## Supported Things

| Thing Type ID   | Description                |
| --------------- | -------------------------- |
| listener        | Bluetooth broadcast device |

The binding uses one of the Bluetooth bridges, which needs to be configured first.

## Discovery

No discovery is implemented.

## Thing Configuration

| Parameter           | Required | Default | Description                                                                    |
|---------------------|----------|---------|--------------------------------------------------------------------------------|
| address             | yes      |         | The address of the bluetooth device (in format "XX:XX:XX:XX:XX:XX")            |
| changeByteOrder     | no       | false   | For number channels with length 2, 4 or 8 bytes byte order could be changed. Default (false) order is little endian. |
| autoChannelCreation | no       | false   | Enable automatic channel creation from received service and manufacturer data |
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

## Full Example

### Thing Configuration

```java
Bridge bluetooth:bluez:abc [ address="00:00:00:00:00:00" ]
Thing bluetooth:bluez:abc:device (bluetooth:bluez:abc) [ address="11:11:11:11:11:11", changeByteOrder="false" ] {
    Channels:
        Type number : temperature [ dataBegin="6", orderBigEndian="true", payloadLength="13", multiplicator="0.1", uuid="181a", dataLength="2" ]
        Type number : humidity [ dataBegin="8", orderBigEndian="true", payloadLength="13", multiplicator="0.1", uuid="181a", dataLength="2" ]
}
```

### Item Configuration

```java
Number temperature "Temperature"    { channel="bluetooth:bluez:abc:device:temperature" }
Number humidity    "Humidity"       { channel="bluetooth:bluez:abc:device:humidity" }
```
