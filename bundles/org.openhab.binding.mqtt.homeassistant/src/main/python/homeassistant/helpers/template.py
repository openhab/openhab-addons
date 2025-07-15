"""Template helper methods for rendering strings with Home Assistant data."""

from __future__ import annotations

from ast import literal_eval
import base64
import collections.abc
from collections.abc import Callable, Iterable, Mapping, MutableSequence
from contextlib import AbstractContextManager
from contextvars import ContextVar
from datetime import date, datetime, time, timedelta
from functools import lru_cache, wraps
import hashlib
import json
import logging
import math
from operator import contains
import random
import re
import statistics
from struct import error as StructError, pack, unpack_from
from types import CodeType, TracebackType
from typing import (
    Any,
    Literal,
    NoReturn,
    Self,
    overload,
)
from urllib.parse import urlencode as urllib_urlencode
import weakref

from awesomeversion import AwesomeVersion
import jinja2
from jinja2 import pass_context, pass_environment
from jinja2.runtime import AsyncLoopContext, LoopContext
from jinja2.sandbox import ImmutableSandboxedEnvironment
from jinja2.utils import Namespace

import voluptuous as vol

from homeassistant.exceptions import TemplateError
from homeassistant.util import (
    dt as dt_util,
    slugify as slugify_util,
)

# mypy: allow-untyped-defs, no-check-untyped-defs

_LOGGER = logging.getLogger(__name__)
_SENTINEL = object()
DATE_STR_FORMAT = "%Y-%m-%d %H:%M:%S"

# Match "simple" ints and floats. -1.0, 1, +5, 5.0
_IS_NUMERIC = re.compile(r"^[+-]?(?!0\d)\d*(?:\.\d*)?$")


template_cv: ContextVar[tuple[str, str] | None] = ContextVar(
    "template_cv", default=None
)

EVAL_CACHE_SIZE = 512

MAX_CUSTOM_TEMPLATE_SIZE = 5 * 1024 * 1024
MAX_TEMPLATE_OUTPUT = 256 * 1024  # 256KiB

def is_template_string(maybe_template: str) -> bool:
    """Check if the input is a Jinja2 template."""
    return "{" in maybe_template and (
        "{%" in maybe_template or "{{" in maybe_template or "{#" in maybe_template
    )


class ResultWrapper:
    """Result wrapper class to store render result."""

    render_result: str | None


def gen_result_wrapper(kls: type[dict | list | set]) -> type:
    """Generate a result wrapper."""

    class Wrapper(kls, ResultWrapper):  # type: ignore[valid-type,misc]
        """Wrapper of a kls that can store render_result."""

        def __init__(self, *args: Any, render_result: str | None = None) -> None:
            super().__init__(*args)
            self.render_result = render_result

        def __str__(self) -> str:
            if self.render_result is None:
                # Can't get set repr to work
                if kls is set:
                    return str(set(self))

                return kls.__str__(self)

            return self.render_result

    return Wrapper


class TupleWrapper(tuple, ResultWrapper):
    """Wrap a tuple."""

    __slots__ = ()

    # This is all magic to be allowed to subclass a tuple.

    def __new__(cls, value: tuple, *, render_result: str | None = None) -> Self:
        """Create a new tuple class."""
        return super().__new__(cls, tuple(value))

    def __init__(self, value: tuple, *, render_result: str | None = None) -> None:
        """Initialize a new tuple class."""
        self.render_result = render_result

    def __str__(self) -> str:
        """Return string representation."""
        if self.render_result is None:
            return super().__str__()

        return self.render_result


_types: tuple[type[dict | list | set], ...] = (dict, list, set)
RESULT_WRAPPERS: dict[type, type] = {kls: gen_result_wrapper(kls) for kls in _types}
RESULT_WRAPPERS[tuple] = TupleWrapper


@lru_cache(maxsize=EVAL_CACHE_SIZE)
def _cached_parse_result(render_result: str) -> Any:
    """Parse a result and cache the result."""
    result = literal_eval(render_result)
    if type(result) in RESULT_WRAPPERS:
        result = RESULT_WRAPPERS[type(result)](result, render_result=render_result)

    # If the literal_eval result is a string, use the original
    # render, by not returning right here. The evaluation of strings
    # resulting in strings impacts quotes, to avoid unexpected
    # output; use the original render instead of the evaluated one.
    # Complex and scientific values are also unexpected. Filter them out.
    if (
        # Filter out string and complex numbers
        not isinstance(result, (str, complex))
        and (
            # Pass if not numeric and not a boolean
            not isinstance(result, (int, float))
            # Or it's a boolean (inherit from int)
            or isinstance(result, bool)
            # Or if it's a digit
            or _IS_NUMERIC.match(render_result) is not None
        )
    ):
        return result

    return render_result


