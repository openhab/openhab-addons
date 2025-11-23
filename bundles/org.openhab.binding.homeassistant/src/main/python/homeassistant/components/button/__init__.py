"""Component to pressing a button as platforms."""

from __future__ import annotations

from enum import StrEnum

import voluptuous as vol


class ButtonDeviceClass(StrEnum):
    """Device class for buttons."""

    IDENTIFY = "identify"
    RESTART = "restart"
    UPDATE = "update"


DEVICE_CLASSES_SCHEMA = vol.All(vol.Lower, vol.Coerce(ButtonDeviceClass))
