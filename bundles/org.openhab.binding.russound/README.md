# Russound Binding

This binding provides integration with any Russound system that support the RIO protocol (all MCA systems, all X systems).
This binding provides  compatibility with RIO Protocol v1.10.
The protocol document can be found in the Russound Portal ("RIO Protocol for 3rd Party Integrators.pdf").
Please update to the latest firmware to provide full compatibility with this binding.
This binding does provide full feedback from the Russound system if events occur outside of openHAB (such as keypad usage).

_Warning:_ Russound becomes unstable if you have two IP based clients connected to the same system.
Do NOT run multiple instances of this binding against the same system - this definitely causes instability.
Running this binding in addition to the MyRussound application seems to work fine, however.

_Warning:_ Try to avoid having multiple media management functions open in different clients (keypads, My Russound app, HABPanel, etc).
Although it seems to work a majority of the times, there have been instances where the sessions become confused.

## Supported Bridges/Things

- Bridge: Russound System (usually the main controller)
- Bridge: Russound Controller (1-6 controllers supported)
- Thing: Russound Source (1-8 sources supported)
- Thing: Russound Zone (1-8 &lsqb;depending on the controller&rsqb; zones supported for each controller)

## Device Discovery

The Russound binding does support discovery.
When you start device discovery, the system will scan all network interfaces and **all IP Addresses in the subnet on each interface** looking for a Russound system device.
If found, the device will be added to the inbox.
Adding the device will then start a scan of the device to discover all the controllers, sources, and zones attached defined on the device.
As these are found, they will be added to the inbox.

## HABPANEL or other UI

All media management functions are supported to allow building of a dynamic UI for the various streaming sources.
All media management channels begin with "mm".
An example HABPanel implementation can be found in the HABPanel forum.

## Thing Configuration

The following configurations occur for each of the bridges/things:

### Russound System

| Name         | Type          | Description                                                              |
|--------------|---------------|--------------------------------------------------------------------------|
| ipAddress    | string        | IP Address or host name of the russound system (usually main controller) |
| ping         | int           | Interval, in seconds, to ping the system to keep connection alive        |
| retryPolling | int           | Interval, in seconds, to retry a failed connection attempt               |
| scanDevice   | boolean       | Whether to scan device at startup and discover controllers/sources/zones |

### Russound Source

| Name   | Type | Description         |
|--------|------|---------------------|
| source | int  | The source # (1-12) |

### Russound Controller

| Name       | Type | Description                    |
|------------|------|--------------------------------|
| controller | int  | The controller address # (1-6) |

### Russound Zone

| Name | Type | Description      |
|------|------|------------------|
| zone | int  | The zone # (1-6) |

## Channels

The following channels are supported for each bridge/thing

### Russound System

| Channel Type ID | Read/Write | Item Type | Description                                                  |
|-----------------|------------|-----------|--------------------------------------------------------------|
| lang            | RW         | String    | System language (english, chinese and russian are supported) |
| allon           | RW         | Switch    | Turn on/off all zones                                        |
| controller      | R          | String    | JSON representation of all controllers in the system         |
| sources         | R          | String    | JSON representation of all sources in the system             |

#### Notes

- The JSON will look like: `[{"id":1, "name":"XXX"},...]`.
  The controller channel will contain up to 6 controllers and the sources will contain up to 8 sources (depending on how the system is configured).

### Russound Source (please see source cross-reference below for what is supported by which sources)

