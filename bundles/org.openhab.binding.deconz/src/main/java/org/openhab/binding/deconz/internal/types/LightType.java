/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.types;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Type of a light as reported by the REST API for usage in {@link org.openhab.binding.deconz.internal.dto.LightMessage}
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public enum LightType {
    ON_OFF_LIGHT("On/Off light"),
    ON_OFF_PLUGIN_UNIT("On/Off plug-in unit"),
    SMART_PLUG("Smart plug"),
    EXTENDED_COLOR_LIGHT("Extended color light"),
    COLOR_LIGHT("Color light"),
    COLOR_DIMMABLE_LIGHT("Color dimmable light"),
    COLOR_TEMPERATURE_LIGHT("Color temperature light"),
    DIMMABLE_LIGHT("Dimmable light"),
    DIMMABLE_PLUGIN_UNIT("Dimmable plug-in unit"),
    WINDOW_COVERING_DEVICE("Window covering device"),
    CONFIGURATION_TOOL("Configuration tool"),
    WARNING_DEVICE("Warning device"),
    DOORLOCK("Door Lock"),
    UNKNOWN("");

    private static final Map<String, LightType> MAPPING = Arrays.stream(LightType.values())
            .collect(Collectors.toMap(v -> v.type, v -> v));
    private static final Logger LOGGER = LoggerFactory.getLogger(LightType.class);

    private String type;

    LightType(String type) {
        this.type = type;
    }

    public static LightType fromString(String s) {
        LightType lightType = MAPPING.getOrDefault(s, UNKNOWN);
        if (lightType == UNKNOWN) {
            LOGGER.debug("Unknown light type '{}' found. This should be reported.", s);
        }
        return lightType;
    }
}
