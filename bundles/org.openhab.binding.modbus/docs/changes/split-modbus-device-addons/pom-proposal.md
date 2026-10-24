# Build Wiring Proposal (Not Applied)

Status: **waiting for human approval**.
Per project rules, `pom.xml` files are only changed by `$Architect` after explicit approval.
The change to `footer.xml` is part of the same set, because it only works together with the `pom.xml` change in step 4.
None of the five changes below is applied yet.
Do not build before all five are applied together, otherwise the Modbus features are missing from the add-on feature repository.

None of the changes adds a third-party dependency, so no license analysis applies.

## 1. New file: `bundles/org.openhab.binding.modbus.legacy/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">

  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.openhab.addons.bundles</groupId>
    <artifactId>org.openhab.addons.reactor.bundles</artifactId>
    <version>5.3.0-SNAPSHOT</version>
  </parent>

  <artifactId>org.openhab.binding.modbus.legacy</artifactId>

  <name>openHAB Add-ons :: Bundles :: Modbus Binding (All Devices)</name>

</project>
```

## 2. `bundles/pom.xml`: register the module

```diff
     <module>org.openhab.binding.modbus.lambda</module>
+    <module>org.openhab.binding.modbus.legacy</module>
     <module>org.openhab.binding.modbus.sbc</module>
```

## 3. `bom/openhab-addons/pom.xml`: register the artifact

```diff
       <artifactId>org.openhab.binding.modbus.lambda</artifactId>
       <version>${project.version}</version>
     </dependency>
+    <dependency>
+      <groupId>org.openhab.addons.bundles</groupId>
+      <artifactId>org.openhab.binding.modbus.legacy</artifactId>
+      <version>${project.version}</version>
+    </dependency>
```

## 4. `features/openhab-addons/pom.xml`: include the Modbus features in the aggregation

```diff
                     <exclude name="**/org.openhab.binding.bluetooth*/**/feature.xml"/>
-                    <exclude name="**/org.openhab.binding.modbus*/**/feature.xml"/>
                     <exclude name="**/org.openhab.binding.mqtt*/**/feature.xml"/>
```

The list `karaf-feature-verification` keeps `openhab-binding-modbus`.
That feature is now the deprecated aggregate, so the verification still resolves the core and all 11 device features.
The entry `openhab-binding-modbus` in the list now verifies the all-in-one feature; adding `openhab-binding-modbus-core` is optional.

## 5. `features/openhab-addons/src/main/resources/footer.xml`: remove the hand-written aggregate

Delete the whole block from `<feature name="openhab-binding-modbus" ...>` to its closing `</feature>` (12 `bundle` lines).
The feature with the same name is now defined in `org.openhab.binding.modbus.legacy/src/main/feature/feature.xml`.

## Rollback

Revert the five changes above.
The new `feature.xml` and `addon.xml` files in the bundles are inert without change 4.
