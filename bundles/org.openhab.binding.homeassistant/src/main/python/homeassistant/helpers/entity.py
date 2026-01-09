"""An abstract class for entities."""

from __future__ import annotations

from typing import Final

import voluptuous as vol

from homeassistant.const import (
    EntityCategory,
)

ENTITY_CATEGORIES_SCHEMA: Final = vol.Coerce(EntityCategory)
