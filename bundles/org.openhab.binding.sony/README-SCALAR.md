# SCALAR

SCALAR (also known as REST-API) is Sony's next generation API for discovery and control of the device.
This service has been implemented in most of the Sony products and has the same (and more) capabilities of all the other services combined.
If your device supports a Scalar thing, you should probably use it versus any of the other services.
The only downside is that it's a bit 'heavier' (depending on the device - will likely issue more calls) and is a bit more complicated to use (many, many channels are produced).

This service dynamically generates the channels based on the device.

## Application status

Sony has 'broken' the API that determines which application is currently running regardless if you use DIAL or Scalar services.
The API that determines whether an application is currently running ALWAYS returns 'stopped' (regardless if it's running or not).
Because of that - you cannnot rely on the application status and there is NO CURRENT WAY to determine if any application is running.

Both DIAL/Scalar will continue to check the status in case Sony fixes this in some later date - but as of this writing - there is NO WAY to determine application status.

## Authentication

Scalar can be authenticated via normal keys or preshared keys as documented in the main [README](README.md).

## Thing Configuration

The configuration for the Scalar thing (in addition to the common parameters)

| Name            | Required | Default | Description                                                                   |
| --------------- | -------- | ------- | ----------------------------------------------------------------------------- |
| accessCode      | No       | RQST    | The access code for the device                                                |
| irccUrl         | No (1)   | None    | The URL/Hostname for the IRCC service                                         |
| commandsMapFile | No (2)   | None    | The commands map file that translates words to the underlying protocol string |
| modelName       | No (3)   | None    | The model name of the device                                                  |

1. See IP Address Configuration above
2. See transformations below
3. Only specify this if the model name is not automatically detected

## Transformations

These services use a commands map file that will convert a word (specified in the command channel) to the underlying command to send to the device.
This file will appear in your openHAB `conf/transformation` directory.

When the device is ONLINE, the commandsMapFile configuration property has been set and the resulting file doesn't exist, the binding will write out the commands supported by the device to that file.
If discovery of the commands is not possible, a default set of commands will be written out which may or may not be correct for the device.
I highly recommend having the binding do this rather than creating the file from scratch.

When the device is auto discovered, the commandsMapFile will be set to "scalar-{thingid}.map" (example: "scalar-ace2a0229f7a.map").
You may want to change that in the things configuration, post-discovery, to something more reasonable.

The format of the file will be: `{word}={cmd}`

1. The word can be anything (in any language) and is the value send to the command channel.
2. The cmd is a URL Encoded value that will be sent to the device.

An example from a Sony BluRay player (that was discovered by the binding):

```
...
Stop=AAAAAwAAHFoAAAAYAw%3D%3D
SubTitle=AAAAAwAAHFoAAABjAw%3D%3D
TopMenu=AAAAAwAAHFoAAAAsAw%3D%3D
Up=AAAAAwAAHFoAAAA5Aw%3D%3D
Yellow=AAAAAwAAHFoAAABpAw%3D%3D
...
```

Please note that you can recreate the .map file by simply deleting it from `conf/transformation` and restarting openHAB.

## HDMI/CEC/ARC (AVRs, SoundBars)

One of the issues with HDMI/CEC/ARC is that you can only increment or decrement the sound level by 1 if you are using HDMI/CEC/ARC to connect a soundbar or AVR.
If you set a volume to a specific level, the sound will only ever go up or down by a single value due to HDMI/CEC protocols.
To overcome this, the addon will (if configured - see below) issue a series of increment/decrement commands to reach a target level.
Example: if the processing is configured (see below), your current sound level is 10 and you set the sound level to 15 - the system will issue 5 increment commands to bring the soundbar/AVR up to 15.

### Configuration options

Edit the `conf/services/runtime.cfg` and add any (or all) of the following values:
1. `sony.things:audio-enablecec`
2. `sony.things:audio-forcecec`
3. `sony.things:audio-cecdelay`

#### audio-enablecec

This enables the HDMI/CEC processing.

Set this value to `true` to allow the system to convert a set volume into a series of increments if HDMI/CEC is detected (see `audio-forcecec`).  

The default value is `false`

#### audio-forcecec

This set's whether to force HDMI/CEC processing (`true`) or attempt to detect whether HDMI/CEC processing should be used (`false`).

You will want to set this value to `true` in the following cases:

1.  A soundbar/AVR is always used (in other words, you will NEVER use the TV speakers).
This will turn of auto-detection and issue less commands since it assumes HDMI/CEC processing.

2.  The soundbar/AVR is not correctly detected on the HDMI/CEC.
If you set the volume and the volume only goes up or down a single increment, then HDMI/CEC detection didn't work and this overrides that detection.

The default value is `false`

#### audio-cecdelay

This is the delay (in ms) between increment/decrement requests.
Depending on your device, you may need to modify the delay to either improve responsiveness (by setting a lower delay if your device handles it properly) or fix missed messages (by setting a higher delay if your device is slower to respond).

The default value is `250` (250ms);

#### WARNING - Sony devices (soundbars, AVRs)

Do ***NOT*** enable this if your soundbar/AVR is a sony device.  
Sony has special processing when the HDMI/CEC device is a Sony device and this logic will simply not work.
You will need to connect to the device (using this binding) directly to change the volume **or** use the IRCC channel to increment/decrement the volume.

Any setting of the volume or incrementing/decrementing the volume will set the TV speaker volume (which is not active) and will ***NOT*** be passed to the soundbar/AVR.

#### Example

```
sony.things:audio-enablecec=true
sony.things:audio-forcecec=true
sony.things:audio-cecdelay=100
```

This will enable special HDMI/CEC processing, force the ARC processing (ie disabling detection) and provide a 100ms delay between commands.

## Channels

The scalar service will dynamically generate the channels supported by your device.
The following table(s) will provide all the various channels supported by this addon - not all channels will be applicable for your device.
Example: Bravia TVs will support input channels but no terminal channels.
Likewise, even if you device supports a specific channel doesn't necessarily mean that channel will receive any data from the device (example: pl_durationsec may be supported by a TV but will ONLY be active if you are playing specific media and if that media's metadata is in a format the TV supports)

