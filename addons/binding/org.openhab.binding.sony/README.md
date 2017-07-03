# Sony Binding

This binding is for Sony IP based product line. 

## Supported Things

The following are the services that are available from different Sony devices.  Please note they are not exclusive of each other (many services are offered on a single device and offer different capabilities).  Feel free to mix and match as you see fit. 

### IRCC

Many Sony products (TVs, AV systems, disc players) provided an IRCC service that provides minimal control to the device and some minimal feedback (via polling) depending on the version.  Additionally, to access the IRCC system on the device - you will need to authenticate to the device (see Authentication below).  From my research, their appears to be 5 versions of this service:

1) Not Specified - implemented on TVs and provides ONLY a command interface (i.e. sending of commands).  No feedback from the device is possible.  Authentication is done via preshared keys only.  No status is available.
2) 1.0 - ???
3) 1.1 - ???
4) 1.2 - ???
5) 1.3 - implemented on blurays.  Provides a command interface, text field entry and status feedback (including disc information).  The status feedback is provided via polling of the device.  Authentication is done via a code that appears on the screen.

Please note that the IRCC service is fully undocumented and much of the work that has gone into this service is based on observations.  

If you have a device that is reporting one of the "???" versions above, please post on the forum and I can give you directions on how we can document (and fix any issues) with those versions.

Please note that Sony has begun transitioning many of it's products over the the WebScalarAPI and the latest firmware updates have begun to disable this service.

#### Power ON notes

The biggest issue with IRCC devices is powering on.  Some devices support wake on lan (WOL) and this binding will attempt to wake the device.  Some devices support an IRCC power on command (rare) and this binding will attempt to use it.  However, some devices won't even send out a discovery packet nor respond to IRCC descriptor requests until the device is on.  If your device falls into that category, the device must be ON to auto-discover it and it must be ON when the binding starts up.  If it's not ON during discovery, the device simply won't be found.  If it's not ON during startup, the binding will remain OFFLINE and attempt to reconnect every config.retry seconds.  Likewise if it's turned OFF, the binding will go OFFLINE and will attempt retries until it's turned back on.

Please note that if you device supports WOL, the device will be woken when the OpenHab comes online since the binding will attempt to read the IRCC descriptor from it.  

The "power" channel will:

1) ON - attempt to wake the device via WOL (if support) or attempt to send the power ON IRCC command (if support).  If the device supports neither, then the "ON" side of the channel will not work and you'll need to rely on some other means to turn the device on
2) OFF - will turn the device off via the power IRCC command.  The binding will attempt to use the discrete POWER OFF command first and if not supported, the POWER toggle command will be used instead (assuming the device is on).

Please note that the initial/current status of the "power" channel is dependent on the IRCC version.  Version 1.3 will detected and generally be correct.  Version 1.0-1.2 is unknown.  Version "Not Specified" cannot determine the status.

#### Power OFF notes

Powering OFF is generally supported on all IRCC devices either through a discrete "Power Off" IRCC command or via the "Power" toggle IRCC command.

### Simple IP

The Simple IP protocol is a simplified version of IRCC and appears to be only supported on some models of Bravia TVs.  You must enable "Simple IP Control" on the devices (generally under Settings->Network->Home Network->IP Control->Simple IP Control) but once enabled - does not need any authentication.  The Simple IP control provides direct access to commonly used functions (channels, inputs, volume, etc) and provides full two-way communications (as things change on the device, openhab will be notified immediately).

### DIAL

ONLY IMPLEMENTED FOR BLURAYS (so far)

The DIAL (DIscovery And Launch) allows you to discover the various applications available on the device and manage those applications (mainly to start or stop them).  This will apply to many of the smart tvs and bluray devices.  Authentication is not needed.

### WebScalarAPI

The WebScalarAPI is Sony's next generation API for discovery and control of the device.  This service will allow work in hand with the IRCC to deliver control of the device and provide for feedback of the device status.  Authentication can be either via a pre-shared key or via code appearing on the device.

All channels are dynamically generated based on what the device supports. 

## Authentication

### IRCC

