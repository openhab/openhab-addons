# IRCC

IRCC (otherwise know as IRCC-IP - InfraRed Compatible Control over Internet Protocol) will allow you to send IR commands to the device over IP.

Many Sony products (TVs, AV systems, disc players) provided an IRCC service that provides minimal control to the device and some minimal feedback (via polling) depending on the version.
From my research, their appears to be 5 versions of this service:

1) Not Specified - implemented on TVs and provides ONLY a command interface (i.e. sending of commands).
No feedback from the device is possible.
No status is available.
2) 1.0 - ???
3) 1.1 - ???
4) 1.2 - ???
5) 1.3 - implemented on blurays.
Provides a command interface, text field entry and status feedback (including disc information).
The status feedback is provided via polling of the device.

Please note that the IRCC service is fully undocumented and much of the work that has gone into this service is based on observations.

If you have a device that is reporting one of the "???" versions above, please post on the forum and I can give you directions on how we can document (and fix any issues) with those versions.

Please note that Sony has begun transitioning many of it's products over the the Scalar API and the latest firmware updates have begun to disable this service.

### Power ON notes

The biggest issue with IRCC devices is powering on.
Some devices support wake on lan (WOL) and this binding will attempt to wake the device.
Some devices support an IRCC power on command (rare) and this binding will attempt to use it.
However, some devices won't even send out a discovery packet nor respond to IRCC descriptor requests until the device is on.
If your device falls into that category, the device must be ON to auto-discover it and it must be ON when the binding starts up.
If it's not ON during discovery, the device simply won't be found.
If it's not ON during startup, the binding will remain OFFLINE and attempt to reconnect every few seconds (configuration option).
Likewise if it's turned OFF, the binding will go OFFLINE and will attempt retries until it's turned back on.

Please note that if you device supports WOL, the device will be woken when the openHAB comes online since the binding will attempt to read the IRCC descriptor from it.

The "power" channel will:

1) ON - attempt to wake the device via WOL (if supported) or attempt to send the power ON IRCC command (if supported).
If the device supports neither, then the "ON" side of the channel will not work and you'll need to rely on some other means to turn the device on
2) OFF - will turn the device off via the power IRCC command.
The binding will attempt to use the discrete POWER OFF command first and if not supported, the POWER toggle command will be used instead (assuming the device is on).

Please note that the initial/current status of the "power" channel is dependent on the IRCC version.
Version 1.3 will detect and generally be correct.
Version 1.0-1.2 is unknown.
Version "Not Specified" cannot determine the status.

### Power OFF notes

Powering OFF is generally supported on all IRCC devices either through a discrete "Power Off" IRCC command or via the "Power" toggle IRCC command.

## Authentication

IRCC can be authenticated via normal keys or preshared keys as documented in the main [README](README.md).

## Thing Configuration

The configuration for the IRCC thing (in addition to the common parameters)

| Name            | Required | Default | Description                                                                   |
| --------------- | -------- | ------- | ----------------------------------------------------------------------------- |
| accessCode      | No       | RQST    | The access code for the device                                                |
| commandsMapFile | No (1)   | None    | The commands map file that translates words to the underlying protocol string |

1. See transformations below


## Transformations

These services use a commands map file that will convert a word (specified in the command channel) to the underlying command to send to the device.
This file will appear in your openHAB ```conf/transformation``` directory.

When the device is ONLINE, the commandsMapFile configuration property has been set and the resulting file doesn't exist, the binding will write out the commands supported by the device to that file.
If discovery of the commands is not possible, a default set of commands will be written out which may or may not be correct for the device.
I highly recommend having the binding do this rather than creating the file from scratch.

When the device is auto discovered, the commandsMapFile will be set to "ircc-{thingid}.map"  (example: "ircc-ace2a0229f7a.map").
You may want to change that in the things configuration, post-discovery, to something more reasonable.

The format of the file will be: ```{word}={protocol}:{cmd}```

1. The word can be anything (in any language) and is the value send to the command channel.
2. The protocol can either be "ircc" for an IRCC command or "url" for a web request command.
3. The cmd is a URL Encoded value that will be sent to the device (or used as an HTTP GET if the "url" protocol).

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

