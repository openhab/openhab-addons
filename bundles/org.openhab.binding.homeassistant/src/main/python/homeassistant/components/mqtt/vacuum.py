"""Support for MQTT vacuums."""

from __future__ import annotations

import voluptuous as vol

from homeassistant.components.vacuum import (
    VacuumEntityFeature,
)
from homeassistant.const import ATTR_SUPPORTED_FEATURES, CONF_NAME
from homeassistant.helpers import config_validation as cv

from .config import MQTT_BASE_SCHEMA
from .const import CONF_COMMAND_TOPIC, CONF_RETAIN, CONF_STATE_TOPIC
from .schemas import MQTT_ENTITY_COMMON_SCHEMA
from .util import valid_publish_topic

STATE_IDLE = "idle"
STATE_DOCKED = "docked"
STATE_ERROR = "error"
STATE_PAUSED = "paused"
STATE_RETURNING = "returning"
STATE_CLEANING = "cleaning"

CONF_SUPPORTED_FEATURES = ATTR_SUPPORTED_FEATURES
CONF_PAYLOAD_TURN_ON = "payload_turn_on"
CONF_PAYLOAD_TURN_OFF = "payload_turn_off"
CONF_PAYLOAD_RETURN_TO_BASE = "payload_return_to_base"
CONF_PAYLOAD_STOP = "payload_stop"
CONF_PAYLOAD_CLEAN_SPOT = "payload_clean_spot"
CONF_PAYLOAD_LOCATE = "payload_locate"
CONF_PAYLOAD_START = "payload_start"
CONF_PAYLOAD_PAUSE = "payload_pause"
CONF_SET_FAN_SPEED_TOPIC = "set_fan_speed_topic"
CONF_FAN_SPEED_LIST = "fan_speed_list"
CONF_SEND_COMMAND_TOPIC = "send_command_topic"

DEFAULT_RETAIN = False

DEFAULT_PAYLOAD_RETURN_TO_BASE = "return_to_base"
DEFAULT_PAYLOAD_STOP = "stop"
DEFAULT_PAYLOAD_CLEAN_SPOT = "clean_spot"
DEFAULT_PAYLOAD_LOCATE = "locate"
DEFAULT_PAYLOAD_START = "start"
DEFAULT_PAYLOAD_PAUSE = "pause"

SERVICE_TO_STRING: dict[VacuumEntityFeature, str] = {
    VacuumEntityFeature.START: "start",
    VacuumEntityFeature.PAUSE: "pause",
    VacuumEntityFeature.STOP: "stop",
    VacuumEntityFeature.RETURN_HOME: "return_home",
    VacuumEntityFeature.FAN_SPEED: "fan_speed",
    VacuumEntityFeature.BATTERY: "battery",
    VacuumEntityFeature.STATUS: "status",
    VacuumEntityFeature.SEND_COMMAND: "send_command",
    VacuumEntityFeature.LOCATE: "locate",
    VacuumEntityFeature.CLEAN_SPOT: "clean_spot",
}

STRING_TO_SERVICE = {v: k for k, v in SERVICE_TO_STRING.items()}
DEFAULT_SERVICES = (
    VacuumEntityFeature.START
    | VacuumEntityFeature.STOP
    | VacuumEntityFeature.RETURN_HOME
    | VacuumEntityFeature.BATTERY
    | VacuumEntityFeature.CLEAN_SPOT
)


def services_to_strings(
    services: VacuumEntityFeature,
    service_to_string: dict[VacuumEntityFeature, str],
) -> list[str]:
    """Convert SUPPORT_* service bitmask to list of service strings."""
    return [
        service_to_string[service]
        for service in service_to_string
        if service & services
    ]


DEFAULT_SERVICE_STRINGS = services_to_strings(DEFAULT_SERVICES, SERVICE_TO_STRING)

PLATFORM_SCHEMA_MODERN = MQTT_BASE_SCHEMA.extend(
    {
        vol.Optional(CONF_FAN_SPEED_LIST, default=[]): vol.All(
            cv.ensure_list, [cv.string]
        ),
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(
            CONF_PAYLOAD_CLEAN_SPOT, default=DEFAULT_PAYLOAD_CLEAN_SPOT
        ): cv.string,
        vol.Optional(CONF_PAYLOAD_LOCATE, default=DEFAULT_PAYLOAD_LOCATE): cv.string,
        vol.Optional(
            CONF_PAYLOAD_RETURN_TO_BASE, default=DEFAULT_PAYLOAD_RETURN_TO_BASE
        ): cv.string,
        vol.Optional(CONF_PAYLOAD_START, default=DEFAULT_PAYLOAD_START): cv.string,
        vol.Optional(CONF_PAYLOAD_PAUSE, default=DEFAULT_PAYLOAD_PAUSE): cv.string,
        vol.Optional(CONF_PAYLOAD_STOP, default=DEFAULT_PAYLOAD_STOP): cv.string,
        vol.Optional(CONF_SEND_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_SET_FAN_SPEED_TOPIC): valid_publish_topic,
        vol.Optional(CONF_STATE_TOPIC): valid_publish_topic,
        vol.Optional(CONF_SUPPORTED_FEATURES, default=DEFAULT_SERVICE_STRINGS): vol.All(
            cv.ensure_list, [vol.In(STRING_TO_SERVICE.keys())]
        ),
        vol.Optional(CONF_COMMAND_TOPIC): valid_publish_topic,
        vol.Optional(CONF_RETAIN, default=DEFAULT_RETAIN): cv.boolean,
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)

DISCOVERY_SCHEMA = PLATFORM_SCHEMA_MODERN.extend({}, extra=vol.ALLOW_EXTRA)
