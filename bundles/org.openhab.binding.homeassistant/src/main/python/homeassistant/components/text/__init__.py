"""Component to allow setting text as platforms."""

from __future__ import annotations

from enum import StrEnum

class TextMode(StrEnum):
    """Modes for text entities."""

    PASSWORD = "password"
    TEXT = "text"