### Channel ID Format

Scalar uses the following format for channels:

`{serviceId}#{channelName}[-{sonyid}]`

1. `serviceId` is the service identifier (see sections below)
2. `channelName` is the name of the channel (listed in each section)
3. `sonyid` is an OPTIONAL additional Sony identifier if there are multiple channel names.
The ID can itself have multiple dashes to further denote uniqueness.

Example:

The current from the system service would have a channel id of `system#currenttime`

The speaker volume from the audo service would have a channel id of `audio#volume-speaker` (the headphone volume would be `audio#volume-headphone`).

A TV with multiple HDMI ports may have a status channel like
`avContent#in_status-extInput-hdmi1` or `avContent#in_status-extInput-hdmi2` .

### General Settings

A number of channels below will be marked as a General Setting (Item Type: GeneralSetting).
This was Sony's way of describing a setting in a general way.
A general setting will have one or more channels decribing the setting and may be a combination of different types of channels (dimmers, switches, numbers).

Example: a general setting my be a "Custom Equalizer" and would actually have dimmer channels describing each band (treble, base, etc)

The following channels may be described (one or more and in any combination):

| Channel Type ID | Read/Write | Item Type | Description                                                                |
| --------------- | ---------- | --------- | -------------------------------------------------------------------------- |
| {name}-{id}     | R          | Number    | A general setting representing a number (may have min/max assigned)        |
| {name}-{id}     | R          | Switch    | A general setting representing an on/off                                   |
| {name}-{id}     | R          | String    | A general setting representing a string (usually an enumeration of values) |
| {name}-{id}     | R          | Dimmer    | A general setting representing a dimmer (may have min/max/step assigned)   |

The {name} represents the name of the channel name (described above) and would have `-{id}` appended to it to represent the unique setting id given by the setting.

Example: on an AVR - a subwoofer would have a general setting for the subwoofer level.
This would be create a channel called `audio#speakersetting-subwooferlevel` (where `audio#speakersetting` is the name of the channel and the general setting id is `-subwooferlevel`).

### Application Control Channels (service id of "appControl")

The following channels are for the application control service.
The application control service provides management of applications that are installed on the device.

| Channel Type ID | Read/Write | Item Type | Description                         |
| --------------- | ---------- | --------- | ----------------------------------- |
| appstatus-{id}  | R (1)      | String    | The application status (start/stop) |
| apptitle-{id}   | R          | String    | The application title               |
| appicon-{id}    | R          | Image     | The application icon                |
| appdata-{id}    | R          | String    | The application data                |

1.  Please note that at the time of this writing, Sony broke the application status and this channel will not correctly reflect what is running

The {id} is the unique identifier of the application and is simply the application stripped of any illegal (for a channel UID) characters and made lowercase.
Example: `Amazon Video` would have an id of `amazonvideo`.
So you'd have `appstatus-amazonvideo`, `apptitle-amazonvideo`, etc.

### Audio Channels (service ID of "audio")

The following channels are for the audio service. The audio service provides management of audio functions on the device.

| Channel Type ID | Read/Write | Item Type      | Description                   |
| --------------- | ---------- | -------------- | ----------------------------- |
| volume-{id}     | R (1)      | Dimmer         | The volume from 0% to 100%    |
| mute-{id}       | R          | Switch         | Whether the volume is muted   |
| soundsetting    | RW         | GeneralSetting | The setting for the sound     |
| speakersetting  | RW         | GeneralSetting | The setting for the speaker   |
| customequalizer | RW         | GeneralSetting | The setting for the equalizer |

1.  Volume will be scaled to the device's range

The {id} is the unique id of the volume from the device.
For TVs, there generally be a 'speaker' or 'main' and a 'headphone'.
For a multi-zone AVR, there will be a volume/mute for each zone (extoutput-zone1, extoutput-zone2, etc).

Example: sending `.2` to the volume-headphone will put the headphone's volume to 20% of the device capability.

### Audio/Video Content Channels (service ID of "avContent")

The following channels are for the audio/visual content service.
The AV content service allows management of all audio/visual functions on the device including what is currently playing.

#### General information

The following channels are general information/settings for the device.

| Channel Type ID  | Read/Write | Item Type      | Description                     |
| ---------------- | ---------- | -------------- | ------------------------------- |
| schemes          | R (1)      | String         | Comma separated list of schemes |
| sources          | R (2)      | String         | Comma separated list of source  |
| bluetoothsetting | RW         | GeneralSetting | The bluetooth settings          |
| playbackmode     | RW         | GeneralSetting | The playback modes              |

1. Scheme are the high level schemes supported by the device.
Examples would be `radio`, `tv`, `dlna` and would also include schems for the input and outputs like `extInput` and `extOutput`.
2. Sources would contain the subcategories of each scheme in the format of `{scheme}:{source}`.
Examples would be `radio:fm`, `tv:analog`, `tv:atsct`, `extInput:hdmi` and `extInput:component`

You can use the sources then to query information about each source via the `avContent:cn_parenturi` (see the Using Content section below).

#### Parental Ratings 

The following channels reflect the parental ratings of the content being played.

| Channel Type ID              | Read/Write | Item Type | Description                        |
| ---------------------------- | ---------- | --------- | ---------------------------------- |
| pr_ratingtypeage             | R          | Number    | The minimum age of the rating type |
| pr_ratingtypesony            | R          | String    | Sony's rating type                 |
| pr_ratingcountry             | R          | String    | The country of the rating system   |
| pr_ratingcustomtypetv        | R          | String    | The TV designated rating           |
| pr_ratingcustomtypempaa      | R          | String    | The MPAA designated rating         |
| pr_ratingcustomtypecaenglish | R          | String    | The english designated rating      |
| pr_ratingcustomtypecafrench  | R          | String    | The french designated rating       |
| pr_unratedlock               | R          | Switch    | Whether unrated can be shown       |

