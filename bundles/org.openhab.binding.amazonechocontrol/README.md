# Amazon Echo Control Binding

This binding can control Amazon Echo devices (Alexa) and Smarthome devices connected through Alexa or a skill.

Upgrade notice:

- The `lastVoiceCommand` channel of the `amazonechocontrol` binding changed its behavior in version 5.0.0.
Due to a wrong implementation the channel changed it's state to an empty string if the same command was received again.
This has been corrected.
If you want to be notified about every state update, please adjust your rule triggers to "received update".
If you want to be notified about state changes (i.e. different commands), use "state changed".
- The write-only channels now use `autoUpdatePolicy=veto` (i.e. they don't update the item's state when a command was send).
- The channels `amazonMusic`, `amazonMusicTrackId`, `amazonPlaylistId`, `radio` and `radioStationId` have been removed because they are no longer supported from Amazon.
You can use the `textCommand` channel with a value of `Play playlist CrazyMusic on AmazonMusic` instead.
- The `lastVoiceCommand` channel will be converted to a read-only channel.
Using commands to that channel is deprecated and will stop working in future versions.
Please use the `textToSpeech` channel instead.

## What this can be used for

It provides features to control and view the current state of echo devices:

- use echo device as text to speech from a rule
- execute a text command
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
- change the equalizer settings
- get information about the next alarm, reminder and timer
- send a message to the echo devices
- control alexa smart thermostat

It also provides features to control devices connected to your echo:

- turn on/off your lights
- change the color
- control groups of lights or just single bulbs
- receive the current state of the lights
- turn on/off smart plugs (e. g. OSRAM)
- receive states of attached sensors like particulate matter or carbon monoxide

Restrictions:

- groups can't receive their current color (multiple colors are possible)
- devices can only receive their state every 60 seconds
- turning on/off (or switch color, change brightness) will send a request for every single bulb in a group

Some ideas what you can do in your home by using rules and other openHAB controlled devices:

- Automatic turn on your amplifier and connect echo with bluetooth if the echo plays music
- If the amplifier was turned off, the echo stop playing and disconnect the bluetooth
- The echo starts playing radio if the light was turned on
- The echo starts playing radio at specified time
- Remind you with a voice message, that a window is open for a long time and it is winter
- Start a routine which welcome you, if you come home
- Start a routine which switch a smart home device connected to Alexa
- Start your briefing if you turn on the light first time in the morning
- Have different flash briefing in the morning and evening
- Let Alexa say 'welcome' to you if you open the door
- Implement own handling for voice commands in a rule
- Change the equalizer settings depending on the bluetooth connection
- Turn on a light on your alexa alarm time
- Activate or deactivate the Alexa Guard with presence detection
- Adjust thermostat setpoint and mode

With the possibility to control your lights you could do:

- a scene-based configuration of your rooms
- connect single bulbs to functions of openHAB
- simulate your presence at home
- automatically turn on your lights at the evening
- integrate your smart bulbs with rules

## Supported Things

| Thing Id             | Thing Type | Description                           |
|----------------------|------------|---------------------------------------|
| account              | Bridge     | Amazon Account                        |
| echo                 | Thing      | Amazon Echo Device                    |
| echospot             | Thing      | Amazon Echo Spot Device               |
| echoshow             | Thing      | Amazon Echo Show Device               |
| wha                  | Thing      | Amazon Echo Whole House Audio Control |
| flashbriefingprofile | Thing      | Amazon Echo Whole House Audio Control |
| smartHomeDevice      | Thing      | Smart Home Device                     |
| smartHomeDeviceGroup | Thing      | Smart Home Device group               |

## First Steps

You must define an `account` (Bridge) before defining any other Thing can be used.

1. Create an Amazon `account` thing
1. Open the URL `YOUR_OPENHAB/amazonechocontrol` in your browser (e.g. `http://openhab:8080/amazonechocontrol/`), click the link for your account thing and login.
1. You should see now a message that the login was successful
1. If you encounter redirect/page refresh issues, enable two-factor authentication (2FA) on your Amazon account.

## Discovery

After configuration of the account thing with the login data, the echo devices registered in the Amazon account, get discovered.
If the device type is not known by the binding, the device will not be discovered.
But you can define any device listed in your Alexa app with the best matching existing device (e.g. echo).
You will find the required serial number in settings of the device in the Alexa app.

### Discover Smart Home Devices

If you want to discover your smart home devices you need to activate it in the 'Amazon Account' thing.
Devices from other skills can be discovered too.
See section _Smart Home Devices_ below for more information.

## Configuration

### `account` Bridge Configuration

