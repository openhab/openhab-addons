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
 * The {@link ActivableRest} is the Java class used to handle portions of the
 * Api that accept to get and set configuration
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ActivableRest<T extends ActivableConfig, Y extends Response<T>> extends ConfigurableRest<T, Y> {

    public ActivableRest(FreeboxOsSession session, Class<Y> classOfResponse, String path, String configPath) {
        super(session, classOfResponse, path, configPath);
    }

    public ActivableRest(FreeboxOsSession session, Class<Y> classOfResponse, UriBuilder parentUri, String path,
            @Nullable String configPath) {
        super(session, classOfResponse, parentUri, path, configPath);
    }

    public ActivableRest(FreeboxOsSession session, Permission required, Class<Y> classOfResponse, String path,
            @Nullable String configPath) throws FreeboxException {
        super(session, classOfResponse, required, path, configPath);
    }

    public boolean getStatus() throws FreeboxException {
        return getConfig().isEnabled();
    }

    public boolean setStatus(boolean enabled) throws FreeboxException {
        T config = getConfig();
        config.setEnabled(enabled);
        return setConfig(config).isEnabled();
    }
}
