# EnOcean Binding

The EnOcean binding connects openHAB to the EnOcean ecosystem.

The binding uses an EnOcean gateway to retrieve sensor data and control actuators.
For _bidirectional_ actuators it is even possible to update the openHAB item state if the actuator gets modified outside of openHAB.
This binding has been developed on an USB300 gateway and was also tested with an EnOceanPi.
As this binding implements a full EnOcean stack, we have full control over these gateways.
This binding can enable the repeater function (level 1 or 2) of these gateways and retrieve detailed information about them.

## Concepts/Configuration

First of all you have to configure an EnOcean transceiver (gateway).
A directly connected USB300 can be auto discovered, an EnOceanPi has to be added manually to openHAB.
Both gateways are represented by an _EnOcean gateway_ in openHAB.
If you want to place the gateway for better reception apart from your openHAB server, you can forward its serial messages over TCP/IP (_ser2net_). In this case you have to define the path to the gateway like this rfc2217://x.x.x.x:3001.
If everything is running fine you should see the _base id_ of your gateway in the properties of your bridge.

The vast majority of EnOcean messages are sent as broadcast messages without an explicit receiver address.
However each EnOcean device is identified by an unique id, called EnOceanId, which is used as the sender address in these messages.
To receive messages from an EnOcean device you have to determine its EnOceanId and add an appropriate thing to openHAB.

If the device is an actuator which you want to control with your gateway from openHAB, you also have to create an unique sender id and announce it to the actuator (_teach-in_).
For security reasons you cannot choose a random id, instead each gateway has 127 unique ids build in, from which you can choose.
A SenderId of your gateway is made up its base id and a number between 1 and 127.
This number can be chosen manually or the next free/unused number can be determined by the binding.

## Supported Things

This binding is developed on and tested with the following devices

 * USB300 and EnOceanPi gateways
 * The following Eltako actuators:
    * FSR14 (light switch)
    * FSB14 (rollershutter)
    * FUD14 (dimmer)
    * FSSA-230V (smart plug)
    * FWZ12-65A (energy meter)
    * FTKE (window / door contact)
    * TF-FGB (window handle)
    * TF-FKB (window contact)
    * TF-AHDSB (outdoor brightness sensor)
    * FAFT60 (outdoor temperature & humidity sensor)
 * The following Opus actuators:
    * GN-A-R12V-SR-4 (light)
    * GN-A-R12V-MF-2 (light)
    * GN-A-R12V-LZ/UD (dimmer)
    * GN-A-R12V-JRG-2 (rollershutter)
    * GN-A-U230V-JRG (rollershutter)
    * OPUS-FUNK PLUS Jalousieaktor 1fach UP (rollershutter)
    * OPUS-Funk PLUS Steckdosenleiste (smart multiple socket)
 * NodOn: 
    * Smart Plug (ASP-2-1-10)
    * In Wall Switch (SIN-2-2-00, SIN-2-1-0x)
    * Temperature & humidity sensor (STPH-2-1-05)
 * Permundo 
    * PSC234 (smart plug with metering) = Afriso APR234
    * PSC132 (smart switch actor with metering)
    * PSC152 (smart blinds control)
 * Thermokon SR04 room control
 * Hoppe SecuSignal window handles
 * Rocker switches (NodOn, Eltako FT55 etc)

However, because of the standardized EnOcean protocol it is more important which EEP this binding supports.
Hence if your device supports one of the following EEPs the chances are good that your device is also supported by this binding.

