"""Constants used by multiple MQTT modules."""

import jinja2

from homeassistant.exceptions import TemplateError

TEMPLATE_ERRORS = (jinja2.TemplateError, TemplateError, TypeError, ValueError)
