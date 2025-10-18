"""Support for MQTT text platform."""

from __future__ import annotations

from typing import Any

import voluptuous as vol

from homeassistant.components import text
from homeassistant.const import (
    CONF_MODE,
    CONF_NAME,
    CONF_VALUE_TEMPLATE,
    MAX_LENGTH_STATE_STATE,
)
from homeassistant.helpers import config_validation as cv

from .config import MQTT_RW_SCHEMA
from .const import CONF_COMMAND_TEMPLATE
from .schemas import MQTT_ENTITY_COMMON_SCHEMA

CONF_MAX = "max"
CONF_MIN = "min"
CONF_PATTERN = "pattern"


def valid_text_size_configuration(config: dict[str, Any]) -> dict[str, Any]:
    """Validate that the text length configuration is valid, throws if it isn't."""
    if config[CONF_MIN] > config[CONF_MAX]:
        raise vol.Invalid("text length min must be <= max")
    if config[CONF_MAX] > MAX_LENGTH_STATE_STATE:
        raise vol.Invalid(f"max text length must be <= {MAX_LENGTH_STATE_STATE}")

    return config


_PLATFORM_SCHEMA_BASE = MQTT_RW_SCHEMA.extend(
    {
        vol.Optional(CONF_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_MAX, default=MAX_LENGTH_STATE_STATE): cv.positive_int,
        vol.Optional(CONF_MIN, default=0): cv.positive_int,
        vol.Optional(CONF_MODE, default=text.TextMode.TEXT): vol.In(
            [text.TextMode.TEXT, text.TextMode.PASSWORD]
        ),
        vol.Optional(CONF_PATTERN): cv.is_regex,
        vol.Optional(CONF_VALUE_TEMPLATE): cv.template,
    },
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)


DISCOVERY_SCHEMA = vol.All(
    _PLATFORM_SCHEMA_BASE.extend({}, extra=vol.REMOVE_EXTRA),
    valid_text_size_configuration,
)
