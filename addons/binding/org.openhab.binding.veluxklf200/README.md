
## Velux KLF 200 Binding

The Velux KLF200 is a bridge that enables the integration and automation of Velux nodes such as automated blinds. The KLF200 communicates natively with existing Velux products using the io-homecontrol protocol. This is a closed protocol. The KLF200 bridge makes devices using this protocol available over a standard ethernet network (through an API).

The original version of the KLF200 (running firmware version 0.1.xx.xx) provided a HTTP based API. However in 2018, Velux release v2 firmware for the device and completely changed the way the KLF200 API operated. Once upgraded to v2 firmware, the HTTP based API was removed in favor of a web-sockets protocol based around the SLIP protocol.

<B>This binding is intended to be used for KLF200 devices running firmware version 0.2.x.x.x.x.</b>

If your device is currently running earlier firmware versions, you can download and install the latest firmware for your KLF200 from https://updates2.velux.com/

## Preparation

Before installing and attempting to configure the KLF200 binding, it is recommended that you perform some preparatory tasks first. This will make it easier and quicker to complete the setup of your KLF200 binding.

 1. **Check Firmware Version**: The quickest way to check which version of the KLF200 firmware you are using is to open a browser and navigate to the Hostname / IP address of your unit. If you get a response and see a login page, this means that you are running a 0.1.x.x.x.x firmware version and this binding will not work for your KLF200 until you upgrade it to the 0.2.x.x.x.x version. If you get no response and your browser says that it is unable to connect, this is a good indication that you are on the 0.2.x.x.x.x firmware version.
 2. **Setup your KLF200**: This binding will automatically discover all of the nodes and scenes that you have setup on your KLF200, so if you haven't already done so, you should set these up first. The easiest way to do this is to have your KLF200 clone the configuration of your existing Velux remote control(s). The procedure for doing this is described in the KLF200 user manual and is relatively straight forward to do (particularly if you use an existing remote such as KLR200). Once you have completed the setup and/or cloning of the configuration, the KLF200 device now knows about all of your existing Velux / io.homecontrol devices. For further details, please refer to the KLF200 setup manual: [https://velcdn.azureedge.net/~/media/marketing/au/downloads/installation%20instructions/klf200-gb_454069-2016-10.pdf](https://velcdn.azureedge.net/~/media/marketing/au/downloads/installation%20instructions/klf200-gb_454069-2016-10.pdf "Click to open in a new window or tab
https://velcdn.azureedge.net/~/media/marketing/au/downloads/installation%20instructions/klf200-gb_454069-2016-10.pdf")
 3. **Note the Password**: When configuring your bridge in Openhab, you will need the password for your KLF200 unit. Some of the Velux documentation is incorrect and has made reference to the default password being "*velux123*" to access the unit remotely using the SLIP protocol. This is incorrect. The correct password to use is the WIFI password. This is printed on a sticker on the back of the KLF200. The password you are looking for is the password written beneath the SSID and is typically about 10 characters in length. Take note of this password, paying particular attention as the password is case sensitive!
 5. **Reboot the KLF200**: Before attempting to configure the binding in Openhab, please first reboot your KLF200 unit. This is because the TCP socket that the KLF200 listens on may have closed due to inactivity. By default, the unit shuts down the socket if it is not used for a period of ~10 minutes. The only way to get it back up and running is to power-cycle the KLF200 unit. Once you have the binding installed and configured, you should not have any probles with the socket closing as the binding takes care of automatically sending a keep-alive ping to the socket periodically to prevent the socket from shutting down due to inactivity.


## Binding Configuration

Install the binding as you normally would when installing a binding within Openhab. Once installed, navigate to your Things and choose to add a Velux KLF200 Bridge. You will then be prompted to provide some configuration parameters as follows:

| Property       | Default                | Required | Description                                           |
|----------------|------------------------|:--------:|-------------------------------------------------------|
| host|               |   Yes    | The hostname or IP address of the KLF200       |
| port| 51200|    Yes    | TCP port for accessing the KLF200.               |
| password| |   Yes    | Password for connecting to the KLF200 (*Please see preparation notes above*). |
| refresh   | 5|    No    | Once the binding is installed, it listens to the KLF200 for notifications about when things have changed. For example, if you use an existing remote control to close a blind, the KLF200 will send a notification to advise that the position of the blind has moved. This binding captures these notifications and updates the relevant Items in Openhab. However, to cater for the possibility that certain velux nodes made get out of sync with their Openhab item counterparts, the binding periodically polls the KLF200 to request an updated state for each item that it is interested in. By default, this polling happens every 5 minutes. However, you can change this to be more or less frequent as required.            |

Once you have clicked save on your bindings configuration, an attempt is made to connect to the KLF200 unit using the information supplied. If successful, you should see the status of the Bridge item change to "On-line". In the event of a problem, please refer to the logs to diagnose the source of the connection issue.

**NOTE:** If you are having difficulty connecting to your KLF200, please ensure you have taken note of point 5 in the 'Preparation' tasks above.


## Supported Things

This binding wraps the KLF200 API and as such, should be able to support all of the device types that exist in the Velux ecosystem. However, please see the table below to confirm if your particular device types are implemented. Whether or not something is implemented is related to whether or not someone with that particular type of device is available to provide or test whether or not the device will work! If your device is not implemented and you would like it to be, please contact me on the forums.

