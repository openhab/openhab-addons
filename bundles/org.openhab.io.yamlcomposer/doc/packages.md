# Packages

Packages provide a way to bundle multiple related YAML sections into a reusable, parameterized unit.
Unlike fragment-level insertion, a package expands into full top‑level sections such as `things:` or `items:`.
These sections may come from an external file or a same‑file template.
They are then merged into the current configuration.

[[toc]]

## Purposes

- **Logical Grouping:**
  Packages allow a **Thing** and its related **Items**, channels, and metadata to be defined together in one file, representing a complete, self‑contained device definition.

- **Reuse with Different Parameters:**
  Through variable substitution, a single package can be instantiated multiple times with different values.
  This makes it easy to define many similar devices such as sensors, lights, or switches from one shared template.

## Package Syntax and Structure

Packages are declared in the main YAML file under the top‑level `packages:` section.
Each entry defines a package ID and the source from which the package content is obtained.

```yaml
packages:
  <package_id>: <package_source>
  <another_package_id>: <package_source>
  ...
```

### Key Components

- **Package ID:**
  A package ID is a unique identifier for the package.
  It may include spaces.
  The special variable `${package_id}` resolves to this key inside the package content.

- **Package Source:**
  A package can be created from either of the following sources:

  - **`!include` (external file):**
    Loads a separate YAML file and applies the package’s variable context to it.
    See the [!include syntax options](include.md#syntax-options).

  - **`!insert` (same‑file template):**
    Expands a template defined under the main file’s `templates:` section.
    See the [!insert syntax options](templates.md#syntax-options).

  Both forms support parameterization through `vars:` and participate fully in package merging.

## Package Source Contents

- **Top‑Level Sections:**
  Package sources contain any combination of top‑level keys such as `things:` and `items:`.

- **Uniqueness:**
  Because package sources can be referenced multiple times, use variable substitutions such as `${package_id}` and unique `vars:` variables for entity UIDs in each invocation to avoid collisions.

- **Deep Nesting:**
  A package source may itself include other packages.
  When a package references another package via `packages:` inside an included file, it creates an inheritance chain.
  Properties merge downward sequentially from the deepest core file out to the final main file.

## Package Example

`main.yaml`:

```yaml
variables:
  broker: mqtt:broker:main

packages:
  livingroom-light: !include
    file: package/mqtt-light.inc.yaml
    vars:
      name: Living_Room_Light
      label: Living Room Light

  bedroom-light: !include
    file: package/mqtt-light.inc.yaml
    vars:
      name: Bed_Room_Light
      label: Bed Room Light
```

`package/mqtt-light.inc.yaml`:

```yaml
things:
  mqtt:topic:${package_id}:
    bridge: ${broker}
    channels:
      power:
        type: switch
        config:
          stateTopic: ${package_id}/state
          commandTopic: ${package_id}/set/state
      # ... other channels (brightness, color)

items:
  ${name}_Power:
    type: Switch
    label: ${label} Power
    channel: mqtt:topic:${package_id}:power
  # ... more items for the light, e.g. brightness, color, etc.
```

Resulting YAML structure:

```yaml
things:
  mqtt:topic:livingroom-light:
    bridge: mqtt:broker:main
    channels:
      power:
        type: switch
        config:
          stateTopic: livingroom-light/state
          commandTopic: livingroom-light/set/state
  mqtt:topic:bedroom-light:
    bridge: mqtt:broker:main
    channels:
      power:
        type: switch
        config:
          stateTopic: bedroom-light/state
          commandTopic: bedroom-light/set/state

items:
  Living_Room_Light_Power:
    type: Switch
    label: Living Room Light Power
    channel: mqtt:topic:livingroom-light:power
  Bed_Room_Light_Power:
    type: Switch
    label: Bed Room Light Power
    channel: mqtt:topic:bedroom-light:power
```

## Merge Behavior

Package merging uses the same recursive merge mechanism that is documented in the [Deep Merge](deep-merge.md) reference.
Scalar, map, and list interactions during package expansion follow the unified deep‑merge rules.

### Default Merge Behavior

Source YAML File:

```yaml
templates:
  number_item:
    items:
      ${package_id}_Item:
        type: Number
        label: Package Label
        tags: [Measurement]
        metadata:
          stateDescription:
            config:
              min: 1
              pattern: '%.3f'
          widget:
            value: oh-card

packages:
  Number: !insert number_item

# This is the final top‑level `items:` section of the configuration
# The packages will merge into this section
items:
  Number_Item:
    label: Power Draw
    dimension: Power
    tags: [Power]
    metadata:
      stateDescription:
        config:
          max: 10
```

Result:

```yaml
items:
  Number_Item:
    type: Number
    label: Power Draw
    dimension: Power
    tags:
      - Measurement
      - Power
    metadata:
      stateDescription:
        config:
          min: 1
          max: 10
          pattern: '%.3f'
      widget:
        value: oh-card
```

## Package Merge Tags

Package consumers can use `!default`, `!replace` (or `!freeze`), and `!remove` to control how package values interact with main‑file values.
The [deep‑merge](deep-merge.md) documentation contains the authoritative, consolidated definitions and examples for these tags.
Use the deep‑merge reference when you need the precise semantics for the merge behavior during package merging.

## Automatic Removal of Empty Values

During merging, empty structures are automatically stripped from the final configuration.
Empty maps (`{}`) and lists (`[]`) as well as map keys whose value is `null` or an empty string are removed.
This keeps the resulting configuration clean and allows packages to define catch‑all defaults.

**Example:**

```yaml
variables:
  icon: null   # default to avoid unknown‑variable warnings

icon: ${icon}
```

Because `icon` evaluates to `null`, the entire `icon:` key is removed from the merged output unless the including file overrides it.

## How Package Merging Differs from YAML Merge Keys

Mappings from packages are merged recursively with the corresponding mappings in the final top‑level section.
This differs from standard YAML merge keys, which perform only shallow merges.

## Strategic Use of Package IDs

Choose a **Package ID** that can also serve as a Thing UID fragment, Item name, or similar identifier.
This avoids defining extra variables in your package source and lets you derive related identifiers directly from `${package_id}`.

You can override `${package_id}` in the `vars:` block if needed.

**Example:**

```yaml
# main file
packages:
  Living_Room_Light: !include light.inc.yaml
  Kitchen_Light: !include light.inc.yaml
```

```yaml
# light.inc.yaml package source
variables:
  id: ${package_id|lower|replace('_', '-')}
  thing_uid: "mqtt:topic:${id}"
  item_name: ${package_id}
  label: ${package_id|replace('_', ' ')}
```

**Resulting variables:**

| Variable        | Living_Room_Light              | Kitchen_Light              |
|-----------------|--------------------------------|----------------------------|
| `${package_id}` | `Living_Room_Light`            | `Kitchen_Light`            |
| `${id}`         | `living-room-light`            | `kitchen-light`            |
| `${thing_uid}`  | `mqtt:topic:living-room-light` | `mqtt:topic:kitchen-light` |
| `${item_name}`  | `Living_Room_Light`            | `Kitchen_Light`            |
| `${label}`      | `Living Room Light`            | `Kitchen Light`            |

## Limitations: Top-Level Merge Keys & Deep Merges in `packages:`

Top-level YAML merge keys (`<<:`) and deep merge directives (`!deep <<:`) are not supported directly inside the `packages:` map.

Each package must be declared explicitly as a direct key under `packages:`.

The following patterns are **not supported**:

```yaml
packages:
  # Shallow merge in packages is NOT supported
  <<: !include common-packages.yaml

  # Deep merge in packages is NOT supported
  !deep <<: !include common-packages.yaml
```

In these forms, the merge key tries to inject package declarations into `packages:` itself.

The engine extracts package declarations before structural composition occurs, so merge keys cannot be used to generate top-level package mappings.
