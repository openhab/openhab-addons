# Denon HEOS Binding

This binding support the HEOS-System from Denon.
The binding provides control of the players and groups within the network.
It also supports selecting favorites and play them on players or groups within the HEOS-Network.
The binding first establishes a connection to one of the players of the HEOS-Network and use them as a bridge.
After a connection is established, the binding searches for all available players and groups via the bridge.
To keep the network traffic low it is recommended to establish only one connection via one bridge.
Connection to the bridge is done via a Telnet connection on port 1255.

## Supported Things

Bridge:
The binding supports a bridge to connect to the HEOS-Network.
A bridge uses the thing ID "bridge".

Player:
A generic player is supported via this binding.
Currently no differences are made between the players.
A player uses the Thing ID "player"

Groups:
The binding supports HEOS groups.
A group uses the Thing ID "group"

## Discovery

This binding supports full automatic discovery of available players to be used as a bridge, players and groups.
You need to add a Bridge device first (which is also auto-discovered by the binding) which can be any HEOS device in your network (preferably which has wired connection).

**Important!**
Please note that only one bridge is required to establish a connection.
Adding a second bridge can cause trouble with the connection.

It is recommended to use the UI to setup the system and add all players and groups.
The bridge is discovered through UPnP in the local network.
Once it is added the players and groups are discovered via the bridge and placed in the Inbox.

## Binding Configuration

This binding does not require any configuration.

## Thing Configuration

### Bridge Configuration

The bridge has the following configuration parameter

| Parameter         | Description                                                 | Required  |
|-----------------  |------------------------------------------------------------ | --------- |
| ipAddress         | The network address of the Bridge                           | yes       |
| username          | The user name to login to the HEOS account                  | no        |
| password          | The password for the HEOS account                           | no        |
| heartbeat         | The time in seconds for the HEOS Heartbeat (default = 60 s) | no        |

The password and the user name are used to login to the HEOS account.
This is required to load the favorites, playlists and so on from personal settings.
If no login information is provided these features can't be used.

```java
Bridge heos:bridge:main "name" [ipAddress="192.168.0.1", username="xxx", password="123456"]
```

### Player Configuration

Player have the following configuration parameter

| Parameter         | Description                                                | Required  |
|-----------------  |----------------------------------------------------------- | --------- |
| pid               | The internal Player ID                                     | yes       |

For manual configuration a player can be defined as followed:

```java
Thing heos:player:player1 "name" [pid="123456789"]
```

