"""Support for MQTT sirens."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.const import (
    CONF_NAME,
    CONF_PAYLOAD_OFF,
    CONF_PAYLOAD_ON,
)
from homeassistant.helpers import config_validation as cv

from .config import MQTT_RW_SCHEMA
from .const import (
    CONF_AVAILABLE_TONES,
    CONF_COMMAND_OFF_TEMPLATE,
    CONF_COMMAND_TEMPLATE,
    CONF_COMMAND_TOPIC,
    CONF_STATE_OFF,
    CONF_STATE_ON,
    CONF_STATE_VALUE_TEMPLATE,
    CONF_SUPPORT_DURATION,
    CONF_SUPPORT_VOLUME_SET,
    DEFAULT_PAYLOAD_OFF,
    DEFAULT_PAYLOAD_ON,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA

PLATFORM_SCHEMA_MODERN = MQTT_RW_SCHEMA.extend(
    {
        vol.Optional(CONF_AVAILABLE_TONES): cv.ensure_list,
        vol.Optional(CONF_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_COMMAND_OFF_TEMPLATE): cv.template,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_PAYLOAD_OFF, default=DEFAULT_PAYLOAD_OFF): cv.string,
        vol.Optional(CONF_PAYLOAD_ON, default=DEFAULT_PAYLOAD_ON): cv.string,
        vol.Optional(CONF_STATE_OFF): cv.string,
        vol.Optional(CONF_STATE_ON): cv.string,
        vol.Optional(CONF_STATE_VALUE_TEMPLATE): cv.template,
        vol.Optional(CONF_SUPPORT_DURATION, default=True): cv.boolean,
        vol.Optional(CONF_SUPPORT_VOLUME_SET, default=True): cv.boolean,
    },
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = vol.All(PLATFORM_SCHEMA_MODERN.extend({}, extra=vol.REMOVE_EXTRA))
