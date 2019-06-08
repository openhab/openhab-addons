# Amazon Echo Control Binding

This binding can control Amazon Echo devices (Alexa) and their smart bulbs.

It provides features to control and view the current state of echo devices:

- use echo device as text to speech from a rule
- volume
- pause/continue/next track/previous track
- connect/disconnect bluetooth devices
- start playing tuneIn radio
- start playing Amazon Music
- control of multi room music
- show album art image in sitemap
- speak a reminder message
- plays an alarm sound
- start traffic news
- start daily briefing
- start weather report
- start good morning report
- start automation routine
- activate multiple configurations of flash briefings
- start playing music by providing the voice command as text (Works with all music providers)
- get last spoken voice command
- change the volume of the alarm

Also this binding includes the features to control your lights connected to your Echo devices:

- turn on/off your lights
- change the color
- control groups of lights or just single bulbs
- receive the current state of the lights

Restrictions:

- groups can't receive their current color (multiple colors are possible)
- lights can only receive their state every 30 seconds
- turning on/off (or switch color, change brightness) will send a request for every single bulb in a group

Some ideas what you can do in your home by using rules and other openHAB controlled devices:

- Automatic turn on your amplifier and connect echo with bluetooth if the echo plays music
- If the amplifier was turned of, the echo stop playing and disconnect the bluetooth
- The echo starts playing radio if the light was turned on
- The echo starts playing radio at specified time 
- Remind you with a voice message, that a window is open for a long time and it is winter
- Start a routine which welcome you, if you come home
- Start a routine which switch a smart home device connected to alexa
- Start your briefing if you turn on the light first time in the morning
- Have different flash briefing in the morning and evening
- Let alexa say 'welcome' to you if you open the door
- Implement own handling for voice commands in a rule

With the possibility to control your lights you could do:

- a scene-based configuration of your rooms
- connect single bulbs to functions of openhab
- simulate your presence at home
- automaticly turn on your lights at the evening
- integrate your smart bulbs with rules

## Note

This binding uses the same API as the Web-Browser-Based Alexa site (alexa.amazon.de).
In other words, it simulates a user which is using the web page.
Unfortunately, the binding can get broken if Amazon change the web site.

The binding is tested with amazon.de, amazon.fr, amazon.it, amazon.com and amazon.co.uk accounts, but should also work with all others. 

## Supported Things

| Thing type id        | Name                                  |
|----------------------|---------------------------------------|
| account              | Amazon Account                        |
| echo                 | Amazon Echo Device                    |
| echospot             | Amazon Echo Spot Device               |
| echoshow             | Amazon Echo Show Device               |
| wha                  | Amazon Echo Whole House Audio Control |
| flashbriefingprofile | Flash briefing profile                |
| light                | Smart bulbs connected to the Echo     |
| lightGroup           | Groups of lights                      |

## First Steps

1) Create an 'Amazon Account' thing
2) open the url YOUR_OPENHAB/amazonechocontrol in your browser (e.g. http://openhab:8080/amazonechocontrol/), click the link for your account thing and login.
3) You should see now a message that the login was successful

## Discovery

After configuration of the account thing with the login data, the echo devices registered in the amazon account, get discovered.
If the device type is not known by the binding, the device will not be discovered.
But you can define any device listed in your alexa app with the best matching existing device (e.g. echo).
You will find the required serial number in settings of the device in the alexa app.

Also you will see your lights in the inbox of openhab. They will automatically be discoverd by openhab based on your amazon login.
If you configured any groups of lights in your amazon alexa app they will be discovered by openhab too.

## Binding Configuration

The binding does not have any configuration.
The configuration of your amazon account must be done in the 'Amazon Account' device.

## Thing Configuration

The Amazon Account does not need any configuration.

### Amazon Devices

All Amazon devices (echo, echospot, echoshow, wha) needs the following configurations:

| Configuration name       | Description                                        |
|--------------------------|----------------------------------------------------|
| serialNumber             | Serial number of the amazon echo in the Alexa app  |

