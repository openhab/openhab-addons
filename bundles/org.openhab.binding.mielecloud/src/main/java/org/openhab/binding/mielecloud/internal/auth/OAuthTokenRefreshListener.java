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
package org.openhab.binding.mielecloud.internal.auth;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listener that is invoked when an OAuth 2 access token was refreshed.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public interface OAuthTokenRefreshListener {
    /**
     * Invoked when a new access token becomes available.
     *
     * @param accessToken The new access token.
     */
    void onNewAccessToken(String accessToken);
}
