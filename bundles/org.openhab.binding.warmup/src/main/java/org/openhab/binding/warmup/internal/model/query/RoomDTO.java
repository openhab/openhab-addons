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
package org.openhab.binding.warmup.internal.model.query;

import java.util.List;

/**
 * @author James Melville - Initial contribution
 */
public class RoomDTO {

    private int id;
    private String roomName;
    private Integer currentTemp;
    private Integer targetTemp;
    private String runMode;
    private Integer overrideDur;
    private List<DeviceDTO> thermostat4ies;

    public String getId() {
        return String.valueOf(id);
    }

    public String getName() {
        return roomName;
    }

    public Integer getCurrentTemperature() {
        return currentTemp;
    }

    public Integer getTargetTemperature() {
        return targetTemp;
    }

    public String getRunMode() {
        return runMode;
    }

    public Integer getOverrideDuration() {
        return overrideDur;
    }

    public List<DeviceDTO> getThermostat4ies() {
        return thermostat4ies;
    }
}
