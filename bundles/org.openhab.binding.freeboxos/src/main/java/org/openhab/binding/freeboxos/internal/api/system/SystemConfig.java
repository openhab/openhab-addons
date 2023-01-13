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
package org.openhab.binding.freeboxos.internal.api.system;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.DiskStatus;

/**
 * The {@link SystemConfig} is the Java class used to map minimal common structure used by the system API in respose to
 * get configuration requests
 *
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SystemConfig {
    private @Nullable String firmwareVersion;
    private @Nullable String mac;
    private @Nullable String serial;
    private @Nullable String uptime;
    private long uptimeVal; // freebox uptime (in seconds)
    private @Nullable String boardName;
    private boolean boxAuthenticated;
    private DiskStatus diskStatus = DiskStatus.UNKNOWN;
    private @Nullable String userMainStorage;
    private List<SystemConfigSensor> sensors = List.of();
    private @Nullable SystemModelInfo modelInfo;
    private List<SystemConfigSensor> fans = List.of();
    private List<SystemConfigExpansion> expansions = List.of();

    public String getFirmwareVersion() {
        return Objects.requireNonNull(firmwareVersion);
    }

    public String getMac() {
        return Objects.requireNonNull(mac);
    }

    public String getSerial() {
        return Objects.requireNonNull(serial);
    }

    public String getUptime() {
        return Objects.requireNonNull(uptime);
    }

    public long getUptimeVal() {
        return uptimeVal;
    }

    public String getBoardName() {
        return Objects.requireNonNull(boardName);
    }

    public boolean getBoxAuthenticated() {
        return boxAuthenticated;
    }

    public DiskStatus getDiskStatus() {
        return diskStatus;
    }

    public String getUserMainStorage() {
        return Objects.requireNonNull(userMainStorage);
    }

    public List<SystemConfigSensor> getSensors() {
        return sensors;
    }

    public SystemModelInfo getModelInfo() {
        return Objects.requireNonNull(modelInfo);
    }

    public List<SystemConfigSensor> getFans() {
        return fans;
    }

    public List<SystemConfigExpansion> getExpansions() {
        return expansions;
    }
}
