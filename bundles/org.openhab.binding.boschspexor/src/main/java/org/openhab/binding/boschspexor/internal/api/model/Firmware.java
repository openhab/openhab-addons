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
package org.openhab.binding.boschspexor.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Representaion of Firmware information
 *
 * @author Marc Fischer - Initial contribution *
 */
@NonNullByDefault
public class Firmware {
    /**
     * Available information about Firmware
     *
     * @author Marc Fischer - Initial contribution
     *
     */
    public enum FirmwareState {
        UpToDate,
        UpdateAvailable,
        Scheduled,
        Installing,
        InstallationFailed,
    }

    private String currentVersion = "";
    private FirmwareState state = FirmwareState.UpToDate;
    private String availableVersion = "";

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public FirmwareState getState() {
        return state;
    }

    public void setState(FirmwareState state) {
        this.state = state;
    }

    public String getAvailableVersion() {
        return availableVersion;
    }

    public void setAvailableVersion(String availableVersion) {
        this.availableVersion = availableVersion;
    }
}
