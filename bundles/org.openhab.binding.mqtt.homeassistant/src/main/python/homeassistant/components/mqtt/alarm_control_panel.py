"""Control a MQTT alarm."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.components import alarm_control_panel as alarm
from homeassistant.components.alarm_control_panel import AlarmControlPanelEntityFeature
from homeassistant.const import CONF_CODE, CONF_NAME, CONF_VALUE_TEMPLATE
from homeassistant.helpers import config_validation as cv

from .config import DEFAULT_RETAIN, MQTT_BASE_SCHEMA
from .const import (
    CONF_COMMAND_TEMPLATE,
    CONF_COMMAND_TOPIC,
    CONF_RETAIN,
    CONF_STATE_TOPIC,
    CONF_SUPPORTED_FEATURES,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_publish_topic, valid_subscribe_topic


_SUPPORTED_FEATURES = {
    "arm_home": AlarmControlPanelEntityFeature.ARM_HOME,
    "arm_away": AlarmControlPanelEntityFeature.ARM_AWAY,
    "arm_night": AlarmControlPanelEntityFeature.ARM_NIGHT,
    "arm_vacation": AlarmControlPanelEntityFeature.ARM_VACATION,
    "arm_custom_bypass": AlarmControlPanelEntityFeature.ARM_CUSTOM_BYPASS,
    "trigger": AlarmControlPanelEntityFeature.TRIGGER,
}

CONF_CODE_ARM_REQUIRED = "code_arm_required"
CONF_CODE_DISARM_REQUIRED = "code_disarm_required"
CONF_CODE_TRIGGER_REQUIRED = "code_trigger_required"
CONF_PAYLOAD_DISARM = "payload_disarm"
CONF_PAYLOAD_ARM_HOME = "payload_arm_home"
CONF_PAYLOAD_ARM_AWAY = "payload_arm_away"
CONF_PAYLOAD_ARM_NIGHT = "payload_arm_night"
CONF_PAYLOAD_ARM_VACATION = "payload_arm_vacation"
CONF_PAYLOAD_ARM_CUSTOM_BYPASS = "payload_arm_custom_bypass"
CONF_PAYLOAD_TRIGGER = "payload_trigger"

DEFAULT_COMMAND_TEMPLATE = "{{action}}"
DEFAULT_ARM_NIGHT = "ARM_NIGHT"
DEFAULT_ARM_VACATION = "ARM_VACATION"
DEFAULT_ARM_AWAY = "ARM_AWAY"
DEFAULT_ARM_HOME = "ARM_HOME"
DEFAULT_ARM_CUSTOM_BYPASS = "ARM_CUSTOM_BYPASS"
DEFAULT_DISARM = "DISARM"
DEFAULT_TRIGGER = "TRIGGER"
DEFAULT_NAME = "MQTT Alarm"

REMOTE_CODE = "REMOTE_CODE"
REMOTE_CODE_TEXT = "REMOTE_CODE_TEXT"

PLATFORM_SCHEMA_MODERN = MQTT_BASE_SCHEMA.extend(
    {
        vol.Optional(CONF_SUPPORTED_FEATURES, default=list(_SUPPORTED_FEATURES)): [
            vol.In(_SUPPORTED_FEATURES)
        ],
        vol.Optional(CONF_CODE): cv.string,
        vol.Optional(CONF_CODE_ARM_REQUIRED, default=True): cv.boolean,
        vol.Optional(CONF_CODE_DISARM_REQUIRED, default=True): cv.boolean,
        vol.Optional(CONF_CODE_TRIGGER_REQUIRED, default=True): cv.boolean,
        vol.Optional(
            CONF_COMMAND_TEMPLATE, default=DEFAULT_COMMAND_TEMPLATE
        ): cv.template,
        vol.Required(CONF_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_PAYLOAD_ARM_AWAY, default=DEFAULT_ARM_AWAY): cv.string,
        vol.Optional(CONF_PAYLOAD_ARM_HOME, default=DEFAULT_ARM_HOME): cv.string,
        vol.Optional(CONF_PAYLOAD_ARM_NIGHT, default=DEFAULT_ARM_NIGHT): cv.string,
        vol.Optional(
            CONF_PAYLOAD_ARM_VACATION, default=DEFAULT_ARM_VACATION
        ): cv.string,
        vol.Optional(
            CONF_PAYLOAD_ARM_CUSTOM_BYPASS, default=DEFAULT_ARM_CUSTOM_BYPASS
        ): cv.string,
        vol.Optional(CONF_PAYLOAD_DISARM, default=DEFAULT_DISARM): cv.string,
        vol.Optional(CONF_PAYLOAD_TRIGGER, default=DEFAULT_TRIGGER): cv.string,
        vol.Optional(CONF_RETAIN, default=DEFAULT_RETAIN): cv.boolean,
        vol.Required(CONF_STATE_TOPIC): valid_subscribe_topic,
        vol.Optional(CONF_VALUE_TEMPLATE): cv.template,
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = PLATFORM_SCHEMA_MODERN.extend({}, extra=vol.REMOVE_EXTRA)
