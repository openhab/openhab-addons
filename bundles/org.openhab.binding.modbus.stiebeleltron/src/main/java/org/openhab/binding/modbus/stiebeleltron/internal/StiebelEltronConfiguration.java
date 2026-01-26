/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.stiebeleltron.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link StiebelEltronConfiguration} class contains basic modbus configuration parameters for Stiebel Eltron Heat
 * Pump
 *
 * @author Paul Frank - Initial contribution
 * @author Thomas Burri - Small changes
 */
@NonNullByDefault
public class StiebelEltronConfiguration {

    /**
     * Poll interval in seconds. Increase this if you encounter connection errors
     */
    private long refresh = 5;

    /**
     * Number of retries before giving up reading from this thing
     */
    private int maxTries = 3;

    /**
     * Gets refresh period in milliseconds
     */
    public long getRefreshMillis() {
        return refresh * 1000;
    }

    /**
     * Gets the maximal retries number
     */
    public int getMaxTries() {
        return maxTries;
    }

    /**
     * Sets the maximal retries number
     */
    public void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }
}
