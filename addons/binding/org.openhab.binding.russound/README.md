# Russound Binding

This binding provides integration with any Russound system that support the RIO protocol (all MCA systems, all X systems).  This binding provides  compatibility with RIO Protocol v1.7 (everything but the Media Managment functionality).  The protocol document can be found in the Russound Portal ("RIO Protocol for 3rd Party Integrators.pdf").  Please update to the latest firmware to provide full compatibility with this binding.  This binding does provide full feedback from the Russound system if events occur outside of openHAB (such as keypad usage).

## Supported Bridges/Things

* Bridge: Russound System (usually the main controller)
* Bridge: Russound Controller (1-6 controllers supported)
* Bridge: Russound Source (1-12 sources supported)
* Bridge: Russound Bank (1-6 banks supported for any tuner source)
* Thing: Russound Bank Preset (1-6 presets supported for each bank)
* Thing: Russound System Favorite (1-32 favorites supported)
* Bridge: Russound Zone (1-6 zones supported for each controller) 
* Thing: Russound Zone Favorite (1-2 zone favorites for each zone)
* Thing: Russound Zone Presets (1-36 presets for each zone [corresponds to banks 1-6, presets 1-6 for each bank])
                              
## Thing Configuration

The following configurations occur for each of the bridges/things:

### Russound System

| Name         | Type          | Description                                                              |
|--------------|---------------|--------------------------------------------------------------------------|
| ipAddress    | string        | IP Address or host name of the russound system (usually main controller) |
| ping         | int           | Interval, in seconds, to ping the system to keep connection alive        |
| retryPolling | int           | Interval, in seconds, to retry a failed connection attempt               |

### Russound System Favorite

| Name         | Type          | Description                                                              |
|--------------|---------------|--------------------------------------------------------------------------|
| favorite     | int           | The favorite # (1-32)                                                    |

### Russound Source

| Name         | Type          | Description                                                              |
|--------------|---------------|--------------------------------------------------------------------------|
| source       | int           | The source # (1-12)                                                      |

### Russound Bank

| Name         | Type          | Description                                                              |
|--------------|---------------|--------------------------------------------------------------------------|
| bank         | int           | The bank # (1-6)                                                         |

### Russound Bank Preset

| Name         | Type          | Description                                                              |
|--------------|---------------|--------------------------------------------------------------------------|
| preset       | int           | The preset # (1-6)                                                       |

### Russound Controller

| Name         | Type          | Description                                                              |
|--------------|---------------|--------------------------------------------------------------------------|
| controller   | int           | The controller address # (1-6)                                           |

### Russound Zone

| Name         | Type          | Description                                                              |
|--------------|---------------|--------------------------------------------------------------------------|
| zone         | int           | The zone # (1-6)                                                         |

### Russound Zone Favorite

| Name         | Type          | Description                                                              |
|--------------|---------------|--------------------------------------------------------------------------|
| favorite     | int           | The zone favorite # (1-2)                                                |

### Russound Zone Preset Commands

| Name         | Type          | Description                                                              |
|--------------|---------------|--------------------------------------------------------------------------|
| preset       | int           | The zone preset # (1-36 - corresponds to bank 1-6, preset 1-6)           |


## Channels

The following channels are supported for each bridge/thing

### Russound System

| Channel Type ID    | Read/Write | Item Type    | Description                                                          |
|--------------------|------------|--------------|--------------------------------------------------------------------- |
| version            | R          | String       | The firmware version of the system                                   |
| status             | R          | Switch       | Whether any controller/zone is on (or if all are off)                |
| language           | RW         | String       | System language (english, chinese and russian are supported)         |

### Russound System Favorite
 
| Channel Type ID    | Read/Write | Item Type    | Description                                                          |
|--------------------|------------|--------------|--------------------------------------------------------------------- |
| name               | R          | String       | The name of the system favorite (changed by zone favorites)          |
| valid              | R          | Switch       | If system favorite is valid or not (changed by zone favorites)       |

### Russound Source (please see source cross-reference below for what is supported by which sources)

