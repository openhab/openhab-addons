# <bindingName> MagentaTV Binding (aka Entertain TV)
<hr>
<b>Release: 2.5.1pre</b>
<hr>
<p>

This Binding allows controlling the Deutsche Telekom Magenta TV receiver series Media Receiver MR4xx and MR2xx (Telekom NGTV / Huawei Envision platform). 
The Binding does NOT support MR3xx/1xx (old Entertain system based on Microsoft technology)!

The binding has been tested with the EntertainTV service as well as the new MagentaTV service (launched in 10/2018).

This include device discovery, sending keys from the remote and also receiving program events.


### Supported Models

- Deutsche Telekom Media Receiver MR401B - fully supported
- Deutsche Telekom Media Receiver MR201  - fully supported
- Deutsche Telekom Media Receiver MR400  - supported with minor restrictions (POWER key)
- Deutsche Telekom Media Receiver MR200  - supported with minor restrictions (POWER key)
- Deutsche Telekom Media Receiver MR3xx  - NOT supported (different platform)
- Deutsche Telekom Media Receiver MR1xx  - NOT supported (different platform)


### Receiver Standby Mode

The Media receiver has 3 different standby modes, which could be selected in the receiver's settings.
Standby - full standby, receiver active all the time
Suspend/Resume - The receiver goes to sleep mode, but could be awaked by a Wake-on-LAN packet
Shutdown - Powering off will shutdown the receiver, can be awaked only with the power button

## Supported Things

<table>
<tr><td>Thing</td><td>Type</td><td>Description</td></tr>
<tr><td>receiver</td><td>Thing</td><td>A MagentaTV Receiver, the binding supports multiple receivers.</td></tr>
</table>


## Discovery

The auto discovery starts when the binding is loaded. UPnP will be used to discover receivers on the local network. Based on UPnP all nessesary parameters are auto-discovered. The receiver needs to be powered on to get discovered.

# Thing Configuration

<b>Using PaperUI</b>

Once the thing will be added from the Inbox in PaperUI you'll need your T-Online credentials to query the userID. Open the thing configuration and enter the credentials:

- Account Name: you T-Online user id, e.g. test7017@t-online.de
- Account Password: the password for your Telekom account. 

For security reasons the credentials will be automatically deleted from the thing configuration (replaced with '***' in the thing config) after the initial authentication process. The openHAB instance needs access to the Internet to perform that operation, which can be disabled afterwards.

One the userID has been obtained from the Telekom portal the binding initiates the pairing with the receiver. This is an automated process. The thing changes to ONLINE state once the pairing result is received. Otherwise open PaperUI-Confuguration-Things and check for an error message. Using [Show Properties] you see more details and could verify if the discovery and pairing were completed successful.

For now the binding selects the first matching network interface, which is not a tunnel, dialup, loopback interface. Check localIP in the properties to see if the right one was selected. A future version of the binding will use the IP address from PaperUI-Configuration-System-Network Settings.

<b>Manually -  .things</b><p>

