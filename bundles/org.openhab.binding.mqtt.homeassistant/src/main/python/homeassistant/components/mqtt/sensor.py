"""Support for MQTT sensors."""

from __future__ import annotations

import logging
from typing import Any

import voluptuous as vol

from homeassistant.components.sensor import (
    CONF_STATE_CLASS,
    DEVICE_CLASS_UNITS,
    DEVICE_CLASSES_SCHEMA,
    STATE_CLASSES_SCHEMA,
    SensorDeviceClass,
    SensorStateClass,
)
from homeassistant.const import (
    CONF_DEVICE_CLASS,
    CONF_FORCE_UPDATE,
    CONF_NAME,
    CONF_UNIT_OF_MEASUREMENT,
)
from homeassistant.helpers import config_validation as cv

from .config import MQTT_RO_SCHEMA
from .const import (
    CONF_EXPIRE_AFTER,
    CONF_LAST_RESET_VALUE_TEMPLATE,
    CONF_OPTIONS,
    CONF_SUGGESTED_DISPLAY_PRECISION,
)
from .schemas import MQTT_ENTITY_COMMON_SCHEMA

_LOGGER = logging.getLogger(__name__)

DEFAULT_FORCE_UPDATE = False

_PLATFORM_SCHEMA_BASE = MQTT_RO_SCHEMA.extend(
    {
        vol.Optional(CONF_DEVICE_CLASS): vol.Any(DEVICE_CLASSES_SCHEMA, None),
        vol.Optional(CONF_EXPIRE_AFTER): cv.positive_int,
        vol.Optional(CONF_FORCE_UPDATE, default=DEFAULT_FORCE_UPDATE): cv.boolean,
        vol.Optional(CONF_LAST_RESET_VALUE_TEMPLATE): cv.template,
        vol.Optional(CONF_NAME): vol.Any(cv.string, None),
        vol.Optional(CONF_OPTIONS): cv.ensure_list,
        vol.Optional(CONF_SUGGESTED_DISPLAY_PRECISION): cv.positive_int,
        vol.Optional(CONF_STATE_CLASS): vol.Any(STATE_CLASSES_SCHEMA, None),
        vol.Optional(CONF_UNIT_OF_MEASUREMENT): vol.Any(cv.string, None),
    }
).extend(MQTT_ENTITY_COMMON_SCHEMA.schema)


def validate_sensor_state_and_device_class_config(config: dict[str, Any]) -> dict[str, Any]:
    """Validate the sensor options, state and device class config."""
    if (
        CONF_LAST_RESET_VALUE_TEMPLATE in config
        and (state_class := config.get(CONF_STATE_CLASS)) != SensorStateClass.TOTAL
    ):
        raise vol.Invalid(
            f"The option `{CONF_LAST_RESET_VALUE_TEMPLATE}` cannot be used "
            f"together with state class `{state_class}`"
        )

    # Only allow `options` to be set for `enum` sensors
    # to limit the possible sensor values
    if (options := config.get(CONF_OPTIONS)) is not None:
        if not options:
            raise vol.Invalid("An empty options list is not allowed")
        if config.get(CONF_STATE_CLASS) or config.get(CONF_UNIT_OF_MEASUREMENT):
            raise vol.Invalid(
                f"Specifying `{CONF_OPTIONS}` is not allowed together with "
                f"the `{CONF_STATE_CLASS}` or `{CONF_UNIT_OF_MEASUREMENT}` option"
            )

        if (device_class := config.get(CONF_DEVICE_CLASS)) != SensorDeviceClass.ENUM:
            raise vol.Invalid(
                f"The option `{CONF_OPTIONS}` must be used "
                f"together with device class `{SensorDeviceClass.ENUM}`, "
                f"got `{CONF_DEVICE_CLASS}` '{device_class}'"
            )

    if (device_class := config.get(CONF_DEVICE_CLASS)) is None or (
        unit_of_measurement := config.get(CONF_UNIT_OF_MEASUREMENT)
    ) is None:
        return config

    if (
        device_class in DEVICE_CLASS_UNITS
        and unit_of_measurement not in DEVICE_CLASS_UNITS[device_class]
    ):
        _LOGGER.warning(
            "The unit of measurement `%s` is not valid "
            "together with device class `%s`. "
            "this will stop working in HA Core 2025.7.0",
            unit_of_measurement,
            device_class,
        )

    return config


DISCOVERY_SCHEMA = vol.All(
    _PLATFORM_SCHEMA_BASE.extend({}, extra=vol.REMOVE_EXTRA),
    validate_sensor_state_and_device_class_config,
)
