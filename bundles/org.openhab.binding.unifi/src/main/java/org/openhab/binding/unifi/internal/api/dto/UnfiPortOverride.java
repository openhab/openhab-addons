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
package org.openhab.binding.unifi.internal.api.dto;

import com.google.gson.annotations.Expose;

/**
 * The {@link UnfiPortOverride} represents the data model of UniFi port override.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class UnfiPortOverride {

    @Expose
    private int portIdx;

    @Expose
    private String portconfId;

    @Expose
    private String poeMode;

    public UnfiPortOverride() {
        // Constructor for GSON.
    }

    public UnfiPortOverride(final int portIdx, final String portconfId, final String poeMode) {
        this.portIdx = portIdx;
        this.portconfId = portconfId;
        this.poeMode = poeMode;
    }

    public int getPortIdx() {
        return portIdx;
    }

    public String getPortconfId() {
        return portconfId;
    }

    public String getPoeMode() {
        return poeMode;
    }

    public void setPortIdx(final int portIdx) {
        this.portIdx = portIdx;
    }

    public void setPortconfId(final String portconfId) {
        this.portconfId = portconfId;
    }

    public void setPoeMode(final String poeMode) {
        this.poeMode = poeMode;
    }

    @Override
    public String toString() {
        return String.format("UnfiPortOverride{portIx: '%d', portconfId: '%s', poeMode: '%s'}", portIdx, portconfId,
                poeMode);
    }
}
