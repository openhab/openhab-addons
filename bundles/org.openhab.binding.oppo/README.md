# Oppo Blu-ray player Binding

This binding can be used to control the OPPO UDP-203/205 or BDP-83/93/95/103/105 Blu-ray player.
Almost all features of the various models of this player line are supported by the binding.
Please review the notes below for some important usage caveats.

The binding supports three different kinds of connections:

- direct IP connection (with caveats),
- serial connection,
- serial over IP connection

For users without a serial port on the server side, you can use a USB to serial adapter.

You don't need to have your player device directly connected to your openHAB server.
You can connect it for example to a Raspberry Pi and use [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) to make the serial connection available on the LAN (serial over IP).

## Supported Things

The supported Thing types are:

- `player` Represents any supported Oppo player; Deprecated.
- `bdp-83` BDP-83 Blu-ray player
- `bdp-93` BDP-93 or BDP-95 Blu-ray player
- `bdp-103` BDP-103 or BDP-103D Blu-ray player
- `bdp-105` BDP-105 or BDP-105D Blu-ray player
- `udp-203` UDP-203 UHD Blu-ray player
- `udp-205` UDP-205 UHD Blu-ray player

## Discovery

Manually initiated Auto-discovery is supported if the player is connected via Ethernet and accessible on the same IP subnet of the openHAB server.
When adding a Thing, choose the Oppo Blu-ray Player Binding and then press the Scan button to initiate discovery.

## Binding Configuration

There are no overall binding configuration settings that need to be set.
All settings are through Thing configuration parameters.

## Thing Configuration

The Thing has the following configuration parameters:

| Parameter Label  | Parameter ID | Description                                                                                                                      | Accepted values               |
|------------------|--------------|----------------------------------------------------------------------------------------------------------------------------------|-------------------------------|
| Player Model     | model        | Deprecated. For the `player` thing type only, specifies what model of player is to be controlled by the binding (required).      | 83, 93, 103, 105, 203, or 205 |
| Address          | host         | Host name or IP address of the Oppo player or serial over IP device.                                                             | host name or IP               |
| Port             | port         | Communication port for using serial over IP. Leave blank if using direct IP connection to the player.                            | IP port number                |
| Serial Port      | serialPort   | Serial port to use for directly connecting to the Oppo player                                                                    | a comm port name              |
| Verbose Mode     | verboseMode  | (Optional) If true, the player will send time updates every second. If set false, the binding polls the player every 10 seconds. | Boolean; default false        |

Some notes:

- The UDP-20x series is fully functional over the direct IP connection but has a known issue where the player can become completely unresponsive to IP control commands.
- To restore IP control, you need to physically disconnect the power cable, then power the player back on (the network stack only starts when the player is powered on).
- All player models can only support one direct IP connection at a time.
- Using the direct IP connection on the BDP series (83/93/95/103/105) is not recommended; use of serial or serial over IP connections is preferred.
- If using the direct IP connection on the BDP series the following control channels only work as read-only: Volume, Mute, Time Mode, Repeat Mode, Zoom Mode, OSD Position, Subtitle Shift, HDMI Mode
- Verbose mode is also not supported while using the direct IP connection on the BDP series.
- As previously noted, when using verbose mode, the player will send time code messages once per second while playback is ongoing.
- In non-verbose (the default), the binding will poll the player every 10 seconds to update play time, track and chapter information instead.
- In order for the direct IP connection to work while the player is turned off, the Device Setup → Standby Mode setting must be set to "Quick Start" or "Network Standby" in the Device Setup menu.
- Likewise, if the player is turned off, it may not be discoverable by the Binding's discovery scan.
- Prior to using the binding, ensure that the player's firmware is up to date with the latest available version.
- Available HDMI modes for BDP-83 & BDP-9x: AUTO, SRC, 1080P, 1080I, 720P, SDP, SDI
- Available HDMI modes for BDP-10x: AUTO, SRC, 4K2K, 1080P, 1080I, 720P, SDP, SDI
- Available HDMI modes for UDP-20x: AUTO, SRC, UHD_AUTO, UHD24, UHD50, UHD60, 1080P_AUTO, 1080P24, 1080P50, 1080P60, 1080I50, 1080I60, 720P50, 720P60, 576P, 576I, 480P, 480I

