# TR-064 Binding

This binding brings support for internet gateway devices that support the TR-064 protocol (e.g. the AVM FritzBox family of routers).
It can be used to gather information from the device and/or re-configure it.
Even though textual configuration is possible, it is strongly recommended to use the Main User Interface for configuration.

## Supported Things

Two Bridge things are supported:

- `generic`: the internet gateway device itself (generic device)
- `fritzbox`: similar to `generic` with extensions for AVM FritzBox devices.

Two kind of Things are supported:

- `subDevice`: a sub-device of the Bridge thing (e.g. a WAN interface)
- `subDeviceLan`: a special type of sub-device that supports MAC-detection

## Discovery

The gateway device needs to be added manually.
After that, sub-devices should be detected automatically.
Otherwise go to "Things", click "+" to add a new thing, select the TR-064 binding and click the "Scan" button.

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

Another optional and advanced configuration parameter is `timeout`.
This parameter applies to all requests to the device (SOAP requests, phonebook retrieval, call lists, ...).
It only needs to be changed from the default value of `5` seconds when the remote device is unexpectedly slow and does not respond within that time.

### `fritzbox`

The `fritzbox` devices can give additional information in dedicated channels, controlled
 by additional parameters (visible if Show Advanced is selected), w.r.t. to `generic` devices.
If the parameters are specified, the corresponding channels will be added to the device.

One or more TAM (telephone answering machines) are supported by most fritzbox devices.
By setting the `tamIndices` parameter you can instruct the binding to add channels for these
 devices to the thing.
Values start with `0`.
This is an optional parameter and multiple values are allowed: add one value per line in the Main User Interface.

Most devices allow to configure call deflections.
If the `callDeflectionIndices` parameter is set, channels for the status of the pre-configured call deflections are added.
Values start with `0`, including the number of "Call Blocks" (two configured call-blocks -> first deflection is `2`).
This is an optional parameter and multiple values are allowed:  add one value per line in the Main User Interface.

Most devices support call lists.
The binding can retrieve these call lists and provide channels for the number of missed calls, inbound calls, outbound calls and rejected (blocked) calls,
for a given number of days. A channel is added to the Thing if such a number is set through the corresponding parameter
in the Main User Interface.
The parameters are: `missedCallDays`, `rejectedCallDays`, `inboundCallDays`, `outboundCallDays` and `callListDays`.

Since FritzOS! 7.20 WAN access of local devices can be controlled by their IPs.
If the `wanBlockIPs` parameter is set, a channel for each IP is created to block/unblock WAN access for this IP.
Values need to be IPv4 addresses in the format `a.b.c.d`.
This is an optional parameter and multiple values are allowed:  add one value per line in the Main User Interface.

If the `PHONEBOOK` profile shall be used, it is necessary to retrieve the phonebooks from the FritzBox.
The `phonebookInterval` is used to set the refresh cycle for phonebooks.
It defaults to 600 seconds, and it can be set to 0 if phonebooks are not used.

Some parameters (e.g. `macOnline`, `wanBlockIPs`) accept lists.
List items are configured one per line in the UI, or are comma separated values when using textual config.
These parameters that accept list can also contain comments.
Comments are separated from the value with a '#' (e.g. `192.168.0.77 # Daughter's iPhone`).
The full string is used for the channel label.

Two more advanced parameters are used for the backup thing action.
The `backupDirectory` is the directory where the backup files are stored.
The default value is the userdata directory.
The `backupPassword` is used to encrypt the backup file.
This is equivalent to setting a password in the UI.
If no password is given, the user password (parameter `password`) is used.

### `subdevice`, `subdeviceLan`

Additional information (i.e. channels) is available in subdevices of the bridge.
Each subdevice is characterized by a unique `uuid` parameter: this is the UUID/UDN of the device.
This is a mandatory parameter to be set in order to add the subdevice. Since the parameter value can only be determined
by examining the SCPD of the root device, the simplest way to obtain it is through auto-discovery.

Auto discovery may find several sub-devices, each one holding channels as described in the following.

The LAN sub-device, in particular, is also used for presence detection.
It therefore optionally contains a channel for each MAC address (in a format 11:11:11:11:11:11, different than the old v1 version of this binding), defined by the parameter `macOnline`.

## Channels

Channels are grouped according to the subdevice they belong to.

### `fritzbox` bridge channels

