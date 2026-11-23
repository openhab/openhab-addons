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
package org.openhab.binding.groheondus.internal.api;

import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.PROPERTY_APPLIANCE_NAME;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.PROPERTY_INSTALLATION_DATE;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.PROPERTY_MEASUREMENT_INTERVAL;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;

/**
 * Appliance metadata.
 *
 * @param measurementTransmissionInterval interval in seconds at which a Sense Guard uploads measurements, if reported
 * @param lastTransmission time the device last transmitted data to the cloud, if reported
 * @param serialNumber serial number, if reported
 * @param firmwareVersion firmware version, if reported
 * @param installationDate installation date as reported by the API, if reported
 * @param measurementPeriod interval in seconds at which a Sense Guard takes measurements, if reported
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public record ApplianceInfo(ApplianceRef ref, String name, int type, @Nullable Integer measurementTransmissionInterval,
        @Nullable Instant lastTransmission, @Nullable String serialNumber, @Nullable String firmwareVersion,
        @Nullable String installationDate, @Nullable Integer measurementPeriod) {
    public static final int TYPE_SENSE = 101;
    public static final int TYPE_SENSE_GUARD = 103;

    private static final String VENDOR = "GROHE";

    /**
     * @return the Thing properties describing this appliance; values the API did not report are left out
     */
    public Map<String, String> properties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(PROPERTY_APPLIANCE_NAME, name);
        properties.put(Thing.PROPERTY_VENDOR, VENDOR);
        String model = switch (type) {
            case TYPE_SENSE -> "Sense";
            case TYPE_SENSE_GUARD -> "Sense Guard";
            default -> null;
        };
        putIfPresent(properties, Thing.PROPERTY_MODEL_ID, model);
        putIfPresent(properties, Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
        putIfPresent(properties, Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
        putIfPresent(properties, PROPERTY_INSTALLATION_DATE, installationDate);
        Integer period = measurementPeriod;
        if (period != null) {
            properties.put(PROPERTY_MEASUREMENT_INTERVAL, period.toString());
        }
        return properties;
    }

    private static void putIfPresent(Map<String, String> properties, String key, @Nullable String value) {
        if (value != null && !value.isBlank()) {
            properties.put(key, value.trim());
        }
    }
}