You will find the serial number in the alexa app or on the webpage YOUR_OPENHAB/amazonechocontrol/YOUR_ACCOUNT (e.g. http://openhab:8080/amazonechocontrol/account1).

### Flash Briefing Profile

The flashbriefingprofile thing has no configuration parameters.
It will be configured at runtime by using the save channel to store the current flash briefing configuration in the thing.

## Channels

| Channel Type ID       | Item Type | Access Mode | Thing Type                    | Description                                                                                                                                                                
|-----------------------|-------------|-----------|-------------------------------|-----------------------------------------------------------------------
| player                | Player      | R/W       | echo, echoshow, echospot, wha | Control the music player e.g. pause/continue/next track/previous track                                                                                                
| volume                | Dimmer      | R/W       | echo, echoshow, echospot      | Control the volume                                                                                            
| shuffle               | Switch      | R/W       | echo, echoshow, echospot, wha | Shuffle play if applicable, e.g. playing a playlist     
| imageUrl              | String      | R         | echo, echoshow, echospot, wha | Url of the album image or radio station logo     
| title                 | String      | R         | echo, echoshow, echospot, wha | Title of the current media     
| subtitle1             | String      | R         | echo, echoshow, echospot, wha | Subtitle of the current media     
| subtitle2             | String      | R         | echo, echoshow, echospot, wha | Additional subtitle of the current media     
| providerDisplayName   | String      | R         | echo, echoshow, echospot, wha | Name of the music provider   
| bluetoothMAC          | String      | R/W       | echo, echoshow, echospot      | Bluetooth device MAC. Used to connect to a specific device or disconnect if a empty string was provided
| bluetooth             | Switch      | R/W       | echo, echoshow, echospot      | Connect/Disconnect to the last used bluetooth device (works after a bluetooth connection was established after the openHAB start) 
| bluetoothDeviceName   | String      | R         | echo, echoshow, echospot      | User friendly name of the connected bluetooth device
| radioStationId        | String      | R/W       | echo, echoshow, echospot, wha | Start playing of a TuneIn radio station by specifying it's id or stops playing if a empty string was provided
| radio                 | Switch      | R/W       | echo, echoshow, echospot, wha | Start playing of the last used TuneIn radio station (works after the radio station started after the openhab start)
| amazonMusicTrackId    | String      | R/W       | echo, echoshow, echospot, wha | Start playing of a Amazon Music track by it's id od stops playing if a empty string was provided
| amazonMusicPlayListId | String      | W         | echo, echoshow, echospot, wha | Write Only! Start playing of a Amazon Music playlist by specifying it's id od stops playing if a empty string was provided. Selection will only work in PaperUI
| amazonMusic           | Switch      | R/W       | echo, echoshow, echospot, wha | Start playing of the last used Amazon Music song (works after at least one song was started after the openhab start)
| remind                | String      | R/W       | echo, echoshow, echospot      | Write Only! Speak the reminder and sends a notification to the Alexa app (Currently the reminder is played and notified two times, this seems to be a bug in the amazon software)
| startRoutine          | String      | W         | echo, echoshow, echospot      | Write Only! Type in what you normally say to Alexa without the preceding "Alexa," 
| musicProviderId       | String      | R/W       | echo, echoshow, echospot      | Current Music provider
| playMusicVoiceCommand | String      | W         | echo, echoshow, echospot      | Write Only! Voice command as text. E.g. 'Yesterday from the Beatles' 
| startCommand          | String      | W         | echo, echoshow, echospot      | Write Only! Used to start anything. Available options: Weather, Traffic, GoodMorning, SingASong, TellStory, FlashBriefing and FlashBriefing.<FlahshbriefingDeviceID> (Note: The options are case sensitive)
| textToSpeech          | String      | W         | echo, echoshow, echospot      | Write Only! Write some text to this channel and alexa will speak it 
| textToSpeechVolume    | Dimmer      | R/W       | echo, echoshow, echospot      | Volume of the textToSpeech channel, if 0 the current volume will be used
| lastVoiceCommand      | String      | R/W       | echo, echoshow, echospot      | Last voice command spoken to the device. Writing to the channel starts voice output.
| mediaProgress         | Dimmer      | R/W       | echo, echoshow, echospot      | Media progress in percent 
| mediaProgressTime     | Number:Time | R/W       | echo, echoshow, echospot      | Media play time 
| mediaLength           | Number:Time | R         | echo, echoshow, echospot      | Media length
| notificationVolume    | Dimmer      | R         | echo, echoshow, echospot      | Notification volume
| ascendingAlarm        | Switch      | R/W       | echo, echoshow, echospot      | Ascending alarm up to the configured volume
| save                  | Switch      | W         | flashbriefingprofile          | Write Only! Stores the current configuration of flash briefings within the thing
| active                | Switch      | R/W       | flashbriefingprofile          | Active the profile
| playOnDevice          | String      | W         | flashbriefingprofile          | Specify the echo serial number or name to start the flash briefing. 
| lightState            | Switch      | R/W       | light, lightGroup             | Shows and changes the state (ON/OFF) of your light or lightgroup
| lightBrightness       | Dimmer      | R/W       | light, lightGroup             | Shows and changes the brightness of your light or lightgroup
| lightColor            | String      | R/W       | light, lightGroup             | Shows and changes the color of your light (groups are not able to show their color!)
| whiteTemperature      | String      | R/W       | light, lightGroup             | White temperatures of your lights

## Advanced Feature Technically Experienced Users

The url <YOUR_OPENHAB>/amazonechocontrol/<YOUR_ACCOUNT>/PROXY/<API_URL> provides a proxy server with an authenticated connection to the amazon alexa server. This can be used to call alexa api from rules.

E.g. to read out the history call from an installation on openhab:8080 with a account named account1:

http://openhab:8080/amazonechocontrol/account1/PROXY/api/activities?startTime=&size=50&offset=1


## Full Example

### amazonechocontrol.things

```
Bridge amazonechocontrol:account:account1 "Amazon Account" @ "Accounts" 
{
    Thing echo                 echo1          "Alexa" @ "Living Room" [serialNumber="SERIAL_NUMBER"]
    Thing echoshow             echoshow1      "Alexa" @ "Kitchen" [serialNumber="SERIAL_NUMBER"]
    Thing echospot             echospot1      "Alexa" @ "Sleeping Room" [serialNumber="SERIAL_NUMBER"]
    Thing wha                  wha1           "Ground Floor Music Group" @ "Music Groups" [serialNumber="SERIAL_NUMBER"]
    Thing flashbriefingprofile flashbriefing1 "Flash Briefing Technical" @ "Flash Briefings" 
    Thing flashbriefingprofile flashbriefing2 "Flash Briefing Life Style" @ "Flash Briefings" 
}
```

You will find the serial number in the Alexa app. 

### amazonechocontrol.items:

Sample for the Thing echo1 only. But it will work in the same way for the other things, only replace the thing name in the channel link.
Take a look in the channel description above to know, which channels are supported by your thing type.

```
Group Alexa_Living_Room <player>

// Player control
Player Echo_Living_Room_Player                "Player"                                (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:player"}
Dimmer Echo_Living_Room_Volume                "Volume [%.0f %%]" <soundvolume>        (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:volume"}
Switch Echo_Living_Room_Shuffle               "Shuffle"                               (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:shuffle"}

// Media channels
Dimmer Echo_Living_Room_MediaProgress    "Media progress"                             (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:mediaProgress"}
Number:Time Echo_Living_Room_MediaProgressTime    "Media progress time [%d %unit%]"   (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:mediaProgressTime"}
Number:Time Echo_Living_Room_MediaLength    "Media length [%d %unit%]"                (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:mediaLength"}

// Player Information
String Echo_Living_Room_ImageUrl              "Image URL"                             (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:imageUrl"}
String Echo_Living_Room_Title                 "Title"                                 (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:title"}
String Echo_Living_Room_Subtitle1             "Subtitle 1"                            (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:subtitle1"}
String Echo_Living_Room_Subtitle2             "Subtitle 2"                            (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:subtitle2"}
String Echo_Living_Room_ProviderDisplayName   "Provider"                              (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:providerDisplayName"}

// Music provider and start command
String Echo_Living_Room_MusicProviderId       "Music Provider Id"                     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:musicProviderId"}
String Echo_Living_Room_PlayMusicCommand      "Play music voice command (Write Only)" (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:playMusicVoiceCommand"}
String Echo_Living_Room_StartCommand          "Start Information" (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:startCommand"}

// TuneIn Radio
String Echo_Living_Room_RadioStationId        "TuneIn Radio Station Id"               (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:radioStationId"}
Switch Echo_Living_Room_Radio                 "TuneIn Radio"                          (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:radio"}

// Amazon Music
String Echo_Living_Room_AmazonMusicTrackId    "Amazon Music Track Id"                 (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:amazonMusicTrackId"}
String Echo_Living_Room_AmazonMusicPlayListId "Amazon Music Playlist Id"              (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:amazonMusicPlayListId"}
Switch Echo_Living_Room_AmazonMusic           "Amazon Music"                          (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:amazonMusic"}

// Bluetooth
String Echo_Living_Room_BluetoothMAC          "Bluetooth MAC Address" <bluetooth>     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetoothMAC"}
Switch Echo_Living_Room_Bluetooth             "Bluetooth"             <bluetooth>     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetooth"}
String Echo_Living_Room_BluetoothDeviceName   "Bluetooth Device"      <bluetooth>     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetoothDeviceName"}

// Commands
String Echo_Living_Room_TTS                   "Text to Speech"                        (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:textToSpeech"}
Dimmer Echo_Living_Room_TTS_Volume            "Text to Speech Volume"                 (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:textToSpeechVolume"}
String Echo_Living_Room_Remind                "Remind"                                (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:remind"}
String Echo_Living_Room_PlayAlarmSound        "Play Alarm Sound"                      (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:playAlarmSound"}
String Echo_Living_Room_StartRoutine          "Start Routine"                         (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:startRoutine"}
Dimmer Echo_Living_Room_NotificationVolume    "Notification volume"                   (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:notificationVolume"}
Switch Echo_Living_Room_AscendingAlarm    "Ascending alarm"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:ascendingAlarm"}

// Feedbacks
String Echo_Living_Room_LastVoiceCommand          "Last voice command"                (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:lastVoiceCommand"}

// Flashbriefings
Switch FlashBriefing_Technical_Save   "Save (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing1:save"} 
Switch FlashBriefing_Technical_Active "Active" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing1:active"}
String FlashBriefing_Technical_Play   "Play (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing1:playOnDevice"}

Switch FlashBriefing_LifeStyle_Save   "Save (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing2:save"} 
Switch FlashBriefing_LifeStyle_Active "Active" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing2:active"}
String FlashBriefing_LifeStyle_Play   "Play (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing2:playOnDevice"}

// Lights and lightgroups - you will find the applianceId in the properties of your light or lightgroup!
Switch Light_State "On/Off" { channel="amazonechocontrol:lightGroup:account1:applianceId:lightState" }
Dimmer Light_Brightness "Brightness" { channel="amazonechocontrol:lightGroup:account1:applianceId:lightBrightness" }
String Light_Color "Color" { channel="amazonechocontrol:lightGroup:account1:applianceId:lightColor" }
String Light_White "White temperature" { channel="amazonechocontrol:lightGroup:account1:applianceId:whiteTemperature" }
```

### amazonechocontrol.sitemap:

```
sitemap amazonechocontrol label="Echo Devices"
{
        Frame label="Alexa" {
            Default   item=Echo_Living_Room_Player
            Slider    item=Echo_Living_Room_Volume
            Slider    item=Echo_Living_Room_MediaProgress
            Text      item=Echo_Living_Room_MediaProgressTime
            Text      item=Echo_Living_Room_MediaLength
            Switch    item=Echo_Living_Room_Shuffle
            Image     item=Echo_Living_Room_ImageUrl      label=""
            Text      item=Echo_Living_Room_Title
            Text      item=Echo_Living_Room_Subtitle1
            Text      item=Echo_Living_Room_Subtitle2
            Text      item=Echo_Living_Room_ProviderDisplayName

            // The listed providers are only samples, you could have more
            Selection item=Echo_Living_Room_MusicProviderId mappings=[ 'TUNEIN'='Radio', 'SPOTIFY'='Spotify', 'AMAZON_MUSIC'='Amazon Music', 'CLOUDPLAYER'='Amazon']
            Text    item=Echo_Living_Room_MusicProviderId

            // To start one of your flashbriefings use Flashbriefing.<YOUR FLASHBRIEFING THING ID>
            Selection item=Echo_Living_Room_StartCommand mappings=[ 'Weather'='Weather', 'Traffic'='Traffic', 'GoodMorning'='Good Morning', 'SingASong'='Song', 'TellStory'='Story', 'FlashBriefing'='Flash Briefing', 'FlashBriefing.flashbriefing1'='Technical', 'FlashBriefing.flashbriefing2'='Life Style' ]

            Selection item=Echo_Living_Room_RadioStationId mappings=[ ''='Off', 's1139'='Antenne Steiermark', 's8007'='Hitradio Ö3', 's16793'='Radio 10', 's8235'='FM4' ]
            Text    item=Echo_Living_Room_RadioStationId
            Switch  item=Echo_Living_Room_Radio

            Text    item=Echo_Living_Room_AmazonMusicTrackId
            Text    item=Echo_Living_Room_AmazonMusicPlayListId
            Switch  item=Echo_Living_Room_AmazonMusic

            Text    item=Echo_Living_Room_BluetoothMAC
            // Change the <YOUR_DEVICE_MAC> Place holder with the MAC address shown, if alexa is connected to the device
            Selection item=Echo_Living_Room_BluetoothMAC mappings=[ ''='Disconnected', '<YOUR_DEVICE_MAC>'='Bluetooth Device 1', '<YOUR_DEVICE_MAC>'='Bluetooth Device 2']

            // These are only view of the possible options. Enable ShowIDsInGUI in the binding configuration and look in drop-down-box of this channel in the Paper UI Control section
            Selection item=Echo_Living_Room_PlayAlarmSound mappings=[ ''='None', 'ECHO:system_alerts_soothing_01'='Adrift', 'ECHO:system_alerts_atonal_02'='Clangy']

            Switch  item=Echo_Living_Room_Bluetooth
            Text    item=Echo_Living_Room_BluetoothDeviceName
            Text    item=Echo_Living_Room_LastVoiceCommand
            Slider  item=Echo_Living_Room_NotificationVolume
            Switch  item=Echo_Living_Room_AscendingAlarm
        }

        Frame label="Flash Briefing Technical" {
            Switch  item=FlashBriefing_Technical_Save
            Switch  item=FlashBriefing_Technical_Active
            Text    item=FlashBriefing_Technical_Play
        }

        Frame label="Flash Briefing Life Style" {
            Switch  item=FlashBriefing_LifeStyle_Save
            Switch  item=FlashBriefing_LifeStyle_Active
            Text    item=FlashBriefing_LifeStyle_Play
        }
		
		Frame label="Lights and light groups" {
			Switch item=Light_State
			Slider item=Light_Brightness
			Selection item=Light_Color mappings=[ ''='', 'red'='Red', 'crimson'='Crimson', 'salmon'='Salmon', 'orange'='Orange', 'gold'='Gold', 'yellow'='Yellow', 'green'='Green', 'turquoise'='Turquoise', 'cyan'='Cyan', 'sky_blue'='Sky Blue', 'blue'='Blue', 'purple'='Purple', 'magenta'='Magenta', 'pink'='Pink', 'lavender'='Lavender' ]
			Selection item=Light_White mappings=[ ''='', 'warm_white'='Warm white', 'soft_white'='Soft white', 'white'='White', 'daylight_white'='Daylight white', 'cool_white'='Cool white' ]
		}
}
```

## How To Get IDs 

1) Open the url YOUR_OPENHAB/amazonechocontrol in your browser (e.g. http://openhab:8080/amazonechocontrol/)
2) Click on the name of the account thing
3) Click on the name of the echo thing 
4) Scroll to the channel and copy the required ID