class Template:
    """Class to hold a template and manage caching and rendering."""

    __slots__ = (
        "__weakref__",
        "_compiled",
        "_compiled_code",
        "_hash_cache",
        "_log_fn",
        "_renders",
        "is_static",
        "template",
    )

    def __init__(self, template: str) -> None:
        """Instantiate a template."""

        if not isinstance(template, str):
            raise TypeError("Expected template to be a string")

        self.template: str = template.strip()
        self._compiled_code: CodeType | None = None
        self._compiled: jinja2.Template | None = None
        self.is_static = not is_template_string(template)
        self._log_fn: Callable[[int, str], None] | None = None
        self._hash_cache: int = hash(self.template)
        self._renders: int = 0

    @property
    def _env(self) -> TemplateEnvironment:
        return _NO_HASS_ENV

    def ensure_valid(self) -> None:
        """Return if template is valid."""
        if self.is_static or self._compiled_code is not None:
            return

        if compiled := self._env.template_cache.get(self.template):
            self._compiled_code = compiled
            return

        with _template_context_manager as cm:
            cm.set_template(self.template, "compiling")
            try:
                self._compiled_code = self._env.compile(self.template)
            except jinja2.TemplateError as err:
                raise TemplateError(err) from err

    def render(
        self,
        variables: Mapping[str, Any] | None = None,
        log_fn: Callable[[int, str], None] | None = None,
        **kwargs: Any,
    ) -> Any:
        """Render given template."""
        if self.is_static:
            return self.template

        self._renders += 1

        if self.is_static:
            return self.template

        compiled = self._compiled or self._ensure_compiled(log_fn)

        if variables is not None:
            kwargs.update(variables)

        try:
            render_result = _render_with_context(self.template, compiled, **kwargs)
        except Exception as err:
            raise TemplateError(err) from err

        if len(render_result) > MAX_TEMPLATE_OUTPUT:
            raise TemplateError(
                f"Template output exceeded maximum size of {MAX_TEMPLATE_OUTPUT} characters"
            )

        render_result = render_result.strip()

        return render_result

    def render_with_possible_json_value(
        self,
        value: Any,
        error_value: Any = _SENTINEL,
        variables: dict[str, Any] | None = None
    ) -> Any:
        """Render template with value exposed.

        If valid JSON will expose value_json too.
        """
        if self.is_static:
            return self.template

        self._renders += 1

        if self.is_static:
            return self.template

        compiled = self._compiled or self._ensure_compiled()

        variables = dict(variables or {})
        variables["value"] = value

        try:  # noqa: SIM105 - suppress is much slower
            variables["value_json"] = json.loads(value)
        except json.decoder.JSONDecodeError:
            pass

        try:
            render_result = _render_with_context(
                self.template, compiled, **variables
            ).strip()
        except jinja2.TemplateError as ex:
            if error_value is _SENTINEL:
                _LOGGER.error(
                    "Error parsing value: %s (value: %s, template: %s)",
                    ex,
                    value,
                    self.template,
                )
            return value if error_value is _SENTINEL else error_value

        return render_result

    def _ensure_compiled(
        self,
        log_fn: Callable[[int, str], None] | None = None,
    ) -> jinja2.Template:
        """Bind a template to a specific hass instance."""
        self.ensure_valid()

        assert self._log_fn is None or self._log_fn == log_fn, (
            "can't change custom log function"
        )
        assert self._compiled_code is not None, "template code was not compiled"

        self._log_fn = log_fn
        env = self._env

        self._compiled = jinja2.Template.from_code(
            env, self._compiled_code, env.globals, None
        )

        return self._compiled

    def __eq__(self, other):
        """Compare template with another."""
        return (
            self.__class__ == other.__class__
            and self.template == other.template
        )

    def __hash__(self) -> int:
        """Hash code for template."""
        return self._hash_cache

    def __repr__(self) -> str:
        """Representation of Template."""
        return f"Template<template=({self.template}) renders={self._renders}>"

def forgiving_boolean(
    value: Any, default: object = _SENTINEL
) -> bool | object:
    """Try to convert value to a boolean."""
    try:
        # Import here, not at top-level to avoid circular import
        from . import config_validation as cv  # pylint: disable=import-outside-toplevel

        return cv.boolean(value)
    except vol.Invalid:
        if default is _SENTINEL:
            raise_no_default("bool", value)
        return default


def now() -> datetime:
    return dt_util.now()


def utcnow() -> datetime:
    return dt_util.utcnow()


def raise_no_default(function: str, value: Any) -> NoReturn:
    """Log warning if no default is specified."""
    template, action = template_cv.get() or ("", "rendering or compiling")
    raise ValueError(
        f"Template error: {function} got invalid input '{value}' when {action} template"
        f" '{template}' but no default was specified"
    )


def forgiving_round(value, precision=0, method="common", default=_SENTINEL):
    """Filter to round a value."""
    try:
        # support rounding methods like jinja
        multiplier = float(10**precision)
        if method == "ceil":
            value = math.ceil(float(value) * multiplier) / multiplier
        elif method == "floor":
            value = math.floor(float(value) * multiplier) / multiplier
        elif method == "half":
            value = round(float(value) * 2) / 2
        else:
            # if method is common or something else, use common rounding
            value = round(float(value), precision)
        return int(value) if precision == 0 else value
    except (ValueError, TypeError):
        # If value can't be converted to float
        if default is _SENTINEL:
            raise_no_default("round", value)
        return default


