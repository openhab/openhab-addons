# Oppo Blu-ray player Binding

This binding can be used to control the Oppo UDP-203/205 or BDP-83/93/95/103/105 Blu-ray player.
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

There is exactly one supported thing type, which represents the player.
It has the `player` id.

## Discovery

Manually initiated Auto-discovery is supported if the player is accessible on the same IP subnet of the openHAB server.
In the Inbox, select Search For Things and then choose the Oppo Blu-ray Player Binding to initiate discovery.

## Binding Configuration

There are no overall binding configuration settings that need to be set.
All settings are through thing configuration parameters.

## Thing Configuration

The thing has the following configuration parameters:

| Parameter Label  | Parameter ID | Description                                                                                                                      | Accepted values           |
|------------------|--------------|----------------------------------------------------------------------------------------------------------------------------------|---------------------------|
| Player Model     | model        | Specifies what model of player is to be controlled by the binding (required).                                                    | 83, 103, 105, 203, or 205 |
| Address          | host         | Host name or IP address of the Oppo player or serial over IP device.                                                             | host name or ip           |
| Port             | port         | Communication port for using serial over IP. Leave blank if using direct IP connection to the player.                            | ip port number            |
| Serial Port      | serialPort   | Serial port to use for directly connecting to the Oppo player                                                                    | a comm port name          |
| Verbose Mode     | verboseMode  | (Optional) If true, the player will send time updates every second. If set false, the binding polls the player every 10 seconds. | Boolean; default false    |

Some notes:

- If using direct IP connection on the BDP series (83/93/95/103/105), verbose mode is not supported.
- For some reason on these models, the unsolicited status update messages are not generated over the IP socket.
- If fast updates are required on these models, a direct serial or serial over IP connection to the player is required.
- The UDP-20x series should be fully functional over direct IP connection but this was not able to be tested by the developer.
- As previously noted, when using verbose mode, the player will send time code messages once per second while playback is ongoing.
- Be aware that this could cause performance impacts to your openHAB system.
- In non-verbose (the default), the binding will poll the player every 10 seconds to update play time, track and chapter information instead.
- In order for the direct IP connection to work while the player is turned off, the Standby Mode setting must be set to "Quick Start" in the Device Setup menu.
- Likewise if the player is turned off, it may not be discoverable by the Binding's discovery scan.
- If the player is switched off when the binding first starts up or if power to the player is ever interrupted, up to 30 seconds may elapse before the binding begins to update when the player is switched on.
- If you experience any issues using the binding, first ensure that the player's firmware is up to date with the latest available version (especially on the older models).
- For the older models, some of the features in the control API were added after the players were shipped.
- Available HDMI modes for BDP-83 & BDP-9x: AUTO, SRC, 1080P, 1080I, 720P, SDP, SDI
- Available HDMI modes for BDP-10x: AUTO, SRC, 4K2K, 1080P, 1080I, 720P, SDP, SDI
- Available HDMI modes for UDP-20x: AUTO, SRC, UHD_AUTO, UHD24, UHD50, UHD60, 1080P_AUTO, 1080P24, 1080P50, 1080P60, 1080I50, 1080I60, 720P50, 720P60, 567P, 567I, 480P, 480I

- On Linux, you may get an error stating the serial port cannot be opened when the Oppo binding tries to load.
- You can get around this by adding the `openhab` user to the `dialout` group like this: `usermod -a -G dialout openhab`.
- Also on Linux you may have issues with the USB if using two serial USB devices e.g. Oppo and RFXcom.
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
| play_mode         | String      | Indicates the current playback mode of the player (ReadOnly)                                                                          |
| control           | Player      | Simulate pressing the transport control buttons on the remote control (play/pause/next/previous/rew/ffwd)                             |
| time_mode         | String      | Sets the time information display mode on the player (T= Title Elapsed, X= Title Remaining, C= Chapter Elapsed, K= Chapter Remaining) |
| time_display      | Number:Time | The playback time elapsed/remaining in seconds (ReadOnly)                                                                             |
| current_title     | Number      | The current title or track number playing (ReadOnly)                                                                                  |
| total_title       | Number      | The total number of titles or tracks on the disc (ReadOnly)                                                                           |
| current_chapter   | Number      | The current chapter number player (ReadOnly)                                                                                          |
| total_chapter     | Number      | The total number of chapters in the current title (ReadOnly)                                                                          |
| repeat_mode       | String      | Sets the current repeat mode (00-06)                                                                                                  |
| zoom_mode         | String      | Sets the current zoom mode (00-12)                                                                                                    |
| disc_type         | String      | The current type of disc in the player (ReadOnly)                                                                                     |
| audio_type        | String      | The current audio track type (ReadOnly)                                                                                               |
| subtitle_type     | String      | The current subtitle selected (ReadOnly)                                                                                              |
| aspect_ratio      | String      | The aspect ratio of the current video output [UDP-203/205 only] (ReadOnly)                                                            |
| source_resolution | String      | The video resolution of the content being played (ReadOnly)                                                                           |
| output_resolution | String      | The video resolution of the player output (ReadOnly)                                                                                  |
| 3d_indicator      | String      | Indicates if the content playing is 2D or 3D (ReadOnly)                                                                               |
| osd_position      | Number      | Sets the OSD position (0 to 5) [10x models and up]                                                                                    |
| sub_shift         | Number      | Sets the subtitle shift (-10 to 10) [10x models and up] (note more than 5 from 0 throws an error on the BDP103)                       |
| hdmi_mode         | String      | Sets the current HDMI output mode (options vary by model; see notes above for allowed values)                                         |
| hdr_mode          | String      | Sets current HDR output mode (Auto, On, Off) [UDP-203/205 only]                                                                       |
| remote_button     | String      | Simulate pressing a button on the remote control (3 letter code; codes can be found in Appendix A below)                              |