There are two types of authentication that is possible with IRCC - pre-shared key or code request.  When an IRCC version is not specified, only a pre-shared key is possible.  Please note that the default configuration of IRCC devices is to use a Code Request.

#### Pre Shared Key

A pre-shared key is a key (generally 4 digits) that you have set on the device prior to discovery (generally Settings->Network Control->IP Control->Pre Shared Key).  If you have set this on the device and then set the appropriate accessCode in the configuration, no additional authentication is required and the binding should be able to connect to the device.

#### Code Request

A code request will request a code from the device and the device will respond by displaying new code on the screen.  Write this number down and then update the binding configuration with this code (FYI - you only have a limited time to do this - usually 30 or 60 seconds before that code expires).  Once you update the access code in the configuration, the binding will restart and a success message should appear on the device.

Specifically you should:

1) Update the "accessCode" configuration with the value "RQST".  The binding will then reload and send a request to the device.
2) The device will display a new code on the screen (and a countdown to expiration).
3) Update the "accessCode" configuration with the value shown on the screen.  The binding will then reload and ask the device to authorize with that code.
4) If successful, the device will show a success message and the binding should go online.
5) If unsuccessful, the code may have expired - start back at step 1. 

If the device was auto-discovered, the "RQST" will automatically be entered once you approve the device (then you have 30-60 seconds to enter the code displayed on the screen in the PaperUI [Configuration->Things->[the new device]->[configuration]->"Access Code").  If the code expired before you had a chance, simply update double click on the "RQST" and press the OK button - that will force the binding to request a new code.

If you are manually setting up the configuration, saving the file will trigger the above process.

### Simple IP

No authentication is necessary

### DIAL

No authentication is necessary on bluray devices.  On bravia, I haven't figured out the authentication method yet.

### WebScalarAPI

Authentication is done via the same methods as IRCC.  Please see the notes above. 

## Discovery

This binding does attempt to discover Sony devices via UPNP.  Although this binding attempts to wake Sony devices (via wake on lan), many devices do not support WOL nor broadcast when they are turned off or sleeping.  If your devices does not automatically discovered, please turn the device on first and try again.  You may also need to turn on broadcasting via Settings->Network->Remote Start (on) - this setting has a side effect of turning on UPNP discovery.

With the exception of Simple IP service, I highly recommend automatic discovery and setup.  Manually setting up the devices requires you to know the IP Address, path, and port of the service implementation on the device (difficult and vary from device to device).

### IRCC

Any IRCC compliant device should be discovered (if powered on).  If the device is not listed, please power it on and try discovery again.  If you are manually setting it up, you'll need to know the URL to the IRCC.xml file on the device (which varies from device to device).

Please note that the discovery attempts to identify if the device supports WOL and will set the macAddress configuration property automatically.  However, this is only a best guess - you may need to remove the setting if your device does not support WOL or add the setting if your device does implement WOL.  See Thing Configuration->IRCC below.

### Simple IP

Simple IP appears to be implemented only on Bravia TVs.  If a Bravia TV is found, a Simple IP device will be discovered (whether the TV supports Simple IP or not).  Please see your TV documentation for whether Simple IP is supported and how to enable it (probably under Settings->Network->Home Network->IP Control->Simple IP Control).  Manually setting up the device simply requires the IP Address of the device (and potentially the network interface it's using [eth0 for wired, wlan0 for wireless]).

### DIAL

Any DIAL compliant devices should be discovered.  Please note that bravia tvs may be discovered - DO NOT USE THIS SERVICE FOR BRAVIA TVS - ONLY BLURAYS.

### WebScalarAPI

Any WebScalarAPI compliant device should be discovered (if powered on).  If the device is not listed, please power it on and try discovery again.  If you are manually setting it up, you'll need to know the URL to the scalar.xml file on the device (which varies from device to device).


## Thing Configuration

### IRCC

The configuration for the IRCC Service Thing:

| Name             | Required | Default | Description                                                                                                   |
|------------------| ---------|---------|---------------------------------------------------------------------------------------------------------------|
| irccUri          | Yes      | None    | The path to the IRCC.xml descriptor file                                                                      |
| deviceMacAddress | No (1)   | eth0    | The device MAC address to use for wake on lan (WOL).                                                          |
| commandsMapFile  | Yes (3)  | None    | The commands map file that translates words to the underlying protocol string                                 |
| accessCode       | No (2)   | None    | The access code to authenticate with.                                                                         |
| refresh          | No (4)   | 2       | The time, in seconds, to refresh some state from the device (only if the device supports retrieval of status) |
| retryPolling     | No       | 5       | The time, in seconds, to retry connecting to the device                                                       |

1. Only specify if the device support wake on lan (WOL)
2. The access code will either be the pre-shared access code from the device OR
3. See transformations below
4. Only specify if the device provides status information.  Set to negative to disable (-1).

### Simple IP

The configuration for the Simple IP Service Thing:

| Name             | Required | Default | Description                                                                   |
|------------------| ---------|---------|-------------------------------------------------------------------------------|
| ipAddress        | Yes      | None    | The IP or host name of the device                                             |
| commandsMapFile  | Yes (2)  | None    | The commands map file that translates words to the underlying protocol string |
| netInterface     | No (1)   | eth0    | The network interface the is using (eth0 for wired, wlan0 for wireless).      |
| ping             | No       | 30      | The time, in seconds, to ping the device to keep the connection open.         |
| refresh          | No       | 30      | The time, in seconds, to refresh some state from the device                   |
| retryPolling     | No       | 10      | The time, in seconds, to attempt reconnects to the device                     |

1. The netInterface is ONLY required if you wish to retrieve the broadcast address or mac address 
2. See transformations below

### DIAL

The configuration for the IRCC Service Thing:

| Name             | Required | Default | Description                                                                                                   |
|------------------| ---------|---------|---------------------------------------------------------------------------------------------------------------|
| dialUri          | Yes      | None    | The path to the dial descriptor file                                                                          |
| deviceMacAddress | No (1)   | eth0    | The device MAC address to use for wake on lan (WOL).                                                          |
| refresh          | No (2)   | -1      | The time, in seconds, to refresh some state from the device (only if the device supports retrieval of status) |
| retryPolling     | No       | 5       | The time, in seconds, to retry connecting to the device                                                       |

1. Only specify if the device support wake on lan (WOL)
2. A refresh is a fairly costly process and is turned off (set to -1) by default.  By turning this off, changes made on the device itself (via a remote control) will not be detected.

### WebScalarAPI

NOT IMPLEMENTED YET

## Transformations

### IRCC

The IRCC service requires a commands map file that will convert a word (specified in the command channel) to the underlying command to send to the device.

When the IRCC device is ONLINE, the commandsMapFile configuration property has been set and the resulting file doesn't exist, the binding will write out the commands supported by the device to that file.  If discovery of the commands is not possible (IRCC version of "not specified"), a default set of commands will be written out which may or may not be correct for the device.  I highly recommend having the binding do this rather than creating the file from scratch.

When the IRCC device is auto discovered, the commandsMapFile will be set to "ircc-{bindingid}.map".  You may want to change that, post-discovery, to something more reasonable.

The format of the file will be:
<word>=<protocol>:<cmd>

The word can be anything (in any language) and is the value send to the command channel.
The protocol can either be "ircc" for an IRCC command or "url" for a web request command.  
THe cmd is a URL Encoded value that will be sent to the device (or used as an HTTP GET if the "url" protocol).

An example from a Sony BluRay player (that was discovered by the binding):

```
...
Stop=ircc:AAAAAwAAHFoAAAAYAw%3D%3D
SubTitle=ircc:AAAAAwAAHFoAAABjAw%3D%3D
TopMenu=ircc:AAAAAwAAHFoAAAAsAw%3D%3D
Up=ircc:AAAAAwAAHFoAAAA5Aw%3D%3D
Yellow=ircc:AAAAAwAAHFoAAABpAw%3D%3D
ZoomIn=url:http%3A%2F%2F192.168.1.2%3A50002%2FsetBrowse%3Faction%3DzoomIn
ZoomOut=url:http%3A%2F%2F192.168.1.2%3A50002%2FsetBrowse%3Faction%3DzoomOut
...
```  

### Simple IP

The Simple IP service requires a commands map file that will convert a word (specified in the command channel) to the underlying command to send to the device.

When the Simple IP device is ONLINE, the commandsMapFile configuration property has been set and the resulting file doesn't exist, the binding will write out the commands that have been documented so far.  I highly recommend having the binding do this rather than creating the file from scratch.  Please note that the end of the file you will see gaps in the numbers - I believe those are dependent upon the TV's configuration (# of hdmi ports, etc).  Feel free to play with those missing numbers and if you figure out what they do - post a note to the forum and I'll document them.

When the Simple IP device is auto discovered, the commandsMapFile will be set to "simpleip-{bindingid}.map".  You may want to change that, post-discovery, to something more reasonable.

The format of the file will be:
<word>=<cmd>

The word can be anything (in any language) and is the value send to the command channel.
THe cmd is an integer representing the ir command to execute.

An example from a Sony Bravia XBR-43X830C (that was discovered by the binding):

```
...
Input=1
Guide=2
EPG=3
Favorites=4
Display=5
Home=6
...
```  

### DIAL

None

### WebScalarAPI

NOT IMPLEMENTED YET

## Channels

### IRCC

The channels supported depend on the version of the IRCC service.

| Channel Group ID | Channel Type ID | Read/Write | Version |Item Type | Description                                             |
| -----------------|-----------------|------------|---------|----------|---------------------------------------------------------|
| primary          | power           | RW         | any     | Switch   | Whether the device is powered on or not                 |
| primary          | command         | W          | any     | String   | The IRCC command to execute (see transformations above) |
| primary          | contenturl      | RW         | 1.3     | String   | The URL displayed in the device's browser               |
| primary          | textfield       | RW         | 1.3     | String   | The contents of the text field                          |
| primary          | intext          | R          | 1.3     | Switch   | Whether a text field has focus                          |
| primary          | inbrowser       | R          | 1.3     | Switch   | Whether viewing the device's browser or not             |
| primary          | isviewing       | R          | 1.3     | Switch   | Whether viewing content or not                          |
| viewing          | id              | R          | 1.3     | String   | The identifier of what is being viewed                  |
| viewing          | source          | R          | 1.3     | String   | The source being viewed                                 |
| viewing          | zone2source     | R          | 1.3     | String   | The source being viewed in zone 2                       |
| viewing          | title           | R          | 1.3     | String   | The title of the source being viewed                    |
| viewing          | duration        | R          | 1.3     | Number   | The duration (in seconds) of what is being viewed       |
| content          | id              | R          | 1.3     | String   | The identifier of the content                           |
| content          | title           | R          | 1.3     | String   | The title of the content                                |
| content          | class           | R          | 1.3     | String   | The class of the content (video, etc)                   |
| content          | source          | R          | 1.3     | String   | The source of the content (DVD, etc)                    |
| content          | mediatype       | R          | 1.3     | String   | The media type of the content (DVD, USB, etc)           |
| content          | mediasource     | R          | 1.3     | String   | The media format of the content (VIDEO, etc)            |
| content          | edition         | R          | 1.3     | String   | The edition of the content                              |
| content          | description     | R          | 1.3     | String   | The description of the content                          |
| content          | genre           | R          | 1.3     | String   | The genre of the content                                |
| content          | duration        | R          | 1.3     | Number   | The duration (in seconds) of the content                |
| content          | rating          | R          | 1.3     | String   | The rating of the content (R, PG, etc)                  |
| content          | daterelease     | R          | 1.3     | DateTime | The release date of the content                         |
| content          | director        | R          | 1.3     | String   | The director(s) of the content                          |
| content          | producer        | R          | 1.3     | String   | The producer(s) of the content                          |
| content          | screenwriter    | R          | 1.3     | String   | The screen writer(s) of the content                     |
| content          | image           | R          | 1.3     | Image    | The content image                                       |

Notes:

1) Version 1.3 is what I had to test with - channels may work with lower versions but I can't confirm until someone has a device with those versions.
2) "inbrowser" will become true for certain apps as well (not sure why unless the use a browser under the scenes?)
3) "viewingXXX" will only be populated (with the exception of "viewingtitle") when "isviewing" is true and is only set when actually viewing a disc (not an app or browser)
4) "contenttitle" will also represent the browser title when viewing a webpage in the browser
5) "contentXXX" will be available if a disc is inserted (whether you are viewing it or not).
6) Setting the "contenturl" will start the browser on the device and set the url to the content.  Please note that the url MUST begin with "http://" or "https://" for this to work. 


