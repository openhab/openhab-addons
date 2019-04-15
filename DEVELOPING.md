# Developer documentation

## Build system

### Adding dependencies

Generally all dependencies should be OSGi-bundles and available on JCenter.

*** External dependency ***
In most cases they should be referenced in the project POM with scope `provided`:

```
  <dependencies>
    <dependency>
      <groupId>foo.bar</groupId>
      <artifactId>baz</artifactId>
      <version>1.0.0</version>
      <scope>provided</provided>
    </dependency>
  </dependencies>
```

In most cases these dependencies need to be added to the `feature.xml`:

```
  <bundle dependency="true">mvn:foo.bar/baz/1.0.0</bundle>
```

If the bundle is not an OSGi-bundle, you need to wrap it and provide proper names

```
  <feature prerequisite="true">wrap</feature>
  <bundle dependency="true">wrap:mvn:foo.bar/baz/1.0.0$Bundle-Name=Foobar%20Baz%20Library&amp;Bundle-SymbolicName=foo.bar.baz&amp;Bundle-Version=1.0.0</bundle>
```

*** Internal dependency ***

In two cases libraries can be added to the `/lib` directory:
1. The bundle is not available for download
2. The bundle is not an OSGi bundle but needs to be uses for integration tests.
Unlike Karaf, BND is not able to wrap bundles on its own.
In this case the binding works as wrapper.
You need add the library and export all needed packages manually.

The build system automatically picks up all JAR files in `/lib` and embeds them in the new bundle.
In this case they must not be added to the feature.

If the bundles manifest is not properly exporting all needed packages, you can import them manually by adding 

```
  <properties>
    <bnd.importpackage>foo.bar.*,foo.baz.*</bnd.importpackage>
  </properties>
```

to the POM.

If the imported packages need to be exposed to other bundles (e.g. case 2 above), this has to be done manually by adding

```
  <properties>
    <bnd.exportpackage>foo.bar.*;version="1.0.0"</bnd.exportpackage>
  </properties>
```

to the POM.
If `version="1.0.0"` is not set, the packages are exported with the same version as the main bundle.

