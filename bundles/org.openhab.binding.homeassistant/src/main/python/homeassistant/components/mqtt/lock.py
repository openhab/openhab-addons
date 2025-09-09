"""Support for MQTT locks."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.const import (
    CONF_NAME,
    CONF_VALUE_TEMPLATE,
)
from homeassistant.helpers import config_validation as cv
from .config import MQTT_RW_SCHEMA
from .const import (
    CONF_COMMAND_TEMPLATE,
    CONF_PAYLOAD_RESET,
    CONF_STATE_OPEN,
    CONF_STATE_OPENING,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA

CONF_CODE_FORMAT = "code_format"

CONF_PAYLOAD_LOCK = "payload_lock"
CONF_PAYLOAD_UNLOCK = "payload_unlock"
CONF_PAYLOAD_OPEN = "payload_open"

CONF_STATE_LOCKED = "state_locked"
CONF_STATE_LOCKING = "state_locking"

CONF_STATE_UNLOCKED = "state_unlocked"
CONF_STATE_UNLOCKING = "state_unlocking"
CONF_STATE_JAMMED = "state_jammed"

DEFAULT_PAYLOAD_LOCK = "LOCK"
DEFAULT_PAYLOAD_UNLOCK = "UNLOCK"
DEFAULT_PAYLOAD_OPEN = "OPEN"
DEFAULT_PAYLOAD_RESET = "None"
DEFAULT_STATE_LOCKED = "LOCKED"
DEFAULT_STATE_LOCKING = "LOCKING"
DEFAULT_STATE_OPEN = "OPEN"
DEFAULT_STATE_OPENING = "OPENING"
DEFAULT_STATE_UNLOCKED = "UNLOCKED"
DEFAULT_STATE_UNLOCKING = "UNLOCKING"
DEFAULT_STATE_JAMMED = "JAMMED"

PLATFORM_SCHEMA_MODERN = MQTT_RW_SCHEMA.extend(
    {
        vol.Optional(CONF_CODE_FORMAT): cv.is_regex,
        vol.Optional(CONF_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_PAYLOAD_LOCK, default=DEFAULT_PAYLOAD_LOCK): cv.string,
        vol.Optional(CONF_PAYLOAD_UNLOCK, default=DEFAULT_PAYLOAD_UNLOCK): cv.string,
        vol.Optional(CONF_PAYLOAD_OPEN): cv.string,
        vol.Optional(CONF_PAYLOAD_RESET, default=DEFAULT_PAYLOAD_RESET): cv.string,
        vol.Optional(CONF_STATE_JAMMED, default=DEFAULT_STATE_JAMMED): cv.string,
        vol.Optional(CONF_STATE_LOCKED, default=DEFAULT_STATE_LOCKED): cv.string,
        vol.Optional(CONF_STATE_LOCKING, default=DEFAULT_STATE_LOCKING): cv.string,
        vol.Optional(CONF_STATE_OPEN, default=DEFAULT_STATE_OPEN): cv.string,
        vol.Optional(CONF_STATE_OPENING, default=DEFAULT_STATE_OPENING): cv.string,
        vol.Optional(CONF_STATE_UNLOCKED, default=DEFAULT_STATE_UNLOCKED): cv.string,
        vol.Optional(CONF_STATE_UNLOCKING, default=DEFAULT_STATE_UNLOCKING): cv.string,
        vol.Optional(CONF_VALUE_TEMPLATE): cv.template,
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = PLATFORM_SCHEMA_MODERN.extend({}, extra=vol.REMOVE_EXTRA)
