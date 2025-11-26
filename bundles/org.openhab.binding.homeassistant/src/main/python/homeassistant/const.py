"""Constants used by Home Assistant components."""

from __future__ import annotations

from enum import StrEnum
from typing import Final


# Max characters for data stored in the recorder (changes to these limits would require
# a database migration)
MAX_LENGTH_STATE_STATE: Final = 255


# #### CONFIG ####
CONF_BRIGHTNESS: Final = "brightness"
CONF_COLOR_TEMP: Final = "color_temp"
CONF_CODE: Final = "code"
CONF_DEVICE: Final = "device"
CONF_DEVICE_CLASS: Final = "device_class"
CONF_EFFECT: Final = "effect"
CONF_ENTITY_CATEGORY: Final = "entity_category"
CONF_FORCE_UPDATE: Final = "force_update"
CONF_HS: Final = "hs"
CONF_ICON: Final = "icon"
CONF_MODE: Final = "mode"
CONF_MODEL: Final = "model"
CONF_MODEL_ID: Final = "model_id"
CONF_NAME: Final = "name"
CONF_OPTIMISTIC: Final = "optimistic"
CONF_PAYLOAD: Final = "payload"
CONF_PAYLOAD_OFF: Final = "payload_off"
CONF_PAYLOAD_ON: Final = "payload_on"
CONF_PLATFORM: Final = "platform"
CONF_RGB: Final = "rgb"
CONF_STATE_TEMPLATE: Final = "state_template"
CONF_TEMPERATURE_UNIT: Final = "temperature_unit"
CONF_TYPE: Final = "type"
CONF_UNIQUE_ID: Final = "unique_id"
CONF_UNIT_OF_MEASUREMENT: Final = "unit_of_measurement"
CONF_VALUE_TEMPLATE: Final = "value_template"
CONF_XY: Final = "xy"


# #### STATES ####
STATE_ON: Final = "on"
STATE_OFF: Final = "off"
STATE_HOME: Final = "home"
STATE_NOT_HOME: Final = "not_home"
STATE_UNKNOWN: Final = "unknown"
STATE_OPEN: Final = "open"
STATE_OPENING: Final = "opening"
STATE_CLOSED: Final = "closed"
STATE_CLOSING: Final = "closing"
STATE_UNAVAILABLE: Final = "unavailable"


# Bitfield of supported component features for the entity
ATTR_SUPPORTED_FEATURES: Final = "supported_features"


# #### UNITS OF MEASUREMENT ####
# Apparent power units
class UnitOfApparentPower(StrEnum):
    """Apparent power units."""

    VOLT_AMPERE = "VA"


# Power units
class UnitOfPower(StrEnum):
    """Power units."""

    MILLIWATT = "mW"
    WATT = "W"
    KILO_WATT = "kW"
    MEGA_WATT = "MW"
    GIGA_WATT = "GW"
    TERA_WATT = "TW"
    BTU_PER_HOUR = "BTU/h"


# Reactive power units
class UnitOfReactivePower(StrEnum):
    """Reactive power units."""

    VOLT_AMPERE_REACTIVE = "var"
    KILO_VOLT_AMPERE_REACTIVE = "kvar"


# Energy units
class UnitOfEnergy(StrEnum):
    """Energy units."""

    JOULE = "J"
    KILO_JOULE = "kJ"
    MEGA_JOULE = "MJ"
    GIGA_JOULE = "GJ"
    MILLIWATT_HOUR = "mWh"
    WATT_HOUR = "Wh"
    KILO_WATT_HOUR = "kWh"
    MEGA_WATT_HOUR = "MWh"
    GIGA_WATT_HOUR = "GWh"
    TERA_WATT_HOUR = "TWh"
    CALORIE = "cal"
    KILO_CALORIE = "kcal"
    MEGA_CALORIE = "Mcal"
    GIGA_CALORIE = "Gcal"


# Energy Distance units
class UnitOfEnergyDistance(StrEnum):
    """Energy Distance units."""

    KILO_WATT_HOUR_PER_100_KM = "kWh/100km"
    MILES_PER_KILO_WATT_HOUR = "mi/kWh"
    KM_PER_KILO_WATT_HOUR = "km/kWh"


# Electric_current units
class UnitOfElectricCurrent(StrEnum):
    """Electric current units."""

    MILLIAMPERE = "mA"
    AMPERE = "A"


# Electric_potential units
class UnitOfElectricPotential(StrEnum):
    """Electric potential units."""

    MICROVOLT = "µV"
    MILLIVOLT = "mV"
    VOLT = "V"
    KILOVOLT = "kV"
    MEGAVOLT = "MV"


