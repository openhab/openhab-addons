# MagentaTV Binding

This binding allows controlling the Deutsche Telekom Magenta TV Media Receiver series MR4xx and MR2xx (Telekom NGTV / Huawei Envision platform).
The binding does NOT support MR3xx/1xx (old Entertain system based on Microsoft technology)!

Media Receivers are automatically discovered.
You can send keys as you press them on the remote and the binding receives program information when the channel is switched.
The binding provides device discovery, sending keys for the remote and also receiving program information/events.

## Supported Things

|Thing    |Type                                                                    |
|---------|----------------------------------------------------------------------- |
|receiver |A MagentaTV Receiver, the binding supports multiple models (see above). |

### Supported Models

| Model                                  | Status                                        |
|----------------------------------------|-----------------------------------------------|
| Deutsche Telekom Media Receiver MR401B | fully supported                               |
| Deutsche Telekom Media Receiver MR201  | fully supported                               |
| Deutsche Telekom Media Receiver MR400  | supported with minor restrictions (POWER key) |
| Deutsche Telekom Media Receiver MR200  | supported with minor restrictions (POWER key) |
| Deutsche Telekom Media Receiver MR601  | should be supported (not verified)            |
| Deutsche Telekom Media Receiver MR3xx  | NOT supported (different platform)            |
| Deutsche Telekom Media Receiver MR1xx  | NOT supported (different platform)            |

## Auto Discovery

UPnP will be used to discover receivers on the local network and discover the necessary parameters.
The receiver needs to be powered on to get discovered.

Once the receiver is discovered it can be added from the Inbox.
Make sure to set `userId` in the Thing configuration after adding the new thing, see section Thing Configuration.

Note:
The binding uses the network settings in openHAB's system configuration to determine the local IP address.
The device can't be discovered if the openHAB system and receiver are not on the same network (IP/Netmask).
In this case you need to add the Thing manually or use textual configuration (.things).

If you are running openHAB in a Docker container you need to make sure that UPnP discovery is available and network interfaces

## Receiver Standby Mode

The Media receiver has 3 different standby modes, which can be selected in the receiver's settings menu.

|Mode          |Description                                                                                 |
|--------------|--------------------------------------------------------------------------------------------|
|Standby       |Full standby - the receiver is active all the time, even while sleeping, so it can wake up instantly.|
|Suspend/Resume|The receiver goes to sleep mode, but can be awakened by a Wake-on-LAN packet.               |
|Shutdown      |Powering off shuts down the receiver, so that it can be awakened only with the power button.|

`Standby` provides the best results, because the binding can wake up the receiver (Power On/Off).
`Suspend/Resume` requires a Wake-on-LAN packet, which can take longer.
`Shutdown` turns the receiver off, which requires a manual power-on.

There is no way to detect the "display status" of the receiver.
The binding detects Power-Off with the MR401B/MR201 by listening to UPnP events, but can't verify the status when started.
You need to take care on the current status if you power on/off the receiver from scenes.
Check the current status before sending the POWER button, because POWER is a toggle, not ON or OFF (see sample rules).

## Thing Configuration

|Parameter       |Description                                                                                                     |
|----------------|----------------------------------------------------------------------------------------------------------------|
|accountName     |Login Name (email), should be the registered e-mail address for the Telekom Kundencenter                        |
|accountPassword |Account password (same as for the Kundencenter)                                                                 |
|userId          |The technical userId required for the pairing process, see section "Retrieving userId"                      |
|ipAddress       |IP address of the receiver, usually discovered by UPnP                                                          |
|port            |Port to reach the remote service, usually 8081 for the MR401/MR201 or 49152 for MR400/200                       |
|udn             |UPnP Unique Device Name - a hex ID, which includes the 12 digit MAC address at the end (parsed by the binding)  |

For textual configuration at least the `ipAddress`, `udn` and `userId` parameters are required.

### Retrieving the User ID

The binding requires a so called User ID (parameter `userId` in the Thing configuration) to pair with the receiver, which is generated by logging in with your Telekom credentials (Login Name + Password for the "Telekom Kundencenter").
openHAB needs to access the Internet to perform that operation, which can be disabled afterwards.

The binding initiates the pairing with the receiver once the U has been obtained, this is an automated process.
The Thing changes to ONLINE once the pairing result is received, otherwise check the Thing status for an error message.

