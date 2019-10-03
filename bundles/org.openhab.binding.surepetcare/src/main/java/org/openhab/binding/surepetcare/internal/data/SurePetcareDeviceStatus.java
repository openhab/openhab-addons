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

import java.math.BigDecimal;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SurePetcareDeviceStatus} class is used to serialize a JSON object to report the status of a device (e.g.
 * locking mode, LED mode etc.).
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareDeviceStatus {

    public class Locking {
        @SerializedName("mode")
        public Integer modeId;
    }

    public class Version {
        public class Device {
            public BigDecimal hardware;
            public BigDecimal firmware;
        }

        public Device device = new Device();
    }

    public class Signal {
        public Float deviceRssi;
        public Float hubRssi;
    }

    @SerializedName("led_mode")
    private Integer ledModeId;
    @SerializedName("pairing_mode")
    private Integer pairingModeId;
    private Locking locking;
    private Version version;
    private Float battery;
    // learn_mode - unknown type
    private Boolean online;
    private Signal signal = new Signal();

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

    public Locking getLocking() {
        return locking;
    }

    public void setLocking(Locking locking) {
        this.locking = locking;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Float getBattery() {
        return battery;
    }

    public void setBattery(Float battery) {
        this.battery = battery;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public Signal getSignal() {
        return signal;
    }

    public void setSignal(Signal signal) {
        this.signal = signal;
    }

    public SurePetcareDeviceStatus assign(SurePetcareDeviceStatus source) {
        this.ledModeId = source.ledModeId;
        this.pairingModeId = source.pairingModeId;
        this.locking = source.locking;
        this.version = source.version;
        this.battery = source.battery;
        this.online = source.online;
        this.signal = source.signal;
        return this;
    }

}
