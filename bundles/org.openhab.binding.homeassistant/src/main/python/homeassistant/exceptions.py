"""The exceptions used by Home Assistant."""

from __future__ import annotations


class HomeAssistantError(Exception):
    """General Home Assistant exception occurred."""


class ServiceValidationError(HomeAssistantError):
    """A validation exception occurred when calling a service."""


class TemplateError(HomeAssistantError):
    """Error during template rendering."""

    def __init__(self, exception: Exception | str) -> None:
        """Init the error."""
        if isinstance(exception, str):
            super().__init__(exception)
        else:
            super().__init__(f"{exception.__class__.__name__}: {exception}")
