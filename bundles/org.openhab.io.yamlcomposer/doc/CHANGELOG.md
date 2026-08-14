# Changelog

All notable changes to the `YamlComposer` component will be documented in this file.

## [openHAB 5.3]

### New Features

- **[Key-Level `!if`](conditionals#keylevel-form):** Added a shorthand form for the `!if` tag to streamline conditional blocks.
- **[Loop Support](loops):** Added `!for` directive for dynamic sequence iteration and templating.
- **Jinjava Functions & Enumerate:** Added [range()](variables#range), and `enumerate` (as both a filter and function supporting lists and maps with `.key`/`.value` entry accessors) to be used in expressions and loops.
- **[Ruby-Style Ranges](variables#ruby-style-range-syntax):** Added support for Ruby-style range syntax (`[1..5]` inclusive and `[1...5]` exclusive) as an alternative syntax for `range()`.
- **[Inline `!var` Directive](variables#inline-var-directives):** Added `!var` directive for local variable declarations.
  Variables are scoped strictly to the active mapping level and its children, remaining hidden from parent and sibling contexts.

---

## [openHAB 5.2]

- Initial release.
