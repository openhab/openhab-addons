"""Component to interface with switches that can be controlled remotely."""

from __future__ import annotations

from enum import StrEnum

import voluptuous as vol


class SwitchDeviceClass(StrEnum):
    """Device class for switches."""

    OUTLET = "outlet"
    SWITCH = "switch"


DEVICE_CLASSES_SCHEMA = vol.All(vol.Lower, vol.Coerce(SwitchDeviceClass))