| Configuration name              | Default | Description                                                                                                                                                                                                      |
|---------------------------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `discoverSmartHome`             | 0       | 0...No discover, 1...Discover direct connected, 2...Discover direct and Alexa skill devices, 3...Discover direct, Alexa and openHAB skill devices                                                                |
| `pollingIntervalSmartHomeAlexa` | 30      | Defines the time in seconds for openHAB to pull the state of the Alexa connected devices. The minimum is 10 seconds.                                                                                             |
| `pollingIntervalSmartSkills`    | 120     | Defines the time in seconds for openHAB to pull the state of the over a skill connected devices. The minimum is 60 seconds.                                                                                      |
| `activityRequestDelay`          | 10      | The number of seconds between a voice command was detected and the received command is requested from the server. The minimum is 2 seconds. Lower values improve response time but may result in loss of events. |

### Channels

| Channel Type ID | Item Type | Access Mode | Thing Type | Description                                      |
|-----------------|-----------|-------------|------------|--------------------------------------------------|
| `sendMessage`   | String    | W           | account    | Write Only! Sends a message to the Echo devices. |

### Thing Configuration

The `echo`, `echospot`, `echoshow` and `wha` have the same configuration:

| Configuration name       | Description                                        |
|--------------------------|----------------------------------------------------|
| `serialNumber`           | Serial number of the Amazon Echo in the Alexa app  |

You will find the serial number in the Alexa app or on the webpage YOUR_OPENHAB/amazonechocontrol/YOUR_ACCOUNT (e.g. `http://openhab:8080/amazonechocontrol/account1`).

### Echo Device Channels

| Channel Type ID       | Item Type   | Access Mode | Thing Type                    | Description                                                                                                                                                                                                                             |
|-----------------------|-------------|-------------|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| player                | Player      | R/W         | echo, echoshow, echospot, wha | Control the music player  (Supported commands: PLAY or ON, PAUSE or OFF, NEXT, PREVIOUS, REWIND, FASTFORWARD)                                                                                                                           |
| volume                | Dimmer      | R/W         | echo, echoshow, echospot      | Control the volume                                                                                                                                                                                                                      |
| equalizerTreble       | Number      | R/W         | echo, echoshow, echospot      | Control the treble (value from -6 to 6)                                                                                                                                                                                                 |
| equalizerMidrange     | Number      | R/W         | echo, echoshow, echospot      | Control the midrange (value from -6 to 6)                                                                                                                                                                                               |
| equalizerBass         | Number      | R/W         | echo, echoshow, echospot      | Control the bass (value from -6 to 6)                                                                                                                                                                                                   |
| shuffle               | Switch      | R/W         | echo, echoshow, echospot, wha | Shuffle play if applicable, e.g. playing a playlist                                                                                                                                                                                     |
| imageUrl              | String      | R           | echo, echoshow, echospot, wha | Url of the album image or radio station logo                                                                                                                                                                                            |
| title                 | String      | R           | echo, echoshow, echospot, wha | Title of the current media                                                                                                                                                                                                              |
| subtitle1             | String      | R           | echo, echoshow, echospot, wha | Subtitle of the current media                                                                                                                                                                                                           |
| subtitle2             | String      | R           | echo, echoshow, echospot, wha | Additional subtitle of the current media                                                                                                                                                                                                |
| providerDisplayName   | String      | R           | echo, echoshow, echospot, wha | Name of the music provider                                                                                                                                                                                                              |
| bluetoothMAC          | String      | R/W         | echo, echoshow, echospot      | Bluetooth device MAC. Used to connect to a specific device or disconnect if an empty string was provided                                                                                                                                |
| bluetooth             | Switch      | R/W         | echo, echoshow, echospot      | Connect/Disconnect to the last used bluetooth device (works after a bluetooth connection was established after the openHAB start)                                                                                                       |
| bluetoothDeviceName   | String      | R           | echo, echoshow, echospot      | User friendly name of the connected bluetooth device                                                                                                                                                                                    |
| radio                 | Switch      | R/W         | echo, echoshow, echospot, wha | Start playing of the last used TuneIn radio station (works after the radio station started after the openHAB start)                                                                                                                     |
| remind                | String      | W           | echo, echoshow, echospot      | Write Only! Speak the reminder and sends a notification to the Alexa app                                                                                                                                                                |
| nextReminder          | DateTime    | R           | echo, echoshow, echospot      | Next reminder on the device                                                                                                                                                                                                             |
| playAlarmSound        | String      | W           | echo, echoshow, echospot      | Write Only! Plays an Alarm sound                                                                                                                                                                                                        |
| nextAlarm             | DateTime    | R           | echo, echoshow, echospot      | Next alarm on the device                                                                                                                                                                                                                |
| nextMusicAlarm        | DateTime    | R           | echo, echoshow, echospot      | Next music alarm on the device                                                                                                                                                                                                          |
| nextTimer             | DateTime    | R           | echo, echoshow, echospot      | Next timer on the device                                                                                                                                                                                                                |
| startRoutine          | String      | W           | echo, echoshow, echospot      | Write Only! Type in what you normally say to Alexa without the preceding "Alexa,"                                                                                                                                                       |
| musicProviderId       | String      | R/W         | echo, echoshow, echospot      | Current Music provider                                                                                                                                                                                                                  |
| playMusicVoiceCommand | String      | W           | echo, echoshow, echospot      | Write Only! Voice command as text. E.g. 'Yesterday from the Beatles'                                                                                                                                                                    |
| startCommand          | String      | W           | echo, echoshow, echospot      | Write Only! Used to start anything. Available options: Weather, Traffic, GoodMorning, SingASong, TellStory, FlashBriefing and FlashBriefing.{FlashbriefingDeviceID} (Note: The options are case sensitive)                             |
| announcement          | String      | W           | echo, echoshow, echospot      | Write Only! Display the announcement message on the display. See in the tutorial section to learn how it’s possible to set the title and turn off the sound.                                                                            |
| textToSpeech          | String      | W           | echo, echoshow, echospot      | Write Only! Write some text to this channel and Alexa will speak it. It is possible to use plain text or SSML: e.g. `<speak>I want to tell you a secret.<amazon:effect name="whispered">I am not a real human.</amazon:effect></speak>` |
| textToSpeechVolume    | Dimmer      | R/W         | echo, echoshow, echospot      | Volume of the textToSpeech channel, if 0 the current volume will be used                                                                                                                                                                |
| textCommand           | String      | W           | echo, echoshow, echospot      | Write Only! Execute a text command (like a spoken text)                                                                                                                                                                                 |
| lastVoiceCommand      | String      | R           | echo, echoshow, echospot      | Last voice command spoken to the device.                                                                                                                                                                                                |
| lastSpokenText        | String      | R           | echo, echoshow, echospot      | Last spoken text from the device. (for example statements, answers and text to speeches)                                                                                                                                               |
| mediaProgress         | Dimmer      | R/W         | echo, echoshow, echospot      | Media progress in percent                                                                                                                                                                                                               |
| mediaProgressTime     | Number:Time | R/W         | echo, echoshow, echospot      | Media play time                                                                                                                                                                                                                         |
| mediaLength           | Number:Time | R           | echo, echoshow, echospot      | Media length                                                                                                                                                                                                                            |
| notificationVolume    | Dimmer      | R           | echo, echoshow, echospot      | Notification volume                                                                                                                                                                                                                     |
| ascendingAlarm        | Switch      | R/W         | echo, echoshow, echospot      | Ascending alarm up to the configured volume                                                                                                                                                                                             |
| doNotDisturb          | Switch      | R/W         | echo, echoshow, echospot      | Do Not Disturb mode enabled                                                                                                                                                                                                             |