#### Now Playing

The following channels reflect information about what is currently playing.
Please note that which channels are being updated depends on the source of the information and the format it is in (ie album name will only appear if the source is some type of song and the metadata for the song is in a format sony can parse).

Please note the following acyronyms are used:

| Name | Description                |
| ---- | -------------------------- |
| BIVL | Sony Bravia Internet Link  |
| DAB  | Digital Audio Broadcasting |

| Channel Type ID      | Read/Write | Item Type         | Description                       |
| -------------------- | ---------- | ----------------- | --------------------------------- |
| pl_albumname         | R          | String            | Album name                        |
| pl_applicationname   | R          | String            | Application name                  |
| pl_artist            | R          | String            | Artist                            |
| pl_audiochannel      | R          | String            | Audio channel                     |
| pl_audiocodec        | R          | String            | Audio codec                       |
| pl_audiofrequency    | R          | String            | Audio frequency                   |
| pl_bivlassetid       | R          | String            | BIVL asset id                     |
| pl_bivlprovider      | R          | String            | BIVL provider                     |
| pl_bivlserviceid     | R          | String            | BIVL service id                   |
| pl_broadcastfreq     | R          | Number:Frequency  | Broadcasting frequency            |
| pl_broadcastfreqband | R          | String            | Broadcasting frequency band       |
| pl_channelname       | R          | String            | Channel name                      |
| pl_chaptercount      | R          | Number            | Chapter count                     |
| pl_chapterindex      | R          | Number            | Chapter index                     |
| pl_cmd               | RW (1)     | String            | Playing command                   |
| pl_contentkind       | R          | String            | Content kind                      |
| pl_dabcomponentlabel | R          | String            | DAB component label               |
| pl_dabdynamiclabel   | R          | String            | DAB dynamic label                 |
| pl_dabensemblelabel  | R          | String            | DAB ensemble label                |
| pl_dabservicelabel   | R          | String            | DAB service label                 |
| pl_dispnum           | R          | String            | Display number                    |
| pl_durationmsec      | R          | Number:DataAmount | Duration                          |
| pl_durationsec       | R          | Number:DataAmount | Duration                          |
| pl_fileno            | R          | String            | File number                       |
| pl_genre             | R          | String            | Genre                             |
| pl_index             | R          | Number            | Index number                      |
| pl_is3d              | R          | String            | 3D setting                        |
| pl_mediatype         | R          | String            | Media type                        |
| pl_originaldispnum   | R          | String            | Original display number           |
| pl_output            | R          | String            | Output                            |
| pl_parentindex       | R          | Number            | Parent index                      |
| pl_parenturi         | R          | String            | Parent URI                        |
| pl_path              | R          | String            | Path to content                   |
| pl_playlistname      | R          | String            | Playlist name                     |
| pl_playspeed         | R          | String            | Playing speed                     |
| pl_playstepspeed     | R          | Number            | Playing step speed                |
| pl_podcastname       | R          | String            | Podcast name                      |
| pl_positionmsec      | R          | Number:DataAmount | Position                          |
| pl_positionsec       | R          | Number:DataAmount | Position                          |
| pl_presetid          | RW (1)     | Number            | Preset identifier                 |
| pl_programnum        | R          | Number            | Program number                    |
| pl_programtitle      | R          | String            | Program title                     |
| pl_repeattype        | R          | String            | Repeat type                       |
| pl_service           | R          | String            | Service identifier                |
| pl_source            | R          | String            | Source                            |
| pl_sourcelabel       | R          | String            | Source label                      |
| pl_startdatetime     | R          | String            | Start date/time                   |
| pl_state             | R          | String            | Current state                     |
| pl_statesupplement   | R          | String            | Supplemental information to state |
| pl_subtitleindex     | R          | Number            | Subtitle index                    |
| pl_title             | R          | String            | Title                             |
| pl_totalcount        | R          | Number            | Total count                       |
| pl_tripletstr        | R          | String            | Triplet string                    |
| pl_uri               | R          | String            | URI                               |
| pl_videocodec        | R          | String            | Video codec                       |

1. The playing command supports the following:

| Command   | Description                                |
| --------- | ------------------------------------------ |
| play      | Continue playing content                   |
| pause     | Pause content                              |
| stop      | Stop playing content                       |
| next      | Play next content                          |
| prev      | Play previous content                      |
| fwd       | Fast forward content                       |
| bwd       | Rewind content                             |
| fwdseek   | Seek forward (radio only)                  |
| bwdseek   | Seek backward (radio only)                 |
| setpreset | Set current as preset (set as pl_presetid) |
| getpreset | Get preset (as set in pl_presetid)         |

fwd/bwd on a radio will move the frequency manually one step forward/backward

#### TV/Radio Preset channels

Since changing TV and Radio stations are a frequent occurance, the addon will define a `ps_channel-xxxx` channel for each TV and/or radio source.
This is a helper channel to select any scanned (for TV) or preset (for radio) channels.
If the UI you are using supports state, then a button will be created to be able to quickly select the value for the channel

| Channel Type ID  | Read/Write | Item Type | Description               |
| ---------------- | ---------- | --------- | ------------------------- |
| ps_channel-{src} | RW         | String    | The preset for the source |

`{src}` will be the source portion of the source URI.
Example: if you have a source (from the `sources` channel) of `tv:atsct`, a `ps_channel-atsct` will be created with options for each digital (ATSCT) channel defined.

Example: for the XBR-43X830C (TV) - I'd have a channel called `ps_channel-atsct`.
If I send `5.2` to that channel, the TV would switch station 5.2 (and on the next polling, the associated playing and content channels would be updated to reflect that change).

#### Device Inputs

The following will be a list of all inputs defined on the device.
Please note that these will only appear on devices that are single output zone devices (TVs, blurays, etc).
Please see terminal status for multizone devices.
Please note that dynamic inputs (such as a USB) will not be listed here unless the USB was plugged in when the thing was created.

