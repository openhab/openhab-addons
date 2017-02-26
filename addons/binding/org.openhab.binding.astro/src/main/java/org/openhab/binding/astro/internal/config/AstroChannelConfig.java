/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal.config;

/**
 * Channel configuration from openHab.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public class AstroChannelConfig {
    private Integer offset;
    private String earliest;
    private String latest;

    /**
     * Returns the offset.
     */
    public Integer getOffset() {
        return offset;
    }

    /**
     * Sets the offset.
     */
    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    /**
     * Returns the earliest time.
     */
    public String getEarliest() {
        return earliest;
    }

    /**
     * Sets the earliest time.
     */
    public void setEarliest(String earliest) {
        this.earliest = earliest;
    }

    /**
     * Returns the latest time.
     */
    public String getLatest() {
        return latest;
    }

    /**
     * Sets the latest time.
     */
    public void setLatest(String latest) {
        this.latest = latest;
    }

}
