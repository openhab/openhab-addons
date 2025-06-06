"""Helpers for config validation using voluptuous."""

from __future__ import annotations

from enum import StrEnum
import logging
from numbers import Number
import re
from typing import Any, Callable, cast
from urllib.parse import urlparse

import voluptuous as vol

from homeassistant.const import (
    UnitOfTemperature,
)
from homeassistant.exceptions import TemplateError
from homeassistant.util.yaml.objects import NodeStrClass

from . import template as template_helper
from .frame import get_integration_logger


class UrlProtocolSchema(StrEnum):
    """Valid URL protocol schema values."""

    HTTP = "http"
    HTTPS = "https"
    HOMEASSISTANT = "homeassistant"


EXTERNAL_URL_PROTOCOL_SCHEMA_LIST = frozenset(
    {UrlProtocolSchema.HTTP, UrlProtocolSchema.HTTPS}
)
CONFIGURATION_URL_PROTOCOL_SCHEMA_LIST = frozenset(
    {UrlProtocolSchema.HOMEASSISTANT, UrlProtocolSchema.HTTP, UrlProtocolSchema.HTTPS}
)

# Home Assistant types
positive_int = vol.All(vol.Coerce(int), vol.Range(min=0))
positive_float = vol.All(vol.Coerce(float), vol.Range(min=0))

def has_at_most_one_key(*keys: Any) -> Callable[[dict], dict]:
    """Validate that zero keys exist or one key exists."""

    def validate(obj: dict) -> dict:
        """Test zero keys exist or one key exists in dict."""
        if not isinstance(obj, dict):
            raise vol.Invalid("expected dictionary")

        if len(set(keys) & set(obj)) > 1:
            expected = ", ".join(str(k) for k in keys)
            raise vol.Invalid(f"must contain at most one of {expected}.")
        return obj

    return validate


def boolean(value: Any) -> bool:
    """Validate and coerce a boolean value."""
    if isinstance(value, bool):
        return value
    if isinstance(value, str):
        value = value.lower().strip()
        if value in ("1", "true", "yes", "on", "enable"):
            return True
        if value in ("0", "false", "no", "off", "disable"):
            return False
    elif isinstance(value, Number):
        # type ignore: https://github.com/python/mypy/issues/3186
        return value != 0  # type: ignore[comparison-overlap]
    raise vol.Invalid(f"invalid boolean value {value}")


def is_regex(value: Any) -> re.Pattern[Any]:
    """Validate that a string is a valid regular expression."""
    try:
        r = re.compile(value)
    except TypeError as err:
        raise vol.Invalid(
            f"value {value} is of the wrong type for a regular expression"
        ) from err
    except re.error as err:
        raise vol.Invalid(f"value {value} is not a valid regular expression") from err
    return r


def ensure_list(value) -> list:
    """Wrap value in list if it is not one."""
    if value is None:
        return []
    if isinstance(value, list):
        return value
    return [value]


def icon(value: Any) -> str:
    """Validate icon."""
    str_value = str(value)

    if ":" in str_value:
        return str_value

    raise vol.Invalid('Icons should be specified in the form "prefix:name"')


def string(value: Any) -> str:
    """Coerce value to string, except for None."""
    if value is None:
        raise vol.Invalid("string value is None")

    # This is expected to be the most common case, so check it first.
    if type(value) is str or type(value) is NodeStrClass or isinstance(value, str):
        return value

    if isinstance(value, template_helper.ResultWrapper):
        value = value.render_result

    elif isinstance(value, (list, dict)):
        raise vol.Invalid("value should be a string")

    return str(value)


def url(
    value: Any,
    _schema_list: frozenset = EXTERNAL_URL_PROTOCOL_SCHEMA_LIST,
) -> str:
    """Validate an URL."""
    url_in = str(value)

    if urlparse(url_in).scheme in _schema_list:
        return cast(str, vol.Schema(vol.Url())(url_in))

    raise vol.Invalid("invalid url")


def configuration_url(value: Any) -> str:
    """Validate an URL that allows the homeassistant schema."""
    return url(value, CONFIGURATION_URL_PROTOCOL_SCHEMA_LIST)


