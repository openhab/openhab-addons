# Vizio Binding

This binding connects Vizio TVs to openHAB.
The TV must support the Vizio SmartCast API that is found on 2016 and later models.

## Supported Things

There is currently only one supported thing type, which represents a Vizio TV using the `vizio_tv` id.
Multiple Things can be added if more than one Vizio TV is to be controlled.

## Discovery

Auto-discovery is supported if the Vizio TV can be located on the local network using mDNS.
Otherwise the Thing must be manually added.
When the TV is discovered, a pairing process to obtain an authentication token from the TV must be completed using the openHAB console. See below for details.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter   | Description                                                                                                                          |
|-------------|--------------------------------------------------------------------------------------------------------------------------------------|
| hostName    | The host name or IP address of the Vizio TV. Mandatory.                                                                              |
| port        | The port on the Vizio TV that listens for https connections. Default 7345; Use 9000 for older model TVs.                             |
| authToken   | The token that is used to authenticate all commands sent to the TV. See below for instructions to obtain via the openHAB console.    |
| appListJson | A JSON string that defines the apps that are available in the `activeApp` channel drop down. See below for instructions for editing. |

### Console Commands for Pairing

To obtain an authorization token that enables openHAB to authenticate with the TV, the following console commands must be used while the TV is turned on.
The first command will send a pairing start request to the TV. This triggers the TV to display a 4-digit pairing code on screen that must be sent with the second command.

Start Pairing:

```shell
openhab:vizio <thingUID> start_pairing <deviceName>
```

Substitute `<thingUID>` with thing's id, ie: `vizio_tv:00bc3e711660`
Substitute `<deviceName>` the desired device name that will appear in the TV's settings, under Mobile Devices, ie: `Vizio-openHAB`

Submit Pairing Code:

```shell
openhab:vizio <thingUID> submit_code <pairingCode>
```

Substitute `<thingUID>` with the same thing id used above
Substitute `<pairingCode>` with the 4-digit pairing code displayed on the TV, ie: `1234`

The console should then indicate that pairing was successful (token will be displayed) and that the token was saved to the Thing configuration.
If using file-based provisioning of the Thing, the authorization token must be added to the Thing configuration manually.
With an authorization token in place, the binding can now control the TV.

The authorization token text can be re-used in the event that it becomes necessary to setup the binding again.
By simply adding the token that is already recognized by the TV to the Thing configuration, the pairing process can be bypassed.

## Channels

The following channels are available:

| Channel ID  | Item Type | Description                                                                                                                                                                                 |
|-------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| power       | Switch    | Turn the power on the TV on or off. Note: TV may not turn on if power is switched off and the TV is configured for Eco mode.                                                                |
| volume      | Dimmer    | Control the volume on the TV (0-100%).                                                                                                                                                      |
| mute        | Switch    | Mute or unmute the volume on the TV.                                                                                                                                                        |
| source      | String    | Select the source input on the TV. The dropdown list is automatically populated from the TV.                                                                                                |
| activeApp   | String    | A dropdown containing a list of streaming apps defined by the `appListJson` config option that can be launched by the binding. An app started via remote control is automatically selected. |
| control     | Player    | Control Playback e.g. Play/Pause/Next/Previous/FForward/Rewind                                                                                                                              |
| button      | String    | Sends a remote control command the TV. See list of available commands below. (WriteOnly)                                                                                                    |

### List of available button commands for Vizio TVs

- PowerOn
- PowerOff
- PowerToggle
- VolumeUp
- VolumeDown
- MuteOn **(may only work as a toggle)**
- MuteOff **(may only work as a toggle)**
- MuteToggle
- ChannelUp
- ChannelDown
- PreviousCh
- InputToggle
- SeekFwd
- SeekBack
- Play
- Pause
- Up
- Down
- Left
- Right
- Ok
- Back
- Info
- Menu
- Home
- Exit
- Smartcast
- ccToggle
- PictureMode
- WideMode
- WideToggle

### App List Configuration

The Vizio API to launch and identify currently running apps on the TV is very complex.
To handle this, the binding maintains a JSON database of applications and their associated metadata in order to populate the `activeApp` dropdown with available apps.

When the Thing is started for the first time, this JSON database is saved into the `appListJson` configuration parameter.
This list of apps can be edited via the script editor on the Thing configuration.
By editing the JSON, apps that are not desired can be removed from the `activeApp` dropdown and newly discovered apps can be added.