- On Linux, you may get an error stating the serial port cannot be opened when the Oppo binding tries to load.
- You can get around this by adding the `openhab` user to the `dialout` group like this: `usermod -a -G dialout openhab`.
- Also, on Linux you may have issues with the USB if using two serial USB devices e.g. Oppo and RFXcom.
- See the [general documentation about serial port configuration](/docs/administration/serial.html) for more on symlinking the USB ports.
- Here is an example of ser2net.conf (for ser2net version < 4) you can use to share your serial port /dev/ttyUSB0 on IP port 4444 using [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) (take care, the baud rate is specific to the Oppo player):

```text
4444:raw:0:/dev/ttyUSB0:9600 8DATABITS NONE 1STOPBIT LOCAL
```

- Here is an example of ser2net.yaml (for ser2net version >= 4) you can use to share your serial port /dev/ttyUSB0 on IP port 4444 using [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) (take care, the baud rate is specific to the Oppo player):

```yaml
connection: &conOppo
    accepter: tcp,4444
    enable: on
    options:
      kickolduser: true
    connector: serialdev,
              /dev/ttyUSB0,
              9600n81,local
```

## Channels

The following channels are available:

| Channel ID        | Item Type   | Description                                                                                                                           |
|-------------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------|
| power             | Switch      | Turn the power for the player on or off                                                                                               |
| volume            | Dimmer      | Control the volume for the player (0-100%)                                                                                            |
| mute              | Switch      | Mute or unmute the volume on the player                                                                                               |
| source            | Number      | Select the source input for the player (0-6; number of available options varies by model)                                             |
| play-mode         | String      | Indicates the current playback mode of the player (Read-only)                                                                         |
| control           | Player      | Simulate pressing the transport control buttons on the remote control (play/pause/next/previous/rew/ffwd)                             |
| time-mode         | String      | Sets the time information display mode on the player (T= Title Elapsed, X= Title Remaining, C= Chapter Elapsed, K= Chapter Remaining) |
| time-display      | Number:Time | Indicates the time information that matches the display on the player for the current `time mode` setting (Read-only)                 |
| current_title     | Number      | The current title or track number playing (Read-only)                                                                                 |
| total-title       | Number      | The total number of titles or tracks on the disc (Read-only)                                                                          |
| current-chapter   | Number      | The current chapter number (Read-only)                                                                                                |
| total-chapter     | Number      | The total number of chapters in the current title (Read-only)                                                                         |
| title-elapsed     | Number:Time | The playback time elapsed of the current title or track (Read-only)                                                                   |
| title-length      | Number:Time | The total length of the current title or track (Read-only)                                                                            |
| title-end-time    | DateTime    | The date/time when the current title or track will end (Read-only)                                                                    |
| title-progress    | Dimmer      | The current progress [0-100%] of the current title or track (Read-only)                                                               |
| repeat-mode       | String      | Sets the current repeat mode (00-06)                                                                                                  |
| zoom-mode         | String      | Sets the current zoom mode (00-12)                                                                                                    |
| disc-type         | String      | The current type of disc in the player (Read-only)                                                                                    |
| audio-type        | String      | The current audio track type (Read-only)                                                                                              |
| subtitle-type     | String      | The current subtitle selected (Read-only)                                                                                             |
| aspect-ratio      | String      | The aspect ratio of the current video output [UDP-203/205 only] (Read-only)                                                           |
| source-resolution | String      | The video resolution of the content being played (Read-only)                                                                          |
| output-resolution | String      | The video resolution of the player output (Read-only)                                                                                 |
| 3d-indicator      | String      | Indicates if the content playing is 2D or 3D (Read-only)                                                                              |
| osd-position      | Number      | Sets the OSD position (0 to 5)                                                                                                        |
| sub-shift         | Number      | Sets the subtitle shift (-10 to 10)(note more than 5 from 0 throws an error on the BDP-103)                                           |
| hdmi-mode         | String      | Sets the current HDMI output mode (options vary by model; see notes above for allowed values)                                         |
| hdr-mode          | String      | Sets current HDR output mode (Auto, On, Off) [UDP-203/205 only]                                                                       |
| remote-button     | String      | Simulate pressing a button on the remote control [3 letter code; codes can be found in Appendix A below] (Write-only)                 |

## Full Example

### `oppo.things` Example

