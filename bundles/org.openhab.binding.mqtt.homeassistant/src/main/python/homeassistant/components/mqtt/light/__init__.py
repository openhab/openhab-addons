"""Support for MQTT lights."""

from __future__ import annotations

from typing import Any

import voluptuous as vol

from .schema import CONF_SCHEMA, MQTT_LIGHT_SCHEMA_SCHEMA
from .schema_basic import (
    DISCOVERY_SCHEMA_BASIC,
)
from .schema_json import (
    DISCOVERY_SCHEMA_JSON,
)
from .schema_template import (
    DISCOVERY_SCHEMA_TEMPLATE,
)


def validate_mqtt_light_discovery(config_value: dict[str, Any]) -> dict[str, Any]:
    """Validate MQTT light schema for discovery."""
    schemas: dict[str, vol.Schema | vol.All | vol.Any] = {
        "basic": DISCOVERY_SCHEMA_BASIC,
        "json": DISCOVERY_SCHEMA_JSON,
        "template": DISCOVERY_SCHEMA_TEMPLATE,
    }
    config: dict[str, Any] = schemas[config_value[CONF_SCHEMA]](config_value)
    return config


DISCOVERY_SCHEMA = vol.All(
    MQTT_LIGHT_SCHEMA_SCHEMA.extend({}, extra=vol.ALLOW_EXTRA),
    validate_mqtt_light_discovery,
)
