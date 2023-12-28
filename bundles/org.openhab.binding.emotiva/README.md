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

| Parameter       | Values                                                        | Default |
|-----------------|---------------------------------------------------------------|---------|
| ipAddress       | IP address of the processor                                   | -       |
| controlPort     | port number, e.g. 7002                                        | 7002    |
| notifyPort      | port number, e.g. 7003                                        | 7003    |
| setupPortTCP    | port number, e.g. 7100                                        | 7100    |
| menuNotifyPort  | port number, e.g. 7005                                        | 7005    |
| protocolVersion | Emotiva Network Protocol version, e.g. 3.0                    | 2.0     |
| keepAlive       | Time between notification update from device, in milliseconds | 7500    |


## Channels

The Emotiva Processor supports the following channels (some channels are model specific):

| Channel Type ID             | Item Type    | Description                                                |
|-----------------------------|--------------|------------------------------------------------------------|
| _Main zone_                 |              |                                                            |
| mainZone#power              | Switch (RW)  | Main zone power on/off                                     |      
| mainZone#volume             | Dimmer (RW)  | Main zone volume                                           |             
| mainZone#volumeDB           | Number (RW)  | Main zone volume in dB (-96 to 15)                         | 
| mainZone#mute               | Switch (RW)  | Main zone mute                                             | 
| mainZone#source             | String (RW)  | Main zone input (HDMI1, TUNER, ARC, ...)                   | 
| _Zone 2_                    |              |                                                            |
| zone2#power                 | Switch (RW)  | Zone 2 power on/off                                        | 
| zone2#volume                | Dimmer (RW)  | Zone 2 volume                                              | 
| zone2#volumeDB              | Number (RW)  | Zone 2 volume in dB (-80 offset)                           | 
| zone2#mute                  | Switch (RW)  | Zone 2 mute                                                |
| zone2#input                 | String (RW)  | Zone 2 input                                               |
| _General_                   |              |                                                            |
| general#power               | Switch (RW)  | Power on/off                                               |
| general#standby             | String (W)   | Set in standby mode                                        |
| general#menu                | String (RW)  | Enter or exit menu                                         |
| general#menu_control        | String (RW)  | Control menu via string commands                           |
| general#up                  | String (W)   | Menu up                                                    |
| general#down                | String (W)   | Menu down                                                  |
| general#left                | String (W)   | Menu left                                                  |
| general#right               | String (W)   | Menu right                                                 |
| general#enter               | String (W)   | Menu enter                                                 |
| general#dim                 | Switch (RW)  | Cycle through FP dimness settings                          |
| general#mode                | String (RW)  | Select audio mode (auto, dts, ...)                         |
| general#info                | String (W)   | Show info screen                                           |
| general#speaker_preset      | String (RW)  | Select speaker presets (preset1, preset2)                  |
| general#center              | Number (RW)  | Center Volume increment up/down (0.5 step)                 |
| general#subwoofer           | Number (RW)  | Subwoofer Volume increment up/down (0.5 step)              |
| general#surround            | Number (RW)  | Surround Volume increment up/down (0.5 step)               |
| general#back                | Number (RW)  | Back Volume increment up/down (0.5 step)                   |
| general#loudness            | Switch (RW)  | Loudness on/off                                            |
| general#treble              | Number (RW)  | Treble Volume increment up/down (0.5 step)                 |
| general#bass                | Number (RW)  | Bass Volume increment up/down (0.5 step)                   |
| general#tuner_band          | String (R)   | Tuner band, (AM, FM)                                       |
| general#tuner_channel       | String (RW)  | User–assigned station name                                 |
| general#tuner_signal        | String (R)   | Tuner signal quality                                       |
| general#tuner_program       | String (R)   | Tuner program: "Country", "Rock", ...                      |
| general#tuner_RDS           | String (R)   | Tuner RDS string                                           |
| general#audio_input         | String (R)   | Audio input source                                         |
| general#audio_bitstream     | String (R)   | Audio input bitstream type: "PCM 2.0", "ATMOS", etc.       |
| general#audio_bits          | String (R)   | Audio input bits: "32kHZ 24bits", etc.                     |
| general#video_input         | String (R)   | Video input source                                         |
| general#video_format        | String (R)   | Video input format: "1920x1080i/60", "3840x2160p/60", etc. |
| general#video_space         | String (R)   | Video input space: "YcbCr 8bits", etc.                     |
| general#input_[1-8]         | String (R)   | User assigned input names                                  |
| general#selected_mode       | String (R)   | User selected mode for the main zone                       |
| general#selected_movie_music | String (R)   | User selected movie or music mode for main zone            |
| general#mode_ref_stereo     | String (R)   | Label for mode: Reference Stereo                           |
| general#mode_stereo         | String (R)   | Label for mode: Stereo                                     |
| general#mode_music          | String (R)   | Label for mode: Music                                      |
| general#mode_movie          | String (R)   | Label for mode: Movie                                      |
| general#mode_direct         | String (R)   | Label for mode: Direct                                     |
| general#mode_dolby          | String (R)   | Label for mode: Dolby                                      |
| general#mode_dts            | String (R)   | Label for mode: DTS                                        |
| general#mode_all_stereo     | String (R)   | Label for mode: All Stereo                                 |
| general#mode_auto           | String (R)   | Label for mode: Auto                                       |
| general#mode_surround       | String (RW)  | Select audio mode (Auto, Stereo, Dolby, ...)               |
| general#width               | Number (RW)  | Width Volume increment up/down (0.5 step)                  |
| general#height              | Number (RW)  | Height Volume increment up/down (0.5 step)                 |
| general#bar                 | String (R)   | Text displayed on front panel bar of device                |
| menu_display_highlight      | String (R)   | Menu Panel Display: Value in focus                         |
| menu_display_top_start      | String (R)   | Menu Panel Display: Top bar, start cell                    |
| menu_display_top_center     | String (R)   | Menu Panel Display: Top bar, center cell                   |
| menu_display_top_end        | String (R)   | Menu Panel Display: Top bar, end cell                      |
| menu_display_middle_start   | String (R)   | Menu Panel Display: Middle bar, start cell                 |
| menu_display_middle_center  | String (R)   | Menu Panel Display: Middle bar, center cell                |
| menu_display_middle_end     | String (R)   | Menu Panel Display: Middle bar, end cell                   |
| menu_display_bottom_start   | String (R)   | Menu Panel Display: Bottom bar, start cell                 |
| menu_display_bottom_center  | String (R)   | Menu Panel Display: Bottom bar, center cell                |
| menu_display_bottom_end     | String (R)   | Menu Panel Display: Bottom bar, end cell                   |