An entry for an application has a `name` element and a `config` element containing `APP_ID`, `NAME_SPACE` and `MESSAGE` (null for most apps):

```json
{
   "name": "Crackle",
   "config": {
      "APP_ID": "5",
      "NAME_SPACE": 4,
      "MESSAGE": null
   }
},

```

If an app is running that is not currently recognized by the binding, the `activeApp` channel will display a message that contains the `APP_ID` and `NAME_SPACE` which can be used to create the missing record for that app in the JSON.

If an app that is in the JSON database fails to start when selected, try adjusting the `NAME_SPACE` value for that app.
`NAME_SPACE` seems to be a version number and adjusting the number up or down may correct the mismatch between the TV and the binding.

A current list of `APP_ID`'s can be found at <http://scfs.vizio.com/appservice/vizio_apps_prod.json>
and `NAME_SPACE` &amp; `MESSAGE` values needed can be found at <http://scfs.vizio.com/appservice/app_availability_prod.json>

If there is an error in the user supplied `appListJson`, the Thing will fail to start and display a CONFIGURATION_ERROR message.
If all text in `appListJson` is removed (set to null) and the Thing configuration saved, the binding will restore `appListJson` from the binding's JSON db.

## Full Example

### `vizio.things` Example

```java
// Vizio TV
vizio:vizio_tv:mytv1 "My Vizio TV" [ hostName="192.168.10.1", port=7345, authToken="idspisp0pd" ]

```

### `vizio.items` Example

```java
// Vizio TV items:

Switch TV_Power       "Power"              { channel="vizio:vizio_tv:mytv1:power" }
Dimmer TV_Volume      "Volume [%d %%]"     { channel="vizio:vizio_tv:mytv1:volume" }
Switch TV_Mute        "Mute"               { channel="vizio:vizio_tv:mytv1:mute" }
String TV_Source      "Source Input [%s]"  { channel="vizio:vizio_tv:mytv1:source" }
String TV_ActiveApp   "Current App: [%s]"  { channel="vizio:vizio_tv:mytv1:activeApp" }
Player TV_Control     "Playback Control"   { channel="vizio:vizio_tv:mytv1:control" }
String TV_Button      "Send Command to TV" { channel="vizio:vizio_tv:mytv1:button" }

```

### `vizio.sitemap` Example

```perl
sitemap vizio label="Vizio" {
    Frame label="My Vizio TV" {
        Switch item=TV_Power
        // Volume can be a Setpoint also
        Slider item=TV_Volume minValue=0 maxValue=100 step=1 icon="soundvolume"
        Switch item=TV_Mute icon="soundvolume_mute"
        Selection item=TV_Source icon="screen"
        Selection item=TV_ActiveApp icon="screen"
        Default item=TV_Control
        // This Selection is deprecated in favor of the Buttongrid element below
        Selection item=TV_Button
        Buttongrid label="Remote Control" staticIcon=material:tv_remote item=TV_Button buttons=[1:1:POWER="PowerToggle"=switch-off, 1:2:Home="Home"=f7:house, 1:3:Menu="Menu", 1:4:Exit="Exit", 2:2:Up="Up"=f7:arrowtriangle_up, 4:2:Down="Down"=f7:arrowtriangle_down, 3:1:Left="Left"=f7:arrowtriangle_left, 3:3:Right="Right"=f7:arrowtriangle_right, 3:2:Ok="Ok", 2:4:VolumeUp="Volume +", 4:4:VolumeDown="Volume -", 3:4:MuteToggle="Mute"=soundvolume_mute, 5:1:Info="Info", 5:2:Back="Back", 5:3:Smartcast="Smartcast", 5:4:InputToggle="Input Toggle", 6:1:SeekBack="Reverse"=f7:backward, 6:2:Play="Play"=f7:play, 6:3:Pause="Pause"=f7:pause,  6:4:SeekFwd="Forward"=f7:forward, 7:1:ChannelUp="Channel +", 7:2:ChannelDown="Channel -", 7:3:PreviousCh="Previous Ch", 8:1:PictureMode="Picture Mode", 8:2:WideMode="Wide Mode", 8:3:WideToggle="Wide Toggle", 8:4:ccToggle="CC Toggle"]
    }
}

```
