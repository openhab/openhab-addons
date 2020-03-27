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
package org.openhab.binding.freebox.internal.api.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SystemConfig} is the Java class used to map the "SystemConfig"
 * structure used by the system API
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class SystemConfig {
    private String firmwareVersion = "";
    private String mac = "";
    private String serial = "";
    private String uptime = "";
    private long uptimeVal = -1;
    private String boardName = "";
    private boolean boxAuthenticated = false;
    private String diskStatus = "";
    private String boxFlavor = "";
    private String userMainStorage = "";
    private List<Sensor> fans = new ArrayList<>();
    private List<Sensor> sensors = new ArrayList<>();

    public List<Sensor> getSensors() {
        return sensors;
    }

    public List<Sensor> getFans() {
        return fans;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getMac() {
        return mac;
    }

    public String getSerial() {
        return serial;
    }

    public String getUptime() {
        return uptime;
    }

    public long getUptimeVal() {
        return uptimeVal;
    }

    public String getBoardName() {
        return boardName;
    }

    public boolean isBoxAuthenticated() {
        return boxAuthenticated;
    }

    public String getDiskStatus() {
        return diskStatus;
    }

    public String getBoxFlavor() {
        return boxFlavor;
    }

    public String getUserMainStorage() {
        return userMainStorage;
    }
}
