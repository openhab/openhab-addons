# Samsung TV Binding

This binding integrates the [Samsung TV's](https://www.samsung.com).

## Supported Things

There is one Thing per TV.

## Discovery

The TV's are discovered through UPnP protocol in the local network and all devices are put in the Inbox. TV must be ON for this to work.

## Binding Configuration

Basic operation does not require any special configuration. 

The binding has the following configuration options, which can be set for "binding:samsungtv":

| Parameter             | Name                      | Description                                                   | Required  |
|-----------------------|---------------------------|---------------------------------------------------------------|-----------|
| hostName              | Host Name                 | Network address of the Samsung TV                             | yes       |
| port                  | TCP Port                  | TCP port of the Samsung TV                                    | no        |
| macAddress            | MAC Address               | MAC Address of the Samsung TV                                 | no        |
| refreshInterval       | Refresh Interval          | States how often a refresh shall occur in milliseconds        | no        |
| protocol              | Remote Control Protocol   | The type of remote control protocol                           | yes       |
| webSocketToken        | Websocket Token           | Security token for secure websocket connection                | no        |
| subscription          | Subscribe to UPNP         | Reduces polling on UPNP devices                               | no        |
| smartThingsApiKey     | Smartthings PAT           | Smartthings Personal Access Token                             | no        |
| smartThingsDeviceId   | Smartthings Device ID     | Smartthings Device ID for this TV                             | no        |

## Thing Configuration

The Samsung TV Thing requires the host name and port address as a configuration value in order for the binding to know how to access it.
Samsung TV's publish several UPnP devices and the hostname is used to recognize those UPnP devices.
Port address is used for remote control emulation protocol.
Additionally, a refresh interval can be configured in milliseconds to specify how often TV resources are polled. Default is 1000 ms.  

E.g.

```java
Thing samsungtv:tv:livingroom [ hostName="192.168.1.10", port=55000, macAddress="78:bd:bc:9f:12:34", refreshInterval=1000 ]
```

Different ports are used on different models. It may be 55000, 8001 or 8002.

If you have a <2016 TV, the interface will be *Legacy*, and the port is likely 55000.  
If you have a >2016 TV, the interface will be either *websocket* on port 8001, or *websocketsecure* on port 8002.  
If your TV supports *websocketsecure*, you **MUST** use it, otherwise the `keyCode` and all dependent channels will not work.

In order for the binding to control your TV, you will be asked to accept the remote connection (from openHAB) on your TV. You have 30 seconds to accept the connection. If you fail to accept it, then most channels will not work.  
Once you have accepted the connection, the returned token is stored in the binding, so you don't have to repeat this every time openHAB is restarted.  

If the connection has been refused, or you don't have your TV configured to allow remote connections, the binding will not work. If you are having problems, check the settings on your TV, sometimes a family member denies the popup (because they don't know what it is), and after that nothing will work.  
You can set the connection to `Allow` on the TV, or delete the openHAB entry, and try the connection again.

The binding will try to automatically discover the correct protocol for your TV, so **don't change it** unless you know it is wrong.

Under `advanced`, you can enter a Smartthings PAT, and Device Id. This enables more channels via the Smartthings cloud. This is only for TV's that support Smartthings. No hub is required. The binding will attempt to discover the device ID for your TV automatically, you can enter it manually if automatic detection fails.  
Also under `advanced`, you have the ability to turn on *"Subscribe to UPnP events"*. This is off by default. This option reduces (but does not eliminate) the polling of UPnP services. You can enable it if you want to test it out. If you disable this setting (after testing), you should power cycle your TV to remove the old subscriptions.

For >2019 TV's, there is an app workaround, see [App Discovery](#app-discovery) for details.

## Channels

TVs support the following channels:

| Channel Type ID     | Item Type| Access Mode| Description                                                                                             |
|---------------------|----------|------------|---------------------------------------------------------------------------------------------------------|
| volume              | Dimmer   | RW         | Volume level of the TV.                                                                                 |
| mute                | Switch   | RW         | Mute state of the TV.                                                                                   |
| brightness          | Dimmer   | RW         | Brightness of the TV picture.                                                                           |
| contrast            | Dimmer   | RW         | Contrast of the TV picture.                                                                             |
| sharpness           | Dimmer   | RW         | Sharpness of the TV picture.                                                                            |
| colorTemperature    | Number   | RW         | Color temperature of the TV picture. Minimum value is 0 and maximum 4.                                  |
| sourceName          | String   | RW         | Name of the current source (eg HDMI1).                                                                  |
| sourceId            | Number   | RW         | Id of the current source.                                                                               |
| channel             | Number   | RW         | Selected TV channel number.                                                                             |
| programTitle        | String   | R          | Program title of the current channel.                                                                   |
| channelName         | String   | R          | Name of the current TV channel.                                                                         |
| url                 | String   | W          | Start TV web browser and go the given web page.                                                         |
| stopBrowser         | Switch   | W          | Stop TV's web browser and go back to TV mode.                                                           |
| keyCode             | String   | W          | The key code channel emulates the infrared remote controller and allows to send virtual button presses. |
| sourceApp           | String   | RW         | Currently active App.                                                                                   |
| power               | Switch   | RW         | TV power. Some of the Samsung TV models doesn't allow to set Power ON remotely.                         |
| artMode             | Switch   | RW         | TV art mode for Samsung The Frame TV's.                                                                 |
| setArtMode          | Switch   | W          | Manual input for setting internal ArtMode tracking for Samsung The Frame TV's >2021.                    |
| artImage            | Image    | RW         | The currently selected art (thumbnail)                                                                  |
| artLabel            | String   | RW         | The currently selected art (label) - can also set the current art                                       |
| artJson             | String   | RW         | Send/receive commands from the TV art websocket Channel                                                 |
| artBrightness       | Dimmer   | RW         | ArtMode Brightness                                                                                      |
| artColorTemperature | Number   | RW         | ArtMode Color temperature Minnimum value is -5 and maximum 5                                            |

