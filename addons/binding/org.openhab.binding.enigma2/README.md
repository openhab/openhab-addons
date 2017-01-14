# openHAB2 binding for Enigma2 STB

This is the openHAB2 binding for an Enigma2 STB
Here you can find a precompiled [Binding](https://github.com/tratho/org.openhab.binding.enigma2-dist)

After installing the binding into the your openHAB2 distribution, you can start discovering your devices through the PaperUI GUI (btw: see discovery hints on the bottom of this document).

After discovering and configuring the device through the web GUI you may use it. To access them, the 'classical openHAB1 way', trough the items / sitemaps / rules way. Here a few samples for the configuration:

From the **items/enimga2.items** file:
```
Switch  Enigma2_Power                          "Power: [%s]"          <switch>      { channel="bosesoundtouch:device:BOSEMACADDR:power" }
Dimmer  Enigma2_Volume                         "Volume: [%d %%]"      <volume>      { channel="bosesoundtouch:device:BOSEMACADDR:volume" }
Number  Enigma2_Volume                         "Volume: [%d %%]"      <volume>      { channel="bosesoundtouch:device:BOSEMACADDR:volume" }
Switch  Enigma2_Mute                           "mute: [%s]"           <volume_mute> { channel="bosesoundtouch:device:BOSEMACADDR:mute" }
String  Enigma2_PlayerControl                  "Mode: [%s]"           <text>        { channel="bosesoundtouch:device:BOSEMACADDR:playerControl" }
String  Enigma2_ChannelNumber                  "Zone: [%s]"           <text>        { channel="bosesoundtouch:device:BOSEMACADDR:channelNumber" }
String  Enigma2_nowPlayingChannel              "Zone control: [%s]"   <text>        { channel="bosesoundtouch:device:BOSEMACADDR:nowPlayingChannel" }
String  Enigma2_nowPlayingTitle                "Preset: [%s]"         <text>        { channel="bosesoundtouch:device:BOSEMACADDR:nowPlayingTitle" }
String  Enigma2_nowPlayingDescription          "Key Code: [%s]"       <text>        { channel="bosesoundtouch:device:BOSEMACADDR:nowPlayingDescription" }
String  Enigma2_nowPlayingDescriptionExtended  "Album: [%s]"          <text>        { channel="bosesoundtouch:device:BOSEMACADDR:nowPlayingDescriptionExtended" }
```

A simple sitemap **sitemaps/enimga2.sitemap**:

```
sitemap demo label="Enigma2 Test Items"
{
	Frame label="Enigma2" {
        Switch    item=Enigma2_Power
		Slider    item=Enigma2_Volume
		Setpoint  item=Enigma2_Volume
		Switch    item=Enigma2_Mute
		Text      item=Enigma2_PlayerControl
		Text      item=Enigma2_ChannelNumber
		Text      item=Enigma2_nowPlayingChannel
		Text      item=Enigma2_nowPlayingTitle
		Text      item=Bose1_KeyCode
		Text      item=Enigma2_nowPlayingDescription
		Text      item=Enigma2_nowPlayingDescriptionExtended
	}
}
```

If you need support or have new idea's please use the [openHAB cummunity](https://community.openhab.org/t/enigma2-binding/20178) to post the requests.

#### Known issues and Workarounds

##### Discovery
At the moment not really stable
Devices that should be found are
-optimuss
-dreambox
