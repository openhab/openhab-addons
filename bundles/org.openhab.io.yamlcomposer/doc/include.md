# Including Other Files

`!include` inserts the referenced file or structure exactly at the position where the include appears.

YAML Composer supports including external YAML files to facilitate modular, reusable, and maintainable configurations.****
This is especially useful for modular reuse, creating device [packages](packages.md), or separating concerns across multiple files.

[[toc]]

## Syntax Options

### Short Form

```yaml
keyname: !include filename.inc.yaml
```

```yaml
keyname: !include filename.inc.yaml?arg1=value1&flag
```

The short form allows you to reference another file directly after the `!include` tag.
Optional arguments may be appended using URL‑style query syntax:

- `arg1=value1` assigns a specific value
- `flag` (a value‑less argument) is interpreted as `true`
- Multiple arguments are separated with `&`
- The file name, argument names, and argument values are URL‑decoded.
  If any of them contain characters such as `+`, `%`, or other reserved symbols, make sure to URL‑encode them.

This form is ideal when you only need to include a file and pass a small number of simple parameters.

### Long Form (supports variables)

```yaml
# Block style (multi-line)
keyname: !include
  file: filename.inc.yaml
  vars:
    var1: value1
    var2: value2
```

```yaml
# Flow style (single-line)
keyname: !include { file: filename.inc.yaml, vars: { var1: value1, var2: value2 } }
```

The `vars:` mapping is layered on top of the file’s current variables to form the evaluation context.
In the long form, the `vars:` section is optional.

::: tip Passing Existing Variables to Included Files

The `vars:` section of an `!include` directive can contain literal values or references to **existing variables**.

Example:

```yaml
keyname: !include
  file: xx.inc.yaml
  vars:
    var1: !sub ${mainvar}
```

The include file can now refer to `var1` without relying on the variable names used in the main file.
This is especially useful when the same include file is shared across multiple configurations that may use different variable names.

:::

The contents of the include file are inserted as the value for the given key.
This means that the top-level keys in the include file become sub-keys of the key in the main file.

## Variable Resolution Order

Understanding how variables interact across files is important when using includes, especially when include files define their own defaults.

When include files are involved, variables can originate from multiple sources.
Their values are resolved according to the following order, from highest priority to lowest priority.

1. Inline `vars` in `!include` directives
1. Global `variables` defined in the main file
1. Local `variables` defined inside the include file

Variables provided in the `vars` section of an `!include` directive are visible only within the included file, but they override both global and local variables.

Local variables defined inside an include file can act as default values when they are not provided in the `!include` directive.
This allows include files to be reused with different parameters while keeping sensible defaults inside the file itself.

**Example:**

`main.yaml`:

```yaml
variables:
  broker: mqtt:broker:main

things:
  mqtt:topic:livingroom-window: !include
    file: mqtt_contact.inc.yaml
    vars:
      label: Living Room Window
      id: livingroom-window

  mqtt:topic:bedroom-window: !include
    file: mqtt_contact.inc.yaml
    vars:
      label: Bedroom Window
      id: bedroom-window
      broker: mqtt:broker:external # override the global broker variable
```

`mqtt_contact.inc.yaml`:

```yaml
bridge: !sub ${broker}
label: !sub ${label}
config:
  availabilityTopic: !sub ${id}/availability
  payloadAvailable: online
  payloadNotAvailable: offline
```

Resulting configuration:

```yaml
things:
  mqtt:topic:livingroom-window:
    bridge: mqtt:broker:main
    label: Living Room Window
    config:
      availabilityTopic: livingroom-window/availability
      payloadAvailable: online
      payloadNotAvailable: offline

  mqtt:topic:bedroom-window:
    bridge: mqtt:broker:external
    label: Bedroom Window
    config:
      availabilityTopic: bedroom-window/availability
      payloadAvailable: online
      payloadNotAvailable: offline
```

## File Naming & Reload Behavior

### Include File Naming

Include files should use a dedicated extension, either `.inc.yaml` or `.inc.yml`.
Files with a normal `.yaml` extension are treated as **main configuration files** and must contain the full [top‑level structure](index.md) required by the YAML configuration schema.