def multiply(value, amount, default=_SENTINEL):
    """Filter to convert value to float and multiply it."""
    try:
        return float(value) * amount
    except (ValueError, TypeError):
        # If value can't be converted to float
        if default is _SENTINEL:
            raise_no_default("multiply", value)
        return default


def add(value, amount, default=_SENTINEL):
    """Filter to convert value to float and add it."""
    try:
        return float(value) + amount
    except (ValueError, TypeError):
        # If value can't be converted to float
        if default is _SENTINEL:
            raise_no_default("add", value)
        return default


def logarithm(value, base=math.e, default=_SENTINEL):
    """Filter and function to get logarithm of the value with a specific base."""
    try:
        base_float = float(base)
    except (ValueError, TypeError):
        if default is _SENTINEL:
            raise_no_default("log", base)
        return default
    try:
        value_float = float(value)
        return math.log(value_float, base_float)
    except (ValueError, TypeError):
        if default is _SENTINEL:
            raise_no_default("log", value)
        return default


def sine(value, default=_SENTINEL):
    """Filter and function to get sine of the value."""
    try:
        return math.sin(float(value))
    except (ValueError, TypeError):
        if default is _SENTINEL:
            raise_no_default("sin", value)
        return default


def cosine(value, default=_SENTINEL):
    """Filter and function to get cosine of the value."""
    try:
        return math.cos(float(value))
    except (ValueError, TypeError):
        if default is _SENTINEL:
            raise_no_default("cos", value)
        return default


def tangent(value, default=_SENTINEL):
    """Filter and function to get tangent of the value."""
    try:
        return math.tan(float(value))
    except (ValueError, TypeError):
        if default is _SENTINEL:
            raise_no_default("tan", value)
        return default


def arc_sine(value, default=_SENTINEL):
    """Filter and function to get arc sine of the value."""
    try:
        return math.asin(float(value))
    except (ValueError, TypeError):
        if default is _SENTINEL:
            raise_no_default("asin", value)
        return default


def arc_cosine(value, default=_SENTINEL):
    """Filter and function to get arc cosine of the value."""
    try:
        return math.acos(float(value))
    except (ValueError, TypeError):
        if default is _SENTINEL:
            raise_no_default("acos", value)
        return default


def arc_tangent(value, default=_SENTINEL):
    """Filter and function to get arc tangent of the value."""
    try:
        return math.atan(float(value))
    except (ValueError, TypeError):
        if default is _SENTINEL:
            raise_no_default("atan", value)
        return default


def arc_tangent2(*args, default=_SENTINEL):
    """Filter and function to calculate four quadrant arc tangent of y / x.

    The parameters to atan2 may be passed either in an iterable or as separate arguments
    The default value may be passed either as a positional or in a keyword argument
    """
    try:
        if 1 <= len(args) <= 2 and isinstance(args[0], (list, tuple)):
            if len(args) == 2 and default is _SENTINEL:
                # Default value passed as a positional argument
                default = args[1]
            args = args[0]
        elif len(args) == 3 and default is _SENTINEL:
            # Default value passed as a positional argument
            default = args[2]

        return math.atan2(float(args[0]), float(args[1]))
    except (ValueError, TypeError):
        if default is _SENTINEL:
            raise_no_default("atan2", args)
        return default


def version(value):
    """Filter and function to get version object of the value."""
    return AwesomeVersion(value)


def square_root(value, default=_SENTINEL):
    """Filter and function to get square root of the value."""
    try:
        return math.sqrt(float(value))
    except (ValueError, TypeError):
        if default is _SENTINEL:
            raise_no_default("sqrt", value)
        return default


def timestamp_custom(value, date_format=DATE_STR_FORMAT, local=True, default=_SENTINEL):
    """Filter to convert given timestamp to format."""
    try:
        result = dt_util.utc_from_timestamp(value)

        if local:
            result = dt_util.as_local(result)

        return result.strftime(date_format)
    except (ValueError, TypeError):
        # If timestamp can't be converted
        if default is _SENTINEL:
            raise_no_default("timestamp_custom", value)
        return default


def timestamp_local(value, default=_SENTINEL):
    """Filter to convert given timestamp to local date/time."""
    try:
        return dt_util.as_local(dt_util.utc_from_timestamp(value)).isoformat()
    except (ValueError, TypeError):
        # If timestamp can't be converted
        if default is _SENTINEL:
            raise_no_default("timestamp_local", value)
        return default


def timestamp_utc(value, default=_SENTINEL):
    """Filter to convert given timestamp to UTC date/time."""
    try:
        return dt_util.utc_from_timestamp(value).isoformat()
    except (ValueError, TypeError):
        # If timestamp can't be converted
        if default is _SENTINEL:
            raise_no_default("timestamp_utc", value)
        return default


def forgiving_as_timestamp(value, default=_SENTINEL):
    """Filter and function which tries to convert value to timestamp."""
    try:
        return dt_util.as_timestamp(value)
    except (ValueError, TypeError):
        if default is _SENTINEL:
            raise_no_default("as_timestamp", value)
        return default


