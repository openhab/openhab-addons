# Loxone Binding

This binding integrates [Loxone Miniserver](https://www.loxone.com/enen/products/miniserver-extensions/) with [OpenHAB](http://www.openhab.org/). Miniserver is represented as a [Thing](http://docs.openhab.org/configuration/things.html). Miniserver controls, that are visible in the Loxone [UI](https://www.loxone.com/enen/kb/user-interface-configuration/), are exposed as OpenHAB channels.

Binding has the Loxone-specific code separated in a .core package. This code does not depend on the openHAB framework and can be easily used to handle Loxone Miniservers in other Java applications.

## Features
Following features are currently supported:
* [Discovery](https://en.wikipedia.org/wiki/Simple_Service_Discovery_Protocol) of Miniservers available on the local network
* Creation of channels for Loxone controls that are exposed in the Loxone [UI](https://www.loxone.com/enen/kb/user-interface-configuration/)
* Tagging of channels and [items](http://docs.openhab.org/configuration/items.html) with tags that can be recognized by [Alexa](https://en.wikipedia.org/wiki/Amazon_Alexa) openHAB [skill](https://www.amazon.com/openHAB-Foundation/dp/B01MTY7Z5L), so voice can be used to command Loxone controls
* Management of a Websocket connection to the Miniserver and updating Thing status accordingly
* Updates of OpenHAB channel's state in runtime according to control's state changes on the Miniserver
* Passing channel commands to the Miniserver's controls

## Supported Things

This binding supports [Loxone Miniservers](https://www.loxone.com/enen/products/miniserver-extensions/) for accessing controls that are configured in their UI.

Thing ID is defined in the following way: `loxone:miniserver:<serial>`, where <serial> is a serial number of the Miniserver (effectively this is the MAC address of its network interface).

## Discovery

[Loxone Miniservers](https://www.loxone.com/enen/products/miniserver-extensions/) are automatically discovered by the binding and put in the Inbox. [Discovery](https://en.wikipedia.org/wiki/Simple_Service_Discovery_Protocol) is performed using [UPnP](https://en.wikipedia.org/wiki/Universal_Plug_and_Play) protocol.

Before a Miniserver Thing can go online, it must be configured with a user name and a password of an account available on the Miniserver. Please set them manually in Thing configuration after you add a new Miniserver Thing from your Inbox.

## Channels

This binding creates channels for controls that are [used in Loxone's user interface](https://www.loxone.com/enen/kb/user-interface-configuration/). Each control may have one of more channels, depending on various states it has. Currently supported controls are presented in the table below.

|[Loxone API Control](https://www.loxone.com/enen/kb/api/)|Loxone Block-Functions|[Item Types](http://docs.openhab.org/concepts/items.html)|Supported Commands|Channel Types|Channel IDs|
|----|----|----|----|----|----|
|InfoOnlyAnalog|Analog [virtual inputs](https://www.loxone.com/enen/kb/virtual-inputs-outputs/) (virtual state) |`Number`|none (read-only value)|`loxone:miniserver:<serial>:infoonlyanalog:<uuid>`<br> This channel type is created dynamically for each control, because control contains custom display format string|`loxone:miniserver:<serial>:<uuid>`|
|InfoOnlyDigital|Digital [virtual inputs](https://www.loxone.com/enen/kb/virtual-inputs-outputs/) (virtual state) |`String`<br>`Number`|none (read-only value)|`loxone:miniserver:<serial>:infoonlydigital`| `loxone:miniserver:<serial>:<uuid>`|
|Jalousie| Blinds, [Automatic Blinds](https://www.loxone.com/enen/kb/automatic-blinds/), Automatic Blinds Integrated| `Rollershutter`| `UpDown.*`<br>`StopMove.*`<br>`Percent`|`loxone:miniserver:<serial>:rollershutter`|`loxone:miniserver:<serial>:<uuid>`
|LightController|[Lighting controller](https://www.loxone.com/enen/kb/lighting-controller/), [Hotel lighting controller](https://www.loxone.com/enen/kb/hotel-lighting-controller/)<br>Additionally, for each configured output of a lighting controller, a new independent control (with own channel/item) will be created.|`Number`|`Decimal` (select lighting scene)<br>`OnOffType.*` (select all off or all on scene)|`loxone:miniserver:<serial>:lightcontroller:<uuid>`<br>This channel type is created dynamically for each controller, because it contains custom list of selectable values.|`loxone:miniserver:<serial>:<uuid>`|
|Pushbutton | [Virtual inputs](https://www.loxone.com/enen/kb/virtual-inputs-outputs/) of pushbutton type | `Switch` | `OnOffType.ON` (generates Pulse command)|`loxone:miniserver:<serial>:switch`|`loxone:miniserver:<serial>:<uuid>`
|Radio|[Radio button 8x and 16x](https://www.loxone.com/enen/kb/radio-buttons/)|`Number`|`Decimal` (select output number 1-8/16 or 0 for all outputs off)<br>`OnOffType.OFF` (all outputs off)|`loxone:miniserver:<serial>:radio:<uuid>`<br>This channel type is created dynamically for each radio button, because it contains custom list of selectable value.|`loxone:miniserver:<serial>:<uuid>`
|Switch | [Virtual inputs](https://www.loxone.com/enen/kb/virtual-inputs-outputs/) of switch type<br>[Push-button](https://www.loxone.com/enen/kb/push-button/) | `Switch` |`OnOffType.*`|`loxone:miniserver:<serial>:switch`|`loxone:miniserver:<serial>:<uuid>`
|TextState|[State](https://www.loxone.com/enen/kb/state/)|`String`|none (read-only value)|`loxone:miniserver:<serial>:text`|`loxone:miniserver:<serial>:<uuid>`|

If your control is supported, but binding does not recognize it, please check if it is exposed in Loxone UI using [Loxone Config](https://www.loxone.com/enen/kb-cat/loxone-config/). application.

Channel ID is defined in the following way: 

  * For primary control's channel: `loxone:miniserver:<serial>:<control-UUID>`
  * For other control's channels (currently no such controls): `loxone:miniserver:<serial>:<control-UUID>-<channel-index>`, where `channel-index >=1`


### Loxone and Amazon Alexa

Your OpenHAB server can be exposed through [myopenHAB](http://www.myopenhab.org/) cloud service to  [Amazon Alexa](https://en.wikipedia.org/wiki/Amazon_Alexa) device with [openHAB skill](https://www.amazon.com/openHAB-Foundation/dp/B01MTY7Z5L) enabled. To enable this service, please consult instructions available [here](https://community.openhab.org/t/official-alexa-smart-home-skill-for-openhab-2/23533).

When creating a Miniserver Thing in the openHAB's Item Linking Simple Mode, Loxone binding will automatically create item tags required by Alexa, so that Miniserver's controls can be discovered by [Alexa smart home]( https://www.amazon.com/alexasmarthome) module. Tags will be created for switches, which belong to a category of "lighting" type. This will allow you to command Loxone controls with your voice.

Alexa will recognize items by their labels, which will be equal to the corresponding control's name on the Miniserver. In case your controls are named in a way not directly suiting voice commands, you will need to manually add and link new items to the channels, and add proper tags as described in the above instructions.

Please consult [tutorial](http://docs.openhab.org/tutorials/beginner/configuration.html) on what simple mode is and how to enable it.

## Thing Offline Reasons
There can be following reasons why Miniserver status is `OFFLINE`:

* __Configuration Error__
    * _Unknown host_
        * Miniserver host/ip address can't be resolved. No connection attempt will be made.
    * _User not authorized_
        * Invalid user name or password or user not authorized to connect to the Miniserver. Binding will make another attempt to connect after some time.
    * _Too many failed login attempts - stopped trying_
        * Miniserver locked out user for too many failed login attempts. In this case binding will stop trying to connect to the Miniserver. A new connection will be attempted only when user corrects user name or password in the configuration parameters.
    * _Internal error_
        * Probably a code defect, collect debug data and submit an issue. Binding will try to reconnect, but with unknown chance for success.
    * _Other_
        * An exception occured and its details will be displayed
* __Communication Error__
    * _Error communicating with Miniserver_
        * I/O error occurred during established communication with the Miniserver, most likely due to network connectivity issues, Miniserver going offline or Loxone Config is uploading a new configuration. A reconnect attempt will be made soon. Please consult detailed message against one of the following:
            * _"Text message size [XX] exceeds maximum size [YY]"_ - adjust text message size in advanced parameters to be above XX value
            * _"Binary message size [XX] exceeds maximum size [YY]"_ - adjust binary message size in advanced parameters to be above XX value
    * _User authentication timeout_
        * Authentication procedure took too long time and Miniserver closed connection. It should not occur under normal conditions and may indicate performance issue on binding's OS side.
    * _Timeout due to no activity_
        * Miniserver closed connection because there was no activity from binding. It should not occur under normal conditions, as it is prevented by sending keep-alive messages from the binding to the Miniserver. By default Miniserver's timeout is 5 minutes and period between binding's keep-alive messages is 4 minutes. If you see this error, try changing the keep-alive period in binding's configuration to a smaller value.
    * _Other_
        * An exception occured and its details will be displayed

## Advanced Parameters

### Timeouts

Timeout values control various parts of Websocket connection management. They can be tuned, when abnormal behavior of the binding is observed, which can be attributed to timing.

Timeout values can be changed in advanced parameters section of the thing's configuration page.

* _First connection delay_
    * Time in seconds between binding initialization with all necessary parameters and first connection attempt.
    * Range: 0-120 s, default: 1 s
* _Period between connection keep-alive messages_
    * Time in seconds between sending two consecutive keep-alive messages, in order to inform Miniserver about active connection and prevent it from disconnecting.
    * Range: 1-600 s, default: 240 s (4 minutes, Miniserver default connection timeout is 5 minutes)
* _Connect error delay_
    * Time in seconds between failed Websocket connect attempt and another attempt to connect. Websocket connection is established before authentication and data transfer. It can usually fail due to unreachable Miniserver.
    * Range: 0-600 s, default: 10 s
* _Authentication error delay_
    * Time in seconds between user authentication error and another connection attempt. User authentication error can be a result of a wrong name or password, or no authority granted to the user on the Miniserver. If this time is too short, Miniserver will eventually lock out user for longer period of time due to too many failed login attempts. This time should allow administrator to fix authentication issue without being locked out. Connection retry is required, because very rarely Miniserver seems to reject correct credentials, which pass on another exactly same attempt.
    * Range: 0-3600 s, default: 60 s
* _Communication error delay_
    * Time in seconds between active connection close, as a result of communication error, and next connection attempt. This relates to all types of network communication issues, which can occur and cease to exist randomly to the binding. It is desired that the binding monitors the situation and brings things back to online as soon as Miniserver is accesible.
    * Range: 0-3600 s, default: 30 s

### Sizes

* _Maximum binary message size (kB)_
    * For Websocket client, a maximum size of a binary message that can be received from the Miniserver. If you get communication errors with a message indicating there are too long binary messages received, you may need to adjust this parameter.
    * Range: 0-100 MB, default: 3 MB
* _Maximum text message size (kB)_
    * For Websocket client, a maximum size of a text message that can be received from the Miniserver. If you get communication errors with a message indicating there are too long text messages received, you may need to adjust this parameter.
    * Range: 0-100 MB, default: 512 KB

## Limitations
* As there is no push button item type in OpenHAB, Loxone's push button is an OpenHAB's switch, which always generates a short pulse on changing its state to on. If you use simple UI mode and framework generates items for you, switches for push buttons will still be toggle switches. To change it to push button style, you have to create item manually with autoupdate=false parameter.
