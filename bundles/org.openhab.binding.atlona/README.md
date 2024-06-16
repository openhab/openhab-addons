# Atlona Binding

This binding integrates [Atlona](https://www.atlona.com) AT-UHD-PRO3 or AT-PRO3HD HDBaseT matrix switches into your openHAB installation.

## Supported Things

This binding supports the following thing types:

| Thing         | Thing Type | Description                                                                                 |
|---------------|------------|---------------------------------------------------------------------------------------------|
| pro3-44m      | Thing      | The [AT-UHD-PRO3-44M 4x4 HDBaseT matrix](https://atlona.com/product/at-uhd-pro3-44m/)       |
| pro3-66m      | Thing      | The [AT-UHD-PRO3-66M 6x6 HDBaseT matrix](https://atlona.com/product/at-uhd-pro3-66m/)       |
| pro3-88m      | Thing      | The [AT-UHD-PRO3-88M 8x8 HDBaseT matrix](https://atlona.com/product/at-uhd-pro3-88m/)       |
| pro3-1616m    | Thing      | The [AT-UHD-PRO3-1616M 16x16 HDBaseT matrix](https://atlona.com/product/at-uhd-pro3-1616m/) |
| pro3-hd44m    | Thing      | The [AT-PRO3HD44M 4x4 HDBaseT matrix](https://atlona.com/product/at-pro3hd44m/)             |
| pro3-hd66m    | Thing      | The [AT-PRO3HD66M 6x6 HDBaseT matrix](https://atlona.com/product/at-pro3hd66m/)             |

## Discovery

The Atlona AT-UHD-PRO3 switch can be discovered by starting a discovery scan in the UI and then logging into your switch and pressing the "SDDP" button on the "Network" tab.
The "SDDP" (simple device discovery protocol) button will initiate the discovery process.
If "Telnet Login" is enabled ("Network" tab from the switch configuration UI), you will need to set the username and password in the configuration of the newly discovered thing before a connection can be made.

## Thing Configuration

The thing has the following configuration parameters:

| Name            | Type    | Description                                                             | Default | Required | Advanced |
|-----------------|---------|-------------------------------------------------------------------------|---------|----------|----------|
| ipAddress       | text    | Hostname or IP address of the matrix switch                             | N/A     | yes      | no       |
| userName        | text    | The username to login with (only if Telnet Login is enabled)            | N/A     | no       | yes      |
| password        | text    | The password to login with (only if Telnet Login is enabled)            | N/A     | no       | yes      |
| polling         | Integer | The interval to poll for the current state of the switch in sec.        | 600     | no       | yes      |
| ping            | Integer | The interval to ping the switch to keep the connection alive in sec.    | 30      | no       | yes      |
| retryPolling    | Integer | The interval to retry a connection if the connection has failed in sec. | 10      | no       | yes      |

### username/password

The userName/password configuration options are optional and are only required if you have your switch set with "Telnet Login" enabled (on the "Network" tab from the switch configuration UI).
The user must be a valid user listed on the "Users" tab of the switch configuration UI in this case.

### polling

Polling will automatically occur when (re)connecting to the switch to get the initial state of the switch.
If you have anything outside of openHAB that can modify the switch state (front panel, IR, telnet session or another automation system), you will likely want to set this setting to a much lower value.

### ping

The Atlona switch will time out any IP connection after a specific time (specified by "IP Timeout" on the "Network" tab from the switch configuration UI - 120 by default).
The ping setting MUST be lower than that value.
If it is higher than the "IP Timeout" value, the switch will timeout our connection and the thing will go OFFLINE (until a reconnect attempt is made).

## Channels

The following channels are available:

| Thing      | Channel ID                                                      | Item Type | Access | Description                                                                               |
|------------|-----------------------------------------------------------------|-----------|--------|-------------------------------------------------------------------------------------------|
| pro3-44m   | primary#power                                                   | Switch    | RW     | Matrix Power Switch                                                                       |
| pro3-44m   | primary#panellock                                               | Switch    | RW     | Sets the front panel locked or unlocked                                                   |
| pro3-44m   | primary#irenable                                                | Switch    | RW     | Enables/Disabled the front panel IR                                                       |
| pro3-44m   | primary#presetcmd                                               | Switch    | W      | Sends a preset command ('saveX', 'recallX', 'clearX') - see notes below                   |
| pro3-44m   | primary#matrixcmd                                               | Switch    | W      | Sends a matrix command ('resetmatrix', 'resetports', 'allportsX') - see notes below       |
| pro3-44m   | port1#portpower                                                 | Switch    | RW     | Enables/Disables output port #1                                                           |
| pro3-44m   | port1#portoutput                                                | Number    | RW     | Sets output port #1 to the specified input port                                           |
| pro3-44m   | port2#portpower                                                 | Switch    | RW     | Enables/Disables output port #2                                                           |
| pro3-44m   | port2#portoutput                                                | Number    | RW     | Sets output port #2 to the specified input port                                           |
| pro3-44m   | port3#portpower                                                 | Switch    | RW     | Enables/Disables output port #3                                                           |
| pro3-44m   | port3#portoutput                                                | Number    | RW     | Sets output port #3 to the specified input port                                           |
| pro3-44m   | port4#portpower                                                 | Switch    | RW     | Enables/Disables output port #4                                                           |
| pro3-44m   | port4#portoutput                                                | Number    | RW     | Sets output port #4 to the specified input port                                           |
| pro3-44m   | port5#portpower                                                 | Switch    | RW     | Enables/Disables output port #5                                                           |
| pro3-44m   | port5#portoutput                                                | Number    | RW     | Sets output port #5 to the specified input port                                           |
| pro3-44m   | mirror5#portmirrorenabled                                       | Number    | RW     | Sets hdmi port #5 to enable/disable port mirroring                                        |
| pro3-44m   | mirror5#portmirror                                              | Number    | RW     | Sets hdmi port #5 to mirror the specified output port (if enabled)                        |
| pro3-44m   | volume1#volume                                                  | Number    | RW     | Sets the volume of audio port #1 to the specified decibel level (between -79db to +15db)  |
| pro3-44m   | volume1#volumemute                                              | Switch    | RW     | Mutes/Unmutes audio port #1                                                               |
| pro3-44m   | volume2#volume                                                  | Number    | RW     | Sets the volume of audio port #2 to the specified decibel level (between -79db to +15db)  |
| pro3-44m   | volume2#volumemute                                              | Switch    | RW     | Mutes/Unmutes audio port #2                                                               |
| pro3-44m   | volume3#volume                                                  | Number    | RW     | Sets the volume of audio port #3 to the specified decibel level (between -79db to +15db)  |
| pro3-44m   | volume3#volumemute                                              | Switch    | RW     | Mutes/Unmutes audio port #3                                                               |
|            |                                                                 |           |        |                                                                                           |
| pro3-66m   | ALL OF THE pro3-44M channels (except different mirror settings) |           |        |                                                                                           |
| pro3-66m   | port6#portpower                                                 | Switch    | RW     | Enables/Disables output port #6                                                           |
| pro3-66m   | port6#portoutput                                                | Number    | RW     | Sets output port #6 to the specified input port                                           |
| pro3-66m   | port7#portpower                                                 | Switch    | RW     | Enables/Disables output port #7                                                           |
| pro3-66m   | port7#portoutput                                                | Number    | RW     | Sets output port #7 to the specified input port                                           |
| pro3-66m   | port8#portpower                                                 | Switch    | RW     | Enables/Disables output port #8                                                           |
| pro3-66m   | port8#portoutput                                                | Number    | RW     | Sets output port #8 to the specified input port                                           |
| pro3-66m   | mirror6#portmirrorenabled                                       | Number    | RW     | Sets hdmi port #6 to enable/disable port mirroring                                        |
| pro3-66m   | mirror6#portmirror                                              | Number    | RW     | Sets hdmi port #6 to mirror the specified output port (if enabled)                        |
| pro3-66m   | mirror8#portmirrorenabled                                       | Number    | RW     | Sets hdmi port #8 to enable/disable port mirroring                                        |
| pro3-66m   | mirror8#portmirror                                              | Number    | RW     | Sets hdmi port #8 to mirror the specified output port (if enabled)                        |
| pro3-66m   | volume4#volume                                                  | Number    | RW     | Sets the volume of audio port #4 to the specified decibel level (between -79db to +15db)  |
| pro3-66m   | volume4#volumemute                                              | Switch    | RW     | Mutes/Unmutes audio port #4                                                               |
|            |                                                                 |           |        |                                                                                           |
| pro3-88m   | ALL OF THE pro3-66M channels (except different mirror settings) |           |        |                                                                                           |
| pro3-88m   | port9#portpower                                                 | Switch    | RW     | Enables/Disables output port #9                                                           |
| pro3-88m   | port9#portoutput                                                | Number    | RW     | Sets output port #9 to the specified input port                                           |
| pro3-88m   | port10#portpower                                                | Switch    | RW     | Enables/Disables output port #10                                                          |
| pro3-88m   | port10#portoutput                                               | Number    | RW     | Sets output port #10 to the specified input port                                          |
| pro3-88m   | mirror8#portmirrorenabled                                       | Number    | RW     | Sets hdmi port #8 to enable/disable port mirroring                                        |
| pro3-88m   | mirror8#portmirror                                              | Number    | RW     | Sets hdmi port #8 to mirror the specified output port (if enabled)                        |
| pro3-88m   | mirror10#portmirrorenabled                                      | Number    | RW     | Sets hdmi port #10 to enable/disable port mirroring                                       |
| pro3-88m   | mirror10#portmirror                                             | Number    | RW     | Sets hdmi port #10 to mirror the specified output port (if enabled)                       |
| pro3-88m   | volume5#volume                                                  | Number    | RW     | Sets the volume of audio port #5 to the specified decibel level (between -79db to +15db)  |
| pro3-88m   | volume5#volumemute                                              | Switch    | RW     | Mutes/Unmutes audio port #5                                                               |
| pro3-88m   | volume6#volume                                                  | Number    | RW     | Sets the volume of audio port #6 to the specified decibel level (between -79db to +15db)  |
| pro3-88m   | volume6#volumemute                                              | Switch    | RW     | Mutes/Unmutes audio port #6                                                               |
|            |                                                                 |           |        |                                                                                           |
| pro3-1616m | ALL OF THE pro3-88M channels (except different mirror settings) |           |        |                                                                                           |
| pro3-1616m | port11#portpower                                                | Switch    | RW     | Enables/Disables output port #11                                                          |
| pro3-1616m | port11#portoutput                                               | Number    | RW     | Sets output port #11 to the specified input port                                          |
| pro3-1616m | port12#portpower                                                | Switch    | RW     | Enables/Disables output port #12                                                          |
| pro3-1616m | port12#portoutput                                               | Number    | RW     | Sets output port #12 to the specified input port                                          |
| pro3-1616m | port13#portpower                                                | Switch    | RW     | Enables/Disables output port #13                                                          |
| pro3-1616m | port13#portoutput                                               | Number    | RW     | Sets output port #13 to the specified input port                                          |
| pro3-1616m | port14#portpower                                                | Switch    | RW     | Enables/Disables output port #14                                                          |
| pro3-1616m | port14#portoutput                                               | Number    | RW     | Sets output port #14 to the specified input port                                          |
| pro3-1616m | port15#portpower                                                | Switch    | RW     | Enables/Disables output port #15                                                          |
| pro3-1616m | port15#portoutput                                               | Number    | RW     | Sets output port #15 to the specified input port                                          |
| pro3-1616m | port16#portpower                                                | Switch    | RW     | Enables/Disables output port #16                                                          |
| pro3-1616m | port16#portoutput                                               | Number    | RW     | Sets output port #16 to the specified input port                                          |
| pro3-1616m | port17#portpower                                                | Switch    | RW     | Enables/Disables output port #17                                                          |
| pro3-1616m | port17#portoutput                                               | Number    | RW     | Sets output port #17 to the specified input port                                          |
| pro3-1616m | port18#portpower                                                | Switch    | RW     | Enables/Disables output port #18                                                          |
| pro3-1616m | port18#portoutput                                               | Number    | RW     | Sets output port #18 to the specified input port                                          |
| pro3-1616m | port19#portpower                                                | Switch    | RW     | Enables/Disables output port #19                                                          |
| pro3-1616m | port19#portoutput                                               | Number    | RW     | Sets output port #19 to the specified input port                                          |
| pro3-1616m | port20#portpower                                                | Switch    | RW     | Enables/Disables output port #20                                                          |
| pro3-1616m | port20#portoutput                                               | Number    | RW     | Sets output port #20 to the specified input port                                          |
| pro3-1616m | mirror17#portmirrorenabled                                      | Number    | RW     | Sets hdmi port #17 to enable/disable port mirroring                                       |
| pro3-1616m | mirror17#portmirror                                             | Number    | RW     | Sets hdmi port #17 to mirror the specified output port (if enabled)                       |
| pro3-1616m | mirror18#portmirrorenabled                                      | Number    | RW     | Sets hdmi port #18 to enable/disable port mirroring                                       |
| pro3-1616m | mirror18#portmirror                                             | Number    | RW     | Sets hdmi port #18 to mirror the specified output port (if enabled)                       |
| pro3-1616m | mirror19#portmirrorenabled                                      | Number    | RW     | Sets hdmi port #19 to enable/disable port mirroring                                       |
| pro3-1616m | mirror19#portmirror                                             | Number    | RW     | Sets hdmi port #19 to mirror the specified output port (if enabled)                       |
| pro3-1616m | mirror20#portmirrorenabled                                      | Number    | RW     | Sets hdmi port #20 to enable/disable port mirroring                                       |
| pro3-1616m | mirror20#portmirror                                             | Number    | RW     | Sets hdmi port #20 to mirror the specified output port (if enabled)                       |
| pro3-1616m | volume7#volume                                                  | Number    | RW     | Sets the volume of audio port #7 to the specified decibel level (between -79db to +15db)  |
| pro3-1616m | volume7#volumemute                                              | Switch    | RW     | Mutes/Unmutes audio port #7                                                               |
| pro3-1616m | volume8#volume                                                  | Number    | RW     | Sets the volume of audio port #8 to the specified decibel level (between -79db to +15db)  |
| pro3-1616m | volume8#volumemute                                              | Switch    | RW     | Mutes/Unmutes audio port #8                                                               |
| pro3-1616m | volume9#volume                                                  | Number    | RW     | Sets the volume of audio port #9 to the specified decibel level (between -79db to +15db)  |
| pro3-1616m | volume9#volumemute                                              | Switch    | RW     | Mutes/Unmutes audio port #9                                                               |
| pro3-1616m | volume10#volume                                                 | Number    | RW     | Sets the volume of audio port #10 to the specified decibel level (between -79db to +15db) |
| pro3-1616m | volume10#volumemute                                             | Switch    | RW     | Mutes/Unmutes audio port #10                                                              |
| pro3-1616m | volume11#volume                                                 | Number    | RW     | Sets the volume of audio port #11 to the specified decibel level (between -79db to +15db) |
| pro3-1616m | volume11#volumemute                                             | Switch    | RW     | Mutes/Unmutes audio port #11                                                              |
| pro3-1616m | volume12#volume                                                 | Number    | RW     | Sets the volume of audio port #12 to the specified decibel level (between -79db to +15db) |
| pro3-1616m | volume12#volumemute                                             | Switch    | RW     | Mutes/Unmutes audio port #12                                                              |
|            |                                                                 |           |        |                                                                                           |
| pro3-hd44m | primary#power                                                   | Switch    | RW     | Matrix Power Switch                                                                       |
| pro3-hd44m | primary#panellock                                               | Switch    | RW     | Sets the front panel locked or unlocked                                                   |
| pro3-hd44m | primary#irenable                                                | Switch    | RW     | Enables/Disabled the front panel IR                                                       |
| pro3-hd44m | primary#presetcmd                                               | Switch    | W      | Sends a preset command ('saveX', 'recallX', 'clearX') - see notes below                   |
| pro3-hd44m | primary#matrixcmd                                               | Switch    | W      | Sends a matrix command ('resetmatrix', 'resetports', 'allportsX') - see notes below       |
| pro3-hd44m | port1#portoutput                                                | Number    | RW     | Sets output port #1 to the specified input port                                           |
| pro3-hd44m | port2#portoutput                                                | Number    | RW     | Sets output port #2 to the specified input port                                           |
| pro3-hd44m | port3#portoutput                                                | Number    | RW     | Sets output port #3 to the specified input port                                           |
| pro3-hd44m | port4#portoutput                                                | Number    | RW     | Sets output port #4 to the specified input port                                           |
|            |                                                                 |           |        |                                                                                           |
| pro3-hd66m | primary#power                                                   | Switch    | RW     | Matrix Power Switch                                                                       |
| pro3-hd66m | primary#panellock                                               | Switch    | RW     | Sets the front panel locked or unlocked                                                   |
| pro3-hd66m | primary#irenable                                                | Switch    | RW     | Enables/Disabled the front panel IR                                                       |
| pro3-hd66m | primary#presetcmd                                               | Switch    | W      | Sends a preset command ('saveX', 'recallX', 'clearX') - see notes below                   |
| pro3-hd66m | primary#matrixcmd                                               | Switch    | W      | Sends a matrix command ('resetmatrix', 'resetports', 'allportsX') - see notes below       |
| pro3-hd66m | port1#portoutput                                                | Number    | RW     | Sets output port #1 to the specified input port                                           |
| pro3-hd66m | port2#portoutput                                                | Number    | RW     | Sets output port #2 to the specified input port                                           |
| pro3-hd66m | port3#portoutput                                                | Number    | RW     | Sets output port #3 to the specified input port                                           |
| pro3-hd66m | port4#portoutput                                                | Number    | RW     | Sets output port #4 to the specified input port                                           |
| pro3-hd66m | port5#portoutput                                                | Number    | RW     | Sets output port #5 to the specified input port                                           |
| pro3-hd66m | port5#portoutput                                                | Number    | RW     | Sets output port #6 to the specified input port                                           |

### presetcmd

The presetcmd channel will take the following commands:

| Command | Description                                |
|---------|--------------------------------------------|
| saveX   | Saves the current input/output to preset X |
| recallX | Sets the input/output to preset X          |
| clearX  | Clears the preset X                        |

Note: if X doesn't exist - nothing will occur.
The # of presets allowed depends on the firmware you are using (5 presets up to rev 13, 10 for rev 14 and above).

### matrixcmd

The matrixcmd channel will take the following commands:

| Command     | Description                                                                                                                         |
|-------------|-------------------------------------------------------------------------------------------------------------------------------------|
| resetmatrix | Resets the matrix back to its default values (USE WITH CARE!). Note: some firmware upgrades require a resetmatrix after installing. |
| resetports  | Resets the ports back to their default values (outputX=inputX)                                                                      |
| allportsX   | Sets all the output ports to the input port X                                                                                       |

Note: if X doesn't exist - nothing will occur.
The # of presets allowed depends on the firmware you are using (5 presets up to rev 13, 10 for rev 14 and above).

## Changes/Warnings

Note: Firmware versions 16.1.x are now the latest available for the AT-UHD-PRO3 line.
The following issues were noted back when this binding was originally developed using older firmware versions.

As of firmware 1.6.03 (rev 13), there were three issues with the Atlona firmware:

- The `clearX` command does not work.  The TCP/IP command "ClearX" as specified in Atlona's protocol will ALWAYS return a "Command Failed".  Please avoid this channel until Atlona releases a new firmware.

- There is no way to query for the current status of `panellock` and `irenable`.  The thing will default `panellock` to OFF and `irenable` to ON at startup.

- If you make a change in the switches UI that requires a reboot (mainly changing any of the settings on the "Network" tab in the switch configuration UI), this add-on's connection will be inconsistently closed at different times.
The thing will go OFFLINE and then back ONLINE when the reconnect attempt is made - and then it starts all over again.  Please make sure you reboot as soon as possible when the switch UI notifies you.

- A bug in the firmware will sometimes cause memory presets to disappear after a reboot.

As of firmware 1.6.8 (rev 14),

- The `clearX` command has been fixed and works now.

- The number of presets has increased to 10.

- If telnet mode is enabled, you must use the admin username/password to issue a `resetmatrix` command.

## Full Example

### Things

Here is an example with minimal configuration parameters (using default values with no telnet login):

```java
atlona:pro3-88m:home [ ipAddress="192.168.1.30" ]
```

Here is another example with minimal configuration parameters (using default values with telnet login):

```java
atlona:pro3-88m:home [ ipAddress="192.168.1.30", userName="me", password="12345" ]
```

Here is a full configuration example:

```java
atlona:pro3-88m:home [ ipAddress="192.168.1.30", userName="me", password="12345", polling=600, ping=30, retryPolling=10 ]
```

### Items

Here is an example of items for the AT-UHD-PRO33-88M:

```java
Switch Atlona_Power "Power" { channel = "atlona:pro3-88m:home:primary#power" }
Switch Atlona_PanelLock "Panel Lock" { channel = "atlona:pro3-88m:home:primary#panellock" }
Switch Atlona_Presets "Preset Command" { channel = "atlona:pro3-88m:home:primary#presetcmd" }
Switch Atlona_IRLock "IR Lock" { channel = "atlona:pro3-88m:home:primary#irenable" }
Switch Atlona_PortPower1 "Port Power 1" { channel = "atlona:pro3-88m:home:port1#power" }
Switch Atlona_PortPower2 "Port Power 2" { channel = "atlona:pro3-88m:home:port2#power" }
Switch Atlona_PortPower3 "Port Power 3" { channel = "atlona:pro3-88m:home:port3#power" }
Switch Atlona_PortPower4 "Port Power 4" { channel = "atlona:pro3-88m:home:port4#power" }
Switch Atlona_PortPower5 "Port Power 5" { channel = "atlona:pro3-88m:home:port5#power" }
Switch Atlona_PortPower6 "Port Power 6" { channel = "atlona:pro3-88m:home:port6#power" }
Switch Atlona_PortPower7 "Port Power 7" { channel = "atlona:pro3-88m:home:port7#power" }
Switch Atlona_PortPower8 "Port Power 8" { channel = "atlona:pro3-88m:home:port8#power" }
Switch Atlona_PortPower9 "Port Power 9" { channel = "atlona:pro3-88m:home:port9#power" }
Switch Atlona_PortPower10 "Port Power 10" { channel = "atlona:pro3-88m:home:port10#power" }
Number Atlona_PortOutput1 "Living Room [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:port1#portoutput" }
Number Atlona_PortOutput2 "Master Bed [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:port2#portoutput" }
Number Atlona_PortOutput3 "Kitchen [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:port3#portoutput" }
Number Atlona_PortOutput4 "Output 4 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:port4#portoutput" }
Number Atlona_PortOutput5 "Output 5 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:port5#portoutput" }
Number Atlona_PortOutput6 "Output 6 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:port6#portoutput" }
Number Atlona_PortOutput7 "Output 7 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:port7#portoutput" }
Number Atlona_PortOutput8 "Output 8 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:port8#portoutput" }
Number Atlona_PortOutput9 "Output 9 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:port9#portoutput" }
Number Atlona_PortOutput10 "Output 10 [MAP(atlonainputports.map):%s]" { channel = "atlona:pro3-88m:home:port10#portoutput" }
Number Atlona_PortMirror8 "Hdmi Mirror 8 [MAP(atlonaoutputports.map):%s]" { channel = "atlona:pro3-88m:home:mirror8#portmirror" }
Number Atlona_PortMirror10 "Hdmi Mirror 10 [MAP(atlonaoutputports.map):%s]" { channel = "atlona:pro3-88m:home:mirror10#portmirror" }
Number Atlona_Volume1 "Volume 1 [%s db]" { channel = "atlona:pro3-88m:home:volume1#volume" }
Number Atlona_Volume2 "Volume 2 [%s db]" { channel = "atlona:pro3-88m:home:volume2#volume" }
Number Atlona_Volume3 "Volume 3 [%s db]" { channel = "atlona:pro3-88m:home:volume3#volume" }
Number Atlona_Volume4 "Volume 4 [%s db]" { channel = "atlona:pro3-88m:home:volume4#volume" }
Number Atlona_Volume5 "Volume 5 [%s db]" { channel = "atlona:pro3-88m:home:volume5#volume" }
Number Atlona_Volume6 "Volume 6 [%s db]" { channel = "atlona:pro3-88m:home:volume6#volume" }
Switch Atlona_VolumeMute1 "Mute 1" { channel = "atlona:pro3-88m:home:volume1#volumemute" }
Switch Atlona_VolumeMute2 "Mute 2" { channel = "atlona:pro3-88m:home:volume1#volumemute" }
Switch Atlona_VolumeMute3 "Mute 3" { channel = "atlona:pro3-88m:home:volume1#volumemute" }
Switch Atlona_VolumeMute4 "Mute 4" { channel = "atlona:pro3-88m:home:volume1#volumemute" }
Switch Atlona_VolumeMute5 "Mute 5" { channel = "atlona:pro3-88m:home:volume1#volumemute" }
Switch Atlona_VolumeMute6 "Mute 6" { channel = "atlona:pro3-88m:home:volume1#volumemute" }
```

### Sitemap

```perl
sitemap demo label="Main Menu" {
    Frame label="Atlona" {
        Text label="Device" {
            Switch item=Atlona_Power
            Switch item=Atlona_PanelLock
            Switch item=Atlona_IRLock
            Text item=Atlona_Presets
        }
        Text label="Ports" {
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
            Selection item=Atlona_PortOutput1 mappings=[1="CableBox",2="Blu-ray Player",3="Roku",4="Apple TV",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput2 mappings=[1="CableBox",2="Blu-ray Player",3="Roku",4="Apple TV",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput3 mappings=[1="CableBox",2="Blu-ray Player",3="Roku",4="Apple TV",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput4 mappings=[1="CableBox",2="Blu-ray Player",3="Roku",4="Apple TV",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput5 mappings=[1="CableBox",2="Blu-ray Player",3="Roku",4="Apple TV",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput6 mappings=[1="CableBox",2="Blu-ray Player",3="Roku",4="Apple TV",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput7 mappings=[1="CableBox",2="Blu-ray Player",3="Roku",4="Apple TV",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput8 mappings=[1="CableBox",2="Blu-ray Player",3="Roku",4="Apple TV",5="Input 5",6="Input 6",7="Input 7",8="Input 8"] visibility=[Atlona_PortMirror8==0]
            Selection item=Atlona_PortOutput9 mappings=[1="CableBox",2="Blu-ray Player",3="Roku",4="Apple TV",5="Input 5",6="Input 6",7="Input 7",8="Input 8"]
            Selection item=Atlona_PortOutput10 mappings=[1="CableBox",2="Blu-ray Player",3="Roku",4="Apple TV",5="Input 5",6="Input 6",7="Input 7",8="Input 8"] visibility=[Atlona_PortMirror10==0]
            Selection item=Atlona_PortMirror8 mappings=[0="None",1="Living Room",2="Master Bed",3="Kitchen",4="Output 4",5="Output 5",6="Output 6",7="Output 7",9="Output 9"]
            Selection item=Atlona_PortMirror10 mappings=[0="None",1="Living Room",2="Master Bed",3="Kitchen",4="Output 4",5="Output 5",6="Output 6",7="Output 7",9="Output 9"]
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
    }
}
```

## Transformation Maps

The following is some example transformation maps you can create.
Be sure they are in sync with the mappings above.

### atlonainputports.map

```text
1=CableBox
2=Blu-ray Player
3=Roku
4=Apple TV
5=Input 5
6=Input 6
7=Input 7
8=Input 8
-=-
NULL=-
```

### atlonaoutputports.map

```text
1=Living Room
2=Master Bed
3=Kitchen
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
