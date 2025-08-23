"""Support for MQTT buttons."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.components.button import DEVICE_CLASSES_SCHEMA
from homeassistant.const import CONF_DEVICE_CLASS, CONF_NAME
from homeassistant.helpers import config_validation as cv

from .config import DEFAULT_RETAIN, MQTT_BASE_SCHEMA
from .const import CONF_COMMAND_TEMPLATE, CONF_COMMAND_TOPIC, CONF_RETAIN
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_publish_topic

CONF_PAYLOAD_PRESS = "payload_press"
DEFAULT_PAYLOAD_PRESS = "PRESS"

PLATFORM_SCHEMA_MODERN = MQTT_BASE_SCHEMA.extend(
    {
        vol.Optional(CONF_COMMAND_TEMPLATE): cv.template,
        vol.Required(CONF_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_DEVICE_CLASS): vol.Any(DEVICE_CLASSES_SCHEMA, None),
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_PAYLOAD_PRESS, default=DEFAULT_PAYLOAD_PRESS): cv.string,
        vol.Optional(CONF_RETAIN, default=DEFAULT_RETAIN): cv.boolean,
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = PLATFORM_SCHEMA_MODERN.extend({}, extra=vol.REMOVE_EXTRA)
