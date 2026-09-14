# Loops (`!for`)

The `!for` tag performs configuration unrolling and iteration during the **preprocessing phase**.

> **Note:** Iterations are evaluated **once** when the YAML file is loaded.
> These are not runtime rules.
> They do not react to live state changes in openHAB.

[[toc]]

## When to Use `!for`

Use the `!for` tag to dynamically generate repetitive openHAB configuration structures based on the **Resolution Context** (variables defined in the file, injected via `!include` or `!insert`, or [environment globals](variables.md#env-to-access-environment-variables)).

Typical use cases in openHAB:

- **Generate multiple Things** from a list of device IDs.
- **Generate multiple Channels** from structured metadata.
- **Generate multiple Items** from a list, map, or equipment definition.
- **Generate semantic model structures** (rooms, equipment, points).
- **Generate metadata blocks** (e.g., multiple `stateDescription` entries).
- **Unroll lists** into a parent array (e.g., multiple tags, multiple groups).

## Basic Syntax

The `!for` tag evaluates an expression or collection and iterates over its items.
The expression inside the tag specifies the loop variable(s) and the target collection or range.

### Map Key Expansion

When applied as a map key, the unrolled entries are merged directly into the parent map.

```yaml
things:
  !for "id in ['kitchen', 'garage']":
    "mqtt:topic:${id}":
      label: "${id | label}"
      channels:
        temperature:
          stateTopic: "home/${id}/temp"
```

Result:

```yaml
things:
  mqtt:topic:kitchen:
    label: "Kitchen"
    channels:
      temperature:
        stateTopic: "home/kitchen/temp"

  mqtt:topic:garage:
    label: "Garage"
    channels:
      temperature:
        stateTopic: "home/garage/temp"
```

### Range Generation

Use `range(start, end)` to generate numeric sequences — useful for numbered devices, channels, or items.

```yaml
items:
  !for "i in range(1, 4)":
    "Light_${i}":
      type: Switch
      channel: "zigbee:device:bridge:light_${i}:state"
```

Result:

```yaml
items:
  Light_1:
    type: Switch
    channel: zigbee:device:bridge:light_1:state
  Light_2:
    type: Switch
    channel: zigbee:device:bridge:light_2:state
  Light_3:
    type: Switch
    channel: zigbee:device:bridge:light_3:state
```

::: tip

[Ruby-style range](variables#ruby-style-range-syntax) syntax is also supported: `[1..3]` (inclusive) and `[1...4]` (exclusive).

**Example:** `!for i in [1..3]` loops from `1` to `3`.

:::

## Iteration Patterns

The `!for` tag supports several unpacking and indexing patterns.

### 1. Simple List or Array Iteration

Useful for generating multiple Items or Channels from a simple list.

```yaml
variables:
  sensors: ["temp", "humidity"]

items:
  !for "sensor in sensors":
    "Sensor_${sensor}":
      type: Number
      channel: "mqtt:topic:env:${sensor}"
```

### 2. Map / Dictionary Key-Value Iteration

Ideal for equipment definitions where each entry has structured metadata.

```yaml
variables:
  equipment_channels:
    ch1: { label: "Main Light", type: "Switch" }
    ch2: { label: "Fan", type: "Switch" }

channels:
  !for "id, cfg in equipment_channels":
    "${id}":
      label: "${cfg.label}"
      type: "${cfg.type}"
```

### 3. Tuple and List Unpacking

Useful for defining multiple Things or Items with paired attributes.

```yaml
things:
  !for "id, binding in [['Light1', 'mqtt'], ['Temp1', 'zigbee']]":
    "${binding}:generic:${id}":
      label: "${id}"
```

### 4. Index & Item Enumeration (`enumerate`)

Use the `enumerate` filter or function when you need both the zero-based index and the collection item simultaneously during iteration.
When applied to maps, it pairs the index with map entry objects, allowing access via `.key` and `.value`.

```yaml
variables:
  items: [alpha, beta, gamma]
  mapping:
    kitchen: Light
    garage: Door

devices:
  # Using the enumerate filter with list unpacking
  !for "index, item in items | enumerate":
    "dev_${index}": "${item}"

  # Using the enumerate function with map entry unpacking
  !for "idx, entry in enumerate(mapping)":
    "${entry.key}_${idx}": "${entry.value}"
```

## Conditional Filtering

Filter loop items using an inline `if` clause.

```yaml
variables:
  sensors:
    - { id: "s1", enabled: true }
    - { id: "s2", enabled: false }
    - { id: "s3", enabled: true }

items:
  !for "sensor in sensors if sensor.enabled":
    "Sensor_${sensor.id}":
      type: Number
```

Result:

```yaml
items:
  Sensor_s1:
    type: Number
  Sensor_s3:
    type: Number
```

## Disambiguating Duplicate Keys

YAML requires each key in a map to be unique.
A `!for` loop is treated as a map key, and its entire scalar value — including any inline comments — is used for key comparison.

A duplicate‑key error only occurs when two `!for` blocks have **identical key scalars**.
If the expressions differ, the keys are already unique and no special handling is required.

```yaml
items:
  !for "id, label in equipment":
    "Item_${id}_A": "${label}"

  !for "id, label in equipment":        # Error! Identical key → duplicate
    "Item_${id}_B": "${label}"
```

To make identical expressions unique, add a harmless inline comment:

```yaml
items:
  !for "id, label in equipment # pass1":
    "Item_${id}_A": "${label}"

  !for "id, label in equipment # pass2":
    "Item_${id}_B": "${label}"
```

Only loops with identical expressions require disambiguation.
Different expressions naturally produce different keys.

## Advanced Integration

### Nesting and Composition

Loops are fully recursive.
Useful for generating multi-dimensional structures like multi-room equipment, grids, or channel groups.

```yaml
items:
  !for "room in ['Kitchen', 'Garage']":
    !for "i in range(1, 3)":
      "${room}_Sensor_${i}":
        type: Number
```

Result:

```yaml
items:
  Kitchen_Sensor_1:
    type: Number
  Kitchen_Sensor_2:
    type: Number
  Garage_Sensor_1:
    type: Number
  Garage_Sensor_2:
    type: Number
```

## Common Pitfalls

1. **Duplicate Keys**: Multiple `!for` blocks in the same map require unique keys (use inline comments).
1. **Invalid Target**: Ensure the loop expression resolves to a valid list or map.
1. **Static Preprocessing**: Loops run at load time only; they cannot react to runtime state changes.

See [Expression Syntax](variables.md#expression-syntax) for more details.
