/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
 * @author Arne Seime - Initial contribution
 */
public class PodDetails {
    public String id;
    public String macAddress;
    public String firmwareVersion;
    public String firmwareType;
    @SerializedName("serial")
    public String serialNumber;
    public String temperatureUnit;
    public String productModel;
    public AcState acState;
    @SerializedName("measurements")
    public Measurement lastMeasurement;
    private ModeCapabilityWrapper remoteCapabilities;
    public ConnectionStatus connectionStatus;
    public Room room;
    public Schedule[] schedules;
    public Timer timer;

    public Map<String, ModeCapability> getRemoteCapabilities() {
        return remoteCapabilities.modes;
    }

    public boolean isAlive() {
        return connectionStatus.alive;
    }

    public String getRoomName() {
        return room.name;
    }
}