### Simple IP

All devices support the following channels (non exhaustive):

| Channel Type ID   | Read/Write | Item Type    | Description                                                     |
|-------------------|------------|--------------|-----------------------------------------------------------------|
| ir                | W          | String       | The ir codes to send (see transformations above)                |
| power             | RW         | Switch       | Whether device is powered on                                    |
| volume            | RW         | Dimmer       | The volume for the device                                       |
| audiomute         | RW         | Switch       | Whether the audio is muted                                      |
| channel           | RW         | String       | The channel in the form of "x.x" ("50.1") or "x" ("13")         |
| tripletchannel    | RW         | String       | The triplet channel in the form of "x.x.x" ("32736.32736.1024") |
| inputsource       | RW         | String       | The input source ("antenna"). See note 1 below                  |
| input             | RW         | String       | The input in the form of "xxxxyyyy" ("HDMI1"). See note 2 below |
| picturemute       | RW         | Switch       | Whether the picture is shown or not (muted)                     |
| togglepicturemute | W          | Switch       | Toggles the picture mute                                        |
| pip               | RW         | Switch       | Enables or disabled picture-in-picture                          |
| togglepip         | W          | Switch       | Toggles the picture-in-picture enabling                         |
| togglepipposition | W          | Switch       | Toggles the picture-in-picture position                         |

