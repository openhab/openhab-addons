"""JSON utility functions."""

from __future__ import annotations

import json
from typing import Any

def json_loads_object(obj: bytes | bytearray | memoryview | str, /) -> dict[str, Any]:
    """Parse JSON data and ensure result is a dictionary."""
    value = json.loads(obj)
    # Avoid isinstance overhead as we are not interested in dict subclasses
    if type(value) is dict:
        return value
    raise ValueError(f"Expected JSON to be parsed as a dict got {type(value)}")
