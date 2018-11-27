# openHAB Hue Emulation Service

Hue Emulation exposes openHAB items as Hue devices to other Hue HTTP API compatible applications like an Amazon Echo.  

## Discovery:

As soon as the binding is enabled, it will announce the presence of an (emulated) HUE bridge of the second generation (square bridge).
Hue bridges are using the Universal Plug and Play (UPnP) protocol for discovery.

Like the real HUE bridge the service must be put into pairing mode before other applications can access it. 

## Exposed devices

It is important to note that you are exposing *Items* not *Things* or *Channels*.
Only Color, Dimmer and Switch type *Items* are supported.

This service can emulate 3 different devices:

* An OSRAM SMART+ Plug,
* a dimmable white color Philips A19 bulb and
* an a Philips Gen 3 LCT010 extended color bulb.

The exposed Hue-type depends on some criteria:

* If the item has the category "ColorLight": It will be exposed as a color bulb
* If the item has the category "Light": It will be exposed as a dimmable white bulb.

This initial type determination is overriden if the item is tagged.
Tags can be configured in Paper UI, please refer to the next section.

The following default tags are setup:
* "Switchable": Item will be exposed as a switchable
* "Lighting": Item will be exposed as a dimmable white bulb
* "ColorLighting": Item will be exposed as a color bulb

It is the responsibility of binding developers to categories and default tag their
available *Channels*, so that linked Items are automatically exposed with this service.

## Exposed names

Your items labels are used for exposing! The default naming schema in Paper UI
for automatically linked items unfortunately names *Items* like their Channel names,
so usually "Brightness" or "Color". You want to rename those!

## Configuration:

All options are available in Paper UI.

Pairing can be turned on and off:

```
org.openhab.hueemulation:pairingEnabled=false
```

You can define a pairing timeout in seconds.
After that timeout, the `pairingEnabled` is automatically set to `false`.

```
org.openhab.hueemulation:pairingTimeout=60
```

For systems with multiple IP addresses the IP to use for UPNP may optionally be specified.
Otherwise the first non loopback address will be used.

```
org.openhab.hueemulation:discoveryIp=192.168.1.100
```

One of the comma separated tags must match for the item to be exposed. Can be empty to match every item.

```
org.openhab.hueemulation:restrictToTagsSwitches=Switchable
org.openhab.hueemulation:restrictToTagsWhiteLights=Lighting
org.openhab.hueemulation:restrictToTagsColorLights=ColorLighting
```


## Text configuration example

The item label will be used as the Hue device name. 

```
Switch  TestSwitch      "Kitchen Switch" [ "Switchable" ]
Color   TestColorBulb   "Bathroom"       [ "ColorLighting" ]
Dimmer  TestDimmer      "Hallway"        [ "Lighting" ]
```