|Thing type                       | EEP family  | EEP Types     | Channels¹                    |  Devices²                      | Pairing   |
|---------------------------------|-------------|---------------|------------------------------|--------------------------------|-----------|
| bridge                          | -           | -             | repeaterMode, setBaseId      | USB300, EnOceanPi              | -         |
| pushButton                      | F6-01       | 0x01          | pushButton                   |                                | Manually  |
| rockerSwitch                    | F6-02       | 0x01-02       | rockerswitchA, rockerswitchB | Eltako FT55                    | Discovery |
| mechanicalHandle                | F6-10       | 0x00-01       | windowHandleState, contact   | Hoppe SecuSignal handles, Eltako TF-FGB | Discovery |
| contact                         | D5-00       | 0x01          | contact                      | Eltako FTK(E) & TF-FKB            | Discovery |
| temperatureSensor               | A5-02       | 0x01-30       | temperature                  | Thermokon SR65                 | Discovery |
| temperatureHumiditySensor       | A5-04       | 0x01-03       | humidity, temperature        | Eltako FTSB                    | Discovery |
| occupancySensor                 | A5-07       | 0x01-03       | illumination, batteryVoltage,<br/>motionDetection | NodON PIR-2-1-01 | Discovery |
| lightTemperatureOccupancySensor | A5-08       | 0x01-03       | illumination, temperature,<br/>occupancy, motionDetection | Eltako FABH | Discovery |
| lightSensor                     | A5-06       | 0x01          | illumination                 | Eltako TF-AHDSB                | Discovery |
| roomOperatingPanel              | A5-10       | 0x01-23       | temperature, setPoint, fanSpeedStage,<br/>occupancy           | Thermokon SR04 | Discovery |
| automatedMeterSensor            | A5-12       | 0x00-03       | counter, currentNumber, instantpower,<br/>totalusage, amrLitre, amrCubicMetre | FWZ12 | Discovery |
| centralCommand                  | A5-38       | 0x08          | dimmer, generalSwitch        | Eltako FUD14, FSR14            | Teach-in |
| rollershutter                   | A5-3F/D2-05/A5-38 | 0x7F/00/08 | rollershutter             | Eltako FSB14, NodOn SIN-2-RS-01| Teach-in/Discovery |
| measurementSwitch               | D2-01       | 0x00-0F,11,12 | generalSwitch(/A/B), instantpower,<br/>totalusage, repeaterMode | NodOn In Wall Switch | Discovery |
| classicDevice                   | F6-02       | 0x01-02       | virtualRockerswitchA, virtualRockerswitchB | - | Teach-in |

¹ Not all channels are supported by all devices, it depends which specific EEP type is used by the device, all thing types additionally support `rssi`, `repeatCount` and `lastReceived` channels

² These are just examples of supported devices

Furthermore following supporting EEP family is available too: A5-11, types 0x03 (rollershutter position status) and 0x04 (extended light status).

A `rockerSwitch` is used to receive messages from a physical EnOcean Rocker Switch.
A `classicDevice` is used to for older EnOcean devices which react only on rocker switch messages (like Opus GN-A-R12V-SR-4).
As these devices do not send their current status, you have to add additional listener channels for each physical Rocker Switch to your thing.
In this way you can still sync your item status with the physical status of your device whenever it gets modified by a physical rocker switch.
The classic device simulates a physical Rocker Switch.
Per default the classic device uses the channel A of the simulated rocker switch.
If you want to use channel B you have to use virtualRockerswitchB in conjunction with rules to send commands.

## Pairing

Most of the EnOcean devices can be automatically created and configured as an openHAB thing through the discovery service.
The EnOcean protocol defines a so called "teach-in" process to announce the abilities and services of an EnOcean device and pair devices.
To pair an EnOcean device with its openHAB thing representation, you have to differentiate between sensors and actuators.

### Sensors

To pair a sensor with its thing, you first have to start the discovery scan for this binding in PaperUI.
Then press the "teach-in" button of the sensor.
The sensor sends a teach-in message which contains the information about the EEP and the EnOceanId of the sensor.
If the EEP is known by this binding the thing representation of the device is created.
The corresponding channels are created dynamically, too. 

### Actuators
 
If the actuator supports UTE teach-in, the corresponding thing can be created and paired automatically.
First you have to **start the discovery scan for a gateway** in PaperUI.
Then press the teach-in button of the actuator.
If the EEP of the actuator is known, the binding sends an UTE teach-in response with a new SenderId and creates a new thing with its channels. 

If the actuator does not support UTE teach-ins, you have to create, configure and choose the right EEP of the thing manually.
It is important to link the teach-in channel of this thing to a switch item.
Afterwards you have to **activate the pairing mode of the actuator**.
Then switch on the teach-in item to send a teach-in message to the actuator.
If the pairing was successful, you can control the actuator and unlink the teach-in channel now.
The content of this teach-in message is device specific and can be configured through the teach-in channel.

