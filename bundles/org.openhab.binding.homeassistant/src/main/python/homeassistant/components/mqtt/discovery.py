"""Support for MQTT discovery."""

from __future__ import annotations

import logging
import re
from typing import Any

import voluptuous as vol

from homeassistant.const import CONF_DEVICE, CONF_PLATFORM
from homeassistant.helpers import config_validation as cv
from homeassistant.helpers.service_info.mqtt import ReceivePayloadType
from homeassistant.helpers.typing import DiscoveryInfoType
from homeassistant.util.json import json_loads_object

from .abbreviations import ABBREVIATIONS, DEVICE_ABBREVIATIONS, ORIGIN_ABBREVIATIONS
from .const import (
    ATTR_DISCOVERY_HASH,
    ATTR_DISCOVERY_PAYLOAD,
    ATTR_DISCOVERY_TOPIC,
    CONF_AVAILABILITY,
    CONF_COMPONENTS,
    CONF_ORIGIN,
    CONF_TOPIC,
)
from .models import MqttComponentConfig
from .schemas import DEVICE_DISCOVERY_SCHEMA, MQTT_ORIGIN_INFO_SCHEMA, SHARED_OPTIONS

ABBREVIATIONS_SET = set(ABBREVIATIONS)
DEVICE_ABBREVIATIONS_SET = set(DEVICE_ABBREVIATIONS)
ORIGIN_ABBREVIATIONS_SET = set(ORIGIN_ABBREVIATIONS)

TOPIC_MATCHER = re.compile(
    r"(?P<component>\w+)/(?:(?P<node_id>[a-zA-Z0-9_-]+)/)"
    r"?(?P<object_id>[a-zA-Z0-9_-]+)$"
)

TOPIC_BASE = "~"

CONF_MIGRATE_DISCOVERY = "migrate_discovery"

MIGRATE_DISCOVERY_SCHEMA = vol.Schema(
    {vol.Optional(CONF_MIGRATE_DISCOVERY): True},
)


class MQTTDiscoveryPayload(dict[str, Any]):
    """Class to hold and MQTT discovery payload and discovery data."""

    device_discovery: bool = False
    migrate_discovery: bool = False
    discovery_data: DiscoveryInfoType


def _process_discovery_migration(payload: MQTTDiscoveryPayload) -> bool:
    """Process a discovery migration request in the discovery payload."""
    # Allow abbreviation
    if migr_discvry := (payload.pop("migr_discvry", None)):
        payload[CONF_MIGRATE_DISCOVERY] = migr_discvry
    if CONF_MIGRATE_DISCOVERY in payload:
        MIGRATE_DISCOVERY_SCHEMA(payload)
        payload.migrate_discovery = True
        payload.clear()
        return True
    return False


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

def _replace_topic_base(discovery_payload: MQTTDiscoveryPayload) -> None:
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

def _parse_device_payload(
    payload: ReceivePayloadType,
    object_id: str,
    node_id: str | None,
) -> MQTTDiscoveryPayload:
    """Parse a device discovery payload.

    The device discovery payload is translated info the config payloads for every single
    component inside the device based configuration.
    An empty payload is translated in a cleanup, which forwards an empty payload to all
    removed components.
    """
    device_payload = MQTTDiscoveryPayload()
    if payload == "":
        # This should not be possible, because we handle vanishing topics separately in the Java code
        return device_payload
    device_payload = MQTTDiscoveryPayload(json_loads_object(payload))
    if _process_discovery_migration(device_payload):
        return device_payload
    _replace_all_abbreviations(device_payload)
    DEVICE_DISCOVERY_SCHEMA(device_payload)
    return device_payload

def _valid_origin_info(discovery_payload: MQTTDiscoveryPayload):
    """Parse and validate origin info from a single component discovery payload."""
    if CONF_ORIGIN not in discovery_payload:
        return
    MQTT_ORIGIN_INFO_SCHEMA(discovery_payload[CONF_ORIGIN])

def _merge_common_device_options(
    component_config: MQTTDiscoveryPayload, device_config: dict[str, Any]
) -> None:
    """Merge common device options with the component config options.

    Common options are:
        CONF_AVAILABILITY,
        CONF_AVAILABILITY_MODE,
        CONF_AVAILABILITY_TEMPLATE,
        CONF_AVAILABILITY_TOPIC,
        CONF_COMMAND_TOPIC,
        CONF_PAYLOAD_AVAILABLE,
        CONF_PAYLOAD_NOT_AVAILABLE,
        CONF_STATE_TOPIC,
    Common options in the body of the device based config are inherited into
    the component. Unless the option is explicitly specified at component level,
    in that case the option at component level will override the common option.
    """
    for option in SHARED_OPTIONS:
        if option in device_config and option not in component_config:
            component_config[option] = device_config.get(option)


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
from .image import DISCOVERY_SCHEMA as IMAGE_DISCOVERY_SCHEMA
from .lawn_mower import DISCOVERY_SCHEMA as LAWN_MOWER_DISCOVERY_SCHEMA
from .light import DISCOVERY_SCHEMA as LIGHT_DISCOVERY_SCHEMA
from .lock import DISCOVERY_SCHEMA as LOCK_DISCOVERY_SCHEMA
from .notify import DISCOVERY_SCHEMA as NOTIFY_DISCOVERY_SCHEMA
from .number import DISCOVERY_SCHEMA as NUMBER_DISCOVERY_SCHEMA
from .scene import DISCOVERY_SCHEMA as SCENE_DISCOVERY_SCHEMA
from .select import DISCOVERY_SCHEMA as SELECT_DISCOVERY_SCHEMA
from .sensor import DISCOVERY_SCHEMA as SENSOR_DISCOVERY_SCHEMA
from .siren import DISCOVERY_SCHEMA as SIREN_DISCOVERY_SCHEMA
from .switch import DISCOVERY_SCHEMA as SWITCH_DISCOVERY_SCHEMA
from .tag import DISCOVERY_SCHEMA as TAG_DISCOVERY_SCHEMA
from .text import DISCOVERY_SCHEMA as TEXT_DISCOVERY_SCHEMA
from .update import DISCOVERY_SCHEMA as UPDATE_DISCOVERY_SCHEMA
from .vacuum import DISCOVERY_SCHEMA as VACUUM_DISCOVERY_SCHEMA
from .valve import DISCOVERY_SCHEMA as VALVE_DISCOVERY_SCHEMA
from .water_heater import DISCOVERY_SCHEMA as WATER_HEATER_DISCOVERY_SCHEMA

