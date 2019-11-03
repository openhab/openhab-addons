# MagentaTV Binding (aka TelekomTV aka EntertainTV)

---

## Release: 2.5-pre5

This Binding allows controlling the Deutsche Telekom Magenta TV receiver series Media Receiver MR4xx and MR2xx (Telekom NGTV / Huawei Envision platform). 
The Binding does NOT support MR3xx/1xx (old Entertain system based on Microsoft technology)!

The binding has been tested with the EntertainTV service as well as the new MagentaTV service (launched in 10/2018).

This include device discovery, sending keys from the remote and also receiving program events.

This binding replaces the former versions EntertainTV and TelekomTV bindings.

---

Please check out the [openHAB community thread](https://community.openhab.org/t/magentatv-entertaintv-binding-for-deutsche-telekom-mr-3xx-and-4xx) and discuss your ideas, requests and technical problems with the community.

---

### Supported Models

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

Standby provides the best results, because the binding could wakeup the receiver (Power On/Off). Suspend/Resume would require a Wake-on-LAN, which could be done, but is currently not implemented. Shutdown turns the receiver off, which requires a manual PowerOn.

There is no way to detect the "display status" of the receiver. The binding detects PowerOff with the MR401B/MR201 by listening to UPnP events, but can't verify the status when started. You need to take care on the current status if you power on/off the receiver from scenes. Check the current status before sending the POWER button, because POWER is a toggle, not ON or OFF (see sample rules).

### Supported Things

| Thing    | Type                                                           |
| ---------| -------------------------------------------------------------- |
| receiver | A MagentaTV Receiver, the binding supports multiple receivers. |


## Discovery

The auto discovery starts when the binding is loaded. UPnP will be used to discover receivers on the local network. Based on UPnP all nessesary parameters are auto-discovered. The receiver needs to be powered on to get discovered.

# Thing Configuration

## Using PaperUI

Once the thing will be added from the Inbox in PaperUI you'll need your T-Online credentials to query the userID. Open the thing configuration and enter the credentials:

| Field            | Description                                      |
| ---------------- | ------------------------------------------------ |
| Account Name     | Your T-Online user id, e.g. test7017@t-online.de |
| Account Password | The password for your Telekom account.           |

For security reasons the credentials will be automatically deleted from the thing configuration (replaced with '***' in the thing config) after the initial authentication process. The openHAB instance needs access to the Internet to perform that operation, which can be disabled afterwards.

One the userID has been obtained from the Telekom portal the binding initiates the pairing with the receiver. This is an automated process. The thing changes to ONLINE state once the pairing result is received. Otherwise open PaperUI-Confuguration-Things and check for an error message. Using [Show Properties] you see more details and could verify if the discovery and pairing were completed successful.

For now the binding selects the first matching network interface, which is not a tunnel, dialup, loopback interface. Check localIP in the properties to see if the right one was selected. A future version of the binding will use the IP address from PaperUI-Configuration-System-Network Settings.

## Manual configuration

### Thing Parameters

| Parameter |Description |
| ----------| -------------------------------------------------------------------- |
| udn | UPnP Unique Device Name - a hexadecimal ID, which includes the 12 digit MAC address at the end (parsed by the binding to get the receiver's MAC) |
| modelId | Type of Media Receiver: DMS_TPB for MR400 amd MR200 ; MR401B for MR401B and MR201 |
| ipAddress | IP address of the receiver, usually discovered by UPnP |
| port | Port to reach the remote service, usually 8081 for the MR401/MR201 or 49152 for MR400/200 |
| accountName | T-Online account name, should be the registered e-mail address |
| accountPassword | T-Online password for the account |


## Channels

| Group | Channel | Description |
| ----- | -------| ------------------------------------------------------------ |
| control | power       | Switching the channel simulates pressing the power button (same as sending "POWER" to the key channel). The receiver doesn't offer ON and OFF, but just toggles the power state. For that it's tricky to ensure the power state. Maybe some future versions will use some kind of testing to determine the current state. |
| | channelUp   |Switch one channel up (same as sending "CHUP" to the key channel) |
| | channelDown |Switch one channel down (same as sending "CHDOWN" to the key channel) |
| | volumeUp    | Increase volume (same as sending "VOLUP" to the key channel) |
| | volumeDown  |Decrease volume (same as sending "VOLDOWN" to the key channel) |
| | key         | Updates to this channel simulate a "key pressed" to the receiver. Those include Menu, EPG etc. (see below) |
| status | channel     | Changing this channel can be used to simulate entering the channel number on the remote - digit by digit, e.g. 10 will be send as '1' and '0' key. ||
| | channelCode | The channel code from the EPG |
| | playMode | Current play mode - this info is not reliable |
| |  runStatus |
| program | programTitle |Title of the running program or video being played |
| | programText | Some description |
| | programStart |Time when the program started |
| | programDuration | Remaining time, usually not updated for TV program | 
| | programPosition | Position within a movie (0 for regular programs). |

Channels receiving event information when changing the channel or playing a video:

## Supported Key Code (channel key)

| Key | Description |
| ---- | -----------|
| POWER| Power on/off the receiver (check standby mode) |
| 0..9 | Key 0..9 |
| DELETE | Delete key (text edit) |
| ENTER | Enter/Select key |
| RED | Special Actions: red |
| GREEN | Special Actions:green |
| YELLOW | Special Actions: yellow |
| BLUE | Special Actions:blue |
| EPG | Electronic Program Guide |
| OPTION | Display options |
| UP | Up arrow |
| DOWN | Down arrow |
| LEFT | Left arrow |
| RIGHT | Right arrow |
| OK | OK button |
| BACK | Return to last menu |
| EXIT | Exit menu |
| MENU | Menu |
| INFO | Display information |
| FAV | Display favorites |
| VOLUP | Volume up |
| VOLDOWN | Volume down |
| MUTE | Mute speakers |
| CHUP | Channel up |
| CHDOWN | Channel down |
| PLAY | Play | 
| PAUSE | Play | 
| STOP | Stop playing |
| RECORD | Start recording |
| REWIND | Rewind |
| FORWARD | Forward |
| LASTCH | Last chapter |
| NEXTCH | Next chapter |
| PIP | Activate Program-in-Program |
| PGUP | Page up |
| PGDOWN | Page down |
| PAIR | Re-pair with the receiver |

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

Due to the fact the POWER is a toggle button and the binding can't detect the current status, which could lead into the situation that you want to power on the receiver as part of a scene, but due to the fact that it is already ON you switch it off. We spend some time to fiddle out a better handling and find a way to detect a network message when the receiver gets powered off (MR4xx only). In this case MagentaTV_Power is switch to OFF.

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
