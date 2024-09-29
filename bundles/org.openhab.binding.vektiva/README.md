# Vektiva Binding

This binding allows control of [Vektiva](https://vektiva.com) products.

## Supported Things

The only supported thing is the [SMARWI](https://vektiva.com/en/about-smarwi/how-it-works).

## Discovery

Automatic discovery is currently not supported by the Vektiva API.

## SMARWI Configuration

To manually add a SMARWI thing just enter the local network IP address of the device.
If you want to change the polling frequency of thing availability and status, please change the advanced parameter _refreshInterval_.
If you are running 203.2.4 or newer firmware you can enable the websockets support for better user experience by setting the _useWebSockets_ parameter.

## Channels

The exposed channels are:

| Name    | Type           | Description |
| ------- |:--------------:|:-----------:|
| control | Rollershutter  | It reacts to standard roller shutter commands _UP/DOWN/STOP_. The percentual closure (dimmer) is also supported - you can partially open window, but thing state is only open/close. |
| status  | String         | Shows the window status (Stopped, Moving, Not ready, Blocked) |

## Full Example

*.things:

```java
Thing vektiva:smarwi:5d43c74f [ ip="192.168.1.22", refreshInterval=30 ]
```

*.items

```java
Rollershutter Smarwi "Smarwi [%d %%]" { channel="vektiva:smarwi:5d43c74f:control" }
Dimmer SmarwiD "Smarwi [%.1f]" { channel="vektiva:smarwi:5d43c74f:control" }
String SmarwiStatus "Smarwi status [%s]" { channel="vektiva:smarwi:5d43c74f:status" }
```

*.sitemap

```perl
Default item=Smarwi
Default item=SmarwiD
Default item=SmarwiStatus
```

## Note

This binding currently does not support controlling via vektiva.online cloud service and uses local device API, which is described here: <https://vektiva.gitlab.io/vektivadocs/api/api.html>
