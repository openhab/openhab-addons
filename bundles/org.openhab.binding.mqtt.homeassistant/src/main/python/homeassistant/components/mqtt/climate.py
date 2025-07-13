"""Support for MQTT climate devices."""

from __future__ import annotations

from typing import Any

import voluptuous as vol

from homeassistant.components import climate
from homeassistant.components.climate import (
    DEFAULT_MAX_HUMIDITY,
    DEFAULT_MIN_HUMIDITY,
    FAN_AUTO,
    FAN_HIGH,
    FAN_LOW,
    FAN_MEDIUM,
    PRESET_NONE,
    SWING_OFF,
    SWING_ON,
    HVACMode,
)
from homeassistant.const import (
    CONF_NAME,
    CONF_OPTIMISTIC,
    CONF_PAYLOAD_OFF,
    CONF_PAYLOAD_ON,
    CONF_TEMPERATURE_UNIT,
    CONF_VALUE_TEMPLATE,
    PRECISION_HALVES,
    PRECISION_TENTHS,
    PRECISION_WHOLE,
)
from homeassistant.helpers import config_validation as cv

from .config import DEFAULT_RETAIN, MQTT_BASE_SCHEMA
from .const import (
    CONF_ACTION_TEMPLATE,
    CONF_ACTION_TOPIC,
    CONF_CURRENT_HUMIDITY_TEMPLATE,
    CONF_CURRENT_HUMIDITY_TOPIC,
    CONF_CURRENT_TEMP_TEMPLATE,
    CONF_CURRENT_TEMP_TOPIC,
    CONF_MODE_COMMAND_TEMPLATE,
    CONF_MODE_COMMAND_TOPIC,
    CONF_MODE_LIST,
    CONF_MODE_STATE_TEMPLATE,
    CONF_MODE_STATE_TOPIC,
    CONF_POWER_COMMAND_TEMPLATE,
    CONF_POWER_COMMAND_TOPIC,
    CONF_PRECISION,
    CONF_RETAIN,
    CONF_TEMP_COMMAND_TEMPLATE,
    CONF_TEMP_COMMAND_TOPIC,
    CONF_TEMP_INITIAL,
    CONF_TEMP_MAX,
    CONF_TEMP_MIN,
    CONF_TEMP_STATE_TEMPLATE,
    CONF_TEMP_STATE_TOPIC,
    DEFAULT_OPTIMISTIC,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_publish_topic, valid_subscribe_topic

CONF_FAN_MODE_COMMAND_TEMPLATE = "fan_mode_command_template"
CONF_FAN_MODE_COMMAND_TOPIC = "fan_mode_command_topic"
CONF_FAN_MODE_LIST = "fan_modes"
CONF_FAN_MODE_STATE_TEMPLATE = "fan_mode_state_template"
CONF_FAN_MODE_STATE_TOPIC = "fan_mode_state_topic"

CONF_HUMIDITY_COMMAND_TEMPLATE = "target_humidity_command_template"
CONF_HUMIDITY_COMMAND_TOPIC = "target_humidity_command_topic"
CONF_HUMIDITY_STATE_TEMPLATE = "target_humidity_state_template"
CONF_HUMIDITY_STATE_TOPIC = "target_humidity_state_topic"
CONF_HUMIDITY_MAX = "max_humidity"
CONF_HUMIDITY_MIN = "min_humidity"

CONF_PRESET_MODE_STATE_TOPIC = "preset_mode_state_topic"
CONF_PRESET_MODE_COMMAND_TOPIC = "preset_mode_command_topic"
CONF_PRESET_MODE_VALUE_TEMPLATE = "preset_mode_value_template"
CONF_PRESET_MODE_COMMAND_TEMPLATE = "preset_mode_command_template"
CONF_PRESET_MODES_LIST = "preset_modes"

CONF_SWING_HORIZONTAL_MODE_COMMAND_TEMPLATE = "swing_horizontal_mode_command_template"
CONF_SWING_HORIZONTAL_MODE_COMMAND_TOPIC = "swing_horizontal_mode_command_topic"
CONF_SWING_HORIZONTAL_MODE_LIST = "swing_horizontal_modes"
CONF_SWING_HORIZONTAL_MODE_STATE_TEMPLATE = "swing_horizontal_mode_state_template"
CONF_SWING_HORIZONTAL_MODE_STATE_TOPIC = "swing_horizontal_mode_state_topic"

CONF_SWING_MODE_COMMAND_TEMPLATE = "swing_mode_command_template"
CONF_SWING_MODE_COMMAND_TOPIC = "swing_mode_command_topic"
CONF_SWING_MODE_LIST = "swing_modes"
CONF_SWING_MODE_STATE_TEMPLATE = "swing_mode_state_template"
CONF_SWING_MODE_STATE_TOPIC = "swing_mode_state_topic"

CONF_TEMP_HIGH_COMMAND_TEMPLATE = "temperature_high_command_template"
CONF_TEMP_HIGH_COMMAND_TOPIC = "temperature_high_command_topic"
CONF_TEMP_HIGH_STATE_TEMPLATE = "temperature_high_state_template"
CONF_TEMP_HIGH_STATE_TOPIC = "temperature_high_state_topic"
CONF_TEMP_LOW_COMMAND_TEMPLATE = "temperature_low_command_template"
CONF_TEMP_LOW_COMMAND_TOPIC = "temperature_low_command_topic"
CONF_TEMP_LOW_STATE_TEMPLATE = "temperature_low_state_template"
CONF_TEMP_LOW_STATE_TOPIC = "temperature_low_state_topic"
CONF_TEMP_STEP = "temp_step"

DEFAULT_INITIAL_TEMPERATURE = 21.0


def valid_preset_mode_configuration(config: dict[str, Any]) -> dict[str, Any]:
    """Validate that the preset mode reset payload is not one of the preset modes."""
    if PRESET_NONE in config[CONF_PRESET_MODES_LIST]:
        raise vol.Invalid("preset_modes must not include preset mode 'none'")
    return config


def valid_humidity_range_configuration(config: dict[str, Any]) -> dict[str, Any]:
    """Validate a target_humidity range configuration, throws otherwise."""
    if config[CONF_HUMIDITY_MIN] >= config[CONF_HUMIDITY_MAX]:
        raise vol.Invalid("target_humidity_max must be > target_humidity_min")
    if config[CONF_HUMIDITY_MAX] > 100:
        raise vol.Invalid("max_humidity must be <= 100")

    return config


def valid_humidity_state_configuration(config: dict[str, Any]) -> dict[str, Any]:
    """Validate humidity state.

    Ensure that if CONF_HUMIDITY_STATE_TOPIC is set then
    CONF_HUMIDITY_COMMAND_TOPIC is also set.
    """
    if (
        CONF_HUMIDITY_STATE_TOPIC in config
        and CONF_HUMIDITY_COMMAND_TOPIC not in config
    ):
        raise vol.Invalid(
            f"{CONF_HUMIDITY_STATE_TOPIC} cannot be used without"
            f" {CONF_HUMIDITY_COMMAND_TOPIC}"
        )

    return config


_PLATFORM_SCHEMA_BASE = MQTT_BASE_SCHEMA.extend(
    {
        vol.Optional(CONF_CURRENT_HUMIDITY_TEMPLATE): cv.template,
        vol.Optional(CONF_CURRENT_HUMIDITY_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_CURRENT_TEMP_TEMPLATE): cv.template,
        vol.Optional(CONF_CURRENT_TEMP_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_FAN_MODE_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_FAN_MODE_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(
            CONF_FAN_MODE_LIST,
            default=[FAN_AUTO, FAN_LOW, FAN_MEDIUM, FAN_HIGH],
        ): cv.ensure_list,
        vol.Optional(CONF_FAN_MODE_STATE_TEMPLATE): cv.template,
        vol.Optional(CONF_FAN_MODE_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_HUMIDITY_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_HUMIDITY_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(
            CONF_HUMIDITY_MIN, default=DEFAULT_MIN_HUMIDITY
        ): cv.positive_float,
        vol.Optional(
            CONF_HUMIDITY_MAX, default=DEFAULT_MAX_HUMIDITY
        ): cv.positive_float,
        vol.Optional(CONF_HUMIDITY_STATE_TEMPLATE): cv.template,
        vol.Optional(CONF_HUMIDITY_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_MODE_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_MODE_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(
            CONF_MODE_LIST,
            default=[
                HVACMode.AUTO,
                HVACMode.OFF,
                HVACMode.COOL,
                HVACMode.HEAT,
                HVACMode.DRY,
                HVACMode.FAN_ONLY,
            ],
        ): cv.ensure_list,
        vol.Optional(CONF_MODE_STATE_TEMPLATE): cv.template,
        vol.Optional(CONF_MODE_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_OPTIMISTIC, default=DEFAULT_OPTIMISTIC): cv.boolean,
        vol.Optional(CONF_PAYLOAD_ON, default="ON"): cv.string,
        vol.Optional(CONF_PAYLOAD_OFF, default="OFF"): cv.string,
        vol.Optional(CONF_POWER_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_POWER_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_PRECISION): vol.In(
            [PRECISION_TENTHS, PRECISION_HALVES, PRECISION_WHOLE]
        ),
        vol.Optional(CONF_RETAIN, default=DEFAULT_RETAIN): cv.boolean,
        vol.Optional(CONF_ACTION_TEMPLATE): cv.template,
        vol.Optional(CONF_ACTION_TOPIC): valid_subscribe_topic,
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
        vol.Optional(CONF_SWING_HORIZONTAL_MODE_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_SWING_HORIZONTAL_MODE_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(
            CONF_SWING_HORIZONTAL_MODE_LIST, default=[SWING_ON, SWING_OFF]
        ): cv.ensure_list,
        vol.Optional(CONF_SWING_HORIZONTAL_MODE_STATE_TEMPLATE): cv.template,
        vol.Optional(CONF_SWING_HORIZONTAL_MODE_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_SWING_MODE_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_SWING_MODE_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(
            CONF_SWING_MODE_LIST, default=[SWING_ON, SWING_OFF]
        ): cv.ensure_list,
        vol.Optional(CONF_SWING_MODE_STATE_TEMPLATE): cv.template,
        vol.Optional(CONF_SWING_MODE_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_TEMP_INITIAL): vol.All(vol.Coerce(float)),
        vol.Optional(CONF_TEMP_MIN): vol.Coerce(float),
        vol.Optional(CONF_TEMP_MAX): vol.Coerce(float),
        vol.Optional(CONF_TEMP_STEP, default=1.0): vol.Coerce(float),
        vol.Optional(CONF_TEMP_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_TEMP_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_TEMP_HIGH_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_TEMP_HIGH_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_TEMP_HIGH_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_TEMP_HIGH_STATE_TEMPLATE): cv.template,
        vol.Optional(CONF_TEMP_LOW_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_TEMP_LOW_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_TEMP_LOW_STATE_TEMPLATE): cv.template,
        vol.Optional(CONF_TEMP_LOW_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_TEMP_STATE_TEMPLATE): cv.template,
        vol.Optional(CONF_TEMP_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_TEMPERATURE_UNIT): cv.temperature_unit,
        vol.Optional(CONF_VALUE_TEMPLATE): cv.template,
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

_DISCOVERY_SCHEMA_BASE = _PLATFORM_SCHEMA_BASE.extend({}, extra=vol.REMOVE_EXTRA)

DISCOVERY_SCHEMA = vol.All(
    _DISCOVERY_SCHEMA_BASE,
    valid_preset_mode_configuration,
    valid_humidity_range_configuration,
    valid_humidity_state_configuration,
)
