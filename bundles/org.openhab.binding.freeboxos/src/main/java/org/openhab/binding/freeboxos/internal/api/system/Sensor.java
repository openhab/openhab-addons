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
package org.openhab.binding.freeboxos.internal.api.system;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Sensor} is the Java class used to map the fans and sensors part of the "SystemConfig"
 * structure used by the system API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Sensor {
    private final Logger logger = LoggerFactory.getLogger(Sensor.class);

    public enum SensorKind {
        FAN("Vitesse"),
        TEMP("Température"),
        UNKNOWN("Uknown");

        private String label;

        SensorKind(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
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
            try {
                return SensorKind.valueOf(kind);
            } catch (IllegalArgumentException ignore) {
                // Will be logged and result UNKNOWN returned
            }
        }
        logger.warn("Unknown sensor retrieved : {}", id);
        return SensorKind.UNKNOWN;
    }
}
