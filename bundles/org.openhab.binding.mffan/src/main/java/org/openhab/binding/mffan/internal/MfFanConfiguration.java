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
package org.openhab.binding.mffan.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MfFanConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Mark Brooks - Initial contribution
 */
@NonNullByDefault
public class MfFanConfiguration {

    private String ipAddress;
    private Integer pollingPeriod;

    public MfFanConfiguration() {
        this.ipAddress = "";
        this.pollingPeriod = 120;
    }

    public String getIpAddress() {
        return this.ipAddress.trim();
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPollingPeriod() {
        return this.pollingPeriod;
    }

    public void setPollingPeriod(Integer pollingPeriod) {
        this.pollingPeriod = pollingPeriod;
    }

    public static boolean validateConfig(@Nullable MfFanConfiguration config) {
        if (config == null || config.getIpAddress().isBlank()) {
            return false;
        }
        return config.getPollingPeriod() >= 10;
    }
}