1.  The text of the input source is specific to the TV.  The documentation lists as valid dvbt, dvbc, dvbs, isdbt, isdbbs, isdbcs, antenna, cable, isdbgt.  However, "atsct" seems to be supported as well and others may be valid. 
2.  The input can be either "TV" or "xxxxyyyy" where xxxx is the port name and yyyy is the port number.  Valid port names (case insensitive) are "hdmi", "scart", "composite", "component", "screen mirroring", and "pc rgb input".  The port number is dependent on how many ports the device supports.  Example: the X830 supports 4 hdmi ports - so "hdmi1", "hdmi2", "hdmi3" and "hdmi4" are all valid.

### DIAL

The DIAL service will interactively create channels (based on what is installed on the device).  The channels will be:
| Channel Type ID | Read/Write | Item Type    | Description                         |
|-----------------|------------|--------------|-------------------------------------|
| state-{id}      | RW         | Switch       | Whether the app is running or not   |
| icon-{id}       | R          | Image        | The icon related to the application |

The {id} is the unique identifier that the device has assigned to the application.  

Example: On my bluray device, "Netflix" is identified as "com.sony.iptv.type.NRDP".  The channels would then be:
"state-com.sony.iptv.type.NRDP" with a label of "Netflix"
"icon-com.sony.iptv.type.NRDP" with a label of "Netflix Icon"

