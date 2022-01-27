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
package org.openhab.binding.modbus.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for poller thing
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusPollerConfiguration {
    private long refresh;
    private int start;
    private int length;
    private @Nullable String type;
    private int maxTries = 3;// backwards compatibility and tests
    private long cacheMillis = 50L;

    /**
     * Gets refresh period in milliseconds
     */
    public long getRefresh() {
        return refresh;
    }

    /**
     * Sets refresh period in milliseconds
     */

    public void setRefresh(long refresh) {
        this.refresh = refresh;
    }

    /**
     * Get address of the first register, coil, or discrete input to poll. Input as zero-based index number.
     *
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets address of the first register, coil, or discrete input to poll. Input as zero-based index number.
     *
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Gets number of registers, coils or discrete inputs to read.
     */
    public int getLength() {
        return length;
    }

    /**
     * Sets number of registers, coils or discrete inputs to read.
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Gets type of modbus items to poll
     *
     */
    public @Nullable String getType() {
        return type;
    }

    /**
     * Sets type of modbus items to poll
     *
     */
    public void setType(String type) {
        this.type = type;
    }

    public int getMaxTries() {
        return maxTries;
    }

    public void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }

    /**
     * Gets time to cache data.
     *
     * This is used for reusing cached data with explicit refresh calls.
     */
    public long getCacheMillis() {
        return cacheMillis;
    }

    /**
     * Sets time to cache data, in milliseconds
     *
     */
    public void setCacheMillis(long cacheMillis) {
        this.cacheMillis = cacheMillis;
    }
}
