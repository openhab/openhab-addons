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

/**
 * The {@link PhoneConfig} is the Java class used to map the
 * structure used by the phone configuration API
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class PhoneConfig {
    private String network;
    private Boolean dectEcoMode;
    private String dectPin;
    private Integer dectRingPattern;
    private Boolean dectRegistration;
    private Boolean dectNemoMode;
    private Boolean dectEnabled;
    private Boolean dectRingOnOff;

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public Boolean getDectEcoMode() {
        return dectEcoMode;
    }

    public void setDect_eco_mode(Boolean dectEcoMode) {
        this.dectEcoMode = dectEcoMode;
    }

    public String getDectPin() {
        return dectPin;
    }

    public void setDectPin(String dectPin) {
        this.dectPin = dectPin;
    }

    public Integer getDectRingPattern() {
        return dectRingPattern;
    }

    public void setDectRingPattern(Integer dectRingPattern) {
        this.dectRingPattern = dectRingPattern;
    }

    public Boolean getDectRegistration() {
        return dectRegistration;
    }

    public void setDectRegistration(Boolean dectRegistration) {
        this.dectRegistration = dectRegistration;
    }

    public Boolean getDectNemoMode() {
        return dectNemoMode;
    }

    public void setDectNemoMode(Boolean dectNemoMode) {
        this.dectNemoMode = dectNemoMode;
    }

    public Boolean getDectEnabled() {
        return dectEnabled;
    }

    public void setDect_enabled(Boolean dectEnabled) {
        this.dectEnabled = dectEnabled;
    }

    public Boolean getDectRingOnOff() {
        return dectRingOnOff;
    }

    public void setDect_ring_on_off(Boolean dectRingOnOff) {
        this.dectRingOnOff = dectRingOnOff;
    }
}
