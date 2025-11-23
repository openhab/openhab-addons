"""Provides the constants needed for component."""

from enum import IntFlag


class AlarmControlPanelEntityFeature(IntFlag):
    """Supported features of the alarm control panel entity."""

    ARM_HOME = 1
    ARM_AWAY = 2
    ARM_NIGHT = 4
    TRIGGER = 8
    ARM_CUSTOM_BYPASS = 16
    ARM_VACATION = 32
