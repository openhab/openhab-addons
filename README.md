# openHAB 2 Add-ons

This repository contains the official set of add-ons that are implemented on top of openHAB 2 Core APIs.
Add-ons that got accepted in here will be maintained (e.g. adapted to new core APIs)
by the [openHAB 2 maintainers](https://github.com/orgs/openhab/teams/2-x-add-ons-maintainers).

To get started with binding development, follow our guidelines and tutorials over at https://www.openhab.org/docs/developer/.

If you are interested in openHAB 2 Core development, we invite you to come by on https://github.com/openhab/openhab-core.

## Add-ons in other repositories

Some add-ons are not in this repository, but still part of the official [openHAB 2 distribution](https://github.com/openhab/openhab-distro).
An incomplete list of other repositories follows below:

* https://github.com/openhab/org.openhab.binding.zwave
* https://github.com/openhab/org.openhab.binding.zigbee
* https://github.com/openhab/openhab-webui

## Build

openHAB 2 add-ons are [Java](https://en.wikipedia.org/wiki/Java_(programming_language)) `.jar` files.

The openHAB 2 build system is based on [maven](https://maven.apache.org/what-is-maven.html).
The official IDE (Integrated development environment) is Eclipse.

To build all add-ons from the command-line, type in:
`mvn clean install`

If you prefer an IDE, follow the instructions on https://www.openhab.org/docs/developer/development/ide.html