def as_datetime(value: Any, default: Any = _SENTINEL) -> Any:
    """Filter and to convert a time string or UNIX timestamp to datetime object."""
    # Return datetime.datetime object without changes
    if type(value) is datetime:
        return value
    # Add midnight to datetime.date object
    if type(value) is date:
        return datetime.combine(value, time(0, 0, 0))
    try:
        # Check for a valid UNIX timestamp string, int or float
        timestamp = float(value)
        return dt_util.utc_from_timestamp(timestamp)
    except (ValueError, TypeError):
        # Try to parse datetime string to datetime object
        try:
            return dt_util.parse_datetime(value, raise_on_error=True)
        except (ValueError, TypeError):
            if default is _SENTINEL:
                # Return None on string input
                # to ensure backwards compatibility with HA Core 2024.1 and before.
                if isinstance(value, str):
                    return None
                raise_no_default("as_datetime", value)
            return default


def as_timedelta(value: str) -> timedelta | None:
    """Parse a ISO8601 duration like 'PT10M' to a timedelta."""
    return dt_util.parse_duration(value)


def strptime(string, fmt, default=_SENTINEL):
    """Parse a time string to datetime."""
    try:
        return datetime.strptime(string, fmt)
    except (ValueError, AttributeError, TypeError):
        if default is _SENTINEL:
            raise_no_default("strptime", string)
        return default


def fail_when_undefined(value):
    """Filter to force a failure when the value is undefined."""
    if isinstance(value, jinja2.Undefined):
        value()
    return value


def min_max_from_filter(builtin_filter: Any, name: str) -> Any:
    """Convert a built-in min/max Jinja filter to a global function.

    The parameters may be passed as an iterable or as separate arguments.
    """

    @pass_environment
    @wraps(builtin_filter)
    def wrapper(environment: jinja2.Environment, *args: Any, **kwargs: Any) -> Any:
        if len(args) == 0:
            raise TypeError(f"{name} expected at least 1 argument, got 0")

        if len(args) == 1:
            if isinstance(args[0], Iterable):
                return builtin_filter(environment, args[0], **kwargs)

            raise TypeError(f"'{type(args[0]).__name__}' object is not iterable")

        return builtin_filter(environment, args, **kwargs)

    return pass_environment(wrapper)


def average(*args: Any, default: Any = _SENTINEL) -> Any:
    """Filter and function to calculate the arithmetic mean.

    Calculates of an iterable or of two or more arguments.

    The parameters may be passed as an iterable or as separate arguments.
    """
    if len(args) == 0:
        raise TypeError("average expected at least 1 argument, got 0")

    # If first argument is iterable and more than 1 argument provided but not a named
    # default, then use 2nd argument as default.
    if isinstance(args[0], Iterable):
        average_list = args[0]
        if len(args) > 1 and default is _SENTINEL:
            default = args[1]
    elif len(args) == 1:
        raise TypeError(f"'{type(args[0]).__name__}' object is not iterable")
    else:
        average_list = args

    try:
        return statistics.fmean(average_list)
    except (TypeError, statistics.StatisticsError):
        if default is _SENTINEL:
            raise_no_default("average", args)
        return default


def median(*args: Any, default: Any = _SENTINEL) -> Any:
    """Filter and function to calculate the median.

    Calculates median of an iterable of two or more arguments.

    The parameters may be passed as an iterable or as separate arguments.
    """
    if len(args) == 0:
        raise TypeError("median expected at least 1 argument, got 0")

    # If first argument is a list or tuple and more than 1 argument provided but not a named
    # default, then use 2nd argument as default.
    if isinstance(args[0], Iterable):
        median_list = args[0]
        if len(args) > 1 and default is _SENTINEL:
            default = args[1]
    elif len(args) == 1:
        raise TypeError(f"'{type(args[0]).__name__}' object is not iterable")
    else:
        median_list = args

    try:
        return statistics.median(median_list)
    except (TypeError, statistics.StatisticsError):
        if default is _SENTINEL:
            raise_no_default("median", args)
        return default


def statistical_mode(*args: Any, default: Any = _SENTINEL) -> Any:
    """Filter and function to calculate the statistical mode.

    Calculates mode of an iterable of two or more arguments.

    The parameters may be passed as an iterable or as separate arguments.
    """
    if not args:
        raise TypeError("statistical_mode expected at least 1 argument, got 0")

    # If first argument is a list or tuple and more than 1 argument provided but not a named
    # default, then use 2nd argument as default.
    if len(args) == 1 and isinstance(args[0], Iterable):
        mode_list = args[0]
    elif isinstance(args[0], list | tuple):
        mode_list = args[0]
        if len(args) > 1 and default is _SENTINEL:
            default = args[1]
    elif len(args) == 1:
        raise TypeError(f"'{type(args[0]).__name__}' object is not iterable")
    else:
        mode_list = args

    try:
        return statistics.mode(mode_list)
    except (TypeError, statistics.StatisticsError):
        if default is _SENTINEL:
            raise_no_default("statistical_mode", args)
        return default


def forgiving_float(value, default=_SENTINEL):
    """Try to convert value to a float."""
    try:
        return float(value)
    except (ValueError, TypeError):
        if default is _SENTINEL:
            raise_no_default("float", value)
        return default