To pair a classicDevice with an EnOcean device, you first have to activate the pairing mode of the actuator.
Then switch the virtualRockerSwitchA On/Off.

Each EnOcean gateway supports 127 unique SenderIds.
The SenderId of a thing can be set manually or determined automatically by the binding.
In case of an UTE teach-in the next unused SenderId is taken automatically.
To set this SenderId to a specific one, you have to use the nextSenderId parameter of your gateway.

## Thing Configuration

The pairing process of an openHAB thing and an EnOcean device has to be triggered within Paper UI.
Therefore if you do not want to use Paper UI, a mixed mode configuration approach has to be done.
To determine the EEP and EnOceanId of the device and announce a SenderId to it, you first have to pair an openHAB thing with the EnOcean device.
Afterwards you can delete this thing and manage it with its necessary parameters through a configuration file.
If you change the SenderId of your thing, you have to pair again the thing with your device.

|Thing type                       | Parameter         | Meaning                     | Possible Values |
|---------------------------------|-------------------|-----------------------------|---|
| bridge                          | path              | Path to the EnOcean Gateway | COM3, /dev/ttyAMA0, rfc2217://x.x.x.x:3001 |
|                                 | nextSenderId      | Set SenderId of next created thing.<br/>If omitted, the next unused SenderId is taken | 1-127 |
| pushButton                      | receivingEEPId    | EEP used for receiving msg  | F6_01_01 |
|                                 | enoceanId         | EnOceanId of device this thing belongs to | hex value as string |
| rockerSwitch                    | receivingEEPId    |                             | F6_02_01, F6_02_02 |
|                                 | enoceanId         | | |
| mechanicalHandle                | receivingEEPId    |                             | F6_10_00, F6_10_01, A5_14_09 |
|                                 | enoceanId         | | |
| contact                         | receivingEEPId    |                             | D5_00_01, A5_14_01_ELTAKO |
|                                 | enoceanId         | | |
| temperatureSensor               | receivingEEPId    |                             | A5_02_01-0B, A5_02_10-1B, A5_02_20, A5_02_30 |
|                                 | enoceanId         | | |
| temperatureHumiditySensor       | receivingEEPId    |                             | A5_04_01-03 |
|                                 | enoceanId         | | |
| occupancySensor                 | receivingEEPId    |                             | A5_07_01-03 |
|                                 | enoceanId         | | |
| lightTemperatureOccupancySensor | receivingEEPId    |                             | A5_08_01-03, A5_08_01_FXBH |
|                                 | enoceanId         | | |
| lightSensor                     | receivingEEPId    |                             | A5_06_01, A5_06_01_ELTAKO |
|                                 | enoceanId         | | |
| roomOperatingPanel              | receivingEEPId    |                             | A5_10_01-0D, A5_10_10-1F, A5_10_20-23 |
|                                 | enoceanId         | | |
| automatedMeterSensor            | receivingEEPId    |                             | A5_12_00-03 |
|                                 | enoceanId         | | |
| centralCommand                  | senderIdOffset    | SenderId used for sending msg.<br/>If omitted, nextSenderId of bridge is used | 1-127 |
|                                 | enoceanId         | | |
|                                 | sendingEEPId      | EEP used for sending msg    | A5_38_08_01, A5_38_08_02 |
|                                 | broadcastMessages | Send broadcast or addressed msg | true, false |
|                                 | receivingEEPId    |                             | F6_00_00, A5_38_08_02, A5_11_04 |
|                                 | suppressRepeating | Suppress repeating of msg   | true, false |
| rollershutter                   | senderIdOffset    |                             | 1-127 |
|                                 | enoceanId         | | |
|                                 | sendingEEPId      |                             | A5_3F_7F_EltakoFSB, A5_38_08_07, D2_05_00 |
|                                 | broadcastMessages |                             | true, false |
|                                 | receivingEEPId¹   |                             | A5_3F_7F_EltakoFSB, A5_11_03, D2_05_00 |
|                                 | suppressRepeating |                             | true, false |
|                                 | pollingInterval   | Refresh interval in seconds | Integer |
| measurementSwitch               | senderIdOffset    |                             | 1-127 |
|                                 | enoceanId         | | |
|                                 | sendingEEPId      |                             | D2_01_00-0F, D2_01_11, D2_01_12,<br/>D2_01_09_PERMUNDO, D2_01_0F_NODON, D2_01_12_NODON |
|                                 | receivingEEPId¹   |                             | D2_01_00-0F, D2_01_11, D2_01_12,<br/>D2_01_09_PERMUNDO, D2_01_0F_NODON, D2_01_12_NODON,<br/> A5_12_01 |
|                                 | broadcastMessages |                             | true, false |
|                                 | pollingInterval   |                             | Integer |
|                                 | suppressRepeating |                             | true, false |
| classicDevice                   | senderIdOffset    |                             | 1-127 |
|                                 | sendingEEPId      |                             | F6_02_01, F6_02_02 |
|                                 | broadcastMessages |                             | true, false |
|                                 | receivingEEPId    |                             | F6_02_01, F6_02_02 |
|                                 | suppressRepeating |                             | true, false |

