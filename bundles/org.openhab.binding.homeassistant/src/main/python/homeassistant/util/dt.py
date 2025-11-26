"""Helper methods to handle the time in Home Assistant."""

from __future__ import annotations

from contextlib import suppress
import datetime as dt
from functools import partial
import re
from typing import Any, Literal, overload
import zoneinfo

from datetime import datetime

UTC = dt.UTC
DEFAULT_TIME_ZONE: dt.tzinfo = dt.UTC

# EPOCHORDINAL is not exposed as a constant
# https://github.com/python/cpython/blob/3.10/Lib/zoneinfo/_zoneinfo.py#L12
EPOCHORDINAL = dt.datetime(1970, 1, 1).toordinal()

# Copyright (c) Django Software Foundation and individual contributors.
# All rights reserved.
# https://github.com/django/django/blob/main/LICENSE
DATETIME_RE = re.compile(
    r"(?P<year>\d{4})-(?P<month>\d{1,2})-(?P<day>\d{1,2})"
    r"[T ](?P<hour>\d{1,2}):(?P<minute>\d{1,2})"
    r"(?::(?P<second>\d{1,2})(?:\.(?P<microsecond>\d{1,6})\d{0,6})?)?"
    r"(?P<tzinfo>Z|[+-]\d{2}(?::?\d{2})?)?$"
)

# Copyright (c) Django Software Foundation and individual contributors.
# All rights reserved.
# https://github.com/django/django/blob/main/LICENSE
STANDARD_DURATION_RE = re.compile(
    r"^"
    r"(?:(?P<days>-?\d+) (days?, )?)?"
    r"(?P<sign>-?)"
    r"((?:(?P<hours>\d+):)(?=\d+:\d+))?"
    r"(?:(?P<minutes>\d+):)?"
    r"(?P<seconds>\d+)"
    r"(?:[\.,](?P<microseconds>\d{1,6})\d{0,6})?"
    r"$"
)

# Copyright (c) Django Software Foundation and individual contributors.
# All rights reserved.
# https://github.com/django/django/blob/main/LICENSE
ISO8601_DURATION_RE = re.compile(
    r"^(?P<sign>[-+]?)"
    r"P"
    r"(?:(?P<days>\d+([\.,]\d+)?)D)?"
    r"(?:T"
    r"(?:(?P<hours>\d+([\.,]\d+)?)H)?"
    r"(?:(?P<minutes>\d+([\.,]\d+)?)M)?"
    r"(?:(?P<seconds>\d+([\.,]\d+)?)S)?"
    r")?"
    r"$"
)

# Copyright (c) Django Software Foundation and individual contributors.
# All rights reserved.
# https://github.com/django/django/blob/main/LICENSE
POSTGRES_INTERVAL_RE = re.compile(
    r"^"
    r"(?:(?P<days>-?\d+) (days? ?))?"
    r"(?:(?P<sign>[-+])?"
    r"(?P<hours>\d+):"
    r"(?P<minutes>\d\d):"
    r"(?P<seconds>\d\d)"
    r"(?:\.(?P<microseconds>\d{1,6}))?"
    r")?$"
)


def set_default_time_zone(time_zone: dt.tzinfo) -> None:
    """Set a default time zone to be used when none is specified.

    Async friendly.
    """
    # pylint: disable-next=global-statement
    global DEFAULT_TIME_ZONE  # noqa: PLW0603

    assert isinstance(time_zone, dt.tzinfo)

    DEFAULT_TIME_ZONE = time_zone


def get_time_zone(time_zone_str: str) -> zoneinfo.ZoneInfo | None:
    """Get time zone from string. Return None if unable to determine."""
    try:
        return zoneinfo.ZoneInfo(time_zone_str)
    except zoneinfo.ZoneInfoNotFoundError:
        return None


# We use a partial here since it is implemented in native code
# and avoids the global lookup of UTC
utcnow = partial(dt.datetime.now, UTC)
utcnow.__doc__ = "Get now in UTC time."


def now(time_zone: dt.tzinfo | None = None) -> dt.datetime:
    """Get now in specified time zone."""
    return dt.datetime.now(time_zone or DEFAULT_TIME_ZONE)


def as_timestamp(dt_value: dt.datetime | str) -> float:
    """Convert a date/time into a unix time (seconds since 1970)."""
    parsed_dt: dt.datetime | None
    if isinstance(dt_value, dt.datetime):
        parsed_dt = dt_value
    else:
        parsed_dt = parse_datetime(str(dt_value))
    if parsed_dt is None:
        raise ValueError("not a valid date/time.")
    return parsed_dt.timestamp()


def as_local(dattim: dt.datetime) -> dt.datetime:
    """Convert a UTC datetime object to local time zone."""
    if dattim.tzinfo == DEFAULT_TIME_ZONE:
        return dattim
    if dattim.tzinfo is None:
        dattim = dattim.replace(tzinfo=DEFAULT_TIME_ZONE)

    return dattim.astimezone(DEFAULT_TIME_ZONE)


# We use a partial here to improve performance by avoiding the global lookup
# of UTC and the function call overhead.
utc_from_timestamp = partial(dt.datetime.fromtimestamp, tz=UTC)
"""Return a UTC time from a timestamp."""


def start_of_local_day(dt_or_d: dt.date | dt.datetime | None = None) -> dt.datetime:
    """Return local datetime object of start of day from date or datetime."""
    if dt_or_d is None:
        date: dt.date = now().date()
    elif isinstance(dt_or_d, dt.datetime):
        date = dt_or_d.date()
    else:
        date = dt_or_d

    return dt.datetime.combine(date, dt.time(), tzinfo=DEFAULT_TIME_ZONE)


