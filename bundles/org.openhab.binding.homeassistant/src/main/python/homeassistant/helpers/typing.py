"""Typing Helpers for Home Assistant."""

from collections.abc import Mapping
from typing import Any

type ConfigType = dict[str, Any]
type DiscoveryInfoType = dict[str, Any]
type TemplateVarsType = Mapping[str, Any] | None
