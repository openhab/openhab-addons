"""Provides functionality to interact with lights."""

from __future__ import annotations

from collections.abc import Iterable

import voluptuous as vol

from .const import (  # noqa: F401
    COLOR_MODES_COLOR,
    VALID_COLOR_MODES,
    ColorMode
)

def valid_supported_color_modes(
    color_modes: Iterable,
) -> set:
    """Validate the given color modes."""
    color_modes = set(color_modes)
    if (
        not color_modes
        or ColorMode.UNKNOWN in color_modes
        or (ColorMode.BRIGHTNESS in color_modes and len(color_modes) > 1)
        or (ColorMode.ONOFF in color_modes and len(color_modes) > 1)
        or (ColorMode.WHITE in color_modes and not color_supported(color_modes))
    ):
        raise vol.Error(f"Invalid supported_color_modes {sorted(color_modes)}")
    return color_modes

def color_supported(color_modes: Iterable | None) -> bool:
    """Test if color is supported."""
    if not color_modes:
        return False
    return not COLOR_MODES_COLOR.isdisjoint(color_modes)