<table>
<tr><td>Parameter</td><td>Description</td></tr>
<tr><td>udn</td><td>UPnP Unique Device Name - a hexadecimal ID, which includes the 12 digit MAC address at the end (parsed by the binding to get the receiver's MAC)</td></tr>
<tr><td>modelId</td><td>Type of Media Receiver:<br>DMS_TPB: MR400, MR200<br>MR401B: MR401B, MR201</td></tr>
<tr><td>ipAddress</td><td>IP address of the receiver, usually discovered by UPnP</td></tr>
<tr><td>port</td><td>Port to reach the remote service, usually 8081 for the MR401/MR201 or 49152 for MR400/200</td></tr>
<tr><td>accountName</td><td>T-Online account name, should be the registered e-mail address</td></tr>
<tr><td>accountPassword</td><td>T-Online password for the account.</td></tr>
<tr><td></td><td></td></tr>
</table>

## Channels

Channels controlling the receiver:

<table style="width:100%">
<tr align="left"><th>Channel</th><th>Description</th></tr>
<tr><td>power</td><td>Switching the channel simulates pressing the power button (same as sending "POWER" to the key channel). The receiver doesn't offer ON and OFF, but just toggles the power state. For that it's tricky to ensure the power state. Maybe some future versions will use some kind of testing to determine the current state. </td></tr>
<tr><td>Channel</td><td>Changing this channel can be used to simulate entering the channel number on the remote digit by digit, e.g. 10 will be send as '1' and '0' key.</td></tr>
<tr><td>Channel Up</td><td>Switch one channel up (same as sending "CHUP" to the key channel).</td></tr>
<tr><td>Channel Down</td><td>Switch one channel down (same as sending "CHDOWN" to the key channel).</td></tr>
<tr><td>Volume Up</td><td>Increase volume (same as sending "VOLUP" to the key channel).</td></tr>
<tr><td>Volume Down</td><td>Decrease volume (same as sending "VOLDOWN" to the key channel).</td></tr>
<tr><td>key</td><td>Updates to this channel simulate a "key pressed" to the receiver. Those include Menu, EPG etc. (see below)</td></tr>
</table>


Channels receiving event information when chaning the channel or playing a video:

<table style="width:100%">
<tr align="left"><th>Channel</th><th>Description</th></tr>
<tr><td>program</td><td>Title of the running program or video being played.</td></tr>
<tr><td>description</td><td>This could be a textual description of the content, the title of the episode etc.</td></tr>
<tr><td>startTime</td><td>Time when the program started.</td></tr>
<tr><td>duration</td><td>Duration of the program.</td></tr>
<tr><td>playPosition</td><td>Position within a video (0 for regular programs).</td></tr>
<tr><td>playMode</td><td>Current play mode - this info is not reliable.</td></tr>
</table>


The following keys are supported (channel key):

<table style="width:100%">
<tr align="left"><th>Key</td><th>Description</th></tr>
<tr><td>POWER</td><td>Power on/off the receiver (check standby mode)
<tr><td>0..9</td><td>Key 0..9</td></tr>
<tr><td>DELETE</td><td>Delete key (text edit)</td></tr>
<tr><td>ENTER</td><td>Enter/Select key</td></tr>
<tr><td>RED</td><td>Special Actions: red</td></tr>
<tr><td>GREEN</td><td>Special Actions:green</td></tr>
<tr><td>YELLOW</td><td>Special Actions: yellow</td></tr>
<tr><td>BLUE</td><td>Special Actions:blue</td></tr>
<tr><td>EPG</td><td>Electronic Program Guide</td></tr>
<tr><td>OPTION</td><td>Display options</td></tr>
<tr><td>UP</td><td>Up arrow</td></tr>
<tr><td>DOWN</td><td>Down arrow</td></tr>
<tr><td>LEFT</td><td>Left arrow</td></tr>
<tr><td>RIGHT</td><td>Right arrow</td></tr>
<tr><td>OK</td><td>OK button</td></tr>
<tr><td>BACK</td><td>Return to last menu</td></tr>
<tr><td>EXIT</td><td>Exit menu</td></tr>
<tr><td>MENU</td><td>Menu</td></tr>
<tr><td>INFO</td><td>Display information</td></tr>
<tr><td>FAV</td><td>Display favorites</td></tr>
<tr><td>VOLUP</td><td>Volume up</td></tr>
<tr><td>VOLDOWN</td><td>Volume down</td></tr>
<tr><td>MUTE</td><td>Mute speakers</td></tr>
<tr><td>CHUP</td><td>Channel up</td></tr>
<tr><td>CHDOWN</td><td>Channel down</td></tr>
<tr><td>PLAY</td><td>Play</td><td>
<tr><td>PAUSE</td><td>Play</td><td>
<tr><td>STOP</td><td>Stop playing</td></tr>
<tr><td>RECORD</td><td>Start recording</td></tr>
<tr><td>REWIND</td><td>Rewind</td></tr>
<tr><td>FORWARD</td><td>Forward</td></tr>
<tr><td>LASTCH</td><td>Last chapter</td></tr>
<tr><td>NEXTCH</td><td>Next chapter</td></tr>
<tr><td>PIP</td><td>Activate Program-in-Program</td></tr>
<tr><td>PGUP</td><td>Page up</td></tr>
<tr><td>PGDOWN</td><td>Page down</td></tr>
<tr><td>PAIR</td><td>Re-pair with the receiver</td></tr>
</table>

In addition you could send any key code in the 0xXXXX format. Refer to <a href="http://support.huawei.com/hedex/pages/DOC1100366313CEH0713H/01/DOC1100366313CEH0713H/01/resources/dsv_hdx_idp/DSV/en/en-us_topic_0094619112.html">Key Codes for magenta/Huawei Media Receiver</a>


## Full Configuraton Example

<b>magentatv.things:</b><p>

Thing Magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX "MagentaTV" [ udn="e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX", modelId="MR401B", ipAddress="192.168.1.1", port="8081", accounName="markus7017@t-online.de", accountPassword="thispassword"]<p>

<table>
<tr><b>magentatv.items</b><p></tr>
<tr><b># MagentaTV Control</b><br>
Switch MagentaTV_Power        "Power"       {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:power"}<br>
Switch MagentaTV_ChannelUp   "Channel +"    {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:channelUp"}<br>
Switch MagentaTV_ChannelDown "Channel -"    {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:channelDown"}<br>
Switch MagentaTV_ChannelUp   "Volume +"     {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:volumeUp"}<br>
Switch MagentaTV_ChannelDown "Volume -"     {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:volumeDown"}<br>
String MagentaTV_ChannelDown "Key"          {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:key"}<br>
</tr><p>
<tr><b># MagentaTV Program Information</b><br>
String MagentaTV_ProgTitle  "Program Title" {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:programTitle"}<br>
String MagentaTV_ProgText   "Description"   {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:programText"}<br>
String MagentaTV_ProgText   "Start Time"    {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:programStart"}<br>
String MagentaTV_ProgText   "Duration"      {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:programDuration"}<br>
String MagentaTV_ProgText   "Position"      {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:programPosition"}<br>
</tr><p>
<tr><b># MagentaTV Play Status</b><br>
Number MagentaTV_Channel   "Channel"        {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:channel"}<br>
Number MagentaTV_ChCode    "Channel Code"   {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:channelCode"}<br>
String MagentaTV_PlayMode  "Play Mode"      {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:playMode"}<br>
String MagentaTV_RunStatus "Run Status"     {channel="magentatv:receiver:e8dbce32-64c8-51b5-a712-XXXXXXXXXXXX:runStatus"}<br>
</tr><p>
</table>

<b>Sitemap:</b><p>
t.b.d.<p>

<b>magentatv.rules</b><p>
t.b.d.<p>
<p>