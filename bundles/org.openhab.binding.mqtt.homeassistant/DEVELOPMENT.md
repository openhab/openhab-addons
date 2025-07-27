# MQTT Home Assistant Binding Development

src/main/python is forked from [Home Assistant core](https://github.com/home-assistant/core), in order to have near-perfect compatibility with for the Jinja templates.
It was forked from the dev branch as of 2025-04-23, corresponding to the 2025.4.3 release of Home Assistant.

The following alterations have been made:

- Code not specifically used by this binding has been stripped out.
- Generics and some type checks have been removed, being incompatible with GraalPy 24.2, which roughly corresponds with Python 3.11.
- The standard json library is used, instead of orjson, since orjson requires a Rust compiler and would pre-compile native extensions for the architecture of the build environment, and embed them in the JAR, thus making it incompatible with other runtime architectures.
  AFAICT this should still be fully compatible, since Home Assistant explicitly sets multiple options in order to disable features that are orjson specific.
- ciso8601 is not included, since it has a native extension. Instead, the stdlib parser is used.
- All asynchronous processing has been removed; the Java side threading model dominates.
- The `hass` variable has been removed from templates; Limited templates (which are what MQTT integrations use) set it to `None` anyway.
- Limited and strict template options have been removed; it's assumed that templates are limited and not strict.
