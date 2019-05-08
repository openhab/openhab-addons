# LaMetric Binding

The LaMetric binding allows to connect openHab to LaMetric Time connected clock devices, providing following features:

* Control the LaMetric Time Device
    * Control Display Brightness
    * Change Audio Volume
    * Enable / Disable Bluetooth
    * Activate an Application
* Send notifications messages
* Control the core (built-in) apps

## Supported Things

The device acts as a bridge and is exposed as "LaMetric Time" Thing.
The "LaMetric Time" Thing is directly responsible for device operations which include the display, audio, bluetooth, and notifications.
All apps are implemented as separate things under the bridge.

| App               | Thing Type   | Description                                                   |
|-------------------|--------------|---------------------------------------------------------------|
| Clock             | clockApp     | Clock that dispays time and date                              |
| Timer             | countdownApp | A countdown timer that counts by seconds                       |
| Radio             | radioApp     | Streaming radio player                                        |
| Stopwatch         | stopwatchApp | Stopwatch that counts up by seconds                           |
| Weather           | weatherApp   | Current weather conditions as well as a forecast              |

## Discovery

The binding supports two levels of discovery - device and apps.
Device discovery is accomplished via UPnP.
Once a device is added, discovery will find all apps installed on the device and suggest them as individual things with the device being the bridge.

## Binding Configuration

The binding requires no special configuration.

## Thing Configuration

### Bridge (Thing ID: "device")