| Channel Type ID      | Read/Write | Item Type | Description                                                        |
|----------------------|------------|-----------|--------------------------------------------------------------------|
| name                 | R          | String    | The name of the source                                             |
| type                 | R          | String    | The type of source                                                 |
| channel              | R          | String    | The currently playing channel (usually tuner frequency)            |
| channelname          | R          | String    | The currently playing channel name                                 |
| composername         | R          | String    | The currently playing composer name                                |
| genre                | R          | String    | The currently playing genre                                        |
| artistname           | R          | String    | The currently playing artist name                                  |
| albumname            | R          | String    | The currently playing album name                                   |
| coverarturl          | R          | String    | The currently playing URL to the cover art                         |
| playlistname         | R          | String    | The currently playing play list name                               |
| songname             | R          | String    | The currently playing song name                                    |
| rating               | R          | String    | The rating for the currently played song (can be changed via zone) |
| mode                 | R          | String    | The provider mode or streaming service                             |
| shufflemode          | R          | String    | The current shuffle mode                                           |
| repeatmode           | R          | String    | The current repeat mode                                            |
| programservicename   | R          | String    | The program service name (PSN)                                     |
| radiotext            | R          | String    | The radio text                                                     |
| radiotext2           | R          | String    | The radio text (line 2)                                            |
| radiotext3           | R          | String    | The radio text (line 3)                                            |
| radiotext4           | R          | String    | The radio text (line 4)                                            |
| volume               | R          | String    | The source's volume level (undocumented)                           |
| banks                | RW         | String    | JSON representation of all banks in the system                     |
| mmscreen             | R          | String    | The media management screen id                                     |
| mmtitle              | R          | String    | The media management screen title                                  |
| mmmenu               | R          | String    | The media management screen menu json                              |
| mmattr               | R          | String    | The media management attribute                                     |
| mmmenubuttonoktext   | R          | String    | The media management OK button text                                |
| mmmenubuttonbacktext | R          | String    | The media management Cancel button text                            |
| mminfotext           | R          | String    | The media management information text                              |
| mmhelptext           | R          | String    | The media management help text                                     |
| mmtextfield          | R          | String    | The media management text field                                    |

#### Notes

1. Banks are only supported tuner sources and the JSON array will have exactly 6 banks in it (with IDs from 1 to 6).
    For non-tuner sources, an empty JSON array (`[]`) will be returned.
    For tuner sources, the JSON will look like: `[{"id":1, "name":"XXX"},...]`.
    A bank's name can be updated by sending the representation back to the channel.
    Example: `[{"id":1,"name":"FM1"},{"id":3,"name":"FM3"}]` will set the name of bank #1 to "FM1 and bank#3 to "FM3" (leaving all other bank names the same).
    After an update, the banks channel will be refreshed with the full JSON representation of all banks.
    If the name has not been changed in the refreshed value, the russound rejected the name change for some reason (generally too long of a name or a duplicate name).
1. All media management channels are ONLY valid on streaming sources (not tuners).
    All channels will return a JSON representation like `{"id":xxx, "value":"yyy"}` where 'xxx' will be a sequential identifier of the message and 'yyy' will be the payload.
    The payload will be a simple string in all cases.
    However, the mmmenu string will be a raw JSON string representing the menu structure.
    Please review the media management section in the RIO protocol document from russound for the specifications.

### Russound Controller

| Channel Type ID | Read/Write | Item Type | Description                                            |
|-----------------|------------|-----------|--------------------------------------------------------|
| zones           | R          | String    | The JSON representation of all zones in the controller |

#### Notes

- The JSON will look like: `[{"id":1, "name":"XXX"},...]`

### Russound Zone

