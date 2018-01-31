/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.sunspec.internal;

/**
 * The {@link SunSpecConfiguration} class contains fields mapping thing configuration paramters.
 *
 * @author Nagy Attila Gabor - Initial contribution
 */
public class SunSpecConfiguration {

    /**
     * Refresh interval in seconds
     */
    private long refresh;

    private int maxTries = 3;// backwards compatibility and tests

    /**
     * Base address of the block to parse. Only used at manual setup
     */
    private Integer address;

    /**
     * Length of the block to parse. Only used at manual setup
     */
    private Integer length;

    public Integer getAddress() {
        return address;
    }

    public void setAddress(Integer address) {
        this.address = address;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * Gets refresh period in milliseconds
     */
    public long getRefreshMillis() {
        return refresh * 1000;
    }

    /**
     * Sets refresh period in milliseconds
     */
    public void setRefreshMillis(long refresh) {
        this.refresh = refresh / 1000;
    }

    public int getMaxTries() {
        return maxTries;
    }

    public void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }

}