## Advanced Feature Technically Experienced Users

The url <YOUR_OPENHAB>/amazonechocontrol/<YOUR_ACCOUNT>/PROXY/<API_URL> provides a proxy server with an authenticated connection to the Amazon Alexa server.
This can be used to call Alexa API from rules.

E.g. to read out the history call from an installation on openhab:8080 with an account named account1:

`http://openhab:8080/amazonechocontrol/account1/PROXY/api/activities?startTime=&size=50&offset=1`

### Example

#### echo.things

```java
Bridge amazonechocontrol:account:account1 "Amazon Account" @ "Accounts" [discoverSmartHome=2, pollingIntervalSmartHomeAlexa=30, pollingIntervalSmartSkills=120]
{
    Thing echo                 echo1                    "Alexa" @ "Living Room" [serialNumber="SERIAL_NUMBER"]
    Thing echoshow             echoshow1                "Alexa" @ "Kitchen" [serialNumber="SERIAL_NUMBER"]
    Thing echospot             echospot1                "Alexa" @ "Sleeping Room" [serialNumber="SERIAL_NUMBER"]
    Thing wha                  wha1                     "Ground Floor Music Group" @ "Music Groups" [serialNumber="SERIAL_NUMBER"]
    Thing flashbriefingprofile flashbriefing1           "Flash Briefing Technical" @ "Flash Briefings"
    Thing flashbriefingprofile flashbriefing2           "Flash Briefing Life Style" @ "Flash Briefings"
    Thing smartHomeDevice      smartHomeDevice1         "Smart Home Device 1" @ "Living Room" [id="ID"]
    Thing smartHomeDevice      smartHomeDevice2         "Smart Home Device 2" @ "Living Room" [id="ID"]
    Thing smartHomeDevice      smartHomeDevice3         "Smart Home Device 3" @ "Living Room" [id="ID"]
    Thing smartHomeDeviceGroup smartHomeDeviceGroup1    "Living Room Group" @   "Living Room" [id="ID"]}
```