PID behind the heos:player:--- should be changed as required.
It is recommended to use the Player PID.
If the PID isn't known it can be discovered by establishing a Telnet connection (port 1255) to one player and search for available players (Command: heos://player/get_players) within the network.
Another way is to use the UI to discover the Player via the bridge and get the PID.
For further details refer to the [HEOS CLI](https://rn.dmglobal.com/usmodel/HEOS_CLI_ProtocolSpecification-Version-1.17.pdf) specification.

### Group Configuration

Player have the following configuration parameter

| Parameter         | Description                                                                          | Required  |
|-----------------  |------------------------------------------------------------------------------------- | --------- |
| members           | The members of the groups. These are the player IDs. IDs have to be separated by ";" | yes       |

Groups will automatically appear in the Inbox if that Group is active.
To do this, build your Group from the HEOS app, then the group will appear in the Inbox.

```java
Thing heos:group:group1 "name" [members="45345634;35534567"]
```

### Defining Bridge and Players together

Defining Player and Bridge together.
To ensure that the players and groups are attached to the bridge the definition can be like:

```java
Bridge heos:bridge:main "Bridge" [ipAddress="192.168.0.1", username="userName", password="123456"] {
    player Kitchen "Kitchen"[pid="434523813"]
    player LivingRoom "Living Room"[pid="918797451"]
    group 813793755 "Ground Level"[members="434523813;918797451"]
}
```

## Channels

### Channels of Thing type 'player'

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
| CurrentPosition   | Number:Time   | Shows the current track position in seconds                           |
| Duration          | Number:Time   | The overall track duration in seconds                                 |
| Type              | String        | The type of the played media. Station or song for example             |
| Station           | String        | The station name if it is a station (Spotify shows track name....)    |
| PlayUrl           | String        | Plays a media file located at the URL                                 |
| Shuffle           | Switch        | Switches shuffle ON or OFF                                            |
| RepeatMode        | String        | Defines the repeat mode: Inputs are: "One" , "All" or "Off"           |
| Favorites         | String        | Plays a favorite. The selection options are retrieved automatically  |
| Playlists         | String        | Plays a playlist. The selection options are retrieved automatically   |
| Queue             | String        | Plays from the queue. The queue items are retrieved automatically     |
| ClearQueue        | Switch        | Clear the queue when turned ON                                        |

The `Favorites`, `Playlists`, `Queue` selection options are queried automatically from the HEOS system (if you set up any in the HEOS app).
This means the available options will be visible in a Selection, you don't have to specify them manually.
You can send commands to these channels from rules by sending the name of the selected item (For example: Starting a favorite radio channel from rule).

#### Example

```java
Player LivingRoom_Control "Control" {channel="heos:player:main:LivingRoom:Control"}
Selection item=LivingRoom_Playlists     label="Playlist" icon="music"
```

### Channels of Thing type 'group'

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
| CurrentPosition   | Number:Time   | Shows the current track position in seconds                           |
| Duration          | Number:Time   | The overall track duration in seconds                                 |
| Type              | String        | The type of the played media. Station or song for example             |
| Station           | String        | The station name if it is a station (Spotify shows track name....)    |
| Inputs            | String        | The input to be switched to. Input values from HEOS protocol          |
| PlayUrl           | String        | Plays a media file located at the URL                                 |
| Shuffle           | Switch        | Switches shuffle ON or OFF                                            |
| RepeatMode        | String        | Defines the repeat mode: Inputs are: "One" ; "All" or "Off"           |
| Favorites         | String        | Plays a favorite. The selection options are retrieved automatically  |
| Playlists         | String        | Plays a playlist. The selection options are retrieved automatically   |
| Queue             | String        | Plays from the queue. The queue items are retrieved automatically     |
| ClearQueue        | Switch        | Clear the queue when turned ON                                        |

The `Favorites`, `Playlists`, `Queue` selection options are queried automatically from the HEOS system (if you set up any in the HEOS app).
This means the available options will be visible in a Selection, you don't have to specify them manually.
You can send commands to these channels from rules by sending the name of the selected item (For example: Starting a favorite radio channel from rule).

### Available inputs

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

A current list can be found within the HEOS CLI protocol which can be found [here](https://rn.dmglobal.com/euheos/HEOS_CLI_ProtocolSpecification_2021.pdf).

### Channels of Thing type 'bridge'

| Channel ID            | Item Type     | Description                                                                                                                                               |
|---------------------- |-----------    |--------------------------------------------------------------------------------------------------------------------------------------------------------   |
| Reboot                | Switch        | Reboot the whole HEOS System. Can be used if you get in trouble with the system                                                                           |
| BuildGroup            | Switch        | Is used to define a group. The player which shall be grouped has to be selected first. If Switch is then activated the group is built.                    |

For a list of the commands please refer to the [HEOS CLI protocol](https://rn.dmglobal.com/euheos/HEOS_CLI_ProtocolSpecification_2021.pdf).

## _Dynamic Channels_

Also the bridge supports dynamic channels which represent the players of the network.
They are added dynamically if a player is found. The player and group channels are only shown on the bridge.

### Player Channels

| Channel ID    | Item Type     | Description                                                                                           |
|------------   |-----------    |-----------------------------------------------------------------------------------------------------  |
| {playerID}    | Switch        | A channel which represents the player. Please check via UI how the correct Channel Type looks like.   |

Example

 ```java
Switch Player_1 "Player [%s]" {channel="heos:bridge:main:P123456789"}
 ```

 The {playerUID} has either a P in front of the number which indicates that this is a player or a G to indicate this is a group.

## Full Example

### `demo.things` Example

```java
Bridge heos:bridge:main "Bridge" [ipAddress="192.168.0.1", username="userName", password="123456"] {
    player Kitchen "Kitchen"[pid="434523813"]
    player LivingRoom "Living Room"[pid="918797451"]
    group 813793755 "Ground Level"[members="434523813;918797451"]
}
```

### `demo.items` Example

```java
Player LivingRoom_Control "Control" {channel="heos:player:main:LivingRoom:Control"}
Switch LivingRoom_Mute "Mute"{channel="heos:player:main:LivingRoom:Mute"}
Dimmer LivingRoom_Volume "Volume" {channel="heos:player:main:LivingRoom:Volume"}
String LivingRoom_Title "Title [%s]" {channel="heos:player:main:LivingRoom:Title"}
String LivingRoom_Interpret "Interpret [%s]" {channel="heos:player:main:LivingRoom:Artist"}
String LivingRoom_Album "Album [%s]" {channel="heos:player:main:LivingRoom:Album"}
String LivingRoom_Favorites {channel="heos:player:main:LivingRoom:Favorites"}
String LivingRoom_Playlists {channel="heos:player:main:LivingRoom:Playlists"}
```

### demo.sitemap

```perl
Frame label="LivingRoom" {
    Default item=LivingRoom_Control
    Default item=LivingRoom_Mute
    Default item=LivingRoom_Volume
    Default item=LivingRoom_Title
    Default item=LivingRoom_Interpret
    Default item=LivingRoom_Album
    Selection item=LivingRoom_Favorites label="Favorite" icon="music"
    Selection item=LivingRoom_Playlists label="Playlist" icon="music"
}
```

## Detailed Explanation

This section gives some detailed explanations how to use the binding.

### Grouping Players

Players can be grouped via the binding.
The easiest way to do this is to use the created Group type Thing. To group them simply use the `Ungroup` channel on the Group. Switching this Switch ON and OFF will group and ungroup that Group.
The first player which is selected will be the Group leader.
Therefore changing play/pause and some other things at any player (which is included in that group) will also change that at the whole group.
Muting and Volume on the other hand can be changed individually for each Player also for the group leader.
If you want to change that for the whole group you have to do it via the Group thing.

### Inputs

To play inputs like the Aux_In it can be played at each player or group.
It is also possible to play an input from another player at the selected player.
To do so, first select the player channel of the player where the input is located (source) at the bridge.
Then use the input channel of the player where the source shall be played (destination) to activate the input.

#### Example

Player A = Kitchen (destination)
Player B = Living Room (source)

Items:

```java
Switch HeosBridge_Play_Living "Living Room" (gHeos) {channel="heos:bridge:ed0ac1ff-0193-65c6-c1b8-506137456a50:P918797451"}
String HeosKitchen_Input   (gHeos) {channel="heos:player:918797451:Inputs"}
String HeosKitchen_InputSelect "Input"  (gHeos)
```

Rule for kitchen:

```java
rule "Play AuxIn from Living Room"
when
    Item HeosKitchen_InputSelect received command
then
    if (receivedCommand.toString == "aux_in_1") {
        sendCommand(HeosKitchen_Input, "aux_in_1")
    }
    if (receivedCommand.toString == "LivingRoom") {
        sendCommand(HeosBridge_Play_Living, ON)
        sendCommand(HeosKitchen_Input, "aux_in_1")
        sendCommand(HeosBridge_Play_Living, OFF) //Switch player channel off again to be sure that it is OFF
    }
end
```

Sitemap:

```java
Switch item=HeosKitchen_InputSelect mappings=[aux_in_1 = "Aux In" , LivingRoom = "Living Room"]
```

### The Online Status of Groups and Players

The online state of a Thing can be helpful for groups to control the visibility of group items within sitemap.
So if the group is removed the visibility of those items is also changed.

#### Groups and Players Example

First you have to define a new Item within the Item section which is used later within the Sitemap:

Items:

```java
String HeosGroup_Status
```

Then we need a rule which triggers the state if an Item goes Online or Offline.

Rules:

```java
rule "Online State Heos Group"

when
    Thing "heos:group:1747557118" changed
then
    var thingStatus = getThingStatusInfo("heos:group:1747557118")
    sendCommand(HeosGroup_Status, thingStatus.getStatus.toString)
end

```

Sitemap:

```perl
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

## Rule Actions

Multiple actions are supported by this binding. In classic rules these are accessible as shown in the example below:

```java
 val actions = getActions("heos","heos:bridge:bridgeId")
 if(null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
 } else {
        actions.playInputFromPlayer(-3213214, "aux_in_1", 89089081)
 }
```

### playInputFromPlayer(sourcePlayer, sourceInput, destination)

Allows to play a source from a player to another player.
