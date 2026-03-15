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
from homeassistant.helpers.typing import ConfigType

from .config import MQTT_RW_SCHEMA
from .const import (
    CONF_COMMAND_TEMPLATE,
    CONF_DIRECTION_COMMAND_TEMPLATE,
    CONF_DIRECTION_COMMAND_TOPIC,
    CONF_DIRECTION_STATE_TOPIC,
    CONF_DIRECTION_VALUE_TEMPLATE,
    CONF_OSCILLATION_COMMAND_TEMPLATE,
    CONF_OSCILLATION_COMMAND_TOPIC,
    CONF_OSCILLATION_STATE_TOPIC,
    CONF_OSCILLATION_VALUE_TEMPLATE,
    CONF_PAYLOAD_OSCILLATION_OFF,
    CONF_PAYLOAD_OSCILLATION_ON,
    CONF_PAYLOAD_RESET_PERCENTAGE,
    CONF_PAYLOAD_RESET_PRESET_MODE,
    CONF_PERCENTAGE_COMMAND_TEMPLATE,
    CONF_PERCENTAGE_COMMAND_TOPIC,
    CONF_PERCENTAGE_STATE_TOPIC,
    CONF_PERCENTAGE_VALUE_TEMPLATE,
    CONF_PRESET_MODE_COMMAND_TEMPLATE,
    CONF_PRESET_MODE_COMMAND_TOPIC,
    CONF_PRESET_MODE_STATE_TOPIC,
    CONF_PRESET_MODE_VALUE_TEMPLATE,
    CONF_PRESET_MODES_LIST,
    CONF_SPEED_RANGE_MAX,
    CONF_SPEED_RANGE_MIN,
    CONF_STATE_VALUE_TEMPLATE,
    DEFAULT_PAYLOAD_OFF,
    DEFAULT_PAYLOAD_ON,
    DEFAULT_PAYLOAD_OSCILLATE_OFF,
    DEFAULT_PAYLOAD_OSCILLATE_ON,
    DEFAULT_PAYLOAD_RESET,
    DEFAULT_SPEED_RANGE_MAX,
    DEFAULT_SPEED_RANGE_MIN,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_publish_topic, valid_subscribe_topic


def valid_speed_range_configuration(config: ConfigType) -> ConfigType:
    """Validate that the fan speed_range configuration is valid, throws if it isn't."""
    if config[CONF_SPEED_RANGE_MIN] == 0:
        raise vol.Invalid("speed_range_min must be > 0")
    if config[CONF_SPEED_RANGE_MIN] >= config[CONF_SPEED_RANGE_MAX]:
        raise vol.Invalid("speed_range_max must be > speed_range_min")
    return config


def valid_preset_mode_configuration(config: ConfigType) -> ConfigType:
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
            CONF_PAYLOAD_OSCILLATION_OFF, default=DEFAULT_PAYLOAD_OSCILLATE_OFF
        ): cv.string,
        vol.Optional(
            CONF_PAYLOAD_OSCILLATION_ON, default=DEFAULT_PAYLOAD_OSCILLATE_ON
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