def forgiving_float_filter(value, default=_SENTINEL):
    """Try to convert value to a float."""
    try:
        return float(value)
    except (ValueError, TypeError):
        if default is _SENTINEL:
            raise_no_default("float", value)
        return default


def forgiving_int(value, default=_SENTINEL, base=10):
    """Try to convert value to an int, and raise if it fails."""
    result = jinja2.filters.do_int(value, default=default, base=base)
    if result is _SENTINEL:
        raise_no_default("int", value)
    return result


def forgiving_int_filter(value, default=_SENTINEL, base=10):
    """Try to convert value to an int, and raise if it fails."""
    result = jinja2.filters.do_int(value, default=default, base=base)
    if result is _SENTINEL:
        raise_no_default("int", value)
    return result


def is_number(value):
    """Try to convert value to a float."""
    try:
        fvalue = float(value)
    except (ValueError, TypeError):
        return False
    if not math.isfinite(fvalue):
        return False
    return True


def _is_list(value: Any) -> bool:
    """Return whether a value is a list."""
    return isinstance(value, list)


def _is_set(value: Any) -> bool:
    """Return whether a value is a set."""
    return isinstance(value, set)


def _is_tuple(value: Any) -> bool:
    """Return whether a value is a tuple."""
    return isinstance(value, tuple)


def _to_set(value: Any) -> set[Any]:
    """Convert value to set."""
    return set(value)


def _to_tuple(value):
    """Convert value to tuple."""
    return tuple(value)


def _is_datetime(value: Any) -> bool:
    """Return whether a value is a datetime."""
    return isinstance(value, datetime)


def _is_string_like(value: Any) -> bool:
    """Return whether a value is a string or string like object."""
    return isinstance(value, (str, bytes, bytearray))


def regex_match(value, find="", ignorecase=False):
    """Match value using regex."""
    if not isinstance(value, str):
        value = str(value)
    flags = re.IGNORECASE if ignorecase else 0
    return bool(_regex_cache(find, flags).match(value))


_regex_cache = lru_cache(maxsize=128)(re.compile)


def regex_replace(value="", find="", replace="", ignorecase=False):
    """Replace using regex."""
    if not isinstance(value, str):
        value = str(value)
    flags = re.IGNORECASE if ignorecase else 0
    return _regex_cache(find, flags).sub(replace, value)


def regex_search(value, find="", ignorecase=False):
    """Search using regex."""
    if not isinstance(value, str):
        value = str(value)
    flags = re.IGNORECASE if ignorecase else 0
    return bool(_regex_cache(find, flags).search(value))


def regex_findall_index(value, find="", index=0, ignorecase=False):
    """Find all matches using regex and then pick specific match index."""
    return regex_findall(value, find, ignorecase)[index]


def regex_findall(value, find="", ignorecase=False):
    """Find all matches using regex."""
    if not isinstance(value, str):
        value = str(value)
    flags = re.IGNORECASE if ignorecase else 0
    return _regex_cache(find, flags).findall(value)


def bitwise_and(first_value, second_value):
    """Perform a bitwise and operation."""
    return first_value & second_value


def bitwise_or(first_value, second_value):
    """Perform a bitwise or operation."""
    return first_value | second_value


def bitwise_xor(first_value, second_value):
    """Perform a bitwise xor operation."""
    return first_value ^ second_value


def struct_pack(value: Any | None, format_string: str) -> bytes | None:
    """Pack an object into a bytes object."""
    try:
        return pack(format_string, value)
    except StructError:
        _LOGGER.warning(
            (
                "Template warning: 'pack' unable to pack object '%s' with type '%s' and"
                " format_string '%s' see https://docs.python.org/3/library/struct.html"
                " for more information"
            ),
            str(value),
            type(value).__name__,
            format_string,
        )
        return None


def struct_unpack(value: bytes, format_string: str, offset: int = 0) -> Any | None:
    """Unpack an object from bytes an return the first native object."""
    try:
        return unpack_from(format_string, value, offset)[0]
    except StructError:
        _LOGGER.warning(
            (
                "Template warning: 'unpack' unable to unpack object '%s' with"
                " format_string '%s' and offset %s see"
                " https://docs.python.org/3/library/struct.html for more information"
            ),
            value,
            format_string,
            offset,
        )
        return None


def base64_encode(value: str) -> str:
    """Perform base64 encode."""
    return base64.b64encode(value.encode("utf-8")).decode("utf-8")


def base64_decode(value: str, encoding: str | None = "utf-8") -> str | bytes:
    """Perform base64 decode."""
    decoded = base64.b64decode(value)
    if encoding:
        return decoded.decode(encoding)

    return decoded


def ordinal(value):
    """Perform ordinal conversion."""
    suffixes = ["th", "st", "nd", "rd"] + ["th"] * 6  # codespell:ignore nd
    return str(value) + (
        suffixes[(int(str(value)[-1])) % 10]
        if int(str(value)[-2:]) % 100 not in range(11, 14)
        else "th"
    )


def from_json(value):
    """Convert a JSON string to an object."""
    return json.loads(value)