# Degree units
DEGREE: Final = "°"


# Temperature units
class UnitOfTemperature(StrEnum):
    """Temperature units."""

    CELSIUS = "°C"
    FAHRENHEIT = "°F"
    KELVIN = "K"


# Time units
class UnitOfTime(StrEnum):
    """Time units."""

    MICROSECONDS = "μs"
    MILLISECONDS = "ms"
    SECONDS = "s"
    MINUTES = "min"
    HOURS = "h"
    DAYS = "d"
    WEEKS = "w"
    MONTHS = "m"
    YEARS = "y"


# Length units
class UnitOfLength(StrEnum):
    """Length units."""

    MILLIMETERS = "mm"
    CENTIMETERS = "cm"
    METERS = "m"
    KILOMETERS = "km"
    INCHES = "in"
    FEET = "ft"
    YARDS = "yd"
    MILES = "mi"
    NAUTICAL_MILES = "nmi"


# Frequency units
class UnitOfFrequency(StrEnum):
    """Frequency units."""

    HERTZ = "Hz"
    KILOHERTZ = "kHz"
    MEGAHERTZ = "MHz"
    GIGAHERTZ = "GHz"


# Pressure units
class UnitOfPressure(StrEnum):
    """Pressure units."""

    PA = "Pa"
    HPA = "hPa"
    KPA = "kPa"
    BAR = "bar"
    CBAR = "cbar"
    MBAR = "mbar"
    MMHG = "mmHg"
    INHG = "inHg"
    PSI = "psi"


# Sound pressure units
class UnitOfSoundPressure(StrEnum):
    """Sound pressure units."""

    DECIBEL = "dB"
    WEIGHTED_DECIBEL_A = "dBA"


# Volume units
class UnitOfVolume(StrEnum):
    """Volume units."""

    CUBIC_FEET = "ft³"
    CENTUM_CUBIC_FEET = "CCF"
    CUBIC_METERS = "m³"
    LITERS = "L"
    MILLILITERS = "mL"
    GALLONS = "gal"
    """Assumed to be US gallons in conversion utilities.

    British/Imperial gallons are not yet supported"""
    FLUID_OUNCES = "fl. oz."
    """Assumed to be US fluid ounces in conversion utilities.

    British/Imperial fluid ounces are not yet supported"""


# Volume Flow Rate units
class UnitOfVolumeFlowRate(StrEnum):
    """Volume flow rate units."""

    CUBIC_METERS_PER_HOUR = "m³/h"
    CUBIC_METERS_PER_SECOND = "m³/s"
    CUBIC_FEET_PER_MINUTE = "ft³/min"
    LITERS_PER_HOUR = "L/h"
    LITERS_PER_MINUTE = "L/min"
    LITERS_PER_SECOND = "L/s"
    GALLONS_PER_MINUTE = "gal/min"
    MILLILITERS_PER_SECOND = "mL/s"


class UnitOfArea(StrEnum):
    """Area units."""

    SQUARE_METERS = "m²"
    SQUARE_CENTIMETERS = "cm²"
    SQUARE_KILOMETERS = "km²"
    SQUARE_MILLIMETERS = "mm²"
    SQUARE_INCHES = "in²"
    SQUARE_FEET = "ft²"
    SQUARE_YARDS = "yd²"
    SQUARE_MILES = "mi²"
    ACRES = "ac"
    HECTARES = "ha"


# Mass units
class UnitOfMass(StrEnum):
    """Mass units."""

    GRAMS = "g"
    KILOGRAMS = "kg"
    MILLIGRAMS = "mg"
    MICROGRAMS = "µg"
    OUNCES = "oz"
    POUNDS = "lb"
    STONES = "st"


class UnitOfConductivity(StrEnum):
    """Conductivity units."""

    SIEMENS_PER_CM = "S/cm"
    MICROSIEMENS_PER_CM = "µS/cm"
    MILLISIEMENS_PER_CM = "mS/cm"

    # Deprecated aliases
    SIEMENS = "S/cm"
    """Deprecated: Please use UnitOfConductivity.SIEMENS_PER_CM"""
    MICROSIEMENS = "µS/cm"
    """Deprecated: Please use UnitOfConductivity.MICROSIEMENS_PER_CM"""
    MILLISIEMENS = "mS/cm"
    """Deprecated: Please use UnitOfConductivity.MILLISIEMENS_PER_CM"""


# Light units
LIGHT_LUX: Final = "lx"

# Percentage units
PERCENTAGE: Final = "%"


