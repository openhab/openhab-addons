"""Support for MQTT JSON lights."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.components.light import (
    VALID_COLOR_MODES,
    valid_supported_color_modes,
)
from homeassistant.const import (
    CONF_BRIGHTNESS,
    CONF_COLOR_TEMP,
    CONF_EFFECT,
    CONF_HS,
    CONF_NAME,
    CONF_RGB,
    CONF_XY,
)
from homeassistant.helpers import config_validation as cv

from ..config import DEFAULT_QOS, DEFAULT_RETAIN, MQTT_RW_SCHEMA
from ..const import (
    CONF_COLOR_MODE,
    CONF_COLOR_TEMP_KELVIN,
    CONF_EFFECT_LIST,
    CONF_FLASH,
    CONF_FLASH_TIME_LONG,
    CONF_FLASH_TIME_SHORT,
    CONF_MAX_KELVIN,
    CONF_MAX_MIREDS,
    CONF_MIN_KELVIN,
    CONF_MIN_MIREDS,
    CONF_QOS,
    CONF_RETAIN,
    CONF_STATE_TOPIC,
    CONF_SUPPORTED_COLOR_MODES,
    CONF_TRANSITION,
    DEFAULT_BRIGHTNESS,
    DEFAULT_BRIGHTNESS_SCALE,
    DEFAULT_EFFECT,
    DEFAULT_FLASH_TIME_LONG,
    DEFAULT_FLASH_TIME_SHORT,
    DEFAULT_WHITE_SCALE,
)
from ..schemas import MQTT_ENTITY_COMMON_SCHEMA
from ..util import valid_subscribe_topic
from .schema import MQTT_LIGHT_SCHEMA_SCHEMA
from .schema_basic import (
    CONF_BRIGHTNESS_SCALE,
    CONF_WHITE_SCALE,
)

DEFAULT_FLASH = True
DEFAULT_TRANSITION = True

_PLATFORM_SCHEMA_BASE = (
    MQTT_RW_SCHEMA.extend(
        {
            vol.Optional(CONF_BRIGHTNESS, default=DEFAULT_BRIGHTNESS): cv.boolean,
            vol.Optional(
                CONF_BRIGHTNESS_SCALE, default=DEFAULT_BRIGHTNESS_SCALE
            ): vol.All(vol.Coerce(int), vol.Range(min=1)),
            vol.Optional(CONF_COLOR_TEMP_KELVIN, default=False): cv.boolean,
            vol.Optional(CONF_EFFECT, default=DEFAULT_EFFECT): cv.boolean,
            vol.Optional(CONF_EFFECT_LIST): vol.All(cv.ensure_list, [cv.string]),
            vol.Optional(CONF_FLASH, default=DEFAULT_FLASH): cv.boolean,
            vol.Optional(
                CONF_FLASH_TIME_LONG, default=DEFAULT_FLASH_TIME_LONG
            ): cv.positive_int,
            vol.Optional(
                CONF_FLASH_TIME_SHORT, default=DEFAULT_FLASH_TIME_SHORT
            ): cv.positive_int,
            vol.Optional(CONF_MAX_MIREDS): cv.positive_int,
            vol.Optional(CONF_MIN_MIREDS): cv.positive_int,
            vol.Optional(CONF_MAX_KELVIN): cv.positive_int,
            vol.Optional(CONF_MIN_KELVIN): cv.positive_int,
            vol.Optional(CONF_NAME): vol.Any(cv.string, None),
            vol.Optional(CONF_QOS, default=DEFAULT_QOS): vol.All(
                vol.Coerce(int), vol.In([0, 1, 2])
            ),
            vol.Optional(CONF_RETAIN, default=DEFAULT_RETAIN): cv.boolean,
            vol.Optional(CONF_STATE_TOPIC): valid_subscribe_topic,
            vol.Optional(CONF_SUPPORTED_COLOR_MODES): vol.All(
                cv.ensure_list,
                [vol.In(VALID_COLOR_MODES)],
                vol.Unique(),
                valid_supported_color_modes,
            ),
            vol.Optional(CONF_TRANSITION, default=DEFAULT_TRANSITION): cv.boolean,
            vol.Optional(CONF_WHITE_SCALE, default=DEFAULT_WHITE_SCALE): vol.All(
                vol.Coerce(int), vol.Range(min=1)
            ),
        },
    )
    .extend(MQTT_ENTITY_COMMON_SCHEMA.schema)
    .extend(MQTT_LIGHT_SCHEMA_SCHEMA.schema)
)

# Support for legacy color_mode handling was removed with HA Core 2025.3
# The removed attributes can be removed from the schema's from HA Core 2026.3
DISCOVERY_SCHEMA_JSON = vol.All(
    cv.removed(CONF_COLOR_MODE, raise_if_present=False),
    cv.removed(CONF_COLOR_TEMP, raise_if_present=False),
    cv.removed(CONF_HS, raise_if_present=False),
    cv.removed(CONF_RGB, raise_if_present=False),
    cv.removed(CONF_XY, raise_if_present=False),
    _PLATFORM_SCHEMA_BASE.extend({}, extra=vol.REMOVE_EXTRA),
)