def to_json(
    value: Any,
    ensure_ascii: bool = False,
    pretty_print: bool = False,
    sort_keys: bool = False,
) -> str:
    """Convert an object to a JSON string."""
    return json.dumps(
        value,
        ensure_ascii=ensure_ascii,
        indent=2 if pretty_print else None,
        sort_keys=sort_keys,
    )


@pass_context
def random_every_time(context, values):
    """Choose a random value.

    Unlike Jinja's random filter,
    this is context-dependent to avoid caching the chosen value.
    """
    return random.choice(values)


def today_at(time_str: str = "") -> datetime:
    today = dt_util.start_of_local_day()
    if not time_str:
        return today

    if (time_today := dt_util.parse_time(time_str)) is None:
        raise ValueError(
            f"could not convert {type(time_str).__name__} to datetime: '{time_str}'"
        )

    return datetime.combine(today, time_today, today.tzinfo)


def relative_time(value: Any) -> Any:
    """Take a datetime and return its "age" as a string.

    The age can be in second, minute, hour, day, month or year. Only the
    biggest unit is considered, e.g. if it's 2 days and 3 hours, "2 days" will
    be returned.
    If the input datetime is in the future,
    the input datetime will be returned.

    If the input are not a datetime object the input will be returned unmodified.

    Note: This template function is deprecated in favor of `time_until`, but is still
    supported so as not to break old templates.
    """

    if not isinstance(value, datetime):
        return value
    if not value.tzinfo:
        value = dt_util.as_local(value)
    if dt_util.now() < value:
        return value
    return dt_util.get_age(value)


def time_since(value: Any | datetime, precision: int = 1) -> Any:
    """Take a datetime and return its "age" as a string.

    The age can be in seconds, minutes, hours, days, months and year.

    precision is the number of units to return, with the last unit rounded.

    If the value not a datetime object the input will be returned unmodified.
    """
    if not isinstance(value, datetime):
        return value
    if not value.tzinfo:
        value = dt_util.as_local(value)
    if dt_util.now() < value:
        return value

    return dt_util.get_age(value, precision)


def time_until(value: Any | datetime, precision: int = 1) -> Any:
    """Take a datetime and return the amount of time until that time as a string.

    The time until can be in seconds, minutes, hours, days, months and years.

    precision is the number of units to return, with the last unit rounded.

    If the value not a datetime object the input will be returned unmodified.
    """
    if not isinstance(value, datetime):
        return value
    if not value.tzinfo:
        value = dt_util.as_local(value)
    if dt_util.now() > value:
        return value

    return dt_util.get_time_remaining(value, precision)


def urlencode(value):
    """Urlencode dictionary and return as UTF-8 string."""
    return urllib_urlencode(value).encode("utf-8")


def slugify(value, separator="_"):
    """Convert a string into a slug, such as what is used for entity ids."""
    return slugify_util(value, separator=separator)


def iif(
    value: Any, if_true: Any = True, if_false: Any = False, if_none: Any = _SENTINEL
) -> Any:
    """Immediate if function/filter that allow for common if/else constructs.

    https://en.wikipedia.org/wiki/IIf

    Examples:
        {{ is_state("device_tracker.frenck", "home") | iif("yes", "no") }}
        {{ iif(1==2, "yes", "no") }}
        {{ (1 == 1) | iif("yes", "no") }}

    """
    if value is None and if_none is not _SENTINEL:
        return if_none
    if bool(value):
        return if_true
    return if_false


def shuffle(*args: Any, seed: Any = None) -> MutableSequence[Any]:
    """Shuffle a list, either with a seed or without."""
    if not args:
        raise TypeError("shuffle expected at least 1 argument, got 0")

    # If first argument is iterable and more than 1 argument provided
    # but not a named seed, then use 2nd argument as seed.
    if isinstance(args[0], Iterable):
        items = list(args[0])
        if len(args) > 1 and seed is None:
            seed = args[1]
    elif len(args) == 1:
        raise TypeError(f"'{type(args[0]).__name__}' object is not iterable")
    else:
        items = list(args)

    if seed:
        r = random.Random(seed)
        r.shuffle(items)
    else:
        random.shuffle(items)
    return items


def typeof(value: Any) -> Any:
    """Return the type of value passed to debug types."""
    return value.__class__.__name__


def flatten(value: Iterable[Any], levels: int | None = None) -> list[Any]:
    """Flattens list of lists."""
    if not isinstance(value, Iterable) or isinstance(value, str):
        raise TypeError(f"flatten expected a list, got {type(value).__name__}")

    flattened: list[Any] = []
    for item in value:
        if isinstance(item, Iterable) and not isinstance(item, str):
            if levels is None:
                flattened.extend(flatten(item))
            elif levels >= 1:
                flattened.extend(flatten(item, levels=(levels - 1)))
            else:
                flattened.append(item)
        else:
            flattened.append(item)
    return flattened


def intersect(value: Iterable[Any], other: Iterable[Any]) -> list[Any]:
    """Return the common elements between two lists."""
    if not isinstance(value, Iterable) or isinstance(value, str):
        raise TypeError(f"intersect expected a list, got {type(value).__name__}")
    if not isinstance(other, Iterable) or isinstance(other, str):
        raise TypeError(f"intersect expected a list, got {type(other).__name__}")

    return list(set(value) & set(other))


