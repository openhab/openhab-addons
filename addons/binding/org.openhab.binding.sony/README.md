# Sony Binding

This binding is for Sony products and provides two way communications between OpenHAB and the Sony device.

## Supported Things

Only Sony Bravia TVs are currently supported.

## Discovery
This binding fully support automatic discovery of Sony Bravia TVs.  The PaperUI may be used to discover this (as well as manually enter the binding).

## Binding Configuration

For this binding to correctly connect - you must enable "Simple IP Control" on the Bravia device.  Please see your TV documentation (although it's probably under Settings->Network->Home Network->IP Control->Simple IP Control).

## Thing Configuration

The configuration for the Bravia Thing:
| Name | Required   | Default | Description  |
| ipAddress | Yes | None | The IP or host name of the Bravia device |
| netInterface | No* | eth0 | The network interface the Bravia is using (eth0 for wired, wlan0 for wireless). |
| ping | No | 30 | The time, in seconds, to ping the Bravia device to keep the connection open. |
| refresh | No | 30 | The time, in seconds, to refresh some state from the device |
| retryPolling | No | 10 | The time, in seconds, to attempt reconnects to the device |

* The netInterface is ONLY required if you wish to retrieve the broadcast address or mac address 


## Channels

All devices support the following channels (non exhaustive):

| Channel Type ID | Read/Write | Item Type    | Description  |
|-----------------|------------------------|--------------|----------------- |
| ir | W | String | The IRCC code to send to Bravia.  See IRCC codes below |
| power | RW | Switch | Whether device is powered on |
| volume | RW | Dimmer | The volume for the device |
| audiomute | RW | Switch | Whether the audio is muted |
| channel | RW | String | The channel in the form of "x.x" ("50.1") or "x" ("13") |
| tripletchannel | RW | String | The triplet channel in the form of "x.x.x" ("32736.32736.1024") |
| inputsource | RW | String | The input source ("antenna"). See note 1 below |
| input | RW | String | The input in the form of "xxxxyyyy" ("HDMI1"). See note 2 below |
| picturemute | RW | Switch | Whether the picture is shown or not (muted) |
| togglepicturemute | W | Toggles the picture mute |
| pip | RW | Enables or disabled picture-in-picture |
| togglepip | W | Toggles the picture-in-picture enabling |
| togglepipposition | W | Toggles the picture-in-picture position |
| broadcastaddress | R | The address the device uses for broadcasting |
| macaddress | R | The MAC address of the device |

1.  The text of the input source is specific to the TV.  The documentation lists as valid dvbt, dvbc, dvbs, isdbt, isdbbs, isdbcs, antenna, cable, isdbgt.  However, "atsct" seems to be supported as well and others may be valid. 
2.  The input can be either "TV" or "xxxxyyyy" where xxxx is the port name and yyyy is the port number.  Valid port names (case insensitive) are "hdmi", "scart", "composite", "component", "screen mirroring", and "pc rgb input".  The port number is dependent on how many ports the device supports.  Example: the X830 supports 4 hdmi ports - so "hdmi1", "hdmi2", "hdmi3" and "hdmi4" are all valid.
  
## Full Example

demo.Things:

```
sony:bravia:home [ ipAddress="192.168.1.72", netInterface="eth0", ping=30, retryPolling=10 ]
```

demo.items:

```
Number Bravia_IR "IR [%s]" { channel="sony:bravia:home:ir" }
Switch Bravia_Power "Power [%s]" { channel="sony:bravia:home:power" }
Dimmer Bravia_Volume "Volume [%s]" { channel="sony:bravia:home:volume" }
Switch Bravia_AudioMute "Audio Mute [%s]" { channel="sony:bravia:home:audiomute" }
String Bravia_Channel "Channel [%s]" { channel="sony:bravia:home:channel" }
String Bravia_TripletChannel "Triplet Channel [%s]" { channel="sony:bravia:home:tripletchannel" }
String Bravia_InputSource "Input Source [%s]" { channel="sony:bravia:home:inputsource" }
String Bravia_Input "Input [%s]" { channel="sony:bravia:home:input" }
Switch Bravia_PictureMute "Picture Mute [%s]" { channel="sony:bravia:home:picturemute" }
Switch Bravia_TogglePictureMute "Toggle Picture Mute [%s]" { channel="sony:bravia:home:togglepicturemute", autoupdate="false"  }
Switch Bravia_Pip "PIP [%s]" { channel="sony:bravia:home:pip" }
Switch Bravia_TogglePip "Toggle PIP [%s]" { channel="sony:bravia:home:togglepip", autoupdate="false" }
Switch Bravia_TogglePipPosition "Toggle PIP Position [%s]" { channel="sony:bravia:home:togglepipposition", autoupdate="false" }
String Bravia_BroadcastAddress "Broadcast [%s]" { channel="sony:bravia:home:broadcastaddress" }
String Bravia_MacAddress "MAC [%s]" { channel="sony:bravia:home:macaddress" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame label="Sony Bravia" {
        Selection item=Bravia_IR mappings=[18="1",19="2",20="3",21="4",22="5",23="6",24="7",25="8",26="9",27="0",33="Channel Up",44="Channel Down",98="Unknown",101="Unknown2"]
        Switch item=Bravia_Power
        Slider item=Bravia_Volume
        Switch item=Bravia_AudioMute
        Selection item=Bravia_Channel mappings=[4.1="4.1", 5.1="5.1", 5.2="5.2", 50.1="50.1", 50.2="50.2"]
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

## IRCC Codes
The following is a list of codes documented - there may be more or some may have changed since the document has been published.

0=Power Off
1=Input
2=Guide
3=EPG
4=Favorites
5=Display
6=Home
7=Options
8=Return
9=Up
10=Down
11=Right
12=Left
13=Confirm
14=Red
15=Green
16=Yellow
17=Blue
18=Num1
19=Num2
20=Num3
21=Num4
22=Num5
23=Num6
24=Num7
25=Num8
26=Num9
27=Num0
28=Num11
29=Num12
30=Volume Up
31=Volume Down
32=Mute
33=Channel Up
34=Channel Down
35=Subtitle
36=Closed Caption
37=Enter
38=DOT
39=Analog
40=Teletext
41=Exit
42=Analog2
43=*AD
44=Digital
45=Analog?
46=BS
47=CS
48=BS/CS
49=Ddata
50=Pic Off
51=Tv_Radio
52=Theater
53=SEN
54=Internet Widgets
55=Internet Video
56=Netflix
57=Scene Select
58=Model3D
59=iManual
60=Audio
61=Wide
62=Jump
63=PAP
64=MyEPG
65=Program Description
66=Write Chapter
67=TrackID
68=Ten Key
69=AppliCast
70=acTVila
71=Delete Video
72=Photo Frame
73=TV Pause
74=Key Pad
75=Media
76=Sync Menu
77=Forward
78=Play
79=Rewind
80=Prev
81=Stop
82=Next
83=Rec
84=Pause
85=Eject
86=Flash Plus
87=Flash Minus
88=Top Menus
89=Popup Menu
90=Rakuraku Start
91=One Touch Time Rec
92=One Touch View
93=One Touch Rec
94=One Touch Stop
95=DUX
96=Football Mode
97=Social