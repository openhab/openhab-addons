/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.PermissionException;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * Base class for the various rest managers available through the API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RestManager {
    protected static final String REBOOT_ACTION = "reboot";
    protected static final String SYSTEM_PATH = "system";

    protected class GenericResponse extends Response<Object> {
    }

    private final UriBuilder uriBuilder;
    protected final FreeboxOsSession session;

    public RestManager(FreeboxOsSession session, LoginManager.Permission required, UriBuilder uri)
            throws FreeboxException {
        this.uriBuilder = uri;
        this.session = session;
        if (required != LoginManager.Permission.NONE && !session.hasPermission(required)) {
            throw new PermissionException(required, "Permission missing: %s", required.toString());
        }
    }

    protected UriBuilder getUriBuilder() {
        return uriBuilder.clone();
    }

    private URI buildUri(String... pathElements) {
        UriBuilder localBuilder = getUriBuilder();
        for (String path : pathElements) {
            localBuilder.path(path);
        }
        return localBuilder.build();
    }

    // Returns the first and supposed only element from the list. Presence of this element is expected and mandatory
    private <F> F controlSingleton(List<F> result) {
        if (result.size() == 1) {
            return result.get(0);
        }
        throw new IllegalArgumentException("Result is empty or not singleton");
    }

    protected <F, T extends Response<F>> List<F> get(Class<T> clazz, String... pathElements) throws FreeboxException {
        return session.execute(buildUri(pathElements), GET, clazz, null);
    }

    protected <F, T extends Response<F>> F getSingle(Class<T> clazz, String... pathElements) throws FreeboxException {
        return controlSingleton(get(clazz, pathElements));
    }

    protected <F, T extends Response<F>> F post(Object payload, Class<T> clazz, String... pathElements)
            throws FreeboxException {
        return controlSingleton(session.execute(buildUri(pathElements), POST, clazz, payload));
    }

    protected void post(String... pathElements) throws FreeboxException {
        post(null, pathElements);
    }

    protected <F> void post(@Nullable F payload, String... pathElements) throws FreeboxException {
        session.execute(buildUri(pathElements), POST, GenericResponse.class, payload);
    }

    protected <F, T extends Response<F>> F put(Class<T> clazz, F payload, String... pathElements)
            throws FreeboxException {
        return controlSingleton(session.execute(buildUri(pathElements), PUT, clazz, payload));
    }

    protected <F, T extends Response<F>> void put(F payload, String... pathElements) throws FreeboxException {
        session.execute(buildUri(pathElements), PUT, GenericResponse.class, payload);
    }
}