| Channel Type ID     | Read/Write | Item Type | Description                               |
| ------------------- | ---------- | --------- | ----------------------------------------- |
| in_uri-{inp}        | R (1)      | String    | The URI of the input                      |
| in_title-{inp}      | R (2)      | String    | The title of the input (hdmi, etc)        |
| in_label-{inp}      | R (2)      | String    | The label of the input                    |
| in_icon-{inp}       | R (3)      | String    | The icon meta data representing the input |
| in_connection-{inp} | R (4)      | Switch    | Whether something is connected            |
| in_status-{inp}     | R (5)      | String    | The status of the input                   |

1. Each input on the system will be assigned this set of channels.
If you have for inputs (HDMI1-HDMI4), you'd have four sets of these channels each named for the input (`in_uri-hdmi1`, `in_uri-hdmi2`, etc).
Note: if your device only has a single input - the channel will be named `in_uri-main`.
2. The title is the official title of the input (hdmi1, hdmi2, etc) and the label is text that you have specified on the device for the input (maybe DVD, Media, etc).
3. This is meta data for the icon.
Example: this channel will contain `meta:hdmi` for an HDMI icon or `meta:video` for the Video input icon.
4. The connection will tell you if something is connected to that input.
5. The status will tell you the status (the actual text depends on the device).
Generally, if this is blank - the input is not selected.
If not blank, the input is selected.
If the input has some type of content, the status will be 'true' or 'active'.
If the input is selected but the feeding device is off (or is not outputting any content), the status generally will be 'false' or 'inactive'.

#### Terminal Sources

The following will be a list of all inputs and outputs defined on the device (may be virtual inputs/outputs as well such as bluetooth).
If the device is a single output zone device, there will be a single device with an id of "main" that describes the output (inputs will be defined in the `in_` channels).
If the device is a multi-zone devices (AVRS), then the terminals will include all inputs and outputs.

Please note that dynamic inputs (such as a USB) will not be listed here unless the USB was plugged in when the thing was created.

| Channel Type ID    | Read/Write | Item Type | Description                               |
| ------------------ | ---------- | --------- | ----------------------------------------- |
| tm_uri-{id}        | R (1)      | String    | The URI of the terminal                   |
| tm_title-{id}      | R (2)      | String    | The title of the terminal                 |
| tm_label-{id}      | R (2)      | String    | The label of the terminal                 |
| tm_icon-{id}       | R          | Image     | The icon representing the terminal        |
| tm_connection-{id} | R (3)      | String    | The connection status of the terminal     |
| tm_active-{id}     | RW (4)     | Switch    | Whether the terminal is active            |
| tm_source-{id}     | RW (5)     | String    | The source URI connected to this terminal |

1. Each terminal (input or output) will be assigned this set of channels.
If you have for inputs (HDMI1-HDMI4), you'd have four sets of these channels each named for the input (`tm_uri-hdmi1`, `tm_uri-hdmi2`, etc).
Note: if your device only has a single input or a single output - the channel will be named `tm_uri-main`.
2. The title is the official title of the terminal (hdmi1, hdmi2, etc) and the label is text that you have specified on the device for the input (maybe DVD, Media, etc).
3. The connection will tell you if something is connected to that terminal.
Unlike `in_connection`, this is an open ended string from sony that potentially gives you more information than a switch.
Generally the value will always be "connected" on AVRs
4. Specifies whether the terminal is active or not.
Active, generally, means either the terminal is selected (in the case of inputs) or is powered (in the case of outputs).
Setting active on an output will power on the terminal
5. This will be the source identifier of the source that is connected to this terminal if that terminal is an output.
Setting it on an output terminal will switch the terminal to that input source.

Please note that some terminal sources **cannot** be activated via the tm_active (sending "ON" to the tm_active channel will do nothing).
Sources that fall into this category are generally content based sources - like USB storage, BluRay/DVD storage, etc.
To activate these, you just select the content (as described in the next section) for the terminal source to become active.

#### Content

This set of channels allows you to browse through some type of content (USB, TV channels, radio stations, etc).
Available starting points will be found in the `sources` channel described above.
Please use the `isbrowesable` channel to determine if that source is a browesable source (for some reasons DLNA is not a browseable source as they want you to use a DLNA service to browse it instead).

Please note that which channels are being updated depends on the source of the information and the format it is in (ie album name will only appear if the content is some type of song and the metadata for the song is in a format sony can parse).

Please note the following acyronyms are used:

| Name | Description                |
| ---- | -------------------------- |
| BIVL | Sony Bravia Internet Link  |
| DAB  | Digital Audio Broadcasting |
| EPG  | Electronic Program Guide   |

