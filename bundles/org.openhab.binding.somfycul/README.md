# SomfyCUL Binding

This binding supports controlling [Somfy RTS Rollershutters](https://www.somfysystems.com/en-us/products/rolling-shutters) via CUL sticks.

## Supported Things

Currently these things are supported:

- `cul-device`: The CUL stick bridge used to send the actual signals to your rollershutters
- `somfy-device`: The rollershutters (UP, DOWN, MY/STOP control of a rollershutter)

## Discovery

There is no auto discovery

## Thing Configuration

### `cul-device` Thing Configuration

| Parameter            | Type   | Description                                                           | Default      | Required | Advanced |
| -------------------- | ------ | --------------------------------------------------------------------- | ------------ | -------- | -------- |
| port                 | String | The serial port (COM1, /dev/ttyS0, ...) your CUL stick is attached to | /dev/ttyUSB0 | yes      | no       |
| baudrate             | int    | The serial port baud rate                                             | 38400        | yes      | no       |

### `somfy-device` Thing Configuration

There is no thing configuration for the `somfy-device` things.

## Channels

| Channel  | Type          | Description                        |
| -------- | ------------- | ---------------------------------- |
| position | Rollershutter | Device control (UP, DOWN, MY/STOP) |
| program  | Switch        | Device program button (pairing)    |

## Full Example

Things and Items can be configured in the UI, or by using a `things` or `items` file like in the following example:

### Thing Configuration

somfy.things:

```java
// the CUL stick
somfycul:cul-device:cul [ port="/dev/ttyUSB0", baudrate="38400" ]
/** optionally, define a custom port (e.g. /dev/ttyNanoCUL) as a udev rule, for example 
 * file: /etc/udev/rules.d/20_nanoCUL.rules
 * content: SUBSYSTEM=="tty", ATTRS{idVendor}=="0403", ATTRS{idProduct}=="6001", ATTRS{serial}=="433", SYMLINK+="ttyNanoCUL"
 * 
 * change idVendor, idProduct and idSerial depending on your CUL stick.
 * you can inspect the values of your CUL stick like this:
 * $ udevadm info -a -p $(udevadm info -q path -n /dev/ttyUSB0)
 */

// the rollershutters
somfycul:somfy-device:livingroom_shutter_left (somfycul:cul-device:cul)
somfycul:somfy-device:livingroom_shutter_right (somfycul:cul-device:cul)
somfycul:somfy-device:bedroom_shutter (somfycul:cul-device:cul)
```

### Item Configuration

somfy.items:

```java
// you only need the switch items to for pairing/copy from your existing RTS remote and should remove/comment them later on
Switch      Shutter_Livingroom_Left     "Shutter Livingroom left"       {channel="somfycul:somfy-device:livingroom_shutter_left:program"}
Switch      Shutter_Livingroom_Right    "Shutter Livingroom right"      {channel="somfycul:somfy-device:livingroom_shutter_right:program"}
Switch      Shutter_Bedroom             "Shutter Bedroom"               {channel="somfycul:somfy-device:bedroom_shutter:program"}

// these will be your rollershutters with UP, DOWN, MY/STOP commands
Rollershutter Shutter_Livingroom_Left   "Shutter Livingroom left"       {channel="somfycul:somfy-device:livingroom_shutter_left:position"}
Rollershutter Shutter_Livingroom_Right  "Shutter Livingroom right"      {channel="somfycul:somfy-device:livingroom_shutter_right:position"}
Rollershutter Shutter_Bedroom           "Shutter Bedroom"               {channel="somfycul:somfy-device:bedroom_shutter:position"}
```

### Sitemap Configuration

```perl
sitemap home label="Home" {
        Frame label="Ground Floor" icon="groundfloor" {
                Text label="Livingroom" icon="livingroom" {
                        Default item=Shutter_Livingroom_Left
                        Default item=Shutter_Livingroom_Right
                        }

                }

        Frame label="First Floor" icon="firstfloor" {
                Text label="Bedroom" icon="bedroom" {
                        Default item=Shutter_Bedroom
                }
        }
}
```


### Clone Somfy RTS Remote to `somfy-device`

To clone an existing Somfy RTS Remote as a `somfy-device`, have your Somfy RTS remote ready and properly programmed to control your rollershutter. You'll need to have your `somfy-device` `items` set to `Switch` type and use the `program` channel. 

Use the manufacturers procedure to _copy_ a remote to another remote. Usually this means selecting the channel you wish to copy on your Somfy RTS remote, pressing and holding the `program` or `PROG` button for 2-3 seconds until the rollershutter briefly moves up/down to confirm it is about to be programmed.
Use your `somfy-device` `Switch` `item` to _press_ `program` shortly after to copy the remote channel to this `somfy-device`.

You can now set your `somfy-device` `items` back to `Rollershutter` item type.

## Props

Shoutout to [@weisserd](https://github.com/weisserd) for his initial creation of this binding and allowing me to use his codebase to make it an official openHAB binding.
