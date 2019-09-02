# openHAB 2 Add-ons

<img align="right" width="220" src="./logo.png" />

[![Build Status](https://travis-ci.org/openhab/openhab2-addons.svg)](https://travis-ci.org/openhab/openhab2-addons)
[![EPL-2.0](https://img.shields.io/badge/license-EPL%202-green.svg)](https://opensource.org/licenses/EPL-2.0)
[![Bountysource](https://www.bountysource.com/badge/tracker?tracker_id=2164344)](https://www.bountysource.com/teams/openhab/issues?tracker_ids=2164344)

This repository contains the official set of add-ons that are implemented on top of openHAB 2 Core APIs.
Add-ons that got accepted in here will be maintained (e.g. adapted to new core APIs)
by the [openHAB 2 maintainers](https://github.com/orgs/openhab/teams/2-x-add-ons-maintainers).

To get started with binding development, follow our guidelines and tutorials over at https://www.openhab.org/docs/developer.

If you are interested in openHAB 2 Core development, we invite you to come by on https://github.com/openhab/openhab-core.

## Add-ons in other repositories

Some add-ons are not in this repository, but still part of the official [openHAB 2 distribution](https://github.com/openhab/openhab-distro).
An incomplete list of other repositories follows below:

* https://github.com/openhab/org.openhab.binding.zwave
* https://github.com/openhab/org.openhab.binding.zigbee
* https://github.com/openhab/openhab-webui

## Development / Repository Organization

openHAB 2 add-ons are [Java](https://en.wikipedia.org/wiki/Java_(programming_language)) `.jar` files.

The openHAB 2 build system is based on [Maven](https://maven.apache.org/what-is-maven.html).
The official IDE (Integrated development environment) is Eclipse.

You find the following repository structure:

```
.
+-- bom       Maven buildsystem: Bill of materials
|   +-- openhab-addons  Lists all extensions for other repos to reference them
|   +-- ...             Other boms
|
+-- bundles   Official openHAB extensions
|   +-- org.openhab.binding.airquality
|   +-- org.openhab.binding.astro
|   +-- ...
|
+-- features  Part of the runtime dependency resolver ("Karaf features")
|
+-- itests    Integration tests. Those tests require parts of the framework to run.
|   +-- org.openhab.binding.astro.tests
|   +-- org.openhab.binding.avmfritz.tests
|   +-- ...
|
+-- src/etc   Auxilary buildsystem files: The license header for automatic checks for example
+-- tools     Static code analyser instructions
|
+-- CODEOWNERS  This file assigns people to directories so that they are informed if a pull-request
                would modify their addons.
```

### Command line build

To build all add-ons from the command-line, type in:

`mvn clean install`

Optionally you can skip tests (`-DskipTests`) or skip some static analysis (`-DskipChecks`) this does  improve the build time but could hide problems in your code. For binding development you want to run that command without skipping checks and tests.

Subsequent calls can include the `-o` for offline as in: `mvn clean install -DskipChecks -o` which will be a bit faster.

For integration tests you might need to run: `mvn clean install -DwithResolver -DskipChecks`

You find a generated `.jar` file per bundle in the respective bundle `/target` directory.

### How to develop via an Integrated Development Environment (IDE)

We have assembled some step-by-step guides for different IDEs on our developer documentation website:

https://www.openhab.org/docs/developer/#setup-the-development-environment

Happy coding!