# Copyright (c) Django Software Foundation and individual contributors.
# All rights reserved.
# https://github.com/django/django/blob/main/LICENSE
@overload
def parse_datetime(dt_str: str) -> dt.datetime | None: ...


@overload
def parse_datetime(dt_str: str, *, raise_on_error: Literal[True]) -> dt.datetime: ...


@overload
def parse_datetime(
    dt_str: str, *, raise_on_error: Literal[False]
) -> dt.datetime | None: ...


def parse_datetime(dt_str: str, *, raise_on_error: bool = False) -> dt.datetime | None:
    """Parse a string and return a datetime.datetime.

    This function supports time zone offsets. When the input contains one,
    the output uses a timezone with a fixed offset from UTC.
    Raises ValueError if the input is well formatted but not a valid datetime.

    If the input isn't well formatted, returns None if raise_on_error is False
    or raises ValueError if it's True.
    """
    # First try if the string can be parsed by the stdlib
    with suppress(ValueError, IndexError):
        return datetime.fromisoformat(dt_str)

    # stdlib failed to parse the string, fall back to regex
    if not (match := DATETIME_RE.match(dt_str)):
        if raise_on_error:
            raise ValueError
        return None
    kws: dict[str, Any] = match.groupdict()
    if kws["microsecond"]:
        kws["microsecond"] = kws["microsecond"].ljust(6, "0")
    tzinfo_str = kws.pop("tzinfo")

    tzinfo: dt.tzinfo | None = None
    if tzinfo_str == "Z":
        tzinfo = UTC
    elif tzinfo_str is not None:
        offset_mins = int(tzinfo_str[-2:]) if len(tzinfo_str) > 3 else 0
        offset_hours = int(tzinfo_str[1:3])
        offset = dt.timedelta(hours=offset_hours, minutes=offset_mins)
        if tzinfo_str[0] == "-":
            offset = -offset
        tzinfo = dt.timezone(offset)
    kws = {k: int(v) for k, v in kws.items() if v is not None}
    kws["tzinfo"] = tzinfo
    return dt.datetime(**kws)


# Copyright (c) Django Software Foundation and individual contributors.
# All rights reserved.
# https://github.com/django/django/blob/master/LICENSE
def parse_duration(value: str) -> dt.timedelta | None:
    """Parse a duration string and return a datetime.timedelta.

    Also supports ISO 8601 representation and PostgreSQL's day-time interval
    format.
    """
    match = (
        STANDARD_DURATION_RE.match(value)
        or ISO8601_DURATION_RE.match(value)
        or POSTGRES_INTERVAL_RE.match(value)
    )
    if match:
        kws = match.groupdict()
        sign = -1 if kws.pop("sign", "+") == "-" else 1
        if kws.get("microseconds"):
            kws["microseconds"] = kws["microseconds"].ljust(6, "0")
        time_delta_args: dict[str, float] = {
            k: float(v.replace(",", ".")) for k, v in kws.items() if v is not None
        }
        days = dt.timedelta(float(time_delta_args.pop("days", 0.0) or 0.0))
        if match.re == ISO8601_DURATION_RE:
            days *= sign
        return days + sign * dt.timedelta(**time_delta_args)
    return None


def parse_time(time_str: str) -> dt.time | None:
    """Parse a time string (00:20:00) into Time object.

    Return None if invalid.
    """
    parts = str(time_str).split(":")
    if len(parts) < 2:
        return None
    try:
        hour = int(parts[0])
        minute = int(parts[1])
        second = int(parts[2]) if len(parts) > 2 else 0
        return dt.time(hour, minute, second)
    except ValueError:
        # ValueError if value cannot be converted to an int or not in range
        return None


def _get_timestring(timediff: float, precision: int = 1) -> str:
    """Return a string representation of a time diff."""

    def formatn(number: int, unit: str) -> str:
        """Add "unit" if it's plural."""
        if number == 1:
            return f"1 {unit} "
        return f"{number:d} {unit}s "

    if timediff == 0.0:
        return "0 seconds"

    units = ("year", "month", "day", "hour", "minute", "second")

    factors = (365 * 24 * 60 * 60, 30 * 24 * 60 * 60, 24 * 60 * 60, 60 * 60, 60, 1)

    result_string: str = ""
    current_precision = 0

    for i, current_factor in enumerate(factors):
        selected_unit = units[i]
        if timediff < current_factor:
            continue
        current_precision = current_precision + 1
        if current_precision == precision:
            return (
                result_string + formatn(round(timediff / current_factor), selected_unit)
            ).rstrip()
        curr_diff = int(timediff // current_factor)
        result_string += formatn(curr_diff, selected_unit)
        timediff -= (curr_diff) * current_factor

    return result_string.rstrip()


def get_age(date: dt.datetime, precision: int = 1) -> str:
    """Take a datetime and return its "age" as a string.

    The age can be in second, minute, hour, day, month and year.

    depth number of units will be returned, with the last unit rounded

    The date must be in the past or a ValueException will be raised.
    """

    delta = (now() - date).total_seconds()

    rounded_delta = round(delta)

    if rounded_delta < 0:
        raise ValueError("Time value is in the future")
    return _get_timestring(rounded_delta, precision)


def get_time_remaining(date: dt.datetime, precision: int = 1) -> str:
    """Take a datetime and return its "age" as a string.

    The age can be in second, minute, hour, day, month and year.

    depth number of units will be returned, with the last unit rounded

    The date must be in the future or a ValueException will be raised.
    """

    delta = (date - now()).total_seconds()

    rounded_delta = round(delta)

    if rounded_delta < 0:
        raise ValueError("Time value is in the past")

    return _get_timestring(rounded_delta, precision)
