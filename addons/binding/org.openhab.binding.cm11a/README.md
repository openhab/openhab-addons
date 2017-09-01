# Cm11a (X10 controller) Binding
The cm11a is a serial computer interface that allows the computer control attached X10 modules. See the Hardware section below for a further description of the cm11a. This is implemented as an OpenHAB 2 binding.

##Credit
This binding is made possible because of the previous work done by "Anthony Green". His work can be seen at: <https://github.com/engineergreen/openhab/tree/cm11a-binding/bundles/binding/org.openhab.binding.cm11a>

## Supported things
The binding currently supports the following thing types:

* switch
* dimmer

## Discovery
Discovery is not supported because that kind of information is not available in the cm11a or the X10 specification.

## X10 powerline monitoring
The cm11a has the ability to capture x10 messages on the powerline. This binding captures those messages, decodes them and updates the item state. If the UI has a channel open to the server those changes will be reflected in the UI. Otherwise, a refresh of the browser will show the current state.

##Configuration
This binding is an OpenHAB 2 binding and uses the Bridge / Thing design with the cm11a being the Bridge and the controlled modules being the Things. The following definitions are specified in the .things file.

### Bridge Configuration
The bridge requires the serial port used to connect the cm11a to the computer.

    Bridge cm11a:cm11a:MyCm11a  [ serialPort="COM3" ] or
    Bridge cm11a:cm11a:MyCm11a  [ serialPort="/dev/ttyUSB0" ]


### Thing Configuration
Each attached thing must specify whether it is a dimmer or a switch and the house/unit code (i.e. A1).

    Thing switch SwitchA1 [ HouseUnitCode="A1" ]
    Thing dimmer DimmerA2 [ HouseUnitCode="A2" ]

### Example

    Bridge cm11a:cm11a:MyCm11a  [ serialPort="COM3" ] {
        Thing switch SwitchA1 [ HouseUnitCode="A1" ]
        Thing dimmer DimmerA2 [ HouseUnitCode="A2" ]
        Thing dimmer DimmerA3 [ HouseUnitCode="A3" ]
        Thing switch SwitchA5 [ HouseUnitCode="A5" ]
        Thing switch SwitchA6 [ HouseUnitCode="A6" ]
        Thing dimmer DimmerA7 [ HouseUnitCode="A7" ]
        Thing switch SwitchA8 [ HouseUnitCode="A8" ]
    }

## Items
These are specified in the .items file. This section describes the specifics related to this binding. Please see the [Items documentation](http://docs.openhab.org/configuration/items.html) for a full explanation of configuring items.

The most important thing is getting the **channel** specification correct. The general format is:

    { channel="cm11a:switch:MyCma11a:SwitchA1:switchstatus }

The parts (separated by :) are defined as:

1. cm11a to specify this is a cm11a device
2. switch or dimmer to specify this is a switch or dimmer **Thing**
3. MyCm11a to identify which cm11a this is attached to. This corresponds to the third segment in the **Bridge** definition.
4. SwitchA1 to identify which **Thing** this belongs to.
5. switchstatus or lightlevel which specifies the channel in the item you want displayed. switchstatus is the only available option for a switch. And, lightlevel is the only available option for a dimmer.

### Example

    Switch SwitchA1  "Kitchen Plug"   <light>  (someGroup)  { channel="cm11a:switch:MyCm11a:SwitchA1:switchstatus" }
    Dimmer DimmerA2  "Porch lights"   <slider> (someGroup)  { channel="cm11a:dimmer:MyCm11a:DimmerA2:lightlevel" }

## Hardware - cm11a
The cm11a is an older device that communicates over a serial interface. Most people connect it to a computer using a serial to USB adapter.  This binding has been tested with serial port and an adapter.

X10 (and thus the cm11a) supports two types of modules. The Switch (also called Appliance) module supports being turned on and off. The Lamp module supports on, off, dim and bright.

In addition to controlling X10 modules the cm11a listens on the powerline and reports to the computer changes made to X10 modules via other controllers. 

### Use of serial port
The binding opens the serial port when it starts and keeps it open until the binding is terminated. If the serial port is disconnected a reconnect will be attempted the next time it is needed.  Therefore, other applications should not attempt to use the port when OpneHAB is running. However, another program could load macros into the cm11a before OpenHAB starts. 

### cm11a macros
The cm11a is also able to store a schedule and control modules based on that schedule. That functionality in not currently supported by or used by this binding. This binding doesn't clear macros from the cm11a so other programs could load macros before OpenHAB is started. If you want to do scheduling using OpenHAB you should be sure there are no macros in the cm11a. The `heyu clear` command can be used for this purpose. 

## Known issues
1. When OpenHAB starts up it doesn't restore the last state of each module. And, the cm11a does not provide a discovery service. Therefore it assumes everything off.
2. The dimmer slider can get out of sync with the actual light because of the way X10 works. On some switches if you turn them on they will go to full bright and some switches will return to the previous dim level. 

## Installation
The following jar files need to be placed in the addons directory:
1. org.openhab.binding.cm11a-<version>.jar          - This binding
2. org.openhab.io.transport.serial-<version>.jar    - Provides serial communications library 

## References

1. [CM11A (X10) Protocol Document](http://wanderingsamurai.net/electronics/cm11a-x10-protocol-document)
2. [Heyu - control software for the cm11a](http://www.heyu.org/)
3. cm11a Controllers are available for purchase from several sites on the internet including EBay and Amazon.