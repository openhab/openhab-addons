# EspMilightHub Binding

This binding allows an open source esp8266 based bridge to automatically find and add Milight globes.
The hubs can be built from 2 ready made boards and only need connecting with 7 wires.
They can be very easy to build with no soldering needed.

Advantages to using this DIY bridge over the OEM bridge:

- Almost unlimited groups to give individual control over an entire house of Milight globes without needing multiple bridges.
- If using the Milight remotes to control the globes, this binding can update the openHAB controls the moment a key is pressed on the physical remotes.
- Supports auto discovery.

## Setup the hardware

In depth details on how to build and what the bridge is can be found here: <https://blog.christophermullins.com/2017/02/11/milight-wifi-gateway-emulator-on-an-esp8266>

A quick overview of the steps to get the hardware going are:

- Connect a nodemcu/D1 mini/esp8266 to your computer via a USB cable.
- Download the latest BIN file from here <https://github.com/sidoh/esp8266_milight_hub/releases>
- Download esp8266flasher if you are on windows <https://github.com/nodemcu/nodemcu-flasher>
- Check the blog above on more info for Mac or Linux.
- Open the flasher tool and make sure the flash size is 4mb or whatever your esp8266 board has.
- Flash the bin and press the reset button on the board when it completes.
- Connect to the wifi access point of the esp directly with your phone/tablet and setup wifi details.
- Login by using the IP address of the esp8266 in a web browser and the control panel will show up.
- Connect 7 wires between the two ready made PCBs as shown in the blog.
- Setup a MQTT broker as this method uses the faster and lightweight MQTT protocol and not UDP.

## Setup the Firmware

Enter the control panel for the ESP8266 by using any browser and enter the IP address.
The following options need to be changed in the firmware for the binding to work.
Click on SETTINGS>MQTT>:

**mqtt_topic_pattern:**
`milight/commands/:device_id/:device_type/:group_id`

**mqtt_update_topic_pattern:**
Leave this blank.

**mqtt_state_topic_pattern:**
`milight/states/:device_id/:device_type/:group_id`

**MQTT Client Status Topic:**
`milight/status`

**MQTT Client Status Messages Mode:**
Simple.

**group_state_fields:**
IMPORTANT: Make sure only the following are ticked:

- state
- level
- hue
- saturation
- mode
- color_temp
- bulb_mode

Fill in the MQTT broker fields with the correct details so the hub can connect and then click **save**.
Now when you use any Milight remote control, you will see MQTT topics being created that should include `level` and `hsb` in the messages.
If you see `brightness` and not `level`, then go back and follow the above setup steps.

You can use this Linux command to watch all MQTT topics from Milight:

```shell
mosquitto_sub -u usernamehere -P passwordhere -p 1883 -v -t 'milight/#'
```

You can also use the mosquitto_pub command to send your own commands and watch the bulbs respond all without the binding being setup.
Everything this binding does goes in and out via MQTT and can be watched with the above command.
Once you have setup and test the hub you can move onto using the binding.

## Supported Things

This binding is best thought of as a remote control emulator, so the things are really the type of remote that you own and not the globes.
The Milight protocol is 1 way only so there is no way to find actual globes.

| Thing Type ID | Description |
|-|-|
| `rgb_cct` | Remote that has 4 channels and controls globes with full colour, and both cool and warm whites. |
| `fut089` | Remote is the newer 8 channel type called FUT089 and your globes are the rgb_cct. |
| `cct` | Remote is 4 channels and the globes have no colours with only cool and warm white controls. |
| `fut091` | Remote is the newer 8 group model called a fut091 and your globes are cct. |
| `rgbw` | Remote is 4 channels and the globes have RGB and a fixed white. |
| `rgb` | Remote is 4 channels and the globes have full RGB with no white. |

## Discovery

First install the MQTT binding and setup a `broker` thing and make sure it is ONLINE, as this binding uses the MQTT binding to talk to your broker and hence that binding must be setup first.
Next, move a control on either a physical remote, or used a virtual control inside the esp8266 control panel web page which cause a MQTT message to be sent.
This binding should then detect the new device the moment the control is moved and a new entry should appear in your INBOX.

