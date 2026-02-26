# YAML Composer

YAML Composer introduces extended YAML features that make openHAB configuration more modular, reusable, and maintainable. These features let you structure configuration as composable building blocks rather than large, repetitive files.

The add-on loads enhanced-syntax YAML files from `OPENHAB_CONF/yamlcomposer/` and compiles them into fully resolved plain YAML written to `OPENHAB_CONF/yaml/composed/`.

[[toc]]

## Feature Summary

YAML Composer adds several enhancements on top of standard YAML.
Each feature addresses a different kind of reuse, composition, or abstraction to help you build cleaner and more maintainable YAML.

| Feature                                    | Purpose                                                   | Typical Use                                                                                                    |
|--------------------------------------------|-----------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| **Variables and Substitution (`${..}`)**   | Insert dynamic values or evaluate expressions             | Build labels, topics, IDs, or computed values                                                                  |
| **Conditionals (`!if`)**                   | Conditionally include or exclude YAML blocks              | Enable or disable features when using packages or template flags                                               |
| **Include (`!include`)**                   | Insert the contents of another file                       | Reuse YAML across files; parameterize reusable blocks                                                          |
| **Templates (`!insert`)**                  | Reuse YAML defined within the same file                   | Local parameterized blocks; reusable channel or item fragments                                                 |
| **Packages**                               | Bundle multiple top-level sections into one reusable unit | Define reusable device structures containing things, items, metadata; sourced from external files or templates |
| **Anchors and Aliases (`&name`, `*name`)** | Define small, reusable YAML fragments                     | Static defaults, shared fields                                                                                 |
| **Merge Keys (`<<:`)**                     | Combine mappings from multiple sources                    | Layer defaults, override fields, compose structures                                                            |

Each feature has a dedicated documentation page:

- [Variables and Substitution](doc/variables.md)
- [Conditionals](doc/conditionals.md)
- [Include](doc/include.md)
- [Templates](doc/templates.md)
- [Packages](doc/packages.md)
- [Anchors and Aliases](doc/anchors.md)
- [Merge Keys](doc/merge-keys.md)

These features can be used independently, but they become especially powerful when combined.

For a general introduction to YAML, see [YAML Basics](doc/basics.md).

## Packaging Example

**CONF/yamlcompose/LivingRoom.yaml:**

```yaml
version: 1

variables:
  # Captures "LivingRoom" from the filename automatically
  location: ${__FILE_NAME__} # => LivingRoom

packages:
  Light1: !include
    file: $pkg/zigbee_light.inc.yaml
    vars: &LIGHT_VARS
      color_temperature: {} # Include color temperature feature
      power: # Customize the power item
        groups:
          - gInsideLights
          - gSmartLights

  Light2: !include
    file: $pkg/zigbee_light.inc.yaml
    vars:
      <<: *LIGHT_VARS
```

**CONF/yamlcompose/pkg/zigbee_light.inc.yaml:**

```yaml
# This is the package file, i.e. the template for a zigbee light
variables:
  id: ${location}_${package_id}
  thingid: ${id | lower | replace("_", "-")}
  equipment: ${id}_Equipment
  label: ${id | label}

  # Set defaults
  power: &DEFAULTS
    groups: []
  dimmer:
    <<: *DEFAULTS

things:
  mqtt:topic:${thingid}:
    bridge: mqtt:broker:mosquitto
    channels:
      power:
        type: switch
        config:
          stateTopic: zigbee2mqtt/${thingid}/state
          commandTopic: zigbee2mqtt/${thingid}/set/state

      dimmer:
        type: dimmer
        config:
          stateTopic: zigbee2mqtt/${thingid}/brightness
          commandTopic: zigbee2mqtt/${thingid}/set/brightness

      <<: !if
        if: VARS.containsKey('color_temperature')
        then:
          color-temperature: !sub
            type: dimmer
            config:
              stateTopic: zigbee2mqtt/${thingid}/color_temp
              commandTopic: zigbee2mqtt/${thingid}/set/color_temp

items:
  ${equipment}:
    type: Group
    label: ${label} Equipment
    tags: [Lightbulb]
    groups: ${[location]}

  ${id}:
    type: Switch
    label: ${label}
    tags: [Control, Light]
    groups: ${[equipment] + power.groups}
    channel: mqtt:topic:${thingid}:power

  ${id}_Dimmer:
    type: Dimmer
    label: ${label} Brightness
    tags: [Control, Level]
    groups: ${[equipment] + dimmer.groups}
    channel: mqtt:topic:${thingid}:dimmer

  <<: !if
    if: VARS.containsKey('color_temperature')
    then:
      ${id}_CT:
        type: Dimmer
        label: ${label} Color Temperature
        tags: [Control, ColorTemperature]
        groups: ${[equipment] + color_temperature.groups}
        channel: mqtt:topic:${thingid}:color-temperature
```

<details>
<summary><b>Output in CONF/yaml/composed/LivingRoom.yaml:</b></summary>

