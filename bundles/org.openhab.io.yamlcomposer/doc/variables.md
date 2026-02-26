# Variables & Substitution

Variables let you define reusable values and substitute them throughout your YAML configuration.
They make templates flexible, reduce hard‑coded values, and work across both the current file and any included files.

[[toc]]

## Variable Definition

Variables are defined in a top‑level `variables:` section.
It is recommended to place this section at the beginning of the file so it's easier to find.

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

Defining variables is only the first step.
To actually use them inside your YAML structures, you must enable substitution explicitly using the `!sub` tag.

Substitution is opt‑in and controlled, so values are only interpolated where you mark them.
This section explains how substitution works, how to customize it, and how it interacts with anchors, include files, and other reuse mechanisms.

### The `!sub` Tag

Variable substitutions can only occur inside YAML nodes tagged with `!sub`.

When applied to a scalar value, any `${...}` expression inside the string is replaced with the result of the expression.
Substitutions can appear anywhere within the value, so `"Location: ${floorName} – ${roomName}"` will expand to `"Location: Ground Floor – Kitchen"` when those variables are set accordingly.

The same behavior applies to YAML keys.

**Example:**

```yaml
variables:
  room: Kitchen
  light_id: Kitchen_Light

items:
  !sub ${light_id}:
    label: !sub ${room} Light
```

The resulting document will be:

```yaml
items:
  Kitchen_Light:
    label: Kitchen Light
```

### The `!nosub` Tag

When a YAML map or list is tagged with `!sub`, substitutions apply recursively to that structure.

In some cases, you may want to prevent substitutions within part of a structure.
The `!nosub` tag provides this fine‑grained control to disable substitutions for a specific node without affecting the rest of the `!sub` structure.
Both tags apply recursively, and when they overlap, the innermost tag controls whether substitution occurs.

**Example:**

In the example below, `!sub` applies recursively except where a nested `!nosub` disables substitution.
`${LITERAL}` indicates a value where substitution is not performed because `!nosub` disables interpolation inside a `!sub` block.

```yaml
top: !sub
  foo: ${substituted}
  bar: !nosub
    baz: ${LITERAL}
    quux: !sub ${substituted}
    grault: ${LITERAL}
  qux: ${substituted}
  groot: !nosub ${LITERAL}
```

## Expression Syntax