#### echo.items

Sample for the Thing echo1 only. But it will work in the same way for the other things, only replace the thing name in the channel link.
Take a look in the channel description above to know, which channels are supported by your thing type.

```java
// Account
String Echo_Living_Room_SendMessage             "SendMessage"                           {channel="amazonechocontrol:account:account1:sendMessage"}

Group Alexa_Living_Room <player>

// Player control
Player Echo_Living_Room_Player                  "Player"                                (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:player"}
Dimmer Echo_Living_Room_Volume                  "Volume [%.0f %%]" <soundvolume>        (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:volume"}
Number Echo_Living_Room_Treble                  "Treble"                                (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:equalizerTreble"}
Number Echo_Living_Room_Midrange                "Midrange"                              (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:equalizerMidrange"}
Number Echo_Living_Room_Bass                    "Bass"                                  (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:equalizerBass"}
Switch Echo_Living_Room_Shuffle                 "Shuffle"                               (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:shuffle"}

// Media channels
Dimmer Echo_Living_Room_MediaProgress           "Media progress"                        (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:mediaProgress"}
Number:Time Echo_Living_Room_MediaProgressTime  "Media progress time [%d %unit%]"       (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:mediaProgressTime"}
Number:Time Echo_Living_Room_MediaLength        "Media length [%d %unit%]"              (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:mediaLength"}

// Player Information
String Echo_Living_Room_ImageUrl                "Image URL"                             (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:imageUrl"}
String Echo_Living_Room_Title                   "Title"                                 (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:title"}
String Echo_Living_Room_Subtitle1               "Subtitle 1"                            (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:subtitle1"}
String Echo_Living_Room_Subtitle2               "Subtitle 2"                            (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:subtitle2"}
String Echo_Living_Room_ProviderDisplayName     "Provider"                              (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:providerDisplayName"}

// Music provider and start command
String Echo_Living_Room_MusicProviderId         "Music Provider Id"                     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:musicProviderId"}
String Echo_Living_Room_PlayMusicCommand        "Play music voice command (Write Only)" (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:playMusicVoiceCommand"}
String Echo_Living_Room_StartCommand            "Start Information"                     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:startCommand"}

// Bluetooth
String Echo_Living_Room_BluetoothMAC            "Bluetooth MAC Address" <bluetooth>     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetoothMAC"}
Switch Echo_Living_Room_Bluetooth               "Bluetooth"             <bluetooth>     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetooth"}
String Echo_Living_Room_BluetoothDeviceName     "Bluetooth Device"      <bluetooth>     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetoothDeviceName"}

// Commands
String Echo_Living_Room_Announcement            "Announcement"                          (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:announcement"}
String Echo_Living_Room_TTS                     "Text to Speech"                        (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:textToSpeech"}
Dimmer Echo_Living_Room_TTS_Volume              "Text to Speech Volume"                 (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:textToSpeechVolume"}
String Echo_Living_Room_Remind                  "Remind"                                (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:remind"}
String Echo_Living_Room_PlayAlarmSound          "Play Alarm Sound"                      (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:playAlarmSound"}
String Echo_Living_Room_StartRoutine            "Start Routine"                         (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:startRoutine"}
Dimmer Echo_Living_Room_NotificationVolume      "Notification volume"                   (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:notificationVolume"}
Switch Echo_Living_Room_AscendingAlarm          "Ascending alarm"                       (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:ascendingAlarm"}

// Feedbacks
String Echo_Living_Room_LastVoiceCommand        "Last voice command"                    (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:lastVoiceCommand"}
String Echo_Living_Room_LastSpokenText          "Last spoken text"                      (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:lastSpokenText"}
DateTime Echo_Living_Room_NextReminder          "Next reminder"                         (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:nextReminder"}
DateTime Echo_Living_Room_NextAlarm             "Next alarm"                            (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:nextAlarm"}
DateTime Echo_Living_Room_NextMusicAlarm        "Next music alarm"                      (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:nextMusicAlarm"}
DateTime Echo_Living_Room_NextTimer             "Next timer"                            (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:nextTimer"}
```

#### echo.sitemap

