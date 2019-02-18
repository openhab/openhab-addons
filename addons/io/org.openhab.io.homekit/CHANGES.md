# Changes

## 2.4.0-homekit-6

### Add support for smoke and CO detectors

[Cody Cutrer](https://github.com/ccutrer) has added support for Smoke and CO detectors. See the README for instructions on configuring.

### HAP-Java fixes

Several fixes HAP-Java from Cody Cutrer have been included in the release:

* [Fix reading encrypted frames that don't line up with network frames](https://github.com/beowulfe/HAP-Java/pull/64)
* [Bring HAP-Java in to compliance with the spec](https://github.com/beowulfe/HAP-Java/pull/65)
* [Some optimizations to HAP-Java responses](https://github.com/beowulfe/HAP-Java/pull/65)
* [Fix a pairing issue in which HAP-Java could listen on the wrong interface](https://github.com/beowulfe/HAP-Java/pull/67)

### Artifact is now versioned

Per request, the artifact is versioned with each milestone release.

### Update on merging changes

With the next HAP-Java release around the corner, I will rebase this branch on to 2.5.0 and start to integrate changes in to master. Further releases are likely to be based on 2.5.0; from what I understand we're to expect little issues running a plugin built for 2.5.x on 2.4.x, so long as no 2.5.x functionality is used.

## 2.4.0-homekit-5

### Breaking changes

The following config options have been renamed:

* `org.openhab.homekit:thermostatCoolMode` to `org.openhab.homekit:thermostatTargetModeCool`
* `org.openhab.homekit:thermostatHeatMode` to `org.openhab.homekit:thermostatTargetModeHeat`
* `org.openhab.homekit:thermostatAutoMode` to `org.openhab.homekit:thermostatTargetModeAuto`
* `org.openhab.homekit:thermostatOffMode` to `org.openhab.homekit:thermostatTargetModeOff`

Further, the following required config options have been specified:

* `org.openhab.homekit:thermostatCurrenModeCooling`
* `org.openhab.homekit:thermostatCurrenModeHeating`
* `org.openhab.homekit:thermostatCurrenModeOff`


You will need to update your homekit configuration accordingly, either by editing your homekit config file, or by editing the configuration for the IO service using the paper UI.

### Thermostat fixes

Previously, the mapping of target and current thermostat mode was broken. It is considered illegal to return current mode of "AUTO", and resulted in an error. The issue has been fixed in my fork of homekit (see https://github.com/beowulfe/HAP-Java/issues/60)

Support for an item indicating the thermostat current mode has been added.

The homekit plugin configuration screen for paper-ui has been improved, adding groupings for each set of mappings.

The tag `homekit:HeatingCoolingMode` has been changed to `homekit:TargetHeatingCoolingMode`. The old tag is supported still, but you should update your thermostat tagged item, respectively.

## 2.4.0-homekit-4

### Allow motion and leak accessories to be backed by Contact items

Some Z-wave devices configure these things to expose their channels as an OpenClosedType, rather than OnOffType, due to the fact that Contact items are read-only. In this release, we allow these boolean-like status accessories to be backed by either a Switch or a Contact item.

For Motion sensors, Open is considered "motion detected" (think "window open" as the actionable event). Similarly, leaks are reported if the backing Item is Open.

### Debounce the refreshing of Homekit items

The plugin now waits for items to be stable for a full second before creating, removing, and deleting the associated homekit accessories. This greatly reduces the chaos, overhead, and misleading log messages when adding, removing, or changing multiple items at a time.

### Contact and Occupancy Sensors support

Support is added for contact and occupancy sensors.

### WindowCovering is brought back

I have fixed the issue with WindowCovering in my upstream fork of HAP-Java. WindowCovering now functions properly and has been re-enabled.

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
