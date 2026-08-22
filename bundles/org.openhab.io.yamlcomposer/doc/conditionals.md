# Conditionals (!if)

Conditional tags perform logical branching during the **preprocessing phase**.

> **Note:** Conditions are evaluated **once** when the YAML file is loaded.
> These are not runtime rules.
> They do not react to live state changes in openHAB.

[[toc]]

## When to Use Conditional Tags

Use conditional tags to adapt your configuration based on the **Resolution Context**.
The Resolution Context includes variables defined in the file, variables injected via `!include` or `!insert`, and [environment globals](variables.md#env-to-access-environment-variables).

Conditional tags are useful for selecting configuration blocks, enabling optional features, or merging additional properties.

- **Conditional Snippets**: choose between alternative configuration blocks or values.
- **Optional Properties**: conditionally merge in additional settings using [merge keys (<<)](merge-keys.md).
- **Multi-Branch Logic**: select one of several possible configuration branches.

## Conditional Forms

The conditional system supports three forms:

- **Key‑Level Form**: uses `!if`, `!elseif` (and aliases `!elsif`, `!elif`), and `!else ~:` as map keys.
- **Mapping Form**: uses `if:`, `then:`, and `else:` keys inside a single mapping.
- **Sequence Form**: uses `if:`, `elseif:`, and `else:` entries inside a list.

All forms evaluate expressions once during preprocessing.

### Differences Between the Three Forms

Each form serves a different purpose and behaves differently when used inside maps or lists.
The table below summarizes the key differences so you can choose the right form before diving into the detailed syntax.

| Feature / Aspect       | **Key‑Level Form**                                                                 | **Mapping Form**                                                                            | **Sequence Form**                                                                                                                                                                                              |
|:-----------------------|:-----------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Syntax**             | `!if <expr>:`<br>`!elseif <expr>:`<br>`!else ~:`                                   | `!if`<br>&nbsp;&nbsp;`if: <expr>`<br>&nbsp;&nbsp;`then: <val>`<br>&nbsp;&nbsp;`else: <val>` | `!if`<br>&nbsp;&nbsp;`- if: <expr>`<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`then: <val>`<br>&nbsp;&nbsp;`- elseif: <expr>`<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`then: <val>`<br>&nbsp;&nbsp;`- else: <val>` |
| **Behavior in Maps**   | Merges nested key‑value pairs directly into the parent map                         | Resolves to a single value                                                                  | Resolves to a single value                                                                                                                                                                                     |
| **Behavior in Lists**  | Splices items directly into the parent list                                        | Returns a single list element (branch may itself be a list)                                 | Returns a single list element (branch may itself be a list)                                                                                                                                                    |
| **Multi‑Branching**    | Supported via sibling keys (`!elseif`, `!else ~`)                                  | Single condition (`if` / `then` / `else`)                                                   | Multi‑branch entries (`if`, `elseif`, `else`)                                                                                                                                                                  |
| **Unmatched Fallback** | Inactive branches are omitted entirely                                             | Resolves to `null`                                                                          | Resolves to `null`                                                                                                                                                                                             |
| **Primary Use Case**   | Conditionally merging groups of properties or inserting multiple inline list items | Simple ternary scalar/container assignment                                                  | Multi‑branch ternary scalar/container assignment                                                                                                                                                               |

## Key‑Level Form

The key‑level form applies conditional tags directly as map keys.
The tags `!if`, `!elseif` (and its aliases `!elsif`, `!elif`), and `!else ~` allow multi‑branch logic using separate map entries.
Each tag evaluates its expression (except `!else ~`, which has no expression).
The nested content (map or list) underneath the selected tag is merged into the parent structure.

Use this form when you want to conditionally merge additional map entries or list items into the surrounding structure.

::: tip Hints

- Expressions may be quoted or unquoted.
  Quote expressions when they contain characters YAML might misinterpret, such as `:` or `#`.
- If the expression is truthy, the nested content (map or list) is merged into the parent structure.
- If the expression is falsy, the nested content is ignored.
- Branches are evaluated in order from top to bottom.
  Only the first truthy branch is selected.
  Inactive branches are ignored.

:::

### Simple Example

```yaml
variables:
  items_count: 20
  things_count: 5

test:
  !if '"bar" == "bar"':
    foo: bar
  !if items_count > 10:
    items: a lot of items
  !if things_count > 10:
    things: a lot of things
  other: baz
```

Result:

```yaml
test:
  foo: bar
  items: a lot of items
  other: baz
```

### List Example

```yaml
variables:
  add_extra: true

items:
  MyItem:
    tags:
      - alpha
      - beta
      - !if add_extra:
          - gamma
          - delta
      - epsilon
```

Result:

```yaml
items:
  MyItem:
    tags:
      - alpha
      - beta
      - gamma
      - delta
      - epsilon
```

### Multi‑Branch Example

```yaml
mode:
  !if "env == 'prod'":
    value: "production"
  !elseif "env == 'staging'":
    value: "staging"
  !elif "env == 'dev'": # !elseif, !elif, and !elsif can be used interchangeably
    value: "development"
  !else ~:
    value: "unknown"
```

Result:

```yaml
mode:
  value: "production"
```

::: tip Important — `!else` Requires `~`

A bare `!else:` is **invalid YAML** because it produces an empty mapping.

You **must** write:

```yaml
!else ~:
```

The `~` is YAML's canonical `null` literal and exists only to satisfy YAML's requirement that every key has a value.

:::

### Key Uniqueness

Each conditional tag is a map key.
YAML requires each key to be unique.
A duplicate‑key error occurs only when two conditional tags have identical key scalars.
Inline comments may be used to make identical expressions unique.

```yaml
test:
  !if "true # unique 1":
    foo: bar
  !if "true # unique 2":
    qux: quux
  !if "true # unique 3":
    corge: grault
```

### Returning Simple Values

The _key‑level form_ is intended for returning maps and lists.
Use inline expressions when you want to return a simple scalar value.
Inline expressions do not require conditional tags.
Inline expressions evaluate directly to a single value.

```yaml
foo: ${"High" if 5 > 2 else "Low"}
```

Alternatively the other two forms below also return a single value.

## Mapping Form

Use the mapping form for simple if/else decisions inside a single block.

```yaml
example: !if
  if: env == 'prod'
  then: "secure-server-url"
  else: "localhost"
```

| Key    | Description                                                                                                             | Required |
|:-------|:------------------------------------------------------------------------------------------------------------------------|:---------|
| `if`   | The expression to evaluate.                                                                                             | Yes      |
| `then` | The value to return if truthy. Can be a scalar, map, list, or any valid tag.                                            | Yes      |
| `else` | The value to return if falsy.                                                                                           | No       |

## Sequence Form

Use the sequence form when you prefer to express multi‑branch logic inside a single list.
This form mirrors the behavior of the key‑level tags but keeps all branches grouped together.

Conditions are evaluated in order and stop at the first truthy match.
If no condition matches and no `else` is provided, the tag resolves to `null`.

```yaml
environment_type: !if
  - if: hardware_version >= 2
    then: "high-power-mode"
  - elseif: battery_powered
    then: "eco-mode"
  - else: "standard-mode"
```

## Expression Evaluation

Expressions used in key‑level `!if` and `!elseif` tags follow the same evaluation rules as expressions in `if:` keys.

### Bare Expressions (Recommended)

The string is evaluated directly as an expression against the available variables.

```yaml
if: count > 10 and status == 'ALARM'
```

::: tip
Quote expressions when they contain characters YAML would otherwise misinterpret, such as `:` or `#`.
:::

### Substitution Pattern (Advanced — Double Evaluation)

If you use a substitution pattern inside an `if:` key, the substitution engine runs first.
The resulting string is then evaluated as an expression.

```yaml
variables:
  operator: ">"

test: !if
  if: 75 ${operator} 50
  then: "High"
```

## Truthiness Rules

Falsy values:

- `false`
- `null`
- `0` or `0.0`
- empty strings (`""`)
- empty lists (`[]`)
- empty maps (`{}`)

All other values are truthy.

### Short‑Circuiting (Lazy Evaluation)

Only the active branch is processed.
Tags such as `!include` inside inactive branches are ignored.
Inactive branches do not load files and do not cause errors.

## Advanced Integration

### Nesting and Composition

Conditional tags are fully recursive.
You can nest conditional tags inside `then` or `else` blocks.

```yaml
status: !if
  if: device_online
  then: !if
    if: battery_level < 20
    then: "online-low-battery"
    else: "online-healthy"
  else: "offline"
```

### Conditional Merging (Mixins)

Use conditional tags with the YAML merge key (`<<`) to conditionally mix in sets of properties.

```yaml
server_config:
  port: 8080
  <<: !if
    if: is_prod
    then:
      ssl_enabled: true
      strict_security: true
```

### Using !include and !insert

You can return entire files or templates by using `!include` or `!insert` inside a branch.
Only the tag in the active branch is processed.

```yaml
network_settings: !if
  if: wifi_enabled
  then: !include wifi-config.inc.yaml
  else: !insert ethernet-template
```

## Common Pitfalls

1. **Expression vs String Literal**: `if: production` checks for a variable named `production`.
   Quote string literals: `if: env == 'production'`.
1. **Incorrect `!else` syntax**:
   A bare `!else:` is invalid.
   Always write `!else ~:` when using the key‑level form.
1. **Omitting `else`**: If no condition matches and there is no `else`, the result is `null`.
1. **Invalid YAML**: Even inactive branches must be syntactically valid YAML.
1. **Branch Ordering**: In the key‑level form, branches are evaluated in map order.
   The first truthy `!if` or `!elseif` wins.
   The `!else ~` branch applies only when no earlier branch matches.

See [Expression Syntax](variables.md#expression-syntax) for more details.
