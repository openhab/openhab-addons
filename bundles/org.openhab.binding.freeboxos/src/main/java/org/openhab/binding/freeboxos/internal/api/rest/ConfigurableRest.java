/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

/**
 * The {@link ConfigurableRest} is the Java class used to handle portions of the Api that accept to get and set
 * configuration based on a given DTO
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ConfigurableRest<T, Y extends Response<T>> extends RestManager {
    protected static final String CONFIG_PATH = "config";

    private final Class<Y> responseClazz;
    private final @Nullable String configPath;

    protected ConfigurableRest(FreeboxOsSession session, LoginManager.Permission required, Class<Y> responseClazz,
            UriBuilder uri, @Nullable String configPath) throws FreeboxException {
        super(session, required, uri);
        this.responseClazz = responseClazz;
        this.configPath = configPath;
    }

    public T getConfig() throws FreeboxException {
        return configPath != null ? getSingle(responseClazz, configPath) : getSingle(responseClazz);
    }

    protected T setConfig(T config) throws FreeboxException {
        return configPath != null ? put(responseClazz, config, configPath) : put(responseClazz, config);
    }
}
