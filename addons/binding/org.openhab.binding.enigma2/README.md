# openHAB2 binding for Enigma2 STB

This is the openHAB2 binding for an Enigma2 STB
Here you can find a precompiled [Binding](https://github.com/tratho/org.openhab.binding.enigma2-dist)

After installing the binding into the your openHAB2 distribution, you can start discovering your devices through the PaperUI GUI (btw: see discovery hints on the bottom of this document).

After discovering and configuring the device through the web GUI you may use it. To access them, the 'classical openHAB1 way', trough the items / sitemaps / rules way. Here a few samples for the configuration:

From the **items/enigma2.items** file:
```
Switch  Enigma2_Power                          "Power: [%s]"          <switch>      { channel="enigma2:device:IPADDRESS:power" }
Dimmer  Enigma2_Volume                         "Volume: [%d %%]"      <volume>      { channel="benigma2:device:IPADDRESS:volume" }
Number  Enigma2_Volume                         "Volume: [%d %%]"      <volume>      { channel="benigma2:device:IPADDRESS:volume" }
Switch  Enigma2_Mute                           "mute: [%s]"           <volume_mute> { channel="benigma2:device:IPADDRESS:mute" }
String  Enigma2_PlayerControl                  "Mode: [%s]"           <text>        { channel="benigma2:device:IPADDRESS:playerControl" }
String  Enigma2_Channel                        "Zone: [%s]"           <text>        { channel="benigma2:device:IPADDRESS:channel" }
String  Enigma2_RemoteKey                      "RemoteKey: [%s]"      <text>        { channel="benigma2:device:IPADDRESS:remoteKey" }
String  Enigma2_SendMessage                    "RemoteKey: [%s]"      <text>        { channel="benigma2:device:IPADDRESS:sendMessage" }
String  Enigma2_SendWarning                    "RemoteKey: [%s]"      <text>        { channel="benigma2:device:IPADDRESS:sendWarning" }
String  Enigma2_SendQuestion                   "RemoteKey: [%s]"      <text>        { channel="benigma2:device:IPADDRESS:sendQuestion" }
Switch  Enigma2_GetAnswer                      "RemoteKey: [%s]"      <switch>      { channel="benigma2:device:IPADDRESS:getAnswer" }
String  Enigma2_nowPlayingTitle                "Preset: [%s]"         <text>        { channel="benigma2:device:IPADDRESS:nowPlayingTitle" }
String  Enigma2_nowPlayingDescription          "Key Code: [%s]"       <text>        { channel="benigma2:device:IPADDRESS:nowPlayingDescription" }
String  Enigma2_nowPlayingDescriptionExtended  "Album: [%s]"          <text>        { channel="benigma2:device:IPADDRESS:nowPlayingDescriptionExtended" }
```

A simple sitemap **sitemaps/enigma2.sitemap**:

```
sitemap enigma2 label="Enigma2 Test Items"
{
    Frame label="Enigma2" {
        Switch    item=Enigma2_Power
        Slider    item=Enigma2_Volume
        Setpoint  item=Enigma2_Volume
        Switch    item=Enigma2_Mute
        Text      item=Enigma2_PlayerControl
        Text      item=Enigma2_Channel
        Text      item=Enigma2_RemoteKey
        Text      item=Enigma2_SendMessage
        Text      item=Enigma2_SendWarning
        Text      item=Enigma2_SendQuestion
        Switch    item=Enigma2_GetAnswer
        Text      item=Enigma2_nowPlayingChannel
        Text      item=Enigma2_nowPlayingTitle
        Text      item=Enigma2_nowPlayingDescription
        Text      item=Enigma2_nowPlayingDescriptionExtended
    }
}
```

If you need support or have new ideas please use the [openHAB cummunity](https://community.openhab.org/t/enigma2-binding/20178) to post the requests.

#### Known issues and Workarounds

##### Discovery
At the moment not really stable
Devices that should be found are
-optimuss
-dreambox
