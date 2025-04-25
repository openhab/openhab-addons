"""Support for tracking MQTT enabled devices identified."""

from __future__ import annotations

from typing import Any

import voluptuous as vol

from homeassistant.components.device_tracker import SourceType
from homeassistant.const import (
    CONF_NAME,
    CONF_VALUE_TEMPLATE,
    STATE_HOME,
    STATE_NOT_HOME,
)
from homeassistant.helpers import config_validation as cv

from .config import MQTT_BASE_SCHEMA
from .const import (
    CONF_JSON_ATTRS_TOPIC,
    CONF_PAYLOAD_RESET,
    CONF_STATE_TOPIC,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_subscribe_topic

CONF_PAYLOAD_HOME = "payload_home"
CONF_PAYLOAD_NOT_HOME = "payload_not_home"
CONF_SOURCE_TYPE = "source_type"

DEFAULT_PAYLOAD_RESET = "None"
DEFAULT_SOURCE_TYPE = SourceType.GPS


def valid_config(config: dict[str, Any]) -> dict[str, Any]:
    """Check if there is a state topic or json_attributes_topic."""
    if CONF_STATE_TOPIC not in config and CONF_JSON_ATTRS_TOPIC not in config:
        raise vol.Invalid(
            f"Invalid device tracker config, missing {CONF_STATE_TOPIC} or {CONF_JSON_ATTRS_TOPIC}, got: {config}"
        )
    return config


PLATFORM_SCHEMA_MODERN_BASE = MQTT_BASE_SCHEMA.extend(
    {
        vol.Optional(CONF_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_VALUE_TEMPLATE): cv.template,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_PAYLOAD_HOME, default=STATE_HOME): cv.string,
        vol.Optional(CONF_PAYLOAD_NOT_HOME, default=STATE_NOT_HOME): cv.string,
        vol.Optional(CONF_PAYLOAD_RESET, default=DEFAULT_PAYLOAD_RESET): cv.string,
        vol.Optional(CONF_SOURCE_TYPE, default=DEFAULT_SOURCE_TYPE): vol.Coerce(
            SourceType
        ),
    },
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = vol.All(
    PLATFORM_SCHEMA_MODERN_BASE.extend({}, extra=vol.REMOVE_EXTRA), valid_config
)
