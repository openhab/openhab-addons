# Deep Merge & Structural Composition

This document is the canonical reference for recursive merge operations and structural precedence across the YAML composition engine.

Deep merging allows you to combine nested mapping structures without wiping out child keys.
The composition engine uses a single, unified pipeline to resolve both inline merge directives (`!deep <<:`) and top-level [package resolution](packages.md).

[[toc]]

## Core Principles & Precedence

### Shallow vs. Deep Merge Syntax

- Shallow Merge (`<<:`): Standard YAML 1.1 merge key.
  - Shallow merge performs a top-level key copy.
  - If a key already exists in the target map, its entire value (including nested child maps) is left untouched.
- Deep Merge (`!deep <<:`): Recursive composition modifier.
  - Deep merge recursively traverses nested mapping structures to combine sub-keys, while combining sub-lists by appending non-duplicate items, rather than overwriting parent objects.

### Target-Priority Rule

By default, Target (local) nodes take priority over Source (merged/imported) nodes.

- Target keys override source keys.
- Unmatched source keys are injected into the target structure.
- Conflict resolution tags (`!default`, `!freeze`/`!replace`, `!remove`) explicitly alter this target-first precedence.
  See below for more details.

## Syntax Variants & Disambiguation

### Standard Deep Merge

Apply `!deep` directly to the `<<:` merge key node:

```yaml
target:
  network:
    ip: 192.168.1.50
    subnet: 255.255.255.0

  !deep <<:
    network:
      gateway: 192.168.1.1
      subnet: 10.0.0.1
```

Result:

```yaml
target:
  network:
    ip: 192.168.1.50      # Retained from target
    subnet: 255.255.255.0 # Target priority over source
    gateway: 192.168.1.1  # Injected from source
```

### Disambiguated Multi-Merge Keys

Standard YAML forbids duplicate map keys.

To perform multiple deep merge operations within the same mapping context, use quoted merge keys containing comments (`!deep "<< #comment":`):

```yaml
target:
  security:
    ssl: true

  !deep "<< #base_config":
    security:
      ciphers: high

  !deep "<< #override_config":
    security:
      ssl: false
```

### Multi-Source Sequences

Provide a list of mappings to a single `!deep <<:` key to evaluate multiple sources in sequence.

Earlier items in the sequence take precedence over later items:

```yaml
target:
  services:
    - web

  !deep <<:
    - services: [web, api]
    - services: [db]
```

Result:

```yaml
target:
  services:
    - web
    - api
    - db
```

## Conflict Resolution Tags

Tags attached to target values modify how incoming source values combine with the document:

| Tag Modifier           | Behavior During Composition                                                                                                         |
|:-----------------------|:------------------------------------------------------------------------------------------------------------------------------------|
| `!default`             | **Surrenders Priority.** Marks target as fallback; incoming source value replaces target scalar/list or wins map sub-key conflicts. |
| `!freeze` / `!replace` | **Locks Node.** Locks target container completely. Blocks sub-key recursive merging and prevents source key injection.              |
| `!remove`              | **Suppresses Key.** Prunes target key and its node entirely during bottom-up traversal; prevents source key from populating.        |

## Data Type Resolution Rules

### Mapping Behavior

Maps merge key-by-key recursively.

- Default: Sub-keys in target override sub-keys in source.
- Missing keys in source are added to target.
- Target tagged `!default`: Source map sub-keys take precedence over target sub-keys during recursive evaluation.
- Target tagged `!freeze` / `!replace`: Target map remains untouched.

### List Behavior

Lists append elements in order and remove duplicates, supporting both list-level and per-item directives during deep merges.

- **Default**: Target list items are preserved first, source items are appended, and duplicate items are deduplicated.
- **Per-item `!default`**: Target list items tagged with `!default` are omitted if the incoming source list provides values. If the incoming source list is empty, `!default` items are retained.
- **Per-item `!remove`**: Target list items tagged with `!remove` purge matching values from the final merged list.
- **List-level `!default`**: If the entire target list is tagged with `!default`, the source list completely replaces the target list.
- **List-level `!freeze` / `!replace`**: Target list remains unchanged.

