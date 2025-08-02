# openHAB Add-ons

<img align="right" width="220" src="logo.png" alt="openHAB logo" />

[![GitHub Actions Build Status](https://github.com/openhab/openhab-addons/actions/workflows/ci-build.yml/badge.svg?branch=main)](https://github.com/openhab/openhab-addons/actions/workflows/ci-build.yml)
[![Jenkins Build Status](https://ci.openhab.org/job/openHAB-Addons/badge/icon)](https://ci.openhab.org/job/openHAB-Addons/)
[![EPL-2.0](https://img.shields.io/badge/license-EPL%202-green.svg)](https://opensource.org/licenses/EPL-2.0)
[![Crowdin](https://badges.crowdin.net/openhab-addons/localized.svg)](https://crowdin.com/project/openhab-addons)

This repository contains the official set of add-ons that are implemented on top of openHAB Core APIs.
Add-ons that got accepted in here will be maintained (e.g. adapted to new core APIs)
by the [openHAB Add-on maintainers](https://github.com/orgs/openhab/teams/add-ons-maintainers).

To get started with add-on development, follow our guidelines and tutorials over at <https://www.openhab.org/docs/developer>.

If you are interested in openHAB Core development, we invite you to come by on <https://github.com/openhab/openhab-core>.

## Add-ons in other repositories

Some add-ons are not in this repository, but still part of the official [openHAB distribution](https://github.com/openhab/openhab-distro).
An incomplete list of other repositories follows below:

- <https://github.com/openhab/org.openhab.binding.zwave>
- <https://github.com/openhab/org.openhab.binding.zigbee>
- <https://github.com/openhab/openhab-webui>

## Development / Repository Organization

openHAB add-ons are [Java](https://en.wikipedia.org/wiki/Java_(programming_language)) `.jar` files.

The openHAB build system is based on [Maven](https://maven.apache.org/what-is-maven.html).
The official IDE (Integrated development environment) is Eclipse.

You find the following repository structure:

```text
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

```shell
mvn clean install
```

Most of the time you do not need to build all bindings, but only the binding you are working on.
To simply build only your binding use the `-pl` option.
For example to build only the astro binding:

```shell
mvn clean install -pl :org.openhab.binding.astro
```

If you have a binding that has dependencies that are dynamically as specified in the feature.xml you can create a `.kar` instead of a `.jar` file.
A `.kar` file will include the feature.xml and when added to openHAB will load and activate any dependencies specified in the feature.xml file.
To create a `.kar` file run Maven with the goal `karaf:kar`:

```shell
mvn clean install karaf:kar -pl :org.openhab.binding.astro
```

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
| `-pl :<add-on directory>`     | Build a single add-on                               |

For example you can skip checks and tests during development with:

```shell
mvn clean install -DskipChecks -DskipTests -pl :org.openhab.binding.astro
```

Adding these options improves the build time but could hide problems in your code.
Parallel builds are also less easy to debug and the increased load may cause timing sensitive tests to fail.

#### Translations

Add-on translations are managed via [Crowdin](https://crowdin.com/project/openhab-addons).
The English translation is taken from the openHAB-addons GitHub repo and automatically imported in Crowdin when changes are made to the English i18n properties file.
When translations are added or updated and approved in Crowdin, a pull request is automatically created by Crowdin.
Therefore translations should not be edited in the openHAB-addons repo, but only in Crowdin.
Otherwise translation are overridden by the automatic process.

To fill the English properties file run the following Maven command on an add-on:

```shell
mvn i18n:generate-default-translations
```

This command can also update the file when things or channel are added or updated.

In some cases the command does not work, and requires the full plug-in name.
In that case use:

```shell
mvn org.openhab.core.tools:i18n-maven-plugin:5.0.0:generate-default-translations
```

#### Code Quality

To check if your code is following the [code style](https://www.openhab.org/docs/developer/guidelines.html#b-code-formatting-rules-style) run:

```shell
mvn spotless:check
```

To reformat your code so it conforms to the code style you can run:

```shell
mvn spotless:apply
```

### Integration Tests

When your add-on also has an integration test in the `itests` directory, you may need to update the runbundles in the `itest.bndrun` file when the Maven dependencies change.
Maven can resolve the integration test dependencies automatically by executing:

```shell
mvn clean install -DwithResolver -DskipChecks
```

The build generates a `.jar` file per bundle in the respective bundle `/target` directory.

### How to develop via an Integrated Development Environment (IDE)

We have assembled some step-by-step guides for different IDEs on our developer documentation website:

<https://www.openhab.org/docs/developer/#setup-the-development-environment>

Happy coding!
