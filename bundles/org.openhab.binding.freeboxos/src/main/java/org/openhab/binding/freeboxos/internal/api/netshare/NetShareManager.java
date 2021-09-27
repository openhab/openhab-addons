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
package org.openhab.binding.freeboxos.internal.api.netshare;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ConfigurableRest;
import org.openhab.binding.freeboxos.internal.api.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.netshare.SambaConfig.SambaConfigResponse;

/**
 * The {@link NetShareManager} is the Java class used to handle api requests
 * related to network shares
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NetShareManager extends ConfigurableRest<SambaConfig, SambaConfigResponse> {
    private static final String NETSHARE_SUB_PATH = "netshare";
    private static final String SAMBA_SUB_PATH = "samba";

    public NetShareManager(FreeboxOsSession session) {
        super(NETSHARE_SUB_PATH, SAMBA_SUB_PATH, session, SambaConfigResponse.class);
    }
}
