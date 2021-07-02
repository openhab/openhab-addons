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
package org.openhab.binding.freeboxos.internal.api;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;

/**
 * Base class for all various rest managers
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RestManager {
    private final ApiHandler apiHandler;
    private final UriBuilder uriBuilder;

    public RestManager(ApiHandler apiHandler, String path) {
        this.apiHandler = apiHandler;
        this.uriBuilder = apiHandler.getUriBuilder().path(path);
    }

    protected UriBuilder getUriBuilder() {
        return uriBuilder.clone();
    }

    private URI buildUri(@Nullable String path) {
        UriBuilder builder = getUriBuilder();
        if (path != null) {
            builder.path(path);
        }
        return builder.build();
    }

    public <F, T extends ListResponse<F>> List<F> getList(URI uri, Class<T> classOfT, boolean retryAuth)
            throws FreeboxException {
        return apiHandler.executeList(uri, HttpMethod.GET, null, classOfT, retryAuth);
    }

    public <F, T extends ListResponse<F>> List<F> getList(Class<T> classOfT, boolean retryAuth)
            throws FreeboxException {
        return apiHandler.executeList(buildUri(null), HttpMethod.GET, null, classOfT, retryAuth);
    }

    public <F, T extends Response<F>> F get(@Nullable String path, @Nullable Class<T> classOfT, boolean retryAuth)
            throws FreeboxException {
        return apiHandler.execute(buildUri(path), HttpMethod.GET, null, classOfT, retryAuth);
    }

    public void post(String path, @Nullable Object payload) throws FreeboxException {
        apiHandler.execute(buildUri(path), HttpMethod.POST, payload, null, true);
    }

    public <F, T extends Response<F>> F post(String path, @Nullable Object payload, Class<T> classOfT)
            throws FreeboxException {
        return apiHandler.execute(buildUri(path), HttpMethod.POST, payload, classOfT, true);
    }

    public <F, T extends Response<F>> F put(@Nullable String path, Object payload, Class<T> classOfT)
            throws FreeboxException {
        return apiHandler.execute(buildUri(path), HttpMethod.PUT, payload, classOfT, true);
    }
}
