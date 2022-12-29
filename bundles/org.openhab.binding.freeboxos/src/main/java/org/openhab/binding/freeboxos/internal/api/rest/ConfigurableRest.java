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
package org.openhab.binding.freeboxos.internal.api.rest;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;

/**
 * The {@link ConfigurableRest} is the Java class used to handle portions of the
 * Api that accept to get and set configuration
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ConfigurableRest<T, Y extends Response<T>> extends RestManager {
    private final Class<Y> responseClazz;
    private final @Nullable String configPath;

    public ConfigurableRest(FreeboxOsSession session, Class<Y> responseClazz, String path,
            @Nullable String configPath) {
        super(session, path);
        this.responseClazz = responseClazz;
        this.configPath = configPath;
    }

    public ConfigurableRest(FreeboxOsSession session, Class<Y> responseClazz, UriBuilder parentUri, String path,
            @Nullable String configPath) {
        super(session, parentUri, path);
        this.responseClazz = responseClazz;
        this.configPath = configPath;
    }

    public ConfigurableRest(FreeboxOsSession session, Class<Y> responseClazz, Permission required, String path,
            @Nullable String configPath) throws FreeboxException {
        super(session, required, path);
        this.responseClazz = responseClazz;
        this.configPath = configPath;
    }

    public T getConfig() throws FreeboxException {
        return configPath != null ? get(responseClazz, configPath) : get(responseClazz);
    }

    public T setConfig(T config) throws FreeboxException {
        return configPath != null ? put(responseClazz, config, configPath) : put(responseClazz, config);
    }
}
