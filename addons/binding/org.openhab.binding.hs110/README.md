# openhab2-addon-hs110
OpenHAB Addon for TP-Link HS100/HS110


# Issues and Contribution
Development happens on the following github project:
https://github.com/computerlyrik/openhab2-addon-hs110

# Device Support and Auto-Discovery

## HS110 
- Auto-Update and Interact: Switching On/Off
- Auto-Update and Display: Current Wattage
- Auto-Update and Display: Decoded sysinfo

## HS100 
Should work, but Wattage will be UNDEF

## Auto-Discovery and Item Creation
Just use Paper-UI to set up your basic configuration.


# Known Bugs
I was not able to test this Plugin with HS100 Plugs.
The Discovery might work, but they will be registered as HS110 Plugs.

# FAQ

- Are other TP-Link devices supported?
Currently not, but if using the same communication protocol it should be easy to implement. Please open an issue at the development project.

# Development

## Building / Testing

Make sure you have all requirements met for [developing openhab2-addons](http://docs.openhab.org/developers/development/bindings.html).

* Use maven to build a package  `mvn clean package`
* Copy the resulting `.jar` from `target/` to your openhab-`addons/` directory.

## Release Cycle

Include the Project into openhab2-addons using
`git subtree pull --squash --prefix addons/binding/org.openhab.binding.hs110 https://github.com/computerlyrik/openhab2-addon-hs110  master` 

## Authors

 * Christian Fischer, 2017, https://github.com/computerlyrik
