# Amazon Echo Control Binding

This binding can control Amazon Echo devices (Alexa).

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

## Note

This binding uses the same API as the Web-Browser-Based Alexa site (alexa.amazon.de).
In other words, it simulates a user which is using the web page.
Unfortunately, the binding can get broken if Amazon change the web site.

The binding is tested with amazon.de, amazon.com and amazon.co.uk accounts, but should also work with all others. 

## Warning

For the connection to the Amazon server, your password of the Amazon account is required, this will be stored in your openHAB thing device configuration.
So you should be sure, that nobody other has access to your configuration! 

## What Else You Should Know

All the display options are updated by polling the amazon server.
The polling time can be configured, but a minimum of 10 seconds is required.
The default is 60 seconds, which means the it can take up to 60 seconds to see the correct state.
It's not know, if there is a limit implemented in the amazon server if the polling is too fast and maybe amazon will lock your account. 30 seconds seems to be safe.

## Supported Things

| Thing type id        | Name                                  |
|----------------------|---------------------------------------|
| account              | Amazon Account                        |
| echo                 | Amazon Echo Device                    |
| echospot             | Amazon Echo Spot Device               |
| echoshow             | Amazon Echo Show Device               |
| wha                  | Amazon Echo Whole House Audio Control |
| flashbriefingprofile | Flash briefing profile                |

## First Steps

1) Create an 'Amazon Account' thing
2) Configure your credentials in the account thing (2 factor authentication is not supported!)
3) After confirmation:
a) the 'Account Thing' goes Online -> continue with 4)
b) the 'Account Thing' stays offline:  
open the url YOUR_OPENHAB/amazonechocontrol in your browser (e.g. http://openhab:8080/amazonechocontrol/), click the link for your account thing and try to login.
4) The echo device things get automatically discovered and can be accepted

## Discovery

After configuration of the account thing with the login data, the echo devices registered in the amazon account, get discovered.
If the device type is not known by the binding, the device will not be discovered.
But you can define any device listed in your alexa app with the best matching existing device (e.g. echo).
You will find the required serial number in settings of the device in the alexa app.

## Binding Configuration

The binding does not have any configuration.
The configuration of your amazon account must be done in the 'Amazon Account' device.

## Thing Configuration

The Amazon Account thing needs the following configurations:

| Configuration name       | Description                                                               |
|--------------------------|---------------------------------------------------------------------------|
| amazonSite               | The amazon site where the echos are registered. e.g. amazon.de            |
| email                    | Email of your amazon account                                              |
| password                 | Password of your amazon account                                           |
| pollingIntervalInSeconds | Polling interval for the device state in seconds. Default 30, minimum 10  |

IMPORTANT: If the Account thing does not go online and reports a login error, read the instructions in "First Steps" above.

### Amazon Devices

All Amazon devices (echo, echospot, echoshow, wha) needs the following configurations:

| Configuration name       | Description                                        |
|--------------------------|----------------------------------------------------|
| serialNumber             | Serial number of the amazon echo in the Alexa app  |

You will find the serial number in the alexa app.

### Flash Briefing Profile

The flashbriefingprofile thing has no configuration parameters.
It will be configured at runtime by using the save channel to store the current flash briefing configuration in the thing.

## Channels

| Channel Type ID       | Item Type | Access Mode | Thing Type | Description                                                                                                                                                                
|-----------------------|-----------|-------------|------------|------------------------------------------------------------------------------------------
| player                | Player    | R/W         | echo, echoshow, echospot, wha | Control the music player e.g. pause/continue/next track/previous track                                                                                                
| volume                | Dimmer    | R/W         | echo, echoshow, echospot      | Control the volume                                                                                            
| shuffle               | Switch    | R/W         | echo, echoshow, echospot, wha | Shuffle play if applicable, e.g. playing a playlist     
| imageUrl              | String    | R           | echo, echoshow, echospot, wha | Url of the album image or radio station logo     
| title                 | String    | R           | echo, echoshow, echospot, wha | Title of the current media     
| subtitle1             | String    | R           | echo, echoshow, echospot, wha | Subtitle of the current media     
| subtitle2             | String    | R           | echo, echoshow, echospot, wha | Additional subtitle of the current media     
| providerDisplayName   | String    | R           | echo, echoshow, echospot, wha | Name of the music provider   
| bluetoothMAC          | String    | R/W         | echo, echoshow, echospot      | Bluetooth device MAC. Used to connect to a specific device or disconnect if a empty string was provided
| bluetooth             | Switch    | R/W         | echo, echoshow, echospot      | Connect/Disconnect to the last used bluetooth device (works after a bluetooth connection was established after the openHAB start) 
| bluetoothDeviceName   | String    | R           | echo, echoshow, echospot      | User friendly name of the connected bluetooth device
| radioStationId        | String    | R/W         | echo, echoshow, echospot, wha | Start playing of a TuneIn radio station by specifying it's id or stops playing if a empty string was provided
| radio                 | Switch    | R/W         | echo, echoshow, echospot, wha | Start playing of the last used TuneIn radio station (works after the radio station started after the openhab start)
| amazonMusicTrackId    | String    | R/W         | echo, echoshow, echospot, wha | Start playing of a Amazon Music track by it's id od stops playing if a empty string was provided
| amazonMusicPlayListId | String    | W         | echo, echoshow, echospot, wha | Write Only! Start playing of a Amazon Music playlist by specifying it's id od stops playing if a empty string was provided. Selection will only work in PaperUI
| amazonMusic           | Switch    | R/W         | echo, echoshow, echospot, wha | Start playing of the last used Amazon Music song (works after at least one song was started after the openhab start)
| remind                | String    | R/W         | echo, echoshow, echospot      | Write Only! Speak the reminder and sends a notification to the Alexa app (Currently the reminder is played and notified two times, this seems to be a bug in the amazon software)
| startRoutine          | Switch    | W         | echo, echoshow, echospot      | Write Only! Type in what you normally say to Alexa without the preceding "Alexa," 
| musicProviderId       | String    | R/W         | echo, echoshow, echospot      | Current Music provider
| playMusicVoiceCommand | String    | W         | echo, echoshow, echospot      | Write Only! Voice command as text. E.g. 'Yesterday from the Beatles' 
| startCommand          | String    | W         | echo, echoshow, echospot      | Write Only! Used to start anything. Available options: Weather, Traffic, GoodMorning, SingASong, TellStory, FlashBriefing and FlashBriefing.<FlahshbriefingDeviceID> (Note: The options are case sensitive)
| textToSpeech          | String    | W         | echo, echoshow, echospot      | Write Only! Write some text to this channel and alexa will speak it 
| save                  | Switch    | W         | flashbriefingprofile     | Write Only! Stores the current configuration of flash briefings within the thing
| active                | Switch    | R/W       | flashbriefingprofile     | Active the profile
| playOnDevice          | String    | W         | flashbriefingprofile     | Specify the echo serial number or name to start the flash briefing. 

