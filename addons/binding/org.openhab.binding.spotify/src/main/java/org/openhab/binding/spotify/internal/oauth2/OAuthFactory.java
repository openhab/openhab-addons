/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.oauth2;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.osgi.service.component.annotations.Component;

/**
 * Factory class to create a {@link OAuthClientService}. This class is modeled after the ESH OAuth2 service.
 * This class will be removed when the ESH OAuth2 service can be used with this binding.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@Component(service = OAuthFactory.class, immediate = true, configurationPid = "binding.spotify.oauthfactory")
@NonNullByDefault
public class OAuthFactory {

    public OAuthClientService getOrCreateOAuthClientService(String handle, String spotifyApiTokenUrl,
            String spotifyAuthorizeUrl, String clientId, String clientSecret, String spotifyScopes,
            boolean supportsBasicAuth, ScheduledExecutorService scheduler, HttpClient httpClient, String refreshToken) {
        OAuthClientService service = new OAuthClientService(clientId, clientSecret, scheduler, httpClient);
        AccessTokenResponse acr = new AccessTokenResponse();
        acr.setRefreshToken(refreshToken);
        service.importAccessTokenResponse(acr);
        return service;
    }

    public void ungetOAuthService(String handle) {
        // No implementation in this factory
    }
}
