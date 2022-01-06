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
package org.openhab.binding.ecobee.internal.dto.thermostat;

import java.util.List;

/**
 * The {@link RemoteSensorDTO} represents a sensor connected to the thermostat.
 * The remote sensor data will only show computed occupancy, as does the thermostat.
 * Definition - For a given sensor, computed occupancy means a sensor is occupied
 * if any motion was detected in the past 30 minutes. RemoteSensor data changes
 * trigger the runtimeRevision to be updated. The data updates are sent at an
 * interval of 3 mins maximum. This means that you should not poll quicker
 * than once every 3 mins for revision changes.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class RemoteSensorDTO {
    /*
     * The unique sensor identifier. It is composed of deviceName + deviceId
     * separated by colons, for example: rs:100
     */
    public String id;

    /*
     * The user assigned sensor name.
     */
    public String name;

    /*
     * The type of sensor. Values: thermostat, ecobee3_remote_sensor,
     * monitor_sensor, control_sensor.
     */
    public String type;

    /*
     * The unique 4-digit alphanumeric sensor code. For ecobee3 remote
     * sensors this corresponds to the code found on the back of the physical sensor.
     */
    public String code;

    /*
     * This flag indicates whether the remote sensor is currently in use
     * by a comfort setting. See Climate for more information.
     */
    public Boolean inUse;

    /*
     * The list of remoteSensorCapability objects for the remote sensor.
     */
    public List<RemoteSensorCapabilityDTO> capability;
}
