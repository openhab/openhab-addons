/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.nobohub.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;

/**
 * Unit tests for the Nobø Hub bridge configuration.
 *
 * A thing configuration is bound to a configuration class by field name, so a parameter declared in the thing type
 * whose name has no matching field is silently dropped and the field keeps its default value.
 *
 * @author Max Freedom Pollard - Initial contribution
 */
@NonNullByDefault
public class NoboHubBridgeConfigurationTest {

    private static final Path BRIDGE_XML = Path.of("src", "main", "resources", "OH-INF", "thing", "bridge.xml");
    private static final Pattern PARAMETER_NAME_PATTERN = Pattern.compile("<parameter\\s+name=\"([^\"]+)\"");

    private static final String KEEPALIVE_INTERVAL = "keepaliveInterval";

    @Test
    public void everyDeclaredParameterHasAMatchingConfigurationField() throws IOException {
        List<String> declaredParameters = readDeclaredParameterNames();
        assertFalse(declaredParameters.isEmpty(), "No configuration parameters found in " + BRIDGE_XML);

        Set<String> fieldNames = new HashSet<>();
        for (Field field : NoboHubBridgeConfiguration.class.getFields()) {
            fieldNames.add(field.getName());
        }

        for (String parameter : declaredParameters) {
            assertTrue(fieldNames.contains(parameter), "Parameter '" + parameter + "' declared in " + BRIDGE_XML
                    + " has no matching field in NoboHubBridgeConfiguration");
        }
    }

    @Test
    public void keepaliveIntervalReachesTheConfiguration() throws ReflectiveOperationException {
        Configuration configuration = new Configuration(Map.of(KEEPALIVE_INTERVAL, 30));

        NoboHubBridgeConfiguration config = configuration.as(NoboHubBridgeConfiguration.class);

        // Read by name because it is the parameter name matching the field name that is under test here.
        assertEquals(30, NoboHubBridgeConfiguration.class.getField(KEEPALIVE_INTERVAL).getInt(config));
    }

    private List<String> readDeclaredParameterNames() throws IOException {
        List<String> names = new ArrayList<>();
        for (String line : Files.readAllLines(BRIDGE_XML)) {
            Matcher matcher = PARAMETER_NAME_PATTERN.matcher(line);
            if (matcher.find()) {
                names.add(matcher.group(1));
            }
        }
        return names;
    }
}