| Channel Type ID    | Read/Write | Item Type    | Description                                                          |
|--------------------|------------|--------------|--------------------------------------------------------------------- |
| name               | R          | String       | The name of the source                                               |
| type               | R          | String       | The type of source                                                   |
| ipaddress          | R          | String       | The IP Address of the source                                         |
| composername       | R          | String       | The currently playing composer name                                  |
| channel            | R          | String       | The currently playing channel (usually tuner frequency)              |
| channelname        | R          | String       | The currently playing channel name                                   |
| genre              | R          | String       | The currently playing genre                                          |
| artistname         | R          | String       | The currently playing artist name                                    |
| albumname          | R          | String       | The currently playing album name                                     |
| coverarturl        | R          | String       | The currently playing URL to the cover art                           |
| coverart           | R          | Image        | The currently playing cover art image                                |
| playlistname       | R          | String       | The currently playing play list name                                 |
| songname           | R          | String       | The currently playing song name                                      |
| mode               | R          | String       | The provider mode or streaming service                               |
| shufflemode        | R          | String       | The current shuffle mode                                             |
| repeatmode         | R          | String       | The current repeat mode                                              |
| rating             | R          | String       | The rating for the currently played song (can be changed via zone)   |
| programservicename | R          | String       | The program service name (PSN)                                       |
| radiotext          | R          | String       | The radio text                                                       |
| radiotext2         | R          | String       | The radio text (line 2)                                              |
| radiotext3         | R          | String       | The radio text (line 3)                                              |
| radiotext4         | R          | String       | The radio text (line 4)                                              |
| volume             | R          | String       | The source's volume level (undocumented)                             |

### Russound Bank

| Channel Type ID    | Read/Write | Item Type    | Description                                                          |
|--------------------|------------|--------------|--------------------------------------------------------------------- |
| name               | R          | String       | The name of the bank (changed by SCS-C5 software)                    |

### Russound Preset

| Channel Type ID    | Read/Write | Item Type    | Description                                                          |
|--------------------|------------|--------------|--------------------------------------------------------------------- |
| name               | R          | String       | The name of the Preset (changed by zone preset commands)             |
| valid              | R          | Switch       | If preset is valid or not (changed by zone preset commands)          |

### Russound Controller

