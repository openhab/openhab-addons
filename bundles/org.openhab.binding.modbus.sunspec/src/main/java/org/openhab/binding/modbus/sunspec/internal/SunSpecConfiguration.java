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
package org.openhab.binding.modbus.sunspec.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SunSpecConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Nagy Attila Gábor - Initial contribution
 */
@NonNullByDefault
public class SunSpecConfiguration {

    /**
     * Refresh interval in seconds
     */
    private long refresh;

    private int maxTries = 3;// backwards compatibility and tests

    /**
     * Base address of the block to parse. Only used at manual setup
     */
    private int address;

    /**
     * Length of the block to parse. Only used at manual setup
     */
    private int length;

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

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
}
