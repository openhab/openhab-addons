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
 * The {@link AfpManager} is the Java class used to handle api requests related to Afp shares
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AfpManager extends ConfigurableRest<AfpManager.Afp, AfpManager.ConfigResponse> {
    private static final String AFP_PATH = "afp";

    protected static class ConfigResponse extends Response<Afp> {
    }

    protected static record Afp(boolean enabled, boolean guestAllow, ServerType serverType, @Nullable String loginName,
            @Nullable String loginPassword) {
        private enum ServerType {
            POWERBOOK,
            POWERMAC,
            MACMINI,
            IMAC,
            MACBOOK,
            MACBOOKPRO,
            MACBOOKAIR,
            MACPRO,
            APPLETV,
            AIRPORT,
            XSERVE,
            UNKNOWN
        }
    }

    public AfpManager(FreeboxOsSession session, UriBuilder uriBuilder) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, ConfigResponse.class, uriBuilder.path(AFP_PATH), null);
    }

    public boolean getStatus() throws FreeboxException {
        return getConfig().enabled;
    }

    public boolean setStatus(boolean enabled) throws FreeboxException {
        Afp config = getConfig();
        Afp newConfig = new Afp(enabled, config.guestAllow, config.serverType, config.loginName, config.loginPassword);
        return setConfig(newConfig).enabled;
    }
}