There are different ways to setup the User ID:

1. Use the openHAB console

Run the following command on the console and provide your Telekom account credentials:

```shell
openhab> openhab:magentatv login
Username (email): mail@example.com
Password: topsecret

Attempting login...
Login successful, returned User ID is 1903AAAAAAAAC7E9718BBBCBCBCBCBC
Edit thing configuration and copy this value to the field userId
```

On successful login the console will show the User ID value. Copy&amp;Paste this value to the Thing configuration (parameter `userId`) of the receiver.
If you have multiple receivers under the same MagentaTV subscription you can use this value for all of them.

1. Provide your credentials in the UI

If you do not want to use the openHAB console, you can also setup the credentials in the Thing configuration

- Account Name (`accountName`) is your Login Name for the Telekom Kundencenter (registered email address)
- Account Password (`accountPassword`) is the corresponding password.

The binding uses these credentials to login to your account, retrieves the `userId` parameter and sets it in the Thing configuration.
For security reasons the credentials are automatically deleted from the thing configuration after the initial process.

## Channels

|Group   |Channel        |Item-Type|Description                                                               |
|--------|---------------|---------|--------------------------------------------------------------------------|
|control |power          |Switch   |Toggle power state (same as sending "POWER" to the key channel), see note.|
|        |channel        |Number   |Select program channel (outbound only, current channel is not available)  |
|        |player         |Player   |Send commands to the receiver - see below                                 |
|        |key            |String   |Send key code to the receiver (see code table below)                      |
|        |mute           |Switch   |Mute volume (mute the speaker)                                            |
|status  |playMode       |String   |Current play mode - this info is not reliable                             |
|        |channelCode    |Number   |The channel code from the EPG.                                            |
|program |title          |String   |Title of the running program or video being played                        |
|        |text           |String   |Some description (as reported by the receiver, could be empty)            |
|        |start          |DateTime |Time when the program started                                             |
|        |position       |Number   |Position in minutes within a movie.                                       |
|        |duration       |Number   |Remaining time in minutes, usually not updated for TV program             |

Please note:

- POWER is a toggle button, not an on/off switch.
The binding tries to detect and maintain the correct state, but due to device limitations this is not always possible.
Make sure the receiver's and binding's state are in sync when OH is restarted (binding assumes state is OFF).
- Channels receiving event information get updated when changing the channel or playing a video.

There is no way to read the current status, therefore they don't get initialized on startup nor being updated in real-time.

The player channel supports the following actions:

|Channel |Command        |Description                       |
|--------|---------------|----------------------------------|
|player  |PLAY           |Start playing media               |
|        |PAUSE          |Pause player                      |
|        |NEXT           |Move to the next chapter          |
|        |PREVIOUS       |Move to the previous chapter      |
|        |FASTFORWARD    |Switch to forward mode            |
|        |REWIND         |Switch to rewind mode             |
|        |ON or OFF      |Toggle power - see notes on power |

## Supported Key Code (channel key)

| Key      | Description                                    |
| ---------|------------------------------------------------|
| POWER    | Power on/off the receiver (check standby mode) |
| 0..9     | Key 0..9                                       |
| SPACE    | Space key                                      |
| POUND    | # key                                          |
| START    | * key                                          |
| DELETE   | Delete key (text edit)                         |
| ENTER    | Enter/Select key                               |
| RED      | Special Actions: red                           |
| GREEN    | Special Actions: green                         |
| YELLOW   | Special Actions: yellow                        |
| BLUE     | Special Actions: blue                          |
| EPG      | Electronic Program Guide                       |
| OPTION   | Display options                                |
| SETTINGS | Display options                                |
| UP       | Up arrow                                       |
| DOWN     | Down arrow                                     |
| PGUP     | Page up                                        |
| PGDOWN   | Page down                                      |
| LEFT     | Left arrow                                     |
| RIGHT    | Right arrow                                    |
| OK       | OK button                                      |
| BACK     | Return to last menu                            |
| EXIT     | Exit menu                                      |
| MENU     | Menu                                           |
| INFO     | Display information                            |
| FAV      | Display favorites                              |
| VOLUP    | Volume up                                      |
| VOLDOWN  | Volume down                                    |
| MUTE     | Mute speakers                                  |
| CHUP     | Channel up                                     |
| CHDOWN   | Channel down                                   |
| PLAY     | Play                                           |
| PAUSE    | Play                                           |
| STOP     | Stop playing                                   |
| RECORD   | Start recording                                |
| REWIND   | Rewind                                         |
| FORWARD  | Forward                                        |
| LASTCH   | Last channel                                   |
| NEXTCH   | Next channel                                   |
| LASTCHAP | Last chapter                                   |
| NEXTCHAP | Next chapter                                   |
| PAIR     | Re-pair with the receiver                      |