def temperature_unit(value: Any) -> UnitOfTemperature:
    """Validate and transform temperature unit."""
    value = str(value).upper()
    if value == "C":
        return UnitOfTemperature.CELSIUS
    if value == "F":
        return UnitOfTemperature.FAHRENHEIT
    raise vol.Invalid("invalid temperature unit (expected C or F)")


def template(value: Any | None) -> template_helper.Template:
    """Validate a jinja2 template."""
    if value is None:
        raise vol.Invalid("template value is None")
    if isinstance(value, (list, dict, template_helper.Template)):
        raise vol.Invalid("template value should be a string")

    template_value = template_helper.Template(str(value))

    try:
        template_value.ensure_valid()
    except TemplateError as ex:
        raise vol.Invalid(f"invalid template ({ex})") from ex
    return template_value


def _deprecated_or_removed(
    key: str,
    replacement_key: str | None,
    default: Any | None,
    raise_if_present: bool,
    option_removed: bool,
) -> Callable[[dict], dict]:
    """Log key as deprecated and provide a replacement (if exists) or fail.

    Expected behavior:
        - Outputs or throws the appropriate deprecation warning if key is detected
        - Outputs or throws the appropriate error if key is detected
          and removed from support
        - Processes schema moving the value from key to replacement_key
        - Processes schema changing nothing if only replacement_key provided
        - No warning if only replacement_key provided
        - No warning if neither key nor replacement_key are provided
            - Adds replacement_key with default value in this case
    """

    def validator(config: dict) -> dict:
        """Check if key is in config and log warning or error."""
        if key in config:
            if option_removed:
                level = logging.ERROR
                option_status = "has been removed"
            else:
                level = logging.WARNING
                option_status = "is deprecated"

            try:
                near = (
                    f"near {config.__config_file__}"  # type: ignore[attr-defined]
                    f":{config.__line__} "  # type: ignore[attr-defined]
                )
            except AttributeError:
                near = ""
            arguments: tuple[str, ...]
            if replacement_key:
                warning = "The '%s' option %s%s, please replace it with '%s'"
                arguments = (key, near, option_status, replacement_key)
            else:
                warning = (
                    "The '%s' option %s%s, please remove it from your configuration"
                )
                arguments = (key, near, option_status)

            if raise_if_present:
                raise vol.Invalid(warning % arguments)

            get_integration_logger(__name__).log(level, warning, *arguments)
            value = config[key]
            if replacement_key or option_removed:
                config.pop(key)
        else:
            value = default

        keys = [key]
        if replacement_key:
            keys.append(replacement_key)
            if value is not None and (
                replacement_key not in config or default == config.get(replacement_key)
            ):
                config[replacement_key] = value

        return has_at_most_one_key(*keys)(config)

    return validator


def deprecated(
    key: str,
    replacement_key: str | None = None,
    default: Any | None = None,
    raise_if_present: bool | None = False,
) -> Callable[[dict], dict]:
    """Log key as deprecated and provide a replacement (if exists).

    Expected behavior:
        - Outputs the appropriate deprecation warning if key is detected
          or raises an exception
        - Processes schema moving the value from key to replacement_key
        - Processes schema changing nothing if only replacement_key provided
        - No warning if only replacement_key provided
        - No warning if neither key nor replacement_key are provided
            - Adds replacement_key with default value in this case
    """
    return _deprecated_or_removed(
        key,
        replacement_key=replacement_key,
        default=default,
        raise_if_present=raise_if_present or False,
        option_removed=False,
    )


def removed(
    key: str,
    default: Any | None = None,
    raise_if_present: bool | None = True,
) -> Callable[[dict], dict]:
    """Log key as deprecated and fail the config validation.

    Expected behavior:
        - Outputs the appropriate error if key is detected and removed from
          support or raises an exception.
    """
    return _deprecated_or_removed(
        key,
        replacement_key=None,
        default=default,
        raise_if_present=raise_if_present or False,
        option_removed=True,
    )
