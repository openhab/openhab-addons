/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.NAAccessTokenResponse;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.GrantType;
import org.openhab.binding.netatmo.internal.config.NetatmoBindingConfiguration;
import org.openhab.core.auth.client.oauth2.OAuthFactory;

/**
 * Allows access to the AutomowerConnectApi
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ConnectApi extends RestManager {
    private static final String TOKEN_URL = "oauth2/token";
    private static final String GRANT_BASE = "grant_type=%s&client_id=%s&client_secret=%s";
    private static final String TOKEN_REQ = "&username=%s&password=%s&scope=%s";
    private static final String TOKEN_REF = "&refresh_token=%s";

    private final NetatmoBindingConfiguration configuration;

    // TODO : finalize once #1888 over
    // private final OAuthClientService authService;

    public ConnectApi(ApiBridge apiClient, OAuthFactory oAuthFactory, NetatmoBindingConfiguration configuration) {
        super(apiClient, Set.of());
        this.configuration = configuration;
        // TODO : finalize once #1888 over
        // this.authService = oAuthFactory.createOAuthClientService(SERVICE_PID, NETATMO_BASE_URL + TOKEN_URL, null,
        // configuration.clientId != null ? configuration.clientId : "", configuration.clientSecret, scope, false)
        // .withDeserializer(NAOAuthDeserializer.class);
        // this.loginManager = new LoginManager(apiClient, configuration);
    }

    public NAAccessTokenResponse authenticate() throws NetatmoException {
        // TODO : finalize once #1888 over
        // try {
        // AccessTokenResponse result = authService.getAccessTokenResponse();
        // if (result == null) {
        // result = authService.getAccessTokenByResourceOwnerPasswordCredentials(userName, password, scope);
        // }
        // return result;
        // } catch (OAuthException | IOException | OAuthResponseException e) {
        // throw new NetatmoException("Unable to authenticate", e);
        // }
        // return loginManager.openSession();
        String req = getBaseRequest(GrantType.PASSWORD);

        List<String> scopes = new ArrayList<>();
        NetatmoConstants.ALL_SCOPES.forEach(scope -> scopes.add(scope.name().toLowerCase()));

        req += String.format(TOKEN_REQ, configuration.username, configuration.password, String.join(" ", scopes));

        NAAccessTokenResponse authorization = apiHandler.post(TOKEN_URL, req, NAAccessTokenResponse.class, true);

        apiHandler.onAccessTokenResponse(authorization.getAccessToken(), authorization.getScope());
        return authorization;
    }

    private String getBaseRequest(GrantType type) {
        return String.format(GRANT_BASE, type.name().toLowerCase(), configuration.clientId, configuration.clientSecret);
    }

    public void refreshToken(String refreshToken) throws NetatmoException {
        String req = getBaseRequest(GrantType.REFRESH_TOKEN);
        req += String.format(TOKEN_REF, refreshToken);
        NAAccessTokenResponse authorization = apiHandler.post(TOKEN_URL, req, NAAccessTokenResponse.class, true);
        apiHandler.onAccessTokenResponse(authorization.getAccessToken(), authorization.getScope());
    }
}
