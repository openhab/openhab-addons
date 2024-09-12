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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link FtpManager} is the Java class used to handle api requests related to ftp
 *
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FtpManager extends ConfigurableRest<FtpManager.Config, FtpManager.ConfigResponse> {
    private static final String PATH = "ftp";

    protected static class ConfigResponse extends Response<Config> {
    }

    protected static record Config(boolean enabled, boolean allowAnonymous, boolean allowAnonymousWrite,
            boolean allowRemoteAccess, boolean weakPassword, int portCtrl, int portData, String remoteDomain) {
    }

    public FtpManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, ConfigResponse.class, session.getUriBuilder().path(PATH),
                CONFIG_PATH);
    }

    public boolean getStatus() throws FreeboxException {
        return getConfig().enabled();
    }

    public boolean setStatus(boolean enabled) throws FreeboxException {
        Config oldConfig = getConfig();
        Config newConfig = new Config(enabled, oldConfig.allowAnonymous, oldConfig.allowAnonymousWrite,
                oldConfig.allowRemoteAccess, oldConfig.weakPassword, oldConfig.portCtrl, oldConfig.portData,
                oldConfig.remoteDomain);
        return setConfig(newConfig).enabled();
    }
}
