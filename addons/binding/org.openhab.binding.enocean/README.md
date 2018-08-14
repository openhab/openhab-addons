# EnOcean Binding

The EnOcean binding connects OpenHab to the EnOcean ecosystem.

The binding uses an EnOcean gateway to retrieve sensor data and control actuators. For _bidirectional_ actuators it is even possible to update the OpenHab item state if the actuator gets modified outside of OpenHab.
This binding has been developed on an USB300 gateway and was also tested with an EnOceanPi. As this binding implements a full EnOcean stack, we have full control over these gateways. This binding can enable the repeater function (level 1 or 2) of these gateways and retrieve detailed information about them.   

## Concepts/Configuration

First of all you have to configure an EnOcean Transceiver (Gateway). This device has to be added manually to OpenHab and is represented by an _EnOcean bridge_. You just have to set the right serial port. If everything is running fine you should see the _base id_ of your gateway in the properties of your bridge.

EnOcean messages are mainly send as broadcast messages without an explicit receiver address. However each message contains a unique sender address (EnOcean Id) to determine from which device this message was sent. To receive messages from an EnOcean device you have to determine its EnOcean Id and add an appropriate thing to OpenHab. **The thing Id has to be set to the EnOcean Id**. If the device is an actuator which you want to control, you have to generate an unique sender id and announce it to the actuator (_teach-in_). This sender id is made up of the base id of the EnOcean gateway and a number between 1 and 127. This number can be set manually or determined by the binding.

## Supported Things

This binding is developed on and tested with the following things

 * USB300 and EnOceanPi gateways as OpenHab bridges
 * The following Eltako actuators:
    * FSR14 (light switch)
    * FSB14 (rollershutter)
    * FUD14 (dimmer)
    * FSSA-230V (smart plug)
 * The following Opus actuators:
    * GN-A-R12V-SR-4 (light)
    * GN-A-R12V-MF-2 (light)
    * GN-A-R12V-LZ/UD (dimmer)
    * GN-A-R12V-JRG-2 (rollershutter)
    * GN-A-U230V-JRG (rollershutter)
    * OPUS-FUNK PLUS Jalousieaktor 1fach UP (rollershutter)
    * OPUS-Funk PLUS Steckdosenleiste (smart multiple socket)
 * NodOn Smart Plug (ASP-2-1-10), In Wall Switch (SIN-2-2-00, SIN-2-1-0x), Permundo PSC234 (smart plug with metering)
 * Thermokon SR04 room control
 * Hoppe SecuSignal window handles
 * Rocker switches (NodOn, Eltako FT55 etc)

However because of the standardized EnOcean protocol it is more important which EEP this binding supports: F6-02, F6-10, D5-00, A5-10, A5-38 (switching and dimming), A5-37 (use generic things). Hence if your device supports one of these EEPs the chances are good that your device is also supported by this binding.

## Discovery

Most of the EnOcean devices can be automatically created and configured as an OpenHab thing through the discovery service. The EnOcean protocol defines a so called "teach-in" process to announce the abilities and services of an EnOcean device and pair devices. To pair an EnOcean device with its OpenHab thing representation, you have to differentiate between sensors and actuators.

### Sensors

To pair a sensor with its thing, you first have to start the discovery scan for this binding in PaperUI. Then press the "teach-in" button of the sensor. The sensor sends a teach-in message which contains the information about the EEP and the device Id of the sensor. If the EEP is known by this binding the thing representation of the device is created. The corresponding channels are created dynamically, too. 

### Actuators
 
If the actuator supports UTE teach-ins, the corresponding thing can be created and paired automatically. First you have to start the discovery scan for this binding in PaperUI. Then press the teach-in button of the actuator. 

If the actuator does not support UTE teach-ins, you have to create, configure and choose the right EEP of the thing manually. It is important to link the teach-in channel of this thing. Afterwards you have to activate the pairing mode of the actuator. Then switch on the teach-in item(/channel) to send a teach-in message to the actuator. If the pairing was successful, you can control the actuator and unlink the teach-in channel.


## Adding Enocean-Things (Actuators, Switches, Sensors, etc.)

https://github.com/fruggy83/enocean/wiki#how-to-add-an-enocean-thing-actuator-sensor-wallswitch-etc-via-paperui

## Thing Configuration

Things can and should by configured through PaperUI. Following the most important config parameters:

Bridge

 * Serial port: The serial port to which the EnOcean gateway is connected to
 * Next device id: The device Id which should be taken for the next created actuator without an explicit device Id
 
Things

 * Thing Id: EnOcean Id of the device. 4 byte hex string (abcdef00).
 * Bridge: Must be provided
 * Sender Id: Is used to generate the unique EnOcean device Id for sending messages (added to the base Id of the gateway). If you leave it empty, the next free Id will be determined automatically (see "next device id" of bridge). The resulting device Id can be seen through the properties.
 * (Sending/receiving) EEP: Set the EEP which should be used to send and receive message to or from the device.
 
## Generic Things

  If an Enocean device uses an unsupported EEP or _A5-3F-7F_, you have to create a generice thing. Generic things support all generic channels like switch, number, string etc. However you have to specify how to convert the Enocean messages from the thing into Openhab state updates and how to convert the Openhab commands into Enocean messages. These conversion functions can be defined with the help of transformation functions like MAP or JavaScript.

For an inbound transformation (Enocean message => Openhab state) you get the channel id and the Enocean data seperated by a pipe.

```
ChannelId|EnoceanData(Hex)=OpenhabState|Value
genericLightSwitch|70=OnOffType|ON
genericLightSwitch|50=OnOffType|OFF
```

Outbound transformation (Openhab command => Enocean message):

```
ChannelId|OpenhabCommand=EnoceanData(Hex)
genericLightSwitch|ON=01000009
genericLightSwitch|OFF=01000008
```

## Channels

The channels of a thing are determined automatically based on the choosen EEP. The following channels are supported: (Light) Switch, Dimmer, Rollershutter, Temperature, Handle state and many more.

## Profiles

The (sensor) rocker switches use _system:rawrocker_ channels. So they trigger _DIR1[/2]_\__PRESSED_ and DIR1[/2]_\__RELEASED_ events. You can use these events in your rules

```
rule "Sonos ON"
when
    Channel 'enocean:rockerSwitch:4326d3ef:ffffffff:rockerswitchB' triggered DIR1_PRESSED
then
    Sonos_Bad_Control.sendCommand("PLAY")
end

rule "Sonos OFF"
when
    Channel 'enocean:rockerSwitch:4326d3ef:ffffffff:rockerswitchB' triggered DIR2_PRESSED
then
    Sonos_Bad_Control.sendCommand("PAUSE")
end
```

or link them directly to your items. The following items are supported by profiles: switch, dimmer, player.

## Credits

Many thanks to:

 * The NodOn support for their hints about the ADT and UTE teach in messages.
 * The fhem project for the inspiration and their EnOcean addon
 * [leifbladt](https://github.com/leifbladt) for his work on the installation notes
 * [Casshern81](https://github.com/Casshern81) for his work on the documentation, valuable hints and testing efforts
 * [bodiroga](https://github.com/bodiroga) for implementing the USB Discovery service