To identify all of the channel ids - look into the log file.  This service (on startup) will provide a logging message like:
Creating channel 'Netflix' with an id of 'com.sony.iptv.type.NRDP' 

Note: if you install a new application on the device, this binding will need to be restarted to pickup on and create a channel for the new application.  
  
## Full Examples

### IRCC

*Really recommended to autodiscover rather than manually setup thing file*
ircc.Things:

```
sony:ircc:home [ irccUri="http://192.168.1.72:20970/sony/webapi/ssdp/dd.xml", macAddress="aa:bb:cc:dd:ee:ff", commandsMapFile="ircccodes.map", accessCode="1111", refresh=-1 ]
```

ircc.items:

```
String IRCC_Version "Version [%s]" { channel="sony:ircc:home:primary#version" }
Switch IRCC_Power "Power [%s]" { channel="sony:ircc:home:primary#power" }
String IRCC_Command "Command [%s]" { channel="sony:ircc:home:primary#command" }
Switch IRCC_InBrowser "In browser [%s]" { channel="sony:ircc:home:primary#inbrowser" }
String IRCC_ContentURL "URL [%s]" { channel="sony:ircc:home:primary#contenturl" }
Switch IRCC_InText "In text [%s]" { channel="sony:ircc:home:primary#intext" }
String IRCC_Text "Text [%s]" { channel="sony:ircc:home:primary#textfield" }

Switch IRCC_IsViewing "Is Viewing [%s]" { channel="sony:ircc:home:primary#isviewing" }
String IRCC_ViewingId "ViewingID [%s]" { channel="sony:ircc:home:viewing#id" }
String IRCC_ViewingSource "Viewing Source [%s]" { channel="sony:ircc:home:viewing#source" }
String IRCC_ViewingClass "Viewing Class [%s]" { channel="sony:ircc:home:viewing#class" }
String IRCC_ViewingTitle "Viewing Title [%s]" { channel="sony:ircc:home:viewing#title" }
Number IRCC_ViewingDuration "Viewing Duration [%s] seconds" { channel="sony:ircc:home:viewing#duration" }

String IRCC_ContentId "ContentID [%s]" { channel="sony:ircc:home:content#id" }
String IRCC_ContentTitle "Content Title [%s]" { channel="sony:ircc:home:content#title" }
String IRCC_ContentClass "Content Class [%s]" { channel="sony:ircc:home:content#class" }
String IRCC_ContentSource "Content Source [%s]" { channel="sony:ircc:home:content#source" }
String IRCC_ContentMediaType "Content Media Type [%s]" { channel="sony:ircc:home:content#mediatype" }
String IRCC_ContentMediaFormat "Content Media Format [%s]" { channel="sony:ircc:home:content#mediaformat" }
String IRCC_ContentEdition "Content Edition [%s]" { channel="sony:ircc:home:content#edition" }
String IRCC_ContentDescription "Content Description [%s]" { channel="sony:ircc:home:content#description" }
String IRCC_ContentGenre "Content Genre [%s]" { channel="sony:ircc:home:content#genre" }
Number IRCC_ContentDuration "Content Duration [%s] seconds" { channel="sony:ircc:home:content#duration" }
String IRCC_ContentRating "Content Rating [%s]" { channel="sony:ircc:home:content#rating" }
DateTime IRCC_ContentDateRelease "Content Date Released [%F]" { channel="sony:ircc:home:content#daterelease" }
String IRCC_ContentDirector "Content Director(s) [%s]" { channel="sony:ircc:home:content#director" }
String IRCC_ContentProducer "Content Producer(s) [%s]" { channel="sony:ircc:home:content#producer" }
String IRCC_ContentScreenWriter "Content Screen Writer(s) [%s]" { channel="sony:ircc:home:content#screenwriter" }
Image IRCC_ContentImage "Content Image" { channel="sony:ircc:home:content#image" }
```

