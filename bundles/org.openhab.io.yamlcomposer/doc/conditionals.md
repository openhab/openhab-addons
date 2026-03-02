# Conditionals (!if)

The `!if` tag performs logical branching during the **preprocessing phase**.

> **Note:** Conditions are evaluated **once** when the YAML file is loaded.
> These are not runtime rules.
> They do not react to live state changes in openHAB.

[[toc]]

## When to Use `!if`

Use the `!if` tag to adapt your configuration based on the **Resolution Context** (variables defined in the file, injected via `!include` or `!insert`, or [environment globals](variables.md#env-to-access-environment-variables)).

- **Conditional Snippets**: choose between alternative configuration blocks or values.
- **Optional Properties**: conditionally merge in additional settings using [merge keys (<<)](merge-keys.md).

## Basic Syntax

The `!if` tag supports two forms: a **Mapping Form** for simple logic and a **Sequence Form** for multiple branches.

The `if:` and `elseif:` keys are treated as **implicit expressions**.
You do not need to wrap them in `${...}` or use a `!sub` tag.

### Mapping Form (Simple)

Use this for simple if/else decisions.

```yaml
example: !if
  if: env == 'prod'
  then: "secure-server-url"
  else: "localhost"
```

| Key    | Description                                                                                                             | Required |
|:-------|:------------------------------------------------------------------------------------------------------------------------|:---------|
| `if`   | The expression to evaluate.                                                                                             | Yes      |
| `then` | The value to return if **truthy** ([see rules below](#truthiness-rules)). Can be a scalar, map, list, or any valid tag. | Yes      |
| `else` | The value to return if **falsy**.                                                                                       | No       |

### Sequence Form (Multiple Branches)

Use this for multiple ordered conditions.

Evaluates conditions in order and stops at the first **truthy** match.
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

The `if:` key follows a specific order of operations.

### 1. Bare Expressions (Recommended)

The string is evaluated directly as an expression against the available variables.

```yaml
if: count > 10 and status == 'ALARM'
```

::: tip
The expression can be quoted when it contains characters that YAML would otherwise misinterpret, such as `:` or `#`.
:::

### 2. Using `!sub` (Advanced — Double Evaluation)

If you use `!sub` inside an `if:` key, the substitution engine runs **first** to resolve `${...}` patterns.
The resulting string is then evaluated as an expression.
This is useful for building logic strings from variables.

```yaml
variables:
  operator: ">"

test: !if
  if: !sub 75 ${operator} 50
  # Step 1: !sub resolves to "75 > 50"
  # Step 2: expression evaluates to true
  then: "High"
```

## Truthiness Rules

When a value is used in a conditional, it is first evaluated and then interpreted as either **truthy** or **falsy**.

The following values are considered **falsy**:

- `false`
- `null`
- `0` or `0.0`
- empty strings (`""`)
- empty lists (`[]`)
- empty maps (`{}`)

All other values are **truthy**.
This includes any non‑empty string, any non‑zero number, and any non‑empty collection.

### Short-Circuiting (Lazy Evaluation)

Only the active branch is processed.
Tags such as `!include` inside inactive branches are ignored.
An `!include` in an inactive branch is never loaded and will not cause errors if the file does not exist.

## Advanced Integration

### Nesting and Composition

The `!if` tag is fully recursive.
You can nest `!if` tags within the `then` or `else` blocks to create complex decision trees.

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

Use `!if` with the YAML merge key (`<<`) to conditionally mix in sets of properties.

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
   To check for the literal string, quote it: `if: env == 'production'`.
1. **Omitting `else`**: If no condition matches and there is no `else`, the result is `null`.
1. **Invalid YAML**: Even inactive branches must be syntactically valid YAML.

See [Expression Syntax](variables.md#expression-syntax) for more details.