```java
sitemap amazonechocontrol label="Echo Devices"
{
        Frame label="Alexa" {
            Default   item=Echo_Living_Room_Player
            Slider    item=Echo_Living_Room_Volume
            Setpoint  item=Echo_Living_Room_Volume   minValue=0  maxValue=100 step=5
            Setpoint  item=Echo_Living_Room_Treble   minValue=-6 maxValue=6   step=1
            Setpoint  item=Echo_Living_Room_Midrange minValue=-6 maxValue=6   step=1
            Setpoint  item=Echo_Living_Room_Bass     minValue=-6 maxValue=6   step=1
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

            Text    item=Echo_Living_Room_BluetoothMAC
            // Change the <YOUR_DEVICE_MAC> Place holder with the MAC address shown, if Alexa is connected to the device
            Selection item=Echo_Living_Room_BluetoothMAC mappings=[ ''='Disconnected', '<YOUR_DEVICE_MAC>'='Bluetooth Device 1', '<YOUR_DEVICE_MAC>'='Bluetooth Device 2']

            // These are only view of the possible options.
            Selection item=Echo_Living_Room_PlayAlarmSound mappings=[ ''='None', 'ECHO:system_alerts_soothing_01'='Adrift', 'ECHO:system_alerts_atonal_02'='Clangy']

            Switch  item=Echo_Living_Room_Bluetooth
            Text    item=Echo_Living_Room_BluetoothDeviceName
            Text    item=Echo_Living_Room_LastVoiceCommand
            Text    item=Echo_Living_Room_LastSpokenText
            Slider  item=Echo_Living_Room_NotificationVolume
            Switch  item=Echo_Living_Room_AscendingAlarm
        }
}
```

### `flashbriefingprofile` Thing Configuration

The `flashbriefingprofile` has no configuration parameters.
It will be configured at runtime by using the save channel to store the current flash briefing configuration in the thing. Create a `flashbriefingprofile` Thing for each set you need.
E.g. One Flashbriefing profile with technical news and wheater, one for playing world news and one for sport news.

Flash briefings are sets of information that can be configured in your Alexa app.
The app only allows to have one flash-briefing configuration at the same time (e.g. weather and news).
The `flashbriefingprofile` thing helps you to overcome this limitation.

To set it up using managed (UI) configuration:

1. Use the app to create the flash-briefing configuration you want.
1. Start the discovery, you should see a new "flashbriefingprofile" thing. If this is not the case, a thing with the current configuration already exists.
1. Add that thing (you can use a custom name if you want).
1. Repeat steps 1-3 for other configurations (you can add as many as you want, make sure they are different).

Textual configuration (untested, not recommended):

1. Add a new `flashbriefiungprofilething` to your `.things` file.
1. Use the app to create the flash-briefing configuration you want.
1. Send `ON` to the `save` channel of the thing you created in step 1.
1. Repeat steps 1-3 for other configurations (you can add as many as you want, make sure they are different).

### Flash Briefing Profile Channels

| Channel Type ID | Item Type | Access Mode | Description                                                                                                                                   |
|-----------------|-----------|:-----------:|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `save`          | Switch    |      W      | Write Only! Stores the current configuration of flash briefings.                                                                              |
| `active`        | Switch    |     R/W     | Activates this flash briefing as default ON ALL DEVICES.                                                                                      |
| `playOnDevice`  | String    |      W      | Specify the echo serial number or name to start the flash-briefing. This is only exceuted once and the default configuration does not change. |

**Attention:** Be careful when using the `save` channel.
Storing the same configuration to several things may result in unpredictable behavior.

### Flash Briefing Example

#### flashbriefings.things

```java
Bridge amazonechocontrol:account:account1 "Amazon Account" @ "Accounts" [discoverSmartHome=2]
{
    Thing flashbriefingprofile flashbriefing1 "Flash Briefing Technical" @ "Flash Briefings"
    Thing flashbriefingprofile flashbriefing2 "Flash Briefing Life Style" @ "Flash Briefings"
}
```

#### flashbriefings.items

```java
// Flashbriefings
Switch FlashBriefing_Technical_Save   "Save (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing1:save"}
Switch FlashBriefing_Technical_Active "Active" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing1:active"}
String FlashBriefing_Technical_Play   "Play (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing1:playOnDevice"}

Switch FlashBriefing_LifeStyle_Save   "Save (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing2:save"}
Switch FlashBriefing_LifeStyle_Active "Active" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing2:active"}
String FlashBriefing_LifeStyle_Play   "Play (Write only)" { channel="amazonechocontrol:flashbriefingprofile:account1:flashbriefing2:playOnDevice"}
```

#### flashbriefings.sitemap

```java
sitemap flashbriefings label="Flash Briefings"
{
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
}
```

### `smartHomeDevice` and `smartHomeDeviceGroup` Thing Configuration

| Configuration name       | Description                                                               |
|--------------------------|---------------------------------------------------------------------------|
| id                       | The id of the device or device group                                      |

The only possibility to find out the id is using discovery function in the UI.
You can use that id if you want to define the thing in a file.

1. Open the url YOUR_OPENHAB/amazonechocontrol in your browser (e.g. `http://openhab:8080/amazonechocontrol/`)
1. Click on the name of the account thing
1. Click on the name of the echo thing
1. Scroll to the channel and copy the required ID

