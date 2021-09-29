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

    public ActivableRest(String path, @Nullable String configPath, FreeboxOsSession session, Class<Y> classOfResponse) {
        super(path, configPath, session, classOfResponse);
    }

    public ActivableRest(String path, @Nullable String configPath, FreeboxOsSession session, Permission required,
            Class<Y> classOfResponse) throws FreeboxException {
        super(path, configPath, session, required, classOfResponse);
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
