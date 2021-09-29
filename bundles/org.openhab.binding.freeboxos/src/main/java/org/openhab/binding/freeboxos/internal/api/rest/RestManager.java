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
package org.openhab.binding.freeboxos.internal.api.rest;

import static org.eclipse.jetty.http.HttpMethod.*;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;

/**
 * Base class for all various rest managers
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RestManager {
    protected static final String CONFIG_SUB_PATH = "config";
    protected static final String REBOOT_SUB_PATH = "reboot";

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

    private URI buildUri(String path) {
        return getUriBuilder().path(path).build();
    }

    @SuppressWarnings("null")
    public <F, T extends Response<List<F>>> List<F> getList(Class<T> classOfT, URI uri) throws FreeboxException {
        // GetList may return null object because API does not return anything for empty lists
        List<F> result = session.execute(uri, GET, classOfT, null);
        return result != null ? result : List.of();
    }

    public <F, T extends Response<F>> F get(Class<T> classOfT) throws FreeboxException {
        return session.execute(getUriBuilder().build(), GET, classOfT, null);
    }

    public <F, T extends Response<F>> F get(Class<T> classOfT, String path) throws FreeboxException {
        return session.execute(buildUri(path), GET, classOfT, null);
    }

    public void post(String path, Object payload) throws FreeboxException {
        session.execute(buildUri(path), POST, GenericResponse.class, payload);
    }

    public void post(String path) throws FreeboxException {
        session.execute(buildUri(path), POST, GenericResponse.class, null);
    }

    public <F, T extends Response<F>> F post(Class<T> classOfT, String path, Object payload) throws FreeboxException {
        return session.execute(buildUri(path), POST, classOfT, payload);
    }

    public <F, T extends Response<F>> F put(Class<T> classOfT, F payload) throws FreeboxException {
        return session.execute(getUriBuilder().build(), PUT, classOfT, payload);
    }

    public <F, T extends Response<F>> F put(Class<T> classOfT, String path, F payload) throws FreeboxException {
        return session.execute(buildUri(path), PUT, classOfT, payload);
    }

    private class GenericResponse extends Response<Object> {

    }
}
