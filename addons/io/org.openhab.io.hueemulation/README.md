

#openHAB Hue Emulation Service

Hue Emulation exposes openHAB items as Hue devices to other Hue HTTP API compatible applications like an Amazon Echo.  

##Features:
* UPNP automatic discovery 
* Support ON/OFF and Percent/Decimal item types
* Can expose any type of item, not just lights
* Pairing (security) can be enabled/disabled in real time using the configuration service (under services in the PaperUI for example)  

##Configuration:
Pairing can be turned on and off:

```
org.openhab.hueemulation:pairingEnabled=false
```
##Device Taging
To expose an item on the service apply any Apple HomeKit style tag to it.  The item label will be used as the Hue Device name.
```
Switch  TestSwitch1     "Kitchen Switch" ["homekit:Switch"]
Switch  TestSwitch2     "Bathroom" ["homekit:Lightbulb"]
Dimmer  TestDimmer3     "Hallway" ["homekit:DimmableLightbulb"]
Number  TestNumber4     "Cool Set Point" ["homekit:coolingThreshold"]
```