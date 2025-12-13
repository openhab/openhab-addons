/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enumeration of HomeKit status codes.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum StatusCode {
    SUCCESS(0),
    INSUFFICIENT_PRIVILEDGES(-70401), // Request denied due to insufficient privileges.
    UNABLE_TO_PERFORM_OPERATION(-70402), // Unable to perform operation with requested service or characteristic
    RESOURCE_BUSY(-70403), // Resource is busy, try again.
    READ_ONLY(-70404), // Cannot write to read only characteristic.
    WRITE_ONLY(-70405), // Cannot read from a write only characteristic.
    NOTIFICATION_NOT_SUPPORTED(-70406), // Notification is not supported for characteristic.
    OUT_OF_RESOURCES(-70407), // Out of resources to process request.
    OPERATION_TIMEOUT(-70408), // Operation timed out.
    RESOURCE_DOES_NOT_EXIST(-70409), // Resource does not exist.
    INVALID_WRITE_VALUE(-70410), // Accessory received an invalid value in a write request.
    INSUFFICIENT_AUTHORIZATION(-70411);// Insufficient Authorization

    private final int code;

    StatusCode(int id) {
        this.code = id;
    }

    public @Nullable static StatusCode from(int code) {
        for (StatusCode value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