¹ multiple values possible, EEPs have to be of different EEP families.
If you want to receive messages of your EnOcean devices you have to set **the enoceanId to the EnOceanId of your device**.

## Channels

The channels of a thing are determined automatically based on the chosen EEP. 

|Channel              | Item               | Description |
|---------------------|--------------------|---------------------------------|
| repeaterMode        | String             | Set repeater level to 1, 2 or disable |
| setBaseId           | String             | Changes the BaseId of your gateway. This can only be done 10 times! So use it with care. |
| pushButton          | Trigger            | Channel type system:rawbutton, emits PRESSED and RELEASED events |
| rockerswitchA/B     | Trigger            | Channel type system:rawrocker, emits DIR1_PRESSED, DIR1_RELEASED, DIR2_PRESSED, DIR2_RELEASED events |
| windowHandleState   | String             | Textual representation of handle position (OPEN, CLOSED, TILTED) |
| contact             | Contact            | State OPEN/CLOSED (tilted handle => OPEN) |
| temperature         | Number:Temperature | Temperature in degree Celsius |
| humidity            | Number             | Relative humidity level in percentages |
| illumination        | Number:Illuminance | Illumination in lux |
| occupancy           | Switch             | Occupancy button pressed (ON) or released (OFF) |
| motionDetection     | Switch             | On=Motion detected, Off=not |
| setPoint            | Number             | linear set point |
| fanSpeedStage       | String             | Fan speed: -1 (Auto), 0, 1, 2, 3 |
| dimmer              | Dimmer             | Dimmer value in percent |
| generalSwitch(/A/B) | Switch             | Switch something (channel A/B) ON/OFF |
| rollershutter       | Rollershutter      | Shut time (shutTime) in seconds can be configured |
| angle               | Number:Angle       | The angle for blinds |
| instantpower        | Number:Power       | Instant power consumption in Watts |
| totalusage          | Number:Energy      | Used energy in Kilowatt hours |
| teachInCMD          | Switch             | Sends a teach-in msg, content can configured with parameter teachInMSG |
| virtualSwitchA      | Switch             | Used to convert switch item commands into rocker switch messages (channel A used)<br/>Time in ms between sending a pressed and release message can be defined with channel parameter duration.<br/>The switch mode (rocker switch: use DIR1 and DIR2, toggle: use just one DIR) can be set with channel parameter switchMode (rockerSwitch, toggleButtonDir1, toggleButtonDir2) |
| virtualRollershutterA | Rollershutter      | Used to convert rollershutter item commands into rocker switch messages (channel A used) |
| rockerswitchListenerSwitch | Switch        | Used to convert rocker switch messages into switch item state updates |
| rockerswitchListenerRollershutter | Rollershutter | Used to convert rocker switch messages into rollershutter item state updates |
| virtualRockerswitchB | String             | Used to send plain rocker switch messages (channel B used) |
| batteryVoltage       | Number:ElectricPotential | Battery voltage for things with battery |
| energyStorage        | Number:ElectricPotential | Energy storage, don't know what this means... |
| rssi                 | Number                   | Received Signal Strength Indication (dBm) of last received message |
| repeatCount          | Number                   | Number of repeaters involved in the transmission of the telegram |
| lastReceived         | DateTime                 | Date and time the last telegram was received |

