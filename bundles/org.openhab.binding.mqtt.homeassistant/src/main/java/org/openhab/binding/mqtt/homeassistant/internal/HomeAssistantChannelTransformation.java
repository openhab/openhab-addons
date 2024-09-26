/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.homeassistant.internal.component.AbstractComponent;
import org.openhab.core.thing.binding.generic.ChannelTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

/**
 * Provides a channel transformation for a Home Assistant channel with a
 * Jinja2 template, providing the additional context and extensions required by Home Assistant
 * Based in part on the JinjaTransformationService
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class HomeAssistantChannelTransformation extends ChannelTransformation {
    public static class UndefinedException extends InvalidInputException {
        public UndefinedException(JinjavaInterpreter interpreter) {
            super(interpreter, "is_defined", "Value is undefined");
        }
    }

    private final Logger logger = LoggerFactory.getLogger(HomeAssistantChannelTransformation.class);

    private final Jinjava jinjava;
    private final AbstractComponent component;
    private final String template;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HomeAssistantChannelTransformation(Jinjava jinjava, AbstractComponent component, String template) {
        super((String) null);
        this.jinjava = jinjava;
        this.component = component;
        this.template = template;
    }

    @Override
    public boolean isEmpty() {
        return template.isEmpty();
    }

    @Override
    public Optional<String> apply(String value) {
        return apply(template, value);
    }

    public Optional<String> apply(String template, String value) {
        Map<String, @Nullable Object> bindings = new HashMap<>();

        logger.debug("about to transform '{}' by the function '{}'", value, template);

        bindings.put("value", value);

        try {
            JsonNode tree = objectMapper.readTree(value);
            bindings.put("value_json", toObject(tree));
        } catch (IOException e) {
            // ok, then value_json is null...
        }

        return apply(template, bindings);
    }

    public Optional<String> apply(String template, Map<String, @Nullable Object> bindings) {
        String transformationResult;

        try {
            transformationResult = jinjava.render(template, bindings);
        } catch (FatalTemplateErrorsException e) {
            var error = e.getErrors().iterator();
            Exception exception = null;
            if (error.hasNext()) {
                exception = error.next().getException();
            }
            if (exception instanceof UndefinedException) {
                // They used the is_defined filter; it's expected to return null, with no warning
                return Optional.empty();
            }
            logger.warn("Applying template {} for component {} failed: {} ({})", template,
                    component.getHaID().toShortTopic(), e.getMessage(), e.getClass());
            return Optional.empty();
        }

        logger.debug("transformation resulted in '{}'", transformationResult);

        return Optional.of(transformationResult);
    }

    private static @Nullable Object toObject(JsonNode node) {
        switch (node.getNodeType()) {
            case ARRAY: {
                List<@Nullable Object> result = new ArrayList<>();
                for (JsonNode el : node) {
                    result.add(toObject(el));
                }
                return result;
            }
            case NUMBER:
                return node.decimalValue();
            case OBJECT: {
                Map<String, @Nullable Object> result = new HashMap<>();
                Iterator<Entry<String, JsonNode>> it = node.fields();
                while (it.hasNext()) {
                    Entry<String, JsonNode> field = it.next();
                    result.put(field.getKey(), toObject(field.getValue()));
                }
                return result;
            }
            case STRING:
                return node.asText();
            case BOOLEAN:
                return node.asBoolean();
            case NULL:
            default:
                return null;
        }
    }
}
