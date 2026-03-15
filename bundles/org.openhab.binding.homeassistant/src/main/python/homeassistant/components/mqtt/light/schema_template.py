"""Support for MQTT Template lights."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.const import (
    CONF_NAME,
    CONF_STATE_TEMPLATE,
)
from homeassistant.helpers import config_validation as cv

from ..config import MQTT_RW_SCHEMA
from ..const import (
    CONF_BLUE_TEMPLATE,
    CONF_BRIGHTNESS_TEMPLATE,
    CONF_COLOR_TEMP_KELVIN,
    CONF_COLOR_TEMP_TEMPLATE,
    CONF_COMMAND_OFF_TEMPLATE,
    CONF_COMMAND_ON_TEMPLATE,
    CONF_EFFECT_LIST,
    CONF_EFFECT_TEMPLATE,
    CONF_GREEN_TEMPLATE,
    CONF_MAX_KELVIN,
    CONF_MAX_MIREDS,
    CONF_MIN_KELVIN,
    CONF_MIN_MIREDS,
    CONF_RED_TEMPLATE,
)
from ..schemas import MQTT_ENTITY_COMMON_SCHEMA
from .schema import MQTT_LIGHT_SCHEMA_SCHEMA


PLATFORM_SCHEMA_MODERN_TEMPLATE = (
    MQTT_RW_SCHEMA.extend(
        {
            vol.Optional(CONF_BLUE_TEMPLATE): cv.template,
            vol.Optional(CONF_BRIGHTNESS_TEMPLATE): cv.template,
            vol.Optional(CONF_COLOR_TEMP_KELVIN, default=False): cv.boolean,
            vol.Optional(CONF_COLOR_TEMP_TEMPLATE): cv.template,
            vol.Required(CONF_COMMAND_OFF_TEMPLATE): cv.template,
            vol.Required(CONF_COMMAND_ON_TEMPLATE): cv.template,
            vol.Optional(CONF_EFFECT_LIST): vol.All(cv.ensure_list, [cv.string]),
            vol.Optional(CONF_EFFECT_TEMPLATE): cv.template,
            vol.Optional(CONF_GREEN_TEMPLATE): cv.template,
            vol.Optional(CONF_MAX_KELVIN): cv.positive_int,
            vol.Optional(CONF_MIN_KELVIN): cv.positive_int,
            vol.Optional(CONF_MAX_MIREDS): cv.positive_int,
            vol.Optional(CONF_MIN_MIREDS): cv.positive_int,
            vol.Optional(CONF_NAME): vol.Any(cv.string, None),
            vol.Optional(CONF_RED_TEMPLATE): cv.template,
            vol.Optional(CONF_STATE_TEMPLATE): cv.template,
        }
    )
    .extend(MQTT_ENTITY_COMMON_SCHEMA.schema)
    .extend(MQTT_LIGHT_SCHEMA_SCHEMA.schema)
)

DISCOVERY_SCHEMA_TEMPLATE = vol.All(
    PLATFORM_SCHEMA_MODERN_TEMPLATE.extend({}, extra=vol.REMOVE_EXTRA),
)
