"""Control a MQTT alarm."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.const import CONF_CODE, CONF_NAME, CONF_VALUE_TEMPLATE
from homeassistant.helpers import config_validation as cv

from .config import DEFAULT_RETAIN, MQTT_BASE_SCHEMA
from .const import (
    ALARM_CONTROL_PANEL_SUPPORTED_FEATURES,
    CONF_CODE_ARM_REQUIRED,
    CONF_CODE_DISARM_REQUIRED,
    CONF_CODE_TRIGGER_REQUIRED,
    CONF_COMMAND_TEMPLATE,
    CONF_COMMAND_TOPIC,
    CONF_PAYLOAD_ARM_AWAY,
    CONF_PAYLOAD_ARM_CUSTOM_BYPASS,
    CONF_PAYLOAD_ARM_HOME,
    CONF_PAYLOAD_ARM_NIGHT,
    CONF_PAYLOAD_ARM_VACATION,
    CONF_PAYLOAD_DISARM,
    CONF_PAYLOAD_TRIGGER,
    CONF_RETAIN,
    CONF_STATE_TOPIC,
    CONF_SUPPORTED_FEATURES,
    DEFAULT_ALARM_CONTROL_PANEL_COMMAND_TEMPLATE,
    DEFAULT_PAYLOAD_ARM_AWAY,
    DEFAULT_PAYLOAD_ARM_CUSTOM_BYPASS,
    DEFAULT_PAYLOAD_ARM_HOME,
    DEFAULT_PAYLOAD_ARM_NIGHT,
    DEFAULT_PAYLOAD_ARM_VACATION,
    DEFAULT_PAYLOAD_DISARM,
    DEFAULT_PAYLOAD_TRIGGER,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_publish_topic, valid_subscribe_topic

PLATFORM_SCHEMA_MODERN = MQTT_BASE_SCHEMA.extend(
    {
        vol.Optional(
            CONF_SUPPORTED_FEATURES,
            default=list(ALARM_CONTROL_PANEL_SUPPORTED_FEATURES),
        ): [vol.In(ALARM_CONTROL_PANEL_SUPPORTED_FEATURES)],
        vol.Optional(CONF_CODE): cv.string,
        vol.Optional(CONF_CODE_ARM_REQUIRED, default=True): cv.boolean,
        vol.Optional(CONF_CODE_DISARM_REQUIRED, default=True): cv.boolean,
        vol.Optional(CONF_CODE_TRIGGER_REQUIRED, default=True): cv.boolean,
        vol.Optional(
            CONF_COMMAND_TEMPLATE, default=DEFAULT_ALARM_CONTROL_PANEL_COMMAND_TEMPLATE
        ): cv.template,
        vol.Required(CONF_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(

            CONF_PAYLOAD_ARM_AWAY, default=DEFAULT_PAYLOAD_ARM_AWAY
        ): cv.string,
        vol.Optional(
            CONF_PAYLOAD_ARM_HOME, default=DEFAULT_PAYLOAD_ARM_HOME
        ): cv.string,
        vol.Optional(
            CONF_PAYLOAD_ARM_NIGHT, default=DEFAULT_PAYLOAD_ARM_NIGHT
        ): cv.string,
        vol.Optional(
            CONF_PAYLOAD_ARM_VACATION, default=DEFAULT_PAYLOAD_ARM_VACATION
        ): cv.string,
        vol.Optional(
            CONF_PAYLOAD_ARM_CUSTOM_BYPASS, default=DEFAULT_PAYLOAD_ARM_CUSTOM_BYPASS
        ): cv.string,
        vol.Optional(CONF_PAYLOAD_DISARM, default=DEFAULT_PAYLOAD_DISARM): cv.string,
        vol.Optional(CONF_PAYLOAD_TRIGGER, default=DEFAULT_PAYLOAD_TRIGGER): cv.string,
        vol.Optional(CONF_RETAIN, default=DEFAULT_RETAIN): cv.boolean,
        vol.Required(CONF_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_VALUE_TEMPLATE): cv.template,
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = PLATFORM_SCHEMA_MODERN.extend({}, extra=vol.REMOVE_EXTRA)
