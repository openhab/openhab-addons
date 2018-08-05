# Denon HEOS Binding

This binding support the HEOS-System from Denon for openHAB 2. The binding provides control of the players and groups within the network. It also supports selecting favorites and play them on players or groups within the HEOS-Network. 
The binding first establishes a connection to one of the players of the HEOS-Network and use them as a bridge. After a connection is established, the binding searches for all available players and groups via the bridge. To keep the network traffic low it is recommended to establish only one connection via one bridge. Connection to the bridge is done via a Telnet connection.

#### A detailed explanation of binding possibilities and the handling can be found at the end. Also some examples are provided how the binding can be used

## Supported Things

Bridge:
The binding supports a bride for connecting to the HEOS-Network

Player:
A generic player is supported via this binding. Currently no differences are made between the players.

Groups:
Groups are supported by this binding.


## Discovery

This binding supports full automatic discovery of available players to be used as a bridge, players and groups (both after establishing a connection via a bridge). It is recommended to use the PaperUI or other GUI to setup the system and add all players and groups.
The bridge is discovered through UPnP in the local network. Once it is added the players and groups are discovered via the bridge and placed within the inbox.
Nethertheless also manual configuration is possible

## Binding Configuration

This binding does not require any configuration via a .cfg file. The configuration is done via the Thing definition.
Within the binding the callback URL can be set. This URL is needed if a player or a group is registered as an audio sink within openHAB. If a device is registered as an audio sink notification sounds will be played at this device.
For further information about notifications also refer to the [openHAB documentation](http://docs.openHAB.org/configuration/multimedia.html).

The URL defines the base address where the sound files are located. For example if the files are placed within the openHAB directory \openHAB-conf\sounds the callback address has be set to the openHAB server address inclusive the port.  

Example: http://192.168.0.7:8080 if the sound file can be accessed there, or if the sound file is located on the openHAB server http://localhost:8080 for example.


## Thing Configuration

**It is recommended to configure the things via the PaperUI or HABmin**

### Bridge Configuration

The bridge can be added via the PaperUI. After adding the bridge, the user name and password can be set by editing the thing via the PaperUI. For manual configuration the following parameters can be defined. The ipAddress has to be defined. All other fields are optional.
The password and the user name are used to login to the HEOS account. This is required to load the favorites, playlists and so on from personal settings. If no login information is provided these features can't be used.  

````
Bridge heos:bridge:main "name" [ipAddress="192.168.0.1", name="Default", unserName="xxx", password="123456"]  
````

### Player Configuration

Players can be added via the PaperUI. All fields are then filled automatically.
For manual configuration a player is defined as followed:

````
Thing heos:player:pid "name" [pid="123456789", name="name", model="modelName", ipAddress="192.168.0.xxx"] 
````

PID behind the heos:player:--- should be changed as required. Every name or value can be used. It is recommended to use the player PID. Within the configuration the PID is mandatory. The rest is not required.
If the PID isn't known it can be discovered by establishing a TelNet connection (port 1255) to one player and search for available players
(Command: heos://player/get_players)
within the network. For further details refer to the [HEOS CLI](http://rn.dmglobal.com/euheos/HEOS_CLI_ProtocolSpecification.pdf) specification.


*If player is configured by PaperUI the UID of the player is equal to the player ID (PID) from the HEOS system*

### Group Configuration

```
Thing heos:group:memberHash "name" [gid="123456789", name="name", model="modelName", ipAddress="192.168.0.xxx", memberHash="123456789"] 
```

*If group is configured by PaperUI the group UID is calculated by the Hash value of the group members*

Required fields are the GID which is the PID of the group leading player.  

### Defining Bridge and Players together

Defining Player and Bridge together. To ensure that the players and groups are attached to the bridge the definition can be like:

```
Bridge heos:bridge:main "Bridge" [ipAddress="192.168.0.1", name="Bridge", userName="userName", password="123456"] {
	
	player Kitchen "Kitchen"[pid="434523813", name="Kitchen", type="Player"]
	player LivingRoom "Living Room"[pid="918797451", name="Living Room", type="Player"]
  	player 813793755 "Bath Room"[pid="813793755", name="Bath Room", type="Player"]
	
}
```

## Channels

Note:
The channels have different paths if you configure our Things manual or via an UI. It is recommended to check the correct path via an UI.


### Player provide the following channels:

| Channel ID        | Item Type     | Description                                                           |
|-----------------  |-----------    |---------------------------------------------------------------------  |
| Control           | Player        | Play (also ON) / Pause (also OFF) / Next / Previous                   |
| Volume            | Dimmer        | Volume control / also accepts "DECREASE" & "INCREASE"                 |
| Mute              | Switch        | Mute the Player                                                       |
| Title             | String        | Song Title                                                            |
| Artist            | String        | Song Artist                                                           |
| Album             | String        | Album Title                                                           |
| Cover             | Image         | The cover of the actual song                                          |
| Inputs            | String        | The input to be switched to. Input values from HEOS protocol          |
| CurrentPosition   | Number        | Shows the current track position in seconds                           |
| Duration          | Number        | The overall track duration in seconds                                 |
| Type              | String        | The type of the played media. Station or song for example             |
| Station           | String        | The station name if it is a station (Spotify shows track name....)    |
| PlayUrl           | String        | Plays a media file located at the URL                                 |
| Shuffle           | Switch        | Switches shuffle ON or OFF                                            |
| RepeatMode        | String        | Defines the repeat mode: Inputs are: "One" ; "All" or "Off"           |
| Playlists         | String        | Plays a playlist. Playlists are identified by numbers (starting at 0!). List can be found in the HEOS App            |



####Example:

```
Player LivingRoom_Control "Control" {channel="heos:player:main:LivingRoom:Control"}
```

### Groups provide the following channels:

| Channel ID        | Item Type     | Description                                                           |
|-----------------  |-----------    |--------------------------------------------------------------------   |
| Control           | Player        | Play (also ON) / Pause (also OFF) / Next / Previous                   |
| Volume            | Dimmer        | Volume control / also accepts "DECREASE" & "INCREASE"                 |
| Mute              | Switch        | Mute the Group                                                        |
| Title             | String        | Song Title                                                            |
| Artist            | String        | Song Artist                                                           |
| Album             | String        | Album Title                                                           |
| Ungroup           | Switch        | Deletes the group (OFF) or generate the group again (ON)              |
| Cover             | Image         | The cover of the actual song                                          |
| CurrentPosition   | Number        | Shows the current track position in seconds                           |
| Duration          | Number        | The overall track duration in seconds                                 |
| Type              | String        | The type of the played media. Station or song for example             |
| Station           | String        | The station name if it is a station (Spotify shows track name....)    |
| Inputs            | String        | The input to be switched to. Input values from HEOS protocol          |
| PlayUrl           | String        | Plays a media file located at the URL                                 |
| Shuffle           | Switch        | Switches shuffle ON or OFF                                            |
| RepeatMode        | String        | Defines the repeat mode: Inputs are: "One" ; "All" or "Off"           |
| Playlists         | String        | Plays a playlist. Playlists are identified by numbers (starting at 0!). List can be found in the HEOS App            |
#### Inputs depending on Player type (Date 12.02.2017):

| Input names   |
|-------------- |
| aux_in_1      |
| aux_in_2      |
| aux_in_3      |
| aux_in_4      |
| aux1          |
| aux2          |
| aux3          |
| aux4          |
| aux5          |
| aux6          |
| aux7          |
| line_in_1     |
| line_in_2     |
| line_in_3     |
| line_in_4     |
| coax_in_1     |
| coax_in_2     |
| optical_in_1  |
| optical_in_2  |
| hdmi_in_1     |
| hdmi_arc_1    |
| cable_sat     |
| dvd           |
| bluray        |
| game          |
| mediaplayer   |
| cd            |
| tuner         |
| hdradio       |
| tvaudio       |
| phono         |

An actual list can be found within the HEOS CLI protocol which can be found [here](http://rn.dmglobal.com/euheos/HEOS_CLI_ProtocolSpecification.pdf).

### The Bridge provides the following channels:

| Channel ID            | Item Type     | Description                                                                                                                                               |
|---------------------- |-----------    |--------------------------------------------------------------------------------------------------------------------------------------------------------   |
| Reboot                | Switch        | Reboot the whole HEOS System. Can be used if you get in trouble with the system                                                                           |
| DynamicGroupHandling  | Switch        | If this option id activated the system automatically removes groups if they are ungrouped. Only works if the group is added via an UI.                    |
| BuildGroup            | Switch        | Is used to define a group. The player which shall be grouped has to be selected first. If Switch is then activated the group is build.                    |
| RawCommand            | String        | A channel where every HEOS CLI command can be send to.                                                                                                    |
| PlayUrl               | String        | Plays a media file located at the URL. First select the player channel where the stram shall be played. Then send the stream via the Play URL channel.    |

For a list of the commands please refer to the [HEOS CLI protocol](http://rn.dmglobal.com/euheos/HEOS_CLI_ProtocolSpecification.pdf).

#### RawCommand example

```
heos://player/get_player_info?pid=975314685
```

At the moment no feedback is provided which can be used. Result of the command can be seen on the DEBUG level at the console




## *Dynamic Channels*

Also the bridge supports dynamic channels which represent the players of the network and the favorites. They are added dynamically if a player is found and if favorites are defined within the HEOS Account. To activate Favorites the system has to be signed in to the HEOS Account.


### Favorite Channels

| Channel ID    | Item Type     | Description                                                                                               |
|------------   |-----------    |-------------------------------------------------------------------------------------------------------    |
| {mid}         | Switch        | A channel which represents the favorite. Please check via UI how the correct Channel Type looks like.     |
 
 Example

 ```
 Switch Favorite_1 "Fav 1 [%s]" {channel="heos:bridge:main:s17492"}
 ```

### Player Channels

| Channel ID    | Item Type     | Description                                                                                           |
|------------   |-----------    |-----------------------------------------------------------------------------------------------------  |
| {playerID}    | Switch        | A channel which represents the player. Please check via UI how the correct Channel Type looks like.   |

Example

 ```
 Switch Player_1 "Player [%s]" {channel="heos:bridge:main:P123456789"}  
 ```

 The {playerUID} has either a P in front of the number which indicates that this is a player or a G to indicate this is a group.
 
 
 **Note: It seems at the moment that the dynamic channels are only work correctly if things are managed via UI.**

## Full Example

###demo.things:

```
Bridge heos:bridge:main "Bridge" [ipAddress="192.168.0.1", name="Bridge", userName="userName", password="123456"] {
	
	player Kitchen "Kitchen"[pid="434523813", name="Kitchen", type="Player"]
	player LivingRoom "Living Room"[pid="918797451", name="Living Room", type="Player"]
  	player 813793755 "Bath Room"[pid="813793755", name="Bath Room", type="Player"]
	
}
```

###demo.items:

```
Player LivingRoom_Control "Control" {channel="heos:player:main:LivingRoom:Control"}
Switch LivingRoom_Mute "Mute"{channel="heos:player:main:LivingRoom:Mute"}
Dimmer LivingRoom_Volume "Volume" {channel="heos:player:main:LivingRoom:Volume"}
String LivingRoom_Title "Title [%s]" {channel="heos:player:main:LivingRoom:Title"}
String LivingRoom_Interpret "Interpret [%s]" {channel="heos:player:main:LivingRoom:Interpret"}
String LivingRoom_Album "Album [%s]" {channel="heos:player:main:LivingRoom:Album"}
```

###demo.sitemap

```
   Frame label="LivingRoom" {
    	Default item=LivingRoom_Control
    	Default item=LivingRoom_Mute
    	Default item=LivingRoom_Volume
    	Default item=LivingRoom_Title
    	Default item=LivingRoom_Interpret
    	Default item=LivingRoom_Album
    }
```

## Detailed Explanation

This section gives some detailed explanations how to use the binding.

### Grouping Players

Players can be grouped via the binding. To do so, select the player channels of the players you want to group at the bridge and then use the "Made Group" channel to create the group. The first player which is selected will be the group leader. The group GID then is the same as the PID of the group leader. Therefore changing play pause and some other things at the leading player will also change that at the whole group. Muting and Volume on the other hand can be changed individually for each player also for the group leader. If you want to change that for the whole group you have to do it via the group thing.

### Inputs

To play inputs like the Aux_In it can be played at each player or group. It is also possible to play an input from an other player at the selected player. To do so, first select the player channel of the player where the input is located (source) at the bridge. Then use the input channel of the player where the source shall be played (destination) to activate the input.

#### Example

Player A = Kitchen (destination)
Player B = Living Room (source)

Items:

```
Switch HeosBridge_Play_Living	"Living Room"	(gHeos)	{channel="heos:bridge:ed0ac1ff-0193-65c6-c1b8-506137456a50:P918797451"}
String HeosKitchen_Input			(gHeos) {channel="heos:player:918797451:Inputs"}
String HeosKitchen_InputSelect	"Input"		(gHeos)	
```

Rule for kitchen:

```
rule "Play AuxIn from Living Room"
	when
		Item HeosKitchen_InputSelect received command
	then
		if (receivedCommand.toString == "aux_in_1") {
			sendCommand(HeosKitchen_Input, "aux_in_1")
			
		} if (receivedCommand.toString == "LivingRoom") {
			sendCommand(HeosBridge_Play_Living, ON)
			sendCommand(HeosKitchen_Input, "aux_in_1")
			sendCommand(HeosBridge_Play_Living, OFF)	//Switch player channel off again to be sure that it is OFF
		}
```

Sitemap:

```
Switch item=HeosKitchen_InputSelect	mappings=[aux_in_1 = "Aux In" , LivingRoom = "Living Room"]
```

### The OnlineState of Groups and Players

The online state of a Thing can be helpful for groups to control the visibility of group items within sitemap. So if the group is removed the visibility of those items is also changed.

#### Example

First you have to define a new Item within the Item section which is used later within the Sitemap:

Items:

```
String HeosGroup_Status

```

Then we need a rule which triggers the state if an Item goes Online or Offline.

Rules:

```
rule "Online State Heos Group" 

when
    Thing "heos:group:1747557118" changed 
then
    var thingStatus = getThingStatusInfo("heos:group:1747557118")
    sendCommand(HeosGroup_Status, thingStatus.getStatus.toString)    
end

```

Sitemap:

```
Frame label="Heos Group" visibility=[HeosGroup_Status==ONLINE] {
	
	Default item=HeosGroup1_Player
	Default item=HeosGroup1_Volume
	Default item=HeosGroup1_Mute
	Default item=HeosGroup1_Favorites
	Default item=HeosGroup1_Playlist
	
	Text item=HeosGroup1_Song {
		Default item=HeosGroup1_Song
		Default item=HeosGroup1_Artist
		Default item=HeosGroup1_Album
		Image item=HeosGroup1_Cover url=""
	}
	
}
```

### Dynamic Group Handling (Keep Groups)

Groups are a more or less dynamic item compared to the players. The players are always present within the network and are only added or removed if they are added or removed physically. Groups on the other hand are dynamic because they are only virtual created and removed. 
Therefore dynamic group handling only sets groups to OFFLINE if the option is activated. This is indicated via the "OnlineStatus" channel of the group. If the group then is created again the binding automatically sets the status to online without the need of action by the user.
If dynamic group handling is deactivated the group thing is completely removed if the binding is notified about a removed group. The group then has to be added manually again if created.


### Playlists

Playlists can be played by sending the number (starts at 0!) to the binding via the playlists channel at the corresponding player or group. To find the correct number for the playlist, please have a look to the HEOS App and see at which position the playlist you want to play is located.

#### Example

Items:

```
String HeosKitchen_Playlist		"Playlists" (gHeos) {channel="heos:player:918797451:Playlists"}

```

Sitemap:

```
Switch item=HeosKitchen_Playlists  	mappings=[0="San Glaser", 1="Classic", 2="Beasty Boys"]
```