| Channel Type ID    | Read/Write | Item Type  | Description                                                                               |
|--------------------|------------|------------|-------------------------------------------------------------------------------------------|
| name               | R          | String     | The name of the zone (changed by SCS-C5 software)                                         |
| source             | RW         | Number     | The (physical) number for the current source                                              |
| bass               | RW         | Number     | The bass setting (-10 to 10)                                                              |
| treble             | RW         | Number     | The treble setting (-10 to 10)                                                            |
| balance            | RW         | Number     | The balance setting (-10 &lsqb;full left&rsqb; to 10 &lsqb;full right&rsqb;)              |
| loudness           | RW         | Switch     | Set's the loudness on/off                                                                 |
| turnonvolume       | RW         | Dimmer     | The initial volume when turned on (0 to 100)                                              |
| donotdisturb       | RW         | String     | The do not disturb setting (on/off/slave)                                                 |
| partymode          | RW         | String     | The party mode (on/off/master)                                                            |
| status             | RW         | Switch     | Whether the zone is on or off                                                             |
| volume             | RW         | Dimmer     | The current volume of the zone (0 to 100)                                                 |
| mute               | RW         | Switch     | Whether the zone is muted or not                                                          |
| page               | R          | Switch     | Whether the zone is in paging mode or not                                                 |
| sharedsource       | R          | Switch     | Whether the zone's source is being shared or not                                          |
| sleeptimeremaining | RW         | Number     | Sleep time, in minutes, remaining (0 to 60 in 5 step increments)                          |
| lasterror          | R          | String     | The last error that occurred in the zone                                                  |
| enabled            | R          | Switch     | Whether the zone is enabled or not                                                        |
| repeat             | W          | Switch     | Toggle the repeat mode for the current source                                             |
| shuffle            | W          | Switch     | Toggle the shuffle mode for the current source                                            |
| rating             | W          | Switch     | Signal a like (ON) or dislike (OFF) to the current source                                 |
| keypress           | W          | String     | (Advanced) Send a keypress from the zone                                                  |
| keyrelease         | W          | String     | (Advanced) Send a keyrelease from the zone                                                |
| keyhold            | W          | String     | (Advanced) Send a keyhold from the zone                                                   |
| keycode            | W          | String     | (Advanced) Send a keycode from the zone                                                   |
| event              | W          | String     | (Advanced) Send an event from the zone                                                    |
| systemfavorites    | RW         | String*    | The JSON representation for system favorites                                              |
| zonefavorites      | RW         | String**   | The JSON representation for zone favorites                                                |
| presets            | RW         | String***  | The JSON representation for zone presets                                                  |
| mminit             | W          | Switch**** | Whether to initial a media management session (ON) or close an existing one (OFF)         |
| mmcontextmenu      | W          | Switch**** | Whether to initial a media management context session (ON) or close an existing one (OFF) |

#### Notes:

1. As of the time of this document, rating ON (like) produced an error in the firmware from the related command.
    This has been reported to Russound.
1. keypress/keyrelease/keyhold/keycode/event are advanced commands that will pass the related event string to Russound (i.e. `EVENT C[x].Z[y]!KeyPress [stringtype]`).
    Please see the "RIO Protocol for 3rd Party Integrators.pdf" (found at the Russound Portal) for proper string forms.
1. If you send an OnOffType to the volume will have the same affect as turning the zone on/off (ie sending OnOffType to "status")
1. The volume PercentType will be scaled to Russound's volume of 0-50 (ie 50% = volume of 25, 100% = volume of 50)
1. Initialize a media management session by sending ON to the channel.
    The related source thing will then start sending out media management information in the MM channels.
    To close the session - simply send OFF to the channel.
    Sending OFF to the channel when a session has not been initialized does nothing.
    Likewise if the related source is a tuner, this command does nothing.

##### System Favorites

The JSON will look like `[{"id":xxx,"valid":true,"name":"yyyy"},...]` and will have a representation for each VALID favorite on the system (ie where "valid" is true).
You will have up to 32 system favorites in the JSON array (the ID field will be between 1 and 32).
System favorites will be the same on ALL zones (because they are system level).
This channel appears on the zone because when you send a system favorite representation to zone channel, it sets the system favorite to what is playing in the zone.

There are three different ways to use this channel:

1. Save a system favorite.  Send a representation with "valid" set to true.
    Example: to set system favorite 3 to what is playing in the zone: `[{"id":3,"valid":true,"name":"80s Rock"}]`.
    If system favorite 3 was invalid, this would save what is currently playing and make it valid.
    If system favorite 3 was already valid, this would overlay the favorite with what is currently playing and change its name.
1. Update the name of a system favorite.
    Send a representation of an existing ID with "valid" set to true and the new name.
    Example: we could update system favorite 3 (after the above statement) by sending: `[{"id":3,"valid":true,"name":"80s Rock Even More"}]`.
    Note this will ONLY change the name (this will NOT save what is currently playing to the system favorite).
1. Delete a system favorite.
    Send a representation with "valid" as false.
    Example: deleting system favorite 3 (after the above statements) by sending: `[{"id":3","valid":false"}]`

