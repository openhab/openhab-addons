# CUL Binding

The aim of this binding is to allow the connection from openHAB to a
using the [CUL USB dongle](http://busware.de/tiki-index.php?page=CUL) 

## Supported Things

This binding support 6 different things types

| Thing          | Type   | Description                                                                                                        |
|----------------|--------|--------------------------------------------------------------------------------------------------------------------|
| cul_max_bridge | Bridge | CUL if using a serial CUL
| cun_max_bridge | Bridge | CUN if using a network CUL

## Discovery

CUL has no discovery feature

## Binding Configuration

There are no binding wide settings as all configuration settings.
When using a serial port, you may need to add `-Dgnu.io.rxtx.SerialPorts=/dev/ttyACM0` in your server startup.  Please consult the [forum](https://community.openhab.org) for the latest information.

## Thing Configuration

### CUL Bridge Configuration

| Property | Default | Required | Description |
|----------|---------|:--------:|-------------|
| device   |         |   Yes    | in the form `<device>`, where `<device>` is a local serial port eg `/dev/ttyACM0` |
| baudrate |         |   No     | one of 75, 110, 300, 1200, 2400, 4800, 9600, 19200, **38400**, 57600, 115200 |
| parity   |         |   No     | one of EVEN, ODD, MARK, **NONE**, SPACE |

When using a serial port, you may need to add `-Dgnu.io.rxtx.SerialPorts=/dev/ttyACM0` in your server startup.  Please consult the [forum](https://community.openhab.org) for the latest information.

### CUN Bridge Configuration

| Property      | Default | Required | Description |
|---------------|---------|:--------:|-------------|
| networkPath   |         |   Yes    | in the form `<device>`, where `<device>` is `<host>:<port>`, where `<host>` is the host name or IP address and `<port>` is the port number. If no `<port>` is provided the default `2323` is assumed |

## Channels

Depending on the thing it supports different Channels

| Channel Type ID | Item Type          | Description                                                                                                                                                                                                                                               | Available on thing                                                    |
|-----------------|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| credits         | Number             | This channel indicates the remaining credits. Due to regulatory compliance reasons in Europe, the cul is allowed to send at no more than 1% of the time, which sums up to 36 seconds per hour. Once the threshold has been reached, the cul stops sending commands for the remaining time of the hour.  | cul_max_bridge, cun_max_bridge |
| led             | Switch             | Turns on / off the led on the CUL. This channel is write only and may indicate false values after restart as it assume the led is always after restart | cul_max_bridge, cun_max_bridge |

