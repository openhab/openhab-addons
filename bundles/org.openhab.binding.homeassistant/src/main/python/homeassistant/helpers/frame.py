"""Provide frame helper for finding the current frame context."""

from __future__ import annotations

from dataclasses import dataclass
from  functools import cached_property
import linecache
import logging
import sys
from types import FrameType

from homeassistant.exceptions import HomeAssistantError


@dataclass(kw_only=True)
class IntegrationFrame:
    """Integration frame container."""

    custom_integration: bool
    integration: str
    module: str | None
    relative_filename: str
    frame: FrameType

    @cached_property
    def line_number(self) -> int:
        """Return the line number of the frame."""
        return self.frame.f_lineno

    @cached_property
    def filename(self) -> str:
        """Return the filename of the frame."""
        return self.frame.f_code.co_filename

    @cached_property
    def line(self) -> str:
        """Return the line of the frame."""
        return (linecache.getline(self.filename, self.line_number) or "?").strip()


def get_integration_logger(fallback_name: str) -> logging.Logger:
    """Return a logger by checking the current integration frame.

    If Python is unable to access the sources files, the call stack frame
    will be missing information, so let's guard by requiring a fallback name.
    https://github.com/home-assistant/core/issues/24982
    """
    try:
        integration_frame = get_integration_frame()
    except MissingIntegrationFrame:
        return logging.getLogger(fallback_name)

    if integration_frame.custom_integration:
        logger_name = f"custom_components.{integration_frame.integration}"
    else:
        logger_name = f"homeassistant.components.{integration_frame.integration}"

    return logging.getLogger(logger_name)

def get_current_frame(depth: int = 0) -> FrameType:
    """Return the current frame."""
    # Add one to depth since get_current_frame is included
    return sys._getframe(depth + 1)  # noqa: SLF001


def get_integration_frame(exclude_integrations: set | None = None) -> IntegrationFrame:
    """Return the frame, integration and integration path of the current stack frame."""
    found_frame = None
    if not exclude_integrations:
        exclude_integrations = set()

    frame: FrameType | None = get_current_frame()
    while frame is not None:
        filename = frame.f_code.co_filename

        for path in ("custom_components/", "homeassistant/components/"):
            try:
                index = filename.index(path)
                start = index + len(path)
                end = filename.index("/", start)
                integration = filename[start:end]
                if integration not in exclude_integrations:
                    found_frame = frame

                break
            except ValueError:
                continue

        if found_frame is not None:
            break

        frame = frame.f_back

    if found_frame is None:
        raise MissingIntegrationFrame

    found_module: str | None = None
    for module, module_obj in dict(sys.modules).items():
        if not hasattr(module_obj, "__file__"):
            continue
        if module_obj.__file__ == found_frame.f_code.co_filename:
            found_module = module
            break

    return IntegrationFrame(
        custom_integration=path == "custom_components/",
        integration=integration,
        module=found_module,
        relative_filename=found_frame.f_code.co_filename[index:],
        frame=found_frame,
    )


class MissingIntegrationFrame(HomeAssistantError):
    """Raised when no integration is found in the frame."""
