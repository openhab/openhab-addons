"""Support for MQTT lights."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.const import (
    CONF_NAME,
    CONF_PAYLOAD_OFF,
    CONF_PAYLOAD_ON,
)
from homeassistant.helpers import config_validation as cv

from ..config import MQTT_RW_SCHEMA
from ..const import (
    CONF_BRIGHTNESS_COMMAND_TEMPLATE,
    CONF_BRIGHTNESS_COMMAND_TOPIC,
    CONF_BRIGHTNESS_SCALE,
    CONF_BRIGHTNESS_STATE_TOPIC,
    CONF_BRIGHTNESS_VALUE_TEMPLATE,
    CONF_COLOR_MODE_STATE_TOPIC,
    CONF_COLOR_MODE_VALUE_TEMPLATE,
    CONF_COLOR_TEMP_COMMAND_TEMPLATE,
    CONF_COLOR_TEMP_COMMAND_TOPIC,
    CONF_COLOR_TEMP_KELVIN,
    CONF_COLOR_TEMP_STATE_TOPIC,
    CONF_COLOR_TEMP_VALUE_TEMPLATE,
    CONF_EFFECT_COMMAND_TEMPLATE,
    CONF_EFFECT_COMMAND_TOPIC,
    CONF_EFFECT_LIST,
    CONF_EFFECT_STATE_TOPIC,
    CONF_EFFECT_VALUE_TEMPLATE,
    CONF_HS_COMMAND_TEMPLATE,
    CONF_HS_COMMAND_TOPIC,
    CONF_HS_STATE_TOPIC,
    CONF_HS_VALUE_TEMPLATE,
    CONF_MAX_KELVIN,
    CONF_MAX_MIREDS,
    CONF_MIN_KELVIN,
    CONF_MIN_MIREDS,
    CONF_ON_COMMAND_TYPE,
    CONF_RGB_COMMAND_TEMPLATE,
    CONF_RGB_COMMAND_TOPIC,
    CONF_RGB_STATE_TOPIC,
    CONF_RGB_VALUE_TEMPLATE,
    CONF_RGBW_COMMAND_TEMPLATE,
    CONF_RGBW_COMMAND_TOPIC,
    CONF_RGBW_STATE_TOPIC,
    CONF_RGBW_VALUE_TEMPLATE,
    CONF_RGBWW_COMMAND_TEMPLATE,
    CONF_RGBWW_COMMAND_TOPIC,
    CONF_RGBWW_STATE_TOPIC,
    CONF_RGBWW_VALUE_TEMPLATE,
    CONF_STATE_VALUE_TEMPLATE,
    CONF_WHITE_COMMAND_TOPIC,
    CONF_WHITE_SCALE,
    CONF_XY_COMMAND_TEMPLATE,
    CONF_XY_COMMAND_TOPIC,
    CONF_XY_STATE_TOPIC,
    CONF_XY_VALUE_TEMPLATE,
    DEFAULT_BRIGHTNESS_SCALE,
    DEFAULT_ON_COMMAND_TYPE,
    DEFAULT_PAYLOAD_OFF,
    DEFAULT_PAYLOAD_ON,
    DEFAULT_WHITE_SCALE,
)
from ..schemas import MQTT_ENTITY_COMMON_SCHEMA
from ..util import valid_publish_topic, valid_subscribe_topic
from .schema import MQTT_LIGHT_SCHEMA_SCHEMA

VALUES_ON_COMMAND_TYPE = ["first", "last", "brightness"]

PLATFORM_SCHEMA_MODERN_BASIC = (
    MQTT_RW_SCHEMA.extend(
        {
            vol.Optional(CONF_BRIGHTNESS_COMMAND_TEMPLATE): cv.template,
            vol.Optional(CONF_BRIGHTNESS_COMMAND_TOPIC): valid_publish_topic,
            vol.Optional(
                CONF_BRIGHTNESS_SCALE, default=DEFAULT_BRIGHTNESS_SCALE
            ): vol.All(vol.Coerce(int), vol.Range(min=1)),
            vol.Optional(CONF_BRIGHTNESS_STATE_TOPIC): valid_subscribe_topic,
            vol.Optional(CONF_BRIGHTNESS_VALUE_TEMPLATE): cv.template,
            vol.Optional(CONF_COLOR_MODE_STATE_TOPIC): valid_subscribe_topic,
            vol.Optional(CONF_COLOR_MODE_VALUE_TEMPLATE): cv.template,
            vol.Optional(CONF_COLOR_TEMP_COMMAND_TEMPLATE): cv.template,
            vol.Optional(CONF_COLOR_TEMP_COMMAND_TOPIC): valid_publish_topic,
            vol.Optional(CONF_COLOR_TEMP_STATE_TOPIC): valid_subscribe_topic,
            vol.Optional(CONF_COLOR_TEMP_VALUE_TEMPLATE): cv.template,
            vol.Optional(CONF_COLOR_TEMP_KELVIN, default=False): cv.boolean,
            vol.Optional(CONF_EFFECT_COMMAND_TEMPLATE): cv.template,
            vol.Optional(CONF_EFFECT_COMMAND_TOPIC): valid_publish_topic,
            vol.Optional(CONF_EFFECT_LIST): vol.All(cv.ensure_list, [cv.string]),
            vol.Optional(CONF_EFFECT_STATE_TOPIC): valid_subscribe_topic,
            vol.Optional(CONF_EFFECT_VALUE_TEMPLATE): cv.template,
            vol.Optional(CONF_HS_COMMAND_TEMPLATE): cv.template,
            vol.Optional(CONF_HS_COMMAND_TOPIC): valid_publish_topic,
            vol.Optional(CONF_HS_STATE_TOPIC): valid_subscribe_topic,
            vol.Optional(CONF_HS_VALUE_TEMPLATE): cv.template,
            vol.Optional(CONF_MAX_MIREDS): cv.positive_int,
            vol.Optional(CONF_MIN_MIREDS): cv.positive_int,
            vol.Optional(CONF_MAX_KELVIN): cv.positive_int,
            vol.Optional(CONF_MIN_KELVIN): cv.positive_int,
            vol.Optional(CONF_NAME): vol.Any(cv.string, None),
            vol.Optional(CONF_ON_COMMAND_TYPE, default=DEFAULT_ON_COMMAND_TYPE): vol.In(
                VALUES_ON_COMMAND_TYPE
            ),
            vol.Optional(CONF_PAYLOAD_OFF, default=DEFAULT_PAYLOAD_OFF): cv.string,
            vol.Optional(CONF_PAYLOAD_ON, default=DEFAULT_PAYLOAD_ON): cv.string,
            vol.Optional(CONF_RGB_COMMAND_TEMPLATE): cv.template,
            vol.Optional(CONF_RGB_COMMAND_TOPIC): valid_publish_topic,
            vol.Optional(CONF_RGB_STATE_TOPIC): valid_subscribe_topic,
            vol.Optional(CONF_RGB_VALUE_TEMPLATE): cv.template,
            vol.Optional(CONF_RGBW_COMMAND_TEMPLATE): cv.template,
            vol.Optional(CONF_RGBW_COMMAND_TOPIC): valid_publish_topic,
            vol.Optional(CONF_RGBW_STATE_TOPIC): valid_subscribe_topic,
            vol.Optional(CONF_RGBW_VALUE_TEMPLATE): cv.template,
            vol.Optional(CONF_RGBWW_COMMAND_TEMPLATE): cv.template,
            vol.Optional(CONF_RGBWW_COMMAND_TOPIC): valid_publish_topic,
            vol.Optional(CONF_RGBWW_STATE_TOPIC): valid_subscribe_topic,
            vol.Optional(CONF_RGBWW_VALUE_TEMPLATE): cv.template,
            vol.Optional(CONF_STATE_VALUE_TEMPLATE): cv.template,
            vol.Optional(CONF_WHITE_COMMAND_TOPIC): valid_publish_topic,
            vol.Optional(CONF_WHITE_SCALE, default=DEFAULT_WHITE_SCALE): vol.All(
                vol.Coerce(int), vol.Range(min=1)
            ),
            vol.Optional(CONF_XY_COMMAND_TEMPLATE): cv.template,
            vol.Optional(CONF_XY_COMMAND_TOPIC): valid_publish_topic,
            vol.Optional(CONF_XY_STATE_TOPIC): valid_subscribe_topic,
            vol.Optional(CONF_XY_VALUE_TEMPLATE): cv.template,
        },
    )
    .extend(MQTT_ENTITY_COMMON_SCHEMA.schema)
    .extend(MQTT_LIGHT_SCHEMA_SCHEMA.schema)
)

DISCOVERY_SCHEMA_BASIC = vol.All(
    PLATFORM_SCHEMA_MODERN_BASIC.extend({}, extra=vol.REMOVE_EXTRA),
)