def difference(value: Iterable[Any], other: Iterable[Any]) -> list[Any]:
    """Return elements in first list that are not in second list."""
    if not isinstance(value, Iterable) or isinstance(value, str):
        raise TypeError(f"difference expected a list, got {type(value).__name__}")
    if not isinstance(other, Iterable) or isinstance(other, str):
        raise TypeError(f"difference expected a list, got {type(other).__name__}")

    return list(set(value) - set(other))


def union(value: Iterable[Any], other: Iterable[Any]) -> list[Any]:
    """Return all unique elements from both lists combined."""
    if not isinstance(value, Iterable) or isinstance(value, str):
        raise TypeError(f"union expected a list, got {type(value).__name__}")
    if not isinstance(other, Iterable) or isinstance(other, str):
        raise TypeError(f"union expected a list, got {type(other).__name__}")

    return list(set(value) | set(other))


def symmetric_difference(value: Iterable[Any], other: Iterable[Any]) -> list[Any]:
    """Return elements that are in either list but not in both."""
    if not isinstance(value, Iterable) or isinstance(value, str):
        raise TypeError(
            f"symmetric_difference expected a list, got {type(value).__name__}"
        )
    if not isinstance(other, Iterable) or isinstance(other, str):
        raise TypeError(
            f"symmetric_difference expected a list, got {type(other).__name__}"
        )

    return list(set(value) ^ set(other))


def combine(*args: Any, recursive: bool = False) -> dict[Any, Any]:
    """Combine multiple dictionaries into one."""
    if not args:
        raise TypeError("combine expected at least 1 argument, got 0")

    result: dict[Any, Any] = {}
    for arg in args:
        if not isinstance(arg, dict):
            raise TypeError(f"combine expected a dict, got {type(arg).__name__}")

        if recursive:
            for key, value in arg.items():
                if (
                    key in result
                    and isinstance(result[key], dict)
                    and isinstance(value, dict)
                ):
                    result[key] = combine(result[key], value, recursive=True)
                else:
                    result[key] = value
        else:
            result |= arg

    return result


def md5(value: str) -> str:
    """Generate md5 hash from a string."""
    return hashlib.md5(value.encode()).hexdigest()


def sha1(value: str) -> str:
    """Generate sha1 hash from a string."""
    return hashlib.sha1(value.encode()).hexdigest()


def sha256(value: str) -> str:
    """Generate sha256 hash from a string."""
    return hashlib.sha256(value.encode()).hexdigest()


def sha512(value: str) -> str:
    """Generate sha512 hash from a string."""
    return hashlib.sha512(value.encode()).hexdigest()


class TemplateContextManager(AbstractContextManager):
    """Context manager to store template being parsed or rendered in a ContextVar."""

    def set_template(self, template_str: str, action: str) -> None:
        """Store template being parsed or rendered in a Contextvar to aid error handling."""
        template_cv.set((template_str, action))

    def __exit__(
        self,
        exc_type: type[BaseException] | None,
        exc_value: BaseException | None,
        traceback: TracebackType | None,
    ) -> None:
        """Raise any exception triggered within the runtime context."""
        template_cv.set(None)


_template_context_manager = TemplateContextManager()


def _render_with_context(
    template_str: str, template: jinja2.Template, **kwargs: Any
) -> str:
    """Store template being rendered in a ContextVar to aid error handling."""
    with _template_context_manager as cm:
        cm.set_template(template_str, "rendering")
        return template.render(**kwargs)

