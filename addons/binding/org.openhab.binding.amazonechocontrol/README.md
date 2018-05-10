# Amazon Echo Control Binding

This binding can control Amazon Echo devices (Alexa).

It provides features to control and view the current state of echo devices:

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

## Note ##

This binding uses the same API as the Web-Browser-Based Alexa site (alexa.amazon.de).
In other words, it simulates a user which is using the web page.
Unfortunately, the binding can get broken if Amazon change the web site.

The binding is tested with amazon.de and amazon.co.uk accounts, but should also work with all others. 

## Warning ##

For the connection to the Amazon server, your password of the Amazon account is required, this will be stored in your openHAB thing device configuration.
So you should be sure, that nobody other has access to your configuration! 

## What else you should know ##

All the display options are updated by polling the amazon server.
The polling time can be configured, but a minimum of 10 seconds is required.
The default is 60 seconds, which means the it can take up to 60 seconds to see the correct state.
I do not know, if there is a limit implemented in the amazon server if the polling is too fast and maybe amazon will lock your account. 60 seconds seems to be safe.

## Supported Things

| Thing type id        | Name                                  |
|----------------------|---------------------------------------|
| account              | Amazon Account                        |
| echo                 | Amazon Echo Device                    |
| echospot             | Amazon Echo Spot Device               |
| echoshow             | Amazon Echo Show Device               |
| wha                  | Amazon Echo Whole House Audio Control |
| flashbriefingprofile | Flash briefing profile                |
| unknown              | Unknown Echo Device or App\*          |

\* The unknown device will provide all channels, but maybe not all of them supported by your device.

## Discovery

The first 'Amazon Account' thing will be automatically discovered.
After configuration of the thing with the account data, a 'Amazon <???>' thing will be discovered for each registered device.
If the device type is not known by the binding, an 'Unknown' device will be created.

## Binding Configuration

The binding does not have any configuration.
The configuration of your amazon account must be done in the 'Amazon Account' device.

## Thing Configuration

The Amazon Account thing need the following configurations:

| Configuration name       | Description                                                               |
|--------------------------|---------------------------------------------------------------------------|
| amazonSite               | The amazon site where the echos are registered. e.g. amazon.de            |
| email                    | Email of your amazon account                                              |
| password                 | Password of your amazon account                                           |
| pollingIntervalInSeconds | Polling interval for the device state in seconds. Default 60, minimum 10  |

2 factor authentication is not supported!

