"""Support for MQTT valve devices."""

from __future__ import annotations

from typing import Any

import voluptuous as vol

from homeassistant.components.valve import (
    DEVICE_CLASSES_SCHEMA,
    ValveState,
)
from homeassistant.const import (
    CONF_DEVICE_CLASS,
    CONF_NAME,
    CONF_OPTIMISTIC,
    CONF_VALUE_TEMPLATE,
)
from homeassistant.helpers import config_validation as cv

from .config import MQTT_BASE_SCHEMA
from .const import (
    CONF_COMMAND_TEMPLATE,
    CONF_COMMAND_TOPIC,
    CONF_PAYLOAD_CLOSE,
    CONF_PAYLOAD_OPEN,
    CONF_PAYLOAD_STOP,
    CONF_POSITION_CLOSED,
    CONF_POSITION_OPEN,
    CONF_RETAIN,
    CONF_STATE_CLOSED,
    CONF_STATE_CLOSING,
    CONF_STATE_OPEN,
    CONF_STATE_OPENING,
    CONF_STATE_TOPIC,
    DEFAULT_OPTIMISTIC,
    DEFAULT_PAYLOAD_CLOSE,
    DEFAULT_PAYLOAD_OPEN,
    DEFAULT_POSITION_CLOSED,
    DEFAULT_POSITION_OPEN,
    DEFAULT_RETAIN,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_publish_topic, valid_subscribe_topic

CONF_REPORTS_POSITION = "reports_position"

NO_POSITION_KEYS = (
    CONF_PAYLOAD_CLOSE,
    CONF_PAYLOAD_OPEN,
    CONF_STATE_CLOSED,
    CONF_STATE_OPEN,
)

DEFAULTS = {
    CONF_PAYLOAD_CLOSE: DEFAULT_PAYLOAD_CLOSE,
    CONF_PAYLOAD_OPEN: DEFAULT_PAYLOAD_OPEN,
    CONF_STATE_OPEN: ValveState.OPEN,
    CONF_STATE_CLOSED: ValveState.CLOSED,
}


def _validate_and_add_defaults(config: dict[str, Any]) -> dict[str, Any]:
    """Validate config options and set defaults."""
    if config[CONF_REPORTS_POSITION] and any(key in config for key in NO_POSITION_KEYS):
        raise vol.Invalid(
            "Options `payload_open`, `payload_close`, `state_open` and "
            "`state_closed` are not allowed if the valve reports a position."
        )
    return {**DEFAULTS, **config}


_PLATFORM_SCHEMA_BASE = MQTT_BASE_SCHEMA.extend(
    {
        vol.Optional(CONF_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_DEVICE_CLASS): vol.Any(DEVICE_CLASSES_SCHEMA, None),
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_OPTIMISTIC, default=DEFAULT_OPTIMISTIC): cv.boolean,
        vol.Optional(CONF_PAYLOAD_CLOSE): vol.Any(cv.string, None),
        vol.Optional(CONF_PAYLOAD_OPEN): vol.Any(cv.string, None),
        vol.Optional(CONF_PAYLOAD_STOP): vol.Any(cv.string, None),
        vol.Optional(CONF_POSITION_CLOSED, default=DEFAULT_POSITION_CLOSED): int,
        vol.Optional(CONF_POSITION_OPEN, default=DEFAULT_POSITION_OPEN): int,
        vol.Optional(CONF_REPORTS_POSITION, default=False): cv.boolean,
        vol.Optional(CONF_RETAIN, default=DEFAULT_RETAIN): cv.boolean,
        vol.Optional(CONF_STATE_CLOSED): cv.string,
        vol.Optional(CONF_STATE_CLOSING, default=ValveState.CLOSING): cv.string,
        vol.Optional(CONF_STATE_OPEN): cv.string,
        vol.Optional(CONF_STATE_OPENING, default=ValveState.OPENING): cv.string,
        vol.Optional(CONF_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_VALUE_TEMPLATE): cv.template,
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = vol.All(
    _PLATFORM_SCHEMA_BASE.extend({}, extra=vol.REMOVE_EXTRA),
    _validate_and_add_defaults,
)
