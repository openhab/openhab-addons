# Atlona Binding

This binding integrates Atlona AT-UHD-PRO3 HDBaseT matrix switches (http://www.atlona.com) to your openHab installation.

## Supported Things

This binding supports the following thing types:

| Thing         | Thing Type | Description                                             |
|---------------|------------|---------------------------------------------------------|
| pro3-44m      | Thing      | The AT-UHD-PRO3-44M 4x4 HDBaseT matrix.                 |
| pro3-66m      | Thing      | The AT-UHD-PRO3-66M 6x6 HDBaseT matrix.                 |
| pro3-88m      | Thing      | The AT-UHD-PRO3-88M 8x8 HDBaseT matrix.                 |
| pro3-1616m    | Thing      | The AT-UHD-PRO3-1616M 16x16 HDBaseT matrix.             |

## Discovery

The Atlona AT-UHD-PRO3 switch can be discovered by starting a scan in openhab2 and then logging into your switch and pressing the "SDDP" button on the "Network" tab.  The "SDDP" 
(simple device discovery protocol) button will initiate the discovery process.  If "Telnet Login" is enabled ("Network" tab from the switch configuration UI), you will need to set the
username and password in the configuration of the newly discovered thing before a connection can be made. 

## Binding configuration

```
atlona:pro3-88m:home [ ipAddress="192.168.1.30", userName="me", password="12345", polling=600, ping=30, retryPolling=10 ]
```

- ipAddress: Hostname or IP address of the matrix switch
- userName: (optional) the username to login with (only if Telnet Login is enabled)
- password: (optional) the password to login with (only if Telnet Login is enabled)
- polling: (optional) the time (in seconds) to poll the state from the actual switch (default: 600)
- ping: (optional) the time (in seconds) to ping the switch to keep our connection alive (default: 30)
- retryPolling: (optional) the time (in seconds) to retry a connection if the connection has failed (default: 10)

### username/password
The userName/password configuration options are optional and are only required if you have your switch set with "Telnet Login" enabled (on the "Network" tab from the switch configuration UI).  The user must be a valid user listed on the "Users" tab of the switch configuration UI in this case.

### polling
Polling will automatically occur when (re)connecting to the switch to get the initial state of the switch.  If you have anything outside of openhab that can modify the switch state (front panel, IR, telnet session or another automation system), you will likely want to set this setting to a much lower value.

### ping
The Atlona switch will time out any IP connection after a specific time (specified by "IP Timeout" on the "Network" tab from the switch configuration UI - 120 by default).  The ping setting MUST be lower than that value.  If it is higher than the "IP Timeout" value, the switch will timeout our connection and the thing will go OFFLINE (until a reconnect attempt is made).

## Channels

| Thing         | Channel Type ID          | Item Type    | Access | Description                                                                               |
|---------------|--------------------------|--------------|--------|-------------------------------------------------------------------------------------------|
| pro3-44m      | power                    | Switch       | RW     | Matrix Power Switch                                                                       |
| pro3-44m      | version                  | String       | R      | Matrix Version (firmware)                                                                 |
| pro3-44m      | type                     | String       | R      | Matrix Type (model #)                                                                     |
| pro3-44m      | panellock                | Switch       | RW     | Sets the front panel locked or unlocked                                                   |
| pro3-44m      | irenable                 | Switch       | RW     | Enables/Disabled the front panel IR                                                       |
| pro3-44m      | portpower1               | Switch       | RW     | Enables/Disables output port #1                                                           |
| pro3-44m      | portpower2               | Switch       | RW     | Enables/Disables output port #2                                                           |
| pro3-44m      | portpower3               | Switch       | RW     | Enables/Disables output port #3                                                           |
| pro3-44m      | portpower4               | Switch       | RW     | Enables/Disables output port #4                                                           |
| pro3-44m      | portpower5               | Switch       | RW     | Enables/Disables output port #5                                                           |
| pro3-44m      | portoutput1              | Number       | RW     | Sets output port #1 to the specified input port                                           |
| pro3-44m      | portoutput2              | Number       | RW     | Sets output port #2 to the specified input port                                           |
| pro3-44m      | portoutput3              | Number       | RW     | Sets output port #3 to the specified input port                                           |
| pro3-44m      | portoutput4              | Number       | RW     | Sets output port #4 to the specified input port                                           |
| pro3-44m      | portoutput5              | Number       | RW     | Sets output port #5 to the specified input port                                           |
| pro3-44m      | portall                  | Number       | RW     | Sets ALL of the output ports to the specified input port                                  |
| pro3-44m      | resetports               | Switch       | RW     | Resets all ports back to their defaults                                                   |
| pro3-44m      | portmirror5              | Number       | RW     | Sets hdmi port #5 to mirror the specified output port                                     |
| pro3-44m      | volume1                  | Number       | RW     | Sets the volume of audio port #1 to the specified decibel level (between -79db to +15db)  |
| pro3-44m      | volume2                  | Number       | RW     | Sets the volume of audio port #2 to the specified decibel level (between -79db to +15db)  |
| pro3-44m      | volume3                  | Number       | RW     | Sets the volume of audio port #3 to the specified decibel level (between -79db to +15db)  |
| pro3-44m      | volumemute1              | Switch       | RW     | Mutes/Unmutes audio port #1                                                               |
| pro3-44m      | volumemute2              | Switch       | RW     | Mutes/Unmutes audio port #2                                                               |
| pro3-44m      | volumemute3              | Switch       | RW     | Mutes/Unmutes audio port #3                                                               |
| pro3-44m      | saveio                   | Number       | RW     | Saves the current input/output settings to the specified preset number                    |
| pro3-44m      | recallio                 | Number       | RW     | Recalls input/output settings from the specified preset number                            |
| pro3-44m      | cleario                  | Number       | RW     | Clears the specified present number                                                       |
| pro3-44m      | resetmatrix              | Switch       | RW     | Resets the matrix back to its defaults (USE WITH CARE!)                                   |
|               |                          |              |        |                                                                                           |
| pro3-66m      | ALL OF THE pro3-44M channels (except portmirrorX)                                                                                            |
| pro3-66m      | portpower6               | Switch       | RW     | Enables/Disables output port #6                                                           |
| pro3-66m      | portpower7               | Switch       | RW     | Enables/Disables output port #7                                                           |
| pro3-66m      | portpower8               | Switch       | RW     | Enables/Disables output port #8                                                           |
| pro3-66m      | portoutput6              | Number       | RW     | Sets output port #6 to the specified input port                                           |
| pro3-66m      | portoutput7              | Number       | RW     | Sets output port #7 to the specified input port                                           |
| pro3-66m      | portoutput8              | Number       | RW     | Sets output port #8 to the specified input port                                           |
| pro3-66m      | portmirror6              | Number       | RW     | Sets hdmi port #6 to mirror the specified output port                                     |
| pro3-66m      | portmirror8              | Number       | RW     | Sets hdmi port #8 to mirror the specified output port                                     |
| pro3-66m      | volume4                  | Number       | RW     | Sets the volume of audio port #4 to the specified decibel level (between -79db to +15db)  |
| pro3-66m      | volumemute4              | Switch       | RW     | Mutes/Unmutes audio port #4                                                               |
|               |                          |              |        |                                                                                           |
| pro3-88m      | ALL OF THE pro3-66M channels (except portmirrorX)                                                                                            |
| pro3-88m      | portpower9               | Switch       | RW     | Enables/Disables output port #9                                                           |
| pro3-88m      | portpower10              | Switch       | RW     | Enables/Disables output port #10                                                          |
| pro3-88m      | portoutput9              | Number       | RW     | Sets output port #9 to the specified input port                                           |
| pro3-88m      | portoutput10             | Number       | RW     | Sets output port #10 to the specified input port                                          |
| pro3-88m      | portmirror8              | Number       | RW     | Sets hdmi port #8 to mirror the specified output port                                     |
| pro3-88m      | portmirror10             | Number       | RW     | Sets hdmi port #10 to mirror the specified output port                                    |
| pro3-88m      | volume5                  | Number       | RW     | Sets the volume of audio port #5 to the specified decibel level (between -79db to +15db)  | 
| pro3-88m      | volume6                  | Number       | RW     | Sets the volume of audio port #6 to the specified decibel level (between -79db to +15db)  |
| pro3-88m      | volumemute5              | Switch       | RW     | Mutes/Unmutes audio port #5                                                               |
| pro3-88m      | volumemute6              | Switch       | RW     | Mutes/Unmutes audio port #6                                                               |
|               |                          |              |        |                                                                                           |
| pro3-1616m    | ALL OF THE pro3-88M channels (except portmirrorX)                                                                                            |
| pro3-1616m    | portpower11              | Switch       | RW     | Enables/Disables output port #11                                                          |
| pro3-1616m    | portpower12              | Switch       | RW     | Enables/Disables output port #12                                                          |
| pro3-1616m    | portpower13              | Switch       | RW     | Enables/Disables output port #13                                                          |
| pro3-1616m    | portpower14              | Switch       | RW     | Enables/Disables output port #14                                                          |
| pro3-1616m    | portpower15              | Switch       | RW     | Enables/Disables output port #15                                                          |
| pro3-1616m    | portpower16              | Switch       | RW     | Enables/Disables output port #16                                                          |
| pro3-1616m    | portpower17              | Switch       | RW     | Enables/Disables output port #17                                                          |
| pro3-1616m    | portpower18              | Switch       | RW     | Enables/Disables output port #18                                                          |
| pro3-1616m    | portpower19              | Switch       | RW     | Enables/Disables output port #19                                                          |
| pro3-1616m    | portpower20              | Switch       | RW     | Enables/Disables output port #20                                                          |
| pro3-1616m    | portoutput10             | Number       | RW     | Sets output port #10 to the specified input port                                          |
| pro3-1616m    | portoutput11             | Number       | RW     | Sets output port #11 to the specified input port                                          |
| pro3-1616m    | portoutput12             | Number       | RW     | Sets output port #12 to the specified input port                                          |
| pro3-1616m    | portoutput13             | Number       | RW     | Sets output port #13 to the specified input port                                          |
| pro3-1616m    | portoutput14             | Number       | RW     | Sets output port #14 to the specified input port                                          |
| pro3-1616m    | portoutput15             | Number       | RW     | Sets output port #15 to the specified input port                                          |
| pro3-1616m    | portoutput16             | Number       | RW     | Sets output port #16 to the specified input port                                          |
| pro3-1616m    | portoutput17             | Number       | RW     | Sets output port #17 to the specified input port                                          |
| pro3-1616m    | portoutput18             | Number       | RW     | Sets output port #18 to the specified input port                                          |
| pro3-1616m    | portoutput19             | Number       | RW     | Sets output port #19 to the specified input port                                          |
| pro3-1616m    | portoutput20             | Number       | RW     | Sets output port #20 to the specified input port                                          |
| pro3-1616m    | portmirror17             | Number       | RW     | Sets hdmi port #17 to mirror the specified output port                                    |
| pro3-1616m    | portmirror18             | Number       | RW     | Sets hdmi port #18 to mirror the specified output port                                    |
| pro3-1616m    | portmirror19             | Number       | RW     | Sets hdmi port #19 to mirror the specified output port                                    |
| pro3-1616m    | portmirror20             | Number       | RW     | Sets hdmi port #20 to mirror the specified output port                                    |
| pro3-1616m    | volume7                  | Number       | RW     | Sets the volume of audio port #7 to the specified decibel level (between -79db to +15db)  |
| pro3-1616m    | volume8                  | Number       | RW     | Sets the volume of audio port #8 to the specified decibel level (between -79db to +15db)  |
| pro3-1616m    | volume9                  | Number       | RW     | Sets the volume of audio port #9 to the specified decibel level (between -79db to +15db)  |
| pro3-1616m    | volume10                 | Number       | RW     | Sets the volume of audio port #10 to the specified decibel level (between -79db to +15db) |
| pro3-1616m    | volume11                 | Number       | RW     | Sets the volume of audio port #11 to the specified decibel level (between -79db to +15db) |
| pro3-1616m    | volume12                 | Number       | RW     | Sets the volume of audio port #12 to the specified decibel level (between -79db to +15db) |
| pro3-1616m    | volumemute7              | Switch       | RW     | Mutes/Unmutes audio port #7                                                               |
| pro3-1616m    | volumemute8              | Switch       | RW     | Mutes/Unmutes audio port #8                                                               |
| pro3-1616m    | volumemute9              | Switch       | RW     | Mutes/Unmutes audio port #9                                                               |
| pro3-1616m    | volumemute10             | Switch       | RW     | Mutes/Unmutes audio port #10                                                              |
| pro3-1616m    | volumemute11             | Switch       | RW     | Mutes/Unmutes audio port #11                                                              |
| pro3-1616m    | volumemute12             | Switch       | RW     | Mutes/Unmutes audio port #12                                                              |


## Warnings
As of firmware 1.6.03, there are three issues on Atlona firmware (I've notified them on these issues):
- cleario channel does not work.  The TCP/IP command "ClearX" as specified in Atlona's protocol will ALWAYS return a "Command Failed".  Please avoid this channel until atlona releases a new firmware.
- There is no way to query what the current status is of: panellock, irenable nor any of the **HDMI mirroring**.  This addon simply assumes that panellock is off, irenable is on and all port mirroring is set to none.  
- If you make a change in the switches UI that requires a reboot (mainly changing any of the settings on the "Network" tab in the switch configuration UI, our connection will be inconsistently closed at different times.  The thing will go OFFLINE and then back ONLINE when the reconnect attempt is made - and then it starts all over again.  Please make sure you reboot as soon as possible when the switch UI notifies you.

## Example

### Things

Here is an example with minimal configuration parameters (using default values with no telnet login):

```
atlona:pro3-88m:home [ ipAddress="192.168.1.30" ]
```

Here is another example with minimal configuration parameters (using default values with telnet login):

```
atlona:pro3-88m:home [ ipAddress="192.168.1.30", userName="me", password="12345" ]
```

Here is a full configuration example:

```
atlona:pro3-88m:home [ ipAddress="192.168.1.30", userName="me", password="12345", polling=600, ping=30, retryPolling=10 ]
```

### Items

Here is an example of items for the AT-UHD-PRO33-88M:

```
Switch Atlona_Power "Power" { channel = "atlona:pro3-88m:home:power" }
String Atlona_Version "Version [%s]" { channel = "atlona:pro3-88m:home:version" }
String Atlona_Type "Model [%s]" { channel = "atlona:pro3-88m:home:type" }
Switch Atlona_PanelLock "Panel Lock" { channel = "atlona:pro3-88m:home:panellock",autoupdate="false" }
Switch Atlona_ResetPorts "Reset All Ports" { channel = "atlona:pro3-88m:home:resetports",autoupdate="false" }
Switch Atlona_PortPower1 "Port Power 1" { channel = "atlona:pro3-88m:home:portpower1" }
Switch Atlona_PortPower2 "Port Power 2" { channel = "atlona:pro3-88m:home:portpower2" }
Switch Atlona_PortPower3 "Port Power 3" { channel = "atlona:pro3-88m:home:portpower3" }
Switch Atlona_PortPower4 "Port Power 4" { channel = "atlona:pro3-88m:home:portpower4" }
Switch Atlona_PortPower5 "Port Power 5" { channel = "atlona:pro3-88m:home:portpower5" }
Switch Atlona_PortPower6 "Port Power 6" { channel = "atlona:pro3-88m:home:portpower6" }
Switch Atlona_PortPower7 "Port Power 7" { channel = "atlona:pro3-88m:home:portpower7" }
Switch Atlona_PortPower8 "Port Power 8" { channel = "atlona:pro3-88m:home:portpower8" }
Switch Atlona_PortPower9 "Port Power 9" { channel = "atlona:pro3-88m:home:portpower9" }
Switch Atlona_PortPower10 "Port Power 10" { channel = "atlona:pro3-88m:home:portpower10" }
Number Atlona_PortAll "Set All Ports [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:portall",autoupdate="false" }
Number Atlona_PortOutput1 "Output 1 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:portoutput1" }
Number Atlona_PortOutput2 "Output 2 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:portoutput2" }
Number Atlona_PortOutput3 "Output 3 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:portoutput3" }
Number Atlona_PortOutput4 "Output 4 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:portoutput4" }
Number Atlona_PortOutput5 "Output 5 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:portoutput5" }
Number Atlona_PortOutput6 "Output 6 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:portoutput6" }
Number Atlona_PortOutput7 "Output 7 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:portoutput7" }
Number Atlona_PortOutput8 "Output 8 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:portoutput8" }
Number Atlona_PortOutput9 "Output 9 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:portoutput9" }
Number Atlona_PortOutput10 "Output 10 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:portoutput10" }
Number Atlona_PortMirror8 "Hdmi Mirror 8 [MAP(atlonaoutputports.map):%s]" { channel = "atlona:pro3-88m:home:portmirror8" }
Number Atlona_PortMirror10 "Hdmi Mirror 10 [MAP(atlonaoutputports.map):%s]" { channel = "atlona:pro3-88m:home:portmirror10" }
Number Atlona_Volume1 "Volume 1 [%s db]" { channel = "atlona:pro3-88m:home:volume1" }
Number Atlona_Volume2 "Volume 2 [%s db]" { channel = "atlona:pro3-88m:home:volume2" }
Number Atlona_Volume3 "Volume 3 [%s db]" { channel = "atlona:pro3-88m:home:volume3" }
Number Atlona_Volume4 "Volume 4 [%s db]" { channel = "atlona:pro3-88m:home:volume4" }
Number Atlona_Volume5 "Volume 5 [%s db]" { channel = "atlona:pro3-88m:home:volume5" }
Number Atlona_Volume6 "Volume 6 [%s db]" { channel = "atlona:pro3-88m:home:volume6" }
Switch Atlona_VolumeMute1 "Mute 1" { channel = "atlona:pro3-88m:home:volumemute1" }
Switch Atlona_VolumeMute2 "Mute 2" { channel = "atlona:pro3-88m:home:volumemute2" }
Switch Atlona_VolumeMute3 "Mute 3" { channel = "atlona:pro3-88m:home:volumemute3" }
Switch Atlona_VolumeMute4 "Mute 4" { channel = "atlona:pro3-88m:home:volumemute4" }
Switch Atlona_VolumeMute5 "Mute 5" { channel = "atlona:pro3-88m:home:volumemute5" }
Switch Atlona_VolumeMute6 "Mute 6" { channel = "atlona:pro3-88m:home:volumemute6" }
Switch Atlona_IRLock "IR Lock" { channel = "atlona:pro3-88m:home:irenable",autoupdate="false" }
Number Atlona_SavePreset "Save Preset [MAP(atlonapresets.map):%s]" { channel = "atlona:pro3-88m:home:saveio" }
Number Atlona_RecallPreset "Recall Preset [MAP(atlonapresets.map):%s]" { channel = "atlona:pro3-88m:home:recallio" }
Number Atlona_ClearPreset "Clear Preset [MAP(atlonapresets.map):%s]" { channel = "atlona:pro3-88m:home:cleario" }
Switch Atlona_ResetMatrix "Reset Matrix" { channel = "atlona:pro3-88m:home:resetmatrix",autoupdate="false" }
```

### SiteMap

```
sitemap demo label="Main Menu"
{
    Frame label="Atlona" {
        Text item=Atlona_Type
        Text item=Atlona_Version
        Text label="Device" {
            Switch item=Atlona_Power
            Switch item=Atlona_PanelLock
            Switch item=Atlona_ResetMatrix mappings=[ON="Reset"]
            Switch item=Atlona_IRLock
        }
        Text label="Ports" {
            Switch item=Atlona_ResetPorts mappings=[ON="Reset"]
            Switch item=Atlona_PortPower1 
            Switch item=Atlona_PortPower2
            Switch item=Atlona_PortPower3
            Switch item=Atlona_PortPower4
            Switch item=Atlona_PortPower5
            Switch item=Atlona_PortPower6
            Switch item=Atlona_PortPower7
            Switch item=Atlona_PortPower8
            Switch item=Atlona_PortPower9
            Switch item=Atlona_PortPower10
            Selection item=Atlona_PortAll mappings=[1="Input 1",2="Input 2",3="Input 3",4="Input 4",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput1 mappings=[1="Input 1",2="Input 2",3="Input 3",4="Input 4",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput2 mappings=[1="Input 1",2="Input 2",3="Input 3",4="Input 4",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput3 mappings=[1="Input 1",2="Input 2",3="Input 3",4="Input 4",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput4 mappings=[1="Input 1",2="Input 2",3="Input 3",4="Input 4",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput5 mappings=[1="Input 1",2="Input 2",3="Input 3",4="Input 4",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput6 mappings=[1="Input 1",2="Input 2",3="Input 3",4="Input 4",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput7 mappings=[1="Input 1",2="Input 2",3="Input 3",4="Input 4",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput8 mappings=[1="Input 1",2="Input 2",3="Input 3",4="Input 4",5="Input 5",6="Input 6",7="Input 7",8="Input 8"] visibility=[Atlona_PortMirror8==0]
            Selection item=Atlona_PortOutput9 mappings=[1="Input 1",2="Input 2",3="Input 3",4="Input 4",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput10 mappings=[1="Input 1",2="Input 2",3="Input 3",4="Input 4",5="Input 5",6="Input 6",7="Input 7",8="Input 8"] visibility=[Atlona_PortMirror10==0]
            Selection item=Atlona_PortMirror8 mappings=[0="None",1="Output 1",2="Output 2",3="Output 3",4="Output 4",5="Output 5",6="Output 6",7="Output 7",9="Output 9"]
            Selection item=Atlona_PortMirror10 mappings=[0="None",1="Output 1",2="Output 2",3="Output 3",4="Output 4",5="Output 5",6="Output 6",7="Output 7",9="Output 9"]
        }
        Text label="Audio" {
            Setpoint item=Atlona_Volume1 minValue=-79 maxValue=15
            Setpoint item=Atlona_Volume2 minValue=-79 maxValue=15
            Setpoint item=Atlona_Volume3 minValue=-79 maxValue=15
            Setpoint item=Atlona_Volume4 minValue=-79 maxValue=15
            Setpoint item=Atlona_Volume5 minValue=-79 maxValue=15
            Setpoint item=Atlona_Volume6 minValue=-79 maxValue=15
            Switch item=Atlona_VolumeMute1
            Switch item=Atlona_VolumeMute2
            Switch item=Atlona_VolumeMute3
            Switch item=Atlona_VolumeMute4
            Switch item=Atlona_VolumeMute5
            Switch item=Atlona_VolumeMute6
        }
        Text label="Presets" {
            Selection item=Atlona_SavePreset mappings=[1="Preset 1",2="Preset 2",3="Preset 3",4="Preset 4",5="Preset 5"]
            Selection item=Atlona_RecallPreset mappings=[1="Preset 1",2="Preset 2",3="Preset 3",4="Preset 4",5="Preset 5"]
            Selection item=Atlona_ClearPreset mappings=[1="Preset 1",2="Preset 2",3="Preset 3",4="Preset 4",5="Preset 5"]
        }
    }
}
```
A few notes on the above example:
- Atlona_PortOutput8 and Atlona_PortOutput10 (both are HDMI ports on the 8x8) are ONLY visible if the associated port mirroring (Atlona_PortMirror8 or Atlona_PortMirror10) is none (meaning that the HDMI ports are outputing from an input port and are NOT mirroring an output port).
- If you change the names of the input, output or preset labels, make sure you change them in both the sitemap and the following transform files. 

# Transformation Maps
### atlonainputports.map
```
1=Input 1
2=Input 2
3=Input 3
4=Input 4
5=Input 5
6=Input 6
7=Input 7
8=Input 8
-=-
NULL=-
```

### atlonaoutputports.map
```
1=Output 1
2=Output 2
3=Output 3
4=Output 4
5=Output 5
6=Output 6
7=Output 7
8=Output 8
9=Output 9
10=Output 10
-=-
NULL=-
```

### atlonapresets.map
```
0=None
1=Preset 1
2=Preset 2
3=Preset 3
4=Preset 4
5=Preset 5
-=None
NULL=None
```