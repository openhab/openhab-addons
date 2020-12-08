![TiVo Logo](doc/TiVo_lockup_BLK.png)

## TiVo Binding
This binding integrates [TiVo](https://www.tivo.com/) Digital Video Recorders (DVR) that support the Tivo [TiVo TCP Control Protocol v1.1](https://www.tivo.com/assets/images/abouttivo/resources/downloads/brochures/TiVo_TCP_Network_Remote_Control_Protocol.pdf).

## Supported Things
Most TiVo DVRs that support network remote control can be managed/supported by this binding.  Check the web site of your service provider for the precise specification of the TiVo box they have provided.

All TiVo devices must:

 1. be connected to a local area TCP/IP network that can be reached by the openHAB instance (this is not the WAN network interface used by cable service providers to provide the TV signals).    
 2. have the Network Remote Control function enabled to support discovery and control of the device.  This setting can be found using the remote control at:

    * Tivo branded boxes - using the remote go to TiVo Central > Messages & Settings > Settings > Remote, CableCARD & Devices > Network Remote Control.  Choose Enabled, press Select.
    * Virgin Media branded boxes - using the remote select Home > Help and Settings > Settings > Devices > Network Remote Control.  Select the option Allow network based remote controls.

## Binding Configuration
The binding requires no manual configuration.  Tivo devices with the network remote control interface enabled, will be displayed within the Inbox.  

You can also add these manually, you will need to specify the LAN IP address of your Tivo Device.

## Thing Configuration

Auto-discovery is recommended for the discovery and creation of TiVo things, however they can also be created using the .things file format.  The following minimum parameters should be used:

```
Thing tivo:sckt:test_device[deviceName = "Test Device", address="192.168.0.19"]
```

Where:
* **test_device** is the unique thing ID for the device (alpha numeric, no spaces)
* **device name** is the name of the device (if omitted the name of the device will be specified as 'My Tivo') 
* **address** the IP address or host name of the device

See the Parameters section below, for the definition of the other optional parameter field names / values.

## Channels

All devices support the following channels (non exhaustive):

| Channel Type ID | Item Type       | Display Name                          | Description                                                                                                                                                                                                                                                       |
|-----------------|-----------------|---------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| channelForce    | Number (1-9999) | Current Channel - Forced (FORCECH)    | Displays the current channel number. When changed, tunes the DVR to the specified channel, **cancelling any recordings in progress if necessary** i.e. when all tuners are already in use / recording. The TiVo must be in Live TV mode for this command to work. |
| channelSet      | Number (1-9999) | Current Channel - Request (SETCH)     | Displays the current channel number. When changed, tunes the DVR to the specified channel (unless a recording is in progress on all available tuners). The TiVo must be in Live TV mode for this command to work.                                                 |
| menuTeleport    | String          | Change Special/Menu Screen (TELEPORT) | Change to one of the following TiVo menu screens: TIVO (Home), LIVE TV, GUIDE, NOW PLAYING (My Shows).                                                                                                                                                            |
| irCommand       | String          | Remote Control Button (IRCOMMAND)     | Send a simulated button push from the remote control to the TiVo. See Appendix A in document TCP Remote Protocol 1.1 for supported codes.                                                                                                                         |
| kbdCommand      | String          | Keyboard Command (KEYBOARD)           | Sends a code corresponding to a keyboard key press to the TiVo e.g. A-Z. See Appendix A in document TCP Remote Protocol 1.1 for supported characters and special character codes.                                                                                 |
| dvrStatus       | String          | TiVo Status                           | Action return code / channel information returned by the TiVo.                                                                                                                                                                                                    |
| customCmd       | String          | Custom Command                        | Send any custom commands that are not documented within the official specification. Both the command and action string must be supplied. **Note: support is not provided for undocumented commands!**                                                             |

* Commands to each of the channels (except 'Custom Command') do not need to include the command keyword only the action/parameter.  For example, to change channel simply post/send the number of the channel **without** the keywords SETCH or  FORCECH.
* Custom Command is provided to allow the testing of any commands not documented within the official documentation.  In this instance the COMMAND and any parameters must be sent as a single string.
* Keyboard commands must currently be issued one character at a time to the item (this is how the TiVo natively supports these command).
* To send multiple copies of the same Keyboard command, append a asterisk with the number of repeats required e.g. NUM2*4 would send the number 2 four times. This is useful for performing searches where the number characters can only be accessed by pressing the keys multiple times in rapid succession i.e. each key press cycles through characters A, B, C, 2. See the search script below for an example of this in action.
* Special characters must also be changed to the appropriate command e.g. the comma symbol( ,) must not be sent it should be replaced by 'COMMA'.


## Parameters
| Parameter         | Display Name                         | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|-------------------|--------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| deviceName        | Device Name                          | A friendly name to refer to this device. Default: Device name specified on the TiVo or 'My Tivo' if no connection can be made at initial device configuration.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| address           | Address                              | The IP address or hostname of your TiVo box.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| tcpPort           | TCP Port                             | The TCP port number used to connect to the TiVo. **Default: 31339**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| numRetry          | Connection Retries                   | The number of times to attempt reconnection to the TiVo box, if there is a connection failure. **Default: 5**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| keepConActive     | Keep Connection Open                 | Keep connection to the TiVo open. Recommended for monitoring the TiVo for changes in TV channels. <br><br>Disable if other applications that use the Remote Control Protocol port will also be used e.g. mobile phone remote control applications. **Default: True (Enabled)**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| pollForChanges    | Poll for Channel Changes             | Check TiVo for channel changes. Enable if openHAB and a physical remote control (or other services use the Remote Control Protocol) will be used. **Default: True (Enabled)**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| pollInterval      | Polling Interval (Seconds)           | Number of seconds between polling jobs to update status information from the TiVo.  **Default: 10**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| cmdWaitInterval   | Command Wait Interval (Milliseconds) | Period to wait AFTER a command is sent to the TiVo in milliseconds, before checking that the command has completed. **Default: 200**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| ignoreChannels    | Channels to Ignore                   | Used in channel UP/DOWN operations to avoid invalid channel numbers that are not part of your subscription (these impact the speed of changing channels). Channels you list in a comma separated list e.g. 109, 111, 999 are skipped/ignored when changing the channel UP or DOWN.<br><br>You can also exclude a range of channel numbers by using a hyphen between the lower and upper numbers e.g. 109, 101, 800-850, 999. New entries are sorted into numerical order when saved. <br><br>During normal channel changing operations any invalid channels detected are automatically learnt and added to this list, however the maximum gap for 'auto learning' between valid channels is 10. <br><br>Any gap larger than this will not be automatically learnt and the Channel UP/DOWN operation will fail. If your service has gap(s) larger than 10 channels, you should exclude these manually or use the Perform Channel Scan function to populate this list. |
| minChannel        | Min Channel Number                   | The first valid channel number available on the TiVo. **Default: 100 (min 1)**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| maxChannel        | Max Channel Number                   | The last valid channel number available on the TiVo. **Default: 999 (max 9999)**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| ignoreChannelScan | Channel Scan                         | Performs a channel scan between Min Channel Number and Max Channel Number, populates the **Channels to Ignore** settings any channels that are not accessible / part of your subscription. <br><br>**Note:** Existing Channels to Ignore settings are retained, you will need to manually remove any entries for new channels added to your service (or remove all existing Channels to Ignore and run a new scan).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |


## Configuration Parameters Notes
The following notes may help to understand the correct configuration properties for your set-up:

### Connection Performance
* If openHAB is the only device or application that you have that makes use of the Network Remote Control functions of your Tivo, enable the **Keep Connection Open** option.  This will connect and lock the port in-use preventing any other device from connecting it.  If you use some other application, disable this option. Performance is improved if the connection is kept open.
* **Poll for Channel Changes** only needs to be enabled if you also plan to use the TiVo remote control or other application to change channel.  If openHAB is your only method of control, you can disable this option.  Turning polling off, minimises the periodic polling overhead on your hardware.
 
### Channel Changing
* You can set the current channel using the item bound to either the `tivoChannelForce` or the `tivoChannelSet` by simply posting/sending the channel number to the item.
* Channel UP/DOWN commands work by increasing to decreasing the channel number stored within the `tivoChannelSet` channel.  This item must contain a value for this to work.  
* The TiVo will learn channel numbers that are not available as part of your subscription as you navigate / change channel.  Channel changing  operations will be slower if there is a large gap between valid channels.  Any gap between valid channel number must not exceed 10.  If you have a gap larger than this any channel UP/DOWN operations will fail.  You must therefore add any of these gaps to the range of **Channels to Ignore** manually or use the **Perform Channel Scan** option to pre-populate the ignored channels (recommended).
* The **Channels to Ignore** section allows you to exclude any channels that you do not want to see or are not part of your subscription.  Both individual channel numbers and ranges of channels are supported e.g. 109, 106, 801-850, 999.
* Set the correct Minimum and Maximum channel numbers BEFORE you run a full channel scan.  By default these are set at 100 and 999.   Consult your Tivo program guide to find these.
* **Perform Channel Scan** will systematically change the channels between the specified Minimum and Maximum, identifying which of these are valid.  At least one tuner must be available (not recording) while this operation completes.  If this process is interrupted e.g. by a configuration change or restart, the system will restart the scan at the beginning.  Any channels that are marked as being ignored will not be tested again.  
* You can run a channel scan while the system is in Standby mode.  
* The channels will change 'onscreen' while you run a channel scan while the TiVo is in normal operation (it will not however cancel any recordings in progress if all tuners are busy recording).  Therefore, you should avoid running a scan when someone is watching their favorite shows!  If all channels are in use or recordings start on all available tuners the channel scan will stop/fail.
* The channel scanning process will take approximately 2 seconds per channel.  With the default channel range a scan will therefore take between 15 and 20 minutes!  The screen will change to the specified channel while the scan is being run.
* If the channel scan fails before completing, you may need to increase the `Command Wait Interval (Milliseconds)'.  A value between 200 (default) and 400 seems to work in testing.  
* When restarting after a failed channel scan all valid channels will be re-tested, but any existing **Channels to Ignore** will be skipped.
* If your provider adds new channels to your subscription line-up, these will have to be manually removed from the list of **Channels to Ignore**.  You can always remove all the entries and do a new full scan to re-populate the list.


## Full Example

**demo.items:**

```
/* TIVO */
/* TIVO */
String      TiVo_Status         "Status"        {channel="tivo:sckt:Living_Room:dvrStatus"}
String      TiVo_MenuScreen     "Menu Screen"   {channel="tivo:sckt:Living_Room:menuTeleport", autoupdate="false"}
Number      TiVo_SetChannel     "Up/Down"       {channel="tivo:sckt:Living_Room:channelSet"}
String      TiVo_SetChannelName "Channel Name"     
Number      TiVo_ForceChannel   "Force Channel" {channel="tivo:sckt:Living_Room:channelForce"}                                                      
String      TiVo_IRCmd          "Ir Cmd"        {channel="tivo:sckt:Living_Room:irCommand", autoupdate="false"}
String      TiVo_KbdCmd         "Keyboard Cmd"  {channel="tivo:sckt:Living_Room:kbdCommand", autoupdate="false"}
String      TiVo_KeyboardStr    "Search String"
Switch      TiVo_Search         "Search"
String      TiVo_CustomCmd      "Custom Cmd"    {channel="tivo:sckt:Living_Room:customCmd", autoupdate="false"}
```
* The item `TiVo_SetChannelName` depends upon a valid `tivo.map` file to translate channel numbers to channel names.  **Hint:**  I sourced my channel listing from a very handy Wikipedia page for VirginMedia services.

**tivoDemo.sitemap:**

```
sitemap tivoDemo label="Main Menu" {
            Frame label="Tivo" {
                Setpoint    item=TiVo_SetChannel      label="[CH %n]"      icon="television"   minValue=100 maxValue=999 step=1
                Text        item=TiVo_SetChannelName  label="Channel"      icon="television"
                Text        item=TiVo_Status          label="Status"       icon="television"
                Switch      item=TiVo_IRCmd           label="Media"        icon="television"   mappings=["REVERSE"="⏪", "PAUSE"="⏸", "PLAY"="⏵", "STOP"="⏹", "FORWARD"="⏩" ]
                Switch      item=TiVo_SetChannel      label="Fav TV"       icon="television"   mappings=[101="BBC1", 104="CH 4", 110="SKY 1", 135="SyFy", 429="Film 4"]            
                Switch      item=TiVo_SetChannel      label="Fav Radio"    icon="television"   mappings=[902="BBC R2", 904="BBC R4 FM", 905="BBC R5", 951="Abs 80s"]
                Switch      item=TiVo_MenuScreen      label="Menus"        icon="television"   mappings=["TIVO"="Home", "LIVETV"="Tv", "GUIDE"="Guide", "NOWPLAYING"="My Shows", "INFO"="Info" ]
                Switch      item=TiVo_IRCmd           label="Navigation"   icon="television"   mappings=["UP"="⏶", "DOWN"="⏷", "LEFT"="⏴", "RIGHT"="⏵", "SELECT"="Select", "EXIT"="Exit" ]
                Switch      item=TiVo_IRCmd           label="Likes"        icon="television"   mappings=["THUMBSUP"="Thumbs Up", "THUMBSDOWN"="Thumbs Down"]
                Switch      item=TiVo_IRCmd           label="Actions"      icon="television"   mappings=["ACTION_A"="Red","ACTION_B"="Green","ACTION_C"="Yellow","ACTION_D"="Blue"]
                Switch      item=TiVo_IRCmd           label="Standby"      icon="television"   mappings=["STANDBY"="Standby","TIVO"="Wake Up"]
                Switch      item=TiVo_IRCmd           label="Channel"      icon="television"   mappings=["CHANNELUP"="CH +","CHANNELDOWN"="CH -"]
                
            }
}
```

* Amend the minValue / maxValue to reflect the minimum and maximum channel numbers of your device.
* This example does not use the 'Current Channel - Forced (FORCECH)' channel.  This method will interrupt your recordings in progress when all you tuners are busy, so is obmitted for safety's sake.
* The item 'TiVo_SetPointName' depends upon a valid tivo.map file to translate channel numbers to channel names.

**tivo.map:**
```
NULL=Unknown
100=Virgin Media Previews
101=BBC One
102=BBC Two
103=ITV
104=Channel 4
105=Channel 5

etc...
```


**tivo.rules:**
The following rule uses the `tivo.map` file to translate the channel number to channel names (populating the `TiVo_SetChannelName`).
```
rule "MapChannel"
when
    Item TiVo_SetChannel changed
then
    var chName = ""
    chName = transform("MAP", "tivo.map", TiVo_SetPoint.state.toString)
    postUpdate(TiVo_SetChannelName, chName)

end

```

* This rule was used to overcome limitations within the HABpanel user interface at the moment when using transform/map functionality.


The following rule shows how a string change to the item `TiVo_KeyboardStr` is split into individual characters and sent to the TiVo.  The method to send a keystroke multiple times is used to simulate rapid keystrokes required to achieve number based searched.  

A simple custom template widget can be used within the HABpanel user interface for tablet based searches.  See [this discussion thread] (https://community.openhab.org/t/tivo-1-1-protocol-new-binding-contribution/5572/21?u=andymb).


```
rule "Search"
when
    Item TiVo_KeyboardStr received command
then
    logInfo("tivo.search","Script started ")
    if (TiVo_KeyboardStr.state != NULL && TiVo_KeyboardStr.state.toString.length > 0) {
        
        // Commands to get us to the Tivo/Home menu and select the search menu using the 'remote'
        // number keys
        sendCommand(TiVo_MenuScreen, "TIVO")
        Thread::sleep(800)
        sendCommand(TiVo_KbdCmd, "NUM4")
        Thread::sleep(800)
        
        var i = 0
        var l = 0
        var char txt = ""
        var srch = TiVo_KeyboardStr.state.toString.toUpperCase
        logInfo("tivo.search"," Searching for: " + srch)
        logDebug("tivo.search"," Search length: " + srch.length)

        while (i < (srch.length)) {
            logDebug("tivo.search"," Loop i=: " + i)
            txt = srch.charAt(i)
            logDebug("tivo.search"," txt: " + txt.toString)
            if (txt.toString.matches("[A-Z]")) {
                // Check for upper case A-Z
                sendCommand(TiVo_KbdCmd, txt.toString)
            } else if (txt.toString.matches(" ")) {
                // Check for Space
                sendCommand(TiVo_KbdCmd, "SPACE")
            } else if (txt.toString.matches("[0-9]")) {
                // Check for numbers 0-9
                l = 0
                switch txt.toString {
                    case "1":
                        sendCommand(TiVo_KbdCmd, "NUM1")
                    case "7": {
                        sendCommand(TiVo_KbdCmd, "NUM7*5")
                        }
                    case "9": {
                        sendCommand(TiVo_KbdCmd, "NUM9*5")
                        }
                    default: {
                        sendCommand(TiVo_KbdCmd, "NUM" + txt.toString + "*4")
                        }
                }
            } else {
                logWarn("tivo.search"," Character not supported by script: " + txt)
            }
            i = i + 1
        }
    }
    lock.unlock()
end

```

* You many need to adjust the two `Thread::sleep(800)` lines, depending on the performance of your TiVo/response from your service providers systems.  <br><br>In testing, response times have varied considerably at different times of the day etc.  You may need to increase the delay until there is sufficient time added for the system to respond consistently to the 'remote control' menu commands.
