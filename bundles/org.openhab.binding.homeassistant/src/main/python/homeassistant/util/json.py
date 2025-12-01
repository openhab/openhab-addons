"""JSON utility functions."""

from __future__ import annotations

import json

type JsonValueType = (
    dict[str, JsonValueType] | list[JsonValueType] | str | int | float | bool | None
)

def json_loads_object(obj: bytes | bytearray | memoryview | str, /) -> JsonValueType:
    """Parse JSON data and ensure result is a dictionary."""
    value = json.loads(obj)
    # Avoid isinstance overhead as we are not interested in dict subclasses
    if type(value) is dict:
        return value
    raise ValueError(f"Expected JSON to be parsed as a dict got {type(value)}")
