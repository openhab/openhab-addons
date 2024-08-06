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
package org.openhab.binding.worxlandroid.internal.codes;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WorxLandroidActionCodes} hosts action codes
 *
 * @author Nils - Initial contribution
 */
@NonNullByDefault
public enum WorxLandroidActionCodes {
    START(1, "start"),
    STOP(2, "stop"),
    HOME(3, "home"),
    ZONETRAINING(4, "zonetraining"),
    LOCK(5, "lock"),
    UNLOCK(6, "unlock");

    public final int code;
    public final String description;

    WorxLandroidActionCodes(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
