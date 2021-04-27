# HdmiCec Binding

Binding to control devices connected via HDMI using CEC protocol. For example if you have a Raspebrry Pi 3 connected to a AV Receiver or TV, you can control other devices connected that support CEC. This is ideal for Sony PlayStation 4 which can't be turned on via IR, or Amazon Fire TV (stick/tv/cube) devices that use BT remotes.

## Supported Things

It should support devices which support CEC. See [libcec supported devices](http://libcec.pulse-eight.com/Vendor/Support) for a list)

## Discovery

This version supports manual discovery. You can invoke discovery via the Refresh link in Paper UI. It will invoke device discovery on the CEC bus. This can take ~30s to switch on and enumerate the devices so is not suitable for continuous background refresh.

## Binding Configuration

The binding requires LibCEC to be installed and configured. 

```
// Things file for Raspberry Pi3
Bridge hdmicec:bridge:local [ cecClientPath="/usr/bin/cec-client", comPort="RPI"] 
```

For Raspberry Pi, add the openhab user to the video group.

```
sudo adduser openhab video
```

## Thing Configuration

Things need to be configured with the deviceIndex (a hex number) and address of the form n.n.n.n - these are found with the discovery process. Currently all devices are pulled in, regardless of type (TV, audio, player, recorder), and treated as the same device type. 

## Channels

- activeSource will send a signal to the target device to tell it to become active. This should switch it on, and configure the correct path through AV equipment to show the source.

- remoteButton will send a button down and up command to a thing. For example from the console you can use

```
smarthome:send hdmicec_equipment_local_Unknown_FireTVCube_remoteButton down
```

to send a down button to the device (in this case a Fire TV Cube). The remote support is dumb, you can try sending any command to any device.

| Names | Names | Names | Names | Names | Names |
| ---- | ---- | ---- | ---- | ---- | ---- |
| Select  |   0   |   Enter   |   Power   |   StopRecord  |   MuteFunction    |
| Up  |   1   |   Clear   |   VolumeUp    |   PauseRecord |   RestoreVolumeFunction   |
| Down    |   2   |   NextFavorite    |   VolumeDown  |   Reserved    |   TuneFunction    |
| Left    |   3   |   ChannelUp   |   Mute    |   Angle   |   SelectMediaFunction |
| Right   |   4   |   ChannelDown |   Play    |   Subpicture  |   SelectA/VInputFunction  |
| RightUp |   5   |   PreviousChannel |   Stop    |   VOD |   SelectAudioInputFunction    |
| RightDown   |   6   |   SoundSelect |   Pause   |   Guide   |   PowerToggleFunction |
| LeftUp  |   7   |   InputSelect |   Record  |   Timer   |   PowerOffFunction    |
| LeftDown    |   8   |   DisplayInformation  |   Rewind  |   InitialConfiguration    |   PowerOnFunction |
| RootMenu    |   9   |   Help    |   Fastforward |   PlayFunction    |   Blue    |
| SetupMenu   |   Dot |   PageUp  |   Eject   |   PausePlayFunction   |   Red |
| ContentsMenu    |       |   PageDown    |   Forward |   RecordFunction  |   Green   |
| FavoriteMenu    |       |       |   Backward    |   PauseRecordFunction |   Yellow  |
| Exit    |       |       |       |   StopFunction    |   Data    |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
