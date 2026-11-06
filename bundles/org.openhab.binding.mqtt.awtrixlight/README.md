# MQTT Awtrix 3 Binding

This binding allows you to control Awtrix 3 (formerly Awtrix Light) LED matrix displays via MQTT.
Awtrix 3 is a firmware for a 32x8 LED matrix display that can show various information like time, weather, notifications and custom text/graphics.
The most popular choice for a device that supports the Awtrix 3 firmware is the Ulanzi tc0001 clock.

The binding also supports clocks that run AWTRIX NG, see [AWTRIX NG](#awtrix-ng).

## Supported Things

This binding supports two types of things:

| Thing Type              | Description                                                                                     |
|-------------------------|-------------------------------------------------------------------------------------------------|
| `awtrix-clock` (Bridge) | Represents an Awtrix 3 display device. Acts as a bridge for apps.                               |
| `awtrix-app`            | Represents an app running on the Awtrix display. Apps can show text, icons, notifications, etc. |

The binding was tested with the Ulanzi tc0001 clock.

## Discovery

The binding can automatically discover Awtrix 3 devices that publish their status to the configured MQTT broker.
Make sure to use a mqtt prefix that starts with `awtrix` for discovery to work.
It is however recommended to use a prefix with two topic levels, for example `awtrix/clock1` so that you can discover and control multiple devices.
Once a device is discovered, it will appear in the inbox.
There is no need to trigger a discovery scan manually.
Default Awtrix apps can also be discovered if `discoverDefaultApps` is enabled on the bridge.
This is however not recommended as the default apps cannot be controlled with this binding.

## Binding Configuration

The Awtrix 3 binding does not offer any binding configuration parameters.

## Thing Configuration

### Bridge Configuration (`awtrix-clock`)

| Parameter             | Description                                                                                                                                | Default  | Required |
|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------|----------|----------|
| `basetopic`           | The MQTT base topic for the Awtrix device                                                                                                  | "awtrix" | Yes      |
| `appLockTimeout`      | Timeout in seconds before releasing the lock to a selected app and returning to normal app cycle (see App Configuration for more details). | 10       | Yes      |
| `discoverDefaultApps` | Enable discovery of default apps. Since default apps cannot be controlled by this binding this should usually be disabled.                 | false    | Yes      |
| `lowBatteryThreshold` | Battery level threshold for low battery warning.                                                                                           | 25       | Yes      |

### App Configuration (`awtrix-app`)

| Parameter    | Description                        | Default | Required |
|--------------|------------------------------------|---------|----------|
| `appname`    | Name of the app                    | -       | Yes      |
| `useButtons` | Enable button control for this app | false   | No       |

When you enable the button control for an app, you can lock the app to the display by pushing the select button on the clock device.
A red indicator will be shown while the app is locked and will start to blink shortly before the lock ends.
The lock will last for the appLockTimeout set for the bridge.
As long as the app is locked the normal app cycle is disabled and you can control the app by pressing the left and right buttons or the select button on the clock device.
Pressing a button while the app is locked will reset the lock timeout to the value set for appLockTimeout. Left and right button presses will emit button events on the clock itself and the selected app.
The button events can be used by rules to change the displayed app or perform any other actions (for example change the text color of the app or skip the current song playing on your audio device).

## Channels

### Bridge Channels (`awtrix-clock`)

| Channel           | Type                 | Read/Write | Description                                                                                                                                                                                                                                                                           |
|-------------------|----------------------|------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `app`             | String               | R          | Currently active app: Will show the name of the app that is currently shown on the display.                                                                                                                                                                                           |
| `auto-brightness` | Switch               | RW         | Automatic brightness control: The clock will adjust the display brightness automatically based on ambient light.                                                                                                                                                                      |
| `battery-level`   | Number               | R          | Battery level: The battery level of the internal battery in percent.                                                                                                                                                                                                                  |
| `brightness`      | Dimmer               | RW         | Display brightness: The brightness of the display in percent.                                                                                                                                                                                                                         |
| `button-left`     | Trigger              | R          | Left button press event: Triggered when the left button is pressed/released (Event PRESSED or RELEASED).                                                                                                                                                                              |
| `button-right`    | Trigger              | R          | Right button press event: Triggered when the right button is pressed/released (Event PRESSED or RELEASED).                                                                                                                                                                            |
| `button-select`   | Trigger              | R          | Select button press event: Triggered when the select button is pressed/released (Event PRESSED or RELEASED).                                                                                                                                                                          |
| `display`         | Switch               | RW         | Display on/off: Switches the display on or off. The clock will still stay on while the display is off.                                                                                                                                                                                |
| `humidity`        | Number:Dimensionless | R          | Relative humidity: Relative humidity in percent. For the Ulanzi clock values are usually very inaccurate.                                                                                                                                                                             |
| `indicator-1`     | Switch               | RW         | Control first indicator LED: Switches the first indicator LED on or off. The color of the LED will be green but can be customised by using Thing actions (you can also use blinking/fading effects).                                                                                  |
| `indicator-2`     | Switch               | RW         | Control second indicator LED: Switches the second indicator LED on or off.The color of the LED will be green but can be customised by using Thing actions (you can also use blinking/fading effects).                                                                                 |
| `indicator-3`     | Switch               | RW         | Control third indicator LED: Switches the third indicator LED on or off. The color of the LED will be green but can be customised by using Thing actions (you can also use blinking/fading effects).                                                                                  |
| `low-battery`     | Switch               | R          | Low battery warning: Will be switched ON as soon as the battery level drops below the lowBatteryThreshold set for the bridge.                                                                                                                                                         |
| `lux`             | Number:Illuminance   | R          | Ambient light level: Ambient light level in lux as measured by the built-in light sensor.                                                                                                                                                                                             |
| `rssi`            | Number:Dimensionless | R          | WiFi signal strength (RSSI): WiFi signal strength (RSSI) in dBm.                                                                                                                                                                                                                      |
| `rtttl`           | String               | W          | Play RTTTL ringtone: Play a ringtone specified in RTTTL format (see <https://de.wikipedia.org/wiki/Ring_Tones_Text_Transfer_Language>)                                                                                                                                                  |
| `screen`          | String               | R          | Screen image: Allows you to mirror the screen image from the clock. The screen image will be updated automatically when the app changes but can be updated manually by sending a RefreshType command to the channel.                                                                  |
| `sound`           | String               | W          | Play sound file: The sound file must be available on the clock device in the MELODIES folder. Save a file with a valid RTTTL string (e.g. melody.txt) in this folder and play it by sending a String command to the channel with the filename without file extension (e.g. "melody"). |
| `temperature`     | Number:Temperature   | R          | Device temperature: Temperature in °C as measured by the built-in temperature sensor. For the Ulanzi clock values are usually very inaccurate.                                                                                                                                        |

### App Channels (`awtrix-app`)

| Channel               | Type                 | Read/Write | Description                                                                                                                                                                                                                        |
|-----------------------|----------------------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `active`\*            | Switch               | RW         | Enable/disable the app: Switches the app on or off. Note that channels of inactive apps will be reset to their default values during a restart of openHAB.                                                                         |
| `autoscale`           | Switch               | RW         | Enable/disable autoscaling for bar and line chart.                                                                                                                                                                                |
| `background`          | Color                | RW         | Sets a background color.                                                                                                                                                                                                           |
| `bar`                 | String               | RW         | Shows a bar chart: Send a string with values separated by commas (e.g. "value1,value2,value3"). Only the last 16 values will be displayed.                                                                                         |
| `blink`               | Number:Time          | RW         | Blink text: Blink the text in the specified interval. Ignored if rainbow or gradient-color is set.                                                                                                                                |
| `center`              | Switch               | RW         | Center short text horizontally and disable scrolling.                                                                                                                                                                              |
| `color`               | Color                | RW         | Text, bar or line chart color.                                                                                                                                                                                                     |
| `duration`            | Number:Time          | RW         | Display duration in seconds.                                                                                                                                                                                                       |
| `effect`              | String               | RW         | Display effect (see <https://blueforcer.github.io/awtrix3/#/effects> for possible values).                                                                                                                                           |
| `effect-blend`        | Switch               | RW         | Enable smoother effect transitions. Only to be used with effect.                                                                                                                                                                   |
| `effect-palette`      | String               | RW         | Color palette for effects (see <https://blueforcer.github.io/awtrix3/#/effects> for possible values and how to create custom palettes). Only to be used with effect.                                                                 |
| `effect-speed`        | Number:Dimensionless | RW         | Effect animation speed: Higher means faster (see <https://blueforcer.github.io/awtrix3/#/effects>). Only to be used with effect.                                                                                                     |
| `fade`                | Number:Time          | RW         | Fade text: Fades the text in and out in the specified interval. Ignored if rainbow, gradient-color or blink is set.                                                                                                                       |
| `gradient-color`      | Color                | RW         | Secondary color for gradient effects. Use color for setting the primary color.                                                                                                                                                     |
| `icon`                | String               | RW         | Icon name to display: Install icons on the clock device first.                                                                                                                                                                     |
| `lifetime`\*          | Number:Time          | RW         | App lifetime: Define how long the app will remain active on the clock.                                                                                                                                                             |
| `lifetime-mode`\*     | String               | RW         | Lifetime mode: Define if the app should be deleted (Command DELETE) or marked as stale (Command STALE) after lifetime.                                                                                                             |
| `line`                | String               | RW         | Shows a line chart: Send a string with values separated by commas (e.g. "value1,value2,value3"). Only the last 16 values will be displayed.                                                                                        |
| `overlay`             | String               | RW         | Enable overlay mode: Shows a weather overlay effect (can be any of clear, snow, rain, drizzle, storm, thunder, frost).                                                                                                             |
| `progress`            | Number:Dimensionless | RW         | Progress value: Shows a progress bar at the bottom of the app with the specified percentage value.                                                                                                                                 |
| `progress-background` | Color                | RW         | Progress bar background color: Background color for the progress bar.                                                                                                                                                              |
| `progress-color`      | Color                | RW         | Progress bar color: Color for the progress bar.                                                                                                                                                                                    |
| `push-icon`           | String               | RW         | Push icon animation (STATIC=Icon doesn't move, PUSHOUT=Icon moves with text and will not appear again, PUSHOUTRETURN=Icon moves with text but appears again when the text starts to scroll again).                                 |
| `rainbow`             | Switch               | RW         | Enable rainbow effect: Uses a rainbow effect for the displayed text. Overrides color, gradient-color, blink and fade.                                                                                                                            |
| `reset`*              | Switch               | RW         | Reset app to default state: All channels will be reset to their default values.                                                                                                                                                    |
| `scroll-speed`        | Number:Dimensionless | RW         | Text scrolling speed: Provide as percentage value. The original speed is 100%. Values above 100% will increase the scrolling speed, values below 100% will decrease it. Setting this value to 0 will disable scrolling completely. |
| `text`                | String               | RW         | Text to display. Supports inline color formatting with font color tags (see Text Color Tags section below for details).                                                                                                            |
| `text-case`           | Number:Dimensionless | RW         | Set text case (0=default, 1=uppercase, 2=unchanged).                                                                                                                                                                              |
| `text-offset`         | Number:Dimensionless | RW         | Text offset position: Horizontal offset of the text in pixels.                                                                                                                                                                     |
| `top-text`            | Switch               | RW         | Draws the text on the top of the display.                                                                                                                                                                                          |

\* Cannot be used with notification Actions (see section Actions)

## Text Color Tags

The `text` channel supports inline color formatting using simple HTML-like font color tags. This allows you to display text with multiple colors in a single app.

### Syntax

Use the following format to apply colors to specific parts of your text:

```html
<font color="#RRGGBB">colored text</font>
```

Where `RRGGBB` is a 6-digit hexadecimal color code (e.g., `FF0000` for red, `00FF00` for green, `0000FF` for blue).

### Examples

```java
// Multiple colored segments - "Hello" and "in" will use the color from the color channel, "World" and "Color" will use the specified colors
items.Custom_Text.sendCommand("Hello <font color=\"#FF0000\">World</font> in <font color=\"#00FF00\">Color</font>")

// All text in custom color
items.Custom_Text.sendCommand("<font color=\"#FF6600\">Temperature: 25°C</font>")
```

### Important Notes

- **Default color**: Text outside of `<font>` tags will be displayed in the color defined by the `color` channel.
- **Text effects**: When color tags are used, the `gradient-color` and `rainbow` channel are ignored. Therefore `blink` and `fade` will also work in conjunction with text color tags even when `rainbow` and/or `gradient-color` were set.
- **Tags cannot be nested**: `<font>` tags must not be placed inside other `<font>` tags. Nesting is not supported and will result in incorrect parsing.
- **Case-insensitive hex values**: Both uppercase and lowercase hex values are supported (e.g., `#FF0000` or `#ff0000`).
- **Malformed tags**: If a tag is malformed (e.g., missing closing tag), the parser will gracefully handle it by applying the default color.

## Actions

The binding supports various actions that can be used in rules to control the Awtrix display. To use these actions, you need to import them in your rules (see examples below).

The following actions are supported:

<table>
    <thead>
        <tr>
            <th>Action Name</th>
            <th>Description</th>
            <th>Parameter Type</th>
            <th>Parameter Name</th>
            <th>Parameter Description</th>
        </tr>
    </thead>
  <tr valign='top'>
    <td rowspan="3">blinkIndicator</td>
    <td rowspan="3">Blink an indicator LED.</td>
    <td>int</td>
    <td>indicatorId</td>
    <td>1 for top indicator, 2 for center indicator or 3 for bottom indicator.</td>
  </tr>
  <tr>
    <td>int[]</td>
    <td>rgb</td>
    <td>Provide an array with exactly 3 values for red, green and blue (in the range 0-255).</td>
  </tr>
  <tr>
    <td>int</td>
    <td>blinkTimeInMs</td>
    <td>Provide the blink time in milliseconds.</td>
  </tr>
   <tr valign='top'>
    <td rowspan="3">fadeIndicator</td>
    <td rowspan="3">Fade an indicator LED in and out.</td>
    <td>int</td>
    <td>indicatorId</td>
    <td>1 for top indicator, 2 for center indicator or 3 for bottom indicator.</td>
  </tr>
  <tr>
    <td>int[]</td>
    <td>rgb</td>
    <td>Provide an array with exactly 3 values for red, green and blue (in the range 0-255).</td>
  </tr>
  <tr>
    <td>int</td>
    <td>fadeTimeInMs</td>
    <td>Provide the fade time in milliseconds.</td>
  </tr>
  <tr valign='top'>
    <td rowspan="2">activateIndicator</td>
    <td rowspan="2">Activate an indicator LED.</td>
    <td>int</td>
    <td>indicatorId</td>
    <td>1 for top indicator, 2 for center indicator or 3 for bottom indicator.</td>
  </tr>
  <tr>
    <td>int[]</td>
    <td>rgb</td>
    <td>Provide an array with exactly 3 values for red, green and blue (in the range 0-255).</td>
  </tr>
  <tr valign='top'>
    <td>deactivateIndicator</td>
    <td>Deactivate an indicator LED.</td>
    <td>int</td>
    <td>indicatorId</td>
    <td>1 for top indicator, 2 for center indicator or 3 for bottom indicator.</td>
  </tr>
  <tr valign='top'>
    <td>reboot</td>
    <td>Reboot the device</td>
    <td></td>
    <td></td>
    <td></td>
  </tr>
  <tr valign='top'>
    <td>sleep</td>
    <td>Make the device sleep for a number of seconds.</td>
    <td>int</td>
    <td>seconds</td>
    <td>Device will wake up after the specified number of seconds. Sleep can only be interrupted by a press of the select button.</td>
  </tr>
    <tr valign='top'>
    <td>upgrade</td>
    <td>Upgrade the device if a firmware update is available.</td>
    <td></td>
    <td></td>
    <td></td>
  </tr>
  <tr valign='top'>
    <td>playSound</td>
    <td>Play a sound file saved on the device.</td>
    <td>String</td>
    <td>melody</td>
    <td >The sound file name saved in the clocks MELODIES folder (without the file extension).</td>
  </tr>
 <tr valign='top'>
    <td>playRtttl</td>
    <td>Play a rtttl sound.</td>
    <td>String</td>
    <td>rtttl</td>
    <td >The rtttl string to play.</td>
  </tr>
  <tr valign='top'>
    <td rowspan="2">showNotification</td>
    <td rowspan="2">Show a notification.</td>
    <td>String</td>
    <td>message</td>
    <td>The message to show.</td>
  </tr>
  <tr>
    <td>String</td>
    <td>icon</td>
    <td>The name of the icon saved on the device that is shown with the message.</td>
  </tr>
  <tr valign='top'>
    <td rowspan="8">showCustomNotification</td>
    <td rowspan="8">Show a notification with maximal customization options.</td>
    <td>Map&lt;String, Object&gt;</td>
    <td>appParams</td>
    <td>Map that holds any parameter that is available for an Awtrix App as shown in the App Channels section of the documentation. Use the channel ids in the App Channels section as keys.</td>
  </tr>
  <tr>
    <td>boolean</td>
    <td>hold</td>
    <td>Whether the notification should stay on the screen until the user presses the select button.</td>
  </tr>
  <tr>
    <td>boolean</td>
    <td>wakeUp</td>
    <td>Whether the notification should wake up the device if the display is currently switched off.</td>
  </tr>
  <tr>
    <td>boolean</td>
    <td>stack</td>
    <td>Whether the notification should be stacked on top of the previous notification or replace the currently active notification.</td>
  </tr>
  <tr>
    <td>String</td>
    <td>rtttl</td>
    <td>Play the specified rtttl sound when displaying the notification.</td>
  </tr>
  <tr>
    <td>String</td>
    <td>rtttl</td>
    <td>Play the specified rtttl ringtone when displaying the notification. Set to null for no sound or when the sound parameter is set.</td>
  </tr>
  <tr>
    <td>String</td>
    <td>sound</td>
    <td>Play the specified sound file when displaying the notification. Set to null for no sound or when the rtttl parameter is set.</td>
  </tr>
  <tr>
    <td>boolean</td>
    <td>loopSound</td>
    <td>Whether the sound should be played in a loop until the notification is dismissed.</td>
  </tr>
  <tr valign='top'>
    <td>dismissNotification</td>
    <td>Dismisses the currently shown notification.</td>
    <td></td>
    <td></td>
    <td></td>
  </tr>
</table>

## Full Example

### Things

```java
Bridge mqtt:broker:myBroker [ host="localhost", port=1883 ]
{
    Bridge mqtt:awtrix-clock:myBroker:myAwtrix "Living Room Display" (mqtt:broker:myBroker) [ basetopic="awtrix", appLockTimeout=10, lowBatteryThreshold=25 ] {
        Thing awtrix-app washing "My Washing Machine App" [ appname="washing", useButtons=true ]
        Thing awtrix-app doorbell "My Doorbell App" [ appname="doorbell" ]
        Thing awtrix-app mediaplayer "My MediaPlayer App" [ appname="mediaplayer" ]
        Thing awtrix-app other "My Other App" [ appname="other" ]
    }
}
```

### Items

```java
// Bridge items (Living Room Display)
Group gAwtrix "Living Room Awtrix Display" <screen>
Dimmer Display_Brightness "Brightness [%d %%]" (gAwtrix) { channel="mqtt:awtrix-clock:myBroker:myAwtrix:brightness" }
Switch Display_Screen "Screen" (gAwtrix) { channel="mqtt:awtrix-clock:myBroker:myAwtrix:display" }
String Display_SoundFile "Sound File" (gAwtrix) { channel="mqtt:awtrix-clock:myBroker:myAwtrix:sound" }
Switch Display_AutoBrightness "Auto Brightness" (gAwtrix) { channel="mqtt:awtrix-clock:myBroker:myAwtrix:auto-brightness" }
Number:Temperature Display_Temperature "Temperature [%.1f °C]" (gAwtrix) { channel="mqtt:awtrix-clock:myBroker:myAwtrix:temperature" }
Number:Dimensionless Display_Humidity "Humidity [%d %%]" (gAwtrix) { channel="mqtt:awtrix-clock:myBroker:myAwtrix:humidity" }
Number Display_Battery "Battery Level [%d %%]" (gAwtrix) { channel="mqtt:awtrix-clock:myBroker:myAwtrix:battery-level" }
Switch Display_LowBattery "Low Battery" (gAwtrix) { channel="mqtt:awtrix-clock:myBroker:myAwtrix:low-battery" }
Number:Power Display_WiFi "WiFi Signal [%d dBm]" (gAwtrix) { channel="mqtt:awtrix-clock:myBroker:myAwtrix:rssi", unit="dBm" }
String Display_CurrentApp "Active App [%s]" (gAwtrix) { channel="mqtt:awtrix-clock:myBroker:myAwtrix:app" }

// Washing Machine App items
Group gAwtrixWashing "Washing Machine App"
Switch Washing_Active "Washing Active" (gAwtrixWashing) { channel="mqtt:awtrix-app:myBroker:myAwtrix:washing:active" }
String Washing_Text "Washing Text" (gAwtrixWashing) { channel="mqtt:awtrix-app:myBroker:myAwtrix:washing:text" }
Color Washing_Color "Washing Color" (gAwtrixWashing) { channel="mqtt:awtrix-app:myBroker:myAwtrix:washing:color" }
Number Washing_Duration "Washing Duration" (gAwtrixWashing) { channel="mqtt:awtrix-app:myBroker:myAwtrix:washing:duration" }

// Doorbell App items
Group gAwtrixDoorbell "Doorbell App"
Switch Doorbell_Active "Doorbell Active" (gAwtrixDoorbell) { channel="mqtt:awtrix-app:myBroker:myAwtrix:doorbell:active" }
String Doorbell_Text "Doorbell Text" (gAwtrixDoorbell) { channel="mqtt:awtrix-app:myBroker:myAwtrix:doorbell:text" }
String Doorbell_Icon "Doorbell Icon" (gAwtrixDoorbell) { channel="mqtt:awtrix-app:myBroker:myAwtrix:doorbell:icon" }
Color Doorbell_Color "Doorbell Color" (gAwtrixDoorbell) { channel="mqtt:awtrix-app:myBroker:myAwtrix:doorbell:color" }
Switch Doorbell_Top_Text "Doorbell Top Text" (gAwtrixDoorbell) { channel="mqtt:awtrix-app:myBroker:myAwtrix:doorbell:top-text" }

// Media Player App items with advanced features
Group gAwtrixMediaPlayer "Media Player App"
Switch MediaPlayer_Active "Media Player Active" (gAwtrixMediaPlayer) { channel="mqtt:awtrix-app:myBroker:myAwtrix:mediaplayer:active" }
String MediaPlayer_Text "Media Player Text" (gAwtrixMediaPlayer) { channel="mqtt:awtrix-app:myBroker:myAwtrix:mediaplayer:text" }
String MediaPlayer_Icon "Media Player Icon" (gAwtrixMediaPlayer) { channel="mqtt:awtrix-app:myBroker:myAwtrix:mediaplayer:icon" }
Color MediaPlayer_Color "Media Player Color" (gAwtrixMediaPlayer) { channel="mqtt:awtrix-app:myBroker:myAwtrix:mediaplayer:color" }
Color MediaPlayer_Background "Background Color" (gAwtrixMediaPlayer) { channel="mqtt:awtrix-app:myBroker:myAwtrix:mediaplayer:background" }
Number:Dimensionless MediaPlayer_ScrollSpeed "Scroll Speed" (gAwtrixMediaPlayer) { channel="mqtt:awtrix-app:myBroker:myAwtrix:mediaplayer:scroll-speed" }
Switch MediaPlayer_Center "Center Text" (gAwtrixMediaPlayer) { channel="mqtt:awtrix-app:myBroker:myAwtrix:mediaplayer:center" }
Number:Dimensionless MediaPlayer_Progress "Progress [%d %%]" (gAwtrixMediaPlayer) { channel="mqtt:awtrix-app:myBroker:myAwtrix:mediaplayer:progress" }
Color MediaPlayer_ProgressColor "Progress Color" (gAwtrixMediaPlayer) { channel="mqtt:awtrix-app:myBroker:myAwtrix:mediaplayer:progress-color" }
```

### Sitemap

```perl

sitemap awtrix label="Awtrix Display" {
    Frame label="Display Control" {
        Switch item=Display_Power
        Slider item=Display_Brightness
        Switch item=Display_Screen
        Switch item=Display_Sound
        Switch item=Display_AutoBrightness
        Text item=Display_Temperature
        Text item=Display_Humidity
        Text item=Display_Battery visibility=[Display_LowBattery==ON]
        Text item=Display_WiFi
        Text item=Display_CurrentApp
    }
    
    Frame label="Washing Machine App" {
        Switch item=Washing_Active
        Text item=Washing_Text
        Colorpicker item=Washing_Color
        Slider item=Washing_Duration
    }
    
    Frame label="Doorbell App" {
        Switch item=Doorbell_Active
        Text item=Doorbell_Text
        Text item=Doorbell_Icon
        Colorpicker item=Doorbell_Color
        Switch item=Doorbell_Top_Text
    }
    
    Frame label="Media Player App" {
        Switch item=MediaPlayer_Active
        Text item=MediaPlayer_Text
        Text item=MediaPlayer_Icon
        Colorpicker item=MediaPlayer_Color
        Colorpicker item=MediaPlayer_Background
        Slider item=MediaPlayer_ScrollSpeed
        Switch item=MediaPlayer_Center
        Slider item=MediaPlayer_Progress
        Colorpicker item=MediaPlayer_ProgressColor
    }
}

```

### Actions

The binding provides various actions that can be used in rules to control the Awtrix display. To use these actions, you need to import them in your rules.

Rules DSL:

```java
val awtrixActions = getActions("mqtt.awtrixlight", "mqtt:awtrix-clock:myBroker:myAwtrix")
```

JS Scripting:

```java
var awtrixActions = actions.thingActions("mqtt.awtrixlight", "mqtt:awtrix-clock:myBroker:myAwtrix");
```

### Indicator Control

Control the three indicator LEDs on the Awtrix display (JS Scripting):

```java
// Blink indicator 1 in red for 1 second
awtrixActions.blinkIndicator(1, [255,0,0], 1000)

// Fade indicator 2 to blue over 2 seconds
awtrixActions.fadeIndicator(2, [0,0,255], 2000)

// Turn on indicator 3 in green
awtrixActions.activateIndicator(3, [0,255,0])

// Turn off indicator 1
awtrixActions.deactivateIndicator(1)
```

### Device Control

Control basic device functions:

```java
// Reboot the device
awtrixActions.reboot()

// Put device to sleep for 60 seconds
awtrixActions.sleep(60)

// Perform firmware upgrade
awtrixActions.upgrade()
```

### Sound Control

Play sounds and melodies:

```java
// Play a predefined sound file (without extension)
awtrixActions.playSound("notification")

// Play an RTTTL melody
awtrixActions.playRtttl("Indiana:d=4,o=5,b=250:e,8p,8f,8g,8p,1c6,8p.,d,8p,8e,1f,p.,g,8p,8a,8b,8p,1f6,p,a,8p,8b,2c6,2d6,2e6,e,8p,8f,8g,8p,1c6,p,d6,8p,8e6,1f.6,g,8p,8g,e.6,8p,d6,8p,8g,e.6,8p,d6,8p,8g,f.6,8p,e6,8p,8d6,2c6")
```

### Notifications

Display notifications on the screen:

```java
// Show simple notification with icon
awtrixActions.showNotification("Hello World", "alert")

// Show custom notification with advanced options
val params = newHashMap(
    'text' -> 'Custom Message',
    'icon' -> 'warning',
    'color' -> newArrayList(255,165,0),  // Orange color
    'rainbow' -> true,
    'duration' -> 10
)
awtrixActions.showCustomNotification(
    params,     // Notification parameters
    false,      // hold: Keep notification until manually cleared
    true,       // wakeUp: Wake up from screen saver
    true,       // stack: Add to notification stack
    "Indiana:d=4,o=5,b=250:e,8p,8f,8g,8p,1c6,8p.,d,8p,8e,1f,p.,g,8p,8a,8b,8p,1f6,p,a,8p,8b,2c6,2d6,2e6,e,8p,8f,8g,8p,1c6,p,d6,8p,8e6,1f.6,g,8p,8g,e.6,8p,d6,8p,8g,e.6,8p,d6,8p,8g,f.6,8p,e6,8p,8d6,2c6", // RTTTL sound to play (not both sound and rtttl)
    "alert",    // Sound file to play (not both sound and rtttl)
    false       // loopSound: Loop the sound until manually stopped
)
```

#### Custom Notification Parameters

The action method parameters:

| Parameter   | Type    | Description                              |
|-------------|---------|------------------------------------------|
| `hold`      | Boolean | Keep notification until manually cleared |
| `wakeUp`    | Boolean | Wake up from screen saver                |
| `stack`     | Boolean | Add to notification stack                |
| `rtttl`     | String  | RTTTL melody to play                     |
| `sound`     | String  | Sound file to play (without extension)   |
| `loopSound` | Boolean | Loop the sound                           |
| `params`    | Map     | Notification parameters                  |

The `showCustomNotification` action accepts all app channels as shown above as parameters in the params map.

### Rule Examples

Here are some example rules demonstrating various features:

```java

rule "Battery Status Indicator Demo"
when
    Item Display_Battery changed
then
    if (Display_Battery.state <= 20) {
        // Show low battery warning with blinking red indicator
        getActions("mqtt.awtrixlight", "mqtt:awtrix-clock:myBroker:myAwtrix").blinkIndicator(1, [255,0,0], 500)
        getActions("mqtt.awtrixlight", "mqtt:awtrix-clock:myBroker:myAwtrix").showNotification("Low Battery!", "battery-alert")
    } else if (Display_Battery.state <= 50) {
        // Show yellow indicator for medium battery
        getActions("mqtt.awtrixlight", "mqtt:awtrix-clock:myBroker:myAwtrix").setIndicator(1, [255,255,0])
    } else {
        // Show green indicator for good battery
        getActions("mqtt.awtrixlight", "mqtt:awtrix-clock:myBroker:myAwtrix").setIndicator(1, [0,255,0])
    }
end

rule "Door Bell Demo"
when
    Item Doorbell_Button changed to ON
then
    // Play sound and show notification
    getActions("mqtt.awtrixlight", "mqtt:awtrix-clock:myBroker:myAwtrix").playRtttl("doorbell:d=4,o=6,b=100:8e,8g,8e,8c")
    
    var params = newHashMap(
        'text' -> "Doorbell",
        'icon' -> "bell-ring",
        'color' -> newArrayList(0,255,255),  // Cyan color
        'pushIcon' -> "PUSHOUT",
        'center' -> true
    )
    getActions("mqtt.awtrixlight", "mqtt:awtrix-clock:myBroker:myAwtrix").showCustomNotification(
        params, false, true, true, "", "", false
    )
end

rule "Progress Bar Demo"
when
    Item Washing_Machine_Progress changed
then
    var progress = (Washing_Machine_Progress.state as Number).intValue
    
    // Update custom app with progress bar
    Custom_Text.sendCommand("Washing")
    Custom_Icon.sendCommand("washing-machine")
    Custom_Progress.sendCommand(progress)
    Custom_ProgressColor.sendCommand("0,255,0")  // Green progress bar
    
    if (progress == 100) {
        // Play sound when done
        getActions("mqtt.awtrixlight", "mqtt:awtrix-clock:myBroker:myAwtrix").playSound("complete")
    }
end
```

These rules demonstrate:

- Using indicators to show battery status
- Creating custom notifications with icons and colors
- Playing RTTTL melodies and sound files
- Displaying progress bars

## AWTRIX NG

Clocks that run the [AWTRIX NG](https://github.com/Blueforcer/awtrix-ng) firmware, the successor of AWTRIX 3, are supported with their own thing types.
AWTRIX NG changed the MQTT topics and the payloads of AWTRIX 3, so the things, channels and rule actions differ from the ones above.
Where the two differ, the AWTRIX NG things follow AWTRIX NG.
Notes on the changes of the firmware are in the [migration guide of AWTRIX NG](https://blueforcer.github.io/awtrix-ng/esp32/guides/migrating-from-awtrix3/).

### AWTRIX NG Things

| Thing Type                 | Description                                                                                        |
| -------------------------- | -------------------------------------------------------------------------------------------------- |
| `awtrix-ng-clock` (Bridge) | An AWTRIX NG clock. It is a bridge for the apps, and a child of the MQTT broker bridge (`broker`). |
| `awtrix-ng-app`            | An app that openHAB pushes to the clock: text, icon, charts, progress bar, effects and drawings.   |

### AWTRIX NG Discovery

The clock is discovered from the retained `<prefix>/state/device` message that it publishes.
Enable MQTT in the web interface of the clock, point it to the broker of the MQTT bridge in openHAB, and the clock shows up in the inbox.
The default prefix of AWTRIX NG is the MAC address of the clock (12 lowercase hexadecimal digits), which is used as the `basetopic`.

A prefix with several levels (`home/clock1`) is not discovered, because the discovery only looks at the first topic level.
Add such a clock manually and enter the prefix as `basetopic`.

Apps that are shown on the clock but unknown to openHAB are offered in the inbox of the bridge.
The built-in apps of the clock (Time, Date, Temperature, Humidity and Battery) are only offered when `discoverDefaultApps` is enabled, and the `Notification` app is never offered.

**Adopting a discovered app replaces it.**
AWTRIX NG offers no way to read what another system has pushed to an app.
When you accept a discovered app, the binding pushes an app with the same name that is empty at first, and your channels fill it from then on.
Whatever the other system had pushed under that name is gone.
Accept an app only when openHAB is meant to take it over.

### AWTRIX NG Thing Configuration

#### Bridge Configuration (`awtrix-ng-clock`)

| Parameter             | Description                                                                                                  | Default | Required |
| --------------------- | ------------------------------------------------------------------------------------------------------------ | ------- | -------- |
| `basetopic`           | MQTT prefix of the clock, as shown in its MQTT settings. By default this is its 12 character MAC address.    | -       | Yes      |
| `appLockTimeout`      | Seconds until an app that was locked with the select button (see `useButtons`) releases the clock again.     | 10      | No       |
| `discoverDefaultApps` | Also offer the built-in apps of the clock in the inbox. Their look is only controlled by the clock settings. | false   | No       |

#### App Configuration (`awtrix-ng-app`)

| Parameter    | Description                                                                      | Default | Required |
| ------------ | -------------------------------------------------------------------------------- | ------- | -------- |
| `appname`    | Name of the app on the clock: 1 to 32 letters, digits, `-` or `_`.               | -       | Yes      |
| `useButtons` | Let the buttons of the clock lock the app and trigger the button channels of it. | false   | No       |

With `useButtons` enabled, pressing the select button locks the app on the display.
While the app is locked, the automatic app rotation and the button navigation of the clock are switched off, and the third indicator shows red.
The indicator starts to fade in and out for the last seconds before the lock ends.
The left and right buttons, and the select button, trigger the button channels of the clock and of the locked app while the lock lasts.
Each press renews the lock for `appLockTimeout` seconds.
When the lock ends, the binding restores the rotation and navigation settings and the third indicator as the clock reported them before the lock.
If the clock is offline when the lock ends, this happens when it is back online.
That state is also kept on the broker (`<prefix>/openhab/applock`), so that an openHAB restart during a lock does not leave the clock stuck.
A lock on the broker that this openHAB did not create is taken for one that was left behind and is ended.
Do not let two openHAB instances use the same clock with `useButtons`, as they would end each other's locks.

### How AWTRIX NG Apps Are Kept

AWTRIX NG does not keep pushed apps over a reboot, and the commands of the binding are not retained on the broker.
Each app handler therefore pushes its app again whenever the clock sends its capabilities, which it does every time it connects.
The state of an app, which openHAB needs after a restart, is kept as a retained message on `<prefix>/openhab/apps/<appname>`.
The clock ignores this topic.
The topic is cleared when the app thing is removed.

### AWTRIX NG Channels

The option lists of the channels `transition-effect` and `overlay` of the clock, and `effect`, `overlay` and `font` of the apps, are filled from what the clock reports as its capabilities.

Durations are in milliseconds, as in the API of AWTRIX NG.
A command without a unit is taken as milliseconds, a quantity such as `7.5 s` is converted.

#### Bridge Channels (`awtrix-ng-clock`)

| Channel                       | Type                     | R/W | Description                                                                                                                               |
| ----------------------------- | ------------------------ | --- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| `alert-volume`                | Number:Dimensionless     | RW  | Alert Volume: Share of the master volume for notifications and alerts                                                                     |
| `app`                         | String                   | RW  | Active App: App currently shown on the screen. A command switches to the app with that name.                                              |
| `app-duration`                | Number:Time              | RW  | App Duration: Display time per app and default duration of a notification                                                                 |
| `app-volume`                  | Number:Dimensionless     | RW  | App Volume: Share of the master volume for the sounds of scripts                                                                          |
| `auto-brightness`             | Switch                   | RW  | Automatic Brightness: Let the clock set the brightness automatically based on its light sensor                                            |
| `auto-transition`             | Switch                   | RW  | Automatic App Rotation: Rotate through the apps automatically                                                                             |
| `battery-color`               | Color                    | RW  | Battery Color: Text color of the battery app                                                                                              |
| `battery-level`               | Number:Dimensionless     | R   | Battery Level: Battery level in percent                                                                                                   |
| `battery-voltage`             | Number:ElectricPotential | R   | Battery Voltage: Battery voltage calculated from the battery pin                                                                          |
| `block-navigation`            | Switch                   | RW  | Block Button Navigation: Disable app switching, display toggle and menu with the buttons. Button events are still reported.               |
| `brightness`                  | Number:Dimensionless     | RW  | Display Brightness: Screen brightness in percent. Setting it switches the automatic brightness off.                                       |
| `button-left`                 | Trigger                  | R   | Button Left: Left button pressed or released                                                                                              |
| `button-right`                | Trigger                  | R   | Button Right: Right button pressed or released                                                                                            |
| `button-select`               | Trigger                  | R   | Button Select: Select button pressed or released                                                                                          |
| `calendar-body-color`         | Color                    | RW  | Calendar Body Color: Background of the calendar box                                                                                       |
| `calendar-header-color`       | Color                    | RW  | Calendar Header Color: Top bar of the calendar box                                                                                        |
| `calendar-text-color`         | Color                    | RW  | Calendar Text Color: Day number of the calendar box                                                                                       |
| `date-color`                  | Color                    | RW  | Date Color: Text color of the date                                                                                                        |
| `date-month-names`            | Switch                   | RW  | Date Month Names: Use three letter month names instead of numbers                                                                         |
| `date-order`                  | String                   | RW  | Date Order: Order of day, month and year                                                                                                  |
| `date-separator`              | String                   | RW  | Date Separator: Character between the parts of the date                                                                                   |
| `date-show-weekday`           | Switch                   | RW  | Date Weekday: Prepend the weekday to the date                                                                                             |
| `date-year-mode`              | String                   | RW  | Date Year: How the year is shown                                                                                                          |
| `display`                     | Switch                   | RW  | Display Power: Switches the display ON or OFF                                                                                             |
| `error`                       | Trigger                  | R   | Command Error: Triggered with the JSON reported by the clock when it rejected a command                                                   |
| `fps`                         | Number                   | R   | Frame Rate: Actual display refresh rate in frames per second                                                                              |
| `free-heap`                   | Number:DataAmount        | R   | Free Memory: Free heap memory of the clock                                                                                                |
| `humidity`                    | Number:Dimensionless     | R   | Humidity: Relative humidity                                                                                                               |
| `humidity-color`              | Color                    | RW  | Humidity Color: Text color of the humidity app                                                                                            |
| `indicator-1`                 | Color                    | RW  | Indicator 1: Color of the indicator dot. ON uses green, OFF or black switches the indicator off.                                          |
| `indicator-2`                 | Color                    | RW  | Indicator 2: Color of the indicator dot. ON uses green, OFF or black switches the indicator off.                                          |
| `indicator-3`                 | Color                    | RW  | Indicator 3: Color of the indicator dot. ON uses green, OFF or black switches the indicator off.                                          |
| `ip-address`                  | String                   | R   | IP Address: Network address of the clock                                                                                                  |
| `light-level`                 | Number:Dimensionless     | R   | Light Level: Ambient light as a relative percentage (not lux)                                                                             |
| `low-battery`                 | Switch                   | R   | Low Battery: Low battery warning of the clock                                                                                             |
| `moodlight`                   | Color                    | RW  | Mood Light: Fills the display with a color. ON uses white, OFF or black switches the mood light off. The clock does not report its state. |
| `overlay`                     | String                   | RW  | Weather Overlay: Weather effect over the whole display. An empty command clears it. The clock does not report its state.                  |
| `pressure`                    | Number:Pressure          | R   | Pressure: Air pressure                                                                                                                    |
| `rssi`                        | Number:Power             | R   | WiFi Signal: RSSI value of the WiFi signal                                                                                                |
| `rtttl`                       | String                   | W   | Play RTTTL: Plays a melody in RTTTL notation, e.g. "two:d=4,o=5,b=200:c,e"                                                                |
| `screen`                      | Image                    | R   | Screen Mirror: Image of the screen of the clock. It is refreshed when the app changes, or on a REFRESH command.                           |
| `sound`                       | String                   | W   | Play Sound: Plays the stored sound with the given name                                                                                    |
| `temperature`                 | Number:Temperature       | R   | Temperature: Temperature measured by the clock                                                                                            |
| `temperature-color`           | Color                    | RW  | Temperature Color: Text color of the temperature app                                                                                      |
| `text-color`                  | Color                    | RW  | Text Color: Default text color                                                                                                            |
| `time-24h`                    | Switch                   | RW  | 24 Hour Clock: 24 hour format, otherwise 12 hour                                                                                          |
| `time-color`                  | Color                    | RW  | Clock Color: Text color of the clock                                                                                                      |
| `time-leading-zero`           | Switch                   | RW  | Leading Zero: Show the hour with two digits                                                                                               |
| `time-mode`                   | Number                   | RW  | Clock Style: Layout of the clock app                                                                                                      |
| `time-separator`              | String                   | RW  | Clock Separator: Behavior of the colon of the clock                                                                                       |
| `time-show-ampm`              | Switch                   | RW  | Show AM/PM: Show AM or PM, needs the 12 hour format and hidden when seconds are shown                                                     |
| `time-show-seconds`           | Switch                   | RW  | Show Seconds: Show the seconds in the clock                                                                                               |
| `transition-direction`        | String                   | RW  | Transition Direction: Reverses directional transitions                                                                                    |
| `transition-duration`         | Number:Time              | RW  | Transition Duration: Length of the animation between apps, 0 for instant                                                                  |
| `transition-effect`           | String                   | RW  | Transition Effect: Animation between apps                                                                                                 |
| `uppercase`                   | Switch                   | RW  | Uppercase: Show pushed apps and notifications in capitals                                                                                 |
| `uptime`                      | Number:Time              | R   | Uptime: Time since the last start of the clock                                                                                            |
| `use-celsius`                 | Switch                   | RW  | Use Celsius: Show the temperature in Celsius, otherwise in Fahrenheit                                                                     |
| `volume`                      | Number:Dimensionless     | RW  | Master Volume: Master volume, 0 silences everything                                                                                       |
| `weekday-bar-active-color`    | Color                    | RW  | Weekday Bar Active Color: Color of the current day if it is a workday                                                                     |
| `weekday-bar-inactive-color`  | Color                    | RW  | Weekday Bar Inactive Color: Color of the other workdays                                                                                   |
| `weekday-bar-show`            | Switch                   | RW  | Weekday Bar: Show the weekday bar of the clock app                                                                                        |
| `weekday-bar-start-on-monday` | Switch                   | RW  | Week Starts on Monday: The weekday bar starts on Monday, otherwise on Sunday                                                              |

The clock does not report the state of `moodlight` and `overlay`, so these two channels keep what was last sent.
The settings channels are written to `<prefix>/cmd/settings` and are read back from `<prefix>/state/settings`.

#### App Channels (`awtrix-ng-app`)

| Channel                | Type                 | R/W | Description                                                                                                                 |
| ---------------------- | -------------------- | --- | --------------------------------------------------------------------------------------------------------------------------- |
| `active`               | Switch               | R   | Active: The app is shown on the clock right now                                                                             |
| `background-color`     | Color                | RW  | Background Color: Color of the background                                                                                   |
| `bar-chart`            | String               | RW  | Bar Chart: Comma separated values of a bar chart, at most the newest 16                                                     |
| `button-left`          | Trigger              | R   | Button Left: Left button pressed or released while the app is locked for button control                                     |
| `button-right`         | Trigger              | R   | Button Right: Right button pressed or released while the app is locked for button control                                   |
| `button-select`        | Trigger              | R   | Button Select: Select button pressed or released while the app is locked for button control                                 |
| `chart-autoscale`      | Switch               | RW  | Chart Autoscale: Scale the chart to its values instead of a fixed range                                                     |
| `chart-color`          | Color                | RW  | Chart Color: Color of the chart, the text color if not set                                                                  |
| `draw`                 | String               | RW  | Draw Commands: JSON array of draw commands, e.g. [["pixel",1,2,"#FF0000"]]                                                  |
| `duration`             | Number:Time          | RW  | Duration: How long the app is shown, 0 for the default of the clock                                                         |
| `effect`               | String               | RW  | Background Effect: Animated background of the app                                                                           |
| `effect-speed`         | Number               | RW  | Effect Speed: Speed of the effect, from 0.1 to 10                                                                           |
| `enabled`              | Switch               | RW  | Enabled: Switches the app in the rotation of the clock on or off                                                            |
| `font`                 | String               | RW  | Font: Font of the text                                                                                                      |
| `icon`                 | String               | RW  | Icon: ID of an icon, or an image as a data URL (data:image/gif;base64,... or data:image/jpeg;base64,...)                    |
| `icon-gap`             | Number               | RW  | Icon Gap: Space between the icon and the text, in pixels                                                                    |
| `icon-mode`            | String               | RW  | Icon Mode: Whether the icon stays or moves with the text                                                                    |
| `icon-offset-x`        | Number               | RW  | Icon Offset: Moves the icon left or right, in pixels                                                                        |
| `icons`                | String               | RW  | Extra Icons: JSON array of at most 4 extra icons, e.g. [{"icon":"1234","x":0,"y":0}]                                        |
| `lifetime`             | Number:Time          | RW  | Lifetime: How long the app lives without an update, 0 for forever                                                           |
| `lifetime-expiry`      | String               | RW  | Lifetime Expiry: Whether an expired app is removed or only marked                                                           |
| `line-chart`           | String               | RW  | Line Chart: Comma separated values of a line chart, at most the newest 16                                                   |
| `overlay`              | String               | RW  | Weather Overlay: Weather effect over the app                                                                                |
| `palette`              | String               | RW  | Palette: Name of a built-in palette, or colors separated by commas (#RRGGBB,#RRGGBB)                                        |
| `palette-blend`        | Switch               | RW  | Palette Blend: Smooth instead of hard transitions between the colors of the palette                                         |
| `palette-span`         | Number               | RW  | Palette Span: Pixels after which the palette repeats, 0 to stretch it over the text                                         |
| `palette-speed`        | Number               | RW  | Palette Speed: Speed of the palette animation, 0 for none                                                                   |
| `progress`             | Number:Dimensionless | RW  | Progress: Progress bar in percent, -1 switches it off                                                                       |
| `progress-color`       | Color                | RW  | Progress Color: Color of the filled part of the progress bar                                                                |
| `progress-track-color` | Color                | RW  | Progress Track Color: Color of the empty part of the progress bar                                                           |
| `repeat`               | Number               | RW  | Repeat: Number of scroll cycles, 0 for off                                                                                  |
| `reset`                | Switch               | RW  | Reset: Sets the app back to its defaults                                                                                    |
| `scroll-direction`     | String               | RW  | Scroll Direction: Direction the text travels                                                                                |
| `scroll-entry`         | String               | RW  | Scroll Entry: Where the text starts, inline or offscreen                                                                    |
| `scroll-gap`           | Number               | RW  | Scroll Gap: Gap between the repetitions of a looping text, in pixels                                                        |
| `scroll-hold`          | Number:Time          | RW  | Scroll Hold: Pause before and between the movements of the text                                                             |
| `scroll-mode`          | String               | RW  | Scroll Mode: How the text moves                                                                                             |
| `scroll-speed`         | Number:Dimensionless | RW  | Scroll Speed: Scroll speed in percent of the base speed                                                                     |
| `scroll-when-fits`     | String               | RW  | Scroll When Fits: Whether text that fits the display still moves                                                            |
| `text`                 | String               | RW  | Text: Text of the app. Parts between `<font color="#RRGGBB">` and `</font>` are colored, text outside of the tags is white. |
| `text-align`           | String               | RW  | Text Alignment: Where text that fits the display sits                                                                       |
| `text-blink`           | Number:Time          | RW  | Text Blink: Blink period of the text, 0 for off                                                                             |
| `text-case`            | String               | RW  | Text Case: Upper or lower case of the text. inherit follows the uppercase setting of the clock.                             |
| `text-color`           | Color                | RW  | Text Color: Color of the text. Setting it switches the text palette off.                                                    |
| `text-fade`            | Number:Time          | RW  | Text Fade: Period of a smooth fade in and out of the text, 0 for off                                                        |
| `text-in-front`        | Switch               | RW  | Text In Front: Draw the text on top of charts and drawings                                                                  |
| `text-offset-x`        | Number               | RW  | Text Offset: Moves the text left or right, in pixels                                                                        |
| `text-palette`         | Switch               | RW  | Text Palette: Color the text from the palette of the app. Setting a text color switches it off.                             |

The `text` channel supports color tags, see [Text Color Tags](#awtrix-ng-text-color-tags).

Setting `text-color` switches `text-palette` off, and `text-palette` makes the text use the palette of the app.

The channels `bar-chart` and `line-chart` show the newest 16 values of the comma separated list.

### AWTRIX NG Text Color Tags

The `text` channel colors parts of the text with font tags:

```html
<font color="#RRGGBB">colored text</font>
```

Text outside of the tags is white.
The binding sends the colored parts to AWTRIX NG as a list of fragments.

```java
items.Custom_Text.sendCommand("Hello <font color=\"#FF0000\">World</font> in <font color=\"#00FF00\">Color</font>")
```

### AWTRIX NG Full Example

#### Things

```java
Bridge mqtt:broker:myBroker [ host="mosquitto.local" ]

Bridge mqtt:awtrix-ng-clock:myBroker:myClock "Living Room Clock" (mqtt:broker:myBroker) [ basetopic="aabbccddeeff" ] {
    Thing awtrix-ng-app washing "Washing Machine" [ appname="washing" ]
    Thing awtrix-ng-app doorbell "Doorbell" [ appname="doorbell", useButtons=true ]
}
```

#### Items

```java
Number:Dimensionless Clock_Brightness "Brightness [%d %%]" { channel="mqtt:awtrix-ng-clock:myBroker:myClock:brightness" }
Switch Clock_Display "Display" { channel="mqtt:awtrix-ng-clock:myBroker:myClock:display" }
Color Clock_Indicator1 "Indicator 1" { channel="mqtt:awtrix-ng-clock:myBroker:myClock:indicator-1" }
String Clock_App "Active App [%s]" { channel="mqtt:awtrix-ng-clock:myBroker:myClock:app" }
Image Clock_Screen "Screen" { channel="mqtt:awtrix-ng-clock:myBroker:myClock:screen" }

String Washing_Text "Text" { channel="mqtt:awtrix-ng-app:myBroker:myClock:washing:text" }
String Washing_Icon "Icon" { channel="mqtt:awtrix-ng-app:myBroker:myClock:washing:icon" }
Color Washing_Color "Color" { channel="mqtt:awtrix-ng-app:myBroker:myClock:washing:text-color" }
Number:Dimensionless Washing_Progress "Progress [%d %%]" { channel="mqtt:awtrix-ng-app:myBroker:myClock:washing:progress" }
Number:Time Washing_Duration "Duration" { channel="mqtt:awtrix-ng-app:myBroker:myClock:washing:duration" }
```

### AWTRIX NG Rule Actions

The bridge offers actions in the scope `mqtt.awtrixlight`.

Rules DSL:

```java
val awtrix = getActions("mqtt.awtrixlight", "mqtt:awtrix-ng-clock:myBroker:myClock")
```

JS Scripting:

```javascript
var awtrix = actions.thingActions("mqtt.awtrixlight", "mqtt:awtrix-ng-clock:myBroker:myClock");
```

| Action                                      | Description                                                                                         |
| ------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| `activateIndicator(indicator, color)`       | Switches indicator 1 to 3 on in a color (`#RRGGBB`).                                                |
| `deactivateIndicator(indicator)`            | Switches the indicator off.                                                                         |
| `blinkIndicator(indicator, color, blinkMs)` | Blinks the indicator.                                                                               |
| `fadeIndicator(indicator, color, fadeMs)`   | Fades the indicator.                                                                                |
| `setMoodlight(color, brightness)`           | Fills the display with a color. The brightness (0 to 255) may be `null` for the current brightness. |
| `clearMoodlight()`                          | Switches the mood light off.                                                                        |
| `reboot()`                                  | Reboots the clock.                                                                                  |
| `sleep(durationMs)`                         | Sends the clock to deep sleep for the time.                                                         |
| `playSound(name)`                           | Plays a sound that is stored on the clock.                                                          |
| `playRtttl(rtttl)`                          | Plays a melody in RTTTL notation.                                                                   |
| `playTrack(track)`                          | Plays a track of the DFPlayer of the clock.                                                         |
| `stopSound(group)`                          | Stops the sounds of a group (`alert`, `app`, `radio`), or all sounds when empty.                    |
| `nextApp()`, `previousApp()`                | Switches to the next or previous app.                                                               |
| `switchApp(name, fast)`                     | Switches to an app. `fast` skips the transition and may be `null`.                                  |
| `setAppOrder(order, disabled)`              | Sets the order of the apps, and the complete list of apps that are switched off.                    |
| `setAppEnabled(name, enabled)`              | Switches one app on or off in the rotation.                                                         |
| `showNotification(text, icon)`              | Shows a notification with a text. The icon may be `null`.                                           |
| `showCustomNotification(params)`            | Shows a notification with the keys of the AWTRIX NG payload.                                        |
| `dismissNotification()`                     | Dismisses the notification that is shown.                                                           |
| `dismissNamedNotification(name)`            | Dismisses the notification that was shown with the `name`.                                          |

Every argument has to be given, as the actions have no shorter forms. Pass `null` for those that may be `null`.

#### Notifications

`showCustomNotification` takes a map with the keys of the AWTRIX NG notification payload, for example `text`, `textColor`, `durationMs`, `hold`, `stack`, `wakeup`, `sound` and `name`.
Colors are `#RRGGBB` strings.
AWTRIX NG rejects a whole payload that contains a key it does not know, so the binding checks the keys first.
A notification with an unknown key, or with a value that does not fit its key, is not sent and the reason is logged.
A `name` is limited to 1 to 32 letters, digits, `-` and `_`, because the clock needs it for the topic of `dismissNamedNotification`.

```java
val params = newHashMap(
    'text' -> 'Doorbell',
    'icon' -> 'bell',
    'textColor' -> '#00FFFF',
    'durationMs' -> 8000,
    'wakeup' -> true,
    'sound' -> newHashMap('file' -> 'ding'),
    'name' -> 'door'
)
awtrix.showCustomNotification(params)
```

#### Examples

```java
rule "Low battery indicator"
when
    Item Clock_Battery changed
then
    val awtrix = getActions("mqtt.awtrixlight", "mqtt:awtrix-ng-clock:myBroker:myClock")
    if ((Clock_Battery.state as Number).intValue <= 20) {
        awtrix.blinkIndicator(1, "#FF0000", 500)
        awtrix.showNotification("Low Battery!", "battery-alert")
    } else {
        awtrix.deactivateIndicator(1)
    }
end

rule "Door bell"
when
    Item Doorbell_Button changed to ON
then
    val awtrix = getActions("mqtt.awtrixlight", "mqtt:awtrix-ng-clock:myBroker:myClock")
    awtrix.playRtttl("doorbell:d=4,o=6,b=100:8e,8g,8e,8c")
    awtrix.showNotification("Doorbell", "bell-ring")
end
```

### Switching from AWTRIX 3 to AWTRIX NG

A clock that is flashed with AWTRIX NG and keeps its MQTT prefix does not have to be set up again.
The `awtrix-clock` thing notices the NG firmware and changes itself to an `awtrix-ng-clock`, and its `awtrix-app` things follow it to `awtrix-ng-app`.
The thing keeps its UID, label, location and the configuration that both firmware types share (`basetopic`, `appLockTimeout`, `discoverDefaultApps`, and `appname` and `useButtons` of the apps).
`lowBatteryThreshold` is gone, `low-battery` is reported by the clock.
The change only goes this way.
To use a clock with AWTRIX 3 again, delete the AWTRIX NG things and add the AWTRIX 3 things through discovery.

The clock is taken as AWTRIX NG when it sends its device state on `<prefix>/state/device` with a changing `uptimeSeconds`.
AWTRIX NG sends it every `statsInterval` (10 seconds by default), so the change takes up to that long after the clock runs AWTRIX NG.
The broker keeps the last device state of a clock, which could be left over from a clock that was flashed back to AWTRIX 3 or is switched off.
That is why the first device state after connecting to the broker is not enough, the uptime in a later one has to differ.
A change is logged at INFO level.

The channels of the thing are replaced by those of the new thing type.
Links of channels with the same ID stay, but AWTRIX NG changed what some of them mean (see the tables below), so check the items of the changed channels.
A link to a channel that the new thing type does not have stays on its item, but it has no effect any more.
Link the item to the new channel and remove the old link.

AWTRIX NG only accepts app names of 1 to 32 letters, digits, `-` and `_`.
An app with another name shows a configuration error after the change.

The AWTRIX 3 things keep the state of their apps as retained messages on `<prefix>/custom/<appname>`.
AWTRIX NG ignores them, but they stay on the broker.
They can be removed with an empty retained message, for example `mosquitto_pub -r -n -t awtrix/custom/washing`.

Notes on the changes of the firmware are in the [migration guide of AWTRIX NG](https://blueforcer.github.io/awtrix-ng/esp32/guides/migrating-from-awtrix3/).
Where the two differ, the AWTRIX NG things follow AWTRIX NG.

#### Bridge Channels After the Change

| Awtrix 3         | AWTRIX NG                                                                                                                             |
| ---------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| `lux`            | `light-level`, a percentage and not lux.                                                                                              |
| `indicator-1..3` | The same names, but a Color channel instead of a Switch.                                                                              |
| `screen`         | The same name, an Image channel that follows the size of the panel.                                                                   |
| -                | New: voltage, memory, frame rate, IP address, uptime, volumes, rotation, clock and date format, colors, mood light, overlay, `error`. |

#### App Channels After the Change

| Awtrix 3                    | AWTRIX NG                                                                                |
| --------------------------- | ---------------------------------------------------------------------------------------- |
| `active`                    | `enabled` switches the app in the rotation. `active` only reports that the app is shown. |
| `autoscale`                 | `chart-autoscale`                                                                        |
| `background`                | `background-color`                                                                       |
| `bar`, `line`               | `bar-chart`, `line-chart`                                                                |
| `blink-text`, `fade-text`   | `text-blink`, `text-fade`                                                                |
| `center`                    | `text-align`                                                                             |
| `color`                     | `text-color`, and `chart-color` for charts                                               |
| `duration`, `lifetime`      | The same names, in milliseconds instead of seconds                                       |
| `effect-blend`              | `palette-blend`                                                                          |
| `effect-speed`              | The same name, from 0.1 to 10 instead of a percentage                                    |
| `effect-palette`            | `palette`                                                                                |
| `lifetime-mode`             | `lifetime-expiry`                                                                        |
| `overlay`                   | The same name, an empty command clears the overlay instead of `Clear`                    |
| `progress-background`       | `progress-track-color`                                                                   |
| `push-icon`                 | `icon-mode`                                                                              |
| `rainbow`, `gradient-color` | `palette` together with `text-palette`                                                   |
| `text-case`                 | The same name, with the words `inherit`, `upper` and `asTyped`                           |
| `text-offset`               | `text-offset-x`                                                                          |
| `top-text`                  | Removed, AWTRIX NG has no equivalent                                                     |

New channels are the scroll settings, the icon settings (`icons`, `icon-mode`, offsets), `font`, `repeat`, `palette-*`, `text-in-front`, `draw` and the button triggers.
