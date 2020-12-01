# Simple IP

The Simple IP protocol is a simplified version of IRCC and appears to be only supported on some models of Bravia TVs.  You must enable "Simple IP Control" on the devices (generally under ```Settings->Network->Home Network->IP Control->Simple IP Control```) but once enabled - does not need any authentication.  The Simple IP control provides direct access to commonly used functions (channels, inputs, volume, etc) and provides full two-way communications (as things change on the device, openHAB will be notified immediately).

## Authentication

Simple IP needs no additional authentication and should automatically come online if the configuration is correct (and the TV has been setup correctly).

## Thing Configuration

The configuration for the Simple IP Service Thing:

| Name            | Required | Default | Description                                                                   |
| --------------- | -------- | ------- | ----------------------------------------------------------------------------- |
| commandsMapFile | No (1)   | None    | The commands map file that translates words to the underlying protocol string |
| netInterface    | No (2)   | eth0    | The network interface the is using (eth0 for wired, wlan0 for wireless).      |

1. See transformations below
2. The netInterface is ONLY required if you wish to retrieve the broadcast address or mac address 

## Transformations

The Simple IP service requires a commands map file that will convert a word (specified in the command channel) to the underlying command to send to the device.  This file will appear in your openHAB ```conf/transformation``` directory.

When the Simple IP device is ONLINE, the commandsMapFile configuration property has been set and the resulting file doesn't exist, the binding will write out the commands that have been documented so far.  I highly recommend having the binding do this rather than creating the file from scratch.  Please note that the end of the file you will see gaps in the numbers - I believe those are dependent upon the TV's configuration (# of hdmi ports, etc).  Feel free to play with those missing numbers and if you figure out what they do - post a note to the forum and I'll document them.

When the Simple IP device is auto discovered, the commandsMapFile will be set to "simpleip-{thingid}.map".  You may want to change that, post-discovery, to something more reasonable.

The format of the file will be:
```{word}={cmd}```

1. The word can be anything (in any language) and is the value send to the command channel.
2. The cmd is an integer representing the ir command to execute.

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

Please note that you can recreate the .map file by simply deleting it from ```conf/transformation``` and restarting openHAB.

## Channels

All devices support the following channels (non exhaustive):

| Channel Type ID   | Read/Write | Item Type | Description                                                     |
| ----------------- | ---------- | --------- | --------------------------------------------------------------- |
| ir                | W          | String    | The ir codes to send (see transformations above)                |
| power             | R          | Switch    | Whether device is powered on                                    |
| volume            | R          | Dimmer    | The volume for the device                                       |
| audiomute         | R          | Switch    | Whether the audio is muted                                      |
| channel           | R          | String    | The channel in the form of "x.x" ("50.1") or "x" ("13")         |
| tripletchannel    | R          | String    | The triplet channel in the form of "x.x.x" ("32736.32736.1024") |
| inputsource       | R          | String    | The input source ("antenna"). See note 1 below                  |
| input             | R          | String    | The input in the form of "xxxxyyyy" ("HDMI1"). See note 2 below |
| picturemute       | R          | Switch    | Whether the picture is shown or not (muted)                     |
| togglepicturemute | W          | Switch    | Toggles the picture mute                                        |
| pip               | R          | Switch    | Enables or disabled picture-in-picture                          |
| togglepip         | W          | Switch    | Toggles the picture-in-picture enabling                         |
| togglepipposition | W          | Switch    | Toggles the picture-in-picture position                         |

1.  The text of the input source is specific to the TV.  The documentation lists as valid dvbt, dvbc, dvbs, isdbt, isdbbs, isdbcs, antenna, cable, isdbgt.  However, "atsct" seems to be supported as well and others may be valid. 
2.  The input can be either "TV" or "xxxxyyyy" where xxxx is the port name and yyyy is the port number.  Valid port names (case insensitive) are "hdmi", "scart", "composite", "component", "screen mirroring", and "pc rgb input".  The port number is dependent on how many ports the device supports.  Example: the X830 supports 4 hdmi ports - so "hdmi1", "hdmi2", "hdmi3" and "hdmi4" are all valid.

## Full Example

simpleip.Things:

```
sony:simpleip:home [ deviceAddress="192.168.1.72", commandsMapFile="braviaircodes.map", netInterface="eth0" ]
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
    }
}
```
