# Changes

## 2.4.0-homekit-3

### Removed WindowCovering

WindowCovering support was removed as it causes the Homekit plugin to fail in catastrophic ways. Further investigation is required for why it failed so badly. Once it is resolved, it will be added back.

### Improved strategy for dealing with groups of items as a composite thing

The approach for dealing with grouped items has been refactored extensively, paving the way for devices to optionally include extra characteristics more easily.

Since item added/removed updates from OpenHab come in as a stream of additions and removals, one at a time, debouncing should be implemented in order to suppress the error messages of "device is lacking required characteristic" while OpenHab is in the process of reconfiguring some list of items. This will be implemented soon. For now, the functionality works, but the logs are a bit noisy.

### Support for optional battery levels with leak and motion sensors

With the refactoring, devices such as leak sensors and motion sensors can now optionally report when their battery is low. Battery level can be reported as a percentage (0-100, **not** 0.00 - 1.00), or as a switch (where ON == Low battery). More information on how this is configured can be seen in the README.

### Fix a characteristic subscription leak when the homekit addon is reloaded

Characteristic subscriptions would accumulate with each reload, causing noisy logs and a (not totally severe) memory leak. This release includes a build of the HAP-Java extension that has this issue patched. See https://github.com/beowulfe/HAP-Java/issues/54 for more information.


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