The Expression syntax is based on the [Jinja expression](https://jinja.palletsprojects.com/en/stable/templates/#expressions) language (via Jinjava).
Only expressions inside `${...}` are supported — template blocks and macros such as `{% if %}` or `{% for %}` are not available.

The simplest expression contains a variable name.
The expression will be substituted with the content of the variable.

The syntax of variable references:

- `label` — refers to the scalar variable named `label`
- `rooms[0]` — refers to the first element of the list named `rooms`
- `mqtt.broker` — refers to the `broker` subkey of the `mqtt` map
- `mqtt['broker']` — refers to the same `broker` subkey
- `mqtt[key]` — refers to the same value as `mqtt.broker` when the variable `key` has the value `broker`

An expression can also include string, arithmetic, and boolean operations.

> **Note:** Referencing an undefined variable resolves to an empty string and emits a warning.
> For more details see [how to handle missing variables](#undefined-variable-handling).

**Examples:**

- `"Hello " ~ mqtt.username` — concatenates strings using Jinja's `~` operator
- `rooms[0] ~ " sensor"` — uses a list element and appends text
- `temperature + 5` — adds 5 to the value of the `temperature` variable
- `(width * height) / 100` — performs arithmetic using multiple variables
- `count > 10` — evaluates to `true` if `count` is greater than 10
- `enabled and (mode == "auto")` — combines boolean and comparison operators
- `"Room " ~ (index + 1)` — mixes arithmetic and string concatenation

::: tip String Concatenation

You can concatenate strings inside an expression using the `~` operator:

```yaml
value: !sub "${'Hello ' ~ username}"
```

The `~` operator is the preferred and safer way to concatenate strings because it **automatically converts non‑string operands into strings** before joining them.
By contrast, the `+` operator only works when **both** operands are already strings.

For simple cases, you don't need an expression at all.
Adjacent literal text and substitution patterns are automatically joined:

```yaml
value: !sub "Hello ${username}"
```

This is often clearer and avoids unnecessary use of operators.

:::

### Return Types & Coercion

The output type of a `!sub` tag depends on how the substitution is structured within the YAML scalar.

#### Type Preservation (Single Expression)

If the YAML value consists **entirely** of a single substitution pattern, the resulting object preserves the original Java type returned by the expression.
This allows you to inject complex structures like lists, maps, booleans, or numbers directly into the configuration.

```yaml
# Becomes a real Boolean (true/false)
is_active: !sub ${status == 'ON'}

# Becomes a real List
target_rooms: !sub ${rooms}

# Quoting ensures YAML doesn't misinterpret special characters,
# but the return type remains a Map if it's the only thing inside
connection: !sub "${mqtt_config_map}"
```

#### String Coercion (Mixed Content)

If the substitution pattern is combined with any other text (even a single space), or if multiple patterns are used together, the entire value is coerced into a **String**.

```yaml
# Becomes a String: "Status is ON"
description: !sub "Status is ${status}"

# Becomes a String: "1020" (not 30)
concatenated: !sub "${10}${20}"

# Becomes a String: "Kitchen " (note the trailing space)
room_name: !sub "${room} "
```

| Syntax Pattern      | Resulting Type | Example Output          |
|:--------------------|:---------------|:------------------------|
| `!sub ${expr}`      | **Preserved**  | `[Item1, Item2]` (List) |
| `!sub "${expr}"`    | **Preserved**  | `true` (Boolean)        |
| `!sub Text ${expr}` | **String**     | `"Count: 5"` (String)   |
| `!sub ${ex1}${ex2}` | **String**     | `"1020"` (String)       |

::: tip Recommendation
If you need to pass a list or map to a key, ensure the `!sub` contains exactly one `${...}` block and no surrounding text.
:::

### List Concatenation with `+`

Jinja’s `+` operator also supports list concatenation.
When one side of the expression is a list and the other is a scalar, the scalar is automatically wrapped into a single‑element list.

**Example:**

```yaml
variables:
  groups: [Group1, Group2]
  location: SemanticLocationGroup

effective_groups: !sub ${ groups + location }
# → [Group1, Group2, SemanticLocationGroup]
```

This works because Jinja treats:

```text
groups + location
```

as:

```text
['Group1', 'Group2'] + ['SemanticLocationGroup']
```

You can also concatenate two lists directly:

```yaml
!sub ${ ['A'] + ['B', 'C'] }   # → ['A', 'B', 'C']
```

This behavior originates from Jinja’s expression language, which follows Python‑style list semantics.

### Built-in Filters

Jinja offers a number of built‑in filters that are useful when building YAML structures.
Filters are applied to a variable or value using the syntax `variable|filter`, and they can be chained, e.g. `variable|filter1|filter2`.

Some commonly used filters are listed below:

#### Text transformation

| Filter       | Description                                                   |
|--------------|---------------------------------------------------------------|
| `capitalize` | Capitalize a value.                                           |
| `title`      | Return a titlecased version.                                  |
| `lower`      | Convert a value to lowercase.                                 |
| `upper`      | Convert a value to uppercase.                                 |
| `replace`    | Replace a substring.                                          |
| `trim`       | Strip leading and trailing characters, by default whitespace. |

#### Formatting

| Filter   | Description                                   |
|----------|-----------------------------------------------|
| `format` | Apply values to a printf-style format string. |
| `round`  | Round a number to an optional precision.      |
| `int`    | Convert a value into an integer.              |

#### Collection helpers

| Filter   | Description                            |
|----------|----------------------------------------|
| `first`  | Return the first item of a list.       |
| `length` | Return the length of a list or string. |

#### Fallbacks

| Filter    | Description                                                   |
|-----------|---------------------------------------------------------------|
| `default` | Return a default value if the variable is empty or undefined. |

##### Default Example

```yaml
label: !sub ${room_label | default('Kitchen')}
```

For a complete list of built-in filters, see the Jinja documentation:
[Jinja Filters](https://jinja.palletsprojects.com/en/stable/templates/#builtin-filters).

---

### Custom Filters

YAML Composer provides several custom filters to help in YAML configuration building.

| Filter          | Description                                        |
|-----------------|----------------------------------------------------|
| [label](#label) | Convert an identifier into a human‑friendly label. |
| [dig](#dig)     | Safely navigate deeply nested map structures.      |

#### `label`

Formats an identifier into a human‑friendly label.

##### Behavior

- Splits words on whitespace, hyphens (`-`), underscores (`_`), and repeated separators.
- Splits camelCase and PascalCase
  (`powerGrid` → `Power Grid`, `LivingRoom` → `Living Room`, `StatusLED` → `Status LED`).
- Collapses multiple spaces and title‑cases the resulting words.
- Leaves fully uppercase inputs unchanged (`FOOBAR` → `FOOBAR`).

##### Why this exists

Package IDs, item names, and thing UIDs are often written in a specific format.
The `label` filter turns them into clean, human‑friendly labels without requiring a separate value.

##### Examples

```sh
${ "foo bar" | label } → "Foo Bar"
${ "fooBar" | label } → "Foo Bar"
${ "foo-bar_baz" | label } → "Foo Bar Baz"
${ "multiple---separators___here" | label } → "Multiple Separators Here"
${ "StatusLED" | label } → "Status LED"
```

#### `dig`

Safely navigates deeply nested map structures without raising errors.
Instead of throwing an error when a key is missing, it returns `null`, making it useful for optional or partially defined configuration data.

##### Behavior

- Traverses nested maps and lists using a sequence of keys
- Accepts multiple key arguments, a dot‑separated path, or any combination of both
- List indices may be provided as numbers or numeric strings (e.g., `2` or `'2'`)
- Returns the value if all keys exist
- Returns `null` if any key in the chain is missing
- Works seamlessly with `default()` to provide fallbacks

##### Example

```yaml
variables:
  infrastructure:
    config:
      login:
        user: alice
      servers:
        - host: a.example.com
        - host: b.example.com

username: !sub ${ infrastructure | dig('config', 'login', 'user') }
# → "alice"

password: !sub ${ infrastructure | dig('config', 'login', 'password') }
# → null

dot_notation: !sub ${ infrastructure | dig('config.login.user') }
# → "alice"

mixed_notation: !sub ${ infrastructure | dig('config', 'login.user') }
# → "alice"

list_access: !sub ${ infrastructure | dig('config', 'servers', 1, 'host') }
# → "b.example.com"

list_access_string_index: !sub ${ infrastructure | dig('config.servers.0.host') }
# → "a.example.com"
```

See [undefined variable handling](#undefined-variable-handling).

### Conditional Expressions

Expressions can include Jinja’s inline `if` form, which selects between values based on a condition.

**Syntax:**

```python
<value_if_true> if <condition> [else <value_if_false>]
```

The `else` part is optional.
When omitted, the expression evaluates to `null`.
If the result is used in a string context, it becomes an empty string.

**Examples:**

```yaml
label: !sub "${'Hot' if temperature > 25 else 'Cool'}"
state: !sub "${'ON' if enabled else 'OFF'}"
topic: !sub "${rooms[0] if rooms|length > 0 else 'no-room'}"
```

See also [conditional YAML blocks](conditionals.md)

## Common Pitfalls

When working with expressions and filters, a few patterns can lead to confusing results.
These are the most common issues to watch out for:

::: warning Pitfalls

1. **Forgetting the `!sub` tag**

    Substitution only happens inside nodes tagged with `!sub`.
    If the tag is missing, expressions like `${...}` are left untouched.

    ```yaml
    label: "Room ${index}"       # no substitution happens
    label: !sub "Room ${index}"  # substitution works
    ```

1. **Substitution in keys requires tagging the key or the map**

    Substitution in keys only happens when the key itself — or the map containing it — is tagged with `!sub`.

    ```yaml
    # Tagging the map enables substitution for everything
    # in the map, including all keys and values
    items: !sub
      ${room}_Light:
        label: "Static label"
    ```

    You can also tag just the key if you don't want substitution applied to the whole map:

    ```yaml
    items:
      !sub ${room}_Light:
        label: "Static label"
    ```

1. **Using a reserved keyword as a variable name**

    Certain keywords are reserved in Jinja and should not be used as variable names.
    These include: `in`, `True`, `False`, `true`, `false`, `null`, `empty`, `if`, `else`, `and`, `or`, `not`.

    If you use one of these as a variable, you’ll encounter an error such as:

    ```xml
    syntax error at position 7, encountered '}', expected <IDENTIFIER>|<STRING>|<FLOAT>|<INTEGER>|'true'|'false'|'null'|'-'|'!'|'not'|'empty'|'('
    ```

    To resolve this, rename the variable or use the `VARS` form described in [VARS and Reserved Keywords](#vars-and-reserved-keywords).

1. **Unquoted expressions containing operators**

    Expressions that include operators (`~`, `+`, `-`, `*`, `/`, `==`, etc.) must be quoted, or YAML may misinterpret them.

    ```yaml
    value: !sub "${'Hello ' ~ name}" # Note the "" around the pattern
    ```

1. **Using `+` when you intended string concatenation**

    The `+` operator only works when both operands already have the same type.

    - Two numbers → numeric addition
    - Two strings → string concatenation
    - Mixed types → error

    If your goal is to build strings, use `~`, which always coerces values to text and never fails due to type mismatch.

    ```yaml
    value1: !sub "${1 + 2}"          # → 3    (numeric addition)
    value2: !sub "${'a' + 'b'}"      # → "ab" (string concatenation)
    value3: !sub "${'a' + 1}"        # error  (mixed types)
    value4: !sub "${'a' ~ 1}"        # → "a1" (string coercion + concatenation)
    ```

1. **Confusing string and numeric types in expressions**

    Jinjava respects the types defined in YAML.
    A variable defined as a number behaves numerically; a variable defined as a string behaves textually.
    This affects how operators behave and can lead to surprising results.

    ```yaml
    variables:
      count: 1
      label: "1"

    value1: !sub "${count + 1}"     # → 2    (numeric addition)
    value2: !sub "${label + 1}"     # error  (string + number)
    value3: !sub "${label ~ 1}"     # → "11" (string coercion)
    value4: !sub "${count ~ 1}"     # → "11" (number coerced to string)
    ```

    Use `~` when you want to treat values as text, regardless of how they were defined in YAML.

1. **Mixing filters and strings without `~`**

    When combining filtered values with text, use `~` to ensure proper string conversion.

    ```yaml
    value: !sub "${rooms|length ~ ' rooms'}"
    ```

1. **Whitespace sensitivity**

    Quoted strings preserve all spaces exactly as written.
    But spaces outside quotes — including spaces around expressions inside `${ ... }` — are not preserved.

    ```yaml
    label1: "  padded  "              # spaces preserved
    label2:   padded                  # spaces trimmed by YAML

    label3: !sub "${' x '}"           # → " x " (spaces inside quotes preserved)
    label4: !sub "${   ' x '   }"     # → " x " (outer spaces ignored)
    label5: !sub "${   x   }"         # → value of x only (outer spaces ignored)
    ```

1. **Expecting full Jinja template features**

    Jinjava itself supports statements and macros, but YAML Composer layer does **not**.
    YAML Composer only evaluates `${ ... }` expressions — template blocks such as `{% for %}`, `{% if %}`, and macros are not available.

    ```yaml
    # Supported
    label: !sub "Room ${ index }"

    # Not supported
    {% for room in rooms %}
      {{ room }}
    {% endfor %}
    ```

:::

## Advanced Usage

### Predefined Variables

The Composer injects a set of predefined variables that are automatically available during YAML processing.

Available Predefined Variables:

| Variable           | Description                                                                                                                                            |
|:-------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------|
| `OPENHAB_CONF`     | Absolute path to openHAB's main configuration directory. Typically `/etc/openhab` (apt) or `/openhab/conf` (Docker).                                   |
| `OPENHAB_USERDATA` | Absolute path to openHAB's userdata directory. Typically `/var/lib/openhab` (apt) or `/openhab/userdata` (Docker).                                     |
| `__FILE__`         | Absolute path to the current file, e.g. `/path/to/file.inc.yaml`.                                                                                      |
| `__FILE_NAME__`    | Filename portion without the extension or leading path, e.g. `file.inc`.                                                                               |
| `__FILE_EXT__`     | File extension portion of the current file name, e.g. `yaml`.                                                                                          |
| `__DIRECTORY__`    | Directory portion of the current file, e.g. `/path/to`.                                                                                                |
| `__DIR__`          | Alias for `__DIRECTORY__`.                                                                                                                             |
| `package_id`       | In a package file, automatically resolved to the [Package ID](packages.md#package-syntax-and-structure). Available only within included package files. |

These variables can be interpolated just like regular ones using `${...}` syntax.
They may be helpful when constructing paths for the [!include](include.md) directive.

### `VARS` and Reserved Keywords

Some variable names cannot be referenced normally because Jinja reserves them as expression keywords.

For instance, if you have a variable `and: green`, it cannot be accessed directly, because `and` is the logical‑AND operator in Jinja expressions.
Writing `${"red " + and + " blue"}` will not evaluate to `red green blue` — Jinja will treat `and` as the operator and produce a syntax error.

::: tip How to Reference Variables That Use Reserved Keywords

When a variable name collides with a Jinja reserved keyword, you can access it through the `VARS` dictionary:

```yaml
foo: !sub ${VARS['and']}
```

:::

This form is also useful when a variable name contains characters that are normally invalid in expressions, such as `living-room` or even `living room`.
It is likewise useful when the variable name itself is stored in another variable.
However, for simplicity and readability, such naming patterns should generally be avoided.

### `ENV` to Access Environment Variables

A special variable `ENV` exposes a map of environment variables.
This is especially useful when running openHAB in Docker, where environment variables can be set directly in a `docker-compose` file.

**Example:**

```yaml
# Suppose the environment contains:
#   OPENHAB_FOO=bar
#   OPENHAB_MODE=production

mode: !sub ${ENV.OPENHAB_MODE}   # → "production"
foo:  !sub ${ENV.OPENHAB_FOO}    # → "bar"
```

### Referencing Other Variables During Definition

Variables may reference **other variables**, including those defined earlier in the **same** `variables:` block.
The only requirement is that a variable must be defined **before** it is used.

**Example:**

```yaml
variables:
  foo: bar
  baz: !sub ${foo|upper}   # => foo is defined before baz
```

Variables can also reference [inherited variables](include.md#variable-resolution-order) when used inside included files or packages.

`main.yaml`:

```yaml
variables:
  room: "Kitchen"

items:
  !include child.inc.yaml
```

`child.inc.yaml`:

```yaml
variables:
  label: !sub ${room} Light   # => "Kitchen Light"

ExampleItem:
  label: !sub ${label}
```

::: tip

Referencing other variables also lets you build values step‑by‑step: compute intermediate results with expressions, then combine them into a final variable.

Example:

```yaml
variables:
  contact_type: door   # May be overridden by the including file

  groups: !sub ${ ['AllDoors'] if contact_type == 'door' else ['AllWindows'] }
  semantic_location: LivingRoom
  effective_group: !sub ${ groups + semantic_location }   # => [AllDoors, LivingRoom]
```

:::

### Loading Variable Data From Another File

You can assign a variable to the contents of another file using `!include`.
This can be combined with [merge keys, substitutions, and conditional expressions](merge-keys.md#using-merge-keys-with-sub).
Because `!include` can be parameterized, you can parameterize an entire section and store the result in a variable.

```yaml
variables:
  external_data: !include other_file.inc.yaml
```

### Using `!include` to Perform a Common Transformation

Include files don’t need to return mappings — they can also return a single computed value.
This makes them useful for reusable transformations that you want to apply across multiple files.

**Example:**

`extract_suffix.inc.yaml`

```yaml
# Extracts the numeric suffix from the ${input} variable
# e.g. for "LivingRoom_PIR2" it will extract "2"
!sub ${input.replaceAll(".*?([0-9]*)$", "$1")}
```

This “utility function” can then be used anywhere you need the same transformation:

```yaml
variables: !sub
  suffix: !include extract_suffix.inc.yaml?input=${package_id}
```

### Undefined Variable Handling

Referencing an undefined variable logs a warning and evaluates to `null`.
To avoid triggering this warning, you can first check for the variable's existence using the [VARS](#vars-and-reserved-keywords) special dictionary.

This can be combined with the [default() filter](#default-example) to provide fallback values when a variable is missing.
The [dig](#dig) filter is also useful when navigating deeply nested structures, as it returns `null` instead of raising an error when any key in the chain is absent.

**Examples:**

```yaml
# None of these will trigger a warning despite missing variable `host`
host_defined: !sub ${ 'host' in VARS }            # → false (variable 'host' is not defined)
host: !sub ${ VARS.host }                         # → null
host: !sub ${ VARS | dig('host') }                # → null (alternative method)
user: !sub ${ VARS | dig('config', 'username') }  # → null

# With a default value
host: !sub ${ VARS | dig('config', 'host', 'address') | default('127.0.0.1') }
# → 127.0.0.1
```

### Calling Java Methods

Inside an expression, variables keep their actual Java types, so you can call methods on them just as you would in Java.

Common types you may encounter include:

- [String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html)
- [Integer](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Integer.html)
- [Double](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Double.html)
- [Boolean](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Boolean.html)
- [Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html)
- [List](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html)

This is especially useful when you need functionality beyond the built‑in filters — for example, using `String.replaceAll()` with regular expressions.

**Example:**

```yaml
variables:
  device_id: "sensor_ABC123"

  # Call a Java String method; result is a real Boolean
  is_sensor: !sub ${ device_id.startsWith("sensor_") }

items:
  DeviceInfo:
    type: String
    label: !sub ${ "Sensor Device" if is_sensor else "Other Device" }
```

### Interpolation and Inserted Content

Interpolation (`!sub`) does **not** apply recursively to content inserted via anchors or include files, because interpolation happens **before** merges and includes are applied.

#### Example with an anchor

```yaml
variables:
  room: "Kitchen"

.base: &BASE
  label: "${room}"   # no !sub here -> won't be interpolated

items: !sub
  Item1:
    <<: *BASE        # !sub does not apply to the contents of the anchor
```

**Result:**

```yaml
items:
  Item1:
    label: "${room}" # stays literal
```

#### Example with an include file

`main.yaml`:

```yaml
variables:
  room: Kitchen
  file: child.inc.yaml

items: !sub                # <- this !sub
  Item1:
    <<: !include "${file}" # -> applies to this pattern -> ${file} is interpolated
                           # -> but not to the included content
  Item2:
    label: ${room}         # -> also applies to this pattern
```

`child.inc.yaml`:

```yaml
label: ${room}             # no !sub here -> won't be interpolated
```

**Result:**

```yaml
items:
  Item1:
    label: ${room}        # stays literal
  Item2:
    label: Kitchen
```

### Custom Pattern Delimiters

Most users never need to change the default `${...}` delimiters.
However, if your content already contains `${...}` patterns or you prefer clearer separation, you can define custom delimiters by selecting a **named pattern**.

Custom delimiters are introduced using the syntax:

```yaml
!sub:pattern_name
```

Here, `pattern_name` refers to a variable whose value defines the opening and closing delimiters.
The usual variable‑resolution rules apply.
This also means you can define the pattern in the main file and use the same pattern consistently across all included files.

**Example:**

```yaml
variables:
  square:  "[..]"
  percent: "%(..)"
  jinja:   "{{..}}"

foo: !sub:square   "Hello [mqtt.username]!"
bar: !sub:percent  "Hello %(mqtt.username)!"
baz: !sub:jinja    "Hello {{ mqtt.username }}!"
```

Choose delimiters that are unlikely to appear in your content to avoid accidental substitutions.
