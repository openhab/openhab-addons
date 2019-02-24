/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.transform.jinja.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jinjava.Jinjava;

/**
 * <p>
 * The implementation of {@link TransformationService} which transforms the input by Jinja2 Expressions.
 *
 * @author Jochen Klein - Initial contribution
 *
 */
@NonNullByDefault
@Component(immediate = true, property = { "smarthome.transform=JINJA" })
public class JinjaTransformationService implements TransformationService {

    private final Logger logger = LoggerFactory.getLogger(JinjaTransformationService.class);

    private Jinjava jinjava = new Jinjava();

    /**
     * Transforms the input <code>value</code> by Jinja template.
     *
     * @param template Jinja template
     * @param value    String may contain JSON
     * @throws TransformationException
     */
    @Override
    public @Nullable String transform(String template, String value) throws TransformationException {
        logger.debug("about to transform '{}' by the function '{}'", value, template);

        Map<String, @Nullable Object> bindings = new HashMap<>();
        bindings.put("value", value);

        try {
            JsonNode tree = new ObjectMapper().readTree(value);
            bindings.put("value_json", toObject(tree));
        } catch (IOException e) {
            // ok, then value_json is null...
        }

        String transformationResult = jinjava.render(template, bindings);

        logger.debug("transformation resulted in '{}'", transformationResult);

        return transformationResult;
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