## Tutorials

### Let alexa speak a text from a rule:

1) Create a rule with a trigger of your choice

```php
rule "Say welcome if the door opens"
when
    Item Door_Contact changed to OPEN
then
    Echo_Living_Room_TTS.sendCommand('Hello World')
end
```

## Playing an alarm sound for 15 seconds with an openHAB rule if an door contact was opened:

1) Do get the ID of your sound, follow the steps in "How To Get IDs"
2) Write down the text in the square brackets. e.g. ECHO:system_alerts_repetitive01 for the nightstand sound
3) Create a rule for start playing the sound:

```php
var Timer stopAlarmTimer = null

rule "Turn on alarm sound for 15 seconds if door opens"
when
    Item Door_Contact changed to OPEN
then
    Echo_Living_Room_PlayAlarmSound.sendCommand('ECHO:system_alerts_repetitive01')
    if (stopAlarmTimer === null)
    {
        stopAlarmTimer = createTimer(now.plusSeconds(15)) [|
            stopAlarmTimer.cancel()
            stopAlarmTimer = null
            Echo_Living_Room_PlayAlarmSound.sendCommand('')
        ]
    }
end
```

Note 1: Do not use a to short time for playing the sound, because alexa needs some time to start playing the sound.
It's not recommended to use a time below 10 seconds.

