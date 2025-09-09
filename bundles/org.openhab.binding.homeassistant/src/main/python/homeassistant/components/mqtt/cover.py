"""Support for MQTT cover devices."""

from __future__ import annotations

from typing import Any

import voluptuous as vol

from homeassistant.components.cover import (
    DEVICE_CLASSES_SCHEMA,
)
from homeassistant.const import (
    CONF_DEVICE_CLASS,
    CONF_NAME,
    CONF_OPTIMISTIC,
    CONF_VALUE_TEMPLATE,
    STATE_CLOSED,
    STATE_CLOSING,
    STATE_OPEN,
    STATE_OPENING,
)
from homeassistant.helpers import config_validation as cv

from .config import MQTT_BASE_SCHEMA
from .const import (
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

CONF_GET_POSITION_TOPIC = "position_topic"
CONF_GET_POSITION_TEMPLATE = "position_template"
CONF_SET_POSITION_TOPIC = "set_position_topic"
CONF_SET_POSITION_TEMPLATE = "set_position_template"
CONF_TILT_COMMAND_TOPIC = "tilt_command_topic"
CONF_TILT_COMMAND_TEMPLATE = "tilt_command_template"
CONF_TILT_STATUS_TOPIC = "tilt_status_topic"
CONF_TILT_STATUS_TEMPLATE = "tilt_status_template"

CONF_STATE_STOPPED = "state_stopped"
CONF_PAYLOAD_STOP_TILT = "payload_stop_tilt"
CONF_TILT_CLOSED_POSITION = "tilt_closed_value"
CONF_TILT_MAX = "tilt_max"
CONF_TILT_MIN = "tilt_min"
CONF_TILT_OPEN_POSITION = "tilt_opened_value"
CONF_TILT_STATE_OPTIMISTIC = "tilt_optimistic"

TILT_PAYLOAD = "tilt"
COVER_PAYLOAD = "cover"

DEFAULT_NAME = "MQTT Cover"

DEFAULT_STATE_STOPPED = "stopped"
DEFAULT_PAYLOAD_STOP = "STOP"

DEFAULT_TILT_CLOSED_POSITION = 0
DEFAULT_TILT_MAX = 100
DEFAULT_TILT_MIN = 0
DEFAULT_TILT_OPEN_POSITION = 100
DEFAULT_TILT_OPTIMISTIC = False


def validate_options(config: dict[str, Any]) -> dict[str, Any]:
    """Validate options.

    If set position topic is set then get position topic is set as well.
    """
    if CONF_SET_POSITION_TOPIC in config and CONF_GET_POSITION_TOPIC not in config:
        raise vol.Invalid(
            f"'{CONF_SET_POSITION_TOPIC}' must be set together with"
            f" '{CONF_GET_POSITION_TOPIC}'."
        )

    # if templates are set make sure the topic for the template is also set

    if CONF_VALUE_TEMPLATE in config and CONF_STATE_TOPIC not in config:
        raise vol.Invalid(
            f"'{CONF_VALUE_TEMPLATE}' must be set together with '{CONF_STATE_TOPIC}'."
        )

    if CONF_GET_POSITION_TEMPLATE in config and CONF_GET_POSITION_TOPIC not in config:
        raise vol.Invalid(
            f"'{CONF_GET_POSITION_TEMPLATE}' must be set together with"
            f" '{CONF_GET_POSITION_TOPIC}'."
        )

    if CONF_SET_POSITION_TEMPLATE in config and CONF_SET_POSITION_TOPIC not in config:
        raise vol.Invalid(
            f"'{CONF_SET_POSITION_TEMPLATE}' must be set together with"
            f" '{CONF_SET_POSITION_TOPIC}'."
        )

    if CONF_TILT_COMMAND_TEMPLATE in config and CONF_TILT_COMMAND_TOPIC not in config:
        raise vol.Invalid(
            f"'{CONF_TILT_COMMAND_TEMPLATE}' must be set together with"
            f" '{CONF_TILT_COMMAND_TOPIC}'."
        )

    if CONF_TILT_STATUS_TEMPLATE in config and CONF_TILT_STATUS_TOPIC not in config:
        raise vol.Invalid(
            f"'{CONF_TILT_STATUS_TEMPLATE}' must be set together with"
            f" '{CONF_TILT_STATUS_TOPIC}'."
        )

    return config


_PLATFORM_SCHEMA_BASE = MQTT_BASE_SCHEMA.extend(
    {
        vol.Optional(CONF_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_DEVICE_CLASS): vol.Any(DEVICE_CLASSES_SCHEMA, None),
        vol.Optional(CONF_GET_POSITION_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_OPTIMISTIC, default=DEFAULT_OPTIMISTIC): cv.boolean,
        vol.Optional(CONF_PAYLOAD_CLOSE, default=DEFAULT_PAYLOAD_CLOSE): vol.Any(
            cv.string, None
        ),
        vol.Optional(CONF_PAYLOAD_OPEN, default=DEFAULT_PAYLOAD_OPEN): vol.Any(
            cv.string, None
        ),
        vol.Optional(CONF_PAYLOAD_STOP, default=DEFAULT_PAYLOAD_STOP): vol.Any(
            cv.string, None
        ),
        vol.Optional(CONF_POSITION_CLOSED, default=DEFAULT_POSITION_CLOSED): int,
        vol.Optional(CONF_POSITION_OPEN, default=DEFAULT_POSITION_OPEN): int,
        vol.Optional(CONF_RETAIN, default=DEFAULT_RETAIN): cv.boolean,
        vol.Optional(CONF_SET_POSITION_TEMPLATE): cv.template,
        vol.Optional(CONF_SET_POSITION_TOPIC): valid_publish_topic,
        vol.Optional(CONF_STATE_CLOSED, default=STATE_CLOSED): cv.string,
        vol.Optional(CONF_STATE_CLOSING, default=STATE_CLOSING): cv.string,
        vol.Optional(CONF_STATE_OPEN, default=STATE_OPEN): cv.string,
        vol.Optional(CONF_STATE_OPENING, default=STATE_OPENING): cv.string,
        vol.Optional(CONF_STATE_STOPPED, default=DEFAULT_STATE_STOPPED): cv.string,
        vol.Optional(CONF_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(
            CONF_TILT_CLOSED_POSITION, default=DEFAULT_TILT_CLOSED_POSITION
        ): int,
        vol.Optional(CONF_TILT_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_TILT_MAX, default=DEFAULT_TILT_MAX): int,
        vol.Optional(CONF_TILT_MIN, default=DEFAULT_TILT_MIN): int,
        vol.Optional(CONF_TILT_OPEN_POSITION, default=DEFAULT_TILT_OPEN_POSITION): int,
        vol.Optional(
            CONF_TILT_STATE_OPTIMISTIC, default=DEFAULT_TILT_OPTIMISTIC
        ): cv.boolean,
        vol.Optional(CONF_TILT_STATUS_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_TILT_STATUS_TEMPLATE): cv.template,
        vol.Optional(CONF_VALUE_TEMPLATE): cv.template,
        vol.Optional(CONF_GET_POSITION_TEMPLATE): cv.template,
        vol.Optional(CONF_TILT_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_PAYLOAD_STOP_TILT, default=DEFAULT_PAYLOAD_STOP): vol.Any(
            cv.string, None
        ),
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = vol.All(
    _PLATFORM_SCHEMA_BASE.extend({}, extra=vol.REMOVE_EXTRA),
    validate_options,
)
