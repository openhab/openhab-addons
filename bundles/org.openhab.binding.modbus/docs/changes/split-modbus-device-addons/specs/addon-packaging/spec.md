# Delta for Add-on Packaging

## ADDED Requirements

### Requirement: Core add-on without devices

The binding SHALL offer an add-on that provides the Modbus bridges, the poller and the generic data thing without any device-specific thing types.

#### Scenario: Install the core only

- GIVEN a runtime with no Modbus add-on installed
- WHEN the user installs the add-on "Modbus Core Binding"
- THEN the Modbus TCP and serial bridge, the poller and the generic data thing types are available
- AND no device-specific thing type (for example E3DC or SunSpec) is available

### Requirement: One add-on per device

The binding SHALL offer one add-on for each supported device family, and each of them SHALL install the core automatically.

#### Scenario: Install a single device

- GIVEN a runtime with no Modbus add-on installed
- WHEN the user installs the add-on "Modbus E3DC Binding"
- THEN the E3DC thing types are available
- AND the Modbus bridges and the generic data thing types are available
- AND the thing types of other devices (for example SunSpec) are not available

#### Scenario: Install two devices

- GIVEN a runtime with the add-on "Modbus E3DC Binding" installed
- WHEN the user installs the add-on "Modbus SunSpec Binding"
- THEN the thing types of both devices are available
- AND the Modbus core is installed once

#### Scenario: Every supported device has an entry

- GIVEN a release that contains this change
- WHEN the user lists the available add-ons
- THEN an entry exists for each of: Anker SOLIX, E3DC, Fox ESS, Helios easyControls, Kermi, Lambda, Saia Burgess Controls, Stiebel Eltron, Studer, Sungrow, SunSpec
- AND each entry name starts with "Modbus"

### Requirement: All-in-one add-on keeps its ID

The binding SHALL keep an add-on with the ID `modbus` that installs the core and all device extensions of the previous release, so that existing installations behave as before.

#### Scenario: Existing installation is upgraded

- GIVEN an installation with the add-on `modbus` and a configured E3DC thing
- WHEN the runtime is upgraded to a release containing this change
- THEN the core and all device extensions are installed as before
- AND the thing configuration is unchanged
- AND the E3DC thing initialises without any user action

#### Scenario: Lean installation

- GIVEN a runtime with no Modbus add-on installed
- WHEN the user installs only "Modbus E3DC Binding"
- THEN only the core and the E3DC extension are installed

#### Scenario: All-in-one content is frozen

- GIVEN a device extension added after this change
- WHEN the user installs the add-on `modbus`
- THEN the new device extension is not part of it

#### Scenario: Description points to the lean installation

- GIVEN a user browsing the available add-ons
- WHEN the user opens the entry with the ID `modbus`
- THEN its name states that it contains all devices
- AND the description refers to the Modbus Core Binding and the device add-ons

### Requirement: Add Thing for installed devices

The Main UI SHALL offer the thing types of the installed Modbus add-ons through the entry for the binding ID `modbus`.

#### Scenario: Add a device thing with the all-in-one add-on

- GIVEN the add-on `modbus` is installed
- WHEN the user opens the entry "Modbus Binding (All Devices)" in the list of installed bindings and chooses to add a thing
- THEN all device thing types and the Modbus bridges are offered

#### Scenario: Add a device thing in a lean installation (follow-up in the Main UI)

- GIVEN only "Modbus Core Binding" and "Modbus E3DC Binding" are installed
- WHEN the user chooses to add a thing for one of them
- THEN the E3DC thing types and the Modbus bridges are offered
- NOTE this needs a change in openhab-webui; until then the page `/settings/things/add/modbus` or a `.things` file is used

### Requirement: Old and new add-ons coexist

The binding MUST allow the all-in-one add-on and the individual add-ons to be installed at the same time.

#### Scenario: Both installed

- GIVEN the add-ons `modbus` and "Modbus E3DC Binding" are installed
- WHEN the runtime is started
- THEN every Modbus bundle is active exactly once

#### Scenario: All-in-one add-on removed

- GIVEN the add-ons `modbus` and "Modbus E3DC Binding" are installed
- WHEN the user uninstalls the add-on `modbus`
- THEN the E3DC thing types and the Modbus bridges remain available
- AND the thing types of devices that are not installed individually are no longer available

### Requirement: Identifiers are stable

The binding MUST keep all thing type IDs, channel IDs and the binding ID `modbus` unchanged.

#### Scenario: Existing things and links survive

- GIVEN things and item links created before this change
- WHEN the runtime is upgraded and the corresponding add-ons are installed
- THEN all thing UIDs and channel links remain valid without modification