```java
// UDP-203 direct IP connection
oppo:udp-203:myoppo "OPPO UDP-203" [ host="192.168.0.10", verboseMode=true ]

// BDP-103 direct serial connection
oppo:bdp-103:myoppo "OPPO BDP-103" [ serialPort="COM5", verboseMode=true ]

// Generic serial over IP connection
oppo:player:myoppo "OPPO Blu-ray" [ host="192.168.0.9", port=4444, model=103, verboseMode=true ]

```

### `oppo.items` Example

```java
// Note: replace `player` with the appropriate Thing type

Switch oppo_power "Power" { channel="oppo:player:myoppo:power" }
Dimmer oppo_volume "Volume [%d %%]" { channel="oppo:player:myoppo:volume" }
Switch oppo_mute "Mute" { channel="oppo:player:myoppo:mute" }
Number oppo_source "Source Input [%s]" { channel="oppo:player:myoppo:source" }
String oppo_play_mode "Play Mode [%s]" { channel="oppo:player:myoppo:play-mode" }
Player oppo_control "Control" { channel="oppo:player:myoppo:control" }
String oppo_time_mode "Time Mode [%s]" { channel="oppo:player:myoppo:time-mode" }
Number:Time oppo_time_display "Time [%1$tT]" { channel="oppo:player:myoppo:time-display" }
Number oppo_current_title "Current Title/Track [%s]" { channel="oppo:player:myoppo:current-title" }
Number oppo_total_title "Total Title/Track [%s]" { channel="oppo:player:myoppo:total-title" }
Number oppo_current_chapter "Current Chapter [%s]" { channel="oppo:player:myoppo:current-chapter" }
Number oppo_total_chapter "Total Chapter [%s]" { channel="oppo:player:myoppo:total-chapter" }
Number:Time oppo_title_elapsed "Title Elapsed [%1$tT]" { channel="oppo:player:myoppo:title-elapsed" }
Number:Time oppo_title_length "Title Length [%1$tT]" { channel="oppo:player:myoppo:title-length" }
DateTime oppo_title_end_time "Title End Time [%1$tY-%1$tm-%1$td %1$tH:%1$tM]" { channel="oppo:player:myoppo:title-end-time" }
Dimmer oppo_title_progress "Progress [%.0f%%]" { channel="oppo:player:myoppo:title-progress" }
String oppo_repeat_mode "Repeat Mode [%s]" { channel="oppo:player:myoppo:repeat-mode" }
String oppo_zoom_mode "Zoom Mode [%s]" { channel="oppo:player:myoppo:zoom-mode" }
String oppo_disc_type "Disc Type [%s]" { channel="oppo:player:myoppo:disc-type" }
String oppo_audio_type "Audio Type [%s]" { channel="oppo:player:myoppo:audio-type" }
String oppo_subtitle_type "Subtitle Type [%s]" { channel="oppo:player:myoppo:subtitle-type" }
String oppo_aspect_ratio "Aspect Ratio [%s]" { channel="oppo:player:myoppo:aspect-ratio" }
String oppo_source_resolution "Source Resolution [%s]" { channel="oppo:player:myoppo:source-resolution" }
String oppo_output_resolution "Output Resolution [%s]" { channel="oppo:player:myoppo:output-resolution" }
String oppo_3d_indicator "3D/2D Indicator [%s]" { channel="oppo:player:myoppo:3d-indicator" }
Number oppo_osd_position "OSD Position [%s]" { channel="oppo:player:myoppo:osd-position" }
Number oppo_sub_shift "Subtitle Shift [%s]" { channel="oppo:player:myoppo:sub-shift" }
String oppo_hdmi_mode "HDMI Mode [%s]" { channel="oppo:player:myoppo:hdmi-mode" }
String oppo_hdr_mode "HDR Mode [%s]" { channel="oppo:player:myoppo:hdr-mode" }
String oppo_remote_button "Remote Button [%s]" { channel="oppo:player:myoppo:remote-button" }
```

### `oppo.sitemap` Example