| Channel Type ID             | Read/Write | Item Type         | Description                      |
| --------------------------- | ---------- | ----------------- | -------------------------------- |
| cn_albumname                | R          | String            | Album name                       |
| cn_applicationname          | R          | String            | Application name                 |
| cn_artist                   | R          | String            | Artist                           |
| cn_audiochannel             | R          | String            | Audio channel                    |
| cn_audiocodec               | R          | String            | Audio codec                      |
| cn_audiofrequency           | R          | String            | Audio frequency                  |
| cn_bivlserviceid            | R          | String            | BIVL service id                  |
| cn_bivleassetid             | R          | String            | BIVL asset id                    |
| cn_bivlprovider             | R          | String            | BIVL provider                    |
| cn_broadcastfreq            | R          | Number:Frequency  | Broadcast frequency              |
| cn_broadcastfreqband        | R          | String            | Broadcase frequency band         |
| cn_channelname              | R          | String            | Channel name                     |
| cn_channelsurfingvisibility | RW (4)     | String            | Visibility setting for surfing   |
| cn_chaptercount             | R          | Number            | Chapter count                    |
| cn_chapterindex             | R          | Number            | Chapter index                    |
| cn_childcount               | R (1)      | Number            | Count of children                |
| cn_clipcount                | R          | Number            | Clip count                       |
| cn_cmd                      | RW (2)     | String            | Content command                  |
| cn_contentkind              | R          | String            | Content kind                     |
| cn_contenttype              | R          | String            | Content type                     |
| cn_createdtime              | R          | String            | Content created date/time        |
| cn_dabcomponentlabel        | R          | String            | DAB component label              |
| cn_dabdynamiclabel          | R          | String            | DAB dynamic label                |
| cn_dabensemblelabel         | R          | String            | DAB ensemble label               |
| cn_dabservicelabel          | R          | String            | DAB service label                |
| cn_description              | R          | String            | Content description              |
| cn_directremotenum          | R          | Number            | Direct remote number             |
| cn_dispnum                  | R          | String            | Display number                   |
| cn_durationmsec             | R          | Number:DataAmount | Duration                         |
| cn_durationsec              | R          | Number:DataAmount | Duration                         |
| cn_epgvisibility            | RW (4)     | String            | Visibility setting for EPG       |
| cn_eventid                  | R          | String            | Event identifier                 |
| cn_fileno                   | R          | String            | File number                      |
| cn_filesizebyte             | R          | Number:DataAmount | File size                        |
| cn_folderno                 | R          | String            | Folder number                    |
| cn_genre                    | R          | String            | Genre                            |
| cn_globalplaybackcount      | R          | Number            | Global playback count            |
| cn_hasresume                | R (3)      | String            | Can resume                       |
| cn_idx                      | RW (1)     | Number            | Content index number             |
| cn_is3d                     | R (3)      | String            | 3D setting                       |
| cn_is4k                     | R (3)      | String            | 4K setting                       |
| cn_isalreadyplayed          | R (3)      | String            | Already played setting           |
| cn_isautodelete             | R (3)      | String            | Auto delete setting              |
| cn_isbrowsable              | R (3)      | String            | Whether browsesable or not       |
| cn_isnew                    | R (3)      | String            | Whether new or not               |
| cn_isplayable               | R (3)      | String            | Whether playable or not          |
| cn_isplaylist               | R (3)      | String            | Whether a playlist or not        |
| cn_isprotected              | RW (3)     | String            | Whether protected or not         |
| cn_issoundphoto             | R (3)      | String            | Whether a sound photo or not     |
| cn_mediatype                | R          | String            | Media type                       |
| cn_originaldispnum          | R          | String            | Original display number          |
| cn_output                   | R          | String            | Possible output                  |
| cn_parentalcountry          | R          | String            | Parental rating country          |
| cn_parentalrating           | R          | String            | Parental rating                  |
| cn_parentalsystem           | R          | String            | Parental rating system           |
| cn_parentindex              | R          | Number            | Parent index                     |
| cn_parenturi                | RW (1)     | String            | Parent content URI               |
| cn_path                     | R          | String            | Path to content                  |
| cn_playlistname             | R          | String            | Playlist name                    |
| cn_podcastname              | R          | String            | PODcast name                     |
| cn_productid                | R          | String            | Product identifier               |
| cn_programmediatype         | R          | String            | Program media type               |
| cn_programnum               | R          | Number            | Program number                   |
| cn_programservicetype       | R          | String            | Program serivce type             |
| cn_programtitle             | R          | String            | Program title                    |
| cn_remoteplaytype           | R          | String            | Remote play type                 |
| cn_repeattype               | R          | String            | Repeat type                      |
| cn_service                  | R          | String            | Service type                     |
| cn_sizemb                   | R          | Number:DataAmount | Size                             |
| cn_source                   | R          | String            | Source                           |
| cn_sourcelabel              | R          | String            | Source label                     |
| cn_startdatetime            | R          | String            | Start date/time                  |
| cn_state                    | R          | String            | Current state                    |
| cn_statesupplement          | R          | String            | Supplementl information to state |
| cn_storageuri               | R          | String            | Storage URI                      |
| cn_subtitlelanguage         | R          | String            | Subtitle language                |
| cn_subtitletitle            | R          | String            | Subtitle title                   |
| cn_synccontentpriority      | R          | String            | Synchronized content priority    |
| cn_title                    | R          | String            | Content title                    |
| cn_totalcount               | R          | Number            | Total count                      |
| cn_tripletstr               | R          | String            | Triplet string                   |
| cn_uri                      | R (1)      | String            | Content URI                      |
| cn_usercontentflag          | R          | Switch            | User content flag                |
| cn_videocodec               | R          | String            | Video codec                      |
| cn_visibility               | RW (4)     | String            | General visibility setting       |

1. Please refer to "Using Content" section for more information
2. The only command supported is "select" to start playing the content
3. Generally these flags contain either "true" or "false".
They are not switches since sony defined the item as strings and there may potentially be other values I'm unaware of.
4. You can set the visibility of the content to the various guides available and this generally only applies to TV/Radio stations (where you can 'surf' through them)

### Browser Channels (service ID of "browser")

The following list the channels for the browser service.
The browser service allows management of a browser on the device.

| Channel Type ID | Read/Write | Item Type | Description                     |
| --------------- | ---------- | --------- | ------------------------------- |
| browsercontrol  | RW         | String    | The browser command             |
| texturl         | RW         | String    | The URL in the browser          |
| texttitle       | R          | String    | The title of the current page   |
| texttype        | R          | String    | The type of the current page    |
| textfavicon     | R          | Image     | The favicon of the current page |

The browsercontrol allows you to send a command to the browser such as 'start' (or 'activate') or 'stop' to start/stop the browser.
Send a URL to the texturl to have the browser go to that URL (please note that you can generally just send the URL and the browser will automatically activate).

### Illumination (service ID of "illumination")

The following channels are for the illumination service. The illumination service provides management of illumination functions on the device.

| Channel Type ID      | Read/Write | Item Type      | Description                      |
| -------------------- | ---------- | -------------- | -------------------------------- |
| illuminationsettings | RW         | GeneralSetting | The setting for the illumination |

### CEC Channels (service ID of "cec")

The following list the channels for the HDMI CEC (consumer electronics control) service.
The CEC service allows management of the HDMI CEC settings.