# This needs to be kept in sync with the Java AbstractComponent subclasses
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
    'image': IMAGE_DISCOVERY_SCHEMA,
    'lawn_mower': LAWN_MOWER_DISCOVERY_SCHEMA,
    'light': LIGHT_DISCOVERY_SCHEMA,
    'lock': LOCK_DISCOVERY_SCHEMA,
    'notify': NOTIFY_DISCOVERY_SCHEMA,
    'number': NUMBER_DISCOVERY_SCHEMA,
    'scene': SCENE_DISCOVERY_SCHEMA,
    'select': SELECT_DISCOVERY_SCHEMA,
    'sensor': SENSOR_DISCOVERY_SCHEMA,
    'siren': SIREN_DISCOVERY_SCHEMA,
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
def process_discovery_config(topic, payload):
    if not (match := TOPIC_MATCHER.match(topic)):
        raise ValueError(f"Received message on illegal discovery topic '{topic}'. The topic"
                         " contains non allowed characters. For more information see "
                         "https://www.home-assistant.io/integrations/mqtt/#discovery-topic")

    component, node_id, object_id = match.groups()

    discovered_components: list[MqttComponentConfig] = []
    if component == CONF_DEVICE:
        # Process device based discovery message and regenerate
        # cleanup config for the all the components that are being removed.
        # This is done when a component in the device config is omitted and detected
        # as being removed, or when the device config update payload is empty.
        # In that case this will regenerate a cleanup message for all already
        # discovered components that were linked to the initial device discovery.
        device_discovery_payload = _parse_device_payload(
            payload, object_id, node_id
        )
        if device_discovery_payload.migrate_discovery:
            # If this is a migration discovery, we do not want to generate component configs
            # based on the device config, because the device config will be used to trigger
            # the migration and will not contain the necessary component config data.
            return [MqttComponentConfig(component, object_id, node_id, device_discovery_payload)]
        device_config: dict[str, Any]
        origin_config: dict[str, Any] | None
        component_configs: dict[str, dict[str, Any]]
        device_config = device_discovery_payload[CONF_DEVICE]
        origin_config = device_discovery_payload.get(CONF_ORIGIN)
        component_configs = device_discovery_payload[CONF_COMPONENTS]
        if len(component_configs) == 0:
            return [MqttComponentConfig(component, object_id, node_id, device_discovery_payload)]
        for component_id, config in component_configs.items():
            component = config.pop(CONF_PLATFORM)
            # The object_id in the device discovery topic is the unique identifier.
            # It is used as node_id for the components it contains.
            component_node_id = object_id
            # The component_id in the discovery payload is used as object_id
            # If we have an additional node_id in the discovery topic,
            # we extend the component_id with it.
            component_object_id = (
                f"{node_id} {component_id}" if node_id else component_id
            )
            # We add wrapper to the discovery payload with the discovery data.
            # If the dict is empty after removing the platform, the payload is
            # assumed to remove the existing config and we do not want to add
            # device or orig or shared availability attributes.
            if discovery_payload := MQTTDiscoveryPayload(config):
                discovery_payload[CONF_DEVICE] = device_config
                discovery_payload[CONF_ORIGIN] = origin_config
                # Only assign shared config options
                # when they are not set at entity level
                _merge_common_device_options(
                    discovery_payload, device_discovery_payload
                )
            discovery_payload.device_discovery = True
            discovery_payload.migrate_discovery = (
                device_discovery_payload.migrate_discovery
            )
            discovered_components.append(
                MqttComponentConfig(
                    component,
                    component_object_id,
                    component_node_id,
                    discovery_payload,
                )
            )
    else:
        # Process component based discovery message
        discovery_payload = MQTTDiscoveryPayload(json_loads_object(payload) if payload else {})
        if not _process_discovery_migration(discovery_payload):
            _replace_all_abbreviations(discovery_payload)
            _valid_origin_info(discovery_payload)

        discovered_components.append(
            MqttComponentConfig(component, object_id, node_id, discovery_payload)
        )

    for component_config in discovered_components:
        component = component_config.component
        node_id = component_config.node_id
        object_id = component_config.object_id
        discovery_payload = component_config.discovery_payload

        if TOPIC_BASE in discovery_payload:
            _replace_topic_base(discovery_payload)

        # If present, the node_id will be included in the discovery_id.
        discovery_id = f"{node_id} {object_id}" if node_id else object_id
        discovery_hash = (component, discovery_id)

        # Attach MQTT topic to the payload, used for debug prints
        discovery_payload.discovery_data = {
            ATTR_DISCOVERY_HASH: discovery_hash,
            ATTR_DISCOVERY_PAYLOAD: discovery_payload,
            ATTR_DISCOVERY_TOPIC: topic,
        }

        if component in _DISCOVERY_SCHEMAS and not discovery_payload.migrate_discovery:
            discovery_schema = _DISCOVERY_SCHEMAS[component]
            component_config.discovery_payload = discovery_schema(discovery_payload)
    
    return discovered_components
