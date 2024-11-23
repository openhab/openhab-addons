/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link InsteonPLMConfiguration} is the configuration for an insteon plm bridge.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonPLMConfiguration extends InsteonBridgeConfiguration {

    private String serialPort = "";
    private int baudRate = 19200;

    public String getSerialPort() {
        return serialPort;
    }

    public int getBaudRate() {
        return baudRate;
    }

    @Override
    public String getId() {
        return serialPort;
    }

    @Override
    public String toString() {
        String s = "";
        s += " serialPort=" + serialPort;
        s += " baudRate=" + baudRate;
        s += super.toString();
        return s;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InsteonPLMConfiguration other = (InsteonPLMConfiguration) obj;
        return serialPort.equals(other.serialPort);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + serialPort.hashCode();
        return result;
    }

    public static InsteonPLMConfiguration valueOf(String serialPort, @Nullable Integer baudRate) {
        InsteonPLMConfiguration config = new InsteonPLMConfiguration();
        config.serialPort = serialPort;
        if (baudRate != null) {
            config.baudRate = baudRate;
        }
        return config;
    }
}
