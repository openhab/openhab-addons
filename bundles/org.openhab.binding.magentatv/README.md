# MagentaTV Binding

This Binding allows controlling the Deutsche Telekom Magenta TV Media Receiver series MR4xx and MR2xx (Telekom NGTV / Huawei Envision platform). 
The Binding does NOT support MR3xx/1xx (old Entertain system based on Microsoft technology)!

Media Receivers are automatically discovered.
You could send keys as you press them on the remote and the binding receives program information when the channel is switched (no
The binding provides device discovery, sending keys for the remote and also receiving program information/events.

## Supported Models

* Deutsche Telekom Media Receiver MR401B - fully supported
* Deutsche Telekom Media Receiver MR201  - fully supported
* Deutsche Telekom Media Receiver MR400  - supported with minor restrictions (POWER key)
* Deutsche Telekom Media Receiver MR200  - supported with minor restrictions (POWER key)
* Deutsche Telekom Media Receiver MR3xx  - NOT supported (different platform)
* Deutsche Telekom Media Receiver MR1xx  - NOT supported (different platform)


### Receiver Standby Mode

The Media receiver has 3 different standby modes, which could be selected in the receiver's settings.

- Standby - full standby, receiver active all the time
- Suspend/Resume - The receiver goes to sleep mode, but could be awaked by a Wake-on-LAN packet
- Shutdown - Powering off will shutdown the receiver, can be awaked only with the power button

Standby provides the best results, because the binding could wake-up the receiver (Power On/Off). Suspend/Resume would require a Wake-on-LAN, which could be done, but is currently not implemented. 
Shutdown turns the receiver off, which requires a manual PowerOn.

There is no way to detect the "display status" of the receiver. 
The binding detects PowerOff with the MR401B/MR201 by listening to UPnP events, but can't verify the status when started.
You need to take care on the current status if you power on/off the receiver from scenes.
Check the current status before sending the POWER button, because POWER is a toggle, not ON or OFF (see sample rules).

### Discovery

The auto discovery starts when the binding is loaded.
UPnP will be used to discover receivers on the local network.
Based on UPnP all necessary parameters are auto-discovered.
The receiver needs to be powered on to get discovered.

### Supported Things

|Thing    |Type                                                           |
|---------|-------------------------------------------------------------- |
|receiver |A MagentaTV Receiver, the binding supports multiple receivers. |



# Device setup

Once the thing will be added from the Inbox in PaperUI you'll need your T-Online credentials to query the userID.
Open the thing configuration and enter the credentials:

|Field            |Description                                      |
|---------------- |------------------------------------------------ |
|Account Name     |Your T-Online user id, e.g. test7017@t-online.de |
|Account Password |The password for your Telekom account.           |

For security reasons the credentials will be automatically deleted from the thing configuration (replaced with '***' in the thing config) after the initial authentication process.
The openHAB instance needs access to the Internet to perform that operation, which can be disabled afterwards.

One the userID has been obtained from the Telekom portal the binding initiates the pairing with the receiver.
This is an automated process.
The thing changes to ONLINE state once the pairing result is received.
Otherwise open PaperUI:Confuguration:Things and check for an error message.
Using [Show Properties] you see more details and could verify if the discovery and pairing were completed successful.

The binding uses the network settings in openHAB system configuration to determine the local ip address. The device can't be discovered when the openHAB system and receiver are not on the same network (ip/netmask).

## Thing Configuration

|Parameter       |Description                                                                                                     |
|----------------|----------------------------------------------------------------------------------------------------------------|
|udn             |UPnP Unique Device Name - a hexa ID, which includes the 12 digit MAC address at the end (parsed by the binding) |
|modelId         |Type of Media Receiver: DMS_TPB for MR400 amd MR200 ; MR401B for MR401B and MR201                               |
|ipAddress       |IP address of the receiver, usually discovered by UPnP                                                          |
|port            |Port to reach the remote service, usually 8081 for the MR401/MR201 or 49152 for MR400/200                       |
|accountName     |T-Online account name, should be the registered e-mail address                                                  |
|accountPassword |T-Online password for the account                                                                               |


## Channels

|Group   |Channel        |Description                                                              |
|-------|---------------|--------------------------------------------------------------------------|
|control |power          |Switching the channel simulates pressing the power button (same as sending "POWER" to the key channel). The receiver doesn't offer ON and OFF, but just toggles the power state. For that it's tricky to ensure the power state. Maybe some future versions will use some kind of testing to determine the current state.                                                                    |
|        |player         |Send commands to the receiver - see below                                |
|        |volumeUp       |Increase the volume                                                      |
|        |volumeDown     |Decrease the volume                                                      |
|        |mute           |Mute volume (mute the speaker)                                           |
|        |channel        |Select program channel (outbound only, current channel is not available) |
|        |channelUp      |Switch one channel up (same as sending "CHUP" to the key channel)        |
|        |channelDown    |Switch one channel down (same as sending "CHDOWN" to the key channel)    |
|        |key            |Send key code to the receiver (see code table below)                     |
|status  |channelCode    |The channel code from the EPG                                            |
|        |playMode       |Current play mode - this info is not reliable                            |
|program |programTitle   |Title of the running program or video being played                       |
|        |programText    | Some description (as reported by the receiver, could be empty)          |
|        |programStart   |Time when the program started                                            |
|        |programDuration| Remaining time, usually not updated for TV program                      | 
|        |programPosition| Position within a movie (0 for regular programs).                       |

Channels receiving event information when changing the channel or playing a video.

## Player and channel

The player control supports the following actions:

|Channel |Command                           |Description                       |
|--------|----------------------------------|----------------------------------|
|player  |PlayPauseType.PLAY                |Start playing media               |
|        |PlayPauseType.PAUSE               |Pause player                      |
|        |NextPreviousType.NEXT             |Move to the next chapter          |
|        |NextPreviousType.PREVIOUS         |Move to the previous chapter      |
|        |RewindFastforwardType.FASTFORWARD |Switch to forward mode            |
|        |RewindFastforwardType.REWIND      |Switch to rewind mode             |
|        |OnOffType.ON or OFF               |Toggle power - see notes on power |
|channel |Numeric string                    |Send channel number key by key    |

## Supported Key Code (channel key)

| Key    | Description                                    |
| -------|------------------------------------------------|
| POWER  | Power on/off the receiver (check standby mode) |
| 0..9   | Key 0..9                                       |
| DELETE | Delete key (text edit)                         |
| ENTER  | Enter/Select key                               |
| RED    | Special Actions: red                           |
| GREEN  | Special Actions:green                          |
| YELLOW | Special Actions: yellow                        |
| BLUE   | Special Actions:blue                           |
| EPG    | Electronic Program Guide                       |
| OPTION | Display options                                |
| UP     | Up arrow                                       |
| DOWN   | Down arrow                                     |
| LEFT   | Left arrow                                     |
| RIGHT  | Right arrow                                    |
| OK     | OK button                                      |
| BACK   | Return to last menu                            |
| EXIT   | Exit menu                                      |
| MENU   | Menu                                           |
| INFO   | Display information                            |
| FAV    | Display favorites                              |
| VOLUP  | Volume up                                      |
| VOLDOWN| Volume down                                    |
| MUTE   | Mute speakers                                  |
| CHUP   | Channel up                                     |
| CHDOWN | Channel down                                   |
| PLAY   | Play                                           | 
| PAUSE  | Play                                           | 
| STOP   | Stop playing                                   |
| RECORD | Start recording                                |
| REWIND | Rewind                                         |
| FORWARD| Forward                                        |
| LASTCH | Last chapter                                   |
| NEXTCH | Next chapter                                   |
| PIP    | Activate Program-in-Program                    |
| PGUP   | Page up                                        |
| PGDOWN | Page down                                      |
| PAIR   | Re-pair with the receiver                      |

In addition you could send any key code in the 0xHHHH format., refer to
[Key Codes for Magenta/Huawei Media Receiver](http://support.huawei.com/hedex/pages/DOC1100366313CEH0713H/01/DOC1100366313CEH0713H/01/resources/dsv_hdx_idp/DSV/en/en-us_topic_0094619112.html)


## Full Configuraton Example

### magentatv.things

```
Thing magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx "MagentaTV" [
udn="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
modelId="MR401B",
ipAddress="xxx.xxx.xxx.xxx", 
port="8081",
accountName="xxxxxx.xxxx@t-online.de",
accountPassword="xxxxxxxxxx"
]
```

### magentatv.items

```
# MagentaTV Control
Switch MagentaTV_Power        "Power"        {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:control#power"}
Switch MagentaTV_ChannelUp    "Channel +"    {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:control#channelUp"}
Switch MagentaTV_ChannelDown  "Channel -"    {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:control#channelDown"}
Switch MagentaTV_VolumeUp     "Volume +"     {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:control#volumeUp"}
Switch MagentaTV_VolumeDown   "Volume -"     {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:control#volumeDown"}
String MagentaTV_Key          "Key"          {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:control#key"}

# MagentaTV Program Information
String MagentaTV_ProgTitle   "Program Title" {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:program#programTitle"}
String MagentaTV_ProgDescr   "Description"   {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:program#programText"}
String MagentaTV_ProgStart   "Start Time"    {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:program#programStart"}
String MagentaTV_ProgDur     "Duration"      {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:program#programDuration"}
String MagentaTV_ProgPos     "Position"      {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:program#programPosition"}

# MagentaTV Play Status
Number MagentaTV_Channel   "Channel"         {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:status#channel"}
Number MagentaTV_ChCode    "Channel Code"    {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:status#channelCode"}
String MagentaTV_PlayMode  "Play Mode"       {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:status#playMode"}
String MagentaTV_RunStatus "Run Status"      {channel="magentatv:receiver:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:status#runStatus"}
```

### sitemap

```
please contribute an example
```

### magentatv.rules

Due to the fact the POWER is a toggle button and the binding can't detect the current status, which could lead into the situation that you want to power on the receiver as part of a scene, but due to the fact that it is already ON you switch it off.
We spend some time to fiddle out a better handling and find a way to detect a network message when the receiver gets powered off (MR4xx only).
In this case MagentaTV_Power is switch to OFF.

This said you could use the following

```
        if (MagentaTV_Power.state != ON) {
            sendCommand(MagentaTV_Power, ON)
        }
```

to switch it ON (within a scene) and 

```
        if (MagentaTV_Power.state != OFF) {
            sendCommand(MagentaTV_Power, OFF)
        }
```

to switch it off.

Maybe after an openHAB restart you need to make sure that OH and receiver are in sync, because the binding can't read the power status on startup.
