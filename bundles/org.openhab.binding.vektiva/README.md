# Vektiva Binding

This binding allows control of [Vektiva](https://vektiva.com) products.

## Supported Things

The only supported Thing is [SMARWI](https://vektiva.com/en/about-smarwi/how-it-works).

## Discovery

Automatic discovery is currently not supported by the Vektiva API.

## SMARWI Configuration

To manually add a SMARWI Thing, enter the device’s local IP address (or hostname).
To change the polling frequency for availability and status, adjust the advanced parameter `refreshInterval` (seconds).
If your device runs firmware 203.2.4 or newer, you can enable WebSockets for faster updates by setting `useWebSockets` to true.

## Channels

The exposed channels are:

| Name    | Type          | Description                                                                                                                                                               |
|---------|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| control | Rollershutter | Reacts to standard roller shutter commands (UP/DOWN/STOP). Percentage position is supported (you can partially open the window), but the Thing state is only open/closed. |
| status  | String        | Window status (Stopped, Moving, Not ready, Blocked)                                                                                                                       |

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