Items linked to bi-directional actuators (actuator sends status messages back) should always disable the `autoupdate`.
This is especially true for Eltako rollershutter, as their position is calculated out of the current position and the moving time.

## Channel Configuration

Some channels can be configured with parameters.

| Channel type  | Parameter      | Meaning                                                              | Possible values                                                                                                                     |
|---------------|----------------|----------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| rollershutter | shutTime       | Time (in seconds) to completely close the rollershutter              |                                                                                                                                     |
| dimmer        | rampingTime    | Duration of dimming                                                  | A5-38-08: Ramping Time (in seconds), 0 = default ramping, 1..255 = seconds to 100%; D2-01-01: 0 = switch, 1-3 = timer 1-3, 4 = stop |
|               | eltakoDimmer   | Flag for Eltako dimmers, because Eltako does interpret this EEP differently | True for Eltako dimmer, false otherwise. Defaults to true for compatibility purpose.                                         |
|               | storeValue     | Store final value. For Eltako devices, block dimming value.          | True or false. Defaults to false.                                         |
| teachInCMD    | manufacturerId | Id is used for 4BS teach in with EEP                                 | HEX                                                                                                                                 |
|               | teachInMSG     | Use this message if teach in type and/or manufacturer id are unknown | HEX                                                                                                                                 |

Possible declaration in Thing DSL:

```xtend
Thing centralCommand 11223344 "Light" @ "Living room" [ enoceanId="11223344", senderIdOffset=15, sendingEEPId="A5_38_08_02", receivingEEPId="A5_38_08_02" ] {
    Channels:
        Type teachInCMD : teachInCMD [ teachInMSG="E0400D80" ]
        Type dimmer : dimmer [ rampingTime=0 ]
}
```

## Rules and Profiles

The rockerSwitch things use _system:rawrocker_ channel types.
So they trigger _DIR1[/2]_\__PRESSED_ and DIR1[/2]_\__RELEASED_ events.
These channels can be directly linked to simple items like Switch, Dimmer or Player with the help of _profiles_.
If you want to do more advanced stuff, you have to implement rules which react to these events

```xtend
rule "Advanced rocker rule"
when
    Channel 'enocean:rockerSwitch:gtwy:AABBCC00:rockerswitchA' triggered DIR1_PRESSED
then
    // do some advanced stuff
end
```

## Example

```xtend
Bridge enocean:bridge:gtwy "EnOcean Gateway" [ path="/dev/ttyAMA0" ] {
   Thing rockerSwitch rs01 "Rocker" @ "Kitchen" [ enoceanId="aabbcc01", receivingEEPId="F6_02_01" ]
   Thing mechanicalHandle mh01 "Door handle" @ "Living room" [ enoceanId="aabbcc02", receivingEEPId="F6_10_00" ]
   Thing roomOperatingPanel p01 "Panel" @ "Floor" [ enoceanId="aabbcc03", receivingEEPId="A5_10_06" ]
   Thing centralCommand cc01 "Light" @ "Kitchen" [ enoceanId="aabbcc04", senderIdOffset=1, sendingEEPId="A5_38_08_01", receivingEEPId="F6_00_00", broadcastMessages=true, suppressRepeating=false ]
   Thing centralCommand cc02 "Dimmer" @ "Living room" [ enoceanId="aabbcc05", senderIdOffset=2, sendingEEPId="A5_38_08_02", receivingEEPId="A5_38_08_02", broadcastMessages=true, suppressRepeating=false ]
   Thing rollershutter r01 "Rollershutter" @ "Kitchen" [ enoceanId="aabbcc06", senderIdOffset=3, sendingEEPId="A5_3F_7F_EltakoFSB", receivingEEPId="A5_3F_7F_EltakoFSB", broadcastMessages=true, suppressRepeating=false ] {Channels: Type rollershutter:rollershutter [shutTime=25]}
   Thing measurementSwitch ms01 "TV Smart Plug" @ "Living room" [ enoceanId="aabbcc07", senderIdOffset=4, sendingEEPId="D2_01_09", broadcastMessages=false, receivingEEPId="D2_01_09","A5_12_01", suppressRepeating=false, pollingInterval=300]
   Thing classicDevice cd01 "Garage_Light" @ "Garage" [ 
        senderIdOffset=5, 
        sendingEEPId="F6_02_01", 
        broadcastMessages=true, 
        receivingEEPId="F6_02_01",
        suppressRepeating=false 
   ] {
        Type virtualSwitchA             : virtualSwitchA              [duration=300, switchMode="rockerSwitch"]
        Type rockerswitchListenerSwitch : Listener1 "Schalter links"  [enoceanId="aabbcc08", channel="channelA", switchMode="toggleButtonDir1"]
        Type rockerswitchListenerSwitch : Listener2 "Schalter rechts" [enoceanId="aabbcc09", channel="channelB", switchMode="toggleButtonDir2"]
   }
}
```

