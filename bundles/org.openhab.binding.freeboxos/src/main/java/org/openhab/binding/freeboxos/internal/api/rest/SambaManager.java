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
 * The {@link SambaManager} is the Java class used to handle api requests related to Samba shares
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SambaManager extends ConfigurableRest<SambaManager.Samba, SambaManager.ConfigResponse> {
    private static final String PATH = "samba";

    protected static class ConfigResponse extends Response<Samba> {
    }

    public static record Samba(boolean fileShareEnabled, boolean printShareEnabled, boolean logonEnabled,
            @Nullable String logonUser, @Nullable String logonPassword, @Nullable String workgroup,
            boolean smbv2Enabled) {
    }

    public SambaManager(FreeboxOsSession session, UriBuilder uriBuilder) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, ConfigResponse.class, uriBuilder.path(PATH), null);
    }

    public boolean setFileShare(boolean enable) throws FreeboxException {
        Samba config = getConfig();
        Samba newConfig = new Samba(enable, config.printShareEnabled, config.logonEnabled, config.logonUser,
                config.logonPassword, config.workgroup, config.smbv2Enabled);
        return setConfig(newConfig).fileShareEnabled();
    }

    public boolean setPrintShare(boolean enable) throws FreeboxException {
        Samba config = getConfig();
        Samba newConfig = new Samba(config.fileShareEnabled, enable, config.logonEnabled, config.logonUser,
                config.logonPassword, config.workgroup, config.smbv2Enabled);
        return setConfig(newConfig).printShareEnabled();
    }
}
