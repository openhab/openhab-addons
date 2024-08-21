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
package org.openhab.io.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * The model representing a NEEO recipe urls (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoRecipeUrls {

    /**
     * The url identifier
     * Note: this URL doesn't seem to be valid
     */
    private final String identify;

    /** The 'power on' url for the recipe. */
    private final String setPowerOn;

    /** The 'power off' url for the recipe */
    private final String setPowerOff;

    /**
     * The url to query the state of the recipe
     * Note: this URL doesn't seem to be valid
     */
    private final String getPowerState;

    /**
     * Instantiates a new neeo recipe urls.
     *
     * @param identify the non-empty identify
     * @param setPowerOn the non-empty power on url
     * @param setPowerOff the non-empty power off url
     * @param getPowerState the non-empty power state url
     */
    public NeeoRecipeUrls(String identify, String setPowerOn, String setPowerOff, String getPowerState) {
        NeeoUtil.requireNotEmpty(identify, "identify cannot be null");
        NeeoUtil.requireNotEmpty(setPowerOn, "setPowerOn cannot be null");
        NeeoUtil.requireNotEmpty(setPowerOff, "setPowerOff cannot be null");
        NeeoUtil.requireNotEmpty(getPowerState, "getPowerState cannot be null");

        this.identify = identify;
        this.setPowerOn = setPowerOn;
        this.setPowerOff = setPowerOff;
        this.getPowerState = getPowerState;
    }

    /**
     * Gets the url identify
     *
     * @return the identify
     */
    public String getIdentify() {
        return identify;
    }

    /**
     * Gets the power on url
     *
     * @return the power on url
     */
    public String getSetPowerOn() {
        return setPowerOn;
    }

    /**
     * Gets the power off url
     *
     * @return the power off url
     */
    public String getSetPowerOff() {
        return setPowerOff;
    }

    /**
     * Gets the power state url
     *
     * @return the the power state url
     */
    public String getGetPowerState() {
        return getPowerState;
    }

    @Override
    public String toString() {
        return "NeeoRecipeUrls [identify=" + identify + ", setPowerOn=" + setPowerOn + ", setPowerOff=" + setPowerOff
                + ", getPowerState=" + getPowerState + "]";
    }
}
