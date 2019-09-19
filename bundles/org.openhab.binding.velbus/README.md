# Velbus Binding

The Velbus binding integrates with a [Velbus](https://www.velbus.eu/) system through a Velbus configuration module (VMB1USB or VMB1RS).

The binding has been tested with a USB configuration module for universal mounting (VMB1USB).

The binding exposes basic actions from the Velbus System that can be triggered from the smartphone/tablet interface, as defined by the Velbus Protocol info sheets.

Supported item types are switches, dimmers and rollershutters.
Pushbutton, temperature sensors and input module states are retrieved and made available in the binding.

## Supported Things


A Velbus configuration module (e.g. VMB1USB) is required as a "bridge" for accessing any other Velbus devices.

The supported Velbus devices are:

```
vmb1bls, vmb1dm, vmb1led, vmb1ry, vmb1ryno, vmb1rynos, vmb2ble, vmb2pbn, vmb4dc, vmb4ry, vmb4ryld, vmb4ryno, vmb6in, vmb6pbn, vmb7in, vmb8ir, vmb8pb, vmb8pbu, vmbdme, vmbdmi, vmbdmir, vmbgp1, vmbgp2, vmbgp4, vmbgp4pir, vmbgpo, vmbgpod, vmbpirc, vmbpirm, vmbpiro
```

The type of a specific device can be found in the configuration section for things in the Paper UI. It is part of the unique thing id which could look like:

```
velbus:vmb4ryld:0424e5d2:01:CH1
```

The thing type is the second string behind the first colon and in this example it is **vmb4ryld**.

## Discovery

The Velbus bridge cannot be discovered automatically. It has to be added manually by defining the serial port of the Velbus Configuration module.

Once the bridge has been added as a thing, a manual scan can be launched to discover all other supported Velbus devices on the bus. These devices will be available in the inbox. The discovery scan will also retrieve the channel names of the Velbus devices.

## Thing Configuration

The Velbus bridge needs to be added first in the things file or through Paper UI.
It is necessary to specify the serial port device used for communication.
On Linux systems, this will usually be either `/dev/ttyS0`, `/dev/ttyUSB0` or `/dev/ttyACM0` (or a higher  number than `0` if multiple devices are present).
On Windows it will be `COM1`, `COM2`, etc.

In the things file, this looks e.g. like

```
Bridge velbus:bridge:1 [ port="COM1" ]
```

For the other Velbus devices, the thing configuration has the following syntax:

```
Thing velbus:<thing type>:<bridgeId>:<thingId> "Label" @ "Location" [CH1="Kitchen Light", CH2="Living Light"]
```

or nested in the bridge configuration:

```
<thing type> <thingId> "Label" @ "Location" [CH1="Kitchen Light", CH2="Living Light"]
```

The following thing types are valid for configuration:

```
vmb1bls, vmb1dm, vmb1led, vmb1ry, vmb1ryno, vmb1rynos, vmb2ble, vmb2pbn, vmb4dc, vmb4ry, vmb4ryld, vmb4ryno, vmb6in, vmb6pbn, vmb7in, vmb8ir, vmb8pb, vmb8pbu, vmbdme, vmbdmi, vmbdmir, vmbgp1, vmbgp2, vmbgp4, vmbgp4pir, vmbgpo, vmbgpod, vmbpirc, vmbpirm, vmbpiro
```

`thingId` is the hexadecimal Velbus address of the thing.

`"Label"` is an optional label for the thing.

`@ "Location"` is optional, and represents the location of the thing.

`[CHx="..."]` is optional, and represents the name of channel x, e.g. CH1 specifies the name of channel 1.


## Channels

For thing type `vmb1bls` the supported channels is `CH1`. UpDown, StopMove and Percent command types are supported.

For thing types `vmb1dm`, `vmb1led`, `vmbdme`, `vmbdmi` and `vmbdmir` the supported channel is `CH1`.
OnOff and Percent command types are supported.
Sending an ON command will switch the dimmer to the value stored when last turning the dimmer off.

For thing types `vmb1ryno`, `vmb1rynos`, `vmb4ryld` and `vmb4ryno` 5 channels are available `CH1` ... `CH5`.
OnOff command types are supported.

For thing type `vmb2ble` the supported channels are `CH1` and `CH2`. UpDown, StopMove and Percent command types are supported.

Thing types `vmb2pbn`, `vmb6pbn`, `vmb7in`, `vmb8ir`, `vmb8pb` and `vmb8pbu` have 8 trigger channels `CH1` ... `CH8`.

For thing type `vmb4dc` 4 channels are available `CH1` ... `CH4`.
OnOff and Percent command types are supported.
Sending an ON command will switch the dimmer to the value stored when last turning the dimmer off.

For thing type `vmb4ry` 4 channels are available `CH1` ... `CH4`.
OnOff command types are supported.

For thing type `vmb4dc` the supported channels are `CH1` ... `CH4`.
OnOff and Percent command types are supported.
Sending an ON command will switch the dimmer to the value stored when last turning the dimmer off.

Thing type `vmb6in`has 6 trigger channels `CH1` ... `CH6`.

Thing types `vmbgp1`, `vmbgp2`, `vmbgp4`, `vmbgp4pir` and `vmbpiro` have 8 trigger channels `CH1` ... `CH8` and one temperature channel `CH9`.

Thing types `vmbgpo` and `vmbgpod` have 32 trigger channels `CH1` ... `CH32` and one temperature channel `CH33`.

Thing types `vmbpirc` and `vmbpirm` have 7 trigger channels `CH1` ... `CH7`.

The trigger channels can be used as a trigger to rules. The event message can be `PRESSED`, `RELEASED`or `LONG_PRESSED`.

## Full Example

.things:

```
Bridge velbus:bridge:1 [ port="COM1"] {
    vmb2ble     01
    vmb2pbn     02
    vmb6pbn     03
    vmb8pbu     04
    vmb7in      05
    vmb4ryld    06
    vmb4dc      07
    vmbgp1      08
    vmbgp2      09
    vmbgp4      0A
    vmbgp4pir   0B
    vmbgpo      0C
    vmbgpod     0D
    vmbpiro     0E
}
```

.items:

```
Switch LivingRoom           {channel="velbus:vmb4ryld:1:06:CH1"}                # Switch for onOff type action
Dimmer TVRoom               {channel="velbus:vmb4dc:1:07:CH2"}                  # Changing brightness dimmer type action
Rollershutter Kitchen       {channel="velbus:vmb2ble:1:01"}                     # Controlling rollershutter or blind type action

Number Temperature_LivingRoom   "Temperature [%.1f °C]"     <temperature> channel="velbus:vmbgp1:1:08:CH09"}  
Number Temperature_Corridor   "Temperature [%.1f °C]"     <temperature> channel="velbus:vmbgpo:1:0C:CH33"}  
Number Temperature_Outside   "Temperature [%.1f °C]"     <temperature> channel="velbus:vmbpiro:1:0E:CH09"}  
```

.sitemap:

```
Switch item=LivingRoom
Slider item=TVRoom
Switch item=TVRoom          # allows switching dimmer item off or on
Rollershutter item=Kitchen
```

Example trigger rule:

```
rule "example trigger rule"
when
    Channel 'velbus:vmb7in:1:05:CH5' triggered PRESSED
then
    var message = receivedEvent.getEvent()
    logInfo("velbusTriggerExample", "Message: {}", message)
    ...
end
```
