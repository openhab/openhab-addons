/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.venstarthermostat.internal.dto;

import java.util.List;

/**
 * The {@link VenstarSensorData} represents sensor data returned from the REST API.
 *
 * @author William Welliver - Initial contribution
 */
public class VenstarSensorData {
    List<VenstarSensor> sensors;

    public List<VenstarSensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<VenstarSensor> sensors) {
        this.sensors = sensors;
    }
}
