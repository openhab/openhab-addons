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
package org.openhab.binding.sensibo.internal.dto.poddetails;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * All classes in the ..binding.sensibo.dto are data transfer classes used by the GSON mapper. This class reflects a
 * part of a request/response data structure.
 *
 * @author Arne Seime - Initial contribution.
 */
public class PodDetailsDTO {
    public String id;
    public String macAddress;
    public String firmwareVersion;
    public String firmwareType;
    @SerializedName("serial")
    public String serialNumber;
    public String temperatureUnit;
    public String productModel;
    public AcStateDTO acState;
    @SerializedName("measurements")
    public MeasurementDTO lastMeasurement;
    public ConnectionStatusDTO connectionStatus;
    public RoomDTO room;
    public ScheduleDTO[] schedules;
    public TimerDTO timer;
    private ModeCapabilityWrapperDTO remoteCapabilities;

    public Map<String, ModeCapabilityDTO> getRemoteCapabilities() {
        return remoteCapabilities.modes;
    }

    public boolean isAlive() {
        return connectionStatus.alive;
    }

    public String getRoomName() {
        return room.name;
    }
}