Please note that you can recreate the .map file by simply deleting it from ```conf/transformation``` and restarting openHAB.

## Channels

The channels supported depend on the version of the IRCC service.

| Channel Group ID | Channel Type ID | Read/Write | Version | Item Type | Description                                             |
| ---------------- | --------------- | ---------- | ------- | --------- | ------------------------------------------------------- |
| primary          | power           | R          | any     | Switch    | Whether the device is powered on or not                 |
| primary          | command         | W          | any     | String    | The IRCC command to execute (see transformations above) |
| primary          | contenturl      | R          | 1.3     | String    | The URL displayed in the device's browser               |
| primary          | textfield       | R          | 1.3     | String    | The contents of the text field                          |
| primary          | intext          | R          | 1.3     | Switch    | Whether a text field has focus                          |
| primary          | inbrowser       | R          | 1.3     | Switch    | Whether viewing the device's browser or not             |
| primary          | isviewing       | R          | 1.3     | Switch    | Whether viewing content or not                          |
| viewing          | id              | R          | 1.3     | String    | The identifier of what is being viewed                  |
| viewing          | source          | R          | 1.3     | String    | The source being viewed                                 |
| viewing          | zone2source     | R          | 1.3     | String    | The source being viewed in zone 2                       |
| viewing          | title           | R          | 1.3     | String    | The title of the source being viewed                    |
| viewing          | duration        | R          | 1.3     | Number    | The duration (in seconds) of what is being viewed       |
| content          | id              | R          | 1.3     | String    | The identifier of the content                           |
| content          | title           | R          | 1.3     | String    | The title of the content                                |
| content          | class           | R          | 1.3     | String    | The class of the content (video, etc)                   |
| content          | source          | R          | 1.3     | String    | The source of the content (DVD, etc)                    |
| content          | mediatype       | R          | 1.3     | String    | The media type of the content (DVD, USB, etc)           |
| content          | mediasource     | R          | 1.3     | String    | The media format of the content (VIDEO, etc)            |
| content          | edition         | R          | 1.3     | String    | The edition of the content                              |
| content          | description     | R          | 1.3     | String    | The description of the content                          |
| content          | genre           | R          | 1.3     | String    | The genre of the content                                |
| content          | duration        | R          | 1.3     | Number    | The duration (in seconds) of the content                |
| content          | rating          | R          | 1.3     | String    | The rating of the content (R, PG, etc)                  |
| content          | daterelease     | R          | 1.3     | DateTime  | The release date of the content                         |
| content          | director        | R          | 1.3     | String    | The director(s) of the content                          |
| content          | producer        | R          | 1.3     | String    | The producer(s) of the content                          |
| content          | screenwriter    | R          | 1.3     | String    | The screen writer(s) of the content                     |
| content          | image           | R          | 1.3     | Image     | The content image                                       |

Notes:

1. *The power switch is simply a toggle to issue power on/off IRCC commands and will NOT reflect the current power state of the device*
2. Version 1.3 is what I had to test with - channels may work with lower versions but I can't confirm until someone has a device with those versions.
3. "inbrowser" will become true for certain apps as well (not sure why unless the use a browser under the scenes?)
4. "viewingXXX" will only be populated (with the exception of "viewingtitle") when "isviewing" is true and is only set when actually viewing a disc (not an app or browser)
5. "contenttitle" will also represent the browser title when viewing a webpage in the browser
6. "contentXXX" will be available if a disc is inserted (whether you are viewing it or not).
7. Setting the "contenturl" will start the browser on the device and set the url to the content.
Please note that the url MUST begin with "http://" or "https://" for this to work.

## Full Examples

*Really recommended to autodiscover rather than manually setup thing file*

ircc.Things:

```
Thing sony:ircc:home [ deviceAddress="http://192.168.1.72:20970/sony/webapi/ssdp/dd.xml", deviceAddressAddress="aa:bb:cc:dd:ee:ff", commandsMapFile="ircccodes.map", accessCode="1111", refresh=-1 ]
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
