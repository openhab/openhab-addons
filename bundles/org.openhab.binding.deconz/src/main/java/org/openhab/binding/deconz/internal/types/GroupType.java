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
package org.openhab.binding.deconz.internal.types;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Type of a group as reported by the REST API for usage in
 * {@link org.openhab.binding.deconz.internal.dto.LightMessage}
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public enum GroupType {
    LIGHT_GROUP("LightGroup"),
    LUMINAIRE("Luminaire"),
    ROOM("Room"),
    LIGHT_SOURCE("Lightsource"),
    UNKNOWN("");

    private static final Map<String, GroupType> MAPPING = Arrays.stream(GroupType.values())
            .collect(Collectors.toMap(v -> v.type, v -> v));
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupType.class);

    private final String type;

    GroupType(String type) {
        this.type = type;
    }

    public static GroupType fromString(String s) {
        GroupType lightType = MAPPING.getOrDefault(s, UNKNOWN);
        if (lightType == UNKNOWN) {
            LOGGER.debug("Unknown group type '{}' found. This should be reported.", s);
        }
        return lightType;
    }
}
