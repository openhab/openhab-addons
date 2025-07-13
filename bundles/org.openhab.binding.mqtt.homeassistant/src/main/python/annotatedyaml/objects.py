"""Custom yaml object types."""

from __future__ import annotations

from typing import Any

import voluptuous as vol
from voluptuous.schema_builder import _compile_scalar

class NodeStrClass(str):
    """Wrapper class to be able to add attributes on a string."""

    __slots__ = ("__config_file__", "__line__")

    __config_file__: str
    __line__: int | str

    def __voluptuous_compile__(self, schema: vol.Schema) -> Any:
        """Needed because vol.Schema.compile does not handle str subclasses."""
        return _compile_scalar(self)