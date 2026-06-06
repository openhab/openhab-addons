"""Provides functionality to interact with humidifier devices."""

from __future__ import annotations

from enum import StrEnum

from .const import (  # noqa: F401
    DEFAULT_MAX_HUMIDITY,
    DEFAULT_MIN_HUMIDITY,
)


class HumidifierDeviceClass(StrEnum):
    """Device class for humidifiers."""

    HUMIDIFIER = "humidifier"
    DEHUMIDIFIER = "dehumidifier"
