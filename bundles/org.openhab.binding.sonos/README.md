# Sonos Binding

This binding integrates the [Sonos Multi-Room Audio system](http://www.sonos.com).

**Attention:** 
You might run into trouble if your control system (the binding) is in another subnet than your Sonos device. 
Sonos devices make use of multicast which in most cases needs additional router configuration outside of a single subnet.   
If you observe communication errors (COMMUNICATION_ERROR/not registered), you might need to configure your router to increase the TTL of the packets send by your Sonos device. 
This happens because of a TTL=1 for ALIVE packets send by Sonos devices, resulting in dropped packets after one hop.

## Supported Things

All available Sonos (playback) devices are supported by this binding. This includes the One, Play:1, Play:3, Play:5, Connect, Connect:Amp, Playbar, Playbase, Beam and Sub. The Bridge and Boost are not supported, but these devices do only have an auxiliary role in the Sonos network and do not have any playback capability. All supported Sonos devices are registered as an audio sink in the framework.

When being defined in a \*.things file, the specific thing types One, PLAY1, PLAY3, PLAY5, PLAYBAR, PLAYBASE, Beam, CONNECT and CONNECTAMP should be used.

Please note that these thing types are case sensitive (you need to define them **exactly as stated above**).

## Discovery

The Sonos devices are discovered through UPnP in the local network and all devices are put in the Inbox. Beware that all Sonos devices have to be added to the local Sonos installation as described in the Sonos setup procedure, e.g. through the Sonos Controller software or smartphone app.

## Binding Configuration

The binding has the following configuration options, which can be set for "binding:sonos":

| Parameter   | Name             | Description                                                              | Required |
|-------------|------------------|--------------------------------------------------------------------------|----------|
| opmlUrl     | OPML Service URL | URL for the OPML/tunein.com service                                      | no       |
| callbackUrl | Callback URL     | URL to use for playing notification sounds, e.g. http://192.168.0.2:8080 | no       |

## Thing Configuration

The Sonos Thing requires the UPnP UDN (Unique Device Name) as a configuration value in order for the binding to know how to access it.
All the Sonos UDN have the "RINCON_000E58D8403A0XXXX" format (value to be found via Sonos item in the PaperUI Inbox).
Additionally, a refresh interval, used to poll the Sonos device, can be specified (in seconds).
You can use the `notificationVolume` property for setting a default volume (in percent) to be used to play notifications.
In the thing file, this looks e.g. like

```
Thing sonos:PLAY1:1 [udn="RINCON_000E58D8403A0XXXX", refresh=60, notificationVolume=25]
```

## Channels

The devices support the following channels:

| Channel Type ID     | Item Type | Access Mode | Description                                                                                                                                               | Thing types                          |
|---------------------|-----------|-------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------|
| add                 | String    | W           | Add the given Zone Player to the group of this Zone Player                                                                                                | all                                  |
| alarm               | Switch    | W           | Set the first occurring alarm either ON or OFF. Alarms first have to be defined through the Sonos Controller app                                          | all                                  |
| alarmproperties     | String    | R           | Properties of the alarm currently running                                                                                                                 | all                                  |
| alarmrunning        | Switch    | R           | Set to ON if the alarm was triggered                                                                                                                      | all                                  |
| clearqueue          | Switch    | W           | Suppress all songs from the current queue                                                                                                                 | all                                  |
| control             | Player    | RW          | Control the Zone Player, e.g. start/stop/next/previous/ffward/rewind                                                                                      | all                                  |
| coordinator         | String    | R           | UDN of the coordinator for the current group                                                                                                              | all                                  |
| currentalbum        | String    | R           | Name of the album currently playing                                                                                                                       | all                                  |
| currentalbumart     | Image     | R           | Cover art of the album currently playing                                                                                                                  | all                                  |
| currentalbumarturl  | String    | R           | Cover art URL of the album currently playing                                                                                                              | all                                  |
| currentartist       | String    | R           | Name of the artist currently playing                                                                                                                      | all                                  |
| currenttitle        | String    | R           | Title of the song currently playing                                                                                                                       | all                                  |
| currenttrack        | String    | R           | Name of the current track or radio station currently playing                                                                                              | all                                  |
| currenttrackuri     | String    | R           | URI of the current track                                                                                                                                  | all                                  |
| currenttransporturi | String    | R           | URI of the current AV transport                                                                                                                           | all                                  |
| favorite            | String    | W           | Play the given favorite entry. The favorite entry has to be predefined in the Sonos Controller app                                                        | all                                  |
| led                 | Switch    | RW          | Set or get the status of the white led on the front of the Zone Player                                                                                    | all                                  |
| linein              | Switch    | R           | Indicator set to ON when the line-in of the Zone Player is connected                                                                                      | PLAY5, CONNECT, CONNECTAMP, PLAYBASE |
| localcoordinator    | Switch    | R           | Indicator set to ON if the this Zone Player is the Zone Group Coordinator                                                                                 | all                                  |
| mute                | Switch    | RW          | Set or get the mute state of the master volume of the Zone Player                                                                                         | all                                  |
| nightmode           | Switch    | RW          | Enable or disable the night mode feature                                                                                                                  | PLAYBAR, PLAYBASE, Beam              |
| notificationsound   | String    | W           | Play a notification sound by a given URI                                                                                                                  | all                                  |
| playlinein          | String    | W           | This channel supports playing the audio source connected to the line-in of the zoneplayer identified by the Thing UID or UPnP UDN provided by the String. | PLAY5, CONNECT, CONNECTAMP, PLAYBAR, PLAYBASE, Beam |
| playlist            | String    | W           | Play the given playlist. The playlist has to predefined in the Sonos Controller app                                                                       | all                                  |
| playqueue           | Switch    | W           | Play the songs from the current queue                                                                                                                     | all                                  |
| playtrack           | Number    | W           | Play the given track number from the current queue                                                                                                        | all                                  |
| playuri             | String    | W           | Play the given URI                                                                                                                                        | all                                  |
| publicaddress       | Switch    | W           | Put all Zone Players in one group, and stream audio from the line-in from the Zone Player that triggered the command                                      | all                                  |
| radio               | String    | W           | Play the given radio station. The radio station has to be predefined in the Sonos Controller app                                                          | all                                  |
| remove              | String    | W           | Remove the given Zone Player from the group of this Zone Player                                                                                           | all                                  |
| repeat              | String    | RW          | Repeat the track or queue playback. The accepted values are OFF, ONE and ALL                                                                              | all                                  |
| restore             | Switch    | W           | Restore the state of the Zone Player                                                                                                                      | all                                  |
| restoreall          | Switch    | W           | Restore the state of all the Zone Players                                                                                                                 | all                                  |
| save                | Switch    | W           | Save the state of the Zone Player                                                                                                                         | all                                  |
| saveall             | Switch    | W           | Save the state of all the Zone Players                                                                                                                    | all                                  |
| shuffle             | Switch    | RW          | Shuffle the queue playback                                                                                                                                | all                                  |
| sleeptimer          | Number    | RW          | Set/show the duration of the SleepTimer in seconds                                                                                                        | all                                  |
| snooze              | Number    | W           | Snooze the running alarm, if any, with the given number of minutes                                                                                        | all                                  |
| speechenhancement   | Switch    | RW          | Enable or disable the speech enhancement feature                                                                                                          | PLAYBAR, PLAYBASE, Beam              |
| standalone          | Switch    | W           | Make the Zone Player leave its Group and become a standalone Zone Player                                                                                  | all                                  |
| state               | String    | R           | The State channel contains state of the Zone Player, e.g. PLAYING, STOPPED, ...                                                                           | all                                  |
| stop                | Switch    | W           | Write `ON` to this channel: Stops the Zone Player player.                                                                                                 | all                                  |
| tuneinstationid     | String    | RW          | Provide the current TuneIn station id or play the TuneIn radio given by its station id                                                                    | all                                  |
| volume              | Dimmer    | RW          | Set or get the master volume of the Zone Player                                                                                                           | all                                  |
| zonegroupid         | String    | R           | Id of the Zone Group the Zone Player belongs to                                                                                                           | all                                  |
| zonename            | String    | R           | Name of the Zone associated to the Zone Player                                                                                                            | all                                  |

## Audio Support

All supported Sonos devices are registered as an audio sink in the framework.
Audio streams are treated as notifications, i.e. they are fed into the `notificationsound` channel.
The `notificationsound` channel change the volume of the audio sink to the value defined in the `notificationVolume` property of the thing and restores it after finished playing.
Note that the Sonos binding has a limit of 20 seconds for notification sounds.
Any sound that is longer than that will be cut off.

URL audio streams (e.g. an Internet radio stream) are an exception and do not get sent to the `notificationsound` channel.
Instead, these will be sent to the `playuri` channel.

## Full Example

demo.things:

```
Thing sonos:PLAY1:living [ udn="RINCON_000E58D8403A0XXXX", refresh=60]
```

demo.items:

```
Group Sonos <player>

Player Sonos_Controller   "Controller"                          (Sonos) {channel="sonos:PLAY1:living:control"}
Dimmer Sonos_Volume       "Volume [%.1f %%]" <soundvolume>      (Sonos) {channel="sonos:PLAY1:living:volume"}
Switch Sonos_Mute         "Mute"             <soundvolume_mute> (Sonos) {channel="sonos:PLAY1:living:mute"}
Switch Sonos_LED          "LED"              <switch>           (Sonos) {channel="sonos:PLAY1:living:led"}
String Sonos_CurrentTrack "Now playing [%s]" <text>             (Sonos) {channel="sonos:PLAY1:living:currenttrack"}
String Sonos_State        "Status [%s]"      <text>             (Sonos) {channel="sonos:PLAY1:living:state"}
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
		Frame label="Sonos" {
			Default item=Sonos_Controller
			Slider  item=Sonos_Volume
			Switch  item=Sonos_Mute
			Switch  item=Sonos_LED
			Text    item=Sonos_CurrentTrack		
			Text    item=Sonos_State
		}
}
```
