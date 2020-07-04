# TouchWand Binding

Touchwand Wanderfull™ Hub basic is a plug & play Z-Wave based controller that uses Wi-Fi and Bluetooth to easily connect all smart home components.
TouchWand products are compatible with most major Z-Wave products, IP controlled devices and KNX devices, providing the ideal solution for building all-inclusive full-featured smart homes.
[TouchWand.com](http://www.touchwand.com)

|Touchwand Wanderfull™ Hub |![](http://www.touchwand.com/wp-content/uploads/2017/12/hub-toch-1.png)|

## Supported Things

TouchWandhWand Hub supports switches, shutters dimmers and wallcontrollers configured in TouchWand hub controller

## Control 

1. **Switch**  - control - ON/OFF
2. **Shutter** - control - UP/DOWN/STOP
3. **Dimmer**  - control - ON/OFF/BRIGHTNESS
4. **wallcontroller** - control - LONG/SHORT

## Discovery

After adding TouchWand Hub the auto discovery will add all switches dimmers and shutters to the inbox

## Binding Configuration

**Touchwand Wanderfull™** Hub Controller need to be added manually by IP address. The controller requires **username** and **password**  
Optional configuration

* Units status polling refresh time (default - 5 seconds)
* Discover secondary units (in case the controller is a primary controller) 
* Http port (default is 80)

## Thing Configuration

No thing configuration is needed

## Full Example

### touchwand.things

Things can be defined manually 
The syntax for touchwand this is 
  
```xtend
Thing <binding_id>:<type_id>:<thing_id> "Label" @ "Location"
```

Where <thing_id> is the unit id in touchwand hub.

```
Bridge touchwand:bridge:1921681116 [ipAddress="192.168.1.116", username="username" , password="password"]{
Thing switch 408 "Strairs light"
Thing switch 411 "South Garden light"
Thing dimmer 415 "Living Room Ceiling dimmer"
Thing switch 418 "Kitchen light"
Thing shutter 345 "Living Room North shutter"
Thing shutter 346 "Living Room South shutter"
}
```

### touchwand.items

```
/* Shutters */
Rollershutter   Rollershutter_345      "Living Room North shutter"    {channel="touchwand:shutter:1921681116:345:shutter"}
Rollershutter   Rollershutter_346      "Living Room South shutter"    {channel="touchwand:shutter:1921681116:346:shutter"}
```

```
/* Switches and Dimmers */
Switch  Switch_408      "Strairs light"                 {channel="touchwand:switch:1921681116:408:switch"}
Switch  Switch_411      "South Garden light"            {channel="touchwand:switch:1921681116:411:switch"}
Dimmer  Switch_415      "Living Room Ceiling dimmer"    {channel="touchwand:switch:1921681116:415:switch"}
Switch  Switch_418      "South Garden light"            {channel="touchwand:switch:1921681116:418:switch"}
```
