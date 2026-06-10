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

The way keys interact depends on their data type.

| Data Type | Behavior  | Description                                                                                              |
|-----------|-----------|----------------------------------------------------------------------------------------------------------|
| Scalar    | Overwrite | A scalar in the final top‑level section replaces the scalar defined at the same path inside the package. |
| Map       | Merge     | Maps are merged key by key, recursively.                                                                 |
| List      | Merge     | Lists are concatenated (package values first) and de-duplicated.                                         |

### Automatic Removal of Empty Values

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

recursive merging allows customization at any depth in the mapping.

### Controlling Package Merge Behavior with Tags

Use these special YAML tags to explicitly override the default recursive merge behavior.
These directives can be declared at any point in the configuration pipeline—either within the **main YAML file** or deeply embedded inside an intermediate **include file** acting as a middle-tier package.

#### 1. The `!replace` Tag

The `!replace` tag forces an absolute replacement for maps or lists that would otherwise merge.
This acts as a destructive splice, completely discarding any existing inherited data structure beneath that key from upstream sources and starting fresh with the new layout provided.

#### 2. The `!remove` Tag

The `!remove` tag cleanly deletes the corresponding key and its entire sub-tree from the configuration hierarchy.
It is ideal for pruning specific components, metadata keys, or configurations exposed by a baseline package that do not apply to the current downstream context.

---

### Example: Nested Package Overrides

When packages include other packages, a middle-tier file can surgically modify or strip elements declared by the core baseline configuration before it ever reaches the main composition file.

#### Core Configuration (`nested-items.inc.yaml`)

Provides a core item layout with deep metadata fields:

```yaml
items:
  target_item:
    label: base_label
    tags:
      - from_nested
    metadata:
      stateDescription:
        value: from_nested
      category:
        value: from_nested
  untouched_item:
    label: untouched_from_nested
```

#### Middle-Tier Override Package (`pkg-with-overrides.inc.yaml`)

This file consumes the core configuration via `!include`, but applies `!replace` and `!remove` tags to selectively adjust properties:

```yaml
packages:
  nested: !include nested-items.inc.yaml
items:
  target_item:
    tags: !replace         # 1. Overwrite the list, discarding 'from_nested'
      - from_outer
    metadata:
      stateDescription: !remove # 2. Entirely purge this sub-tree key
      category: !replace   # 3. Swap out the map structure with a fresh one
        value: from_outer
        config:
          origin: nested_package_override
```

#### Root Configuration (`main.yaml`)

Loads the middle-tier package:

```yaml
packages:
  p1: !include pkg-with-overrides.inc.yaml
```

#### Final Merged Output

When evaluated by the preprocessor, the downstream overrides successfully neutralize the deep properties inherited from the original package file.
The `stateDescription` block is erased, lists and maps are replaced cleanly, and unrelated siblings are passed through untouched:

```yaml
items:
  target_item:
    label: base_label
    tags:
      - from_outer
    metadata:
      category:
        value: from_outer
        config:
          origin: nested_package_override
  untouched_item:
    label: untouched_from_nested
```

::: tip Usage Notes

- `!replace` and `!remove` are evaluated locally within each package layer before that package's content is returned and merged into the caller.
  This allows intermediate include files to cleanly intercept, prune, or completely reset configurations inherited from deeper core packages.
- They can be declared in the root configuration file or anywhere down an inheritance chain of nested package includes.
- `!remove` target references apply to entire map keys; individual array elements within a list cannot be targeted for independent removal.
- Use `!replace` on an array to drop inherited elements and start an array list with entirely new contents.

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

## Limitation: Top-Level Merge Keys in `packages:`

Top-level YAML merge keys (for example `<<:`) inside the `packages:` map are not supported.
Each package must be declared explicitly as a direct key under `packages:`.

The following pattern is **not supported**:

```yaml
packages:
  <<: !include common-packages.yaml
```

In this form, the merge key tries to inject package declarations into `packages:` itself.
The composer does not expand package declarations through top-level merge keys.