The bridge requires a host and an API key. The key can be found by visiting [the LaMetric dev portal](https://developer.lametric.com/user/devices).

| Configuration Parameter | Type    | Description                                            | Default | Required |
|-------------------------|---------|--------------------------------------------------------|---------|----------|
| host                    | text    | Host name or network address of the LaMetric Time      |         | Yes      |
| apiKey                  | text    | API key to access LaMetric Time                        |         | Yes      |

### Core (Built-in) Apps (Thing ID: "clockApp", "countdownApp", "radioApp", "stopwatchApp", "weatherApp")

The core app things can be defined with no configuration at all.
The package name is defaulted for you.
If you do not specify a widget ID, the first available one will be used automatically.
Widgets are instances of the application.
For example, if you duplicated the weather app for two locations, the app would have two widgets.

| Configuration Parameter | Type    | Description                                                     | Default                   | Required |
|-------------------------|---------|-----------------------------------------------------------------|---------------------------|----------|
| widgetId                | text    | The identifier for the exact instance of the app (widget)       | The first widget ID found | No       |

### Sample Thing Configuration

```
Bridge lametrictime:device:demo [ host="somehost", apiKey="ksfjsdkfsksjfs" ]
{
    Thing clockApp     clock       [ widgetId="generatedcorewidgetid1" ]
    Thing countdownApp timer
    Thing radioApp     radio
    Thing stopwatchApp stopwatch
    Thing weatherApp   weather     [ widgetId="generatedcorewidgetid2" ]
}
```

## Channels

### Device

| Channel ID     | Item Type                                        | Description                                                                                                                                                                                                           |
|----------------|--------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| brightness     | Dimmer                                           | The brightness of the display. Please note that changing the brightness will automatically set the 'brightnessMode' to 'manual'.                                                                                      |
| brightnessMode | String (possible values are 'auto' and 'manual') | The mode for the display brightness. If set to 'auto' the brightness is set by the device automatically based on environment illumination. If set to 'manual' the brightness can be changed via 'brightness' channel. |
| volume         | Dimmer                                           | The volume of the device speaker.                                                                                                                                                                                     |
| bluetooth      | Switch                                           | The status of Bluetooth audio streaming on the device.                                                                                                                                                                |
| app            | String                                           | The active application on the device. State options for UIs are determined at runtime automatically. The value must be formatted as '<package name>:<widget ID>'.                                                     |
| info           | String                                           | Send informational notifications to the device.                                                                                                                                                                       |
| warning        | String                                           | Send warning notifications to the device.                                                                                                                                                                             |
| alert          | String                                           | Send alert notifications to the device.                                                                                                                                                                               |

### Apps

Note that app channels have no defined state from the device.
They exist as one-way communication only.

#### Clock App

| Channel ID | Item Type | Description                                                         |
|------------|-----------|---------------------------------------------------------------------|
| setAlarm   | DateTime  | Set the alarm using the given time (note that the date is not used) |
| command    | String    | Send a command to the app (disableAlarm)                            |

#### Timer App

| Channel ID | Item Type | Description                                     |
|------------|-----------|-------------------------------------------------|
| duration   | Number    | Set the duration of the timer in seconds    |
| command    | String    | Send a command to the app (start, pause, reset) |

#### Radio App

| Channel ID | Item Type | Description                                           |
|------------|-----------|-------------------------------------------------------|
| control    | Player    | Control interface to manipulate the radio             |

#### Stopwatch App

| Channel ID | Item Type | Description                                     |
|------------|-----------|-------------------------------------------------|
| command    | String    | Send a command to the app (start, pause, reset) |

#### Weather App

| Channel ID | Item Type | Description                          |
|------------|-----------|--------------------------------------|
| command    | String    | Send a command to the app (forecast) |

## How Tos

The following configuration examples assume the device was added with the thing id `lametrictime:device:demo`.
Replace the thing id in all the configurations with your real thing id which can be looked up via paper UI.

### Notifications

#### Simple text notifications

The binding provides three simple notification channels for info messages (channel id `info`), warning messages (channel id `warning`) and alert messages (channel id `alert`).

To post messages to these channels, simply map them to a String item, e.g. like this:

```
String DeviceNotifyInfo "Info Message" {channel="lametrictime:device:demo:info"}
```

By setting a text on the item, the binding will send the notification which is then shown on the LaMetric device. 

In a rule this can be done the following way:

``` 
DeviceNotifyInfo.sendCommand("My Information Message to be displayed")
```

## Items
 
Sample item configuration:
 
```
Dimmer DeviceBrightness         "Brightness"                                { channel="lametrictime:device:demo:brightness" }
String DeviceBrightnessMode     "Brightness Mode"                           { channel="lametrictime:device:demo:brightnessMode" }
Dimmer DeviceVolume             "Volume"                                    { channel="lametrictime:device:demo:volume" }
Switch DeviceBluetooth          "Bluetooth"                                 { channel="lametrictime:device:demo:bluetooth" }
String DeviceApp                "Application"                               { channel="lametrictime:device:demo:app" }

String DeviceNotifyInfo         "Info Message"                              { channel="lametrictime:device:demo:info" }
String DeviceNotifyWarning      "Warning Message"                           { channel="lametrictime:device:demo:warning" }
String DeviceNotifyAlert        "Alert Message"                             { channel="lametrictime:device:demo:alert" }
Switch NotifyInfo               "Notify Info"
Switch NotifyWarning            "Notify Warning"
Switch NotifyAlert              "Notify Alert"

DateTime ClockSetAlarm          "Set Alarm"                                 { channel="lametrictime:clockApp:demo:clock:setAlarm" }
String   ClockCommand           "Clock Command"                             { channel="lametrictime:clockApp:demo:clock:command" }
Switch   SetAlarmIn1Min         "Set Alarm in 1 min"

Number TimerDuration            "Timer Duration"                            { channel="lametrictime:countdownApp:demo:timer:duration" }
String TimerCommand             "Timer Command"                             { channel="lametrictime:countdownApp:demo:timer:command" }
Switch Set2MinTimer             "Set 2 Minute Timer"

Player RadioControl             "Player"                                    { channel="lametrictime:radioApp:demo:radio:control" }

String StopwatchCommand         "Stopwatch Command"                         { channel="lametrictime:stopwatchApp:demo:stopwatch:command" }

String WeatherCommand           "Weather Command"                           { channel="lametrictime:weatherApp:demo:weather:command" }
```

## Sitemap

Sample sitemap configuration:

**Note:** Populating switch or selection options automatically from the state description is not currently possible with sitemaps.
For this reason, the brightness modes and example applications are repeated here.

```
  Text label="LaMetric Time Demo" {
      Frame label="Device Controls" {
          Slider item=DeviceBrightness
          Switch item=DeviceBrightnessMode mappings=[AUTO="Automatic",MANUAL="Manual"]
          Slider item=DeviceVolume
          Switch item=DeviceBluetooth
          Selection item=DeviceApp mappings=["com.lametric.clock:widgetid"="Clock","com.lametric.countdown:widgetid"="Timer"]
      }
      Frame label="Device Notifications" {
          Switch item=NotifyInfo
          Switch item=NotifyWarning
          Switch item=NotifyAlert
      }
      Frame label="Clock" {
          Switch item=SetAlarmIn1Min
          Selection item=ClockCommand mappings=["disableAlarm"="Disable Alarm"]
      }
      Frame label="Timer" {
          Switch item=Set2MinTimer
          Selection item=TimerCommand mappings=["start"="Start","pause"="Pause","reset"="Reset"]
      }
      Frame label="Radio" {
          Default item=RadioControl
      }
      Frame label="Stopwatch" {
          Selection item=StopwatchCommand mappings=["start"="Start","pause"="Pause","reset"="Reset"]
      }
      Frame label="Weather" {
          Selection item=WeatherCommand mappings=["forecast"="Forecast"]
      }
  }
```

## Rules

Sample rules:

```
import java.util.Calendar

rule "Notify Info"
    when
        Item NotifyInfo changed to ON
    then
        NotifyInfo.postUpdate(OFF)
        
        logInfo("demo.rules", "Sending info notification")
        DeviceNotifyInfo.sendCommand("INFO!")
end

rule "Notify Warning"
    when
        Item NotifyWarning changed to ON
    then
        NotifyWarning.postUpdate(OFF)
        
        logInfo("demo.rules", "Sending warning notification")
        DeviceNotifyWarning.sendCommand("WARNING!")
end

rule "Notify Alert"
    when
        Item NotifyAlert changed to ON
    then
        NotifyAlert.postUpdate(OFF)
        
        logInfo("demo.rules", "Sending alert notification")
        DeviceNotifyAlert.sendCommand("ALERT!")
end

rule "Set Alarm in 1 Minute"
    when
         Item SetAlarmIn1Min changed to ON
    then
         SetAlarmIn1Min.postUpdate(OFF)
         
         logInfo("demo.rules", "Setting alarm for 1 minute from now")
         
         val cal = Calendar.getInstance()
         cal.add(Calendar.MINUTE, 1)
         ClockSetAlarm.sendCommand(new DateTimeType(cal))
end

rule "Set 2 Minute Timer"
    when
         Item Set2MinTimer changed to ON
    then
         Set2MinTimer.postUpdate(OFF)
         
         logInfo("demo.rules", "Configure timer for 2 minutes without starting")
         TimerDuration.sendCommand(120)
end
```
