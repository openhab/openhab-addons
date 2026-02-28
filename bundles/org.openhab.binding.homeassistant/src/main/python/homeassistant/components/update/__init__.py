"""Component to allow for providing device or service updates."""

from __future__ import annotations

from enum import StrEnum

import voluptuous as vol


class UpdateDeviceClass(StrEnum):
    """Device class for update."""

    FIRMWARE = "firmware"


DEVICE_CLASSES_SCHEMA = vol.All(vol.Lower, vol.Coerce(UpdateDeviceClass))
