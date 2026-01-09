"""Provides tag scanning for MQTT."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.const import CONF_DEVICE, CONF_VALUE_TEMPLATE
from homeassistant.helpers import config_validation as cv

from .config import MQTT_BASE_SCHEMA
from .const import CONF_TOPIC
from .schemas import MQTT_ENTITY_DEVICE_INFO_SCHEMA
from .util import valid_subscribe_topic

DISCOVERY_SCHEMA = MQTT_BASE_SCHEMA.extend(
    {
        vol.Optional(CONF_DEVICE): MQTT_ENTITY_DEVICE_INFO_SCHEMA,
        vol.Required(CONF_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_VALUE_TEMPLATE): cv.template,
    },
    extra=vol.REMOVE_EXTRA,
)
