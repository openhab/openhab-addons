# Denon HEOS Binding

HEOS Binding for OpenHab

This binding support the HEOS-System from Denon for OpenHab 2. The binding provides basic control for the player and groups of the network. It also supports selecting favorites and play them on several players or groups on the HEOS-Network. 
The binding first establish a connection to one of the player of the HEOS-Network and use them as a bridge. After a connection is established, the binding searches for all player and grouped via the bridge. To keep the network traffic low it is recommended to establish only one bridge. Connection to the bridge is done via a Telnet connection.

#### A detailed explanation of binding possibilities and the handling can be found at the end. Also some examples are provided how the binding can be used

## Supported Things

Bridge:
The binding support a bride for connecting to the HEOS-Network

Player:
A generic player is supported via this binding. Currently no differences are made between the players. May be introduced in the future

Groups:
Groups are supported by this binding.


## Discovery

This binding support full automatic discovery of bridges, players and groups. It is recommended to use the PaperUI to setup the system and add all players and groups.
The bridge is discovered through UPnP in the local network. Once it is added the players and groups are read via the bridge and placed within the inbox.
Nether the less also manual configuration is possible

## Binding Configuration

This binding does not require any configuration via a .cfg file. The configuration is done via the Thing definition.
Within the binding the callback URL can be set. This URL is needed if a player or a group is registered as an audio sink within OpenHab.
The URL is in most of the cases the URL if the OpenHab server. Also the port has to be defined.
Example: http://192.168.0.7:8080 or if the sound file is located on the OpenHab server http://localhost:8080


## Thing Configuration

**It is recommended to configure the things via the PaperUI or HABmin**

### Bridge Configuration

The bridge can be added via the PaperUI. After adding the bridge the user name and password can set by editing the thing via the PaperUI. For manual configuration the following parameter can be defined. The ipAddress has to be defined. All other fields are optional.

````
Bridge heos:bridge:main "name" [ipAddress="192.168.0.1", name="Default", unserName="xxx", password="123456"]  
````

### Player Configuration

Player can be added via the PaperUI. All fields are then filled automatically.
For manual configuration the player is defined as followed:

````
Thing heos:player:pid "name" [pid="123456789", name="name", model="modelName", ipAdress="192.168.0.xxx", type="Player"] 
````

PID behind the heos:player:--- should be changed as required. Every name or value can be used. It is recommended to use the player PID. Within the configuration the PID as well as the type field is mandatory. The rest is not required.

*If player is configured by PaperUI the UID of the player is equal to the player ID (PID) from the HEOS system*

### Group Configuration

```
Thing heos:group:memberHash "name" [gid="123456789", name="name", model="modelName", ipAdress="192.168.0.xxx", type="Group", memberHash="123456789"] 
```

*If group is configured by PaperUI the group UID is calculated by the Hash value of the group members*

Required fields are the GID which is the PID of the group leading player and the type which is Group here.  

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
The channel have different paths if you configure our Things manual or via an UI. It is recommended to check the correct path via an UI.


### Player provide the following channels:

Channel ID | Item Type | Description
----------------|-----------|-------------
Control | Player | Play (also ON) / Pause (also OFF) / Next / Previous
Volume | Dimmer | Volume control
Mute | Switch | Mute the Player
Title | String | Song Title
Interpret | String | Song Interpret
Album | String  | Album Title
Image_URL | String |The URL where the cover can be found 
Inputs | String | The input to be switched to. Input values from HEOS protocol
OnlineStatus | String | Indicates the status ONLINE or OFFLINE
CurrentPosition | String | Shows the current trakc position in milliseconds
Duration | String | The overall track duration in milliseconds
Type | String | The type of the played media. Station or song for example
Station | String | The station name if it is a station (Spotify shows track name....)
PlayURL | String | Plays a media file located at the URL


####Example:

```
Player LivingRoom_Control "Control" {channel="heos:player:main:LivingRoom:Control"}
```

### Groups provide the following channels:

Channel ID | Item Type | Description
----------------|-----------|-------------
Control | Player | Play (also ON) / Pause (also OFF) / Next / Previous
Volume | Dimmer | Volume control
Mute | Switch | Mute the Group
Title | String | Song Title
Interpret | String | Song Interpret
Album | String  | Album Title
Ungroup | Switch | Deletes the group (OFF) or generate the group again (ON) 
Image_URL | String |The URL where the cover can be found
OnlineStatus | String | Indicates the status ONLINE or OFFLINE
CurrentPosition | String | Shows the current track position in milliseconds
Duration | String | The overall track duration in milliseconds
Type | String | The type of the played media. Station or song for example
Station | String | The station name if it is a station (Spotify shows track name....)
Inputs | String | The input to be switched to. Input values from HEOS protocol
PlayURL | String | Plays a media file located at the URL

#### Inputs depending on Player type (Date 12.02.2017):

