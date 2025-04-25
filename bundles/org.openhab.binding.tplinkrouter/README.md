# tplinkrouter Binding

The tplinkrouter Binding allows monitoring and controlling TP-Link routers.

The binding uses a telnet connection to communicate with the router.

At the moment only wifi part is supported and `TD-W9970` is the only model tested.
This binding may work with other TP-Link router provided that they use the same telnet API.

## Supported Things

This binding provides only the `router` Thing.

## Thing Configuration

### `router` Thing Configuration

| Name            | Type    | Description                                   | Default | Required | Advanced |
|-----------------|---------|-----------------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device          | N/A     | yes      | no       |
| port            | integer | Port for telnet connection                    | 23      | no       | no       |
| username        | text    | Username to access the router (same as WebUI) | N/A     | yes      | no       |
| password        | text    | Password to access the device (same as WebUI) | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in sec.         | 60      | no       | yes      |

## Channels

| Channel               | Type   | Read/Write | Description                              |
|-----------------------|--------|------------|------------------------------------------|
| `wifi#status`         | Switch | RW         | State of the wifi                        |
| `wifi#ssid`           | String | R          | SSID of the wifi network                 |
| `wifi#bandwidth`      | String | R          | Bandwidth of the wifi network            |
| `wifi#qss`            | Switch | RW         | Quick Security Setup of the wifi network |
| `wifi#secMode`        | String | R          | Security Mode of the wifi network        |
| `wifi#authentication` | String | R          | Authentication Mode of the wifi network  |
| `wifi#encryption`     | String | R          | Encryption Mode of the wifi network      |
| `wifi#key`            | String | R          | Password of the wifi network             |

## Full Example

`.things` configuration file:

```java
Thing tplinkrouter:router:myRouter [hostname="192.168.0.1", username="admin", password="myPassword"]
```

`.items` configuration file:

```java
Switch Wifi "Wifi" <QualityOfService> {channel="tplinkrouter:router:myRouter:wifi#status", autoupdate="false"}
String WifiSSID "Wifi SSID" <QualityOfService> {channel="tplinkrouter:router:myRouter:wifi#ssid"}
String BandWidth "Wifi Bandwidth" <QualityOfService> {channel="tplinkrouter:router:myRouter:wifi#bandwidth"}
Switch QSS "Wifi QSS" <QualityOfService> {channel="tplinkrouter:router:myRouter:wifi#qss", autoupdate="false"}
String SecMode "Wifi Security Mode" <QualityOfService> {channel="tplinkrouter:router:myRouter:wifi#secMode"}
String Authentication "Wifi Authentication Mode" <QualityOfService> {channel="tplinkrouter:router:myRouter:wifi#authentication"}
String Encryption "Wifi Encryption Mode" <QualityOfService> {channel="tplinkrouter:router:myRouter:wifi#encryption"}
```
