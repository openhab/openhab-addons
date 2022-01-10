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
package org.openhab.binding.mielecloud.internal.config.exception;

import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception thrown when there already is an ongoing authorization process.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class OngoingAuthorizationException extends RuntimeException {
    private static final long serialVersionUID = -6742384930140134244L;

    @Nullable
    private final LocalDateTime ongoingAuthorizationExpiryTimestamp;

    /**
     * Creates a new {@link OngoingAuthorizationException}.
     *
     * @param message Exception message.
     * @param ongoingAuthorizationExpiryTimestamp Timestamp when the ongoing authorization will expire.
     */
    public OngoingAuthorizationException(String message, @Nullable LocalDateTime ongoingAuthorizationExpiryTimestamp) {
        super(message);
        this.ongoingAuthorizationExpiryTimestamp = ongoingAuthorizationExpiryTimestamp;
    }

    /**
     * Gets the timestamp representing when the ongoing authorization will expire.
     */
    @Nullable
    public LocalDateTime getOngoingAuthorizationExpiryTimestamp() {
        return ongoingAuthorizationExpiryTimestamp;
    }
}
