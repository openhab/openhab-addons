# openHAB Hue Emulation Service

Hue Emulation exposes openHAB items as Hue devices to other Hue HTTP API compatible applications like an Amazon Echo, Google Home or
any Hue compatible application.

Because Amazon Echo and Google Home control openHAB locally this way, it is a fast and reliable way
to voice control your installation. See the Troubleshoot section down below though.

## Discovery:

As soon as the binding is enabled, it will announce the presence of an (emulated) HUE bridge of the second generation (square bridge).
Hue bridges are using the Universal Plug and Play (UPnP) protocol for discovery.

Like the real HUE bridge the service must be put into pairing mode before other applications can access it. 
By default the pairing mode disables itself after 1 minute (can be configured).

## Exposed devices

It is important to note that you are exposing *Items* not *Things* or *Channels*.
Only Color, Dimmer and Switch type *Items* are supported.

This service can emulate 3 different devices:

* An OSRAM SMART+ Plug,
* a dimmable white color Philips A19 bulb and
* an a Philips Gen 3 LCT010 extended color bulb.

The exposed Hue-type depends on some criteria:

* If the item has the category "ColorLight": It will be exposed as a color bulb
* If the item has the category "Light": It will be exposed as a switch.

This initial type determination is overridden if the item is tagged.
Tags can be configured in Paper UI, please refer to the next section.

The following default tags are setup:
* "Switchable": Item will be exposed as an OSRAM SMART+ Plug
* "Lighting": Item will be exposed as a dimmable white bulb
* "ColorLighting": Item will be exposed as a color bulb

It is the responsibility of binding developers to categories and default tag their
available *Channels*, so that linked Items are automatically exposed with this service.

You can tag items manually though as well.

## Exposed names

Your items labels are used for exposing! The default naming schema in Paper UI
for automatically linked items unfortunately names *Items* like their Channel names,
so usually "Brightness" or "Color". You want to rename those.

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

To create an api key on the fly, you can set the following option.

Necessary for Amazon Echos and other devices where the API key cannot be reset.
After a new installation of openHAB or a configuration pruning the old
API keys are gone but the Echos will keep trying with their invalid keys.

```
org.openhab.hueemulation:createNewUserOnEveryEndpoint=false
```

For systems with multiple IP addresses the IP to expose via UPNP may optionally be specified.
Otherwise the openHAB configured primary address will be used.
Usually you do not want to set this option, but change the primary address configuration of openHAB.

```
org.openhab.hueemulation:discoveryIp=192.168.1.100
```

One of the comma separated tags must match for the item to be exposed.
Can be empty to match an item based on the other criteria.

```
org.openhab.hueemulation:restrictToTagsSwitches=Switchable
org.openhab.hueemulation:restrictToTagsWhiteLights=Lighting
org.openhab.hueemulation:restrictToTagsColorLights=ColorLighting
```

## Troubleshooting

Some devices like the Amazon Echo, Google Home and all Philips devices expect a Hue bridge to
run on port 80. You must either port forward your openHAB installation to port 80, install
a reverse proxy on port 80 or let openHAB run on port 80.

You can test if the hue emulation does its job by enabling pairing mode including the option
"Amazon Echo device discovery fix".

1. Navigate with your browser to "http://your-openhab-ip/description.xml" to check the discovery
   response. Check the IP address in there.
2. Navigate with your browser to "http://your-openhab-ip/api/testuser/lights?debug=true"
   to check all exposed lights and switches.

## Text configuration example

The item label will be used as the Hue device name. 

```
Switch  TestSwitch      "Kitchen Switch" [ "Switchable" ]
Color   TestColorBulb   "Bathroom"       [ "ColorLighting" ]
Dimmer  TestDimmer      "Hallway"        [ "Lighting" ]
```
