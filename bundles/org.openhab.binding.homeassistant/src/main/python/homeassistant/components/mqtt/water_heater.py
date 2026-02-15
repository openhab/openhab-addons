"""Support for MQTT water heater devices."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.components.water_heater import (
    STATE_ECO,
    STATE_ELECTRIC,
    STATE_GAS,
    STATE_HEAT_PUMP,
    STATE_HIGH_DEMAND,
    STATE_PERFORMANCE,
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
    STATE_OFF,
)
from homeassistant.helpers import config_validation as cv

from .config import DEFAULT_RETAIN, MQTT_BASE_SCHEMA
from .const import (
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

_PLATFORM_SCHEMA_BASE = MQTT_BASE_SCHEMA.extend(
    {
        vol.Optional(CONF_CURRENT_TEMP_TEMPLATE): cv.template,
        vol.Optional(CONF_CURRENT_TEMP_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_MODE_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_MODE_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(
            CONF_MODE_LIST,
            default=[
                STATE_ECO,
                STATE_ELECTRIC,
                STATE_GAS,
                STATE_HEAT_PUMP,
                STATE_HIGH_DEMAND,
                STATE_PERFORMANCE,
                STATE_OFF,
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
        vol.Optional(CONF_TEMP_INITIAL): cv.positive_int,
        vol.Optional(CONF_TEMP_MIN): vol.Coerce(float),
        vol.Optional(CONF_TEMP_MAX): vol.Coerce(float),
        vol.Optional(CONF_TEMP_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_TEMP_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_TEMP_STATE_TEMPLATE): cv.template,
        vol.Optional(CONF_TEMP_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_TEMPERATURE_UNIT): cv.temperature_unit,
        vol.Optional(CONF_VALUE_TEMPLATE): cv.template,
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

_DISCOVERY_SCHEMA_BASE = _PLATFORM_SCHEMA_BASE.extend({}, extra=vol.REMOVE_EXTRA)

DISCOVERY_SCHEMA = vol.All(
    _DISCOVERY_SCHEMA_BASE,
)