```xtend
Player Kitchen_Sonos "Sonos" (Kitchen) {channel="sonos:PLAY1:ID:control", channel="enocean:rockerSwitch:gtwy:rs01:rockerswitchA" [profile="system:rawrocker-to-play-pause"]}
Dimmer Kitchen_Hue "Hue" <light> {channel="enocean:rockerSwitch:gtwy:rs01:rockerswitchB" [profile="system:rawrocker-to-dimmer"], channel="hue:0220:0017884f6626:9:brightness"}
Rollershutter Kitchen_Rollershutter "Roller shutter" <blinds> (Kitchen) {channel="enocean:rollershutter:gtwy:r01:rollershutter", autoupdate="false"}
Switch Garage_Light "Switch" {
        channel="enocean:classicDevice:gtwy:cd01:virtualRockerswitchA", 
        channel="enocean:classicDevice:gtwy:cd01:Listener1", 
        channel="enocean:classicDevice:gtwy:cd01:Listener2"
}
```

## Generic Things

If an EnOcean device uses an unsupported EEP or _A5-3F-7F_, you have to create a `genericThing`.
Generic things support all channels like switch, number, string etc as generic channels.
However you have to specify how to convert the EnOcean messages of the device into openHAB state updates and how to convert the openHAB commands into EnOcean messages.
These conversion functions can be defined with the help of transformation functions like MAP.

|Thing type                       | Parameter         | Meaning                    | Possible Values |
|---------------------------------|-------------------|----------------------------|---|
| genericThing                    | senderIdOffset    |                            | 1-127 |
|                                 | enoceanId         | EnOceanId of device this thing belongs to | hex value as string |
|                                 | sendingEEPId      | EEP used for sending msg   | F6_FF_FF, A5_FF_FF, D2_FF_FF |
|                                 | receivingEEPId    | EEP used for receiving msg | F6_FF_FF, A5_FF_FF, D2_FF_FF |
|                                 | broadcastMessages |                            | true, false |
|                                 | suppressRepeating |                            | true, false |

Supported channels: genericSwitch, genericRollershutter, genericDimmer, genericNumber, genericString, genericColor, genericTeachInCMD.
You have to define the transformationType (e.g. MAP) and transformationFunction (e.g. for MAP: file name of mapping file) for each of these channels. 

For an inbound transformation (EnOcean message => openHAB state) you receive the channel id and the EnOcean data in hex separated by a pipe.
Your transformation function has to return the openHAB State type and value separated by a pipe.
If you want to use a mapping transformation, your mapping file has to look like this for a genericThing using EEP F6_FF_FF:

```
ChannelId|EnoceanData(Hex)=openHABState|Value
genericSwitch|70=OnOffType|ON
genericSwitch|50=OnOffType|OFF
genericRollershutter|70=PercentType|0
genericRollershutter|50=PercentType|100
```

For an outbound transformation (openHAB command => EnOcean message) you receive the channel id and the command separated by a pipe.
Your transformation function has to return the payload of the EnOcean message.
You do not have to worry about CRC and header data.
If you want to use a mapping transformation, your mapping file has to look like this for a genericThing using EEP A5_FF_FF:

```
ChannelId|openHABCommand=EnoceanData(Hex)
genericSwitch|ON=01000009
genericSwitch|OFF=01000008
```
