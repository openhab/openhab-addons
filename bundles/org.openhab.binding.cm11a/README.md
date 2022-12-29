# Cm11a (X10 controller) Binding

The cm11a is a serial computer interface that allows a computer to control attached X10 modules.

## Hardware - cm11a

The cm11a is an older device that communicates over a serial interface.
Most people connect it to a computer using a serial to USB adapter.  
This binding has been tested with serial port and a serial to USB adapter.

X10 (and thus the cm11a) supports two types of modules.
The Switch (also called Appliance) module supports being turned on and off.
The Lamp module supports on, off, dim and bright.

In addition to controlling X10 modules the cm11a listens on the powerline and reports to the computer changes made to X10 modules via other controllers.

### Use of serial port

The binding opens the serial port when it starts and keeps it open until the binding is terminated.
If the serial port is disconnected a reconnect will be attempted the next time it is needed.  
Therefore, other applications should not attempt to use the port when OpneHAB is running.
However, another program could load macros into the cm11a before openHAB starts.

### cm11a macros

The cm11a is also able to store a schedule and control modules based on that schedule.
That functionality in not currently supported by or used by this binding.
This binding doesn't clear macros from the cm11a so other programs could load macros before openHAB is started.
If you want to do scheduling using openHAB you should be sure there are no macros in the cm11a.
The `heyu clear` command can be used for this purpose.

### X10 powerline monitoring

The cm11a has the ability to capture x10 messages on the powerline.
This binding captures those messages, decodes them and updates the item state.

## Supported things

The binding currently supports the following thing types:

- switch - which supports on and off states
- dimmer - which can be dimmed in addition to turned on or off

## Discovery

Discovery is not supported because that kind of information is not available in the cm11a or the X10 specification.

## Configuration

The cm11a acts as a Bridge to the controlled modules, which are represented as Things.

### Bridge Configuration

The bridge requires the parameter `serialPort` which identifies the serial port the cm11a is connected to.

### Thing Configuration

Each attached thing must specify the `houseUnitCode` set in the device (i.e. A1).

## Channels

| Thing  | Channel Type ID | Item Type | Description        |
|--------|-----------------|-----------|--------------------|
| switch | switchState     | Switch    | An On/Off switch   |
| dimmer | lightDimmer     | Dimmer    | A dimmable  device |

### Example

#### Things

```perl
Bridge cm11a:cm11a:MyCm11a  [ serialPort="COM3" ] {
    Thing switch SwitchA1 [ houseUnitCode="A1" ]
    Thing dimmer DimmerA2 [ houseUnitCode="A2" ]
}
```

#### Items

```java
SwitchA1  "Kitchen Plug"   <light>  (someGroup)  { channel="cm11a:switch:MyCm11a:SwitchA1:switchstatus" }
DimmerA2  "Porch lights"   <slider> (someGroup)  { channel="cm11a:dimmer:MyCm11a:DimmerA2:lightlevel" }
```

## Known issues

1. When openHAB starts up it doesn't restore the last state of each module. And, the cm11a does not provide a discovery service. Therefore it assumes everything off.
1. The dimmer slider can get out of sync with the actual light because of the way X10 works. On some switches if you turn them on they will go to full bright and some switches will return to the previous dim level.

## References

1. [CM11A (X10) Protocol Document](https://wanderingsamurai.net/electronics/cm11a-x10-protocol-document)
1. [Heyu - control software for the cm11a](https://www.heyu.org/)
1. cm11a Controllers are available for purchase from several sites on the internet
