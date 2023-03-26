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
package org.openhab.binding.gpstracker.internal.config;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PointType;

/**
 * The {@link ConfigHelper} class is a configuration helper for channels and profiles.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class ConfigHelper {
    // configuration constants
    public static final String CONFIG_TRACKER_ID = "trackerId";
    public static final String CONFIG_REGION_NAME = "regionName";
    public static final String CONFIG_REGION_RADIUS = "regionRadius";
    public static final String CONFIG_REGION_CENTER_LOCATION = "regionCenterLocation";
    public static final String CONFIG_ACCURACY_THRESHOLD = "accuracyThreshold";

    /**
     * Constructor.
     */
    private ConfigHelper() {
    }

    public static double getRegionRadius(Configuration config) {
        return ((BigDecimal) config.get(CONFIG_REGION_RADIUS)).doubleValue();
    }

    public static double getAccuracyThreshold(Configuration config) {
        Object value = config.get(CONFIG_ACCURACY_THRESHOLD);
        return value != null ? ((BigDecimal) value).doubleValue() : 0;
    }

    public static String getRegionName(Configuration config) {
        return (String) config.get(CONFIG_REGION_NAME);
    }

    public static String getTrackerId(Configuration config) {
        return (String) config.get(CONFIG_TRACKER_ID);
    }

    public static @Nullable PointType getRegionCenterLocation(Configuration config) {
        String location = (String) config.get(CONFIG_REGION_CENTER_LOCATION);
        return location != null ? new PointType(location) : null;
    }
}
