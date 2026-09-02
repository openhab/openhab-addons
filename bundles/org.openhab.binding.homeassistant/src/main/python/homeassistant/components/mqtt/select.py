"""Configure select in a device through MQTT topic."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.const import CONF_NAME, CONF_VALUE_TEMPLATE
from homeassistant.helpers import config_validation as cv

from .config import MQTT_RW_SCHEMA
from .const import (
    CONF_COMMAND_TEMPLATE,
    CONF_OPTIONS,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA

PLATFORM_SCHEMA_MODERN = MQTT_RW_SCHEMA.extend(
    {
        vol.Optional(CONF_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Required(CONF_OPTIONS): cv.ensure_list,
        vol.Optional(CONF_VALUE_TEMPLATE): cv.template,
    },
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = vol.All(PLATFORM_SCHEMA_MODERN.extend({}, extra=vol.REMOVE_EXTRA))
