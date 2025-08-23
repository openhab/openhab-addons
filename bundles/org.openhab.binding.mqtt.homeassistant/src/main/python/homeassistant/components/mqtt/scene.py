"""Support for MQTT scenes."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.const import CONF_NAME, CONF_PAYLOAD_ON
from homeassistant.helpers import config_validation as cv

from .config import MQTT_BASE_SCHEMA
from .const import CONF_COMMAND_TOPIC, CONF_RETAIN
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_publish_topic

DEFAULT_RETAIN = False

PLATFORM_SCHEMA_MODERN = MQTT_BASE_SCHEMA.extend(
    {
        vol.Required(CONF_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_PAYLOAD_ON): cv.string,
        vol.Optional(CONF_RETAIN, default=DEFAULT_RETAIN): cv.boolean,
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = PLATFORM_SCHEMA_MODERN.extend({}, extra=vol.REMOVE_EXTRA)