| Channel Type ID        | Read/Write | Item Type | Description                                                   |
| ---------------------- | ---------- | --------- | ------------------------------------------------------------- |
| controlmode            | RW         | Switch    | Whether CEC is active or not                                  |
| mhlautoinputchangemode | RW         | Switch    | Whether the device will automatically change to the MHL input |
| mhlpowerfeedmode       | RW         | Switch    | Whether the device will power MHL device                      |
| poweroffsyncmode       | RW         | Switch    | Whether the device will turn off with CEC                     |
| poweronsyncmode        | RW         | Switch    | Whether the device will power on with CEC                     |

### System Channels (service ID of "system")

The following list the channels for the system service.
The system service allows management of general system settings.

| Channel Type ID                 | Read/Write | Item Type         | Description                                           |
| ------------------------------- | ---------- | ----------------- | ----------------------------------------------------- |
| powerstatus                     | RW (1)     | Switch            | The current power status (see notes)                  |
| currenttime                     | R          | DateTime          | Current time of the device (format depends on device) |
| ledindicatorstatus              | RW (2)     | String            | LED indicator status                                  |
| powersavingsmode                | RW (3)     | String            | The power savings mode                                |
| wolmode                         | RW         | Switch            | Whether WOL is enabled                                |
| language                        | RW         | String            | The langauge used                                     |
| reboot                          | RW (4)     | Switch            | Whether to reboot the device                          |
| syscmd                          | RW (5)     | String            | The IRCC command to send                              |
| postalcode                      | RW         | String            | The postal code of the device                         |
| devicemiscsettings              | RW         | GeneralSetting    | Misc device settings (timezones, auot update, etc)    |
| powersettings                   | RW         | GeneralSetting    | The power settings (wol, standby, etc)                |
| sleepsettings                   | RW         | GeneralSetting    | The sleep timer settings                              |
| wutangsettings                  | RW         | GeneralSetting    | The wutang settings (google cast settings)            |
| st_devicename-{src}             | R (6)      | String            | The storage device name                               |
| st_error-{src}                  | R (6)      | String            | Any storage errors                                    |
| st_filesystem-{src}             | R (6)      | String            | The storage file system                               |
| st_finalizestatus-{src}         | R (6)      | String            | The storage finalization status                       |
| st_format-{src}                 | R (6)      | String            | The storage format                                    |
| st_formatstatus-{src}           | R (6)      | String            | The storage format status                             |
| st_formattable-{src}            | R (6)      | String            | The storage formattable status                        |
| st_formatting-{src}             | R (6)      | String            | Whether the storage is formatting                     |
| st_freecapacitymb-{src}         | R (6)      | Number:DataAmount | The storage free space                                |
| st_hasnonstandarddata-{src}     | R (6)      | String            | Whether the storage has non-standard data             |
| st_hasunsupportedcontents-{src} | R (6)      | String            | Whether the storage has unsupported contents          |
| st_isavailable-{src}            | R (6)      | String            | Whether the storage is available                      |
| st_islocked-{src}               | R (6)      | String            | Whether the storage is locked                         |
| st_ismanagementinfofull-{src}   | R (6)      | String            | Whether the storage management info is full           |
| st_isprotected-{src}            | R (6)      | String            | Whether the storage is protected                      |
| st_isregistered-{src}           | R (6)      | String            | Whether the storage is registered                     |
| st_isselfrecorded-{src}         | R (6)      | String            | Whether the storage is self recorded                  |
| st_issqvsupported-{src}         | R (6)      | String            | Whether the storage is SQV (standard quality voice)   |
| st_lun-{src}                    | R (6)      | Number            | The storage LUN (logical unit number)                 |
| st_mounted-{src}                | R (6)      | String            | The storage mount status                              |
| st_permission-{src}             | R (6)      | String            | The storage permission                                |
| st_position-{src}               | R (6)      | String            | The storage position (front, back, internal, etc)     |
| st_protocol-{src}               | R (6)      | String            | The storage protocol                                  |
| st_registrationdate-{src}       | R (6)      | String            | The storage registration date                         |
| st_systemareacapacitymb-{src}   | R (6)      | Number:DataAmount | The storage system capacity                           |
| st_timesectofinalize-{src}      | R (6)      | Number:Time       | The time to finalize                                  |
| st_timesectogetcontents-{src}   | R (6)      | Number:Time       | The time to get contents                              |
| st_type-{src}                   | R (6)      | String            | The storage type                                      |
| st_uri-{src}                    | R (6)      | String            | The storage URI                                       |
| st_usbdevicetype-{src}          | R (6)      | String            | The storage USB device type                           |
| st_volumelabel-{src}            | R (6)      | String            | The storage label                                     |
| st_wholeCapacityMB-{src}        | R (6)      | Number:DataAmount | The storage whole capacity                            |

1. The power status may not be accurate on startup.  Some devices will report ON when, in fact, they are off.
2. Sets the LED status - generally "Off", "Low" or "High" (there may be others specific to your device like "AutoBrightnessAdjust")
3. Sets the power savings mode - generally "Off", "Low" or "High" (there may be others specific to your device)
4. Sending 'on' to this channel will reboot the device
5. Sends an IRCC command to the device.
This can either be the raw IRCC command (AAAAAwAAHFoAAAAYAw==) or can be a name (`Home`) that is transformed by the transformation file
6.  These channels will be repeated by every storage source (ie source for a scheme of ```storage```).
Example: if you have a ```USB1``` and ```CD``` storage sources, you'd have a ```st_uri-usb1``` and a ```st_uri-cd``` channel.
Please note that, on many devices, the storage information is not reliable and a bit quirky (the st_mounted status shows unmounted even though the storage is mounted).
However, the st_mounted will reliably change when a source is physically mounted/unmounted from the unit.
Just the initial status will likely be incorrect.

### Video Channels (service ID of "video")

The following list the channels for the video service.
The video service allows management of the video quality itself.

