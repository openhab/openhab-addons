"""Support for MQTT images."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.const import CONF_NAME
from homeassistant.helpers import config_validation as cv
from homeassistant.helpers.typing import ConfigType

from .config import MQTT_BASE_SCHEMA
from .const import (
    CONF_CONTENT_TYPE,
    CONF_IMAGE_ENCODING,
    CONF_IMAGE_TOPIC,
    CONF_URL_TEMPLATE,
    CONF_URL_TOPIC,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_subscribe_topic

def validate_topic_required(config: ConfigType) -> ConfigType:
    """Ensure at least one subscribe topic is configured."""
    if CONF_IMAGE_TOPIC not in config and CONF_URL_TOPIC not in config:
        raise vol.Invalid("Expected one of [`image_topic`, `url_topic`], got none")
    if CONF_CONTENT_TYPE in config and CONF_URL_TOPIC in config:
        raise vol.Invalid(
            "Option `content_type` can not be used together with `url_topic`"
        )
    return config


PLATFORM_SCHEMA_BASE = MQTT_BASE_SCHEMA.extend(
    {
        vol.Optional(CONF_CONTENT_TYPE): cv.string,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Exclusive(CONF_URL_TOPIC, "image_topic"): valid_subscribe_topic,
        vol.Exclusive(CONF_IMAGE_TOPIC, "image_topic"): valid_subscribe_topic,
        vol.Optional(CONF_IMAGE_ENCODING): vol.In({"b64", "raw"}),
        vol.Optional(CONF_URL_TEMPLATE): cv.template,
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

PLATFORM_SCHEMA_MODERN = vol.All(PLATFORM_SCHEMA_BASE.schema, validate_topic_required)

DISCOVERY_SCHEMA = vol.All(
    PLATFORM_SCHEMA_BASE.extend({}, extra=vol.REMOVE_EXTRA), validate_topic_required
)
