# Mikrotik RouterOS Binding

This binding integrates [Mikrotik](https://mikrotik.com/) [RouterOS](https://help.mikrotik.com/docs/display/ROS/RouterOS)
[devices](https://mikrotik.com/products) allowing monitoring of system resources, network interfaces and WiFi clients.

## Supported Things

- `routeros` - An instance of the RouterOS device connection
- `interface` - A network interface inside RouterOS device
- `wifiRegistration` - Any wireless client connected to a RouterOS wireless network (regular or CAPsMAN-managed)

## Discovery

Discovery is currently not supported, but may be implemented in future versions.

## Bridge Configuration

To use this binding you need at least one RouterOS-powered device (Bridge) accessible to the host running
openHAB via network.
Make sure your RouterOS has the API enabled by visiting [<kbd>IP -> Services</kbd>](https://wiki.mikrotik.com/wiki/Manual:IP/Services)
configuration section in
[WinBox](https://wiki.mikrotik.com/wiki/Manual:Winbox).
Take note of the API port number as you'll need it below.
[SSL API connection](https://wiki.mikrotik.com/wiki/Manual:API-SSL) is not yet supported by this binding.
To connect to the RouterOS API, you will need to provide user credentials for the bridge thing.
You may use your current credentials that you use to manage your devices, but it is highly recommended to **create a read-only RouterOS user** since this binding only need to read data from the device.
To do this, proceed to <kbd>System -> Users</kbd> configuration section and add a user to the `read` group.

> Thing type: `routeros`

The RouterOS Bridge configuration parameters are:

| Name | Type | Required | Default | Description |
|---|---|---|---|---|
| host | text | Yes | 192.168.88.1 | Hostname or IP address of the RouterOS device |
| port | integer | No | 8728 | API Port number of the RouterOS device |
| login | text | Yes | admin | The username to access the the RouterOS device |
| password | text | Yes |  | The user password to access the RouterOS device |
| refresh | integer | No | 10 | The refresh interval in seconds to poll the RouterOS device |

**All things provided by this binding require a working bridge to be set up.**

### Bridge Channels

| Channel | Type | Description | Comment |
|---|---|---|---|
| freeSpace | Number:DataAmount | Amount of free storage left on device in bytes |  |
| totalSpace | Number:DataAmount | Amount of total storage available on device in bytes |  |
| usedSpace | Number:Dimensionless | Percentage of used device storage space |  |
| freeMemory | Number:DataAmount | Amount of free memory left on device in bytes |  |
| totalMemory | Number:DataAmount | Amount of total memory available on device in bytes |  |
| usedMemory | Number:Dimensionless | Percentage of used device memory |  |
| cpuLoad | Number:Dimensionless | CPU load percentage |  |
| upSince | DateTime | Time when thing got up |  |

## WiFi Client Thing Configuration

> Thing type: `wifiRegistration`

Represents a wireless client connected to a RouterOS wireless network (direct or CAPsMAN-managed).

The WiFi client thing configuration parameters are:

| Name | Type | Required | Default | Description |
|---|---|---|---|---|
| mac | text | Yes |  | WiFi client MAC address |
| ssid | text | No |  | Constraining SSID for the WiFi client (optional). If client will connect to another SSID, this thing will stay offline until client reconnects to specified SSID. |
| considerContinuous | integer | No | 180 | The interval in seconds to treat the client as connected permanently |

### WiFi client Thing Channels

| Channel | Type | Description | Comment |
|---|---|---|---|
| macAddress | String | MAC address of the client or interface |  |
| comment | String | User-defined comment |  |
| connected | Contact | Reflects connected or disconnected state |  |
| continuous | Contact | Connection is considered long-running |  |
| ssid | String | Wireless Network (SSID) the wireless client is connected to |  |
| interface | String | Network interface name |  |
| signal | system.signal-strength | Signal strength (RSSI) |  |
| upSince | DateTime | Time when thing got up |  |
| lastSeen | DateTime | Time of when the client was last seen connected |  |
| txRate | Number:DataTransferRate | Rate of data transmission in megabits per second |  |
| rxRate | Number:DataTransferRate | Rate of data receiving in megabits per second |  |
| txPacketRate | Number | Rate of data transmission in packets per second |  |
| rxPacketRate | Number | Rate of data receiving in packets per second |  |
| txBytes | Number:DataAmount | Amount of bytes transmitted |  |
| rxBytes | Number:DataAmount | Amount of bytes received |  |
| txPackets | Number | Amount of packets transmitted |  |
| rxPackets | Number | Amount of packets received |  |

## Network Interface Thing Configuration

> Thing type: `interface`

Represents a network interface from RouterOS system (ethernet, wifi, vpn, etc.)
At the moment the binding supports the following RouterOS interface types:

- `ether`
- `bridge`
- `wlan`
- `cap`
- `pppoe-out`
- `ppp-out`
- `lte`
- `l2tp-in`
- `l2tp-out`

The interface thing configuration parameters are:

### Interface Thing Configuration

| Name | Type | Required | Default | Description |
|---|---|---|---|---|
| name | text | Yes |  | RouterOS Interface name (i.e. ether1) |

### Interface Thing Channels

Please note that different on RouterOS interfaces has different data available depending on the kind of interface.
While the common dataset is same, some specific information for specific interface type may be missing. This may
be improved in future binding versions.

Common for all kinds of interfaces:

| Channel | Type | Description | Comment |
|---|---|---|---|
| type | String | Network interface type |  |
| name | String | Network interface name |  |
| comment | String | User-defined comment |  |
| macAddress | String | MAC address of the client or interface |  |
| enabled | Switch | Reflects enabled or disabled state |  |
| connected | Contact | Reflects connected or disconnected state |  |
| lastLinkDownTime | DateTime | Last time when link went down |  |
| lastLinkUpTime | DateTime | Last time when link went up |  |
| linkDowns | Number | Amount of link downs |  |
| txRate | Number:DataTransferRate | Rate of data transmission in megabits per second |  |
| rxRate | Number:DataTransferRate | Rate of data receiving in megabits per second |  |
| txPacketRate | Number | Rate of data transmission in packets per second |  |
| rxPacketRate | Number | Rate of data receiving in packets per second |  |
| txBytes | Number:DataAmount | Amount of bytes transmitted |  |
| rxBytes | Number:DataAmount | Amount of bytes received |  |
| txPackets | Number | Amount of packets transmitted |  |
| rxPackets | Number | Amount of packets received |  |
| txDrops | Number | Amount of packets dropped during transmission |  |
| rxDrops | Number | Amount of packets dropped during receiving |  |
| txErrors | Number | Amount of errors during transmission |  |
| rxErrors | Number | Amount of errors during receiving |  |
| defaultName | String | Interface factory name | Populated only for `ether` interfaces |
| rate | String | Ethernet link rate | Populated only for `ether` interfaces |
| state | String | WiFi interface state |  |
| registeredClients | Number | Amount of clients registered to WiFi interface | Populated only for `cap` interfaces |
| authorizedClients | Number | Amount of clients authorized by WiFi interface | Populated only for `cap` interfaces |
| upSince | DateTime | Time when thing got up | Populated only for `cap` interfaces |

## Text Configuration Example

**Change config options accordingly.**

### things/mikrotik.things

```java
Bridge mikrotik:routeros:rb1 "My RouterBoard" [ host="192.168.0.1", port=8728, login="openhab", password="thatsasecret", refresh=10 ] {
 Thing interface eth1 "Eth1" [ name="ether1" ]
 Thing interface eth2 "Eth2" [ name="ether2-wan1" ]
 Thing interface cap1 "Cap1" [ name="cap5" ]
 Thing interface ppp1 "PPPoE1" [ name="isp-pppoe" ]
 Thing interface tun1 "L2TPSrv1" [ name="l2tp-parents" ]
 Thing wifiRegistration wifi1 "Phone1" [ mac="F4:60:E2:C5:47:94", considerContinuous=60 ]
 Thing wifiRegistration wifi2 "Tablet2" [ mac="18:1D:EA:A5:A2:9E" ]
}
```

### items/mikrotik.items

```java
Group gRB1 "RB3011 System"
Number:DataAmount   My_RB_3011_Free_Space     "Free space"     (gRB1) {channel="mikrotik:routeros:rb1:freeSpace"}
Number:DataAmount   My_RB_3011_Total_Space    "Total space"    (gRB1) {channel="mikrotik:routeros:rb1:totalSpace"}
Number:Dimensionless   My_RB_3011_Used_Space     "Used space"   (gRB1) {channel="mikrotik:routeros:rb1:usedSpace"}
Number:DataAmount   My_RB_3011_Free_Memory    "Free ram"       (gRB1) {channel="mikrotik:routeros:rb1:freeMemory"}
Number:DataAmount   My_RB_3011_Total_Memory   "Total ram"      (gRB1) {channel="mikrotik:routeros:rb1:totalMemory"}
Number:Dimensionless   My_RB_3011_Used_Memory    "Used ram"     (gRB1) {channel="mikrotik:routeros:rb1:usedMemory"}
Number:Dimensionless   My_RB_3011_Cpu_Load       "Cpu load"     (gRB1) {channel="mikrotik:routeros:rb1:cpuLoad"}
DateTime   My_RB_3011_Upsince      "Up since [%1$td.%1$tm.%1$ty %1$tH:%1$tM]"         (gRB1) {channel="mikrotik:routeros:rb1:upSince"}

Group gRB1Eth1 "Ethernet Interface 1"
String     Eth_1_Type                  "Type"                       (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:type"}
String     Eth_1_Name                  "Name"                       (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:name"}
String     Eth_1_Comment               "Comment"                    (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:comment"}
String     Eth_1_Mac_Address           "Mac address"                (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:macAddress"}
Switch     Eth_1_Enabled               "Enabled"                    (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:enabled"}
Contact     Eth_1_Connected             "Connected"                  (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:connected"}
DateTime   Eth_1_Last_Link_Down_Time   "Last link down"             (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:lastLinkDownTime"}
DateTime   Eth_1_Last_Link_Up_Time     "Last link up"               (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:lastLinkUpTime"}
Number     Eth_1_Link_Downs            "Link downs"                 (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:linkDowns"}
Number:DataTransferRate     Eth_1_Tx_Rate               "Transmission rate"          (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:txRate"}
Number:DataTransferRate     Eth_1_Rx_Rate               "Receiving rate"             (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:rxRate"}
Number     Eth_1_Tx_Packet_Rate        "Transmission packet rate"   (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:txPacketRate"}
Number     Eth_1_Rx_Packet_Rate        "Receiving packet rate"      (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:rxPacketRate"}
Number:DataAmount     Eth_1_Tx_Bytes              "Transmitted bytes"          (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:txBytes"}
Number:DataAmount     Eth_1_Rx_Bytes              "Received bytes"             (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:rxBytes"}
Number     Eth_1_Tx_Packets            "Transmitted packets"        (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:txPackets"}
Number     Eth_1_Rx_Packets            "Received packets"           (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:rxPackets"}
Number     Eth_1_Tx_Drops              "Transmission drops"         (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:txDrops"}
Number     Eth_1_Rx_Drops              "Receiving drops"            (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:rxDrops"}
Number     Eth_1_Tx_Errors             "Transmission errors"        (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:txErrors"}
Number     Eth_1_Rx_Errors             "Receiving errors"           (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:rxErrors"}
String     Eth_1_Default_Name          "Default name"               (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:defaultName"}
String     Eth_1_Rate                  "Link rate"                  (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:rate"}
String     Eth_1_Auto_Negotiation      "Auto negotiation"           (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:autoNegotiation"}
String     Eth_1_State                 "State"                      (gRB1Eth1) {channel="mikrotik:interface:rb1:eth1:state"}

Group gRB1Eth2 "Ethernet Interface 2"
String     Eth_2_Type                  "Type"                       (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:type"}
String     Eth_2_Name                  "Name"                       (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:name"}
String     Eth_2_Comment               "Comment"                    (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:comment"}
String     Eth_2_Mac_Address           "Mac address"                (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:macAddress"}
Switch     Eth_2_Enabled               "Enabled"                    (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:enabled"}
Contact     Eth_2_Connected             "Connected"                  (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:connected"}
DateTime   Eth_2_Last_Link_Down_Time   "Last link down"             (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:lastLinkDownTime"}
DateTime   Eth_2_Last_Link_Up_Time     "Last link up"               (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:lastLinkUpTime"}
Number     Eth_2_Link_Downs            "Link downs"                 (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:linkDowns"}
Number:DataTransferRate     Eth_2_Tx_Rate               "Transmission rate"          (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:txRate"}
Number:DataTransferRate     Eth_2_Rx_Rate               "Receiving rate"             (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:rxRate"}
Number     Eth_2_Tx_Packet_Rate        "Transmission packet rate"   (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:txPacketRate"}
Number     Eth_2_Rx_Packet_Rate        "Receiving packet rate"      (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:rxPacketRate"}
Number:DataAmount     Eth_2_Tx_Bytes              "Transmitted bytes"          (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:txBytes"}
Number:DataAmount     Eth_2_Rx_Bytes              "Received bytes"             (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:rxBytes"}
Number     Eth_2_Tx_Packets            "Transmitted packets"        (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:txPackets"}
Number     Eth_2_Rx_Packets            "Received packets"           (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:rxPackets"}
Number     Eth_2_Tx_Drops              "Transmission drops"         (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:txDrops"}
Number     Eth_2_Rx_Drops              "Receiving drops"            (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:rxDrops"}
Number     Eth_2_Tx_Errors             "Transmission errors"        (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:txErrors"}
Number     Eth_2_Rx_Errors             "Receiving errors"           (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:rxErrors"}
String     Eth_2_Default_Name          "Default name"               (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:defaultName"}
String     Eth_2_Rate                  "Link rate"                  (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:rate"}
String     Eth_2_Auto_Negotiation      "Auto negotiation"           (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:autoNegotiation"}
String     Eth_2_State                 "State"                      (gRB1Eth2) {channel="mikrotik:interface:rb1:eth2:state"}

Group gRB1Cap1 "CAPsMAN Inerface 1"
String     Cap_1_Type                  "Type"                       (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:type"}
String     Cap_1_Name                  "Name"                       (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:name"}
String     Cap_1_Comment               "Comment"                    (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:comment"}
String     Cap_1_Mac_Address           "Mac address"                (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:macAddress"}
Switch     Cap_1_Enabled               "Enabled"                    (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:enabled"}
Contact     Cap_1_Connected             "Connected"                  (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:connected"}
DateTime   Cap_1_Last_Link_Down_Time   "Last link down"             (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:lastLinkDownTime"}
DateTime   Cap_1_Last_Link_Up_Time     "Last link up"               (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:lastLinkUpTime"}
Number     Cap_1_Link_Downs            "Link downs"                 (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:linkDowns"}
Number:DataTransferRate     Cap_1_Tx_Rate               "Transmission rate"          (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:txRate"}
Number:DataTransferRate     Cap_1_Rx_Rate               "Receiving rate"             (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:rxRate"}
Number     Cap_1_Tx_Packet_Rate        "Transmission packet rate"   (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:txPacketRate"}
Number     Cap_1_Rx_Packet_Rate        "Receiving packet rate"      (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:rxPacketRate"}
Number:DataAmount     Cap_1_Tx_Bytes              "Transmitted bytes"          (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:txBytes"}
Number:DataAmount     Cap_1_Rx_Bytes              "Received bytes"             (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:rxBytes"}
Number     Cap_1_Tx_Packets            "Transmitted packets"        (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:txPackets"}
Number     Cap_1_Rx_Packets            "Received packets"           (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:rxPackets"}
Number     Cap_1_Tx_Drops              "Transmission drops"         (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:txDrops"}
Number     Cap_1_Rx_Drops              "Receiving drops"            (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:rxDrops"}
Number     Cap_1_Tx_Errors             "Transmission errors"        (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:txErrors"}
Number     Cap_1_Rx_Errors             "Receiving errors"           (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:rxErrors"}
String     Cap_1_State                 "State"                      (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:state"}
Number     Cap_1_Registered_Clients    "Registered clients"         (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:registeredClients"}
Number     Cap_1_Authorized_Clients    "Authorized clients"         (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:authorizedClients"}
DateTime   Cap_1_Up_Since              "Up since"                   (gRB1Cap1) {channel="mikrotik:interface:rb1:cap1:upSince"}

Group gRB1Ppp1 "PPPoE Client 1"
String     PP_Po_E_1_Type                  "Type"                       (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:type"}
String     PP_Po_E_1_Name                  "Name"                       (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:name"}
String     PP_Po_E_1_Comment               "Comment"                    (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:comment"}
String     PP_Po_E_1_Mac_Address           "Mac address"                (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:macAddress"}
Switch     PP_Po_E_1_Enabled               "Enabled"                    (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:enabled"}
Contact     PP_Po_E_1_Connected             "Connected"                  (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:connected"}
DateTime   PP_Po_E_1_Last_Link_Down_Time   "Last link down"             (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:lastLinkDownTime"}
DateTime   PP_Po_E_1_Last_Link_Up_Time     "Last link up"               (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:lastLinkUpTime"}
Number     PP_Po_E_1_Link_Downs            "Link downs"                 (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:linkDowns"}
Number:DataTransferRate     PP_Po_E_1_Tx_Rate               "Transmission rate"          (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:txRate"}
Number:DataTransferRate     PP_Po_E_1_Rx_Rate               "Receiving rate"             (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:rxRate"}
Number     PP_Po_E_1_Tx_Packet_Rate        "Transmission packet rate"   (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:txPacketRate"}
Number     PP_Po_E_1_Rx_Packet_Rate        "Receiving packet rate"      (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:rxPacketRate"}
Number:DataAmount     PP_Po_E_1_Tx_Bytes              "Transmitted bytes"          (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:txBytes"}
Number:DataAmount     PP_Po_E_1_Rx_Bytes              "Received bytes"             (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:rxBytes"}
Number     PP_Po_E_1_Tx_Packets            "Transmitted packets"        (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:txPackets"}
Number     PP_Po_E_1_Rx_Packets            "Received packets"           (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:rxPackets"}
Number     PP_Po_E_1_Tx_Drops              "Transmission drops"         (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:txDrops"}
Number     PP_Po_E_1_Rx_Drops              "Receiving drops"            (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:rxDrops"}
Number     PP_Po_E_1_Tx_Errors             "Transmission errors"        (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:txErrors"}
Number     PP_Po_E_1_Rx_Errors             "Receiving errors"           (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:rxErrors"}
String     PP_Po_E_1_State                 "State"                      (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:state"}
DateTime   PP_Po_E_1_Up_Since              "Up since"                   (gRB1Ppp1) {channel="mikrotik:interface:rb1:ppp1:upSince"}

Group gRB1Tun1 "L2TP Server 1"
String     L_2_TP_Srv_1_Type                  "Type"                       (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:type"}
String     L_2_TP_Srv_1_Name                  "Name"                       (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:name"}
String     L_2_TP_Srv_1_Comment               "Comment"                    (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:comment"}
String     L_2_TP_Srv_1_Mac_Address           "Mac address"                (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:macAddress"}
Switch     L_2_TP_Srv_1_Enabled               "Enabled"                    (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:enabled"}
Contact     L_2_TP_Srv_1_Connected             "Connected"                  (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:connected"}
DateTime   L_2_TP_Srv_1_Last_Link_Down_Time   "Last link down"             (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:lastLinkDownTime"}
DateTime   L_2_TP_Srv_1_Last_Link_Up_Time     "Last link up"               (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:lastLinkUpTime"}
Number     L_2_TP_Srv_1_Link_Downs            "Link downs"                 (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:linkDowns"}
Number:DataTransferRate     L_2_TP_Srv_1_Tx_Rate               "Transmission rate"          (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:txRate"}
Number:DataTransferRate     L_2_TP_Srv_1_Rx_Rate               "Receiving rate"             (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:rxRate"}
Number     L_2_TP_Srv_1_Tx_Packet_Rate        "Transmission packet rate"   (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:txPacketRate"}
Number     L_2_TP_Srv_1_Rx_Packet_Rate        "Receiving packet rate"      (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:rxPacketRate"}
Number:DataAmount     L_2_TP_Srv_1_Tx_Bytes              "Transmitted bytes"          (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:txBytes"}
Number:DataAmount     L_2_TP_Srv_1_Rx_Bytes              "Received bytes"             (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:rxBytes"}
Number     L_2_TP_Srv_1_Tx_Packets            "Transmitted packets"        (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:txPackets"}
Number     L_2_TP_Srv_1_Rx_Packets            "Received packets"           (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:rxPackets"}
Number     L_2_TP_Srv_1_Tx_Drops              "Transmission drops"         (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:txDrops"}
Number     L_2_TP_Srv_1_Rx_Drops              "Receiving drops"            (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:rxDrops"}
Number     L_2_TP_Srv_1_Tx_Errors             "Transmission errors"        (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:txErrors"}
Number     L_2_TP_Srv_1_Rx_Errors             "Receiving errors"           (gRB1Tun1) {channel="mikrotik:interface:rb1:tun1:rxErrors"}

Group gRB1Wifi1 "WiFi Client 1"
String     Phone_1_Mac_Address      "Mac address"                          (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:macAddress"}
String     Phone_1_Comment          "Comment"                              (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:comment"}
Contact     Phone_1_Connected        "Connected"                            (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:connected"}
Contact     Phone_1_Continuous       "Continuous"                           (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:continuous"}
String     Phone_1_Ssid             "Wi fi network"                        (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:ssid"}
String     Phone_1_Interface        "Name"                                 (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:interface"}
Number     Phone_1_Signal           "Received signal strength indicator"   (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:signal"}
DateTime   Phone_1_Up_Since         "Up since"                             (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:upSince"}
DateTime   Phone_1_Last_Seen        "Last seen"                            (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:lastSeen"}
Number:DataTransferRate     Phone_1_Tx_Rate          "Transmission rate"                    (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:txRate"}
Number:DataTransferRate     Phone_1_Rx_Rate          "Receiving rate"                       (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:rxRate"}
Number     Phone_1_Tx_Packet_Rate   "Transmission packet rate"             (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:txPacketRate"}
Number     Phone_1_Rx_Packet_Rate   "Receiving packet rate"                (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:rxPacketRate"}
Number:DataAmount     Phone_1_Tx_Bytes         "Transmitted bytes"                    (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:txBytes"}
Number:DataAmount     Phone_1_Rx_Bytes         "Received bytes"                       (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:rxBytes"}
Number     Phone_1_Tx_Packets       "Transmitted packets"                  (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:txPackets"}
Number     Phone_1_Rx_Packets       "Received packets"                     (gRB1Wifi1) {channel="mikrotik:wifiRegistration:rb1:wifi1:rxPackets"}

Group gRB1Wifi2 "WiFi Client 2"
String     Tablet_2_Mac_Address      "Mac address"                          (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:macAddress"}
String     Tablet_2_Comment          "Comment"                              (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:comment"}
Contact     Tablet_2_Connected        "Connected"                            (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:connected"}
Contact     Tablet_2_Continuous       "Continuous"                           (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:continuous"}
String     Tablet_2_Ssid             "Wi fi network"                        (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:ssid"}
String     Tablet_2_Interface        "Name"                                 (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:interface"}
Number     Tablet_2_Signal           "Received signal strength indicator"   (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:signal"}
DateTime   Tablet_2_Up_Since         "Up since"                             (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:upSince"}
DateTime   Tablet_2_Last_Seen        "Last seen"                            (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:lastSeen"}
Number:DataTransferRate    Tablet_2_Tx_Rate          "Transmission rate"                    (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:txRate"}
Number:DataTransferRate     Tablet_2_Rx_Rate          "Receiving rate"                       (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:rxRate"}
Number     Tablet_2_Tx_Packet_Rate   "Transmission packet rate"             (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:txPacketRate"}
Number     Tablet_2_Rx_Packet_Rate   "Receiving packet rate"                (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:rxPacketRate"}
Number:DataAmount     Tablet_2_Tx_Bytes         "Transmitted bytes"                    (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:txBytes"}
Number:DataAmount     Tablet_2_Rx_Bytes         "Received bytes"                       (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:rxBytes"}
Number     Tablet_2_Tx_Packets       "Transmitted packets"                  (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:txPackets"}
Number     Tablet_2_Rx_Packets       "Received packets"                     (gRB1Wifi2) {channel="mikrotik:wifiRegistration:rb1:wifi2:rxPackets"}
```

### sitemaps/mikrotik.sitemap

```perl
sitemap mikrotik label="Mikrotik Binding Demo"
{
 Frame label="RouterBOARD 1" {
  Group item=gRB1
  Group item=gRB1Eth1
  Group item=gRB1Eth2
  Group item=gRB1Ppp1
  Group item=gRB1Tun1
  Group item=gRB1Cap1
  Group item=gRB1Wifi1
  Group item=gRB1Wifi2
 }
}
```
