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
package org.openhab.binding.knx.internal.monitor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Why a secured telegram could not be decrypted. The numbers are the error types the library hands to
 * {@code SecureApplicationLayer.securityFailure}; the first two cases the library drops without that call.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public enum NotDecryptedReason {
    /** No group key for the destination. */
    NO_KEY,
    /** The keyring lists the senders of the destination, and the source is not one of them. */
    SENDER_NOT_LISTED,
    /** Library error type 1. */
    INVALID_CONTROL_FIELD,
    /** Library error type 2. */
    SEQUENCE_NUMBER,
    /** Library error type 3. */
    AUTHENTICATION,
    /** Library error type 4. */
    ACCESS;

    /** {@return the reason for an error type of the library's hook, null for a type outside 1 to 4} */
    public static @Nullable NotDecryptedReason ofLibraryErrorType(int errorType) {
        return switch (errorType) {
            case 1 -> INVALID_CONTROL_FIELD;
            case 2 -> SEQUENCE_NUMBER;
            case 3 -> AUTHENTICATION;
            case 4 -> ACCESS;
            default -> null;
        };
    }
}
