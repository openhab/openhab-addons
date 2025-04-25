"""Configure update platform in a device through MQTT topic."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.components.update import (
    DEVICE_CLASSES_SCHEMA,
)
from homeassistant.const import CONF_DEVICE_CLASS, CONF_NAME
from homeassistant.helpers import config_validation as cv

from .config import DEFAULT_RETAIN, MQTT_RO_SCHEMA
from .const import CONF_COMMAND_TOPIC, CONF_RETAIN
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_publish_topic, valid_subscribe_topic

CONF_DISPLAY_PRECISION = "display_precision"
CONF_LATEST_VERSION_TEMPLATE = "latest_version_template"
CONF_LATEST_VERSION_TOPIC = "latest_version_topic"
CONF_PAYLOAD_INSTALL = "payload_install"
CONF_RELEASE_SUMMARY = "release_summary"
CONF_RELEASE_URL = "release_url"
CONF_TITLE = "title"


PLATFORM_SCHEMA_MODERN = MQTT_RO_SCHEMA.extend(
    {
        vol.Optional(CONF_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_DEVICE_CLASS): vol.Any(DEVICE_CLASSES_SCHEMA, None),
        vol.Optional(CONF_DISPLAY_PRECISION, default=0): cv.positive_int,
        vol.Optional(CONF_LATEST_VERSION_TEMPLATE): cv.template,
        vol.Optional(CONF_LATEST_VERSION_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_PAYLOAD_INSTALL): cv.string,
        vol.Optional(CONF_RELEASE_SUMMARY): cv.string,
        vol.Optional(CONF_RELEASE_URL): cv.string,
        vol.Optional(CONF_RETAIN, default=DEFAULT_RETAIN): cv.boolean,
        vol.Optional(CONF_TITLE): cv.string,
    },
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)


DISCOVERY_SCHEMA = vol.All(PLATFORM_SCHEMA_MODERN.extend({}, extra=vol.REMOVE_EXTRA))
