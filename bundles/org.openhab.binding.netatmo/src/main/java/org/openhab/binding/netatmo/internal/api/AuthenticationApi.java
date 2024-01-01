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
package org.openhab.binding.netatmo.internal.api;

import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.*;
import static org.openhab.core.auth.oauth2client.internal.Keyword.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.Scope;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;

/**
 * The {@link AuthenticationApi} handles oAuth2 authentication and token refreshing
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Jacob Laursen - Refactored to use standard OAuth2 implementation
 */
@NonNullByDefault
public class AuthenticationApi extends RestManager {
    public static final URI TOKEN_URI = getApiBaseBuilder(PATH_OAUTH, SUB_PATH_TOKEN).build();
    public static final URI AUTH_URI = getApiBaseBuilder(PATH_OAUTH, SUB_PATH_AUTHORIZE).build();

    private List<Scope> grantedScope = List.of();
    private @Nullable String authorization;

    public AuthenticationApi(ApiBridgeHandler bridge) {
        super(bridge, FeatureArea.NONE);
    }

    public void setAccessToken(@Nullable String accessToken) {
        if (accessToken != null) {
            authorization = "Bearer " + accessToken;
        } else {
            authorization = null;
        }
    }

    public void setScope(String scope) {
        grantedScope = Stream.of(scope.split(" ")).map(s -> Scope.valueOf(s.toUpperCase())).toList();
    }

    public void dispose() {
        authorization = null;
        grantedScope = List.of();
    }

    public Optional<String> getAuthorization() {
        return Optional.ofNullable(authorization);
    }

    public boolean matchesScopes(Set<Scope> requiredScopes) {
        return requiredScopes.isEmpty() || grantedScope.containsAll(requiredScopes);
    }

    public boolean isConnected() {
        return authorization != null;
    }

    public static UriBuilder getAuthorizationBuilder(String clientId) {
        return getApiBaseBuilder(PATH_OAUTH, SUB_PATH_AUTHORIZE).queryParam(CLIENT_ID, clientId)
                .queryParam(SCOPE, FeatureArea.ALL_SCOPES).queryParam(STATE, clientId);
    }
}