(R) = read-only (no updates possible)
(RW) = read-write

## Full Example

`.things` file:

```perl
Thing emotiva:processor:1 "XMC-2" @ "Living room" [ipAddress="10.0.0.100", protocolVersion="3.0"]
```

`.items` file:

```perl
Switch                  emotiva_power               "Processor"                     {channel="emotiva:processor:1:general#power"}
Dimmer                  emotiva_volume              "Volume [%d %%]"                {channel="emotiva:processor:1:mainZone#volume"}
Number:Dimensionless    emotiva_volume_db           "Volume [%d dB]"                {channel="emotiva:processor:1:mainzone#volume_db"}
Switch                  emotiva_mute                "Mute"                          {channel="emotiva:processor:1:mainZone#mute"}
String                  emotiva_source              "Source [%s]"                   {channel="emotiva:processor:1:mainZone#input"}
String                  emotiva_mode_surround       "Surround Mode: [%s]"           {channel="emotiva:processor:1:general#mode_surround"}
Number:Dimensionless    emotiva_speakers_center     "Center Trim [%.1f dB]"         {channel="emotiva:processor:1:general#center"}
Switch                  emotiva_zone2power          "Zone 2"                        {channel="emotiva:processor:1:zone2#power"}
String                  emotiva_front_panel_bar     "Bar Text"                      {channel="emotiva:processor:1:general#bar"}
String                  emotiva_menu_control        "Menu Control"                  {channel="emotiva:processor:1:general#menu_control"}
String                  emotiva_menu_hightlight     "Menu field focus"              {channel="emotiva:processor:1:general#menu_display_highlight"}
String                  emotiva_menu_top_start      ""                      <none>  {channel="emotiva:processor:1:general#menu_display_top_start"}
String                  emotiva_menu_top_center     ""                      <none>  {channel="emotiva:processor:1:general#menu_display_top_center"}
String                  emotiva_menu_top_end        ""                      <none>  {channel="emotiva:processor:1:general#menu_display_top_end"}
String                  emotiva_menu_middle_start   ""                      <none>  {channel="emotiva:processor:1:general#menu_display_middle_start"}
String                  emotiva_menu_middle_center  ""                      <none>  {channel="emotiva:processor:1:general#menu_display_middle_center"}
String                  emotiva_menu_middle_end     ""                      <none>  {channel="emotiva:processor:1:general#menu_display_middle_end"}
String                  emotiva_menu_tottom_start   ""                      <none>  {channel="emotiva:processor:1:general#menu_display_bottom_start"}
String                  emotiva_menu_tottom_center  ""                      <none>  {channel="emotiva:processor:1:general#menu_display_bottom_center"}
String                  emotiva_menu_tottom_end     ""                      <none>  {channel="emotiva:processor:1:general#menu_display_bottom_end"}
```

`.sitemap` file:

```perl
...
Group item=emotiva_input label="Processor" icon="receiver" {
    Default   item=emotiva_power
    Default   item=emotiva_mute             
    Setpoint  item=emotiva_volume           
    Default   item=emotiva_volume_db        step=2 minValue=-96.0 maxValue=15.0 
    Selection item=emotiva_source           
    Text      item=emotiva_mode_surround    
    Setpoint  item=emotiva_speakers_center  step=0.5 minValue=-12.0 maxValue=12.0
    Default   item=emotiva_zone2power
}
Frame label="Front Panel" {
    Text item=emotiva_front_panel_bar
    Text item=emotiva_menu_highlight
    Frame label="" {
        Text item=emotiva_menu_top_start
        Text item=emotiva_menu_top_center
        Text item=emotiva_menu_top_end
    }
    Frame label="" {
        Text item=emotiva_menu_middle_start
        Text item=emotiva_menu_middle_center
        Text item=emotiva_menu_middle_end
    }
    Frame label="" {
        Text item=emotiva_menu_bottom_start
        Text item=emotiva_menu_bottom_center
        Text item=emotiva_menu_bottom_end
    }
    Buttongrid label="Menu Control" staticIcon=material:control_camera item=emotiva_menu_control buttons=[1:1:POWER="Power"=switch-off , 1:2:MENU="Menu", 1:3:INFO="Info" , 2:2:UP="Up"=f7:arrowtriangle_up , 4:2:DOWN="Down"=f7:arrowtriangle_down , 3:1:LEFT="Left"=f7:arrowtriangle_left , 3:3:RIGHT="Right"=f7:arrowtriangle_right , 3:2:ENTER="Select" ]
}
...
```

## Network Remote Control protocol Reference

These resources can be useful to learn what to send using the `command`channel:

- [Emotiva Remote Interface Description](https://www.dropbox.com/sh/lvo9lbhu89jqfdb/AACa4iguvWK3I6ONjIpyM5Zca/Emotiva_Remote_Interface_Description%20V3.1.docx)