### List Merging Examples

#### Item-level `!default`

Omits default items when source elements exist:

```yaml
target:
  tags: [Control, !default Power]
  !deep <<:
    tags: [Light]

# Result:
# target:
#   tags: [Control, Light]
```

Retains default items when the source list is empty:

```yaml
target:
  tags: [Control, !default Power]
  !deep <<:
    tags: []

# Result:
# target:
#   tags: [Control, Power]
```

#### Item-level `!remove`

Filters out specified values from the merged result:

```yaml
target:
  tags: [Control, Power, !remove Light]
  !deep <<:
    tags: [Light, Sensor]

# Result:
# target:
#   tags: [Control, Power, Sensor]
```

#### List-level `!default`

Replaces the entire target list when tagged at the list boundary:

```yaml
target:
  tags: !default [Control, Power]
  !deep <<:
    tags: [Light]

# Result:
# target:
#   tags: [Light]
```

### Scalar Behavior

- Default: Target scalar value wins and remains intact.
- Target tagged `!default`: Source scalar value replaces target scalar.

## Simplifying Packages & Modules

Declarative deep merging eliminates procedural logic (such as conditional checks, manual map copying, or list concatenation operators) when constructing reusable package templates and imports.

Package authors no longer need custom key-by-key logic to inspect or append properties.

| Task                     | Manual / Procedural Approach                                              | Declarative Deep Composition                              |
|:-------------------------|:--------------------------------------------------------------------------|:----------------------------------------------------------|
| **Fallback Values**      | Procedural `if/else` checks or conditional key existence guards           | Attach `!default` to package scalars or maps              |
| **Map Combination**      | Manual recursive copying with shallow merge keys to preserve sibling keys | Deep merge key (`!deep <<:`) recursively merges sub-trees |
| **List Aggregation**     | Imperative list concatenation + manual duplicate filtering                | Automatic list item appending and deduplication           |

### Package Composition Example

**Package File (`light_item.inc.yaml`):**

```yaml
variables:
  thingid: ${package_id | lower | replace("_", "-")}
items:
  ${package_id}:
    type: Switch
    # Allow consumer to override label and icon
    label: !default ${package_id | label}
    icon: !default light
    # Automatically append consumer-defined tags and groups
    tags: [Control, Light]
    groups: [MainEquipment]
    autoupdate: false # Without !default, package default wins (consumer override ignored)
    channel: mqtt:topic:${thingid}:power
    !deep <<: ${ARGS} # Deep merge customizations passed from consumer
    metadata:
      ga: Light
```

**Main File (`lights.yaml`):**

```yaml
packages:
  Kitchen_Light: !include
    file: light_item.inc.yaml
    vars:
      # Customize the package
      label: Main Kitchen Light
      icon: kitchen
      tags: [MainLight]
      groups: [Kitchen]
      autoupdate: true
      metadata:
        alexa: Light
```

**Final Resolved Output:**

```yaml
items:
  Kitchen_Light:
    type: Switch
    label: Main Kitchen Light
    icon: kitchen
    tags:
      - Control
      - Light
      - MainLight
    groups:
      - MainEquipment
      - Kitchen
    autoupdate: false
    channel: mqtt:topic:kitchen-light:power
    metadata:
      ga: Light
      alexa: Light
```

## Best Practices

- Use `<<:` for Flat Overrides: Use standard shallow merge keys when replacing whole top-level map properties without recursive key union.
- Use `!deep <<:` for Hierarchical Trees: Use deep merging for multi-level configuration maps, nested settings, and file inheritance.
- Use Quotes for Disambiguation: Always quote custom comment keys (`!deep "<< #description":`) to ensure parser compatibility across YAML linters.
- Use `!freeze` for Security/Immutability: Tag sensitive sub-maps with `!freeze` to prevent imported files or base templates from injecting unwanted keys.
