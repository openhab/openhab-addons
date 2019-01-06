# Changes

## 2.4.0-homekit-1

Pre-release which contains several improvements for the 2.4.0 Homekit plugin. Supports the following new devices:

* Valves
* Motion sensors
* Leak sensors

(low battery alert and improved offline handling coming)

If you add motion sensors or leak sensors and they show they are unreachable, try toggling their value. For Z-wave devices the value is null until OpenHab2 receives the first value. In future release of the plugin the thing offline state will be used, and null item value will be interpreted as off.

## 2.4.0
