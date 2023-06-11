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
package org.openhab.binding.onewire.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SensorId} provides a sensorID for the Onewire bus.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SensorId {
    public static final Pattern SENSOR_ID_PATTERN = Pattern
            .compile("^\\/?((?:(?:1F\\.[0-9A-Fa-f]{12})\\/(?:main|aux)\\/)+)?([0-9A-Fa-f]{2}\\.[0-9A-Fa-f]{12})$");

    private final String sensorId;
    private final String path;
    private final String fullPath;

    /**
     * construct a new SensorId object
     *
     * allowed formats:
     * - "28.0123456789ab"
     * - "1F.1234566890ab/main/28.0123456789ab"
     * - "1F.1234566890ab/aux/28.0123456789ab"
     * - leading "/" characters are allowed but not required
     * - characters are case-insensitive
     * - hubs ("1F.xxxxxxxxxxxx/aux/") may be repeated
     */
    public SensorId(@Nullable String fullPath) {
        if (fullPath == null) {
            throw new IllegalArgumentException();
        }
        Matcher matcher = SENSOR_ID_PATTERN.matcher(fullPath);
        if (matcher.matches() && matcher.groupCount() == 2) {
            path = matcher.group(1) == null ? "" : matcher.group(1);
            sensorId = matcher.group(2);
            this.fullPath = "/" + path + sensorId;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * get the full path to the sensor
     *
     * @return full path (including hub parts, separated by "/" characters)
     */
    public String getFullPath() {
        return fullPath;
    }

    /**
     * get the sensor id
     *
     * @return sensor id without leading "/" character
     */
    public String getId() {
        return sensorId;
    }

    /**
     * get the path of this sensorId
     *
     * @return path without sensor id (including hub parts, separated by "/" characters)
     */
    public String getPath() {
        return path;
    }

    /**
     * get family id (first to characters of sensor id)
     *
     * @return the family id
     */
    public String getFamilyId() {
        return sensorId.substring(0, 2);
    }

    @Override
    public String toString() {
        return fullPath;
    }

    @Override
    public int hashCode() {
        return this.fullPath.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof SensorId)) {
            return false;
        }

        return ((SensorId) o).fullPath.equals(fullPath);
    }
}
