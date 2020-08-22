# Resol Binding

Resol Binding connects to Solar and System Controllers of RESOL - Elektronische Regelungen GmbH, also including branded versions from Viessmann, SOLEX, COSMO, SOLTEX, DeDietrich and many more.

This binding is based on and includes the [Resol-VBUS-Java library](https://github.com/danielwippermann/resol-vbus-java), developed by Daniel Wippermann.

## Supported Things

VBusLAN-Bridge, DataLogger DL2 and DL3 as a live data interface between LAN and Resol VBus.
On the DL3 currently there is only the first VBUS channel supported and the sensors directly connected tot he DL3 are not accessible via this binding.

On top of the bridge devices, which enables access to the VBUS many, if not all, Resol Controllers and Modules like WMZ heat meters, HKM Heating circuit extensions etc. are supported including branded versions from different suppliers.

## Discovery

Discovery is tested for VBus-LAN adapters DL2, DL3 and KM2 devices, it should also work for other devices providing a live data port.

## Binding Configuration

The Resol binding doesn't need any form of configuration in files.
-> ADD HERE the example config

## Thing Configuration

There are different things supported.
The most important and common thing is the VBUS Bridge, the device connecting your VBUS to Password for the VBusLAN needs to be configured!
The other options normally don't need to be touched.

For the Resol controller things nothing is configurable at the time of writing.

## Channels

Channels are dynamically created dependent on the devices connected to the VBus.
So far only reading is supported.
The classical channels are for temerature sensors and the like, but also relais outputs with the output level (0-100%) are visible as numerical values with the corresponding unit.
Some datapoints have an enumeration type and are available in two versions, a numerical and a textual channel.
Examples are Error mask, which is a number for the complete mask and each bit is available as single string channel, or the operation state of a heating circuit.
In those cases the numerical version is hidden and have to be view explicitly if needed, while the string representation has an "-str" suffix in the name.

String values are localized as far as possible, but only French, German and English are supported by the underlaying library which is based on the vbus-specification file from Resol.

Other types are most likely handled as strings and.

## Full Example

For a full description of the setup and examples follow the instructions in the [Getting Started](doc/GETTING_STARTED.md) document.

## Debugging

If something goes not as it should, try to enable the TRACE logging for the resol binding, by adding to /var/lib/openhab2/etc/org.ops4j.pax.logging.cfg

 # Resol logger
 log4j2.logger.resol.name = org.openhab.binding.resol
 log4j2.logger.resol.level = TRACE # set back to INFO or WARN after you made it work
 log4j2.logger.resol.appenderRefs = stdout
 log4j2.logger.resol.appenderRef.stdout.ref = STDOUT


## Status
So far I consider this binding as public beta, it is available via Eclipse IoT Marketplace and currently ongoing rework in a PR against the openhab2-addons repository to have it included there.

### Open Issues

Open issues are tracked via [tickets on github](https://github.com/ramack/openhab2-addons/issues). Please add new problems there and also add comments to existing ones.

Merge GETTING_STARTED.md here adn remove doc folder, as it is too detailed.