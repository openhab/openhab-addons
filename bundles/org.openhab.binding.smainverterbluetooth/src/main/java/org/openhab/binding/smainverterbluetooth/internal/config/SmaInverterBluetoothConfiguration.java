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
package org.openhab.binding.smainverterbluetooth.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SmaInverterBluetoothConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Lee Charlton - Initial contribution
 */
@NonNullByDefault
public class SmaInverterBluetoothConfiguration {

    /**
     * Sample configuration parameters. Replace with your own.
     */
    public String bluetoothAddress = "";
    public String password = "";
    public int refreshInterval = 20;

    public String getBluetoothAddress() {
        return this.bluetoothAddress;
    }

    public String getPassword() {
        return this.password;
    }

    public int getRefreshInterval() {
        return this.refreshInterval;
    }

    public void setBluetoothAddress(String hostname) {
        this.bluetoothAddress = hostname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    @Override
    public String toString() {
        return "SAM Inverter Bluetooth Configuration [hostname=" + bluetoothAddress + ", password=" + password
                + ", refreshInterval=" + refreshInterval + "]";
    }
}
