"""Support for MQTT fans."""

from __future__ import annotations

from typing import Any

import voluptuous as vol

from homeassistant.const import (
    CONF_NAME,
    CONF_PAYLOAD_OFF,
    CONF_PAYLOAD_ON,
)
from homeassistant.helpers import config_validation as cv

from .config import MQTT_RW_SCHEMA
from .const import (
    CONF_COMMAND_TEMPLATE,
    CONF_STATE_VALUE_TEMPLATE,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_publish_topic, valid_subscribe_topic

CONF_DIRECTION_STATE_TOPIC = "direction_state_topic"
CONF_DIRECTION_COMMAND_TOPIC = "direction_command_topic"
CONF_DIRECTION_VALUE_TEMPLATE = "direction_value_template"
CONF_DIRECTION_COMMAND_TEMPLATE = "direction_command_template"
CONF_PERCENTAGE_STATE_TOPIC = "percentage_state_topic"
CONF_PERCENTAGE_COMMAND_TOPIC = "percentage_command_topic"
CONF_PERCENTAGE_VALUE_TEMPLATE = "percentage_value_template"
CONF_PERCENTAGE_COMMAND_TEMPLATE = "percentage_command_template"
CONF_PAYLOAD_RESET_PERCENTAGE = "payload_reset_percentage"
CONF_SPEED_RANGE_MIN = "speed_range_min"
CONF_SPEED_RANGE_MAX = "speed_range_max"
CONF_PRESET_MODE_STATE_TOPIC = "preset_mode_state_topic"
CONF_PRESET_MODE_COMMAND_TOPIC = "preset_mode_command_topic"
CONF_PRESET_MODE_VALUE_TEMPLATE = "preset_mode_value_template"
CONF_PRESET_MODE_COMMAND_TEMPLATE = "preset_mode_command_template"
CONF_PRESET_MODES_LIST = "preset_modes"
CONF_PAYLOAD_RESET_PRESET_MODE = "payload_reset_preset_mode"
CONF_OSCILLATION_STATE_TOPIC = "oscillation_state_topic"
CONF_OSCILLATION_COMMAND_TOPIC = "oscillation_command_topic"
CONF_OSCILLATION_VALUE_TEMPLATE = "oscillation_value_template"
CONF_OSCILLATION_COMMAND_TEMPLATE = "oscillation_command_template"
CONF_PAYLOAD_OSCILLATION_ON = "payload_oscillation_on"
CONF_PAYLOAD_OSCILLATION_OFF = "payload_oscillation_off"

DEFAULT_PAYLOAD_ON = "ON"
DEFAULT_PAYLOAD_OFF = "OFF"
DEFAULT_PAYLOAD_RESET = "None"
DEFAULT_SPEED_RANGE_MIN = 1
DEFAULT_SPEED_RANGE_MAX = 100

OSCILLATE_ON_PAYLOAD = "oscillate_on"
OSCILLATE_OFF_PAYLOAD = "oscillate_off"


def valid_speed_range_configuration(config: dict[str, Any]) -> dict[str, Any]:
    """Validate that the fan speed_range configuration is valid, throws if it isn't."""
    if config[CONF_SPEED_RANGE_MIN] == 0:
        raise vol.Invalid("speed_range_min must be > 0")
    if config[CONF_SPEED_RANGE_MIN] >= config[CONF_SPEED_RANGE_MAX]:
        raise vol.Invalid("speed_range_max must be > speed_range_min")
    return config


def valid_preset_mode_configuration(config: dict[str, Any]) -> dict[str, Any]:
    """Validate that the preset mode reset payload is not one of the preset modes."""
    if config[CONF_PAYLOAD_RESET_PRESET_MODE] in config[CONF_PRESET_MODES_LIST]:
        raise vol.Invalid("preset_modes must not contain payload_reset_preset_mode")
    return config


_PLATFORM_SCHEMA_BASE = MQTT_RW_SCHEMA.extend(
    {
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_DIRECTION_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_DIRECTION_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_DIRECTION_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_DIRECTION_VALUE_TEMPLATE): cv.template,
        vol.Optional(CONF_OSCILLATION_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_OSCILLATION_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_OSCILLATION_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_OSCILLATION_VALUE_TEMPLATE): cv.template,
        vol.Optional(CONF_PERCENTAGE_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_PERCENTAGE_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_PERCENTAGE_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_PERCENTAGE_VALUE_TEMPLATE): cv.template,
        # CONF_PRESET_MODE_COMMAND_TOPIC and CONF_PRESET_MODES_LIST
        # must be used together
        vol.Inclusive(
            CONF_PRESET_MODE_COMMAND_TOPIC, "preset_modes"
        ): valid_publish_topic,
        vol.Inclusive(
            CONF_PRESET_MODES_LIST, "preset_modes", default=[]
        ): cv.ensure_list,
        vol.Optional(CONF_PRESET_MODE_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_PRESET_MODE_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_PRESET_MODE_VALUE_TEMPLATE): cv.template,
        vol.Optional(
            CONF_SPEED_RANGE_MIN, default=DEFAULT_SPEED_RANGE_MIN
        ): cv.positive_int,
        vol.Optional(
            CONF_SPEED_RANGE_MAX, default=DEFAULT_SPEED_RANGE_MAX
        ): cv.positive_int,
        vol.Optional(
            CONF_PAYLOAD_RESET_PERCENTAGE, default=DEFAULT_PAYLOAD_RESET
        ): cv.string,
        vol.Optional(
            CONF_PAYLOAD_RESET_PRESET_MODE, default=DEFAULT_PAYLOAD_RESET
        ): cv.string,
        vol.Optional(CONF_PAYLOAD_OFF, default=DEFAULT_PAYLOAD_OFF): cv.string,
        vol.Optional(CONF_PAYLOAD_ON, default=DEFAULT_PAYLOAD_ON): cv.string,
        vol.Optional(
            CONF_PAYLOAD_OSCILLATION_OFF, default=OSCILLATE_OFF_PAYLOAD
        ): cv.string,
        vol.Optional(
            CONF_PAYLOAD_OSCILLATION_ON, default=OSCILLATE_ON_PAYLOAD
        ): cv.string,
        vol.Optional(CONF_STATE_VALUE_TEMPLATE): cv.template,
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

PLATFORM_SCHEMA_MODERN = vol.All(
    _PLATFORM_SCHEMA_BASE,
    valid_speed_range_configuration,
    valid_preset_mode_configuration,
)

DISCOVERY_SCHEMA = vol.All(
    _PLATFORM_SCHEMA_BASE.extend({}, extra=vol.REMOVE_EXTRA),
    valid_speed_range_configuration,
    valid_preset_mode_configuration,
)