Note 2: The rule have no effect for your default alarm sound used in the alexa app.

### Play a spotify playlist if a switch was changed to on:

1) Do get the ID of your sound, follow the steps in "How To Get IDs"
2) Write down the text in the square brackets. e.g. SPOTIFY for the spotify music provider
3) Create a rule for start playing a song or playlist:

```php
rule "Play a playlist on spotify if a switch was changed"
when
    Item Spotify_Playlist_Switch changed to ON
then
    Echo_Living_Room_PlayMusicProvider.sendCommand('SPOTIFY')
    Echo_Living_Room_PlayMusicCommand.sendCommand('Playlist Party')
end
```

Note: It's recommended to test the command send to play music command first with the voice and the real alexa device. E.g. say 'Alexa, Playlist Party'

### Start playing weather/traffic/etc:

1) Pick up one of the available commands: Weather, Traffic, GoodMorning, SingASong, TellStory, FlashBriefing
2) Create a rule for start playing the information where you provide the command as string:

```php
rule "Start wheater info"
when
    Item Spotify_Start_Wheater_Switch changed to ON
then
     Echo_Living_Room_StartCommand.sendCommand('Weather')
end
```

### Start playing a custom flashbriefing on a device:

1) Do get the ID of your sound, follow the steps in "How To Get IDs"
2) Write down the text in the square brackets. e.g. flashbriefing.flashbriefing1
2) Create a rule for start playing the information where you provide the command as string:

```php
rule "Start wheater info"
when
    Item Spotify_Start_Wheater_Switch changed to ON
then
     Echo_Living_Room_StartCommand.sendCommand('FlashBriefing.flashbriefing1')
end
```

## Credits

The idea for writing this binding came from this blog: http://blog.loetzimmer.de/2017/10/amazon-alexa-hort-auf-die-shell-echo.html (German).
Thank you Alex!
The technical information for the web socket connection to get live alexa state updates cames from Ingo. He has done the alexa iobrokern implementation https://github.com/Apollon77
Thank you Ingo!

## Trademark Disclaimer

TuneIn, Amazon Echo, Amazon Echo Spot, Amazon Echo Show, Amazon Music, Amazon Prime, Alexa and all other products and Amazon, TuneIn and other companies are trademarks™ or registered® trademarks of their respective holders.
Use of them does not imply any affiliation with or endorsement by them. 
