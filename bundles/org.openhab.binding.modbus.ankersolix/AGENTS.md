# Anker SOLIX Binding Development Guide

- This is a Modbus sub-binding and depends on the parent Modbus binding.
- Use Java 21 for builds and tests.
- Keep register mappings, channel metadata, README documentation, and tests synchronized.
- Protocol-sync provenance is documented in `DEVELOPERS.md`.
- Treat optional capability registers as optional: a failed read must not take the Thing offline.
- Preserve fail-open behavior when device capability cannot be determined.
- Validate capability-dependent commands before writing to the device.
- Run the binding-scoped Maven test command before completing changes:
  `./mvnw -f bundles/pom.xml -pl org.openhab.binding.modbus.ankersolix -Dspotbugs.skip=true test`