## Full Example

oppo.things:

```java
// direct IP connection
oppo:player:myoppo "Oppo Blu-ray" [ host="192.168.0.10", model=103, verboseMode=false]

// direct serial connection
oppo:player:myoppo "Oppo Blu-ray" [ serialPort="COM5", model=103, verboseMode=true]

// serial over IP connection
oppo:player:myoppo "Oppo Blu-ray" [ host="192.168.0.9", port=4444, model=103, verboseMode=true]

```

oppo.items:

```java
Switch oppo_power "Power" { channel="oppo:player:myoppo:power" }
Dimmer oppo_volume "Volume [%d %%]" { channel="oppo:player:myoppo:volume" }
Switch oppo_mute "Mute" { channel="oppo:player:myoppo:mute" }
Number oppo_source "Source Input [%s]" { channel="oppo:player:myoppo:source" }
String oppo_play_mode "Play Mode [%s]" { channel="oppo:player:myoppo:play_mode" }
Player oppo_control "Control" { channel="oppo:player:myoppo:control" }
String oppo_time_mode "Time Mode [%s]" { channel="oppo:player:myoppo:time_mode" }
Number:Time oppo_time_display "Time [JS(secondsformat.js):%s]" { channel="oppo:player:myoppo:time_display" }
Number oppo_current_title "Current Title/Track [%s]" { channel="oppo:player:myoppo:current_title" }
Number oppo_total_title "Total Title/Track [%s]" { channel="oppo:player:myoppo:total_title" }
Number oppo_current_chapter "Current Chapter [%s]" { channel="oppo:player:myoppo:current_chapter" }
Number oppo_total_chapter "Total Chapter [%s]" { channel="oppo:player:myoppo:total_chapter" }
String oppo_repeat_mode "Repeat Mode [%s]" { channel="oppo:player:myoppo:repeat_mode" }
String oppo_zoom_mode "Zoom Mode [%s]" { channel="oppo:player:myoppo:zoom_mode" }
String oppo_disc_type "Disc Type [%s]" { channel="oppo:player:myoppo:disc_type" }
String oppo_audio_type "Audio Type [%s]" { channel="oppo:player:myoppo:audio_type" }
String oppo_subtitle_type "Subtitle Type [%s]" { channel="oppo:player:myoppo:subtitle_type" }
String oppo_aspect_ratio "Aspect Ratio [%s]" { channel="oppo:player:myoppo:aspect_ratio" }
String oppo_source_resolution "Source Resolution [%s]" { channel="oppo:player:myoppo:source_resolution" }
String oppo_output_resolution "Output Resolution [%s]" { channel="oppo:player:myoppo:output_resolution" }
String oppo_3d_indicator "3D/2D Indicator [%s]" { channel="oppo:player:myoppo:3d_indicator" }
Number oppo_osd_position "OSD Position [%s]" { channel="oppo:player:myoppo:osd_position" }
Number oppo_sub_shift "Subtitle Shift [%s]" { channel="oppo:player:myoppo:sub_shift" }
String oppo_hdmi_mode "HDMI Mode [%s]" { channel="oppo:player:myoppo:hdmi_mode" }
String oppo_hdr_mode "HDR Mode [%s]" { channel="oppo:player:myoppo:hdr_mode" }
String oppo_remote_button "Remote Button [%s]" { channel="oppo:player:myoppo:remote_button", autoupdate="false" }
```

secondsformat.js:

```javascript
(function(timestamp) {
    var totalSeconds = Date.parse(timestamp) / 1000

    if (isNaN(totalSeconds)) {
        return '-';
    } else {
        hours = Math.floor(totalSeconds / 3600);
        totalSeconds %= 3600;
        minutes = Math.floor(totalSeconds / 60);
        seconds = totalSeconds % 60;
        if ( hours < 10 ) {
            hours = '0' + hours;
        }
        if ( minutes < 10 ) {
            minutes = '0' + minutes;
        }
        if ( seconds < 10 ) {
            seconds = '0' + seconds;
        }
        return hours + ':' + minutes + ':' + seconds;
    }
})(input)
```

oppo.sitemap:

```perl
sitemap oppo label="Oppo Blu-ray" {
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
        Selection item=oppo_remote_button visibility=[oppo_power==ON]
    }
}
```

### Appendix A - 'remote_button' codes:

| Command | Function                                                                    |
|---------|-----------------------------------------------------------------------------|
| POW     | Toggle power ON and OFF                                                     |
| SRC     | Select input source                                                         |
| EJT     | Open/close the disc tray                                                    |
| PON     | Discrete on                                                                 |
| POF     | Discrete off                                                                |
| SYS     | Switch output TV system (PAL/NTSC/MULTI)                                    |
| DIM     | Dim front panel display                                                     |
| PUR     | Pure audio mode (no video)                                                  |
| VUP     | Increase volume                                                             |
| VDN     | Decrease volume                                                             |
| MUT     | Mute/Unmute audio                                                           |
| NU1     | Numeric key 1                                                               |
| NU2     | Numeric key 2                                                               |
| NU3     | Numeric key 3                                                               |
| NU4     | Numeric key 4                                                               |
| NU5     | Numeric key 5                                                               |
| NU6     | Numeric key 6                                                               |
| NU7     | Numeric key 7                                                               |
| NU8     | Numeric key 8                                                               |
| NU9     | Numeric key 9                                                               |
| NU0     | Numeric key 0                                                               |
| CLR     | Clear numeric input                                                         |
| GOT     | Play from a specified location                                              |
| HOM     | Go to Home Menu to select media source                                      |
| PUP     | Show previous page                                                          |
| PDN     | Show next page                                                              |
| OSD     | Show/hide on-screen display                                                 |
| TTL     | Show BD top menu or DVD title menu                                          |
| MNU     | Show BD pop-up menu or DVD menu                                             |
| NUP     | Up Arrow Navigation                                                         |
| NLT     | Left Arrow Navigation                                                       |
| NRT     | Right Arrow Navigation                                                      |
| NDN     | Down Arrow Navigation                                                       |
| SEL     | ENTER Navigation                                                            |
| SET     | Enter the player setup menu                                                 |
| RET     | Return to the previous menu or mode                                         |
| RED     | RED Function varies by content                                              |
| GRN     | GREEN Function varies by content                                            |
| BLU     | BLUE Function varies by content                                             |
| YLW     | YELLOW Function varies by content                                           |
| STP     | Stop playback                                                               |
| PLA     | Start playback                                                              |
| PAU     | Pause playback                                                              |
| PRE     | Skip to previous                                                            |
| REV     | Fast reverse play                                                           |
| FWD     | Fast forward play                                                           |
| NXT     | Skip to next                                                                |
| AUD     | Change audio language or channel                                            |
| SUB     | Change subtitle language                                                    |
| ANG     | Change camera angle                                                         |
| ZOM     | Zoom in/out and adjust aspect ratio                                         |
| SAP     | Turn on/off Secondary Audio Program                                         |
| ATB     | AB Repeat play the selected section                                         |
| RPT     | Repeat play                                                                 |
| PIP     | Show/hide Picture-in-Picture                                                |
| HDM     | Switch output resolution                                                    |
| SUH     | Press and hold the SUBTITLE key. This activates the subtitle shift feature. |
| NFX     | Stop current playback and start the Netflix application                     |
| VDU     | Stop current playback and start the VUDU application                        |
| OPT     | Show/hide the Option menu                                                   |
| M3D     | 3D Show/hide the 2D-to-3D Conversion or 3D adjustment menu                  |
| SEH     | Display the Picture Adjustment menu                                         |
| DRB     | Display the Darbee Adjustment menu                                          |

#### Extra buttons on UDP models:

| Command | Function                                                                            |
|---------|-------------------------------------------------------------------------------------|
| HDR     | Display the HDR selection menu                                                      |
| INH     | Show on-screen detailed information                                                 |
| RLH     | Set resolution to Auto                                                              |
| AVS     | Display the A/V Sync adjustment menu                                                |
| GPA     | Gapless Play. This functions the same as selecting Gapless Play in the Option Menu. |
