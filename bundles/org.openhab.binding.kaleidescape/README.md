# Kaleidescape Binding

This binding now makes it possible to easily integrate almost all of the capabilities of the Kaleidescape control protocol into openHAB.
Beyond just integrating playback transport controls, all meta-data provided via the control protocol is made available for display purposes and to use in rules.
By using rules, it is possible to control other Things such as lighting, projector lens shift, screen masking, etc. based on events that occur during movie playback.
A simulated remote control is available in BasicUI using the Buttongrid sitemap element described below.
Finally, any other command that is supported by the control protocol can be sent to the component through rules.
See [Kaleidescape-System-Control-Protocol-Reference-Manual.pdf](https://support.kaleidescape.com/article/Control-Protocol-Reference-Manual) for a reference of available commands.

## Supported Things

All movie player components including the original K-Player series, M Class Players, Cinema One, Alto, and Strato are supported.
It is important to choose the correct thing type to ensure the available channels are correct for the component being used.

The supported thing types are:

- `player` Any KPlayer, M Class [M300, M500, M700] or Cinema One 1st Gen player
- `cinemaone` Cinema One (2nd Gen)
- `alto`
- `strato` Includes Strato, Strato S, Strato C or Strato V

The binding supports either a TCP/IP connection or direct serial port connection (19200-8-N-1) to the Kaleidescape component.

## Discovery

Auto-discovery is supported for Alto and Strato components if the device can be located on the local network using SDDP.
Manually initiated discovery will locate all legacy Premiere line components if they are on the same IP subnet of the openHAB server.
In the Inbox, select Search For Things and then choose the Kaleidescape Binding to initiate a discovery scan.

## Thing Configuration

The thing has the following configuration parameters:

| Parameter Label                   | Parameter ID           | Description                                                                                                                             | Accepted values                                      |
|-----------------------------------|------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| Address                           | host                   | Host name or IP address of the Kaleidescape component                                                                                   | A host name or IP address                            |
| Port                              | port                   | Communication port of the IP connection                                                                                                 | 10000 (default - should not need to change)          |
| Serial Port                       | serialPort             | Serial port for connecting directly a component                                                                                         | Serial port name (optional)                          |
| Update Period                     | updatePeriod           | Tells the component how often time status updates should be sent (see notes below)                                                      | 0 or 1 are the currently accepted values (default 0) |
| Advanced Volume Control Enabled   | volumeEnabled          | Enable the volume and mute controls in the K iPad & phone apps; when enabled the volume and mute channels described below are active    | Boolean (default false)                              |
| Initial Volume Setting            | initialVolume          | Initial volume level set when the binding starts up                                                                                     | 0 to 75 (default 25)                                 |
| Basic Volume Control Enabled      | volumeBasicEnabled     | Enables stateless volume up/down and mute controls in the K apps; cannot be used when `volumeEnabled` is true (see rules example below) | Boolean (default false)                              |
| Load Highlighted Details          | loadHighlightedDetails | When enabled the binding will automatically load the the metadata channels when the selected item in the UI (Movie or Album) changes    | Boolean (default false)                              |
| Load Album Details                | loadAlbumDetails       | When enabled the binding will automatically load the metadata channels for the currently playing Album                                  | Boolean (default false) N/A for Alto and Strato      |

Some notes:

- The only caveat of note about this binding is the updatePeriod configuration parameter.
- When set to the default of 0, the component only sends running time update messages sporadically (as an example: when the movie chapter changes) while content is playing.
- In this case, the running time channels will also only sporadically update.
- When updatePeriod is set to 1 (values greater than 1 are not yet supported by the control protocol), the component sends running time status update messages every second.
- Be aware that this could cause performance impacts to your openHAB system.

- On Linux, you may get an error stating the serial port cannot be opened when the Kaleidescape binding tries to load.
- You can get around this by adding the `openhab` user to the `dialout` group like this: `usermod -a -G dialout openhab`.
- Also on Linux you may have issues with the USB if using two serial USB devices e.g. Kaleidescape and RFXcom.
- See the [general documentation about serial port configuration](/docs/administration/serial.html) for more on symlinking the USB ports.

## Channels

The following channels are available:

| Channel ID                 | Item Type   | Description                                                                                                                     |
|----------------------------|-------------|---------------------------------------------------------------------------------------------------------------------------------|
| ui#power                   | Switch      | Turn the zone On or Off (system standby)                                                                                        |
| ui#volume                  | Dimmer      | A virtual volume that tracks the volume control from the K app, use as a proxy to adjust a real volume item via follow or rules |
| ui#mute                    | Switch      | A virtual mute switch that tracks the mute status from the K app, use as a proxy to control a real mute item                    |
| ui#control                 | Player      | Control Movie Playback e.g. play/pause/next/previous/ffward/rewind                                                              |
| ui#title_name              | String      | The title of the movie currently playing                                                                                        |
| ui#play_mode               | String      | The current playback mode of the movie                                                                                          |
| ui#play_speed              | String      | The speed of playback scanning                                                                                                  |
| ui#title_num               | Number      | The current movie title number that is playing                                                                                  |
| ui#title_length            | Number:Time | The total running time of the currently playing movie (seconds)                                                                 |
| ui#title_loc               | Number:Time | The running time elapsed of the currently playing movie (seconds)                                                               |
| ui#endtime                 | DateTime    | The date/time when the currently playing movie will end (timestamp)                                                             |
| ui#chapter_num             | Number      | The current chapter number of the movie that is playing                                                                         |
| ui#chapter_length          | Number:Time | The total running time of the current chapter (seconds)                                                                         |
| ui#chapter_loc             | Number:Time | The running time elapsed of the current chapter                                                                                 |
| ui#movie_media_type        | String      | The type of media that is currently playing                                                                                     |
| ui#movie_location          | String      | Identifies the location in the movie, ie: Main content, Intermission, or End Credits                                            |
| ui#aspect_ratio            | String      | Identifies the aspect ratio of the movie                                                                                        |
| ui#video_mode              | String      | Raw output of video mode data from the component, format: 00:00:00                                                              |
| ui#video_mode_composite    | String      | Identifies the video mode currently active on the composite video output                                                        |
| ui#video_mode_component    | String      | Identifies the video mode currently active on the component video output                                                        |
| ui#video_mode_hdmi         | String      | Identifies the video mode currently active on the HDMI video output                                                             |
| ui#video_color             | String      | Provides color information about the current video output (Strato Only)                                                         |
| ui#video_color_eotf        | String      | Identifies the Electro-Optical Transfer Function standard of the current video output (Strato Only)                             |
| ui#content_color           | String      | Provides color information about the currently playing content (Strato Only)                                                    |
| ui#content_color_eotf      | String      | Identifies the Electro-Optical Transfer Function standard of the currently playing content (Strato Only)                        |
| ui#scale_mode              | String      | Identifies whether the image from the player requires scaling                                                                   |
| ui#screen_mask             | String      | Provides aspect ratio and masking information for the current video image                                                       |
| ui#screen_mask2            | String      | Provides masking information based on aspect ratio and overscan area                                                            |
| ui#cinemascape_mask        | String      | When in CinemaScape mode, provides information about the frame aspect ratio                                                     |
| ui#cinemascape_mode        | String      | Identifies the CinemaScape mode currently active                                                                                |
| ui#ui_state                | String      | Provides information about which screen is visible in the Kaleidescape user interface                                           |
| ui#child_mode_state        | String      | Indicates if the onscreen display is displaying the child user interface                                                        |
| ui#readiness_state         | String      | Indicates the system's current idle mode (Not available on Premiere system players)                                             |
| ui#highlighted_selection   | String      | Specifies the handle of the movie or album currently selected on the user interface                                             |
| ui#user_defined_event      | String      | Will contain custom event messages generated by scripts, sent from another component, or system events                          |
| ui#user_input              | String      | Indicates if the user is being prompted for input, what type of input, and any currently entered characters                     |
| ui#user_input_prompt       | String      | Indicates user input prompt info and properties currently shown on screen                                                       |
| ui#sendcmd                 | String      | Sends a raw command to the Kaleidescape player (WriteOnly)                                                                      |
| -- music channels (not available on Alto and Strato) --                                                                                                                    |
| music#control              | Player      | Control Music Playback e.g. play/pause/next/previous/ffward/rewind                                                              |
| music#repeat               | Switch      | Controls repeat playback for music                                                                                              |
| music#random               | Switch      | Controls random playback for music                                                                                              |
| music#track                | String      | The name of the currently playing track                                                                                         |
| music#artist               | String      | The name of the currently playing artist                                                                                        |
| music#album                | String      | The name of the currently playing album                                                                                         |
| music#title                | String      | The raw output from the MUSIC_TITLE api response for use in rules that require track, artist and album changes in one update    |
| music#play_mode            | String      | The current playback mode of the music                                                                                          |
| music#play_speed           | String      | The speed of playback scanning                                                                                                  |
| music#track_length         | Number:Time | The total running time of the current playing track (seconds)                                                                   |
| music#track_position       | Number:Time | The running time elapsed of the current playing track (seconds)                                                                 |
| music#track_progress       | Number      | The percentage complete of the current playing track                                                                            |
| music#track_handle         | String      | The handle of the currently playing track                                                                                       |
| music#album_handle         | String      | The handle of the currently playing album                                                                                       |
| music#nowplay_handle       | String      | The handle of the current now playing list                                                                                      |
| -- metadata display channels (music related channels not available on Alto and Strato) --                                                                                  |
| detail#type                | String      | Indicates if the currently selected item is a Movie or Album                                                                    |
| detail#title               | String      | The title of the selected movie                                                                                                 |
| detail#album_title         | String      | The title of the selected album                                                                                                 |
| detail#cover_art           | Image       | Cover art image of the currently selected item                                                                                  |
| detail#cover_url           | String      | The url of the cover art                                                                                                        |
| detail#hires_cover_url     | String      | The url of the high resolution cover art                                                                                        |
| detail#rating              | String      | The MPAA rating of the selected movie                                                                                           |
| detail#year                | String      | The release year of the selected item                                                                                           |
| detail#running_time        | Number:Time | The total running time of the selected item (seconds)                                                                           |
| detail#actors              | String      | A list of actors appearing in the selected movie                                                                                |
| detail#artist              | String      | The artist of the selected album                                                                                                |
| detail#directors           | String      | A list of directors of the selected movie                                                                                       |
| detail#genres              | String      | A list of genres of the selected item                                                                                           |
| detail#rating_reason       | String      | An explaination of why the selected movie received its rating                                                                   |
| detail#synopsis            | String      | A synopsis of the selected movie                                                                                                |
| detail#review              | String      | A review of the selected album                                                                                                  |
| detail#color_description   | String      | Indicates if the selected movie is in Color, Black and White, etc.                                                              |
| detail#country             | String      | The country that the selected movie originates from                                                                             |
| detail#aspect_ratio        | String      | The aspect ratio of the selected movie                                                                                          |
| detail#disc_location       | String      | Indicates where the disc for the selected item is currently residing in the system (ie Vault, Tray, etc.)                       |

## Full Example

kaleidescape.things:

```java
kaleidescape:strato:myzone1 "Strato Theater Rm" [ host="192.168.1.10", updatePeriod=0, loadHighlightedDetails=true ]
kaleidescape:player:myzone2 "M500 Living Rm" [ host="192.168.1.11", updatePeriod=0, loadHighlightedDetails=true, loadAlbumDetails=true ]
kaleidescape:cinemaone:myzone3 "My Cinema One" [ host="192.168.1.12", updatePeriod=0, loadHighlightedDetails=true, loadAlbumDetails=true ]
```

kaleidescape.items:

```java
// Virtual switch to send a command, see sitemap and rules below
Switch z1_GoMovieCovers   "Go to Movie Covers"

// Movie Channels
Switch z1_Ui_Power "Power" { channel="kaleidescape:player:myzone1:ui#power" }
Dimmer z1_Ui_Volume "Volume" { channel="kaleidescape:player:myzone1:ui#volume" }
Switch z1_Ui_Mute "Mute" { channel="kaleidescape:player:myzone1:ui#mute" }
Player z1_Ui_Control "Control" { channel="kaleidescape:player:myzone1:ui#control" }
String z1_Ui_TitleName "Movie Title: [%s]" { channel="kaleidescape:player:myzone1:ui#title_name" }
String z1_Ui_PlayMode "Play Mode: [%s]" { channel="kaleidescape:player:myzone1:ui#play_mode" }
String z1_Ui_PlaySpeed "Play Speed: [%s]" { channel="kaleidescape:player:myzone1:ui#play_speed" }
Number z1_Ui_TitleNum "Title Number: [%s]" { channel="kaleidescape:player:myzone1:ui#title_num" }
Number:Time z1_Ui_TitleLength "Title Length: [JS(ksecondsformat.js):%s]" { channel="kaleidescape:player:myzone1:ui#title_length" }
Number:Time z1_Ui_TitleLoc "Title Location: [JS(ksecondsformat.js):%s]" { channel="kaleidescape:player:myzone1:ui#title_loc" }
DateTime z1_Ui_TitleEndTime "Title End Time: [%s]" { channel="kaleidescape:player:myzone1:ui#endtime" }
Number z1_Ui_ChapterNum "Chapter Number: [%s]" { channel="kaleidescape:player:myzone1:ui#chapter_num" }
Number:Time z1_Ui_ChapterLength "Chapter Length: [JS(ksecondsformat.js):%s]" { channel="kaleidescape:player:myzone1:ui#chapter_length" }
Number:Time z1_Ui_ChapterLoc "Chapter Location: [JS(ksecondsformat.js):%s]" { channel="kaleidescape:player:myzone1:ui#chapter_loc" }
String z1_Ui_MovieMediaType "Media Type: [%s]" { channel="kaleidescape:player:myzone1:ui#movie_media_type" }
String z1_Ui_MovieLocation "Movie Location: [%s]" { channel="kaleidescape:player:myzone1:ui#movie_location" }
String z1_Ui_AspectRatio "Aspect Ratio: [%s]" { channel="kaleidescape:player:myzone1:ui#aspect_ratio" }
String z1_Ui_VideoMode "Video Mode (raw): [%s]" { channel="kaleidescape:player:myzone1:ui#video_mode" }
String z1_Ui_VideoModeComposite "Video Mode (Composite): [%s]" { channel="kaleidescape:player:myzone1:ui#video_mode_composite" }
String z1_Ui_VideoModeComponent "Video Mode (Component): [%s]" { channel="kaleidescape:player:myzone1:ui#video_mode_component" }
String z1_Ui_VideoModeHdmi "Video Mode (HDMI): [%s]" { channel="kaleidescape:player:myzone1:ui#video_mode_hdmi" }
// Video Color and Content Color only available on the Strato
String z1_Ui_VideoColor "Video Color: [%s]" { channel="kaleidescape:player:myzone1:ui#video_color" }
String z1_Ui_VideoColorEotf "Video Color EOTF: [%s]" { channel="kaleidescape:player:myzone1:ui#video_color_eotf" }
String z1_Ui_ContentColor "Content Color: [%s]" { channel="kaleidescape:player:myzone1:ui#content_color" }
String z1_Ui_ContentColorEotf "Content Color EOTF: [%s]" { channel="kaleidescape:player:myzone1:ui#content_color_eotf" }
String z1_Ui_ScaleMode "Scale Mode: [%s]" { channel="kaleidescape:player:myzone1:ui#scale_mode" }
String z1_Ui_ScreenMask "Screen Mask: [%s]" { channel="kaleidescape:player:myzone1:ui#screen_mask" }
String z1_Ui_ScreenMask2 "Screen Mask 2: [%s]" { channel="kaleidescape:player:myzone1:ui#screen_mask2" }
String z1_Ui_CinemascapeMask "CinemaScape Mask: [%s]" { channel="kaleidescape:player:myzone1:ui#cinemascape_mask" }
String z1_Ui_CinemascapeMode "CinemaScape Mode: [%s]" { channel="kaleidescape:player:myzone1:ui#cinemascape_mode" }
String z1_Ui_UiState "UI State: [%s]" { channel="kaleidescape:player:myzone1:ui#ui_state" }
String z1_Ui_ChildModeState "Child Mode State: [%s]" { channel="kaleidescape:player:myzone1:ui#child_mode_state" }
String z1_Ui_ReadinessState "Readiness State: [%s]" { channel="kaleidescape:player:myzone1:ui#readiness_state" }
String z1_Ui_HighlightedSelection "Highlighted Selection: [%s]" { channel="kaleidescape:player:myzone1:ui#highlighted_selection" }
String z1_Ui_UserDefinedEvent "User Defined Event: [%s]" { channel="kaleidescape:player:myzone1:ui#user_defined_event" }
String z1_Ui_UserInput "User Input: [%s]" { channel="kaleidescape:player:myzone1:ui#user_input" }
String z1_Ui_UserInputPrompt "User Input Prompt[%s]" { channel="kaleidescape:player:myzone1:ui#user_input_prompt" }
String z1_Ui_Sendcmd "Send Command" { channel="kaleidescape:player:myzone1:ui#sendcmd" }

// Music Channels (not available on Alto or Strato)
Player z1_Music_Control "Music Control" { channel="kaleidescape:player:myzone1:music#control" }
Switch z1_Music_Repeat "Repeat" { channel="kaleidescape:player:myzone1:music#repeat" }
Switch z1_Music_Random "Random" { channel="kaleidescape:player:myzone1:music#random" }
String z1_Music_Track "Track: [%s]" { channel="kaleidescape:player:myzone1:music#track" }
String z1_Music_Artist "Artist: [%s]" { channel="kaleidescape:player:myzone1:music#artist" }
String z1_Music_Album "Album: [%s]" { channel="kaleidescape:player:myzone1:music#album" }
String z1_Music_PlayMode "Play Mode: [%s]" { channel="kaleidescape:player:myzone1:music#play_mode" }
String z1_Music_PlaySpeed "Play Speed: [%s]" { channel="kaleidescape:player:myzone1:music#play_speed" }
Number:Time z1_Music_TrackLength "Track Length: [JS(ksecondsformat.js):%s]" { channel="kaleidescape:player:myzone1:music#track_length" }
Number:Time z1_Music_TrackPosition "Track Position: [JS(ksecondsformat.js):%s]" { channel="kaleidescape:player:myzone1:music#track_position" }
Number z1_Music_TrackProgress "Track Progress: [%s %%]" { channel="kaleidescape:player:myzone1:music#track_progress" }
String z1_Music_Title "Music Title Raw: [%s]" { channel="kaleidescape:player:myzone1:music#title" }
String z1_Music_TrackHandle "Track Handle: [%s]" { channel="kaleidescape:player:myzone1:music#track_handle" }
String z1_Music_AlbumHandle "Album Handle: [%s]" { channel="kaleidescape:player:myzone1:music#album_handle" }
String z1_Music_NowplayHandle "Now Playing Handle: [%s]" { channel="kaleidescape:player:myzone1:music#nowplay_handle" }

// Metatdata Display Channels (Album Title, Artist & Review are not available on Alto or Strato)
String z1_Detail_Type "Metadata type: [%s]" { channel="kaleidescape:player:myzone1:detail#type" }
String z1_Detail_Title "Title: [%s]" { channel="kaleidescape:player:myzone1:detail#title" }
String z1_Detail_AlbumTitle "Album: [%s]" { channel="kaleidescape:player:myzone1:detail#album_title" }
Image z1_Detail_CoverArt { channel="kaleidescape:player:myzone1:detail#cover_art" }
String z1_Detail_CoverUrl "[%s]" { channel="kaleidescape:player:myzone1:detail#cover_url" }
String z1_Detail_HiresCoverUrl "[%s]" { channel="kaleidescape:player:myzone1:detail#hires_cover_url" }
String z1_Detail_Rating "Rating: [%s]" { channel="kaleidescape:player:myzone1:detail#rating" }
String z1_Detail_Year "Year: [%s]" { channel="kaleidescape:player:myzone1:detail#year" }
Number:Time z1_Detail_RunningTime "Running Time: [JS(ksecondsformat.js):%s]" { channel="kaleidescape:player:myzone1:detail#running_time" }
String z1_Detail_Actors "Actors: [%s]" { channel="kaleidescape:player:myzone1:detail#actors" }
String z1_Detail_Directors "Directors: [%s]" { channel="kaleidescape:player:myzone1:detail#directors" }
String z1_Detail_Artist "Artist: [%s]" { channel="kaleidescape:player:myzone1:detail#artist" }
String z1_Detail_Genres "Genres: [%s]" { channel="kaleidescape:player:myzone1:detail#genres" }
String z1_Detail_RatingReason "Rating Reason: [%s]" { channel="kaleidescape:player:myzone1:detail#rating_reason" }
String z1_Detail_Synopsis "Synopsis: [%s]" { channel="kaleidescape:player:myzone1:detail#synopsis" }
String z1_Detail_Review "Review: [%s]" { channel="kaleidescape:player:myzone1:detail#review" }
String z1_Detail_ColorDescription "Color Description: [%s]" { channel="kaleidescape:player:myzone1:detail#color_description" }
String z1_Detail_Country "Country: [%s]" { channel="kaleidescape:player:myzone1:detail#country" }
String z1_Detail_AspectRatio "Aspect Ratio: [%s]" { channel="kaleidescape:player:myzone1:detail#aspect_ratio" }
String z1_Detail_DiscLocation "Disc Location: [%s]" { channel="kaleidescape:player:myzone1:detail#disc_location" }
String z1_MovieSearch "Movie Search"
```

ksecondsformat.js:

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

kaleidescape.sitemap:

```perl
sitemap kaleidescape label="Kaleidescape" {
    Frame label="Zone 1" {
        Image item=z1_Detail_CoverArt
        Text item=z1_Detail_Title visibility=[z1_Detail_Type=="movie"] icon="video"
        Text item=z1_Detail_Artist visibility=[z1_Detail_Type=="album"] icon="microphone"
        Text item=z1_Detail_AlbumTitle visibility=[z1_Detail_Type=="album"] icon="soundvolume-0"
        Text item=z1_Detail_Rating visibility=[z1_Detail_Type=="movie"] icon="none"
        Text item=z1_Detail_Year visibility=[z1_Detail_Type=="movie", z1_Detail_Type=="album"] icon="none"
        Text item=z1_Detail_RunningTime visibility=[z1_Detail_Type=="movie", z1_Detail_Type=="album"] icon="time"
        Text item=z1_Detail_Actors visibility=[z1_Detail_Type=="movie"] icon="none"
        Text item=z1_Detail_Directors visibility=[z1_Detail_Type=="movie"] icon="none"
        Text item=z1_Detail_Genres visibility=[z1_Detail_Type=="movie", z1_Detail_Type=="album"] icon="none"
        Text item=z1_Detail_RatingReason visibility=[z1_Detail_Type=="movie"] icon="none"
        Text item=z1_Detail_Synopsis visibility=[z1_Detail_Type=="movie"] icon="none"
        Text item=z1_Detail_Review visibility=[z1_Detail_Type=="album"] icon="none"
        Text item=z1_Detail_ColorDescription visibility=[z1_Detail_Type=="movie"] icon="none"
        Text item=z1_Detail_Country visibility=[z1_Detail_Type=="movie"] icon="none"
        Text item=z1_Detail_AspectRatio visibility=[z1_Detail_Type=="movie"] icon="none"
        Text item=z1_Detail_DiscLocation visibility=[z1_Detail_Type=="movie", z1_Detail_Type=="album"] icon="player"
        Input item=z1_MovieSearch label="Movie Search" staticIcon=zoom inputHint="text"

        Text label="Now Playing - Movie" icon="screen" {
            Switch item=z1_Ui_Power
            Slider item=z1_Ui_Volume
            Switch item=z1_Ui_Mute
            Default item=z1_Ui_Control
            Switch item=z1_GoMovieCovers mappings=[ON="Movie Covers"]
            Text item=z1_Ui_TitleName icon="video"
            Text item=z1_Ui_PlayMode icon="player"
            Text item=z1_Ui_PlaySpeed icon="player"
            Text item=z1_Ui_TitleNum icon="video"
            Text item=z1_Ui_TitleLength icon="time"
            Text item=z1_Ui_TitleLoc icon="time"
            Text item=z1_Ui_TitleEndTime icon="time"
            Text item=z1_Ui_MovieMediaType icon="colorwheel"
            Text item=z1_Ui_ChapterNum icon="video"
            Text item=z1_Ui_ChapterLength icon="time"
            Text item=z1_Ui_ChapterLoc icon="time"
            Text item=z1_Ui_MovieLocation icon="video"
            Text item=z1_Ui_AspectRatio icon="cinemascreen"
            Text item=z1_Ui_VideoMode icon="screen"
            Text item=z1_Ui_VideoModeComposite icon="screen"
            Text item=z1_Ui_VideoModeComponent icon="screen"
            Text item=z1_Ui_VideoModeHdmi icon="screen"
            Text item=z1_Ui_VideoColor icon="screen"
            Text item=z1_Ui_VideoColorEotf icon="screen"
            Text item=z1_Ui_ContentColor icon="screen"
            Text item=z1_Ui_ContentColorEotf icon="screen"
            Text item=z1_Ui_ScaleMode icon="screen"
            Text item=z1_Ui_ScreenMask icon="screen"
            Text item=z1_Ui_ScreenMask2 icon="screen"
            Text item=z1_Ui_CinemascapeMask icon="screen"
            Text item=z1_Ui_CinemascapeMode icon="screen"
            Text item=z1_Ui_UiState icon="player"
            Text item=z1_Ui_ChildModeState icon="player"
            Text item=z1_Ui_ReadinessState icon="switch"
            Text item=z1_Ui_HighlightedSelection icon="zoom"
            Text item=z1_Ui_UserDefinedEvent icon="zoom"
            Text item=z1_Ui_UserInput icon="zoom"
            Text item=z1_Ui_UserInputPrompt icon="zoom"
        }

        Text label="Now Playing - Music" icon="soundvolume-0" {
            Switch item=z1_Ui_Power
            Slider item=z1_Ui_Volume
            Switch item=z1_Ui_Mute
            Default item=z1_Music_Control
            Switch item=z1_Music_Repeat
            Switch item=z1_Music_Random
            Text item=z1_Music_Track icon="soundvolume-0"
            Text item=z1_Music_Artist icon="microphone"
            Text item=z1_Music_Album icon="soundvolume-0"
            Text item=z1_Music_PlayMode icon="player"
            Text item=z1_Music_PlaySpeed icon="player"
            Text item=z1_Music_TrackLength icon="time"
            Text item=z1_Music_TrackPosition icon="time"
            Text item=z1_Music_TrackProgress icon="time"
            Text item=z1_Music_Title icon="zoom"
            Text item=z1_Music_TrackHandle icon="zoom"
            Text item=z1_Music_AlbumHandle icon="zoom"
            Text item=z1_Music_NowplayHandle icon="zoom"
        }
        Buttongrid label="Kaleidescape Remote" staticIcon=material:tv_remote item=z1_Ui_Sendcmd buttons=[1:1:DETAILS="Info", 1:2:STOP="Stop"=f7:stop, 1:3:PAUSE="Pause"=f7:pause, 2:1:PREVIOUS="Previous"=f7:backward_end_alt, 2:2:PLAY="Play"=f7:play, 2:3:NEXT="Next"=f7:forward_end_alt, 3:1:SCAN_REVERSE="Reverse"=f7:backward, 3:2:KALEIDESCAPE_MENU_TOGGLE="Menu", 3:3:SCAN_FORWARD="Forward"=f7:forward, 4:1:GO_MOVIES="Movies"=f7:film, 4:3:GO_MUSIC="Music"=f7:music_note_2, 4:2:UP="Up"=f7:arrowtriangle_up, 6:2:DOWN="Down"=f7:arrowtriangle_down, 5:1:LEFT="Left"=f7:arrowtriangle_left, 5:3:RIGHT="Right"=f7:arrowtriangle_right, 5:2:SELECT="OK", 7:1:DISC_MENU="Disc Menu", 7:2:INTERMISSION_TOGGLE="Intermission", 7:3:PAGE_UP="Page Up", 8:1:SHUFFLE_COVER_ART="Shuffle", 8:2:GO_NOW_PLAYING="Now Playing", 8:3:PAGE_DOWN="Page Down", 9:1:"KEYBOARD_CHARACTER:1"="1", 9:2:"KEYBOARD_CHARACTER:2"="2", 9:3:"KEYBOARD_CHARACTER:3"="3", 10:1:"KEYBOARD_CHARACTER:4"="4", 10:2:"KEYBOARD_CHARACTER:5"="5", 10:3:"KEYBOARD_CHARACTER:6"="6", 11:1:"KEYBOARD_CHARACTER:7"="7", 11:2:"KEYBOARD_CHARACTER:8"="8", 11:3:"KEYBOARD_CHARACTER:9"="9", 12:1:CANCEL="Cancel", 12:2:"KEYBOARD_CHARACTER:0"="0", 12:3:SELECT="Enter", 13:1:RED="Red", 13:2:GREEN="Green", 13:3:BLUE="Blue", 14:2:YELLOW="Yellow"]
    }
}
```

kaleidescape.rules:

```java
var int lightPercent

// send command to go to movie covers when button pressed
rule "Go to Movie Covers"
when
    Item z1_GoMovieCovers received command
then
    z1_Ui_Sendcmd.sendCommand("GO_MOVIE_COVERS")
end

// send command to play a script
rule "Play Script - Great Vistas"
when
    Item z1_PlayScript received command
then
    z1_Ui_Sendcmd.sendCommand("PLAY_SCRIPT:Great Vistas")
end

// handle volume events from K apps or IR remote
// the events can be used to send a command to another item that controls volume
rule "Handle volume events"
when
    Item z1_Ui_UserDefinedEvent received update
then
    var volEvt = newState.toString()

    // When `volumeBasicEnabled` is true for the thing, VOLUME_UP, VOLUME_DOWN and TOGGLE_MUTE are received from the iPad and phone apps
    // VOLUME_UP_PRESS/RELEASE, VOLUME_DOWN_PRESS/RELEASE, TOGGLE_MUTE events will always be received from the IR remote
    // *RELEASE events are not used in this example

    if (volEvt == "VOLUME_UP" || volEvt == "VOLUME_UP_PRESS") {
        logInfo("k rules", "Volumne Up received")
    }

    if (volEvt == "VOLUME_DOWN" || volEvt == "VOLUME_DOWN_PRESS") {
        logInfo("k rules", "Volumne Down received")
    }

    if (volEvt == "TOGGLE_MUTE") {
        logInfo("k rules", "Mute Toggle received")
    }
end

// handle a control system command sent from a kaleidescape script
// the command string is specified in a "Send command to control system" script step
rule "Handle script commands"
when
    Item z1_Ui_UserDefinedEvent received update
then
    if (z1_Ui_UserDefinedEvent.state.toString == "DO_THE_NEEDFUL") {
        logInfo("k rules", "handing the NEEDFUL script command...")
    }
end

rule "Bring up Lights when movie is over"
when
    Item z1_Ui_MovieLocation changed from "Main content" to "End Credits"
then
    // fade the lights up slowly while the credits are rolling
    lightPercent = 0
    while (lightPercent < 100) {
        lightPercent = lightPercent + 5
        logInfo("k rules", "lights at " + lightPercent.toString + " percent")
        // myLightItem.sendCommand(lightPercent)
        Thread::sleep(5000)
    }
end

rule "Bring up Lights at 20 percent during intermission"
when
    Item z1_Ui_MovieLocation changed from "Main content" to "Intermission"
then
    // myLightItem.sendCommand(20)
    logInfo("k rules", "intermission started")
end

rule "Turn lights back off when intermission over"
when
    Item z1_Ui_MovieLocation changed from "Intermission" to "Main content"
then
    // myLightItem.sendCommand(OFF)
    logInfo("k rules", "intermission over")
end

rule "Movie Search"
when
    Item z1_MovieSearch received update
then
    if (newState != NULL && newState.toString.length > 0) {
        z1_Ui_Sendcmd.sendCommand("GO_MOVIE_LIST")
        Thread::sleep(1000)
        z1_Ui_Sendcmd.sendCommand("FILTER_LIST")
        Thread::sleep(300)

        var i = 0
        var srch = newState.toString.toUpperCase
        logInfo("k rules","Searching for: " + srch)

        while (i < (srch.length)) {
            z1_Ui_Sendcmd.sendCommand("KEYBOARD_CHARACTER:" + srch.charAt(i).toString)
            Thread::sleep(100)
            i++
        }
    }
end

// The following are no longer required since the thing configuration will enable automatic loading of metatdata.
// However the examples are still valid for advanced use cases where retrieving metadata from an arbitrary content handle is desired.

rule "Load selected item Metadata"
when
    Item z1_Ui_HighlightedSelection changed
then
    z1_Ui_Sendcmd.sendCommand("GET_CONTENT_DETAILS:" + z1_Ui_HighlightedSelection.state.toString + ":")
end

rule "Load Metadata for currently playing album"
when
    Item z1_Music_AlbumHandle changed
then
    z1_Ui_Sendcmd.sendCommand("GET_CONTENT_DETAILS:" + z1_Music_AlbumHandle.state.toString + ":")
end
```