** HINT ** IMPORTANT: If the Account thing does not go online and reports a login error, open the url YOUR_OPENHAB/amazonechocontrol/ID_OF_ACCOUNT_THING (Replace YOUR_OPENHAB and ID_OF_ACCOUNT_THING with your configuration) in your browser (e.g. http://openhab:8080/amazonechocontrol/account) and try to login.

### Amazon Devices

All Amazon devices (echo, echospot, echoshow, wha, unknown) needs the following configurations:

| Configuration name       | Description                                        |
|--------------------------|----------------------------------------------------|
| serialNumber             | Serial number of the amazon echo in the Alexa app  |

You will find the serial number in the alexa app.

### Flash Briefing Profile

The flashbriefingprofile thing has no configuration parameters.
It will be configured at runtime by using the save channel to store the current flash briefing configuration in the thing.

## Channels

| Channel Type ID     | Item Type | Access Mode | Thing Type | Description                                                                                                                                                                
|---------------------|-----------|-------------|------------|------------------------------------------------------------------------------------------
| player              | Player    | R/W         | echo, echoshow, echospot, wha, unknown | Control the music player e.g. pause/continue/next track/previous track                                                                                                
| volume              | Dimmer    | R/W         | echo, echoshow, echospot, unknown      | Control the volume                                                                                            
| shuffle             | Switch    | R/W         | echo, echoshow, echospot, wha, unknown | Shuffle play if applicable, e.g. playing a playlist     
| imageUrl            | String    | R           | echo, echoshow, echospot, wha, unknown | Url of the album image or radio station logo     
| title               | String    | R           | echo, echoshow, echospot, wha, unknown | Title of the current media     
| subtitle1           | String    | R           | echo, echoshow, echospot, wha, unknown | Subtitle of the current media     
| subtitle2           | String    | R           | echo, echoshow, echospot, wha, unknown | Additional subtitle of the current media     
| providerDisplayName | String    | R           | echo, echoshow, echospot, wha, unknown | Name of the music provider   
| bluetoothId         | String    | R/W         | echo, echoshow, echospot, unknown      | Bluetooth device id. Used to connect to a specific device or disconnect if a empty string was provided
| bluetoothIdSelection| String    | R/W         | echo, echoshow, echospot, unknown      | Bluetooth device selection. The selection currently only works in PaperUI
| bluetooth           | Switch    | R/W         | echo, echoshow, echospot, unknown      | Connect/Disconnect to the last used bluetooth device (works after a bluetooth connection was established after the openHAB start) 
| bluetoothDeviceName | String    | R           | echo, echoshow, echospot, unknown      | User friendly name of the connected bluetooth device
| radioStationId      | String    | R/W         | echo, echoshow, echospot, wha, unknown | Start playing of a TuneIn radio station by specifying it's id or stops playing if a empty string was provided
| radio               | Switch    | R/W         | echo, echoshow, echospot, wha, unknown | Start playing of the last used TuneIn radio station (works after the radio station started after the openhab start)
| amazonMusicTrackId      | String    | R/W         | echo, echoshow, echospot, wha, unknown | Start playing of a Amazon Music track by it's id od stops playing if a empty string was provided
| amazonMusicPlayListId      | String    | W         | echo, echoshow, echospot, wha, unknown | Write Only! Start playing of a Amazon Music playlist by specifying it's id od stops playing if a empty string was provided. Selection will only work in PaperUI
| amazonMusicPlayListIdLastUsed   | String    | R  | echo, echoshow, echospot, wha, unknown | The last play list id started from openHAB
| amazonMusic               | Switch    | R/W         | echo, echoshow, echospot, wha, unknown | Start playing of the last used Amazon Music song (works after at least one song was started after the openhab start)
| remind               | String    | R/W         | echo, echoshow, echospot, unknown      | Write Only! Speak the reminder and sends a notification to the Alexa app (Currently the reminder is played and notified two times, this seems to be a bug in the amazon software)
| playAlarmSound          | String    | R/W         | echo, echoshow, echospot, unknown      | Write Only! Plays an alarm sound. In PaperUI will be a selection box available. For rules use the value shown in the square brackets
| playFlashBriefing          | Switch    | W         | echo, echoshow, echospot, unknown      | Write Only! Starts the flash briefing
| playWeatherReport          | Switch    | W         | echo, echoshow, echospot, unknown      | Write Only! Starts the weather report
| playTrafficNews            | Switch    | W         | echo, echoshow, echospot, unknown      | Write Only! Starts the traffic news
| playGoodMorning            | Switch    | W         | echo, echoshow, echospot, unknown      | Write Only! Starts the good moring report
| startRoutine               | Switch    | W         | echo, echoshow, echospot, unknown      | Write Only! Type in what you normally say to Alexa without the preceding "Alexa," 
| playMusicProvider          | String    | W         | echo, echoshow, echospot, unknown      | Write Only! Music provider used for 'Start music voice command' 
| playMusicVoiceCommand      | String    | W         | echo, echoshow, echospot, unknown      | Write Only! Voice command as text. E.g. 'Yesterday from the Beatles' 
| save            | Switch    | W         | flashbriefingprofile     | Write Only! Stores the current configuration of flash briefings within the thing
| active          | Switch    | R/W       | flashbriefingprofile     | Active the profile
| playOnDevice    | String    | W         | flashbriefingprofile     | Specify the echo serial number or name to start the flash briefing. 

## Full Example

### amzonechocontrol.things

```
Bridge amazonechocontrol:account:account1 "Amazon Account" @ "Accounts" [amazonSite="amazon.de", email="myaccountemail@myprovider.com", password="secure", pollingIntervalInSeconds=60]
{
    Thing echo                 echo1 "Alexa" @ "Living Room" [serialNumber="SERIAL_NUMBER"]
    Thing echoshow             echo2 "Alexa" @ "Kitchen" [serialNumber="SERIAL_NUMBER"]
    Thing echospot             echo3 "Alexa" @ "Sleeping Room" [serialNumber="SERIAL_NUMBER"]
    Thing wha                  echo4 "Alexa" @ "Ground Floor Music Group" [serialNumber="SERIAL_NUMBER"]
    Thing unknown              echo5 "Alexa" @ "Very new echo device" [serialNumber="SERIAL_NUMBER"]
    Thing flashbriefingprofile flashbriefing1 "Flash Briefing" @ "Flash Briefings" 
}
```

You will find the serial number in the Alexa app. 

### amzonechocontrol.items:

Sample for the Thing echo1 only. But it will work in the same way for the other things, only replace the thing name in the channel link.
Take a look in the channel description above to know, which channels are supported by your thing type.

```
Group Alexa_Living_Room <player>

Player Echo_Living_Room_Player               "Player"                            (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:player"}
Dimmer Echo_Living_Room_Volume               "Volume [%.0f %%]" <soundvolume>    (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:volume"}
Switch Echo_Living_Room_Shuffle              "Shuffle"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:shuffle"}
String Echo_Living_Room_ImageUrl             "Image URL"                         (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:imageUrl"}
String Echo_Living_Room_Title                "Title"                             (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:title"}
String Echo_Living_Room_Subtitle1            "Subtitle 1"                        (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:subtitle1"}
String Echo_Living_Room_Subtitle2            "Subtitle 2"                        (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:subtitle2"}
String Echo_Living_Room_ProviderDisplayName  "Provider"                          (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:providerDisplayName"}
String Echo_Living_Room_BluetoothId          "Bluetooth Mac Address" <bluetooth> (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetoothId"}
String Echo_Living_Room_BluetoothId_Selection "Bluetooth Device" <bluetoothIdSelection> (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetoothId"}
Switch Echo_Living_Room_Bluetooth            "Bluetooth"        <bluetooth>      (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetooth"}
String Echo_Living_Room_BluetoothDeviceName  "Bluetooth Device" <bluetooth>      (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetoothDeviceName"}
String Echo_Living_Room_RadioStationId       "TuneIn Radio Station Id"           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:radioStationId"}
Switch Echo_Living_Room_Radio                "TuneIn Radio"                      (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:radio"}
String Echo_Living_Room_AmazonMusicTrackId    "Amazon Music Track Id"            (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:amazonMusicTrackId"}
String Echo_Living_Room_AmazonMusicPlayListId "Amazon Music Playlist Id (Write Only)"  (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:amazonMusicPlayListId"}
String Echo_Living_Room_AmazonMusicPlayListIdLastUsed "Amazon Music Playlist Id last used"  (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:amazonMusicPlayListIdLastUsed"}
Switch Echo_Living_Room_AmazonMusic           "Amazon Music"                     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:amazonMusic"}
String Echo_Living_Room_Remind                "Remind"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:remind"}
String Echo_Living_Room_PlayAlarmSound         "Play Alarm Sound"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:playAlarmSound"}
Switch Echo_Living_Room_PlayFlashBriefing         "Play Flash Briefing"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:playFlashBriefing"}
Switch Echo_Living_Room_PlayWeatherReport         "Play Weather Report"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:playWeatherReport"}
Switch Echo_Living_Room_PlayTrafficNews        "Play Traffic News"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:playTrafficNews"}
Switch Echo_Living_Room_PlayGoodMoring        "Play Good Morning News"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:playGoodMorning"}
String Echo_Living_Room_StartRoutine         "Start Routine"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:startRoutine"}
String Echo_Living_Room_PlayMusicProvider    "Music Provider (Write Only)"             (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:playMusicProvider"}
String Echo_Living_Room_PlayMusicCommand     "Play music voice command (Write Only)"    (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:playMusicVoiceCommand"}

Switch FlashBriefing_Technical_Save  "Save (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing1:save"} 
Switch FlashBriefing_Technical_Active "Active" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing1:active"}
String FlashBriefing_Technical_Play "Play (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing1:playOnDevice"}
```

### amzonechocontrol.sitemap:

```
sitemap amzonechocontrol label="Echo Devices"
{
        Frame label="Alexa" {
            Default item=Echo_Living_Room_Player
            Slider  item=Echo_Living_Room_Volume
            Switch  item=Echo_Living_Room_Shuffle
            Text    item=Echo_Living_Room_Title
            Text    item=Echo_Living_Room_Subtitle1     
            Text    item=Echo_Living_Room_Subtitle2
            Text    item=Echo_Living_Room_ProviderDisplayName
            Text    item=Echo_Living_Room_BluetoothId_Selection
            Text    item=Echo_Living_Room_BluetoothId
            Switch  item=Echo_Living_Room_Bluetooth
            Text    item=Echo_Living_Room_BluetoothDeviceName
            Text    item=Echo_Living_Room_RadioStationId
            Switch  item=Echo_Living_Room_Radio      
            Text    item=Echo_Living_Room_AmazonMusicTrackId
            Text    item=Echo_Living_Room_AmazonMusicPlayListId
            Text    item=Echo_Living_Room_AmazonMusicPlayListIdLastUsed
            Switch  item=Echo_Living_Room_AmazonMusic
            Text    item=Echo_Living_Room_Remind
            Text    item=Echo_Living_Room_PlayAlarmSound
            Switch  item=Echo_Living_Room_PlayFlashBriefing
            Switch  item=Echo_Living_Room_PlayWeatherReport
            Switch  item=Echo_Living_Room_PlayTrafficNews
            Switch  item=Echo_Living_Room_PlayGoodMoring
            Text    item=Echo_Living_Room_StartRoutine
        }
        
        Frame label="Flash Briefing 1" {
            Switch  item=FlashBriefing_Technical_Save
            Switch  item=FlashBriefing_Technical_Active
            Text  item=FlashBriefing_Technical_Play
        }
}
```

To get instead of the id fields an selection box, use the selection element and provide mappings for your favorite id's:

```
        Selection item=Echo_Living_Room_RadioStationId mappings=[ ''='Off', 's1139'='Antenne Steiermark', 's8007'='Hitradio Ö3', 's16793'='Radio 10', 's8235'='FM4' ]
```

## Tutorials

**Playing an alarm sound for 15 seconds with an openHAB rule if an door contact was opened:**

1) Open the Paper UI
2) Navigate to the Control Section
3) Open the Drop-Down of the 'Alarm Sound' channel
4) Select the Sound you want to here
5) Write down the text in the square brackets. e.g. ECHO:system_alerts_repetitive01 for the nightstand sound
6) Create a rule for start playing the sound:


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
I recommend, that you to not use a time below 10 seconds.

