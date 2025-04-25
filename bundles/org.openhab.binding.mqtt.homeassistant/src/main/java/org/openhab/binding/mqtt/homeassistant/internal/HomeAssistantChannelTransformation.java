/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mqtt.homeassistant.internal;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.homeassistant.internal.component.AbstractComponent;
import org.openhab.core.thing.binding.generic.ChannelTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a channel transformation for a Home Assistant channel with a
 * Jinja2 template, providing the additional context and extensions required by Home Assistant
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class HomeAssistantChannelTransformation extends ChannelTransformation {
    // These map to PayloadSentinen.NONE and PayloadSentinel.DEFAULT in mqtt/models.py
    // NONE is used to indicate that errors should be ignored, and if any happen the original
    // payload should be returned directly
    public static final String PAYLOAD_SENTINEL_NONE = "none";
    public static final String PAYLOAD_SENTINEL_DEFAULT = "default";

    private final Logger logger = LoggerFactory.getLogger(HomeAssistantChannelTransformation.class);

    private final HomeAssistantPythonBridge python;
    private final AbstractComponent<?> component;
    private final Value template;
    private final boolean command;
    private final String defaultValue;
    private final boolean parseValueAsInteger;

    public HomeAssistantChannelTransformation(HomeAssistantPythonBridge python, AbstractComponent<?> component,
            Value template, boolean command) {
        this(python, component, template, command, PAYLOAD_SENTINEL_NONE, false);
    }

    public HomeAssistantChannelTransformation(HomeAssistantPythonBridge python, AbstractComponent<?> component,
            Value template, boolean command, boolean parseValueAsInteger) {
        this(python, component, template, command, PAYLOAD_SENTINEL_NONE, parseValueAsInteger);
    }

    public HomeAssistantChannelTransformation(HomeAssistantPythonBridge python, AbstractComponent<?> component,
            Value template, String defaultValue) {
        this(python, component, template, false, defaultValue, false);
    }

    private HomeAssistantChannelTransformation(HomeAssistantPythonBridge python, AbstractComponent<?> component,
            Value template, boolean command, String defaultValue, boolean parseValueAsInteger) {
        super((String) null);
        this.python = python;
        this.component = component;
        this.command = command;
        this.template = command ? python.newCommandTemplate(template) : python.newValueTemplate(template);
        this.defaultValue = defaultValue;
        this.parseValueAsInteger = parseValueAsInteger;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Optional<String> apply(String value) {
        Object objValue = value;
        if (parseValueAsInteger) {
            try {
                objValue = (int) Float.parseFloat(value);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse value {} as integer: {}", value, e.getMessage());
                return Optional.empty();
            }
        }
        Object result = transform(objValue);
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(result.toString());
    }

    public @Nullable String transform(Object value) {
        try {
            return command ? python.renderCommandTemplate(template, value)
                    : python.renderValueTemplate(template, value, defaultValue);
        } catch (PolyglotException e) {
            logger.warn("Applying template for component {} failed: {}", component.getHaID().toShortTopic(),
                    e.getMessage(), e);
            return null;
        }
    }

    public @Nullable String transform(Object value, Map<String, @Nullable Object> variables) {
        try {
            return command ? python.renderCommandTemplate(template, value, variables)
                    : python.renderValueTemplate(template, value, defaultValue, variables);
        } catch (PolyglotException e) {
            logger.warn("Applying template for component {} failed: {}", component.getHaID().toShortTopic(),
                    e.getMessage(), e);
            return null;
        }
    }
}