ircc.sitemap

```
sitemap demo label="Main Menu"
{
    Frame label="IRCC" {
        Text item=IRCC_Version
        Switch item=IRCC_Power
        Text item=IRCC_InBrowser
        Text item=IRCC_ContentURL
        Text item=IRCC_InText
        Text item=IRCC_Text
        Text item=IRCC_IsViewing
        Text item=IRCC_ViewingId
        Text item=IRCC_ViewingSource
        Text item=IRCC_ViewingClass
        Text item=IRCC_ViewingTitle
        Text item=IRCC_ViewingDuration
        Text item=IRCC_ContentId
        Text item=IRCC_ContentTitle
        Text item=IRCC_ContentClass
        Text item=IRCC_ContentSource
        Text item=IRCC_ContentMediaType
        Text item=IRCC_ContentMediaFormat
        Text item=IRCC_ContentEdition
        Text item=IRCC_ContentDescription
        Text item=IRCC_ContentGenre
        Text item=IRCC_ContentDuration
        Text item=IRCC_ContentRating
        Text item=IRCC_ContentDateRelease
        Text item=IRCC_ContentDirector
        Text item=IRCC_ContentProducer
        Text item=IRCC_ContentScreenWriter
        ImageItem item=IRCC_ContentImage
    }
}
```

### Simple IP

simpleip.Things:

```
sony:simpleip:home [ ipAddress="192.168.1.72", commandsMapFile="braviaircodes.map", netInterface="eth0", ping=30, retryPolling=10 ]
```

simpleip.items:

```
String Bravia_IR "IR [%s]" { channel="sony:simpleip:home:ir" }
Switch Bravia_Power "Power [%s]" { channel="sony:simpleip:home:power" }
Dimmer Bravia_Volume "Volume [%s]" { channel="sony:simpleip:home:volume" }
Switch Bravia_AudioMute "Audio Mute [%s]" { channel="sony:simpleip:home:audiomute" }
String Bravia_Channel "Channel [%s]" { channel="sony:simpleip:home:channel" }
String Bravia_TripletChannel "Triplet Channel [%s]" { channel="sony:simpleip:home:tripletchannel" }
String Bravia_InputSource "Input Source [%s]" { channel="sony:simpleip:home:inputsource" }
String Bravia_Input "Input [%s]" { channel="sony:simpleip:home:input" }
Switch Bravia_PictureMute "Picture Mute [%s]" { channel="sony:simpleip:home:picturemute" }
Switch Bravia_TogglePictureMute "Toggle Picture Mute [%s]" { channel="sony:simpleip:home:togglepicturemute", autoupdate="false"  }
Switch Bravia_Pip "PIP [%s]" { channel="sony:simpleip:home:pip" }
Switch Bravia_TogglePip "Toggle PIP [%s]" { channel="sony:simpleip:home:togglepip", autoupdate="false" }
Switch Bravia_TogglePipPosition "Toggle PIP Position [%s]" { channel="sony:simpleip:home:togglepipposition", autoupdate="false" }
String Bravia_BroadcastAddress "Broadcast [%s]" { channel="sony:simpleip:home:broadcastaddress" }
String Bravia_MacAddress "MAC [%s]" { channel="sony:simpleip:home:macaddress" }
```

simpleip.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame label="Sony Bravia" {
        Selection item=Bravia_IR mappings=[Channel-Up="Channel Up",Channel-Down="Channel Down",Left="Left"]
        Switch item=Bravia_Power
        Slider item=Bravia_Volume
        Switch item=Bravia_AudioMute
        Selection item=Bravia_Channel mappings=[4.1="ABC(1)", 5.1="NBC(1)", 5.2="NBC(2)", 13="CBS", 50.1="WRAL(1)", 50.2="WRAL(2)"]
        Text item=Bravia_TripletChannel
        Selection item=Bravia_InputSource mappings=[atsct="ATSCT", dvbt="DVBT", dvbc="DVBC", dvbs="DVBS", isdbt="ISDBT", isdbbs="ISDBBS", isdbcs="ISDBCS", antenna="Antenna", cable="Cable", isdbgt="ISDBGT"]
        Selection item=Bravia_Input mappings=[TV="TV", HDMI1="HDMI1", HDMI2="HDMI2"]
        Switch item=Bravia_PictureMute
        Switch item=Bravia_TogglePictureMute mappings=[ON="Toggle"]
        Switch item=Bravia_Pip
        Switch item=Bravia_TogglePip mappings=[ON="Toggle"]
        Switch item=Bravia_TogglePipPosition mappings=[ON="Toggle"]
        Text item=Bravia_BroadcastAddress
        Text item=Bravia_MacAddress
    }
}
```

### DIAL

dial.Things:

```
sony:dial:home [ irccUri="http://192.168.1.71:50201/dial.xml", macAddress="aa:bb:cc:dd:ee:ff", refresh=-1 ]
```

dial.items:

```
Switch DIAL_Netflix "Netflix [%s]" { channel="sony:dial:home:state-com.sony.iptv.type.NRDP" }
Image DIAL_NetflixIcon "Icon" { channel="sony:dial:home:icon-com.sony.iptv.type.NRDP" }
```


dial.sitemap

```
sitemap demo label="Main Menu"
{
    Frame label="DIAL" {
        Switch item=DIAL_Netflix
        ImageItem item=DIAL_NetflixIcon
    }
}
```

### WebScalarAPI

NOT IMPLEMENTED YET
