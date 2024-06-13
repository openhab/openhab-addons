# Emotiva Binding

This binding integrates Emotiva AV processors by using the Emotiva Network Remote Control protocol.

## Supported Things

This binding supports Emotiva processors with Emotiva Network Remote Control protocol support.
The thing type for all of them is `processor`.

Tested models: Emotiva XMC-2

## Discovery

The binding automatically discovers devices on your network.

## Thing Configuration

The Emotiva Processor thing requires the `ipAddress` it can connect to.
There are more parameters which all have defaults set.

| Parameter             | Values                                                        | Default |
|-----------------------|---------------------------------------------------------------|---------|
| ipAddress             | IP address of the processor                                   | -       |
| controlPort           | port number, e.g. 7002                                        | 7002    |
| notifyPort            | port number, e.g. 7003                                        | 7003    |
| infoPort              | port number, e.g. 7004                                        | 7004    |
| setupPortTCP          | port number, e.g. 7100                                        | 7100    |
| menuNotifyPort        | port number, e.g. 7005                                        | 7005    |
| protocolVersion       | Emotiva Network Protocol version, e.g. 3.0                    | 2.0     |
| keepAlive             | Time between notification update from device, in milliseconds | 7500    |
| retryConnectInMinutes | Time between connection retry, in minutes                     | 2       |


## Channels

The Emotiva Processor supports the following channels (some channels are model specific):