**NOTE:** channels: brightness, contrast, sharpness, colorTemperature don't work on newer TV's.  
**NOTE:** channels: sourceName, sourceId, programTitle, channelName and stopBrowser may need additional configuration. 

Some channels do not work on some TV's. It depends on the age of your TV, and what kind of interface it has. Only link channels that work on your TV, polling channels that your TV doesn't have may cause errors, and other problems. see [Tested TV Models](#tested-tv-models).

### keyCode channel:

`keyCode` is a String channel, that emulates a remote control. it allows you to send keys to the TV, as if they were from the remote control, hence it is send only.

This is one of the more useful channels, and several new features have been added in this binding.
Now all keyCode channel sends are queued, so they don’t overlap each other. You can also now use in line delays, and keypresses (in mS). for example:  
sending:
`"KEY_MENU, 1000, KEY_DOWN, KEY_DOWN, KEY_ENTER, 2000, KEY_EXIT"`

Results in a 1 second pause after `KEY_MENU` before `KEY_DOWN` is sent, and a 2 second delay before `KEY_EXIT` is sent. The other commands have 300mS delays between them.

**NOTE:** the delay replaces the 300 mS default delay (so 1000 is 1 second, not 1.3 seconds).

To send keyPresses (like a long press of the power button), you would send:
`"-4000,KEY_POWER"`
This sends a 4 second press of the power button. You can combine these with other commands and delays like this:
`"-3000, KEY_RETURN, 1000, KEY_MENU"`
This does a long press (3 seconds) of the RETURN key (on my TV this exits Netflix or Disney+ etc), then waits 1 second, then exits the menu.

The delimiter is `,`.

By not overlapping, I mean that if you send two strings one after the other, they are executed sequentially. ie:
sending
`"-3000, KEY_RETURN, 100, KEY_MENU"`
immediately followed by:
`"KEY_EXIT"`
would send a long press of return, a 1 second pause, then menu, followed by exit 300mS later.

Spaces are ignored. The supported keys can be listed in the Thing `keyCode` channel

Mouse events and text entry are now supported. Send `{"x":0, "y":0}` to move the mouse to 0,0, send `LeftClick` or `RightClick` to click the mouse.  
Send `"text"` to send the word text to the TV. Any text that you want to send has to be enclosed in `"` to be recognized as a text entry.

Here is an example to fill in the URL if you launch the browser:

```java
TV_keyCode.sendCommand("3000,{\"x\":0, \"y\":-430},1000,KEY_ENTER,2000,\"http://your_url_here\"")
```

Another example:

```java
TV_keyCode.sendCommand("{\"x\":0, \"y\":-430},1000,LeftClick")
```

**NOTE:** You have to escape the `"` in the string.

### url

`url` is a String channel, but on later TV's (>2016) it will not fill in the url for you. It will launch the browser, you can then use a rule to read the url (from the channel) and use the `keyCode` channel to enter the URL. Bit of a kludge, but it works.

The `sourceApp` channel will show `Internet` (if configured correctly) and sending `""` to the `sourceapp` channel will exit the browser. You can also send `ON` to the `stopBrowser` channel.

### stopBrowser

`stopbrowser` is a Switch channel. Sending `ON` exits the current app, sending `OFF` sends a long press of the `KEY_EXIT` button (3 seconds).

### Power

The power channel is available on all TV's. Depending on the age of your TV, you may not be able to send power ON commands (see [WOL](#wol)). It should represent the ON state of your TV though.

## Frame TV's

Frame TV's have additional channels.
**NOTE:** If you don't have a Frame TV, don't link the `art` channels, it will confuse the binding, especially power control.

### artMode:

`artMode` is a Switch channel. When `power` is ON, `artMode` will be OFF. If the `artMode` channel is commanded `OFF`, then the TV will power down to standby/off mode (this takes 4 seconds).  
Commanding ON to `artMode` will try to power up the TV in art mode, and commanding ON to `power` will try to power the TV up in ON mode, but see WOL limitations.  

To determine the ON/ART/OFF state of your TV, you have to read both `power` and `artMode`.

**NOTE:** If you don't have a Frame TV, don't use the `artMode` channel, it will confuse the power handling logic.

### setArtMode:

**NOTE** Samsung added back the art API in Firmware 1622 to >2021 Frame TV's. If you have this version of firmware or higher, don't use the `setArtMode` channel, as it is not neccessary.

`setArtMode` is a Switch channel. Since Samsung removed the art api in 2022, the TV has no way of knowing if it is in art mode or playing a TV source. This switch is to allow you to manually tell the TV what mode it is in.  

If you only use the binding to turn the TV on and off or to Standby, the binding will keep track of the TV state. If, however you use the remote to turn the TV on or off from art mode, the binding cannot detect this, and the power state will become invalid.  
This input allows you to set the internal art mode state from an external source (say by monitoring the power usage of the TV, or by querying the ex-link port) - thus keeping the power state consistent.

**NOTE:** If you don't have a >2021 Frame TV, don't use the `setArtMode` channel, it will confuse the power handling logic.

### artImage:

`artImage` is an Image channel that receives a thumbnail of the art that would be displayed in artMode (even if the TV is on). It receives iimages only (you can't send a command to it due to openHAB lmitations).

### artLabel:

`artlabel` is a String channel that receives the *intenal* lable of the artwork displayed. This will be something like `MY_0010` or `SAM-0123`. `MY` means it's art you uploaded, `SAM` means its from the Samsung art gallery.  
You have to figure out what the label actually represents.  

You can send commands to the channel. It accepts, Strings, string representations of a `Rawtype` image and `RawType` Images. If you send a String, such as `MY-0013`, it will display that art on the TV. If the TV is ON, playing live TV, then the Tv will switch to artMode.  
If you send a `RawType` image, then the image (jpg or png or some other common image format) will be uploaded to the TV, and stored in it's internal storage - if you have space.  

The string representation of a `Rawtype` image is of the form `"data:image/png;base64,iVBORw0KGgoAAA........AAElFTkSuQmCC"` where the data is the base64 encoded binary data. the command would look like this:

```java
TV_ArtLabel.sendCommand("data:image/png;base64,iVBORw0KGgoAAA........AAElFTkSuQmCC")
```

here is an example `sitemap` entry:

```java
Selection item=TV_ArtLabel mappings=["MY_F0061"="Large Bauble","MY_F0063"="Small Bauble","MY_F0062"="Presents","MY_F0060"="Single Bauble","MY_F0055"="Gold Bauble","MY_F0057"="Snowflake","MY_F0054"="Stag","MY_F0056"="Pine","MY_F0059"="Cabin","SAM-S4632"="Snowy Trees","SAM-S2607"="Icy Trees","SAM-S0109"="Whale"]                      
```

### artJson:

`artJson` is a String channel that receives the output of the art websocket channel on the TV. You can also send commands to this channel.

If you send a plain text command, the command is wrapped in the required formatting, and sent to the TV artChannel. you can use this feature to send any supported command to the TV, the response will be returned on the same channel.
If you wrap the command with `{` `}`, then the whole string is treated as a json command, and sent as-is to the channel (basic required fields will be added).

Currently known working commands for 2021 and earlier TV's are:

```
    get_api_version
    get_artmode_status
    set_artmode_status "value" on or off
    get_auto_rotation_status
    set_auto_rotation_status "type" is "slideshow" pr 'shuffelslideshow", "value" is off or duration in minutes "category_id" is a string representing the category
    get_device_info
    get_content_list
    get_current_artwork
    get_thumbnail - downloads thumbnail in same format as uploaded
    send_image - uploads image jpg/png etc
    delete_image_list - list in "content_id"
    select_image - selects image to display (display optional) image label in "content_id", "show" true or false
    get_photo_filter_list
    set_photo_filter
    get_matte_list
    set_matte
    get_motion_timer (and set) valid values: "off","5","15","30","60","120","240", send settiing in "value"
    get_motion_sensitivity (and set) min 1 max 3 set in "value"
    get_color_temperature (and set) min -5 max +5 set in "value"
    get_brightness (and set) min 1 max 10 set in "value"
    get_brightness_sensor_setting (and set) on or off in "value"
```

Currently known working commands for 2022 and later TV's are:

```
    api_version
    get_artmode_status
    set_artmode_status "value" on or off
    get_slideshow_status
    set_slideshow_status "type" is "slideshow" pr 'shuffelslideshow", "value" is off or duration in minutes "category_id" is a string representing the category
    get_device_info
    get_content_list
    get_current_artwork
    get_thumbnail_list - downloads list of thumbnails in same format as uploaded
    send_image - uploads image jpg/png etc
    delete_image_list - list in "content_id"
    select_image - selects image to display (display optional) image label in "content_id", "show" true or false
    get_photo_filter_list
    set_photo_filter
    get_matte_list
    set_matte
    get_artmode_settings - returns the below values
    set_motion_timer valid values: "off","5","15","30","60","120","240", send settiing in "value"
    set_motion_sensitivity min 1 max 3 set in "value"
    set_color_temperature min -5 max +5 set in "value"
    set_brightness min 1 max 10 set in "value"
    set_brightness_sensor_setting on or off in "value"
```

Some of these commands are quite complex, so I don't reccomend using them eg `get_thumbnail`, `get_thumbnail_list` and `send_image`.
Some are simple, so to get the list of art currently on your TV, just send:

```java
TV_ArtJson.sendCommand("get_content_list")
```

To set the current artwork, but not display it, you would send:

```java
TV_ArtJson.sendCommand("{\"request\":\"select_image\", \"content_id\":\"MY_0009\",\"show\":false}")
```

**NOTE:** You have to escape the `"` in the json string.

These are just the commands I know, there are probably others, let me know if you find more that work.

### artbrightness:

`artBrightness` is a dimmer channel that sets the brightness of the art in ArtMode. It does not affect the TV brightness. Normally the brightness of the artwork is controlled automatically, and the current value is polled and reported via this channel.  
You can change the brightness of the artwork (but automatic control is still enabled, unless you turn it off).

There are only 10 levels of brighness, so you could use a `Setpoint` control for this channel in your `sitemap` - eg:

```java
Slider item=TV_ArtBrightness visibility=[TV_ArtMode==ON]
Setpoint item=TV_ArtBrightness minValue=0 maxValue=100 step=10 visibility=[TV_ArtMode==ON]
```

### artColorTemperature:

`artColorTemperature` is a Number channel, it reports the "warmth" of the artwork from -5 to 5 (default 0). It's not polled, but is updated when artmode status is updated.  
You can use a `Setpoint` contol for this item in your `sitemap` eg:

```java
Setpoint item=TV_ArtColorTemperature minValue=-5 maxValue=5 step=1 visibility=[TV_ArtMode==ON]
```

## Full Example

### samsungtv.things

you can configure the Thing and/or channels/items in text files. The Text configuration for the Thing is like this:

```java
Thing samsungtv:tv:family_room "Samsung The Frame 55" [ hostName="192.168.100.73", port=8002, macAddress="10:2d:42:01:6d:17", refreshInterval=1000, protocol="SecureWebSocket", webSocketToken="16225986", smartThingsApiKey="cae5ac2a-6770-4fa4-a531-4d4e415872be", smartThingsDeviceId="996ff19f-d12b-4c5d-1989-6768a7ad6271", subscription=true ]
```

### samsungtv.items

Channels and items follow the usual conventions.

```java
Group   gLivingRoomTV    "Living room TV" <screen>
Dimmer  TV_Volume        "Volume"         <soundvolume>        (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:volume" }
Switch  TV_Mute          "Mute"           <soundvolume_mute>   (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:mute" }
String  TV_SourceName    "Source Name [%s]"                    (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:sourceName" }
String  TV_SourceApp     "Source App [%s]"                     (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:sourceApp" }
String  TV_ProgramTitle  "Program Title [%s]"                  (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:programTitle" }
String  TV_ChannelName   "Channel Name [%s]"                   (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:channelName" }
String  TV_KeyCode       "Key Code"                            (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:keyCode" }
Switch  TV_Power         "Power [%s]"                          (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:power" }
Switch  TV_ArtMode       "Art Mode [%s]"                       (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:artMode" }
Switch  TV_SetArtMode    "Set Art Mode [%s]"                   (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:setArtMode" }
String  TV_ArtLabel      "Current Art [%s]"                    (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:artLabel" }
Image   TV_ArtImage      "Current Art"                         (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:artImage" }
String  TV_ArtJson       "Art Json [%s]"                       (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:artJson" }
Dimmer  TV_ArtBri        "Art Brightness [%d%%]"               (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:artBrightness" }
Number  TV_ArtCT         "Art CT [%d]"                         (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:artColorTemperature" }
```

## WOL

Wake on Lan is supported by Samsung TV’s after 2016. The binding will attempt to use WOL to turn on a TV, if `power` (or `artMode`) is commanded ON.  
This only works on TV's after 2016, and has some quirks.
 
* Does not work on TV's <2016
* Does not work on hardwired ethernet connected TV's **if you have a soundbar connected via ARC/eARC**
* Works on WiFi connected TV's (with or without soundbar)
* May need to enable this function on the TV
* May have to wait up to 1 minute before turning TV back on, as TV does not power down immediately (and so doesn't respond to WOL)

You will have to experiment to see if it works for you. If not, you can power on the TV using IR (if you have a Harmony Hub, or GC iTach or similar).

## Apps

The `sourceApp` channel is a string channel, it displays the name of the current app, `artMode` or `slideshow` if the TV is in artMode, or blank for regular TV.  
You can launch an app, by sending its name or appID to the channel. if you send `""` to the channel, it closes the current app.

Here is an example `sitemap` entry:

```java
Switch item=TV_SourceApp mappings=["Netflix"="Netflix","Apple TV"="Apple TV","Disney+"="Disney+","Tubi"="Tubi","Internet"="Internet",""="Exit"]
```

### Frame TV

On a Frame TV, you can start a slideshow by sending the slideshow type, followed by a duration (and optional category) eg:

```java
TV_SourceApp.sendCommand("shuffleslideshow,1440")
```

or a sitemap entry:

```java
Switch item=TV_SourceApp label="Slideshow" mappings=["shuffleslideshow,1440"="shuffle 1 day","suffleslideshow,3"="shuffle 3 mins","slideshow,1440"="slideshow 1 day","slideshow,off"="Off"]
```

Sending `slideshow,off` turns the slideshow feature of the TV off.

### App Discovery

Apps are automatically discovered on TV's >2015 and <2020 (or 2019 it's not clear when the API was removed).

**NOTE:** This is an old Apps list, on later TV's the app ID's have changed.  
List of known apps and the respective name that can be passed on to the `sourceApp` channel.
Values are confirmed to work on UE50MU6179.

| App           | Value in sourceApp | Description                       |
|---------------|--------------------|-----------------------------------|
| ARD Mediathek | `ARD Mediathek`    | German public TV broadcasting app |
| Browser       | `Internet`         | Built-in WWW browser              |
| Netflix       | `Netflix`          | Netflix App                       |
| Prime Video   | `Prime Video`      | Prime Video App                   |
| YouTube       | `YouTube`          | YouTube App                       |
| ZDF Mediathek | `ZDF mediathek`    | German public TV broadcasting app |

To discover all installed apps names, you can enable the DEBUG log output from the binding to see a list of apps that have been discovered as installed. This list is displayed once, shortly after the TV is turned On.   

If you have a TV >2019, then the list of apps will not be discovered. Instead, a default list of known appID's is built into the binding, these cover most common apps. The binding will attempt to discover these apps, and, if you are lucky, your app will be found and you have nothing further to do. It is possible that new apps have been added, or are specific to your country that are not in the built in list, in which case you can add these apps manually.  

#### Adding apps manually

If the app you need is not discovered, a file `samsungtv.cfg` will need to be be created in the openHAB config services directory (`/etc/openhab/services/` for Linux systems).  

You need to edit the file `samsungtv.cfg`, and add in the name, appID, and type of the apps you have installed on your TV. Here is a sample for the contents of the `samsungtv.cfg` file:

```java
# This file is for the samsungtv binding
# It contains a list in json format of apps that can be run on the TV
# It is provided for TV >2020 when the api that returns a list of installed apps was removed
# format is:
# { "name":"app name", "appId":"app id", "type":2 }
# Where "app name" is the plain text name used to start or display the app, eg "Netflix", "Disney+"
# "app id" is the internal appId assigned by Samsung in the app store. This is hard to find
# See https://github.com/tavicu/homebridge-samsung-tizen/wiki/Applications for the details
# app id is usually a 13 digit number, eg Netflix is "3201907018807"
# the type is an integer, either 2 or 4. 2 is DEEP_LINK (all apps are this type on >2020 TV's)
# type 4 is NATIVE_LAUNCH and the only app that used to use this was "com.tizen.browser" for the
# built in webbrowser.
# This default list will be overwritten by the list retrived from the TV (if your TV is prior to 2020)
# You should edit this list down to just the apps you have installed on your TV.
# NOTE! it is unknown how accurate this list is!
#
#
{ "name":"Internet"                , "appId":"3202010022079"    , "type":2 }
{ "name":"Netflix"                 , "appId":"3201907018807"    , "type":2 }
{ "name":"YouTube"                 , "appId":"111299001912"     , "type":2 }
{ "name":"YouTube TV"              , "appId":"3201707014489"    , "type":2 }
{ "name":"YouTube Kids"            , "appId":"3201611010983"    , "type":2 }
{ "name":"HBO Max"                 , "appId":"3201601007230"    , "type":2 }
{ "name":"Hulu"                    , "appId":"3201601007625"    , "type":2 }
{ "name":"Plex"                    , "appId":"3201512006963"    , "type":2 }
{ "name":"Prime Video"             , "appId":"3201910019365"    , "type":2 }
{ "name":"Rakuten TV"              , "appId":"3201511006428"    , "type":2 }
{ "name":"Disney+"                 , "appId":"3201901017640"    , "type":2 }
{ "name":"NOW TV"                  , "appId":"3201603008746"    , "type":2 }
{ "name":"NOW PlayTV"              , "appId":"3202011022131"    , "type":2 }
{ "name":"VOYO.RO"                 , "appId":"111299000769"     , "type":2 }
{ "name":"Discovery+"              , "appId":"3201803015944"    , "type":2 }
{ "name":"Apple TV"                , "appId":"3201807016597"    , "type":2 }
{ "name":"Apple Music"             , "appId":"3201908019041"    , "type":2 }
{ "name":"Spotify"                 , "appId":"3201606009684"    , "type":2 }
{ "name":"TIDAL"                   , "appId":"3201805016367"    , "type":2 }
{ "name":"TuneIn"                  , "appId":"121299000101"     , "type":2 }
{ "name":"Deezer"                  , "appId":"121299000101"     , "type":2 }
{ "name":"Radio UK"                , "appId":"3201711015226"    , "type":2 }
{ "name":"Radio WOW"               , "appId":"3202012022468"    , "type":2 }
{ "name":"Steam Link"              , "appId":"3201702011851"    , "type":2 }
{ "name":"Gallery"                 , "appId":"3201710015037"    , "type":2 }
{ "name":"Focus Sat"               , "appId":"3201906018693"    , "type":2 }
{ "name":"PrivacyChoices"          , "appId":"3201909019271"    , "type":2 }
{ "name":"AntenaPlay.ro"           , "appId":"3201611011005"    , "type":2 }
{ "name":"Eurosport Player"        , "appId":"3201703012079"    , "type":2 }
{ "name":"EduPedia"                , "appId":"3201608010385"    , "type":2 }
{ "name":"BBC News"                , "appId":"3201602007865"    , "type":2 }
{ "name":"BBC Sounds"              , "appId":"3202003020365"    , "type":2 }
{ "name":"BBC iPlayer"             , "appId":"3201601007670"    , "type":2 }
{ "name":"The Weather Network"     , "appId":"111399000741"     , "type":2 }
{ "name":"Orange TV Go"            , "appId":"3201710014866"    , "type":2 }
{ "name":"Facebook Watch"          , "appId":"11091000000"      , "type":2 }
{ "name":"ITV Hub"                 , "appId":"121299000089"     , "type":2 }
{ "name":"UKTV Play"               , "appId":"3201806016432"    , "type":2 }
{ "name":"All 4"                   , "appId":"111299002148"     , "type":2 }
{ "name":"VUDU"                    , "appId":"111012010001"     , "type":2 }
{ "name":"Explore Google Assistant", "appId":"3202004020674"    , "type":2 }
{ "name":"Amazon Alexa"            , "appId":"3202004020626"    , "type":2 }
{ "name":"My5"                     , "appId":"121299000612"     , "type":2 }
{ "name":"SmartThings"             , "appId":"3201910019378"    , "type":2 }
{ "name":"BritBox"                 , "appId":"3201909019175"    , "type":2 }
{ "name":"TikTok"                  , "appId":"3202008021577"    , "type":2 }
{ "name":"RaiPlay"                 , "appId":"111399002034"     , "type":2 }
{ "name":"DAZN"                    , "appId":"3201806016390"    , "type":2 }
{ "name":"McAfee Security"         , "appId":"3201612011418"    , "type":2 }
{ "name":"hayu"                    , "appId":"3201806016381"    , "type":2 }
{ "name":"Tubi"                    , "appId":"3201504001965"    , "type":2 }
{ "name":"CTV"                     , "appId":"3201506003486"    , "type":2 }
{ "name":"Crave"                   , "appId":"3201506003488"    , "type":2 }
{ "name":"MLB"                     , "appId":"3201603008210"    , "type":2 }
{ "name":"Love Nature 4K"          , "appId":"3201703012065"    , "type":2 }
{ "name":"SiriusXM"                , "appId":"111399002220"     , "type":2 }
{ "name":"7plus"                   , "appId":"3201803015934"    , "type":2 }
{ "name":"9Now"                    , "appId":"3201607010031"    , "type":2 }
{ "name":"Kayo Sports"             , "appId":"3201910019354"    , "type":2 }
{ "name":"ABC iview"               , "appId":"3201812017479"    , "type":2 }
{ "name":"10 play"                 , "appId":"3201704012147"    , "type":2 }
{ "name":"Telstra"                 , "appId":"11101000407"      , "type":2 }
{ "name":"Telecine"                , "appId":"3201604009182"    , "type":2 }
{ "name":"globoplay"               , "appId":"3201908019022"    , "type":2 }
{ "name":"DIRECTV GO"              , "appId":"3201907018786"    , "type":2 }
{ "name":"Stan"                    , "appId":"3201606009798"    , "type":2 }
{ "name":"BINGE"                   , "appId":"3202010022098"    , "type":2 }
{ "name":"Foxtel"                  , "appId":"3201910019449"    , "type":2 }
{ "name":"SBS On Demand"           , "appId":"3201510005981"    , "type":2 }
{ "name":"Security Center"         , "appId":"3202009021877"    , "type":2 }
{ "name":"Google Duo"              , "appId":"3202008021439"    , "type":2 }
{ "name":"Kidoodle.TV"             , "appId":"3201910019457"    , "type":2 }
{ "name":"Embly"                   , "appId":"vYmY3ACVaa.emby"  , "type":2 }
{ "name":"Viaplay"                 , "appId":"niYSnzL6h1.Viaplay"          , "type":2 }
{ "name":"SF Anytime"              , "appId":"sntmlv8LDm.SFAnytime"        , "type":2 }
{ "name":"SVT Play"                , "appId":"5exPmCT0nz.svtplay"          , "type":2 }
{ "name":"TV4 Play"                , "appId":"cczN3dzcl6.TV4"              , "type":2 }
{ "name":"C More"                  , "appId":"7fEIL5XfcE.CMore"            , "type":2 }
{ "name":"Comhem Play"             , "appId":"SQgb61mZHw.ComhemPlay"       , "type":2 }
{ "name":"Viafree"                 , "appId":"hs9ONwyP2U.ViafreeBigscreen" , "type":2 }
```

Enter this into the `samsungtv.cfg` file and save it. The file contents are read automatically every time the file is updated. The binding will check to see if the app is installed, and start polling the status every 10 seconds (or more if your refresh interval is set higher).  
Apps that are not installed are deleted from the list (internally, the file is not updated). If you install an app on the TV, which is not in the built in list, you have to update the file with it's appID, or at least touch the file for the new app to be registered with the binding.  

The entry for `Internet` is important, as this is the TV web browser App. on older TV's it's `org.tizen.browser`, but this is not correct on later TV's (>2019). This is the app used for the `url` channel, so it needs to be set correctly if you use this channel.
`org.tizen.browser` is the internal default, and does launch the browser on all TV's, but on later TV's this is just an alias for the actual app, so the `sourceApp` channel will not be updated correctly unless the correct appID is entered here. The built in list has the correct current appID for the browser, but if it changes or is incorrect for your TV, you can update it here.

You can use any name you want in this list, as long as the appID is valid. The binding will then allow you to launch the app using your name, the official name, or the appID.

## Smartthings

In order to be able to control the TV input (HDMI1, HDMI2 etc), you have to link the binding to the smartthngs API, as there is no local control capable of switching the TV input.  
There are several steps required to enable this feature, and no hub is needed.
In order to connect to the Smartthings cloud, there are a few steps to take.

1. Set the samsungtv logs to at least DEBUG
2. Create a Samsung account (probably already have one when you set up your TV)
3. Add Your TV to the Smartthings App
4. Go to https://account.smartthings.com/tokens and create a Personal Access Token (PAT). check off all the features you want (I would add them all).
5. Go to the openHAB Samsung TV Thing, and update the configuration with your PAT (click on advanced). You will fill in Device ID later if necessary.
6. Save the Thing, and watch the logs.
    
The binding will attempt to find the Device ID for your TV. If you have several TV’s of the same type, you will have to manually identify the Device ID for the current Thing from the logs. The device ID should look something like 996ff19f-d12b-4c5d-1989-6768a7ad6271. If you have only one TV of each type, Device ID should get filled in for you.
You can now link the `sourceName`, `sourceId`, `channel` and `channelName` channels, and should see the values updating. You can change the TV input source by sending `"HDMI1"`, or `"HDMI2"` to the `sourceName` channel, the exact string will depend on your TV, and how many inputs you have. You can also send a number to the `sourceId` channel.

**NOTE:** You may not get anything for `channelName`, as most TV’s don’t report it. You can only send commands to `channel`, `sourceName` and `sourceId`, `channelName` is read only.

## UPnP Subscriptions

UPnP Subscriptions are supported. This is an experimental feature which reduces the polling of UPnP services (off by default).

## Tested TV Models

Remote control channels (eg power, keyCode):
Samsung TV C (2010), D (2011), E (2012) and F (2013) models should be supported via the legacy interface.
Samsung TV H (2014) and J (2015) are **NOT supported** - these TV's use a pin code for access, and encryption for commands.
Samsung TV K (2016) and onwards are supported via websocket interface.

Even if the remote control channels are not supported, the UPnP channels may still work.

Art channels on all Frame TV's are supported.
 
Because Samsung does not publish any documentation about the TV's UPnP interface, there could be differences between different TV models, which could lead to mismatch problems.

Tested TV models (but this table may be out of date):

| Model          | State   | Notes                                                                                                                                                  |
|----------------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| KU6519         | PARTIAL | Supported channels: `volume`, `mute`, `power`,  `keyCode` (at least)                                                                                   |
| LE40D579       | PARTIAL | Supported channels: `volume`, `mute`, `channel`, `keyCode`, `sourceName`,  `programTitle`, `channelName`,  `power`                                     |
| LE40C650       | PARTIAL | Supported channels: `volume`, `mute`, `channel`, `keyCode`, `brightness`, `contrast`, `colorTemperature`, `power` (only power off, unable to power on) |
| UE40F6500      | OK      | All channels except `colorTemperature`, `programTitle` and `channelName` are working                                                                   |
| UE40J6300AU    | PARTIAL | Supported channels: `volume`, `mute`, `sourceName`, `power`                                                                                            |
| UE43MU6199     | PARTIAL | Supported channels: `volume`, `mute`, `power` (at least)                                                                                               |
| UE46D5700      | PARTIAL | Supports at my home only commands via the fake remote, no discovery                                                                                    |
| UE46E5505      | OK      | Initial contribution is done by this model                                                                                                             |
| UE46F6510SS    | PARTIAL | Supported channels: `volume`, `mute`, `channel` (at least)                                                                                             |
| UE48J5670SU    | PARTIAL | Supported channels: `volume`, `sourceName`                                                                                                             |
| UE50MU6179     | PARTIAL | Supported channels: `volume`, `mute`, `power`, `keyCode`, `channel`, `sourceApp`, `url`                                                                |
| UE55LS003      | PARTIAL | Supported channels: `volume`, `mute`, `sourceApp`, `url`, `keyCode`, `power`, `artMode`                                                                |
| UE58RU7179UXZG | PARTIAL | Supported channels: `volume`, `mute`, `power`, `keyCode` (at least)                                                                                    |
| UN50J5200      | PARTIAL | Status is retrieved (confirmed `power`, `media title`). Operating device seems not working.                                                            |
| UN46EH5300     | OK      | All channels except `programTitle` and `channelName` are working                                                                                       |
| UE75MU6179     | PARTIAL | All channels except `brightness`, `contrast`, `colorTemperature` and `sharpness`                                                                       |
| QN55LS03AAFXZC | PARTIAL | Supported channels: `volume`, `mute`, `keyCode`, `power`, `artMode`, `url`, `artImage`, `artLabel`, `artJson`, `artBrightness`,`artColorTemperature`   |
| QN43LS03BAFXZC | PARTIAL | Supported channels: `volume`, `mute`, `keyCode`, `power`, `artMode`, `url`, `artImage`, `artLabel`, `artJson`, `artBrightness`,`artColorTemperature`   |

If you enable the Smartthings interface, this adds back the `sourceName`, `sourceId`, `programTitle` and `channelName` channels on >2016 TV's
Samsung removed the app API support in >2019 TV's, if your TV is >2019, see the section on [Apps](#apps).
Samsung removed the art API support in >2021 TV's, if your Frame TV is >2021, see the section on [setArtMode](#setartmode).
Samsung re-introduced the art API in firmware 1622 for >2021 Frame TV's. if you have ths version, art channels will work correctly.

**NOTE:** `brightness`, `contrast`, `colorTemperature` and `sharpness` channels only work on legacy interface TV's (<2016).

## Troubleshooting

On legacy TV's, you may see an error like this:

```
2021-12-08 12:19:50.262 [DEBUG] [port.upnp.internal.UpnpIOServiceImpl] - Error reading SOAP response message. Can't transform message payload: org.jupnp.model.action.ActionException: The argument value is invalid. Invalid number of input or output arguments in XML message, expected 2 but found 1.
```

This is not an actual error, but is what is returned when a value is polled that does not yet exist, such as the URL for the TV browser, when the browser isn’t running. These messages are not new, and can be ignored. Enabling `subscription` will eliminate them.  

The `getSupportedChannelNames` messages are not UPnP services, they are not actually services that are supported *by your TV* at all. They are the internal capabilities of whatever method is being used for communication (which could be direct port connection, UPnP or websocket). 
They also do not reflect the actual capabilities of your TV, just what that method supports, on your TV, they may do nothing.

You should get `volume` and `mute` channels working at the minnimum. Other channels may or may not work, depending on your TV and the binding configuration.

If you see errors that say `no route to host` or similar things, it means your TV is off. The binding cannot discover, control or poll a TV that is off.

For the binding to function properly it is very important that your network config allows the machine running openHAB to receive UPnP multicast traffic.  
Multicast traffic is not propogated between different subnets, or VLANS, unless you specifically configure your router to do this. Many switches have IGMP Snooping enabled by default, which filters out multicast traffic.  
If you want to check the communication between the machine and the TV is working, you can try the following:

### Check if your Linux machine receives multicast traffic

**With your TV OFF (ie totally off)**

- Login to the Linux console of your openHAB machine.
- make sure you have __netcat__ installed
- Enter `netcat -ukl 1900` or `netcat -ukl -p 1900` depending on your version of Linux

### Check if your Windows/Mac machine receives multicast traffic

**With your TV OFF (ie totally off)**

- Download Wireshark on your openHAB machine
- Start and select the network interface which is connected to the same network as the TV
- Filter for the multicast messages with the expression `udp.dstport == 1900 && data.text` if you have "Show data as text" enabled, otherwise just filter for `udp.dstport == 1900`

### What you should see

You may see some messages (this is a good thing, it means you are receiving UPnP traffic).

Now turn your TV ON (with the remote control).

You should see several messages like the following:

```
NOTIFY * HTTP/1.1
HOST: 239.255.255.250:1900
CACHE-CONTROL: max-age=1800
DATE: Tue, 18 Jan 2022 17:07:18 GMT
LOCATION: http://192.168.100.73:9197/dmr
NT: urn:schemas-upnp-org:device:MediaRenderer:1
NTS: ssdp:alive
SERVER: SHP, UPnP/1.0, Samsung UPnP SDK/1.0
USN: uuid:ea645e34-d3dd-4b9b-a246-e1947f8973d6::urn:schemas-upnp-org:device:MediaRenderer:1
```

Where the ip address in `LOCATION` is the ip address of your TV, and the `USN` varies. `MediaRenderer` is the most important service, as this is what the binding uses to detect if your TV is online/turned On or not.

If you now turn your TV off, you will see similar messages, but with `NTS: ssdp:byebye`. This is how the binding detects that your TV has turned OFF.

Try this several times over a period of 30 minutes after you have discovered the TV and added the binding. This is because when you discover the binding, a UPnP `M-SEARCH` packet is broadcast, which will enable mulicast traffic, but your network (router or switches) can eventually start filtering out multicast traffic, leading to unrealiable behaviour.  
If you see these messages, then basic communications is working, and you should be able to turn your TV Off (and on later TV's) ON, and have the status reported correctly.

### Multiple network interfaces

If you have more than one network interface on your openHAB machine, you may have to change the `Network` setings in the openHAB control panel. Make sure the `Primary Address` is selected correctly (The same subnet as your TV is connected to).

### I'm not seeing any messages, or not Reliably

- Most likely your machine is not receiving multicast messages
- Check your network config:
    - Routers often block multicast - enable it.
    - Make sure the openHAB machine and the TV are in the same subnet/VLAN.
    - disable `IGMP Snooping` if it is enabled on your switches.
    - enable/disable `Enable multicast enhancement (IGMPv3)` if you have it (sometimes this helps).
    - Try to connect your openHAB machine or TV via Ethernet instead of WiFi (AP's can filter Multicasts).
    - Make sure you don't have any firewall rules blocking multicast.
    - if you are using a Docker container, ensure you use the `--net=host` setting, as Docker filters multicast broadcasts by default.

### I see the messages, but something else is not working properly

There are several other common issues that you can check for:

- Your TV is not supported. H (2014) and J (2015) TV's are not supported, as they have an encrypted interface.
- You are trying to discover a TV that is OFF (some TV's have a timeout, and turn off automatically).
- Remote control is not enabled on your TV. You have to specifically enable IP control and WOL on the TV.
- You have not accepted the request to allow remote control on your TV, or, you denied the request previously.
- You have selected an invalid combination of protocol and port in the binding.
    - The binding will attempt to auto configure the correct protocol and port on discovery, but you can change this later to an invalid configuration, eg:
    - Protocol None is not valid
    - Protocol Legacy will not work on >2016 TV's
    - Protocol websocket only works with port 8001
    - Protocol websocketsecure only works with port 8002. If your TV supports websocketsecure on port 8002, you *must* use it, or many things will not work.
- The channel you are trying to use is not supported on your TV.
    - Only some channels are supported on different TV's
    - Some channels require additional configuration on >2016 TV's. eg `SmartThings` configuration, or Apps confguration.
    - Some channels are read only on certain TV's
- I can't turn my TV ON.
    - Older TV's (<2016) do not support tuning ON
    - WOL is not enabled on your TV (you have to specifically enable it)
    - You have a soundbar connected to your TV and are connected using wired Ethernet.
    - The MAC address in the binding configuratiion is blank/wrong.
    - You have to wait up to 60 seconds after turning OFF, before you can turn back ON (This is a Samsung feature called "instant on")
- My TV asks me to accept the connection every time I turn the TV on
    - You have the TV set to "Always Ask" for external connections. You need to set it to "Only ask the First Time". To get to the Device Manager, press the home button on your TV remote and navigate to Settings → General → External Device Manager → Device Connect Manager and change the setting.
    - You are using a text `.things` file entry for the TV `thing`, and you haven't entered the `webSocketToken` in the text file definition. The token is shown on the binding config page. See [Binding Configuration](#binding-configuration).

