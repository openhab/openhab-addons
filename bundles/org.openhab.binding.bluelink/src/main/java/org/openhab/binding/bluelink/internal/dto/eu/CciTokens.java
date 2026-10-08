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
package org.openhab.binding.bluelink.internal.dto.eu;

/**
 * Token set of the OneApp/CCI API, returned by the token and token-refresh endpoints and sent back as the
 * token-refresh request body.
 *
 * @author Carlo Dischler - Initial contribution
 */
public record CciTokens(String accessToken, String refreshToken, String nonCcsToken, String exchangeableAccessToken,
        String exchangeableRefreshToken, String nonCcsRefreshToken, String idToken) {
}
