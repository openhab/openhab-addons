# UniFi Binding

This binding integrates with [Ubiquiti UniFi Networks](https://www.ubnt.com/products/#unifi) allowing for presence detection of network clients.

## Supported Things

- `controller` - An instance of the UniFi controller software
- `site` - A site thing with connection statistics
- `wlan` - A wireless network thing. Control Wi-Fi network and easy access to access.
- `wirelessClient` - Any wireless client connected to a UniFi wireless network
- `wiredClient` - A wired client connected to the UniFi network
- `poePort` - A PoE (Power over Ethernet) port on a UniFi switch
- `accessPoint` - An access point managed by the UniFi controller software
- `network` - A network managed by the UniFi controller software.

## Discovery

The binding supports discovery of things connected to a UniFi controller (Bridge).
To discover things start the discovery process manually.

## Binding Configuration

The binding has no configuration options, all configuration is done at the Bridge and Thing levels.

## Bridge Configuration

You need at least one UniFi Controller (Bridge) for this binding to work.
It requires a network accessible instance of the [Ubiquiti Networks Controller Software](https://www.ubnt.com/download/unifi).

The following table describes the Bridge configuration parameters:

| Parameter      | Description                                                                 | Config   | Default |
|----------------|-----------------------------------------------------------------------------|--------- |---------|
| host           | Hostname of IP address of the UniFi Controller                              | Required | -       |
| port           | Port of the UniFi Controller. For UniFi OS, the default port is usually 443 | Required | -       |
| unifios        | If the UniFi Controller is running on UniFi OS                              | Required | false   |
| username       | The username to access the UniFi Controller                                 | Required | -       |
| password       | The password to access the UniFi Controller                                 | Required | -       |
| refresh        | Refresh interval in seconds                                                 | Optional | 10      |
| timeoutSeconds | Request timeout in seconds. Increase if you experience TimeoutExceptions    | Optional | 5       |

## Thing Configuration

You must define a UniFi Controller (Bridge) before defining UniFi Things for this binding to work.

### `site`

The following table describes the `site` configuration parameters:

| Parameter    | Description                                                  | Config   | Default |
| ------------ | -------------------------------------------------------------|--------- | ------- |
| sid          | The name, description or id of the site                      | Required | -       |

### `wlan`

The following table describes the `wlan` configuration parameters:

| Parameter    | Description                                                  | Config   | Default |
| ------------ | -------------------------------------------------------------|--------- | ------- |
| wid          | The name or id of the WLAN                                   | Required | -       |

### `wirelessClient` & `wiredClient`

The following table describes the `wirelessClient` & `wiredClient` configuration parameters:

| Parameter    | Description                                                  | Config   | Default |
| ------------ | -------------------------------------------------------------|--------- | ------- |
| cid          | The MAC address, IP address, hostname or alias of the client | Required | -       |
| site         | The site where the client should be found                    | Optional | -       |
| considerHome | The interval in seconds to consider the client as home       | Optional | 180     |

Here's some additional notes regarding the thing configuration parameters:

#### `cid`

The `cid` parameter is a universal "client identifier". It accepts the following values:

  1. MAC address [highest priority]
  1. IP address
  1. Hostname (as show by the controller)
  1. Alias (as defined by you in the controller UI) [lowest priority]

The priority essentially means the binding attempts to lookup by MAC address, then by IP address, then by hostname and finally by alias.
Once it finds a matching client, it short circuits and stops searching.
Most of the time, you will simply use the  MAC address.

#### `site`

The `site` parameter is optional. If you leave it blank, the client will appear `ONLINE` if found in _any_ site defined on the controller.

You may use the `site` parameter as a filter if you only want the client to appear home if it is found in the site defined in the `site` parameter.

Additionally, you may use friendly site names as they appear in the controller UI.

#### `considerHome`

The `considerHome` parameter allows you to control how quickly the binding marks a client as away.
For example, using the default of `180` (seconds), the binding will report a client away as soon as `lastSeen` + `180` (seconds) < `now`.

### `poePort`

The following table describes the `poePort` configuration parameters:

| Parameter  | Description                                               | Config   |
|------------|-----------------------------------------------------------|----------|
| portNumber | The port number as reported by the switch (starts with 1) | Required |
| macAddress | The MAC address of the switch device the port is part of  | Required |

### `accessPoint`

The following table describes the `accessPoint` configuration parameters:

| Parameter    | Description                                     | Config   | Default |
| ------------ | ------------------------------------------------|--------- | ------- |
| mac          | The MAC address of the access point             | Required | -       |
| site         | The site where the access point should be found | Optional | -       |

### `network`

The following table describes the `network` configuration parameters:

| Parameter    | Description                                                  | Config   | Default |
| ------------ | -------------------------------------------------------------|--------- | ------- |
| nid          | The id of the network                                        | Required | -       |

## Channels

### `site`

The `site` information that is retrieved is available as these channels:

| Channel ID            | Item Type | Description                                                            | Permissions |
|-----------------------|-----------|------------------------------------------------------------------------|-------------|
| totalClients          | Number    | Total number of clients connected                                      | Read        |
| wirelessClients       | Number    | Number of wireless clients connected                                   | Read        |
| wiredClients          | Number    | Number of wired clients connected                                      | Read        |
| guestClients          | Number    | Number of guest clients connected                                      | Read        |
| guestVoucher          | String    | Guest voucher for access through the guest portal                      | Read        |
| guestVouchersGenerate | String    | Generate additional guest vouchers for access through the guest portal | Write       |

The `guestVouchersGenerate` string channel is a command only channel that will trigger voucher creation.
It has configuration parameters to tailor the vouchers created:

| Parameter                | Description                                                                 | Config   | Default |
| ------------------------ | --------------------------------------------------------------------------- |--------- | ------- |
| voucherCount             | Number of vouchers to create                                                | Optional |    1    |
| voucherExpiration        | Minutes a voucher is valid after activation (default is 1 day)              | Optional |  1440   |
| voucherUsers             | Number of users for voucher, 0 for no limit                                 | Optional |    1    |
| voucherUpLimit           | Upload speed limit in kbps, no limit if not set                             | Optional |         |
| voucherDownLimit         | Download speed limit in kbps, no limit if not set                           | Optional |         |
| voucherDataQuota         | Data transfer quota in MB per user, no limit if not set                     | Optional |         |

### `wlan`

The `wlan` information that is retrieved is available as these channels:

| Channel ID      | Item Type | Description                                                                     | Permissions |
|-----------------|-----------|---------------------------------------------------------------------------------|-------------|
| enable          | Switch    | Enable status of the WLAN                                                       | Read, Write |
| wirelessClients | Number    | Number of wireless clients connected                                            | Read        |
| guestClients    | Number    | Number of guest clients connected                                               | Read        |
| essid           | String    | Wireless Network (ESSID)                                                        | Read        |
| site            | String    | UniFi Site the client is associated with                                        | Read        |
| security        | String    | Security protocol of the Wi-Fi network                                          | Read        |
| wlanBand        | String    | Wireless LAN band of the Wi-Fi network                                          | Read        |
| wpaEnc          | String    | WPA Encoding of the Wi-Fi network                                               | Read        |
| wpaMode         | String    | WPA Mode of the Wi-Fi network                                                   | Read        |
| passphrase      | String    | Passphrase of the Wi-Fi network                                                 | Read        |
| qrcodeEncoding  | String    | MECARD like encoding to generate a QR Code for easy access to the Wi-Fi network | Read        |

::: warning Attention
If you link an item to the `passphrase` or `qrcodeEncoding` channel your Wi-Fi password will be  exposed in openHAB.
The password will also be visible in openHAB event log.
:::

The `qrcodeEncoding` channel can be used to easily create a QR Code to access, for example, a guest network.
It contains a MECARD like representation of the access.
This is the notation used in QR Codes that can be scanned by mobile phones.

### `wirelessClient`

The `wirelessClient` information that is retrieved is available as these channels:

| Channel ID | Item Type            | Description                                                          | Permissions |
|------------|----------------------|----------------------------------------------------------------------|-------------|
| online     | Switch               | Online status of the client                                          | Read        |
| name       | String               | Name of device (from the controller web UI)                          | Read        |
| hostname   | String               | Hostname of device (from the controller web UI)                      | Read        |
| site       | String               | Site name (from the controller web UI) the client is associated with | Read        |
| macAddress | String               | MAC address of the client                                            | Read        |
| ipAddress  | String               | IP address of the client                                             | Read        |
| guest      | Switch               | On if this is a guest client                                         | Read        |
| ap         | String               | Access point (AP) the client is connected to                         | Read        |
| essid      | String               | Network name (ESSID) the client is connected to                      | Read        |
| rssi       | Number:Power         | Received signal strength indicator (RSSI) of the client              | Read        |
| uptime     | Number:Time          | Uptime of the client (in seconds)                                    | Read        |
| lastSeen   | DateTime             | Date and Time the client was last seen                               | Read        |
| experience | Number:Dimensionless | Overall health indication of the client (in percentage)              | Read        |
| blocked    | Switch               | Blocked status of the client                                         | Read, Write |
| cmd        | String               | Command channel: `reconnect` to force the client to reconnect        | Write       |
| reconnect  | Switch               | Force the client to reconnect                                        | Write       |

_Note: All channels with the Write permission require administrator credentials as defined in the controller._

### `wiredClient`

The `wiredClient` information that is retrieved is available as these channels:

| Channel ID | Item Type            | Description                                                          | Permissions |
|------------|----------------------|----------------------------------------------------------------------|-------------|
| online     | Switch               | Online status of the client                                          | Read        |
| name       | String               | Name of device (from the controller web UI)                          | Read        |
| hostname   | String               | Hostname of device (from the controller web UI)                      | Read        |
| site       | String               | Site name (from the controller web UI) the client is associated with | Read        |
| macAddress | String               | MAC address of the client                                            | Read        |
| ipAddress  | String               | IP address of the client                                             | Read        |
| uptime     | Number:Time          | Uptime of the client (in seconds)                                    | Read        |
| lastSeen   | DateTime             | Date and Time the client was last seen                               | Read        |
| experience | Number:Dimensionless | Overall health indication of the client (in percentage)              | Read        |
| blocked    | Switch               | Blocked status of the client                                         | Read, Write |

#### `blocked`

The `blocked` channel allows you to block / unblock a client via the controller.

#### `reconnect`

The `reconnect` channel allows you to force a client to reconnect.
Sending `ON` to this channel will trigger a reconnect via the controller.

### `poePort`

The `poePort` information that is retrieved is available as these channels:

| Channel ID | Item Type                | Description                                           | Permissions |
|------------|--------------------------|-------------------------------------------------------|-------------|
| online     | Switch                   | Online status of the port                             | Read        |
| mode       | Selection                | Select the PoE mode: off, auto, pasv24 or passthrough | Read, Write |
| enable     | Switch                   | Enable Power over Ethernet                            | Read, Write |
| cmd        | String                   | Command channel: `power-cycle`: Power Cycle port      | Write       |
| power      | Number:Power             | Power consumption of the port in Watt                 | Read        |
| voltage    | Number:ElectricPotential | Voltage of the port in Volt                           | Read        |
| current    | Number:ElectricCurrent   | Current used by the port in mA                        | Read        |

The `enable` switch channel has a configuration parameter `mode` which is the value used to switch PoE on when the channel is switched to ON.
The default mode value is `auto`.

### `accessPoint`

The `accessPoint` information that is retrieved is available as these channels:

| Channel ID | Item Type            | Description                                                          | Permissions |
|------------|----------------------|----------------------------------------------------------------------|-------------|
| online     | Switch               | Online status of the device                                          | Read        |
| enable     | Switch               | Enable or disable the access point                                   | Read, Write |
| name       | String               | Name of device (from the controller web UI)                          | Read        |
| site       | String               | Site name (from the controller web UI) the device is associated with | Read        |
| ipAddress  | String               | IP address of the device                                             | Read        |
| uptime     | Number:Time          | Uptime of the device (in seconds)                                    | Read        |
| lastSeen   | DateTime             | Date and Time the device was last seen                               | Read        |
| experience | Number:Dimensionless | The average health indication of the connected clients               | Read        |
| led        | Switch               | Switch the LED on or off                                             | Read, Write |

### `network`

The `network` information that is retrieved is available as these channels:

| Channel ID      | Item Type | Description                                                                     | Permissions |
|-----------------|-----------|---------------------------------------------------------------------------------|-------------|
| enable          | Switch    | Enable status of the network                                                    | Read, Write |
| totalClients    | Number    | Total number of clients connected                                               | Read        |
| site            | String    | UniFi Site the client is associated with                                        | Read        |
| purpose         | String    | Purpose of the network (e.g. Corporate, Guest, WAN, VPN, VLAN Only)             | Read        |

## Rule Actions

As an alternative to using the `guestVoucher` and `guestVouchersGenerate` channels on the `site` thing, it is possible to use rule actions on the thing to generate, revoke and list guest vouchers.
The following actions are available:

- `boolean success = generateVoucher(Integer expire, Integer users, Integer upLimit, Integer downLimit, Integer dataQuota)`
- `boolean success = generateVouchers(Integer count, Integer expire, Integer users, Integer upLimit, Integer downLimit, Integer dataQuota)`
- `boolean success = revokeVoucher(String voucherCode)`
- `boolean success = revokeVouchers(List<String> voucherCodes)`
- `boolean success = revokeAllVouchers()`
- `String vouchers = listVouchers()`

Since there is a separate rule action instance for each `site` thing, this needs to be retrieved through `getActions(scope, thingUID)`.
The first parameter always has to be `unifi` and the second is the full Thing UID of the site that should be used.
Once this action instance is retrieved, you can invoke the action method on it.

Boolean return values for the actions indicate success or failure.

The `generateVoucher(s)` actions parameters match the configuration parameters for the `guestVouchersGenerate` channel.
With the actions, these parameters can be controlled in a rule or script.
`null` values for the parameters are allowed, and will set the parameter to the default value.

| Parameter     | Description                                                                 | Default |
| ------------- | --------------------------------------------------------------------------- | ------- |
| count         | Number of vouchers to create                                                |    1    |
| expire        | Minutes a voucher is valid after activation (default is 1 day)              |  1440   |
| users         | Number of users for voucher, 0 for no limit                                 |    1    |
| upLimit       | Upload speed limit in kbps, no limit if not set                             |         |
| downLimit     | Download speed limit in kbps, no limit if not set                           |         |
| dataQuota     | Data transfer quota in MB per user, no limit if not set                     |         |

The `revoke...` actions allow you to revoke previously created vouchers.
The parameter is the voucher code or a list of voucher codes to be revoked.

The `listVouchers` action will return a json string representing the currently available vouchers for the site.
The json contains all parameters for the voucher, therefore it is possible to filter on these in a rule or script.
For example, one could retrieve all vouchers created before a certain time and use the `revokeVouchers` action to delete these.
The structure of the returned json is (depending on content, some fields may be missing):

```json
[
    {
        "code": "3867791284",
        "createTime": "2023-01-31T14:40:47Z",
        "duration": 1440,
        "quota": 2,
        "used": 0,
        "qosUsageQuota": 300,
        "qosRateMaxUp": 200,
        "qosRateMaxDown": 100,
        "qosOverwrite": true,
        "note": "I added a note when creating vouchers in the UniFi hotspot UI",
        "status": "VALID_MULTI"
    },
    {
        "code": "0021952641",
        "createTime": "2023-01-31T14:38:47Z",
        "duration": 1440,
        "quota": 1,
        "used": 0,
        "qosOverwrite": false,
        "status": "VALID_ONE"
    },
    { ... }
]
```

## Full Example

### `things/unifi.things`

```java
Bridge unifi:controller:home "UniFi Controller" [ host="unifi", port=8443, unifios=false, username="$username", password="$password", refresh=10 ] {
    Thing wirelessClient matthewsPhone "Matthew's iPhone" [ cid="$cid", site="default", considerHome=180 ]
    Thing site mysite "My Site" [ sid="$sid" ]
}
```

:::tip Note
Usually on Unifi OS, the default port is 443
:::

Replace `$user`, `$password`, `$cid` and `$sid` accordingly.

### `items/unifi.items`

```java
Switch       MatthewsPhone           "Matthew's iPhone [MAP(unifi.map):%s]"             { channel="unifi:wirelessClient:home:matthewsPhone:online" }
String       MatthewsPhoneSite       "Matthew's iPhone: Site [%s]"                      { channel="unifi:wirelessClient:home:matthewsPhone:site" }
String       MatthewsPhoneMAC        "Matthew's iPhone: MAC [%s]"                       { channel="unifi:wirelessClient:home:matthewsPhone:macAddress" }
String       MatthewsPhoneIP         "Matthew's iPhone: IP [%s]"                        { channel="unifi:wirelessClient:home:matthewsPhone:ipAddress" }
String       MatthewsPhoneAP         "Matthew's iPhone: AP [%s]"                        { channel="unifi:wirelessClient:home:matthewsPhone:ap" }
String       MatthewsPhoneESSID      "Matthew's iPhone: ESSID [%s]"                     { channel="unifi:wirelessClient:home:matthewsPhone:essid" }
Number:Power MatthewsPhoneRSSI       "Matthew's iPhone: RSSI [%d dBm]"                  { channel="unifi:wirelessClient:home:matthewsPhone:rssi" }
Number:Time  MatthewsPhoneUptime     "Matthew's iPhone: Uptime [%1$tR]"                 { channel="unifi:wirelessClient:home:matthewsPhone:uptime" }
DateTime     MatthewsPhoneLastSeen   "Matthew's iPhone: Last Seen [%1$tH:%1$tM:%1$tS]"  { channel="unifi:wirelessClient:home:matthewsPhone:lastSeen" }
Switch       MatthewsPhoneBlocked    "Matthew's iPhone: Blocked"                        { channel="unifi:wirelessClient:home:matthewsPhone:blocked" }
Switch       MatthewsPhoneReconnect  "Matthew's iPhone: Reconnect"                      { channel="unifi:wirelessClient:home:matthewsPhone:reconnect" }
```

### `transform/unifi.map`

```text
ON=Home
OFF=Away
```

### `sitemaps/unifi.sitemap`

```perl
sitemap unifi label="UniFi Binding"
{
    Frame {
        Text item=MatthewsPhone
        Text item=MatthewsPhoneSite
        Text item=MatthewsPhoneMAC
        Text item=MatthewsPhoneIP
        Text item=MatthewsPhoneAP
        Text item=MatthewsPhoneESSID
        Text item=MatthewsPhoneRSSI
        Text item=MatthewsPhoneUptime
        Text item=MatthewsPhoneLastSeen
        Switch item=MatthewsPhoneBlocked
        Switch item=MatthewsPhoneReconnect
    }
}
```

### `rule actions` for `site` thing

```java
val uniFiActions = getActions("unifi","unifi:site:home:mysite")
val success = uniFiActions.generateVoucher(null, null, null, 100, 500, 250)
```

```java
val uniFiActions = getActions("unifi","unifi:site:home:mysite")
val vouchersJson = uniFiActions.listVouchers()
```

```java
import java.util.List

val List<String> voucherList = newArrayList("38677-91284", "46415-36104")
val uniFiActions = getActions("unifi","unifi:site:home:mysite")
val success = uniFiActions.revokeVouchers(voucherList)
```
