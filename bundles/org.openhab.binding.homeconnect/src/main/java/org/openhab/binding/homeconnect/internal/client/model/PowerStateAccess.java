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
package org.openhab.binding.homeconnect.internal.client.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PowerStateAccess} enum defines the access types for the power state of the device.
 *
 * @author Philipp Schneider - Initial contribution
 *
 */
@NonNullByDefault
public enum PowerStateAccess {

    READ_ONLY,

    READ_WRITE;

    public static PowerStateAccess fromString(String access) {
        switch (access.toLowerCase()) {
            case "read":
                return READ_ONLY;
            case "readwrite":
                return READ_WRITE;
            default:
                // Default to READ_ONLY if the access type is not recognized
                return READ_ONLY;
        }
    }
}
