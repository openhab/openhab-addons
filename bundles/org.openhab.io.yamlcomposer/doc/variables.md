# Variables & Substitution

Variables allow you to define reusable values and substitute them throughout your YAML configuration.
They provide the flexibility needed for complex templates and reduce hard-coded values.
These variables work consistently across both the current file and any included packages.

[[toc]]

## Variable Definition

Variables are defined within a top-level `variables:` section.
It is recommended to place this section at the beginning of the file for better organization.

**Example:**

```yaml
variables:
  # Scalar variables
  expire: 5m
  label: Living Room

  # Map variable
  mqtt:
    broker: mqtt:broker:mybroker

  # List variable
  rooms:
    - Kitchen
    - Bedroom
```

## Variable Substitution

### Default Substitution Behavior

Substitution enables dynamic value construction instead of static hard-coding.
Whenever a scalar contains a `${...}` pattern, the preprocessor evaluates the expression and replaces the pattern with the result.
This makes it easy to construct labels, identifiers, and paths from defined variables.
Substitution also applies to YAML keys to allow identifiers to be built dynamically.

**Example:**

```yaml
variables:
  room: Kitchen
  light_id: Kitchen_Light

items:
  ${light_id}:
    label: ${room} Light
```

**Resulting Output:**

```yaml
items:
  Kitchen_Light:
    label: Kitchen Light
```

### Return Types & Coercion

The output type depends on how the substitution is structured within the YAML scalar.

#### Type Preservation (Single Expression)

In YAML, quoting normally sets the value to be a string.
However, when a quoted value consists **entirely** of a substitution expression, it behaves differently: the resulting object preserves the original Java type returned by the expression.
The purpose of quoting in this case is simply to prevent YAML from interpreting characters inside the expression (such as colons) as YAML syntax and rejecting the value.

```yaml
is_active: ${status == 'ON'}       # Boolean(true)
target_rooms: ${rooms}             # List

# Quoting is optional here because the expression is YAML‑friendly
connection: "${mqtt_config_map}"   # Map
```

In contrast, some expressions **must** be quoted because they contain characters that YAML would otherwise treat as structural syntax:

```yaml
# Without quotes, YAML will interpret the colon inside the expression
# as a mapping delimiter and reject it as invalid syntax.
result: "${true ? 1 : 0}"          # Integer(1)
```

::: tip Important
Type preservation applies **only** when the entire value is a substitution expression.
If the value is not exactly `${expr}` or `"${expr}"`, normal YAML rules apply and the result is a string.
:::

```yaml
moo: 1               # Integer(1)
foo: "1"             # String("1")
bar: "${1}"          # Integer(1)
baz: "Value: ${1}"   # String("Value: 1")
qux: "${'1'}"        # String("1")
```

To explicitly convert the result to a String, use a filter or concatenation:

```yaml
result: "${(true ? 1 : 0) | string}"   # String("1")
result: "${(true ? 1 : 0) ~ ''}"       # String("1")
```

#### String Coercion (Mixed Content)

If the substitution pattern is combined with any other text, or if multiple patterns are used together, the entire value is coerced into a **String**.

```yaml
description: "Status is ${status}" # String: "Status is ON"
concatenated: "${10}${20}"         # String: "1020"
room_name: "${room} "              # String: "Kitchen " (includes space)
```

| Syntax Pattern | Resulting Type | Example Output          |
|:---------------|:---------------|:------------------------|
| `${expr}`      | **Preserved**  | `[Item1, Item2]` (List) |
| `"${expr}"`    | **Preserved**  | `true` (Boolean)        |
| `Text ${expr}` | **String**     | `"Count: 5"` (String)   |
| `${ex1}${ex2}` | **String**     | `"1020"` (String)       |

### The `!literal` Tag and `!sub` Escape Hatch

The `!literal` tag disables substitution recursively for a specific YAML node.
This is useful when you need to preserve the `${...}` syntax as literal text.
Inside a `!literal` section, you can use the `!sub` tag to re-enable substitution for a specific child node.
When these tags overlap, the innermost tag always controls the final behavior.

**Example:**

```yaml
top: !literal
  foo: ${LITERAL}
  bar:
    baz: ${LITERAL}
    quux: !sub ${substituted}
    grault: ${LITERAL}
```

## Expression Syntax

