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
package org.openhab.binding.freeboxos.internal.api.upnpav;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.rest.ConfigurableRest;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;

/**
 * The {@link UPnPAVManager} is the Java class used to handle api requests related to UPnP AV
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class UPnPAVManager extends ConfigurableRest<UPnPAVConfig, UPnPAVManager.ConfigResponse> {
    public static class ConfigResponse extends Response<UPnPAVConfig> {
    }

    public UPnPAVManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.NONE, ConfigResponse.class, session.getUriBuilder().path(UPNPAV_URL),
                CONFIG_SUB_PATH);
    }

    public boolean getStatus() throws FreeboxException {
        return getConfig().isEnabled();
    }

    public boolean setStatus(boolean enabled) throws FreeboxException {
        UPnPAVConfig config = getConfig();
        config.setEnabled(enabled);
        return setConfig(config).isEnabled();
    }
}