| Channel Type ID        | Read/Write | Item Type      | Description                                         |
| ---------------------- | ---------- | -------------- | --------------------------------------------------- |
| picturequalitysettings | RW         | GeneralSetting | The settings for picture quality                    |

If supported, contains about 20 different quality settings (dimming, mode, color, hdr mode, etc)

### Video Screen Channels (service ID of "videoScreen")

The following list the channels for the video screen service.
The video service allows management of the video (screen) itself.

| Channel Type ID        | Read/Write | Item Type      | Description                                         |
| ---------------------- | ---------- | -------------- | --------------------------------------------------- |
| audiosource            | RW         | String         | The audio source of the screen (speaker, headphone) |
| bannermode             | RW         | String         | The banner mode (demo)                              |
| multiscreenmode        | RW         | String         | The multiscreen mode (pip)                          |
| pipsubscreenposition   | RW         | String         | The pip screen position                             |
| scenesetting           | RW         | String         | The sceen settings                                  |

The values of these channels are pretty much unknown and you'll need to experiment with your device if you wish to use them.

## Using Content

Many devices support content - whether the content is a list of TV stations, a list of radio stations, a DVD/bluray of chapters, a USB of music.
Most content can be represented in a folder structure (which is how Sony represents it).
Every resource is represented by a URI (ex: tv:atsct?dispNum=4.1&trip=1793.25.3&srvName=UNC-TV represents a URI to a digital TV (ATSCT) station 4.1 called UNC-TV).

To use the content involves the following channels:

1. cn_parenturi
2. cn_childcount
3. cn_idx
4. cn_uri
5. cn_cmd

The parenturi is the 'folder' you are looking at.
Example: `tv:atsct` would be the parent of all digital ATSCT channels
The childcount would be how many child items are in parenturi.
Example: if I had 20 ATSCT stations, the count would be `20`
The idx (0-based) represents the current child within the folder.
Example: if idx is `3` - the current content is the 4th child in the parent uri
The uri represents the uri to the current child within the folder.
Example: if the 4th child is UNC-TV, the uri would be `tv:atsct?dispNum=4.1&trip=1793.25.3&srvName=UNC-TV`
The cmd can then be used to issue a command for the current child.
Example: sending `select` to the cmd would select the resource in uri (ie would change the channel to UNC-TV)

### Specific example XBR-43X830C (US Bravia TV)

This example will use the PaperUI with the following channels linked:

