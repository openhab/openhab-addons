"""Component for handling incoming events as a platform."""

from __future__ import annotations

from enum import StrEnum


class EventDeviceClass(StrEnum):
    """Device class for events."""

    DOORBELL = "doorbell"
    BUTTON = "button"
    MOTION = "motion"
