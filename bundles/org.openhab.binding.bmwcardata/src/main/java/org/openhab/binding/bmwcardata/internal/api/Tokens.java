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
package org.openhab.binding.bmwcardata.internal.api;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The tokens of an authorized CarData session. The access token authorizes REST requests, while {@code gcid} and
 * {@code idToken} are the user name and password for the streaming broker.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public record Tokens(String accessToken, String refreshToken, String idToken, String gcid, Instant expiresAt) {

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    @Override
    public String toString() {
        // tokens are deliberately omitted
        return "Tokens [expiresAt=" + expiresAt + "]";
    }
}
