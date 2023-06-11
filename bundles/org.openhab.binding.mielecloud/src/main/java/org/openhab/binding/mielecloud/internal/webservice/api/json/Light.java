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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents the state of a light on a Miele device.
 *
 * @author Roland Edelhoff - Initial contribution
 * @author Björn Lange - Added NOT_SUPPORTED entry
 */
@NonNullByDefault
public enum Light {
    /**
     * {Light} for unknown states.
     */
    UNKNOWN(),

    ENABLE(1),

    DISABLE(2),

    NOT_SUPPORTED(0, 255);

    private List<Integer> ids;

    Light(int... ids) {
        this.ids = Collections.unmodifiableList(Arrays.stream(ids).boxed().collect(Collectors.toList()));
    }

    /**
     * Gets the {@link Light} state matching the given ID.
     * 
     * @param id The ID.
     * @return The matching {@link Light} or {@code UNKNOWN} if no ID matches.
     */
    public static Light fromId(@Nullable Integer id) {
        for (Light light : Light.values()) {
            if (light.ids.contains(id)) {
                return light;
            }
        }

        return Light.UNKNOWN;
    }

    /**
     * Formats this instance for interaction with the Miele webservice.
     */
    public String format() {
        if (ids.isEmpty()) {
            return "";
        } else {
            return Integer.toString(ids.get(0));
        }
    }
}
