/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal.data;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SurePetcareDeviceControl} class is used to serialize a JSON object to control certain parameters of a
 * device (e.g. locking mode, curfew etc.).
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareDeviceControl {

    public class Curfew {
        public Curfew(boolean enabled, String lockTime, String unlockTime) {
            this.enabled = enabled;
            this.lockTime = lockTime;
            this.unlockTime = unlockTime;
        }

        public Boolean enabled;
        public String lockTime;
        public String unlockTime;
    }

    @SerializedName("locking")
    private Integer lockingModeId;
    private Boolean fastPolling;
    @SerializedName("led_mode")
    private Integer ledModeId;
    @SerializedName("pairing_mode")
    private Integer pairingModeId;
    private List<Curfew> curfew;

    public Integer getLockingModeId() {
        return lockingModeId;
    }

    public void setLockingModeId(Integer lockingModeId) {
        this.lockingModeId = lockingModeId;
    }

    public Boolean isFastPolling() {
        return fastPolling;
    }

    public void setFastPolling(Boolean fastPolling) {
        this.fastPolling = fastPolling;
    }

    public Integer getLedModeId() {
        return ledModeId;
    }

    public void setLedModeId(Integer ledModeId) {
        this.ledModeId = ledModeId;
    }

    public Integer getPairingModeId() {
        return pairingModeId;
    }

    public void setPairingModeId(Integer pairingModeId) {
        this.pairingModeId = pairingModeId;
    }

    public List<Curfew> getCurfew() {
        return curfew;
    }

    public void setCurfew(List<Curfew> curfew) {
        this.curfew = curfew;
    }
}