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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents a broadcast seek and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SeekBroadcastStation_1_0 {
    /** Constant for forward direction */
    public static final String DIR_FWD = "fwd";

    /** Constant for backward direction */
    public static final String DIR_BWD = "bwd";

    /** Constant for auto tuning */
    public static final String TUN_AUTO = "auto";

    /** Constant for manual tuning (step tuning) */
    public static final String TUN_MANUAL = "manual";

    /** The direction of the seek */
    private final String direction;

    /** The type of tuning during the seek */
    private final String tuning;

    /**
     * Constructs the seek from the parms
     * 
     * @param direction true for forward seek, false for backward
     * @param tuning true for autotune, false for manual
     */
    public SeekBroadcastStation_1_0(final boolean direction, final boolean tuning) {
        this.direction = direction ? DIR_FWD : DIR_BWD;
        this.tuning = tuning ? TUN_AUTO : TUN_MANUAL;
    }

    /**
     * The direction of the seek
     * 
     * @return the direction of the seek
     */
    public String getDirection() {
        return direction;
    }

    /**
     * The type of seek tuning
     * 
     * @return the type of seek tuning
     */
    public String getTuning() {
        return tuning;
    }
}
