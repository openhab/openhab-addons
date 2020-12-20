# TR-064 Binding

This binding brings support for internet gateway devices that support the TR-064 protocol.
It can be used to gather information from the device and/or re-configure it.

## Supported Things

Four thing types are supported:

- `generic`: the internet gateway device itself (generic device)
- `fritzbox`: similar to `generic` with extensions for AVM FritzBox devices
- `subDevice`: a sub-device of a `rootDevice` (e.g. a WAN interface) 
- `subDeviceLan`: a special type of sub-device that supports MAC-detection

## Discovery

The gateway device needs to be added manually.
After that, sub-devices are detected automatically.

## Thing Configuration

All thing types have a `refresh` parameter.
It sets the refresh-interval in seconds for each device channel.
The default value is 60.

### `generic`, `fritzbox`

The `host` parameter is required to communicate with the device.
It can be a hostname or an IP address.

For accessing the device you need to supply credentials.
If you only configured password authentication for your device, the `user` parameter must be skipped and it will default to `dslf-config`.
The second credential parameter is `password`, which is mandatory.
For security reasons it is highly recommended to set both, username and password.

### `fritzbox`

All additional parameters for `fritzbox` devices (i.e. except those that are shared with `generic`) are advanced parameters.

One or more TAM (telephone answering machines) are supported by most devices.
By setting the `tamIndices` parameter you can instruct the binding to add channels for these devices to the thing.
Values start with `0`.
This is an optional parameter and multiple values are allowed.

Most devices allow to configure call deflections.
If the `callDeflectionIndices` parameter is set, channels for the status of the pre-configured call deflections are added.
Values start with `0`, including the number of "Call Blocks" (two configured call-blocks -> first deflection is `2`).
This is an optional parameter and multiple values are allowed.

Most devices support call lists.
The binding can analyze these call lists and provide channels for the number of missed calls, inbound calls, outbound calls and rejected (blocked) calls.
The days for which this analysis takes place can be controlled with the `missedCallDays`, `rejectedCallDays`, `inboundCallDays`, `outboundCallDays` and `callListDays`.
This is an optional parameter and multiple values are allowed.

Since FritzOS! 7.20 WAN access of local devices can be controlled by their IPs.
If the `wanBlockIPs` parameter is set, a channel for each IP is created to block/unblock WAN access for this IP.
Values need to be IPv4 addresses in the format `a.b.c.d`.
This is an optional parameter and multiple values are allowed.

If the `PHONEBOOK` profile shall be used, it is necessary to retrieve the phonebooks from the FritzBox.
The `phonebookInterval` is uses to set the refresh cycle for phonebooks.

### `subdevice`, `subdeviceLan`

Besides the bridge that the thing is attached to, sub-devices have a `uuid` parameter.
This is the UUID/UDN of the device and a mandatory parameter.
Since the value can only be determined by examining the SCPD of the root device, the simplest way to get hold of them is through auto-discovery.

For `subdeviceLan` devices (type is detected automatically during discovery) the parameter `macOnline` can be defined.
It adds a channel for each MAC (format 11:11:11:11:11:11) that shows the online status of the respective device.
This is an optional parameter and multiple values are allowed.

## Channels

