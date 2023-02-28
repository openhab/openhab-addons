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
package org.openhab.binding.nanoleaf.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents rhythm module settings
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class Rhythm {

    private boolean rhythmConnected;
    private boolean rhythmActive;
    private int rhythmId;
    private String hardwareVersion = "";
    private String firmwareVersion = "";
    private boolean auxAvailable;
    private int rhythmMode;
    private @Nullable RhythmPos rhythmPos;

    public boolean getRhythmConnected() {
        return rhythmConnected;
    }

    public void setRhythmConnected(boolean rhythmConnected) {
        this.rhythmConnected = rhythmConnected;
    }

    public boolean getRhythmActive() {
        return rhythmActive;
    }

    public void setRhythmActive(boolean rhythmActive) {
        this.rhythmActive = rhythmActive;
    }

    public int getRhythmId() {
        return rhythmId;
    }

    public void setRhythmId(int rhythmId) {
        this.rhythmId = rhythmId;
    }

    public String getHardwareVersion() {
        return this.hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getFirmwareVersion() {
        return this.firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public boolean getAuxAvailable() {
        return auxAvailable;
    }

    public void setAuxAvailable(boolean auxAvailable) {
        this.auxAvailable = auxAvailable;
    }

    public int getRhythmMode() {
        return rhythmMode;
    }

    public void setRhythmMode(int rhythmMode) {
        this.rhythmMode = rhythmMode;
    }

    public @Nullable RhythmPos getRhythmPos() {
        return rhythmPos;
    }

    public void setRhythmPos(RhythmPos rhythmPos) {
        this.rhythmPos = rhythmPos;
    }
}
