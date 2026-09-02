"""Support for Cover devices."""

from __future__ import annotations

from enum import StrEnum

import voluptuous as vol


class CoverDeviceClass(StrEnum):
    """Device class for cover."""

    # Refer to the cover dev docs for device class descriptions
    AWNING = "awning"
    BLIND = "blind"
    CURTAIN = "curtain"
    DAMPER = "damper"
    DOOR = "door"
    GARAGE = "garage"
    GATE = "gate"
    SHADE = "shade"
    SHUTTER = "shutter"
    WINDOW = "window"


DEVICE_CLASSES_SCHEMA = vol.All(vol.Lower, vol.Coerce(CoverDeviceClass))