Advanced channels appear only if the corresponding parameters are set in the Thing definition.

| channel                    | item-type                 | advanced | description                                                    |
|----------------------------|---------------------------|:--------:|----------------------------------------------------------------|
| `callDeflectionEnable`     | `Switch`                  |     x    | Enable/Disable the call deflection setup with the given index. |
| `callList`                 | `String`                  |     x    | A string containing the call list as JSON (see below)          |
| `deviceLog`                | `String`                  |     x    | A string containing the last log messages                      |
| `missedCalls`              | `Number`                  |          | Number of missed calls within the given number of days.        |
| `outboundCalls`            | `Number`                  |     x    | Number of outbound calls within the given number of days.      |
| `inboundCalls`             | `Number`                  |     x    | Number of inbound calls within the given number of days.       |
| `reboot`                   | `Switch`                  |          | Reboot                                                         |
| `rejectedCalls`            | `Number`                  |     x    | Number of rejected calls within the given number of days.      |
| `securityPort`             | `Number`                  |     x    | The port for connecting via HTTPS to the TR-064 service.       |
| `tamEnable`                | `Switch`                  |     x    | Enable/Disable the answering machine with the given index.     |
| `tamNewMessages`           | `Number`                  |     x    | The number of new messages of the given answering machine.     |
| `uptime`                   | `Number:Time`             |          | Uptime of the device                                           |

Call lists are provided via the `callList` channel for one or more days (as configured) as JSON.
The JSON consists of an array of individual calls with the fields `date`, `type`, `localNumber`, `remoteNumber`, `duration`.
The call-types are the same as provided by the FritzBox, i.e. `1` (inbound), `2` (missed), `3` (outbound), `10` (rejected).

### LAN `subdeviceLan` channels

| channel              | item-type                 | advanced | description                                                                                                  |
|----------------------|---------------------------|:--------:|--------------------------------------------------------------------------------------------------------------|
| `wifi24GHzEnable`    | `Switch`                  |          | Enable/Disable the 2.4 GHz WiFi device. Deprecated for removal. Use `wifi1Enable`.                           |
| `wifi5GHzEnable`     | `Switch`                  |          | Enable/Disable the 5.0 GHz WiFi device. Deprecated for removal. Use `wifi2Enable`.                           |
| `wifiGuestEnable`    | `Switch`                  |          | Enable/Disable the guest WiFi. Deprecated for removal. Use `wifi3Enable`.                                    |
| `wifiXEnable`        | `Switch`                  |          | Enable/Disable the WiFi X. See below for details.                                                            |
| `macOnline`          | `Switch`                  |    x     | Online status of the device with the given MAC                                                               |
| `macOnlineIpAddress` | `String`                  |    x     | IP of the MAC (uses same parameter as `macOnline`)                                                           |
| `macSignalStrength1` | `Number`                  |    x     | Wifi Signal Strength of the device with the given MAC. This is set in case the Device is connected to 2.4Ghz |
| `macSpeed1`          | `Number:DataTransferRate` |    x     | Wifi Speed of the device with the given MAC. This is set in case the Device is connected to 2.4Ghz           |
| `macSignalStrength2` | `Number`                  |    x     | Wifi Signal Strength of the device with the given MAC. This is set in case the Device is connected to 5Ghz   |
| `macSpeed2`          | `Number:DataTransferRate` |    x     | Wifi Speed of the device with the given MAC. This is set in case the Device is connected to 5Ghz             |

_Note:_ The `wifi24GHzEnable`, `wifi5GHzEnable` and `wifiGuestEnable`channels have been deprecated and will be removed in future versions.
They are replaced by `wifiXEnable` (with `X` being a number between `1` and `4`).

- FritzBoxes which do not support 5 GHz use `wifi1Enable` for the standard WiFi and `wifi2Enable`for the guest WiFi.
- FritzBoxes which support 5 GHz use `wifi1Enable` for the 2.5 GHz WiFi, `wifi2Enable` for the 5 GHz WiFi and `wifi3Enable` for the guest WiFi.
- FritzBoxes which support two 5 GHz networks use `wifi1Enable` for the 2.5 GHz WiFi, `wifi2Enable` and `wifi3Enable` for the 5 GHz WiFis and `wifi4Enable` for the guest WiFi.

### WANConnection `subdevice` channels

