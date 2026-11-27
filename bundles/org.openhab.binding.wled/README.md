# WLED Binding

This binding allows you to auto discover and use LED strings based on the WLED project:
<https://github.com/Aircoookie/WLED>

## Supported Things

| Thing Type ID | Description                                                                                                                                                |
|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `json`        | A bridge to a WLED device using the JSON API. Add this Thing first.                                                                                        |
| `segment`     | A segment is used to turn a LED strip or string, into 1 or more lights. Each segment is like a separate light globe that can have its own color or effect. |

## Discovery

The auto discovery will find your WLED if your network supports mDNS and the UDP port 5353 is not blocked by a fire wall.
Before discovering any WLED devices, you may wish to name them by providing a 'Server description' in the WLED web page, CONFIG>User Interface> setup page.
openHAB will then discover and auto name your WLED bridge Thing to the name provided as the 'Server description'.
Segments will be discovered with an Inbox scan after the bridge Thing is first showing up as ONLINE.
Any segments that have been given a name in the WLED firmware, will be given the same name when discovery adds them to the Inbox.

## Bridge Thing Configuration

| Parameter             | Description                                                                                                                                                                                                                                          | Required | Default |
|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------|
| `address`             | The full URL to your WLED device. Example is `http://192.168.0.2:80`                                                                                                                                                                                 | Y        |         |
| `pollTime`            | How often in seconds you want the states of the LED fetched in case you make changes with a non openHAB app, web browser, or the light is auto changing FX or presets.                                                                               | Y        | 10      |
| `saturationThreshold` | Allows you to use a colorpicker control linked to the `masterControls` channel to trigger only using the pure white LEDs instead of creating fake white light from the RGB channels. Try setting the value to 12 or leave this on 0 for RGB strings. | Y        | 0       |

## Bridge Thing Channels

