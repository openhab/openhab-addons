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

- **Nesting:**
  A package source may itself include other files or templates.

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
things: !sub
  mqtt:topic:${package_id}:
    bridge: ${broker}
    channels:
      power:
        type: switch
        config:
          stateTopic: ${package_id}/state
          commandTopic: ${package_id}/set/state
      # ... other channels (brightness, color)

items: !sub
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

### Final Top-Level Sections

A final top‑level section is the fully expanded `things:`, `items:`, or other top‑level section of the configuration that openHAB receives after all packages, templates, includes, and merges have been applied.
Entries defined directly under these sections can merge with package‑generated entries when their identifiers match.
Entries remain independent when their identifiers do not match.

::: tip Note
The following example uses `!insert`, but the same merge rules apply to packages sourced from `!include`.
:::

When a package is expanded, its contents are merged into the main YAML structure.
You may customize the resulting structure by overriding, adding, or removing elements defined in the package.
This is done by redefining the elements you want to customize in the main file.
These redefinitions appear in the final top‑level section.

### Default Merge Behavior

Source YAML File:

```yaml
templates:
  number_item:
    items: !sub
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

The way keys interact depends on their data type.

| Data Type | Behavior  | Description                                                                                              |
|-----------|-----------|----------------------------------------------------------------------------------------------------------|
| Scalar    | Overwrite | A scalar in the final top‑level section replaces the scalar defined at the same path inside the package. |
| Map       | Merge     | Maps are merged key by key, recursively.                                                                 |
| List      | Merge     | Lists are concatenated.                                                                                  |

### Automatic Removal of Empty Values

During merging, empty structures are automatically stripped from the final configuration.
Empty maps (`{}`) and lists (`[]`) as well as map keys whose value is `null` or an empty string are removed.
This keeps the resulting configuration clean and allows packages to define catch‑all defaults.

**Example:**

```yaml
variables:
  icon: null   # default to avoid unknown‑variable warnings

icon: !sub ${icon}
```

Because `icon` evaluates to `null`, the entire `icon:` key is removed from the merged output unless the including file overrides it.

### How Package Merging Differs from YAML Merge Keys

Mappings from packages are merged recursively with the corresponding mappings in the final top‑level section.
This differs from standard YAML merge keys, which perform only shallow merges.

**Merge Key (shallow merge):**

```yaml
# merge key:
targetkey:
  foo:
    bar:
      boo: baz
  <<: # merge `foo` into `targetkey`
    foo:
      bar:
        boo: waldo
        goo: fy
      qux: quux
```

```yaml
# result — the merge key's foo mapping
# is ignored because foo already exists in main
targetkey:
  foo:
    bar:
      boo: baz
```

**Package Merging (recursive merge):**

```yaml
# main file
targetkey:
  foo:
    bar:
      boo: baz

packages:
  anyid: !include packagefile.inc.yaml
```

```yaml
# packagefile.inc.yaml
targetkey:
  foo:
    bar:
      boo: waldo
      goo: fy
    qux: quux
```

```yaml
# result:
targetkey:
  foo:
    bar:
      boo: baz  # main file overrides matching keys
      goo: fy   # but includes additional keys...
    qux: quux   # from the package
```

Recursive merging allows customization at any depth in the mapping.

### Controlling Package Merge Behavior with Tags

Use these special YAML tags in the main file to override the default merge behavior.

#### 1. The `!replace` Tag

The `!replace` tag forces a replacement for maps or lists that would otherwise merge.
This is useful when you want to discard the package's list or map and start fresh.

#### 2. The `!remove` Tag

The `!remove` tag removes the corresponding key from the final configuration.
This is ideal for excluding specific entities or properties from a generic package.

**Example:**

`main.yaml`:

```yaml
packages:
  Number: !include pkg/number.inc.yaml

# Custom overrides
items:
  Number_Item:             # Matches the resulting item name
    tags: !replace [Power] # Force overwrite, not merge
    metadata:
      stateDescription:
        config: !replace   # Force overwrite of this map
          format: "%.1f"
      widget: !remove      # Remove this key from the result
```

`pkg/number.inc.yaml`:

```yaml
items: !sub
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
```

Result:

```yaml
items:
  Number_Item:
    type: Number
    label: Package Label
    tags:                # tags from the package was !replaced, not merged
      - Power
    metadata:
      stateDescription:
        config:          # config from the package was !replaced, not merged
          format: '%.1f'
```

::: tip Usage Notes

- `!replace` and `!remove` are only valid in the top-level of the **main YAML file**.
- They are ignored if used inside a package source.
- `!remove` removes the entire key; it cannot remove individual list items.
- Use `!replace` to prune unwanted inherited map keys or list items.

:::

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
variables: !sub
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
