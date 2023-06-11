# Flic Button Binding 

openHAB binding for using [Flic Buttons](https://flic.io/)
with a [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci) bridge.

Currently, although Flic Buttons are BLE devices, this binding only supports fliclib-linux-hci (flicd) as a bridge.
The openHAB Bluetooth Bindings are not supported.
Flicd requires a seperate Bluetooth adapter to work, so if you use this binding together with e.g. the
[Bluez Binding](https://www.openhab.org/addons/bindings/bluetooth.bluez/),
two physical Bluetooth adapters are required (one for Bluez and one for flicd).
Be aware that flicd requires an initial internet connection for the verification of the buttons.
After buttons are initially added to flicd, an internet connection is not required anymore.

## Supported Things

| Thing Type ID   | Description               |
| --------------- | ------------------------- |
| flicd-bridge | The bridge representing a running instance of [fliclib-linux-hci (flicd)](https://github.com/50ButtonsEach/fliclib-linux-hci) on the server. |
| button | The Flic button (supports Flic 1 buttons as well as Flic 2 buttons) |

## Discovery

* There is no automatic discovery for flicd-bridge available.
* After flicd-bridge is (manually) configured, buttons will be automatically discovered via background discovery as soon
  as they're added with [simpleclient](https://github.com/50ButtonsEach/fliclib-linux-hci).

If they're already attached to the flicd-bridge before configuring this binding, they can be discovered by triggering an
active scan.

## Thing Configuration

### flicd-bridge

Example for textual configuration:

```
Bridge flicbutton:flicd-bridge:mybridge
```

The default host is localhost:5551 (this should be sufficient if flicd is running with default settings on the same server as openHAB).
If your flicd service is running somewhere else, specify it like this:

```
Bridge flicbutton:flicd-bridge:mybridge [ hostname="<YOUR_HOSTNAME>",  port=<YOUR_PORT>]
```

If flicd is running on a remote host, please do not forget to start it with the parameter `-s <openHAB IP>`, otherwise it won't be accessible for openHAB (more details on [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci)).

### button

For the button, the only config parameter is the MAC address.
Normally, no textual configuration is necessary as buttons are auto discovered as soon as the bridge is configured.
If you want to use textual configuration anyway, you can do it like this:

```
Bridge flicbutton:flicd-bridge:mybridge [ hostname="<YOUR_HOSTNAME>",  port=<YOUR_PORT>] {
    Thing button myflic1 "<YOUR_LABEL>" [address ="<MAC_ADDRESS>"]
    Thing button myflic2 "<YOUR_LABEL>" [address ="<MAC_ADDRESS>"]
    ...
}
```

You can lookup the MAC addresses of your buttons within the inbox of the UI.
You're free to choose any label you like for your button.

## Channels

| Channel ID                | Channel Type             | Item Type                | Description                    |
| ------------------------- | ------------------------ | --------------------------| ------------------------------ |
| rawbutton                | [System Trigger Channel](https://www.openhab.org/docs/developer/bindings/thing-xml.html#system-trigger-channel-types) `system.rawbutton`  | Depends on the  [Trigger Profile](https://www.openhab.org/docs/configuration/items.html#profiles) used | Raw Button channel that triggers `PRESSED` and `RELEASED` events, allows to use openHAB profiles or own implementations via rules to detect e.g. double clicks, long presses etc.  |
| button                   | [System Trigger Channel](https://www.openhab.org/docs/developer/bindings/thing-xml.html#system-trigger-channel-types) `system.button`    | Depends on the [Trigger Profile](https://www.openhab.org/docs/configuration/items.html#profiles) used | Button channel that is using Flic's implementation for detecting long, short or double clicks. Triggers `SHORT_PRESSED`,`DOUBLE_PRESSED` and `LONG_PRESSED` events.   |
| battery-level            | [System State Channel](https://www.openhab.org/docs/developer/bindings/thing-xml.html#system-state-channel-types) `system.battery-level`     | Number | Represents the battery level as a percentage (0-100%).
## Example

### Initial setup

1. Setup and run flicd as described in [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci). 
   Please consider that you need a separate Bluetooth adapter. Shared usage with other Bluetooth services (e.g. Bluez)
   is not possible.
1. Connect your buttons to flicd using the simpleclient as described in
   [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci). Flicd has to run in background the whole
   time, simpleclient can be killed after you successfully test the button connects.
1. Add a flicd-bridge via the UI or textual configuration. Please consider that flicd only accepts connections from
   localhost by default, to enable remote connections from openHAB you have to use the `--server-addr` parameter as
   described in [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci).
1. When the bridge is online, buttons newly added via simpleclient will automatically get discovered via background
   discovery. To discover buttons that were set up before the binding was setup, please run an active scan.

### Configuration Example using Profiles

[Profiles](https://www.openhab.org/docs/configuration/items.html#profiles) are the recommended way to use this binding.

demo.things:

```
Bridge flicbutton:flicd-bridge:local-flicd {
	Thing button flic_livingroom "Yellow Button Living Room" [address = "60:13:B3:02:18:BD"]
	Thing button flic_kitchen "Black Button Kitchen" [address = "B5:7E:59:78:86:9F"]
}
```

demo.items:

```
Dimmer Light_LivingRoom  { channel="milight:rgbLed:milight2:4:ledbrightness", channel="flicbutton:button:local-flicd:flic_livingroom:rawbutton" [profile="rawbutton-toggle-switch"], channel="flicbutton:button:local-flicd:flic_kitchen:rawbutton" [profile="rawbutton-toggle-switch"] }  // We have a combined kitchen / livingroom, so we control the living room lights with switches from the living room and from the kitchen
Switch Light_Kitchen    { channel="hue:group:1:kitchen-bulbs:switch", channel="flicbutton:button:local-flicd:flic_kitchen:rawbutton" [profile="rawbutton-toggle-switch"] }
```

### Configuration Example using Rules

It's also possible to setup [Rules](https://www.openhab.org/docs/configuration/rules-dsl.html).
The following rules help to initially test your setup as they'll trigger log messages on incoming events.

```
rule "Button rule using the button channel"

when
    Channel "flicbutton:button:local-flicd:flic_livingroom:button" triggered SHORT_PRESSED
then
    logInfo("Flic", "Flic 'short pressed' triggered")
end

rule "Button rule directly using the rawbutton channel"

when
    Channel "flicbutton:button:local-flicd:flic_livingroom:rawbutton" triggered
then
    logInfo("Flic", "Flic pressed: " + receivedEvent.event)
end
```
