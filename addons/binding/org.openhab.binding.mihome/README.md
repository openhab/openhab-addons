# Xiaomi Mi Smart Home Binding

This binding allows your openHAB to communicate with the Xiaomi Smart Home Suite.

In order to connect the Gateway, you need to install the MiHome app
from the [Google Play](https://play.google.com/store/apps/details?id=com.xiaomi.smarthome) or [AppStore](https://itunes.apple.com/app/mi-home-xiaomi-for-your-smarthome/id957323480).

## Supported devices

*   Xiaomi Smart Gateway v2 (with radio support)
*   Xiaomi Smart Temperature and Humidity Sensor (round one)
*   Xiaomi Smart Door/Window Sensor (round one)
*   Xiaomi Wireless Switch (round one)
*   Xiaomi Motion Sensor / IR Human Body sensor
*   Xiaomi Smart Plug
*   Xiaomi Smart Magic Cube
*   Xiaomi Aqara ZigBee Wired Wall Switch (1 and 2 buttons)
*   Xiaomi Aqara ZigBee Wireless Wall Switch (1 and 2 buttons)
*   Xiaomi Aqara Smart Curtain
*   Xiaomi Aqara Water Leak Sensor
*   Xiaomi Aqara Wireless Switch (square one)
*   Xiaomi Aqara Temperature, Humidity and Pressure Sensor (square one)
*   Xiaomi Aqara Door/Window Sensor (square one)
*   Xiaomi Aqara Motion Sensor (with light intensity support)
*   Xiaomi Mijia Honeywell Gas Alarm Detector
*   Xiaomi Mijia Honeywell Fire Alarm Detector

(not yet confirmed)

*   Xiaomi Aqara Neutral Wall Switch (1 and 2 buttons)

## Setup

*   Install the binding
*   Setup Gateway to be discoverable

    1.  Add Gateway 2 or 3 to your WiFi Network
    2.  Install MiHome app from [Google Play](https://play.google.com/store/apps/details?id=com.xiaomi.smarthome) or [AppStore](https://itunes.apple.com/app/mi-home-xiaomi-for-your-smarthome/id957323480) (your phone may need to be changed to English language first)
    3.  Set your region to Mainland China under Settings -> Locale (seems to be required)
    4.  Update gateway to the latest firmware (note that update window may pop up sequentially)
    5.  Enable developer mode:

        1.  Select your Gateway in the MiHome app
        2.  Go to the "..." menu on the top right corner and click "About"
        3.  Tap the version number "Version : 2.XX" at the bottom of the screen repeatedly until you enable developer mode
        4.  You should now have 2 extra options listed: `local area network communication protocol` and `gateway information`
        5.  Choose `local area network communication protocol`
        6.  Tap the toggle switch to enable LAN functions. Note down the developer key (something like: 91bg8zfkf9vd6uw7)
        7.  Make sure you hit the OK button (to the right of the cancel button) to save your changes

*   In openHAB you should now be able to discover the Xiaomi Gateway
*   From now on you don't really need the app anymore - only if you're keen on updates or you want to add devices (see below), which also can be done without the app
*   Enter the previously noted developer key in openHAB Paper UI -> Configuration -> Things -> Xiaomi Gateway -> Edit -> Developer Key. Save
    (This is required if you want to be able to send controls to the devices like the light of the gateway)
*   Your sensors should be getting discovered by openHAB as you add and use them

## Connecting sub-devices (sensors) to the Gateway

There are two ways of connecting Xiaomi devices to the gateway:

*   Online - within the MiHome App
*   Offline - manual

    1.  Click 3 times on the Gateway's button
    2.  Gateway will flash in blue and you will hear female voice in Chinese
    3.  Place the needle into the sensor and hold it for at least 3 seconds
    4.  You'll hear confirmation message in Chinese
    5.  The device appears in openHAB thing Inbox

*   If you don't want to hear the Chinese voice every time, you can disable it by setting the volume to minimum in the MiHome App (same for the blinking light)
*   The devices don't need an Internet connection to be working after you have set up the developer mode BUT you won't be able to connect to them via App anymore - easiest way is to block their outgoing Internet connection in your router and enable it later, when you want to check for updates etc

## Important information

The binding requires port `9898` to not be used by any other service on the system.

## Full Example

### xiaomi.things:

```
Bridge mihome:bridge:f0b429XXXXXX "Xiaomi Gateway" [ serialNumber="f0b429XXXXXX", ipAddress="192.168.0.3", port=9898, key="XXXXXXXXXXXXXXXX", pollingInterval=6000 ] {
    Thing mihome:gateway:f0b429XXXXXX "Xiaomi Mi Smart Home Gateway" [itemId="f0b429XXXXXX"]

    Thing mihome:sensor_ht:158d0001XXXXXX "Xiaomi Temperature Sensor" [itemId="158d0001XXXXXX"]

    Thing mihome:sensor_motion:158d0001XXXXXX "Xiaomi Motion Sensor" [itemId="158d0001XXXXXX"]

    Thing mihome:sensor_plug:158d0001XXXXXX "Xiaomi Plug" [itemId="158d0001XXXXXX"]

    Thing mihome:sensor_magnet:158d0001XXXXXX "Xiaomi Door Sensor" [itemId="158d0001XXXXXX"]

    Thing mihome:sensor_switch:158d0001XXXXXX "Xiaomi Mi Wireless Switch" [itemId="158d0001XXXXXX"]

    Thing mihome:86sw2:158d0001XXXXXX "Aqara Wireless Wall Switch" [itemId="158d0001XXXXXX"]
}
```

### xiaomi.items:

```
// Xiaomi Gateway
Switch Gateway_LightSwitch <light> { channel="mihome:gateway:<ID>:brightness" }
Dimmer Gateway_Brightness <dimmablelight> { channel="mihome:gateway:<ID>:brightness" }
Color Gateway_Color <rgb> { channel="mihome:gateway:<ID>:color" }
Dimmer Gateway_ColorTemperature <heating> { channel="mihome:gateway:<ID>:colorTemperature" }
Number Gateway_AmbientLight <sun> { channel="mihome:gateway:<ID>:illumination" }
Number Gateway_Sound <soundvolume-0> { channel="mihome:gateway:<ID>:sound" }
Switch Gateway_SoundSwitch <soundvolume_mute> { channel="mihome:gateway:<ID>:enableSound" }
Dimmer Gateway_SoundVolume <soundvolume> { channel="mihome:gateway:<ID>:volume" }

// Xiaomi Temperature and Humidity Sensor
Number HT_Temperature <temperature> { channel="mihome:sensor_ht:<ID>:temperature" }
Number HT_Humidity <humidity> { channel="mihome:sensor_ht:<ID>:humidity" }
Number HT_Battery <battery> { channel="mihome:sensor_ht:<ID>:batteryLevel" }
Switch HT_BatteryLow <energy> { channel="mihome:sensor_ht:<ID>:lowBattery" }

// Xiaomi Motion Sensor
Switch MotionSensor_MotionStatus <motion>  { channel="mihome:sensor_motion:<ID>:motion" }
// minimum 5 seconds - remember that the sensor only triggers every minute to save energy
Number MotionSensor_MotionTimer <clock> { channel="mihome:sensor_motion:<ID>:motionOffTimer" }
DateTime MotionSensor_LastMotion "[%1$tY-%1$tm-%1$td  %1$tH:%1$tM]" <clock-on> { channel="mihome:sensor_motion:<ID>:lastMotion" }
Number MotionSensor_Battery <battery> { channel="mihome:sensor_motion:<ID>:batteryLevel" }
Switch MotionSensor_BatteryLow <energy> { channel="mihome:sensor_motion:<ID>:lowBattery" }

// Xiaomi Plug
Switch Plug_Switch <switch> { channel="mihome:sensor_plug:<ID>:power" }
Switch Plug_Active <switch> { channel="mihome:sensor_plug:<ID>:inUse" }
Number Plug_Power <energy> { channel="mihome:sensor_plug:<ID>:loadPower" }
Number Plug_Consumption <line-incline> { channel="mihome:sensor_plug:<ID>:powerConsumed" }

// Xiaomi Window Switch
Contact WindowSwitch_Status <window>  { channel="mihome:sensor_magnet:<ID>:isOpen" }
// minimum 30 seconds
Number WindowSwitch_AlarmTimer <clock> { channel="mihome:sensor_magnet:<ID>:isOpenAlarmTimer" }
DateTime WindowSwitch_LastOpened "[%1$tY-%1$tm-%1$td  %1$tH:%1$tM]" <clock-on> { channel="mihome:sensor_magnet:<ID>:lastOpened" }
Number WindowSwitch_Battery <battery> { channel="mihome:sensor_magnet:<ID>:batteryLevel" }
Switch WindowSwitch_BatteryLow <energy> { channel="mihome:sensor_magnet:<ID>:lowBattery" }

// Xiaomi Cube - see "xiaomi.rules" for action triggers
Number Cube_RotationAngle { channel="mihome:sensor_cube:<ID>:rotationAngle" }
Number Cube_RotationTime { channel="mihome:sensor_cube:<ID>:rotationTime" }
Number Cube_Battery <battery> { channel="mihome:sensor_cube:<ID>:batteryLevel" }
Switch Cube_BatteryLow <energy> { channel="mihome:sensor_cube:<ID>:lowBattery" }

// Xiaomi Switch - see "xiaomi.rules" for action triggers
Number Switch_Battery <battery> { channel="mihome:sensor_switch:<ID>:batteryLevel" }
Switch Switch_BatteryLow <energy> { channel="mihome:sensor_switch:<ID>:lowBattery" }

// Xiaomi Aqara Battery Powered Switch 1- see "xiaomi.rules" for action triggers
Number AqaraSwitch1_Battery <battery> { channel="mihome:86sw1:<ID>:batteryLevel" }
Switch AqaraSwitch1_BatteryLow <energy> { channel="mihome:86sw1:<ID>:lowBattery" }

// Xiaomi Aqara Battery Powered Switch 2- see "xiaomi.rules" for action triggers
Number AqaraSwitch2_Battery <battery> { channel="mihome:86sw2:<ID>:batteryLevel" }
Switch AqaraSwitch2_BatteryLow <energy> { channel="mihome:86sw2:<ID>:lowBattery" }

// Xiaomi Aqara Mains Powered Wall Switch 1
Switch AqaraWallSwitch <switch> { channel="mihome:ctrl_neutral1:<ID>:ch1" }

// Xiaomi Aqara Mains Powered Wall Switch 2
Switch AqaraWallSwitch1 <switch> { channel="mihome:ctrl_neutral2:<ID>:ch1" }
Switch AqaraWallSwitch2 <switch> { channel="mihome:ctrl_neutral2:<ID>:ch2" }

// Xiaomi Aqara Intelligent Curtain Motor
Rollershutter CurtainMotorControl <blinds> { channel="curtain:<ID>:curtainControl" }
```

### xiaomi.rules:

```
rule "Xiaomi Switch"
when
    Channel "mihome:sensor_switch:<ID>:button" triggered
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

rule "Xiaomi Cube"
when
    Channel 'mihome:sensor_cube:<ID>:action' triggered
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

rule "Xiaomi Motion Sensor"
when
    Item MotionSensor_MotionStatus changed
then
    if (MotionSensor_MotionStatus.state == ON) {
        <ACTION>
    } else {
        <ACTION>
    }
end

rule "Xiaomi Window Switch"
when
    Item WindowSwitch_Status changed
then
    if (WindowSwitch_Status.state == OPEN) {
        <ACTION>
    } else {
        <ACTION>
    }
end

rule "Xiaomi Window Switch - Window is open alarm"
when
    Channel "mihome:sensor_magnet:<ID>:isOpenAlarm" triggered ALARM
then
    <ACTION>
end

rule "Xiaomi Aqara Battery Powered 1 Button Switch"
when
    Channel "mihome:86sw1:<ID>:ch1" triggered SHORT_PRESSED
then
    <ACTION>
end

rule "Xiaomi Aqara Battery Powered 2 Button Switch"
when
    Channel "mihome:86sw2:<ID>:ch1" triggered SHORT_PRESSED
then
    <ACTION>
end

rule "Xiaomi Aqara Battery Powered 2 Button Switch"
when
    Channel "mihome:86sw2:<ID>:ch2" triggered SHORT_PRESSED
then
    <ACTION>
end

rule "Xiaomi Aqara Battery Powered 2 Button Switch"
when
    Channel "mihome:86sw2:<ID>:dual_ch" triggered SHORT_PRESSED
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
