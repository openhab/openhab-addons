/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homewizard.internal.devices.energy_socket;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json object obtained from the HomeWizard Energy Socket State API
 *
 * @author Daniël van Os - Initial contribution
 *
 */
@NonNullByDefault
public class HomeWizardEnergySocketStatePayload {
    @SerializedName("power_on")
    private boolean powerOn;
    @SerializedName("switch_lock")
    private boolean switchLock;
    private int brightness = 0;

    /**
     * Getter for the power_on field
     *
     * @return true if the device is currently on, false if it is off
     */
    public boolean getPowerOn() {
        return powerOn;
    }

    /**
     * Setter for the power_on field
     *
     * @param powerOn true to turn the device on, false to turn it off
     */
    public void setPowerOn(boolean powerOn) {
        this.powerOn = powerOn;
    }

    /**
     * Getter for the switch_lock field
     *
     * @return true if the device currently locked, false if it is not
     */
    public boolean getSwitchLock() {
        return switchLock;
    }

    /**
     * Setter for the power_on field
     *
     * @param switchLock true to lock the device, false to unlock it
     */
    public void setSwitchLock(boolean switchLock) {
        this.switchLock = switchLock;
    }

    /**
     * Getter for the ring brightness
     *
     * @return ring brightness percentage
     */
    public int getBrightness() {
        return brightness;
    }

    /**
     * Setter for the ring brightness
     *
     * @param brightness ring brightness
     */
    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    @Override
    public String toString() {
        return String.format("State [power_on: %b switch_lock: %b brightness: %d]", powerOn, switchLock, brightness);
    }
}