class TemplateEnvironment(ImmutableSandboxedEnvironment):
    """The Home Assistant template environment."""

    def __init__(
        self,
    ) -> None:
        """Initialise template environment."""
        super().__init__(undefined=jinja2.StrictUndefined)
        self.template_cache: weakref.WeakValueDictionary[
            str | jinja2.nodes.Template, CodeType | None
        ] = weakref.WeakValueDictionary()
        self.add_extension("jinja2.ext.loopcontrols")

        self.globals["acos"] = arc_cosine
        self.globals["as_datetime"] = as_datetime
        self.globals["as_local"] = dt_util.as_local
        self.globals["as_timedelta"] = as_timedelta
        self.globals["as_timestamp"] = forgiving_as_timestamp
        self.globals["asin"] = arc_sine
        self.globals["atan"] = arc_tangent
        self.globals["atan2"] = arc_tangent2
        self.globals["average"] = average
        self.globals["bool"] = forgiving_boolean
        self.globals["combine"] = combine
        self.globals["cos"] = cosine
        self.globals["difference"] = difference
        self.globals["e"] = math.e
        self.globals["flatten"] = flatten
        self.globals["float"] = forgiving_float
        self.globals["iif"] = iif
        self.globals["int"] = forgiving_int
        self.globals["intersect"] = intersect
        self.globals["is_number"] = is_number
        self.globals["log"] = logarithm
        self.globals["max"] = min_max_from_filter(self.filters["max"], "max")
        self.globals["md5"] = md5
        self.globals["median"] = median
        self.globals["min"] = min_max_from_filter(self.filters["min"], "min")
        self.globals["pack"] = struct_pack
        self.globals["pi"] = math.pi
        self.globals["set"] = _to_set
        self.globals["sha1"] = sha1
        self.globals["sha256"] = sha256
        self.globals["sha512"] = sha512
        self.globals["shuffle"] = shuffle
        self.globals["sin"] = sine
        self.globals["slugify"] = slugify
        self.globals["sqrt"] = square_root
        self.globals["statistical_mode"] = statistical_mode
        self.globals["strptime"] = strptime
        self.globals["symmetric_difference"] = symmetric_difference
        self.globals["tan"] = tangent
        self.globals["tau"] = math.pi * 2
        self.globals["timedelta"] = timedelta
        self.globals["tuple"] = _to_tuple
        self.globals["typeof"] = typeof
        self.globals["union"] = union
        self.globals["unpack"] = struct_unpack
        self.globals["urlencode"] = urlencode
        self.globals["version"] = version
        self.globals["zip"] = zip

        self.filters["acos"] = arc_cosine
        self.filters["add"] = add
        self.filters["as_datetime"] = as_datetime
        self.filters["as_local"] = dt_util.as_local
        self.filters["as_timedelta"] = as_timedelta
        self.filters["as_timestamp"] = forgiving_as_timestamp
        self.filters["asin"] = arc_sine
        self.filters["atan"] = arc_tangent
        self.filters["atan2"] = arc_tangent2
        self.filters["average"] = average
        self.filters["base64_decode"] = base64_decode
        self.filters["base64_encode"] = base64_encode
        self.filters["bitwise_and"] = bitwise_and
        self.filters["bitwise_or"] = bitwise_or
        self.filters["bitwise_xor"] = bitwise_xor
        self.filters["bool"] = forgiving_boolean
        self.filters["combine"] = combine
        self.filters["contains"] = contains
        self.filters["cos"] = cosine
        self.filters["difference"] = difference
        self.filters["flatten"] = flatten
        self.filters["float"] = forgiving_float_filter
        self.filters["from_json"] = from_json
        self.filters["iif"] = iif
        self.filters["int"] = forgiving_int_filter
        self.filters["intersect"] = intersect
        self.filters["is_defined"] = fail_when_undefined
        self.filters["is_number"] = is_number
        self.filters["log"] = logarithm
        self.filters["md5"] = md5
        self.filters["median"] = median
        self.filters["multiply"] = multiply
        self.filters["ord"] = ord
        self.filters["ordinal"] = ordinal
        self.filters["pack"] = struct_pack
        self.filters["random"] = random_every_time
        self.filters["regex_findall_index"] = regex_findall_index
        self.filters["regex_findall"] = regex_findall
        self.filters["regex_match"] = regex_match
        self.filters["regex_replace"] = regex_replace
        self.filters["regex_search"] = regex_search
        self.filters["round"] = forgiving_round
        self.filters["sha1"] = sha1
        self.filters["sha256"] = sha256
        self.filters["sha512"] = sha512
        self.filters["shuffle"] = shuffle
        self.filters["sin"] = sine
        self.filters["slugify"] = slugify
        self.filters["sqrt"] = square_root
        self.filters["statistical_mode"] = statistical_mode
        self.filters["symmetric_difference"] = symmetric_difference
        self.filters["tan"] = tangent
        self.filters["timestamp_custom"] = timestamp_custom
        self.filters["timestamp_local"] = timestamp_local
        self.filters["timestamp_utc"] = timestamp_utc
        self.filters["to_json"] = to_json
        self.filters["typeof"] = typeof
        self.filters["union"] = union
        self.filters["unpack"] = struct_unpack
        self.filters["version"] = version

        self.tests["contains"] = contains
        self.tests["datetime"] = _is_datetime
        self.tests["is_number"] = is_number
        self.tests["list"] = _is_list
        self.tests["match"] = regex_match
        self.tests["search"] = regex_search
        self.tests["set"] = _is_set
        self.tests["string_like"] = _is_string_like
        self.tests["tuple"] = _is_tuple

    def is_safe_attribute(self, obj, attr, value):
        """Test if attribute is safe."""
        if isinstance(
            obj, (LoopContext, AsyncLoopContext)
        ):
            return attr[0] != "_"

        if isinstance(obj, Namespace):
            return True

        return super().is_safe_attribute(obj, attr, value)

    @overload
    def compile(
        self,
        source: str | jinja2.nodes.Template,
        name: str | None = None,
        filename: str | None = None,
        raw: Literal[False] = False,
        defer_init: bool = False,
    ) -> CodeType: ...

    @overload
    def compile(
        self,
        source: str | jinja2.nodes.Template,
        name: str | None = None,
        filename: str | None = None,
        raw: Literal[True] = ...,
        defer_init: bool = False,
    ) -> str: ...

    def compile(
        self,
        source: str | jinja2.nodes.Template,
    ) -> CodeType | str:
        """Compile the template."""

        compiled = super().compile(source)
        self.template_cache[source] = compiled
        return compiled


_NO_HASS_ENV = TemplateEnvironment()