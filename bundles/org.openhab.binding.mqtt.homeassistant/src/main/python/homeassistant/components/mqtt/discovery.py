"""Support for MQTT discovery."""

from __future__ import annotations

import logging
import re
from typing import Any

import voluptuous as vol

from homeassistant.const import CONF_DEVICE
from homeassistant.helpers import config_validation as cv
from homeassistant.util.json import json_loads_object

from .abbreviations import ABBREVIATIONS, DEVICE_ABBREVIATIONS, ORIGIN_ABBREVIATIONS
from .const import (
    CONF_AVAILABILITY,
    CONF_COMPONENTS,
    CONF_ORIGIN,
    CONF_TOPIC,
)
from .schemas import MQTT_ORIGIN_INFO_SCHEMA

ABBREVIATIONS_SET = set(ABBREVIATIONS)
DEVICE_ABBREVIATIONS_SET = set(DEVICE_ABBREVIATIONS)
ORIGIN_ABBREVIATIONS_SET = set(ORIGIN_ABBREVIATIONS)

_LOGGER = logging.getLogger(__name__)

TOPIC_MATCHER = re.compile(
    r"(?P<component>\w+)/(?:(?P<node_id>[a-zA-Z0-9_-]+)/)"
    r"?(?P<object_id>[a-zA-Z0-9_-]+)/config"
)

TOPIC_BASE = "~"

def _replace_abbreviations(
    payload: dict[str, Any] | str,
    abbreviations: dict[str, str],
    abbreviations_set: set[str],
) -> None:
    """Replace abbreviations in an MQTT discovery payload."""
    if not isinstance(payload, dict):
        return
    for key in abbreviations_set.intersection(payload):
        payload[abbreviations[key]] = payload.pop(key)

def _replace_all_abbreviations(
    discovery_payload: dict[str, Any], component_only: bool = False
) -> None:
    """Replace all abbreviations in an MQTT discovery payload."""

    _replace_abbreviations(discovery_payload, ABBREVIATIONS, ABBREVIATIONS_SET)

    if CONF_AVAILABILITY in discovery_payload:
        for availability_conf in cv.ensure_list(discovery_payload[CONF_AVAILABILITY]):
            _replace_abbreviations(availability_conf, ABBREVIATIONS, ABBREVIATIONS_SET)

    if component_only:
        return

    if CONF_ORIGIN in discovery_payload:
        _replace_abbreviations(
            discovery_payload[CONF_ORIGIN],
            ORIGIN_ABBREVIATIONS,
            ORIGIN_ABBREVIATIONS_SET,
        )

    if CONF_DEVICE in discovery_payload:
        _replace_abbreviations(
            discovery_payload[CONF_DEVICE],
            DEVICE_ABBREVIATIONS,
            DEVICE_ABBREVIATIONS_SET,
        )

    if CONF_COMPONENTS in discovery_payload:
        if not isinstance(discovery_payload[CONF_COMPONENTS], dict):
            return
        for comp_conf in discovery_payload[CONF_COMPONENTS].values():
            _replace_all_abbreviations(comp_conf, component_only=True)

def _replace_topic_base(discovery_payload: dict[str, Any]) -> None:
    """Replace topic base in MQTT discovery data."""
    base = discovery_payload.pop(TOPIC_BASE)
    for key, value in discovery_payload.items():
        if isinstance(value, str) and value:
            if value[0] == TOPIC_BASE and key.endswith("topic"):
                discovery_payload[key] = f"{base}{value[1:]}"
            if value[-1] == TOPIC_BASE and key.endswith("topic"):
                discovery_payload[key] = f"{value[:-1]}{base}"
    if discovery_payload.get(CONF_AVAILABILITY):
        for availability_conf in cv.ensure_list(discovery_payload[CONF_AVAILABILITY]):
            if not isinstance(availability_conf, dict):
                continue
            if topic := str(availability_conf.get(CONF_TOPIC)):
                if topic[0] == TOPIC_BASE:
                    availability_conf[CONF_TOPIC] = f"{base}{topic[1:]}"
                if topic[-1] == TOPIC_BASE:
                    availability_conf[CONF_TOPIC] = f"{topic[:-1]}{base}"

def _valid_origin_info(discovery_payload: dict[str, Any]) -> bool:
    """Parse and validate origin info from a single component discovery payload."""
    if CONF_ORIGIN not in discovery_payload:
        return True
    try:
        MQTT_ORIGIN_INFO_SCHEMA(discovery_payload[CONF_ORIGIN])
    except Exception as exc:  # noqa:BLE001
        _LOGGER.warning(
            "Unable to parse origin information from discovery message: %s, got %s",
            exc,
            discovery_payload[CONF_ORIGIN],
        )
        return False
    return True

# ******************************************************************************
# The rest of this file is _not_ directly Home Assistant code -- but is based on
# it -- in order to marry Home Assistant's discovery process with openHAB's