In addition you could send any key code in the 0xHHHH format., refer to
[Key Codes for Magenta/Huawei Media Receiver](https://support.huawei.com/hedex/pages/DOC1100366313CEH0713H/01/DOC1100366313CEH0713H/01/resources/dsv_hdx_idp/DSV/en/en-us_topic_0094619112.html)

## Full Configuraton Example

### magentatv.things

```java
Thing magentatv:receiver:XXXXXXXXXXX "MagentaTV" [
  udn="XXXXXXXXXXX",
  ipAddress="xxx.xxx.xxx.xxx",
  accountName="xxxxxx.xxxx@t-online.de",
  accountPassword="xxxxxxxxxx"
]
```

### magentatv.items

```java
# MagentaTV Control
Switch MagentaTV_Power        "Power"        {channel="magentatv:receiver:XXXXXXXXXXX:control#power"}
Number MagentaTV_Channel      "Channel"      {channel="magentatv:receiver:XXXXXXXXXXX:status#channel"}
String MagentaTV_Key          "Key"          {channel="magentatv:receiver:XXXXXXXXXXX:control#key"}

# MagentaTV Program Information
String MagentaTV_ProgTitle   "Program Title" {channel="magentatv:receiver:XXXXXXXXXXX:program#title"}
String MagentaTV_ProgDescr   "Description"   {channel="magentatv:receiver:XXXXXXXXXXX:program#text"}
String MagentaTV_ProgStart   "Start Time"    {channel="magentatv:receiver:XXXXXXXXXXX:program#tart"}
String MagentaTV_ProgDur     "Duration"      {channel="magentatv:receiver:XXXXXXXXXXX:program#duration"}
String MagentaTV_ProgPos     "Position"      {channel="magentatv:receiver:XXXXXXXXXXX:program#position"}

# MagentaTV Play Status
Number MagentaTV_ChCode    "Channel Code"    {channel="magentatv:receiver:XXXXXXXXXXX:status#channelCode"}
String MagentaTV_PlayMode  "Play Mode"       {channel="magentatv:receiver:XXXXXXXXXXX:status#playMode"}
String MagentaTV_RunStatus "Run Status"      {channel="magentatv:receiver:XXXXXXXXXXX:status#runStatus"}
```

or

```java
Group    gRB_GF_LR_TVReceiver "RB_GF_LR: TV Receiver"
         (gRB_GF_LivingRoom, gMedia, gSpeechCmnd)

Switch   RB_GF_LR_TVReceiver_Power
         "RB_GF_LR: TV Receiver power [MAP(i18n_switch.map):%s]"
         (gRB_GF_LR_TVReceiver)
         {channel="magentatv:receiver:XXXXXXXXXXX:control#power"}
Player   RB_GF_LR_TVReceiver_Control
         "RB_GF_LR: TV Receiver control"
         (gRB_GF_LR_TVReceiver)
         {channel="magentatv:receiver:XXXXXXXXXXX:control#player"}
Switch   RB_GF_LR_TVReceiver_Mute
         "RB_GF_LR: TV Receiver mute [MAP(i18n_switch.map):%s]"
         (gRB_GF_LR_TVReceiver)
         {channel="magentatv:receiver:XXXXXXXXXXX:control#mute"}
Number   RB_GF_LR_TVReceiver_Channel
         "RB_GF_LR: TV Receiver Channel"
         (gRB_GF_LR_TVReceiver)
         {channel="magentatv:receiver:XXXXXXXXXXX:control#channel"}
String   RB_GF_LR_TVReceiver_Key
         "RB_GF_LR: TV Receiver Key"
         (gRB_GF_LR_TVReceiver)
         {channel="magentatv:receiver:XXXXXXXXXXX:control#key"}
String   RB_GF_LR_TVReceiver_ProgTitle
         "Label [%s]"
         (gRB_GF_LR_TVReceiver)
         {channel="magentatv:receiver:XXXXXXXXXXX:program#title"}
String   RB_GF_LR_TVReceiver_ProgDescription
         "Label [%s]"
         (gRB_GF_LR_TVReceiver)
         {channel="magentatv:receiver:XXXXXXXXXXX:program#text"}
DateTime RB_GF_LR_TVReceiver_ProgStart
         "Label [%1$td.%1$tm.%1$ty %1$tH:%1$tM]"
         (gRB_GF_LR_TVReceiver)
         {channel="magentatv:receiver:XXXXXXXXXXX:program#start"}
Number:Time RB_GF_LR_TVReceiver_ProgDuration
         "Label [%d min]"
         (gRB_GF_LR_TVReceiver)
         {channel="magentatv:receiver:XXXXXXXXXXX:program#duration"}
Number:Time RB_GF_LR_TVReceiver_PlayPosition
         "Label [%d min.]"
         (gRB_GF_LR_TVReceiver)
         {channel="magentatv:receiver:XXXXXXXXXXX:program#position"}
String   RB_GF_LR_TVReceiver_PlayMode
         "Label [%s]"
         (gRB_GF_LR_TVReceiver)
         {channel="magentatv:receiver:XXXXXXXXXXX:status#playMode"}
```

### sitemap

```perl
Text label="TV" icon="it_television" {
  Frame label="Bedienung"  {
    Switch item=RB_GF_LR_TVReceiver_Power label="Ein/Aus []" icon="control_on_off" mappings=[ ON="Ein/Aus" ]
    Default item=RB_GF_LR_TVReceiver_Control label="Player []" icon=""
    Switch item=RB_GF_LR_TVReceiver_Key label="Lautstärke []" icon="audio_volume_high" mappings=[ "VOLUP"="˄", "VOLDOWN"="˅" ]
    Slider item=RB_GF_LR_TVReceiver_Volume label="Lautstärke [%d %%]" icon="audio_volume_high"
    Switch item=RB_GF_LR_TVReceiver_Key label="Programm []" icon="audio_playlist" mappings=[ "CHUP"="˄", "CHDOWN"="˅" ]
    Selection item=RB_GF_LR_TVReceiver_Channel label="Kanal [%s]" icon="audio_playlist" mappings=[ 1="ARD", 2="ZDF", 3="RTL", 4="SAT.1", 5="ProSieben", 6="VOX" ]
    Switch item=RB_GF_LR_TVReceiver_Mute label="Mute []" icon="audio_volume_mute" mappings=[ ON="mute", OFF="unmute" ]
    }
  Frame label="Aktuelles Programm" {
    Text item=RB_GF_LR_TVReceiver_ProgTitle label="Sendung [%s]" icon="it_television"
    Text item=RB_GF_LR_TVReceiver_ProgDescription label="Beschreibung [%s]" icon="it_television"
    Text item=RB_GF_LR_TVReceiver_ProgStart label="Start [%1$td.%1$tm.%1$ty %1$tH:%1$tM]" icon="time_clock"
    Text item=RB_GF_LR_TVReceiver_ProgDuration label="Dauer [%d min.]" icon="time_clock"
    Text item=RB_GF_LR_TVReceiver_PlayPosition label="Position [%d min.]" icon="it_television"
    Text item=RB_GF_LR_TVReceiver_PlayMode label="Mode [%s]" icon="it_television"
  }
}

```

### magentatv.rules

Due to the fact the POWER is a toggle button and the binding cannot detect the current status, which could lead into the situation that you want to power on the receiver as part of a scene, but due to the fact that it is already ON you switch it off.

Beginning with models 401/201 and new the binding is able to detect the Power-OFF condition, which can be used in a rule to improve this situation

```java
if (MagentaTV_Power.state != ON) {
    sendCommand(MagentaTV_Power, ON)
}
```

to switch it ON and

```java
if (MagentaTV_Power.state != OFF) {
    sendCommand(MagentaTV_Power, OFF)
}
```

to switch it off.

After an openHAB restart you need to make sure that OH and receiver are in sync, because the binding can't read the power status at startup.