**Important:** Even if your particular device type is not yet implemented directly, you can still use this binding to control that device via a scene. Specifically, just record a scene to do what you want to do with the device and that scene should then appear in your Paper-UI Inbox.
|  Product | Supported |

| #| Velux Product| Supported| Notes / Description                                           |
|----------------|------------------------|:--------:|----------------------------------------------------
|1|Scene|**Yes**| Discovers scenes and allows execution.
|2|Blind | Yes | 
|3 | Interior Venetian Blind | *No* | 
|4 | Roller Shutter | *No*  | 
|5 | Vertical Exterior Awning | *No*  | 
|6 | Window Opener | *No*  | 
|7 | Garage Door Opener |  *No* | 
|8 | Light | *No*  | 
|9 | Gate Opener | *No*  | 
|10 | Door Lock |  *No* | 
|11 | Window Lock |  *No* | 
|12 | Vertical Interior Blinds | **Yes** | Discovers and allows granular movement of individual blinds. 
|13 | Dual Roller Shutter |  *No* | 
|14 | On/Off Switch | *No*  | 
|15 | Horizontal Awning |  *No* | 
|16 | Exterior Venetian Blind |  *No* | 
|17 | Louver Blind |  *No* | 
|18 | Curtain Track |  *No* | 
|19 | Ventilation Point |  *No* | 
|20 | Exterior Heating |  *No* | 
|21 | Swinging Shutters |  *No* | 




## Discovery

Once the bridge has been configured and comes on-line, it should start a background discovery process to try to determine any of the supported device types (see above) that are configured on your KLF200. After a short while, newly discovered items should appear in your 'Inbox'. 

## Thing Configuration

Individual things do not need to be configured as they are configured automatically when discovered by the bridge. Where appropriate both the bridge and individual things have their properties updated so show relevant static or KLF200 configured information that may be of casual interest.

## Channels

Based on the currently supported things, the following channels are available:

| Channel Type ID  | Item Type | Description                                    | Thing types supporting this channel                             |
|------------------|-----------|------------------------------------------------|-----------------------------------------------------------------|
| connection_status| Switch    | Read-only switch that indicates connectivity to the KLF200 bridge. | klf200-bridge|
| scene_trigger| Switch| Switch to allow triggering of a scene. When switched on, the switch remains on until the scene has completed its execution. If switched off (by a user / rule) during its execution, the scene is stopped mid-execution.      | velux_scene                       |
| blind-control| RollerShutter| Allows granular control of a blind, up, down, stop or movement to a specific percentage open / closed. | velux_blind                                      |

## Binding Features

| # | Feature | Description / Notes
|-----|----------|----------|
|1| KLF200 Date / Time | The KLF200 does not have the ability to connect to a time server to establish the current time. When it is powered on, its internal clock sets the time to a default value in the year 2000. The binding therefore sets the time on the KLF200 to the current time. Although not technically necessary, doing so means that values returned that include dates / times (such as last command executed) return a meaningful time. It also means that if you are having any difficulty with your KLF200 and need to review its logs, the time-stamp on the log entries will be a little more meaningful. Each time the KLF200 is rebooted, the time is reset back to 2000. To ensure it says current, the binding periodically sets the time to make sure it
|2| Information | Where nodes have relevant version, setting or configuration information recorded on the KLF 200, this information is added to the properties of the respective item. Examples of this include software version numbers and serial numbers of nodes.
|3| Home Monitor Service | The binding automatically switches on the 'Home Monitoring Service' on the KLF200. This service allows the KLF to monitor the state of all of the devices that it knows about and therefore provide feedback to the binding in the event that something changes. For example, if you were to close a blind by using an existing remote control, this biding would be notified of the position change of the node by virtue of Home Monitor being turned on. The factory default is that this setting is turned off.    

## Other Resources

The KLF200 API has been developed based on the API specification provided by Velux:
[https://www.velux.com//velcdn.azureedge.net/~/media/com/api/klf200/technical%20specification%20for%20klf%20200%20api-ver3-16.pdf](https://www.velux.com//velcdn.azureedge.net/~/media/com/api/klf200/technical%20specification%20for%20klf%20200%20api-ver3-16.pdf "Click to open in a new window or tab
https://www.velux.com//velcdn.azureedge.net/~/media/com/api/klf200/technical%20specification%20for%20klf%20200%20api-ver3-16.pdf")

## Known Issues

The following are the known issues with the binding at present. If you spot anything else, please let me know on the OH forums. All feedback and suggestions are welcome!

| # | Issue | Description / Notes
|-----|----------|----------|
|1| Initial State of Nodes | A limitation of the KLF200 appears to be that when it is first powered on, it does not query all of its nodes (eg: blinds and shutters) to determine their position. As such, (in some cases) if after power-on, you request the position of a node (eg: blind), the position is unknown. Only after a node is operated does the KLF200 learn of its position and then subsequently reports correctly. For this binding, this means that when you initially power on your KLF200 and OH connects to it, all of your items may report an unknown state initially. This will correct itself automatically over time as the various nodes are operated either through OH or directly using existing remote controls or switches.


## Credits

I would like to particularly acknowledge the work of Guenther Schreiner in his creation of the original Openhab1 firmware v0.1 binding for the KLF200: https://github.com/openhab/openhab1-addons/tree/master/bundles/binding/org.openhab.binding.velux

I got plenty of inspiration from the v1 binding and in particular some fragments of his code pertaining to the SLIP protocol were re-used.
