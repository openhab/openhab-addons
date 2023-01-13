/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.system;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.SensorKind;

/**
 * The {@link SystemConfigSensor} is the Java class used to map the fans and sensors part of the "SystemConfig"
 * structure used by the system API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SystemConfigSensor {
    private @Nullable String id;
    private @Nullable String name;
    private int value;

    public String getId() {
        return Objects.requireNonNull(id);
    }

    public String getName() {
        return Objects.requireNonNull(name);
    }

    public int getValue() {
        return value;
    }

    public SensorKind getKind() {
        String[] elements = getId().split("_");
        if (elements.length > 0) {
            String kind = elements[0].replaceAll("\\d", "").toUpperCase();
            try {
                return SensorKind.valueOf(kind);
            } catch (IllegalArgumentException ignore) { // returning UNKNOWN
            }
        }
        return SensorKind.UNKNOWN;
    }
}
