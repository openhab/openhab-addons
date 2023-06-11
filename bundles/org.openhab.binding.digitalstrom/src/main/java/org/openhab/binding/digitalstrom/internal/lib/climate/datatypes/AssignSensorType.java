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
package org.openhab.binding.digitalstrom.internal.lib.climate.datatypes;

import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.SensorEnum;

/**
 * The {@link AssignSensorType} assigns a sensor type of a zone to the dSUID of the sensor-device.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class AssignSensorType {

    private final SensorEnum sensorType;
    private final String dsuid;

    /**
     * Create a new {@link AssignSensorType}.
     *
     * @param sensorType must not be null
     * @param dSUID must not be null
     */
    public AssignSensorType(SensorEnum sensorType, String dSUID) {
        this.sensorType = sensorType;
        dsuid = dSUID;
    }

    /**
     * Returns the sensor type as {@link SensorEnum}.
     *
     * @return the sensor type
     */
    public SensorEnum getSensorType() {
        return sensorType;
    }

    /**
     * Returns the dSUID of the assign sensor-device.
     *
     * @return the dSUID
     */
    public String getDSUID() {
        return dsuid;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AssignSensorType [SENSOR_TYPE=" + sensorType + ", dSUID=" + dsuid + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dsuid == null) ? 0 : dsuid.hashCode());
        result = prime * result + ((sensorType == null) ? 0 : sensorType.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AssignSensorType)) {
            return false;
        }
        AssignSensorType other = (AssignSensorType) obj;
        if (dsuid == null) {
            if (other.dsuid != null) {
                return false;
            }
        } else if (!dsuid.equals(other.dsuid)) {
            return false;
        }
        if (sensorType != other.sensorType) {
            return false;
        }
        return true;
    }
}
