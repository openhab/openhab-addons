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

## Development / Repository Organisation

openHAB 2 add-ons are [Java](https://en.wikipedia.org/wiki/Java_(programming_language)) `.jar` files.

The openHAB 2 build system is based on [maven](https://maven.apache.org/what-is-maven.html).
The official IDE (Integrated development environment) is Eclipse.

You find the following repository structure:

```
.
+-- bom      Maven buildsystem: Bill of materials
|   +-- openhab-addons  Lists all extensions for other repos to reference them
|   +-- ...             Other boms
|
+-- bundles  Official openHAB extensions
|   +-- org.openhab.binding.airquality
|   +-- org.openhab.binding.astro
|   +-- ...
|
+-- features/       An extension usually has dependencies (at least openHAB core).
|            |      In those feature files are the dependencies for the OSGi container declared.
|            +-- openhab-addons-external/src/main/feature/feature.xml
|            +-- openhab-addons/src/main/feature/feature.xml
|
+-- itests   Integration tests. Those tests require parts of the framework to run.
|   +-- org.openhab.binding.astro.tests
|   +-- org.openhab.binding.avmfritz.tests
|   +-- ...
|
+-- src/etc  Auxilary buildsystem files: The license header for automatic checks for example
+-- tools    Static code analyser instructions
|
+-- CODEOWNERS  This file assigns people to directories so that they are informed if a pull-request
                would modify that directory/binding.
```

### Command line build

To build all add-ons from the command-line, type in:

`mvn clean install`

Optionally you can skip tests (`-DskipTests`) or skip some static analysis (`-DskipChecks`) this does  improve the build time but could hide problems in your code. For binding development you want to run that command without skipping checks and tests.

Subsequent calls can include the `-o` for offline as in: `mvn clean install -DskipChecks -o` which will be a bit faster.

For integration tests you might need to run: `mvn clean install -DwithResolver -DskipChecks`.

### How to develop in the Eclipse IDE

This is a temporary guide until we came up with a new IDE setup.

1. Install Bndtools in your Eclipse IDE.
2. Checkout the openHAB demo application: `git clone --depth=1 https://github.com/maggu2810/openhab-demo`.
3. Import the directory: In Eclipse File->Import->"Existing maven projects".
   Wait for the download and build to finish (about 3-5 minutes).
4. Checkout this repository: `git clone --depth=1 https://github.com/openhab/openhab2-addons`
5. In Eclipse File->Import->"Existing maven projects": Add the binding you want to develop to the workspace. (Create a new binding by copying an existing one.)
6. In Eclipse Package Explorer: Search for `pom.xml` in the demo-app project.
    ![Bildschirmfoto vom 2019-03-19 13-46-48](https://user-images.githubusercontent.com/66436/54607049-a9031700-4a4d-11e9-9b9d-64a620270d28.png)
    Add your addon as maven dependency like so (replace `astro`!):
   ```xml
   <project ...>
     ...
     <dependencies>
        <dependency>
            <groupId>org.openhab.addons.bundles</groupId>
            <artifactId>org.openhab.binding.astro</artifactId>
            <version>${project.version}</version>
            <scope>runtime</scope>
        </dependency>
     </dependencies>
   </project>
   ```
7. In Eclipse Package Explorer: Search for `app.bndrun` in the "demo-app" project.
   Double click (takes a few seconds).
8. Add your project to "Run requirements" via drag&drop from the Package Explorer.
    ![Bildschirmfoto vom 2019-03-18 12-26-03](https://user-images.githubusercontent.com/66436/54527103-2c066d80-4979-11e9-8852-c06a41f4d50b.png)
9. Execute with "Run OSGi"

The demo application runs a slim set of openHAB core bundles including automations (next gen rules) and PaperUI. The startup should only take about 5 seconds and you are greeted by the openHAB console where you can type in console commands.

Happy coding!