```perl
sitemap oppo label="OPPO Blu-ray" {
    Frame label="Player"    {
        Switch item=oppo_power
        // Volume can be a Setpoint also
        Slider item=oppo_volume minValue=0 maxValue=100 step=1 visibility=[oppo_power==ON] icon="soundvolume"
        Switch item=oppo_mute visibility=[oppo_power==ON] icon="soundvolume_mute"
        Selection item=oppo_source visibility=[oppo_power==ON] icon="player"
        Text item=oppo_play_mode visibility=[oppo_power==ON] icon="zoom"
        Default item=oppo_control visibility=[oppo_power==ON]
        Selection item=oppo_time_mode visibility=[oppo_power==ON] icon="time"
        Text item=oppo_time_display visibility=[oppo_power==ON] icon="time"
        Text item=oppo_current_title visibility=[oppo_power==ON] icon="zoom"
        Text item=oppo_total_title visibility=[oppo_power==ON] icon="zoom"
        Text item=oppo_current_chapter visibility=[oppo_power==ON] icon="zoom"
        Text item=oppo_total_chapter visibility=[oppo_power==ON] icon="zoom"
        Text item=oppo_title_elapsed visibility=[oppo_power==ON] icon="time"
        Text item=oppo_title_length visibility=[oppo_power==ON] icon="time"
        Text item=oppo_title_end_time visibility=[oppo_power==ON] icon="time"
        Slider item=oppo_title_progress visibility=[oppo_power==ON] icon="time"
        Selection item=oppo_repeat_mode visibility=[oppo_power==ON] icon="none"
        Selection item=oppo_zoom_mode visibility=[oppo_power==ON] icon="none"
        Text item=oppo_disc_type visibility=[oppo_power==ON] icon="none"
        Text item=oppo_audio_type visibility=[oppo_power==ON] icon="none"
        Text item=oppo_subtitle_type visibility=[oppo_power==ON] icon="none"
        Text item=oppo_aspect_ratio visibility=[oppo_power==ON] icon="none"
        Text item=oppo_source_resolution visibility=[oppo_power==ON] icon="video"
        Text item=oppo_output_resolution visibility=[oppo_power==ON] icon="video"
        Text item=oppo_3d_indicator visibility=[oppo_power==ON] icon="none"
        Setpoint item=oppo_osd_position label="OSD Position [%d]" minValue=0 maxValue=5 step=1 visibility=[oppo_power==ON]
        Setpoint item=oppo_sub_shift label="Sub Title Shift [%d]" minValue=-10 maxValue=10 step=1 visibility=[oppo_power==ON]
        Selection item=oppo_hdmi_mode visibility=[oppo_power==ON] icon="video"
        Selection item=oppo_hdr_mode visibility=[oppo_power==ON] icon="colorwheel"
        // This Selection is deprecated in favor of the Buttongrid element below
        Selection item=oppo_remote_button visibility=[oppo_power==ON]
        Buttongrid label="Remote Control" staticIcon=material:tv_remote item=oppo_remote_button buttons=[1:1:POW="Power"=switch-off, 1:3:SRC="Input", 1:4:EJT="Open"=f7:eject, 2:2:NFX="Netflix", 2:3:VDU="Vudu", 3:1:PUR="Pure Audio", 3:2:VDN="Volume -", 3:3:VUP="Volume +", 3:4:MUT="Mute"=soundvolume_mute, 4:1:NU1="1", 4:2:NU2="2", 4:3:NU3="3", 4:4:HOM="Home"=f7:house, 5:1:NU4="4", 5:2:NU5="5", 5:3:NU6="6", 5:4:PUP="Page Up", 6:1:NU7="7", 6:2:NU8="8", 6:3:NU9="9", 6:4:PDN="Page Down", 7:1:CLR="Clear", 7:2:NU0="0", 7:3:GOT="Goto", 7:4:OSD="Info", 8:1:TTL="Top Menu", 8:2:NUP="Up"=f7:arrowtriangle_up, 8:3:MNU="Pop-Up Menu", 9:1:NLT="Left"=f7:arrowtriangle_left, 9:2:SEL="Enter", 9:3:NRT="Right"=f7:arrowtriangle_right, 10:1:OPT="Option", 10:2:NDN="Down"=f7:arrowtriangle_down, 10:3:RET="Return", 11:1:RED="Red", 11:2:GRN="Green", 11:3:BLU="Blue", 11:4:YLW="Yellow", 12:1:STP="Stop"=f7:stop, 12:2:PLA="Play"=f7:play, 12:3:PAU="Pause"=f7:pause, 13:1:PRE="Prev"=f7:backward_end_alt, 13:2:REV="Rev"=f7:backward, 13:3:FWD="Fwd"=f7:forward, 13:4:NXT="Next"=f7:forward_end_alt, 14:1:AUD="Audio", 14:2:SUB="Subtitle", 14:3:ZOM="Zoom", 14:4:M3D="3D", 15:1:SET="Setup", 15:2:ATB="AB Replay", 15:3:RPT="Repeat", 15:4:DIM="Dimmer", 16:1:HDM="Resolution", 16:2:SEH="Picture", 16:3:INH="Detail Info", 16:4:HDR="HDR"]
    }
}
```

