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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PhoneConfig} is the Java class used to map the
 * structure used by the phone configuration API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PhoneConfig {
    private String network = "";
    private boolean dectEcoMode;
    private String dectPin = "";
    private int dectRingPattern;
    private boolean dectRegistration;
    private boolean dectNemoMode;
    private boolean dectEnabled;
    private boolean dectRingOnOff;

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public boolean getDectEcoMode() {
        return dectEcoMode;
    }

    public void setDectEcoMode(boolean dectEcoMode) {
        this.dectEcoMode = dectEcoMode;
    }

    public String getDectPin() {
        return dectPin;
    }

    public void setDectPin(String dectPin) {
        this.dectPin = dectPin;
    }

    public int getDectRingPattern() {
        return dectRingPattern;
    }

    public void setDectRingPattern(int dectRingPattern) {
        this.dectRingPattern = dectRingPattern;
    }

    public boolean getDectRegistration() {
        return dectRegistration;
    }

    public void setDectRegistration(boolean dectRegistration) {
        this.dectRegistration = dectRegistration;
    }

    public boolean getDectNemoMode() {
        return dectNemoMode;
    }

    public void setDectNemoMode(boolean dectNemoMode) {
        this.dectNemoMode = dectNemoMode;
    }

    public boolean getDectEnabled() {
        return dectEnabled;
    }

    public void setDectEnabled(boolean dectEnabled) {
        this.dectEnabled = dectEnabled;
    }

    public boolean getDectRingOnOff() {
        return dectRingOnOff;
    }

    public void setDectRingOnOff(boolean dectRingOnOff) {
        this.dectRingOnOff = dectRingOnOff;
    }
}