Discovered smart home devices show a `deviceIdentifierList` in their thing properties, containing one or more serial numbers.
You can check if any of these serial numbers is associated with another device and use this to identify devices with similar/same names.

### Smart Home Device Channels

The channels of the smarthome devices will be generated at runtime.
Check in the UI thing configurations, which channels are created.

| Channel ID               | Item Type            | Access Mode | Thing Type                            | Description                                                                                                                 |
|--------------------------|----------------------|-------------|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| powerState               | Switch               | R/W         | smartHomeDevice, smartHomeDeviceGroup | Shows and changes the state (ON/OFF) of your device                                                                         |
| brightness               | Dimmer               | R/W         | smartHomeDevice, smartHomeDeviceGroup | Shows and changes the brightness of your lamp                                                                               |
| color                    | Color                | R/(W)       | smartHomeDevice, smartHomeDeviceGroup | Shows the color of your light                                                                                               |
| colorName                | String               | R/W         | smartHomeDevice, smartHomeDeviceGroup | Shows and changes the color name of your light (groups are not able to show their color)                                    |
| colorTemperatureInKelvin | Number:Temperature   | R/W         | smartHomeDevice, smartHomeDeviceGroup | Shows the color temperature of your light                                                                                   |
| colorTemperatureName     | String               | R/W         | smartHomeDevice, smartHomeDeviceGroup | White temperatures name of your lights (groups are not able to show their color)                                            |
| armState                 | String               | R/W         | smartHomeDevice, smartHomeDeviceGroup | State of your alarm guard. Options: ARMED_AWAY, ARMED_STAY, ARMED_NIGHT, DISARMED (groups are not able to show their state) |
| burglaryAlarm            | Contact              | R           | smartHomeDevice                       | Burglary alarm                                                                                                              |
| carbonMonoxideAlarm      | Contact              | R           | smartHomeDevice                       | Carbon monoxide detection alarm                                                                                             |
| fireAlarm                | Contact              | R           | smartHomeDevice                       | Fire alarm                                                                                                                  |
| waterAlarm               | Contact              | R           | smartHomeDevice                       | Water alarm                                                                                                                 |
| glassBreakDetectionState | Contact              | R           | smartHomeDevice                       | Glass break detection alarm                                                                                                 |
| smokeAlarmDetectionState | Contact              | R           | smartHomeDevice                       | Smoke detection alarm                                                                                                       |
| temperature              | Number               | R           | smartHomeDevice                       | Temperature                                                                                                                 |
| targetSetpoint           | Number:Temperature   | R/W         | smartHomeDevice                       | Thermostat Setpoint                                                                                                         |
| upperSetpoint            | Number:Temperature   | R/W         | smartHomeDevice                       | Thermostat Upper Setpoint                                                                                                   |
| lowerSetpoint            | Number:Temperature   | R/W         | smartHomeDevice                       | Thermostat Lower Setpoint                                                                                                   |
| relativeHumidity         | Number:Dimensionless | R           | smartHomeDevice                       | Thermostat humidity                                                                                                         |
| thermostatMode           | String               | R/W         | smartHomeDevice                       | Thermostat Mode (`AUTO`, `COOL`, `HEAT`, `OFF`, `ECO`)                                                                      |
| motionDetected           | Switch               | R           | smartHomeDevice                       | A motion was detected if ON                                                                                                 |
| contact                  | Contact              | R           | smartHomeDevice                       | A contact sensor OPEN if detected, CLOSED if NOT_DETECTED                                                                   |
| geoLocation              | Location             | R           | smartHomeDevice                       | The location (e.g. of a Tile)                                                                                               |

**Note:** the channels of `smartHomeDevices` and `smartHomeDeviceGroup` will be created dynamically based on the capabilities reported by the Amazon server. This can take a little bit of time.
The polling interval configured in the Account Thing to get the state is specified in minutes and has a minimum of 10. This means it takes up to 10 minutes to see the state of a channel. The reason for this low interval is, that the polling causes a big server load for the Smart Home Skills.

**Note:** The `color` channel is read-only by default because Alexa does only support setting colors by their name.
It has a configuration parameter `matchColors` which enables writing to that channel and tries to find the closes available color when sending a command to Alexa.

### Smart Home Example

#### smarthome.things

```java
Bridge amazonechocontrol:account:account1 "Amazon Account" @ "Accounts" [discoverSmartHome=2, pollingIntervalSmartHomeAlexa=30, pollingIntervalSmartSkills=120]
{
    Thing smartHomeDevice      smartHomeDevice1 "Smart Home Device 1" @ "Living Room" [id="ID"]
    Thing smartHomeDevice      smartHomeDevice2 "Smart Home Device 2" @ "Living Room" [id="ID"]
    Thing smartHomeDevice      smartHomeDevice3 "Smart Home Device 3" @ "Living Room" [id="ID"]
    Thing smartHomeDeviceGroup smartHomeDeviceGroup1 "Living Room Group" @   "Living Room" [id="ID"]
}
```

