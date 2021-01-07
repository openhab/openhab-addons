/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Sensor} is the Java class used to map the fans and sensors part of the "SystemConfig"
 * structure used by the system API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Sensor {
    public enum SensorKind {
        FAN,
        TEMP,
        UNKNOWN;
    }

    private @NonNullByDefault({}) String id;
    private @NonNullByDefault({}) String name;
    private int value;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public SensorKind getKind() {
        String[] elements = id.split("_");
        if (elements.length > 0) {
            String kind = elements[0].replaceAll("\\d", "").toUpperCase();
            return SensorKind.valueOf(kind);
        }
        return SensorKind.UNKNOWN;
    }
}