```yaml
version: 1

things:
  mqtt:topic:livingroom-light1:
    bridge: mqtt:broker:mosquitto
    channels:
      power:
        type: switch
        config:
          stateTopic: zigbee2mqtt/livingroom-light1/state
          commandTopic: zigbee2mqtt/livingroom-light1/set/state
      dimmer:
        type: dimmer
        config:
          stateTopic: zigbee2mqtt/livingroom-light1/brightness
          commandTopic: zigbee2mqtt/livingroom-light1/set/brightness
      color-temperature:
        type: dimmer
        config:
          stateTopic: zigbee2mqtt/livingroom-light1/color_temp
          commandTopic: zigbee2mqtt/livingroom-light1/set/color_temp

  mqtt:topic:livingroom-light2:
    bridge: mqtt:broker:mosquitto
    channels:
      power:
        type: switch
        config:
          stateTopic: zigbee2mqtt/livingroom-light2/state
          commandTopic: zigbee2mqtt/livingroom-light2/set/state
      dimmer:
        type: dimmer
        config:
          stateTopic: zigbee2mqtt/livingroom-light2/brightness
          commandTopic: zigbee2mqtt/livingroom-light2/set/brightness
      color-temperature:
        type: dimmer
        config:
          stateTopic: zigbee2mqtt/livingroom-light2/color_temp
          commandTopic: zigbee2mqtt/livingroom-light2/set/color_temp

items:
  LivingRoom_Light1_Equipment:
    type: Group
    label: Living Room Light 1 Equipment
    tags:
      - Lightbulb
    groups:
      - LivingRoom

  LivingRoom_Light1:
    type: Switch
    label: Living Room Light 1
    tags:
      - Control
      - Light
    groups:
      - LivingRoom_Light1_Equipment
      - gInsideLights
      - gSmartLights
    channel: mqtt:topic:livingroom-light1:power

  LivingRoom_Light1_Dimmer:
    type: Dimmer
    label: Living Room Light 1 Brightness
    tags:
      - Control
      - Level
    groups:
      - LivingRoom_Light1_Equipment
    channel: mqtt:topic:livingroom-light1:dimmer

  LivingRoom_Light1_CT:
    type: Dimmer
    label: Living Room Light 1 Color Temperature
    tags:
      - Control
      - ColorTemperature
    groups:
      - LivingRoom_Light1_Equipment
    channel: mqtt:topic:livingroom-light1:color-temperature

  LivingRoom_Light2_Equipment:
    type: Group
    label: Living Room Light 2 Equipment
    tags:
      - Lightbulb
    groups:
      - LivingRoom

  LivingRoom_Light2:
    type: Switch
    label: Living Room Light 2
    tags:
      - Control
      - Light
    groups:
      - LivingRoom_Light2_Equipment
      - gInsideLights
      - gSmartLights
    channel: mqtt:topic:livingroom-light2:power

  LivingRoom_Light2_Dimmer:
    type: Dimmer
    label: Living Room Light 2 Brightness
    tags:
      - Control
      - Level
    groups:
      - LivingRoom_Light2_Equipment
    channel: mqtt:topic:livingroom-light2:dimmer

  LivingRoom_Light2_CT:
    type: Dimmer
    label: Living Room Light 2 Color Temperature
    tags:
      - Control
      - ColorTemperature
    groups:
      - LivingRoom_Light2_Equipment
    channel: mqtt:topic:livingroom-light2:color-temperature
```

</details>

## Processing Overview

YAML Composer reads enhanced-syntax YAML files and performs a compilation pass that expands all extended features into a single, fully resolved YAML document that openHAB can load.

During compilation, YAML Composer performs the following steps:

1. **YAML Parsing**: The source file is parsed into an internal structure.
2. **Variable Substitution (`${..}`)**: Expressions are evaluated and injected.
3. **Conditionals (`!if`)**: Conditional logic determines which blocks remain.
4. **Template and Include Expansion**: `!insert` and `!include` bring in referenced content, often using resolved variables.
5. **Package Expansion**: External or local packages are loaded and expanded into their component sections.
6. **Recursive Merging**: Merge keys and package structures are combined into the main document.
7. **Hidden Key Removal**: Keys beginning with `.` are removed from the final output.

The resulting YAML contains:

- all variables resolved
- all conditionals evaluated
- all templates and includes expanded
- all anchors and merges applied
- all packages integrated
- all hidden keys removed

This final compiled YAML is written to `CONF/yaml/composed/`, where openHAB loads it as Things, Items, Metadata, and other configuration elements as defined by the Core YAML Configuration structure.

## Hidden Keys

Keys beginning with a dot (`.`) are treated as hidden. They:

- exist only during compilation
- are ideal for storing anchors, templates, or shared structures
- keep visible configuration clean
- are removed from the final output

**Example:**

```yaml
.base-switch: &BASE_SWITCH
  type: Switch
  autoupdate: false

items:
  Light1:
    <<: *BASE_SWITCH
    label: Light One
```

## File Structure and Conventions

YAML files can be organized freely, but the following conventions improve clarity and maintainability:

- Place `variables:` and `templates:` near the top of the file.
- Group reusable structures under hidden keys.
- Use anchors for static fragments and includes for parameterized ones.
- Keep packages in separate files when they represent reusable device or feature definitions.

These conventions are optional but help keep complex configurations predictable and easy to navigate.
