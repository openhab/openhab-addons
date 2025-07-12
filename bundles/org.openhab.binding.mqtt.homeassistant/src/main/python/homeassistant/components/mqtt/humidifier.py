"""Support for MQTT humidifiers."""

from __future__ import annotations

from typing import Any

import voluptuous as vol

from homeassistant.components.humidifier import (
    DEFAULT_MAX_HUMIDITY,
    DEFAULT_MIN_HUMIDITY,
    HumidifierDeviceClass,
)
from homeassistant.const import (
    CONF_NAME,
    CONF_PAYLOAD_OFF,
    CONF_PAYLOAD_ON,
)
from homeassistant.helpers import config_validation as cv

from .config import MQTT_RW_SCHEMA
from .const import (
    CONF_ACTION_TEMPLATE,
    CONF_ACTION_TOPIC,
    CONF_COMMAND_TEMPLATE,
    CONF_CURRENT_HUMIDITY_TEMPLATE,
    CONF_CURRENT_HUMIDITY_TOPIC,
    CONF_STATE_VALUE_TEMPLATE,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_publish_topic, valid_subscribe_topic

CONF_AVAILABLE_MODES_LIST = "modes"
CONF_DEVICE_CLASS = "device_class"
CONF_MODE_COMMAND_TEMPLATE = "mode_command_template"
CONF_MODE_COMMAND_TOPIC = "mode_command_topic"
CONF_MODE_STATE_TOPIC = "mode_state_topic"
CONF_MODE_STATE_TEMPLATE = "mode_state_template"
CONF_PAYLOAD_RESET_MODE = "payload_reset_mode"
CONF_PAYLOAD_RESET_HUMIDITY = "payload_reset_humidity"
CONF_TARGET_HUMIDITY_COMMAND_TEMPLATE = "target_humidity_command_template"
CONF_TARGET_HUMIDITY_COMMAND_TOPIC = "target_humidity_command_topic"
CONF_TARGET_HUMIDITY_MIN = "min_humidity"
CONF_TARGET_HUMIDITY_MAX = "max_humidity"
CONF_TARGET_HUMIDITY_STATE_TEMPLATE = "target_humidity_state_template"
CONF_TARGET_HUMIDITY_STATE_TOPIC = "target_humidity_state_topic"

DEFAULT_NAME = "MQTT Humidifier"
DEFAULT_PAYLOAD_ON = "ON"
DEFAULT_PAYLOAD_OFF = "OFF"
DEFAULT_PAYLOAD_RESET = "None"


def valid_mode_configuration(config: dict[str, Any]) -> dict[str, Any]:
    """Validate that the mode reset payload is not one of the available modes."""
    if config[CONF_PAYLOAD_RESET_MODE] in config[CONF_AVAILABLE_MODES_LIST]:
        raise vol.Invalid("modes must not contain payload_reset_mode")
    return config


def valid_humidity_range_configuration(config: dict[str, Any]) -> dict[str, Any]:
    """Validate humidity range.

    Ensures that the target_humidity range configuration is valid,
    throws if it isn't.
    """
    if config[CONF_TARGET_HUMIDITY_MIN] >= config[CONF_TARGET_HUMIDITY_MAX]:
        raise vol.Invalid("target_humidity_max must be > target_humidity_min")
    if config[CONF_TARGET_HUMIDITY_MAX] > 100:
        raise vol.Invalid("max_humidity must be <= 100")

    return config


_PLATFORM_SCHEMA_BASE = MQTT_RW_SCHEMA.extend(
    {
        vol.Optional(CONF_ACTION_TEMPLATE): cv.template,
        vol.Optional(CONF_ACTION_TOPIC): valid_subscribe_topic,
        # CONF_AVAIALABLE_MODES_LIST and CONF_MODE_COMMAND_TOPIC must be used together
        vol.Inclusive(
            CONF_AVAILABLE_MODES_LIST, "available_modes", default=[]
        ): cv.ensure_list,
        vol.Inclusive(CONF_MODE_COMMAND_TOPIC, "available_modes"): valid_publish_topic,
        vol.Optional(CONF_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_CURRENT_HUMIDITY_TEMPLATE): cv.template,
        vol.Optional(CONF_CURRENT_HUMIDITY_TOPIC): valid_subscribe_topic,
        vol.Optional(
            CONF_DEVICE_CLASS, default=HumidifierDeviceClass.HUMIDIFIER
        ): vol.In(
            [HumidifierDeviceClass.HUMIDIFIER, HumidifierDeviceClass.DEHUMIDIFIER, None]
        ),
        vol.Optional(CONF_MODE_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_MODE_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_MODE_STATE_TEMPLATE): cv.template,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_PAYLOAD_OFF, default=DEFAULT_PAYLOAD_OFF): cv.string,
        vol.Optional(CONF_PAYLOAD_ON, default=DEFAULT_PAYLOAD_ON): cv.string,
        vol.Optional(CONF_STATE_VALUE_TEMPLATE): cv.template,
        vol.Required(CONF_TARGET_HUMIDITY_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_TARGET_HUMIDITY_COMMAND_TEMPLATE): cv.template,
        vol.Optional(
            CONF_TARGET_HUMIDITY_MAX, default=DEFAULT_MAX_HUMIDITY
        ): cv.positive_float,
        vol.Optional(
            CONF_TARGET_HUMIDITY_MIN, default=DEFAULT_MIN_HUMIDITY
        ): cv.positive_float,
        vol.Optional(CONF_TARGET_HUMIDITY_STATE_TEMPLATE): cv.template,
        vol.Optional(CONF_TARGET_HUMIDITY_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(
            CONF_PAYLOAD_RESET_HUMIDITY, default=DEFAULT_PAYLOAD_RESET
        ): cv.string,
        vol.Optional(CONF_PAYLOAD_RESET_MODE, default=DEFAULT_PAYLOAD_RESET): cv.string,
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

PLATFORM_SCHEMA_MODERN = vol.All(
    _PLATFORM_SCHEMA_BASE,
    valid_humidity_range_configuration,
    valid_mode_configuration,
)

DISCOVERY_SCHEMA = vol.All(
    _PLATFORM_SCHEMA_BASE.extend({}, extra=vol.REMOVE_EXTRA),
    valid_humidity_range_configuration,
    valid_mode_configuration,
)
