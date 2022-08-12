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
package org.openhab.io.homekit.internal;

/**
 * Provides the configured and static settings for the Homekit addon
 *
 * @author Andy Lintner - Initial contribution
 */
public class HomekitSettings {
    public static final String CONFIG_PID = "org.openhab.homekit";
    public static final String MANUFACTURER = "openHAB Community";
    public static final String SERIAL_NUMBER = "none";
    public static final String MODEL = "openHAB";
    public static final String HARDWARE_REVISION = "3.0";

    public String name = "openHAB";
    public int port = 9123;
    public int instances = 1;
    public String pin = "031-45-154";
    public String setupId;
    public String qrCode;
    public int startDelay = 30;
    public boolean useFahrenheitTemperature = false;
    public boolean useOHmDNS = false;
    public boolean blockUserDeletion = false;
    public String thermostatTargetModeHeat = "HeatOn";
    public String thermostatTargetModeCool = "CoolOn";
    public String thermostatTargetModeAuto = "Auto";
    public String thermostatTargetModeOff = "Off";
    public String thermostatCurrentModeHeating = "HeatOn";
    public String thermostatCurrentModeCooling = "CoolOn";
    public String thermostatCurrentModeOff = "Off";
    public String doorCurrentStateOpen = "OPEN";
    public String doorCurrentStateOpening = "OPENING";
    public String doorCurrentStateClosed = "CLOSED";
    public String doorCurrentStateClosing = "CLOSING";
    public String doorCurrentStateStopped = "STOPPED";
    public String doorTargetStateClosed = "CLOSED";
    public String doorTargetStateOpen = "OPEN";
    public String networkInterface;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pin == null) ? 0 : pin.hashCode());
        result = prime * result + ((setupId == null) ? 0 : setupId.hashCode());
        result = prime * result + port;
        result = prime * result + ((thermostatTargetModeAuto == null) ? 0 : thermostatTargetModeAuto.hashCode());
        result = prime * result + ((thermostatTargetModeCool == null) ? 0 : thermostatTargetModeCool.hashCode());
        result = prime * result + ((thermostatTargetModeHeat == null) ? 0 : thermostatTargetModeHeat.hashCode());
        result = prime * result + ((thermostatTargetModeOff == null) ? 0 : thermostatTargetModeOff.hashCode());
        result = prime * result + (useFahrenheitTemperature ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HomekitSettings other = (HomekitSettings) obj;
        if (pin == null) {
            if (other.pin != null) {
                return false;
            }
        } else if (!useOHmDNS != other.useOHmDNS) {
            return false;
        } else if (!blockUserDeletion != other.blockUserDeletion) {
            return false;
        } else if (!pin.equals(other.pin)) {
            return false;
        } else if (!setupId.equals(other.setupId)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        if (instances != other.instances) {
            return false;
        }
        if (thermostatTargetModeAuto == null) {
            if (other.thermostatTargetModeAuto != null) {
                return false;
            }
        } else if (!thermostatTargetModeAuto.equals(other.thermostatTargetModeAuto)) {
            return false;
        }
        if (thermostatTargetModeCool == null) {
            if (other.thermostatTargetModeCool != null) {
                return false;
            }
        } else if (!thermostatTargetModeCool.equals(other.thermostatTargetModeCool)) {
            return false;
        }
        if (thermostatTargetModeHeat == null) {
            if (other.thermostatTargetModeHeat != null) {
                return false;
            }
        } else if (!thermostatTargetModeHeat.equals(other.thermostatTargetModeHeat)) {
            return false;
        }
        if (thermostatTargetModeOff == null) {
            if (other.thermostatTargetModeOff != null) {
                return false;
            }
        } else if (!thermostatTargetModeOff.equals(other.thermostatTargetModeOff)) {
            return false;
        }
        if (useFahrenheitTemperature != other.useFahrenheitTemperature) {
            return false;
        }
        return true;
    }
}
