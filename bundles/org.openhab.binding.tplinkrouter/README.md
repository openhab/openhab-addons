# tplinkrouter Binding

The tplinkrouter Binding allows monitoring and controlling TP-Link routers.

The binding use a telnet connection to communicate with the router.

At the moment only wifi part is supported.

## Supported Things

This binding provides only the `TD-W9970` Thing since it's the only model tested.

This Thing may work with other TP-Link router provided that they use the same telnet API.

## Thing Configuration

### `TD-W9970` Thing Configuration

| Name            | Type    | Description                                   | Default | Required | Advanced |
|-----------------|---------|-----------------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device          | N/A     | yes      | no       |
| username        | text    | Username to access the router (same as WebUI) | N/A     | yes      | no       |
| password        | text    | Password to access the device (same as WebUI) | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in sec.         | 60      | no       | yes      |

## Channels

| Channel               | Type   | Read/Write | Description                              |
|-----------------------|--------|------------|------------------------------------------|
| `wifi#Status`         | Switch | RW         | State of the wifi                        |
| `wifi#SSID`           | String | R          | SSID of the wifi network                 |
| `wifi#bandWidth`      | String | R          | Bandwidth of the wifi network            |
| `wifi#QSS`            | Switch | RW         | Quick Security Setup of the wifi network |
| `wifi#SecMode`        | String | R          | Security Mode of the wifi network        |
| `wifi#authentication` | String | R          | Authentication Mode of the wifi network  |
| `wifi#encryption`     | String | R          | Encryption Mode of the wifi network      |
| `wifi#Key`            | String | R          | Password of the wifi network             |

## Full Example

`.things` configuration file:

```
Thing tplinkrouter:router:myRouter [hostname="192.168.0.1", username="admin", password="myPassword"]
```

`.items` configuration file:

```
Switch Wifi "Wifi" <QualityOfService> {channel="tplinkrouter:TD-W9970:myRouter:wifi#Status", autoupdate="false"}
Switch WifiSSID "Wifi SSID" <QualityOfService> {channel="tplinkrouter:TD-W9970:myRouter:wifi#SSID", autoupdate="false"}
String BandWidth "Wifi Bandwidth" <QualityOfService> {channel="tplinkrouter:TD-W9970:myRouter:wifi#Bandwidth", autoupdate="false"}
String QSS "Wifi QSS" <QualityOfService> {channel="tplinkrouter:TD-W9970:myRouter:wifi#QSS", autoupdate="false"}
String SecMode "Wifi Security Mode" <QualityOfService> {channel="tplinkrouter:TD-W9970:myRouter:wifi#SecMode", autoupdate="false"}
String Authentication "Wifi Authentication Mode" <QualityOfService> {channel="tplinkrouter:TD-W9970:myRouter:wifi#authentication", autoupdate="false"}
String Encryption "Wifi Encryption Mode" <QualityOfService> {channel="tplinkrouter:TD-W9970:myRouter:wifi#encryption", autoupdate="false"}
```