| Channel Type ID                    | Item Type          | Description                                                |
|------------------------------------|--------------------|------------------------------------------------------------|
| _Main zone_                        |                    |                                                            |
| main-zone#power                    | Switch (RW)        | Main zone power on/off                                     |      
| main-zone#volume                   | Dimmer (RW)        | Main zone volume in percentage (0 to 100)                  |             
| main-zone#volume-db                | Number (RW)        | Main zone volume in dB (-96 to 15)                         | 
| main-zone#mute                     | Switch (RW)        | Main zone mute                                             | 
| main-zone#source                   | String (RW)        | Main zone input (HDMI1, TUNER, ARC, ...)                   | 
| _Zone 2_                           |                    |                                                            |
| zone2#power                        | Switch (RW)        | Zone 2 power on/off                                        | 
| zone2#volume                       | Dimmer (RW)        | Zone 2 volume in percentage (0 to 100)                     | 
| zone2#volume-db                    | Number (RW)        | Zone 2 volume in dB (-80 offset)                           | 
| zone2#mute                         | Switch (RW)        | Zone 2 mute                                                |
| zone2#input                        | String (RW)        | Zone 2 input                                               |
| _General_                          |                    |                                                            |
| general#power                      | Switch (RW)        | Power on/off                                               |
| general#standby                    | String (W)         | Set in standby mode                                        |
| general#menu                       | String (RW)        | Enter or exit menu                                         |
| general#menu-control               | String (W)         | Control menu via string commands                           |
| general#up                         | String (W)         | Menu up                                                    |
| general#down                       | String (W)         | Menu down                                                  |
| general#left                       | String (W)         | Menu left                                                  |
| general#right                      | String (W)         | Menu right                                                 |
| general#enter                      | String (W)         | Menu enter                                                 |
| general#dim                        | Switch (RW)        | Cycle through FP dimness settings                          |
| general#mode                       | String (RW)        | Select audio mode (auto, dts, ...)                         |
| general#info                       | String (W)         | Show info screen                                           |
| general#speaker-preset             | String (RW)        | Select speaker presets (preset1, preset2)                  |
| general#center                     | Number (RW)        | Center Volume increment up/down (0.5 step)                 |
| general#subwoofer                  | Number (RW)        | Subwoofer Volume increment up/down (0.5 step)              |
| general#surround                   | Number (RW)        | Surround Volume increment up/down (0.5 step)               |
| general#back                       | Number (RW)        | Back Volume increment up/down (0.5 step)                   |
| general#loudness                   | Switch (RW)        | Loudness on/off                                            |
| general#treble                     | Number (RW)        | Treble Volume increment up/down (0.5 step)                 |
| general#bass                       | Number (RW)        | Bass Volume increment up/down (0.5 step)                   |
| general#frequenncy                 | Rollershutter (W)  | Frequency up/down, (100 kHz step)                          |
| general#seek                       | Rollershutter (W)  | Seek signal up/down                                        |
| general#channel                    | Rollershutter (W)  | Channel up/down                                            |
| general#tuner-band                 | String (R)         | Tuner band, (AM, FM)                                       |
| general#tuner-channel              | String (RW)        | User–assigned station name                                 |
| general#tuner-signal               | String (R)         | Tuner signal quality                                       |
| general#tuner-program              | String (R)         | Tuner program: "Country", "Rock", ...                      |
| general#tuner-RDS                  | String (R)         | Tuner RDS string                                           |
| general#audio-input                | String (R)         | Audio input source                                         |
| general#audio-bitstream            | String (R)         | Audio input bitstream type: "PCM 2.0", "ATMOS", etc.       |
| general#audio-bits                 | String (R)         | Audio input bits: "32kHZ 24bits", etc.                     |
| general#video-input                | String (R)         | Video input source                                         |
| general#video-format               | String (R)         | Video input format: "1920x1080i/60", "3840x2160p/60", etc. |
| general#video-space                | String (R)         | Video input space: "YcbCr 8bits", etc.                     |
| general#input-[1-8]                | String (R)         | User assigned input names                                  |
| general#selected-mode              | String (R)         | User selected mode for the main zone                       |
| general#selected-movie-music       | String (R)         | User selected movie or music mode for main zone            |
| general#mode-ref-stereo            | String (R)         | Label for mode: Reference Stereo                           |
| general#mode-stereo                | String (R)         | Label for mode: Stereo                                     |
| general#mode-music                 | String (R)         | Label for mode: Music                                      |
| general#mode-movie                 | String (R)         | Label for mode: Movie                                      |
| general#mode-direct                | String (R)         | Label for mode: Direct                                     |
| general#mode-dolby                 | String (R)         | Label for mode: Dolby                                      |
| general#mode-dts                   | String (R)         | Label for mode: DTS                                        |
| general#mode-all-stereo            | String (R)         | Label for mode: All Stereo                                 |
| general#mode-auto                  | String (R)         | Label for mode: Auto                                       |
| general#mode-surround              | String (RW)        | Select audio mode (Auto, Stereo, Dolby, ...)               |
| general#width                      | Number (RW)        | Width Volume increment up/down (0.5 step)                  |
| general#height                     | Number (RW)        | Height Volume increment up/down (0.5 step)                 |
| general#bar                        | String (R)         | Text displayed on front panel bar of device                |
| general#menu-display-highlight     | String (R)         | Menu Panel Display: Value in focus                         |
| general#menu-display-top-start     | String (R)         | Menu Panel Display: Top bar, start cell                    |
| general#menu-display-top-center    | String (R)         | Menu Panel Display: Top bar, center cell                   |
| general#menu-display-top-end       | String (R)         | Menu Panel Display: Top bar, end cell                      |
| general#menu-display-middle-start  | String (R)         | Menu Panel Display: Middle bar, start cell                 |
| general#menu-display-middle-center | String (R)         | Menu Panel Display: Middle bar, center cell                |
| general#menu-display-middle-end    | String (R)         | Menu Panel Display: Middle bar, end cell                   |
| general#menu-display-bottom-start  | String (R)         | Menu Panel Display: Bottom bar, start cell                 |
| general#menu-display-bottom-center | String (R)         | Menu Panel Display: Bottom bar, center cell                |
| general#menu-display-bottom-end    | String (R)         | Menu Panel Display: Bottom bar, end cell                   |

(R)  = read-only (no updates possible)
(W)  = write-only
(RW) = read-write

## Full Example

### `.things` file:

```perl
Thing emotiva:processor:1 "XMC-2" @ "Living room" [ipAddress="10.0.0.100", protocolVersion="3.0"]
```

### `.items` file:

```perl
Switch                  emotiva-power               "Processor"                     {channel="emotiva:processor:1:general#power"}
Dimmer                  emotiva-volume              "Volume [%d %%]"                {channel="emotiva:processor:1:main-zone#volume"}
Number:Dimensionless    emotiva-volume-db           "Volume [%d dB]"                {channel="emotiva:processor:1:main-zone#volume-db"}
Switch                  emotiva-mute                "Mute"                          {channel="emotiva:processor:1:main-zone#mute"}
String                  emotiva-source              "Source [%s]"                   {channel="emotiva:processor:1:main-zone#input"}
String                  emotiva-mode-surround       "Surround Mode: [%s]"           {channel="emotiva:processor:1:general#mode-surround"}
Number:Dimensionless    emotiva-speakers-center     "Center Trim [%.1f dB]"         {channel="emotiva:processor:1:general#center"}
Switch                  emotiva-zone2power          "Zone 2"                        {channel="emotiva:processor:1:zone2#power"}
String                  emotiva-front-panel-bar     "Bar Text"                      {channel="emotiva:processor:1:general#bar"}
String                  emotiva-menu-control        "Menu Control"                  {channel="emotiva:processor:1:general#menu-control"}
String                  emotiva-menu-hightlight     "Menu field focus"              {channel="emotiva:processor:1:general#menu-display-highlight"}
String                  emotiva-menu-top-start      ""                      <none>  {channel="emotiva:processor:1:general#menu-display-top-start"}
String                  emotiva-menu-top-center     ""                      <none>  {channel="emotiva:processor:1:general#menu-display-top-center"}
String                  emotiva-menu-top-end        ""                      <none>  {channel="emotiva:processor:1:general#menu-display-top-end"}
String                  emotiva-menu-middle-start   ""                      <none>  {channel="emotiva:processor:1:general#menu-display-middle-start"}
String                  emotiva-menu-middle-center  ""                      <none>  {channel="emotiva:processor:1:general#menu-display-middle-center"}
String                  emotiva-menu-middle-end     ""                      <none>  {channel="emotiva:processor:1:general#menu-display-middle-end"}
String                  emotiva-menu-tottom-start   ""                      <none>  {channel="emotiva:processor:1:general#menu-display-bottom-start"}
String                  emotiva-menu-tottom-center  ""                      <none>  {channel="emotiva:processor:1:general#menu-display-bottom-center"}
String                  emotiva-menu-tottom-end     ""                      <none>  {channel="emotiva:processor:1:general#menu-display-bottom-end"}
```

### `.sitemap` file:

```perl
Group item=emotiva-input label="Processor" icon="receiver" {
    Default   item=emotiva-power
    Default   item=emotiva-mute             
    Setpoint  item=emotiva-volume           
    Default   item=emotiva-volume-db        step=2 minValue=-96.0 maxValue=15.0 
    Selection item=emotiva-source           
    Text      item=emotiva-mode-surround    
    Setpoint  item=emotiva-speakers-center  step=0.5 minValue=-12.0 maxValue=12.0
    Default   item=emotiva-zone2power
}
Frame label="Front Panel" {
    Text item=emotiva-front-panel-bar
    Text item=emotiva-menu-highlight
    Frame label="" {
        Text item=emotiva-menu-top-start
        Text item=emotiva-menu-top-center
        Text item=emotiva-menu-top-end
    }
    Frame label="" {
        Text item=emotiva-menu-middle-start
        Text item=emotiva-menu-middle-center
        Text item=emotiva-menu-middle-end
    }
    Frame label="" {
        Text item=emotiva-menu-bottom-start
        Text item=emotiva-menu-bottom-center
        Text item=emotiva-menu-bottom-end
    }
    Buttongrid label="Menu Control" staticIcon=material:control-camera item=emotiva-menu_control buttons=[1:1:POWER="Power"=switch-off , 1:2:MENU="Menu", 1:3:INFO="Info" , 2:2:UP="Up"=f7:arrowtriangle_up , 4:2:DOWN="Down"=f7:arrowtriangle_down , 3:1:LEFT="Left"=f7:arrowtriangle_left , 3:3:RIGHT="Right"=f7:arrowtriangle_right , 3:2:ENTER="Select" ]
}
```

## Network Remote Control Protocol Reference

These resources can be useful to learn what to send using the `command` channel:

- [Emotiva Remote Interface Description](https://www.dropbox.com/sh/lvo9lbhu89jqfdb/AACa4iguvWK3I6ONjIpyM5Zca/Emotiva_Remote_Interface_Description%20V3.1.docx)
