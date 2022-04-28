/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.eclipse.jetty.http.HttpMethod.POST;
import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.*;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.Scope;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;

/**
 * Base class for all various rest managers
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class RestManager {
    private static final UriBuilder API_BASE_BUILDER = UriBuilder.fromUri(URL_API);
    private static final UriBuilder APP_URI_BUILDER = UriBuilder.fromUri(URL_APP).path(PATH_API);
    private static final UriBuilder API_URI_BUILDER = getApiBaseBuilder().path(PATH_API);

    private final Set<Scope> requiredScopes;
    private final ApiBridgeHandler apiBridge;

    public RestManager(ApiBridgeHandler apiBridge, FeatureArea features) {
        this.requiredScopes = features.scopes;
        this.apiBridge = apiBridge;
    }

    protected <T extends ApiResponse<?>> T get(UriBuilder uriBuilder, Class<T> clazz) throws NetatmoException {
        return executeUri(uriBuilder, HttpMethod.GET, clazz, null, null);
    }

    protected <T extends ApiResponse<?>> T post(UriBuilder uriBuilder, Class<T> clazz, @Nullable String payload,
            @Nullable String contentType) throws NetatmoException {
        return executeUri(uriBuilder, HttpMethod.POST, clazz, payload, contentType);
    }

    protected <T> T post(URI uri, Class<T> clazz, Map<String, String> entries) throws NetatmoException {
        return apiBridge.executeUri(uri, POST, clazz, toRequest(entries),
                "application/x-www-form-urlencoded;charset=UTF-8", 3);
    }

    private <T extends ApiResponse<?>> T executeUri(UriBuilder uriBuilder, HttpMethod method, Class<T> clazz,
            @Nullable String payload, @Nullable String contentType) throws NetatmoException {
        URI uri = uriBuilder.build();
        T response = apiBridge.executeUri(uri, method, clazz, payload, contentType, 3);
        if (response instanceof ApiResponse.Ok && ((ApiResponse.Ok) response).failed()) {
            throw new NetatmoException("Command failed : %s for uri : %s", response.getStatus(), uri.toString());
        }
        return response;
    }

    private static UriBuilder appendParams(UriBuilder builder, @Nullable Object... params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("appendParams : params count must be even");
        }
        for (int i = 0; i < params.length; i += 2) {
            Object query = params[i];
            if (query instanceof String) {
                Object param = params[i + 1];
                if (param != null) {
                    builder.queryParam((String) query, param);
                }
            } else {
                throw new IllegalArgumentException("appendParams : even parameters must be Strings");
            }
        }
        return builder;
    }

    protected static UriBuilder getApiBaseBuilder() {
        return API_BASE_BUILDER.clone();
    }

    public static UriBuilder getApiUriBuilder(String path, @Nullable Object... params) {
        return appendParams(API_URI_BUILDER.clone().path(path), params);
    }

    protected static UriBuilder getAppUriBuilder(String path, @Nullable Object... params) {
        return appendParams(APP_URI_BUILDER.clone().path(path), params);
    }

    private String toRequest(Map<String, String> entries) {
        return entries.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
    }

    public Set<Scope> getRequiredScopes() {
        return requiredScopes;
    }
}
