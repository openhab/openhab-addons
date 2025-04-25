"""Provides device automations for MQTT."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.const import (
    CONF_DEVICE,
    CONF_TYPE,
    CONF_VALUE_TEMPLATE,
)
from homeassistant.helpers import config_validation as cv

from .config import MQTT_BASE_SCHEMA
from .const import (
    CONF_PAYLOAD,
    CONF_TOPIC,
)
from .schemas import MQTT_ENTITY_DEVICE_INFO_SCHEMA

CONF_AUTOMATION_TYPE = "automation_type"
CONF_SUBTYPE = "subtype"

TRIGGER_DISCOVERY_SCHEMA = MQTT_BASE_SCHEMA.extend(
    {
        vol.Required(CONF_AUTOMATION_TYPE): str,
        vol.Required(CONF_DEVICE): MQTT_ENTITY_DEVICE_INFO_SCHEMA,
        vol.Optional(CONF_PAYLOAD, default=None): vol.Any(None, cv.string),
        vol.Required(CONF_SUBTYPE): cv.string,
        vol.Required(CONF_TOPIC): cv.string,
        vol.Required(CONF_TYPE): cv.string,
        vol.Optional(CONF_VALUE_TEMPLATE, default=None): vol.Any(None, cv.string),
    },
    extra=vol.REMOVE_EXTRA,
)
