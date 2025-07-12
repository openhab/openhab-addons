"""Configure number in a device through MQTT topic."""

from __future__ import annotations

from typing import Any

import voluptuous as vol

from homeassistant.components.number import (
    DEFAULT_MAX_VALUE,
    DEFAULT_MIN_VALUE,
    DEFAULT_STEP,
    NumberDeviceClass,
    NumberMode,
)
from homeassistant.const import (
    CONF_DEVICE_CLASS,
    CONF_MODE,
    CONF_NAME,
    CONF_UNIT_OF_MEASUREMENT,
    CONF_VALUE_TEMPLATE,
)
from homeassistant.helpers import config_validation as cv

from .config import MQTT_RW_SCHEMA
from .const import (
    CONF_COMMAND_TEMPLATE,
    CONF_PAYLOAD_RESET,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA

CONF_MIN = "min"
CONF_MAX = "max"
CONF_STEP = "step"

DEFAULT_PAYLOAD_RESET = "None"

def validate_config(config: dict[str, Any]) -> dict[str, Any]:
    """Validate that the configuration is valid, throws if it isn't."""
    if config[CONF_MIN] > config[CONF_MAX]:
        raise vol.Invalid(f"{CONF_MAX} must be >= {CONF_MIN}")

    return config


_PLATFORM_SCHEMA_BASE = MQTT_RW_SCHEMA.extend(
    {
        vol.Optional(CONF_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_DEVICE_CLASS): vol.Any(
            vol.All(vol.Lower, vol.Coerce(NumberDeviceClass)), None
        ),
        vol.Optional(CONF_MAX, default=DEFAULT_MAX_VALUE): vol.Coerce(float),
        vol.Optional(CONF_MIN, default=DEFAULT_MIN_VALUE): vol.Coerce(float),
        vol.Optional(CONF_MODE, default=NumberMode.AUTO): vol.Coerce(NumberMode),
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_PAYLOAD_RESET, default=DEFAULT_PAYLOAD_RESET): cv.string,
        vol.Optional(CONF_STEP, default=DEFAULT_STEP): vol.All(
            vol.Coerce(float), vol.Range(min=1e-3)
        ),
        vol.Optional(CONF_UNIT_OF_MEASUREMENT): vol.Any(cv.string, None),
        vol.Optional(CONF_VALUE_TEMPLATE): cv.template,
    },
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = vol.All(
    _PLATFORM_SCHEMA_BASE.extend({}, extra=vol.REMOVE_EXTRA),
    validate_config,
)