| Channel                 | Type        | Description                                                                                                                                                                                                                         |
|-------------------------|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `globalBrightness`      | Dimmer      | Changes the brightness of all segments at the same time.                                                                                                                                                                            |
| `presets`               | String      | A list of presets that you can select from and will display -1 when no presets are running.                                                                                                                                         |
| `playlists`             | String      | A list of playlists that you can select from and will display -1 when none are running.                                                                                                                                             |
| `presetCycle`           | Switch      | Turns ON/OFF the automatic changing from one preset to the next. Only in V0.12.0 and older firmwares.                                                                                                                               |
| `presetDuration`        | Number:Time | How long in seconds it will display a preset for, before it begins to change from one preset to the next with `presetCycle` turned ON. Only in V0.12.0 and older firmwares.                                                         |
| `transformTime`         | Number:Time | How long in seconds it takes to transform/morph from one look to the next.                                                                                                                                                          |
| `sleep`                 | Switch      | Turns on the sleep or 'night light' timer which can be configured to work in many different ways. Refer to WLED documentation for how this can be setup. The default action is the light will fade to OFF over the next 60 minutes. |
| `sleepMode`             | String      | Timed Light Mode selects how the light will fade or increase when the sleep timer is turned ON.                                                                                                                                     |
| `sleepDuration`         | Number:Time | Time it takes to change/fade to the target brightness.                                                                                                                                                                              |
| `sleepTargetBrightness` | Dimmer      | Sets how bright the light will be after the sleep duration time has expired.                                                                                                                                                        |
| `syncSend`              | Switch      | Sends UDP packets that tell other WLED lights to follow this one.                                                                                                                                                                   |
| `syncReceive`           | Switch      | Allows UDP packets from other WLED lights to control this one.                                                                                                                                                                      |
| `liveOverride`          | String      | A value of "0" turns off, "1" will override live data to display what you want, and "2" overrides until you reboot the ESP device.                                                                                                  |
| `sleepRemaining`        | Number:Time | Read only. The time that is left until the `sleep` timer has finished, 0 when it is not running.                                                                                                                                    |
| `uptime`                | Number:Time | Read only. How long the WLED device has been running since it was last started. Needs firmware 0.14.0 or newer.                                                                                                                      |
| `freeHeap`              | Number:DataAmount | Read only. The free memory of the WLED device. Needs firmware 0.14.0 or newer.                                                                                                                                                 |
| `wifiSignal`            | Number:Dimensionless | Read only. The quality of the Wi-Fi signal in percent. Needs firmware 0.14.0 or newer.                                                                                                                                    |
| `estimatedCurrent`      | Number:ElectricCurrent | Read only. The current in Ampere that WLED estimates the LEDs are using. Needs firmware 0.14.0 or newer.                                                                                                                |
| `fps`                   | Number      | Read only. The frames per second that the device is showing. Needs firmware 0.14.0 or newer.                                                                                                                                        |
| `syncSendGroups`        | Number      | The sync groups this light sends to, as a bit mask: group 1 is 1, group 2 is 2, group 3 is 4 and so on up to 128. Needs firmware 0.14.0 or newer. |
| `syncReceiveGroups`     | Number      | The sync groups this light listens to, same bit mask as above.                                                                                                                                                                      |
| `mainSegment`           | Number      | The number of the main segment.                                                                                                                                                                                                     |
| `ledMap`                | Number      | Loads the LED map: 0 is `ledmap.json`, 1 to 9 are `ledmap1.json` to `ledmap9.json`. Write only, the device does not report the active map. Needs firmware 0.14.0 or newer.                                                                                                                                                                |
| `liveActive`            | Switch      | Read only. ON while another device or app sends live data (DDP, E1.31, Art-Net...). Needs firmware 0.14.0 or newer.                                                                                                                 |
| `liveMode`              | String      | Read only. The protocol of the live data.                                                                                                                                                                                           |
| `liveSource`            | String      | Read only. The IP address that sends the live data.                                                                                                                                                                                 |
| `audioReactive`         | Switch      | Turns the AudioReactive usermod on or off. Only works if the usermod is installed. The value is read from the device settings every 20 poll cycles.                                                                                 |
| `audioSyncMode`         | String      | Audio sync of the AudioReactive usermod: `0` off, `1` send the audio to other WLED devices, `2` receive the audio. Only works if the usermod is installed.                                                                          |

The bridge Thing also has the properties `firmwareVersion`, `name`, `ledCount`, `architecture` and `brand` when the firmware is 0.14.0 or newer.

## Thing Configuration

| Parameter      | Description                                                                                                              | Required | Default |
|----------------|--------------------------------------------------------------------------------------------------------------------------|----------|---------|
| `segmentIndex` | The index number to the LED segment you wish these channels to control. Leave on 0 if you do not know what a segment is. | Y        | 0       |

## Thing Channels

