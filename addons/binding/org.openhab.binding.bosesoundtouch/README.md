# openHAB2 binding for Bose SoundTouch

This is an initial version of my openHAB2 binding for the Bose SoundTouch multiroom system.

After installing the binding into the your openHAB2 distribution, you can start discovering your devices through the PaperUI GUI (btw: see discovery hints on the bottom of this document).

After discovering and configuring the device through the web GUI I think you want to use it. I access them the 'classical openHAB1 way' trough the items / sitemap / rules way. Here a few samples of my configuration:

From the **items/bose.items** file:
```
Switch  Bose1_Power                      "Power: [%s]"          <switch>      { channel="bosesoundtouch:device:BOSEMACADDR:power" }
Dimmer  Bose1_Volume                     "Volume: [%d %%]"      <volume>      { channel="bosesoundtouch:device:BOSEMACADDR:volume" }
Switch  Bose1_Mute                       "mute: [%s]"           <volume_mute> { channel="bosesoundtouch:device:BOSEMACADDR:mute" }
String  Bose1_OperationMode              "Mode: [%s]"           <text>        { channel="bosesoundtouch:device:BOSEMACADDR:operationMode" }
String  Bose1_ZoneInfo                   "Zone: [%s]"           <text>        { channel="bosesoundtouch:device:BOSEMACADDR:zoneInfo", autoupdate="false" }
String  Bose1_ZoneControl                "Zone control: [%s]"   <text>        { channel="bosesoundtouch:device:BOSEMACADDR:zoneControl" }
String  Bose1_KeyCode                    "Key Code: [%s]"       <text>        { channel="bosesoundtouch:device:BOSEMACADDR:keyCode" }
String  Bose1_nowPlayingAlbum            "Album: [%s]"          <text>        { channel="bosesoundtouch:device:BOSEMACADDR:nowPlayingAlbum" }
String  Bose1_nowPlayingArtist           "Artist: [%s]"         <text>        { channel="bosesoundtouch:device:BOSEMACADDR:nowPlayingArtist" }
String  Bose1_nowPlayingArtwork          "Art: [%s]"            <text>        { channel="bosesoundtouch:device:BOSEMACADDR:nowPlayingArtwork" }
String  Bose1_nowPlayingDescription      "Description: [%s]"    <text>        { channel="bosesoundtouch:device:BOSEMACADDR:nowPlayingDescription" }
String  Bose1_nowPlayingItemName         "Playing: [%s]"        <text>        { channel="bosesoundtouch:device:BOSEMACADDR:nowPlayingItemName" }
String  Bose1_nowPlayingPlayStatus       "Play state: [%s]"     <text>        { channel="bosesoundtouch:device:BOSEMACADDR:nowPlayingPlayStatus" }
String  Bose1_nowPlayingStationLocation  "Radio Location: [%s]" <text>        { channel="bosesoundtouch:device:BOSEMACADDR:nowPlayingStationLocation" }
String  Bose1_nowPlayingStationName      "Radio Name: [%s]"     <text>        { channel="bosesoundtouch:device:BOSEMACADDR:nowPlayingStationName" }
String  Bose1_nowPlayingTrack            "Track: [%s]"          <text>        { channel="bosesoundtouch:device:BOSEMACADDR:nowPlayingTrack" }
```

A simple sitemap **sitemaps/bose.sitemap**:

```
sitemap demo label="Bose Test Items"
{
	Frame label="Bose 1" {
        Switch item=Bose1_Power
		Slider item=Bose1_Volume
		Switch item=Bose1_Mute
		Text item=Bose1_OperationMode
		Text item=Bose1_ZoneInfo
		Text item=Bose1_ZoneControl
		Text item=Bose1_KeyCode
		Text item=Bose1_nowPlayingAlbum
		Text item=Bose1_nowPlayingArtist
		Text item=Bose1_nowPlayingArtwork
		Text item=Bose1_nowPlayingDescription
		Text item=Bose1_nowPlayingItemName
		Text item=Bose1_nowPlayingPlayStatus
		Text item=Bose1_nowPlayingStationLocation
		Text item=Bose1_nowPlayingTrack
	}
}
```

A few samples for the channels (for the CLI):
```
smarthome send Bose1_Volume "10"

smarthome send Bose1_KeyCode "PRESET_1"
smarthome send Bose1_ZoneControl "add <devicename>” e.g.
smarthome send Bose1_ZoneControl "add livingroom"
smarthome send Bose1_ZoneControl "add <device-mac-address>” e.g.
```
this also can be done through rules:

Bose.rule:
```
rule "Bose: Combine Kitchen with living room"
when
        Item Bose1_power changed
then
        if (Bose1_power.state == ON) {
                sendCommand(Bose1_ZoneControl, "add kitchen")
       }
end

rule "wake up"
when
        Time cron "0 30 6 ? * MON,TUE,WED,THU,FRI”
then
       sendCommand(Bose1_Power, ON)
       sendCommand(Bose1_Volume, 10)
       sendCommand(Bose1_KeyCode, “PRESET_1")
end
```

I hope this gives you some idea's how to use this plugin. It's not 100% ready but the things start to work.

If you need support or have new idea's please use the [openHAB cummunity](https://community.openhab.org/t/bose-soundtouch-binding/5678) to post the requests.


#### Known issues and Workarounds

##### Limmited support

Basic things work:

 * Turning On/Off
 * Setting / Reading Volume
 * Muting / Play / Pause
 * Grouping / Ungrouping devices
 * Start playing things from 'Presets'

What's missing is direct control of sources (Servers / Radio Stations / Spotify ...)  trough the interfaces. I currently also have no real concept how this can be adopted with the openHAB2 interface. If you have some good idea's and use cases, drop me a messing in the forum or open a ticket ;)

##### Discovery
I had some troubles to get discovery on linux working. It worked like a charm on my OSX devel box, but on my production server was not able to discover the bose devices.

After tracing it down: It seems that the Bonjour/MDNS/UPnP-Stuff on linux binds to ipv6 only, and currently the bose devices only support IPv4. The trick was to set
```shell
export _JAVA_OPTIONS="-Djava.net.preferIPv4Stack=true”
```
before starting openHAB. When this is set the discovery worked on the linux box.

##### State inconsistency after openHAB2
The next known issue is during openHAB2 startup:
The SoundTouch speakers are contacted in a quite early phase during openHAB2 startup and all status information is fetched. But in this stage it seems that the binding files are not pparsed, and so you see invalid states when you open the sitemap. Just turn the speakers on/off to refresh all states.

##### Detailed states missing
On my devel box I see all the detailed states. But on my production box I see that the states seem to be updated accordingly (on the console logs) but the values are not visible within the sitemap. Currently I don’t have an idea why. Maybe you can drop me a short note if it’s working on your box or not.

I think that’s all for now….

