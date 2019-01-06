# Changes

## 2.4.0-homekit-2

Merge in [Epike's WindowCovering patch](https://github.com/epike/openhab2-addons/commit/8a9ca1b2d22f8a8b589dd9e3106ba618471511ab). The more-generic, Apple Homekit friendly label WindowCovering is used rather than Blinds. Users upgrading from Epike's patch should update their labels (Don't include both labels simultaneously, as `Blinds` is still supported, while deprecated).

## 2.4.0-homekit-1

Pre-release which contains several improvements for the 2.4.0 Homekit plugin. Supports the following new devices:

* Valves
* Motion sensors
* Leak sensors

(low battery alert and improved offline handling coming)

If you add motion sensors or leak sensors and they show they are unreachable, try toggling their value. For Z-wave devices the value is null until OpenHab2 receives the first value. In future release of the plugin the thing offline state will be used, and null item value will be interpreted as off.

## 2.4.0