The channel will be refreshed with the new representation after processing.
If the refreshed representation doesn't include the changes, the russound system rejected them for some reason (generally length of the name).

##### Zone Favorites

The JSON will look like `[{"id":xxx,"valid":true,"name":"yyyy"},...]` and will have a representation for each VALID favorite in the zone (ie where "valid" is true).
You will have up to 2 zone favorites in the JSON array (the ID field will be between 1 and 2).

There are two different ways to use this channel:

1. Save a zone favorite.
    Send a representation with "valid" set to true.
    Example: to set zone favorite 2 to what is playing in the zone: `[{"id":2,"valid":true,"name":"80s Rock"}]`.
1. Delete a zone favorite.
    Send a representation with "valid" as false.
    Example: deleting zone favorite 2 (after the above statement) by sending: `[{"id":2","valid":false"}]`

There is no ability to change JUST the name.
Sending a new name will save the new name AND set the favorite to what is currently playing.

The channel will be refreshed with the new representation after processing.
If the refreshed representation doesn't include the changes, the russound system rejected them for some reason (generally length of the name).

##### Zone Presets

The JSON will look like `[{"id":xxx,"valid":true,"name":"yyyy", "bank": xxx, "bankPreset":yyyy},...]` and will have a representation for each VALID preset in the zone (ie where "valid" is true).
Please note that this channel is only valid if the related source is a tuner.
If not a tuner, an empty json array will be returned.
You will have up to 36 presets to choose from (ID from 1 to 36).
The "bank" and "bankPreset" are readonly (will be ignored if sent) and are informational only (i.e. specify the bank and the preset within the bank for convenience).

There are two different ways to use this channel:

1. Save a preset.
    Send a representation to an ID that is invalid with "valid" set to true.
    Example: to set a zone pret 2 to what is playing in the zone: `[{"id":2,"valid":true,"name":"103.7 FM"}]`.
1. Save a preset with default name.
    Send a representation to an ID that is invalid with "valid" set to true.
    Example: to set a zone pret 2 to what is playing in the zone: `[{"id":2,"valid":true,"name":"103.7 FM"}]`.
1. Delete a zone favorite.
    Send a representation with "valid" as false.
    Example: deleting zone favorite 2 (after the above statement) by sending: `[{"id":2","valid":false"}]`

There is no ability to change JUST the name.
Sending a new name will save the new name AND set the favorite to what is currently playing.

The channel will be refreshed with the new representation after processing.
If the refreshed representation doesn't include the changes, the russound system rejected them for some reason (generally length of the name).

### Source channel support cross reference

| Channel Type ID    | Sirius | XM | SMS3 | DMS 3.1 Media | DMS 3.1 AM/FM | iBridge | Internal AM/FM | Arcam T32 | Others |
|--------------------|--------|----|------|---------------|---------------|---------|----------------|-----------|--------|
| name               | X      | X  | X    | X             | X             | X       | X              | X         | X      |
| type               | X      | X  | X    | X             | X             | X       | X              | X         | X      |
| ipaddress          |        |    | X    | X             | X             |         |                |           |        |
| composername       | X      |    |      |               |               |         |                |           |        |
| channel            |        |    |      |               | X             |         | X              |           |        |
| channelname        | X      | X  |      | X             |               |         |                | X         |        |
| genre              | X      | X  |      |               |               |         |                | X         |        |
| artistname         | X      | X  | X    | X             |               | X       |                |           |        |
| albumname          |        |    | X    | X             |               | X       |                |           |        |
| coverarturl        | 1      |    |      | X             |               |         |                |           |        |
| playlistname       |        |    | X    | X             |               | X       |                |           |        |
| songname           | X      | X  | X    | X             |               | X       |                |           |        |
| mode               |        |    |      | X             |               |         |                |           |        |
| shufflemode        |        |    |      | X             |               | X       |                |           |        |
| repeatmode         |        |    |      | X             |               |         |                |           |        |
| rating             |        |    |      | X             |               |         |                |           |        |
| programservicename |        |    |      |               | X             |         | X              |           |        |
| radiotext          |        |    |      |               | X             |         | X              | X         |        |
| radiotext2         |        |    |      |               |               |         |                | X         |        |
| radiotext3         |        |    |      |               |               |         |                | X         |        |
| radiotext4         |        |    |      |               |               |         |                | X         |        |

