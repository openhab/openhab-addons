"""Support for Valve devices."""

from __future__ import annotations

from enum import StrEnum

import voluptuous as vol

from .const import ValveState


class ValveDeviceClass(StrEnum):
    """Device class for valve."""

    # Refer to the valve dev docs for device class descriptions
    WATER = "water"
    GAS = "gas"


DEVICE_CLASSES_SCHEMA = vol.All(vol.Lower, vol.Coerce(ValveDeviceClass))
