/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal.rscp;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Error codes an E3/DC device returns; {@link #UNKNOWN_TAG} means the data point is not implemented.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public enum RscpError {

    NOT_HANDLED(0x01),
    ACCESS_DENIED(0x02),
    FORMAT(0x03),
    AGAIN(0x04),
    OUT_OF_BOUNDS(0x05),
    NOT_AVAILABLE(0x06),
    UNKNOWN_TAG(0x07),
    ALREADY_IN_USE(0x08),
    UNKNOWN(-1);

    private final int code;

    RscpError(int code) {
        this.code = code;
    }

    /**
     * The wire code of the error.
     */
    public int getCode() {
        return code;
    }

    /**
     * The error of the given wire code.
     */
    public static RscpError fromCode(int code) {
        for (RscpError error : values()) {
            if (error.code == code) {
                return error;
            }
        }
        return UNKNOWN;
    }
}
