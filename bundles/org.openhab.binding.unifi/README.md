# UniFi Binding

This binding integrates with [Ubiquiti UniFi Networks](https://www.ubnt.com/products/#unifi) allowing for presence detection of network clients.


## Supported Things

* `controller` - An instance of the UniFi controller software 
* `wirelessClient` - Any wireless client connected to a UniFi wireless network


## Discovery

Discovery is currently not supported.


## Binding Configuration
 
The binding has no configuration options, all configuration is done at the Bridge and Thing levels.

 
## Bridge Configuration

You need at least one UniFi Controller (Bridge) for this binding to work. It requires a network accessible instance of the [Ubiquiti Networks Controller Software](https://www.ubnt.com/download/unifi).    

The following table describes the Bridge configuration parameters:

| Parameter                | Description                                                                 | Config   | Default |
| ------------------------ | --------------------------------------------------------------------------- |--------- | ------- |
| host                     | Hostname of IP address of the UniFi Controller                              | Required | -       |
| port                     | Port of the UniFi Controller. For UniFi OS, the default port is usually 443 | Required | -       |
| unifios                  | If the UniFi Controller is running on UniFi OS                              | Required | false   |
| username                 | The username to access the UniFi Controller                                 | Required | -       |
| password                 | The password to access the UniFi Controller                                 | Required | -       |
| refresh                  | Refresh interval in seconds                                                 | Optional | 10      |

## Thing Configuration

You must define a UniFi Controller (Bridge) before defining UniFi Clients (Things) for this binding to work.

The following table describes the Thing configuration parameters:

| Parameter    | Description                                                  | Config   | Default |
| ------------ | -------------------------------------------------------------|--------- | ------- |
| cid          | The MAC address, IP address, hostname or alias of the client | Required | -       |
| site         | The site where the client should be found                    | Optional | -       |
| considerHome | The interval in seconds to consider the client as home       | Optional | 180     |

Here's some additional notes regarding the thing configuration parameters:

##### `cid`

The `cid` parameter is a universal "client identifier". It accepts the following values:

  1. MAC address [highest priority]
  1. IP address
  1. Hostname (as show by the controller)
  1. Alias (as defined by you in the controller UI) [lowest priority]
  
The priority essentially means the binding attempts to lookup by MAC address, then by IP address, then by hostname and finally by alias. Once it finds a matching client, it short circuits and stops searching. Most of the time, you will simply use the  MAC address.   

##### `site`

The `site` parameter is optional. If you leave it blank, the client will appear `ONLINE` if found in *any* site defined on the controller.

You may use the `site` parameter as a filter if you only want the client to appear home if it is found in the site defined in the `site` parameter.

Additionally, you may use friendly site names as they appear in the controller UI.

##### `considerHome`

The `considerHome` parameter allows you to control how quickly the binding marks a client as away. For example, using the default of `180` (seconds), the binding will report a client away as soon as `lastSeen` + `180` (seconds) < `now`


## Channels

The Wireless Client information that is retrieved is available as these channels:

| Channel ID | Item Type | Description                                                          | Permissions |
|------------|-----------|--------------------------------------------------------------------- | ----------- |
| online     | Switch    | Online status of the client                                          | Read        |
| site       | String    | Site name (from the controller web UI) the client is associated with | Read        |
| macAddress | String    | MAC address of the client                                            | Read        |
| ipAddress  | String    | IP address of the client                                             | Read        |
| ap         | String    | Access point (AP) the client is connected to                         | Read        |
| essid      | String    | Network name (ESSID) the client is connected to                      | Read        |
| rssi       | Number    | Received signal strength indicator (RSSI) of the client              | Read        |
| uptime     | Number    | Uptime of the wireless client (in seconds)                           | Read        |
| lastSeen   | DateTime  | Date and Time the wireless client was last seen                      | Read        |
| blocked    | Switch    | Blocked status of the client                                         | Read, Write |
| reconnect  | Switch    | Force the client to be reconnect                                     | Write       |

_Note: All channels with the Write permission require administrator credentials as defined in the controller._

##### `blocked`

The `blocked` channel allows you to block / unblock a client via the controller.

##### `reconnect`

The `reconnect` channel allows you to force a client to reconnect. Sending `ON` to this channel will trigger a reconnect via the controller.


## Full Example

things/unifi.things

```
Bridge unifi:controller:home "UniFi Controller" [ host="unifi", port=8443, unifios=false, username="$username", password="$password", refresh=10 ] {
	Thing wirelessClient matthewsPhone "Matthew's iPhone" [ cid="$cid", site="default", considerHome=180 ]
}
```

_Note: Usually on Unifi OS, the default port is 443_

Replace `$user`, `$password` and `$cid` accordingly.

items/unifi.items

```
Switch   MatthewsPhone           "Matthew's iPhone [MAP(unifi.map):%s]"             { channel="unifi:wirelessClient:home:matthewsPhone:online" }
String   MatthewsPhoneSite       "Matthew's iPhone: Site [%s]"                      { channel="unifi:wirelessClient:home:matthewsPhone:site" }
String   MatthewsPhoneMAC        "Matthew's iPhone: MAC [%s]"                       { channel="unifi:wirelessClient:home:matthewsPhone:macAddress" }
String   MatthewsPhoneIP         "Matthew's iPhone: IP [%s]"                        { channel="unifi:wirelessClient:home:matthewsPhone:ipAddress" }
String   MatthewsPhoneAP         "Matthew's iPhone: AP [%s]"                        { channel="unifi:wirelessClient:home:matthewsPhone:ap" }
String   MatthewsPhoneESSID      "Matthew's iPhone: ESSID [%s]"                     { channel="unifi:wirelessClient:home:matthewsPhone:essid" }
Number   MatthewsPhoneRSSI       "Matthew's iPhone: RSSI [%d]"                      { channel="unifi:wirelessClient:home:matthewsPhone:rssi" }
Number   MatthewsPhoneUptime     "Matthew's iPhone: Uptime [%d]"                    { channel="unifi:wirelessClient:home:matthewsPhone:uptime" }
DateTime MatthewsPhoneLastSeen   "Matthew's iPhone: Last Seen [%1$tH:%1$tM:%1$tS]"  { channel="unifi:wirelessClient:home:matthewsPhone:lastSeen" } 
Switch   MatthewsPhoneBlocked    "Matthew's iPhone: Blocked"                        { channel="unifi:wirelessClient:home:matthewsPhone:blocked" }
Switch   MatthewsPhoneReconnect  "Matthew's iPhone: Reconnect"                      { channel="unifi:wirelessClient:home:matthewsPhone:reconnect" }
```

transform/unifi.map

```
ON=Home
OFF=Away
```

sitemaps/unifi.sitemap

```
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