To remove a saved state from your MQTT broker that causes an entry in your INBOX you can use this command or use the ignore feature of openHAB.

```shell
mosquitto_pub -u username -P password -p 1883 -t 'milight/states/0x0/rgb_cct/1' -n -r
```

Note that the group 0 (or ALL group) is not auto discovered as a thing and thus has to be added manually if needed.

## Thing Configuration

| Parameter | Description | Required | Default |
|-|-|-|-|
| `whiteHue` | When both the `whiteHue` and `whiteSat` values are seen by the binding it will trigger the white LEDS. Set to -1 to disable, 0 for Alexa, or 35 for Google Home. | Y | 35 |
| `whiteSat` | When both the whiteHue and whiteSat values are seen by the binding it will trigger the white LEDS. Set to -1 to disable, 100 for Alexa or 32 for Google Home. | Y | 32 |
| `favouriteWhite` | When one of the shortcuts triggers white mode, use this for the colour white instead of the default colour. | Y |200 |
| `dimmedCT` | Traditional globes grow warmer the more they are dimmed. Set this to 370, or leave blank to disable. | N | blank |
| `oneTriggersNightMode` | Night mode is a much lower level of light and this feature allows it to be auto selected when your fader/slider moves to 1%. NOTE: Night mode by design locks out some controls of a physical remote, so this feature is disabled by default. | Y | false |
| `powerFailsToMinimum` | If lights loose power from the power switch OR a power outage, they will default to using the lowest brightness if the light was turned off before the power failure occurred. | Y | true |
| `whiteThreshold` | This feature allows you to use a color control to change to using the real white LEDs when the saturation is equal to, or below this threshold. -1 will disable this feature. | Y | 12 |
| `duvThreshold` | This feature allows you to use a color control to change to using the real warm/cool white LEDs to set a white color temperature if the color is within a certain threshold of the block body curve. 1 will effectively disable this feature. The default settings maps well to Apple's HomeKit that will allow you to choose a color temperature, but sends it as an HSB value. See <https://www.waveformlighting.com/tech/calculate-duv-from-cie-1931-xy-coordinates/> for more information. | Y | 0.003 |

## Channels

| Channel | Type | Description |
|-|-|-|
| `level` | Dimmer | Level changes the brightness of the globe. Not present if the bulb supports the `colour` channel. |
| `colourTemperature` | Dimmer | Change from cool to warm white with this control. |
| `colourTemperatureAbs` | Number:Temperature | Colour temperature in mireds. |
| `colour` | Color | Allows you to change the colour, brightness and saturation of the globe. Can also be linked directly with a Dimmer item if you happen to have a bulb that doesn't support colour, but is controlled by a remote that normally does support colour. |
| `discoMode` | String | Switch to a Disco mode directly from a drop down list. |
| `bulbMode` | String (read only) | Displays the mode the bulb is currently in so that rules can determine if the globe is white, a color, disco modes or night mode are selected. |
| `command` | String | Sends the raw commands that the buttons on a remote send. |

## Note Regarding Transmission Delays

If you have lots of globes and openHAB turns them all on, you may notice a delay that causes the globes to turn on one by one and the delay can add up when a lot of globes are installed in your house.
This is caused by the time it takes to transmit the desired setting to the globe multiplied by how many times the hub repeats transmitting the setting.
Since it takes around 2.8ms for a setting to be transmitted, if the firmware is set to repeat the packets 50 times it would then take 2.8*50 = 140ms before the next globe starts to have its new state transmitted by the hub.
You can reduce the packet repeats to speed up the response of this binding and the hub by tweaking a few settings.

Settings can be found on the radio tab in the esp control panel using your browser.
Suggested settings are as follows:

- Packet repeats = 12 (if you only turn 1 globe on or off it uses this value)
- Packet repeat throttle threshold = 200
- Packet repeat throttle sensitivity = 0
- Packet repeat minimum = 8 (When turning multiple globes on and off it will use this value as it throttles the repeats back to reduce latency/delay between each globe)

## Important for Textual Configuration

This binding requires things to have a specific format for the unique ID, the auto discovery does this for you.

