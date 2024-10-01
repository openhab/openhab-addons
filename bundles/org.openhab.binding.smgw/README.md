# PPC SMGW Binding

The PPC SMGW binding adds support for PPC Smart Meter Gateways.
The gateway is commonly installed by the network operator to allow remote access to a smart meter.
It also provides a HAN (home area network) interface for local access.

To use the HAN interface you need to connect it to your local network with an ethernet cable.

## Supported Things

- `smgw`: A smart meter gateway device.

## Thing Configuration

### `smgw` Thing Configuration

| Name       | Type | Description                          | Default        | Required | Advanced |
|------------|------|--------------------------------------|----------------|----------|----------|
| `hostname` | text | Hostname or IP address of the device | `192.168.1.200 | no       | no       |
| `username` | text | Username to access the device        | N/A            | yes      | no       |
| `password` | text | Password to access the device        | N/A            | yes      | no       |

The default value for the hostname matches the default value according to PPC's documentation.
Check with your network operator's documentation if DHCP has been enabled or a different fixed address has been set.

Username and password are typically supplied by the network operator.
Login with certificate is not supported.

## Channels

| Channel     | Type          | Read/Write | Description                                    |
|-------------|---------------|------------|------------------------------------------------|
| `meter`     | Number:Energy | R          | The meter reading of the smart meter.          |
| `timestamp` | DateTime      | R          | The date and time for which the meter reading. |

Channels are refreshed every 900s.
