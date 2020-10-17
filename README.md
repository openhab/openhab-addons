# openHAB Add-ons

<img align="right" width="220" src="./logo.png" />

[![Build Status](https://travis-ci.com/openhab/openhab-addons.svg)](https://travis-ci.com/openhab/openhab-addons)
[![EPL-2.0](https://img.shields.io/badge/license-EPL%202-green.svg)](https://opensource.org/licenses/EPL-2.0)
[![Bountysource](https://www.bountysource.com/badge/tracker?tracker_id=2164344)](https://www.bountysource.com/teams/openhab/issues?tracker_ids=2164344)

This repository contains the official set of add-ons that are implemented on top of openHAB Core APIs.
Add-ons that got accepted in here will be maintained (e.g. adapted to new core APIs)
by the [openHAB Add-on maintainers](https://github.com/orgs/openhab/teams/add-ons-maintainers).

To get started with binding development, follow our guidelines and tutorials over at https://www.openhab.org/docs/developer.

If you are interested in openHAB Core development, we invite you to come by on https://github.com/openhab/openhab-core.

## Add-ons in other repositories

Some add-ons are not in this repository, but still part of the official [openHAB distribution](https://github.com/openhab/openhab-distro).
An incomplete list of other repositories follows below:

* https://github.com/openhab/org.openhab.binding.zwave
* https://github.com/openhab/org.openhab.binding.zigbee
* https://github.com/openhab/openhab-webui

## Development / Repository Organization

openHAB add-ons are [Java](https://en.wikipedia.org/wiki/Java_(programming_language)) `.jar` files.

The openHAB build system is based on [Maven](https://maven.apache.org/what-is-maven.html).
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
                would modify their add-ons.
```

### Command line build

To build all add-ons from the command-line, type in:

`mvn clean install`

To improve build times you can add the following options to the command:

| Option                        | Description                                         |
| ----------------------------- | --------------------------------------------------- |
| `-DskipChecks`                | Skip the static analysis (Checkstyle, FindBugs)     |
| `-DskipTests`                 | Skip the execution of tests                         |
| `-Dmaven.test.skip=true`      | Skip the compilation and execution of tests         |
| `-Dfeatures.verify.skip=true` | Skip the Karaf feature verification                 |
| `-Dspotless.check.skip=true`  | Skip the Spotless code style checks                 |
| `-o`                          | Work offline so Maven does not download any updates |
| `-T 1C`                       | Build in parallel, using 1 thread per core          |

For example you can skip checks and tests during development with:

`mvn clean install -DskipChecks -DskipTests`

Adding these options improves the build time but could hide problems in your code.
Parallel builds are also less easy to debug and the increased load may cause timing sensitive tests to fail.

To check if your code is following the [code style](https://www.openhab.org/docs/developer/guidelines.html#b-code-formatting-rules-style) run: `mvn spotless:check`
To reformat your code so it conforms to the code style you can run: `mvn spotless:apply`

When your add-on also has an integration test in the `itests` directory, you may need to update the runbundles in the `itest.bndrun` file when the Maven dependencies change.
Maven can resolve the integration test dependencies automatically by executing: `mvn clean install -DwithResolver -DskipChecks`

The build generates a `.jar` file per bundle in the respective bundle `/target` directory.

### How to develop via an Integrated Development Environment (IDE)

We have assembled some step-by-step guides for different IDEs on our developer documentation website:

https://www.openhab.org/docs/developer/#setup-the-development-environment

Happy coding!
