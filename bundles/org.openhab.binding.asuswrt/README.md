# Asuswrt Binding

This binding adds support to read information from ASUS-Routers (Copyright © ASUS).

## Supported Things

This binding supports ASUS routers with Asuswrt or [Asuswrt-Merlin](https://www.asuswrt-merlin.net/) firmware.
Firmware 5.x.x (some DSL models) is NOT supported (not Asuswrt).

| ThingType     | Name       | Descripion                           |
|---------------|------------|--------------------------------------|
| bridge        | router     | Router to which the binding connects |
| -             | interface  | Network interface of the router      |
| -             | client     | Client is connected to the bridge    |

### `router` Thing Configuration

| Name            | Type    | Description                           | Default             | Required | Advanced |
|-----------------|---------|---------------------------------------|---------------------|----------|----------|
| hostname        | text    | Hostname or IP address of the device  | router.asus.com     | yes      | no       |
| username        | text    | Username to access the device         | N/A                 | yes      | no       |
| password        | text    | Password to access the device         | N/A                 | yes      | no       |
| useSSL          | boolean | Connect over SSL or use http://       | false               | no       | no       |
| refreshInterval | integer | Interval the device is polled in sec. | 20                  | no       | yes      |
| httpPort        | integer | HTTP-Port                             | 80                  | no       | yes      |
| httpsPort       | integer | HTTPS-Port                            | 443                 | no       | yes      |

### `interface` Thing Configuration

| Name            | Type    | Description                           | Default             | Required | Advanced |
|-----------------|---------|---------------------------------------|---------------------|----------|----------|
| interfaceName   | text    | options name of interface (wan/lan)   | N/A                 | yes      | no       |

### `client` Thing Configuration

| Name            | Type    | Description                           | Default             | Required | Advanced |
|-----------------|---------|---------------------------------------|---------------------|----------|----------|
| macAddress      | text    | Unique MAC address of the device      | N/A                 | yes      | no       |
| clientNick      | text    | Nickname used by OH                   | N/A                 | no       | no       |


## Properties

All devices support some of the following properties:

| property         | description                  | things supporting this channel        |
|------------------|------------------------------|---------------------------------------|
| vendor           | Vendor of device             | router, client                        |
| dnsName          | DNS name of device           | router, client                        |


## Channels

All devices support some of the following channels:

| group            | channel              |type                    | description                                | things supporting this channel    |
|------------------|----------------------|------------------------|--------------------------------------------|-----------------------------------|
| network-info     | mac-address          | text (RO)              | HW address                                 | interface, client                 |
|                  | ip-address           | text (RO)              | IP address                                 | interface                         |
|                  | ip-method            | text (RO)              | IP method (static/dhcp)                    | interface, client                 |
|                  | subnet               | text (RO)              | Subnetmask                                 | interface                         |
|                  | gateway              | text (RO)              | Default gateway                            | interface                         |
|                  | dns-servers          | text (RO)              | DNS servers                                | interface                         |
|                  | network-state        | Switch (RO)            | Client is online                           | interface, client                 |
|                  | internet-state       | Switch (RO)            | Client connected to Internet               | client                            |
| sys-info         | mem-total            | Number:DataAmountype   | Total memory in MB                         | router                            |
|                  | mem-used             | Number:DataAmountype   | Used memory in MB                          | router                            |
|                  | mem-free             | Number:DataAmountype   | Free memory in MB                          | router                            |
|                  | mem-used-percent     | Number:Dimensionles    | Used memory in %                           | router                            |
|                  | cpu-used-percent     | Number:Dimensionles    | Total CPU usage in percent over all cores  | router                            |
| client-list      | known-clients        | text (RO)              | Known clients with name and MAC addresses  | router                            |
|                  | online-clients       | text (RO)              | Online clients with name and MAC addresses | router                            |
|                  | online-macs          | text (RO)              | List with MAC addresses of online clients  | router                            |
|                  | online-clients-count | Number:Dimensionless   | Count of online clients                    | router                            |
| traffic          | current-rx           | Number:DataTransferRate| Current DataTransferRate MBits/s (receive) | interface, client                 |
|                  | current-tx           | Number:DataTransferRate| Current DataTransferRate MBits/s (send)    | interface, client                 |
|                  | today-rx             | Number:DataAmount      | Data received since 0:00 a clock in MB     | interface, client                 |
|                  | today-tx             | Number:DataAmount      | Data sent since 0:00 a clock in MB         | interface, client                 |
|                  | total-rx             | Number:DataAmount      | Data received since reboot in MB           | interface, client                 |
|                  | total-tx             | Number:DataAmount      | Data sent since reboot in MB               | interface, client                 |


## Events

All devices support some of the following Events:

| group            | event               |kind        | description                                                            | things supporting this event    |
|------------------|---------------------|------------|------------------------------------------------------------------------|---------------------------------|
| network-info     | connection-event    | Trigger    | Fired if connection is established ('connected') or ('disconnected')   | interface                       |
|                  | client-online-event | Trigger    | Fired if client leaves ('gone') or enters ('connected') the network    | client                          |
| client-list      | client-online-event | Trigger    | Fired if client leaves ('gone') or enters ('connected') the network    | router                          |
