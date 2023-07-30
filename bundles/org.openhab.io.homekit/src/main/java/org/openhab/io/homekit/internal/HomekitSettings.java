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
    public boolean useDummyAccessories = false;
    public boolean useFahrenheitTemperature = false;
    public boolean useOHmDNS = false;
    public boolean blockUserDeletion = false;
    public String networkInterface;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pin == null) ? 0 : pin.hashCode());
        result = prime * result + ((setupId == null) ? 0 : setupId.hashCode());
        result = prime * result + port;
        result = prime * result + (useFahrenheitTemperature ? 1231 : 1237);
        result = prime * result + (useDummyAccessories ? 1249 : 1259);
        return result;
    }

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
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
        if (useFahrenheitTemperature != other.useFahrenheitTemperature) {
            return false;
        }
        if (useDummyAccessories != other.useDummyAccessories) {
            return false;
        }
        return true;
    }
}
