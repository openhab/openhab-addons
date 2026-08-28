# Variables & Substitution

Variables allow you to define reusable values and substitute them throughout your YAML configuration.
They provide the flexibility needed for complex templates and reduce hard-coded values.
These variables work consistently across the current file, included files, templates, and packages.

[[toc]]

## Variable Definition

Variables can be defined either globally using a top-level `variables:` block or inline anywhere in a document using the `!var` directive tag.

### Top-Level `variables:` Block

The `variables:` section defines variables at the root level.
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

### Inline `!var` Directives

The `!var` directive declares or reassigns variables **inline at the key level** within a mapping.
Each directive updates the local variable scope immediately and produces **no output key** in the final composed structure.

Variables defined this way are **local to the current mapping node** and automatically propagate to all of its descendants.

#### `!var` Syntax

Declare a variable using the key‑level form:

```yaml
!var name: value
```

Multiple `!var` directives may appear sequentially. Later directives may reference variables defined earlier in the same mapping.

```yaml
!var host: "localhost"
!var port: 8080
!var base_url: "http://${host}:${port}"
!var api_url: "${base_url}/v1"

endpoint: "${api_url}/users"
```

**Result:**

```yaml
endpoint: "http://localhost:8080/v1/users"
```

::: tip Position dependence and usage
The top‑level `variables:` block is position independent (its variables are visible everywhere).
Inline `!var` directives are position dependent: they take effect where they appear and only affect subsequent entries in the same mapping node and its descendants.
`!var` can be used to **define new local variables** or to **override** existing ones.
See [Variable Scoping and Isolation](#variable-scoping-and-isolation) for more info.
:::

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

## Filters

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

| Filter      | Description                                                                         |
|:------------|:------------------------------------------------------------------------------------|
| `label`     | Converts identifiers (camelCase, snake_case) into human-friendly titles.            |
| `dig`       | Safely navigates deep maps; returns `null` instead of an error if a key is missing. |
| `enumerate` | Maps collections, arrays, or maps into an indexed list of `[index, item]` pairs.    |

**`dig` Example:**

```yaml
# Dot notation and mixed access supported
user: ${ infrastructure | dig('config.login.user') }
host: ${ VARS | dig('config', 'servers', 1, 'host') | default('localhost') }
```

**`enumerate` Example:**

The `enumerate` filter and function map any collection, array, or map into a list of index-item pairs, making them fully compatible with `!for` loops and tuple unpacking.

```yaml
variables:
  items: ["alpha", "beta", "gamma"]
  mapping:
    alpha: "one"
    beta: "two"

# Using the filter syntax with a !for loop
devices:
  !for "index, item in items | enumerate":
    "dev_${index}": "${item}"

# Using the function syntax with a !for loop
items_mapped:
  !for "index, item in enumerate(items)":
    "item_${index}": "${item}"

# Enumerating maps yields index and Map.Entry pairs (.key and .value)
map_results:
  !for "idx, entry in enumerate(mapping)":
    "item_${idx}_${entry.key}": "${entry.value}"
```

## Functions

Expressions support both built-in functions and custom functions to generate sequences, handle iterations, and transform data.

### Built-in Functions

#### `range()`

**Syntax:**

**`range([start,] stop[, step])`**: Generates a sequence of integers. It supports single-argument bounds (stop), start/stop bounds, and custom positive or negative steps.

**Example:**

`${range(3)}` produces `[0, 1, 2]`

#### Ruby-Style Range Syntax

The Composer also supports Ruby-style range literals inside `${...}` expressions.

Ruby defines two range operators:

- `..` — **inclusive** range
- `...` — **exclusive** range (stop value is not included)

These behave the same way inside Composer expressions.

```yaml
${[0..2]}    # Inclusive: [0, 1, 2]
${[0...2]}   # Exclusive: [0, 1]
```

Both forms produce a real Java `List` when used as a standalone expression, following the same type‑preservation rules described above.

This syntax is equivalent to calling `range()`:

```yaml
${range(0, 3)}   # Same as [0..2]
${range(0, 2)}   # Same as [0...2]
```

Use whichever form is clearer for your template.

### Custom Functions

#### `enumerate()`

**Syntax:**

**`enumerate(target)`**: Maps collections, arrays, or maps into a list of indexed `[index, item]` pairs. When used with maps, it yields entry objects supporting `.key` and `.value` accessors.

**Example:**

```yaml
variables:
  items: ["alpha", "beta", "gamma"]
  mapping:
    alpha: "one"
    beta: "two"

# Using the enumerate filter
enumerated_list: ${items | enumerate}
# Result: [[0, "alpha"], [1, "beta"], [2, "gamma"]]

# Using the enumerate function
enumerated_list_func: ${enumerate(items)}
# Result: [[0, "alpha"], [1, "beta"], [2, "gamma"]]

# Enumerating maps yields indexed Map.Entry objects.
# Each entry wraps the original key-value pair and provides explicit
# property accessors: .key (for the map key) and .value (for the map value).
enumerated_map: ${enumerate(mapping)}
# Result Structure: [[0, alpha=one], [1, beta=two]]
#
# Accessing properties from a specific index (e.g., the first item):
#   - Key access:   ${enumerated_map[0][1].key}   -> "alpha"
#   - Value access: ${enumerated_map[0][1].value} -> "one"
```

## Advanced Topics

### Variable Scoping and Isolation

Variable scoping in YAML Composer follows a strict combination of **sequential evaluation**, **lexical mapping‑node boundaries**, and **downstream inheritance**. The two declaration mechanisms — the top‑level `variables:` block (global scope) and inline `!var` directives (local scope) — participate in the same unified scoping model. This section explains how they interact and lists the concrete rules you must follow.

#### Global vs Local: how `variables:` and `!var` interact

- **Global variables (`variables:` block)**
  - Define the **initial scope** for the entire file.
  - Are visible everywhere in the file, including included files, templates, and loops, regardless of where the `variables:` block appears in the document (i.e., `variables:` is position independent).
  - Are evaluated as part of the file composition and act as the root values that inline `!var` directives may override locally.
  - **Cannot** override system variables (e.g., `OPENHAB_CONF`, `__FILE__`, etc.).

- **Inline variables (`!var` directives)**
  - Declare or reassign variables **inline at the key level** within a mapping.
  - Update the local variable scope immediately and produce **no output key** in the final composed structure.
  - Are **local to the mapping node** in which they appear and automatically propagate to that node’s descendants.
  - Override global variables for that mapping and its descendants but do **not** change the `variables:` block itself.
  - Are **position dependent**: a `!var` only affects entries that appear **after** it in the same mapping node. A `!var` placed at the root mapping behaves like a global override **from the point it appears onward** but does not retroactively change values already evaluated earlier in the file.

**Practical summary:** treat `variables:` as the file’s initial defaults (position independent) and `!var` as local, sequential declarations that take effect at the point they are evaluated (position dependent).
A `!var` may either **define a new local variable** or **override** an existing one; when a `!var` appears at the root mapping it behaves like a global override only for entries processed after it appears and does not retroactively change values already evaluated earlier in the file.

#### Core rules for `!var`

##### 1. Sequential Evaluation (Order Matters)

`!var` directives apply **immediately** and affect all subsequent keys, values, and nested mappings **within the same mapping node**.

- Earlier entries cannot see variables declared later.
- Reassigning a variable updates its value only for entries that appear after the reassignment.
- Reassignments evaluate expressions using the **current** scope value.

```yaml
!var mode: "dev"
first: "${mode}"     # "dev"

!var mode: "prod"
second: "${mode}"    # "prod"
```

##### 2. Mapping‑Node Boundaries (Lexical Scope)

Each mapping node defines a scope.

- Child mappings **inherit** all variables visible at the moment they are created.
- Variables declared **inside** a child mapping:
  - apply only to that child and its descendants,
  - do **not** propagate back to the parent,
  - do **not** leak sideways into sibling mappings.

**Diagram:**

```text
parent-map:
├─ !var a: 1              ← defines `a` in this mapping
├─ key1: ${a}             ← sees `a`
│
├─ child-map:             ← new mapping node (inherits `a`)
│   ├─ key2: ${a}         ← sees `a` but not `b`
│   │                       (because `b` is declared *after* this entry)
│   ├─ !var b: 2          ← defines `b` only in this child mapping
│   └─ key3: ${b}         ← sees `b` (same mapping node, declared earlier)
│
└─ key4: ${a}             ← sees `a` but not `b`
                           (because `b` was declared inside child-map)
```

##### 3. List Context Restriction

`!var` is valid **only inside mapping nodes**.

If used inside a list item:

```yaml
- !var foo: bar
```

the list item becomes a mapping containing the directive.

If the list item must remain a scalar, declare the variable in the parent mapping instead.

### Predefined Variables

The Composer injects environmental and file-system context automatically.
These variables can be interpolated just like regular ones and are helpful when constructing paths for directives.

| Variable           | Description                                                          |
|:-------------------|:---------------------------------------------------------------------|
| `OPENHAB_CONF`     | Absolute path to openHAB's main configuration directory.             |
| `OPENHAB_USERDATA` | Absolute path to openHAB's userdata directory.                       |
| `__FILE__`         | Absolute path to the current file.                                   |
| `__FILE_NAME__`    | Filename portion without the extension or leading path.              |
| `__FILE_EXT__`     | File extension portion of the current file name.                     |
| `__DIRECTORY__`    | Directory portion of the current file.                               |
| `__DIR__`          | Alias for `__DIRECTORY__`.                                           |
| `VARS`             | Map containing all variables currently visible in the current scope. |

#### Contextual / Special Variables

These variables are dynamically populated based on the current execution context and are not always present.

| Variable     | Description                                                                                                                                                          |
|:-------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ARGS`       | Map containing only variables explicitly injected at the immediate `!include`/`!insert` call site (excludes global variables and those injected by parent includes). |
| `package_id` | Automatically resolved to the Package ID within included package context.                                                                                            |

::: warning Predefined Variable Protection
With the exception of `package_id`, all predefined and contextual variables listed above are reserved by the Composer.
They cannot be overridden or redefined by `!var` directives or `variables:` blocks.
Attempting to redefine any of these protected variables logs a warning and leaves the system value intact.
:::

### Handling Reserved Keywords

If a variable name is a Jinja keyword (like `and`, `or`, `if`), access it via the `VARS` dictionary.
This also works for variable names containing characters like hyphens that are invalid in direct references.

```yaml
foo: ${VARS['and']}
```

### ENV to Access Environment Variables

The YAML Composer provides a special variable map, **`ENV`**, which exposes system environment variables to your Composer source files.
This is especially useful when running openHAB inside Docker, where environment variables are commonly used for deployment‑specific configuration.

```yaml
mode: ${ENV.OPENHAB_MODE}   # Resolves to the environment value
```

::: tip Note
If a Composer source file references environment variables via standard lookups (e.g., `${ENV.VAR_NAME}` or `${ENV['VAR_NAME']}`), **changes to those variable values will automatically trigger regeneration** of the compiled YAML during openHAB startup.

Advanced operations on `ENV`—such as checking key existence (`'VAR' in ENV` or `ENV.containsKey(...)`), iterating over the map, or querying map properties (e.g., `ENV.size()`)—are not tracked for auto-regeneration.
:::

This feature is **not the same** as [openHAB Core’s environment variable expansion](/docs/configuration/things.html#defining-things-using-files) used in `.things` files.

#### Differences Between YAML Composer and Core ENV Expansion

| Feature            | YAML Composer                                                                                                                                             | Core                                                                                                                                        |
|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| **Syntax**         | `${ENV.NAME}` or `${ENV["NAME"]}`                                                                                                                         | `${ENV:NAME}`                                                                                                                               |
| **Where it works** | Anywhere in Composer YAML source: labels, UIDs, locations, conditions, parameters, keys, etc.                                                             | Only inside **Thing configuration values**. Can be used outside YAML Composer, directly inside `CONF/yaml/` files.                          |
| **When applied**   | During **Composer generation**. The generated YAML contains the **resolved value**, not the `${ENV...}` expression. UI and Core see only the final value. | During **Thing initialization**. The YAML file still contains the literal `${ENV:NAME}` pattern; Core resolves it at runtime.               |
| **How to use**     | Use Composer’s `${ENV.*}` syntax normally. It behaves like any other variable reference.                                                                  | Must wrap the literal `${ENV:NAME}` inside a [!literal](#the-literal-tag-and-sub-escape-hatch) block to prevent Composer from expanding it. |

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

1. **Unquoted Operators**: Expressions containing YAML‑significant characters such as `:` or `?` must be quoted; otherwise YAML interprets those characters as structural syntax and rejects the value.
1. **Reserved Names & System Variables**: System variables (`OPENHAB_CONF`, `__FILE__`, etc.) and Jinja keywords (`true`, `false`, `null`, `in`, `if`) cannot be overwritten.
1. **`+` vs `~`**: Use `~` for strings to avoid type mismatch errors and use `+` for numbers or lists.
1. **Jinja Blocks**: Block‑level Jinja constructs (e.g., `{% for %}`) are not supported. Use YAMLComposer’s own control‑flow tags, such as `!if`/`!elseif`/`!else` and `!for`.
1. **Whitespace Sensitivity**: Spaces outside of quotes inside `${ ... }` are ignored, but spaces in quoted strings are preserved.
