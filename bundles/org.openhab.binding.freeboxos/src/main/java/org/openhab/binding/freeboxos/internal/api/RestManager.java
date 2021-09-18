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

import static org.eclipse.jetty.http.HttpMethod.*;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;

/**
 * Base class for all various rest managers
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RestManager {
    private final UriBuilder uriBuilder;
    protected FreeboxOsSession session;

    public RestManager(String path, FreeboxOsSession session) {
        this.uriBuilder = session.getUriBuilder().path(path);
        this.session = session;
    }

    public RestManager(String path, FreeboxOsSession session, Permission required) throws FreeboxException {
        this(path, session);
        if (!session.hasPermission(required)) {
            throw new FreeboxException("Permission missing : " + required.toString());
        }
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
        return session.executeList(uri, GET, retryAuth, 3, classOfT, null);
    }

    public <F, T extends ListResponse<F>> List<F> getList(Class<T> classOfT, boolean retryAuth)
            throws FreeboxException {
        return getList(buildUri(null), classOfT, retryAuth);
    }

    public <F, T extends Response<F>> F get(@Nullable String path, @Nullable Class<T> classOfT, boolean retryAuth)
            throws FreeboxException {
        return session.execute(buildUri(path), GET, retryAuth, 3, classOfT, null);
    }

    public void post(String path, Object payload) throws FreeboxException {
        session.execute(buildUri(path), POST, true, 3, GenericResponse.class, payload);
    }

    public void post(String path) throws FreeboxException {
        session.execute(buildUri(path), POST, true, 3, GenericResponse.class, null);
    }

    public <F, T extends Response<F>> F post(Class<T> classOfT, @Nullable String path, @Nullable Object payload)
            throws FreeboxException {
        return session.execute(buildUri(path), POST, true, 3, classOfT, payload);
    }

    public <F, T extends Response<F>> F put(Class<T> classOfT, @Nullable String path, Object payload)
            throws FreeboxException {
        return session.execute(buildUri(path), PUT, true, 3, classOfT, payload);
    }

    private class GenericResponse extends Response<Object> {

    }
}