#### smarthome.items

Sample for the Thing echo1 only. But it will work in the same way for the other things, only replace the thing name in the channel link.
Take a look in the channel description above to know which channels are supported by your thing type.

```java
// Lights and lightgroups
Switch Light_State "On/Off"            { channel="amazonechocontrol:smartHomeDevice:account1:smartHomeDevice1:powerState" }
Dimmer Light_Brightness "Brightness"   { channel="amazonechocontrol:smartHomeDevice:account1:smartHomeDevice1:brightness" }
Color  Light_Color "Color"             { channel="amazonechocontrol:smartHomeDevice:account1:smartHomeDevice1:color" }
String Light_Color_Name "Color Name"   { channel="amazonechocontrol:smartHomeDevice:account1:smartHomeDevice1:colorName" }
String Light_White "White temperature" { channel="amazonechocontrol:smartHomeDevice:account1:smartHomeDevice1:colorTemperatureName" }

// Smart plugs
Switch Plug_State "On/Off"             { channel="amazonechocontrol:smartHomeDevice:account1:smartHomeDevice2:powerState" }

// Alexa Guard
Switch Arm_State "State"               { channel="amazonechocontrol:smartHomeDevice:account1:smartHomeDevice3:armState" }

// Smart Home device group
Switch Group_State "On/Off"            { channel="amazonechocontrol:smartHomeDeviceGroup:account1:smartHomeDeviceGroup1:powerState" }
```

The only possibility to find out the id for the smartHomeDevice and smartHomeDeviceGroup Things is by using the discover function.

#### smarthome.sitemap

```java
sitemap smarthome label="Smart Home Devices"
{
        Frame label="Lights and light groups" {
            Switch item=Light_State
            Slider item=Light_Brightness
            Default item=Light_Color
            Selection item=Light_Color_Name mappings=[ ''='', 'red'='Red', 'crimson'='Crimson', 'salmon'='Salmon', 'orange'='Orange', 'gold'='Gold', 'yellow'='Yellow', 'green'='Green', 'turquoise'='Turquoise', 'cyan'='Cyan', 'sky_blue'='Sky Blue', 'blue'='Blue', 'purple'='Purple', 'magenta'='Magenta', 'pink'='Pink', 'lavender'='Lavender' ]
            Selection item=Light_White mappings=[ ''='', 'warm_white'='Warm white', 'soft_white'='Soft white', 'white'='White', 'daylight_white'='Daylight white', 'cool_white'='Cool white' ]
            Switch item=Light_State
            Switch item=Group_State
            Selection item=Arm_State mappings=[ 'ARMED_AWAY'='Active', 'ARMED_STAY'='Present', 'ARMED_NIGHT'='Night', 'DISARMED'='Deactivated' ]
        }
}
```

#### Rule for calculating the distance of a Tile from your home

Link the `geoLocation` channel of the Tile thing to a `Location` item named `CarLocation`.
Add a second item of type `Number:Length` with the name `CarDistance` (adjust state description to your needs, e.g. miles or km as unit).
Create a rule that triggers on change of that item with the DSL script as action:

```javascript
var homeLocation = new PointType("50.273448, 8.409950")
CarDistance.postUpdate(homeLocation.distanceFrom(CarLocation.state as PointType).toString + " m")
```

## Advanced Features for Technically Experienced Users

The url <YOUR_OPENHAB>/amazonechocontrol/<YOUR_ACCOUNT>/PROXY/<API_URL> provides a proxy server with an authenticated connection to the Amazon Alexa server.
This can be used to call Alexa API from rules.

E.g. to read out the history call from an installation on openhab:8080 with an account named account1:

`http://openhab:8080/amazonechocontrol/account1/PROXY/api/activities?startTime=&size=50&offset=1`

To resolve login problems the connection settings of an `account` thing can be reset via the karaf console.
The command `amazonechocontrol listAccounts` shows a list of all available `account` things.
The command `amazonechocontrol resetAccount <id>` resets the device id and all other connection settings.
After resetting a connection, a new login as described above is necessary.

## Tutorials

### Let Alexa speak a text from a rule

1) Create a rule with a trigger of your choice

```java
rule "Say welcome if the door opens"
when
    Item Door_Contact changed to OPEN
then
    Echo_Living_Room_TTS.sendCommand('Hello World')
end
```