The Expression syntax is based on the [Jinja expression](https://jinja.palletsprojects.com/en/stable/templates/#expressions) language.
Only expressions inside `${...}` are supported; template blocks like `{% if %}` or `{% for %}` are not available.

### Variable References

1. `label`: Refers to a scalar variable.
1. `rooms[0]`: Refers to the first element of a list.
1. `mqtt.broker` or `mqtt['broker']`: Refers to a map subkey.
1. `mqtt[key]`: Resolves the key dynamically using the value of the `key` variable.

### Operations & Concatenation

An expression can include string, arithmetic, and boolean operations.

1. **String Concatenation**: Use the `~` operator (e.g., `"Room " ~ index`).
1. **Coercion**: The `~` operator is the preferred way to join values because it automatically converts non-string operands into strings.
1. **Automatic Joining**: Adjacent literal text and substitution patterns are automatically joined without operators (e.g., `value: "Hello ${username}"`).

> **Note:** Referencing an undefined variable resolves to `null`, logs a warning, and results in an empty string if used in a string context.

### List Concatenation with `+`

Jinja’s `+` operator supports list concatenation.
If one side is a list and the other is a scalar, the scalar is automatically wrapped into a single-element list.

**Example:**

```yaml
variables:
  groups: [Group1, Group2]
  location: SemanticLocationGroup

effective_groups: ${ groups + location }
# Result: [Group1, Group2, SemanticLocationGroup]
```

### Built-in Filters

Filters are applied using the `variable|filter` syntax and can be chained.

#### Text Transformation

| Filter       | Description                                                 |
|:-------------|:------------------------------------------------------------|
| `capitalize` | Capitalize a value.                                         |
| `title`      | Return a titlecased version.                                |
| `lower`      | Convert a value to lowercase.                               |
| `upper`      | Convert a value to uppercase.                               |
| `replace`    | Replace a substring.                                        |
| `trim`       | Strip leading and trailing characters (default whitespace). |

#### Formatting

| Filter   | Description                                   |
|:---------|:----------------------------------------------|
| `format` | Apply values to a printf-style format string. |
| `round`  | Round a number to an optional precision.      |
| `int`    | Convert a value into an integer.              |

#### Collection Helpers

| Filter   | Description                            |
|:---------|:---------------------------------------|
| `first`  | Return the first item of a list.       |
| `length` | Return the length of a list or string. |

#### Fallbacks

| Filter    | Description                                                   |
|:----------|:--------------------------------------------------------------|
| `default` | Return a default value if the variable is empty or undefined. |

**Default Example:**

```yaml
label: ${room_label | default('Kitchen')}
```

### Custom Filters

| Filter  | Description                                                                         |
|:--------|:------------------------------------------------------------------------------------|
| `label` | Converts identifiers (camelCase, snake_case) into human-friendly titles.            |
| `dig`   | Safely navigates deep maps; returns `null` instead of an error if a key is missing. |

**`dig` Example:**

```yaml
# Dot notation and mixed access supported
user: ${ infrastructure | dig('config.login.user') }
host: ${ VARS | dig('config', 'servers', 1, 'host') | default('localhost') }
```

## Advanced Usage

### Predefined Variables

The Composer injects environmental and file-system context automatically.
These variables can be interpolated just like regular ones and are helpful when constructing paths for directives.

| Variable           | Description                                                             |
|:-------------------|:------------------------------------------------------------------------|
| `OPENHAB_CONF`     | Absolute path to openHAB's main configuration directory.                |
| `OPENHAB_USERDATA` | Absolute path to openHAB's userdata directory.                          |
| `__FILE__`         | Absolute path to the current file.                                      |
| `__FILE_NAME__`    | Filename portion without the extension or leading path.                 |
| `__FILE_EXT__`     | File extension portion of the current file name.                        |
| `__DIRECTORY__`    | Directory portion of the current file.                                  |
| `__DIR__`          | Alias for `__DIRECTORY__`.                                              |
| `package_id`       | Automatically resolved to the Package ID within included package files. |

### Handling Reserved Keywords

If a variable name is a Jinja keyword (like `and`, `or`, `if`), access it via the `VARS` dictionary.
This also works for variable names containing characters like hyphens that are invalid in direct references.

```yaml
foo: ${VARS['and']}
```

### ENV to Access Environment Variables

A special variable `ENV` exposes a map of system environment variables.
This is especially useful for configurations running within Docker containers.

```yaml
mode: ${ENV.OPENHAB_MODE}   # Resolves to the environment value
```

### Calling Java Methods

Variables retain their Java types, allowing you to call standard methods directly.

Common types you may encounter include:

- [String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html)
- [Integer](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Integer.html)
- [Double](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Double.html)
- [Boolean](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Boolean.html)
- [Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html)
- [List](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html)

This is useful for logic beyond built-in filters, such as complex regex replacements.

```yaml
# Use Java String methods for complex logic
is_sensor: ${ device_id.startsWith("sensor_") }
clean_id: ${ device_id.replaceAll("ABC", "XYZ") }
```

### Custom Pattern Delimiters

If your content conflicts with `${...}`, you can define custom delimiters using a named pattern.
This allows the same pattern to be used consistently across all included files.

```yaml
variables:
  jinja: "{{..}}"

foo: !sub:jinja "Hello {{ username }}!"
```

## Common Pitfalls

1. **Unquoted Operators**: Expressions containing YAML‑significant characters such as `:` or `?` must be quoted; otherwise YAML interprets those characters as structural syntax and rejects the value
1. **Reserved Names**: Avoid naming variables using keywords like `true`, `false`, `null`, `in`, or `if`.
1. **`+` vs `~`**: Use `~` for strings to avoid type mismatch errors and use `+` for numbers or lists.
1. **Jinja Blocks**: Block‑level Jinja constructs (e.g., `{% for %}`) are not supported. Use YAMLComposer’s own control‑flow tags, such as `!if`/`!elseif`/`!else` and `!for`.
1. **Whitespace Sensitivity**: Spaces outside of quotes inside `${ ... }` are ignored, but spaces in quoted strings are preserved.
