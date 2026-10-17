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
package org.openhab.binding.melcloud.internal.home.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Persisted MELCloud Home authentication state.
 *
 * <p>
 * Only the refresh token is persisted, via {@code StorageService}; the short-lived access token is kept in memory
 * only and is re-derived on every restart via
 * {@link org.openhab.binding.melcloud.internal.home.api.MelCloudHomeAuthService#refreshToken(String)}.
 *
 * <p>
 * The username records which account the refresh token belongs to. Without it, a session persisted for one account
 * would still be resumed after the bridge is reconfigured with another account's credentials.
 *
 * @param username the account the refresh token belongs to; {@code null} for state persisted by an earlier version
 * @param refreshToken the last known MELCloud Home OAuth refresh token
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
record MelCloudHomeAuthState(@Nullable String username, String refreshToken) {

    @Override
    public String toString() {
        return "MelCloudHomeAuthState[username=<redacted>, refreshToken=<redacted>]";
    }
}