| channel                    | item-type                 | advanced | description                                                    |
|----------------------------|---------------------------|:--------:|----------------------------------------------------------------|
| `Uptime`                   | `Number:Time`             |          | Uptime                                                         |
| `pppUptime`                | `Number:Time`             |          | Uptime (if using PPP)                                          |
| `wanConnectionStatus`      | `String`                  |          | Connection Status                                              |
| `wanPppConnectionStatus`   | `String`                  |          | Connection Status (if using PPP)                               |
| `wanIpAddress`             | `String`                  |    x     | WAN IP Address                                                 |
| `wanPppIpAddress`          | `String`                  |    x     | WAN IP Address (if using PPP)                                  |

### WAN `subdevice` channels

| channel                    | item-type                 | advanced | description                                                    |
|----------------------------|---------------------------|:--------:|----------------------------------------------------------------|
| `dslCRCErrors`             | `Number:Dimensionless`    |    x     | DSL CRC Errors                                                 |
| `dslDownstreamMaxRate`     | `Number:DataTransferRate` |    x     | DSL Max Downstream Rate                                        |
| `dslDownstreamCurrRate`    | `Number:DataTransferRate` |    x     | DSL Curr. Downstream Rate                                      |
| `dslDownstreamNoiseMargin` | `Number:Dimensionless`    |    x     | DSL Downstream Noise Margin                                    |
| `dslDownstreamAttenuation` | `Number:Dimensionless`    |    x     | DSL Downstream Attenuation                                     |
| `dslEnable`                | `Switch`                  |          | DSL Enable                                                     |
| `dslFECErrors`             | `Number:Dimensionless`    |    x     | DSL FEC Errors                                                 |
| `dslHECErrors`             | `Number:Dimensionless`    |    x     | DSL HEC Errors                                                 |
| `dslStatus`                | `String`                  |          | DSL Status                                                     |
| `dslUpstreamMaxRate`       | `Number:DataTransferRate` |    x     | DSL Max Upstream Rate                                          |
| `dslUpstreamCurrRate`      | `Number:DataTransferRate` |    x     | DSL Curr. Upstream Rate                                        |
| `dslUpstreamNoiseMargin`   | `Number:Dimensionless`    |    x     | DSL Upstream Noise Margin                                      |
| `dslUpstreamAttenuation`   | `Number:Dimensionless`    |    x     | DSL Upstream Attenuation                                       |
| `wanAccessType`            | `String`                  |    x     | Access Type                                                    |
| `wanMaxDownstreamRate`     | `Number:DataTransferRate` |    x     | Max. Downstream Rate                                           |
| `wanMaxUpstreamRate`       | `Number:DataTransferRate` |    x     | Max. Upstream Rate                                             |
| `wanCurrentDownstreamRate` | `Number:DataTransferRate` |    x     | Current Downstream Rate (average last 15 seconds)              |
| `wanCurrentUpstreamRate`   | `Number:DataTransferRate` |    x     | Current Upstream Rate (average last 15 seconds)                |
| `wanPhysicalLinkStatus`    | `String`                  |    x     | Link Status                                                    |
| `wanTotalBytesReceived`    | `Number:DataAmount`       |    x     | Total Bytes Received                                           |
| `wanTotalBytesSent`        | `Number:DataAmount`       |    x     | Total Bytes Sent                                               |

**Note:** AVM FritzBox devices use 4-byte-unsigned-integers for `wanTotalBytesReceived` and `wanTotalBytesSent`, because of that the counters are reset after around 4GB data.

## `PHONEBOOK` Profile

The binding provides a profile for using the FritzBox phonebooks for resolving numbers to names.
The `PHONEBOOK` profile takes strings containing the number as input and provides strings with the caller's name, if found.

The parameter `thingUid` with the UID of the phonebook providing thing is a mandatory parameter.
If only a specific phonebook from the device should be used, this can be specified with the `phonebookName` parameter.
The default is to use all available phonebooks from the specified thing.
In case the format of the number in the phonebook and the format of the number from the channel are different (e.g. regarding country prefixes), the `matchCount` parameter can be used.
The configured `matchCount` is counted from the right end and denotes the number of matching characters needed to consider this number as matching.
Negative `matchCount` values skip digits from the left (e.g. if the input number is `033998005671` a `matchCount` of `-1` would remove the leading `0` ).
A `matchCount` of `0` is considered as "match everything".
Matching is done on normalized versions of the numbers that have all characters except digits, '+' and '*' removed.
There is an optional configuration parameter called `phoneNumberIndex` that should be used when linking to a channel with item type `StringListType` (like `Call` in the example below), which determines which number to be picked, i.e. to or from.

