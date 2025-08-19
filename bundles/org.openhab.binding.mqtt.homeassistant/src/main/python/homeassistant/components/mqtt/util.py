"""Utility functions for the MQTT integration."""

from __future__ import annotations

from functools import lru_cache
from typing import Any

import voluptuous as vol

from homeassistant.helpers import config_validation as cv

_VALID_QOS_SCHEMA = vol.All(vol.Coerce(int), vol.In([0, 1, 2]))


def valid_topic(topic: Any) -> str:
    """Validate that this is a valid topic name/filter.

    This function is not cached and is not expected to be called
    directly outside of this module. It is not marked as protected
    only because its tested directly in test_util.py.

    If it gets used outside of valid_subscribe_topic and
    valid_publish_topic, it may need an lru_cache decorator or
    an lru_cache decorator on the function where its used.
    """
    validated_topic = cv.string(topic)
    try:
        raw_validated_topic = validated_topic.encode("utf-8")
    except UnicodeError as err:
        raise vol.Invalid("MQTT topic name/filter must be valid UTF-8 string.") from err
    if not raw_validated_topic:
        raise vol.Invalid("MQTT topic name/filter must not be empty.")
    if len(raw_validated_topic) > 65535:
        raise vol.Invalid(
            "MQTT topic name/filter must not be longer than 65535 encoded bytes."
        )

    for char in validated_topic:
        if char == "\0":
            raise vol.Invalid("MQTT topic name/filter must not contain null character.")
        if char <= "\u001f" or "\u007f" <= char <= "\u009f":
            raise vol.Invalid(
                "MQTT topic name/filter must not contain control characters."
            )
        if "\ufdd0" <= char <= "\ufdef" or (ord(char) & 0xFFFF) in (0xFFFE, 0xFFFF):
            raise vol.Invalid("MQTT topic name/filter must not contain non-characters.")

    return validated_topic


@lru_cache
def valid_subscribe_topic(topic: Any) -> str:
    """Validate that we can subscribe using this MQTT topic."""
    validated_topic = valid_topic(topic)
    if "+" in validated_topic:
        for i in (i for i, c in enumerate(validated_topic) if c == "+"):
            if (i > 0 and validated_topic[i - 1] != "/") or (
                i < len(validated_topic) - 1 and validated_topic[i + 1] != "/"
            ):
                raise vol.Invalid(
                    "Single-level wildcard must occupy an entire level of the filter"
                )

    index = validated_topic.find("#")
    if index != -1:
        if index != len(validated_topic) - 1:
            # If there are multiple wildcards, this will also trigger
            raise vol.Invalid(
                "Multi-level wildcard must be the last character in the topic filter."
            )
        if len(validated_topic) > 1 and validated_topic[index - 1] != "/":
            raise vol.Invalid(
                "Multi-level wildcard must be after a topic level separator."
            )

    return validated_topic


@lru_cache
def valid_publish_topic(topic: Any) -> str:
    """Validate that we can publish using this MQTT topic."""
    validated_topic = valid_topic(topic)
    if "+" in validated_topic or "#" in validated_topic:
        raise vol.Invalid("Wildcards cannot be used in topic names")
    return validated_topic


def valid_qos_schema(qos: Any) -> int:
    """Validate that QOS value is valid."""
    validated_qos: int = _VALID_QOS_SCHEMA(qos)
    return validated_qos
