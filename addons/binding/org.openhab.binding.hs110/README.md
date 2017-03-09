# openhab2-addon-hs110
OpenHAB Addon for TP-Link HS100/HS110 (Development Project)


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

# Development

## Release Cycle

Include the Project into openhab2-addons using
`git subtree pull https://github.com/computerlyrik/openhab2-addon-hs110 --prefix addons/binding/org.openhab.binding.hs110 master` 

## Authors

 * Christian Fischer, 2017, https://github.com/computerlyrik
