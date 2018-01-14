# Loxone Binding

This binding integrates [Loxone Miniserver](https://www.loxone.com/enen/products/miniserver-extensions/) with [openHAB](http://www.openhab.org/).
Miniserver is represented as a [Thing](http://docs.openhab.org/configuration/things.html). Miniserver controls, that are visible in the Loxone [UI](https://www.loxone.com/enen/kb/user-interface-configuration/), are exposed as openHAB channels.

## Features

The following features are currently supported:

*   [Discovery](https://en.wikipedia.org/wiki/Simple_Service_Discovery_Protocol) of Miniservers available on the local network
*   Creation of channels for Loxone controls that are exposed in the Loxone [UI](https://www.loxone.com/enen/kb/user-interface-configuration/)
*   Tagging of channels and [items](http://docs.openhab.org/configuration/items.html) with tags that can be recognized by [Alexa](https://en.wikipedia.org/wiki/Amazon_Alexa) openHAB [skill](https://www.amazon.com/openHAB-Foundation/dp/B01MTY7Z5L), so voice can be used to command Loxone controls
*   Management of a Websocket connection to the Miniserver and updating Thing status accordingly
*   Updates of openHAB channel's state in runtime according to control's state changes on the Miniserver
*   Passing channel commands to the Miniserver's controls

## Things

This binding supports [Loxone Miniservers](https://www.loxone.com/enen/products/miniserver-extensions/) for accessing controls that are configured in their UI.

The Thing UID of automatically discovered Miniservers is: `loxone:miniserver:<serial>`, where `<serial>` is a serial number of the Miniserver (effectively this is the MAC address of its network interface).

### Discovery

[Loxone Miniservers](https://www.loxone.com/enen/products/miniserver-extensions/) are automatically discovered by the binding and put in the Inbox. [Discovery](https://en.wikipedia.org/wiki/Simple_Service_Discovery_Protocol) is performed using [UPnP](https://en.wikipedia.org/wiki/Universal_Plug_and_Play) protocol.

Before a Miniserver Thing can go online, it must be configured with a user name and a password of an account available on the Miniserver.
Please set them manually in Thing configuration after you add a new Miniserver Thing from your Inbox.

### Manual configuration

As an alternative to the automatic discovery process, Miniservers can be configured manually, through an entry in [.things file](http://docs.openhab.org/configuration/things.html#defining-things-using-files).
The entry should have the following syntax:

`loxone:miniserver:<thing-id> [ user="<user>", password="<password>", host="<host>", port=<port>, ... ]`

Where:

*   `<thing-id>` is a unique ID for your Miniserver (you can but do not have to use Miniserver's MAC address here)
*   `<user>` and `<password>` are the credentials used to log into the Miniserver
*   `<host>` is a host name or IP of the Miniserver
*   `<port>` is a port of web services on the Miniserver (please notice that port, as a number, is not surrounded by quotation marks, while the other values described above are)
*   `...` are optional advanced parameters - please refer to _Advanced parameters_ section at the end of this instruction for a list of available options

Example 1 - minimal required configuration:

        `loxone:miniserver:504F2414780F [ user="kryten", password="jmc2017", host="loxone.local", port=80 ]`

Example 2 - additionally keep alive period is set to 2 minutes and Websocket maximum binary message size to 8MB:

        `loxone:miniserver:504F2414780F [ user="kryten", password="jmc2017", host="192.168.0.210", port=80, keepAlivePeriod=120, maxBinMsgSize=8192 ]`

### Thing Offline Reasons

There can be following reasons why Miniserver status is `OFFLINE`:

*   __Configuration Error__
    *   _Unknown host_
    *   Miniserver host/ip address can't be resolved. No connection attempt will be made.
    *   _User not authorized_
        *   Invalid user name or password or user not authorized to connect to the Miniserver. Binding will make another attempt to connect after some time.
    *   _Too many failed login attempts - stopped trying_
        *   Miniserver locked out user for too many failed login attempts. In this case binding will stop trying to connect to the Miniserver. A new connection will be attempted only when user corrects user name or password in the configuration parameters.
    *   _Internal error_
        *   Probably a code defect, collect debug data and submit an issue. Binding will try to reconnect, but with unknown chance for success.
    *   _Other_
        *   An exception occured and its details will be displayed
*   __Communication Error__
    *   _Error communicating with Miniserver_
        *   I/O error occurred during established communication with the Miniserver, most likely due to network connectivity issues, Miniserver going offline or Loxone Config is uploading a new configuration. A reconnect attempt will be made soon. Please consult detailed message against one of the following:
            *   _"Text message size &lsqbXX&rsqb exceeds maximum size &lsqbYY&rsqb"_ - adjust text message size in advanced parameters to be above XX value
            *   _"Binary message size &lsqbXX&rsqb exceeds maximum size &lsqbYY&rsqb"_ - adjust binary message size in advanced parameters to be above XX value
    *   _User authentication timeout_
        *   Authentication procedure took too long time and Miniserver closed connection. It should not occur under normal conditions and may indicate performance issue on binding's OS side.
    *   _Timeout due to no activity_
        *   Miniserver closed connection because there was no activity from binding. It should not occur under normal conditions, as it is prevented by sending keep-alive messages from the binding to the Miniserver. By default Miniserver's timeout is 5 minutes and period between binding's keep-alive messages is 4 minutes. If you see this error, try changing the keep-alive period in binding's configuration to a smaller value.
    *   _Other_
        *   An exception occured and its details will be displayed

## Channels

This binding creates channels for controls that are [used in Loxone's user interface](https://www.loxone.com/enen/kb/user-interface-configuration/).
Currently supported controls are presented in the table below.

| [Loxone API Control](https://www.loxone.com/enen/kb/api/) | Loxone Block-Functions                                                                                                                                                                                                                                                                                                    | [Item Types](http://docs.openhab.org/concepts/items.html) | Supported Commands                                                                                                           |
|-----------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| Dimmer                                                    | [Dimmer](https://www.loxone.com/enen/kb/dimmer/)                                                                                                                                                                                                                                                                          | `Dimmer`                                                  | `OnOffType.*`<br>`Percent`                                                                                                   |
| InfoOnlyAnalog                                            | Analog [virtual inputs](https://www.loxone.com/enen/kb/virtual-inputs-outputs/) (virtual state)                                                                                                                                                                                                                           | `Number`                                                  | none (read-only value)                                                                                                       |
| InfoOnlyDigital                                           | Digital [virtual inputs](https://www.loxone.com/enen/kb/virtual-inputs-outputs/) (virtual state)                                                                                                                                                                                                                          | `String`                                                  | none (read-only value)                                                                                                       |
| Jalousie                                                  | Blinds, [Automatic Blinds](https://www.loxone.com/enen/kb/automatic-blinds/), Automatic Blinds Integrated                                                                                                                                                                                                                 | `Rollershutter`                                           | `UpDown.*`<br>`StopMove.*`<br>`Percent`                                                                                      |
| LightController                                           | [Lighting controller V1 (obsolete)](https://www.loxone.com/enen/kb/lighting-controller/), [Hotel lighting controller](https://www.loxone.com/enen/kb/hotel-lighting-controller/)<br>Additionally, for each configured output of a lighting controller, a new independent control (with own channel/item) will be created. | `Number`                                                  | `Decimal` (select lighting scene)<br>`UpDownType.*` (swipe through scenes)<br>`OnOffType.*` (select all off or all on scene) |
| LightControllerV2                                         | [Lighting controller](https://www.loxone.com/enen/kb/lighting-controller-v2/)<br>Additionally, for each configured output and for each mood of a lighting controller, a new independent control (with own channel/item) will be created.                                                                                  | `Number`                                                  | `Decimal` (select mood)<br>`UpDownType.*` (swipe through moods)                                                              |
| LightControllerV2 Mood                                    | A mood defined for a [Lighting controller](https://www.loxone.com/enen/kb/lighting-controller-v2/). Each mood will have own channel and can be operated independently in order to allow mixing of moods.                                                                                                                  | `Switch`                                                  | `OnOffType.*` (mixes mood in or out of the controller)                                                                       |
| Pushbutton                                                | [Virtual inputs](https://www.loxone.com/enen/kb/virtual-inputs-outputs/) of pushbutton type                                                                                                                                                                                                                               | `Switch`                                                  | `OnOffType.ON` (generates Pulse command)                                                                                     |
| Radio                                                     | [Radio button 8x and 16x](https://www.loxone.com/enen/kb/radio-buttons/)                                                                                                                                                                                                                                                  | `Number`                                                  | `Decimal` (select output number 1-8/16 or 0 for all outputs off)<br>`OnOffType.OFF` (all outputs off)                        |
| Switch                                                    | [Virtual inputs](https://www.loxone.com/enen/kb/virtual-inputs-outputs/) of switch type<br>[Push-button](https://www.loxone.com/enen/kb/push-button/)                                                                                                                                                                     | `Switch`                                                  | `OnOffType.*`                                                                                                                |
| TextState                                                 | [State](https://www.loxone.com/enen/kb/state/)                                                                                                                                                                                                                                                                            | `String`                                                  | none (read-only value)                                                                                                       |
| TimedSwitch                                               | [Stairwell light switch](https://www.loxone.com/enen/kb/stairwell-light-switch/) or [Multifunction switch](https://www.loxone.com/enen/kb/multifunction-switch/)                                                                                                                                                          | `Switch` <br> <br> `Number`                               | `OnOffType.*` (ON send pulse to Loxone) <br> <br> Read-only countdown value to off                                           |


If your control is supported, but binding does not recognize it, please check if it is exposed in Loxone UI using [Loxone Config](https://www.loxone.com/enen/kb-cat/loxone-config/) application.

Channel ID is defined in the following way:

*   `loxone:miniserver:<serial>:<control-UUID>`

Channel label is defined in the following way:

*   For controls that belong to a room: `<Room name> / <Control name>`
*   For controls without a room: `<Control name>`

## Advanced Parameters

This section describes the optional advanced parameters that can be configured for a Miniserver. They can be set using UI (e.g. PaperUI) or in a .things file.
If a parameter is not explicitly defined, binding will use its default value.

To define a parameter value in a .things file, please refer to it by parameter's ID, for example:

        `keepAlivePeriod=120`

### Timeouts

Timeout values control various parts of Websocket connection management.
They can be tuned, when abnormal behavior of the binding is observed, which can be attributed to timing.
<br>

| ID                | Name                                          | Range    | Default | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
|-------------------|-----------------------------------------------|----------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `firstConDelay`   | First connection delay                        | 0-120 s  | 1 s     | Time in seconds between binding initialization with all necessary parameters and first connection attempt.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| `keepAlivePeriod` | Period between connection keep-alive messages | 1-600 s  | 240 s   | Time in seconds between sending two consecutive keep-alive messages, in order to inform Miniserver about active connection and prevent it from disconnecting. Miniserver default connection timeout is 5 minutes, so default is set to 4 minutes.                                                                                                                                                                                                                                                                                                                                                                       |
| `connectErrDelay` | Connect error delay                           | 0-600 s  | 10 s    | Time in seconds between failed Websocket connect attempt and another attempt to connect. Websocket connection is established before authentication and data transfer. It can usually fail due to unreachable Miniserver.                                                                                                                                                                                                                                                                                                                                                                                                |
| `responseTimeout` | Response timeout                              | 0-60 s   | 4 s     | Time to wait for a response from Miniserver to a request sent from the binding. A request can be any of: websocket connect request, credentials hashing key request, configuration request, enabling of state updates (until initial states are received). If this time passed without the expected reaction from the Miniserver, the connection will be closed. A new connection attempt may be made, depending on the situation.                                                                                                                                                                                      |
| `userErrorDelay`  | Authentication error delay                    | 0-3600 s | 60 s    | Time in seconds between user authentication error and another connection attempt. User authentication error can be a result of a wrong name or password, or no authority granted to the user on the Miniserver. If this time is too short, Miniserver will eventually lock out the user for a longer period of time due to too many failed login attempts. This time should allow the administrator to fix the authentication issue without being locked out. Connection retry is required, because very rarely Miniserver seems to reject correct credentials, which are successful on a subsequent identical attempt. |
| `comErrorDelay`   | Communication error delay                     | 0-3600 s | 30 s    | Time in seconds between an active connection closes, as a result of a communication error, and next connection attempt. This relates to all types of network communication issues, which can occur and cease to exist randomly to the binding. It is desired that the binding monitors the situation and brings things back to online as soon as Miniserver is accessible.                                                                                                                                                                                                                                              |

### Sizes

| ID               | Name                             | Range    | Default     | Description                                                                                                                                                                                                                                      |
|------------------|----------------------------------|----------|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `maxBinMsgSize`  | Maximum binary message size (kB) | 0-100 MB | 3072 (3 MB) | For Websocket client, a maximum size of a binary message that can be received from the Miniserver. If you get communication errors with a message indicating there are too long binary messages received, you may need to adjust this parameter. |
| `maxTextMsgSize` | Maximum text message size (kB)   | 0-100 MB | 512 KB      | For Websocket client, a maximum size of a text message that can be received from the Miniserver. If you get communication errors with a message indicating there are too long text messages received, you may need to adjust this parameter.     |


## Limitations

*   As there is no push button item type in openHAB, Loxone's push button is an openHAB's switch, which always generates a short pulse on changing its state to on.
If you use simple UI mode and framework generates items for you, switches for push buttons will still be toggle switches.
To change it to the push button style, you have to create item manually with `autoupdate=false` parameter.
An example of such item definition is given in the _Items_ section above.

## Automatic Configuration Example

The simplest and quickest way of configuring a Loxone Miniserver with openHAB is to use automatic configuration features:

*   Make sure your Miniserver is up and running and on the same network segment as openHAB server.
*   Add Loxone binding from the available `Add-ons`.
*   In `Configuration/System` page, set `Item Linking` to `Simple Mode` (don't forget to save your choice).
*   Add your Miniserver Thing from the `Inbox`, after automatic discovery is performed by the framework during binding initialization.
*   Configure your Miniserver by editing Miniserver Thing in `Configuration/Things` page and providing user name and password.
*   Miniserver Thing should go online. Channels and Items will be automatically created and configured.
*   On the `Control` page, you can test Miniserver Items and interact with them.
*   As the user interface, you may use [HABPanel](http://docs.openhab.org/addons/uis/habpanel/readme.html), where all Miniserver's items are ready for picking up, using entirely the graphical user interface.

## Manual Configuration Example

A more advanced setup requires manual creation and editing of openHAB configuration files, according to the instructions provided in [configuration user guide](http://docs.openhab.org/configuration/index.html).
In this example we will manually configure:

*   A Miniserver with serial number 504F2414780F, available at IP 192.168.0.220 and with web services port 80
*   A Miniserver's user named "kryten" and password "jmc2017"
*   Items for:
    *   Temperature of the Miniserver - a Virtual Analog State functional block
    *   State of a garage door - a Virtual Digital State funtional block (ON=door open, OFF=door closed)
    *   Kitchen lights switch - a Switch Subcontrol at the AI1 output of a Lighting Controller functional block (with a tag recognizable by Alexa service)
    *   Pushbutton to switch all lights off - a Virtual Input of Pushbutton type functional block (pushbutton realized by adding `autoupdate="false"` parameter)
    *   Kitchen blinds - a Jalousie functional block
    *   Lighting scene - a Lighting Controller functional block
    *   Output valve selection for garden watering - 8x Radio Button functional block, where only one valve can be open at a time
    *   A text displaying current alarm's state - a State functional block

### things/loxone.things:

```
loxone:miniserver:504F2414780F [ user="kryten", password="jmc2017", host="192.168.0.220", port=80
  ```

### items/loxone.items:

```
// Type       ID              Label                                  Icon          Tags         Settings

Number        Miniserver_Temp "Miniserver temperature: [%.1f °C]"    <temperature>              {channel="loxone:miniserver:504F2414780F:0F2F2133-017D-3C82-FFFF203EB0C34B9E"}
Switch        Garage_Door     "Garage door [MAP(garagedoor.map):%s]" <garagedoor>               {channel="loxone:miniserver:504F2414780F:0F2F2133-017D-3C82-FFFF203EB0C34B9E"}
Switch        Kitchen_Lights  "Kitchen Lights"                       <switch>      ["lighting"] {channel="loxone:miniserver:504F2414780F:0EC5E0CF-0255-6ABD-FFFF402FB0C24B9E_AI1"}
Switch        Stair_Lights    "Stair Lights"                         <switch>      ["lighting"] {channel="loxone:miniserver:504F2414780F:0EC5E0CF-0255-31BD-FFFF402FB0C24B9E"}
Number        Stair_Lights-1  "Stair Lights Deactivation Delay"      <clock>       ["lighting"] {channel="loxone:miniserver:504F2414780F:0EC5E0CF-0255-31BD-FFFF402FB0C24B9E-1"}
Switch        Reset_Lights    "Switch all lights off"                <switch>      ["lighting"] {channel="loxone:miniserver:504F2414780F:0F2F2133-01AD-3282-FFFF201EB0C24B9E",autoupdate="false"}
Rollershutter Kitchen_Blinds  "Kitchen blinds"                       <blinds>                   {channel="loxone:miniserver:504F2414780F:0F2E2123-014D-3232-FFEF204EB3C24B9E"}
Dimmer        Kitchen_Dimmer  "Kitchen dimmer"                       <slider>      ["lighting"] {channel="loxone:miniserver:504F2414780F:0F2E2123-014D-3232-FFEF207EB3C24B9E"}
Number        Light_Scene     "Lighting scene"                       <light>                    {channel="loxone:miniserver:504F2414780F:0FC4E0DF-0255-6ABD-FFFE403FB0C34B9E"}
Number        Mood_Selector   "Lighting mood"                        <light>                    {channel="loxone:miniserver:504F2414780F:0FC4E0DF-0255-6ABD-FFFE203EA0C34B9E"}
Switch        Mood_Enter_Home "Entering home"                        <light>                    {channel="loxone:miniserver:504F2414780F:0FC4E0DF-0255-6ABD-FFFE203EA0C34B9E-M1"}
Switch        Mood_Read_Book  "Reading book"                         <light>                    {channel="loxone:miniserver:504F2414780F:0FC4E0DF-0255-6ABD-FFFE203EA0C34B9E-M2"}
Switch        Mood_Evening    "Evening setup"                        <light>                    {channel="loxone:miniserver:504F2414780F:0FC4E0DF-0255-6ABD-FFFE203EA0C34B9E-M3"}
Number        Garden_Valve    "Garden watering section"              <garden>                   {channel="loxone:miniserver:504F2414780F:0FC5E0DF-0355-6AAD-FFFE403FB0C34B9E"}
String        Alarm_State     "Alarm state [%s]"                     <alarm>                    {channel="loxone:miniserver:504F2414780F:0F2E2134-017D-3E82-FFFF433FB4A34B9E"}
```

### sitemaps/loxone.sitemap:

```
sitemap loxone label="Loxone Example Menu"
{
    Frame label="Demo Controls" {
        Text      item=Miniserver_Temp
        Text      item=Garage_Door
        Switch    item=Kitchen_Lights
        Switch    item=Reset_Lights
        Switch    item=Kitchen_Blinds
        Slider    item=Kitchen_Dimmer switchSupport
        Switch    item=Stairs_Light
        Text      item=Stairs_Light-1
        Selection item=Light_Scene mappings=[0="All off", 1="My scene 1", 2="My scene 2", 9="All on"]
        Selection item=Mood_Selector
        Switch    item=Mood_Enter_Home
        Switch    item=Mood_Read_Book
        Switch    item=Mood_Evening
        Setpoint  item=Garden_Valve minValue=0 maxValue=8 step=1
        Text      item=Alarm_State
    }
}
```

### transform/garagedoor.map:

```java
OFF=Closed
ON=Open
-=Unknown
```
