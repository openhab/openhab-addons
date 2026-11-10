# TailWind Binding - Smart Garage Door Controller

![TailWind logo](doc/Tailwind-logo-250x88-dark.png)

This binding is used to enable communication between OpenHab and [TailWind's Smart Automatic Garage Controller](https://gotailwind.com/).

## Overview

The garage door controllers are automatically discovered.

There is a single Thing created for each controller connected to the local Ethernet-WiFi network with channels that allow control of the doors (garage, gates, doors...) connected to the controller.

OpenHab linked channels will be updated regardless of whether the Tailwind's web/smarphone app, door remote or other integrations are used to configure, open or close doors.

Details for connecting to the controller's API server can be found on TailWind's GitHub site here: [Tailwind Local Control API](https://github.com/Scott--R/Tailwind_Local_Control_API)

## Versions and Installation

This binding lives in its own repository and is built for two openHAB lines:

| openHAB | Branch | Release tags | Java to build |
|---------|--------|--------------|---------------|
| 5.x     | `main` | `v5.x.y`     | 21            |
| 4.x     | `4.x`  | `v4.x.y`     | 17            |

To install, download the jar for your openHAB version from the
[Releases](https://github.com/dschoepel/openhab-binding-tailwind/releases) page and copy it
into your openHAB `addons` folder (for example `/usr/share/openhab/addons/`). If you installed
the binding from the Marketplace, uninstall it there first, and remove any older
`org.openhab.binding.tailwind` jar from the `addons` folder. Existing Things and Items keep
working, because the binding id (`tailwind`) has not changed.

To build it yourself, check out the branch for your openHAB version and run
`mvn clean install` with the matching JDK. The jar is written to `target/`.

## Testing

#### TailWind iQ3:

<ul>
<li>using auto discovery and adding thing via inbox (<b>preferred</b>)</li>
<li>using manual configuration</li>
<li>using file based setup </li>
<li>via a direct Ethernet (LAN) & Wi-Fi connection with the iQ3 on same local network as the OpenHab server</li> 
</ul>

## Supported Things

|               Thing                | Type | Description                       | Connection      | Doors |   Tested    |
| :--------------------------------: | :--: | :-------------------------------- | --------------- | :---: | :---------: |
| ![TailWind](doc/tailwind_iQ35.png) | iQ3  | Smart Automatic Garage Controller | Ethernet / WiFi |   3   | &#9989; Yes |

A Thing UID will have three components **bindingId** + **model** + **unique id** (the MAC address of the device).

For example - **tailwind:iQ3:08d1f91202ec**

## Discovery

The binding can auto-discover the TailWind garage controllers present on your **local** network.

If the TailWind device was discovered, the Thing properties will list specifications reported by the controller including vendor, firmware/hardware versions, number of doors connected, MAC address, model, and web server URL.

Auto-discovery is enabled by default. To disable it, you can create a file in the services directory called tailwind.cfg with the following content:

```ruby
# Configuration for the tailwind binding
#
# Auto discovery parameter
# true to enable, false to disable
org.openhab.tailwind:enableAutoDiscovery=false
```

This configuration parameter only controls the TailWind auto-discovery process, not the openHAB auto-discovery. Moreover, if OpenHAB's auto-discovery is disabled, the TailWind auto-discovery is disabled too.

Once added as a Thing, the user can control up to three doors per controller, similar to how they are controlled using TailWind's web or smartphone app.

## Thing Configuration

The TailWind controller thing has the following configuration parameters:

| Parameter                                                      | Parameter Id         | Req/Opt  | Description                                                                                                                                                                                                     |                          Default                           |  Type   | Accepted Values                                                                                                                                             |
| :------------------------------------------------------------- | :------------------- | :------: | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :--------------------------------------------------------: | :-----: | :---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Number of Doors Controlled                                     | doorCount            | Required | The number of doors connected to the controller                                                                                                                                                                 |                             1                              | Integer | 1, 2 or 3                                                                                                                                                   |
| API Web Server Address/URL                                     | webServerAddress     | Required | IP address or host name of the TailWind controller web server (API). Host name format is tailwind-MAC address.local (i.e. tailwind-aa0b0cd0e0f0.local). An http:// prefix is removed. | URL if auto-discovered, user provided if manually created. | String  | Valid IP address or URL ending with .local                                                                                                                  |
| Authorization token for the local API server on the controller | authToken            | Required | Token obtained from the TailWind [Web Application](web.gotailwind.com). Instructions can be found here: [Token Instructions](https://github.com/Scott--R/Tailwind_Local_Control_API?tab=readme-ov-file#2-token) |                    Placeholder (123456)                    | String  | Must not be blank and have 6 characters                                                                                                                     |
| OpenHab Server IPV4 address                                    | openHabHostAddress   | Required | This is the IP address of the OpenHab server where the TailWind binding is installed. This is used to specify an address:port value for sending Tailwind UDP status messages to the binding.                    |                          _Blank_                           | String  | Must not be blank, is a valid IPV4 address, is one of the IP addresses assigned to the OpenHab server's network interfaces. Preferably the primary address. |
| <b>\*ADVANCED</b>                                              |
| Door 1 Name\*                                                  | doorOneName          | Required | Specify custom name for door one                                                                                                                                                                                |                           Door 1                           | String  | Must not be blank or a duplicate of the other 2 door names                                                                                                  |
| Door 1 Partial Open\*                                          | doorOnePartialOpen   | Required | Number of seconds to partially open this door                                                                                                                                                                   |                        2.5 seconds                         |  Float  | 0.5 - 15 seconds                                                                                                                                            |
| Door 2 Name\*                                                  | doorTwoName          | Required | Specify custom name for door two                                                                                                                                                                                |                           Door 2                           | String  | Must not be blank or a duplicate of the other 2 door names                                                                                                  |
| Door 2 Partial Open\*                                          | doorTwoPartialOpen   | Required | Number of seconds to partially open this door                                                                                                                                                                   |                        2.5 seconds                         |  Float  | 0.5 - 15 seconds                                                                                                                                            |
| Door 3 Name\*                                                  | doorThreeName        | Required | Specify custom name for door three                                                                                                                                                                              |                           Door 3                           | String  | Must not be blank or a duplicate of the other 2 door names                                                                                                  |
| Door 3 Partial Open\*                                          | doorThreePartialOpen | Required | Number of seconds to partially open this door                                                                                                                                                                   |                        2.5 seconds                         |  Float  | 0.5 - 15 seconds                                                                                                                                            |

> Note: Starred <b>\*</b> items are hidden unless "Show advanced" checked on UI

## Channels

TailWind Thing channels are listed by channel group (group name preceeds channel with a # sign).

> When manually creating these channels, always use the group prefix to ensure the UI puts them together in the correct group.

### TailWind Controller

| Label                   | Channel Type UID            | Item Type | Read/Write | Description                                                                                                                                                                                                                                                         |
| :---------------------- | :-------------------------- | :-------- | :--------: | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Number of Doors         | controller#doorNum          | Number    |     R      | Number of doors being controlled.                                                                                                                                                                                                                                   |
| Night Mode              | controller#nightModeEnabled | Number    |     R      | Night mode is enabled (can only be changed using TailWind app). 0=disabled, 1=enabled                                                                                                                                                                               |
| Brightness              | controller#ledBrightness    | Dimmer    |     RW     | Controls the brightness of the controller LED. Values from 0% - 100%                                                                                                                                                                                                |
| WiFi RSSI               | controller#routerRssi       | Number    |     R      | Wi-Fi network received signal strength in dBm. readings between Strong(-50) and Good(-67) are ideal.                                                                                                                                                                |
| <b>Advanced</b>         |
| Model Number\*          | controller#productID        | String    |     R      | TailWind device model number                                                                                                                                                                                                                                        |
| Device ID\*             | controller#deviceID         | String    |     R      | TailWind device ID (MAC address). Format is unique to how TailWind stores this. Each number pair is sepated by an underscore, if first digit of pair is zero (0) it is not included. For example address AA:0B:0C:D0:E0:F0 would be represented as aa_b_c_d0_e0_f0. |
| Firmware Version\*      | controller#firmwareVersion  | String    |     R      | Firmware version reported by the controller. |
| Server Monitoring\*     | controller#serverMonitor    | Switch    |     RW     | ON: the controller reboots itself when it loses its connection to the TailWind cloud server. OFF: it stays up without internet access. Requires firmware 10.61 or later; UNDEF on older firmware. |
| Send Support Commands\* | controller#supportCommand   | String    |     RW     | Infrequently used support commands. Values: 'reboot' = reboot the controller device, 'blink' = identify the controller device by flashing white light three times.                                                                                                  |

> Note: Starred <b>\*</b> items are hidden unless "Show advanced" checked on UI
> </br>

### Door One

| Label                    | Channel Type UID    | Item Type | Read/Write | Description                                                                                                                                                       |
| :----------------------- | :------------------ | :-------- | :--------: | :---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Door 1 Index             | doorOne#index       | Number    |     R      | Door number index assigned by TailWind (0)                                                                                                                        |
| Door 1 Status            | doorOne#status      | String    |     R      | Door status (open, close, lock, enable, disable, reboot)                                                                                                          |
| Door 1 Door Control      | doorOne#openClose   | String    |     RW     | Door control (open, partial, close)                                                                                                                               |
| Door 1 Partial Open Time | doorOne#partialOpen | Number    |     R      | Partial door open time in seconds (0.5 - 15). Ony updated via Thing configuration.                                                                                |
| Door 1 Locked Up Status  | doorOne#lockup      | Number    |     R      | Door lockup (0 - not locked up, 1 = locked up)                                                                                                                    |
| Door 1 Is Configured     | doorOne#disabled    | Number    |     R      | Door is in disabled state (0 - enabled, 1 - disabled). Could mean no door is wired to the controller or it is wired but it was disabled using the smartphone app. |

</br>

### Door Two

| Label                    | Channel Type UID    | Item Type | Read/Write | Description                                                                                                                                                       |
| :----------------------- | :------------------ | :-------- | :--------: | :---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Door 2 Index             | doorTwo#index       | Number    |     R      | Door number index assigned by TailWind (1)                                                                                                                        |
| Door 2 Status            | doorTwo#status      | String    |     R      | Door status (open, close, lock, enable, disable, reboot)                                                                                                          |
| Door 2 Door Control      | doorTwo#openClose   | String    |     RW     | Door control (open, partial, close)                                                                                                                               |
| Door 2 Partial Open Time | doorTwo#partialOpen | Number    |     R      | Partial door open time in seconds (0.5 - 15). Ony updated via Thing configuration.                                                                                |
| Door 2 Locked Up Status  | doorTwo#lockup      | Number    |     R      | Door lockup (0 - not locked up, 1 = locked up)                                                                                                                    |
| Door 2 Is Configured     | doorTwo#disabled    | Number    |     R      | Door is in disabled state (0 - enabled, 1 - disabled). Could mean no door is wired to the controller or it is wired but it was disabled using the smartphone app. |

</br>

## Door Three

| Label                    | Channel Type UID      | Item Type | Read/Write | Description                                                                                                                                                       |
| :----------------------- | :-------------------- | :-------- | :--------: | :---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Door 3 Index             | doorThree#index       | Number    |     R      | Door number index assigned by TailWind (2)                                                                                                                        |
| Door 3 Status            | doorThree#status      | String    |     R      | Door status (open, close, lock, enable, disable, reboot)                                                                                                          |
| Door 3 Door Control      | doorThree#openClose   | String    |     RW     | Door control (open, partial, close)                                                                                                                               |
| Door 3 Partial Open Time | doorThree#partialOpen | Number    |     R      | Partial door open time in seconds (0.5 - 15). Ony updated via Thing configuration.                                                                                |
| Door 3 Locked Up Status  | doorThree#lockup      | Number    |     R      | Door lockup (0 - not locked up, 1 = locked up)                                                                                                                    |
| Door 3 Is Configured     | doorThree#disabled    | Number    |     R      | Door is in disabled state (0 - enabled, 1 - disabled). Could mean no door is wired to the controller or it is wired but it was disabled using the smartphone app. |

## Full Example

### Thing Configuration

> Thing <binding_id>:<type_id>:<thing_id> "Label" @ "Location" [ \<parameters> ]

Using the syntax for [defining a Thing](https://www.openhab.org/docs/configuration/things.html#defining-things-using-files):

- the binding_id should always be 'tailwind'
- the type_id should be the value for a Type in Supported Things table above (i.e iQ3)
- the thing_id can be any unique value, best if the device MAC address is used. See additional considerations below to find the MAC address.

The webServer Address can either be the IP Address of the controller or the URL. Using the URL is best approach as you dont have to worry about the controller device IP address changing.

```java
Thing tailwind:iQ3:09d1f01202ec "Tailwind iQ3" [doorCount=2, webServerAddress="tailwind-09d1f01202ec.local", authToken="138952", openHabHostAddress="192.168.1.129", doorOneName="Door 1", doorOnePartialOpen=2.75, doorTwoName="Door 2", doorTwoPartialOpen=3.0]
```

### Item Configuration

```java
// TailWind Controller - Grouping
Number    Tailwind_iQ3_Number_of_Doors    "Number Of Doors"    {channel="tailwind:iQ3:09d1f01202ec:controller#doorNum"}
Dimmer    Tailwind_iQ3_Brightness         "Brightness"         {channel="tailwind:iQ3:09d1f01202ec:controller#ledBrightness"}
Number    Tailwind_iQ3_WiFi_RSSI          "WiFi RSSI"          {channel="tailwind:iQ3:09d1f01202ec:controller#routerRssi"}
// Door 1 - Grouping
String    Tailwind_iQ3_Door_1_Status      "Door 1 Status"      {channel="tailwind:iQ3:09d1f01202ec:doorOne#status"}
String    Tailwind_iQ3_Door_1_Control     "Door 1 Control"     {channel="tailwind:iQ3:09d1f01202ec:doorOne#openClose"}
// Door 1 - Grouping
String    Tailwind_iQ3_Door_2_Status      "Door 2 Status"      {channel="tailwind:iQ3:09d1f01202ec:doorTwo#status"}
String    Tailwind_iQ3_Door_2_Control     "Door 2 Control"     {channel="tailwind:iQ3:09d1f01202ec:doorTwo#openClose"}
```

## Additional Considerations

### Limitations

The TailWind controller can only be configured to send status update messages to one host (OpenHab server) listening for them.

This will not be a problem unless you try to run this binding on more than one OpenHab server at the same time for the same Tailwind controller.

### Web Server URL

The URL syntax is:

```xml
tailwind-<MACAddress>.local
```

The MAC address can be found using the TailWind smart-phone mobile application.

1. Open the app and click on the "gear" icon for a controller.
2. At the top of the screen, upper right side, the MAC is listed in a format similar to this: 09:d1:f0:12:02:ec
3. The corresponding URL would be tailwind-09d1f01202ec.local.

### Authentication Token

The Token is called the "local control key" and can be found on the TailWind web application (this cannot be found on the smart-phone/mobile application).

1. Login to the web application here: [Web Application](web.gotailwind.com)
2. The top menu on this screen should list an option called "Local Control Key". If its not visible, there will be a "Hamburger" icon on the upper left side of the screen.
3. Click on "Local Control Key" to see the token or generate one if it has not already been created.
4. Copy this token and use it to configure the 'authToken' configuration parameter.

The instructions can also be found here: [Token Instructions](https://github.com/Scott--R/Tailwind_Local_Control_API?tab=readme-ov-file#2-token)
