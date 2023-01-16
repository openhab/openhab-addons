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
package org.openhab.binding.freeboxos.internal.api.netshare.samba;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.SAMBA_SUB_PATH;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.rest.ConfigurableRest;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;

/**
 * The {@link SambaManager} is the Java class used to handle api requests related to Samba shares
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SambaManager extends ConfigurableRest<SambaConfig, SambaManager.ConfigResponse> {
    public static class ConfigResponse extends Response<SambaConfig> {
    }

    public SambaManager(FreeboxOsSession session, UriBuilder uriBuilder) throws FreeboxException {
        super(session, Permission.NONE, ConfigResponse.class, uriBuilder.path(SAMBA_SUB_PATH), null);
    }
}