If doing textual configuration, you need to add the Device ID and Group ID together to create the things unique ID.
The DeviceID is different for each remote.
The GroupID can be 0 (all channels on the remote), or 1 to 8 for each of the individual channels on the remote).
If you do not understand this, please use auto discovery to do it for you.

The formula is
DeviceID + GroupID = ThingUID

For example:

| Device ID | Group ID |ThingUID  |
|-----------|----------|----------|
| 0xE6C     | 4        | 0xE6C4   |
| 0xB4CA    | 4        | 0xB4CA4  |
| 0xB4CA    | 8        | 0xB4CA8  |
| 0xB4CA    | 0        | 0xB4CA0  |

## Using Group 0

The group 0 (or ALL group) with the Group ID 0 can be used to control all bulbs that are paired with one specific remote at once.
While this functionality can also be achieved by using openHAB groups with even greater flexibility, the group 0 must be setup if you want to capture physical remote control events for the ALL group, and keep physical devices synchronized to their openHAB representations.
Milight remotes send all commands with the Group ID 0 after the master ON/OFF buttons have been used.
If the group 0 has not been setup these events will be lost and your Item states will no longer be synchonized with the actual device states until you issue a command via openHAB.
If you do not use a remote at all or you only control other bulbs than the ones controlled by openHAB you should not need to setup the ALL group.

Since the group 0 is not needed in every case the autodiscovery feature will not detect this group as a Thing automatically.
To create the group, use textual files or the openHAB UI to manually add a Thing with the correct Unique ID as described in section [Important for Textual Configuration](#important-for-textual-configuration).
To create a Thing for the group 0, simply create a new Thing that has the same type as one of the auto discovered Things of the same remote and modify the ThingUID as described in section linked above.

If you do not need separate group 0 controls in openHAB, but wish to have all the controls for the sub groups update when a physical remote is used, you only need to create the thing for group 0.
Only if you want the controls do you need to link any channels and create the items, as creating the thing will subscribe the binding to the MQTT topic for group 0.

## Full Example

To use these examples for textual configuration, you must already have a configured MQTT `broker` thing, and know its unique ID.
This UID will be used in the things file and will replace the text `myBroker`.
The first line in the things file will create a `broker` thing and this can be removed if you have already setup a broker in another file or via the UI already.

*.things

```java
Bridge mqtt:broker:myBroker [ host="localhost", secure=false, password="*******", qos=1, username="user"]
Thing mqtt:rgb_cct:myBroker:0xE6C4 "Hallway" (mqtt:broker:myBroker) @ "MQTT"
```

*.items

```java
Dimmer Hallway_Level "Front Hall" {channel="mqtt:rgb_cct:myBroker:0xE6C4:colour"}
Dimmer Hallway_ColourTemperature "White Color Temp" {channel="mqtt:rgb_cct:myBroker:0xE6C4:colourTemperature"}
Number:Temperature Hallway_ColourTemperatureK "White Color Temp [%d %unit%]" {channel="mqtt:rgb_cct:myBroker:0xE6C4:colourTemperatureAbs", unit="K"}
Color  Hallway_Colour "Front Hall" ["Lighting"] {channel="mqtt:rgb_cct:myBroker:0xE6C4:colour"}
String Hallway_DiscoMode "Disco Mode" {channel="mqtt:rgb_cct:myBroker:0xE6C4:discoMode"}
String Hallway_BulbCommand "Send Command" {channel="mqtt:rgb_cct:myBroker:0xE6C4:command"}
String Hallway_BulbMode "Bulb Mode" {channel="mqtt:rgb_cct:myBroker:0xE6C4:bulbMode"}

```

*.sitemap

```perl
Text label="Hallway" icon="light"
{
    Switch      item=Hallway_Level
    Slider      item=Hallway_Level
    Slider      item=Hallway_ColourTemperature
    Colorpicker item=Hallway_Colour
    Selection   item=Hallway_DiscoMode
    Text        item=Hallway_BulbMode
    Switch item=Hallway_BulbCommand mappings=[next_mode='Mode +', previous_mode='Mode -', mode_speed_up='Speed +', mode_speed_down='Speed -', set_white='White', night_mode='Night' ]
}
```