### Appendix A - 'remote_button' codes

| Command | Function                                                                              |
|---------|---------------------------------------------------------------------------------------|
| POW     | Toggle power ON and OFF                                                               |
| SRC     | Select input source                                                                   |
| EJT     | Open/close the disc tray                                                              |
| PON     | Discrete on                                                                           |
| POF     | Discrete off                                                                          |
| SYS     | Switch output TV system (PAL/NTSC/MULTI)                                              |
| DIM     | Dim front panel display                                                               |
| PUR     | Pure audio mode (no video)                                                            |
| VUP     | Increase volume                                                                       |
| VDN     | Decrease volume                                                                       |
| MUT     | Mute/Unmute audio                                                                     |
| NU1     | Numeric key 1                                                                         |
| NU2     | Numeric key 2                                                                         |
| NU3     | Numeric key 3                                                                         |
| NU4     | Numeric key 4                                                                         |
| NU5     | Numeric key 5                                                                         |
| NU6     | Numeric key 6                                                                         |
| NU7     | Numeric key 7                                                                         |
| NU8     | Numeric key 8                                                                         |
| NU9     | Numeric key 9                                                                         |
| NU0     | Numeric key 0                                                                         |
| CLR     | Clear numeric input                                                                   |
| GOT     | Play from a specified location                                                        |
| HOM     | Go to Home Menu to select media source                                                |
| PUP     | Show previous page                                                                    |
| PDN     | Show next page                                                                        |
| OSD     | Show/hide on-screen display                                                           |
| TTL     | Show BD top menu or DVD title menu                                                    |
| MNU     | Show BD pop-up menu or DVD menu                                                       |
| NUP     | Up Arrow Navigation                                                                   |
| NLT     | Left Arrow Navigation                                                                 |
| NRT     | Right Arrow Navigation                                                                |
| NDN     | Down Arrow Navigation                                                                 |
| SEL     | ENTER Navigation                                                                      |
| SET     | Enter the player setup menu                                                           |
| RET     | Return to the previous menu or mode                                                   |
| RED     | RED Function varies by content                                                        |
| GRN     | GREEN Function varies by content                                                      |
| BLU     | BLUE Function varies by content                                                       |
| YLW     | YELLOW Function varies by content                                                     |
| STP     | Stop playback                                                                         |
| PLA     | Start playback                                                                        |
| PAU     | Pause playback                                                                        |
| PRE     | Skip to previous                                                                      |
| REV     | Fast reverse play                                                                     |
| FWD     | Fast forward play                                                                     |
| NXT     | Skip to next                                                                          |
| AUD     | Change audio language or channel                                                      |
| SUB     | Change subtitle language                                                              |
| ANG     | Change camera angle                                                                   |
| ZOM     | Zoom in/out and adjust aspect ratio                                                   |
| SAP     | Turn on/off Secondary Audio Program                                                   |
| ATB     | AB Repeat play the selected section                                                   |
| RPT     | Repeat play                                                                           |
| PIP     | Show/hide Picture-in-Picture                                                          |
| HDM     | Switch output resolution                                                              |
| SUH     | Press and hold the SUBTITLE key. This activates the subtitle shift feature.           |
| NFX     | Stop current playback and start the Netflix application (BDP-10x models only)         |
| VDU     | Stop current playback and start the VUDU application (BDP-10x models only)            |
| OPT     | Show/hide the Option menu (BDP-10x & UDP-20x models)                                  |
| M3D     | 3D Show/hide the 2D-to-3D Conversion or 3D adjustment menu (BDP-10x & UDP-20x models) |
| SEH     | Display the Picture Adjustment menu (BDP-10x & UDP-20x models)                        |
| DRB     | Display the Darbee Adjustment menu (BDP-103D/105D models only)                        |
| HDR     | Display the HDR selection menu (UDP-20x models only)                                  |
| INH     | Show on-screen detailed information (UDP-20x models only)                             |
| RLH     | Set resolution to Auto (UDP-20x models only)                                          |
| AVS     | Display the A/V Sync adjustment menu (UDP-20x models only)                            |
| GPA     | Gapless Play (UDP-20x models only)                                                    |
