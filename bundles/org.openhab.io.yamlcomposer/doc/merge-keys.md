# Merge Keys

Merge keys (`<<`) let you combine mappings defined directly in a mapping with other mappings defined elsewhere, such as:

- [Anchors](anchors.md)
- [!include](include.md) (External files)
- [!insert](templates.md) (In-file Templates)
- [!if](conditionals.md) (Conditionals)
- [Variables](variables.md)

They promote reusability and avoid repetition by letting you define common mappings once and update them in a single place.

[[toc]]

## What Merge Keys Do

A merge key takes one or more source mappings and merges their key–value pairs into the current mapping.
The source mappings may be written inline, referenced through an anchor, or loaded from another file.
The merged values behave exactly as if they were written inline.
Local fields may add to or override the merged content.

## Merge Rules

- Only mappings can be merged.
- Merge keys may appear multiple times or as a list.
- Merge order matters because keys from earlier mappings override keys from later ones.
- Local keys always override merged keys, even when defined after a merge key.
- Merges are shallow, and nested mappings are replaced rather than combined.

### Example: Multiple Merges and Precedence

```yaml
.defaults: &DEFAULTS
  icon: default
  autoupdate: false

.metadata: &LIGHTS
  tags: [Light]
  icon: light

items:
  Lamp:
    <<: [*LIGHTS, *DEFAULTS]
    label: Desk Lamp
```

Result:

```yaml
items:
  Lamp:
    label: Desk Lamp
    icon: light        # from LIGHTS (earlier mapping wins)
    autoupdate: false
    tags: [Light]
```

## Limitations

- Merge keys only merge mappings, not lists.
- Merge keys cannot modify scalar values.
- Merge keys cannot be used inside sequences unless the sequence elements are mappings.

## Integrating with Other Features

### Anchors

[Anchors](anchors.md) define reusable structures whose content can be inserted into the current mapping via an alias.
Merge keys then combine that anchored content with any local fields in the mapping.
Anchors are a standard YAML feature for sharing data within a single file.
Variables or templates are often preferred for more complex needs.

```yaml
items:
  Light1: &LIGHT_BASE
    type: Switch
    icon: light
    autoupdate: false
    label: Light One

  Light2:
    <<: *LIGHT_BASE
    label: Light Two
```

See also: [Using Hidden Keys for Anchors](anchors.md#using-hidden-keys-for-anchors).

### `!include`

The [!include](include.md) tag loads the contents of another file so it can be inserted into the current mapping.
Merge keys then combine the included content with any local fields in the mapping.

**Benefits over anchors:**

- Included files can be parameterized, allowing the same structure to be reused with different values.
- Included files can be shared across many different YAML files, whereas anchors are limited to a single file.

```yaml
# light-common.inc.yaml
power:
  type: switch
  stateTopic: !sub ${id}/power
availability:
  type: contact
  stateTopic: !sub ${id}/availability
```

```yaml
# light-color.inc.yaml
color:
  type: color
  stateTopic: !sub ${id}/color
```

```yaml
# main.yaml
things:
  mqtt:topic:living-room-light:
    channels:
      <<: !include { file: light-common.inc.yaml, vars: { id: living-room-light } }
      <<: !include { file: light-color.inc.yaml, vars: { id: living-room-light } }
```

### Templates (`!insert`)

The [!insert](templates.md) tag inserts the contents of an in-file template into the current mapping.
Merge keys then combine the template’s fields with any local fields in that mapping.

Templates can be parameterized when used in a merge key.

```yaml
templates:
  common_channels:
    power:
      type: switch
      stateTopic: !sub ${id}/power
    availability:
      type: contact
      stateTopic: !sub ${id}/availability

  color_channel:
    color:
      type: color
      stateTopic: !sub ${id}/color

things:
  mqtt:topic:living-room-light:
    channels:
      <<: !insert { template: common_channels, vars: { id: living-room-light } }
      <<: !insert { template: color_channel, vars: { id: living-room-light } }
```

### Conditionals (`!if`)

The [!if](conditionals.md) tag allows you to choose between different mappings or return `null` to skip the merge entirely.
The engine resolves tags before the merge, so the merge key receives the final mapping.

```yaml
# Merge extra settings only for production
server:
  port: 8080
  <<: !if
    if: !sub ${is_prod}
    value: { ssl: true, cache: true }
```

### Substitution (`!sub`)

Merge keys can combine content produced dynamically by a `!sub` expression.
`!sub` is type‑aware, so if a variable contains a map, the result is a map that can be merged directly.

This enables inline conditional merging using Python-style expressions.

```yaml
variables:
  has_color: true
  color_channel:
    color:
      type: color

things:
  mqtt:topic:light:
    channels:
      power:
        type: switch
      # Returns color_channel map if true
      <<: !sub ${color_channel if has_color}
```

::: tip Hints

- Merge keys only merge mappings, so `!sub` must resolve to a map.
- Avoid compound patterns such as `${foo}${bar}` or `x${foo}` because they are interpreted as literal strings and cannot be merged.
- Merging `null` or an empty map is a no-op, effectively omitting the merge key.
- If you use `!sub` inside an array for a merge key, make sure the array uses block style.
:::

## Related Topics

For details on how variables are resolved and how substitution interacts with YAML structures, see [Variables & Substitution — Interpolation and Inserted Content](variables.md#interpolation-and-inserted-content).

## Best Practices

- Use merge keys to define reusable base templates.
- Keep anchors inside hidden keys to avoid clutter.
- Use includes or packages to centralize shared structures.
- Prefer simple, predictable structures for reusable blocks.