In contrast, `.inc.yaml` files are recognized as include fragments and are only processed when referenced through `!include`.

### Path Resolution

Include file paths may be written as absolute paths or as paths relative to the current file.
YAML Composer also supports two shorthand prefixes that simplify referencing files inside the configuration directory.

#### Shorthand Prefixes

| Shorthand           | Resolves To                         | Meaning                                                                                                                    |
|---------------------|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `@/path`<br>`@path` | `${OPENHAB_CONF}/path`              | The openHAB configuration root. Use when referencing files anywhere under `OPENHAB_CONF`. The slash after `@` is optional. |
| `$/path`<br>`$path` | `${OPENHAB_CONF}/yamlcomposer/path` | The source directory for your extended-syntax YAML files. The slash after `$` is optional.                                 |

##### `@/path` → `${OPENHAB_CONF}/path`

A leading `@` resolves directly to the openHAB configuration root (`OPENHAB_CONF`).

```yaml
key: !include "@/yaml/includes/device.inc.yaml"
# Resolves to: ${OPENHAB_CONF}/yaml/includes/device.inc.yaml
```

**Notes:**

- Paths beginning with `@` **must be quoted** (e.g., `"@/path"`), because YAML plain scalars cannot begin with `@`.
- The slash after `@` is **optional**: `"@yaml/includes/device.inc.yaml"` works the same.
- Use `@` when you want to reference files anywhere under the configuration root without writing long absolute paths.

##### `$/path` → `${OPENHAB_CONF}/yamlcomposer/path`

A leading `$` resolves to `OPENHAB_CONF/yamlcomposer`.

**Example directory layout:**

```sh
OPENHAB_CONF/
  yamlcomposer/
    shared.inc.yaml
    lights/
      kitchen/
        main.yaml
```

If `main.yaml` contains:

```yaml
key: !include "$/shared.inc.yaml"
```

Then `$` resolves to:

```sh
${OPENHAB_CONF}/yamlcomposer
```

So the final resolved path becomes:

```sh
${OPENHAB_CONF}/yamlcomposer/shared.inc.yaml
```

**Notes:**

- The slash after `$` is **optional**: `"$shared.inc.yaml"` works the same.
- It avoids brittle paths like `../../../shared.inc.yaml`.

#### Relative Paths

If the path does not begin with `/`, `@`, or `$` it is interpreted as a path **relative to the directory of the including file**.
You may use `.` and `..` to refer to the current and parent directory.

**Example directory layout:**

```sh
yamlcomposer/
  parent.inc.yaml
  main/
    main.yaml
    shared.inc.yaml
    common/
      defaults.inc.yaml
```

**Same directory:**

```yaml
key: !include "shared.inc.yaml"
# Resolves to: yamlcomposer/main/shared.inc.yaml
```

**Navigate downward (into a subdirectory):**

```yaml
key: !include "common/defaults.inc.yaml"
# Resolves to: yamlcomposer/main/common/defaults.inc.yaml
```

**Navigate upward:**

```yaml
key: !include "../parent.inc.yaml"
# Resolves to: yamlcomposer/parent.inc.yaml
```

Relative paths always resolve from the directory containing the including file.

#### Using Variable Substitutions

You can use variable substitution patterns in the file name to construct paths dynamically.
This includes normal variables, [predefined variables](variables.md#predefined-variables), and environment variables exposed through `ENV`.

These can be interpolated using `!sub ${...}` and are especially useful for building portable or relative include paths.

**Example:**

```yaml
keyname: !include
  file: !sub ${OPENHAB_CONF}/packages/hue-light.pkg.yaml
```

### File Organization

It may be helpful to store include files in a dedicated subdirectory.
These files can be referenced using relative paths, the `@` or `$` shorthands, or full absolute paths.
Choose whichever style best matches your preference.

### Nested Includes

Include files may themselves contain `!include` directives.
This allows configurations to be composed from multiple layers:

main.yaml → a.inc.yaml → b.inc.yaml → …

### Reload Behavior

When an include file changes, YAML Composer reloads the main files that reference it rather than attempting to load the include file directly.
Reloads occur only if the include file is located within `${OPENHAB_CONF}/yamlcomposer/`.
