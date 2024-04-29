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
package org.openhab.binding.smaenergymeter.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link EnergyMeterConfig} class holds the configuration properties of the binding.
 *
 * @author Osman Basha - Initial contribution
 */
@NonNullByDefault
public class EnergyMeterConfig {

    private @Nullable String mcastGroup;
    private int port = 9522;
    private int pollingPeriod = 30;
    private @Nullable String serialNumber;

    public @Nullable String getMcastGroup() {
        return mcastGroup;
    }

    public void setMcastGroup(String mcastGroup) {
        this.mcastGroup = mcastGroup;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPollingPeriod() {
        return pollingPeriod;
    }

    public void setPollingPeriod(int pollingPeriod) {
        this.pollingPeriod = pollingPeriod;
    }

    public @Nullable String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
