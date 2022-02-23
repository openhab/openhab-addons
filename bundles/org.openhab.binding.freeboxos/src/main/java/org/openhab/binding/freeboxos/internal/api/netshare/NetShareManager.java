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
package org.openhab.binding.freeboxos.internal.api.netshare;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.netshare.SambaConfig.SambaConfigResponse;
import org.openhab.binding.freeboxos.internal.api.rest.ActivableRest;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.rest.RestManager;

/**
 * The {@link NetShareManager} is the Java class used to handle api requests
 * related to network shares
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NetShareManager extends RestManager {
    private static final String NETSHARE_SUB_PATH = "netshare";

    public class SambaManager extends ActivableRest<SambaConfig, SambaConfigResponse> {
        private static final String SAMBA_SUB_PATH = "samba";

        public SambaManager(FreeboxOsSession session, UriBuilder uriBuilder) {
            super(session, SambaConfigResponse.class, uriBuilder, SAMBA_SUB_PATH, null);
        }
    }

    public NetShareManager(FreeboxOsSession session) {
        super(session, NETSHARE_SUB_PATH);
        session.addManager(SambaManager.class, new SambaManager(session, getUriBuilder()));

        // TODO : on pourra ajouter la gestion des partages Mac OS
    }
}
