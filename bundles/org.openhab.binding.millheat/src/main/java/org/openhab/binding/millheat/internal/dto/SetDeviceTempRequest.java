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
package org.openhab.binding.millheat.internal.dto;

import org.openhab.binding.millheat.internal.model.Heater;

/**
 * This DTO class wraps the set device temp request
 *
 * @see SetRoomTempRequest
 * @author Arne Seime - Initial contribution
 */
public class SetDeviceTempRequest implements AbstractRequest {
    public final int subDomain;
    public final long deviceId;
    public final boolean testStatus = true;
    public final int operation;
    public final boolean status;
    public final boolean windStatus;
    public final int holdTemp;
    public final int tempType = 0; // FIXED?
    public final int powerLevel = 0; // FIXED?

    @Override
    public String getRequestUrl() {
        return "deviceControl";
    }

    public SetDeviceTempRequest(final Heater heater, final int targetTemperature, final boolean masterSwitch,
            final boolean fanActive) {
        this.subDomain = heater.getSubDomain();
        this.deviceId = heater.getId();
        this.holdTemp = targetTemperature;
        this.status = masterSwitch;
        this.windStatus = fanActive;
        if (fanActive != heater.fanActive()) {
            // Changed
            operation = 4;
        } else if (heater.getTargetTemp() != targetTemperature) {
            operation = 1;
        } else {
            operation = 0;
        }
    }
}