1. cn_parenturi (as described above)
2. cn_childcount (as described above)
3. cn_idx (as described above)
4. cn_uri (as described above)
5. cn_cmd (as described above)
6. cn_title (the current child's title)
7. sources (see next statement)

First you need to find the highest level parent URI your device supports.
The simpliest method would be to link the `sources` channel and view it.
The sources channel will provide a comma delimited list of the high level URIs.
Please note that a USB source may not be present immediately after plugging a USB into the device (the source will appear after the next polling refresh).

From the X830C - the sources are `tv:analog,extInput:widi,extInput:cec,tv:atsct,extInput:component,extInput:composite,extInput:hdmi`

To view all the digital channels for ATSCT, I double click on the cn_parenturi, type in `tv:atsct` and press the checkmark.

After a second or so, the cn_count was updated to `29` (saying I have 29 channels), the cn_idx was updated to `0` (defaults back to 0 on a parent uri change), the cn_uri was updated to `tv:atsct?dispNum=4.1&trip=1793.25.3&srvName=UNC-TV` and the cn_title was updated to `UNC-TV`.

If I double check on the cn_idx, update it to `5` and press the check mark...

After a second or so, the cn_uri was updated to `tv:atsct?dispNum=5.2&trip=1851.48.4&srvName=WRAL DT` and the cn_title was updated to `WRAL DT`.

To have the TV change to that channel - I find the cn_cmd and press the `Select` button (which is the same as sending `select` to the cn_cmd channel) and the TV switches to digital station 5.2 (WRAL DT)

You can use the same general technique to navigate USB folder structure

## Full Examples

_Really recommended to autodiscover rather than manually setup thing file_

The scalar service will dynamically generate the channels supported by your device.
Since potentially hundreds of channels can be created, the following is a small example to help get you started.

scalar.Things:

```
Thing sony:scalar:home [ deviceAddress="http://192.168.1.123:52323/dmr.xml", deviceMacAddress="aa:bb:cc:dd:ee:ff", refresh=-1 ]
```

scalar.items:

```
String Scalar_ParentUri "Parent URI" { channel="sony:scalar:home:avContent#cn_parenturi" }
Number Scalar_ChildCount "Child Count" { channel="sony:scalar:home:avContent#cn_childcount" }
Number Scalar_ContentIdx "Content Index" { channel="sony:scalar:home:avContent#cn_idx" }
String Scalar_ContentUri "Content URI" { channel="sony:scalar:home:avContent#cn_uri" }
String Scalar_SelectContent "Select Content URI" { channel="sony:scalar:home:avContent#cn_cmd" }
```

## Advanced Users Only

The following information is for more advanced users...

### Local Information

**THIS IS NOT RECOMMENDED TO USE BUT I'M DOCUMENTING THIS AS A LAST RESORT**

One of the issues with the dynamically create channels is that some channels are not detected if the device is not on.
A common example would be a Bravia TV.
If you start openHAB and the thing goes online without the TV being one, there is a high likelyhood that the `audio#volume-xxx` channels will not be defined.

The first attempt to 'fix' this situation was to be able to create a thing type for the TV (that predefines the channels needed) and then to use that local thing type as the source of channels.

This addon defines the following directories:

1. `userdata/sony/definition/capabilities` which will contain the capabilities of the TV after it's gone online
2. `userdata/sony/definition/types` which will define a thing type of the TV after it's gone online
3. `userdata/sony/db/local/types` which will be the source of local thing types

Essentially - when the TV goes online - this addon will:

1. Query the device capabilities and write them to the `userdata/sony/definition/capabilities/{modelname}.json` if that file doesn't already exist
2. If a file `userdata/sony/db/local/types/{modelname}.json` exists, constructs the thing and thing type from this file
3. If the file doesn't exist, dynamically determine the channels for the device and write a thing type defintion to `userdata/sony/definition/types/{modelname}.json` if it doesn't already exist.

Now - to solve the problem of the non detected channels - you can follow this procedure:

1. Stop openHAB
2. Delete `{modelname}.json` from `userdata/sony/definition/types` if it exists.
3. Turn the device on and make sure it's at the home screen
4. Start openHAB and wait for the thing to go online
5. Verify that `{modelname}.json` in `userdata/sony/definition/types` exists.
6. Copy the `{modelname}.json` in `userdata/sony/db/local/types` directory
7. Wait a minute or so and the thing type of your thing will change to this file

From this point on - the channels created will come from the local file rather than be dynamically created.
The only exception to this is that the application channels (netflix, youtube, etc) will continue to be dynamic.

### GITHUB Information

While the local support will work, there are a number of downsides to it.
Everyone would have to do this, updates to the thing type will be nearly impossible (because everyone would have to make local changes to JSON - yuck!) and this doesn't help me diagnose issues with the device (since everything is local).

Because of this - in addition to writing things locally, the addon will query GITHUB for thing types and use thing types defined there if found.
Likewise it will upload device thing types/capabilities to allow for quicker diagnosis of new/updated capabilities as sony releases them.
This is somewhat similar to what the ZWAVE addon does (but uses github as the DB instead).

Please note that the ONLY identifying information being uploaded would be the name you assigned to the device and this will ONLY upload information if your model is new or has changed from the existing one.

There are two github repositories this addon works with:

1. [sonydevices/openHAB](https://github.com/sonydevices/openHAB) - this will contain the capabilities and thing types specific to openHAB
2. [sonydevices/dev](https://github.com/sonydevices/dev) - this will contain the master list of API calls

The github addon will:

1. On startup, the addon will look for `{modelname}.json` (as defined above) from `userdata/sony/db/local/types` and will use that if found.
2. If not found, the addon will look for `{modelname}.json` in `userdata/sony/db/github/types` and use that if found
3. If not found, the addon will look for `{modelname}.json` in the GITHUB repository under `sonydevices/openHAB/thingtypes`.
If found, the file will be downloaded to `userdata/sony/db/github/types` and will be used.
4. The thing then goes online
5. The github implementation will then compare the device capabilities to the `sonydevices/dev/apiinfo/restapi.json` (the master API document) and if any new/updated capabilities are found, will open a new issue on `sonydevices/dev` with the new/updated capability (if an issue doesn't already exist).
6. Likewise, the implementation will compare the thing type (if dynamically generated) to the `sonydevices/openHAB/thingtypes` and if any new/updated thing type channels are found, will open a new issue on `sonydevices/openHAB` with the new/updated thing type (if an issue doesn't already exist).
7. Finally, the implementation will compare the device capabilities (if dynamically generated) to the `sonydevices/openHAB/definitions/thingtypes` for the same model and if any new/updated capabilities are found, will open a new issue on `sonydevices/openHAB` with the new/updated capabilities (if an issue doesn't already exist).

By using github, this addon can be supported by a team of people and have a continued existance beyond myself.

### Disabling local/github information

You may disable either local or the github information by editing the `conf/services/runtime.cfg` and including the following:

```
sony.sources:local=false
sony.sources:github=false
```

Setting either value to anything but 'false' will result in that provider being activated.
Please note that disabling will disable the ability to use custom thing types for your devices.

### Sony Support pages

This addon provides some support pages that can be useful to you.
If you go to "{ip}:{port}/sony", you can access the page (where IP/port is the IP/Port address that openHAB is listening on for it's web interface).

#### Sony API/Definition Explorer

The first page is the API/Definition explorer.
This will allow you to load your device file and explore the API on your own.
Please ignore the "Rest API, Merge File and Save" functionality as it only applies to myself (to manage devices).

To use this page:

1. To explore your device, press the "Load File" button and navigate to your device file (in `userdata/sony/definition/capabilities` folder as described above) and open your device JSON file.
This will load all the capabilities into the right side.
2. Click on one of the capabiliites and the left side will be loaded with details of the call
3. Change any parameters you want to press the execute button
4. Results appear in the bottom window.

Example of seeing the digital channels on a TV:

1. Click on the `avContent/getSchemeList` capability and press execute (there are no parameters for this call)
2. The results will be something like `[[{"scheme":"tv"},{"scheme":"extInput"}]]`.
3. Click on the `avContent/getSourceList` capability, change the paramater from `{"scheme":"string"}` to `{"scheme":"tv"}` (as shown in the getSchemeList call) and then press execute
4. The results will be something like `[[{"source":"tv:analog"},{"source":"tv:atsct"}]]`
5. Copy the source you want to explore.
For this example I copied `{"source":"tv:atsct"}` (digital ATSCT source)
6. Click on the `avContent/getContentList` (highest version) capability
7. Replace the parameter with what you copied.
For our example - `{"source":"tv:atsct"}` and press execute
8. The results will be something like `[[{"uri":"tv:atsct?dispNum=4.1&trip=1793.25.3&srvName=UNC-TV","title":"UNC-TV","index":0,"dispNum":"4.1","originalDispNum":"4.1","tripletStr":"1793.25.3","programNum":3,"programMediaType":"tv","visibility":"visible"},...`.
You'll have a single line for each digital (ATSCT) TV station that has been scanned on the set.
9.  Copy the URI portion that you want to tune to.
For our example - `tv:atsct?dispNum=4.1&trip=1793.25.3&srvName=UNC-TV`
10. Click on the `avContent/playContent` and replace the `{"uri":"string"}` with `{"uri":"tv:atsct?dispNum=4.1&trip=1793.25.3&srvName=UNC-TV"}` and press Execute
11. The TV should switch to that channel.
For our example - it will switch to station 4.1 (UNC-TV).

Feel free to explore other APIs