from .alarm_control_panel import DISCOVERY_SCHEMA as ALARM_CONTROL_PANEL_DISCOVERY_SCHEMA
from .binary_sensor import DISCOVERY_SCHEMA as BINARY_SENSOR_DISCOVERY_SCHEMA
from .button import DISCOVERY_SCHEMA as BUTTON_SENSOR_DISCOVERY_SCHEMA
from .camera import DISCOVERY_SCHEMA as CAMERA_DISCOVERY_SCHEMA
from .climate import DISCOVERY_SCHEMA as CLIMATE_DISCOVERY_SCHEMA
from .cover import DISCOVERY_SCHEMA as COVER_DISCOVERY_SCHEMA
from .device_tracker import DISCOVERY_SCHEMA as DEVICE_TRACKER_DISCOVERY_SCHEMA
from .device_trigger import TRIGGER_DISCOVERY_SCHEMA as DEVICE_TRIGGER_DISCOVERY_SCHEMA
from .event import DISCOVERY_SCHEMA as EVENT_DISCOVERY_SCHEMA
from .fan import DISCOVERY_SCHEMA as FAN_DISCOVERY_SCHEMA
from .humidifier import DISCOVERY_SCHEMA as HUMIDIFIER_DISCOVERY_SCHEMA
from .light import DISCOVERY_SCHEMA as LIGHT_DISCOVERY_SCHEMA
from .lock import DISCOVERY_SCHEMA as LOCK_DISCOVERY_SCHEMA
from .number import DISCOVERY_SCHEMA as NUMBER_DISCOVERY_SCHEMA
from .scene import DISCOVERY_SCHEMA as SCENE_DISCOVERY_SCHEMA
from .select import DISCOVERY_SCHEMA as SELECT_DISCOVERY_SCHEMA
from .sensor import DISCOVERY_SCHEMA as SENSOR_DISCOVERY_SCHEMA
from .switch import DISCOVERY_SCHEMA as SWITCH_DISCOVERY_SCHEMA
from .tag import DISCOVERY_SCHEMA as TAG_DISCOVERY_SCHEMA
from .text import DISCOVERY_SCHEMA as TEXT_DISCOVERY_SCHEMA
from .update import DISCOVERY_SCHEMA as UPDATE_DISCOVERY_SCHEMA
from .vacuum import DISCOVERY_SCHEMA as VACUUM_DISCOVERY_SCHEMA
from .valve import DISCOVERY_SCHEMA as VALVE_DISCOVERY_SCHEMA
from .water_heater import DISCOVERY_SCHEMA as WATER_HEATER_DISCOVERY_SCHEMA

_DISCOVERY_SCHEMAS = {
    'alarm_control_panel': ALARM_CONTROL_PANEL_DISCOVERY_SCHEMA,
    'binary_sensor': BINARY_SENSOR_DISCOVERY_SCHEMA,
    'button': BUTTON_SENSOR_DISCOVERY_SCHEMA,
    'camera': CAMERA_DISCOVERY_SCHEMA,
    'climate': CLIMATE_DISCOVERY_SCHEMA,
    'cover': COVER_DISCOVERY_SCHEMA,
    'device_tracker': DEVICE_TRACKER_DISCOVERY_SCHEMA,
    'device_automation': DEVICE_TRIGGER_DISCOVERY_SCHEMA,
    'event': EVENT_DISCOVERY_SCHEMA,
    'fan': FAN_DISCOVERY_SCHEMA,
    'humidifier': HUMIDIFIER_DISCOVERY_SCHEMA,
    'light': LIGHT_DISCOVERY_SCHEMA,
    'lock': LOCK_DISCOVERY_SCHEMA,
    'number': NUMBER_DISCOVERY_SCHEMA,
    'scene': SCENE_DISCOVERY_SCHEMA,
    'select': SELECT_DISCOVERY_SCHEMA,
    'sensor': SENSOR_DISCOVERY_SCHEMA,
    'switch': SWITCH_DISCOVERY_SCHEMA,
    'tag': TAG_DISCOVERY_SCHEMA,
    'text': TEXT_DISCOVERY_SCHEMA,
    'update': UPDATE_DISCOVERY_SCHEMA,
    'vacuum': VACUUM_DISCOVERY_SCHEMA,
    'valve': VALVE_DISCOVERY_SCHEMA,
    'water_heater': WATER_HEATER_DISCOVERY_SCHEMA,
}

# This is partially based on async_discovery_message_received
# Logging is not done; errors are simply raised so the logging
# can occur in Java
def process_discovery_config(component, payload):
    # Process component based discovery message
    discovery_payload = json_loads_object(payload) if payload else {}
    _replace_all_abbreviations(discovery_payload)
    if not _valid_origin_info(discovery_payload):
        return
    
    if TOPIC_BASE in discovery_payload:
        _replace_topic_base(discovery_payload)

    if component not in _DISCOVERY_SCHEMAS:
        raise ValueError("Unknown component type %s", component)

    discovery_schema = _DISCOVERY_SCHEMAS[component]
    discovery_payload = discovery_schema(discovery_payload)
    
    return discovery_payload
