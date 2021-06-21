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

import static org.openhab.binding.netatmo.internal.api.NetatmoConstants.*;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.Scope;

/**
 * Base class for all various rest managers
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class RestManager {
    private static final UriBuilder API_BASE_BUILDER = UriBuilder.fromUri(URL_API);
    private static final UriBuilder API_URI_BUILDER = API_BASE_BUILDER.clone().path(PATH_API);
    private static final UriBuilder APP_URI_BUILDER = UriBuilder.fromUri(URL_APP).path(PATH_API);
    protected static final URI OAUTH_URI = API_BASE_BUILDER.clone().path(PATH_OAUTH).build();

    private final Set<Scope> requiredScopes;
    protected final ApiBridge apiHandler;

    // public RestManager(ApiBridge apiHandler) {
    // this(apiHandler, Collections.emptySet());
    // }

    // public RestManager(ApiBridge apiHandler, Set<Scope> requiredScopes) {
    // this.apiHandler = apiHandler;
    // this.requiredScopes = requiredScopes;
    // }

    public RestManager(ApiBridge apiHandler, FeatureArea features) {
        this.apiHandler = apiHandler;
        this.requiredScopes = features.getScopes();
    }

    public <T extends ApiResponse<?>> T get(UriBuilder uriBuilder, Class<T> classOfT) throws NetatmoException {
        return executeUri(uriBuilder, HttpMethod.GET, classOfT, null);
    }

    public <T extends ApiResponse<?>> T post(UriBuilder uriBuilder, Class<T> classOfT, @Nullable String payload)
            throws NetatmoException {
        return executeUri(uriBuilder, HttpMethod.POST, classOfT, payload);
    }

    private <T extends ApiResponse<?>> T executeUri(UriBuilder uriBuilder, HttpMethod method, Class<T> classOfT,
            @Nullable String payload) throws NetatmoException {
        T response = apiHandler.executeUri(uriBuilder.build(), HttpMethod.POST, classOfT, payload);
        if (response instanceof ApiResponse.Ok) {
            ApiResponse.Ok okResponse = (ApiResponse.Ok) response;
            if (!okResponse.isSuccess()) {
                throw new NetatmoException(String.format("Unsuccessfull command : %s for uri : %s",
                        response.getStatus(), uriBuilder.build().toString()));
            }
        }
        return response;
    }

    protected UriBuilder getApiUriBuilder() {
        return API_URI_BUILDER.clone();
    }

    protected UriBuilder getAppUriBuilder() {
        return APP_URI_BUILDER.clone();
    }

    public Set<Scope> getRequiredScopes() {
        return requiredScopes;
    }
}