Input names |
----------------|
aux_in_1 /
aux_in_2 /
aux_in_3 /
aux_in_4 /
aux1 /
aux2 /
aux3 /
aux4 /
aux5 /
aux6 /
aux7 /
line_in_1 /
line_in_2 /
line_in_3 /
line_in_4 /
coax_in_1 /
coax_in_2 /
optical_in_1 /
optical_in_2 /
hdmi_in_1 /
hdmi_arc_1 /
cable_sat /
dvd /
bluray /
game /
mediaplayer /
cd /
tuner /
hdradio /
tvaudio /
phono /



### The Bridge provides the following channels:

Channel ID | Item Type | Description
----------------|-----------|-------------
Reboot | Switch | Reboot the whole HEOS System. Can be used if you get in trouble with the system
DynamicGroupHandling | Switch | If this option id activated the system automatically removes groups if they are ungrouped. Only works if the group is added via an UI.
BuildGroup | Switch | Is used to define a group. The player which shall be grouped has to be selected first. If Switch is then activated the group is build.
Playlists | String | Plays a playlist on the prior selected Player Channel (see below) Playlists are identified by numbers. List can be found in the HEOS App
RawCommand | String | A channel where every HEOS CLI command can be send to.
PlayURL | String | Plays a media file located at the URL. First select the player channel where the stram shall be played. Then send the stream via the Play URL channel.

#### RawCommand example

```
heos://player/get_player_info?pid=975314685
```

At the moment no feedback is provided which can be used. Result of the command can be seen on the DEBUG level at the console




## *Dynamic Channels*

Also the bridge supports dynamic channels which represent the player of the network and the favorites. They are dynamically added if player are found and if favorites are defined within the HEOS Account. To activate Favorites the system has to be signed in to the HEOS Account.


### Favorite Channels

Channel ID | Item Type | Description
----------------|-----------|-------------
 {mid} | Switch | A channel which represents the favorite. Please check via UI how the correct Channel Type looks like. (Experimental)
 
 Example

 ```
 Switch Favorite_1 "Fav 1 [%s]" {channel="heos:bridge:main:s17492"}
 ```

### Player Channels

Channel ID | Item Type | Description
----------------|-----------|-------------
{playerID} | Switch | A channel which represents the player. Please check via UI how the correct Channel Type looks like. (Experimental)

Example

 ```
 Switch Player_1 "Player [%s]" {channel="heos:bridge:main:P123456789"}  
 ```

 The {playerUID} has either a P before the number which indicates that this is a player or a G to indicate this is a group.
 
 
 **Note: Both functions are experimental. It seems at the moment that the dynamic channels are only work correctly if things are managed via UI.**

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

Players can be grouped via the binding. To do so, select the player channels of the players you want to group at the bridge and then use the "Made Group" channel to create the group. The first player which is selected will be the group leader. The group GID then is the same as the PID of the group leader. Therefore changing play pause and some other things at the leading player will also change that at the group. Muting and the Volume on the other hand can be changed individually for each player also for the group leader. If you want to change that for the whole group you have to do it via the group thing.

### Inputs

To play inputs like the Aux In can be played at eacht player or group. It is also possible to play an input from an other player at the selected player. To do so, first select the player channel of the player where the input is located (source) at the bridge. Then use the input channel of the player where the source shall be played (destination) to activate the input.

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

### The OnlineState

The online state shows if an item is online or offline. This can be helpful for groups to control the visibility of group items within the sitemap. So if the group is removed the visibility of those items is also changed.,

#### Example

Sitemap:

```
Frame label="Heos Group" visibility=[HeosGroup1_Status==ONLINE] {
	
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

Playlists can be played by sending the number (starts at 0!) to the binding via the playlists channel at the bridge. To find the correct number for the playlist have a look to the HEOS App and see at which position the playlist you want to play is located.
To tell the binding at which player or group the playlist shall be played, select the related player or group channel at the bridge prior sending the command to the playlist channel.
This can be done by a rule within openHab

#### Example

Items:

```
Switch HeosBridge_Play_Kitchen		"Kitchen"   (gHeos) {channel="heos:bridge:ed0ac1ff-0193-65c6-c1b8-506137456a50:P918797451"}
String HeosBridge_Playlists		"Playlists" (gHeos) {channel="heos:bridge:ed0ac1ff-0193-65c6-c1b8-506137456a50:Playlists"}
Number HeosKitchen_Playlist		"Playlist"  (gHeos)
```

Rule:

```
rule"Playlist"
	when
		Item HeosKitchen_Playlist received command
	then	
		sendCommand(HeosBridge_Play_Kitchen, ON)
		sendCommand(HeosBridge_Playlists, receivedCommand.toString)
		sendCommand(HeosBridge_Play_Kitchen, OFF)		
	end
```

Sitemap:

```
Switch item=HeosKitchen_Playlist  	mappings=[0="San Glaser", 1="Classic", 2="Beasty Boys"]
```

### 