1. Sirius Internal Radio Only

## Example

The following is an example of

1. Main controller (#1) at ipaddress 192.168.1.24
1. One Sources connected to it (#1 is the internal AM/FM)
1. Four zones on the controller (1-4 in various rooms)

.things

```java
russound:rio:home [ ipAddress="192.168.1.24", ping=30, retryPolling=10 ]
russound:controller:1 (russound:rio:home) [ controller=1 ]
russound:source:1 (russound:rio:home) [ source=1 ]
russound:zone:1  (russound:controller:1) [ zone=1 ]
russound:zone:2  (russound:controller:1) [ zone=2 ]
russound:zone:3  (russound:controller:1) [ zone=3 ]
russound:zone:4  (russound:controller:1) [ zone=4 ]
```

This is an example of all the items that can be included (regardless of the above setup)
.items

```java
Switch Rio_Status "Status [%s]" { channel="russound:rio:home:status" }
Switch Rio_AllOn "All Zones" { channel="russound:rio:home:allon" }

String Rio_Zone_Name "Name [%s]" { channel="russound:zone:1:name" }
Switch Rio_Zone_Status "Status" { channel="russound:zone:1:status" }
Number Rio_Zone_Source "Source [%s]" { channel="russound:zone:1:source" }
Number Rio_Zone_Bass "Bass [%s]" { channel="russound:zone:1:bass" }
Number Rio_Zone_Treble "Treble [%s]" { channel="russound:zone:1:treble" }
Number Rio_Zone_Balance "Balance [%s]" { channel="russound:zone:1:balance" }
Switch Rio_Zone_Loudness "Loudness [%s]" { channel="russound:zone:1:loudness" }
Number Rio_Zone_TurnOnVolume "Turn on Volume [%s]" { channel="russound:zone:1:turnonvolume" }
String Rio_Zone_DoNotDisturb "Do not Disturb [%s]" { channel="russound:zone:1:donotdisturb" }
String Rio_Zone_PartyMode "Party Mode [%s]" { channel="russound:zone:1:partymode" }
Dimmer Rio_Zone_Volume "Volume [%s %%]"   { channel="russound:zone:1:volume" }
Switch Rio_Zone_Mute "Mute [%s]" { channel="russound:zone:1:mute" }
Switch Rio_Zone_Page "Page [%s]" { channel="russound:zone:1:page" }
Switch Rio_Zone_SharedSource "Shared Source [%s]" { channel="russound:zone:1:sharedsource" }
Number Rio_Zone_SleepTime "Sleep Time Remaining [%s]"   { channel="russound:zone:1:sleeptimeremaining" }
String Rio_Zone_LastError "Last Error  [%s]" { channel="russound:zone:1:lasterror" }
Switch Rio_Zone_Enabled "Enabled [%s]" { channel="russound:zone:1:enabled" }
Switch Rio_Zone_Repeat "Toggle Repeat" { channel="russound:zone:1:repeat", autoupdate="false" }
Switch Rio_Zone_Shuffle "Toggle Shuffle" { channel="russound:zone:1:shuffle", autoupdate="false" }
Switch Rio_Zone_Rating "Rating" { channel="russound:zone:1:rating", autoupdate="false" }

String Rio_Src_Name "Name [%s]" { channel="russound:source:1:name" }
String Rio_Src_Type "Type [%s]" { channel="russound:source:1:type" }
String Rio_Src_Composer "Composer [%s]" { channel="russound:source:1:composername" }
String Rio_Src_Channel "Channel [%s]" { channel="russound:source:1:channel" }
String Rio_Src_ChannelName "Channel Name [%s]" { channel="russound:source:1:channelname" }
String Rio_Src_Genre "Genre [%s]" { channel="russound:source:1:genre" }
String Rio_Src_ArtistName "Artist [%s]" { channel="russound:source:1:artistname" }
String Rio_Src_AlbumName "Album [%s]" { channel="russound:source:1:albumname" }
String Rio_Src_Cover "Cover Art [%s]" { channel="russound:source:1:coverarturl" }
String Rio_Src_PlaylistName "PlayList [%s]" { channel="russound:source:1:playlistname" }
String Rio_Src_SongName "Song [%s]" { channel="russound:source:1:songname" }
String Rio_Src_Mode "Mode [%s]" { channel="russound:source:1:mode" }
String Rio_Src_Shuffle "Shuffle [%s]" { channel="russound:source:1:shufflemode" }
String Rio_Src_Repeat "Repeat [%s]" { channel="russound:source:1:repeatmode" }
String Rio_Src_Rating "Rating [%s]" { channel="russound:source:1:rating" }
String Rio_Src_ProgramServiceName "PSN [%s]" { channel="russound:source:1:programservicename" }
String Rio_Src_RadioText "Radio Text [%s]" { channel="russound:source:1:radiotext" }
String Rio_Src_RadioText2 "Radio Text #2 [%s]" { channel="russound:source:1:radiotext2" }
String Rio_Src_RadioText3 "Radio Text #3 [%s]" { channel="russound:source:1:radiotext3" }
String Rio_Src_RadioText4 "Radio Text #4 [%s]" { channel="russound:source:1:radiotext4" }
```

.sitemap

```perl
Frame label="Russound" {
 Text label="System" {
  Selection item=Rio_Lang mappings=[ENGLISH="English", RUSSIAN="Russian", CHINESE="Chinese"]
  Switch item=Rio_AllOn
 }

 Text label="Controller 1" {

  Text label="Zone 1" {
   Text item=Rio_Zone_Name
   Switch item=Rio_Zone_Status
   Selection item=Rio_Zone_Source mappings=[1="AM/FM", 2="Stream #1", 3="Stream #2", 4="Stream #3"]
   Setpoint item=Rio_Zone_Bass
   Setpoint item=Rio_Zone_Treble
   Setpoint item=Rio_Zone_Balance
   Switch item=Rio_Zone_Loudness
   Setpoint item=Rio_Zone_TurnOnVolume
   Selection item=Rio_Zone_DoNotDisturb mappings=[ON="On", OFF="Off", SLAVE="Slave"]
   Selection item=Rio_Zone_PartyMode mappings=[ON="On", OFF="Off", MASTER="Master"]
   Slider item=Rio_Zone_Volume
   Switch item=Rio_Zone_Mute
   Text item=Rio_Zone_Page
   Text item=Rio_Zone_SharedSource
   Setpoint item=Rio_Zone_SleepTime minValue="0" maxValue="60" step="5"
   Text item=Rio_Zone_LastError
   Text item=Rio_Zone_Enabled
   Switch item=Rio_Zone_Shuffle mappings=[ON="Toggle"]
   Switch item=Rio_Zone_Repeat mappings=[ON="Toggle"]
   Switch item=Rio_Zone_Rating mappings=[ON="Like"]
   Switch item=Rio_Zone_Rating mappings=[OFF="Dislike"]

   Text label="Source" {
    Text item= Rio_Src_Type
    Text item= Rio_Src_IP
    Text item= Rio_Src_Composer
    Text item= Rio_Src_Channel
    Text item= Rio_Src_ChannelName
    Text item= Rio_Src_Genre
    Text item= Rio_Src_ArtistName
    Text item= Rio_Src_AlbumName
    Text item= Rio_Src_Cover
    Text item= Rio_Src_PlaylistName
    Text item= Rio_Src_SongName
    Text item= Rio_Src_Mode
    Text item= Rio_Src_Shuffle
    Text item= Rio_Src_Repeat
    Text item= Rio_Src_Rating
    Text item= Rio_Src_ProgramServiceName
    Text item= Rio_Src_RadioText
    Text item= Rio_Src_RadioText2
    Text item= Rio_Src_RadioText3
    Text item= Rio_Src_RadioText4
   }
  }
 }
}
```
