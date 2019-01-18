/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.astro.internal.config;

/**
 * Channel configuration from Eclipse SmartHome.
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
