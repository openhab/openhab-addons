# YAML Composer

YAML Composer introduces extended YAML features that make openHAB configuration more modular, reusable, and maintainable. These features let you structure configuration as composable building blocks rather than large, repetitive files.

The add-on loads enhanced-syntax YAML files from `OPENHAB_CONF/yamlcomposer/` and compiles them into fully resolved plain YAML written to `OPENHAB_CONF/yaml/composed/`.

[[toc]]

## Feature Summary

YAML Composer adds several enhancements on top of standard YAML.
Each feature addresses a different kind of reuse, composition, or abstraction to help you build cleaner and more maintainable YAML.

| Feature                                    | Purpose                                                   | Typical Use                                                                                                    |
|--------------------------------------------|-----------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| **Variables and Substitution (`!sub`)**    | Insert dynamic values or evaluate expressions             | Build labels, topics, IDs, or computed values                                                                  |
| **Conditionals (`!if`)**                   | Conditionally include or exclude YAML blocks              | Enable or disable features when using packages or template flags                                               |
| **Include (`!include`)**                   | Insert the contents of another file                       | Reuse YAML across files; parameterize reusable blocks                                                          |
| **Templates (`!insert`)**                  | Reuse YAML defined within the same file                   | Local parameterized blocks; reusable channel or item fragments                                                 |
| **Packages**                               | Bundle multiple top-level sections into one reusable unit | Define reusable device structures containing things, items, metadata; sourced from external files or templates |
| **Anchors and Aliases (`&name`, `*name`)** | Define small, reusable YAML fragments                     | Static defaults, shared fields                                                                                 |
| **Merge Keys (`<<:`)**                     | Combine mappings from multiple sources                    | Layer defaults, override fields, compose structures                                                            |

Each feature has a dedicated documentation page:

- [Variables and Substitution](doc/variables.md)
- [Conditionals](doc/conditionals.md)
- [Include](doc/include.md)
- [Templates](doc/templates.md)
- [Packages](doc/packages.md)
- [Anchors and Aliases](doc/anchors.md)
- [Merge Keys](doc/merge-keys.md)

These features can be used independently, but they become especially powerful when combined.

For a general introduction to YAML, see [YAML Basics](doc/basics.md).

## Processing Overview

YAML Composer reads enhanced-syntax YAML files and performs a compilation pass that expands all extended features into a single, fully resolved YAML document that openHAB can load.

During compilation, YAML Composer performs the following steps:

1. **YAML Parsing**: The source file is parsed into an internal structure.
2. **Variable Substitution (`!sub`)**: Expressions are evaluated and injected.
3. **Conditionals (`!if`)**: Conditional logic determines which blocks remain.
4. **Template and Include Expansion**: `!insert` and `!include` bring in referenced content, often using resolved variables.
5. **Package Expansion**: External or local packages are loaded and expanded into their component sections.
6. **Recursive Merging**: Merge keys and package structures are combined into the main document.
7. **Hidden Key Removal**: Keys beginning with `.` are removed from the final output.

The resulting YAML contains:

- all variables resolved
- all conditionals evaluated
- all templates and includes expanded
- all anchors and merges applied
- all packages integrated
- all hidden keys removed

This final compiled YAML is written to `CONF/yaml/composed/`, where openHAB loads it as Things, Items, Metadata, and other configuration elements as defined by the Core YAML Configuration structure.

## Hidden Keys

Keys beginning with a dot (`.`) are treated as hidden. They:

- exist only during compilation
- are ideal for storing anchors, templates, or shared structures
- keep visible configuration clean
- are removed from the final output

**Example:**

```yaml
.base-switch: &BASE_SWITCH
  type: Switch
  autoupdate: false

items:
  Light1:
    <<: *BASE_SWITCH
    label: Light One
```

## File Structure and Conventions

YAML files can be organized freely, but the following conventions improve clarity and maintainability:

- Place `variables:` and `templates:` near the top of the file.
- Group reusable structures under hidden keys.
- Use anchors for static fragments and includes for parameterized ones.
- Keep packages in separate files when they represent reusable device or feature definitions.

These conventions are optional but help keep complex configurations predictable and easy to navigate.
