/*
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
package org.openhab.binding.modbus.lambda.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HeatpumpConfiguration} class contains fields mapping
 * thing configuration parameters.
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 */
@NonNullByDefault
public class HeatpumpConfiguration {

    /**
     * Refresh interval in seconds
     */
    private int refresh = 30;

    private int maxTries = 3;

    // backwards compatibility and tests

    /**
     * Subindex to calculate the base adress of the modbus registers
     */
    private int subindex = 0;

    /**
     * Gets refresh period in milliseconds
     */
    public long getRefreshMillis() {
        return refresh * 1000;
    }

    public int getMaxTries() {
        return maxTries;
    }

    public void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }

    public int getSubindex() {
        return subindex;
    }

    public void setSubindex(int subindex) {
        this.subindex = subindex;
    }
}