| channel                    | item-type                 | advanced | description                                                    |
|----------------------------|---------------------------|:--------:|----------------------------------------------------------------|
| `callDeflectionEnable`     | `Switch`                  |          | Enable/Disable the call deflection setup with the given index. |
| `callList`                 | `String`                  |     x    | A string containing the call list as JSON (see below)          |    
| `deviceLog`                | `String`                  |     x    | A string containing the last log messages                      |
| `dslCRCErrors`             | `Number:Dimensionless`    |     x    | DSL CRC Errors                                                 |
| `dslDownstreamNoiseMargin` | `Number:Dimensionless`    |     x    | DSL Downstream Noise Margin                                    |
| `dslDownstreamNoiseMargin` | `Number:Dimensionless`    |     x    | DSL Downstream Attenuation                                     |
| `dslEnable`                | `Switch`                  |          | DSL Enable                                                     |
| `dslFECErrors`             | `Number:Dimensionless`    |     x    | DSL FEC Errors                                                 |
| `dslHECErrors`             | `Number:Dimensionless`    |     x    | DSL HEC Errors                                                 |
| `dslStatus`                | `Switch`                  |          | DSL Status                                                     |
| `dslUpstreamNoiseMargin`   | `Number:Dimensionless`    |     x    | DSL Upstream Noise Margin                                      |
| `dslUpstreamNoiseMargin`   | `Number:Dimensionless`    |     x    | DSL Upstream Attenuation                                       |
| `inboundCalls`             | `Number`                  |     x    | Number of inbound calls within the given number of days.       |
| `macOnline`                | `Switch`                  |     x    | Online status of the device with the given MAC                 |
| `missedCalls`              | `Number`                  |          | Number of missed calls within the given number of days.        |
| `outboundCalls`            | `Number`                  |     x    | Number of outbound calls within the given number of days.      |
| `reboot`                   | `Switch`                  |          | Reboot                                                         |
| `rejectedCalls`            | `Number`                  |     x    | Number of rejected calls within the given number of days.      |
| `securityPort`             | `Number`                  |     x    | The port for connecting via HTTPS to the TR-064 service.       |
| `tamEnable`                | `Switch`                  |          | Enable/Disable the answering machine with the given index.     |
| `tamNewMessages`           | `Number`                  |          | The number of new messages of the given answering machine.     |
| `uptime`                   | `Number:Time`             |          | Uptime                                                         |
| `pppUptime`                | `Number:Time`             |          | Uptime (if using PPP)                                          |
| `wanAccessType`            | `String`                  |     x    | Access Type                                                    |
| `wanConnectionStatus`      | `String`                  |          | Connection Status                                              |
| `wanPppConnectionStatus`   | `String`                  |          | Connection Status (if using PPP)                               |
| `wanIpAddress`             | `String`                  |     x    | WAN IP Address                                                 |
| `wanPppIpAddress`          | `String`                  |     x    | WAN IP Address (if using PPP)                                  |
| `wanMaxDownstreamRate`     | `Number:DataTransferRate` |     x    | Max. Downstream Rate                                           |
| `wanMaxUpstreamRate`       | `Number:DataTransferRate` |     x    | Max. Upstream Rate                                             |
| `wanPhysicalLinkStatus`    | `String`                  |     x    | Link Status                                                    |
| `wanTotalBytesReceived`    | `Number:DataAmount`       |     x    | Total Bytes Received                                           |
| `wanTotalBytesSent`        | `Number:DataAmount`       |     x    | Total Bytes Sent                                               |
| `wifi24GHzEnable`          | `Switch`                  |          | Enable/Disable the 2.4 GHz WiFi device.                        |
| `wifi5GHzEnable`           | `Switch`                  |          | Enable/Disable the 5.0 GHz WiFi device.                        |
| `wifiGuestEnable`          | `Switch`                  |          | Enable/Disable the guest WiFi.                                 |

### Channel `callList`

Call lists are provided for one or more days (as configured) as JSON.
The JSON consists of an array of individual calls with the fields `date`, `type`, `localNumber`, `remoteNumber`, `duration`.
The call-types are the same as provided by the FritzBox, i.e. `1` (inbound), `2` (missed), `3` (outbound), `10` (rejected).
 
## `PHONEBOOK` Profile

The binding provides a profile for using the FritzBox phonebooks for resolving numbers to names.
The `PHONEBOOK` profile takes strings containing the number as input and provides strings with the caller's name, if found.

The parameter `thingUid` with the UID of the phonebook providing thing is a mandatory parameter.
If only a specific phonebook from the device should be used, this can be specified with the `phonebookName` parameter.
The default is to use all available phonebooks from the specified thing.
In case the format of the number in the phonebook and the format of the number from the channel are different (e.g. regarding country prefixes), the `matchCount` parameter can be used.
The configured `matchCount` is counted from the right end and denotes the number of matching characters needed to consider this number as matching.