# Irradiance units
class UnitOfIrradiance(StrEnum):
    """Irradiance units."""

    WATTS_PER_SQUARE_METER = "W/m²"
    BTUS_PER_HOUR_SQUARE_FOOT = "BTU/(h⋅ft²)"


class UnitOfVolumetricFlux(StrEnum):
    """Volumetric flux, commonly used for precipitation intensity.

    The derivation of these units is a volume of rain amassing in a container
    with constant cross section in a given time
    """

    INCHES_PER_DAY = "in/d"
    """Derived from in³/(in²⋅d)"""

    INCHES_PER_HOUR = "in/h"
    """Derived from in³/(in²⋅h)"""

    MILLIMETERS_PER_DAY = "mm/d"
    """Derived from mm³/(mm²⋅d)"""

    MILLIMETERS_PER_HOUR = "mm/h"
    """Derived from mm³/(mm²⋅h)"""


class UnitOfPrecipitationDepth(StrEnum):
    """Precipitation depth.

    The derivation of these units is a volume of rain amassing in a container
    with constant cross section
    """

    INCHES = "in"
    """Derived from in³/in²"""

    MILLIMETERS = "mm"
    """Derived from mm³/mm²"""

    CENTIMETERS = "cm"
    """Derived from cm³/cm²"""


# Concentration units
CONCENTRATION_MICROGRAMS_PER_CUBIC_METER: Final = "µg/m³"
CONCENTRATION_PARTS_PER_MILLION: Final = "ppm"
CONCENTRATION_PARTS_PER_BILLION: Final = "ppb"


class UnitOfBloodGlucoseConcentration(StrEnum):
    """Blood glucose concentration units."""

    MILLIGRAMS_PER_DECILITER = "mg/dL"
    MILLIMOLE_PER_LITER = "mmol/L"


# Speed units
class UnitOfSpeed(StrEnum):
    """Speed units."""

    BEAUFORT = "Beaufort"
    FEET_PER_SECOND = "ft/s"
    INCHES_PER_SECOND = "in/s"
    METERS_PER_SECOND = "m/s"
    KILOMETERS_PER_HOUR = "km/h"
    KNOTS = "kn"
    MILES_PER_HOUR = "mph"
    MILLIMETERS_PER_SECOND = "mm/s"


# Signal_strength units
SIGNAL_STRENGTH_DECIBELS: Final = "dB"
SIGNAL_STRENGTH_DECIBELS_MILLIWATT: Final = "dBm"


# Data units
class UnitOfInformation(StrEnum):
    """Information units."""

    BITS = "bit"
    KILOBITS = "kbit"
    MEGABITS = "Mbit"
    GIGABITS = "Gbit"
    BYTES = "B"
    KILOBYTES = "kB"
    MEGABYTES = "MB"
    GIGABYTES = "GB"
    TERABYTES = "TB"
    PETABYTES = "PB"
    EXABYTES = "EB"
    ZETTABYTES = "ZB"
    YOTTABYTES = "YB"
    KIBIBYTES = "KiB"
    MEBIBYTES = "MiB"
    GIBIBYTES = "GiB"
    TEBIBYTES = "TiB"
    PEBIBYTES = "PiB"
    EXBIBYTES = "EiB"
    ZEBIBYTES = "ZiB"
    YOBIBYTES = "YiB"


# Data_rate units
class UnitOfDataRate(StrEnum):
    """Data rate units."""

    BITS_PER_SECOND = "bit/s"
    KILOBITS_PER_SECOND = "kbit/s"
    MEGABITS_PER_SECOND = "Mbit/s"
    GIGABITS_PER_SECOND = "Gbit/s"
    BYTES_PER_SECOND = "B/s"
    KILOBYTES_PER_SECOND = "kB/s"
    MEGABYTES_PER_SECOND = "MB/s"
    GIGABYTES_PER_SECOND = "GB/s"
    KIBIBYTES_PER_SECOND = "KiB/s"
    MEBIBYTES_PER_SECOND = "MiB/s"
    GIBIBYTES_PER_SECOND = "GiB/s"


# The degree of precision for platforms
PRECISION_WHOLE: Final = 1
PRECISION_HALVES: Final = 0.5
PRECISION_TENTHS: Final = 0.1


class EntityCategory(StrEnum):
    """Category of an entity.

    An entity with a category will:
    - Not be exposed to cloud, Alexa, or Google Assistant components
    - Not be included in indirect service calls to devices or areas
    """

    # Config: An entity which allows changing the configuration of a device.
    CONFIG = "config"

    # Diagnostic: An entity exposing some configuration parameter,
    # or diagnostics of a device.
    DIAGNOSTIC = "diagnostic"
