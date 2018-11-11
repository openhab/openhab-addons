# openHAB Hue Emulation Service

Hue Emulation exposes openHAB items as Hue devices to other Hue HTTP API compatible applications like an Amazon Echo.  

## Discovery:

As soon as the binding is enabled, it will announce the presence of an (emulated) HUE bridge.
Like the real HUE bridge the service must be put into pairing mode before other applications can access it. 

## Supported item types

Can expose Color, Dimmer, Switch type items. The exposed type depends on some criteria:

* If the item has the category "ColorLight": It will be exposed as a color bulb
* If the item has the category "Light": It will be exposed as a white bulb.

This initial type determination is overriden if the item is tagged. Restriction tags can be configured
in PaperUI, please refer to the next section.

The following default tags are recognised:
* "Switchable": Item will be exposed as a switchable
* "Lighting": Item will be exposed as a white bulb
* "ColorLighting": Item will be exposed as a color bulb

## Configuration:

All options are available in PaperUI.

Pairing can be turned on and off:

```
org.openhab.hueemulation:pairingEnabled=false
```

(Optional) For systems with multiple IP addresses the IP to use for UPNP may be specified, otherwise the first non loopback address will be used.

```
org.openhab.hueemulation:discoveryIp=192.168.1.100
```

One of the comma separated tags must match for the item to be exposed. Can be empty to match every item.

```
org.openhab.hueemulation:restrictToTagsSwitches=Switchable
org.openhab.hueemulation:restrictToTagsWhiteLights=Lighting
org.openhab.hueemulation:restrictToTagsColorLights=ColorLighting
```


## Example

The item label will be used as the Hue device name. 

```
Switch  TestSwitch1     "Kitchen Switch" [ "Switchable" ]
Switch  TestSwitch2     "Bathroom" [ "Lighting" ]
Dimmer  TestDimmer3     "Hallway" [ "Lighting" ]
Number  TestNumber4     "Temperature Set Point" [ "TargetTemperature" ]
```
