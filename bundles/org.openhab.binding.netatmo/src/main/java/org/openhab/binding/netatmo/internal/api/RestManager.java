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
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.Scope;

/**
 * Base class for all various rest managers
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class RestManager {
    private static final UriBuilder API_BASE_BUILDER = UriBuilder.fromUri(NA_API_URL);
    private static final UriBuilder API_URI_BUILDER = API_BASE_BUILDER.clone().path(NA_API_PATH);
    private static final UriBuilder APP_URI_BUILDER = UriBuilder.fromUri(NA_APP_URL).path(NA_API_PATH);
    protected static final URI OAUTH_URI = API_BASE_BUILDER.clone().path(NA_OAUTH_PATH).build();

    private final Set<Scope> requiredScopes;
    protected final ApiBridge apiHandler;

    public RestManager(ApiBridge apiHandler) {
        this(apiHandler, Collections.emptySet());
    }

    public RestManager(ApiBridge apiHandler, Set<Scope> requiredScopes) {
        this.apiHandler = apiHandler;
        this.requiredScopes = requiredScopes;
    }

    public <T extends ApiResponse<?>> T get(UriBuilder uriBuilder, Class<T> classOfT) throws NetatmoException {
        return apiHandler.executeUri(uriBuilder.build(), HttpMethod.GET, classOfT, null);
    }

    public <T extends ApiResponse<?>> T post(UriBuilder uriBuilder, Class<T> classOfT, @Nullable String payload)
            throws NetatmoException {
        T response = apiHandler.executeUri(uriBuilder.build(), HttpMethod.POST, classOfT, payload);
        if (response instanceof ApiOkResponse) {
            ApiOkResponse okResponse = (ApiOkResponse) response;
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
