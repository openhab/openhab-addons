# openHAB Hue Emulation Service

Hue Emulation exposes openHAB items as Hue devices to other Hue HTTP API compatible applications like an Amazon Echo.  

## Features:

*   UPNP automatic discovery
*   Support ON/OFF and Percent/Decimal item types
*   Can expose any type of item, not just lights
*   Pairing (security) can be enabled/disabled in real time using the configuration service (under services in the PaperUI for example)  

## Configuration:

Pairing can be turned on and off:

```
org.openhab.hueemulation:pairingEnabled=false
```

(Optional) For systems with multiple IP addresses the IP to use for UPNP may be specified, otherwise the first non loopback address will be used.

```
org.openhab.hueemulation:discoveryIp=192.168.1.100
```

## Device Tagging

To expose an item on the service, apply a supported tag (which are "Lighting", "Switchable", "TargetTemperature") to it.
The item label will be used as the Hue device name.

```
Switch  TestSwitch1     "Kitchen Switch" [ "Switchable" ]
Switch  TestSwitch2     "Bathroom" [ "Lighting" ]
Dimmer  TestDimmer3     "Hallway" [ "Lighting" ]
Number  TestNumber4     "Temperature Set Point" [ "TargetTemperature" ]
```
