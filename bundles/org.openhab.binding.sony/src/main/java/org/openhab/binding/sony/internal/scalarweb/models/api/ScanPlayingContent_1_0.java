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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents the scan direction and is used for serialization
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScanPlayingContent_1_0 {
    /** Constant for a forward direction scan */
    public static final String DIR_FWD = "fwd";

    /** Constant for a backward direction scan */
    public static final String DIR_BWD = "bwd";

    /** The direction of the scan */
    private final String direction;

    /** The output to use */
    private final String output;

    /**
     * Constructs the scan
     * 
     * @param fwd whether to scan forward (true) or not (false)
     * @param output the non-null, possibly empty (for default) output to use
     */
    public ScanPlayingContent_1_0(final boolean fwd, final String output) {
        Objects.requireNonNull(output, "output cannot be null");

        this.direction = fwd ? DIR_FWD : DIR_BWD;
        this.output = output;
    }

    /**
     * Get's the direction
     * 
     * @return the direction
     */
    public String getDirection() {
        return direction;
    }

    /**
     * Get's the output
     * 
     * @return the output
     */
    public String getOutput() {
        return output;
    }
}
