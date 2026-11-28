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
package org.openhab.binding.homekit.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of the status values returned in a SetupEndpoints response.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum SetupEndpointsStatus {
    SUCCESS(0x00),
    BUSY(0x01),
    ERROR(0x02);

    public final int value;

    SetupEndpointsStatus(int value) {
        this.value = value;
    }

    public static SetupEndpointsStatus fromValue(int value) {
        for (SetupEndpointsStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown SetupEndpoints status: " + value);
    }
}
