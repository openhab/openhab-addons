# Xiaomi Mi Smart Home Binding

This binding allows your openHAB to communicate with the Xiaomi Smart Home Suite.
It consists of devices communicating over a ZigBee network with a ZigBee - WiFi gateway. 

The devices are very affordable and you can get them from your favourite chinese marktes like [AliExpress](https://www.aliexpress.com/) or [GearBest](https://www.gearbest.com). The sensors run on a coincell battery for over a year. 

After setup, you can disconnect the gateway from the internet to keep your sensor information private.

## Supported devices

| Device | Picture |
| --- | --- |
| Gateway v2 (with radio support) or v3  | ![](https://ae01.alicdn.com/kf/HTB1gF76ciqAXuNjy1Xdq6yYcVXa4/Original-Xiaomi-Mi-Gateway-2-Smart-Home-Kit-Multi-functional-Gateway-Work-with-Mi-Door-Sensor.jpg_300x300.jpg) |
| Mijia Temperature and Humidity Sensor | ![](https://ae01.alicdn.com/kf/HTB1ksk_MXXXXXcWXVXXq6xXFXXXz/Original-Xiaomi-Mi-Smart-Temperature-and-Humidity-Sensor-Put-the-baby-Home-office-Work-With-Android.jpg_300x300.jpg)|
| Aqara Temperature, Humidity and Pressure Sensor | ![](https://ae01.alicdn.com/kf/HTB1fD1URVXXXXaDXFXXq6xXFXXXU/Neue-Original-Xiaomi-Aqara-Intelligente-Luftdruck-Temperatur-Luftfeuchtigkeit-Sensor-Arbeit-Mit-Android-IOS-APP-Fasten-schiff.jpg_300x300.jpg) |
| Mijia Door/Window Sensor | ![](https://ae01.alicdn.com/kf/HTB1WQb3SpXXXXcLXpXXq6xXFXXXz/100-Original-Intelligent-Mini-Mijia-Xiaomi-MI-Door-Window-Sensor-for-Xiaomi-Smart-Home-Suite-Devices.jpg_300x300.jpg) |
| Aqara Door/Window Sensor | ![](https://ae01.alicdn.com/kf/HTB1C2f7RVXXXXbNXpXXq6xXFXXX9/Auf-lager-Original-xiaomi-aqara-Smart-T-ren-und-Fenster-Sensor-Mijia-Smart-home-kit-Zigbee.jpg_300x300.jpg) |
| Mijia Human Body Sensor | ![](https://ae01.alicdn.com/kf/HTB1mvasRXXXXXaZXVXXq6xXFXXXY/XIAOMI-Mi-mijia-Infrared-Motion-Sensor-Smart-Human-Body-Sensor-for-Home-Safety-Smart-Remote-Control.jpg_300x300.jpg) |
| Aqara Motion Sensor (with light intensity support) | ![](https://ae01.alicdn.com/kf/HTB1LaENRFXXXXXNapXXq6xXFXXXZ/Xiaomi-Aqara-Body-Sensor-Light-Intensity-Sensors-ZigBee-wifi-Wireless-Connection-Work-for-xiaomi-smart-home.jpg_300x300.jpg) |
| Smart Socket (Zigbee version) | ![](https://ae01.alicdn.com/kf/HTB17Fy2QXXXXXajaXXXq6xXFXXXQ/Original-Xiaomi-Smart-Socket-Plug-Mi-Zigbee-WiFi-APP-Wireless-Control-Switches-EU-US-AU-Timer.jpg_300x300.jpg) |
| Magic Cube Controller | ![](https://ae01.alicdn.com/kf/HTB1IFoebPuhSKJjSspmq6AQDpXam/Xiaomi-Mi-Magic-Cube-Controller-Zigbee-Version-Controlled-by-Six-Actions-For-Smart-Home-Device-work.jpg_300x300.jpg) |
| Aqara Magic Cube Controller | ![](https://ae01.alicdn.com/kf/HTB1ih7YsL9TBuNjy1zbq6xpepXal/Original-Xiaomi-Aqara-Cube-Magic-Cube-Smart-Home-Controller-Zigbee-Version-6-Gestures-Operation-Mijia-Smart.jpg_300x300.jpg) |
| Aqara Vibration Sensor | ![](https://ae01.alicdn.com/kf/HTB1mjNTKXuWBuNjSszbq6AS7FXaS/Xiaomi-Aqara-Zigbee-Shock-Sensor-Mijia-Aqara-Smart-Motion-Sensor-Vibration-Detection-Alarm-Monitor-for-MiHome.jpg_300x300.jpg) |
| Mijia Wireless Switch | ![](https://ae01.alicdn.com/kf/HTB1qoEAPVXXXXXdaVXXq6xXFXXXr/Original-Xiaomi-Smart-Wireless-Switch-App-Remote-Control-Smart-Home-Intelligent-Device-Accessories-For-Xiaomi-Smart.jpg_300x300.jpg) |
| Aqara Wireless Switch | ![](https://ae01.alicdn.com/kf/HTB17DMORVXXXXbdXFXXq6xXFXXXe/Xiaomi-Mijia-AQara-Smart-Multifunktionale-Intelligente-Drahtlose-Schalter-Schl-ssel-Kreiselkompa-Errichtet-Funktion-Arbeit-Mit-Android.jpg_300x300.jpg) |
| Aqara Wireless Switch (with acceleration sensor) | ![](https://ae01.alicdn.com/kf/HTB1YGiNaNsIL1JjSZFqq6AeCpXaX/Original-xiaomi-Mijia-aqara-wireless-key-Upgraded-with-acceleration-sensor-magic-Mi-cube-sensor-work-with.jpg_300x300.jpg) |
| Aqara Wall Switch (1 & 2 Button / With or Without Neutral Line) | ![](https://ae01.alicdn.com/kf/HTB1VGfGXL9TBuNjy1zbq6xpepXam/Original-Xiaomi-Aqara-Smart-Light-Control-Fire-Wire-Zero-Line-Double-Single-Key-ZiGBee-Wall-Switch.jpg_300x300.jpg) |
| Aqara Wireless Light Control (1 & 2 Button) | ![](https://ae01.alicdn.com/kf/HTB19u.tPVXXXXbbXVXXq6xXFXXXH/Original-Xiaomi-Aqara-Smart-Switch-Light-Control-ZiGBee-Wireless-Key-Wall-Switch-By-Smarphone-Mi-Home.jpg_300x300.jpg) |
| Aqara Curtain Motor | ![](https://ae01.alicdn.com/kf/HTB1jaMXQVXXXXXBXVXXq6xXFXXXF/Original-xiaomi-Aqara-Curtain-motor-Zigbee-wifi-Remote-Control-work-for-Xiaomi-Smart-home-kit-Mi.jpg_300x300.jpg) |
| Aqara Water Leak Sensor | ![](https://ae01.alicdn.com/kf/HTB1zWulSVXXXXaVXXXXq6xXFXXXW/2018-Newest-Xiaomi-Mijia-Aqara-Water-Immersing-Sensor-Flood-Water-Leak-Detector-for-Home-Remote-Alarm.jpg_300x300.jpg) |
| Honeywell Gas Detector | ![](https://ae01.alicdn.com/kf/HTB1F_ffQpXXXXaxXpXXq6xXFXXXS/Xiaomi-Mijia-Honeywell-Smart-Gas-Alarm-CH4-berwachung-Decke-Wand-Montiert-Einfach-Installieren-Typ-Mihome-APP.jpg_300x300.jpg) |
| Honeywell Smoke Detector | ![](https://ae01.alicdn.com/kf/HTB12DGKQpXXXXaeaXXXq6xXFXXXK/Xiaomi-Mijia-Honeywell-Smart-Fire-Alarm-Detector-Progressive-Sound-Photoelectric-Smoke-Sensor-Remote-Linkage-Mihome-APP.jpg_300x300.jpg) |
| Aqara Fingerprint & Keyless Card & PIN Lock | ![](https://ae01.alicdn.com/kf/HTB1lsuqjjuhSKJjSspaq6xFgFXaD/Original-xiaomi-Mijia-aqara-Smart-door-lock-Digital-Touch-Screen-Keyless-Fingerprint-Password-work-to-mi.jpg_300x300.jpg) |

## Setup

*   Install the binding
*   Is your gateway already configured to connect to your WiFi? If not:

    	1.  Install MiHome app from [Google Play](https://play.google.com/store/apps/details?id=com.xiaomi.smarthome) or [AppStore](https://itunes.apple.com/app/mi-home-xiaomi-for-your-smarthome/id957323480) (your phone may need to be changed to English language first)
    	2.  In the app create a Mi Home account and make sure to set your region to Mainland (China) under Settings -> Locale
    	3.  If asked, update your gateway to the latest firmware (note that update window may pop up sequentially)

*   Enable developer mode of your gateway:

        1.  Select your Gateway in the MiHome app
        2.  Go to the "..." menu on the top right corner and click "About"
        3.  Tap the version number "Plug-in version : 2.XX.X" at the bottom of the screen repeatedly until you enable developer mode
        4.  You should now have 2 extra options listed: `wireless communication protocol` and `hub info`
        5.  Choose `wireless communication protocol`
        6.  Tap the toggle switch to enable WiFi functions. Note down the developer key (aka password), something like: 91bg8zfkf9vd6uw7
        7.  Make sure you hit the OK button (to the right of the cancel button) to save your changes

*   In openHAB you should now be able to discover the Xiaomi Gateway
*   From now on you don't really need the app anymore. Only if you want to update the gateway firmware or if you want to add devices (see below). But adding devices can also be done without the app (see below)
*   Enter the previously noted developer key in openHAB Paper UI -> Configuration -> Things -> Xiaomi Gateway -> Edit -> Developer Key. Save (This is required if you want to be able to send controls to the devices like the light of the gateway)

## Connecting devices to the gateway

There are three ways of connecting supported devices to the gateway:

*   Online - within the MiHome App
*   Offline - manual

    1.  Click 3 times on the Gateway's button
    2.  Gateway will flash in blue and you will hear female voice in Chinese
    3.  Place the needle into the sensor and hold it for at least 3 seconds
    4.  You'll hear confirmation message in Chinese
    5.  The device appears in openHAB thing Inbox

__If you don't want to hear the Chinese voice every time, you can disable it by setting the volume to minimum in the MiHome App (same for the blinking light)__

* With the binding

    1.  after adding the gateway make sure you have entered the right developer key
    2.  setup a Switch Item for the channel `mihome:gateway:xxxxxxxxxxx:joinPermission` (see Examples) or control the switch via PaperUI
    3.  after switching you have 30 seconds to include your new device

__The devices don't need an Internet connection to be working after you have set up the developer mode BUT you won't be able to connect to them via App anymore - easiest way is to block their outgoing Internet connection in your router and enable it later, when you want to check for updates etc__

## Network configuration

- The binding requires port `9898` to not be used by any other service on the system.
- Make sure multicast traffic is correctly routed between the gateway and your openHAB instance

## Configuration examples

### xiaomi.things:

```
Bridge mihome:bridge:f0b429XXXXXX "Xiaomi Gateway" [ serialNumber="f0b429XXXXXX", ipAddress="192.168.0.3", port=9898, key="XXXXXXXXXXXXXXXX", pollingInterval=6000 ] {
  Things:
    gateway f0b429XXXXXX "Xiaomi Mi Smart Home Gateway" [itemId="f0b429XXXXXX"]
    sensor_ht 158d0001XXXXXX "Xiaomi Temperature Sensor" [itemId="158d0001XXXXXX"]
    sensor_weather_v1 158d0001XXXXXX "Xiaomi Aqara Temp, Hum and Press Sensor" [itemId="158d0001XXXXXX"]
    sensor_motion 158d0001XXXXXX "Xiaomi Motion Sensor" [itemId="158d0001XXXXXX"]
    sensor_plug 158d0001XXXXXX "Xiaomi Plug" [itemId="158d0001XXXXXX"]
    sensor_magnet 158d0001XXXXXX "Xiaomi Door Sensor" [itemId="158d0001XXXXXX"]
    sensor_switch 158d0001XXXXXX "Xiaomi Mi Wireless Switch" [itemId="158d0001XXXXXX"]
    86sw2 158d0001XXXXXX "Aqara Wireless Wall Switch" [itemId="158d0001XXXXXX"]
}
```

### xiaomi.items:

```
// Replace <GwID> with itemId of gateway from Things file
// Replace <ID> with itemId of item from Things file
// Gateway 
Switch Gateway_AddDevice { channel="mihome:gateway:<GwID>:<ID>:joinPermission" }
Switch Gateway_LightSwitch <light> { channel="mihome:gateway:<GwID>:<ID>:brightness" }
Dimmer Gateway_Brightness <dimmablelight> { channel="mihome:gateway:<GwID>:<ID>:brightness" }
Color Gateway_Color <rgb> { channel="mihome:gateway:<GwID>:<ID>:color" }
Dimmer Gateway_ColorTemperature <heating> { channel="mihome:gateway:<GwID>:<ID>:colorTemperature" }
Number Gateway_AmbientLight <sun> { channel="mihome:gateway:<GwID>:<ID>:illumination" }
Number Gateway_Sound <soundvolume-0> { channel="mihome:gateway:<GwID>:<ID>:sound" }
Switch Gateway_SoundSwitch <soundvolume_mute> { channel="mihome:gateway:<GwID>:<ID>:enableSound" }
Dimmer Gateway_SoundVolume <soundvolume> { channel="mihome:gateway:<GwID>:<ID>:volume" }

// Temperature and Humidity Sensor
Number:Temperature HT_Temperature <temperature> { channel="mihome:sensor_ht:<GwID>:<ID>:temperature" }
Number:Dimensionless HT_Humidity <humidity> { channel="mihome:sensor_ht:<GwID>:<ID>:humidity" }
Number HT_Battery <battery> { channel="mihome:sensor_ht:<GwID>:<ID>:batteryLevel" }
Switch HT_BatteryLow <energy> { channel="mihome:sensor_ht:<GwID>:<ID>:lowBattery" }

// Aqara Temperature, Humidity and pressure Sensor
Number:Temperature HTP_Temperature <temperature> { channel="mihome:sensor_weather_v1:<GwID>:<ID>:temperature" }
Number:Dimensionless HTP_Humidity <humidity> { channel="mihome:sensor_weather_v1:<GwID>:<ID>:humidity" }
Number:Pressure HTP_Pressure <pressure> { channel="mihome:sensor_weather_v1:<GwID>:<ID>:pressure" }
Number HTP_Battery <battery> { channel="mihome:sensor_weather_v1:<GwID>:<ID>:batteryLevel" }
Switch HTP_BatteryLow <energy> { channel="mihome:sensor_weather_v1:<GwID>:<ID>:lowBattery" }

// Mijia & Aqara Door/Window Sensor
Contact WindowSwitch_Status <window>  { channel="mihome:sensor_magnet:<GwID>:<ID>:isOpen" }
// minimum 30 seconds
Number WindowSwitch_AlarmTimer <clock> { channel="mihome:sensor_magnet:<GwID>:<ID>:isOpenAlarmTimer" }
DateTime WindowSwitch_LastOpened "[%1$tY-%1$tm-%1$td  %1$tH:%1$tM]" <clock-on> { channel="mihome:sensor_magnet:<GwID>:<ID>:lastOpened" }
Number WindowSwitch_Battery <battery> { channel="mihome:sensor_magnet:<GwID>:<ID>:batteryLevel" }
Switch WindowSwitch_BatteryLow <energy> { channel="mihome:sensor_magnet:<GwID>:<ID>:lowBattery" }

// Mijia Motion Sensor
Switch MotionSensor_MotionStatus <motion>  { channel="mihome:sensor_motion:<GwID>:<ID>:motion" }
// minimum 5 seconds - remember that the sensor only triggers every minute to save energy
Number MotionSensor_MotionTimer <clock> { channel="mihome:sensor_motion:<GwID>:<ID>:motionOffTimer" }
DateTime MotionSensor_LastMotion "[%1$tY-%1$tm-%1$td  %1$tH:%1$tM]" <clock-on> { channel="mihome:sensor_motion:<GwID>:<ID>:lastMotion" }
Number MotionSensor_Battery <battery> { channel="mihome:sensor_motion:<GwID>:<ID>:batteryLevel" }
Switch MotionSensor_BatteryLow <energy> { channel="mihome:sensor_motion:<GwID>:<ID>:lowBattery" }

// Aqara Motion Sensor
Switch MotionSensor_MotionStatus <motion>  { channel="mihome:sensor_motion_aq2:<GwID>:<ID>:motion" }
// minimum 5 seconds - the sensor only triggers once every minute to save energy
Number MotionSensor_MotionTimer <clock> { channel="mihome:sensor_motion_aq2:<GwID>:<ID>:motionOffTimer" }
DateTime MotionSensor_LastMotion "[%1$tY-%1$tm-%1$td  %1$tH:%1$tM]" <clock-on> { channel="mihome:sensor_motion_aq2:<GwID>:<ID>:lastMotion" }
Number MotionSensor_Battery <battery> { channel="mihome:sensor_motion_aq2:<GwID>:<ID>:batteryLevel" }
Switch MotionSensor_BatteryLow <energy> { channel="mihome:sensor_motion_aq2:<GwID>:<ID>:lowBattery" }
Number MotionSensor_Lux "LUX [%.1f]" <sun> { channel="mihome:sensor_motion_aq2:<GwID>:<ID>:illumination" }

// Smart Socket
Switch Plug_Switch <switch> { channel="mihome:sensor_plug:<GwID>:<ID>:power" }
Switch Plug_Active <switch> { channel="mihome:sensor_plug:<GwID>:<ID>:inUse" }
Number Plug_Power <energy> { channel="mihome:sensor_plug:<GwID>:<ID>:loadPower" }
Number Plug_Consumption <line-incline> { channel="mihome:sensor_plug:<GwID>:<ID>:powerConsumed" }

// Mijia & Aqara Cube Controller - see "xiaomi.rules" for action triggers
DateTime Cube_LastAction "[%1$tY-%1$tm-%1$td  %1$tH:%1$tM]" <clock-on> { channel="mihome:sensor_cube:<GwID>:<ID>:lastAction" }
Number:Angle Cube_RotationAngle { channel="mihome:sensor_cube:<GwID>:<ID>:rotationAngle" }
Number:Time Cube_RotationTime { channel="mihome:sensor_cube:<GwID>:<ID>:rotationTime" }
Number Cube_Battery <battery> { channel="mihome:sensor_cube:<GwID>:<ID>:batteryLevel" }
Switch Cube_BatteryLow <energy> { channel="mihome:sensor_cube:<GwID>:<ID>:lowBattery" }

// Aqara Smart Motion Sensor - see "xiaomi.rules" for action triggers
DateTime Vibration_LastAction "[%1$tY-%1$tm-%1$td  %1$tH:%1$tM]" <clock-on> { channel="mihome:sensor_vibration:<GwID>:<ID>:lastAction" }
Number Vibration_TiltAngle { channel="mihome:sensor_vibration:<GwID>:<ID>:tiltAngle" }
Number Vibration_CoordinateX { channel="mihome:sensor_vibration:<GwID>:<ID>:coordinateX" }
Number Vibration_CoordinateY { channel="mihome:sensor_vibration:<GwID>:<ID>:coordinateY" }
Number Vibration_CoordinateZ { channel="mihome:sensor_vibration:<GwID>:<ID>:coordinateZ" }
Number Vibration_BedActivity { channel="mihome:sensor_vibration:<GwID>:<ID>:bedActivity" }
Number Vibration_Battery <battery> { channel="mihome:sensor_vibration:<GwID>:<ID>:batteryLevel" }
Switch Vibration_BatteryLow <energy> { channel="mihome:sensor_vibration:<GwID>:<ID>:lowBattery" }

// Mijia & Aqara Wireless Switch - see "xiaomi.rules" for action triggers
Number Switch_Battery <battery> { channel="mihome:sensor_switch:<GwID>:<ID>:batteryLevel" }
Switch Switch_BatteryLow <energy> { channel="mihome:sensor_switch:<GwID>:<ID>:lowBattery" }

// Aqara Wirelss Light Control (1 Button) - see "xiaomi.rules" for action triggers
Number AqaraSwitch1_Battery <battery> { channel="mihome:86sw1:<GwID>:<ID>:batteryLevel" }
Switch AqaraSwitch1_BatteryLow <energy> { channel="mihome:86sw1:<GwID>:<ID>:lowBattery" }

// Aqara Wirelss Light Control (2 Button) - see "xiaomi.rules" for action triggers
Number AqaraSwitch2_Battery <battery> { channel="mihome:86sw2:<GwID>:<ID>:batteryLevel" }
Switch AqaraSwitch2_BatteryLow <energy> { channel="mihome:86sw2:<GwID>:<ID>:lowBattery" }

// Aqara Wall Switch (1 Button)
Switch AqaraWallSwitch <switch> { channel="mihome:ctrl_neutral1:<GwID>:<ID>:ch1" }

// Aqara Wall Switch (2 Button)
Switch AqaraWallSwitch1 <switch> { channel="mihome:ctrl_neutral2:<GwID>:<ID>:ch1" }
Switch AqaraWallSwitch2 <switch> { channel="mihome:ctrl_neutral2:<GwID>:<ID>:ch2" }

// Aqara Wall Switch (1 Button & without neutral line)
Switch AqaraWallSwitch <switch> { channel="mihome:ctrl_ln1:<GwID>:<ID>:ch1" }

// Aqara Wall Switch (2 Button & without neutral line)
Switch AqaraWallSwitch1 <switch> { channel="mihome:ctrl_ln2:<GwID>:<ID>:ch1" }
Switch AqaraWallSwitch2 <switch> { channel="mihome:ctrl_ln2:<GwID>:<ID>:ch2" }

// Aqara Curtain Motor
Rollershutter CurtainMotorControl <blinds> { channel="curtain:<GwID>:<ID>:curtainControl" }
```

### xiaomi.rules:

```
rule "Mijia & Aqara Wireless Switch"
when
    Channel "mihome:sensor_switch:<GwID>:<ID>:button" triggered
then
    var actionName = receivedEvent.getEvent()
    switch(actionName) {
        case "SHORT_PRESSED": {
            <ACTION>
        }
        case "DOUBLE_PRESSED": {
            <ACTION>
        }
        case "LONG_PRESSED": {
            <ACTION>
        }
        case "LONG_RELEASED": {
            <ACTION>
        }
    }
end

rule "Mijia & Aqara Cube Controller"
when
    Channel 'mihome:sensor_cube:<GwID>:<ID>:action' triggered
then
    var actionName = receivedEvent.getEvent()
    switch(actionName) {
        case "MOVE": {
            <ACTION>
        }
        case "ROTATE_RIGHT": {
            <ACTION>
        }
        case "ROTATE_LEFT": {
            <ACTION>
        }
        case "FLIP90": {
            <ACTION>
        }
        case "FLIP180": {
            <ACTION>
        }
        case "TAP_TWICE": {
            <ACTION>
        }
        case "SHAKE_AIR": {
            <ACTION>
        }
        case "FREE_FALL": {
            <ACTION>
        }
        case "ALERT": {
            <ACTION>
        }
    }
end

rule "Aqara Smart Motion Sensor"
when
    Channel 'mihome:sensor_vibration:<GwID>:<ID>:action' triggered
then
    var actionName = receivedEvent.getEvent()
    switch(actionName) {
        case "VIBRATE": {
            <ACTION>
        }
        case "TILT": {
            <ACTION>
        }
        case "FREE_FALL": {
            <ACTION>
        }
    }
end


rule "Mijia & Aqara Motion Sensor"
when
    Item MotionSensor_MotionStatus changed
then
    if (MotionSensor_MotionStatus.state == ON) {
        <ACTION>
    } else {
        <ACTION>
    }
end

rule "Mijia & Aqara Door/Window Sensor"
when
    Item WindowSwitch_Status changed
then
    if (WindowSwitch_Status.state == OPEN) {
        <ACTION>
    } else {
        <ACTION>
    }
end

rule "Mijia & Aqara Door/Window Sensor - Window is open for longer than WindowSwitch_AlarmTimer"
when
    Channel "mihome:sensor_magnet:<GwID>:<ID>:isOpenAlarm" triggered ALARM
then
    <ACTION>
end

rule "Aqara Wirelss Light Control (1 Button)"
when
    Channel "mihome:86sw1:<GwID>:<ID>:ch1" triggered SHORT_PRESSED
then
    <ACTION>
end

rule "Aqara Wirelss Light Control (2 Button)"
when
    Channel "mihome:86sw2:<GwID>:<ID>:ch1" triggered SHORT_PRESSED
then
    <ACTION>
end

rule "Aqara Wirelss Light Control (2 Button)"
when
    Channel "mihome:86sw2:<GwID>:<ID>:ch2" triggered SHORT_PRESSED
then
    <ACTION>
end

rule "Aqara Wirelss Light Control (2 Button)"
when
    Channel "mihome:86sw2:<GwID>:<ID>:dual_ch" triggered SHORT_PRESSED
then
    <ACTION>
end

// This rule is applicable for every battery powered sensor device
rule "Xiaomi Motion Sensor Low Battery"
when
    Item MotionSensor_BatteryLow changed to ON
then
    <ACTION>
end

rule "Play quiet knock-knock ringtone with the Xiaomi Gateway"
when
    // Item ExampleSwitch changed to ON
then
    sendCommand(Gateway_SoundVolume, 2)
    sendCommand(Gateway_Sound, 11)
    Thread::sleep(2000) /* wait for 2 seconds */
    sendCommand(Gateway_Sound, 10000)
    sendCommand(Gateway_SoundVolume, 0)
end
```

### xiaomi.sitemap:

```
sitemap xiaomi label="Xiaomi" {
    // Example for selection of predefined sound file - you can also upload your own files with the official MiHome App and play them!
	Frame {
        ...

        // Selection for Xiaomi Gateway Sounds
        // 10000 is STOP
        // >10001 are own sounds you uploaded to the gateway
        Selection item=Gateway_Sound mappings=[ 0="police car 1",
                                                1="police car 2",
                                                2="accident",
                                                3="countdown",
                                                4="ghost",
                                                5="sniper rifle",
                                                6="battle",
                                                7="air raid",
                                                8="bark",
                                                10="doorbell",
                                                11="knock at a door",
                                                12="amuse",
                                                13="alarm clock",
                                                20="mimix",
                                                21="enthusuastic",
                                                22="guitar classic",
                                                23="ice world piano",
                                                24="leisure time",
                                                25="child hood",
                                                26="morning stream liet",
                                                27="music box",
                                                28="orange",
                                                29="thinker"]
    }
}
```

## Supporting new devices

The Xiaomi ecosystem grows at a steady rate. So there is a good chance that in the future even more devices get added to the suite. This section describes, how to get the necessary information to support new device types.

### Preconditions

- you know how to access the [openHAB Console](https://www.openhab.org/docs/administration/console.html)
- you have connected your gateway to openHAB and the communication is working

### Enable debug logging for the binding

- Enter ```log:set TRACE org.openhab.binding.mihome``` in the console to enable full logs.

    _When you are done you can disable the extended logging with ```log:set DEFAULT org.openhab.binding.mihome```_

- Exit the console and start [viewing the logs](https://www.openhab.org/docs/tutorial/logs.html).

### Connect the new device

- Go through the normal procedure to add a device to the gateway
- The device won't show up in your inbox, but it will send messages to the gateway which you can see in the logs
- Analyse the logs and find the model name of the new device

_Example: Aqara Vibration Sensor_

    2018-09-08 00:58:14.903 [DEBUG] [discovery.XiaomiItemDiscoveryService] - Unknown discovered model: vibration

### Gather information about the new device

The devices send different types of messages to the gateway.
You have to capture as many of them as possible, so that the device is fully supported in the end.

1. Heartbeat (transmitted usually every 60 minutes)
2. Report (device reports new values)
3. Read ACK (binding refreshes all sensor values after a restart of openHAB)
4. Write ACK (device has received a command) __not avaiable for sensor devices__

___You can filter the log to show only relevant information, just replace {deviceName} with the model name of the new device___

    tail -f /var/log/openhab2/openhab.log -f /var/log/openhab2/events.log | grep {deviceName}

_Example: Aqara Vibration Sensor_

```
user@computer:~$ tail -f /var/log/openhab2/openhab.log -f /var/log/openhab2/events.log | grep vibration
2018-09-08 00:58:14.878 [TRACE] [g.mihome.handler.XiaomiBridgeHandler] - Received message {"cmd":"report","model":"vibration","sid":"158d0002a92499","short_id":2226,"data":"{\"status\":\"vibrate\"}"}
2018-09-08 00:58:14.903 [DEBUG] [discovery.XiaomiItemDiscoveryService] - Unknown discovered model: vibration
2018-09-08 01:00:11.952 [TRACE] [g.mihome.handler.XiaomiBridgeHandler] - Received message {"cmd":"report","model":"vibration","sid":"158d0002a92499","short_id":2226,"data":"{\"status\":\"vibrate\"}"}
2018-09-08 01:00:11.984 [DEBUG] [discovery.XiaomiItemDiscoveryService] - Unknown discovered model: vibration
2018-09-08 01:00:13.064 [TRACE] [g.mihome.handler.XiaomiBridgeHandler] - Received message {"cmd":"report","model":"vibration","sid":"158d0002a92499","short_id":2226,"data":"{\"status\":\"tilt\"}"}
2018-09-08 01:00:13.073 [DEBUG] [discovery.XiaomiItemDiscoveryService] - Unknown discovered model: vibration
2018-09-08 01:00:13.089 [TRACE] [g.mihome.handler.XiaomiBridgeHandler] - Received message {"cmd":"report","model":"vibration","sid":"158d0002a92499","short_id":2226,"data":"{\"final_tilt_angle\":\"71\"}"}
2018-09-08 01:00:13.105 [DEBUG] [discovery.XiaomiItemDiscoveryService] - Unknown discovered model: vibration
2018-09-08 01:00:15.131 [TRACE] [g.mihome.handler.XiaomiBridgeHandler] - Received message {"cmd":"report","model":"vibration","sid":"158d0002a92499","short_id":2226,"data":"{\"coordination\":\"71,1080,286\"}"}
2018-09-08 01:00:15.168 [DEBUG] [discovery.XiaomiItemDiscoveryService] - Unknown discovered model: vibration
2018-09-08 01:00:18.179 [TRACE] [g.mihome.handler.XiaomiBridgeHandler] - Received message {"cmd":"report","model":"vibration","sid":"158d0002a92499","short_id":2226,"data":"{\"status\":\"tilt\"}"}
2018-09-08 01:00:18.187 [DEBUG] [discovery.XiaomiItemDiscoveryService] - Unknown discovered model: vibration
2018-09-08 01:00:19.216 [TRACE] [g.mihome.handler.XiaomiBridgeHandler] - Received message {"cmd":"report","model":"vibration","sid":"158d0002a92499","short_id":2226,"data":"{\"coordination\":\"10,84,1248\"}"}
2018-09-08 01:00:19.247 [DEBUG] [discovery.XiaomiItemDiscoveryService] - Unknown discovered model: vibration
2018-09-08 01:00:21.468 [TRACE] [g.mihome.handler.XiaomiBridgeHandler] - Received message {"cmd":"report","model":"vibration","sid":"158d0002a92499","short_id":2226,"data":"{\"status\":\"free_fall\"}"}
2018-09-08 01:00:21.502 [DEBUG] [discovery.XiaomiItemDiscoveryService] - Unknown discovered model: vibration
2018-09-08 01:00:22.494 [TRACE] [g.mihome.handler.XiaomiBridgeHandler] - Received message {"cmd":"report","model":"vibration","sid":"158d0002a92499","short_id":2226,"data":"{\"coordination\":\"87,51,1247\"}"}
2018-09-08 01:00:22.527 [DEBUG] [discovery.XiaomiItemDiscoveryService] - Unknown discovered model: vibration
2018-09-08 01:00:26.587 [TRACE] [g.mihome.handler.XiaomiBridgeHandler] - Received message {"cmd":"report","model":"vibration","sid":"158d0002a92499","short_id":2226,"data":"{\"final_tilt_angle\":\"19\"}"}
2018-09-08 01:00:26.619 [DEBUG] [discovery.XiaomiItemDiscoveryService] - Unknown discovered model: vibration
2018-09-08 01:00:28.630 [TRACE] [g.mihome.handler.XiaomiBridgeHandler] - Received message {"cmd":"report","model":"vibration","sid":"158d0002a92499","short_id":2226,"data":"{\"coordination\":\"475,119,1147\"}"}
2018-09-08 01:00:28.660 [DEBUG] [discovery.XiaomiItemDiscoveryService] - Unknown discovered model: vibration
2018-09-08 01:05:30.418 [TRACE] [g.mihome.handler.XiaomiBridgeHandler] - Received message {"cmd":"report","model":"vibration","sid":"158d0002a92499","short_id":2226,"data":"{\"bed_activity\":\"158\"}"}
2018-09-08 01:05:30.441 [DEBUG] [discovery.XiaomiItemDiscoveryService] - Unknown discovered model: vibration
```

### Open a new issue or get your hands dirty

Every little help is welcome, be part of the community!