## Rule Action

### Phonebook lookup

The phonebooks of a `fritzbox` thing can be used to lookup a number from rules via a thing action:

`String name = phonebookLookup(String number, String phonebook, int matchCount)`

`phonebook` and `matchCount` are optional parameters.
You can omit one or both of these parameters.
The configured `matchCount` is counted from the right end and denotes the number of matching characters needed to consider this number as matching.
Negative `matchCount` values skip digits from the left (e.g. if the input number is `033998005671` a `matchCount` of `-1` would remove the leading `0` ).
A `matchCount` of `0` is considered as "match everything" and is used as default if no other value is given.
As in the phonebook profile, matching is done on normalized versions of the numbers that have all characters except digits, '+' and '*' removed.
The return value is either the phonebook entry (if found) or the input number.

Example (use all phonebooks, match 5 digits from right):

```java
val tr064Actions = getActions("tr064","tr064:fritzbox:2a28aee1ee")
val result = tr064Actions.phonebookLookup("49157712341234", 5)
```

### Fritz!Box Backup

The `fritzbox` things can create configuration backups of the Fritz!Box.

The default configuration of the Fritz!Boxes requires 2-factor-authentication for creating backups.
If you see a `Failed to get configuration backup URL: HTTP-Response-Code 500 (Internal Server Error), SOAP-Fault: 866 (second factor authentication required)` warning, you need to disable 2-actor authentication.
But beware: depending on your configuration this might be a security issue.
The setting can be found under "System -> FRITZ!Box Users -> Login to the Home Network -> Confirm".

When executed, the action requests a backup file with the given password in the configured path.
The backup file is names as `ThingFriendlyName dd.mm.yyyy HHMM.export` (e.g. `My FritzBox 18.06.2021 1720.export`).
Files with the same name will be overwritten, so make sure that you trigger the rules at different times if your devices have the same friendly name.

```java
val tr064Actions = getActions("tr064","tr064:fritzbox:2a28aee1ee")
tr064Actions.createConfigurationBackup()
```

## A note on textual configuration

Textual configuration through a `.things` file is possible but, at present, strongly discouraged because it is significantly more error-prone
than the configuration through Main User Interface.

If an advanced user is really motivated to define a textual configuration, it is suggested to perform
an automatic scan through the user interface first in order to extract the required parameters (namely the different `uuid` of the
needed subdevices).

The definition of the bridge and of the subdevices things is the following

```java
Bridge tr064:fritzbox:rootuid "Root label" @ "location" [ host="192.168.1.1", user="user", password="passwd",
                                                         phonebookInterval="0"]{
    Thing subdeviceLan LAN "label LAN"   [ uuid="uuid:xxxxxxxx-xxxx-xxxx-yyyy-xxxxxxxxxxxx",
                                                macOnline="XX:XX:XX:XX:XX:XX",
                                                          "YY:YY:YY:YY:YY:YY"]
    Thing subdevice WAN "label WAN"               [ uuid="uuid:xxxxxxxx-xxxx-xxxx-zzzz-xxxxxxxxxxxx"]
    Thing subdevice WANCon "label WANConnection"  [ uuid="uuid:xxxxxxxx-xxxx-xxxx-wwww-xxxxxxxxxxxx"]
    }
```

The channel are automatically generated and it is simpler to use the Main User Interface to copy the textual definition of the channel

```java
Switch PresXX "[%s]" {channel="tr064:subdeviceLan:rootuid:LAN:macOnline_XX_3AXX_3AXX_3AXX_3AXX_3AXX"}
Switch PresYY "[%s]" {channel="tr064:subdeviceLan:rootuid:LAN:macOnline_YY_3AYY_3AYY_3AYY_3AYY_3AYY"}
```

Example `*.items` file using the `PHONEBOOK` profile for storing the name of a caller in an item. it matches 8 digits from the right of the "from" number (note the escaping of `:` to `_3A`):

```java
Call IncomingCallResolved "Caller name: [%s]" { channel="avmfritz:fritzbox:fritzbox:incoming_call" [profile="transform:PHONEBOOK", phonebook="tr064_3Afritzbox_3AfritzboxTR064", phoneNumberIndex="1", matchCount="8"] }
```
