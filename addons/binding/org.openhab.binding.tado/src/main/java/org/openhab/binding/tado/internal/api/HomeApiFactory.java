/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.internal.api;

import org.openhab.binding.tado.internal.api.auth.Authorizer;
import org.openhab.binding.tado.internal.api.auth.OAuthAuthorizer;
import org.openhab.binding.tado.internal.api.client.HomeApi;

import com.google.gson.Gson;

/**
 * Factory to create and configure {@link HomeApi} instances.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class HomeApiFactory {
    private static final String OAUTH_SCOPE = "home.user";
    private static final String OAUTH_CLIENT_ID = "public-api-preview";
    private static final String OAUTH_CLIENT_SECRET = "4HJGRffVR8xb3XdEUQpjgZ1VplJi6Xgw";

    public HomeApi create(String username, String password) {
        Gson gson = GsonBuilderFactory.defaultGsonBuilder().create();
        Authorizer authorizer = new OAuthAuthorizer().passwordFlow(username, password).clientId(OAUTH_CLIENT_ID)
                .clientSecret(OAUTH_CLIENT_SECRET).scopes(OAUTH_SCOPE);
        return new HomeApi(gson, authorizer);
    }
}
