"""Models used by multiple MQTT modules."""

from __future__ import annotations

from ast import literal_eval
from collections.abc import Mapping
from enum import StrEnum
import logging
from typing import Any

from homeassistant.exceptions import ServiceValidationError, TemplateError
from homeassistant.helpers import template

from .const import TEMPLATE_ERRORS

class PayloadSentinel(StrEnum):
    """Sentinel for `render_with_possible_json_value`."""

    NONE = "none"
    DEFAULT = "default"


_LOGGER = logging.getLogger(__name__)


def convert_outgoing_mqtt_payload(
    payload: str | bytes | int | float | None,
) -> str | bytes | int | float | None:
    """Ensure correct raw MQTT payload is passed as bytes for publishing."""
    if isinstance(payload, str) and payload.startswith(("b'", 'b"')):
        try:
            native_object = literal_eval(payload)
        except (ValueError, TypeError, SyntaxError, MemoryError):
            pass
        else:
            if isinstance(native_object, bytes):
                return native_object

    return payload


class MqttCommandTemplateException(ServiceValidationError):
    """Handle MqttCommandTemplate exceptions."""

    _message: str

    def __init__(
        self,
        *args: object,
        base_exception: Exception,
        command_template: str,
        value: str | bytes | int | float | None,
    ) -> None:
        """Initialize exception."""
        super().__init__(base_exception, *args)
        value_log = str(value)
        self._message = (
            f"{type(base_exception).__name__}: {base_exception} rendering template"
            f", template: '{command_template}' and payload: {value_log}"
        )

    def __str__(self) -> str:
        """Return exception message string."""
        return self._message


class MqttCommandTemplate:
    """Class for rendering MQTT payload with command templates."""

    def __init__(
        self,
        command_template: template.Template | None,
    ) -> None:
        """Instantiate a command template."""
        self._template_state: template.TemplateStateFromEntityId | None = None
        self._command_template = command_template

    def render(
        self,
        value: str | bytes | int | float | None = None,
        variables: Mapping[str, Any] | None = None,
    ) -> str | bytes | int | float | None:
        """Render or convert the command template with given value or variables."""
        if self._command_template is None:
            return value

        values: dict[str, Any] = {"value": value}

        if variables is not None:
            values.update(variables)
        _LOGGER.debug(
            "Rendering outgoing payload with variables %s and %s",
            values,
            self._command_template,
        )
        try:
            return convert_outgoing_mqtt_payload(
                self._command_template.render(values, parse_result=False)
            )
        except TemplateError as exc:
            raise MqttCommandTemplateException(
                base_exception=exc,
                command_template=self._command_template.template,
                value=value,
            ) from exc


class MqttValueTemplateException(TemplateError):
    """Handle MqttValueTemplate exceptions."""

    _message: str

    def __init__(
        self,
        *args: object,
        base_exception: Exception,
        value_template: str,
        default: str | bytes | bytearray | PayloadSentinel,
        payload: str | bytes | bytearray,
    ) -> None:
        """Initialize exception."""
        super().__init__(base_exception, *args)
        default_log = str(default)
        default_payload_log = (
            "" if default is PayloadSentinel.NONE else f", default value: {default_log}"
        )
        payload_log = str(payload)
        self._message = (
            f"{type(base_exception).__name__}: {base_exception} rendering template"
            f", template: '{value_template}'{default_payload_log} and payload: {payload_log}"
        )

    def __str__(self) -> str:
        """Return exception message string."""
        return self._message


class MqttValueTemplate:
    """Class for rendering MQTT value template with possible json values."""

    def __init__(
        self,
        value_template: template.Template | None,
    ) -> None:
        """Instantiate a value template."""
        self._value_template = value_template

    def render_with_possible_json_value(
        self,
        payload: str | bytes | bytearray,
        default: str | bytes | bytearray | PayloadSentinel = PayloadSentinel.NONE,
        variables: Mapping[str, Any] | None = None,
    ) -> str | bytes | bytearray:
        """Render with possible json value or pass-though a received MQTT value."""
        rendered_payload: str | bytes | bytearray

        if self._value_template is None:
            return payload

        values: dict[str, Any] = {}

        if variables is not None:
            values.update(variables)

        if default is PayloadSentinel.NONE:
            _LOGGER.debug(
                "Rendering incoming payload '%s' with variables %s and %s",
                payload,
                values,
                self._value_template,
            )
            try:
                rendered_payload = (
                    self._value_template.render_with_possible_json_value(
                        payload, variables=values
                    )
                )
            except TEMPLATE_ERRORS as exc:
                raise MqttValueTemplateException(
                    base_exception=exc,
                    value_template=self._value_template.template,
                    default=default,
                    payload=payload,
                ) from exc
            return rendered_payload

        _LOGGER.debug(
            (
                "Rendering incoming payload '%s' with variables %s with default value"
                " '%s' and %s"
            ),
            payload,
            values,
            default,
            self._value_template,
        )
        try:
            rendered_payload = (
                self._value_template.render_with_possible_json_value(
                    payload, default, variables=values
                )
            )
        except TEMPLATE_ERRORS as exc:
            raise MqttValueTemplateException(
                base_exception=exc,
                value_template=self._value_template.template,
                default=default,
                payload=payload,
            ) from exc
        return rendered_payload
