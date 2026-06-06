"""Camera that loads a picture from an MQTT topic."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.const import CONF_NAME
from homeassistant.helpers import config_validation as cv

from .config import MQTT_BASE_SCHEMA
from .const import CONF_TOPIC
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_subscribe_topic

CONF_IMAGE_ENCODING = "image_encoding"

PLATFORM_SCHEMA_BASE = MQTT_BASE_SCHEMA.extend(
    {
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Required(CONF_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_IMAGE_ENCODING): "b64",
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = PLATFORM_SCHEMA_BASE.extend({}, extra=vol.REMOVE_EXTRA)