Note 2: The rule have no effect for your default alarm sound used in the alexa app.

**Play a spotify playlist if a switch was changed to on:**

1) Open the Paper UI
2) Navigate to the Control Section
3) Open the Drop-Down of the 'Music provider for the start music voice command' channel
4) Select the Provider you want to use
5) Write down the text in the square brackets. e.g. SPOTIFY for the spotify music provider
6) Create a rule for start playing a song or playlist:


```php
rule "Play a playlist on spotify if a switch was changed"
when
    Item Spotify_Playlist_Switch changed to ON
then
    Echo_Living_Room_PlayMusicProvider.sendCommand('SPOTIFY')
    Echo_Living_Room_PlayMusicCommand.sendCommand('Playlist Party')
end
```

Note: I recommend, that you test the command send to play music command first with your voice on your alexa device. E.g. say 'Alexa, Playlist Party'

## Credits

The idea for writing this binding came from this blog: http://blog.loetzimmer.de/2017/10/amazon-alexa-hort-auf-die-shell-echo.html (German).
Thank you Alex!

## Trademark Disclaimer

TuneIn, Amazon Echo, Amazon Echo Spot, Amazon Echo Show, Amazon Music, Amazon Prime, Alexa and all other products and Amazon, TuneIn and other companies are trademarks™ or registered® trademarks of their respective holders.
Use of them does not imply any affiliation with or endorsement by them. 
