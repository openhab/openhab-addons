"""Provides the constants needed for component."""

from enum import IntFlag, StrEnum


class HVACMode(StrEnum):
    """HVAC mode for climate devices."""

    # All activity disabled / Device is off/standby
    OFF = "off"

    # Heating
    HEAT = "heat"

    # Cooling
    COOL = "cool"

    # The device supports heating/cooling to a range
    HEAT_COOL = "heat_cool"

    # The temperature is set based on a schedule, learned behavior, AI or some
    # other related mechanism. User is not able to adjust the temperature
    AUTO = "auto"

    # Device is in Dry/Humidity mode
    DRY = "dry"

    # Only the fan is on, not fan and another mode like cool
    FAN_ONLY = "fan_only"


# No preset is active
PRESET_NONE = "none"

# Possible fan state
FAN_AUTO = "auto"
FAN_LOW = "low"
FAN_MEDIUM = "medium"
FAN_HIGH = "high"


# Possible swing state
SWING_ON = "on"
SWING_OFF = "off"


DEFAULT_MIN_HUMIDITY = 30
DEFAULT_MAX_HUMIDITY = 99
