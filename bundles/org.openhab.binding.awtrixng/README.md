# AWTRIX NG Binding

This binding controls clocks that run the [AWTRIX NG](https://github.com/Blueforcer/awtrix-ng) firmware, the successor of AWTRIX 3, over MQTT.
The connection to the broker comes from the [MQTT binding](https://www.openhab.org/addons/bindings/mqtt/), which has to be installed.
It supports the Ulanzi TC001 and the other displays that AWTRIX NG runs on.

AWTRIX NG changed the MQTT topics and the payloads of AWTRIX 3, so this binding is a sibling of the [MQTT Awtrix 3 binding](https://www.openhab.org/addons/bindings/mqtt.awtrixlight/) and not an update of it.
Both can be installed side by side.
Users of the Awtrix 3 binding find the differences in [Migrating from the Awtrix 3 binding](#migrating-from-the-awtrix-3-binding).

## Supported Things

| Thing Type       | Description                                                                                        |
| ---------------- | -------------------------------------------------------------------------------------------------- |
| `clock` (Bridge) | An AWTRIX NG clock. It is a bridge for the apps, and a child of the MQTT broker bridge (`broker`). |
| `app`            | An app that openHAB pushes to the clock: text, icon, charts, progress bar, effects and drawings.   |

## Discovery

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

## Binding Configuration

The binding does not have any binding configuration parameters.

## Thing Configuration

### Bridge Configuration (`clock`)

| Parameter             | Description                                                                                                  | Default | Required |
| --------------------- | ------------------------------------------------------------------------------------------------------------ | ------- | -------- |
| `basetopic`           | MQTT prefix of the clock, as shown in its MQTT settings. By default this is its 12 character MAC address.    | -       | Yes      |
| `appLockTimeout`      | Seconds until an app that was locked with the select button (see `useButtons`) releases the clock again.     | 10      | No       |
| `discoverDefaultApps` | Also offer the built-in apps of the clock in the inbox. Their look is only controlled by the clock settings. | false   | No       |

### App Configuration (`app`)

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

## How the Binding Keeps Apps

AWTRIX NG does not keep pushed apps over a reboot, and the commands of the binding are not retained on the broker.
Each app handler therefore pushes its app again whenever the clock sends its capabilities, which it does every time it connects.
The state of an app, which openHAB needs after a restart, is kept as a retained message on `<prefix>/openhab/apps/<appname>`.
The clock ignores this topic.
The topic is cleared when the app thing is removed.

## Channels

The option lists of the channels `transition-effect` and `overlay` of the clock, and `effect`, `overlay` and `font` of the apps, are filled from what the clock reports as its capabilities.

Durations are in milliseconds, as in the API of AWTRIX NG.
A command without a unit is taken as milliseconds, a quantity such as `7.5 s` is converted.

### Bridge Channels (`clock`)

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

### App Channels (`app`)

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

The `text` channel supports color tags, see [Text Color Tags](#text-color-tags).

Setting `text-color` switches `text-palette` off, and `text-palette` makes the text use the palette of the app.

The channels `bar-chart` and `line-chart` show the newest 16 values of the comma separated list.

## Text Color Tags

The `text` channel colors parts of the text with font tags:

```html
<font color="#RRGGBB">colored text</font>
```

Text outside of the tags is white.
The binding sends the colored parts to AWTRIX NG as a list of fragments.

```java
items.Custom_Text.sendCommand("Hello <font color=\"#FF0000\">World</font> in <font color=\"#00FF00\">Color</font>")
```

## Full Example

### Thing Configuration

```java
Bridge mqtt:broker:myBroker [ host="mosquitto.local" ]

Bridge awtrixng:clock:myBroker:myClock "Living Room Clock" (mqtt:broker:myBroker) [ basetopic="aabbccddeeff" ] {
    Thing app washing "Washing Machine" [ appname="washing" ]
    Thing app doorbell "Doorbell" [ appname="doorbell", useButtons=true ]
}
```

### Items

```java
Number:Dimensionless Clock_Brightness "Brightness [%d %%]" { channel="awtrixng:clock:myBroker:myClock:brightness" }
Switch Clock_Display "Display" { channel="awtrixng:clock:myBroker:myClock:display" }
Color Clock_Indicator1 "Indicator 1" { channel="awtrixng:clock:myBroker:myClock:indicator-1" }
String Clock_App "Active App [%s]" { channel="awtrixng:clock:myBroker:myClock:app" }
Image Clock_Screen "Screen" { channel="awtrixng:clock:myBroker:myClock:screen" }

String Washing_Text "Text" { channel="awtrixng:app:myBroker:myClock:washing:text" }
String Washing_Icon "Icon" { channel="awtrixng:app:myBroker:myClock:washing:icon" }
Color Washing_Color "Color" { channel="awtrixng:app:myBroker:myClock:washing:text-color" }
Number:Dimensionless Washing_Progress "Progress [%d %%]" { channel="awtrixng:app:myBroker:myClock:washing:progress" }
Number:Time Washing_Duration "Duration" { channel="awtrixng:app:myBroker:myClock:washing:duration" }
```

## Rule Actions

The bridge offers actions in the scope `awtrixng`.

Rules DSL:

```java
val awtrix = getActions("awtrixng", "awtrixng:clock:myBroker:myClock")
```

JS Scripting:

```javascript
var awtrix = actions.thingActions("awtrixng", "awtrixng:clock:myBroker:myClock");
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

### Notifications

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

### Examples

```java
rule "Low battery indicator"
when
    Item Clock_Battery changed
then
    val awtrix = getActions("awtrixng", "awtrixng:clock:myBroker:myClock")
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
    val awtrix = getActions("awtrixng", "awtrixng:clock:myBroker:myClock")
    awtrix.playRtttl("doorbell:d=4,o=6,b=100:8e,8g,8e,8c")
    awtrix.showNotification("Doorbell", "bell-ring")
end
```

## Migrating from the Awtrix 3 binding

AWTRIX NG is not compatible with AWTRIX 3, and where the two differ, this binding follows AWTRIX NG.
Things, channels and rules have to be recreated, they cannot be converted.
Notes on the changes of the firmware are in the [migration guide of AWTRIX NG](https://blueforcer.github.io/awtrix-ng/esp32/guides/migrating-from-awtrix3/).

### Things

| Awtrix 3 binding    | This binding     |
| ------------------- | ---------------- |
| `mqtt:awtrix-clock` | `awtrixng:clock` |
| `mqtt:awtrix-app`   | `awtrixng:app`   |

- `basetopic` has no default and is the prefix of the clock, by default its MAC address, where the Awtrix 3 binding defaulted to `awtrix`.
- `lowBatteryThreshold` is gone. `low-battery` is reported by the clock.
- The binding ID is `awtrixng` and no longer `mqtt`, and so is the action scope.

### Bridge Channels

| Awtrix 3         | This binding                                                                                                                          |
| ---------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| `lux`            | `light-level`, a percentage and not lux.                                                                                              |
| `indicator-1..3` | The same names, but a Color channel instead of a Switch.                                                                              |
| `screen`         | The same name, an Image channel that follows the size of the panel.                                                                   |
| -                | New: voltage, memory, frame rate, IP address, uptime, volumes, rotation, clock and date format, colors, mood light, overlay, `error`. |

### App Channels

| Awtrix 3                    | This binding                                                                             |
| --------------------------- | ---------------------------------------------------------------------------------------- |
| `active`                    | `enabled` switches the app in the rotation. `active` only reports that the app is shown. |
| `autoscale`                 | `chart-autoscale`                                                                        |
| `background`                | `background-color`                                                                       |
| `bar`, `line`               | `bar-chart`, `line-chart`                                                                |
| `blink`, `fade`             | `text-blink`, `text-fade`                                                                |
| `center`                    | `text-align`                                                                             |
| `color`                     | `text-color`, and `chart-color` for charts                                               |
| `duration`, `lifetime`      | The same names, in milliseconds instead of seconds                                       |
| `effect-blend`              | `palette-blend`                                                                          |
| `effect-palette`            | `palette`                                                                                |
| `lifetime-mode`             | `lifetime-expiry`                                                                        |
| `progress-background`       | `progress-track-color`                                                                   |
| `push-icon`                 | `icon-mode`                                                                              |
| `rainbow`, `gradient-color` | `palette` together with `text-palette`                                                   |
| `text-case`                 | The same name, with the words `inherit`, `upper` and `asTyped`                           |
| `text-offset`               | `text-offset-x`                                                                          |
| `top-text`                  | Removed, AWTRIX NG has no equivalent                                                     |

New channels are the scroll settings, the icon settings (`icons`, `icon-mode`, offsets), `font`, `repeat`, `palette-*`, `text-in-front`, `draw` and the button triggers.

### Actions

- Colors are `#RRGGBB` strings and no longer an `[r,g,b]` array, and times are in milliseconds.
- `sleep` takes milliseconds instead of seconds.
- `upgrade` is removed, AWTRIX NG updates only over HTTP.
- `showCustomNotification` takes only the keys of AWTRIX NG and no longer the `hold`, `wakeUp`, `stack`, `rtttl`, `sound` and `loopSound` arguments, they are keys of the map.
- New: `setMoodlight`, `clearMoodlight`, `playTrack`, `stopSound`, `dismissNamedNotification`, `nextApp`, `previousApp`, `switchApp`, `setAppOrder`, `setAppEnabled`.
