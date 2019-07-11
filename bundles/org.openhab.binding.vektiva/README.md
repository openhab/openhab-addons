# Vektiva Binding

This binding allows control of Vektiva products. (http://vektiva.com)

## Supported Things

The currently supported thing is Smarwi (https://vektiva.com/smarwi)

## Discovery

The automatic discovery is not currently supported by thing's API.

## Binding Configuration

This binding does not require specific configuration.

## Smarwi Configuration

To manually add a Smarwi thing just enter the local network IP address of the device. 
If you want to change the polling frequency of thing availability and status, please change the advanced parameter _refreshInterval_. 
If you are running 203.2.4 or newer firmware you can enable the websockets support for better user experience by setting the _useWebSockets_ parameter.

## Channels

The exposed channels are :

| name | type | descripton |
| --- |:---:|:-----:|
| control |  Rollershutter | It reacts to standard roller shutter commands _UP/DOWN/STOP_. The percentual closure (dimmer) is also supported - you can partially open window, but thing state is only open/close. |
| status | String | Shows the window status (Stopped, Moving, Not ready, Blocked) |

## Full Example

*.things:

```
Thing vektiva:smarwi:5d43c74f [ ip="192.168.1.22", refreshInterval=30 ]
```

*.items

```
Rollershutter Smarwi "Smarwi [%d %%]" { channel="vektiva:smarwi:5d43c74f:control" }
Dimmer SmarwiD "Smarwi [%.1f]" { channel="vektiva:smarwi:5d43c74f:control" }
String SmarwiStatus "Smarwi status [%s]" { channel="vektiva:smarwi:5d43c74f:status" }
```

*.sitemap

```
Default item=Smarwi
Default item=SmarwiD
Default item=SmarwiStatus
```

## Note

This binding currently does not support controlling via vektiva.online cloud service and uses local device API, which is described here: https://vektiva.gitlab.io/vektivadocs/api/api.html
