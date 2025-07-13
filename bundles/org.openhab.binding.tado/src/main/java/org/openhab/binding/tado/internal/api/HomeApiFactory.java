/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tado.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tado.swagger.codegen.api.GsonBuilderFactory;
import org.openhab.binding.tado.swagger.codegen.api.client.HomeApi;
import org.openhab.binding.tado.swagger.codegen.api.client.OAuthorizerV2;
import org.openhab.core.auth.client.oauth2.OAuthClientService;

import com.google.gson.Gson;

/**
 * Factory to create and configure {@link HomeApi} instances.
 *
 * @author Dennis Frommknecht - Initial contribution
 * @author Andrew Fiddian-Green - Use OAuthAuthorizerV2
 */
@NonNullByDefault
public class HomeApiFactory {

    public HomeApi create(OAuthClientService oAuthClientService) {
        Gson gson = GsonBuilderFactory.defaultGsonBuilder().create();
        OAuthorizerV2 authorizer = new OAuthorizerV2(oAuthClientService);
        return new HomeApi(gson, authorizer);
    }
}