You can also use [SSML](https://docs.aws.amazon.com/polly/latest/dg/supported-ssml.html) to provide a better voice experience

```java
rule "Say welcome if the door opens"
when
    Item Door_Contact changed to OPEN
then
    Echo_Living_Room_TTS.sendCommand('<speak>I want to tell you a secret.<amazon:effect name="whispered">I am not a real human.</amazon:effect>.Can you believe it?</speak>')
end
```

### Show an announcement on the echo show or echo spot

1) Create a rule with a trigger of your choice

Simple:

```java
rule "Say welcome if the door opens"
when
    Item Door_Contact changed to OPEN
then
    Echo_Living_Room_Announcement.sendCommand('Door opened')
end
```

Expert:

You can use a json formatted string to control title, sound and volume:

```json
{ "sound": true, "speak":"<Speak>", "title": "<Title>", "body": "<Body Text>", "volume": 20}
```

The combination of `sound=true` and `speak` in SSML syntax is not allowed.
Not all properties need to be specified.
The value for `volume` can be between 0 and 100 to set the volume.
A volume value smaller then 0 means that the current alexa volume should be used.
No specification uses the volume from the `textToSpeechVolume` channel.

Note: If you turn off the sound and Alexa is playing music, it will anyway turn down the volume for a moment. This behavior can not be changed.

```java
rule "Say welcome if the door opens"
when
    Item Door_Contact changed to OPEN
then
    Echo_Living_Room_Announcement.sendCommand('{ "sound": false, "title": "Doorstep", "body": "Door opened"}')
end
```

## Playing an alarm sound for 15 seconds with an openHAB rule if a door contact was opened

1. Do get the ID of your sound, follow the steps in "How To Get IDs"
1. Write down the text in the square brackets. e.g. ECHO:system_alerts_repetitive01 for the nightstand sound
1. Create a rule for start playing the sound:

```java
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

Note 1: Do not use a to short time for playing the sound, because Alexa needs some time to start playing the sound.
It is not recommended to use a time below 10 seconds.

Note 2: The rule have no effect for your default alarm sound used in the Alexa app.

### Play a spotify playlist if a switch was changed to on

1. Do get the ID of your sound, follow the steps in "How To Get IDs"
1. Write down the text in the square brackets. e.g. SPOTIFY for the spotify music provider
1. Create a rule for start playing a song or playlist:

```java
rule "Play a playlist on spotify if a switch was changed"
when
    Item Spotify_Playlist_Switch changed to ON
then
    Echo_Living_Room_PlayMusicProvider.sendCommand('SPOTIFY')
    Echo_Living_Room_PlayMusicCommand.sendCommand('Playlist Party')
end
```

Note: It is recommended to test the command send to play music command first with the voice and the real Alexa device. E.g. say 'Alexa, Playlist Party'

### Start playing weather/traffic/etc

1. Pick up one of the available commands: Weather, Traffic, GoodMorning, SingASong, TellStory, FlashBriefing
1. Create a rule for start playing the information where you provide the command as string:

```java
rule "Start wheater info"
when
    Item Spotify_Start_Wheater_Switch changed to ON
then
     Echo_Living_Room_StartCommand.sendCommand('Weather')
end
```

### Start playing a custom flashbriefing on a device

1. Do get the ID of your sound, follow the steps in "How To Get IDs"
1. Write down the text in the square brackets. e.g. flashbriefing.flashbriefing1
1. Create a rule for start playing the information where you provide the command as string:

```java
rule "Start wheater info"
when
    Item Spotify_Start_Wheater_Switch changed to ON
then
     Echo_Living_Room_StartCommand.sendCommand('FlashBriefing.flashbriefing1')
end
```

## Note

This binding uses the same API as the Web-Browser-Based Alexa site (alexa.amazon.de).
In other words, it simulates a user which is using the web page.
Unfortunately, this binding can break if Amazon changes the web site.

The binding is tested with amazon.de, amazon.fr, amazon.it, amazon.com and amazon.co.uk accounts, but should also work with all others.

## Credits

The idea for writing this binding came from this blog: [https://blog.loetzimmer.de/2017/10/amazon-alexa-hort-auf-die-shell-echo.html](https://blog.loetzimmer.de/2017/10/amazon-alexa-hort-auf-die-shell-echo.html) (German).
Thank you Alex!
The technical information for the web socket connection to get live Alexa state updates cames from Ingo.
He has done the [Alexa ioBroker implementation](https://github.com/Apollon77)
Thank you Ingo!

## Trademark Disclaimer

TuneIn, Amazon Echo, Amazon Echo Spot, Amazon Echo Show, Amazon Music, Amazon Prime, Alexa and all other products and Amazon, TuneIn and other companies are trademarks™ or registered® trademarks of their respective holders.
Use of them does not imply any affiliation with or endorsement by them.