## Full Example

### amazonechocontrol.things

```
Bridge amazonechocontrol:account:account1 "Amazon Account" @ "Accounts" [amazonSite="amazon.de", email="mail@example.com", password="secure", pollingIntervalInSeconds=60]
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
Player Echo_Living_Room_Player               "Player"                            (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:player"}
Dimmer Echo_Living_Room_Volume               "Volume [%.0f %%]" <soundvolume>    (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:volume"}
Switch Echo_Living_Room_Shuffle              "Shuffle"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:shuffle"}

// Player Information
String Echo_Living_Room_ImageUrl             "Image URL"                         (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:imageUrl"}
String Echo_Living_Room_Title                "Title"                             (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:title"}
String Echo_Living_Room_Subtitle1            "Subtitle 1"                        (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:subtitle1"}
String Echo_Living_Room_Subtitle2            "Subtitle 2"                        (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:subtitle2"}
String Echo_Living_Room_ProviderDisplayName  "Provider"                          (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:providerDisplayName"}

// Music provider and start command
String Echo_Living_Room_MusicProviderId      "Music Provider Id"                 (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:musicProviderId"}
String Echo_Living_Room_PlayMusicCommand     "Play music voice command (Write Only)" (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:playMusicVoiceCommand"}
String Echo_Living_Room_StartCommand         "Start Information" (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:startCommand"}

// TuneIn Radio
String Echo_Living_Room_RadioStationId       "TuneIn Radio Station Id"           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:radioStationId"}
Switch Echo_Living_Room_Radio                "TuneIn Radio"                      (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:radio"}

// Amazon Music
String Echo_Living_Room_AmazonMusicTrackId    "Amazon Music Track Id"            (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:amazonMusicTrackId"}
String Echo_Living_Room_AmazonMusicPlayListId "Amazon Music Playlist Id"  (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:amazonMusicPlayListId"}
Switch Echo_Living_Room_AmazonMusic           "Amazon Music"                     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:amazonMusic"}

// Bluetooth
String Echo_Living_Room_BluetoothMAC          "Bluetooth MAC Address" <bluetooth> (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetoothMAC"}
Switch Echo_Living_Room_Bluetooth            "Bluetooth"        <bluetooth>      (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetooth"}
String Echo_Living_Room_BluetoothDeviceName  "Bluetooth Device" <bluetooth>      (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetoothDeviceName"}

// Commands
String Echo_Living_Room_TTS                "Text to Speech"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:textToSpeech"}
String Echo_Living_Room_Remind                "Remind"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:remind"}
String Echo_Living_Room_PlayAlarmSound         "Play Alarm Sound"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:playAlarmSound"}
String Echo_Living_Room_StartRoutine         "Start Routine"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:startRoutine"}

// Flashbriefings
Switch FlashBriefing_Technical_Save  "Save (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing1:save"} 
Switch FlashBriefing_Technical_Active "Active" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing1:active"}
String FlashBriefing_Technical_Play "Play (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing1:playOnDevice"}

Switch FlashBriefing_LifeStyle_Save  "Save (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing2:save"} 
Switch FlashBriefing_LifeStyle_Active "Active" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing2:active"}
String FlashBriefing_LifeStyle_Play "Play (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing2:playOnDevice"}
```

### amazonechocontrol.sitemap:

```
sitemap amzonechocontrol label="Echo Devices"
{
        Frame label="Alexa" {
            Default   item=Echo_Living_Room_Player
            Slider    item=Echo_Living_Room_Volume
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
        }
        
        Frame label="Flash Briefing Technical" {
            Switch  item=FlashBriefing_Technical_Save
            Switch  item=FlashBriefing_Technical_Active
            Text  item=FlashBriefing_Technical_Play
        }
        
        Frame label="Flash Briefing Life Style" {
            Switch  item=FlashBriefing_LifeStyle_Save
            Switch  item=FlashBriefing_LifeStyle_Active
            Text  item=FlashBriefing_LifeStyle_Play
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

## Trademark Disclaimer

TuneIn, Amazon Echo, Amazon Echo Spot, Amazon Echo Show, Amazon Music, Amazon Prime, Alexa and all other products and Amazon, TuneIn and other companies are trademarks™ or registered® trademarks of their respective holders.
Use of them does not imply any affiliation with or endorsement by them. 
