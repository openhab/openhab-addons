#Russound Serial Binding

This binding allows you to control a [Russound MCA/CAA](https://www.russound.com/products/audio-systems/multi-room-controllers/caa66-system-and-kits/caa66-controller-amplifier) system over a
serial connection. There is an existing [Russound plugin](https://github.com/openhab/openhab2-addons/tree/master/addons/binding/org.openhab.binding.russound), but that works over a network connection on a different protocol (RIO).

This binding only only works on a device that supports communicating over a serial port via  a file, so OSX/Linux will work but Windows will not.

This plugin uses a [kotlin library]([https://github.com/holmes/russound) to communicate via the serial protocol.

## Supported Things

There are 2 supported Thing(s):
1. Bridge: This is your connection to the controller. You'll specify the serial port once per bridge.
1. Zones: You'll have a 1:1 mapping of a Zone Thing to a physical zone on your Russound device.

## Discovery and Thing Configuration

There is no auto-discovery.

#### Bridge
You must specify the serial port the Bridge is connected to when setting up the Bridge Thing.

#### Zones
You must specify the zone number (0-based) this Thing is targeting. This maps directly to the number on the back of the device.

## Channels

Name       | Type | Description
---|---|---
zonePower | Switch | Whether the zone is ON or OFF
zoneVolume | Dimmer | Volume level of zone
zoneSource | Number | The source that's currently playing (0-based)
zoneBass | Number | Bass Setting (-10 to 10)
zoneTreble | Number | Treble Setting (-10 to 10)
zoneBalance | Number | Balance Setting (-10 full left to 10 full right)
zoneLoudness | Switch | Whether the loudness setting is on or off
zoneTurnOnVolume | Dimmer | This is a write only setting! Determines the volume when the zone is turned on

## Full Example

Sample .things file
```
Bridge russoundserial:russound-serial-bridge:CAA-66 "Russound CAA-66" [ serialPort = "/dev/ttyUSB0" ] {
    Thing zone zone0 "Family Room Speakers" @ "Family Room" [ zoneNumber = 0 ]
    Thing zone zone1 "Kitchen Speakers" @ "Kitchen" [ zoneNumber = 1 ]
    Thing zone zone2 "Backyard Speakers" @ "Backyard" [ zoneNumber = 2 ]
    Thing zone zone3 "Master Bedroom Speakers" @ "Master Bedroom" [ zoneNumber = 3 ]
}
```

Sample .items file
```
Switch family_room_audio_power "Power" { channel="russoundserial:zone:CAA-66:zone0:power" }
Dimmer family_room_audio_volume "Volume [%d]" { channel="russoundserial:zone:CAA-66:zone0:volume" }
Number family_room_audio_source "Source" { channel="russoundserial:zone:CAA-66:zone0:source" }

Switch kitchen_audio_power "Power" { channel="russoundserial:zone:CAA-66:zone1:power" }
Dimmer kitchen_audio_volume "Volume" { channel="russoundserial:zone:CAA-66:zone1:volume" }
Number kitchen_audio_source "Source" { channel="russoundserial:zone:CAA-66:zone1:source" }

Switch backyard_audio_power "Power" { channel="russoundserial:zone:CAA-66:zone2:power" }
Dimmer backyard_audio_volume "Volume" { channel="russoundserial:zone:CAA-66:zone2:volume" }
Number backyard_audio_source "Source" { channel="russoundserial:zone:CAA-66:zone2:source" }

Switch master_bedroom_audio_power "Power" { channel="russoundserial:zone:CAA-66:zone3:power" }
Dimmer master_bedroom_audio_volume "Volume" { channel="russoundserial:zone:CAA-66:zone3:volume" }
Number master_bedroom_audio_source "Source" { channel="russoundserial:zone:CAA-66:zone3:source" }

```

Sample .rules for controlling
```
kitchen_audio_power.sendCommand(ON)
```

Sample .sitemap
```
sitemap audio label="Audio Control" {
  Frame label="Family Room" {
    Switch item=family_room_audio_power label="Power"
    Setpoint item=family_room_audio_volume label="Volume [%d]" minValue=0 maxValue=100 step=2
    Selection item=family_room_audio_source label="Source:" mappings=[0="Family Room TV", 1="Chromecast"]
  }

  Frame label="Kitchen" {
    Switch item=kitchen_audio_power label="Power:"
    Setpoint item=kitchen_audio_volume label="Volume: [%d]" minValue=0 maxValue=100 step=2
    Selection item=kitchen_audio_source label="Source:" mappings=[0="Family Room TV", 1="Chromecast"]
  }

  Frame label="Back Yard" {
    Switch item=backyard_audio_power label="Power:"
    Slider item=backyard_audio_volume label="Volume: [%d]"
    Selection item=backyard_audio_source label="Source:" mappings=[0="Family Room TV", 1="Chromecast"]
  }

  Frame label="Master Bedroom" {
    Switch item=master_bedroom_audio_power label="Power:"
    Setpoint item=master_bedroom_audio_volume label="Volume: [%d]" minValue=0 maxValue=100 step=2
    Selection item=master_bedroom_audio_source label="Source:" mappings=[0="Family Room TV", 1="Chromecast"]
  }
}

```


## Kotlin

Kotlin is a pretty cool language and the core library that this plugin uses is based on it. Included are OSGI'd versions of the kotlin library for Java 8.
