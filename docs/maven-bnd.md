# Migration to a Maven + Bnd based build system

We are in the process of changing the build system.
We are not accepting new pull requests that are still using the old way from here on.

## Background

openHAB is a java project that is build around the OSGi framework. openHAB add-ons are OSGi bundles.

Over the years the [bnd software](https://bnd.bndtools.org/) ("bnd is the engine behind a number of popular software development tools that support OSGi") has established itself as the defacto standard for creating OSGi bundles.

The openHAB maintainers have decided to move from the very Eclipse centric buildsystem to maven/bnd. A combination that not only allows to develop bindings seamlessly in other IDEs than Eclipse, but Java beginners and intermediates find a commonly known maven buildsystem.

## Advantages of the new buildsystem

* Builds faster
* Offline mode builds
* Less buildsystem file overhead
* External dependencies per extension via the standard maven dependency system
* Unit tests are now part of the same project (Eclipse allows to create a unit test for a class via GUI now)

## How to migrate

1. Move your add-on to `bundles/`. Adapt the `addons/pom.xml` and `bundles/pom.xml`
2. Copy over the `.classpath` from an already migrated bundle
3. Copy over `.project` and adapt the `name`.
4. Copy over the `pom.xml` from an already migrated bundle: Adapt the `name` and `artifactId`.
5. Add maven dependencies to the `pom.xml` file if your add-on requires dependencies like so:
    ```xml
    <dependencies>
        <dependency>
        <groupId>com.sun.xml.bind</groupId>
        <artifactId>jaxb-impl</artifactId>
        <version>2.2.11</version>
        </dependency>
    </dependencies>
    ```
    Remove those included jar files. ONLY IF you can't find a maven central bundle you can keep the jar file in the `lib/` directory.
6. Remove `META-INF` and `OSGI-INF` and `build.properties`
7. Move `ESH-INF` to `src/main/resources/ESH-INF`
8. Move unit tests from a potential separate `.test` project into `src/test`.
9. Move integration tests into a new bundle, see the [`itests/`](https://github.com/openhab/openhab2-addons/tree/master/itests) directory for examples.
10. Change [`CODEOWNERS`](https://github.com/openhab/openhab2-addons/tree/master/CODEOWNERS) (this notifies maintainers when a pull request happens on their binding)
11. Adapt [`bom/openhab-addons/pom.xml`](https://github.com/openhab/openhab2-addons/tree/master/bom/openhab-addons/pom.xml)
   * Replace "org.openhab.binding" with "org.openhab.addons.bundles" for your add-on entry
12. Adapt [`features/karaf/openhab-addons/src/main/feature/feature.xml`](https://github.com/openhab/openhab2-addons/tree/master/features/karaf/openhab-addons/src/main/feature/feature.xml) like so:
   Replace "org.openhab.binding" with "org.openhab.addons.bundles" for your add-on entry.
   If you have external dependencies, you must list them here, too. An example:
   ```xml
      <feature name="openhab-binding-lgwebos" description="LG webOS Binding" version="${project.version}">
           ...
          <bundle dependency="true">mvn:org.apache.httpcomponents/httpclient-osgi/4.2.3</bundle>
          <bundle start-level="80">mvn:org.openhab.binding/org.openhab.binding.lgwebos/${project.version}</bundle>
      </feature>
   ```
   There are two types of external libraries. Those that are ready for OSGi (like the one above) and those that aren't. The latter ones are listed with a **wrap** prefix keyword like this:
   ```xml
    <feature prerequisite="true">wrap</feature>
    <bundle dependency="true">wrap:mvn:io.netty/netty-common/4.1.34.Final$Bundle-Name=Netty%20Common&amp;Bundle-SymbolicName=io.netty.netty-common&amp;Bundle-Version=4.1.34</bundle>
   ```
   Please ask for details and review if in doubt.

There is a bash script to automatically migrate a binding. Run this script from the openhab2-addons directory: https://gist.github.com/Hilbrand/a7127d0fb7afd36124a78a248453ff03. The script doesn't migrate test projects and also locally added library in a lib folder need to migrated manually.

#### Examples

* for an add-on with lib directory: https://github.com/openhab/openhab2-addons/tree/master/bundles/org.openhab.binding.allplay

## How to build on the command line

Enter `mvn clean install -DskipChecks` in the project directory or itest project directory.

Subsequent calls can include the `-o` for offline.

For integration tests you might need to run: `mvn clean install -DwithResolver`.

Skip checks and tests if you just want to test the compilation process and perform the command from within the directory of the bundle that you want to build:

`mvn clean install -DskipChecks -DskipTests -o` 

## How to develop in the Eclipse IDE

1. Install Bndtools in your Eclipse IDE. (Is automatically installed if you have used the Eclipse Installer for openHAB development.)
2. Checkout the bnd based openHAB demo application: `git clone --depth=1 https://github.com/maggu2810/openhab-demo`.
3. Open the directory in Eclipse. Wait for the download and build to finish (about 3-5 minutes).
4. Checkout openhab2-addons: `git clone --depth=1 https://github.com/openhab/openhab2-addons`
5. In Eclipse File, Import, "Existing maven projects": Add your migrated project (not all projects!) to the workspace.
6. In Eclipse Package Explorer: Search for `pom.xml` in the demo-app project.
    ![Bildschirmfoto vom 2019-03-19 13-46-48](https://user-images.githubusercontent.com/66436/54607049-a9031700-4a4d-11e9-9b9d-64a620270d28.png)
    Add your add-on as maven dependency like so:
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
7. In Eclipse Package Explorer: Search for `app.bndrun` in the demo-app project. Double click (takes a few seconds).
8. Add your project to "Run requirements" via drag&drop from the package explorer.
    ![Bildschirmfoto vom 2019-03-18 12-26-03](https://user-images.githubusercontent.com/66436/54527103-2c066d80-4979-11e9-8852-c06a41f4d50b.png)
9. Execute with "Run OSGi"

The demo application runs a slim set of openHAB core bundles including automations (next gen rules) and PaperUI (http://127.0.0.1:8080/paperui/). The startup should only take about 5 seconds and you are greeted by the openHAB console where you can type in console commands.