| Channel             | Type   | Description                                                                                                                                                                                                                                       |
|---------------------|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `masterControls`    | Color  | Gives you control over the WLED segment like it is any normal light. Tag this control for Alexa or Google/Nest to change the lights instantly to any colour, brightness or on/off state that you ask for regardless of what mode the light is in. |
| `segmentBrightness` | Dimmer | Allows you to Dim and turn the entire segment ON and OFF.                                                                                                                                                                                         |
| `primaryColor`      | Color  | The primary colour used in FX.                                                                                                                                                                                                                    |
| `primaryWhite`      | Dimmer | The amount of white light used in the primary colour. Only available if you have RGBW LEDs.                                                                                                                                                       |
| `secondaryColor`    | Color  | The secondary colour used in FX.                                                                                                                                                                                                                  |
| `secondaryWhite`    | Dimmer | The amount of white light used in the second colour. Only available if you have RGBW LEDs.                                                                                                                                                        |
| `tertiaryColor`     | Color  | The third colour used in FX.                                                                                                                                                                                                                      |
| `tertiaryWhite`     | Dimmer | The amount of white light used in the third colour. Only available if you have RGBW LEDs.                                                                                                                                                         |
| `palettes`          | String | A list of colour palettes you can select from that are used in the FX.                                                                                                                                                                            |
| `fx`                | String | A list of Effects you can select from.                                                                                                                                                                                                            |
| `speed`             | Dimmer | Changes the speed of the loaded effect.                                                                                                                                                                                                           |
| `intensity`         | Dimmer | Changes the intensity of the loaded effect.                                                                                                                                                                                                       |
| `mirror`            | Switch | Mirror the effect for this segment.                                                                                                                                                                                                               |
| `reverse`           | Switch | Reverse the effect for this segment.                                                                                                                                                                                                              |
| `grouping`          | Number | The number of LEDs that are grouped together to display as one pixel in FX. Use metadata to display a list widget slider.                                                                                                                         |
| `spacing`           | Number | The number of LEDs that will not light up in between FX pixels. Use metadata to display a list widget slider.                                                                                                                                     |
| `whiteTemperature`  | Dimmer | The white spectrum from warm (0%) to cold (100%). Only available if your LEDs have CCT support.                                                                                                                                                   |
| `custom1`           | Dimmer | The first custom slider of the effect. What it does depends on the selected effect. Needs firmware 0.14.0 or newer.                                                                                                                              |
| `custom2`           | Dimmer | The second custom slider of the effect. Needs firmware 0.14.0 or newer.                                                                                                                                                                           |
| `custom3`           | Dimmer | The third custom slider of the effect, this one only has 32 steps. Needs firmware 0.14.0 or newer.                                                                                                                                               |
| `option1`           | Switch | The first option of the effect. What it does depends on the selected effect. Needs firmware 0.14.0 or newer.                                                                                                                                     |
| `option2`           | Switch | The second option of the effect. Needs firmware 0.14.0 or newer.                                                                                                                                                                                  |
| `option3`           | Switch | The third option of the effect. Needs firmware 0.14.0 or newer.                                                                                                                                                                                   |
| `freeze`            | Switch | Freezes the effect of the segment on the current frame. Needs firmware 0.14.0 or newer.                                                                                                                                                          |
| `selected`          | Switch | Segments that are selected receive the commands that are sent without a segment, like in the WLED app.                                                                                                                                           |
| `segmentStart`      | Number | The first LED of the segment, counting starts at 0.                                                                                                                                                                                               |
| `segmentStop`       | Number | The first LED after the end of the segment, so this LED is not part of the segment.                                                                                                                                                              |
| `segmentName`       | String | The name of the segment. Needs firmware 0.13.0 or newer.                                                                                                                                                                                          |
| `mapping`           | String | How the effect is mapped on a 2D matrix: `0` pixels, `1` bar, `2` arc, `3` corner. Needs firmware 0.14.0 or newer.                                                                                                                              |
| `soundSimulation`   | String | The simulated sound for sound reactive effects without audio: `0` BeatSin, `1` WeWillRockYou, `2` 10_3, `3` 14_3. Needs firmware 0.14.0 or newer.                                                                                              |

Channels that were added to the binding are added to existing Things the next time the bridge connects.
Channels that the firmware or the LEDs can not use, like `whiteTemperature` for LEDs without CCT, are removed from the Thing.
If you update the firmware of the WLED device or change the LED type afterwards, restart the Things or re-add them to get the matching channels.

## Rule Actions

This binding has these rule Actions that can be used with the `json` bridge Thing:

| Action                                         | Description                                                                                                                  |
|------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| `savePreset(int presetNumber)`                 | Saves the current state of the WLED string into a preset slot that you can specify.                                          |
| `savePreset(int presetNumber, String name)`    | Same as above, but also gives the preset a name.                                                                             |
| `deletePreset(int presetNumber)`               | Deletes a preset slot. Needs firmware 0.13.0 or newer.                                                                       |
| `reboot()`                                     | Reboots the WLED device. Needs firmware 0.13.0 or newer.                                                                     |
| `getPresets()`                                 | Returns a text with a line for each preset and playlist, with name, brightness, transition time, effects, colors and for playlists the presets, durations and repeat. |
| `getPresetsJson()`                             | Returns the `presets.json` of the device with every setting of every preset.                                                 |
| `savePresetState(int slot, String name, String json)` | Saves a preset with the content you give, like `on`, `bri`, `transition` (in 0.1 seconds) and `seg`, instead of the current state. |
| `savePlaylist(int slot, String name, String presets, String durations, String transitions, int repeat, int end)` | Saves a playlist. `presets` is a list like `"1,2,3"`, `durations` and `transitions` are seconds like `"10,10,5"` (one value is used for all), `repeat` 0 is forever, `end` is the preset to apply after the last run. |
| `getSettings()`                                | Returns all settings of the device as JSON (`/json/cfg`).                                                                    |
| `getSetting(String path)`                      | Returns one setting, the path is the names separated by dots, for example `um.AudioReactive.sync.mode`.                       |
| `setSettings(String json)`                     | Changes settings, the JSON only needs the settings that change. Some settings only work after a `reboot()`.                   |
| `setSetting(String path, String value)`        | Changes one setting, for example `setSetting("um.AudioReactive.sync.mode", "1")` to send the audio to other devices.          |
| `getStateJson()`, `getInfoJson()`              | Return the raw `/json/state` and `/json/info`.                                                                               |
| `getNodes()`                                   | Returns the other WLED devices that this device found in the network.                                                        |
| `getLiveLeds()`                                | Returns the color of every LED right now.                                                                                    |
| `getEffectData()`                              | Returns what each effect uses (names of sliders and colors), from `/json/fxdata`.                                            |
| `syncClock()`                                  | Sets the clock of the device to the time of openHAB.                                                                         |
| `sendState(String json)`                       | Sends a raw JSON object to the WLED `/json/state` API, for features that have no channel. The reply updates the channels.    |

In Xtend rules, you can use the Actions like this.

```java
getActions("wled", "wled:json:XmasTree").savePreset(5,"Flashy Preset")
logInfo("wled", getActions("wled", "wled:json:XmasTree").getPresets())
getActions("wled", "wled:json:XmasTree").setSetting("um.AudioReactive.sync.mode", "1")
logInfo("wled", getActions("wled", "wled:json:XmasTree").getSetting("um.AudioReactive.sync.mode"))
getActions("wled", "wled:json:XmasTree").savePresetState(6, "Slow Rainbow", '{"on":true,"bri":128,"transition":20,"seg":[{"id":0,"fx":9,"sx":60}]}')
getActions("wled", "wled:json:XmasTree").savePlaylist(7, "Show", "1,2,6", "30,30,60", "2", 0, 0)
getActions("wled", "wled:json:XmasTree").sendState('{"seg":[{"id":0,"fx":9}]}')
```

## Sitemap Example

If you use the ADMIN>MODEL>`Create equipment from Thing` feature you can use the below and just change the name before the underscore to match what you named the `segment` Thing when it was added via the Inbox.

*.sitemap

```perl
        Text label="XmasLights" icon="rgb"{
            Switch item=XmasTree_MasterControls
            Slider item=XmasTree_SegmentBrightness
            Colorpicker item=XmasTree_MasterControls
            Switch item=XmasTree_SleepTimer
            Colorpicker item=XmasTree_PrimaryColor
            Colorpicker item=XmasTree_SecondaryColor
            Selection item=XmasTree_Effect
            Selection item=XmasTree_Palettes
            Selection item=XmasTree_Presets
            Default item=XmasTree_FXSpeed
            Default item=XmasTree_FXIntensity
            Selection item=XmasTree_TransformTime mappings=[0='0 seconds', 2 ='2 seconds', 10='10 seconds', 30='30 seconds', 60='60 seconds']
        }

```