| Channel Type ID    | Read/Write | Item Type    | Description                                                          |
|--------------------|------------|--------------|--------------------------------------------------------------------- |
| type               | R          | String       | The model type of the controller (i.e. "MCA-C5")                     |
| ipaddress          | R          | String       | The IPAddress of the controller (only if it's the main controller)   |
| macaddress         | R          | String       | The MAC Address of the controller (only if it's the main controller) |

### Russound Zone
      
| Channel Type ID    | Read/Write | Item Type    | Description                                                          |
|--------------------|------------|--------------|--------------------------------------------------------------------- |
| status             | RW         | Switch       | Whether the zone is on or off                                        |
| name               | R          | String       | The name of the zone (changed by SCS-C5 software)                    |
| source             | RW         | Number       | The (physical) number for the current source                         |
| volume             | RW         | Number       | The current volume of the zone (0 to 50)                             |
| mute               | RW         | Switch       | Whether the zone is muted or not                                     |
| bass               | RW         | Number       | The bass setting (-10 to 10)                                         |
| treble             | RW         | Number       | The treble setting (-10 to 10)                                       |
| balance            | RW         | Number       | The balance setting (-10 [full left] to 10 [full right])             |
| loudness           | RW         | Switch       | Set's the loudness on/off                                            |
| turnonvolume       | RW         | Number       | The initial volume when turned on (0 to 50)                          |
| donotdisturb       | RW         | String       | The do not disturb setting (on/off/slave)                            |
| partymode          | RW         | String       | The party mode (on/off/master)                                       |
| page               | R          | Switch       | Whether the zone is in paging mode or not                            |
| sharedsource       | R          | Switch       | Whether the zone's source is being shared or not                     |
| sleeptimeremaining | RW         | Number       | Sleep time, in minutes, remaining (0 to 60 in 5 step increments)     |
| lasterror          | R          | String       | The last error that occurred in the zone                             |
| enabled            | R          | Switch       | Whether the zone is enabled or not                                   |
| repeat             | W          | Switch       | Toggle the repeat mode for the current source                        |
| shuffle            | W          | Switch       | Toggle the shuffle mode for the current source                       |
| rating             | W          | Switch       | Signal a like (ON) or dislike (OFF) to the current source            |
| keypress           | W          | String       | (Advanced) Send a keypress from the zone                             |
| keyrelease         | W          | String       | (Advanced) Send a keyrelease from the zone                           |
| keyhold            | W          | String       | (Advanced) Send a keyhold from the zone                              |
| keycode            | W          | String       | (Advanced) Send a keycode from the zone                              |
| event              | W          | String       | (Advanced) Send an event from the zone                               |

* As of the time of this document, rating ON (like) produced an error in the firmware from the related command.  This has been reported to Russound.
* keypress/keyrelease/keyhold/keycode/event are advanced commands that will pass the related event string to Russound (i.e. "EVENT C[x].Z[y]!KeyPress [stringtype]").  Please see the "RIO Protocol for 3rd Party Integrators.pdf" (found at the Russound Portal) for proper string forms.
* If you send a OnOffType to the volume will have the same affect as turning the zone on/off (ie sending OnOffType to "status")
* The volume PercentType will be scaled to Russound's volume of 0-50 (ie 50% = volume of 25, 100% = volume of 50)


### Russound Zone Favorite   

| Channel Type ID    | Read/Write | Item Type    | Description                                                                  |
|--------------------|------------|--------------|----------------------------------------------------------------------------- |
| name               | RW         | String       | The name of the zone favorite (only saved when the 'savexxx' cmd is issued)  |
| valid              | R          | Switch       | If favorite is valid or not ('on' when favorite is saved, 'off' when deleted |
| cmd                | W          | String       | The favorite command (see note below)                                        |

The favorite command channel ("cmd") supports the following

| Command Text | Description                                         |
|--------------|-----------------------------------------------------|
| savesys      | Save the associated zone as the a system favorite   |
| restoresys   | Restores the system favorite to the associated zone |
| deletesys    | Deletes the system favorite                         |
| savezone     | Save the associated zone as the a zone favorite     |
| restorezone  | Restores the zone favorite to the associated zone   |
| deletezone   | Deletes the zone favorite                           |

### Russound Zone Preset Commands

| Channel Type ID    | Read/Write | Item Type    | Description                                                                             |
|--------------------|------------|--------------|-----------------------------------------------------------------------------------------|
| name               | RW         | String       | The name of the preset (only saved when the 'save' preset cmd is issued)                |
| valid              | R          | Switch       | If favorite is valid or not ('on' when a preset is saved, 'off' when preset is deleted) |
| cmd                | W          | String       | The preset command (see note below)                                                     |

The preset command channel ("cmd") supports the following

| Command Text | Description                                |
|--------------|--------------------------------------------|
| save         | Save the associated zone as the preset     |
| restore      | Restores the preset to the associated zone |
| delete       | Deletes the preset                         |

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

1.  Sirius Internal Radio Only

## Full Example

The following is an example of 

1. Main controller (#1) at ipaddress 192.168.1.24
2. Two Sources connected to it (#1 is the internal AM/FM and #2 is a DMS 3.1)
3. Two System favorites (#1 FM 102.9, #2 Pandora on DMS)
4. One bank (called "FM-1")
5. Two presets within the bank (#1 FM 100.7, #2 FM 105.1)
6. Four zones on the controller (1-4 in various rooms)
7. Zone 1 has two favorites (#1 Spotify on DMS, #2 Airplay on DMS)
8. Zone 2 has two presets (#1 corresponds to bank 1/preset 1 [102.9], #2 corresponds to bank1/preset 2 [Pandora])

.things

```
russound:rio:home [ ipAddress="192.168.1.24", ping=30, retryPolling=10 ]
russound:sysfavorite:1 (russound:rio:home) [ favorite=1 ]
russound:sysfavorite:2 (russound:rio:home) [ favorite=2 ]
russound:controller:1 (russound:rio:home) [ controller=1 ] 
russound:source:1 (russound:rio:home) [ source=1 ]
russound:source:2 (russound:rio:home) [ source=2 ]
russound:bank:1 (russound:source:1) [ bank=1 ]
russound:bankpreset:1 (russound:bank:1) [ preset=1 ]
russound:bankpreset:2 (russound:bank:1) [ preset=2 ]
russound:zone:1  (russound:controller:1) [ zone=1 ]
russound:zone:2  (russound:controller:1) [ zone=2 ]
russound:zone:3  (russound:controller:1) [ zone=3 ]
russound:zone:4  (russound:controller:1) [ zone=4 ]
russound:zonefavorite:1 (russound:zone:1) [ favorite=1 ]
russound:zonefavorite:2 (russound:zone:1) [ favorite=2 ]
russound:zonepreset:1 (russound:zone:2) [ preset=1 ]
russound:zonepreset:2 (russound:zone:2) [ preset=2 ]
```

This is an example of all the items that can be included (regardless of the above setup)
.items

```
String Rio_Version "Version [%s]" { channel="russound:rio:home:version" }
String Rio_Lang "Language [%s]" { channel="russound:rio:home:lang" }
Switch Rio_Status "Status [%s]" { channel="russound:rio:home:status" }
Switch Rio_AllOn "All Zones" { channel="russound:rio:home:allon" }

String Rio_Ctl_Type "Model [%s]" { channel="russound:controller:1:type" }
String Rio_Ctl_IPAddress "IP Address [%s]" { channel="russound:controller:1:ipaddress" }
String Rio_Ctl_MacAddress "MAC [%s]" { channel="russound:controller:1:macaddress" }

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
String Rio_Src_IP "IPAddress [%s]" { channel="russound:source:1:ipaddress" }
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

String Rio_Sys_Favorite_Name "Name1 [%s]" { channel="russound:sysfavorite:1:name" }
Switch Rio_Sys_Favorite_Valid "Valid1 [%s]" { channel="russound:sysfavorite:1:valid" }
String Rio_Sys_Favorite_Name2 "Name2 [%s]" { channel="russound:sysfavorite:2:name" }
Switch Rio_Sys_Favorite_Valid2 "Valid2 [%s]" { channel="russound:sysfavorite:2:valid" }

String Rio_Zone_Favorite_Name "Name [%s]" { channel="russound:zonefavorite:1:name" }
Switch Rio_Zone_Favorite_Valid "Valid [%s]" { channel="russound:zonefavorite:1:valid", autoupdate="false" }
String Rio_Zone_Favorite_Cmd "Command" { channel="russound:zonefavorite:1:cmd" }
String Rio_Zone_Favorite_Name2 "Name2 [%s]" { channel="russound:zonefavorite:2:name" }
Switch Rio_Zone_Favorite_Valid2 "Valid2 [%s]" { channel="russound:zonefavorite:2:valid", autoupdate="false" }
String Rio_Zone_Favorite_Cmd2 "Command2" { channel="russound:zonefavorite:2:cmd"  }

String Rio_Src_Bank_Name "Name [%s]" { channel="russound:bank:1:name" }

String Rio_Bank_Preset_Name "Name [%s]" { channel="russound:bankpreset:1:name" }
Switch Rio_Bank_Preset_Valid "Valid [%s]" { channel="russound:bankpreset:1:valid" }
String Rio_Bank_Preset_Name2 "Name2 [%s]" { channel="russound:bankpreset:2:name" }
Switch Rio_Bank_Preset_Valid2 "Valid2 [%s]" { channel="russound:bankpreset:2:valid" }

String Rio_Zone_Preset_Cmd "Command" { channel="russound:zonepreset:1:cmd"  }
String Rio_Zone_Preset_Cmd2 "Command2" { channel="russound:zonepreset:2:cmd"  }
```

.sitemap

```
Frame label="Russound" {
 Text label="System" {
  Text item=Rio_Version
  Text item=Rio_Status
  Selection item=Rio_Lang mappings=[ENGLISH="English", RUSSIAN="Russian", CHINESE="Chinese"]
  Switch item=Rio_AllOn
  Text label="Favorites" {
   Text item=Rio_Sys_Favorite_Name
   Text item=Rio_Sys_Favorite_Valid
   Text item=Rio_Sys_Favorite_Name2
   Text item=Rio_Sys_Favorite_Valid2
  }
 }
 Text label="Source 1" {
  Text label="Bank 1" {
   Text item=Rio_Src_Bank_Name
   Text label="Presets" {
    Text item=Rio_Bank_Preset_Name
    Text item=Rio_Bank_Preset_Valid
    Text item=Rio_Bank_Preset_Name2
    Text item=Rio_Bank_Preset_Valid2
   }
  }
 }
 
 Text label="Controller 1" {
  Text item=Rio_Ctl_Type
  Text item=Rio_Ctl_IPAddress
  Text item=Rio_Ctl_MacAddress
  
  Text label="Zone 1" {
   Text item=Rio_Zone_Name
   Switch item=Rio_Zone_Status
   Selection item=Rio_Zone_Source mappings=[1="Room1", 2="Room2", 3="Room3", 4="Room4"]
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
    Image item= Rio_Src_Cover
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
   
   Text label="Favorite" {
    Text item=Rio_Zone_Favorite_Name
    Text item=Rio_Zone_Favorite_Valid
    Selection item=Rio_Zone_Favorite_Cmd mappings=[savezone="Save Zone", restorezone="Restore Zone", deletezone="Delete Zone", savesys="Save System", restoresys="Restore System", deletesys="Delete System"]
    Text item=Rio_Zone_Favorite_Name2
    Text item=Rio_Zone_Favorite_Valid2
    Selection item=Rio_Zone_Favorite_Cmd2 mappings=[savezone="Save Zone", restorezone="Restore Zone", deletezone="Delete Zone", savesys="Save System", restoresys="Restore System", deletesys="Delete System"]
   }

   Text label="Preset" {
    Selection item=Rio_Zone_Preset_Cmd mappings=[save="Save", restore="Restore", delete="Delete"]
    Selection item=Rio_Zone_Preset_Cmd2 mappings=[save="Save", restore="Restore", delete="Delete"]
   }
  }
 }
}
```


