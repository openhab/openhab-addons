# Listener Binding

Bluetooth extension that supports receiving broadcasted data.
These are manufacturer and service data broadcasted from Bluetooth device.
It is therefore possible to receive data from Bluetooth device without connecting to them or from non-connectable device.
This makes it different from other bindings that require a two-way connection for communication or are designed only for a specific device.
Listener binding can work with any device that spontaneously advertise its data.
To obtain data from such a device, however, it is necessary to know what device provides the data, respectively what services the device has implemented.
The nRF Connect Android app is ideal for debugging the data transmitted by the device and detecting the transmitted service UUIDs.

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

| Channel             | Type   | Read/Write | Description                 | User configurable |
|---------------------|--------|------------|-----------------------------|-------------------|
| rssi                | Number | R          | Signal strength             | N                 |
| advertise-interval  | Number | R          | Advertising interval        | N                 |
| manufacturer-number | Number | R          | Manufacturer numeric data   | Y                 |
| manufacturer-raw    | String | R          | Manufacturer raw data       | Y                 |
| service-number      | Number | R          | Service numeric data        | Y                 |
| service-raw         | String | R          | Service raw data            | Y                 |

### Manufacturer-number channels

Number value is returned.
It is be based on Bluetooth manufacturer data.
Manufacturer data is specific data that could advertise Bluetooth device.

| Parameter           | Default | Description |
|---------------------|---------|-------------|
| dataBegin           |         | Data begin in raw data byte array |
| dataLength          | 1       | Data length. Only values 1, 2, 4, 8 are allowed (for datatypes byte, short, integer and long respectively) |
| multiplyer          | 1       | Simple data conversion by value multiplication |
| payloadLength       | 0       | Expected payload length. Default is 0 that means no payload length check is provided. Otherwise only payload with specified length will be accepted |

### Manufacturer-raw channels

String value is returned.
Channel value represents all received data as string value.
It is based on Bluetooth manufacturer data.
Manufacturer data specific data that could advertise Bluetooth device.
It could be used to debug Bluetooth data.

| Parameter           | Default | Description |
|---------------------|---------|-------------|
| dataBegin           |         | Data begin in raw data byte array |
| dataLength          | 1       | Data length. If set to 0 the data will be loaded till the end |

### Service-number channels

Number value is returned.
It is be based on Bluetooth service data.

| Parameter           | Default | Description  |
|---------------------|---------|--------------|
| uuid                |         | UUID of the Bluetooth service |
| dataBegin           |         | Data begin in raw data byte array |
| dataLength          | 1       | Data length. Only values 1, 2, 4, 8 are allowed (for datatypes byte, short, integer and long respectively) |
| multiplyer          | 1       | Simple data conversion by value multiplication |
| payloadLength       | 0       | Expected payload length. Default is 0 that means no payload length check is provided. Otherwise only payload with specified length will be accepted |

### Service-raw channels

String value is returned.
Channel value represents all received data as string value.
It is based on Bluetooth service data.
It could be used to debug Bluetooth data.

| Parameter           | Default | Description  |
|---------------------|---------|--------------|
| uuid                |         | UUID of the Bluetooth service  |
| dataBegin           |         | Data begin in raw data byte array |
| dataLength          | 1       | Data length. If set to 0 the data will be loaded till the end |

## Full Example

### `demo.things` Example

```java
Bridge bluetooth:bluez:myBridge [ address="00:00:00:00:00:00" ]
Thing bluetooth:listener:myBridge:myDevice (bluetooth:bluez:myBridge) [ address="11:11:11:11:11:11", changeByteOrder="false" ] {
    Channels:
        Type service-number : temperature [ dataBegin="6", orderBigEndian="true", payloadLength="13", multiplyer="0.1", uuid="181a", dataLength="2" ]
        Type service-number : humidity [ dataBegin="8", orderBigEndian="true", payloadLength="13", multiplyer="0.1", uuid="181a", dataLength="2" ]
        Type service-raw : servicedata [ uuid="181a", dataBegin="0", dataLength="0" ]        
}
```

### `demo.items` Example

```java
Number temperature "Temperature"    { channel="bluetooth:listener:myBridge:myDevice:temperature" }
Number humidity    "Humidity"       { channel="bluetooth:listener:myBridge:myDevice:humidity" }
Number serviceData "Service 181a"   { channel="bluetooth:listener:myBridge:myDevice:servicedata" }
```
