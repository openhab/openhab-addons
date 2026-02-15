"""Support for MQTT lawn mowers."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.const import CONF_NAME, CONF_OPTIMISTIC
from homeassistant.helpers import config_validation as cv

from .config import MQTT_BASE_SCHEMA
from .const import CONF_RETAIN, DEFAULT_OPTIMISTIC, DEFAULT_RETAIN
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_publish_topic, valid_subscribe_topic

CONF_ACTIVITY_STATE_TOPIC = "activity_state_topic"
CONF_ACTIVITY_VALUE_TEMPLATE = "activity_value_template"
CONF_DOCK_COMMAND_TOPIC = "dock_command_topic"
CONF_DOCK_COMMAND_TEMPLATE = "dock_command_template"
CONF_PAUSE_COMMAND_TOPIC = "pause_command_topic"
CONF_PAUSE_COMMAND_TEMPLATE = "pause_command_template"
CONF_START_MOWING_COMMAND_TOPIC = "start_mowing_command_topic"
CONF_START_MOWING_COMMAND_TEMPLATE = "start_mowing_command_template"

PLATFORM_SCHEMA_MODERN = MQTT_BASE_SCHEMA.extend(
    {
        vol.Optional(CONF_ACTIVITY_VALUE_TEMPLATE): cv.template,
        vol.Optional(CONF_ACTIVITY_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_DOCK_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_DOCK_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_OPTIMISTIC, default=DEFAULT_OPTIMISTIC): cv.boolean,
        vol.Optional(CONF_PAUSE_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_PAUSE_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_RETAIN, default=DEFAULT_RETAIN): cv.boolean,
        vol.Optional(CONF_START_MOWING_COMMAND_TEMPLATE): cv.template,
        vol.Optional(CONF_START_MOWING_COMMAND_TOPIC): valid_publish_topic,
    },
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = vol.All(PLATFORM_SCHEMA_MODERN.extend({}, extra=vol.REMOVE_EXTRA))
