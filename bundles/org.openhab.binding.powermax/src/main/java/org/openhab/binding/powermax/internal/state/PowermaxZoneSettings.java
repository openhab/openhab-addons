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
package org.openhab.binding.powermax.internal.state;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to store the settings of a zone
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxZoneSettings {

    // Note: PowermaxStatusMessage contains hardcoded references to some of these strings

    private static final String[] ZONE_TYPES = { "Non-Alarm", "Emergency", "Flood", "Gas", "Delay 1", "Delay 2",
            "Interior-Follow", "Perimeter", "Perimeter-Follow", "24 Hours Silent", "24 Hours Audible", "Fire",
            "Interior", "Home Delay", "Temperature", "Outdoor" };

    private static final String[] ZONE_CHIMES = { "Off", "Melody", "Zone" };

    private final @Nullable String chime;
    private final boolean[] partitions;

    private @Nullable String name;
    private @Nullable String type;
    private @Nullable String sensorType;
    private boolean alwaysInAlarm;

    public PowermaxZoneSettings(@Nullable String name, byte type, byte chime, @Nullable String sensorType,
            boolean[] partitions) {
        this.name = name;
        this.type = ((type & 0x000000FF) < ZONE_TYPES.length) ? ZONE_TYPES[type & 0x000000FF] : null;
        this.chime = ((chime & 0x000000FF) < ZONE_CHIMES.length) ? ZONE_CHIMES[chime & 0x000000FF] : null;
        this.sensorType = sensorType;
        this.partitions = partitions;
        this.alwaysInAlarm = ((type == 2) || (type == 3) || (type == 9) || (type == 10) || (type == 11)
                || (type == 14));
    }

    /**
     * @return the zone name
     */
    public String getName() {
        String localName = name;
        return (localName == null) ? "Unknown" : localName;
    }

    /**
     * Set the zone name
     *
     * @param name the zone name
     */
    public void setName(@Nullable String name) {
        this.name = name;
    }

    /**
     * @return the zone type
     */
    public String getType() {
        String localType = type;
        return (localType == null) ? "Unknown" : localType;
    }

    /**
     * Set the zone type
     *
     * @param type the zone type as an internal code
     */
    public void setType(byte type) {
        this.type = ((type & 0x000000FF) < ZONE_TYPES.length) ? ZONE_TYPES[type & 0x000000FF] : null;
        this.alwaysInAlarm = ((type == 2) || (type == 3) || (type == 9) || (type == 10) || (type == 11)
                || (type == 14));
    }

    public String getChime() {
        String localChime = chime;
        return (localChime == null) ? "Unknown" : localChime;
    }

    /**
     * @return the sensor type of this zone
     */
    public String getSensorType() {
        String localSensorType = sensorType;
        return (localSensorType == null) ? "Unknown" : localSensorType;
    }

    /**
     * Set the sensor type of this zone
     *
     * @param sensorType the sensor type
     */
    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    /**
     * @return true if the sensor type of this zone is a motion sensor
     */
    public boolean isMotionSensor() {
        return PowermaxSensorType.MOTION_SENSOR_1.getLabel().equalsIgnoreCase(getSensorType());
    }

    /**
     * @param number the partition number (first partition is number 1)
     *
     * @return true if the zone is attached to this partition; false if not
     */
    public boolean isInPartition(int number) {
        return ((number <= 0) || (number > partitions.length)) ? false : partitions[number - 1];
    }

    /**
     * @return true if the zone type is always in alarm; false if not
     */
    public boolean isAlwaysInAlarm() {
        return alwaysInAlarm;
    }
}
