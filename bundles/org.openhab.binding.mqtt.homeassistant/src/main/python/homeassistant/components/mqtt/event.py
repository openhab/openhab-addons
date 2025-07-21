"""Support for MQTT events."""

from __future__ import annotations

from collections.abc import Callable
import logging
from typing import Any

import voluptuous as vol

from homeassistant.components.event import EventDeviceClass
from homeassistant.const import CONF_DEVICE_CLASS, CONF_NAME
from homeassistant.helpers import config_validation as cv

from .config import MQTT_RO_SCHEMA
from .schemas import MQTT_ENTITY_COMMON_SCHEMA

CONF_EVENT_TYPES = "event_types"

DEVICE_CLASS_SCHEMA = vol.All(vol.Lower, vol.Coerce(EventDeviceClass))

_PLATFORM_SCHEMA_BASE = MQTT_RO_SCHEMA.extend(
    {
        vol.Optional(CONF_DEVICE_CLASS): DEVICE_CLASS_SCHEMA,
        vol.Optional(CONF_NAME): vol.Any(None, cv.string),
        vol.Required(CONF_EVENT_TYPES): vol.All(cv.ensure_list, [cv.string]),
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = vol.All(
    _PLATFORM_SCHEMA_BASE.extend({}, extra=vol.REMOVE_EXTRA),
)
